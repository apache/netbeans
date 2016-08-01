/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.resolve;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.PackageElement;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.jetbrains.kotlin.resolve.sources.LibrarySourcesIndex;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinFileType;

public class KotlinSourceIndex {
    
    private final Map<PackageElement, LibrarySourcesIndex> packageIndexes = 
            new HashMap<PackageElement, LibrarySourcesIndex>();
    
    public static KotlinSourceIndex getInstance(org.netbeans.api.project.Project kotlinProject){
        Project ideaProject = KotlinEnvironment.getEnvironment(kotlinProject).getProject();
        return ServiceManager.getService(ideaProject, KotlinSourceIndex.class);
    }
    
    public static boolean isKotlinSource(String simpleName){
        return simpleName.endsWith(KotlinFileType.EXTENSION);
    }
    
    @Nullable
    public static char[] getSource(PackageElement packageElement, String simpleName){
        org.netbeans.api.project.Project project = NetBeansJavaProjectElementUtils.getProject(packageElement);
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
