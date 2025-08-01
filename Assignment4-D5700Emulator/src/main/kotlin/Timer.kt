/**
 * Timer Implementation for D5700
 * 
 * The D5700 timer decrements at 60Hz (every 16ms) when the value is not 0.
 * This class manages the timer functionality for the CPU.
 */
class Timer {
    
    // Timer value (0-255, 8-bit)
    private var time: Int = 0
    
    // Flag to track if timer is enabled/running
    private var isRunning: Boolean = false
    
    // Timestamp for last decrement (for simulation purposes)
    private var lastDecrementTime: Long = System.currentTimeMillis()
    
    /**
     * Get the current timer value
     * @return current timer value (0-255)
     */
    fun getTime(): Int {
        return time
    }
    
    /**
     * Set the timer value
     * @param value new timer value (0-255)
     */
    fun setTime(value: Int) {
        if (value < 0 || value > 0xFF) {
            throw IllegalArgumentException("Timer value out of bounds: $value (must be 0-255)")
        }
        time = value
        
        // Timer starts running when set to non-zero value
        isRunning = (value > 0)
        lastDecrementTime = System.currentTimeMillis()
    }
    
    /**
     * Manually decrement the timer by 1 if not already 0
     * This is called by the CPU at 60Hz simulation
     */
    fun decrement() {
        if (time > 0) {
            time--
            lastDecrementTime = System.currentTimeMillis()
            
            // Stop running when reaches 0
            if (time == 0) {
                isRunning = false
            }
        }
    }
    
    /**
     * Update timer based on real time (for more accurate simulation)
     * This method calculates how many decrements should have occurred
     * based on the 60Hz rate (16.67ms per decrement)
     */
    fun updateTimer() {
        if (!isRunning || time == 0) {
            return
        }
        
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastDecrementTime
        
        // Calculate how many 60Hz cycles have passed (16.67ms per cycle)
        val cyclesPassed = (elapsedTime / 16.67).toInt()
        
        if (cyclesPassed > 0) {
            // Decrement by the number of cycles that have passed
            val newTime = maxOf(0, time - cyclesPassed)
            time = newTime
            lastDecrementTime = currentTime
            
            // Stop running when reaches 0
            if (time == 0) {
                isRunning = false
            }
        }
    }
    
    /**
     * Check if the timer is currently running (non-zero)
     * @return true if timer is running, false if stopped
     */
    fun isRunning(): Boolean {
        return isRunning && time > 0
    }
    
    /**
     * Reset the timer to 0 and stop it
     */
    fun reset() {
        time = 0
        isRunning = false
        lastDecrementTime = System.currentTimeMillis()
    }
    
    /**
     * Get the time remaining in milliseconds (approximate)
     * Based on 60Hz decrement rate
     * @return approximate milliseconds remaining
     */
    fun getTimeRemainingMs(): Long {
        if (time == 0) {
            return 0
        }
        // Each count represents 1/60th of a second = 16.67ms
        return (time * 16.67).toLong()
    }
    
    /**
     * Get the time remaining in seconds (approximate)
     * @return approximate seconds remaining
     */
    fun getTimeRemainingSec(): Double {
        if (time == 0) {
            return 0.0
        }
        // Each count represents 1/60th of a second
        return time / 60.0
    }
    
    /**
     * Get a string representation of the timer state
     */
    override fun toString(): String {
        val status = if (isRunning) "RUNNING" else "STOPPED"
        val timeMs = getTimeRemainingMs()
        return "Timer: $time ($status, ~${timeMs}ms remaining)"
    }
}