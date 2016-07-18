package org.black.kotlin.projectsextensions.maven.buildextender;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class PomXmlEditor {

    private final NbMavenProjectImpl project;
    private final String kotlinVersion = "1.0.3";
    private final String groupIdName = "org.jetbrains.kotlin";
    
    public PomXmlEditor(NbMavenProjectImpl project) {
        this.project = project;
        try {
            checkKotlinStdLibDependency();
        } catch (DocumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Element createDependenciesElement(Element root) {
        DefaultElement dependenciesElement = new DefaultElement("dependencies");
        root.content().add(dependenciesElement);
        
        return root.element("dependencies");
    }
    
    private void createStdlibDependency(Element dependencies) {
        DefaultElement dependency = new DefaultElement("dependency");
        
        DefaultElement groupId = new DefaultElement("groupId");
        groupId.addText(groupIdName);
        
        DefaultElement artifactId = new DefaultElement("artifactId");
        artifactId.addText("kotlin-stdlib");
        
        DefaultElement version = new DefaultElement("version");
        version.addText(kotlinVersion);
        
        dependency.add(groupId);
        dependency.add(artifactId);
        dependency.add(version);
        
        dependencies.content().add(dependency);
    }
    
    private void checkKotlinStdLibDependency() throws DocumentException {
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
            if (dep.element("groupId").getText().equals("org.jetbrains.kotlin") && 
                    dep.element("artifactId").getText().equals("kotlin-stdlib")) {
                stdlibDependency = dep;
                break;
            }
        }
        
        if (stdlibDependency == null) {
            createStdlibDependency(dependencies);
        }
    }
    

    
}
