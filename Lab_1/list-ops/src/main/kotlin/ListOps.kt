fun <T> List<T>.customAppend(list: List<T>): List<T> {
    return this + list
}

fun List<Any>.customConcat(): List<Any> {
    val res = mutableListOf<Any>()
    this.forEach { e ->
        when (e) {
            is List<*> -> res.addAll(e.map { it as Any }.customConcat())
            else -> res.add(e)
        }
    }
    return res
}

fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
    val res = mutableListOf<T>()
    this.forEach { if (predicate(it)) res.add(it) }
    return res
}

val List<Any>.customSize: Int
    get() {
        var count = 0
        this.forEach { count++ }
        return count
    }

fun <T, U> List<T>.customMap(transform: (T) -> U): List<U> {
    val res = mutableListOf<U>()
    this.forEach { res.add(transform(it)) }
    return res
}

fun <T, U> List<T>.customFoldLeft(initial: U, f: (U, T) -> U): U {
    var res = initial
    this.forEach { res = f(res, it) }
    return res
}

fun <T, U> List<T>.customFoldRight(initial: U, f: (T, U) -> U): U {
    var res = initial
    this.customReverse().forEach { res = f(it, res) }
    return res
}

fun <T> List<T>.customReverse(): List<T> {
    val reversedList = mutableListOf<T>()
    for (i in size - 1 downTo 0) {
        reversedList.add(this[i])
    }
    return reversedList
}