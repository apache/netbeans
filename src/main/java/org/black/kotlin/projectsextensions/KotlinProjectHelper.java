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
        if (!(checkProject(project))){
            return null;
        }
        
        if (!kotlinSources.containsKey(project)) {
            kotlinSources.put(project, new KotlinSources(project));
        }
        
        return kotlinSources.get(project);
    }
    
    public FileObject getLightClassesDirectory(Project project){
        if (!(checkProject(project))){
            return null;
        }
        
        if (!(lightClassesDirs.containsKey(project))) {
            lightClassesDirs.put(project, setLightClassesDir(project));
        }
        
        return lightClassesDirs.get(project);
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
        if (!(checkProject(project))){
            return null;
        }
        
        if (!extendedClassPaths.containsKey(project)){
            if (project instanceof J2SEProject) {
                extendedClassPaths.put(project, new J2SEExtendedClassPathProvider(project));
            }
            if (project instanceof NbMavenProjectImpl) {
                    extendedClassPaths.put(project, new MavenExtendedClassPath((NbMavenProjectImpl) project));
            }
        }
        
        return extendedClassPaths.get(project);
    }
    
    public void updateExtendedClassPath(Project project) {
        if (project instanceof J2SEProject) {
            extendedClassPaths.put(project, new J2SEExtendedClassPathProvider(project));
        }
        if (project instanceof NbMavenProjectImpl) {
                extendedClassPaths.put(project, new MavenExtendedClassPath((NbMavenProjectImpl) project));
        }
        NetBeansJavaProjectElementUtils.updateClasspathInfo(project);
        KotlinEnvironment.updateKotlinEnvironment(project);
    }
    
}
