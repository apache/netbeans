/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.groovy.grailsproject.completion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Petr Hejl
 */
@ServiceProvider(service = CompletionProvider.class)
public class ControllerCompletionProvider implements CompletionProvider {

    private static final Map<MethodSignature, String> INSTANCE_METHODS = new HashMap<MethodSignature, String>();

    // FIXME move it to some resource file, check the grails version - this is for 1.0.4
    static {
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.util.List"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.util.List", "java.lang.String"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.util.Map"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.util.Map", "java.lang.String"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.lang.String"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("chain",
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N // return value void ?
        INSTANCE_METHODS.put(new MethodSignature("redirect",
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"java.lang.Object"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"java.lang.String"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"groovy.lang.Closure"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"java.util.Map", "groovy.lang.Closure"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("withFormat",
                new String[] {"groovy.lang.Closure"}), "java.lang.Object"); // NOI18N
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<MethodSignature, CompletionItem> getMethods(CompletionContext context) {
       Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        if (isInControllerFolder(context)) {
            for (Map.Entry<MethodSignature, String> entry : INSTANCE_METHODS.entrySet()) {
                result.put(entry.getKey(), CompletionItem.forDynamicMethod(
                        context.getAnchor(), entry.getKey().getName(), entry.getKey().getParameters(),
                                entry.getValue(), false));
            }
        }
        return result;
    }
    
    @Override
    public Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context) {
        return Collections.emptyMap();
    }
    
    private boolean isInControllerFolder(CompletionContext context) {
        if (context.getSourceFile() == null) {
            return false;
        }

        Project project = FileOwnerQuery.getOwner(context.getSourceFile());
        if (project != null) {

            if (isController(context.getSourceFile(), project)) {
                return true;
            }
        }
        return false;
    }

    private boolean isController(FileObject source, Project project) {
        if (source == null || !source.getName().endsWith("Controller")) { // NOI18N
            return false;
        }

        FileObject controllerDir = project.getProjectDirectory().getFileObject("grails-app/controllers"); // NOI18N
        if (controllerDir == null || !controllerDir.isFolder()) {
            return false;
        }

        return FileUtil.isParentOf(controllerDir, source);
    }

}
