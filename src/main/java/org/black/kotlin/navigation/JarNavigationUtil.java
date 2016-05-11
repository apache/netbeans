package org.black.kotlin.navigation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import kotlin.Pair;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
/**
 *
 * @author Александр
 */
public class JarNavigationUtil {
    private static final String ARCHIVE_EXTENSION = "jar";
    
    public static FileObject getFileObjectFromJar(File file){
        Pair<String,String> paths = getJarAndInternalPaths(file.getPath());
        File rootFile = new File(paths.getFirst());
        
        FileObject jarRoot = FileUtil.toFileObject(rootFile);
        if (jarRoot == null){
            return null;
        }
        
        
        
        FileObject fileToReturn = null;
        String[] pathParts = paths.getSecond().split(Pattern.quote(Character.toString(File.separatorChar)));
        for (String pathPart : pathParts){
            
            fileToReturn = jarRoot.getFileObject(pathPart);
            if (fileToReturn == null){
                return null;
            }
        }
        
        return fileToReturn;
    }
    
//    private static FileObject obtainJarRoot(InputStream is) throws IOException{
//        JarInputStream input = new JarInputStream(is);
//        FileSystem syst = FileUtil.createMemoryFileSystem();
//        
//    }
    
    private static Pair<String,String> getJarAndInternalPaths(String path){
        String separator = "!" + Pattern.quote(Character.toString(File.separatorChar));
        String[] pathParts = path.split(separator);
        if (pathParts.length == 1){
            return new Pair<String,String>("","");
        }
//        String jarPath = path.split(separator)[0];
        
//        String internalPath = path.substring(jarPath.length() + separator.length());
        
        return new Pair<String, String>(pathParts[0], pathParts[1]);
//        return new Pair<String, String>(jarPath, internalPath);
    }
    
    public static String getFqNameInsideArchive(String globalPath){
        String inside = globalPath.split(ARCHIVE_EXTENSION+"!"+File.separatorChar)[1];
        return inside.replace(File.separatorChar, '/');
    }
    
    
}
