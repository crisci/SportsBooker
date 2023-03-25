import java.util.Collections

class Forth {


    private enum class OP {
        dup,
        drop,
        swap,
        over
    }
    private val arithmeticOp = listOf<String>("+","-","/","*")


    fun evaluate(vararg line: String): List<Int> {

        val stack = mutableListOf<Int>()
        val string = if(line.size == 1) {
            require(!line[0].startsWith(":")) { "illegal operation" }
            line[0]
        } else {
            var s = line[line.size - 1]
            for(i in line.size - 2 downTo 0) {
                val input = line[i].substring(1, line[i].length - 1).trim().split(" ", limit = 2)
                val userWord = input[0].lowercase()
                val def = input[1].lowercase()
                s = s.lowercase().replace(userWord, def)
            }
            s
        }
        var res = 0
        for(i in string.split(" ")) {
            if (i.toIntOrNull() != null) stack.add(i.toInt())
            else {
                require(enumValues<OP>().any { it.name == i.lowercase() } || arithmeticOp.any { it == i }) {"undefined operation"}
                require(stack.isNotEmpty()) { "empty stack" }
                if(i.lowercase() == "swap" || i.lowercase() == "over") require(stack.size > 1) { "only one value on the stack" }
                if( enumValues<OP>().any { it.name == i.lowercase() } ) {
                    when(i.lowercase()) {
                        "dup" -> stack.add(stack.elementAt(stack.size-1))
                        "drop" -> stack.removeAt(stack.size - 1)
                        "swap" -> Collections.swap(stack, stack.size - 1, stack.size - 2)
                        "over" -> stack.add(stack.elementAt(stack.size - 2))
                        else -> throw Exception("undefined operation")
                    }
                } else {
                    require(stack.size > 1) { "only one value on the stack" }
                    if(i.equals("/")) require(stack.elementAt(1) != 0) { "divide by zero" }
                    res = when(i) {
                        "+" -> stack.sumOf { it }
                        "-" -> stack.reduce { a,b -> a-b }
                        "*" -> stack.reduce { a,b -> a*b }
                        "/" -> stack.reduce { a,b -> a/b }
                        else -> throw Exception("undefined operation")
                    }
                    stack.clear()
                    stack.add(res)
                }
            }
        }
        return stack
    }

}