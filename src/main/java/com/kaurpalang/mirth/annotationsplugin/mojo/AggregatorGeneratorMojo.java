package com.kaurpalang.mirth.annotationsplugin.mojo;

import com.kaurpalang.mirth.annotationsplugin.config.Constants;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "generate-aggregator", defaultPhase = LifecyclePhase.INITIALIZE)
public class AggregatorGeneratorMojo extends AbstractMojo {

    @Parameter(property = "aggregatorPath", defaultValue = Constants.DEFAULT_AGGREGATOR_FILE_PATH)
    private String aggregatorPath;

    @Override
    public void execute() {
        File aggregatorFile = new File(this.aggregatorPath);

        try {
            if (!aggregatorFile.exists()) {
                if (!aggregatorFile.getParentFile().mkdirs()) {
                    getLog().error("Aggregation directory creation failed!");
                }

                if (!aggregatorFile.createNewFile()) {
                    getLog().error("Aggregation file creation failed!");
                }
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
