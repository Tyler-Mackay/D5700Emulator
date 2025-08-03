/**
 * ROM (Read-Only Memory) Implementation for D5700
 * 
 * The D5700 reads programs from 4KB ROM cartridges.
 * ROM is typically read-only, but some future cartridges may support writing.
 */
class ROM(private val size: Int = 4096) { // 4KB default size
    
    private val memory = ByteArray(size)
    
    // Flag to indicate if this ROM supports writing (future-proofing)
    private var isWritable = false
    
    /**
     * Read a byte from ROM at the specified address
     * @param address memory address (0 to size-1)
     * @return byte value at address (0-255)
     */
    fun read(address: Int): Int {
        if (address < 0 || address >= size) {
            throw IllegalArgumentException("ROM address out of bounds: $address (size: $size)")
        }
        return memory[address].toInt() and 0xFF
    }
    
    /**
     * Write a byte to ROM at the specified address
     * Most ROM chips are read-only and will throw an exception.
     * Some future cartridges may support writing.
     * @param address memory address (0 to size-1)
     * @param value byte value to write (0-255)
     */
    fun write(address: Int, value: Int) {
        if (address < 0 || address >= size) {
            throw IllegalArgumentException("ROM address out of bounds: $address (size: $size)")
        }
        if (value < 0 || value > 0xFF) {
            throw IllegalArgumentException("ROM value out of bounds: $value")
        }
        
        if (!isWritable) {
            throw IllegalStateException("ROM write operation failed: ROM chip is read-only")
        }
        
        memory[address] = value.toByte()
    }
    
    /**
     * Load program data into ROM (cartridge loading)
     * @param data array of bytes representing the program
     */
    fun loadProgram(data: ByteArray) {
        if (data.size > size) {
            throw IllegalArgumentException("Program too large for ROM: ${data.size} bytes (max: $size)")
        }
        
        // Clear ROM first
        memory.fill(0)
        
        // Load program data
        data.forEachIndexed { index, byte ->
            memory[index] = byte
        }
    }
    
    /**
     * Load program data from integer array (convenience method)
     * @param data array of integers representing the program (each int should be 0-255)
     */
    fun loadProgram(data: IntArray) {
        if (data.size > size) {
            throw IllegalArgumentException("Program too large for ROM: ${data.size} bytes (max: $size)")
        }
        
        // Clear ROM first
        memory.fill(0)
        
        // Load program data with validation
        data.forEachIndexed { index, value ->
            if (value < 0 || value > 0xFF) {
                throw IllegalArgumentException("Invalid byte value in program data: $value at index $index")
            }
            memory[index] = value.toByte()
        }
    }
    
    /**
     * Set whether this ROM supports writing (for future cartridges)
     * @param writable true if ROM supports writing, false for read-only
     */
    fun setWritable(writable: Boolean) {
        isWritable = writable
    }
    
    /**
     * Check if this ROM supports writing
     * @return true if ROM supports writing, false if read-only
     */
    fun isWritable(): Boolean {
        return isWritable
    }
    
    /**
     * Get the size of ROM in bytes
     * @return ROM size in bytes
     */
    fun getSize(): Int {
        return size
    }
    
    /**
     * Get direct access to memory array (for CPU initialization)
     * @return reference to internal memory array
     */
    fun getMemoryArray(): ByteArray {
        return memory
    }
    
    /**
     * Clear all ROM contents to zero
     */
    fun clear() {
        memory.fill(0)
    }
    
    /**
     * Get a string representation of ROM contents (for debugging)
     * Shows first 256 bytes in hex format
     */
    override fun toString(): String {
        val sb = StringBuilder("ROM Contents (first 256 bytes):\n")
        for (i in 0 until minOf(256, size)) {
            if (i % 16 == 0) {
                sb.append("\n${i.toString(16).uppercase().padStart(4, '0')}: ")
            }
            sb.append("${memory[i].toInt().and(0xFF).toString(16).uppercase().padStart(2, '0')} ")
        }
        return sb.toString()
    }
}