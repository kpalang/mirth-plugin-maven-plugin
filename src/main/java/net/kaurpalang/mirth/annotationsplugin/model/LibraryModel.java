package net.kaurpalang.mirth.annotationsplugin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class LibraryModel {
    @Getter private String type;
    @Getter private String path;
}
