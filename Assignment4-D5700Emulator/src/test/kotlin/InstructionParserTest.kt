import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class InstructionParserTest {

    @Test
    fun `test STORE instruction parsing - 00FF stores FF in register 0`() {
        val parser = InstructionParser(0x00FF)
        
        assertEquals(0x0, parser.getOperation(), "Operation should be 0 for STORE")
        assertEquals(0x0, parser.getFirstOperand(), "First operand should be 0 (register 0)")
        assertEquals(0xFF, parser.getByteOperand(), "Byte operand should be FF")
    }

    @Test
    fun `test ADD instruction parsing - 1010 adds r0 and r1, stores in r0`() {
        val parser = InstructionParser(0x1010)
        
        assertEquals(0x1, parser.getOperation(), "Operation should be 1 for ADD")
        assertEquals(0x0, parser.getFirstOperand(), "First operand should be 0 (register 0)")
        assertEquals(0x1, parser.getSecondOperand(), "Second operand should be 1 (register 1)")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0 (register 0)")
    }

    @Test
    fun `test ADD instruction parsing - 1630 adds r6 and r3, stores in r0`() {
        val parser = InstructionParser(0x1630)
        
        assertEquals(0x1, parser.getOperation(), "Operation should be 1 for ADD")
        assertEquals(0x6, parser.getFirstOperand(), "First operand should be 6 (register 6)")
        assertEquals(0x3, parser.getSecondOperand(), "Second operand should be 3 (register 3)")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0 (register 0)")
    }

    @Test
    fun `test SUB instruction parsing - 2010 subtracts r1 from r0, stores in r0`() {
        val parser = InstructionParser(0x2010)
        
        assertEquals(0x2, parser.getOperation(), "Operation should be 2 for SUB")
        assertEquals(0x0, parser.getFirstOperand(), "First operand should be 0 (register 0)")
        assertEquals(0x1, parser.getSecondOperand(), "Second operand should be 1 (register 1)")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0 (register 0)")
    }

    @Test
    fun `test READ instruction parsing - 3700 reads from memory address A into r7`() {
        val parser = InstructionParser(0x3700)
        
        assertEquals(0x3, parser.getOperation(), "Operation should be 3 for READ")
        assertEquals(0x7, parser.getFirstOperand(), "First operand should be 7 (register 7)")
        assertEquals(0x0, parser.getSecondOperand(), "Second operand should be 0")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test WRITE instruction parsing - 4300 writes r3 to memory address A`() {
        val parser = InstructionParser(0x4300)
        
        assertEquals(0x4, parser.getOperation(), "Operation should be 4 for WRITE")
        assertEquals(0x3, parser.getFirstOperand(), "First operand should be 3 (register 3)")
        assertEquals(0x0, parser.getSecondOperand(), "Second operand should be 0")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test JUMP instruction parsing - 51F2 sets program counter to 1F2`() {
        val parser = InstructionParser(0x51F2)
        
        assertEquals(0x5, parser.getOperation(), "Operation should be 5 for JUMP")
        assertEquals(0x1, parser.getFirstOperand(), "First operand should be 1")
        assertEquals(0x1F2, parser.getAddressOperand(), "Address operand should be 1F2")
    }

    @Test
    fun `test READ_KEYBOARD instruction parsing - 6200 reads keyboard input into r2`() {
        val parser = InstructionParser(0x6200)
        
        assertEquals(0x6, parser.getOperation(), "Operation should be 6 for READ_KEYBOARD")
        assertEquals(0x2, parser.getFirstOperand(), "First operand should be 2 (register 2)")
        assertEquals(0x0, parser.getSecondOperand(), "Second operand should be 0")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test SWITCH_MEMORY instruction parsing - 7000 toggles memory flag`() {
        val parser = InstructionParser(0x7000)
        
        assertEquals(0x7, parser.getOperation(), "Operation should be 7 for SWITCH_MEMORY")
        assertEquals(0x0, parser.getFirstOperand(), "First operand should be 0")
        assertEquals(0x0, parser.getSecondOperand(), "Second operand should be 0")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test SKIP_EQUAL instruction parsing - 8120 compares r1 and r2`() {
        val parser = InstructionParser(0x8120)
        
        assertEquals(0x8, parser.getOperation(), "Operation should be 8 for SKIP_EQUAL")
        assertEquals(0x1, parser.getFirstOperand(), "First operand should be 1 (register 1)")
        assertEquals(0x2, parser.getSecondOperand(), "Second operand should be 2 (register 2)")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test SKIP_NOT_EQUAL instruction parsing - 9120 compares r1 and r2`() {
        val parser = InstructionParser(0x9120)
        
        assertEquals(0x9, parser.getOperation(), "Operation should be 9 for SKIP_NOT_EQUAL")
        assertEquals(0x1, parser.getFirstOperand(), "First operand should be 1 (register 1)")
        assertEquals(0x2, parser.getSecondOperand(), "Second operand should be 2 (register 2)")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test SET_A instruction parsing - A255 sets A register to 255`() {
        val parser = InstructionParser(0xA255)
        
        assertEquals(0xA, parser.getOperation(), "Operation should be A for SET_A")
        assertEquals(0x2, parser.getFirstOperand(), "First operand should be 2")
        assertEquals(0x255, parser.getAddressOperand(), "Address operand should be 255")
    }

    @Test
    fun `test SET_T instruction parsing - B0A0 sets T register to A0`() {
        val parser = InstructionParser(0xB0A0)
        
        assertEquals(0xB, parser.getOperation(), "Operation should be B for SET_T")
        assertEquals(0x0, parser.getFirstOperand(), "First operand should be 0")
        assertEquals(0xA0, parser.getByteOperand(), "Byte operand should be A0")
    }

    @Test
    fun `test READ_T instruction parsing - C000 reads T register into r0`() {
        val parser = InstructionParser(0xC000)
        
        assertEquals(0xC, parser.getOperation(), "Operation should be C for READ_T")
        assertEquals(0x0, parser.getFirstOperand(), "First operand should be 0 (register 0)")
        assertEquals(0x0, parser.getSecondOperand(), "Second operand should be 0")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test CONVERT_TO_BASE_10 instruction parsing - D200 converts r2 to base-10`() {
        val parser = InstructionParser(0xD200)
        
        assertEquals(0xD, parser.getOperation(), "Operation should be D for CONVERT_TO_BASE_10")
        assertEquals(0x2, parser.getFirstOperand(), "First operand should be 2 (register 2)")
        assertEquals(0x0, parser.getSecondOperand(), "Second operand should be 0")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test CONVERT_BYTE_TO_ASCII instruction parsing - E010 converts r0 to ASCII in r1`() {
        val parser = InstructionParser(0xE010)
        
        assertEquals(0xE, parser.getOperation(), "Operation should be E for CONVERT_BYTE_TO_ASCII")
        assertEquals(0x0, parser.getFirstOperand(), "First operand should be 0 (register 0)")
        assertEquals(0x1, parser.getSecondOperand(), "Second operand should be 1 (register 1)")
        assertEquals(0x0, parser.getThirdOperand(), "Third operand should be 0")
    }

    @Test
    fun `test DRAW instruction parsing - F123 draws ASCII from r1 at row r2, column r3`() {
        val parser = InstructionParser(0xF123)
        
        assertEquals(0xF, parser.getOperation(), "Operation should be F for DRAW")
        assertEquals(0x1, parser.getFirstOperand(), "First operand should be 1 (register 1)")
        assertEquals(0x2, parser.getSecondOperand(), "Second operand should be 2 (register 2)")
        assertEquals(0x3, parser.getThirdOperand(), "Third operand should be 3 (register 3)")
    }

    @Test
    fun `test address operand calculation with different values`() {
        // Test JUMP instruction with address ABC (A << 8 | BC = 0xABC)
        val parser1 = InstructionParser(0x5ABC)
        assertEquals(0xABC, parser1.getAddressOperand(), "Address should be ABC")
        
        // Test SET_A instruction with address 000 (0 << 8 | 00 = 0x000)
        val parser2 = InstructionParser(0xA000)
        assertEquals(0x000, parser2.getAddressOperand(), "Address should be 000")
        
        // Test with maximum address FFF (F << 8 | FF = 0xFFF)
        val parser3 = InstructionParser(0x5FFF)
        assertEquals(0xFFF, parser3.getAddressOperand(), "Address should be FFF")
    }

    @Test
    fun `test byte operand with different values`() {
        // Test STORE with byte 00
        val parser1 = InstructionParser(0x0100)
        assertEquals(0x00, parser1.getByteOperand(), "Byte should be 00")
        
        // Test STORE with byte FF
        val parser2 = InstructionParser(0x02FF)
        assertEquals(0xFF, parser2.getByteOperand(), "Byte should be FF")
        
        // Test SET_T with byte 7F
        val parser3 = InstructionParser(0xB37F)
        assertEquals(0x7F, parser3.getByteOperand(), "Byte should be 7F")
    }

    @Test
    fun `test raw instruction value`() {
        val instruction = 0x1630
        val parser = InstructionParser(instruction)
        
        assertEquals(instruction, parser.getRawInstruction(), "Raw instruction should match input")
    }

    @Test
    fun `test toString method`() {
        val parser = InstructionParser(0x1630)
        val result = parser.toString()
        
        assertTrue(result.contains("0x1630"), "Should contain hex instruction")
        assertTrue(result.contains("Op: 1"), "Should contain operation")
        assertTrue(result.contains("6"), "Should contain first operand")
        assertTrue(result.contains("3"), "Should contain second operand")
        assertTrue(result.contains("0"), "Should contain third operand")
    }

    @Test
    fun `test edge cases with zero values`() {
        val parser = InstructionParser(0x0000)
        
        assertEquals(0x0, parser.getOperation())
        assertEquals(0x0, parser.getFirstOperand())
        assertEquals(0x0, parser.getSecondOperand())
        assertEquals(0x0, parser.getThirdOperand())
        assertEquals(0x0, parser.getByteOperand())
        assertEquals(0x0, parser.getAddressOperand())
    }

    @Test
    fun `test edge cases with maximum values`() {
        val parser = InstructionParser(0xFFFF)
        
        assertEquals(0xF, parser.getOperation())
        assertEquals(0xF, parser.getFirstOperand())
        assertEquals(0xF, parser.getSecondOperand())
        assertEquals(0xF, parser.getThirdOperand())
        assertEquals(0xFF, parser.getByteOperand())
        assertEquals(0xFFF, parser.getAddressOperand())
    }
}