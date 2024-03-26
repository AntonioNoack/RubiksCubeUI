package me.anno.rubikscube.api.sample

import me.anno.ecs.Component
import me.anno.ecs.EntityQuery.getComponent
import me.anno.ecs.annotations.DebugAction
import me.anno.rubikscube.api.CubeAPI
import kotlin.concurrent.thread

class CubeNotationPattern : Component() {

    var pattern = ""

    @DebugAction
    fun applyPattern() {
        thread {
            val api = getComponent(CubeAPI::class)!!
            api.applyCubeNotation(pattern)
        }
    }
}