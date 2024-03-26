package me.anno.rubikscube.api

import org.joml.Quaterniond
import org.joml.Vector3i
import kotlin.math.PI

enum class Color(val dir: Vector3i, val rotationFromZero: Quaterniond) {
    RED(Vector3i(1, 0, 0), Quaterniond().rotateY(PI / 2)),
    WHITE(Vector3i(0, 1, 0), Quaterniond().rotateX(-PI / 2)),
    BLUE(Vector3i(0, 0, 1), Quaterniond().rotateY(PI)),
    ORANGE(Vector3i(-1, 0, 0), Quaterniond().rotateY(-PI / 2)),
    YELLOW(Vector3i(0, -1, 0), Quaterniond().rotateX(PI / 2)),
    GREEN(Vector3i(0, 0, -1), Quaterniond()),
}