package com.github.pisatoshi.logback;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class InternalMqttClient extends MqttClient implements MqttCallback {
    private MqttClientPersistence clientPersistence;

    public InternalMqttClient(String serverURI, String clientId) throws MqttException {
        this(serverURI,clientId, new MemoryPersistence());
    }

    public InternalMqttClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        super(serverURI, clientId, persistence);
        clientPersistence = persistence;
    }

    @Override
    public void disconnect() throws MqttPersistenceException {
        clientPersistence.close();
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO reconnect
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
