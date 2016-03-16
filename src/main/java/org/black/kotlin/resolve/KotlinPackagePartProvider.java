package org.black.kotlin.resolve;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.load.kotlin.ModuleMapping;
import org.jetbrains.kotlin.load.kotlin.PackageParts;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinPackagePartProvider implements PackagePartProvider{

    private final Set<VirtualFile> roots = new HashSet<VirtualFile>();
    
    public KotlinPackagePartProvider(Project project){
        Set<VirtualFile> tempRoots = KotlinEnvironment.getEnvironment(project).getRoots();
        
        for (VirtualFile root : tempRoots){
            if (root.findChild("META_INF") != null){
                roots.add(root);
            }
        }
    }
    
    @Override
    public List<String> findPackageParts(String packageFqName) {
        String[] pathParts = packageFqName.split(".");
        
        ArrayList<ModuleMapping> mappings = Lists.newArrayList();
        
        mainloop:
        for (VirtualFile root : roots){
            VirtualFile parent = root.getParent();
            for (String part : pathParts){
                if (!part.isEmpty()){
                    parent = parent.findChild(part);
                    if (parent == null)
                        continue mainloop;
                }
            }
            if (root.findChild("META_INF") != null){
                for (VirtualFile child : root.getChildren()){
                    if (child.getName().endsWith(ModuleMapping.MAPPING_FILE_EXT)){
                        try {
                            mappings.add(ModuleMapping.Companion.create(child.contentsToByteArray()));
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                
            } 
        }
        
        Set<String> returnMappings = Sets.newLinkedHashSet();
        
        for (ModuleMapping mapping : mappings){
            PackageParts packageParts = mapping.findPackageParts(packageFqName);
            if (packageParts  != null){
                returnMappings.addAll(packageParts.getParts());
            }
        }
        
        return Lists.newArrayList(returnMappings); 
    }
    
}
