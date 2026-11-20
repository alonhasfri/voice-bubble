package com.voicebubble

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Your own skins: a picture you upload, or a little drawing the AI designs from a description.
 * Saved on the phone; they show up in Settings → Bubble skin next to the built-in ones.
 */
object SkinArt {
    /** What the AI is told: draw on a 100×100 circle with a few simple shapes. */
    const val AI_FORMAT = """You design the look of a small round floating button (a "bubble", about 1.5 cm on a phone screen).
Draw it with vector shapes on a 100×100 canvas; the bubble is the circle centred at (50,50) with radius 50, and
anything outside it is cut off. Fill the circle with the subject, centred and big (it should reach about 8 to 12 units
from the edge). Make it detailed but clear at that small size: 15 to 45 shapes, built in layers from back to front:
big base shapes first, then a darker shade on one side, then lighter highlights (a soft white shine, e.g. "#60FFFFFF"),
then small details (eyes, seeds, sparkles, texture dots). Use a background colour that contrasts with the subject.
Draw it as your own original take on the idea. Check every coordinate is between 0 and 100.
Example (a cute frog), for the format and the level of detail:
{"name":"Frog","background":["#E8F5E9"],"glow":null,"mic":null,"shapes":[{"type":"circle","x":30,"y":30,"r":14,"color":"#66BB6A"},{"type":"circle","x":70,"y":30,"r":14,"color":"#66BB6A"},{"type":"ellipse","x":50,"y":58,"rx":40,"ry":30,"color":"#66BB6A"},{"type":"circle","x":30,"y":30,"r":10,"color":"#FFFFFF"},{"type":"circle","x":70,"y":30,"r":10,"color":"#FFFFFF"},{"type":"circle","x":31,"y":31,"r":6,"color":"#1A1A1A"},{"type":"circle","x":71,"y":31,"r":6,"color":"#1A1A1A"},{"type":"circle","x":33,"y":29,"r":2,"color":"#FFFFFF"},{"type":"circle","x":73,"y":29,"r":2,"color":"#FFFFFF"},{"type":"path","d":"M 26 62 Q 50 80 74 62","color":"#2E7D32","stroke":3},{"type":"circle","x":26,"y":56,"r":4,"color":"#A0F48FB1"},{"type":"circle","x":74,"y":56,"r":4,"color":"#A0F48FB1"}]}
Reply with ONLY a JSON object, no other text:
{
  "name": "short name, 1-3 words",
  "background": ["#RRGGBB", "#RRGGBB"],   // one colour, or two for a diagonal gradient
  "glow": "#RRGGBB" or null,              // soft glow around the bubble
  "mic": "#RRGGBB" or null,               // colour of the microphone drawn in the middle, or null to draw no microphone
  "shapes": [ ... drawn in order, up to 40 ... ]
}
Shapes (colours are "#RRGGBB" or "#AARRGGBB"; add "stroke": width to draw only the outline):
{"type":"circle","x":50,"y":50,"r":20,"color":"#FFFFFF"}
{"type":"ellipse","x":50,"y":50,"rx":30,"ry":15,"color":"#FFFFFF"}
{"type":"rect","x":20,"y":30,"w":60,"h":20,"radius":5,"color":"#FFFFFF"}
{"type":"line","x1":10,"y1":10,"x2":90,"y2":90,"width":4,"color":"#000000"}
{"type":"polygon","points":[[50,10],[90,90],[10,90]],"color":"#FFCC00"}
{"type":"path","d":"M 20 50 Q 50 20 80 50","color":"#000000","stroke":3}   // SVG path: M L H V Q C Z (also lowercase)
{"type":"text","x":50,"y":60,"size":30,"text":"★","color":"#FFFFFF"}     // x,y = centre of the text"""

    class Custom(val id: String, val title: String, val kind: String, val spec: JSONObject?)

    private fun sp(c: Context) = c.getSharedPreferences("skins", Context.MODE_PRIVATE)
    private fun dir(c: Context) = File(c.filesDir, "skins").apply { mkdirs() }
    fun imageFile(c: Context, id: String) = File(dir(c), "$id.png")

