/**
 * D5700 CPU Implementation
 * 
 * Features:
 * - 8 general purpose 8-bit registers (r0-r7)
 * - Program Counter (P) - 16-bit register
 * - Timer (T) - 8-bit register
 * - Address (A) - 16-bit register  
 * - Memory flag (M) - single bit (0 for RAM, 1 for ROM)
 * - 500Hz execution speed
 * - 4KB RAM and ROM
 */
class CPU {
    
    // === REGISTERS ===
    
    // General purpose registers (r0-r7) - 8-bit each
    private val generalRegisters = IntArray(8) { 0 }
    
    // Program Counter (P) - 16-bit register storing current instruction address
    private var programCounter: Int = 0
    
    // Timer (T) - 8-bit register for timer value (decrements at 60Hz when not 0)
    private var timerRegister: Int = 0
    
    // Address (A) - 16-bit register for storing memory addresses
    private var addressRegister: Int = 0
    
    // Memory flag (M) - determines RAM (0) or ROM (1) operations
    private var memoryFlag: Boolean = false
    
    // === MEMORY REFERENCES ===
    // These will be injected later when we create RAM and ROM classes
    private var ram: ByteArray? = null
    private var rom: ByteArray? = null
    
    // === CPU CONTROL METHODS ===
    
    // Instruction factory for creating instruction instances
    private val instructionFactory = InstructionFactory()
    
    /**
     * Executes a single instruction cycle:
     * 1. Fetch instruction from ROM at PC
     * 2. Create appropriate instruction instance
     * 3. Execute instruction (parse, validate, perform, increment PC)
     */
    fun executeInstruction() {
        val instruction = fetchInstruction()
        
        try {
            // Create the appropriate instruction instance
            val instructionInstance = instructionFactory.createInstruction(instruction)
            
            // Execute the instruction (this handles parsing, validation, execution, and PC increment)
            instructionInstance.execute(this)
            
        } catch (e: Exception) {
            println("Instruction execution error at PC ${programCounter.toString(16).uppercase().padStart(4, '0')}: ${e.message}")
            throw e
        }
    }
    
    /**
     * Fetches the next 2-byte instruction from ROM at program counter address
     * @return 16-bit instruction as integer
     */
    fun fetchInstruction(): Int {
        // Ensure PC is even (instructions are 2 bytes)
        if (programCounter % 2 != 0) {
            throw IllegalStateException("Program counter must be even: $programCounter")
        }
        
        // TODO: Implement actual ROM reading when ROM class is available
        // For now, return a placeholder instruction
        val rom = this.rom ?: throw IllegalStateException("ROM not initialized")
        
        // Check if we can read 2 bytes (current PC and PC+1)
        if (programCounter >= rom.size - 1) {
            throw IllegalStateException("Program counter out of bounds: $programCounter (ROM size: ${rom.size})")
        }
        
        // Read 2 bytes from ROM and combine into 16-bit instruction
        val firstByte = rom[programCounter].toInt() and 0xFF
        val secondByte = rom[programCounter + 1].toInt() and 0xFF
        
        return (firstByte shl 8) or secondByte
    }
    
    // === PROGRAM COUNTER METHODS ===
    
    /**
     * Get the current program counter value
     * @return current PC value
     */
    fun getPC(): Int {
        return programCounter
    }
    
    /**
     * Set the program counter to a specific address
     * @param address new PC value (must be even)
     */
    fun setPC(address: Int) {
        if (address % 2 != 0) {
            throw IllegalArgumentException("PC address must be even: $address")
        }
        if (address < 0 || address > 0xFFFF) {
            throw IllegalArgumentException("PC address out of range: $address")
        }
        programCounter = address
    }
    
    /**
     * Increment the program counter by specified amount
     * @param amount amount to increment (typically 2 for normal instruction flow)
     */
    fun incrementPC(amount: Int) {
        val newPC = programCounter + amount
        if (newPC < 0 || newPC > 0xFFFF) {
            throw IllegalArgumentException("PC increment would cause overflow: $newPC")
        }
        programCounter = newPC
    }
    
    // === GENERAL REGISTER METHODS ===
    
    /**
     * Get the value from a general purpose register
     * @param index register index (0-7 for r0-r7)
     * @return register value (0-255)
     */
    fun getRegister(index: Int): Int {
        if (index < 0 || index >= generalRegisters.size) {
            throw IllegalArgumentException("Register index out of range: $index")
        }
        return generalRegisters[index]
    }
    
    /**
     * Set the value of a general purpose register
     * @param index register index (0-7 for r0-r7)
     * @param value new register value (0-255)
     */
    fun setRegister(index: Int, value: Int) {
        if (index < 0 || index >= generalRegisters.size) {
            throw IllegalArgumentException("Register index out of range: $index")
        }
        if (value < 0 || value > 0xFF) {
            throw IllegalArgumentException("Register value out of range: $value")
        }
        generalRegisters[index] = value
    }
    
    // === ADDRESS REGISTER METHODS ===
    
    /**
     * Get the current address register value
     * @return address register value (0-65535)
     */
    fun getAddressRegister(): Int {
        return addressRegister
    }
    
