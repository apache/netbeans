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
package org.netbeans.modules.php.blade.editor.declaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.blade.editor.components.annotation.Namespace;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexUtils;
import org.netbeans.modules.php.blade.project.ComponentsSupport;
import org.netbeans.modules.php.blade.project.ProjectUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class ComponentDeclarationService {

    public Collection<PhpIndexResult> queryComponents(String prefix, FileObject fo) {
        Collection<PhpIndexResult> results = new ArrayList<>();
        Project project = ProjectUtils.getMainOwner(fo);

        if (project == null) {
            return results;
        }
        
        ComponentsSupport componentSupport = ComponentsSupport.getInstance(project);
        
        if (!componentSupport.isScanned()){
            componentSupport.scanForInstalledComponents();
        }

        for (Map.Entry<FileObject, Namespace> namespace : componentSupport.getInstalledComponentNamespace().entrySet()) {
            results.addAll(PhpIndexUtils.queryComponentClass(prefix, namespace.getValue().path(), fo));
        }

        return results;
    }
    
    public FileObject getComponentResourceFile(String componentId, String classQualifiedName, FileObject sourceFo) {
        if (classQualifiedName.toLowerCase().contains(ComponentsSupport.LIVEWIRE_NAME)){
            return getLivewireComponentResourceFile(componentId, sourceFo);
        }
        
        return null;
    }

    public FileObject getLivewireComponentResourceFile(String componentId, FileObject sourceFo) {
        Project project = ProjectUtils.getMainOwner(sourceFo);
        if (project == null) {
            return null;
        }
        
        FileObject componentResource = project.getProjectDirectory().getFileObject("resources/views/livewire/" + componentId + ".blade.php"); // NOI18N

        if (componentResource != null && componentResource.isValid()){
            return componentResource;
        }
        
        return null;
    }
}
