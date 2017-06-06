package com.smallplanet.labainaction

/**
 * Created by javiermoreno on 6/6/17.
 */

class DotMatrix(val dotMatrix: MutableList<MutableList<Dot>>) {

    val one = arrayOf(emptyArray(), arrayOf(2), arrayOf(2), arrayOf(2), arrayOf(2), arrayOf(2), emptyArray())
    val two = arrayOf(emptyArray(), arrayOf(1, 2, 3), arrayOf(3), arrayOf(1, 2, 3), arrayOf(1), arrayOf(1, 2, 3), emptyArray())
    val three = arrayOf(emptyArray(), arrayOf(1, 2, 3), arrayOf(3), arrayOf(1, 2, 3), arrayOf(3), arrayOf(1, 2, 3), emptyArray())
    val four = arrayOf(emptyArray(), arrayOf(1, 3), arrayOf(1, 3), arrayOf(1, 2, 3), arrayOf(3), arrayOf(3), emptyArray())
    val five = arrayOf(emptyArray(), arrayOf(1, 2, 3), arrayOf(1), arrayOf(1, 2, 3), arrayOf(3), arrayOf(1, 2, 3), emptyArray())
    val six = arrayOf(emptyArray(), arrayOf(1, 2, 3), arrayOf(1), arrayOf(1, 2, 3), arrayOf(1, 3), arrayOf(1, 2, 3), emptyArray())
    val seven = arrayOf(emptyArray(), arrayOf(1, 2, 3), arrayOf(3), arrayOf(3), arrayOf(3), arrayOf(3), emptyArray())
    val eight = arrayOf(emptyArray(), arrayOf(1, 2, 3), arrayOf(1, 3), arrayOf(1, 2, 3), arrayOf(1, 3), arrayOf(1, 2, 3), emptyArray())
    val nine = arrayOf(emptyArray(), arrayOf(1, 2, 3), arrayOf(1, 3), arrayOf(1, 2, 3), arrayOf(3), arrayOf(3), emptyArray())
    val zero = arrayOf(emptyArray(), arrayOf(1, 2, 3), arrayOf(1, 3), arrayOf(1, 3), arrayOf(1, 3), arrayOf(1, 2, 3), emptyArray())

    val numbers = arrayOf(zero, one, two, three, four, five, six, seven, eight, nine)

    fun turnOnNumber(n: Int){
        val number = numbers[n]

        for (i in 0..number.size - 1) {
            for (index in number[i])
                dotMatrix[i][index].turnOn()
        }
    }

    fun turnOff() {
        dotMatrix
                .flatMap { it }
                .forEach { it.turnOff() }
    }

    fun turnOn() {
        dotMatrix
                .flatMap { it }
                .forEach { it.turnOn() }
    }
}