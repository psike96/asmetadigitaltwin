package org.asmeta.asmetadt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsmetaDigitalTwin extends DigitalTwin {
		
	// le controllate mi aiutano a tenere traccia dei vecchi valori
	// e quindi quando conviene inviare l'update oppure no
	private List<String> controlledFunctions;
	
	// le monitorate mi aiutano ad ottenere la sottomappa associata
	// con la chiamata del metodo getMonitored()
	private List<String> monitoredFunctions;
	
	public AsmetaDigitalTwin(String twinId, String endpoint) {
		super (twinId, endpoint);
		controlledFunctions = new ArrayList<String>();
		monitoredFunctions = new ArrayList<String>();
	}
	
	
	public Object getMonitored (String monitored) {
		Object value = getProperty(monitored);
		
		if (!monitoredFunctions.contains(monitored))
			monitoredFunctions.add(monitored);
		
		return value;
	}
	
	public Map<String, Object> getMonitored () {
		// preparo la mappa delle funzioni monitorate con i valori associati
		Map<String, Object> monitored = new HashMap<>();
		
		// di tutte le proprieta'
		getProperties().entrySet().stream()
			// considero solo quelle contenute in monitoredFunctions
			.filter(prop -> monitoredFunctions.contains(prop.getKey()))
			// quindi le aggiungo nella mappa che intendo restiutire
			.forEach(m -> monitored.put(m.getKey(), m.getValue()));

		return monitored;
	}
	
	public void updateControlled (String controlled, Object value) {
		// ottengo il vecchio valore contenuto in properties della controllata
		Object oldValue = getSavedProperty(controlled);
		
		// se quella proprieta' ancora non esiste
		// o se il valore e' cambiato rispetto a quello precedente, lo aggiorno
		if (oldValue == null || !oldValue.equals(value))
			setProperty(controlled, value);
		
		// se il nome della funzione controllata non era nella lista controlledFunctions, la inserisco
		if (!controlledFunctions.contains(controlled))
			controlledFunctions.add(controlled);
	}
	
	public void updateControlled (Map<String, Object> controlled) {
		// preparo la mappa che conterra' le funzioni controllate da aggiornare
		// ovvero quelle che hanno un valore diverso rispetto a quello precedente
		Map<String, Object> changedControlledFunctions = new HashMap<>();
		
		// tutte le proprieta' con il loro precedente valore
		Map<String, Object> savedProperties = getSavedProperties();
		
		// di tutte le funzioni controllate richieste da aggiornare
		controlled.entrySet().stream()
			.filter(newControlled -> 
				// considero tutte le funzioni controllate non ancora presenti in properties
				! savedProperties.containsKey(newControlled.getKey()) ||
				// altrimenti, di tutte le funzioni controllate rimaste (presenti in properties),
				// considero solo quelle con il valore cambiato
				! savedProperties.get(newControlled.getKey()).equals(newControlled.getValue())
			// quindi le aggiungo nella mappa changedControlledFunctions
			).forEach(c -> changedControlledFunctions.put(c.getKey(), c.getValue()));
		
		// se e' rimasta almeno una proprieta da aggiornare, chiedo l'aggiornamento
		if (!changedControlledFunctions.isEmpty())
			setProperties(changedControlledFunctions);
		
		// i nomi delle funzioni controllate che non erano presenti nella lista controlledFunctions, li inserisco
		controlled.keySet().stream()
			.filter(controlledName -> !controlledFunctions.contains(controlledName))
			.forEach(controlledName -> controlledFunctions.add(controlledName));
	}

}
