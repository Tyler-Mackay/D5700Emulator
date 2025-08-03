/**
 * Complete D5700 Instruction Set Implementation
 * 
 * This file contains all 16 D5700 instruction implementations and the factory:
 * Basic: STORE, ADD, SUB, READ, WRITE
 * Control: JUMP, SKIP_EQUAL, SKIP_NOT_EQUAL
 * System: READ_KEYBOARD, SWITCH_MEMORY, SET_A, SET_T, READ_T
 * Special: CONVERT_TO_BASE_10, CONVERT_BYTE_TO_ASCII, DRAW
 */

// === INSTRUCTION FACTORY ===

/**
 * InstructionFactory - Factory class for creating D5700 instruction instances
 * 
 * This factory creates the appropriate instruction object based on the opcode
 * extracted from the 2-byte instruction.
 */
class InstructionFactory {
    
    /**
     * Create an instruction instance based on the instruction opcode
     * @param instruction 16-bit instruction value
     * @return appropriate InstructionTemplate subclass instance
     */
    fun createInstruction(instruction: Int): InstructionTemplate {
        val opcode = extractOpcode(instruction)
        
        return when (opcode) {
            0x0 -> StoreInstruction(instruction)
            0x1 -> AddInstruction(instruction)
            0x2 -> SubInstruction(instruction)
            0x3 -> ReadInstruction(instruction)
            0x4 -> WriteInstruction(instruction)
            0x5 -> JumpInstruction(instruction)
            0x6 -> ReadKeyboardInstruction(instruction)
            0x7 -> SwitchMemoryInstruction(instruction)
            0x8 -> SkipEqualInstruction(instruction)
            0x9 -> SkipNotEqualInstruction(instruction)
            0xA -> SetAInstruction(instruction)
            0xB -> SetTInstruction(instruction)
            0xC -> ReadTInstruction(instruction)
            0xD -> ConvertToBase10Instruction(instruction)
            0xE -> ConvertByteToAsciiInstruction(instruction)
            0xF -> DrawInstruction(instruction)
            else -> throw IllegalArgumentException("Unknown instruction opcode: 0x${opcode.toString(16).uppercase()}")
        }
    }
    
    /**
     * Extract the opcode (first nibble) from the instruction
     * @param instruction 16-bit instruction value
     * @return opcode (0-15)
     */
    private fun extractOpcode(instruction: Int): Int {
        return (instruction shr 12) and 0x0F
    }
    
    /**
     * Check if an opcode is valid
     * @param opcode instruction opcode
     * @return true if valid, false otherwise
     */
    fun isValidOpcode(opcode: Int): Boolean {
        return opcode in 0x0..0xF
    }
    
    /**
     * Get instruction name for debugging/testing purposes
     * @param opcode instruction opcode
     * @return instruction name string
     */
    fun getInstructionName(opcode: Int): String {
        return when (opcode) {
            0x0 -> "STORE"
            0x1 -> "ADD"
            0x2 -> "SUB"
            0x3 -> "READ"
            0x4 -> "WRITE"
            0x5 -> "JUMP"
            0x6 -> "READ_KEYBOARD"
            0x7 -> "SWITCH_MEMORY"
            0x8 -> "SKIP_EQUAL"
            0x9 -> "SKIP_NOT_EQUAL"
            0xA -> "SET_A"
            0xB -> "SET_T"
            0xC -> "READ_T"
            0xD -> "CONVERT_TO_BASE_10"
            0xE -> "CONVERT_BYTE_TO_ASCII"
            0xF -> "DRAW"
            else -> "UNKNOWN"
        }
    }
}

// === BASIC INSTRUCTIONS ===

/**
 * STORE - Stores byte bb in register rX
 * Format: (0, rX, bb)
 * Example: 00FF stores the value FF in register 0
 */
class StoreInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        // Only validate operand1 as a register index for STORE instruction
        // operand2 and operand3 are part of the byte value, not registers
        if (operand1 > 7) {
            throw IllegalArgumentException("Invalid register index: r$operand1")
        }
        // Byte operand is automatically validated by parser (0-255)
    }
    
    override fun performOperation(cpu: CPU) {
        cpu.setRegister(operand1, byteOperand)
    }
}

/**
 * ADD - Adds the value in rX to the value in rY and stores in rZ
 * Format: (1, rX, rY, rZ)
 * Example: 1010 adds the values in r0 and r1 and stores in r0
 */
class AddInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun performOperation(cpu: CPU) {
        val valueX = cpu.getRegister(operand1)
        val valueY = cpu.getRegister(operand2)
        val result = (valueX + valueY) and 0xFF // Keep result in 8-bit range
        
        cpu.setRegister(operand3, result)
    }
}

/**
 * SUB - Subtracts the value in rY from the value in rX and stores in rZ
 * Format: (2, rX, rY, rZ)
 * Example: 2010 subtracts the value in r1 from r0 and stores in r0
 */
class SubInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun performOperation(cpu: CPU) {
        val valueX = cpu.getRegister(operand1)
        val valueY = cpu.getRegister(operand2)
        val result = (valueX - valueY) and 0xFF // Keep result in 8-bit range (handles underflow)
        
        cpu.setRegister(operand3, result)
    }
}

/**
 * READ - Reads the value in memory at the address stored in A and stores in register rX
 * Reads from ROM if M = 1, RAM if M = 0
 * Format: (3, rX, 00)
 * Example: 3700 stores the value in memory at address A in r7
 */
class ReadInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        super.validateOperands()
        if (operand2 != 0 || operand3 != 0) {
            throw IllegalArgumentException("READ instruction format error: operands 2 and 3 must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        val address = cpu.getAddressRegister()
        val value = cpu.readMemory(address)
        cpu.setRegister(operand1, value)
    }
}

/**
 * WRITE - Writes the value in rX to memory at the address stored in A
 * Will attempt to write to ROM if M = 1. This will fail for most ROM chips.
 * Format: (4, rX, 00)
 * Example: 4300 stores the value in r3 in memory at address A
 */
class WriteInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        super.validateOperands()
        if (operand2 != 0 || operand3 != 0) {
            throw IllegalArgumentException("WRITE instruction format error: operands 2 and 3 must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        val address = cpu.getAddressRegister()
        val value = cpu.getRegister(operand1)
        
        try {
            cpu.writeMemory(address, value)
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Program terminated: ROM write operation failed at address ${address.toString(16).uppercase()}")
        }
    }
}

// === CONTROL FLOW INSTRUCTIONS ===

/**
 * JUMP - Sets P to the value of aaa
 * Terminates the program with an error if aaa is not divisible by 2
 * Program counter is not incremented after this instruction
 * Format: (5, aaa)
 * Example: 51F2 sets the program counter to 1F2
 */
class JumpInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override val shouldIncrementPC: Boolean = false
    
    override fun validateOperands() {
        if (addressOperand % 2 != 0) {
            throw IllegalArgumentException("JUMP address must be even: ${addressOperand.toString(16).uppercase()}")
        }
        if (addressOperand < 0 || addressOperand > 4095) {
            throw IllegalArgumentException("JUMP address out of range: ${addressOperand.toString(16).uppercase()}")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        cpu.setPC(addressOperand)
    }
}

/**
 * SKIP_EQUAL - Compares the values in rX and rY and skips the next instruction if they are equal
 * Format: (8, rX, rY, 0)
 * Example: 8120 compares r1 with r2 and skips the next instruction if they are equal
 */
class SkipEqualInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    private var shouldSkip: Boolean = false
    
    override fun validateOperands() {
        super.validateOperands()
        if (operand3 != 0) {
            throw IllegalArgumentException("SKIP_EQUAL instruction format error: operand 3 must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        val valueX = cpu.getRegister(operand1)
        val valueY = cpu.getRegister(operand2)
        shouldSkip = (valueX == valueY)
    }
    
    override fun incrementPC(cpu: CPU) {
        if (shouldSkip) {
            cpu.incrementPC(4) // Skip next instruction
        } else {
            cpu.incrementPC(2) // Normal increment
        }
    }
}

/**
 * SKIP_NOT_EQUAL - Compares the values in rX and rY and skips the next instruction if they are NOT equal
 * Format: (9, rX, rY, 0)
 * Example: 9120 compares r1 with r2 and skips the next instruction if they are NOT equal
 */
class SkipNotEqualInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    private var shouldSkip: Boolean = false
    
    override fun validateOperands() {
        super.validateOperands()
        if (operand3 != 0) {
            throw IllegalArgumentException("SKIP_NOT_EQUAL instruction format error: operand 3 must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        val valueX = cpu.getRegister(operand1)
        val valueY = cpu.getRegister(operand2)
        shouldSkip = (valueX != valueY)
    }
    
    override fun incrementPC(cpu: CPU) {
        if (shouldSkip) {
            cpu.incrementPC(4) // Skip next instruction
        } else {
            cpu.incrementPC(2) // Normal increment
        }
    }
}

// === SYSTEM INSTRUCTIONS ===

/**
 * READ_KEYBOARD - Pauses the program and waits for keyboard input
 * Only base-16 digits 0-F are allowed as input. Input parsed as number and stored in rX.
 * Format: (6, rX, 00)
 * Example: 6200 stores the number the user typed in r2
 */
class ReadKeyboardInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        super.validateOperands()
        if (operand2 != 0 || operand3 != 0) {
            throw IllegalArgumentException("READ_KEYBOARD instruction format error: operands 2 and 3 must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        // This is handled by the Computer class for proper keyboard simulation
        cpu.setRegister(operand1, 0) // Placeholder
    }
}

/**
 * SWITCH_MEMORY - Toggles the M register - sets to 1 if M is 0 and sets to 0 if M is 1
 * Format: (7000)
 * Example: 7000 toggles between RAM and ROM mode
 */
class SwitchMemoryInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        if (operand1 != 0 || operand2 != 0 || operand3 != 0) {
            throw IllegalArgumentException("SWITCH_MEMORY instruction format error: all operands must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        cpu.toggleMemoryFlag()
    }
}

/**
 * SET_A - Sets the value of A to be aaa
 * Format: (A, aaa)
 * Example: A255 sets A to be 255
 */
class SetAInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        if (addressOperand < 0 || addressOperand > 0xFFFF) {
            throw IllegalArgumentException("SET_A address out of range: ${addressOperand.toString(16).uppercase()}")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        cpu.setAddressRegister(addressOperand)
    }
}

/**
 * SET_T - Sets the value of T to be bb
 * Format: (B, bb, 0)
 * Example: B0A0 sets T to be A0
 */
class SetTInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        // For SET_T instruction format (B, bb, 0), operand1 and operand2 are part of the byte value
        // Only validate that operand3 is 0 as specified in the format
        if (operand3 != 0) {
            throw IllegalArgumentException("SET_T instruction format error: operand 3 must be 0")
        }
        // byteOperand is automatically validated by parser (0-255)
    }
    
    override fun performOperation(cpu: CPU) {
        cpu.setTimerRegister(byteOperand)
    }
}

