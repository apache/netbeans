/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.black.kotlin.resolve;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.List;
import java.util.Set;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.netbeans.api.project.Project;

public class KotlinPackagePartProvider implements PackagePartProvider 
{
    Set<VirtualFile> roots; 
    
    KotlinPackagePartProvider(Project project) {
        roots = org.black.kotlin.model.KotlinEnvironment.getEnvironment(project).getRoots();
    }

    @Override
    public List<String> findPackageParts(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}