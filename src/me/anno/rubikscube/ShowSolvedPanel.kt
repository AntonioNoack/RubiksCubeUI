package me.anno.rubikscube

import me.anno.Time
import me.anno.ecs.EntityQuery.getComponent
import me.anno.fonts.keys.TextCacheKey
import me.anno.gpu.drawing.DrawRectangles
import me.anno.gpu.drawing.DrawTexts
import me.anno.maths.Maths
import me.anno.ui.Panel
import me.anno.ui.Style
import me.anno.ui.base.components.AxisAlignment
import me.anno.utils.Color
import me.anno.utils.Color.mixARGB2
import me.anno.utils.Color.withAlpha

/**
 * Shows that you solved it, and need to shuffle it to play again.
 * */
class ShowSolvedPanel(style: Style) : Panel(style) {
    private val smallFont = style.getFont("text")
    private val bigFont = smallFont.run { withSize(size * 5f) }
    private val smallKey = TextCacheKey("Click to shuffle", smallFont)
    private val bigKey = TextCacheKey("Solved!", bigFont)
    private var state = 0.0
    override fun isOpaqueAt(x: Int, y: Int): Boolean = false // don't catch UI
    override fun onUpdate() {
        super.onUpdate()
        val state0 = scene.getComponent(CubeController::class)!!.isSolved
        state = Maths.mix(if (state0) 0.7 else 0.0, state, Maths.dtTo01(Time.deltaTime))
    }

    override fun onDraw(x0: Int, y0: Int, x1: Int, y1: Int) {
        if (state > 1.0 / 255.0) {
            val bar = bigFont.sizeInt * 4 / 3
            DrawRectangles.drawRect(x, y + (height - bar) / 2, width, bar, Color.black.withAlpha(state.toFloat()))
            DrawTexts.drawText(
                x + width / 2, y + height / 2 - smallFont.sizeInt / 2, bigFont, bigKey,
                Color.white, Color.black.withAlpha(0),
                AxisAlignment.CENTER, AxisAlignment.CENTER
            )
            DrawTexts.drawText(
                x + width / 2, y + height / 2 + bigFont.sizeInt / 2, smallFont, smallKey,
                mixARGB2(Color.white, Color.black, 0.3f), Color.black.withAlpha(0),
                AxisAlignment.CENTER, AxisAlignment.CENTER
            )
        }
    }
}