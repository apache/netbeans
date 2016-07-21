package org.black.kotlin.projectsextensions.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

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
    
    public static boolean isMavenMainModuledProject(NbMavenProjectImpl project) {
        if (isModuled(project)) {
            try {
                if (getMavenProject(project.getProjectDirectory().getParent()) == null) {
                    return true;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    private static FileObject getParentProjectDirectory(FileObject proj) {
        if (projectFactory.isProject(proj.getParent())) {
            FileObject parent = getParentProjectDirectory(proj.getParent());
            
            if (parent != null) {
                return parent;
            }
            return proj.getParent();
        }
        
        return null;
    }
    
    public static NbMavenProjectImpl getMainParent(NbMavenProjectImpl proj) throws IOException {
        NbMavenProjectImpl parent = getMavenProject(getParentProjectDirectory(proj.getProjectDirectory()));
        
        return parent != null ? parent : proj;
    }
    
    public static List<FileObject> getAllChildrenSrcDirectoriesOfProject(NbMavenProjectImpl project) {
        List modules = project.getOriginalMavenProject().getModules();
        List<FileObject> srcDirs = new ArrayList<FileObject>();
        
        for (Object module : modules) {
            if (projectFactory.isProject(project.getProjectDirectory().getFileObject((String) module))) {
                try {
                    NbMavenProjectImpl child = MavenHelper.getMavenProject(project.getProjectDirectory().getFileObject((String) module));
                    if (isModuled(child)){
                        srcDirs.addAll(getAllChildrenSrcDirectoriesOfProject(child));
                    } else {
                        srcDirs.add(child.getProjectDirectory().getFileObject("src"));
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        return srcDirs;
    }
    
}
