interface MotionEventListener : EventListener {
    var x: Int
    var y: Int
    var friction: Float

    fun updateX(x: Int) {
        this.x = x
        onXUpdate(x)
    }

    fun onXUpdate(x: Int) {

    }

    fun updateY(y: Int) {
        this.y = y
    }
}

interface MotionEventPressured : MotionEventListener, PressureEvent


interface MultiTouchEvent : MotionEventListener

interface PressureEvent {
    val pressure: Int
}