package org.black.kotlin.projectsextensions.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    private static final Map<NbMavenProjectImpl, NbMavenProjectImpl> PARENTS = 
            new HashMap<NbMavenProjectImpl, NbMavenProjectImpl>();
    private static final NbMavenProjectFactory PROJECT_FACTORY = 
            new NbMavenProjectFactory();
    
    public static boolean hasParent(NbMavenProjectImpl project) {
        return getParentProjectDirectory(project.getProjectDirectory()) != null;
    }
    
    @Nullable
    public static NbMavenProjectImpl getMavenProject(FileObject dir) throws IOException {
        if (dir == null) {
            return null;
        }
        if (PROJECT_FACTORY.isProject(dir)){
            NbMavenProjectImpl project = (NbMavenProjectImpl) PROJECT_FACTORY.loadProject(
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
        if (PROJECT_FACTORY.isProject(proj.getParent())) {
            FileObject parent = getParentProjectDirectory(proj.getParent());
            
            if (parent != null) {
                return parent;
            }
            return proj.getParent();
        }
        
        return null;
    }
    
    public static NbMavenProjectImpl getMainParent(NbMavenProjectImpl proj) throws IOException {
        NbMavenProjectImpl parent = PARENTS.get(proj);
        if (parent == null){
            parent = getMavenProject(getParentProjectDirectory(proj.getProjectDirectory()));
            PARENTS.put(proj, parent);
        }
        
        return parent != null ? parent : proj;
    }
    
    public static List<FileObject> getAllChildrenSrcDirectoriesOfProject(NbMavenProjectImpl project) {
        List modules = project.getOriginalMavenProject().getModules();
        List<FileObject> srcDirs = new ArrayList<FileObject>();
        
        for (Object module : modules) {
            if (PROJECT_FACTORY.isProject(project.getProjectDirectory().getFileObject((String) module))) {
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
