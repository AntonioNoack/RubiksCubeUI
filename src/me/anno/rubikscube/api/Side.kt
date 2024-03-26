package me.anno.rubikscube.api

enum class Side(val colorSeenFromFront: Color) {
    LEFT(Color.ORANGE),
    RIGHT(Color.RED),
    FRONT(Color.GREEN),
    BACK(Color.BLUE),
    UP(Color.WHITE),
    DOWN(Color.YELLOW)
}