    fun list(c: Context): List<Custom> {
        val arr = try { JSONArray(sp(c).getString("list", "[]")) } catch (e: Exception) { JSONArray() }
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            Custom(o.optString("id"), o.optString("title", "My skin"), o.optString("kind"), o.optJSONObject("spec"))
        }
    }

    private fun save(c: Context, items: List<Custom>) {
        val arr = JSONArray()
        items.forEach { arr.put(JSONObject().put("id", it.id).put("title", it.title).put("kind", it.kind).apply { it.spec?.let { s -> put("spec", s) } }) }
        sp(c).edit().putString("list", arr.toString()).apply()
    }

    fun delete(c: Context, id: String) {
        save(c, list(c).filter { it.id != id })
        imageFile(c, id).delete()
    }

    /** Saves a picture as a skin: cut to a square from the middle, 320 px. Returns the new skin's id. */
    fun addPicture(c: Context, uri: Uri): String? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 320) sample *= 2
        val src = c.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val side = minOf(src.width, src.height)
        val square = Bitmap.createBitmap(src, (src.width - side) / 2, (src.height - side) / 2, side, side)
        val out = Bitmap.createScaledBitmap(square, 320, 320, true)
        val id = "pic_" + System.currentTimeMillis()
        imageFile(c, id).outputStream().use { out.compress(Bitmap.CompressFormat.PNG, 100, it) }
        save(c, list(c) + Custom(id, "My picture", "image", null))
        return id
    }

    /** Reads the AI's answer; null when it isn't a usable drawing (or too bare to look like anything). */
    fun parseSpec(answer: String, minShapes: Int = 0): JSONObject? {
        val start = answer.indexOf('{')
        val end = answer.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        // Some AIs leave // comments in; take them out.
        val json = answer.substring(start, end + 1).lines().joinToString("\n") { line ->
            val i = line.indexOf("//")
            if (i >= 0 && line.substring(0, i).count { it == '"' } % 2 == 0) line.substring(0, i) else line
        }
            // AIs often leave a comma before } or ]: allowed here.
            .replace(Regex(",\\s*([}\\]])"), "$1")
        return try {
            JSONObject(json).takeIf { (it.has("background") || it.has("shapes")) && (it.optJSONArray("shapes")?.length() ?: 0) >= minShapes }
        } catch (e: Exception) { null }
    }

    fun addDesign(c: Context, spec: JSONObject): String {
        val id = "ai_" + System.currentTimeMillis()
        val name = spec.optString("name").trim().take(24).ifEmpty { "AI skin" }
        save(c, list(c) + Custom(id, name, "ai", spec))
        return id
    }

    // ---------------- the ready-made drawn skins ----------------

    private var builtinCache: List<Custom>? = null

    /** The drawn skins that come with the app (assets/skins_art.json; made with tools/skins/gen.py). */
    fun builtin(c: Context): List<Custom> = builtinCache ?: runCatching {
        val arr = JSONArray(c.assets.open("skins_art.json").bufferedReader().use { it.readText() })
        (0 until arr.length()).map { i -> arr.getJSONObject(i).let { Custom(it.getString("id"), it.getString("name"), "ai", it.getJSONObject("spec")) } }
    }.getOrDefault(emptyList()).also { builtinCache = it }

    private val simpleCache = java.util.WeakHashMap<JSONObject, JSONObject>()

    /**
     * The simple version of a drawing: only its bigger shapes (small dots, thin lines, tiny text and fine texture
     * left out), for the "Simple" skin style.
     */
    fun simple(spec: JSONObject): JSONObject = simpleCache.getOrPut(spec) {
        val src = spec.optJSONArray("shapes") ?: JSONArray()
        fun pick(strict: Boolean): JSONArray {
            val out = JSONArray()
            val k = if (strict) 1.0 else 0.35   // how big a part must be to stay
            for (i in 0 until src.length()) {
                val s = src.optJSONObject(i) ?: continue
                val stroke = s.optDouble("stroke", 0.0)
                val keep = when (s.optString("type")) {
                    "circle" -> s.optDouble("r") >= 3.5 * k && (stroke == 0.0 || stroke >= 2.0 * k)
                    "ellipse" -> s.optDouble("rx") * s.optDouble("ry") >= 20 * k && (stroke == 0.0 || stroke >= 2.0 * k)
                    "rect" -> s.optDouble("w") * s.optDouble("h") >= 50 * k
                    "line" -> s.optDouble("width", 2.0) >= 2.4 * k && Math.hypot(s.optDouble("x2") - s.optDouble("x1"), s.optDouble("y2") - s.optDouble("y1")) >= 14 * k
                    "polygon" -> s.optJSONArray("points")?.let { area(it) >= 25 * k } ?: false
                    "path" -> stroke == 0.0 || stroke >= 1.4 * k
                    "text" -> s.optDouble("size") >= 12 * k
                    else -> false
                }
                if (keep) out.put(s)
            }
            return out
        }
        // Too little left (a drawing made of many small parts): leave out only the tiniest.
        var out = pick(true)
        if (out.length() < 4 || out.length() < src.length() / 5) out = pick(false)
        JSONObject(spec.toString()).put("shapes", out)
    }

    private fun area(pts: JSONArray): Double {
        var a = 0.0
        for (i in 0 until pts.length()) {
            val p = pts.optJSONArray(i) ?: return 0.0
            val q = pts.optJSONArray((i + 1) % pts.length()) ?: return 0.0
            a += p.optDouble(0) * q.optDouble(1) - q.optDouble(0) * p.optDouble(1)
        }
        return Math.abs(a) / 2
    }

    // ---------------- sharing ----------------

    private const val PREFIX = "VBSKIN:"

    private fun pack(o: JSONObject): String {
        val bytes = java.io.ByteArrayOutputStream()
        java.util.zip.GZIPOutputStream(bytes).use { it.write(o.toString().toByteArray()) }
        return PREFIX + java.util.Base64.getEncoder().encodeToString(bytes.toByteArray())
    }

    /** Reads a skin code back into its JSON; null when it isn't one. */
    fun unpack(code: String): JSONObject? = try {
        val body = code.trim().substringAfter(PREFIX, "").filter { !it.isWhitespace() }
        if (body.isEmpty()) null else {
            val raw = java.util.zip.GZIPInputStream(java.util.Base64.getDecoder().decode(body).inputStream()).use { it.readBytes() }
            JSONObject(String(raw))
        }
    } catch (e: Exception) { null }

    /** A code for one of your skins, to paste to a friend (pictures are made small so the code stays short). */
    fun shareCode(c: Context, id: String): String? {
        val cs = list(c).firstOrNull { it.id == id } ?: return null
        val o = JSONObject().put("v", 1).put("n", cs.title).put("t", cs.kind)
        when (cs.kind) {
            "ai" -> o.put("s", cs.spec ?: return null)
            "image" -> {
                val bmp = BitmapFactory.decodeFile(imageFile(c, id).path) ?: return null
                val small = Bitmap.createScaledBitmap(bmp, 128, 128, true)
                val out = java.io.ByteArrayOutputStream()
                small.compress(Bitmap.CompressFormat.JPEG, 80, out)
                o.put("img", java.util.Base64.getEncoder().encodeToString(out.toByteArray()))
            }
            else -> return null
        }
        return pack(o)
    }

    /** Adds the skin in a code you were sent. Returns its id, or null when the code doesn't work. */
    fun importCode(c: Context, code: String): String? {
        val o = unpack(code) ?: return null
        val name = o.optString("n").take(24).ifEmpty { "Shared skin" }
        return when (o.optString("t")) {
            "ai" -> o.optJSONObject("s")?.let { spec -> addDesign(c, spec.put("name", name)) }
            "image" -> {
                val bytes = try { java.util.Base64.getDecoder().decode(o.optString("img")) } catch (e: Exception) { return null }
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
                val id = "pic_" + System.currentTimeMillis()
                imageFile(c, id).outputStream().use { Bitmap.createScaledBitmap(bmp, 320, 320, true).compress(Bitmap.CompressFormat.PNG, 100, it) }
                save(c, list(c) + Custom(id, name, "image", null))
                id
            }
            else -> null
        }
    }

    /** For tests: a skin code made straight from a design. */
    fun codeFor(name: String, spec: JSONObject): String = pack(JSONObject().put("v", 1).put("n", name).put("t", "ai").put("s", spec))

    // ---------------- drawing ----------------

    fun color(v: Any?, fallback: Int): Int = try {
        if (v is String && v.startsWith("#")) Color.parseColor(v) else fallback
    } catch (e: Exception) { fallback }

    // Made on first use (not when this file loads), so the parts that only read text work anywhere.
    private val fill by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val textPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; isFakeBoldText = true } }

    /** Draws the AI's shapes inside a circle at (cx, cy) with radius r. */
    fun drawShapes(cv: Canvas, spec: JSONObject, cx: Float, cy: Float, r: Float) {
        val shapes = spec.optJSONArray("shapes") ?: return
        val k = r / 50f
        fun X(v: Double) = cx + ((v - 50) * k).toFloat()
        fun Y(v: Double) = cy + ((v - 50) * k).toFloat()
        for (i in 0 until minOf(shapes.length(), 60)) {
            val s = shapes.optJSONObject(i) ?: continue
            fill.color = color(s.opt("color"), Color.WHITE)
            val stroke = s.optDouble("stroke", 0.0)
            fill.style = if (stroke > 0) Paint.Style.STROKE else Paint.Style.FILL
            fill.strokeWidth = (stroke * k).toFloat()
            fill.strokeCap = Paint.Cap.ROUND; fill.strokeJoin = Paint.Join.ROUND
            runCatching {
                when (s.optString("type")) {
                    "circle" -> cv.drawCircle(X(s.optDouble("x", 50.0)), Y(s.optDouble("y", 50.0)), (s.optDouble("r", 10.0) * k).toFloat(), fill)
                    "ellipse" -> {
                        val x = s.optDouble("x", 50.0); val y = s.optDouble("y", 50.0)
                        val rx = s.optDouble("rx", 10.0); val ry = s.optDouble("ry", 10.0)
                        cv.drawOval(RectF(X(x - rx), Y(y - ry), X(x + rx), Y(y + ry)), fill)
                    }
                    "rect" -> {
                        val x = s.optDouble("x", 0.0); val y = s.optDouble("y", 0.0)
                        val rad = (s.optDouble("radius", 0.0) * k).toFloat()
                        cv.drawRoundRect(RectF(X(x), Y(y), X(x + s.optDouble("w", 10.0)), Y(y + s.optDouble("h", 10.0))), rad, rad, fill)
                    }
                    "line" -> {
                        fill.style = Paint.Style.STROKE
                        fill.strokeWidth = (s.optDouble("width", if (stroke > 0) stroke else 3.0) * k).toFloat()
                        cv.drawLine(X(s.optDouble("x1")), Y(s.optDouble("y1")), X(s.optDouble("x2")), Y(s.optDouble("y2")), fill)
                    }
                    "polygon" -> {
                        val pts = s.optJSONArray("points") ?: return@runCatching
                        val p = Path()
                        for (j in 0 until pts.length()) {
                            val pt = pts.optJSONArray(j) ?: continue
                            val x = X(pt.optDouble(0)); val y = Y(pt.optDouble(1))
                            if (j == 0) p.moveTo(x, y) else p.lineTo(x, y)
                        }
                        p.close(); cv.drawPath(p, fill)
                    }
                    "path" -> svgPath(s.optString("d"), ::X, ::Y)?.let { cv.drawPath(it, fill) }
                    "text" -> {
                        textPaint.color = fill.color
                        textPaint.textSize = (s.optDouble("size", 20.0) * k).toFloat()
                        val y = Y(s.optDouble("y", 50.0)) - (textPaint.ascent() + textPaint.descent()) / 2
                        cv.drawText(s.optString("text").take(6), X(s.optDouble("x", 50.0)), y, textPaint)
                    }
                }
            }
        }
        fill.style = Paint.Style.FILL
    }

    /** A small SVG path reader: M L H V Q C Z, absolute and relative. */
    fun svgPath(d: String, X: (Double) -> Float, Y: (Double) -> Float): Path? {
        val tokens = Regex("[MLHVQCZmlhvqcz]|-?\\d*\\.?\\d+(?:e-?\\d+)?").findAll(d).map { it.value }.toList()
        if (tokens.isEmpty()) return null
        val p = Path()
        var i = 0
        var cmd = 'M'
        var x = 0.0; var y = 0.0; var sx = 0.0; var sy = 0.0
        fun num(): Double = tokens.getOrNull(i++)?.toDoubleOrNull() ?: throw IllegalStateException()
        try {
            while (i < tokens.size) {
                val t = tokens[i]
                if (t[0].isLetter()) { cmd = t[0]; i++; if (cmd == 'Z' || cmd == 'z') { p.close(); x = sx; y = sy; continue } }
                val rel = cmd.isLowerCase()
                when (cmd.uppercaseChar()) {
                    'M' -> { var nx = num(); var ny = num(); if (rel) { nx += x; ny += y }; x = nx; y = ny; sx = x; sy = y
                        p.moveTo(X(x), Y(y)); cmd = if (rel) 'l' else 'L' }
                    'L' -> { var nx = num(); var ny = num(); if (rel) { nx += x; ny += y }; x = nx; y = ny; p.lineTo(X(x), Y(y)) }
                    'H' -> { var nx = num(); if (rel) nx += x; x = nx; p.lineTo(X(x), Y(y)) }
                    'V' -> { var ny = num(); if (rel) ny += y; y = ny; p.lineTo(X(x), Y(y)) }
                    'Q' -> { var x1 = num(); var y1 = num(); var nx = num(); var ny = num()
                        if (rel) { x1 += x; y1 += y; nx += x; ny += y }
                        p.quadTo(X(x1), Y(y1), X(nx), Y(ny)); x = nx; y = ny }
                    'C' -> { var x1 = num(); var y1 = num(); var x2 = num(); var y2 = num(); var nx = num(); var ny = num()
                        if (rel) { x1 += x; y1 += y; x2 += x; y2 += y; nx += x; ny += y }
                        p.cubicTo(X(x1), Y(y1), X(x2), Y(y2), X(nx), Y(ny)); x = nx; y = ny }
                    else -> i++
                }
            }
        } catch (e: IllegalStateException) { /* draw what was read */ }
        return p
    }
}
