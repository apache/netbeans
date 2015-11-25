package org.black.kotlin.project;

import java.io.IOException; 
import org.netbeans.api.project.Project; 
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory; 
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState; 
import org.openide.filesystems.FileObject; 
import org.openide.util.lookup.ServiceProvider; 

/**
 *
 * @author Александр
 */

//@ServiceProvider(service=ProjectFactory.class)
public class KotlinProjectFactory {//implements ProjectFactory {
//
//    public static final String PROJECT_FILE = "kotlin.ktproj";
//    
//    @Override
//    public boolean isProject(FileObject projDir) {
//        return projDir.getFileObject(PROJECT_FILE) != null;
//    }
//
//    @Override
//    public Project loadProject(FileObject dir, ProjectState state) throws IOException {
//        return isProject(dir) ? new KotlinProject(dir,state) : null;
//    }
//
//    @Override
//    public void saveProject(Project prjct) throws IOException, ClassCastException {
//        throw new UnsupportedOperationException("Not supported yet."); 
//    }
// 
}
