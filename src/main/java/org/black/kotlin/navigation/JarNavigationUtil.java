package org.black.kotlin.navigation;

import java.io.File;
import kotlin.Pair;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Александр
 */
public class JarNavigationUtil {
    public static FileObject getFileObjectFromJar(String path){
        Pair<String, String> pathParts = getJarAndInternalPaths(path);
        if (pathParts == null){
            return null;
        }
        
        File jar = new File(pathParts.getFirst());
        jar = jar.getAbsoluteFile();
        
        FileObject fob = FileUtil.toFileObject(jar);
        fob = FileUtil.getArchiveRoot(fob);
        
        String[] internalPathParts = pathParts.getSecond().split("/");
        for (String pathPart : internalPathParts){
            fob = fob.getFileObject(pathPart);
        }
        return fob;
    }
    
    private static Pair<String,String> getJarAndInternalPaths(String path){
        String separator = "!/";
        String[] pathParts = path.split(separator);
        if (pathParts.length == 1){
            return null;
        }
        
        return new Pair<String, String>(pathParts[0], pathParts[1]);
    }
    
}
