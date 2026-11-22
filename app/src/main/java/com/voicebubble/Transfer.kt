package com.voicebubble

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Moving everything from the phone to the computer (shared by both apps).
 *
 * The phone shows a short code (like K7QD-2XMB-9FHT-W3) made of its Wi-Fi address and a random secret. The computer
 * types it, connects straight to the phone on the same Wi-Fi and downloads the data, encrypted with that secret.
 * Nothing goes through the internet. There is also a long code (all the data in the code itself) for when the two
 * can't reach each other.
 */
object Transfer {
    const val PORT = 47614

    /** Settings stores that are moved, and the per-device settings that are not. */
    private val stores = listOf("voice", "words", "characters", "stats", "adapt", "learn", "skins")
    private val deviceOnly = Regex("^(model|liveModel|engine|threads_.*|bubble[XY].*|bubbleX.*|bubbleY.*|keyboard.*|dock.*|bubbleSize.*|liveStatus|onlyWithKeyboard|.*_l)$")

    // ---------------- the data ----------------

    /** Everything worth moving, as JSON. */
    fun export(c: Context): JSONObject {
        val out = JSONObject().put("v", 1)
        val st = JSONObject()
        for (name in stores) {
            val o = JSONObject()
            for ((k, v) in c.getSharedPreferences(name, Context.MODE_PRIVATE).all) {
                if (name == "voice" && deviceOnly.matches(k)) continue
                val e = when (v) {
                    is String -> JSONObject().put("t", "s").put("v", v)
                    is Boolean -> JSONObject().put("t", "b").put("v", v)
                    is Int -> JSONObject().put("t", "i").put("v", v)
                    is Long -> JSONObject().put("t", "l").put("v", v)
                    is Float -> JSONObject().put("t", "f").put("v", v.toDouble())
                    is Set<*> -> JSONObject().put("t", "set").put("v", JSONArray(v.map { it.toString() }))
                    else -> null
                } ?: continue
                o.put(k, e)
            }
            st.put(name, o)
        }
        out.put("stores", st)
        // Your own skin pictures.
        val pics = JSONObject()
        File(c.filesDir, "skins").listFiles()?.filter { it.isFile && it.length() < 600_000 }?.forEach { f ->
            pics.put(f.name, java.util.Base64.getEncoder().encodeToString(f.readBytes()))
        }
        out.put("files", pics)
        return out
    }

    /** Puts the data in this device's settings. Returns how many settings came over. */
    fun import(c: Context, data: JSONObject): Int {
        var n = 0
        val st = data.optJSONObject("stores") ?: return 0
        for (name in st.keys()) {
            if (name !in stores) continue
            val o = st.getJSONObject(name)
            val ed = c.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
            for (k in o.keys()) {
                if (name == "voice" && deviceOnly.matches(k)) continue
                val e = o.optJSONObject(k) ?: continue
                when (e.optString("t")) {
                    "s" -> ed.putString(k, e.optString("v"))
                    "b" -> ed.putBoolean(k, e.optBoolean("v"))
                    "i" -> ed.putInt(k, e.optInt("v"))
                    "l" -> ed.putLong(k, e.optLong("v"))
                    "f" -> ed.putFloat(k, e.optDouble("v").toFloat())
                    "set" -> ed.putStringSet(k, e.optJSONArray("v")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: emptySet())
                    else -> continue
                }
                n++
            }
            ed.apply()
        }
        data.optJSONObject("files")?.let { pics ->
            val dir = File(c.filesDir, "skins").apply { mkdirs() }
            for (name in pics.keys()) {
                if (!name.matches(Regex("[A-Za-z0-9_.-]+"))) continue
                runCatching { File(dir, name).writeBytes(java.util.Base64.getDecoder().decode(pics.getString(name))) }
            }
        }
        return n
    }

    // ---------------- packing ----------------

    fun gzip(b: ByteArray): ByteArray = ByteArrayOutputStream().also { o -> GZIPOutputStream(o).use { it.write(b) } }.toByteArray()
    fun gunzip(b: ByteArray): ByteArray = GZIPInputStream(ByteArrayInputStream(b)).use { it.readBytes() }

    private fun key(secret: ByteArray) = SecretKeySpec(MessageDigest.getInstance("SHA-256").digest("voice-bubble-transfer".toByteArray() + secret), "AES")

