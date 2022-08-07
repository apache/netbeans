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

package org.netbeans.modules.maven.newproject.idenative;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.newproject.TemplateUtils;
import org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.NbBundle;

/**
 * Allows to create the 'ide native' projects in the headless mode. Is not registered as an interceptor, but rather
 * listed explicitly for a template it handles.
 * @author sdedic
 */
public class IDENativeTemplateHandler extends CreateFromTemplateHandler {
    /**
     * Allow to reference project's contents placed elsewhere. Value must be a String
     * path to config filesystem.
     */
    public static final String PARAM_CONTENT_TEMPLATE = "projectContentsTemplate"; // NOI18N
    
    @Override
    protected boolean accept(CreateDescriptor desc) {
        return true;
    }

    @NbBundle.Messages("TITLE_CreatingNewProject=Creating a new project")
    @Override
    protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
        String packaging = desc.getValue(TemplateUtils.PARAM_PACKAGING);

        File projDir = desc.getValue(CommonProjectActions.PROJECT_PARENT_FOLDER);
        if (projDir == null) {
            projDir = FileUtil.toFile(desc.getTarget());
        }
        if (projDir == null) {
            throw new IOException(CommonProjectActions.PROJECT_PARENT_FOLDER + " not specified");
        }
        ProgressHandle handle = desc.getLookup().lookup(ProgressHandle.class);
        boolean handleCreated = handle == null;
        if (handle == null) {
            handle = ProgressHandle.createHandle(Bundle.TITLE_CreatingNewProject());
        }
        File projFile = FileUtil.normalizeFile(projDir);
        ProjectInfo pi = TemplateUtils.createProjectInfo(desc.getName(), desc.getParameters());
        File toCreate = new File(projFile, desc.getName());
        CreateProjectBuilder builder = customizeBuilder(
                new CreateProjectBuilder(
                    toCreate, 
                    pi.groupId, pi.artifactId, pi.version)
                    .setProgressHandle(handle)
                    .setPackaging(packaging)
                    .setPackageName(pi.packageName), pi);
        if (handleCreated) {
            handle.start();
        }
        try {
            builder.create();
            // project has been probably created, let it be recognized
            ProjectManager.getDefault().clearNonProjectCache();
            FileObject fo = FileUtil.toFileObject(toCreate);
            Properties p = new Properties();
            List<FileObject> toOpen = createProjectContents(fo, desc, pi, p);
            return TemplateUtils.afterTemplateCreation(p, desc, toOpen, fo);
        } finally {
            if (handleCreated) {
                handle.finish();
            }
        }
    }
    
    String deriveClassName(String artifactName) {
        String[] parts = artifactName.split("\\W+");
        StringBuilder sb = new StringBuilder();
        for (String s : parts) {
            sb.append(Character.toUpperCase(s.charAt(0)));
            sb.append(s.substring(1));
        }
        return sb.toString();
    }
    
    protected List<FileObject> createProjectContents(FileObject target, CreateDescriptor cd, ProjectInfo pi, Properties p) throws IOException {
        Map<String, Object> params = new HashMap<>(cd.getParameters());
        // remove parameters that were meant for this template
        params.remove("name");
        params.remove("nameAndExt");
        
        params.put(FileBuilder.ATTR_TEMPLATE_MERGE_FOLDERS, Boolean.TRUE);
        params.put("projecName", cd.getName());
        params.put("packagePath", pi.packageName == null ? "" : pi.packageName.replace('.', '/')); // NOI18N
        
        String packaging = cd.getValue(TemplateUtils.PARAM_PACKAGING);
        if (packaging == null || "jar".equals(packaging)) { // NOI18N
            String mainName;
            if (!params.containsKey("mainClassName")) { // NOI18N
                String derived = deriveClassName(pi.artifactId);
                if (BaseUtilities.isJavaIdentifier(derived)) {
                    mainName = derived;
                } else {
                    mainName = "App"; // NOI18N
                }
                params.put("mainClassName", mainName); // NOI18N
            }
        }
        

        String contentPath = null;
        FileObject projectContents = null;
        Object o = cd.getTemplate().getAttribute(PARAM_CONTENT_TEMPLATE);
        if (o == null) {
            FileObject tf = cd.getTemplate();
            String rel = FileUtil.getRelativePath(FileUtil.getConfigFile("Templates"), tf); // NOI18N
            if (rel == null || rel.startsWith("..")) { // NOI18N
                contentPath = null;
            } else {
                projectContents = FileUtil.getConfigFile("Maven2Templates/ProjectContents/" + rel); // NOI18N
            }
        } else {
            contentPath = o.toString();
        }

        if (contentPath != null) {
            projectContents = FileUtil.getConfigFile(contentPath);
        } else if (cd.getTemplate().isFolder()) {
            projectContents = cd.getTemplate();
        }
        
        if (projectContents == null) {
            return null;
        }
        if (!projectContents.isFolder() || !Boolean.TRUE.equals(projectContents.getAttribute("template"))) { // NOI18N
            return null;
        }
        List<FileObject> result = new ArrayList<>();
        
        for (FileObject c : projectContents.getChildren()) {
            result.addAll(new FileBuilder(c, target).defaultMode(FileBuilder.Mode.COPY).withParameters(params).build());
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (v != null) {
                p.put(k, v);
            }
        }
        return result;
    }
    
    protected CreateProjectBuilder customizeBuilder(CreateProjectBuilder builder, ProjectInfo pi) {
        return builder;
    }
}
