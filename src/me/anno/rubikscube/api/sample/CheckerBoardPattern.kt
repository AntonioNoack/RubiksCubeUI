package me.anno.rubikscube.api.sample

import me.anno.ecs.Component
import me.anno.ecs.EntityQuery.getComponent
import me.anno.ecs.annotations.DebugAction
import me.anno.rubikscube.api.CubeAPI
import kotlin.concurrent.thread

class CheckerBoardPattern : Component() {
    @DebugAction
    fun makeCheckerBoardPattern() {
        thread {
            val api = getComponent(CubeAPI::class)!!
            api.applyCubeNotation("L2R2B2F2U2D2")
        }
    }
}