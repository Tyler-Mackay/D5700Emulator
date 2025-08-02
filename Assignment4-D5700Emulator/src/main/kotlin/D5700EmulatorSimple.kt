/**
 * Simple D5700 Emulator - No Input Required
 * 
 * This version automatically loads a ROM file for easy testing in IntelliJ.
 * No user input required - just hit run!
 */
fun main() {
    println("=== D5700 Computer Emulator (Simple) ===")
    println()
    
    // Try different ROM files in order of preference
    val romFiles = listOf(
        "D5700roms/roms/hello.d5700",
        "D5700roms/roms/addition.d5700", 
        "D5700roms/roms/subtraction.d5700",
        "D5700roms/roms/timer.d5700"
    )
    
    var romLoaded = false
    
    for (romPath in romFiles) {
        try {
            println("Trying to load ROM: $romPath")
            runRomFile(romPath)
            romLoaded = true
            break
        } catch (e: Exception) {
            println("Could not load $romPath: ${e.message}")
        }
    }
    
    if (!romLoaded) {
        println("Could not load any ROM files. Running hardcoded test program...")
        runHardcodedDemo()
    }
    
    println()
    println("Simple D5700 Emulator finished.")
}

/**
 * Run a simple hardcoded demo program
 */
fun runHardcodedDemo() {
    try {
        // Simple program: Display "HI" on screen
        val testProgram = intArrayOf(
            0x00, 0x48,  // STORE 'H' (0x48) in r0
            0x01, 0x00,  // STORE 0 (row) in r1
            0x02, 0x00,  // STORE 0 (column) in r2
            0xF0, 0x12,  // DRAW r0 at (r1, r2) - 'H' at (0,0)
            0x00, 0x49,  // STORE 'I' (0x49) in r0
            0x02, 0x01,  // STORE 1 (column) in r2
            0xF0, 0x12,  // DRAW r0 at (r1, r2) - 'I' at (0,1)
            0x52, 0x00   // JUMP to address 0x200 (halt)
        )
        
        println("Creating D5700 computer...")
        val computer = Computer()
        
        println("Loading hardcoded demo program...")
        computer.loadROM(testProgram)
        
        println("Running hardcoded demo...")
        println()
        
        // Create emulator runner for the demo
        val emulator = D5700EmulatorRunner(testProgram)
        emulator.run()
        
        println("Hardcoded demo completed successfully!")
        
    } catch (e: Exception) {
        println("Error in hardcoded demo: ${e.message}")
    }
}