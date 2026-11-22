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
import org.json.JSONObject

// The phone-only half of SkinArt: drawing skins on Android's canvas and saving your pictures.
// (SkinArt.kt itself is shared with the Windows version.)

/** Saves a picture as a skin: cut to a square from the middle, 320 px. Returns the new skin's id. */
fun SkinArt.addPicture(c: Context, uri: Uri): String? {
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
    save(c, list(c) + SkinArt.Custom(id, "My picture", "image", null))
    return id
}

fun SkinArt.color(v: Any?, fallback: Int): Int = try {
    if (v is String && v.startsWith("#")) Color.parseColor(v) else fallback
} catch (e: Exception) { fallback }

// Made on first use (not when this file loads), so the parts that only read text work anywhere.
private val skinFill by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
private val skinText by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; isFakeBoldText = true } }

/** Draws the AI's shapes inside a circle at (cx, cy) with radius r. */
fun SkinArt.drawShapes(cv: Canvas, spec: JSONObject, cx: Float, cy: Float, r: Float) {
    val shapes = spec.optJSONArray("shapes") ?: return
    val k = r / 50f
    fun X(v: Double) = cx + ((v - 50) * k).toFloat()
    fun Y(v: Double) = cy + ((v - 50) * k).toFloat()
    for (i in 0 until minOf(shapes.length(), 60)) {
        val s = shapes.optJSONObject(i) ?: continue
        skinFill.color = color(s.opt("color"), Color.WHITE)
        val stroke = s.optDouble("stroke", 0.0)
        skinFill.style = if (stroke > 0) Paint.Style.STROKE else Paint.Style.FILL
        skinFill.strokeWidth = (stroke * k).toFloat()
        skinFill.strokeCap = Paint.Cap.ROUND; skinFill.strokeJoin = Paint.Join.ROUND
        runCatching {
            when (s.optString("type")) {
                "circle" -> cv.drawCircle(X(s.optDouble("x", 50.0)), Y(s.optDouble("y", 50.0)), (s.optDouble("r", 10.0) * k).toFloat(), skinFill)
                "ellipse" -> {
                    val x = s.optDouble("x", 50.0); val y = s.optDouble("y", 50.0)
                    val rx = s.optDouble("rx", 10.0); val ry = s.optDouble("ry", 10.0)
                    cv.drawOval(RectF(X(x - rx), Y(y - ry), X(x + rx), Y(y + ry)), skinFill)
                }
                "rect" -> {
                    val x = s.optDouble("x", 0.0); val y = s.optDouble("y", 0.0)
                    val rad = (s.optDouble("radius", 0.0) * k).toFloat()
                    cv.drawRoundRect(RectF(X(x), Y(y), X(x + s.optDouble("w", 10.0)), Y(y + s.optDouble("h", 10.0))), rad, rad, skinFill)
                }
                "line" -> {
                    skinFill.style = Paint.Style.STROKE
                    skinFill.strokeWidth = (s.optDouble("width", if (stroke > 0) stroke else 3.0) * k).toFloat()
                    cv.drawLine(X(s.optDouble("x1")), Y(s.optDouble("y1")), X(s.optDouble("x2")), Y(s.optDouble("y2")), skinFill)
                }
                "polygon" -> {
                    val pts = s.optJSONArray("points") ?: return@runCatching
                    val p = Path()
                    for (j in 0 until pts.length()) {
                        val pt = pts.optJSONArray(j) ?: continue
                        val x = X(pt.optDouble(0)); val y = Y(pt.optDouble(1))
                        if (j == 0) p.moveTo(x, y) else p.lineTo(x, y)
                    }
                    p.close(); cv.drawPath(p, skinFill)
                }
                "path" -> svgPath(s.optString("d"), ::X, ::Y)?.let { cv.drawPath(it, skinFill) }
                "text" -> {
                    skinText.color = skinFill.color
                    skinText.textSize = (s.optDouble("size", 20.0) * k).toFloat()
                    val y = Y(s.optDouble("y", 50.0)) - (skinText.ascent() + skinText.descent()) / 2
                    cv.drawText(s.optString("text").take(6), X(s.optDouble("x", 50.0)), y, skinText)
                }
            }
        }
    }
    skinFill.style = Paint.Style.FILL
}

/** A small SVG path reader: M L H V Q C Z, absolute and relative. */
fun SkinArt.svgPath(d: String, X: (Double) -> Float, Y: (Double) -> Float): Path? {
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

/** A skin picture made small (128 px JPEG) for a share code. */
fun smallJpeg(file: java.io.File): ByteArray? {
    val bmp = BitmapFactory.decodeFile(file.path) ?: return null
    val out = java.io.ByteArrayOutputStream()
    Bitmap.createScaledBitmap(bmp, 128, 128, true).compress(Bitmap.CompressFormat.JPEG, 80, out)
    return out.toByteArray()
}

/** A shared picture saved as a skin (320 px PNG). False when it isn't a picture. */
fun savePicture(bytes: ByteArray, file: java.io.File): Boolean {
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return false
    file.outputStream().use { Bitmap.createScaledBitmap(bmp, 320, 320, true).compress(Bitmap.CompressFormat.PNG, 100, it) }
    return true
}
