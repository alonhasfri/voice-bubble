package com.voicebubble.desktop

import org.json.JSONObject
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.RoundRectangle2D

/** Draws the phone app's skins (the same JSON drawings) with Windows' graphics. */
object SkinPainter {
    fun color(v: Any?, fallback: Color): Color {
        if (v !is String || !v.startsWith("#")) return fallback
        return runCatching {
            val h = v.substring(1)
            when (h.length) {
                6 -> Color(h.toInt(16))
                8 -> { val n = h.toLong(16); Color(((n shr 16) and 0xFF).toInt(), ((n shr 8) and 0xFF).toInt(), (n and 0xFF).toInt(), ((n shr 24) and 0xFF).toInt()) }
                else -> fallback
            }
        }.getOrDefault(fallback)
    }

    /** The whole bubble: background, glow-free (Windows draws it small), the drawing, clipped to a circle. */
    fun paint(g: Graphics2D, spec: JSONObject?, size: Int) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        val circle = Ellipse2D.Double(0.0, 0.0, size.toDouble(), size.toDouble())
        val old = g.clip
        g.clip(circle)
        val bg = spec?.optJSONArray("background")
        val c1 = color(bg?.opt(0) ?: spec?.opt("background"), Color(58, 91, 217))
        val c2 = bg?.opt(1)?.let { color(it, c1) }
        g.paint = if (c2 != null) GradientPaint(0f, 0f, c1, size.toFloat(), size.toFloat(), c2) else c1
        g.fill(circle)
        if (spec != null) shapes(g, spec, size)
        g.clip = old
    }

    fun shapes(g: Graphics2D, spec: JSONObject, size: Int) {
        val arr = spec.optJSONArray("shapes") ?: return
        val k = size / 100.0
        fun X(v: Double) = v * k
        fun Y(v: Double) = v * k
        for (i in 0 until minOf(arr.length(), 60)) {
            val s = arr.optJSONObject(i) ?: continue
            g.color = color(s.opt("color"), Color.WHITE)
            val stroke = s.optDouble("stroke", 0.0)
            g.stroke = BasicStroke((stroke * k).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            fun draw(sh: java.awt.Shape) { if (stroke > 0) g.draw(sh) else g.fill(sh) }
            runCatching {
                when (s.optString("type")) {
                    "circle" -> { val r = s.optDouble("r", 10.0); draw(Ellipse2D.Double(X(s.optDouble("x", 50.0) - r), Y(s.optDouble("y", 50.0) - r), 2 * r * k, 2 * r * k)) }
                    "ellipse" -> { val rx = s.optDouble("rx", 10.0); val ry = s.optDouble("ry", 10.0)
                        draw(Ellipse2D.Double(X(s.optDouble("x", 50.0) - rx), Y(s.optDouble("y", 50.0) - ry), 2 * rx * k, 2 * ry * k)) }
                    "rect" -> { val rad = s.optDouble("radius", 0.0) * k * 2
                        draw(RoundRectangle2D.Double(X(s.optDouble("x", 0.0)), Y(s.optDouble("y", 0.0)), s.optDouble("w", 10.0) * k, s.optDouble("h", 10.0) * k, rad, rad)) }
                    "line" -> {
                        g.stroke = BasicStroke((s.optDouble("width", if (stroke > 0) stroke else 3.0) * k).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                        g.draw(Line2D.Double(X(s.optDouble("x1")), Y(s.optDouble("y1")), X(s.optDouble("x2")), Y(s.optDouble("y2"))))
                    }
                    "polygon" -> {
                        val pts = s.optJSONArray("points") ?: return@runCatching
                        val p = GeneralPath()
                        for (j in 0 until pts.length()) { val pt = pts.optJSONArray(j) ?: continue
                            if (j == 0) p.moveTo(X(pt.optDouble(0)), Y(pt.optDouble(1))) else p.lineTo(X(pt.optDouble(0)), Y(pt.optDouble(1))) }
                        p.closePath(); draw(p)
                    }
                    "path" -> svgPath(s.optString("d"), k)?.let { draw(it) }
                    "text" -> {
                        g.font = Font(Font.SANS_SERIF, Font.BOLD, (s.optDouble("size", 20.0) * k).toInt().coerceAtLeast(1))
                        val t = s.optString("text").take(6)
                        val fm = g.fontMetrics
                        g.drawString(t, (X(s.optDouble("x", 50.0)) - fm.stringWidth(t) / 2.0).toFloat(),
                            (Y(s.optDouble("y", 50.0)) + (fm.ascent - fm.descent) / 2.0).toFloat())
                    }
                }
            }
        }
    }

    /** SVG path: M L H V Q C Z, absolute and relative (the same as the phone). */
    fun svgPath(d: String, k: Double): GeneralPath? {
        val tokens = Regex("[MLHVQCZmlhvqcz]|-?\\d*\\.?\\d+(?:e-?\\d+)?").findAll(d).map { it.value }.toList()
        if (tokens.isEmpty()) return null
        val p = GeneralPath()
        var i = 0; var cmd = 'M'
        var x = 0.0; var y = 0.0; var sx = 0.0; var sy = 0.0
        fun num(): Double = tokens.getOrNull(i++)?.toDoubleOrNull() ?: throw IllegalStateException()
        var started = false
        try {
            while (i < tokens.size) {
                val t = tokens[i]
                if (t[0].isLetter()) { cmd = t[0]; i++; if (cmd == 'Z' || cmd == 'z') { if (started) p.closePath(); x = sx; y = sy; continue } }
                val rel = cmd.isLowerCase()
                when (cmd.uppercaseChar()) {
                    'M' -> { var nx = num(); var ny = num(); if (rel) { nx += x; ny += y }; x = nx; y = ny; sx = x; sy = y
                        p.moveTo(x * k, y * k); started = true; cmd = if (rel) 'l' else 'L' }
                    'L' -> { var nx = num(); var ny = num(); if (rel) { nx += x; ny += y }; x = nx; y = ny; if (started) p.lineTo(x * k, y * k) }
                    'H' -> { var nx = num(); if (rel) nx += x; x = nx; if (started) p.lineTo(x * k, y * k) }
                    'V' -> { var ny = num(); if (rel) ny += y; y = ny; if (started) p.lineTo(x * k, y * k) }
                    'Q' -> { var x1 = num(); var y1 = num(); var nx = num(); var ny = num()
                        if (rel) { x1 += x; y1 += y; nx += x; ny += y }
                        if (started) p.quadTo(x1 * k, y1 * k, nx * k, ny * k); x = nx; y = ny }
                    'C' -> { var x1 = num(); var y1 = num(); var x2 = num(); var y2 = num(); var nx = num(); var ny = num()
                        if (rel) { x1 += x; y1 += y; x2 += x; y2 += y; nx += x; ny += y }
                        if (started) p.curveTo(x1 * k, y1 * k, x2 * k, y2 * k, nx * k, ny * k); x = nx; y = ny }
                    else -> i++
                }
            }
        } catch (e: IllegalStateException) { }
        return if (started) p else null
    }

    /** The microphone drawn in the middle (white, or the skin's mic colour). */
    fun mic(g: Graphics2D, size: Int, col: Color) {
        val k = size / 100.0
        g.color = col
        g.fill(RoundRectangle2D.Double(41 * k, 24 * k, 18 * k, 32 * k, 18 * k, 18 * k))
        g.stroke = BasicStroke((4.5 * k).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val arc = GeneralPath(); arc.moveTo(32 * k, 46 * k); arc.curveTo(32 * k, 68 * k, 68 * k, 68 * k, 68 * k, 46 * k)
        g.draw(arc)
        g.draw(Line2D.Double(50 * k, 62 * k, 50 * k, 74 * k))
        g.draw(Line2D.Double(41 * k, 75 * k, 59 * k, 75 * k))
    }
}
