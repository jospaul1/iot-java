/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.gateway;

import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.sample.client.SystemObject;
import com.ibm.iotf.sample.util.Utility;

/**
 * <p>The Gateway sample uses the com.ibm.iotf.client.gateway.GatewayClient class from the IoTF Java Client Library 
 * that simplifies the Gateway interactions with IBM Watson IoT Platform. </p>
 * 
 */
public class SimpleGatewayExample {
	
	private final static String PROPERTIES_FILE_NAME = "device.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";
	
	private final static String DEVICE_TYPE = "iotsample-deviceType";
	private final static String SIMULATOR_DEVICE_ID = "SimulatorDevice01";
	
	private GatewayClient gwClient = null;
	SystemObject obj = new SystemObject();
	private String gwDeviceId;
	private String gwDeviceType;
	
	
	public SimpleGatewayExample() {
		
	}
	
	/**
	 * This method creates a GatewayClient instance by passing the required properties 
	 * and connects the Gateway to the Watson IoT Platform by calling the connect function.
	 * 
	 * After the successful connection to the Watson IoT Platform, the Gateway can perform the following operations,
	 *   1. Publish events for itself and on behalf of devices connected behind the Gateway
	 *   2. Subscribe to commands for itself and on behalf of devices behind the Gateway
	 */
	private void createGatewayClient(String fileName) {
		/**
		 * Load properties file "device.prop"
		 */
		Properties props = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, fileName);
		
		try {
			//Instantiate & connect the Gateway by passing the properties file
			gwClient = new GatewayClient(props);
			this.gwDeviceId = props.getProperty("Device-ID");
			this.gwDeviceType = props.getProperty("Device-Type");
			gwClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * While the Gateway publishes events on behalf of the devices connected behind, the Gateway 
	 * can publish its own events as well. 
	 * 
	 * The sample publishes a blink event every second, that has the CPU and memory utilization of 
	 * this sample Gateway process.
	 */
	private void publishGatewayEvent() {
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", SystemObject.getName());
		try {
			event.addProperty("cpu",  obj.getProcessCpuLoad());
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
		event.addProperty("mem",  obj.getMemoryUsed());
			
		gwClient.publishGatewayEvent("blink", event, 2);
	}
	
	/**
	 * The method publishes a blink event every second, that has the CPU and memory utilization of 
	 * this sample Gateway process.
	 */
	private void publishDeviceEvent() {
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", SystemObject.getName() + "simulator");
		try {
			event.addProperty("cpu",  obj.getProcessCpuLoad());
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
		event.addProperty("mem",  obj.getMemoryUsed());
			
		gwClient.publishDeviceEvent(DEVICE_TYPE, SIMULATOR_DEVICE_ID, "blink", event, 2);
	}
	
	private void disconnect() {
		//Disconnect cleanly
		gwClient.disconnect();
	}
	

	public static void main(String[] args) throws IoTFCReSTException {
		
		SimpleGatewayExample sample = new SimpleGatewayExample();
		
		String fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		sample.createGatewayClient(fileName);

		System.out.println("Gateway Started");
		
		/**
		 * Try to publish a Gateway Event for every second. As like devices, the Gateway
		 * also can have attached sensors and publish events.
		 */
		while(true) {
			sample.publishGatewayEvent();
			sample.publishDeviceEvent();
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {}
		}
		
		//sample.disconnect();
		
	}
}