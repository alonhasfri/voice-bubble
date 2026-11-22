package android.content.res

class Resources { val configuration = Configuration() }

class Configuration {
    /** A desktop screen is always "portrait" here (the phone keeps separate bubble spots per orientation). */
    val orientation = ORIENTATION_PORTRAIT
    companion object { const val ORIENTATION_PORTRAIT = 1; const val ORIENTATION_LANDSCAPE = 2 }
}
