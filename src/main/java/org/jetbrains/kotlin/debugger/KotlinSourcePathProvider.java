/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.debugger;

import com.intellij.psi.util.PsiTreeUtil;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinSourcePathProvider extends SourcePathProvider {

    private final SourcePathProviderImpl impl;
    
    public KotlinSourcePathProvider() {
        impl = new SourcePathProviderImpl();
    }
    
    public KotlinSourcePathProvider(ContextProvider contextProvider) {
        impl = new SourcePathProviderImpl(contextProvider);
    }
    
    @Override
    public String getRelativePath(String url, char directorySeparator, boolean includeExtension) {
        return impl.getRelativePath(url, directorySeparator, includeExtension);
    }

    private String getURLFromProject(Project project, String relativePath) {
        List<KtFile> sourceFiles = ProjectUtils.getSourceFilesWithDependencies(project);
        for (KtFile ktFile : sourceFiles) {
            FqName packageFqName = ktFile.getPackageFqName();
            String fqName = relativePath.replace("/", ".");
            if (!fqName.startsWith(packageFqName.asString())) {
                continue;
            }
            Collection<KtClass> classes = PsiTreeUtil.findChildrenOfType(ktFile, KtClass.class);
            for (KtClass ktClass : classes) {
                if (ktClass.getFqName().asString().equals(fqName)) {
                    String path = ktFile.getVirtualFile().getPath();
                    File file = new File(path);
                    FileObject fo = FileUtil.toFileObject(file);
                    return fo.toURL().toString();
                }
            }
        }
        return null;
    }
    
    @Override
    public String getURL(String relativePath, boolean global) {
        Project project = null;
        Set<FileObject> sourceRootsFO = impl.getSourceRootsFO();
        for ( Project proj : OpenProjects.getDefault().getOpenProjects()) {
            if (!KotlinProjectHelper.INSTANCE.checkProject(proj)) {
                continue;
            }
            
            for (FileObject srcRoot : sourceRootsFO) {
                String srcRootDir = srcRoot.getPath();
                String projDir = proj.getProjectDirectory().getPath();
                if (srcRootDir.startsWith(projDir)) {
                    project = proj;
                    break;
                }
            }
            
            if (project != null) {
                break;
            }
        }
        
        String url = getURLFromProject(project, relativePath);
        if (url != null) {
            return url;
        }
        
        return impl.getURL(relativePath, global);
    }

    @Override
    public String[] getSourceRoots() {
        return impl.getSourceRoots();
    }

    @Override
    public void setSourceRoots(String[] sourceRoots) {
        impl.setSourceRoots(sourceRoots);
    }

    @Override
    public String[] getOriginalSourceRoots() {
        return impl.getOriginalSourceRoots();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        impl.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        impl.removePropertyChangeListener(l);
    }
    
}
