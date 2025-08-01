import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class InstructionSystemTest {
    
    private lateinit var cpu: CPU
    private lateinit var ram: ByteArray
    private lateinit var rom: ByteArray
    private lateinit var factory: InstructionFactory
    
    @BeforeEach
    fun setUp() {
        cpu = CPU()
        ram = ByteArray(4096)
        rom = ByteArray(4096)
        cpu.initialize(ram, rom)
        factory = InstructionFactory()
    }
    
    // === INSTRUCTION FACTORY TESTS ===
    
    @Test
    fun `test InstructionFactory creates correct instruction types`() {
        assertEquals("StoreInstruction", factory.createInstruction(0x00FF).javaClass.simpleName)
        assertEquals("AddInstruction", factory.createInstruction(0x1010).javaClass.simpleName)
        assertEquals("SubInstruction", factory.createInstruction(0x2010).javaClass.simpleName)
        assertEquals("ReadInstruction", factory.createInstruction(0x3700).javaClass.simpleName)
        assertEquals("WriteInstruction", factory.createInstruction(0x4300).javaClass.simpleName)
        assertEquals("JumpInstruction", factory.createInstruction(0x51F2).javaClass.simpleName)
        assertEquals("ReadKeyboardInstruction", factory.createInstruction(0x6200).javaClass.simpleName)
        assertEquals("SwitchMemoryInstruction", factory.createInstruction(0x7000).javaClass.simpleName)
        assertEquals("SkipEqualInstruction", factory.createInstruction(0x8120).javaClass.simpleName)
        assertEquals("SkipNotEqualInstruction", factory.createInstruction(0x9120).javaClass.simpleName)
        assertEquals("SetAInstruction", factory.createInstruction(0xA255).javaClass.simpleName)
        assertEquals("SetTInstruction", factory.createInstruction(0xB0A0).javaClass.simpleName)
        assertEquals("ReadTInstruction", factory.createInstruction(0xC000).javaClass.simpleName)
        assertEquals("ConvertToBase10Instruction", factory.createInstruction(0xD200).javaClass.simpleName)
        assertEquals("ConvertByteToAsciiInstruction", factory.createInstruction(0xE010).javaClass.simpleName)
        assertEquals("DrawInstruction", factory.createInstruction(0xF123).javaClass.simpleName)
    }
    
    @Test
    fun `test InstructionFactory invalid opcode`() {
        // Invalid opcode should throw exception (there are only 16 valid opcodes 0-F)
        assertThrows(IllegalArgumentException::class.java) {
            factory.createInstruction(0xFFFF) // This would have opcode F, which is valid
        }
    }
    
    @Test
    fun `test InstructionFactory instruction names`() {
        assertEquals("STORE", factory.getInstructionName(0x0))
        assertEquals("ADD", factory.getInstructionName(0x1))
        assertEquals("SUB", factory.getInstructionName(0x2))
        assertEquals("JUMP", factory.getInstructionName(0x5))
        assertEquals("DRAW", factory.getInstructionName(0xF))
        assertEquals("UNKNOWN", factory.getInstructionName(0x10))
    }
    
    @Test
    fun `test InstructionFactory opcode validation`() {
        assertTrue(factory.isValidOpcode(0x0))
        assertTrue(factory.isValidOpcode(0xF))
        assertFalse(factory.isValidOpcode(0x10))
        assertFalse(factory.isValidOpcode(-1))
    }
    
    // === BASIC INSTRUCTION TESTS ===
    
    @Test
    fun `test STORE instruction - stores byte in register`() {
        val instruction = factory.createInstruction(0x00FF) // STORE FF in r0
        instruction.execute(cpu)
        
        assertEquals(0xFF, cpu.getRegister(0))
        assertEquals(2, cpu.getPC()) // PC should increment by 2
    }
    
    @Test
    fun `test STORE instruction different registers`() {
        val instruction1 = factory.createInstruction(0x0742) // STORE 42 in r7
        val instruction2 = factory.createInstruction(0x0500) // STORE 00 in r5
        
        instruction1.execute(cpu)
        instruction2.execute(cpu)
        
        assertEquals(0x42, cpu.getRegister(7))
        assertEquals(0x00, cpu.getRegister(5))
    }
    
    @Test
    fun `test ADD instruction - adds two registers`() {
        cpu.setRegister(1, 0x10)
        cpu.setRegister(2, 0x20)
        
        val instruction = factory.createInstruction(0x1123) // ADD r1 + r2, store in r3
        instruction.execute(cpu)
        
        assertEquals(0x30, cpu.getRegister(3))
        assertEquals(2, cpu.getPC())
    }
    
    @Test
    fun `test ADD instruction overflow handling`() {
        cpu.setRegister(1, 0xFF)
        cpu.setRegister(2, 0x02)
        
        val instruction = factory.createInstruction(0x1123) // ADD r1 + r2, store in r3
        instruction.execute(cpu)
        
        assertEquals(0x01, cpu.getRegister(3)) // Should wrap around (0xFF + 0x02 = 0x101 -> 0x01)
    }
    
    @Test
    fun `test SUB instruction - subtracts two registers`() {
        cpu.setRegister(1, 0x30)
        cpu.setRegister(2, 0x10)
        
        val instruction = factory.createInstruction(0x2123) // SUB r1 - r2, store in r3
        instruction.execute(cpu)
        
        assertEquals(0x20, cpu.getRegister(3))
    }
    
    @Test
    fun `test SUB instruction underflow handling`() {
        cpu.setRegister(1, 0x05)
        cpu.setRegister(2, 0x10)
        
        val instruction = factory.createInstruction(0x2123) // SUB r1 - r2, store in r3
        instruction.execute(cpu)
        
        assertEquals(0xF5, cpu.getRegister(3)) // Should wrap around (0x05 - 0x10 = -0x0B -> 0xF5)
    }
    
    @Test
    fun `test READ instruction - reads from memory`() {
        cpu.setAddressRegister(0x100)
        cpu.setMemoryFlag(false) // RAM mode
        ram[0x100] = 0x42.toByte()
        
        val instruction = factory.createInstruction(0x3500) // READ from memory into r5
        instruction.execute(cpu)
        
        assertEquals(0x42, cpu.getRegister(5))
    }
    
    @Test
    fun `test READ instruction from ROM`() {
        cpu.setAddressRegister(0x200)
        cpu.setMemoryFlag(true) // ROM mode
        rom[0x200] = 0x33.toByte()
        
        val instruction = factory.createInstruction(0x3400) // READ from ROM into r4
        instruction.execute(cpu)
        
        assertEquals(0x33, cpu.getRegister(4))
    }
    
    @Test
    fun `test WRITE instruction - writes to memory`() {
        cpu.setRegister(3, 0x55)
        cpu.setAddressRegister(0x150)
        cpu.setMemoryFlag(false) // RAM mode
        
        val instruction = factory.createInstruction(0x4300) // WRITE r3 to memory
        instruction.execute(cpu)
        
        assertEquals(0x55.toByte(), ram[0x150])
    }
    
    // === CONTROL FLOW INSTRUCTION TESTS ===
    
    @Test
    fun `test JUMP instruction - sets PC to address`() {
        val instruction = factory.createInstruction(0x5200) // JUMP to address 0x200
        instruction.execute(cpu)
        
        assertEquals(0x200, cpu.getPC()) // PC should be set to jump address, not incremented
    }
    
    @Test
    fun `test JUMP instruction invalid address`() {
        assertThrows(IllegalArgumentException::class.java) {
            val instruction = factory.createInstruction(0x5201) // JUMP to odd address (invalid)
            instruction.execute(cpu)
        }
    }
    
    @Test
    fun `test SKIP_EQUAL instruction when equal`() {
        cpu.setRegister(1, 0x42)
        cpu.setRegister(2, 0x42)
        
        val instruction = factory.createInstruction(0x8120) // SKIP_EQUAL r1, r2
        instruction.execute(cpu)
        
        assertEquals(4, cpu.getPC()) // Should skip next instruction (increment by 4)
    }
    
    @Test
    fun `test SKIP_EQUAL instruction when not equal`() {
        cpu.setRegister(1, 0x42)  
        cpu.setRegister(2, 0x43)
        
        val instruction = factory.createInstruction(0x8120) // SKIP_EQUAL r1, r2
        instruction.execute(cpu)
        
        assertEquals(2, cpu.getPC()) // Should not skip (normal increment by 2)
    }
    
    @Test
    fun `test SKIP_NOT_EQUAL instruction when not equal`() {
        cpu.setRegister(1, 0x42)
        cpu.setRegister(2, 0x43)
        
        val instruction = factory.createInstruction(0x9120) // SKIP_NOT_EQUAL r1, r2
        instruction.execute(cpu)
        
        assertEquals(4, cpu.getPC()) // Should skip next instruction (increment by 4)
    }
    
    @Test
    fun `test SKIP_NOT_EQUAL instruction when equal`() {
        cpu.setRegister(1, 0x42)
        cpu.setRegister(2, 0x42)
        
        val instruction = factory.createInstruction(0x9120) // SKIP_NOT_EQUAL r1, r2
        instruction.execute(cpu)
        
        assertEquals(2, cpu.getPC()) // Should not skip (normal increment by 2)
    }
    
    // === SYSTEM INSTRUCTION TESTS ===
    
    @Test
    fun `test SWITCH_MEMORY instruction`() {
        cpu.setMemoryFlag(false) // Start with RAM
        
        val instruction = factory.createInstruction(0x7000) // SWITCH_MEMORY
        instruction.execute(cpu)
        
        assertTrue(cpu.getMemoryFlag()) // Should switch to ROM
        
        instruction.execute(cpu) // Execute again
        assertFalse(cpu.getMemoryFlag()) // Should switch back to RAM
    }
    
    @Test
    fun `test SET_A instruction`() {
        val instruction = factory.createInstruction(0xA123) // SET_A to 0x123
        instruction.execute(cpu)
        
        assertEquals(0x123, cpu.getAddressRegister())
        assertEquals(2, cpu.getPC())
    }
    
    @Test
    fun `test SET_T instruction`() {
        val instruction = factory.createInstruction(0xB500) // SET_T to 0x50
        instruction.execute(cpu)
        
        assertEquals(0x50, cpu.getTimerRegister())
        assertEquals(2, cpu.getPC())
    }
    
    @Test
    fun `test READ_T instruction`() {
        cpu.setTimerRegister(0x33)
        
        val instruction = factory.createInstruction(0xC400) // READ_T into r4
        instruction.execute(cpu)
        
        assertEquals(0x33, cpu.getRegister(4))
    }
    
    @Test
    fun `test CONVERT_TO_BASE_10 instruction`() {
        cpu.setRegister(2, 123) // Store decimal 123
        cpu.setAddressRegister(0x300)
        cpu.setMemoryFlag(false) // RAM mode
        
        val instruction = factory.createInstruction(0xD200) // CONVERT_TO_BASE_10 r2
        instruction.execute(cpu)
        
        assertEquals(1, ram[0x300].toInt() and 0xFF) // Hundreds digit
        assertEquals(2, ram[0x301].toInt() and 0xFF) // Tens digit  
        assertEquals(3, ram[0x302].toInt() and 0xFF) // Ones digit
    }
    
    @Test
    fun `test CONVERT_TO_BASE_10 instruction with 255`() {
        cpu.setRegister(1, 255) // Store decimal 255
        cpu.setAddressRegister(0x400)
        cpu.setMemoryFlag(false) // RAM mode
        
        val instruction = factory.createInstruction(0xD100) // CONVERT_TO_BASE_10 r1
        instruction.execute(cpu)
        
        assertEquals(2, ram[0x400].toInt() and 0xFF) // Hundreds digit
        assertEquals(5, ram[0x401].toInt() and 0xFF) // Tens digit
        assertEquals(5, ram[0x402].toInt() and 0xFF) // Ones digit
    }
    
    @Test
    fun `test CONVERT_BYTE_TO_ASCII instruction with digits 0-9`() {
        cpu.setRegister(0, 5) // Digit 5
        
        val instruction = factory.createInstruction(0xE010) // CONVERT_BYTE_TO_ASCII r0 -> r1
        instruction.execute(cpu)
        
        assertEquals(0x35, cpu.getRegister(1)) // ASCII '5' = 0x35
    }
    
    @Test
    fun `test CONVERT_BYTE_TO_ASCII instruction with hex digits A-F`() {
        cpu.setRegister(0, 0xA) // Digit A (10)
        
        val instruction = factory.createInstruction(0xE010) // CONVERT_BYTE_TO_ASCII r0 -> r1
        instruction.execute(cpu)
        
        assertEquals(0x41, cpu.getRegister(1)) // ASCII 'A' = 0x41
    }
    
    @Test
    fun `test CONVERT_BYTE_TO_ASCII instruction invalid digit`() {
        cpu.setRegister(0, 0x10) // Invalid digit (> 0xF)
        
        val instruction = factory.createInstruction(0xE010) // CONVERT_BYTE_TO_ASCII r0 -> r1
        
        assertThrows(IllegalStateException::class.java) {
            instruction.execute(cpu)
        }
    }
    
    @Test
    fun `test DRAW instruction validation`() {
        cpu.setRegister(1, 0x41) // ASCII 'A'
        cpu.setRegister(2, 3)    // Row 3
        cpu.setRegister(3, 4)    // Column 4
        
        val instruction = factory.createInstruction(0xF123) // DRAW r1 at (r2, r3)
        
        // This should not throw an exception (validation should pass)
        assertDoesNotThrow { instruction.execute(cpu) }
    }
    
    @Test
    fun `test DRAW instruction invalid ASCII character`() {
        cpu.setRegister(1, 0x80) // Invalid ASCII (> 0x7F)
        cpu.setRegister(2, 0)    // Row 0
        cpu.setRegister(3, 0)    // Column 0
        
        val instruction = factory.createInstruction(0xF123) // DRAW r1 at (r2, r3)
        
        assertThrows(IllegalStateException::class.java) {
            instruction.execute(cpu)
        }
    }
    
    @Test
    fun `test DRAW instruction invalid coordinates`() {
        cpu.setRegister(1, 0x41) // ASCII 'A'
        cpu.setRegister(2, 8)    // Invalid row (> 7)
        cpu.setRegister(3, 0)    // Column 0
        
        val instruction = factory.createInstruction(0xF123) // DRAW r1 at (r2, r3)
        
        assertThrows(IllegalStateException::class.java) {
            instruction.execute(cpu)
        }
    }
    
    // === INSTRUCTION FORMAT VALIDATION TESTS ===
    
    @Test
    fun `test READ instruction format validation`() {
        assertThrows(IllegalArgumentException::class.java) {
            val instruction = factory.createInstruction(0x3712) // READ with non-zero operands 2,3
            instruction.execute(cpu)
        }
    }
    
    @Test
    fun `test WRITE instruction format validation`() {
        assertThrows(IllegalArgumentException::class.java) {
            val instruction = factory.createInstruction(0x4312) // WRITE with non-zero operands 2,3
            instruction.execute(cpu)
        }
    }
    
    @Test
    fun `test SKIP_EQUAL instruction format validation`() {
        assertThrows(IllegalArgumentException::class.java) {
            val instruction = factory.createInstruction(0x8123) // SKIP_EQUAL with non-zero operand 3
            instruction.execute(cpu)
        }
    }
    
    @Test
    fun `test SWITCH_MEMORY instruction format validation`() {
        assertThrows(IllegalArgumentException::class.java) {
            val instruction = factory.createInstruction(0x7123) // SWITCH_MEMORY with non-zero operands
            instruction.execute(cpu)
        }
    }
    
    // === INTEGRATION TESTS ===
    
    @Test
    fun `test instruction sequence - simple program`() {
        // Program: STORE 42 in r0, STORE 8 in r1, ADD r0+r1 -> r2
        rom[0] = 0x00.toByte()
        rom[1] = 0x2A.toByte()  // STORE 42 in r0
        rom[2] = 0x01.toByte()
        rom[3] = 0x08.toByte()  // STORE 8 in r1
        rom[4] = 0x10.toByte()
        rom[5] = 0x12.toByte()  // ADD r0 + r1 -> r2
        
        cpu.setPC(0)
        
        // Execute first instruction
        cpu.executeInstruction()
        assertEquals(42, cpu.getRegister(0))
        assertEquals(2, cpu.getPC())
        
        // Execute second instruction
        cpu.executeInstruction()
        assertEquals(8, cpu.getRegister(1))
        assertEquals(4, cpu.getPC())
        
        // Execute third instruction
        cpu.executeInstruction()
        assertEquals(50, cpu.getRegister(2)) // 42 + 8 = 50
        assertEquals(6, cpu.getPC())
    }
    
    @Test
    fun `test instruction sequence with memory operations`() {
        // Program: SET_A 0x200, STORE 99 in r0, WRITE r0 to memory, READ back into r1
        rom[0] = 0xA2.toByte()
        rom[1] = 0x00.toByte()  // SET_A 0x200
        rom[2] = 0x00.toByte()
        rom[3] = 0x63.toByte()  // STORE 99 in r0
        rom[4] = 0x40.toByte()
        rom[5] = 0x00.toByte()  // WRITE r0 to memory
        rom[6] = 0x31.toByte()
        rom[7] = 0x00.toByte()  // READ from memory into r1
        
        cpu.setPC(0)
        cpu.setMemoryFlag(false) // RAM mode
        
        // Execute SET_A
        cpu.executeInstruction()
        assertEquals(0x200, cpu.getAddressRegister())
        
        // Execute STORE
        cpu.executeInstruction()
        assertEquals(99, cpu.getRegister(0))
        
        // Execute WRITE
        cpu.executeInstruction()
        assertEquals(99.toByte(), ram[0x200])
        
        // Execute READ
        cpu.executeInstruction()
        assertEquals(99, cpu.getRegister(1))
    }
}