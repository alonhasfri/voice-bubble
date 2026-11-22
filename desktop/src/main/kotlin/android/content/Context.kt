package android.content

import android.content.res.Resources
import java.io.File

/**
 * Windows stand-in for Android's Context: just what Voice Bubble's shared code uses
 * (settings and the app's folder). Everything is kept in %APPDATA%\VoiceBubble.
 */
open class Context(val filesDir: File) {
    val resources = Resources()
    /** The app's bundled files (skins, word packs), from inside the program. */
    val assets = Assets()
    private val stores = HashMap<String, SharedPreferences>()

    fun getSharedPreferences(name: String, mode: Int): SharedPreferences = synchronized(stores) {
        stores.getOrPut(name) { FileSharedPreferences(File(filesDir, "settings/$name.json")) }
    }

    companion object { const val MODE_PRIVATE = 0 }
}

class Assets {
    fun open(name: String): java.io.InputStream =
        Assets::class.java.classLoader.getResourceAsStream(name) ?: throw java.io.FileNotFoundException(name)
}
