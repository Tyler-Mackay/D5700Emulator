import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * D5700 Computer Emulator - Console Version
 * 
 * Alternative implementation using BufferedReader for better IntelliJ compatibility
 */
fun main() {
    println("=== D5700 Computer Emulator (Console Version) ===")
    println()
    
    try {
        // Use BufferedReader instead of Scanner - often works better in IntelliJ
        val reader = BufferedReader(InputStreamReader(System.`in`))
        
        println("Console input system initialized.")
        
        // Prompt for ROM file path
        print("Type in the path to a rom file: ")
        System.out.flush()
        
        // Read input line
        val romPath = try {
            reader.readLine()?.trim() ?: ""
        } catch (e: Exception) {
            println("Failed to read input: ${e.message}")
            println()
            println("Console input is not working properly in IntelliJ.")
            println("Please run from terminal instead:")
            println("  ./gradlew run")
            println()
            println("Or use the 'D5700 Simple (Auto)' run configuration for testing.")
            return
        }
        
        println("You entered: '$romPath'")
        
        // Check if user wants to exit
        if (romPath.lowercase() in listOf("exit", "quit", "q")) {
            println("Goodbye!")
            return
        }
        
        // Load and run the ROM file
        if (romPath.isNotEmpty()) {
            println("Loading ROM file: $romPath")
            runRomFile(romPath)
        } else {
            println("No ROM file specified. Exiting.")
        }
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
    
    println()
    println("D5700 Emulator finished.")
}