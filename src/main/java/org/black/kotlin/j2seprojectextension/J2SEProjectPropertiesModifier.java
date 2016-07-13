package org.black.kotlin.j2seprojectextension;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.codehaus.plexus.util.PropertyUtils;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class J2SEProjectPropertiesModifier {
    
    private final J2SEProject project;
    
    public J2SEProjectPropertiesModifier(J2SEProject project) {
        this.project = project;     
    }
    
    public void turnOffCompileOnSave() {
        FileObject root = project.getProjectDirectory();
        FileObject nbproject = root.getFileObject("nbproject");
        if (nbproject == null) {
            return;
        }
        
        FileObject privateDir = nbproject.getFileObject("private");
        if (privateDir == null) {
            return;
        }
        
        FileObject privateProperties = privateDir.getFileObject("private.properties");
        if (privateProperties == null) {
            return;
        }
        
        Properties props = PropertyUtils.loadProperties(privateProperties.toURL());
        props.setProperty("compile.on.save", "false");
        try {
            FileWriter writer = new FileWriter(privateProperties.getPath());
            writer.write("");
            writer.flush();
            props.store(writer, "");
            writer.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
    }
    
}
