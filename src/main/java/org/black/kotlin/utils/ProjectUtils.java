package org.black.kotlin.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.black.kotlin.project.KotlinProject;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Александр
 */
public class ProjectUtils {
    
    private static final String LIB_FOLDER = "lib";
    private static final String LIB_EXTENSION = "jar";
    public static final String KT_HOME = "C:/kotlin/kotlinc/";
    
    public static String findMain(FileObject[] files) throws FileNotFoundException, IOException {
        for (FileObject file : files) {
            if (!file.isFolder()) {
                for (String line : file.asLines()) {
                    if (line.contains("fun main(")) {
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
    
    public static String getOutputDir(KotlinProject proj){
        
        File path = new File(proj.getProjectDirectory().getPath()+"/build/output");
        
        if (!path.exists()){
            path.mkdirs();
        }
                
        
        String dir = proj.getProjectDirectory().getPath()+"/build/output";
        String[] dirs = dir.split("/");
        StringBuilder outputDir = new StringBuilder("");
        
        for (String str : dirs){
            outputDir.append(str);
            outputDir.append("\\");
        }
        outputDir.append("hello.jar");
        
        return outputDir.toString();
    }

    public static String buildLibPath(String libName) {
        return KT_HOME + buildLibName(libName);
    }
    
    private static String buildLibName(String libName) {
        return LIB_FOLDER + "/" + libName + "." + LIB_EXTENSION;
    }
    
}
