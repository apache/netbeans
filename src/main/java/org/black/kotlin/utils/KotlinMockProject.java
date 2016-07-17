package org.black.kotlin.utils;

import java.io.IOException;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinMockProject {

    private J2SEProject project = null;
            
    public static KotlinMockProject INSTANCE = new KotlinMockProject();
    
    private KotlinMockProject() {}
    
    private AntProjectHelper createHelper() {
        FileObject userDirectory = FileUtil.toFileObject(Places.getUserDirectory());
        String projectName = "ktFilesWithoutProject";
        if (userDirectory.getFileObject(projectName) == null){
            try {
                userDirectory.createFolder(projectName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        AntProjectHelper helper = null;
        
        try {
            helper =  ProjectGenerator.createProject(userDirectory, "org.netbeans.modules.java.j2seproject");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return helper;
    } 
    
    public J2SEProject getMockProject() {
        if (project == null) {
            try {
                project = new J2SEProject(createHelper());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return project;
    }
    
}
