package net.kaurpalang.mirth.annotationsplugin;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

@Mojo(name = "annotations", defaultPhase = LifecyclePhase.PACKAGE)
public class MirthPluginMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(property = "name")
    private String name;

    @Parameter(property = "author")
    private String author;

    @Parameter(property = "path")
    private String path;

    @Parameter(property = "pluginVersion")
    private String pluginVersion;

    @Parameter(property = "mirthVersion")
    private String mirthVersion;

    private File outputDirectory;

    public void execute() throws MojoExecutionException {
        List<String> compile = null;
        try {
            compile = project.getCompileClasspathElements();
            getLog().warn(compile.toString());
        } catch (DependencyResolutionRequiredException e) {
            e.printStackTrace();
        }


        /*
        File f = outputDirectory;

        if (!f.exists()) {
            f.mkdirs();
        }

        File touch = new File(f, "touch.txt");

        FileWriter w = null;
        try {
            w = new FileWriter(touch);

            w.write("touch.txt");
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + touch, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

         */
    }
}
