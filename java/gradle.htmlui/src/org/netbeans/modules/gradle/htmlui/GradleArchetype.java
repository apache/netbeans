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
package org.netbeans.modules.gradle.htmlui;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.MapFormat;

public final class GradleArchetype {
    private final FileObject templates;
    private final File rootDir;
    private final Map<String, Object> params;

    public GradleArchetype(FileObject templates, File rootDir, Map<String, Object> params) {
        this.templates = templates;
        this.rootDir = rootDir;
        this.params = params;
    }

    public final void copyTemplates(TemplateOperation ops) {
        MapFormat mf = new MapFormat(params);
        mf.setLeftBrace("${"); // NOI18N
        mf.setRightBrace("}"); // NOI18N
        List<File> projectDirs = new LinkedList<>();
        projectDirs.add(rootDir);
        
        Enumeration<? extends FileObject> en = templates.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject template = en.nextElement();
            String relativePath = FileUtil.getRelativePath(templates, template);
            if (template.isFolder()) {
                File dir = new File(rootDir, relativePath);
                ops.createFolder(dir);
                Object projectAttr = template.getAttribute("project"); // NOI18N
                if (Boolean.TRUE == projectAttr) {
                    projectDirs.add(dir);
                }
            } else if (template.isData()) {
                Object packageAttr = template.getAttribute("package"); // NOI18N
                if (packageAttr instanceof String) {
                    String relativeParent = FileUtil.getRelativePath(templates, template.getParent());
                    String packageName = mf.format(packageAttr);
                    File sourceRoot = new File(rootDir, relativeParent);
                    ops.createPackage(sourceRoot, packageName);
                    File packageDir = new File(sourceRoot, packageName.replace('.', '/'));

                    Map<String, Object> pparams = new HashMap<>(params);
                    pparams.put("package", packageName); //NOI18N
                    String templateName;
                    try {
                        templateName = DataObject.find(template).getName();
                    } catch (DataObjectNotFoundException ex) {
                        templateName = template.getNameExt();
                    }
                    copyDataTemplate(ops, template, new File(packageDir, templateName), pparams);
                } else {
                    copyDataTemplate(ops, template, new File(rootDir, relativePath), params);
                }
            }
        }
        for (File projectDir : projectDirs) {
            ops.addProjectPreload(projectDir);
        }
    }

    private static void copyDataTemplate(TemplateOperation ops, FileObject template, File target, Map<String, Object> params) {
        Object importantAttr = template.getAttribute("important");
        if (importantAttr == Boolean.TRUE) {
            ops.openFromTemplate(template.getPath(), target, params);
        } else {
            ops.copyFromTemplate(template.getPath(), target, params);
        }
    }
}
