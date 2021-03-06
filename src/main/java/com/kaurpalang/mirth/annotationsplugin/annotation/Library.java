package com.kaurpalang.mirth.annotationsplugin.annotation;

import com.kaurpalang.mirth.annotationsplugin.type.LibraryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Library {
    LibraryType type();
}
