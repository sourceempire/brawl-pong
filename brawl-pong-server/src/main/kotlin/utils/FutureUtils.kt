package utils

import io.vertx.core.Future
import io.vertx.core.Promise

fun <T> futurize(block: Futurize<T>.() -> Unit): Future<T> {
    val f = Futurize<T>()
    f.block()
    return f.future
}

class Futurize<T> {
    val promise: Promise<T> = Promise.promise()
    val future: Future<T>
        get() = promise.future()
}