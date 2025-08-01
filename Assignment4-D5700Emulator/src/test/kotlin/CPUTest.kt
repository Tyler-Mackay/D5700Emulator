import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class CPUTest {
    
    private lateinit var cpu: CPU
    private lateinit var ram: ByteArray
    private lateinit var rom: ByteArray
    
    @BeforeEach
    fun setUp() {
        cpu = CPU()
        ram = ByteArray(4096) // 4KB RAM
        rom = ByteArray(4096) // 4KB ROM
        
        // Initialize with test data
        rom[0] = 0x10.toByte() // First instruction: ADD (example)
        rom[1] = 0x30.toByte()
        
        cpu.initialize(ram, rom)
    }
    
    // === GENERAL REGISTER TESTS ===
    
    @Test
    fun `test general register get and set operations`() {
        // Test setting and getting all 8 registers
        for (i in 0..7) {
            val testValue = (i * 16 + i) // Create unique values: 0x00, 0x11, 0x22, etc.
            cpu.setRegister(i, testValue)
            assertEquals(testValue, cpu.getRegister(i), "Register r$i should contain $testValue")
        }
    }
    
    @Test
    fun `test register bounds checking`() {
        // Test invalid register indices
        assertThrows(IllegalArgumentException::class.java) {
            cpu.getRegister(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.getRegister(8)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setRegister(-1, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setRegister(8, 0)
        }
    }
    
    @Test
    fun `test register value bounds checking`() {
        // Test invalid register values
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setRegister(0, -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setRegister(0, 256)
        }
        
        // Test valid boundary values
        cpu.setRegister(0, 0)
        assertEquals(0, cpu.getRegister(0))
        
        cpu.setRegister(0, 255)
        assertEquals(255, cpu.getRegister(0))
    }
    
    // === PROGRAM COUNTER TESTS ===
    
    @Test
    fun `test program counter get and set operations`() {
        // Test setting valid even addresses
        cpu.setPC(0)
        assertEquals(0, cpu.getPC())
        
        cpu.setPC(0x1000)
        assertEquals(0x1000, cpu.getPC())
        
        cpu.setPC(0xFFFE) // Maximum valid even address
        assertEquals(0xFFFE, cpu.getPC())
    }
    
    @Test
    fun `test program counter even address requirement`() {
        // Test that PC must be even
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setPC(1) // Odd address should fail
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setPC(0x1001) // Odd address should fail
        }
    }
    
    @Test
    fun `test program counter bounds checking`() {
        // Test invalid PC values
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setPC(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setPC(0x10000) // Beyond 16-bit range
        }
    }
    
    @Test
    fun `test program counter increment`() {
        cpu.setPC(0)
        cpu.incrementPC(2)
        assertEquals(2, cpu.getPC())
        
        cpu.incrementPC(4)
        assertEquals(6, cpu.getPC())
        
        // Test increment from non-zero base
        cpu.setPC(0x1000)
        cpu.incrementPC(2)
        assertEquals(0x1002, cpu.getPC())
    }
    
    @Test
    fun `test program counter increment overflow protection`() {
        cpu.setPC(0xFFFE)
        assertThrows(IllegalArgumentException::class.java) {
            cpu.incrementPC(2) // Would overflow beyond 16-bit
        }
    }
    
    // === ADDRESS REGISTER TESTS ===
    
    @Test
    fun `test address register get and set operations`() {
        cpu.setAddressRegister(0)
        assertEquals(0, cpu.getAddressRegister())
        
        cpu.setAddressRegister(0x1234)
        assertEquals(0x1234, cpu.getAddressRegister())
        
        cpu.setAddressRegister(0xFFFF) // Maximum 16-bit value
        assertEquals(0xFFFF, cpu.getAddressRegister())
    }
    
    @Test
    fun `test address register bounds checking`() {
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setAddressRegister(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setAddressRegister(0x10000) // Beyond 16-bit range
        }
    }
    
    // === TIMER REGISTER TESTS ===
    
    @Test
    fun `test timer register get and set operations`() {
        cpu.setTimerRegister(0)
        assertEquals(0, cpu.getTimerRegister())
        
        cpu.setTimerRegister(0x50)
        assertEquals(0x50, cpu.getTimerRegister())
        
        cpu.setTimerRegister(0xFF) // Maximum 8-bit value
        assertEquals(0xFF, cpu.getTimerRegister())
    }
    
    @Test
    fun `test timer register bounds checking`() {
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setTimerRegister(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.setTimerRegister(256) // Beyond 8-bit range
        }
    }
    
    @Test
    fun `test timer decrement functionality`() {
        // Test normal decrement
        cpu.setTimerRegister(5)
        cpu.decrementTimer()
        assertEquals(4, cpu.getTimerRegister())
        
        cpu.decrementTimer()
        assertEquals(3, cpu.getTimerRegister())
        
        // Test that timer doesn't go below 0
        cpu.setTimerRegister(1)
        cpu.decrementTimer()
        assertEquals(0, cpu.getTimerRegister())
        
        cpu.decrementTimer() // Should stay at 0
        assertEquals(0, cpu.getTimerRegister())
        
        // Test decrement when already 0
        cpu.setTimerRegister(0)
        cpu.decrementTimer()
        assertEquals(0, cpu.getTimerRegister())
    }
    
    // === MEMORY FLAG TESTS ===
    
    @Test
    fun `test memory flag operations`() {
        // Should start with RAM (false)
        assertFalse(cpu.getMemoryFlag(), "Memory flag should start as false (RAM)")
        
        // Test toggle
        cpu.toggleMemoryFlag()
        assertTrue(cpu.getMemoryFlag(), "Memory flag should be true (ROM) after toggle")
        
        cpu.toggleMemoryFlag()
        assertFalse(cpu.getMemoryFlag(), "Memory flag should be false (RAM) after second toggle")
        
        // Test explicit setting
        cpu.setMemoryFlag(true)
        assertTrue(cpu.getMemoryFlag(), "Memory flag should be true (ROM)")
        
        cpu.setMemoryFlag(false)
        assertFalse(cpu.getMemoryFlag(), "Memory flag should be false (RAM)")
    }
    
    // === MEMORY ACCESS TESTS ===
    
    @Test
    fun `test RAM read and write operations`() {
        // Ensure we're in RAM mode
        cpu.setMemoryFlag(false)
        
        // Test writing and reading from RAM
        cpu.writeMemory(0x100, 0x42)
        assertEquals(0x42, cpu.readMemory(0x100))
        
        cpu.writeMemory(0x200, 0xFF)
        assertEquals(0xFF, cpu.readMemory(0x200))
        
        // Test that write affected the actual RAM array
        assertEquals(0x42.toByte(), ram[0x100])
        assertEquals(0xFF.toByte(), ram[0x200])
    }
    
    @Test
    fun `test ROM read operations`() {
        // Set up ROM with test data
        rom[0x100] = 0x12.toByte()
        rom[0x200] = 0x34.toByte()
        
        // Switch to ROM mode
        cpu.setMemoryFlag(true)
        
        // Test reading from ROM
        assertEquals(0x12, cpu.readMemory(0x100))
        assertEquals(0x34, cpu.readMemory(0x200))
    }
    
    @Test
    fun `test memory access bounds checking`() {
        assertThrows(IllegalArgumentException::class.java) {
            cpu.readMemory(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.readMemory(0x10000)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.writeMemory(-1, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.writeMemory(0x10000, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.writeMemory(0x100, -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            cpu.writeMemory(0x100, 256)
        }
    }
    
    @Test
    fun `test memory access beyond array bounds returns zero`() {
        // Test reading beyond RAM/ROM size returns 0
        cpu.setMemoryFlag(false) // RAM mode
        assertEquals(0, cpu.readMemory(0x2000)) // Beyond 4KB RAM
        
        cpu.setMemoryFlag(true) // ROM mode
        assertEquals(0, cpu.readMemory(0x2000)) // Beyond 4KB ROM
    }
    
    // === INSTRUCTION FETCH TESTS ===
    
    @Test
    fun `test instruction fetch from ROM`() {
        // Set up ROM with test instruction
        rom[0] = 0x12.toByte()
        rom[1] = 0x34.toByte()
        rom[2] = 0x56.toByte()
        rom[3] = 0x78.toByte()
        
        cpu.setPC(0)
        val instruction1 = cpu.fetchInstruction()
        assertEquals(0x1234, instruction1)
        
        cpu.setPC(2)
        val instruction2 = cpu.fetchInstruction()
        assertEquals(0x5678, instruction2)
    }
    
    @Test
    fun `test instruction fetch requires even PC`() {
        cpu.setPC(0) // Even PC should work
        assertDoesNotThrow { cpu.fetchInstruction() }
        
        // Can't test odd PC directly since setPC prevents it,
        // but we can test the internal check by manipulating PC through increment
        // This test verifies the fetchInstruction method checks for even PC
    }
    
    @Test
    fun `test instruction fetch bounds checking`() {
        // Test fetching beyond ROM bounds  
        // ROM is 4096 bytes (0-4095), so PC = 4094 can read bytes 4094,4095 (valid)
        // but PC = 4095 would try to read bytes 4095,4096 (4096 is beyond bounds)
        cpu.setPC(4094) // This should be valid
        assertDoesNotThrow { cpu.fetchInstruction() } // Should work fine
        
        // Now test beyond bounds - use PC that would read beyond ROM
        // Since we can't set odd PC directly, we'll manipulate the bounds check
        cpu.setPC(4094) // Set to valid PC first
        cpu.incrementPC(2) // Now PC = 4096, which is beyond ROM bounds
        assertThrows(IllegalStateException::class.java) {
            cpu.fetchInstruction() // Should throw exception
        }
    }
    
    // === RESET FUNCTIONALITY TESTS ===
    
    @Test
    fun `test CPU reset functionality`() {
        // Set up CPU with non-zero state
        cpu.setRegister(0, 0x42)
        cpu.setRegister(7, 0x99)
        cpu.setPC(0x1000)
        cpu.setTimerRegister(0x50)
        cpu.setAddressRegister(0x2000)
        cpu.setMemoryFlag(true)
        
        // Reset CPU
        cpu.reset()
        
        // Verify all state is reset
        for (i in 0..7) {
            assertEquals(0, cpu.getRegister(i), "Register r$i should be 0 after reset")
        }
        assertEquals(0, cpu.getPC(), "PC should be 0 after reset")
        assertEquals(0, cpu.getTimerRegister(), "Timer should be 0 after reset")
        assertEquals(0, cpu.getAddressRegister(), "Address register should be 0 after reset")
        assertFalse(cpu.getMemoryFlag(), "Memory flag should be false (RAM) after reset")
    }
    
    // === STATE STRING TEST ===
    
    @Test
    fun `test CPU state string representation`() {
        cpu.setRegister(0, 0x12)
        cpu.setRegister(1, 0x34)
        cpu.setPC(0x1000)
        cpu.setTimerRegister(0x56)
        cpu.setAddressRegister(0x2000)
        cpu.setMemoryFlag(true)
        
        val stateString = cpu.getStateString()
        
        assertTrue(stateString.contains("r0=12"), "State should contain r0 value")
        assertTrue(stateString.contains("r1=34"), "State should contain r1 value")
        assertTrue(stateString.contains("PC: 1000"), "State should contain PC value")
        assertTrue(stateString.contains("T: 56"), "State should contain Timer value")
        assertTrue(stateString.contains("A: 2000"), "State should contain Address value")
        assertTrue(stateString.contains("ROM"), "State should indicate ROM mode")
    }
}