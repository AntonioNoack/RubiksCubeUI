package me.anno.rubikscube

import me.anno.config.DefaultConfig.style
import me.anno.ecs.Entity
import me.anno.ecs.EntityQuery.getComponentInChildren
import me.anno.ecs.components.camera.Camera
import me.anno.ecs.components.camera.control.CameraController
import me.anno.ecs.components.camera.control.OrbitControls
import me.anno.ecs.components.light.sky.Skybox
import me.anno.ecs.prefab.PrefabCache
import me.anno.engine.ECSRegistry
import me.anno.engine.EngineBase
import me.anno.engine.OfficialExtensions
import me.anno.engine.ui.render.PlayMode
import me.anno.engine.ui.render.RenderMode
import me.anno.engine.ui.render.RenderView1
import me.anno.engine.ui.render.SceneView
import me.anno.engine.ui.render.SceneView.Companion.testScene
import me.anno.extensions.ExtensionLoader
import me.anno.io.files.Reference.getReference
import me.anno.mesh.vox.meshing.BlockSide
import me.anno.rubikscube.api.CubeAPI
import me.anno.rubikscube.api.sample.CheckerBoardPattern
import me.anno.rubikscube.api.sample.CubeNotationPattern
import me.anno.ui.base.groups.PanelStack
import me.anno.ui.debug.PureTestEngine.Companion.testPureUI
import me.anno.ui.debug.TestEngine.Companion.testUI3
import me.anno.utils.structures.lists.Lists.firstInstance
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3i
import kotlin.math.PI
import kotlin.math.abs

// done load mesh as entity
// done define rotation axes
// done rotate stuff
// done UI: drag n rotate
// done show when solved
// done randomize
// todo more cubes, and menu to choose cube type (2³, 5³, ...)
// done shuffle button (currently is bound to p-key)
// done hide commandline UI
// todo create cheaper, good looking sky

lateinit var sceneView: SceneView
lateinit var slices: List<RotationSlice>
lateinit var cubes: List<Entity>
lateinit var cubesOrigins: List<Vector3d>
lateinit var scene: Entity

val randomizedSlices = 6

/**
 * Sets up the game, and then lets you play it.
 * */
fun main() {

    OfficialExtensions.register()
    ExtensionLoader.load()
    ECSRegistry.init()

    val meshSrcFile = getReference("res://meshes/Cube 3x3x3.glb")
    scene = PrefabCache[meshSrcFile]!!.createInstance() as Entity

    cubes = scene.children.toList()
    cubesOrigins = cubes.map { Vector3d(it.position) }
    slices = BlockSide.entries // depending on what cube you have, you need to change this
        .map { side ->
            val dir0 = Vector3i(side.x, side.y, side.z)
            val dir = Vector3d(dir0)
            RotationSlice(Quaterniond().rotateAxis(PI / 2, dir), dir0) { cube ->
                cube.dot(dir) > 0.5
            }
        } + BlockSide.entries
        .filter { it.x + it.y + it.z > 0 }
        .map { side ->
            val dir0 = Vector3i(side.x, side.y, side.z)
            val dir = Vector3d(dir0)
            RotationSlice(Quaterniond().rotateAxis(PI / 2, dir), dir0) { cube ->
                abs(cube.dot(dir)) < 0.5
            }
        }

    // add components for controls
    scene.add(CubeController())
    scene.add(MouseDragController())
    // add animated sky (default sky isn't animated)
    scene.add(Skybox())

    scene.add(CubeAPI())
    scene.add(CheckerBoardPattern())
    scene.add(CubeNotationPattern())

    if (false) {
        testPureUI("Rubik's Cube") {
            EngineBase.enableVSync = true // we don't need all fps ^^
            sceneView = SceneView(RenderView1(PlayMode.PLAYING, scene, style), style)
            val controls = OrbitControls()
            controls.needsClickToRotate = true
            controls.rotateLeft = false
            controls.rotateRight = true
            scene.add(CameraController.setup(controls, sceneView.renderer))
            scene.getComponentInChildren(Camera::class)!!.fovY = 60f
            sceneView.renderer.renderMode = RenderMode.FORWARD
            val ui = PanelStack(style)
            ui.add(sceneView)
            ui.add(ShowSolvedPanel(style))
            ui.add(TurnCounterUI(style))
            ui
        }
    } else {
        testUI3("Rubik's Cube") {
            EngineBase.enableVSync = true // we don't need all fps ^^
            val ui = testScene(scene)
            sceneView = ui.listOfAll.firstInstance()
            ui
        }
    }
}