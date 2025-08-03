class Computer {
    
    private val ram: RAM = RAM(4096)  // 4KB RAM
    private val rom: ROM = ROM(4096)  // 4KB ROM  
    private val screen: Screen = Screen()
    private val cpu: CPU = CPU()

    private var isRunning: Boolean = false
    private var programLoaded: Boolean = false
    private var programSize: Int = 0
    private var executionStarted: Boolean = false
    private var hasTerminated: Boolean = false
    
    private val keyboardInputQueue = mutableListOf<Int>()
    
    private var instructionsExecuted: Long = 0
    private var lastExecutionTime: Long = System.currentTimeMillis()
    
    private var lastTimerDecrement: Long = System.currentTimeMillis()
    
    init {
        // Initialize CPU with RAM and ROM references
        cpu.initialize(ram.getMemoryArray(), rom.getMemoryArray())
    }
    
    /**
     * Load ROM data into the computer
     * @param data array of integers representing the program (each int should be 0-255)
     */
    fun loadROM(data: IntArray) {
        rom.loadProgram(data)
        programLoaded = true
        programSize = data.size
        
        // Reset computer state when new program is loaded
        reset()
        
        println("Program loaded: ${data.size} bytes")
    }
    
    /**
     * Execute a single instruction
     * This method is called to step through the program one instruction at a time
     * Handles special instructions that need access to Computer components
     */
    fun executeInstruction() {
        if (!programLoaded) {
            throw IllegalStateException("No program loaded in ROM")
        }
        
        // If program has already terminated, don't execute any more instructions
        if (hasTerminated) {
            return
        }
        
        try {
            // Mark that execution has started
            if (!executionStarted) {
                executionStarted = true
            }
            
            // Check for program termination condition
            if (executionStarted && cpu.getPC() == 0 && instructionsExecuted > 0) {
                hasTerminated = true
                throw IllegalStateException("Program terminated: PC reached 0")
            }

            if (cpu.getPC() >= programSize) {
                hasTerminated = true
                throw IllegalStateException("Program terminated: PC beyond program bounds (PC: ${cpu.getPC()}, Program size: $programSize)")
            }

            val currentTime = System.currentTimeMillis()
            val timerValue = cpu.getTimerRegister()
            
            if (timerValue > 0 && currentTime - lastTimerDecrement >= 17) { // ~60Hz (16.67ms)
                cpu.setTimerRegister(timerValue - 1)
                lastTimerDecrement = currentTime
            }
            
            // Fetch the instruction to check if it needs special handling
            val instruction = cpu.fetchInstruction()
            val opcode = (instruction shr 12) and 0x0F
            
            when (opcode) {
                0x6 -> executeReadKeyboardInstruction(instruction)
                0xB -> executeSetTimerInstruction(instruction)
                0xF -> executeDrawInstruction(instruction)
                else -> {
                    // Execute normal CPU instruction
                    cpu.executeInstruction()
                }
            }
            
            // Update instruction counter
            instructionsExecuted++
            lastExecutionTime = System.currentTimeMillis()
            
        } catch (e: Exception) {
            val message = e.message ?: "Unknown error"
            if (message.contains("Program terminated:")) {
                println("Program execution completed successfully.")
                isRunning = false
                // Don't rethrow for normal program termination
            } else {
                println("Execution error: $message")
                isRunning = false
                throw e
            }
        }
    }
    
    /**
     * Handle READ_KEYBOARD instruction with actual keyboard input
     */
    private fun executeReadKeyboardInstruction(instruction: Int) {
        val parser = InstructionParser(instruction)
        val registerIndex = parser.getFirstOperand()
        
        // Check if have queued input first
        val queuedInput = readKeyboard()
        
        if (queuedInput >= 0) {
            // Input available from queue - store it and continue
            cpu.setRegister(registerIndex, queuedInput)
            cpu.incrementPC(2)
        } else {
            // No queued input - prompt user for interactive input
            print("Waiting for keyboard input: ")
            System.out.flush()
            
            try {
                val input = readLine()
                if (input != null && input.isNotEmpty()) {
                    // Parse input as hexadecimal number
                    val hexValue = try {
                        input.trim().toInt(16)
                    } catch (e: NumberFormatException) {
                        try {
                            input.trim().toInt(10)
                        } catch (e2: NumberFormatException) {
                            input[0].code
                        }
                    }
                    // Ensure value fits in 8-bit register (0-255)
                    cpu.setRegister(registerIndex, hexValue and 0xFF)
                } else {
                    cpu.setRegister(registerIndex, 0)
                }
            } catch (e: Exception) {
                println("Error reading input: ${e.message}")
                cpu.setRegister(registerIndex, 0)
            }
            
            cpu.incrementPC(2)
        }
    }
    
    /**
     * Handle SET_T instruction with proper timer synchronization
     */
    private fun executeSetTimerInstruction(instruction: Int) {
        val parser = InstructionParser(instruction)
        val timerValue = parser.getByteOperand()
        
        // Validate the timer value
        if (timerValue < 0 || timerValue > 0xFF) {
            throw IllegalStateException("Program terminated: SET_T timer value out of range: $timerValue (must be 0-255)")
        }
        
        // Set the CPU timer register (T register)
        cpu.setTimerRegister(timerValue)
        
        // Reset timer
        lastTimerDecrement = System.currentTimeMillis()

        cpu.incrementPC(2)
    }
    
    /**
     * Handle DRAW instruction with actual screen access
     */
    private fun executeDrawInstruction(instruction: Int) {
        val parser = InstructionParser(instruction)
        val asciiCharReg = parser.getFirstOperand()
        val rowReg = parser.getSecondOperand()
        val columnReg = parser.getThirdOperand()
        
        val asciiChar = cpu.getRegister(asciiCharReg)
        val row = rowReg
        val column = columnReg

        if (asciiChar > 0x7F) {
            throw IllegalStateException("Program terminated: DRAW ASCII character out of range: $asciiChar (must be 0-127)")
        }
        if (row > 7) {
            throw IllegalStateException("Program terminated: DRAW row out of bounds: $row (must be 0-7)")
        }
        if (column > 7) {
            throw IllegalStateException("Program terminated: DRAW column out of bounds: $column (must be 0-7)")
        }
        
        // Draw to screen
        screen.draw(asciiChar, row, column)
        cpu.incrementPC(2) // Increment PC manually
    }

    /**
     * Reset the computer to initial state
     */
    fun reset() {
        // Reset all components
        cpu.reset()
        ram.clear()
        screen.clear()
        // Timer is just CPU's T register - reset via cpu.reset()
        
        // Reset computer state
        isRunning = false
        instructionsExecuted = 0
        executionStarted = false
        hasTerminated = false
        keyboardInputQueue.clear()
        lastExecutionTime = System.currentTimeMillis()
        lastTimerDecrement = System.currentTimeMillis()
        
        println("D5700 Computer reset")
    }
    
    /**
     * Get the current screen output as a formatted string
     * @return formatted string representation of the 8x8 display
     */
    fun getScreenOutput(): String {
        return screen.getFormattedDisplay()
    }
    
    /**
     * Check if the computer is currently running
     * @return true if running, false if stopped
     */
    fun isRunning(): Boolean {
        return isRunning && !hasTerminated
    }
    
    /**
     * Check if the program has terminated
     * @return true if terminated, false otherwise
     */
    fun hasTerminated(): Boolean {
        return hasTerminated
    }
    
    /**
     * Get a register value from the CPU
     * @param index register index (0-7 for r0-r7)
     * @return register value (0-255)
     */
    fun getRegisterValue(index: Int): Int {
        return cpu.getRegister(index)
    }
    
    /**
     * Set the timer value (T register)
     * @param value new timer value (0-255)
     */
    fun setTimerValue(value: Int) {
        cpu.setTimerRegister(value)
        lastTimerDecrement = System.currentTimeMillis()
    }
    
    /**
     * Simulate keyboard input
     * @return next keyboard input value (0-15 for hex digits), or -1 if no input
     */
    fun readKeyboard(): Int {
        return if (keyboardInputQueue.isNotEmpty()) {
            keyboardInputQueue.removeAt(0)
        } else {
            -1 // No input available
        }
    }

    /**
     * Add keyboard input to the input queue (for simulation)
     * @param hexValue hex digit value (0-15)
     */
    fun addKeyboardInput(hexValue: Int) {
        if (hexValue < 0 || hexValue > 0xF) {
            throw IllegalArgumentException("Keyboard input must be hex digit (0-15): $hexValue")
        }
        keyboardInputQueue.add(hexValue)
    }
    
    /**
     * Add keyboard input from string (for simulation)
     * @param hexString string of hex digits (e.g., "1A2F")
     */
    fun addKeyboardInput(hexString: String) {
        hexString.forEach { char ->
            val hexValue = when (char.uppercaseChar()) {
                in '0'..'9' -> char - '0'
                in 'A'..'F' -> char.uppercaseChar() - 'A' + 10
                else -> throw IllegalArgumentException("Invalid hex character: $char")
            }
            keyboardInputQueue.add(hexValue)
        }
    }
    
    /**
     * Get the current timer value (T register)
     * @return current timer value (0-255)
     */
    fun getTimerValue(): Int {
        return cpu.getTimerRegister()
    }
    
    /**
     * Get the current program counter value
     * @return current PC value
     */
    fun getProgramCounter(): Int {
        return cpu.getPC()
    }

    /**
     * Check if a program is loaded
     * @return true if program is loaded, false otherwise
     */
    fun isProgramLoaded(): Boolean {
        return programLoaded
    }

    /**
     * Get a comprehensive system status
     * @return detailed system status string  
     */
    fun getSystemStatus(): String {
        val sb = StringBuilder()
        sb.append("=== D5700 Computer Status ===\n")
        sb.append("Running: ${if (isRunning) "YES" else "NO"}\n")
        sb.append("Program Loaded: ${if (programLoaded) "YES" else "NO"}\n")
        sb.append("Instructions Executed: $instructionsExecuted\n")
        sb.append("Keyboard Queue: ${keyboardInputQueue.size} items\n")
        sb.append("\n")
        sb.append(cpu.getStateString())
        sb.append("\n\n")
        sb.append("Timer (T register): ${cpu.getTimerRegister()}")
        sb.append("\n\n")
        sb.append("Screen Output:\n")
        sb.append(screen.getFormattedDisplay())
        
        return sb.toString()
    }
    
    /**
     * Read from RAM at specified address (for debugging)
     * @param address RAM address
     * @return byte value at address
     */
    fun readRAM(address: Int): Int {
        return ram.read(address)
    }
    
    /**
     * Write to RAM at specified address (for debugging)
     * @param address RAM address
     * @param value byte value to write
     */
    fun writeRAM(address: Int, value: Int) {
        ram.write(address, value)
    }
    
    /**
     * Read from ROM at specified address (for debugging)
     * @param address ROM address  
     * @return byte value at address
     */
    fun readROM(address: Int): Int {
        return rom.read(address)
    }
    
    /**
     * Get direct access to screen for advanced operations
     * @return reference to screen object
     */
    fun getScreen(): Screen {
        return screen
    }
}