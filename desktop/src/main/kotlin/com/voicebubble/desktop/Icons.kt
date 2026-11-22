package com.voicebubble.desktop

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** The phone app's line icons (res/drawable/ui_*.xml), drawn in any colour and size. */
object Icons {
    private class P(val path: Path2D.Double, val fill: Boolean, val stroke: Float)
    private class Vec(val vw: Double, val vh: Double, val paths: List<P>)

    private val cache = HashMap<String, Vec?>()
    private val images = HashMap<String, BufferedImage>()

    private fun load(name: String): Vec? = cache.getOrPut(name) {
        runCatching {
            val stream = Icons::class.java.classLoader.getResourceAsStream("drawable/$name.xml") ?: return@runCatching null
            val doc = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = false }.newDocumentBuilder().parse(stream)
            val root = doc.documentElement
            val vw = root.getAttribute("android:viewportWidth").toDoubleOrNull() ?: 24.0
            val vh = root.getAttribute("android:viewportHeight").toDoubleOrNull() ?: 24.0
            val nodes = doc.getElementsByTagName("path")
            val paths = (0 until nodes.length).map { i ->
                val e = nodes.item(i) as org.w3c.dom.Element
                val fill = e.getAttribute("android:fillColor").let { it.isNotEmpty() && !it.startsWith("#00") }
                val sw = e.getAttribute("android:strokeWidth").toFloatOrNull() ?: 0f
                val stroke = e.getAttribute("android:strokeColor").let { it.isNotEmpty() && !it.startsWith("#00") }
                P(parse(e.getAttribute("android:pathData")), fill, if (stroke) sw else 0f)
            }
            Vec(vw, vh, paths)
        }.getOrNull()
    }

    fun paint(g0: Graphics2D, name: String, x: Int, y: Int, size: Int, color: Color) {
        val v = load(name) ?: return
        val g = g0.create() as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        g.translate(x, y)
        val k = size / v.vw
        g.scale(k, size / v.vh)
        g.color = color
        for (p in v.paths) {
            if (p.fill) g.fill(p.path)
            if (p.stroke > 0) { g.stroke = BasicStroke(p.stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); g.draw(p.path) }
        }
        g.dispose()
    }

    fun image(name: String, size: Int, color: Color): BufferedImage = images.getOrPut("$name/$size/${color.rgb}") {
        BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB).also { img ->
            val g = img.createGraphics(); paint(g, name, 0, 0, size, color); g.dispose()
        }
    }

    // ---------------- full SVG path syntax ----------------

    private val tok = Regex("[MmLlHhVvCcSsQqTtAaZz]|[-+]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE][-+]?\\d+)?")

    fun parse(d: String): Path2D.Double {
        val t = tok.findAll(d).map { it.value }.toMutableList()
        val p = Path2D.Double()
        var i = 0; var cmd = 'M'
        var x = 0.0; var y = 0.0; var sx = 0.0; var sy = 0.0
        var cx = 0.0; var cy = 0.0   // last control point (for S / T)
        var lastCmd = ' '
        fun isCmd(s: String) = s[0].isLetter() && s[0] != 'e' && s[0] != 'E'
        fun n(): Double = t[i++].toDouble()
        fun flag(): Boolean {
            // Arc flags can be written together ("a1 1 0 011 1"): read one digit.
            val s = t[i]
            return if (s.length > 1 && (s[0] == '0' || s[0] == '1') && s[1] != '.') { val f = s[0] == '1'; t[i] = s.substring(1); f }
            else { i++; s.toDouble() != 0.0 }
        }
        try {
            while (i < t.size) {
                if (isCmd(t[i])) { cmd = t[i][0]; i++
                    if (cmd == 'Z' || cmd == 'z') { p.closePath(); x = sx; y = sy; lastCmd = 'Z'; continue } }
                val rel = cmd.isLowerCase()
                val ox = if (rel) x else 0.0; val oy = if (rel) y else 0.0
                when (cmd.uppercaseChar()) {
                    'M' -> { x = ox + n(); y = oy + n(); sx = x; sy = y; p.moveTo(x, y); cmd = if (rel) 'l' else 'L' }
                    'L' -> { x = ox + n(); y = oy + n(); p.lineTo(x, y) }
                    'H' -> { x = (if (rel) x else 0.0) + n(); p.lineTo(x, y) }
                    'V' -> { y = (if (rel) y else 0.0) + n(); p.lineTo(x, y) }
                    'C' -> { val x1 = ox + n(); val y1 = oy + n(); val x2 = ox + n(); val y2 = oy + n(); x = ox + n(); y = oy + n()
                        p.curveTo(x1, y1, x2, y2, x, y); cx = x2; cy = y2 }
                    'S' -> { val (x1, y1) = if (lastCmd.uppercaseChar() in "CS") (2 * x - cx) to (2 * y - cy) else x to y
                        val x2 = ox + n(); val y2 = oy + n(); x = ox + n(); y = oy + n(); p.curveTo(x1, y1, x2, y2, x, y); cx = x2; cy = y2 }
                    'Q' -> { val x1 = ox + n(); val y1 = oy + n(); x = ox + n(); y = oy + n(); p.quadTo(x1, y1, x, y); cx = x1; cy = y1 }
                    'T' -> { val (x1, y1) = if (lastCmd.uppercaseChar() in "QT") (2 * x - cx) to (2 * y - cy) else x to y
                        x = ox + n(); y = oy + n(); p.quadTo(x1, y1, x, y); cx = x1; cy = y1 }
                    'A' -> { val rx = n(); val ry = n(); val rot = n(); val large = flag(); val sweep = flag()
                        val nx = ox + n(); val ny = oy + n(); arc(p, x, y, rx, ry, rot, large, sweep, nx, ny); x = nx; y = ny }
                    else -> i++
                }
                lastCmd = cmd
            }
        } catch (e: Exception) { }
        return p
    }

    /** SVG's arc (from its two ends) as Java's arc (from its centre). */
    private fun arc(p: Path2D.Double, x0: Double, y0: Double, rx0: Double, ry0: Double, angle: Double,
                    large: Boolean, sweep: Boolean, x: Double, y: Double) {
        if (rx0 == 0.0 || ry0 == 0.0) { p.lineTo(x, y); return }
        val dx2 = (x0 - x) / 2; val dy2 = (y0 - y) / 2
        val a = Math.toRadians(angle % 360)
        val ca = cos(a); val sa = sin(a)
        val x1 = ca * dx2 + sa * dy2; val y1 = -sa * dx2 + ca * dy2
        var rx = abs(rx0); var ry = abs(ry0)
        val check = x1 * x1 / (rx * rx) + y1 * y1 / (ry * ry)
        if (check > 1) { rx *= sqrt(check); ry *= sqrt(check) }
        val sign = if (large == sweep) -1 else 1
        var sq = ((rx * rx * ry * ry) - (rx * rx * y1 * y1) - (ry * ry * x1 * x1)) / ((rx * rx * y1 * y1) + (ry * ry * x1 * x1))
        if (sq < 0) sq = 0.0
        val coef = sign * sqrt(sq)
        val cx1 = coef * (rx * y1 / ry); val cy1 = coef * -(ry * x1 / rx)
        val cx = (x0 + x) / 2 + (ca * cx1 - sa * cy1); val cy = (y0 + y) / 2 + (sa * cx1 + ca * cy1)
        val ux = (x1 - cx1) / rx; val uy = (y1 - cy1) / ry
        val vx = (-x1 - cx1) / rx; val vy = (-y1 - cy1) / ry
        var start = Math.toDegrees(atan2(uy, ux))
        var extent = Math.toDegrees(atan2(ux * vy - uy * vx, ux * vx + uy * vy))
        if (!sweep && extent > 0) extent -= 360.0 else if (sweep && extent < 0) extent += 360.0
        extent %= 360.0; start %= 360.0
        val arc = Arc2D.Double(cx - rx, cy - ry, rx * 2, ry * 2, -start, -extent, Arc2D.OPEN)
        val tr = AffineTransform.getRotateInstance(a, cx, cy)
        p.append(tr.createTransformedShape(arc).getPathIterator(null), true)
    }

    @Suppress("unused") private const val TAU = 2 * PI
}
