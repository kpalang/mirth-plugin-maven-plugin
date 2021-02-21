package net.kaurpalang.mirth.annotationsplugin.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaurpalang.mirth.annotationsplugin.Constants;
import net.kaurpalang.mirth.annotationsplugin.model.ApiProviderModel;
import net.kaurpalang.mirth.annotationsplugin.model.ServerConfig;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
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
import java.util.List;
import java.util.Set;

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

    @Parameter(property = "outputPath", defaultValue = "${project.basedir}/plugin.xml")
    private String outputPath;

    private ObjectMapper mapper = new ObjectMapper();

    public void execute() throws MojoExecutionException {

        try {
            File aggregatorFile = FileUtils.getFile(this.aggregatorPath);

            if (!aggregatorFile.exists()) {
                getLog().error("Aggregator file does not exist at " + aggregatorFile.getAbsolutePath());
                return;
            }

            ServerConfig config = mapper.readValue(aggregatorFile, ServerConfig.class);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // root element
            Element rootElement = doc.createElement("pluginMetaData");
            rootElement.setAttribute("path", path);
            doc.appendChild(rootElement);

            Element nameElement = doc.createElement("name");
            nameElement.setTextContent(this.name);
            rootElement.appendChild(nameElement);

            Element authorElement = doc.createElement("author");
            authorElement.setTextContent(this.author);
            rootElement.appendChild(authorElement);

            Element pluginVersionElement = doc.createElement("pluginVersion");
            pluginVersionElement.setTextContent(this.pluginVersion);
            rootElement.appendChild(pluginVersionElement);

            Element mirthVersionElement = doc.createElement("mirthVersion");
            mirthVersionElement.setTextContent(this.mirthVersion);
            rootElement.appendChild(mirthVersionElement);

            Element urlElement = doc.createElement("url");
            urlElement.setTextContent(this.url);
            rootElement.appendChild(urlElement);

            Element descriptionElement = doc.createElement("description");
            descriptionElement.setTextContent(this.description);
            rootElement.appendChild(descriptionElement);

            // Server classes
            if (config.getServerClasses().size() > 0) {
                Element serverClasses = getClassesElement(doc, "serverClasses", config.getServerClasses());
                rootElement.appendChild(serverClasses);
            }


            // Client classes
            if (config.getClientClasses().size() > 0) {
                Element clientClasses = getClassesElement(doc, "clientClasses", config.getClientClasses());
                rootElement.appendChild(clientClasses);
            }

            // Api providers
            if (config.getApiProviders().size() > 0) {
                Element apiProviderElement;
                for (ApiProviderModel apiProvider : config.getApiProviders()) {
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

    private Element getClassesElement(Document doc, String rootTagName, Set<String> classes) {
        Element classesElement = doc.createElement(rootTagName);
        Element classStringElement;
        for (String serverClass : classes) {
            classStringElement = doc.createElement("string");
            classStringElement.setTextContent(serverClass);
            classesElement.appendChild(classStringElement);
        }

        return classesElement;
    }
}
