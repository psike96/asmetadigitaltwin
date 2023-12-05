package org.asmeta.asmetadt;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asmeta.parser.ASMParser;
import org.asmeta.simulator_at_runtime.runtime_container.Esit;
import org.asmeta.simulator_at_runtime.runtime_container.RunOutput;
import org.asmeta.simulator_at_runtime.runtime_container.SimulationContainer;

import asmeta.AsmCollection;
import asmeta.definitions.ControlledFunction;
import asmeta.definitions.Function;
import asmeta.definitions.MonitoredFunction;
import asmeta.definitions.domains.EnumTd;

public class AsmetaDTSimulator {
	
	private SimulationContainer modelEngine;
	private int id;
	
	private String asmModelPath;
	
	private Map<String, String> monitoredDomain;
	private Map<String, String> controlledDomain;
	
	private int period;
	private boolean isRunning = false;
	private Thread simulationThread;
	
	private AsmetaDigitalTwin asmetaDT;
	
	public AsmetaDTSimulator (String asmModelPath, AsmetaDigitalTwin asmetaDT, int period) {
		// ----- INIZIALIZZO IL MODEL ENGINE -----
		modelEngine = new SimulationContainer();
    	modelEngine.init(1);
    	
    	this.asmModelPath = asmModelPath;
    	id = modelEngine.startExecution(asmModelPath);
		
		if (id < 0) {
			System.err.println("ERROR: Simulation engine not initialized");
			return;
		}
		
		// ----- INIZIALIZZO LE MAPPE DELLE FUNZIONI CON I DOMINI ASSOCIATI -----
		monitoredDomain = new HashMap<>();
		controlledDomain = new HashMap<>();
		try {
			functionsDomainInitialization(asmModelPath);
		} catch (Exception e) {
			System.err.println("ERROR: functions domain and monitored not initialized");
			return;
		}
		
		// ----- INIZIALIZZO ASMETA DIGITAL TWIN E LE SUE MONITORATE -----
		this.asmetaDT = asmetaDT;
		monitoredDomain.keySet().forEach(monitoredName -> this.asmetaDT.getMonitored(monitoredName));
		
		// ----- INIZIALIZZO LA FUNZIONE DI SIMULAZIONE AL THREAD -----
		this.period = period;
		simulationThread = new Thread(() -> this.simulation());
	}
	
	private void functionsDomainInitialization (String asmModelPath) throws Exception {
		File asmFile = new File(asmModelPath);
		// assert(asmFile.exists());
		
		if (!asmFile.exists()) {
			throw new Exception("ERROR: ASM model file not found");
		}
		
		AsmCollection asm = ASMParser.setUpReadAsm(asmFile);
		
		List<Function> functions = asm.getMain().getHeaderSection().getSignature().getFunction();
		
		for (Function f : functions) {
			if (f instanceof MonitoredFunction) {
				if (f.getCodomain() instanceof EnumTd)
					monitoredDomain.put(f.getName(), "Enum");
				else
					monitoredDomain.put(f.getName(), f.getCodomain().getName());
			} else if (f instanceof ControlledFunction) {
				if (f.getCodomain() instanceof EnumTd)
					controlledDomain.put(f.getName(), "Enum");
				else
					controlledDomain.put(f.getName(), f.getCodomain().getName());
			}
		}
		
		/*
		functions.forEach(f -> {
			if (f instanceof ControlledFunction) {
				controlledDomain.put(
						f.getName(),
						(f.getCodomain() instanceof EnumTd
							? "Enum"
							: f.getCodomain().getName())
				);
			} else if (f instanceof MonitoredFunction) {
				monitoredDomain.put(
						f.getName(),
						(f.getCodomain() instanceof EnumTd
							? "Enum"
							: f.getCodomain().getName())
				);
			}
		});
		*/
	}
	
	// Converte una mappa di monitorate (String,Object) proveniente dal DT in una mappa di (String,String) destinata ad Asmeta
	private Map<String, String> prepareInput (Map<String, Object> monitoredFromDT) {
		Map<String, String> input = monitoredFromDT.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> AsmetaDTMapper.objectToString(entry.getValue(), monitoredDomain.get(entry.getKey()))
		));
		
		return input;
	}
	
	// Converte una mappa di controllate (String,String) proveniente da Asmeta in una mappa di (String,Object) destinata al DT
	private Map<String,Object> prepareOutput (Map<String, String> controlledFromAsmeta) {
		Map<String, Object> output = controlledFromAsmeta.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> AsmetaDTMapper.stringToObject(entry.getValue(), controlledDomain.get(entry.getKey()))
		));
		
		return output;
	}
	
	// Avvia la simulazione
	public void runSimulation () {
		
		if (isRunning) {
			System.out.println("WARNING: the simulation is already running");
			return;
		}
		
		isRunning = true;
		
		simulationThread.start();
		
	}
	
	// Sospende la simulazione
	public void suspendSimulation () {
		
		if (!isRunning) {
			System.out.println("WARNING: the simulation has already been suspended");
			return;
		}
		
		isRunning = false;
		
		try {
            simulationThread.join(); // attendo che la simulazione venga interrotta
            System.out.println("Simulation suspended");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		
	}
	
	/* Corpo della simulazione e del thread che verra' lanciato
	 * Racchiude un ciclo while che continua ad essere eseguito finche' non viene sospeso,
	 * ovvero finche' isRunning rimane true
	 */
	private void simulation () {
		
		Map<String, Object> monitoredFromDT;
		Map<String, String> input;
		
		Map<String, String> controlledFromAsmeta;
		Map<String, Object> output;
		
		RunOutput result;
		
		long startTime;
		long stepDuration;
		
		while (isRunning) {
			startTime = System.currentTimeMillis();
			
			monitoredFromDT = asmetaDT.getMonitored();
			input = prepareInput(monitoredFromDT);
			
			result = modelEngine.runStep(id, input);
			
			if (result.getEsit() == Esit.SAFE) {
				controlledFromAsmeta = result.getControlledvalues();
				output = prepareOutput(controlledFromAsmeta);
				asmetaDT.updateControlled(output);
				// currentState.putAll(controlledFromAsmeta);
			} else {
				System.out.println(String.format("Error: something got wrong with the outcome of the simulated system <%s>", asmModelPath));
			}
			
			stepDuration = System.currentTimeMillis() - startTime;
			if (stepDuration < period) {
				try {
					Thread.sleep(period - stepDuration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	public boolean isRunning () {
		return isRunning;
	}

}
