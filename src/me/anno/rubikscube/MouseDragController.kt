package me.anno.rubikscube

import me.anno.ecs.Component
import me.anno.ecs.Entity
import me.anno.ecs.EntityQuery.getComponent
import me.anno.ecs.interfaces.InputListener
import me.anno.engine.raycast.RayQuery
import me.anno.engine.raycast.Raycast
import me.anno.input.Input
import me.anno.input.Key

/**
 * Implements mouse-drag-based controls
 * */
class MouseDragController : Component(), InputListener {

    private var mouseDownCube: Entity? = null

    private fun getCubeController(): CubeController {
        return getComponent(CubeController::class)!!
    }

    private fun findHitCube(): Entity? {
        val query = RayQuery(
            sceneView.renderer.cameraPosition,
            sceneView.renderer.getMouseRayDirection(),
            1e3
        )
        return if (Raycast.raycastClosestHit(scene, query)) {
            query.result.component?.entity
        } else null
    }

    override fun onKeyDown(key: Key): Boolean {
        if (key == Key.KEY_P) getCubeController().shuffle()
        return if (key == Key.BUTTON_LEFT) {
            mouseDownCube = findHitCube()
            true
        } else super.onKeyDown(key)
    }

    override fun onKeyUp(key: Key): Boolean {
        val srcCube = mouseDownCube
        val controller = getCubeController()
        return if (key == Key.BUTTON_LEFT && srcCube != null &&
            (!controller.isSolved || Input.mouseHasMoved)
        ) {
            val dstCube = findHitCube()
            if (dstCube != null) {
                val bestOption = slices
                    .filter { axis -> axis.filter(srcCube) && axis.filter(dstCube) }
                    .flatMap { axis -> listOf(axis to +1, axis to +3) }
                    .map { (axis, times) ->
                        val transform = controller.getTransform(axis, times)
                        val distance = srcCube.transform.globalPosition.rotate(transform)
                            .distance(dstCube.transform.globalPosition)
                        Triple(axis, times, distance)
                    }
                    .minByOrNull { it.third }
                if (bestOption != null) {
                    controller.rotate(slices.indexOf(bestOption.first), bestOption.second, true)
                }
            }
            true
        } else super.onKeyUp(key)
    }

    override fun onMouseClicked(button: Key, long: Boolean): Boolean {
        val controller = getCubeController()
        return if (button == Key.BUTTON_LEFT && !long && controller.isSolved) {
            controller.shuffle()
            true
        } else super.onMouseClicked(button, long)
    }
}