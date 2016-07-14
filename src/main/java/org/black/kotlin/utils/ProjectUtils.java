package org.black.kotlin.utils;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.black.kotlin.builder.KotlinPsiManager;
import org.black.kotlin.bundledcompiler.BundledCompiler;
import org.black.kotlin.j2seprojectextension.classpath.J2SEExtendedClassPathProvider;
import org.black.kotlin.j2seprojectextension.KotlinProjectHelper;
import org.black.kotlin.project.KotlinClassPathProvider;
import org.black.kotlin.project.KotlinProjectConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class ProjectUtils {

    private static final String LIB_FOLDER = "lib";
    private static final String LIB_EXTENSION = "jar";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static String KT_HOME;

    static {
        if (System.getenv("KOTLIN_HOME") != null) {
            KT_HOME = System.getenv("KOTLIN_HOME") + FILE_SEPARATOR;
        } else if (System.getenv("KT_HOME") != null) {
            KT_HOME = System.getenv("KT_HOME") + FILE_SEPARATOR;
        }
    }

    public static void checkKtHome(ClassLoader cl){
        if (KT_HOME == null){
            FileObject dir = FileUtil.toFileObject(Places.getUserDirectory());
            if (dir.getFileObject("kotlinc") == null){
                BundledCompiler.getKotlinc(cl);
            }
            KT_HOME = Places.getUserDirectory().getAbsolutePath() + FILE_SEPARATOR + "kotlinc"
                    + FILE_SEPARATOR;
        }
    }
    
    /**
     * Finds java file with main method.
     *
     * @param files project files
     * @return file with main method.
     * @throws IOException
     */
    public static FileObject findJavaMain(Project project) throws IOException {
        for (FileObject javaFolder : KotlinProjectHelper.INSTANCE.getKotlinSources(project).//project.getKotlinSources().
                getSrcDirectories(KotlinProjectConstants.JAVA_SOURCE)){
            for (FileObject file : javaFolder.getChildren()){
                if (!file.isFolder()) {
                    for (String line : file.asLines()) {
                        if (line.contains("public static void main(")) {
                            String lineBeginning = line.split("main")[0];
                            if (!lineBeginning.contains("/")){
                                return file;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    private static void makeFileCollection(FileObject[] files, List<FileObject> collection) {
        for (FileObject file : files) {
            if (!file.isFolder()) {
                collection.add(file);
            } else {
                makeFileCollection(file.getChildren(), collection);
            }
        }
    }

    public static KtFile findKotlinMain(Project project) throws IOException {
        List<KtFile> ktFiles = new ArrayList<KtFile>();
        List<FileObject> kotlinFolders = KotlinProjectHelper.INSTANCE.getKotlinSources(project).//project.getKotlinSources().
                getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
        for (FileObject folder : kotlinFolders) {
            for (FileObject file : folder.getChildren()){
                if (!file.isFolder() && KotlinPsiManager.INSTANCE.isKotlinFile(file)){
                    ktFiles.add(KotlinPsiManager.INSTANCE.getParsedFile(file));
                }
            }
        }

        return KtMainDetector.getMainFunctionFile(ktFiles);
    }

    public static String getMainFileClass(Project project) throws IOException {
        KtFile main = findKotlinMain(project);
        if (main != null) {
            String name = main.getName().split(".kt")[0];
            String path = main.getViewProvider().getVirtualFile().getCanonicalPath();
            if (path != null) {
                InputStream is = new FileInputStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String beginning = br.readLine().split(" ")[1].split(";")[0];
                is.close();

                StringBuilder builder = new StringBuilder("");

                builder.append(beginning).append(".");
                char firstCharUpperCase = Character.toUpperCase(name.charAt(0));
                StringBuilder mainFileClass = new StringBuilder("");
                mainFileClass.append(firstCharUpperCase);
                for (int i = 1; i < name.length(); i++) {
                    mainFileClass.append(name.charAt(i));
                }
                mainFileClass.append("Kt");
                builder.append(mainFileClass.toString());
                return builder.toString();
            }
        } else {
            FileObject javaMain = findJavaMain(project);
            if (javaMain != null) {
                String name = javaMain.getName();
                String path = javaMain.getPath();
                InputStream is = new FileInputStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String beginning = br.readLine().split(" ")[1].split(";")[0];
                is.close();
                return beginning + "." + name;
            }
        }
        return null;
    }


    public static void clean(Project proj) {

        try {
            if (proj.getProjectDirectory().getFileObject("build") != null) {
                for (FileObject fo : proj.getProjectDirectory().getFileObject("build").getChildren()){
                    if (!fo.getName().equals("classes"))
                        fo.delete();
                }
                FileObject classesDir = proj.getProjectDirectory().getFileObject("build").getFileObject("classes");
                if (classesDir != null){
                    for (FileObject fo : classesDir.getChildren())
                        fo.delete();
                }
                
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static List<String> createListOfClassPaths(ClassPath boot, ClassPath src, ClassPath compile) {
        List<String> classpath = Lists.newArrayList();
        
        for (ClassPath.Entry entry : boot.entries()){
            String path = entry.getURL().getFile();
            if (path != null){
                try {
                    classpath.add(URLDecoder.decode(path, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        for (ClassPath.Entry entry : src.entries()){
            String path = entry.getURL().getPath();
            if (path != null){
                try {
                    classpath.add(URLDecoder.decode(path, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        for (ClassPath.Entry entry : compile.entries()){
            String path = entry.getURL().getPath();
            if (path != null){
                try {
                    classpath.add(URLDecoder.decode(path, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        return classpath;
    }
    
    @NotNull
    private static List<String> getMavenProjectClassPath(NbMavenProjectImpl project) {
        List<String> classPath = new ArrayList<String>();
        
        try {
            String bootClassPath = System.getProperty("sun.boot.class.path");
            List<String> javaClasspathElements = new ArrayList<String>(Arrays.asList(bootClassPath.split(
                Pattern.quote(System.getProperty("path.separator")))));
            
            classPath.addAll(project.getOriginalMavenProject().getCompileClasspathElements());
            classPath.addAll(project.getOriginalMavenProject().getCompileSourceRoots());
            classPath.addAll(project.getOriginalMavenProject().getRuntimeClasspathElements());
            classPath.addAll(project.getOriginalMavenProject().getSystemClasspathElements());
            classPath.addAll(javaClasspathElements);
            
        } catch (DependencyResolutionRequiredException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return classPath;
}
    
    @NotNull
    private static List<String> getJ2SEProjectClassPath(Project project) {
        J2SEExtendedClassPathProvider extendedProvider = KotlinProjectHelper.INSTANCE.getJ2SEExtendedClassPathProvider(project);
        ClassPath boot = extendedProvider.getProjectSourcesClassPath(ClassPath.BOOT);
        ClassPath src = extendedProvider.getProjectSourcesClassPath(ClassPath.SOURCE);
        ClassPath compile = extendedProvider.getProjectSourcesClassPath(ClassPath.COMPILE);
        
        return createListOfClassPaths(boot, src, compile);
    }
    
    
    
    @NotNull
    public static List<String> getClasspath(Project project) {
        if (project instanceof J2SEProject) {
            return getJ2SEProjectClassPath(project);
        }
        
        if (project instanceof NbMavenProjectImpl) {
            return getMavenProjectClassPath((NbMavenProjectImpl) project);
        }
        
        KotlinClassPathProvider kotlinClassPath = KotlinProjectHelper.INSTANCE.getKotlinClassPathProvider(project);
  
        ClassPath boot = kotlinClassPath.findClassPath(null, ClassPath.BOOT);
        ClassPath src = kotlinClassPath.findClassPath(null, ClassPath.SOURCE);
        ClassPath compile = kotlinClassPath.findClassPath(null, ClassPath.COMPILE);
        
//        ClassPath boot = project.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.BOOT);
//        ClassPath src = project.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.SOURCE);
//        ClassPath compile = project.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.COMPILE);
        
        return createListOfClassPaths(boot, src, compile);
    }

    public static List<String> getLibs(Project proj) {
        List<String> libs = new ArrayList<String>();
        FileObject libFolder = proj.getProjectDirectory().getFileObject("lib");
        for (FileObject fo : libFolder.getChildren()) {
            if (fo.hasExt("jar")) {
                libs.add(fo.getNameExt());
            }
        }
        return libs;
    }

    public static String buildLibPath(String libName) {
        return KT_HOME + buildLibName(libName);
    }

    private static String buildLibName(String libName) {
        return LIB_FOLDER + FILE_SEPARATOR + libName + "." + LIB_EXTENSION;
    }
    
    public static KtFile getKtFile(FileObject file){
        try {
            return KotlinPsiManager.INSTANCE.getParsedFile(file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }
    
    public static KtFile getKtFile(String code, FileObject file){
        return KotlinPsiManager.INSTANCE.parseTextForDiagnostic(code, file);
    }
    
    @NotNull
    public static List<KtFile> getSourceFiles(@NotNull Project project){
        List<KtFile> ktFiles = new ArrayList<KtFile>();
        
        for (FileObject file : KotlinPsiManager.INSTANCE.getFilesByProject(project)){
            ktFiles.add(getKtFile(file));
            
        }
        
        return ktFiles; 
    }
    
    @NotNull
    public static List<KtFile> getSourceFilesWithDependencies(@NotNull Project project){
        //TODO
        List<KtFile> files = getSourceFiles(project);
        return files;
    }

    public static Project getKotlinProjectForFileObject(FileObject file){
        
        for (Project project : OpenProjects.getDefault().getOpenProjects()){
            if (!KotlinProjectHelper.INSTANCE.checkProject(project)){
                continue;
            }
            
            if (file.toURI().toString().contains(project.getProjectDirectory().toURI().toString())){
                return project;
            }
            
            if (file.toURI().toString().contains(KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).toURI().toString())){
                return project;
            }
        }
        
        return null;
        
    }
    
    public static FileObject getFileObjectForDocument(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);

        if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        }

        if (sdp instanceof DataObject) {
            DataObject dobj = (DataObject) sdp;
            return dobj.getPrimaryFile();
        }

        return null;
    }
    
    public static StyledDocument getDocumentFromFileObject(FileObject file) throws IOException{
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (dataObject == null){
            return null;
        }
        
        EditorCookie editorCookie = (EditorCookie) dataObject.getLookup().lookup(EditorCookie.class);
        if (editorCookie == null){
            return null;
        }
        
        editorCookie.open();
        return editorCookie.openDocument();
    }
    
    public static String getKotlinProjectLightClassesPath(Project project){
        return KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).getPath();
    }
    
    public static String getKotlinProjectClassesPath(Project project){
        return project.getProjectDirectory().
                    getFileObject("build").getFileObject("classes").getPath();
    }
    
}
