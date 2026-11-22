package com.voicebubble

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

// Windows versions of the phone's picture helpers for skin codes.

private fun scaled(src: BufferedImage, px: Int, type: Int): BufferedImage {
    val out = BufferedImage(px, px, type)
    val g = out.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.drawImage(src, 0, 0, px, px, null); g.dispose()
    return out
}

fun smallJpeg(file: File): ByteArray? {
    val img = runCatching { ImageIO.read(file) }.getOrNull() ?: return null
    return ByteArrayOutputStream().also { ImageIO.write(scaled(img, 128, BufferedImage.TYPE_INT_RGB), "jpg", it) }.toByteArray()
}

fun savePicture(bytes: ByteArray, file: File): Boolean {
    val img = runCatching { ImageIO.read(bytes.inputStream()) }.getOrNull() ?: return false
    return ImageIO.write(scaled(img, 320, BufferedImage.TYPE_INT_ARGB), "png", file)
}
