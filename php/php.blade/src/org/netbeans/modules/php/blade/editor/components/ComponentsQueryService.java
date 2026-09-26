/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.blade.editor.components;

import org.netbeans.modules.php.blade.editor.components.annotation.Namespace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.modules.php.blade.editor.components.plugins.LivewireComponentResource;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexUtils;
import org.netbeans.modules.php.blade.project.ComponentsSupport;
import org.netbeans.modules.php.blade.project.ProjectUtils;
import org.netbeans.modules.php.blade.syntax.StringUtils;
import org.openide.filesystems.FileObject;

/**
 * Service class used to search, from index, info about a php component class using identified project related filters like : 
 * - namespace
 * - file path
 * 
 * 
 *
 * @author bhaidu
 */
public class ComponentsQueryService {

    
    public Collection<PhpIndexResult> queryComponents(String prefix, FileObject fo) {
        Collection<PhpIndexResult> results = new ArrayList<>();
        Project project = ProjectUtils.getMainOwner(fo);

        if (project == null) {
            return results;
        }

        ComponentsSupport componentSupport = ComponentsSupport.getInstance(project);

        if (!componentSupport.isScanned()) {
            componentSupport.scanForInstalledComponents();
            componentSupport.scanCustomComponentsFolders();
        }

        for (Map.Entry<FileObject, Namespace> namespace : componentSupport.getInstalledComponentNamespace().entrySet()) {
            results.addAll(PhpIndexUtils.queryNamespaceClassesName(fo, prefix, namespace.getValue().path()));
        }

        for (Map.Entry<FileObject, ComponentModel> componentEntry : componentSupport.getComponentClassCollection().entrySet()) {
            FileObject parentDir = componentEntry.getKey().getParent();
            if (componentSupport.getInstalledComponentNamespace().containsKey(parentDir)) {
                continue;
            }
            String className = componentEntry.getKey().getName();
            if (className.toLowerCase().startsWith(prefix)) {
                results.add(new PhpIndexResult(className, componentEntry.getKey(), PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
            }
        }

        return results;
    }

    public Collection<PhpIndexResult> findComponentClass(String prefixClassName, FileObject fo) {
        Collection<PhpIndexResult> results = new ArrayList<>();
        Project project = ProjectUtils.getMainOwner(fo);

        if (project == null) {
            return results;
        }

        ComponentsSupport componentSupport = ComponentsSupport.getInstance(project);

        if (!componentSupport.isScanned()) {
            componentSupport.scanForInstalledComponents();
            componentSupport.scanCustomComponentsFolders();
        } else if (componentSupport.getComponentClassCollection().isEmpty()) {
            componentSupport.scanCustomComponentsFolders();
        }

        for (Map.Entry<FileObject, Namespace> namespace : componentSupport.getInstalledComponentNamespace().entrySet()) {
            results.addAll(PhpIndexUtils.queryExactNamespaceClasses(prefixClassName, namespace.getValue().path(), fo));
        }

        if (prefixClassName.contains(StringUtils.DOT)) {
            //NOT a complete flow, but it should cover the necessities
            String classPathParts[] = prefixClassName.split(StringUtils.ESCAPED_DOT);
            String prefixClassPathName = classPathParts[classPathParts.length - 1];
            for (Map.Entry<FileObject, ComponentModel> componentEntry : componentSupport.getComponentClassCollection().entrySet()) {
                String className = componentEntry.getKey().getName().toLowerCase();
                if (className.equals(prefixClassPathName)) {
                    results.add(new PhpIndexResult(className, componentEntry.getKey(), PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
                }
            }
        } else {
            for (Map.Entry<FileObject, ComponentModel> componentEntry : componentSupport.getComponentClassCollection().entrySet()) {
                FileObject parentDir = componentEntry.getKey().getParent();
                if (componentSupport.getInstalledComponentNamespace().containsKey(parentDir)) {
                    continue;
                }
                String className = componentEntry.getKey().getName();
                if (className.equals(prefixClassName)) {
                    results.add(new PhpIndexResult(className, componentEntry.getKey(), PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
                }
            }
        }

        return results;
    }

    @CheckForNull
    public FileObject getComponentResourceFile(String componentId, String classQualifiedName, FileObject sourceFo) {
        if (classQualifiedName.toLowerCase().contains(LivewireComponentResource.LIVEWIRE_NAME)) {
            return getLivewireComponentResourceFile(componentId, sourceFo);
        }

        return null;
    }

    @CheckForNull
    public FileObject getLivewireComponentResourceFile(String componentId, FileObject sourceFo) {
        Project project = ProjectUtils.getMainOwner(sourceFo);
        if (project == null) {
            return null;
        }

        FileObject componentResource = project.getProjectDirectory().getFileObject(LivewireComponentResource.RESOURCE_PATH + componentId + BladeLanguage.FILE_EXTENSION_WITH_DOT); // NOI18N

        if (componentResource != null && componentResource.isValid()) {
            return componentResource;
        }

        return null;
    }
}
