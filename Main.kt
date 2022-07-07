package converter
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

// functions that the convert functions use
fun alphabet(): List<Char> {
    val alpha = mutableListOf<Char>()
    for (i in 'a'..'z') {
        alpha.add(i)
    }
    return alpha.toList()
}
fun alphabetUpperCase(): List<Char> {
    val alpha = mutableListOf<Char>()
    for (i in 'A'..'Z') {
        alpha.add(i)
    }
    return alpha.toList()
}
fun shortener(number: String): String {
    var toReturn = ""
    when {
        number.length > 5 -> toReturn += number.substring(0, 5)
        number.length < 5 -> {
            val quantity = 5 - number.length
            toReturn += number
            repeat(quantity) {
                toReturn += "0"
            }
        } else -> toReturn = number
    }
    return toReturn
}

//conversion functions
// from all bases to decimal base
fun allToDecimal(number: String, sourceB: String) :String {
    val numberList = mutableListOf<Char>()
    var nI = BigDecimal(1)
    val base = BigDecimal(sourceB)
    val powerBack = mutableListOf<BigDecimal>()
    for (i in 0..number.lastIndex) {
        if (number[0] == '0') {
            numberList.add('0')
            break
        } else {
            if (number[i] == '.') break
            powerBack.add(nI)
            nI *= base
            numberList.add(number[i].uppercaseChar())
        }
    }
    powerBack.reverse()
    val listSum = mutableListOf<BigDecimal>()
    for (i in 0..numberList.lastIndex) {
        if (number[0] == '0') break
        val num = when (numberList[i]) {
            in alphabet() -> {
                BigDecimal(10 + alphabet().indexOf(numberList[i]))
            }
            in alphabetUpperCase() -> {
                BigDecimal(10 + alphabetUpperCase().indexOf(numberList[i]))
            }
            else -> {
                BigDecimal(numberList[i].toString())
            }
        }
        listSum.add(num * powerBack[i])
    }
    var sum = BigDecimal(0)
    for (i in listSum) {
        sum += i
    }

    return sum.setScale(0, RoundingMode.UNNECESSARY).toString()
}


fun pointToDecimal(number: String, sourceBase: String): String {
    //get the fractional of the original number
    val numberBack = mutableListOf<Int>()
    loop1@ for (i in number.lastIndex downTo 0) {
        when (number[i]) {
            '.' -> {
                break@loop1
            }
            in alphabet() -> {
                numberBack.add(alphabet().indexOf(number[i]) + 10)
            }
            in alphabetUpperCase() -> {
                numberBack.add(alphabetUpperCase().indexOf(number[i]) + 10)
            }
            else -> {
                numberBack.add(number[i].toString().toInt())
            }
        }
    }
    //multiply the 1/sourceBase for each number in the numberBack list plus the reminder
    var remind = BigDecimal.ZERO.setScale(5, RoundingMode.HALF_EVEN)

    val base = BigDecimal.ONE.setScale(5, RoundingMode.HALF_EVEN) / BigDecimal(sourceBase).setScale(5, RoundingMode.HALF_EVEN)
    for (i in 0..numberBack.lastIndex) {
        remind = base * (numberBack[i].toBigDecimal() + remind)
    }
    val reminder = remind.setScale(5, RoundingMode.HALF_UP)
    val (uselessZero, splitedResult) = reminder.toString().split(".")

    return shortener(splitedResult)
}

//from decimal base to all bases
fun decimalToAll(numberInDecimal: String, targetBase: String) :String {
    var result = ""
    var numberInteger = ""
    loop1@for (i in numberInDecimal) {
        if (i=='.') break@loop1
        numberInteger += i
    }
    if(numberInteger == "0") result = numberInteger

    if (numberInDecimal[0] != '0') {

        val base = BigDecimal(targetBase)

        var temporalDivision = numberInteger.toBigDecimal().setScale(5, RoundingMode.HALF_EVEN)

        val reversed32 = mutableListOf<String>()

        while (temporalDivision.toInt() != 0) {
            val (divide, reminder) = temporalDivision.setScale(5, RoundingMode.HALF_EVEN).divideAndRemainder(base)
            temporalDivision = divide
            val numberToAdd = when  (reminder.toInt()) {
                in 10..base.toInt() + 10 -> {
                    alphabet()[abs(reminder.toInt() - 10) ]
                }
                else -> (reminder.toInt().toString())
            }
            reversed32.add(numberToAdd.toString())
        }
        result += reversed32.reversed().joinToString("")
    }

    return result
}


fun pointToAll(numberInFractional: String, targetBase: String): String {
    var numberToReturn = ""

    val (integer, fractional) = numberInFractional.split(".")

    val decimal = numberInFractional.toBigDecimal() % BigDecimal("1")
    var number = decimal
    var count = 5
    loop1@while (count > 0) {
        val temporalDecimal = number * BigDecimal(targetBase)
        val decimalPart = temporalDecimal % BigDecimal.ONE
        val temporalInteger = temporalDecimal - decimalPart

        temporalInteger.toBigInteger()

        if (temporalInteger.toInt() > 9) {
            numberToReturn += alphabet()[temporalInteger.toInt() - 10]
        } else {
            numberToReturn += temporalInteger.toInt().toString()
        }
        number = decimalPart % BigDecimal.ONE
        if (temporalDecimal.toInt() == 0) {
            break@loop1
        } else {
            count--
            continue
        }
    }
    return shortener(numberToReturn)
}
// checks if the number is fractional
fun isItFractional(number: String): Boolean {
    return '.' in number
}

// just the main function xd
fun main() {
    level1@while (true) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
        val read  = readLine()!!.split(" ")
        if (read.joinToString() == "/exit" || read.joinToString() == "/EXIT") break@level1
        val sourceB = read[0]
        val targetB = read[1]
        level2@while (true) {
            println("Enter number in base $sourceB to convert to base $targetB (To go back type /back)")
            val number = readLine()!!
            if (number == "/back" || number == "/BACK") continue@level1
            val result = convert(sourceB, targetB, number)
            println("Conversion result: $result")
        }
    }
}

//function to connect the convert functions
fun convert(sourceBase: String, targetBase: String, number: String) :String {

    val numberInDecimal = when(sourceBase) {
        "10" -> number
        else -> {
            if (isItFractional(number)) {
                allToDecimal(number, sourceBase) + "." + pointToDecimal(number, sourceBase)
            } else {
                allToDecimal(number, sourceBase)
            }
        }
    }

    val result = when(targetBase) {
        "10" -> numberInDecimal
        else -> {
            if (isItFractional(numberInDecimal)) {
                decimalToAll(numberInDecimal, targetBase) + "." + pointToAll(numberInDecimal, targetBase)
            } else {
                decimalToAll(numberInDecimal, targetBase)
            }
        }
    }
    return if(sourceBase == targetBase) number else result
}