package org.asmeta.asmetadt;

public class App 
{	
	private static String twinId;
	private static String endpoint;
	private static String asmModelPath;
	private static int period = 3000;
	
	public static void main( String[] args )
    {
		
		for (int i = 0; i+1 < args.length; i += 2) {
			if (!handleParameter(args[i], args[i+1])) {
				return;
			}
		}
		
		if (!areParametersValid()) {
			System.err.println("ERROR: invalid parameters");
			return;
		}
		
		AsmetaDigitalTwin twin = new AsmetaDigitalTwin(twinId, endpoint);
		
		AsmetaDTSimulator simulator = new AsmetaDTSimulator(asmModelPath, twin, period); // 5 secondi
		
		System.out.println("Starting the model simulation...");
		
		simulator.runSimulation();
		
		// Registra un Shutdown Hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Termination of the simulation...");
			simulator.suspendSimulation();
			System.out.println("SIMULATION TERMINATED successfully");
		}));
		
		System.out.println("\nSIMULATION STARTED, press CTRL+C to stop it.\n\n");
		
    }
	
	
	private static boolean handleParameter (String flag, String value) {
		switch (flag) {
			case "-a" -> {
				if (asmModelPath != null) {
					System.err.println("ERROR: asm model path is already setted");
					return false;
				}
				asmModelPath = value;
			}
			case "-t" -> {
				if (twinId != null) {
					System.err.println("ERROR: twinId is already setted");
					return false;
				}
				twinId = value;
			}
			case "-e" -> {
				if (endpoint != null) {
					System.err.println("ERROR: azure digital twins endpoint is already setted");
					return false;
				}
				endpoint = value;
			}
			case "-p" -> {
				if (period != 3000) {
					System.err.println("ERROR: period is already setted");
					return false;
				}
				period = Integer.valueOf(value);
				
			}
			default -> {
				System.out.println(String.format("ERROR: Flag %s not recognized. Only the following are accepted:", flag));
				System.out.println("  -a \\path\\model.asm");
				System.out.println("  -t twinid");
				System.out.println("  -e endpoint");
				System.out.println("  -p period");
				return false;
			}
		}
		return true;
	}
	
	private static boolean areParametersValid () {
		return twinId != null && !twinId.isBlank()
				&& endpoint != null && !endpoint.isBlank()
				&& asmModelPath != null && !asmModelPath.isBlank()
				&& period >= 0;
	}
	
}
