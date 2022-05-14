import interop.io.execute
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString

fun main(vararg args: String) {

    Args(*args).apply {

        val commandsListenerListener = InputService()

        val path = if (configFilePath.trim().startsWith("~")) buildString {
            val echo = execute("echo $configFilePath", staticCFunction { it -> })?.toKString()
                ?: throw RuntimeException("Invalid config file path passed $configFilePath")

            for (char in echo) { // A problem with result having a new line and an extra character not sure what it is yet
                if (char == '\n') break else this.append(char)
            }

        } else configFilePath

        configs.read(path)

        commandsListenerListener.run()

    }

}

