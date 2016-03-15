package org.black.kotlin.resolve;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class KotlinPackagePartProvider implements PackagePartProvider{

    private final Set<VirtualFile> roots = new HashSet<VirtualFile>();
    
    public KotlinPackagePartProvider(Project project){
        Set<VirtualFile> tempRoots = KotlinEnvironment.getEnvironment(project).getRoots();
        
        for (VirtualFile file : tempRoots){
            if (file.findChild("META_INF") != null){
                roots.add(file);
            }
        }
    }
    
    @Override
    public List<String> findPackageParts(String packageFqName) {
        String[] pathParts = packageFqName.split(".");
        
        return null; //TODO
    }
    
}
