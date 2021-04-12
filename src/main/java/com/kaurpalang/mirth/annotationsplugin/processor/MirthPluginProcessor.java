package com.kaurpalang.mirth.annotationsplugin.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.kaurpalang.mirth.annotationsplugin.config.Constants;
import com.kaurpalang.mirth.annotationsplugin.annotation.ApiProvider;
import com.kaurpalang.mirth.annotationsplugin.annotation.ClientClass;
import com.kaurpalang.mirth.annotationsplugin.annotation.ServerClass;
import com.kaurpalang.mirth.annotationsplugin.model.ApiProviderModel;
import com.kaurpalang.mirth.annotationsplugin.model.PluginState;
import org.apache.commons.io.FileUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.util.*;

@AutoService(Processor.class)
@SupportedOptions("aggregator.file")
public class MirthPluginProcessor extends AbstractProcessor {

    private Messager messager;

    private final Set<String> serverClasses = new HashSet<>();
    private final Set<String> clientClasses = new HashSet<>();
    private final Set<ApiProviderModel> apiProviders = new HashSet<>();

    private String aggregatorFileString;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.messager = processingEnv.getMessager();

        if (this.aggregatorFileString == null) {
            this.aggregatorFileString = processingEnv.getOptions().getOrDefault("aggregator.file", Constants.DEFAULT_AGGREGATOR_FILE_PATH);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processClasses(roundEnv, ServerClass.class, serverClasses);
        processClasses(roundEnv, ClientClass.class, clientClasses);
        processApiProviders(roundEnv);

        if (roundEnv.processingOver()) {
            handleProcessingEnd();
        }
        return false;
    }

    /**
     * Method iterates over classes with param annotation, and adds them to specified target set
     *
     * @param roundEnv Round environment
     * @param annotation Annotation to process
     * @param targetSet Set to add found classes to
     */
    private void processClasses(RoundEnvironment roundEnv, Class<? extends Annotation> annotation, Set<String> targetSet) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s", annotation.getSimpleName());
            } else {
                targetSet.add(annotatedElement.asType().toString());
            }
        }
    }

    /**
     * Method iterates over apiProvider classes, and adds them to set
     *
     * @param roundEnv Round environment
     */
    private void processApiProviders(RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(ApiProvider.class)) {

            List<ElementKind> allowedKinds = Arrays.asList(ElementKind.CLASS, ElementKind.INTERFACE);
            if (!allowedKinds.contains(annotatedElement.getKind())) {
                error(annotatedElement, "Only classes can be annotated with @%s", ApiProvider.class.getSimpleName());
            } else {
                ApiProvider provider = annotatedElement.getAnnotation(ApiProvider.class);
                apiProviders.add(new ApiProviderModel(provider.type(), annotatedElement.asType().toString()));
            }
        }
    }

    /**
     * This method runs after current processing round has completed.
     * Method writes all found server-, client- and apiprovider classes to aggregation json file to
     * be stored for plugin.xml generation.
     */
    private void handleProcessingEnd() {
        try {
            File aggregatorFile = new File(this.aggregatorFileString);
            aggregatorFile.getParentFile().mkdirs();

            // If aggregation file exists, just read it in, if not, create one
            PluginState pluginState;
            if (aggregatorFile.createNewFile()) {
                pluginState = new PluginState();
            } else {
                byte[] aggregatorFileArray = FileUtils.readFileToByteArray(aggregatorFile);
                pluginState = mapper.readValue(aggregatorFileArray, PluginState.class);
            }

            // Add classes found during this round to all found files
            pluginState.getServerClasses().addAll(serverClasses);
            pluginState.getClientClasses().addAll(clientClasses);
            pluginState.getApiProviders().addAll(apiProviders);

            // Write to file, with prettyness of course
            String serverJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pluginState);
            Files.write(aggregatorFile.toPath(), serverJson.getBytes());
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(ServerClass.class.getCanonicalName());
        annotations.add(ClientClass.class.getCanonicalName());
        annotations.add(ApiProvider.class.getCanonicalName());
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
