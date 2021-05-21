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
package org.netbeans.modules.maven.newproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = CreateFromTemplateHandler.class, position = 5000)
public final class ArchetypeTemplateHandler extends CreateFromTemplateHandler {
    public ArchetypeTemplateHandler() {
    }

    @Override
    protected boolean accept(CreateDescriptor desc) {
        return desc.getTemplate().hasExt("archetype"); // NOI18N
    }

    @NbBundle.Messages({
        "MSG_NoVersion=No version attribute specified for the Maven project",
        "MSG_NoArtifactId=No artifactId attribute specified for the Maven project",
        "MSG_NoGroupId=No groupId attribute specified for the Maven project",
    })
    @Override
    protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
        Properties archetype = new Properties();
        try (InputStream is = desc.getTemplate().getInputStream()) {
            archetype.load(is);
        }
        mergeProperties(desc, archetype);
        
        String version = archetype.getProperty("version"); // NOI18N
        if (version == null) {
            throw new IOException(Bundle.MSG_NoVersion());
        }
        String artifactId = archetype.getProperty("artifactId"); // NOI18N
        if (version == null) {
            throw new IOException(Bundle.MSG_NoArtifactId());
        }
        String groupId = archetype.getProperty("groupId"); // NOI18N
        if (version == null) {
            throw new IOException(Bundle.MSG_NoGroupId());
        }
        String packageName = archetype.getProperty("package"); // NOI18N
        ProjectInfo pi = new ProjectInfo(groupId, artifactId, version, packageName);
        Archetype arch = new Archetype();
        arch.setArtifactId(archetype.getProperty("archetypeArtifactId")); // NOI18N
        arch.setGroupId(archetype.getProperty("archetypeGroupId")); // NOI18N
        arch.setVersion(archetype.getProperty("archetypeVersion")); // NOI18N
        File projDir = desc.getValue(CommonProjectActions.PROJECT_PARENT_FOLDER);
        if (projDir == null) {
            projDir = FileUtil.toFile(desc.getTarget()).getParentFile();
        }
        if (projDir == null) {
            throw new IOException(CommonProjectActions.PROJECT_PARENT_FOLDER + " not specified");
        }
        
        Map<String, String> filteredProperties = 
                NbCollections.checkedMapByFilter(archetype, String.class, String.class, false);
        final File toCreate = new File(projDir, pi.artifactId);
        ArchetypeWizards.createFromArchetype(toCreate, pi, arch, filteredProperties, true);
        FileObject fo = FileUtil.toFileObject(toCreate);

        List<FileObject> fos = new ArrayList<>();
        collectPomDirs(fo, fos);
        final String toOpen = archetype.getProperty("archetypeOpen"); // NOI18N
        if (toOpen != null) {
            collectFiles(fo, fos, toOpen.split(",")); // NOI18N
        }

        if ("true".equals(archetype.getProperty("archetypeBuild"))) { // NOI18N
            Project prj = ProjectManager.getDefault().findProject(fo);
            ActionProvider ap = prj == null ? null : prj.getLookup().lookup(ActionProvider.class);
            if (ap != null) {
                ap.invokeAction(ActionProvider.COMMAND_BUILD, prj.getLookup());
            }
        }
        return fos;
    }

    static void mergeProperties(CreateDescriptor desc, Properties archetype) {
        putAllTo(desc.getParameters(), archetype);
        Map<String,?> wizardParams = desc.getValue("wizard"); // NOI18N
        if (wizardParams != null) {
            putAllTo(wizardParams, archetype);
        }
    }
    
    private static void collectPomDirs(FileObject dir, Collection<? super FileObject> found) {
        if (dir == null || !dir.isFolder()) {
            return;
        }
        if (dir.getFileObject("pom.xml") == null) {
            return;
        }
        found.add(dir);
        for (FileObject f : dir.getChildren()) {
            collectPomDirs(f, found);
        }
    }

    static void collectFiles(FileObject root, Collection<? super FileObject> found, String... includes) {
        Pattern[] check = new Pattern[includes.length];
        for (int i = 0; i < check.length; i++) {
            check[i] = Pattern.compile(includes[i]);
        }

        Enumeration<? extends FileObject> en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            String relPath = FileUtil.getRelativePath(root, fo);
            if (relPath == null) {
                continue;
            }
            for (Pattern p : check) {
                if (p.matcher(relPath).matches()) {
                    found.add(fo);
                    break;
                }
            }
        }
    }

    private static void putAllTo(Map<String, ?> parameters, Properties archetype) {
        for (Map.Entry<String, ?> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                archetype.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
}
