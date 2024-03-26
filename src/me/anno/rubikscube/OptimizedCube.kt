package me.anno.rubikscube

import me.anno.maths.Maths.SQRT1_2
import org.joml.Quaterniond
import org.joml.Vector3d
import kotlin.math.roundToInt

// we only need position and rotation
data class OptimizedCube(val position: Vector3d, val rotation: Quaterniond) {
    companion object {
        val rots = doubleArrayOf(
            -1.0, -SQRT1_2, -0.5, 0.0, 0.5, SQRT1_2, 1.0
        )
    }

    fun transformed(transform: Quaterniond): OptimizedCube {
        return OptimizedCube(
            position.rotate(transform, Vector3d()).round(),
            transform.mul(rotation, Quaterniond()).round90deg()
        )
    }

    private fun Quaterniond.round90deg(): Quaterniond {
        return set(round90deg(x), round90deg(y), round90deg(z), round90deg(w))
    }

    private fun round90deg(x: Double): Double {
        val idx = (x * 2.7).roundToInt() + 3
        return rots[idx]
    }

}