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
package org.netbeans.modules.gradle.htmlui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.api.templates.FileBuilder.Mode;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.MapFormat;

public final class GradleArchetype {
    private final FileObject templates;
    private final FileObject projectFo;
    private final Map<String, Object> params;

    public GradleArchetype(FileObject templates, FileObject projectFo, Map<String, Object> params) {
        this.templates = templates;
        this.projectFo = projectFo;
        this.params = params;
    }

    public final void copyTemplates() throws IOException {
        MapFormat mf = new MapFormat(params);
        mf.setLeftBrace("${"); // NOI18N
        mf.setRightBrace("}"); // NOI18N
        Enumeration<? extends FileObject> en = templates.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject template = en.nextElement();
            if (!template.isData()) {
                continue;
            }
            String relativeParent = FileUtil.getRelativePath(templates, template.getParent());
            Object packageAttr = template.getAttribute("package"); // NOI18N
            if (packageAttr instanceof String) {
                String packageName = mf.format(packageAttr).replace('.', '/');
                relativeParent += "/" + packageName;
            }
            FileObject destinationFolder = FileUtil.createFolder(projectFo, relativeParent);

            FileObject previous = destinationFolder.getFileObject(template.getNameExt());
            if (previous != null) {
                previous.delete();
            }

            FileBuilder fb = new FileBuilder(template, destinationFolder);
            fb.withParameters(params);
            fb.defaultMode(Mode.COPY);

            FileObject copied = fb.build().iterator().next();

            assert copied != null && copied.getNameExt().equals(template.getNameExt()) : "Created " + copied;
        }
        ProjectManager.getDefault().clearNonProjectCache();
        assert ProjectManager.getDefault().findProject(projectFo) != null : "Project found for " + projectFo;
    }
}
