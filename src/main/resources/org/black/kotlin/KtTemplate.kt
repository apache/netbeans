/* Block comment */
package hello
import kotlin.util.* // line comment

/**
 * Doc comment here for `SomeClass`
 * @see Iterator#next()
 */
@Deprecated("Deprecated class")
public class MyClass<out T : Iterable<T>>(var prop1 : Int)
    fun foo(nullable : String?, r : Runnable, f : () -> Int, fl : FunctionLike, dyn: dynamic) {
        println("length\nis ${nullable?.length} \e")
        val ints = java.util.ArrayList<Int?>(2)
        ints[0] = 102 + f() + fl()
        val myFun = { -> "" };
        var ref = ints.size()
        if (!ints.empty) {
              ints.forEach lit@ {
                  if (it == null) return@lit
                  println(it + ref)
              }
        }
        dyn.dynamicCall()
        dyn.dynamicProp = 5
    }
}

fun Int?.bar() {
    if (this != null) {
        println(toString())
    }
    else {
        println(this.toString())
    }
}

var globalCounter : Int = 5
    get() {
        return field
    }

public abstract class Abstract {
}

object Obj

enum class E { A }
               Bad character: \n
