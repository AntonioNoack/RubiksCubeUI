package me.anno.rubikscube

import me.anno.Time
import me.anno.ecs.Component
import me.anno.ecs.Entity
import me.anno.ecs.annotations.DebugAction
import me.anno.ecs.annotations.DebugProperty
import me.anno.ecs.annotations.Docs
import me.anno.engine.serialization.NotSerializedProperty
import me.anno.maths.Maths.MILLIS_TO_NANOS
import me.anno.utils.pooling.JomlPools
import org.joml.Quaterniond
import kotlin.math.abs
import kotlin.random.Random

/**
 * Implements turning slices of the cube
 * */
class CubeController : Component() {

    companion object {
        fun getTransform(axis: RotationSlice, times: Int): Quaterniond {
            val axisTimes = Quaterniond()
            for (i in 0 until abs(times)) {
                axisTimes.mul(axis.rotation)
            }
            if (times < 0) {
                axisTimes.conjugate()
            }
            return axisTimes
        }

        fun rotateCube(child: Entity, axisTimes: Quaterniond) {
            child.transform.setGlobal(child.transform.globalTransform.rotateLocal(axisTimes))
        }
    }

    @Docs("How many rotations are applied for shuffling")
    var shuffling = 100

    @Docs("How many turns have been done")
    var turnCounter = 0

    @DebugProperty
    @NotSerializedProperty
    var isSolved = true

    fun rotate(axisIndex: Int, times: Int, smoothly: Boolean) {
        // apply rotation
        val axis = slices.getOrNull(axisIndex) ?: return
        val slerpTime = 100 * MILLIS_TO_NANOS
        val axisTimes = getTransform(axis, times)
        val cubesOnSide = cubes.filter { axis.filter(it.position) }
        if (smoothly) {
            for (child in cubesOnSide) {
                child.transform.teleportUpdate(Time.gameTimeN - slerpTime)
                rotateCube(child, axisTimes)
                child.transform.smoothUpdate()
                child.invalidateAABBsCompletely() // todo why is this necessary?
            }
            checkSolved()
            turnCounter++
        } else {
            for (child in cubesOnSide) {
                rotateCube(child, axisTimes)
            }
        }
    }

    @DebugAction
    fun shuffle() {
        // apply random rotations
        val random = Random(Time.gameTimeN)
        for (i in 0 until shuffling) {
            rotate(random.nextInt(randomizedSlices), random.nextInt(3) + 1, false)
        }
        // update transforms
        for (cube in cubes) {
            cube.transform.teleportUpdate()
            cube.invalidateAABBsCompletely()
        }
        // update stats
        checkSolved()
        turnCounter = 0
    }

    private fun checkSolved() {
        // the centers cannot rotate ->
        //  there is only a single permutation in which everything is correct,
        //  and for that each parts position must be zero, and rotation identity
        val sample = cubes.firstOrNull() ?: return
        val v0 = JomlPools.vec3d.index
        val r0 = JomlPools.quat4d.index
        val identity = sample.transform.globalRotation
        isSolved = cubes.indices.all { i ->
            val it = cubes[i]
            it.transform.globalRotation.equals(identity, 0.1) &&
                    it.transform.globalPosition
                        .rotateInv(identity)
                        .distance(cubesOrigins[i]) < 0.1
        }
        JomlPools.vec3d.index = v0
        JomlPools.quat4d.index = r0
    }

}