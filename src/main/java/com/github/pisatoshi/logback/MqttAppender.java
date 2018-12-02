package com.github.pisatoshi.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;

public class MqttAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private InternalMqttClient client;

    private String host = "tcp://localhost:1883";
    private String clientId;
    private String topic = "logback";
    private String username;
    private String password;

    public MqttAppender() {
        super();
    }

    @Override
    public void start() {
        super.start();
        try {
            if (isBlank(clientId)) {
                clientId = UUID.randomUUID().toString();
            }
            client = new InternalMqttClient(host, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            if (isNotBlank(username) && isNotBlank(password)) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }
            client.setCallback(client);
            client.connect(options);
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
    protected void append(ILoggingEvent event) {
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

    private String format(ILoggingEvent event) {
        return String.format("{" +
                    "\"clientId\":\"%s\", " +
                    "\"level\":\"%s\", " +
                    "\"timestamp\": %d, " +
                    "\"logger\": \"%s\", " +
                    "\"thread\": \"%s\", " +
                    "\"message\": \"%s\"" +
                "}", clientId, event.getLevel().toString(), event.getTimeStamp(), event.getLoggerName(),
                event.getThreadName(),
                event.getFormattedMessage());
    }

    private boolean isBlank(String str) {
        return str == null || str.isEmpty() || str.trim().isEmpty();
    }

    private boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
