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
package org.netbeans.modules.php.blade.project;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.blade.editor.components.annotation.Namespace;
import org.netbeans.modules.php.blade.editor.components.annotation.NamespaceRegister;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
@NamespaceRegister({
    @Namespace(path = "App\\View\\Components", fromApp = true, relativeFilePath = "app/View/Components"), // NOI18N
    @Namespace(path = "App\\Http\\Livewire", fromApp = true, relativeFilePath = "app/Http/Livewire"), // NOI18N
    @Namespace(path = "App\\Livewire", fromApp = true, relativeFilePath = "app/Livewire"), // NOI18N
    @Namespace(path = "Illuminate\\Console\\View\\Components"), // NOI18N
    @Namespace(path = "BladeUI\\Icons\\Components", packageName = "blade-ui-kit/blade-icons"), // NOI18N
    @Namespace(path = "BladeUIKit\\Components", packageName = "blade-ui-kit/blade-ui-kit"),}) // NOI18N
public class ComponentsSupport {

    public static final String LIVEWIRE_NAME = "livewire";  // NOI18N
    
    private static final Map<Project, ComponentsSupport> INSTANCES = new HashMap<>();
    private final Map<FileObject, Namespace> installedComponentNamespace = new HashMap<>();
    private boolean scanned = false;
    private final Project project;

    private ComponentsSupport(Project project) {
        this.project = project;
    }

    public static ComponentsSupport getInstance(Project project) {
        synchronized (INSTANCES) {
            if (INSTANCES.containsKey(project)) {
                return INSTANCES.get(project);
            }
            ComponentsSupport instance = new ComponentsSupport(project);
            INSTANCES.put(project, instance);
            return instance;
        }
    }

    public void scanForInstalledComponents() {
        for (Namespace namespace : getRegisteredNamespaces()) {
            FileObject fo;
            if (namespace.fromApp()) {
                //check if folder exists
                fo = project.getProjectDirectory().getFileObject(namespace.relativeFilePath());
            } else {
                fo = project.getProjectDirectory().getFileObject("vendor/" + namespace.relativeFilePath()); // NOI18N

            }
            if (fo == null || !fo.isValid()) {
                continue;
            }
            installedComponentNamespace.put(fo, namespace);
        }
        scanned = true;
    }

    public boolean isScanned() {
        return scanned;
    }

    public Map<FileObject, Namespace> getInstalledComponentNamespace() {
        return installedComponentNamespace;
    }

    public Namespace[] getRegisteredNamespaces() {
        NamespaceRegister namespaceRegister = this.getClass().getAnnotation(NamespaceRegister.class);
        return namespaceRegister.value();
    }
}
