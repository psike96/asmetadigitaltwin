package org.asmeta.simulator_at_runtime.runtime_simulator;

/**
 * 
 * @author Simone Giusso
 *	Exception for invalid id
 */

public class IdNotFoundException extends RuntimeException{
	
	public IdNotFoundException(String message) {
		super(message);
	}
	
}
