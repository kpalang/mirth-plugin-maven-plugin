package com.kaurpalang.mirth.annotationsplugin.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginState {
    @Getter private Set<String> serverClasses;
    @Getter private Set<String> clientClasses;
    @Getter private Set<ApiProviderModel> apiProviders;

    @Getter private List<LibraryModel> runtimeClientLibs;
    @Getter private List<LibraryModel> runtimeSharedLibs;
    @Getter private List<LibraryModel> runtimeServerLibs;

    public PluginState() {
        this.serverClasses = new HashSet<>();
        this.clientClasses = new HashSet<>();
        this.apiProviders = new HashSet<>();

        this.runtimeClientLibs = new ArrayList<>();
        this.runtimeSharedLibs = new ArrayList<>();
        this.runtimeServerLibs = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("ServerClasses %s; ClientClasses %s; ApiProviders %s",
                serverClasses.toString(), clientClasses.toString(), apiProviders.toString());
    }
}
