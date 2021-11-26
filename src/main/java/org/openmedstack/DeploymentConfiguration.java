package org.openmedstack;

import java.net.URI;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class DeploymentConfiguration {
    private String name;
    private String environment;
    private HashMap<Pattern, URI> services = new HashMap<>();
    private URI serviceBus;
    private String serviceBusUsername;
    private String serviceBusPassword;
    private String queueName;
    private String connectionString;
    private Period timeout;
    private String tokenService;
    private List<String> validIssuers = new ArrayList<>();
    private String clientId;
    private String secret;
    private String scope = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public HashMap<Pattern, URI> getServices() {
        return services;
    }

    public void setServices(HashMap<Pattern, URI> services) {
        this.services = services;
    }

    public URI getServiceBus() {
        return serviceBus;
    }

    public void setServiceBus(URI serviceBus) {
        this.serviceBus = serviceBus;
    }

    public String getServiceBusUsername() {
        return serviceBusUsername;
    }

    public void setServiceBusUsername(String serviceBusUsername) {
        this.serviceBusUsername = serviceBusUsername;
    }

    public String getServiceBusPassword() {
        return serviceBusPassword;
    }

    public void setServiceBusPassword(String serviceBusPassword) {
        this.serviceBusPassword = serviceBusPassword;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public Period getTimeout() {
        return timeout;
    }

    public void setTimeout(Period timeout) {
        this.timeout = timeout;
    }

    public String getTokenService() {
        return tokenService;
    }

    public void setTokenService(String tokenService) {
        this.tokenService = tokenService;
    }

    public List<String> getValidIssuers() {
        return validIssuers;
    }

    public void setValidIssuers(List<String> validIssuers) {
        this.validIssuers = validIssuers;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}

