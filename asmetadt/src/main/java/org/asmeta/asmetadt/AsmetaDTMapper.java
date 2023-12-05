package org.asmeta.asmetadt;

public class AsmetaDTMapper {
	
	public static Object stringToObject(String value, String domain) {
		return switch(domain) {
			case "Boolean" 	-> Boolean.valueOf(value);	// ex. value = "true"
			case "Real" 	-> Double.valueOf(value);	// ex. value = "2.23"
			case "Enum"		-> String.valueOf(value);	// ex. value = "HIGH"
			default -> throw new IllegalArgumentException("Unexpected value: " + domain);
		};
	}
	
	public static String objectToString(Object object, String domain) {
		return switch(domain) {
			case "Boolean" 	-> String.valueOf(object);	// object: Boolean
			case "Real" 	-> String.valueOf(object);	// object: Double | Integer
			case "Enum"		-> String.valueOf(object);	// object: String
			default -> throw new IllegalArgumentException("Unexpected value: " + domain);
		};
	}

}
