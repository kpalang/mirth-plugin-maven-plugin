package net.kaurpalang.mirth.annotationsplugin.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class ServerConfig {
    @Getter private Set<String> serverClasses;
    @Getter private Set<String> clientClasses;
    @Getter private Set<ApiProviderModel> apiProviders;

    public ServerConfig() {
        this.serverClasses = new HashSet<>();
        this.clientClasses = new HashSet<>();
        this.apiProviders = new HashSet<>();
    }

    @Override
    public String toString() {
        return String.format("ServerClasses %s; ClientClasses %s; ApiProviders %s",
                serverClasses.toString(), clientClasses.toString(), apiProviders.toString());
    }
}
