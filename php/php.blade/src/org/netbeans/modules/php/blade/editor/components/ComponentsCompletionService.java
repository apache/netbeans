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

import org.netbeans.modules.php.blade.editor.components.annotation.NamespaceRegister;
import org.netbeans.modules.php.blade.editor.components.annotation.Namespace;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexUtils;
import org.netbeans.modules.php.blade.project.ProjectUtils;
import org.openide.filesystems.FileObject;

/**
 * most frequently used plugins for laravel
 * 
 * @author bhaidu
 */
@NamespaceRegister({
    @Namespace(path = "App\\View\\Components", from_app=true, relativeFilePath="app/View/Components"), // NOI18N
    @Namespace(path = "App\\Http\\Livewire", from_app=true, relativeFilePath="app/Http/Livewire"), // NOI18N
    @Namespace(path = "App\\Livewire", from_app=true, relativeFilePath="app/Livewire"), // NOI18N
    @Namespace(path = "Illuminate\\Console\\View\\Components"), // NOI18N
    @Namespace(path = "BladeUIKit\\Components\\Buttons", packageName="blade-ui-kit/blade-ui-kit"), // NOI18N
    @Namespace(path = "BladeUIKit\\Components\\Layouts", packageName="blade-ui-kit/blade-ui-kit"), // NOI18N
    @Namespace(path = "BladeUIKit\\Components\\Forms\\Inputs", packageName="blade-ui-kit/blade-ui-kit"), // NOI18N
})
public class ComponentsCompletionService {

    public Collection<PhpIndexResult> queryComponents(String prefix, FileObject fo) {
        Collection<PhpIndexResult> results = new ArrayList<>();
        Project project = ProjectUtils.getMainOwner(fo);
        for (Namespace namespace : getNamespaces()){
            if (namespace.from_app()){
                //check if folder exists
                if (project.getProjectDirectory().getFileObject(namespace.relativeFilePath()) == null){
                    continue;
                }
            }
            results.addAll(PhpIndexUtils.queryNamespaceClassesName(prefix, namespace.path(), fo));
        }

        return results;
    }

    public Namespace[] getNamespaces() {
        NamespaceRegister namespaceRegister = this.getClass().getAnnotation(NamespaceRegister.class);
        return namespaceRegister.value();
    }
}