    /**
     * Set the address register to a specific value
     * @param address new address value (0-65535)
     */
    fun setAddressRegister(address: Int) {
        if (address < 0 || address > 0xFFFF) {
            throw IllegalArgumentException("Address register value out of range: $address")
        }
        addressRegister = address
    }
    
    // === TIMER REGISTER METHODS ===
    
    /**
     * Get the current timer register value
     * @return timer register value (0-255)
     */
    fun getTimerRegister(): Int {
        return timerRegister
    }
    
    /**
     * Set the timer register to a specific value
     * @param value new timer value (0-255)
     */
    fun setTimerRegister(value: Int) {
        if (value < 0 || value > 0xFF) {
            throw IllegalArgumentException("Timer register value out of range: $value")
        }
        timerRegister = value
    }
    
    /**
     * Decrements the timer register by 1 if not already 0
     * Called at 60Hz by the system timer
     */
    fun decrementTimer() {
        if (timerRegister > 0) {
            timerRegister--
        }
    }
    
    // === MEMORY FLAG METHODS ===
    
    /**
     * Get the current memory flag state
     * @return true for ROM operations, false for RAM operations
     */
    fun getMemoryFlag(): Boolean {
        return memoryFlag
    }
    
    /**
     * Toggle the memory flag between RAM (false) and ROM (true)
     */
    fun toggleMemoryFlag() {
        memoryFlag = !memoryFlag
    }
    
    /**
     * Set the memory flag explicitly
     * @param isROM true for ROM operations, false for RAM operations
     */
    fun setMemoryFlag(isROM: Boolean) {
        memoryFlag = isROM
    }
    
    // === MEMORY ACCESS METHODS ===
    
    /**
     * Read a byte from memory at the specified address
     * Uses memory flag to determine RAM or ROM access
     * @param address memory address to read from
     * @return byte value at address (0-255)
     */
    fun readMemory(address: Int): Int {
        if (address < 0 || address > 0xFFFF) {
            throw IllegalArgumentException("Memory address out of range: $address")
        }
        
        return if (memoryFlag) {
            // Read from ROM
            val rom = this.rom ?: throw IllegalStateException("ROM not initialized")
            if (address >= rom.size) {
                0 // Return 0 for addresses beyond ROM size
            } else {
                rom[address].toInt() and 0xFF
            }
        } else {
            // Read from RAM
            val ram = this.ram ?: throw IllegalStateException("RAM not initialized")
            if (address >= ram.size) {
                0 // Return 0 for addresses beyond RAM size
            } else {
                ram[address].toInt() and 0xFF
            }
        }
    }
    
    /**
     * Write a byte to memory at the specified address
     * Uses memory flag to determine RAM or ROM access
     * @param address memory address to write to
     * @param value byte value to write (0-255)
     */
    fun writeMemory(address: Int, value: Int) {
        if (address < 0 || address > 0xFFFF) {
            throw IllegalArgumentException("Memory address out of range: $address")
        }
        if (value < 0 || value > 0xFF) {
            throw IllegalArgumentException("Memory value out of range: $value")
        }
        
        if (memoryFlag) {
            // Attempt to write to ROM
            val rom = this.rom ?: throw IllegalStateException("ROM not initialized")
            if (address < rom.size) {
                // Most ROM chips are read-only, but some future cartridges may be writable
                // For now, we'll allow writes but log them
                println("Warning: Writing to ROM address $address with value $value")
                // TODO: In future, check if ROM chip supports writing
                rom[address] = value.toByte()
            }
        } else {
            // Write to RAM
            val ram = this.ram ?: throw IllegalStateException("RAM not initialized")
            if (address < ram.size) {
                ram[address] = value.toByte()
            }
        }
    }
    
    // === INITIALIZATION METHODS ===
    
    /**
     * Initialize the CPU with RAM and ROM references
     * @param ram 4KB RAM array
     * @param rom 4KB ROM array
     */
    fun initialize(ram: ByteArray, rom: ByteArray) {
        this.ram = ram
        this.rom = rom
        
        // Reset CPU state
        reset()
    }
    
    /**
     * Reset the CPU to initial state
     */
    fun reset() {
        // Clear all general registers
        for (i in generalRegisters.indices) {
            generalRegisters[i] = 0
        }
        
        // Reset special registers
        programCounter = 0
        timerRegister = 0
        addressRegister = 0
        memoryFlag = false // Start with RAM access
    }
    
    // === DEBUG METHODS ===
    
    /**
     * Get a string representation of current CPU state
     */
    fun getStateString(): String {
        val registers = generalRegisters.mapIndexed { index, value -> 
            "r$index=${value.toString(16).uppercase().padStart(2, '0')}" 
        }.joinToString(", ")
        
        return "CPU State:\n" +
               "  Registers: $registers\n" +
               "  PC: ${programCounter.toString(16).uppercase().padStart(4, '0')}\n" +
               "  T: ${timerRegister.toString(16).uppercase().padStart(2, '0')}\n" +
               "  A: ${addressRegister.toString(16).uppercase().padStart(4, '0')}\n" +
               "  M: ${if (memoryFlag) "ROM" else "RAM"}"
    }
}