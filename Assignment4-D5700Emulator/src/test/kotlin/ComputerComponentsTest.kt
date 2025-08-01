import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class ComputerComponentsTest {
    
    // === RAM TESTS ===
    
    @Test
    fun `test RAM read and write operations`() {
        val ram = RAM(1024) // 1KB for testing
        
        // Test basic read/write
        ram.write(0, 0x42)
        ram.write(100, 0xFF)
        
        assertEquals(0x42, ram.read(0))
        assertEquals(0xFF, ram.read(100))
        assertEquals(0, ram.read(50)) // Unwritten location should be 0
    }
    
    @Test
    fun `test RAM bounds checking`() {
        val ram = RAM(100)
        
        // Test valid bounds
        ram.write(0, 0x42)
        ram.write(99, 0xFF)
        assertEquals(0x42, ram.read(0))
        assertEquals(0xFF, ram.read(99))
        
        // Test invalid bounds
        assertThrows(IllegalArgumentException::class.java) { ram.read(-1) }
        assertThrows(IllegalArgumentException::class.java) { ram.read(100) }
        assertThrows(IllegalArgumentException::class.java) { ram.write(-1, 0) }
        assertThrows(IllegalArgumentException::class.java) { ram.write(100, 0) }
        assertThrows(IllegalArgumentException::class.java) { ram.write(0, 256) }
        assertThrows(IllegalArgumentException::class.java) { ram.write(0, -1) }
    }
    
    @Test
    fun `test RAM clear functionality`() {
        val ram = RAM(100)
        
        // Write some data
        ram.write(0, 0x42)
        ram.write(50, 0xFF)
        
        // Clear RAM
        ram.clear()
        
        // Verify all cleared
        assertEquals(0, ram.read(0))
        assertEquals(0, ram.read(50))
    }
    
    @Test
    fun `test RAM data loading`() {
        val ram = RAM(100)
        val testData = byteArrayOf(0x10, 0x20, 0x30, 0x40)
        
        ram.loadData(10, testData)
        
        assertEquals(0x10, ram.read(10))
        assertEquals(0x20, ram.read(11))
        assertEquals(0x30, ram.read(12))
        assertEquals(0x40, ram.read(13))
    }
    
    // === ROM TESTS ===
    
    @Test
    fun `test ROM read operations`() {
        val rom = ROM(1024)
        val program = intArrayOf(0x10, 0x30, 0x00, 0xFF, 0x52, 0x00)
        
        rom.loadProgram(program)
        
        assertEquals(0x10, rom.read(0))
        assertEquals(0x30, rom.read(1))
        assertEquals(0x00, rom.read(2))
        assertEquals(0xFF, rom.read(3))
        assertEquals(0x52, rom.read(4))
        assertEquals(0x00, rom.read(5))
    }
    
    @Test
    fun `test ROM write protection`() {
        val rom = ROM(100)
        
        // ROM should be read-only by default
        assertFalse(rom.isWritable())
        
        // Writing should fail
        assertThrows(IllegalStateException::class.java) {
            rom.write(0, 0x42)
        }
        
        // Enable writing and test
        rom.setWritable(true)
        assertTrue(rom.isWritable())
        
        rom.write(0, 0x42)
        assertEquals(0x42, rom.read(0))
    }
    
    @Test
    fun `test ROM program loading with byte array`() {
        val rom = ROM(100)
        val program = byteArrayOf(0x10, 0x20, 0x30)
        
        rom.loadProgram(program)
        
        assertEquals(0x10, rom.read(0))
        assertEquals(0x20, rom.read(1))
        assertEquals(0x30, rom.read(2))
        assertEquals(0, rom.read(3)) // Should be cleared
    }
    
    // === TIMER TESTS ===
    
    @Test
    fun `test Timer basic operations`() {
        val timer = Timer()
        
        // Initial state
        assertEquals(0, timer.getTime())
        assertFalse(timer.isRunning())
        
        // Set timer
        timer.setTime(10)
        assertEquals(10, timer.getTime())
        assertTrue(timer.isRunning())
        
        // Decrement
        timer.decrement()
        assertEquals(9, timer.getTime())
        assertTrue(timer.isRunning())
        
        // Decrement to zero
        repeat(9) { timer.decrement() }
        assertEquals(0, timer.getTime())
        assertFalse(timer.isRunning())
        
        // Decrement when already zero
        timer.decrement()
        assertEquals(0, timer.getTime())
    }
    
    @Test
    fun `test Timer bounds checking`() {
        val timer = Timer()
        
        // Valid bounds
        timer.setTime(0)
        timer.setTime(255)
        
        // Invalid bounds
        assertThrows(IllegalArgumentException::class.java) { timer.setTime(-1) }
        assertThrows(IllegalArgumentException::class.java) { timer.setTime(256) }
    }
    
    @Test
    fun `test Timer reset functionality`() {
        val timer = Timer()
        
        timer.setTime(50)
        assertTrue(timer.isRunning())
        
        timer.reset()
        assertEquals(0, timer.getTime())
        assertFalse(timer.isRunning())
    }
    
    @Test
    fun `test Timer time calculations`() {
        val timer = Timer()
        
        timer.setTime(60) // 60 counts = 1 second at 60Hz
        
        assertEquals(1000, timer.getTimeRemainingMs()) // Should be ~1000ms
        assertEquals(1.0, timer.getTimeRemainingSec(), 0.1) // Should be ~1 second
        
        timer.setTime(30) // 30 counts = 0.5 seconds
        assertEquals(0.5, timer.getTimeRemainingSec(), 0.1)
    }
    
    // === SCREEN TESTS ===
    
    @Test
    fun `test Screen basic drawing operations`() {
        val screen = Screen()
        
        // Draw some characters
        screen.draw(0x41, 0, 0) // 'A' at (0,0)
        screen.draw(0x42, 1, 2) // 'B' at (1,2)
        screen.draw(0x43, 7, 7) // 'C' at (7,7)
        
        // Verify characters are drawn
        assertEquals(0x41, screen.getCharAt(0, 0))
        assertEquals(0x42, screen.getCharAt(1, 2))
        assertEquals(0x43, screen.getCharAt(7, 7))
        
        // Verify display contains the characters
        val display = screen.getDisplay()
        assertTrue(display.contains("A"))
    }
    
    @Test
    fun `test Screen bounds checking`() {
        val screen = Screen()
        
        // Valid bounds
        screen.draw(0x41, 0, 0)
        screen.draw(0x42, 7, 7)
        
        // Invalid coordinates
        assertThrows(IllegalArgumentException::class.java) { screen.draw(0x41, -1, 0) }
        assertThrows(IllegalArgumentException::class.java) { screen.draw(0x41, 8, 0) }
        assertThrows(IllegalArgumentException::class.java) { screen.draw(0x41, 0, -1) }
        assertThrows(IllegalArgumentException::class.java) { screen.draw(0x41, 0, 8) }
        
        // Invalid ASCII values
        assertThrows(IllegalArgumentException::class.java) { screen.draw(0x80, 0, 0) } // > 127
        assertThrows(IllegalArgumentException::class.java) { screen.draw(-1, 0, 0) }
    }
    
    @Test
    fun `test Screen clear functionality`() {
        val screen = Screen()
        
        // Draw some characters
        screen.draw(0x41, 0, 0)
        screen.draw(0x42, 4, 4)
        
        // Clear screen
        screen.clear()
        
        // Should all be spaces now
        assertEquals(0x20, screen.getCharAt(0, 0)) // Space character
        assertEquals(0x20, screen.getCharAt(4, 4))
    }
    
    @Test
    fun `test Screen frame buffer operations`() {
        val screen = Screen()
        
        // Test direct frame buffer access
        screen.writeToFrameBuffer(0, 0x41) // Position (0,0)
        screen.writeToFrameBuffer(9, 0x42)  // Position (1,1) = 1*8 + 1 = 9
        
        assertEquals(0x41, screen.readFromFrameBuffer(0))
        assertEquals(0x42, screen.readFromFrameBuffer(9))
        
        // Test bounds
        assertThrows(IllegalArgumentException::class.java) { 
            screen.writeToFrameBuffer(-1, 0x41) 
        }
        assertThrows(IllegalArgumentException::class.java) { 
            screen.writeToFrameBuffer(64, 0x41) 
        }
    }
    
    @Test
    fun `test Screen pattern loading`() {
        val screen = Screen()
        val pattern = intArrayOf(0x48, 0x45, 0x4C, 0x4C, 0x4F, 0x20, 0x20, 0x20) // "HELLO   "
        
        screen.loadPattern(pattern)
        
        assertEquals(0x48, screen.getCharAt(0, 0)) // 'H'
        assertEquals(0x45, screen.getCharAt(0, 1)) // 'E'
        assertEquals(0x4C, screen.getCharAt(0, 2)) // 'L'
        assertEquals(0x4C, screen.getCharAt(0, 3)) // 'L'
        assertEquals(0x4F, screen.getCharAt(0, 4)) // 'O'
    }
    
    @Test
    fun `test Screen dimensions`() {
        val screen = Screen()
        val (width, height) = screen.getDimensions()
        
        assertEquals(8, width)
        assertEquals(8, height)
        assertEquals(64, screen.getFrameBufferSize())
    }
    
    // === COMPUTER INTEGRATION TESTS ===
    
    @Test
    fun `test Computer initialization`() {
        val computer = Computer()
        
        assertFalse(computer.isRunning())
        assertFalse(computer.isProgramLoaded())
        assertEquals(0, computer.getProgramCounter())
        assertEquals(0, computer.getTimerValue())
    }
    
    @Test
    fun `test Computer ROM loading`() {
        val computer = Computer()
        val program = intArrayOf(0x10, 0x30, 0x00, 0xFF) // Simple ADD instruction
        
        computer.loadROM(program)
        
        assertTrue(computer.isProgramLoaded())
        assertEquals(0x10, computer.readROM(0))
        assertEquals(0x30, computer.readROM(1))
        assertEquals(0x00, computer.readROM(2))
        assertEquals(0xFF, computer.readROM(3))
    }
    
    @Test
    fun `test Computer register access`() {
        val computer = Computer()
        val program = intArrayOf(0x00, 0x42) // STORE 0x42 in r0
        
        computer.loadROM(program)
        
        // Initially should be 0
        assertEquals(0, computer.getRegisterValue(0))
        
        // After reset, should still be 0
        computer.reset()
        assertEquals(0, computer.getRegisterValue(0))
    }
    
    @Test
    fun `test Computer timer operations`() {
        val computer = Computer()
        
        computer.setTimerValue(50)
        assertEquals(50, computer.getTimerValue())
        
        computer.reset()
        assertEquals(0, computer.getTimerValue())
    }
    
    @Test
    fun `test Computer keyboard input simulation`() {
        val computer = Computer()
        
        // Initially no input
        assertEquals(-1, computer.readKeyboard())
        
        // Add some input
        computer.addKeyboardInput(0xA)
        computer.addKeyboardInput(0x5)
        
        assertEquals(0xA, computer.readKeyboard())
        assertEquals(0x5, computer.readKeyboard())
        assertEquals(-1, computer.readKeyboard()) // Queue empty
        
        // Test string input
        computer.addKeyboardInput("1AF")
        assertEquals(0x1, computer.readKeyboard())
        assertEquals(0xA, computer.readKeyboard())
        assertEquals(0xF, computer.readKeyboard())
    }
    
    @Test
    fun `test Computer screen access`() {
        val computer = Computer()
        
        val screen = computer.getScreen()
        screen.draw(0x48, 0, 0) // 'H'
        
        val screenOutput = computer.getScreenOutput()
        assertTrue(screenOutput.contains("H"))
    }
    
    @Test
    fun `test Computer memory access`() {
        val computer = Computer()
        
        // Test RAM access
        computer.writeRAM(100, 0x42)
        assertEquals(0x42, computer.readRAM(100))
        
        // Test ROM access (after loading program)
        val program = intArrayOf(0x10, 0x20, 0x30)
        computer.loadROM(program)
        assertEquals(0x10, computer.readROM(0))
        assertEquals(0x20, computer.readROM(1))
        assertEquals(0x30, computer.readROM(2))
    }
    
    @Test
    fun `test Computer system status`() {
        val computer = Computer()
        
        val status = computer.getSystemStatus()
        assertTrue(status.contains("D5700 Computer Status"))
        assertTrue(status.contains("Running: NO"))
        assertTrue(status.contains("Program Loaded: NO"))
    }
    
    @Test
    fun `test Computer reset functionality`() {
        val computer = Computer()
        val program = intArrayOf(0x10, 0x30, 0x00, 0xFF)
        
        computer.loadROM(program)
        computer.setTimerValue(50)
        computer.addKeyboardInput(0xA)
        computer.writeRAM(100, 0x42)
        
        // Verify state before reset
        assertTrue(computer.isProgramLoaded())
        assertEquals(50, computer.getTimerValue())
        assertEquals(0xA, computer.readKeyboard())
        assertEquals(0x42, computer.readRAM(100))
        
        // Reset computer
        computer.reset()
        
        // Verify state after reset
        assertEquals(0, computer.getProgramCounter())
        assertEquals(0, computer.getTimerValue())
        assertEquals(-1, computer.readKeyboard()) // Queue should be empty
        assertEquals(0, computer.readRAM(100)) // RAM should be cleared
        assertFalse(computer.isRunning())
    }
}