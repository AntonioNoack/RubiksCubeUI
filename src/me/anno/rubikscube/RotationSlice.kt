package me.anno.rubikscube

import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3i

/**
 * Rotation axis and which cubes a slice belongs to.
 * It's a lambda instead of a concrete list, because it changes when the cube is turned/shuffled.
 * */
class RotationSlice(val rotation: Quaterniond, val dir : Vector3i, val filter: (Vector3d) -> Boolean)