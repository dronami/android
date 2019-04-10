package com.dronami.brightcitylights

import android.graphics.Color
import kotlin.random.Random

object ColorManager {
    val lightDarkColor: Int = Color.rgb(40,40,40)
    val lightBrightColor: Int = Color.rgb(255,180,0)
    val lightBrightColorB: Int = Color.rgb(230,0,255)
    val skyColor: Int = Color.rgb(10, 10, 60)
    val missionTextColor: Int = Color.rgb(255, 204, 0)

    public enum class Colors(val value: Int) {
        BLUE(0),
        RED(1),
        GREEN(2),
        PURPLE(3),
        SALMON(4),
        GRAY(5)
    }
    // Background front
    // Background side
    // Frame/roof
    // Roof side
    // Roof top
    val colorSets: List<List<Int>> = listOf(
        // Blue
        listOf(Color.rgb(51, 102, 204),
            Color.rgb(31, 65, 132),
            Color.rgb(0, 73, 108),
            Color.rgb(0, 54, 79),
            Color.rgb(0, 106, 155)),
        // Red
        listOf(Color.rgb(145, 8, 8),
            Color.rgb(105, 3, 3),
            Color.rgb(90, 3, 3),
            Color.rgb(77, 2, 2),
            Color.rgb(114, 3, 3)),
        // Green
        listOf(Color.rgb(51, 102, 51),
            Color.rgb(39, 78, 39),
            Color.rgb(33, 67, 33),
            Color.rgb(26, 53, 26),
            Color.rgb(41, 84, 41)),
        // Purple
        listOf(Color.rgb(102, 51, 153),
            Color.rgb(75, 37, 114),
            Color.rgb(73, 0, 147),
            Color.rgb(47, 0, 94),
            Color.rgb(91, 0, 183)),
        // Salmon
        listOf(Color.rgb(204, 51, 102),
            Color.rgb(170, 40, 83),
            Color.rgb(153, 0, 102),
            Color.rgb(111, 0, 74),
            Color.rgb(183, 0, 123)),
        // Gray
        listOf(Color.rgb(94, 103, 138),
            Color.rgb(64, 70, 94),
            Color.rgb(64, 78, 102),
            Color.rgb(46, 56, 73),
            Color.rgb(86, 105, 137))
    )

    fun getColorList(index: Int) : List<Int> {
        if (index == -1) {
            return getRandomColorList()
        }
        return colorSets[index]
    }

    fun getRandomColorList() : List<Int> {
        return colorSets[Random.nextInt(colorSets.count())]
    }

    fun getSpecificColor(colorIndex: Int, typeIndex: Int) : Int {
        return colorSets[colorIndex][typeIndex]
    }

    fun getColorCount() : Int {
        return colorSets.count()
    }
}

object ColorBlender {
    fun blendColors(start: Int, end: Int, ratio: Float) : Int {
        val sR: Int = Color.red(start)
        val sG: Int = Color.green(start)
        val sB: Int = Color.blue(start)

        val eR: Int = Color.red(end)
        val eG: Int = Color.green(end)
        val eB: Int = Color.blue(end)

        return Color.rgb(sR+((eR-sR)*ratio).toInt(), sG+((eG-sG)*ratio).toInt(), sB+((eB-sB)*ratio).toInt())
    }
}