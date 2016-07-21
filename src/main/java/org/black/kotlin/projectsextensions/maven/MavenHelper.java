package org.black.kotlin.projectsextensions.maven;

import java.io.IOException;
import org.jetbrains.annotations.Nullable;
import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenHelper {
    
    private static NbMavenProjectFactory projectFactory = new NbMavenProjectFactory();
    
    @Nullable
    public static NbMavenProjectImpl getMavenProject(FileObject dir) throws IOException {
        if (dir == null) {
            return null;
        }
        if (projectFactory.isProject(dir)){
            NbMavenProjectImpl project = (NbMavenProjectImpl) projectFactory.loadProject(
                    dir, 
                    new ProjectState(){
                @Override
                public void markModified() {}
                @Override
                public void notifyDeleted() throws IllegalStateException {}
            });
            
            return project;
        }
        
        return null;
    }
    
    public static boolean isModuled(NbMavenProjectImpl project) {
        return !project.getOriginalMavenProject().getModules().isEmpty();
    }
    
}
