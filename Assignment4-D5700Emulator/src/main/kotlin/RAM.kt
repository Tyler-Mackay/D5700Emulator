/**
 * RAM (Random Access Memory) Implementation for D5700
 * 
 * The D5700 has 4KB of RAM for storing data and variables.
 * RAM is volatile memory that can be read from and written to.
 */
class RAM(private val size: Int = 4096) { // 4KB default size
    
    private val memory = ByteArray(size)
    
    /**
     * Read a byte from RAM at the specified address
     * @param address memory address (0 to size-1)
     * @return byte value at address (0-255)
     */
    fun read(address: Int): Int {
        if (address < 0 || address >= size) {
            throw IllegalArgumentException("RAM address out of bounds: $address (size: $size)")
        }
        return memory[address].toInt() and 0xFF
    }
    
    /**
     * Write a byte to RAM at the specified address
     * @param address memory address (0 to size-1)
     * @param value byte value to write (0-255)
     */
    fun write(address: Int, value: Int) {
        if (address < 0 || address >= size) {
            throw IllegalArgumentException("RAM address out of bounds: $address (size: $size)")
        }
        if (value < 0 || value > 0xFF) {
            throw IllegalArgumentException("RAM value out of bounds: $value")
        }
        memory[address] = value.toByte()
    }
    
    /**
     * Clear all RAM contents to zero
     */
    fun clear() {
        memory.fill(0)
    }
    
    /**
     * Get the size of RAM in bytes
     * @return RAM size in bytes
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
     * Load data into RAM starting at specified address
     * @param startAddress starting address to load data
     * @param data array of bytes to load
     */
    fun loadData(startAddress: Int, data: ByteArray) {
        if (startAddress < 0 || startAddress >= size) {
            throw IllegalArgumentException("RAM start address out of bounds: $startAddress")
        }
        if (startAddress + data.size > size) {
            throw IllegalArgumentException("Data too large for RAM: ${data.size} bytes at address $startAddress")
        }
        
        data.forEachIndexed { index, byte ->
            memory[startAddress + index] = byte
        }
    }
    
    /**
     * Get a string representation of RAM contents (for debugging)
     * Shows first 256 bytes in hex format
     */
    override fun toString(): String {
        val sb = StringBuilder("RAM Contents (first 256 bytes):\n")
        for (i in 0 until minOf(256, size)) {
            if (i % 16 == 0) {
                sb.append("\n${i.toString(16).uppercase().padStart(4, '0')}: ")
            }
            sb.append("${memory[i].toInt().and(0xFF).toString(16).uppercase().padStart(2, '0')} ")
        }
        return sb.toString()
    }
}