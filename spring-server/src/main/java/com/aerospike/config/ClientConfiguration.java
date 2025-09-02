package com.aerospike.config;

/**
 * Configuration class for Aerospike client connection details
 * Contains only the essential connection parameters
 */
public class ClientConfiguration {
    private final String hostname;
    private final int port;

    public ClientConfiguration(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ClientConfiguration{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                '}';
    }
}
