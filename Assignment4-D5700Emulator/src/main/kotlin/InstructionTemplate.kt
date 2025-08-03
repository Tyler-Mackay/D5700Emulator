abstract class InstructionTemplate(protected val instruction: Int) {
    
    protected lateinit var parser: InstructionParser
    protected var instructionOpcode: Int = 0
    protected var operand1: Int = 0
    protected var operand2: Int = 0
    protected var operand3: Int = 0
    protected var byteOperand: Int = 0
    protected var addressOperand: Int = 0
    
    protected open val shouldIncrementPC: Boolean = true
    protected open val pcIncrementAmount: Int = 2 // Standard 2-byte increment
    
    /**
     * Main execution method - template method pattern
     * Orchestrates the instruction execution process
     */
    fun execute(cpu: CPU) {
        parseInstruction()
        validateOperands()
        performOperation(cpu)
        
        if (shouldIncrementPC) {
            incrementPC(cpu)
        }
    }
    
    /**
     * Parse the instruction into its component operands
     * Protected method - called during execution
     */
    protected open fun parseInstruction() {
        parser = InstructionParser(instruction)
        instructionOpcode = parser.getOperation()
        operand1 = parser.getFirstOperand()
        operand2 = parser.getSecondOperand()
        operand3 = parser.getThirdOperand()
        byteOperand = parser.getByteOperand()
        addressOperand = parser.getAddressOperand()
    }
    
    /**
     * Validate that operands are within acceptable ranges
     * Protected method - called during execution
     * Override in subclasses for specific validation
     */
    protected open fun validateOperands() {
        // Default validation - register indices should be 0-7
        if (operand1 > 7) {
            throw IllegalArgumentException("Invalid register index: r$operand1")
        }
        if (operand2 > 7) {
            throw IllegalArgumentException("Invalid register index: r$operand2") 
        }
        if (operand3 > 7) {
            throw IllegalArgumentException("Invalid register index: r$operand3")
        }
        
        // Byte operands should be 0-255 (checked by parser)
        // Address operands should be 0-4095 for 4KB memory (checked by CPU)
    }
    
    /**
     * Perform the specific operation for this instruction type
     * Abstract method - must be implemented by each instruction
     */
    protected abstract fun performOperation(cpu: CPU)
    
    /**
     * Increment the program counter by the standard amount
     * Protected method - can be overridden for special behavior
     */
    protected open fun incrementPC(cpu: CPU) {
        cpu.incrementPC(pcIncrementAmount)
    }
    
    /**
     * Get a string representation of this instruction
     */
    override fun toString(): String {
        return "Instruction 0x${instruction.toString(16).uppercase().padStart(4, '0')} " +
               "(${this.javaClass.simpleName})"
    }
}