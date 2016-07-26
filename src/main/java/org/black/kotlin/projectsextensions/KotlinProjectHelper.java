package org.black.kotlin.projectsextensions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.projectsextensions.j2se.classpath.J2SEExtendedClassPathProvider;
import org.black.kotlin.project.KotlinClassPathProvider;
import org.black.kotlin.project.KotlinSources;
import org.black.kotlin.projectsextensions.maven.MavenHelper;
import org.black.kotlin.projectsextensions.maven.buildextender.MavenExtendedClassPath;
import org.black.kotlin.projectsextensions.maven.buildextender.MavenModuledProjectExtendedClassPath;
import org.black.kotlin.projectsextensions.maven.classpath.MavenClassPathProviderImpl;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinProjectHelper {
    
    public static KotlinProjectHelper INSTANCE = new KotlinProjectHelper();
    
    private KotlinProjectHelper(){}
    
    private final Map<Project, KotlinSources> kotlinSources = new HashMap<Project, KotlinSources>();
    private final Map<Project, FileObject> lightClassesDirs = new HashMap<Project, FileObject>();
    private final Map<Project, KotlinClassPathProvider> classpaths = new HashMap<Project, KotlinClassPathProvider>();
    private final Map<Project, ClassPathExtender> extendedClassPaths = new HashMap<Project, ClassPathExtender>();
    
    public boolean checkProject(Project project){
        if ((project instanceof J2SEProject) || (project instanceof NbMavenProjectImpl)){
             return true;
         }
        
        return false;
    }
    
    public KotlinSources getKotlinSources(Project project){
        Project p = project;
        
        if (!(checkProject(p))){
            return null;
        }
        
        if (p instanceof NbMavenProjectImpl) {
            try {
                p = MavenHelper.getMainParent((NbMavenProjectImpl) p);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        if (!kotlinSources.containsKey(p)) {
            kotlinSources.put(p, new KotlinSources(p));
        }
        
        return kotlinSources.get(p);
    }
    
    public FileObject getLightClassesDirectory(Project project){
        Project p = project;
        
        if (!(checkProject(p))){
            return null;
        }
        
        if (p instanceof NbMavenProjectImpl) {
            try {
                p = MavenHelper.getMainParent((NbMavenProjectImpl) p);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        if (!(lightClassesDirs.containsKey(p))) {
            lightClassesDirs.put(p, setLightClassesDir(p));
        }
        
        return lightClassesDirs.get(p);
    }
    
    private FileObject setLightClassesDir(Project project){
        if (Places.getUserDirectory() == null){
            return project.getProjectDirectory().
                    getFileObject("build").getFileObject("classes");
        }
        FileObject userDirectory = FileUtil.toFileObject(Places.getUserDirectory());
        String projectName = project.getProjectDirectory().getName();
        if (userDirectory.getFileObject(projectName) == null){
            try {
                userDirectory.createFolder(projectName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return userDirectory.getFileObject(projectName);
    }
    
    public KotlinClassPathProvider getKotlinClassPathProvider(Project project){
        if (!(checkProject(project))){
            return null;
        }
        
        if (!classpaths.containsKey(project)){
            classpaths.put(project, new KotlinClassPathProvider(project));
        }
        
        return classpaths.get(project);
    }
    
        public ClassPathExtender getExtendedClassPath(Project project) {
        Project p = project;
        
        if (!(checkProject(p))){
            return null;
        }
        
        if (p instanceof NbMavenProjectImpl) {
                try {
                    p = MavenHelper.getMainParent((NbMavenProjectImpl) p);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
        }
        
        if (!extendedClassPaths.containsKey(p)){
            if (p instanceof J2SEProject) {
                extendedClassPaths.put(p, new J2SEExtendedClassPathProvider(p));
            }
            if (p instanceof NbMavenProjectImpl) {
                if (!MavenHelper.isModuled((NbMavenProjectImpl) p)){
                    extendedClassPaths.put(p, new MavenExtendedClassPath((NbMavenProjectImpl) p));
                } else {
                    extendedClassPaths.put(p, new MavenModuledProjectExtendedClassPath((NbMavenProjectImpl) p));
                }
            }
        }
        
        return extendedClassPaths.get(p);
    }

    public void updateExtendedClassPath(Project project) {
        Project p = project;
        if (p instanceof J2SEProject) {
            extendedClassPaths.put(p, new J2SEExtendedClassPathProvider(p));
        }
        if (p instanceof NbMavenProjectImpl) {
            MavenClassPathProviderImpl impl = p.getLookup().lookup(MavenClassPathProviderImpl.class);
            if (impl != null) {
                impl.updateClassPath();
            }
            try {
                p = MavenHelper.getMainParent((NbMavenProjectImpl) p);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (!MavenHelper.isModuled((NbMavenProjectImpl) p)){
                extendedClassPaths.put(p, new MavenExtendedClassPath((NbMavenProjectImpl) p));
            } else {
                extendedClassPaths.put(p, new MavenModuledProjectExtendedClassPath((NbMavenProjectImpl) p));
            }
        }
        NetBeansJavaProjectElementUtils.updateClasspathInfo(p);
        KotlinEnvironment.updateKotlinEnvironment(project);
    }
    
}
