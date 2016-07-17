package org.black.kotlin.projectsextensions.maven.buildextender;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;

/**
 *
 * @author Александр
 */
public class PomXmlEditor {

    private final NbMavenProjectImpl project;
    private final String compilerVersion = "1.0.3";
    
    public PomXmlEditor(NbMavenProjectImpl project) {
        this.project = project;
        
//        addKotlinStdlibDependency();
    }

    
    
    private void addKotlinPlugin() {
        Plugin plugin = new Plugin();
        plugin.setArtifactId("kotlin-maven-plugin");
        plugin.setGroupId("org.jetbrains.kotlin");
        plugin.setVersion(compilerVersion);
        
        PluginExecution compileExec = new PluginExecution();
        compileExec.setId("compile");
        compileExec.setPhase("process-sources");
        compileExec.setGoals(Lists.newArrayList("compile"));
        
        PluginExecution testCompileExec = new PluginExecution();
        testCompileExec.setId("test-compile");
        testCompileExec.setPhase("process-test-sources");
        testCompileExec.setGoals(Lists.newArrayList("test-compile"));
        
        plugin.setExecutions(Lists.newArrayList(compileExec, testCompileExec));
        
        project.getOriginalMavenProject().addPlugin(plugin);
    }
    
    private void addKotlinStdlibDependency() {
        Dependency dependency = new Dependency();
        dependency.setGroupId("org.jetbrains.kotlin");
        dependency.setArtifactId("kotlin-stdlib");
        dependency.setVersion(compilerVersion);
        
        
        
        MavenProject mavenProject = project.getOriginalMavenProject();
//        DependencyManagement depMan = mavenProject.getDependencyManagement();
//        if (depMan == null) {
        List<Dependency> deps = mavenProject.getDependencies();
        deps.add(dependency);
        mavenProject.setDependencies(deps);
//        }
//        System.out.println();
//        project.getOriginalMavenProject().getDependencyManagement().addDependency(dependency);
    }
    
}
