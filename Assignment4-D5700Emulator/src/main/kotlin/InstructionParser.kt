/**
 * InstructionParser - Parses D5700 CPU instructions
 * 
 * The D5700 uses 2-byte instructions where:
 * - First nibble (bits 15-12): Operation code (0-F)
 * - Remaining nibbles: Operands depending on instruction format
 */
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

/**
 * Enum representing all D5700 instruction types
 */
//enum class InstructionType(val opcode: Int, val name: String) {
//    STORE(0x0, "STORE"),
//    ADD(0x1, "ADD"),
//    SUB(0x2, "SUB"),
//    READ(0x3, "READ"),
//    WRITE(0x4, "WRITE"),
//    JUMP(0x5, "JUMP"),
//    READ_KEYBOARD(0x6, "READ_KEYBOARD"),
//    SWITCH_MEMORY(0x7, "SWITCH_MEMORY"),
//    SKIP_EQUAL(0x8, "SKIP_EQUAL"),
//    SKIP_NOT_EQUAL(0x9, "SKIP_NOT_EQUAL"),
//    SET_A(0xA, "SET_A"),
//    SET_T(0xB, "SET_T"),
//    READ_T(0xC, "READ_T"),
//    CONVERT_TO_BASE_10(0xD, "CONVERT_TO_BASE_10"),
//    CONVERT_BYTE_TO_ASCII(0xE, "CONVERT_BYTE_TO_ASCII"),
//    DRAW(0xF, "DRAW");
//
//    companion object {
//        fun fromOpcode(opcode: Int): InstructionType? {
//            return values().find { it.opcode == opcode }
//        }
//    }
//}