    fun encrypt(plain: ByteArray, secret: ByteArray): ByteArray {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, key(secret), GCMParameterSpec(128, iv))
        return iv + c.doFinal(plain)
    }

    fun decrypt(data: ByteArray, secret: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, key(secret), GCMParameterSpec(128, data.copyOfRange(0, 12)))
        return c.doFinal(data, 12, data.size - 12)
    }

    // ---------------- the short code ----------------

    private const val ABC = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"   // no I, L, O, U: easy to type

    class Code(val ip: ByteArray, val secret: ByteArray) {
        val host get() = ip.joinToString(".") { (it.toInt() and 0xFF).toString() }
        /** 4 address bytes + 6 secret bytes = 80 bits = 16 letters, in groups of 4. */
        val text: String get() {
            val bytes = ip + secret
            var bits = java.math.BigInteger(1, bytes)
            val sb = StringBuilder()
            repeat(16) { sb.append(ABC[bits.and(java.math.BigInteger.valueOf(31)).toInt()]); bits = bits.shiftRight(5) }
            return sb.reverse().chunked(4).joinToString("-")
        }
    }

    fun newCode(ip: ByteArray) = Code(ip, ByteArray(6).also { SecureRandom().nextBytes(it) })

    /** Reads a typed code (any case, dashes and spaces ignored; O→0, I/L→1). Null when it isn't one. */
    fun parse(typed: String): Code? {
        val s = typed.uppercase().replace('O', '0').replace('I', '1').replace('L', '1').filter { it.isLetterOrDigit() }
        if (s.length != 16 || s.any { it !in ABC }) return null
        var bits = java.math.BigInteger.ZERO
        for (ch in s) bits = bits.shiftLeft(5).or(java.math.BigInteger.valueOf(ABC.indexOf(ch).toLong()))
        val raw = bits.toByteArray().let { b -> if (b.size >= 10) b.copyOfRange(b.size - 10, b.size) else ByteArray(10 - b.size) + b }
        return Code(raw.copyOfRange(0, 4), raw.copyOfRange(4, 10))
    }

    /** This device's address on the local network (Wi-Fi, hotspot or USB), or null. */
    fun localIp(): ByteArray? = runCatching {
        NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.isUp && !it.isLoopback }
            .sortedBy { n -> if (n.name.startsWith("wlan")) 0 else if (n.name.startsWith("rndis") || n.name.startsWith("usb") || n.name.startsWith("ap")) 1 else 2 }
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { it is Inet4Address && it.isSiteLocalAddress }?.address
    }.getOrNull()

    // ---------------- the long code (everything in the text) ----------------

    const val LONG_PREFIX = "VB1:"

    fun longCode(c: Context): String = LONG_PREFIX + java.util.Base64.getEncoder().encodeToString(gzip(export(c).toString().toByteArray()))

    fun readLongCode(code: String): JSONObject? = runCatching {
        val b64 = code.trim().removePrefix(LONG_PREFIX).filter { !it.isWhitespace() }
        JSONObject(String(gunzip(java.util.Base64.getDecoder().decode(b64)), Charsets.UTF_8))
    }.getOrNull()

    // ---------------- sending (phone) and receiving (computer) ----------------

    /**
     * Waits for the computer and sends it the data (encrypted with the code's secret). Runs until [stop] or after
     * one successful send. Blocking: run it on its own thread.
     */
    fun serve(c: Context, code: Code, stop: () -> Boolean, sent: () -> Unit) {
        val server = java.net.ServerSocket(PORT)
        server.soTimeout = 1000
        val body = encrypt(gzip(export(c).toString().toByteArray()), code.secret)
        val path = "/" + hex(MessageDigest.getInstance("SHA-256").digest(code.secret)).take(16)
        server.use {
            while (!stop()) {
                val s = try { server.accept() } catch (e: java.net.SocketTimeoutException) { continue }
                s.use { sock ->
                    sock.soTimeout = 5000
                    val req = sock.getInputStream().bufferedReader().readLine() ?: return@use
                    val out = sock.getOutputStream()
                    if (req.startsWith("GET $path ")) {
                        out.write("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: ${body.size}\r\nConnection: close\r\n\r\n".toByteArray())
                        out.write(body); out.flush()
                        sent()
                        return
                    } else out.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
                }
            }
        }
    }

    /** The computer's side: fetches the data from the phone. Throws with a readable message when it can't. */
    fun receive(code: Code): JSONObject {
        val path = "/" + hex(MessageDigest.getInstance("SHA-256").digest(code.secret)).take(16)
        val conn = java.net.URL("http://${code.host}:$PORT$path").openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 6000; conn.readTimeout = 20000
        val bytes = try {
            if (conn.responseCode != 200) throw IllegalStateException("The phone didn't accept that code. Check it and try again")
            conn.inputStream.use { it.readBytes() }
        } catch (e: java.io.IOException) {
            throw IllegalStateException("Couldn't reach your phone. Keep the transfer screen open on the phone, and have both on the same Wi-Fi. Or use the long code")
        }
        val plain = try { decrypt(bytes, code.secret) } catch (e: Exception) { throw IllegalStateException("That code doesn't match. Check it and try again") }
        return JSONObject(String(gunzip(plain), Charsets.UTF_8))
    }

    private fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }
}
