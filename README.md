# Rubik's Cube - UI
A program, where you can play with a Rubik's Cube (Zauberwürfel), written in [Rem's Engine](https://github.com/AntonioNoack/RemsEngine) as a sample project.
The logic has been kept as simple as possible to make it easy to understand.

Image, of what the game looks like:
![Promo Image](progress/promo.png)

## Controls
The controls are mouse-based only at the moment.
- To turn your camera, move your mouse while pressing the right mouse button.
- To turn a slice of the cube, drag from what cube you want to start from, and where you want it to end.
If there are multiple possibilities, it will choose one randomly (not recommended).

## API: Write your own solver/patterns
There is a subpackage, and component called CubeAPI, where you can control the cube.
The sample package shows how to create a component, and then just add an instance of it in RubiksCube.kt to the variable "scene".
You can then find it in the UI, and edit any variables and run any @DebugAction functions.

There is two sample classes in api/sample, copy them, and make them your own 😊.