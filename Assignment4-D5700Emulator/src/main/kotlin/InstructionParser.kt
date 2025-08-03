class InstructionParser(private val instruction: Int) {
    
    private val firstByte = (instruction shr 8) and 0xFF
    private val secondByte = instruction and 0xFF
    
    /**
     * Get the operation code (first nibble)
     * Performs shift right by 4 on first byte
     */
    fun getOperation(): Int {
        return (firstByte shr 4) and 0x0F
    }
    
    /**
     * Get the first operand (second nibble of first byte)
     * Usually represents a register (rX)
     */
    fun getFirstOperand(): Int {
        return firstByte and 0x0F
    }
    
    /**
     * Get the second operand (first nibble of second byte)
     * Usually represents a register (rY) or part of a byte/address
     */
    fun getSecondOperand(): Int {
        return (secondByte shr 4) and 0x0F
    }
    
    /**
     * Get the third operand (second nibble of second byte)
     * Usually represents a register (rZ) or part of a byte/address
     */
    fun getThirdOperand(): Int {
        return secondByte and 0x0F
    }
    
    /**
     * Get a full byte operand from the second byte
     * Used for instructions that take a byte parameter (bb)
     */
    fun getByteOperand(): Int {
        return secondByte
    }
    
    /**
     * Get a 3-nibble address (12-bit address)
     * Takes first operand, shifts left by 8, ORs with second byte
     * Used for instructions like JUMP (5, aaa) and SET_A (A, aaa)
     */
    fun getAddressOperand(): Int {
        return (getFirstOperand() shl 8) or secondByte
    }
    
    /**
     * Get the raw instruction value
     */
    fun getRawInstruction(): Int {
        return instruction
    }
    
    /**
     * Get a human-readable representation of the instruction
     */
    override fun toString(): String {
        return "Instruction: 0x${instruction.toString(16).uppercase().padStart(4, '0')} " +
               "(Op: ${getOperation().toString(16).uppercase()}, " +
               "Operands: ${getFirstOperand().toString(16).uppercase()}, " +
               "${getSecondOperand().toString(16).uppercase()}, " +
               "${getThirdOperand().toString(16).uppercase()})"
    }
}