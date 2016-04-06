package org.black.kotlin.resolve;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.PackageElement;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.black.kotlin.resolve.sources.LibrarySourcesIndex;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinFileType;

public class KotlinSourceIndex {
    
    private final Map<PackageElement, LibrarySourcesIndex> packageIndexes = 
            new HashMap<PackageElement, LibrarySourcesIndex>();
    
    public static KotlinSourceIndex getInstance(org.netbeans.api.project.Project javaProject){
        Project ideaProject = KotlinEnvironment.getEnvironment(javaProject).getProject();
        return ServiceManager.getService(ideaProject, KotlinSourceIndex.class);
    }
    
    public static boolean isKotlinSource(String simpleName){
        return simpleName.endsWith(KotlinFileType.EXTENSION);
    }
    
    @Nullable
    public static char[] getSource(PackageElement packageElement, String simpleName){
        KotlinProject project = NetBeansJavaProjectElementUtils.getProject(packageElement);
        KotlinSourceIndex index = KotlinSourceIndex.getInstance(project);
        String resolvedPath = index.resolvePath(packageElement, simpleName);
        return NetBeansJavaProjectElementUtils.toBinaryName(project, resolvedPath).toCharArray();
    }
    
    
    
    public String resolvePath(PackageElement packageElement, String pathToSource){
        LibrarySourcesIndex packageIndex = getIndexForRoot(packageElement);
        if (packageIndex == null){
            return pathToSource;
        }
        String simpleName = null;
        String result = packageIndex.resolve(simpleName, packageElement);
        return result != null ? result : pathToSource;
    }
    
    @Nullable
    private LibrarySourcesIndex getIndexForRoot(PackageElement packageRoot){
        LibrarySourcesIndex result = packageIndexes.get(packageRoot);
        if (result != null){
            return result;
        }
       
        LibrarySourcesIndex index = new LibrarySourcesIndex(packageRoot);
        packageIndexes.put(packageRoot, index);
        
        return index;
        
    }
    
}
