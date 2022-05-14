data class ButtonPress(
    val key: Int, val state: State
) : Event {

    enum class State(val indicator: Int) {
        UP(0),
        DOWN(1);

        companion object {
            operator fun invoke(state: Int) = when (state) {
                1 -> DOWN
                else -> UP
            }

            operator fun invoke(state: String) = when (state) {
                "button_down","key_down" -> DOWN
                "button_up","key_up" -> UP
                "button" -> UP
                else -> UP // TODO think I should throw maybe
            }
        }
    }

    companion object {
        operator fun invoke(key: Int, state: Int) = ButtonPress(
            key,
            State(state)
        )
    }

}