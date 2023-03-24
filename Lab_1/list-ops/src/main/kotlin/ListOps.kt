fun <T> List<T>.customAppend(list: List<T>): List<T> {
    return list.customFoldLeft(this) {a,b -> a+b}

}

fun List<Any>.customConcat(): List<Any> {
    return this.flatMap {
        when(it) {
            is List<*> -> it.filterNotNull().customConcat()
            else -> listOf(it as Any)
        }
    }
}
fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
    var list = mutableListOf<T>()
    for(i in this) {
        if(predicate(i))
            list.add(i)
    }
    return list
}

val List<Any>.customSize: Int get() = this.customFoldLeft(0) {a,_ -> a+1}

fun <T, U> List<T>.customMap(transform: (T) -> U): List<U> {
    val list = mutableListOf<U>()
    for(i in this) {
        list.add(transform(i))
    }
    return list
}
fun <T, U> List<T>.customFoldLeft(initial: U, f: (U, T) -> U): U {
    /*évar value : U = initial;
    for(i in this) {
        value = f(value, i)
    }
    return value*/
    return if(this.isEmpty()) initial
    else this.drop(1).customFoldLeft(f(initial, this.first()), f)
}

fun <T, U> List<T>.customFoldRight(initial: U, f: (T, U) -> U): U {
    return if(this.isEmpty()) initial
    else this.dropLast(1).customFoldRight(f(this.last(), initial), f)
}

fun <T> List<T>.customReverse(): List<T> {
    val list = mutableListOf<T>()
    var count = 0;
    for(i in this) {
        if(!this.dropLast(count).isEmpty()) list.add(this.dropLast(count).last())
        count ++;

    }
    return list
}
