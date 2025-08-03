import java.io.File
import java.util.Scanner

/**
 * D5700 Computer Emulator - Main Entry Point
 * 
 * This is the main application that runs D5700 ROM files.
 * It prompts the user for a ROM file path and executes the program.
 */
fun main() {

    
    val scanner = Scanner(System.`in`)
    
    try {
        // Prompt for ROM file path - wait for user input
        print("Type in the path to a rom file: ")
        System.out.flush() // Force the prompt to appear
        
        // Read input line
        val romPath = scanner.nextLine().trim()
        
        // Check if user wants to exit
        if (romPath.lowercase() in listOf("exit", "quit", "q")) {
            println("Goodbye!")
            return
        }
        
        // Load and run the ROM file - only if path provided
        if (romPath.isNotEmpty()) {
            runRomFile(romPath)
        } else {
            println("No ROM file specified. Exiting.")
        }
        
    } catch (e: Exception) {
        println("Input error: ${e.message}")
        println()
        println("If you're having console input issues:")
        println("1. Try the terminal: ./gradlew run")
        println("2. Or use 'D5700 Console Version' run configuration")
        println("3. Or use 'D5700 Simple (Auto)' for testing without input")
    } finally {
        scanner.close()
    }
    
    println()
    println("D5700 Emulator finished.")
}

/**
 * Load and execute a ROM file
 */
fun runRomFile(romPath: String) {
    try {
        // Load ROM file
        val romFile = File(romPath)
        if (!romFile.exists()) {
            println("Error: ROM file not found: $romPath")
            return
        }
        
        if (!romFile.canRead()) {
            println("Error: Cannot read ROM file: $romPath")
            return
        }
        
        // Read ROM data - handle both binary and hex text formats
        val romProgram = if (romPath.endsWith(".d5700")) {
            // Hex text format (like existing ROM files)
            loadHexRomFile(romFile)
        } else {
            // Binary format
            loadBinaryRomFile(romFile)
        }
        
        if (romProgram.isEmpty()) {
            println("Error: Failed to load ROM data")
            return
        }
        

        
        // Create and run the emulator
        val emulator = D5700EmulatorRunner(romProgram)
        emulator.run()
        
    } catch (e: Exception) {
        println("Error loading ROM file: ${e.message}")
    }
}

/**
 * Load a hex text ROM file (like the existing .d5700 files)
 */
fun loadHexRomFile(romFile: File): IntArray {
    try {
        val lines = romFile.readLines()
        val romData = mutableListOf<Int>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                // Parse hex instruction (4 hex chars = 2 bytes)
                if (trimmedLine.length == 4) {
                    val instruction = trimmedLine.toInt(16)
                    // Split into two bytes
                    val firstByte = (instruction shr 8) and 0xFF
                    val secondByte = instruction and 0xFF
                    romData.add(firstByte)
                    romData.add(secondByte)
                } else {
                    println("Warning: Invalid hex instruction format: $trimmedLine")
                }
            }
        }
        
        return romData.toIntArray()
        
    } catch (e: Exception) {
        println("Error parsing hex ROM file: ${e.message}")
        return intArrayOf()
    }
}

/**
 * Load a binary ROM file
 */
fun loadBinaryRomFile(romFile: File): IntArray {
    try {
        val romData = romFile.readBytes()
        if (romData.isEmpty()) {
            println("Error: ROM file is empty")
            return intArrayOf()
        }
        
        if (romData.size > 4096) {
            println("Warning: ROM file is larger than 4KB. Only first 4KB will be loaded.")
        }
        
        // Convert byte array to int array
        val romProgram = IntArray(minOf(romData.size, 4096))
        for (i in romProgram.indices) {
            romProgram[i] = romData[i].toInt() and 0xFF
        }
        
        return romProgram
        
    } catch (e: Exception) {
        println("Error reading binary ROM file: ${e.message}")
        return intArrayOf()
    }
}

/**
 * D5700 Emulator Runner
 * Handles the execution loop and screen updates
 */
class D5700EmulatorRunner(private val romProgram: IntArray) {
    
    private val computer = Computer()
    private var lastScreenState = ""
    private var instructionCount = 0
    private val maxInstructions = 10000 // Prevent infinite loops
    
    fun run() {
        try {
            // Load ROM program
            computer.loadROM(romProgram)
            
            // Set computer to running state and start execution loop
            var shouldContinue = true
            while (shouldContinue && instructionCount < maxInstructions && !computer.hasTerminated()) {
                try {
                    // Execute one instruction
                    computer.executeInstruction()
                    instructionCount++
                    
                    // Check if screen has changed
                    val currentScreenState = computer.getScreenOutput()
                    if (currentScreenState != lastScreenState) {
                        printScreen("")
                        lastScreenState = currentScreenState
                    }
                    
                    // Check if program has terminated after execution
                    if (computer.hasTerminated()) {
                        shouldContinue = false
                    }
                    
                    // Small delay to make execution visible (remove for full speed)
                    Thread.sleep(1)
                    
                } catch (e: Exception) {
                    println("Execution stopped: ${e.message}")
                    shouldContinue = false
                }
            }
            
            if (instructionCount >= maxInstructions) {
                println("Execution stopped: Maximum instruction count reached ($maxInstructions)")
            }
            
        } catch (e: Exception) {
            println("Emulator error: ${e.message}")
        }
    }
    
    /**
     * Print the current screen state with separators
     */
    private fun printScreen(label: String) {
        if (label.isNotEmpty()) {
            println("======== $label ========")
        } else {
            println("========")
        }
        val screenOutput = computer.getScreen().getFormattedDisplay()
        println(screenOutput)
        println("========")
        println()
    }
}