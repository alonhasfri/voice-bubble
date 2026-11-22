package android.content

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Android's settings store, the same calls, kept as a small JSON file. */
interface SharedPreferences {
    fun getString(key: String, def: String?): String?
    fun getStringSet(key: String, def: Set<String>?): Set<String>?
    fun getInt(key: String, def: Int): Int
    fun getLong(key: String, def: Long): Long
    fun getFloat(key: String, def: Float): Float
    fun getBoolean(key: String, def: Boolean): Boolean
    fun contains(key: String): Boolean
    val all: Map<String, *>
    fun edit(): Editor

    interface Editor {
        fun putString(key: String, v: String?): Editor
        fun putStringSet(key: String, v: Set<String>?): Editor
        fun putInt(key: String, v: Int): Editor
        fun putLong(key: String, v: Long): Editor
        fun putFloat(key: String, v: Float): Editor
        fun putBoolean(key: String, v: Boolean): Editor
        fun remove(key: String): Editor
        fun clear(): Editor
        fun apply()
        fun commit(): Boolean
    }
}

class FileSharedPreferences(private val file: File) : SharedPreferences {
    private val map = HashMap<String, Any?>()

    init {
        runCatching {
            val o = JSONObject(file.readText())
            for (k in o.keys()) {
                val v = o.get(k)
                map[k] = if (v is JSONArray) (0 until v.length()).map { v.getString(it) }.toSet() else v
            }
        }
    }

    @Synchronized override fun getString(key: String, def: String?) = map[key] as? String ?: def
    @Suppress("UNCHECKED_CAST")
    @Synchronized override fun getStringSet(key: String, def: Set<String>?) = map[key] as? Set<String> ?: def
    @Synchronized override fun getInt(key: String, def: Int) = (map[key] as? Number)?.toInt() ?: def
    @Synchronized override fun getLong(key: String, def: Long) = (map[key] as? Number)?.toLong() ?: def
    @Synchronized override fun getFloat(key: String, def: Float) = (map[key] as? Number)?.toFloat() ?: def
    @Synchronized override fun getBoolean(key: String, def: Boolean) = map[key] as? Boolean ?: def
    @Synchronized override fun contains(key: String) = map.containsKey(key)
    override val all: Map<String, *> @Synchronized get() = HashMap(map)

    @Synchronized private fun save() {
        val o = JSONObject()
        for ((k, v) in map) o.put(k, if (v is Set<*>) JSONArray(v.toList()) else v)
        file.parentFile?.mkdirs()
        val tmp = File(file.path + ".tmp")
        tmp.writeText(o.toString())
        if (!tmp.renameTo(file)) { file.delete(); tmp.renameTo(file) }
    }

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val changes = ArrayList<Pair<String, Any?>>()
        private var clearAll = false
        private val REMOVE = Any()
        override fun putString(key: String, v: String?) = apply { changes += key to (v ?: REMOVE) }
        override fun putStringSet(key: String, v: Set<String>?) = apply { changes += key to (v?.toSet() ?: REMOVE) }
        override fun putInt(key: String, v: Int) = apply { changes += key to v }
        override fun putLong(key: String, v: Long) = apply { changes += key to v }
        override fun putFloat(key: String, v: Float) = apply { changes += key to v }
        override fun putBoolean(key: String, v: Boolean) = apply { changes += key to v }
        override fun remove(key: String) = apply { changes += key to REMOVE }
        override fun clear() = apply { clearAll = true }
        override fun apply() { commit() }
        override fun commit(): Boolean {
            synchronized(this@FileSharedPreferences) {
                if (clearAll) map.clear()
                for ((k, v) in changes) if (v === REMOVE) map.remove(k) else map[k] = v
                runCatching { save() }
            }
            return true
        }
    }
}
