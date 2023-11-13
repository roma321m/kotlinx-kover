import org.gradle.api.Action
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Proxy
import java.util.Locale
import kotlin.reflect.KClass
//
//class TestExtensionsGenerator {
//}
//
//
//fun <T : Any> generateBlockInstance(type: KClass<T>): T {
//    return Proxy.newProxyInstance(
//        type.java.getClassLoader(),
//        arrayOf<Class<*>>(type.java),
//        BlockHandler()
//    ) as T
//}
//
//class BlockHandler(private val append: (String) -> Unit, private val indents: Int) : InvocationHandler {
//    override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any? {
//        val name = method.name
//        val params = method.parameters
//
//        when {
//            // property getter
//            name.startsWith("get") && name != "get" -> {
//
//            }
//            // property setter
//            name.startsWith("set") && name != "set" -> {
//
//            }
//            // function call with block
//            params.last().type == Action::class.java -> {
//                if (params.size > 1) {
//                    val limitedParams = method.parameters?.toList()?.let { it.subList(0, it.size - 2) } ?: emptyList()
//                    val limitedArgs = args?.toList()?.let { it.subList(0, it.size - 2) } ?: emptyList()
//
//                    // ('a', "b", 3)
//                    append("(")
//                    append(formatArguments(limitedParams, limitedArgs))
//                    append(")")
//                }
//                append(" {\n")
//            }
//            else -> {
//                // function call
////                (method.parameters.last().parameterizedType as ParameterizedType).actualTypeArguments[0]
//
//            }
//        }
//
//        return null
//    }
//}
//
//private class WriterContext(private val append: (String) -> Unit, private val indents: Int) {
//    val indentsString = indent(indents)
//
//    fun nestedContext(): WriterContext {
//        return WriterContext(append, indents + 1)
//    }
//}
//
//private fun formatArguments(parameters: List<Parameter>, args: List<Any?>): String {
//    return parameters.mapIndexed { i, p -> formatValue(p, args[i]) }.joinToString(", ")
//}
//
//private fun formatValue(parameter: Parameter, value: Any?): String {
//
//}
//
//private fun extractPropertyName(methodName: String): String {
//    return methodName.substring(3).replaceFirstChar { it.lowercase(Locale.getDefault()) }
//}
//
//private fun indent(count: Int): String {
//    // In most cases, the nesting depth is no more than 5, so for optimization we use literals for frequently used indents
//    return when (count) {
//        0 -> ""
//        1 -> "    "
//        2 -> "        "
//        3 -> "            "
//        4 -> "                "
//        5 -> "                    "
//        else -> "    ".repeat(count)
//    }
//}
