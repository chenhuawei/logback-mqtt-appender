package com.github.pisatoshi.logback;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.eclipse.paho.client.mqttv3.*;

public class MqttAppender<E> extends UnsynchronizedAppenderBase<E> {

    private InternalMqttClient client;

    private String host = "tcp://localhost:1883";
    private String clientId;
    private String topic = "logback";
    private String username;
    private String password;

    private Layout<E> layout;

    public MqttAppender() {
        super();
    }

    @Override
    public void start() {
        super.start();
        try {
            if (isBlank(clientId)) {
                clientId = MqttClient.generateClientId();
            }
            client = new InternalMqttClient(host, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            if (isNotBlank(username) && isNotBlank(password)) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }
            options.setAutomaticReconnect(true);
            client.setCallback(client);
            IMqttToken token = client.connectWithResult(options);
            Exception e = token.getException();
            if (e != null) {
                e.printStackTrace();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.stop();
    }

    @Override
    protected void append(E event) {
        String json = format(event);
        MqttMessage message = new MqttMessage();
        message.setQos(0);
        message.setPayload(json.getBytes());
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Layout<E> getLayout() {
        return layout;
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }

    private String format(E e) {
        return layout.doLayout(e);
    }

    private boolean isBlank(String str) {
        return str == null || str.isEmpty() || str.trim().isEmpty();
    }

    private boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
