package org.asmeta.simulator_at_runtime.runtime_commander;

/**
 * @author Federico Rebucini
 */
public class CommanderException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5535301548463359822L;
	private CommanderStatus CS;
	public CommanderException(String message, CommanderStatus CS) {
		super(message);
		this.CS=CS;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage()+", actual type: "+CS;
		
	}
}
