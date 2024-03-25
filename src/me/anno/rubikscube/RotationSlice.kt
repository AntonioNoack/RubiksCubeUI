package me.anno.rubikscube

import me.anno.ecs.Entity
import org.joml.Quaterniond

/**
 * Rotation axis and which cubes a slice belongs to.
 * It's a lambda instead of a concrete list, because it changes when the cube is turned/shuffled.
 * */
class RotationSlice(val rotation: Quaterniond, val filter: (Entity) -> Boolean)