package org.black.kotlin.j2seprojectextension.buildextender;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.black.kotlin.utils.ProjectUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinBuildExtender {
    
    public KotlinBuildExtender(Project project) {
        if (!(project instanceof J2SEProject)) {
            return;
        }
        J2SEProject j2seProject = (J2SEProject) project;
        
        addKotlinTasksToScript(project);
    }

    public void addKotlinTasksToScript(Project project) {
        FileObject buildImpl = getBuildImplXml(project);
        
        try {
            addKotlinAnt(buildImpl);
            addKotlinLibProperty(buildImpl);
        } catch (DocumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public FileObject getBuildImplXml(Project project) {
        FileObject projDir=project.getProjectDirectory();
        FileObject buildImpl = projDir.getFileObject("nbproject").getFileObject("build-impl.xml");
        
        return buildImpl;
    }
    
    public void addKotlinLibProperty(FileObject buildImpl) throws DocumentException, UnsupportedEncodingException, IOException{
        SAXReader reader = new SAXReader();
        Document document = reader.read(buildImpl.toURL());
        
        List<Element> elements = document.getRootElement().elements("property");
        boolean hasKotlinLibProperty = false;
        
        for (Element el : elements) {
            if (el.attribute("name").getValue().equals("kotlin.lib")) {
                hasKotlinLibProperty = true;
            }
        }
        
        if (hasKotlinLibProperty) {
            return;
        }
        
        DefaultElement prop = new DefaultElement("property");
        prop.addAttribute("name", "kotlin.lib");
        prop.addAttribute("value", ProjectUtils.KT_HOME + "lib");
        
        List content = document.getRootElement().content();
        if (content != null ) {
            content.add(0, prop);
        }
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(buildImpl.getPath());
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(document);
        out.close();
    }
    
    public void addKotlinAnt(FileObject buildImpl) throws DocumentException, UnsupportedEncodingException, IOException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(buildImpl.toURL());
        
        List<Element> elements = document.getRootElement().elements("typedef");
        boolean hasKotlinLibProperty = false;
        
        for (Element el : elements) {
            if (el.attribute("classpath").getValue().equals("${kotlin.lib}/kotlin-ant.jar")) {
                hasKotlinLibProperty = true;
            }
        }
        
        if (hasKotlinLibProperty) {
            return;
        }
        
        DefaultElement typedef = new DefaultElement("typedef");
        typedef.addAttribute("classpath", "${kotlin.lib}/kotlin-ant.jar");
        typedef.addAttribute("resource", "org/jetbrains/kotlin/ant/antlib.xml");
        
        List content = document.getRootElement().content();
        if (content != null ) {
            content.add(0, typedef);
        }
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(buildImpl.getPath());
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(document);
        out.close();
    }
    
}
