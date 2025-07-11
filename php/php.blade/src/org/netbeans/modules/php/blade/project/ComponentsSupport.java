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

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.blade.editor.components.ComponentModel;
import org.netbeans.modules.php.blade.editor.components.annotation.Namespace;
import org.netbeans.modules.php.blade.editor.components.annotation.NamespaceRegister;
import org.netbeans.modules.php.blade.editor.parser.ParsingUtils;
import org.netbeans.modules.php.blade.syntax.StringUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 *
 * Laravel Project support for blade components
 *
 * At the first action request for a blade component tag completion or decl
 * finder the scan for blade components configuration will be triggered.
 *
 * The usage of ComponentModels collection is to have information about the file
 * and custom attributes
 *
 * @author bogdan
 */
//list of most common laravel framework directory paths for blade components classes
@NamespaceRegister({
    @Namespace(path = "App\\View\\Components", fromApp = true, relativeFilePath = "app/View/Components"),
    @Namespace(path = "App\\Http\\Livewire", fromApp = true, relativeFilePath = "app/Http/Livewire"),
    @Namespace(path = "App\\Livewire", fromApp = true, relativeFilePath = "app/Livewire"),//from 10
    @Namespace(path = "Illuminate\\Console\\View\\Components"),
    @Namespace(path = "BladeUI\\Icons\\Components", packageName = "blade-ui-kit/blade-icons", relativeFilePath = "blade-ui-kit/blade-icons/src/Components"),
    @Namespace(path = "BladeUIKit\\Components", packageName = "blade-ui-kit/blade-ui-kit", relativeFilePath = "blade-ui-kit/blade-ui-kit/src/Components"),})
public class ComponentsSupport {

    public static final String COMPONENT_TAG_NAME_PREFIX = "x-"; //NOI18N
    public static final String COMPONENT_TAG_PREFIX = "<" + COMPONENT_TAG_NAME_PREFIX; //NOI18N
    public static final int COMPONENT_TAG_PREFIX_LENGTH = COMPONENT_TAG_PREFIX.length();

    private static final Map<Project, ComponentsSupport> INSTANCES = new HashMap<>();
    private final Map<FileObject, Namespace> installedComponentNamespace = new HashMap<>();

    private static final RequestProcessor RP = new RequestProcessor(ComponentsSupport.class);
    private static final AtomicBoolean installationScan = new AtomicBoolean(false);
    private final Project project;

    private final Map<FileObject, ComponentModel> componentClassCollection = new HashMap<>();

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
            FileObject fo = null;
            if (namespace.fromApp()) {
                //check if folder exists
                fo = project.getProjectDirectory().getFileObject(namespace.relativeFilePath());
            } else if (namespace.relativeFilePath() != null && namespace.relativeFilePath().length() > 0) {
                fo = project.getProjectDirectory().getFileObject("vendor/" + namespace.relativeFilePath()); // NOI18N

            }
            if (fo == null || !fo.isValid()) {
                continue;
            }
            installedComponentNamespace.put(fo, namespace);
            RP.submit(new ComponentParsingTask(fo, componentClassCollection));
        }

        installationScan.set(true);
    }

    public void scanCustomComponentsFolders() {
        BladeProjectProperties bladeProperties = BladeProjectProperties.getInstance(project);
        String componentsFolder[] = bladeProperties.getBladeComponentsClassPathList();
        for (String folder : componentsFolder) {
            if (folder.length() == 0) {
                continue;
            }
            File folderFile = new File(folder);
            if (!folderFile.exists()) {
                continue;
            }

            FileObject folderObj = FileUtil.toFileObject(folderFile);
            RP.submit(new ComponentParsingTask(folderObj, componentClassCollection));
        }
    }

    public void scanBladeComponentsClassFolder(FileObject file) {
        RP.submit(new ComponentParsingTask(file, componentClassCollection));
    }

    public boolean isScanned() {
        return installationScan.get();
    }

    public Map<FileObject, Namespace> getInstalledComponentNamespace() {
        return installedComponentNamespace;
    }

    public Namespace[] getRegisteredNamespaces() {
        NamespaceRegister namespaceRegister = this.getClass().getAnnotation(NamespaceRegister.class);
        return namespaceRegister.value();
    }

    public Map<FileObject, ComponentModel> getComponentClassCollection() {
        return componentClassCollection;
    }

    public ComponentModel findComponentClass(FileObject file) {
        return componentClassCollection.get(file);
    }

    public static String tag2ClassName(String identifier) {
        return identifier.length() > COMPONENT_TAG_PREFIX_LENGTH ? StringUtils.kebabToCamel(identifier.substring(COMPONENT_TAG_PREFIX_LENGTH)) : ""; // NOI18N
    }

    private final class ComponentParsingTask implements Runnable, Cancellable {

        private final FileObject root;
        private volatile boolean cancelled;
        private volatile Future<Void> future;
        private final Map<FileObject, ComponentModel> componentCollection;

        private ComponentParsingTask(FileObject root, Map<FileObject, ComponentModel> componentCollection) {
            this.root = root;
            this.componentCollection = componentCollection;
        }

        @Override
        public void run() {
            //recursive search
            Enumeration<? extends FileObject> children = root.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject file = children.nextElement();
                if (file.isFolder() || !file.getExt().endsWith("php")) { // NOI18N
                    continue;
                }
                if (!cancelled) {
                    ParsingUtils parsingUtils = new ParsingUtils();
                    parsingUtils.laterParseFileObject(file);
                    PHPParseResult result = parsingUtils.getParserResult();
                    if (result != null) {
                        ComponentModel model = new ComponentModel(file);
                        result.getProgram().accept(new ComponentModelVisitor(model));
                        if (model.isValid()) {
                            componentCollection.putIfAbsent(file, model);
                        }
                    }
                }
            }
        }

        @Override
        public boolean cancel() {
            cancelled = true;
            if (future != null) {
                future.cancel(true);
            }
            return true;
        }
    }

    private class ComponentModelVisitor extends DefaultVisitor {

        private final ComponentModel model;

        public ComponentModelVisitor(ComponentModel model) {
            this.model = model;
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null) {
                super.scan(node);
            }
        }

        @Override
        public void visit(ClassDeclaration node) {
            super.visit(node);
            Expression superClass = node.getSuperClass();
            String superClassName = sanitazeClassName(superClass.toString());
            model.checkClassValidity(superClassName);
        }

        @Override
        public void visit(MethodDeclaration node) {
            if (node.getFunction() == null) {
                return;
            }
            String functionName = node.getFunction().getFunctionName().getName();
            if (functionName.equals("__construct")) { // NOI18N
                List<FormalParameter> formalParameters = node.getFunction().getFormalParameters();
                for (FormalParameter parameter : formalParameters) {
                    model.addConstructorProperty(parameter);
                }
            }
        }

        @Override
        public void visit(FormalParameter node) {
            //short constructor
            model.addConstructorProperty(node);
        }

        private String sanitazeClassName(String className) {
            return className.replace("\\", ""); // NOI18N
        }
    }
}
