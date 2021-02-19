package net.kaurpalang.mirth.annotationsplugin.processor;

import com.google.auto.service.AutoService;
import net.kaurpalang.mirth.annotationsplugin.annotation.ApiProvider;
import net.kaurpalang.mirth.annotationsplugin.annotation.ClientClass;
import net.kaurpalang.mirth.annotationsplugin.annotation.Library;
import net.kaurpalang.mirth.annotationsplugin.annotation.ServerClass;
import org.apache.maven.plugin.MojoExecutionException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

@AutoService(Processor.class)
public class MirthPluginProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    private Set<String> serverClasses = new HashSet<>();
    private Set<String> clientClasses = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processClasses(roundEnv, ServerClass.class, serverClasses);
        processClasses(roundEnv, ClientClass.class, clientClasses);
        
        System.out.println(serverClasses);
        System.out.println(clientClasses);

        if (roundEnv.processingOver()) {
            makeFile();
        }
        return false;
    }

    private void processClasses(RoundEnvironment roundEnv, Class<? extends Annotation> annotation, Set<String> targetList) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s", annotation.getSimpleName());
            } else {
                targetList.add(annotatedElement.asType().toString());
            }
        }
    }

    private void makeFile() {
       File touch = new File("touch.txt");

        FileWriter w = null;
        try {
            w = new FileWriter(touch);

            w.write(serverClasses.toString());
            w.write(clientClasses.toString());
        } catch (IOException e) {
            System.out.println("ei ssaaaa");
            e.printStackTrace();
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(ServerClass.class.getCanonicalName());
        annotations.add(ClientClass.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