/**
 * READ_T - Reads the value of T and stores in rX
 * Format: (C, rX, 00)
 * Example: C000 reads T and stores in r0
 */
class ReadTInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        super.validateOperands()
        if (operand2 != 0 || operand3 != 0) {
            throw IllegalArgumentException("READ_T instruction format error: operands 2 and 3 must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        val timerValue = cpu.getTimerRegister()
        cpu.setRegister(operand1, timerValue)
    }
}

/**
 * CONVERT_TO_BASE_10 - Converts the byte stored in rX to base-10 
 * and stores the 100s digit in A, 10s digit in A+1, and 1s digit in A+2
 * Format: (D, rX, 00)
 * Example: D200 converts value r2 and stores the digits in A, A+1, and A+2
 */
class ConvertToBase10Instruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        super.validateOperands()
        if (operand2 != 0 || operand3 != 0) {
            throw IllegalArgumentException("CONVERT_TO_BASE_10 instruction format error: operands 2 and 3 must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        val value = cpu.getRegister(operand1)
        val baseAddress = cpu.getAddressRegister()
        
        val hundreds = value / 100
        val tens = (value % 100) / 10
        val ones = value % 10
        
        cpu.writeMemory(baseAddress, hundreds)
        cpu.writeMemory(baseAddress + 1, tens)
        cpu.writeMemory(baseAddress + 2, ones)
    }
}

/**
 * CONVERT_BYTE_TO_ASCII - Takes the digit (0-F) stored in rX and converts it to ASCII value, stores in rY
 * Terminates the program with an error if the byte stored in rX is greater than F (base-16)
 * Format: (E, rX, rY, 0)
 * Example: E010 takes the value in r0 and stores the ASCII value in r1
 */
class ConvertByteToAsciiInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        super.validateOperands()
        if (operand3 != 0) {
            throw IllegalArgumentException("CONVERT_BYTE_TO_ASCII instruction format error: operand 3 must be 0")
        }
    }
    
    override fun performOperation(cpu: CPU) {
        val digit = cpu.getRegister(operand1)
        
        if (digit > 0xF) {
            throw IllegalStateException("Program terminated: CONVERT_BYTE_TO_ASCII digit out of range: $digit (must be 0-15)")
        }
        
        val asciiValue = if (digit <= 9) {
            0x30 + digit // '0' to '9'
        } else {
            0x37 + digit // 'A' to 'F'
        }
        
        cpu.setRegister(operand2, asciiValue)
    }
}

/**
 * DRAW - Draws the ASCII character for the byte stored in rX at literal row and column coordinates
 * Format: (F, rX, row, column)
 * Example: F123 draws the ASCII character stored in r1 at row 2, column 3
 */
class DrawInstruction(instruction: Int) : InstructionTemplate(instruction) {
    
    override fun validateOperands() {
        // Only validate operand1 as a register index for DRAW instruction
        // operand2 and operand3 are literal coordinates (0-7), not registers
        if (operand1 > 7) {
            throw IllegalArgumentException("Invalid register index: r$operand1")
        }
        // Coordinates are validated in performOperation
    }
    
    override fun performOperation(cpu: CPU) {
        val asciiChar = cpu.getRegister(operand1)
        val row = operand2  // Use literal coordinate value, not register reference
        val column = operand3  // Use literal coordinate value, not register reference
        
        if (asciiChar > 0x7F) {
            throw IllegalStateException("Program terminated: DRAW ASCII character out of range: $asciiChar (must be 0-127)")
        }
        if (row > 7) {
            throw IllegalStateException("Program terminated: DRAW row out of bounds: $row (must be 0-7)")
        }
        if (column > 7) {
            throw IllegalStateException("Program terminated: DRAW column out of bounds: $column (must be 0-7)")
        }
        
        // This is handled by the Computer class for proper screen access
    }
}