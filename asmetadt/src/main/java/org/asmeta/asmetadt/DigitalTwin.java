package org.asmeta.asmetadt;

import java.util.Map;

import com.azure.core.models.JsonPatchDocument;
import com.azure.digitaltwins.core.BasicDigitalTwin;
import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DigitalTwin {
	
	private static DigitalTwinsClient client;
	private static boolean connected = false;
	
	private String modelId;
	private String twinId;
	
	private Map<String, Object> properties;
	
	public DigitalTwin (String twinId, String endpoint, String tenantId, String clientId, String clientSecret) {
		if (!isConnected()) {
			try {
				client = new DigitalTwinsClientBuilder()
						.credential(
								new ClientSecretCredentialBuilder()
									.tenantId(tenantId)
									.clientId(clientId)
									.clientSecret(clientSecret)
									.build()
								)
						.endpoint(endpoint)
						.buildClient();
				
				connected = true;
				
				initializer (twinId);
			} catch (ErrorResponseException e) {
				return;
			}
		}
	}
	
	public DigitalTwin (String twinId, String endpoint) {
		if (!isConnected()) {
			try {
				client = new DigitalTwinsClientBuilder()
						.credential(
								new DefaultAzureCredentialBuilder().build()
								)
						.endpoint(endpoint)
						.buildClient();
				
				connected = true;
				
				initializer (twinId);
			} catch (ErrorResponseException e) {
				return;
			}
		}
	}

	public DigitalTwin (String twinId) {
		if (!isConnected())
			return; // client non ancora settato
		
		try {
			initializer (twinId);
		} catch (ErrorResponseException e) {
			return;
		}
	}
	
	
	private void initializer (String twinId) throws ErrorResponseException {
		BasicDigitalTwin basic = client.getDigitalTwin(twinId, BasicDigitalTwin.class);
		this.modelId = basic.getMetadata().getModelId();
		this.twinId = basic.getId();
		this.properties = basic.getContents();
	}
	
	private void refreshProperties () {
		BasicDigitalTwin basic = client.getDigitalTwin(twinId, BasicDigitalTwin.class);
		properties.putAll(basic.getContents());
	}
	
	public Object getProperty (String property) {
		BasicDigitalTwin basic = client.getDigitalTwin(twinId, BasicDigitalTwin.class);
		Object propertyValue = basic.getContents().get(property);
		properties.put(property, propertyValue);
		return propertyValue;
	}
	
	public Map<String, Object> getProperties () {
		refreshProperties();
		return properties;
	}
	
	public void setProperty (String property, Object value) {
		try {
			JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
			jsonPatchDocument.appendReplace("/" + property, value);
			client.updateDigitalTwin(twinId, jsonPatchDocument);
		} catch (ErrorResponseException e) {
			JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
			jsonPatchDocument.appendAdd("/" + property, value);
			client.updateDigitalTwin(twinId, jsonPatchDocument);
		}
		properties.put(property, value);
	}
	
	public void setProperties (Map<String, Object> newProperties) {
		
		try {
			JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
			
			newProperties.entrySet().stream().forEach(entry -> {
				if (properties.containsKey(entry.getKey())) {
					jsonPatchDocument.appendReplace("/" + entry.getKey(), entry.getValue());
				} else {
					jsonPatchDocument.appendAdd("/" + entry.getKey(), entry.getValue());
				}
			});
			
			client.updateDigitalTwin(twinId, jsonPatchDocument);
			properties.putAll(newProperties);
			
		} catch (ErrorResponseException e) {
			refreshProperties();
			setProperties(newProperties);
		}
		
	}
	
	
	
	public Map<String, Object> getSavedProperties () {
		return properties;
	}
	
	public Object getSavedProperty (String property) {
		return properties.get(property);
	}
	
	public String getTwinId () {
		return twinId;
	}
	
	public String getModelId () {
		return modelId;
	}
	
	public static boolean isConnected () {
		return connected;
	}
	
	public static DigitalTwinsClient getClient () {
		return client;
	}
	
}
