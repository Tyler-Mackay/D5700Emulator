/**
 * Screen Implementation for D5700
 * 
 * The D5700 has an 8x8 ASCII display with 64 bytes of internal RAM
 * that serves as the frame buffer for ASCII characters at each position.
 */
class Screen {
    
    companion object {
        const val SCREEN_WIDTH = 8
        const val SCREEN_HEIGHT = 8
        const val SCREEN_SIZE = SCREEN_WIDTH * SCREEN_HEIGHT // 64 bytes
    }
    
    // Frame buffer - 64 bytes of internal RAM for ASCII characters
    private val frameBuffer = ByteArray(SCREEN_SIZE) { 0x20 } // Initialize with spaces (0x20)
    
    /**
     * Draw an ASCII character at the specified row and column
     * @param asciiChar ASCII character value (0-127)
     * @param row screen row (0-7)
     * @param column screen column (0-7)
     */
    fun draw(asciiChar: Int, row: Int, column: Int) {
        // Validate ASCII character
        if (asciiChar < 0 || asciiChar > 0x7F) {
            throw IllegalArgumentException("ASCII character out of range: $asciiChar (must be 0-127)")
        }
        
        // Validate screen coordinates
        if (row < 0 || row >= SCREEN_HEIGHT) {
            throw IllegalArgumentException("Screen row out of bounds: $row (must be 0-${SCREEN_HEIGHT-1})")
        }
        if (column < 0 || column >= SCREEN_WIDTH) {
            throw IllegalArgumentException("Screen column out of bounds: $column (must be 0-${SCREEN_WIDTH-1})")
        }
        
        // Calculate frame buffer address from row and column
        val address = row * SCREEN_WIDTH + column
        frameBuffer[address] = asciiChar.toByte()
    }
    
    /**
     * Get the display output as a formatted string
     * @return string representation of the 8x8 display
     */
    fun getDisplay(): String {
        val sb = StringBuilder()
        
        // Add top border
        sb.append("╔").append("═".repeat(SCREEN_WIDTH)).append("╗\n")
        
        // Add screen content row by row
        for (row in 0 until SCREEN_HEIGHT) {
            sb.append("║")
            for (column in 0 until SCREEN_WIDTH) {
                val address = row * SCREEN_WIDTH + column
                val asciiValue = frameBuffer[address].toInt() and 0xFF
                
                // Convert ASCII value to character, use space for unprintable characters
                val char = if (asciiValue in 32..126) {
                    asciiValue.toChar()
                } else {
                    ' ' // Use space for non-printable characters
                }
                sb.append(char)
            }
            sb.append("║\n")
        }
        
        // Add bottom border
        sb.append("╚").append("═".repeat(SCREEN_WIDTH)).append("╝")
        
        return sb.toString()
    }
    
    /**
     * Clear the screen by filling with spaces (0x20)
     */
    fun clear() {
        frameBuffer.fill(0x20) // Fill with space characters
    }
    
    /**
     * Get the ASCII value at a specific screen position
     * @param row screen row (0-7)
     * @param column screen column (0-7)
     * @return ASCII value at the position
     */
    fun getCharAt(row: Int, column: Int): Int {
        if (row < 0 || row >= SCREEN_HEIGHT) {
            throw IllegalArgumentException("Screen row out of bounds: $row")
        }
        if (column < 0 || column >= SCREEN_WIDTH) {
            throw IllegalArgumentException("Screen column out of bounds: $column")
        }
        
        val address = row * SCREEN_WIDTH + column
        return frameBuffer[address].toInt() and 0xFF
    }
    
    /**
     * Write directly to frame buffer at calculated address
     * Used by CPU memory operations when writing to screen memory
     * @param address frame buffer address (0-63)
     * @param asciiChar ASCII character value (0-127)
     */
    fun writeToFrameBuffer(address: Int, asciiChar: Int) {
        if (address < 0 || address >= SCREEN_SIZE) {
            throw IllegalArgumentException("Frame buffer address out of bounds: $address")
        }
        if (asciiChar < 0 || asciiChar > 0x7F) {
            throw IllegalArgumentException("ASCII character out of range: $asciiChar")
        }
        
        frameBuffer[address] = asciiChar.toByte()
    }
    
    /**
     * Read directly from frame buffer at calculated address
     * Used by CPU memory operations when reading from screen memory
     * @param address frame buffer address (0-63)
     * @return ASCII character value at address
     */
    fun readFromFrameBuffer(address: Int): Int {
        if (address < 0 || address >= SCREEN_SIZE) {
            throw IllegalArgumentException("Frame buffer address out of bounds: $address")
        }
        
        return frameBuffer[address].toInt() and 0xFF
    }
    
    /**
     * Get the frame buffer size
     * @return frame buffer size in bytes (64)
     */
    fun getFrameBufferSize(): Int {
        return SCREEN_SIZE
    }
    
    /**
     * Get direct access to frame buffer array (for debugging)
     * @return reference to internal frame buffer array
     */
    fun getFrameBuffer(): ByteArray {
        return frameBuffer
    }
    
    /**
     * Load a pattern or image into the screen
     * @param pattern array of ASCII values to display (max 64 characters)
     */
    fun loadPattern(pattern: IntArray) {
        if (pattern.size > SCREEN_SIZE) {
            throw IllegalArgumentException("Pattern too large: ${pattern.size} (max: $SCREEN_SIZE)")
        }
        
        // Clear screen first
        clear()
        
        // Load pattern
        pattern.forEachIndexed { index, asciiValue ->
            if (asciiValue < 0 || asciiValue > 0x7F) {
                throw IllegalArgumentException("Invalid ASCII value in pattern: $asciiValue at index $index")
            }
            frameBuffer[index] = asciiValue.toByte()
        }
    }
    
    /**
     * Get screen dimensions
     * @return Pair of (width, height)
     */
    fun getDimensions(): Pair<Int, Int> {
        return Pair(SCREEN_WIDTH, SCREEN_HEIGHT)
    }
    
    /**
     * Get a raw text representation of the screen (without borders)
     * @return 8x8 grid of characters
     */
    fun getRawDisplay(): String {
        val sb = StringBuilder()
        for (row in 0 until SCREEN_HEIGHT) {
            for (column in 0 until SCREEN_WIDTH) {
                val address = row * SCREEN_WIDTH + column
                val asciiValue = frameBuffer[address].toInt() and 0xFF
                val char = if (asciiValue in 32..126) {
                    asciiValue.toChar()
                } else {
                    ' '
                }
                sb.append(char)
            }
            if (row < SCREEN_HEIGHT - 1) {
                sb.append('\n')
            }
        }
        return sb.toString()
    }
    
    /**
     * Get formatted display for D5700 emulator using # for blank spaces
     * @return 8x8 grid with # characters for spaces
     */
    fun getFormattedDisplay(): String {
        val sb = StringBuilder()
        for (row in 0 until SCREEN_HEIGHT) {
            for (column in 0 until SCREEN_WIDTH) {
                val address = row * SCREEN_WIDTH + column
                val asciiValue = frameBuffer[address].toInt() and 0xFF
                
                val char = when {
                    asciiValue == 0x20 || asciiValue == 0x00 -> '#' // Use # for spaces and null chars
                    asciiValue in 33..126 -> asciiValue.toChar() // Printable ASCII characters
                    else -> '#' // Use # for non-printable characters
                }
                sb.append(char)
            }
            if (row < SCREEN_HEIGHT - 1) {
                sb.append('\n')
            }
        }
        return sb.toString()
    }
}