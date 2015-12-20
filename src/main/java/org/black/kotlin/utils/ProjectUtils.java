package org.black.kotlin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.project.KotlinProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.kotlin.PackageClassUtils;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class ProjectUtils {

    private static final String LIB_FOLDER = "lib";
    private static final String LIB_EXTENSION = "jar";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String KT_HOME;
    
    private org.netbeans.api.project.ProjectUtils projUtils;

    static {
        if (System.getenv("KOTLIN_HOME") != null)
            KT_HOME = System.getenv("KOTLIN_HOME") + FILE_SEPARATOR;
        else
            KT_HOME = System.getenv("KT_HOME") + FILE_SEPARATOR;
    }
    
    /**
     * Finds file with main method.
     * @param files
     * @return path to file with main method.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static String findMain(FileObject[] files) throws FileNotFoundException, IOException {
        for (FileObject file : files) {
            if (!file.isFolder()) {
                for (String line : file.asLines()) {
                    if (line.contains("fun main(")) {
                        return file.getPath();
                    }
                    if (line.contains("public static void main(")) {
                        return file.getPath();
                    }
                }
            } else {
                String main = findMain(file.getChildren());
                if (main != null) {
                    return main;
                }
            }
        }

        return null;
    }

    private static void makeFileCollection(FileObject[] files, List<FileObject> collection){
        for (FileObject file : files) {
            if (!file.isFolder()) {
                collection.add(file);
            } else {
                makeFileCollection(file.getChildren(),collection);
            }
        }
    }
    
    public static String findMainWithDetector(FileObject[] files) throws FileNotFoundException, IOException {
        List<KtFile> ktFiles = new ArrayList();
        List<FileObject> collection = new ArrayList();
        makeFileCollection(files,collection);
        for (FileObject file : collection) {
            if (!file.isFolder()) {
                ktFiles.add(KotlinEnvironment.parseFile(FileUtil.toFile(file)));
            }
        }
        
        KtFile main = KtMainDetector.getMainFunctionFile(ktFiles);
        
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(main.getName()));
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(main.getPackageFqName().asString()));        

        return "";
    }

    
    
    /**
     * Finds file with main method.
     * @param files
     * @return name of the file with main method.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static String findMainName(FileObject[] files) throws FileNotFoundException, IOException {
        for (FileObject file : files) {
            if (!file.isFolder()) {
                for (String line : file.asLines()) {
                    if (line.contains("fun main(")) {
                        return file.getName();
                    }
                    if (line.contains("public static void main("))
                        return file.getName();
                }
            } else {
                String main = findMainName(file.getChildren());
                if (main != null) {
                    return main;
                }
            }
        }

        return null;
    }
    
    public static String getMainFileClass(FileObject[] files) throws IOException{
        String name = findMainName(files);
        String path = findMain(files);

        InputStream is = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
        String beginning = br.readLine().split(" ")[1].split(";")[0];
        is.close();
        
        StringBuilder builder = new StringBuilder("");
        
        builder.append(beginning).append(".");
        
        
        if (name != null){
            if (path.endsWith("kt")){
                char firstCharUpperCase = Character.toUpperCase(name.charAt(0));
                StringBuilder mainFileClass = new StringBuilder("");
                mainFileClass.append(firstCharUpperCase);
                for (int i = 1; i < name.length();i++){
                    mainFileClass.append(name.charAt(i));
                }
                mainFileClass.append("Kt");
                builder.append(mainFileClass.toString());
                return builder.toString();
            } else if (path.endsWith("java")){
                builder.append(name);
                return builder.toString();
            }
        }

        return null;
    }
    
    /**
     * returns path to output directory.
     * @param proj
     * @return path to output directory 
     */
    public static String getOutputDir(KotlinProject proj) {
        
        File path = new File(proj.getProjectDirectory().getPath() + FILE_SEPARATOR + "build");

        if (!path.exists()) {
            path.mkdirs();
        }

        String dir = proj.getProjectDirectory().getPath() + FILE_SEPARATOR + "build";
        String[] dirs;
        if (FILE_SEPARATOR.equals("\\"))
            dirs = dir.split("\\\\");
        else
            dirs = dir.split(FILE_SEPARATOR);
        StringBuilder outputDir = new StringBuilder("");

        for (String str : dirs) {
            outputDir.append(str);
            outputDir.append(FILE_SEPARATOR);
        }
        outputDir.append(proj.getProjectDirectory().getName()).append(".jar");

        return outputDir.toString();
    }

    public static void clean(KotlinProject proj) {

        try {
            if (proj.getProjectDirectory().getFileObject("build") != null) {
                proj.getProjectDirectory().getFileObject("build").delete();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    @NotNull
    public static List<String> getClasspath() {
        List<String> classpath = new ArrayList(KotlinClasspath.getKotlinClasspath());
        return classpath;
    }

    public static List<String> getLibs(KotlinProject proj){
        List<String> libs = new ArrayList();
        FileObject libFolder = proj.getProjectDirectory().getFileObject("lib");
        for (FileObject fo : libFolder.getChildren()){
            if (fo.hasExt("jar"))
                libs.add(fo.getNameExt());
        }
        return libs;
    }

    @Nullable
    public static String getPackageByFile(FileObject file) {
        return "stub";
    }

    public static FqName createPackageClassName(FileObject file) {
        String filePackage = getPackageByFile(file);
        if (filePackage == null) {
            return null;
        }
        return PackageClassUtils.getPackageClassFqName(new FqName(filePackage));
    }

    public static String buildLibPath(String libName) {
        return KT_HOME + buildLibName(libName);
    }

    private static String buildLibName(String libName) {
        return LIB_FOLDER + FILE_SEPARATOR + libName + "." + LIB_EXTENSION;
    }

}
