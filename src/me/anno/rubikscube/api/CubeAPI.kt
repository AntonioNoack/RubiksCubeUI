package me.anno.rubikscube.api

import me.anno.ecs.Component
import me.anno.ecs.Entity
import me.anno.ecs.EntityQuery.getComponent
import me.anno.ecs.annotations.DebugAction
import me.anno.ecs.annotations.DebugProperty
import me.anno.ecs.components.mesh.MeshComponent
import me.anno.ecs.components.mesh.material.Material
import me.anno.engine.debug.DebugAABB
import me.anno.engine.debug.DebugShapes
import me.anno.gpu.pipeline.Pipeline
import me.anno.rubikscube.CubeController
import me.anno.rubikscube.CubeController.Companion.rotateCube
import me.anno.rubikscube.cubes
import me.anno.rubikscube.cubesOrigins
import me.anno.rubikscube.slices
import me.anno.ui.UIColors
import me.anno.utils.structures.Collections.cross
import org.joml.*
import kotlin.math.PI
import kotlin.math.abs

@Suppress("unused", "MemberVisibilityCanBePrivate")
class CubeAPI : Component() {

    val camera = ArrayList<Matrix4x3d>()

    init {
        camera.add(Matrix4x3d())
    }

    @DebugProperty
    val whatIsSeen
        get() = listOf(-1, 0, 1).cross(listOf(-1, 0, 1), ArrayList(9)).map { (yi, xi) ->
            getColorAt(xi, -yi).name
        }

    @DebugProperty
    val zeroRotation
        get() = Color.entries.map { color ->
            findCube(Vector3d(color.dir)).rotation
        }

    @DebugAction
    fun turnCubeIntoStandard() {
        // todo why is this not working????
        val anyCenterCube = findCube(Vector3d(1.0, 0.0, 0.0))
        val totalRotation = Quaterniond(anyCenterCube.transform.globalRotation)
        for (cube in cubes) {
            rotateCube(cube, totalRotation)
            cube.transform.teleportUpdate()
        }
    }

    fun lookAt(color: Color) {
        camera.add(Matrix4x3d().rotate(color.rotationFromZero))
    }

    private fun findCube(v: Vector3d): Entity {
        return cubes.maxBy { it.position.dot(v.x, v.y, v.z) / it.position.length() }
    }

    fun getColorAt(xi: Int): Color {
        return getColorAt(xi % 3 - 1, 1 - xi / 3)
    }

    var rotateSide = Side.UP
    var rotateColor = Color.WHITE

    @DebugAction
    fun testRotateColor() {
        rotateSideCW(rotateColor, 1)
    }

    @DebugAction
    fun testRotateSide() {
        rotateSideCW(rotateSide, 1)
    }

    fun rotateSideCCW(color: Color, times: Int) {
        val slice = slices.indexOfFirst { color.dir.distance(it.dir) == 0.0 }
        getComponent(CubeController::class)!!.rotate(slice, times, true)
        afterStep()
    }

    fun rotateSideCCW(side: Side, times: Int) {
        val newDir = camera.last().transformDirection(Vector3d(side.colorSeenFromFront.dir))
        val newDir1 = Vector3i(newDir.x.toInt(), newDir.y.toInt(), newDir.z.toInt())
        val slice = slices.indexOfFirst { newDir1.distance(it.dir) == 0.0 }
        getComponent(CubeController::class)!!.rotate(slice, times, true)
        afterStep()
    }

    fun rotateSideCW(side: Side, times: Int) {
        rotateSideCCW(side, -times)
    }

    fun rotateSideCW(color: Color, times: Int) {
        rotateSideCCW(color, -times)
    }

    fun getColorAt(xi: Int, yi: Int): Color {
        val cam = camera.last()
        val dir = cam.transformDirection(Vector3d(xi.toDouble(), yi.toDouble(), 1.0))
        val cube = findCube(dir)
        val meshes = cube.components.filterIsInstance<MeshComponent>()
            .map {
                val material = Pipeline.getMaterial(it.materials, it.getMesh()!!.materials, 0)
                it to material
            }
            .filter { it.second.name.startsWith("M") }
        if (meshes.size == 1) {
            return getColor(meshes.first().second)
        }
        // find the face that looks at us
        //  - given: camera, transformed mesh,
        //  - searched: which material (if > 1) is looking at us? (AABB[viewDir].delta ~ 0)
        val viewDir = cam.transformDirection(Vector3d(0.0, 0.0, 1.0))
        viewDir.rotateInv(cube.rotation)
        val bestMesh = meshes.minBy {
            val bounds = it.first.getMesh()!!.getBounds()
            when {
                abs(viewDir.x) > 0.5 -> bounds.deltaX
                abs(viewDir.y) > 0.5 -> bounds.deltaY
                else -> bounds.deltaZ
            }
        }
        if (showCameraSide) {
            // outline, which meshes were taken
            val bounds = AABBd(bestMesh.first.getMesh()!!.getBounds())
            bounds.addMargin(0.15)
            bounds.transform(cube.transform.globalTransform)
            DebugShapes.debugAABBs.add(DebugAABB(bounds, UIColors.fireBrick, 0.1f))
        }
        return getColor(bestMesh.second)
    }

    var showCameraSide = false

    private fun getColor(material: Material): Color {
        return Color.entries[material.name.substring(1).toInt()]
    }

    @DebugAction
    fun turnLeft() {
        camera.add(Matrix4x3d(camera.last()).rotateY(-PI / 2))
        afterStep()
    }

    @DebugAction
    fun turnRight() {
        camera.add(Matrix4x3d(camera.last()).rotateY(PI / 2))
        afterStep()
    }

    @DebugAction
    fun turnTop() {
        camera.add(Matrix4x3d(camera.last()).rotateX(-PI / 2))
        afterStep()
    }

    @DebugAction
    fun turnDown() {
        camera.add(Matrix4x3d(camera.last()).rotateX(PI / 2))
        afterStep()
    }

    @DebugAction
    fun undoTurn() {
        if (camera.size > 1) {
            camera.removeLast()
            afterStep()
        }
    }

    var stepDelayMillis: Long = 0

    fun afterStep() {
        if (stepDelayMillis > 0) {
            Thread.sleep(stepDelayMillis)
        }
    }

    fun applyCubeNotation(notation: String) {
        var i = 0
        while (i < notation.length) {
            var symbol = notation[i++]
            var count = 1
            if (symbol == '2') {
                symbol = notation[i - 2]
            }
            if (symbol == '3') {
                symbol = notation[i - 2]
                count = 2
            }
            when (symbol) {
                'L' -> rotateSideCW(Side.LEFT, count)
                'R' -> rotateSideCW(Side.RIGHT, count)
                'U' -> rotateSideCW(Side.UP, count)
                'D' -> rotateSideCW(Side.DOWN, count)
                'F' -> rotateSideCW(Side.FRONT, count)
                'B' -> rotateSideCW(Side.BACK, count)
                else -> throw IllegalStateException("Unknown notation ${notation[i]}")
            }
        }
    }

    @DebugAction
    fun resetCube() {
        for (i in cubes.indices) {
            val cube = cubes[i]
            cube.transform.setGlobal(
                cube.transform.globalTransform
                    .identity()
                    .translation(cubesOrigins[i])
            )
            cube.transform.teleportUpdate()
            cube.invalidateAABBsCompletely()
        }
    }
}