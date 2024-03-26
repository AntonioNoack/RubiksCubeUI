package me.anno.rubikscube

import me.anno.Time
import me.anno.ecs.Component
import me.anno.ecs.Entity
import me.anno.ecs.EntityQuery.getComponent
import me.anno.ecs.interfaces.InputListener
import me.anno.engine.Events.addEvent
import me.anno.engine.raycast.RayQuery
import me.anno.engine.raycast.Raycast
import me.anno.input.Input
import me.anno.input.Key
import me.anno.maths.Maths.MILLIS_TO_NANOS
import me.anno.rubikscube.Backtracking.backtrack
import me.anno.rubikscube.CubeController.Companion.getTransform
import org.joml.Quaterniond
import org.joml.Vector3d
import kotlin.concurrent.thread
import kotlin.math.abs

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

    fun solve() {
        val start = cubes.map { cube -> OptimizedCube(Vector3d(cube.position), Quaterniond(cube.rotation)) }
        val transforms = slices
            .flatMap { listOf(it to +1, it to +3) }
            .map { (slice, times) -> slice to getTransform(slice, times) }
        // todo for better equality and hash-ability, round or index coordinates and rotations
        var minScore = Double.POSITIVE_INFINITY
        var best: Backtracking.Task<List<OptimizedCube>>? = null
        Backtracking.solve({ cubes ->
            // determine if cube is good
            cubes.indices.sumOf { i ->
                val cube = cubes[i]
                cube.position.distance(cubesOrigins[i]) + (1.0 - abs(cube.rotation.w))
            }
        }, { cubes ->
            transforms.map { (slice, transform) ->
                rotate(cubes, slice, transform)
            }
        }, { task ->
            val (_, score) = task
            // determine if solved
            if (score < minScore) {
                minScore = score
                best = task
                println("better score: $score")
            }
            score < 1e-16
        }, start, 10000)
        // assign best back
        val chain = backtrack(best!!, best!!.state)
        println(chain.size - 1)
        thread {
            for (i in chain.indices) {
                addEvent { apply(chain[i]) }
                Thread.sleep(100)
            }
        }
    }

    private fun apply(state: List<OptimizedCube>) {
        val time = Time.gameTimeN - 100 * MILLIS_TO_NANOS
        for (i in cubes.indices) {
            val child = cubes[i]
            val tr = child.transform
            val st = state[i]
            tr.teleportUpdate(time)
            tr.setGlobal(
                tr.globalTransform
                    .identity()
                    .translationRotate(st.position, st.rotation)
            )
            tr.smoothUpdate()
            child.invalidateAABBsCompletely()
        }
    }

    private fun rotate(cubes: List<OptimizedCube>, slice: RotationSlice, transform: Quaterniond): List<OptimizedCube> {
        return cubes.map { cube ->
            if (slice.filter(cube.position)) {
                cube.transformed(transform)
            } else cube
        }
    }

    override fun onKeyDown(key: Key): Boolean {
        if (key == Key.KEY_O) solve()
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
                    .filter { axis -> axis.filter(srcCube.position) && axis.filter(dstCube.position) }
                    .flatMap { axis -> listOf(axis to +1, axis to +3) }
                    .map { (axis, times) ->
                        val transform = getTransform(axis, times)
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