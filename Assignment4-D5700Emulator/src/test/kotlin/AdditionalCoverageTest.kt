import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Additional test coverage for areas that may be missing tests
 */
class AdditionalCoverageTest {
    
    private lateinit var cpu: CPU
    private lateinit var ram: ByteArray
    private lateinit var rom: ByteArray
    private lateinit var computer: Computer
    private lateinit var screen: Screen
    private lateinit var ramObj: RAM
    private lateinit var romObj: ROM
    
    @BeforeEach
    fun setUp() {
        cpu = CPU()
        ram = ByteArray(4096)
        rom = ByteArray(4096)
        cpu.initialize(ram, rom)
        computer = Computer()
        screen = Screen()
        ramObj = RAM()
        romObj = ROM()
    }
    
    // === SCREEN CLASS COVERAGE ===
    
    @Test
    fun `test screen basic operations`() {
        screen.draw(0x41, 0, 0) // 'A'
        assertEquals(0x41, screen.getCharAt(0, 0))
        
        screen.clear()
        assertEquals(0x20, screen.getCharAt(0, 0)) // Should be space after clear
    }
    
    @Test
    fun `test screen dimensions`() {
        assertEquals(8, Screen.SCREEN_WIDTH)
        assertEquals(8, Screen.SCREEN_HEIGHT)
        assertEquals(64, Screen.SCREEN_SIZE)
    }
    
    @Test
    fun `test screen formatted display`() {
        screen.clear()
        val display = screen.getFormattedDisplay()
        
        // Should have 8 lines
        val lines = display.split('\n')
        assertEquals(8, lines.size)
        
        // Each line should have 8 characters
        for (line in lines) {
            assertEquals(8, line.length)
        }
    }
    
    // === ROM CLASS COVERAGE ===
    
    @Test
    fun `test ROM basic operations`() {
        assertEquals(4096, romObj.getSize())
        assertEquals(0, romObj.read(0)) // Should start as 0
        
        val program = intArrayOf(0x12, 0x34, 0x56)
        romObj.loadProgram(program)
        
        assertEquals(0x12, romObj.read(0))
        assertEquals(0x34, romObj.read(1))
        assertEquals(0x56, romObj.read(2))
    }
    
    @Test
    fun `test ROM write protection`() {
        assertFalse(romObj.isWritable())
        
        romObj.setWritable(true)
        assertTrue(romObj.isWritable())
        
        assertDoesNotThrow {
            romObj.write(0, 0x42)
        }
        assertEquals(0x42, romObj.read(0))
    }
    
    // === RAM CLASS COVERAGE ===
    
    @Test
    fun `test RAM basic operations`() {
        assertEquals(4096, ramObj.getSize())
        
        ramObj.write(0, 0x42)
        ramObj.write(100, 0xFF)
        
        assertEquals(0x42, ramObj.read(0))
        assertEquals(0xFF, ramObj.read(100))
        assertEquals(0, ramObj.read(50)) // Unwritten should be 0
    }
    
    @Test
    fun `test RAM clear`() {
        ramObj.write(10, 0x55)
        ramObj.write(20, 0xAA)
        
        ramObj.clear()
        
        assertEquals(0, ramObj.read(10))
        assertEquals(0, ramObj.read(20))
    }
    
    // === COMPUTER CLASS ADDITIONAL COVERAGE ===
    
    @Test
    fun `test computer initial state`() {
        assertFalse(computer.isRunning())
        assertFalse(computer.hasTerminated())
        assertEquals(0, computer.getProgramCounter())
        assertEquals(0, computer.getTimerValue())
        assertFalse(computer.getMemoryFlag())
    }
    
    @Test
    fun `test computer program loading`() {
        assertFalse(computer.isProgramLoaded())
        
        val program = intArrayOf(0x00, 0xFF, 0x10, 0x20)
        computer.loadROM(program)
        
        assertTrue(computer.isProgramLoaded())
    }
    
    @Test
    fun `test computer timer operations`() {
        computer.setTimerValue(42)
        assertEquals(42, computer.getTimerValue())
        
        computer.setTimerValue(0)
        assertEquals(0, computer.getTimerValue())
    }
    
    @Test
    fun `test computer keyboard operations`() {
        assertEquals(-1, computer.readKeyboard()) // Empty initially
        
        computer.addKeyboardInput(0x41)
        assertEquals(0x41, computer.readKeyboard())
        assertEquals(-1, computer.readKeyboard()) // Should be empty again
    }
    
    @Test
    fun `test computer memory operations`() {
        computer.writeRAM(100, 0x42)
        assertEquals(0x42, computer.readRAM(100))
        
        // Load a program and test ROM reading
        val program = intArrayOf(0x12, 0x34)
        computer.loadROM(program)
        assertEquals(0x12, computer.readROM(0))
        assertEquals(0x34, computer.readROM(1))
    }
    
