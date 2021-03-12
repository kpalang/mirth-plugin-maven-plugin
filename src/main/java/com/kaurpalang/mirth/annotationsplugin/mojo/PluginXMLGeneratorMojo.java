package com.kaurpalang.mirth.annotationsplugin.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaurpalang.mirth.annotationsplugin.config.Constants;
import com.kaurpalang.mirth.annotationsplugin.model.ApiProviderModel;
import com.kaurpalang.mirth.annotationsplugin.model.LibraryModel;
import com.kaurpalang.mirth.annotationsplugin.model.PluginState;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "generate-plugin-xml", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class PluginXMLGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(property = "name", defaultValue = Constants.DEFAULT_PLUGIN_NAME)
    private String name;

    @Parameter(property = "author", defaultValue = Constants.DEFAULT_PLUGIN_AUTHOR)
    private String author;

    @Parameter(property = "pluginVersion", defaultValue = Constants.DEFAULT_PLUGIN_VERSION)
    private String pluginVersion;

    @Parameter(property = "mirthVersion", defaultValue = Constants.DEFAULT_MIRTH_VERSION)
    private String mirthVersion;

    @Parameter(property = "url", defaultValue = Constants.DEFAULT_PLUGIN_URL)
    private String url;

    @Parameter(property = "description", defaultValue = Constants.DEFAULT_PLUGIN_DESCRIPTION)
    private String description;

    @Parameter(property = "path", defaultValue = Constants.DEFAULT_PLUGIN_PATH)
    private String path;

    @Parameter(property = "aggregatorPath", defaultValue = Constants.DEFAULT_AGGREGATOR_FILE_PATH)
    private String aggregatorPath;

    @Parameter(property = "outputPath", defaultValue = Constants.DEFAULT_PLUGIN_XML_OUTPUT_PATH)
    private String outputPath;

    private ObjectMapper mapper = new ObjectMapper();

    public void execute() {

        try {
            File aggregatorFile = FileUtils.getFile(this.aggregatorPath);

            if (!aggregatorFile.exists()) {
                getLog().error("Aggregator file does not exist at " + aggregatorFile.getAbsolutePath());
                return;
            }

            PluginState pluginState = mapper.readValue(aggregatorFile, PluginState.class);

            // Map runtime libraries to state
            mapRuntimeLibraries(pluginState.getRuntimeClientLibs(), "client");
            mapRuntimeLibraries(pluginState.getRuntimeSharedLibs(), "shared");
            mapRuntimeLibraries(pluginState.getRuntimeServerLibs(), "server");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // root element
            Element rootElement = doc.createElement("pluginMetaData");
            rootElement.setAttribute("path", path);
            doc.appendChild(rootElement);

            // Append detail elements
            appendSimpleChildToElement(doc, rootElement, "name", this.name);
            appendSimpleChildToElement(doc, rootElement, "author", this.author);
            appendSimpleChildToElement(doc, rootElement, "pluginVersion", this.pluginVersion);
            appendSimpleChildToElement(doc, rootElement, "mirthVersion", this.mirthVersion);
            appendSimpleChildToElement(doc, rootElement, "url", this.url);
            appendSimpleChildToElement(doc, rootElement, "description", this.description);

            // Server classes
            if (pluginState.getServerClasses().size() > 0) {
                Element serverClasses = getClassesElement(doc, "serverClasses", pluginState.getServerClasses());
                rootElement.appendChild(serverClasses);
            }

            // Client classes
            if (pluginState.getClientClasses().size() > 0) {
                Element clientClasses = getClassesElement(doc, "clientClasses", pluginState.getClientClasses());
                rootElement.appendChild(clientClasses);
            }

            // Libraries
            MavenProject parentProject = project.getParent();
            List<String> modules = parentProject.getModules().stream()
                    .filter(s -> !s.equals(project.getArtifactId()))
                    .collect(Collectors.toList());

            Path parentBuildDir = parentProject.getBasedir().toPath();

            for (String module : modules) {
                Path submoduleBuildDir = Paths.get(parentBuildDir.toString(), module, "target");

                if (Files.exists(submoduleBuildDir)) {

                    File[] jarfiles = submoduleBuildDir.toFile().listFiles(
                            (dir, name) -> name.equals(String.format("%s-%s.jar", parentProject.getArtifactId(), module))
                    );

                    for (File jarfile : jarfiles) {
                        LibraryModel model = new LibraryModel(module.toUpperCase(), jarfile.getName());
                        appendLibraryChildToElement(doc, rootElement, model);
                    }
                }
            }

            // Runtime libraries
            // Client
            for (LibraryModel model : pluginState.getRuntimeClientLibs()) {
                appendLibraryChildToElement(doc, rootElement, model);
            }
            // Shared
            for (LibraryModel model : pluginState.getRuntimeSharedLibs()) {
                appendLibraryChildToElement(doc, rootElement, model);
            }
            // Server
            for (LibraryModel model : pluginState.getRuntimeServerLibs()) {
                appendLibraryChildToElement(doc, rootElement, model);
            }


            // Api providers
            if (pluginState.getApiProviders().size() > 0) {
                Element apiProviderElement;
                for (ApiProviderModel apiProvider : pluginState.getApiProviders()) {
                    apiProviderElement = doc.createElement("apiProvider");
                    apiProviderElement.setAttribute("type", apiProvider.getType().toString());
                    apiProviderElement.setAttribute("name", apiProvider.getName());
                    rootElement.appendChild(apiProviderElement);
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputPath));
            transformer.transform(source, result);

            // Aggregation file
            if (Files.deleteIfExists(aggregatorFile.toPath())) {
                getLog().info("Aggregation file deleted successfully");
            } else {
                getLog().warn("Aggregation file not deleted. You should delete it manually to avoid conflicts!");
                getLog().warn(aggregatorFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mapRuntimeLibraries(List<LibraryModel> targetList, String submodule) {
        Path submoduleLibsDir = Paths.get(project.getParent().getBasedir().getAbsolutePath(), "libs", "runtime", submodule);

        if (Files.exists(submoduleLibsDir)) {
            File[] jarfiles = submoduleLibsDir.toFile().listFiles();

            for (File jarfile : jarfiles) {
                LibraryModel libModel = new LibraryModel(submodule.toUpperCase(), String.format("libs/%s", jarfile.getName()));
                targetList.add(libModel);
            }
        }
    }

    private Element getClassesElement(Document doc, String rootTagName, Set<String> classes) {
        Element classesElement = doc.createElement(rootTagName);
        for (String serverClass : classes) {
            appendSimpleChildToElement(doc, classesElement, "string", serverClass);
        }

        return classesElement;
    }

    private void appendLibraryChildToElement(Document doc, Element rootElement, LibraryModel model) {
        Element appendableElement = doc.createElement("library");
        appendableElement.setAttribute("type", model.getType());
        appendableElement.setAttribute("path", model.getPath());
        rootElement.appendChild(appendableElement);
    }

    private void appendSimpleChildToElement(Document doc, Element rootElement, String tagname, String textcontent) {
        Element appendableElement = doc.createElement(tagname);
        appendableElement.setTextContent(textcontent);
        rootElement.appendChild(appendableElement);
    }
}
