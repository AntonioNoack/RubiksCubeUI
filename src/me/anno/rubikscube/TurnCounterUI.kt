package me.anno.rubikscube

import me.anno.ecs.EntityQuery.getComponent
import me.anno.fonts.FontManager
import me.anno.fonts.keys.TextCacheKey
import me.anno.gpu.drawing.DrawRectangles
import me.anno.gpu.drawing.DrawTexts
import me.anno.gpu.drawing.GFXx2D.getSizeX
import me.anno.ui.Panel
import me.anno.ui.Style
import me.anno.ui.base.components.AxisAlignment
import me.anno.utils.Color
import me.anno.utils.Color.withAlpha

/**
 * Shows how many turns you took in the bottom right corner.
 * */
class TurnCounterUI(style: Style) : Panel(style) {
    private val font = style.getFont("text")
    override fun isOpaqueAt(x: Int, y: Int): Boolean = false // don't catch UI
    override fun onDraw(x0: Int, y0: Int, x1: Int, y1: Int) {
        val controller = scene.getComponent(CubeController::class)!!
        val tck = TextCacheKey("Turns: ${controller.turnCounter}", font)
        val size = getSizeX(FontManager.getSize(tck, false))
        val drawnWidth = size + font.sizeInt / 3
        val drawnHeight = font.sizeInt * 4 / 3
        val padding = 2
        DrawRectangles.drawRect(
            x + width - drawnWidth - padding, y + height - drawnHeight - padding,
            drawnWidth, drawnHeight, Color.black.withAlpha(0.6f)
        )
        DrawTexts.drawText(
            x + width - drawnWidth / 2 - padding, y + height - drawnHeight / 2 - padding, font, tck,
            Color.white, Color.black.withAlpha(0), AxisAlignment.CENTER, AxisAlignment.CENTER
        )
    }
}