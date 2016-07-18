package org.black.kotlin.projectsextensions.maven.buildextender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class PomXmlModifier {

    private final NbMavenProjectImpl project;
    private final String kotlinVersion = "1.0.3";
    private final String groupIdName = "org.jetbrains.kotlin";
    
    public PomXmlModifier(NbMavenProjectImpl project) {
        this.project = project;
        try {
            checkKotlinStdLibDependency();
            checkKotlinPlugin();
        } catch (DocumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Element createDependenciesElement(Element root) {
        QName qname = new QName("dependencies", root.getQName().getNamespace());
        DefaultElement dependenciesElement = new DefaultElement(qname);
        
        root.content().add(dependenciesElement);
        
        return root.element("dependencies");
    }
    
    private Element createBuildElement(Element root) {
        QName qname = new QName("build", root.getQName().getNamespace());
        DefaultElement buildElement = new DefaultElement(qname);
        
        root.content().add(buildElement);
        
        return root.element("build");
    }
    
    private Element createPluginsElement(Element root) {
        QName qname = new QName("plugins", root.getQName().getNamespace());
        DefaultElement pluginsElement = new DefaultElement(qname);
        
        root.content().add(pluginsElement);
        
        return root.element("plugins");
    }
    
    private void createStdlibDependency(Element dependencies) {
        QName dependencyQname = new QName("dependency", dependencies.getQName().getNamespace());
        DefaultElement dependency = new DefaultElement(dependencyQname);
        
        QName groupIdQname = new QName("groupId", dependencies.getQName().getNamespace());
        DefaultElement groupId = new DefaultElement(groupIdQname);
        groupId.addText(groupIdName);
        
        QName artifactIdQname = new QName("artifactId", dependencies.getQName().getNamespace());
        DefaultElement artifactId = new DefaultElement(artifactIdQname);
        artifactId.addText("kotlin-stdlib");
        
        QName versionQname = new QName("version", dependencies.getQName().getNamespace());
        DefaultElement version = new DefaultElement(versionQname);
        version.addText(kotlinVersion);
        
        dependency.add(groupId);
        dependency.add(artifactId);
        dependency.add(version);
        
        dependencies.content().add(dependency);
    }
    
    private void addExecution(Element executions, String id, String phase, String goal) {
        QName executionQname1 = new QName("execution", executions.getQName().getNamespace());
        DefaultElement execution = new DefaultElement(executionQname1);
        
        QName idQname1 = new QName("id", executions.getQName().getNamespace());
        DefaultElement id1 = new DefaultElement(idQname1);
        id1.addText(id);
        QName phaseQname1 = new QName("phase", executions.getQName().getNamespace());
        DefaultElement phase1 = new DefaultElement(phaseQname1);
        phase1.addText(phase);
        QName goalsQname1 = new QName("goals", executions.getQName().getNamespace());
        DefaultElement goals1 = new DefaultElement(goalsQname1);
        QName goalQname1 = new QName("goal", executions.getQName().getNamespace());
        DefaultElement goal1 = new DefaultElement(goalQname1);
        goal1.addText(goal);
        
        goals1.add(goal1);
        
        execution.add(id1);
        execution.add(phase1);
        execution.add(goals1);
        
        executions.add(execution);
    }
    
    private void addExecutions(Element plugin) {
        QName executionsQname = new QName("executions", plugin.getQName().getNamespace());
        DefaultElement executions = new DefaultElement(executionsQname);
        
        addExecution(executions, "compile", "process-sources", "compile");
        addExecution(executions, "test-compile", "process-test-sources", "test-compile");
        
        plugin.add(executions);
    }
    
    private void createPluginElement(Element plugins) {
        QName pluginQname = new QName("plugin", plugins.getQName().getNamespace());
        DefaultElement plugin = new DefaultElement(pluginQname);
        
        QName groupIdQname = new QName("groupId", plugins.getQName().getNamespace());
        DefaultElement groupId = new DefaultElement(groupIdQname);
        groupId.addText(groupIdName);
        
        QName artifactIdQname = new QName("artifactId", plugins.getQName().getNamespace());
        DefaultElement artifactId = new DefaultElement(artifactIdQname);
        artifactId.addText("kotlin-maven-plugin");
        
        QName versionQname = new QName("version", plugins.getQName().getNamespace());
        DefaultElement version = new DefaultElement(versionQname);
        version.addText(kotlinVersion);
        
        plugin.add(groupId);
        plugin.add(artifactId);
        plugin.add(version);
        
        addExecutions(plugin);
        
        plugins.add(plugin);
    }
    
    private void checkKotlinStdLibDependency() throws DocumentException, IOException {
        File pom = project.getPOMFile();
        SAXReader reader = new SAXReader();
        Document pomDocument = reader.read(pom);
        
        Element root = pomDocument.getRootElement();
        Element dependencies = root.element("dependencies");
        if (dependencies == null) {
            dependencies = createDependenciesElement(root);
        }
        
        Element stdlibDependency = null;
        for (Object el : dependencies.elements("dependency")) {
            Element dep = (Element) el;
            if (dep.element("groupId").getText().equals(groupIdName) && 
                    dep.element("artifactId").getText().equals("kotlin-stdlib")) {
                stdlibDependency = dep;
                break;
            }
        }
        
        if (stdlibDependency == null) {
            createStdlibDependency(dependencies);
        }
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(pom);
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(pomDocument);
        out.close();
    }
    

    private void checkKotlinPlugin() throws DocumentException, IOException {
        File pom = project.getPOMFile();
        SAXReader reader = new SAXReader();
        Document pomDocument = reader.read(pom);
        
        Element root = pomDocument.getRootElement();
        Element build = root.element("build");
        if (build == null) {
            build = createBuildElement(root);
        }
        
        Element plugins = build.element("plugins");
        if (plugins == null) {
            plugins = createPluginsElement(build);
        }
        
        Element plugin = null;
        for (Object el : plugins.elements("plugin")) {
            Element plug = (Element) el;
            if (plug.element("groupId").getText().equals(groupIdName) && 
                    plug.element("artifactId").getText().equals("kotlin-maven-plugin")) {
                plugin = plug;
                break;
            }
        }
        
        if (plugin == null) {
            createPluginElement(plugins);
        }
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(pom);
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(pomDocument);
        out.close();
    }
    
}
