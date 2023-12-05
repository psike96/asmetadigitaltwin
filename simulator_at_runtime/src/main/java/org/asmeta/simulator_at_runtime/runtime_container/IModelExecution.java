package org.asmeta.simulator_at_runtime.runtime_container;

import java.util.Map;
import org.asmeta.simulator.main.AsmModelNotFoundException;
import org.asmeta.simulator.main.MainRuleNotFoundException;
import org.asmeta.parser.ParseException;



/**
 * The Interface IModelExecution.
 * @author Massoda Tchoussi Frank, Patrizia Scandurra
 */
public interface IModelExecution {
	
	 /**
 	 * Start a model execution.
 	 *
 	 * @param modelPath the model path
 	 * @return the simulation identifier (int); -1 if the maximum number of simulations is exceeded 
 	 */
 	int startExecution(String modelPath) throws MainRuleNotFoundException, AsmModelNotFoundException, FullMapException, ParseException;
	 
	 /**
 	 * Run step.
 	 *
 	 * @param ids the simulation id
 	 * @param locationValue the location value
 	 * @param modelPath the model path
 	 * @return the run output
 	 */
 	RunOutput runStep(int ids,Map<String, String> locationValue);
	 /**
	 * Run step.
	 *
	 * @param id the simulation id
	 * @return the run output
	 */
 	RunOutput runStep(int id);
	 /**
	 * Run step with timeout.
	 *
	 * @param ids the simulation id
	 * @param locationValue the location value
	 * @param modelPath the model path
	 * @param timeout the timeout in milliseconds before interrupting
	 * @return the run output
	 */
 	RunOutput runStepTimeout(int ids,Map<String, String> locationValue,int timeout);
	 /**
	 * Run step with timeout.
	 *
	 * @param id the simulation id
	 * @param timeout the timeout in milliseconds before interrupting
	 * @return the run output
	 */
 	RunOutput runStepTimeout(int id, int timeout);
	 
	 /**
 	 * Stop execution.
 	 *
 	 * @param id the id
 	 * @return the int
 	 */
 	int stopExecution(int id);
	 
	 //String getInfo();
	 
	 /**
 	 * Inits the max sim instance.
 	 *
 	 * @param maxSimInstance the max sim instance
 	 * @return the int
 	 */
 	int init(int maxSimInstance);
	
	 /**
	 * Run until empty.
	 *
	 * @param id the simulation id
	 * @param locationValue the location value
	 * @param modelPath the model path
	 * @param max the maximum amount of steps before interrupting
	 * @return the run output
	 */
 	RunOutput runUntilEmpty(int id, Map<String, String> locationValue, int max);
	 /**
	 * Run until empty.
	 *
	 * @param id the simulation id
	 * @param max the maximum amount of steps before interrupting
	 * @return the run output
	 */
 	RunOutput runUntilEmpty(int id, int max);
	 /**
	 * Run until empty.
	 *
	 * @param id the simulation id
	 * @param modelPath the model path
	 * @return the run output
	 */
 	RunOutput runUntilEmpty(int id);
	 /**
	 * Run until empty.
	 *
	 * @param id the simulation id
	 * @param locationValue the location value
	 * @param modelPath the model path
	 * @return the run output
	 */
 	RunOutput runUntilEmpty(int id,  Map<String, String> locationValue);
	 /**
	 * Run until empty with timeout.
	 *
	 * @param id the simulation id
	 * @param max the maximum amount of steps before interrupting
	 * @param timeout the timeout in milliseconds before interrupting
	 * @return the run output
	 */
 	RunOutput runUntilEmptyTimeout(int id, int max, int timeout);
	 /**
	 * Run until empty with timeout.
	 *
	 * @param id the simulation id
	 * @param locationValue the location value
	 * @param modelPath the model path
	 * @param max the maximum amount of steps before interrupting
	 * @param timeout the timeout in milliseconds before interrupting
	 * @return the run output
	 */
 	RunOutput runUntilEmptyTimeout(int id, Map<String, String> locationValue, int max, int timeout);
	 /**
	 * Run until empty with timeout.
	 *
	 * @param id the simulation id
	 * @param modelPath the model path
	 * @param timeout the timeout in milliseconds before interrupting
	 * @return the run output
	 */
 	RunOutput runUntilEmptyTimeout(int id, int timeout);
	 /**
	 * Run until empty with timeout.
	 *
	 * @param id the simulation id
	 * @param locationValue the location value
	 * @param modelPath the model path
	 * @param timeout the timeout in milliseconds before interrupting
	 * @return the run output
	 */
 	RunOutput runUntilEmptyTimeout(int id, Map<String, String> locationValue, int timeout);
 	
	  
}
