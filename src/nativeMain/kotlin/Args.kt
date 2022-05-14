
class Args(private vararg val string: String) {

    var configFilePath: String = "~/.config/k-input-driver.conf"

    enum class Commands(vararg val expressions: String) {
        FILE("-(f|F)", "-((f|F)(i|I)(l|L)(e|E))");

        infix fun test(receiver: String): Boolean {
            for (expression in expressions) {
                if (expression.toRegex().matches(receiver))
                    return true
            }
            return false
        }

        companion object {
            operator fun invoke(receiver: String) = when {
                FILE test receiver -> FILE
                else -> null
            }
        }
    }

    init {
        parse(*string)
    }

    fun parse(vararg args: String) {
        var i = 0

        while (i < args.size) {

            val arg = args[i++]

            when (Commands(arg)) {
                Commands.FILE -> configFilePath = args[i++]
                null -> {
                    println("Unrecognized command")

                }
            }

        }

    }

}