    // === CPU ADDITIONAL COVERAGE ===
    
    @Test
    fun `test CPU memory flag operations`() {
        assertFalse(cpu.getMemoryFlag()) // Should start with RAM
        
        cpu.toggleMemoryFlag()
        assertTrue(cpu.getMemoryFlag()) // Should be ROM now
        
        cpu.toggleMemoryFlag()
        assertFalse(cpu.getMemoryFlag()) // Back to RAM
    }
    
    @Test
    fun `test CPU special register operations`() {
        cpu.setAddressRegister(0x123)
        assertEquals(0x123, cpu.getAddressRegister())
        
        cpu.setTimerRegister(0x42)
        assertEquals(0x42, cpu.getTimerRegister())
        
        cpu.setPC(0x100)
        assertEquals(0x100, cpu.getPC())
    }
    
    // === INSTRUCTION FACTORY COVERAGE ===
    
    @Test
    fun `test instruction factory creates all instructions`() {
        val factory = InstructionFactory()
        
        // Test that all 16 instruction types can be created
        assertDoesNotThrow { factory.createInstruction(0x0000) } // STORE
        assertDoesNotThrow { factory.createInstruction(0x1000) } // ADD
        assertDoesNotThrow { factory.createInstruction(0x2000) } // SUB
        assertDoesNotThrow { factory.createInstruction(0x3000) } // READ
        assertDoesNotThrow { factory.createInstruction(0x4000) } // WRITE
        assertDoesNotThrow { factory.createInstruction(0x5000) } // JUMP
        assertDoesNotThrow { factory.createInstruction(0x6000) } // READ_KEYBOARD
        assertDoesNotThrow { factory.createInstruction(0x7000) } // SWITCH_MEMORY
        assertDoesNotThrow { factory.createInstruction(0x8000) } // SKIP_EQUAL
        assertDoesNotThrow { factory.createInstruction(0x9000) } // SKIP_NOT_EQUAL
        assertDoesNotThrow { factory.createInstruction(0xA000) } // SET_A
        assertDoesNotThrow { factory.createInstruction(0xB000) } // SET_T
        assertDoesNotThrow { factory.createInstruction(0xC000) } // READ_T
        assertDoesNotThrow { factory.createInstruction(0xD000) } // CONVERT_TO_BASE_10
        assertDoesNotThrow { factory.createInstruction(0xE000) } // CONVERT_BYTE_TO_ASCII
        assertDoesNotThrow { factory.createInstruction(0xF000) } // DRAW
    }
    
    @Test
    fun `test instruction factory opcode validation`() {
        val factory = InstructionFactory()
        
        assertTrue(factory.isValidOpcode(0x0))
        assertTrue(factory.isValidOpcode(0x5))
        assertTrue(factory.isValidOpcode(0xF))
        assertFalse(factory.isValidOpcode(0x10))
    }
    
    @Test
    fun `test instruction factory name mapping`() {
        val factory = InstructionFactory()
        
        assertEquals("STORE", factory.getInstructionName(0x0))
        assertEquals("ADD", factory.getInstructionName(0x1))
        assertEquals("JUMP", factory.getInstructionName(0x5))
        assertEquals("DRAW", factory.getInstructionName(0xF))
        assertEquals("UNKNOWN", factory.getInstructionName(0x10))
    }
    
    // === INSTRUCTION PARSER ADDITIONAL COVERAGE ===
    
    @Test
    fun `test instruction parser boundary values`() {
        // Test minimum values
        val parserMin = InstructionParser(0x0000)
        assertEquals(0x0, parserMin.getOperation())
        assertEquals(0x0, parserMin.getFirstOperand())
        assertEquals(0x00, parserMin.getByteOperand())
        assertEquals(0x000, parserMin.getAddressOperand())
        
        // Test maximum values
        val parserMax = InstructionParser(0xFFFF)
        assertEquals(0xF, parserMax.getOperation())
        assertEquals(0xF, parserMax.getFirstOperand())
        assertEquals(0xFF, parserMax.getByteOperand())
        assertEquals(0xFFF, parserMax.getAddressOperand())
    }
    
    @Test
    fun `test instruction parser specific formats`() {
        // Test STORE format: 0rbb (operation 0, register r, byte bb)
        val storeParser = InstructionParser(0x03AB)
        assertEquals(0x0, storeParser.getOperation())
        assertEquals(0x3, storeParser.getFirstOperand())
        assertEquals(0xAB, storeParser.getByteOperand())
        
        // Test JUMP format: 5aaa (operation 5, address aaa)
        val jumpParser = InstructionParser(0x5123)
        assertEquals(0x5, jumpParser.getOperation())
        assertEquals(0x123, jumpParser.getAddressOperand())
    }
}