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
package org.netbeans.modules.maven.newproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
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
        "MSG_NoGroupId=No groupId attribute specified for the Maven project",
    })
    @Override
    public List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
        Properties archetype = new Properties();
        try (InputStream is = desc.getTemplate().getInputStream()) {
            archetype.load(is);
        }
        mergeProperties(desc, archetype);
        
        ProjectInfo pi = TemplateUtils.createProjectInfo(desc.getName(), 
            (Map<String, Object>)(Map)archetype);
        String groupId = archetype.getProperty("groupId"); // NOI18N
        if (groupId == null) {
            throw new IOException(Bundle.MSG_NoGroupId());
        }
        Archetype arch = new Archetype();
        arch.setArtifactId(archetype.getProperty("archetypeArtifactId")); // NOI18N
        arch.setGroupId(archetype.getProperty("archetypeGroupId")); // NOI18N
        arch.setVersion(archetype.getProperty("archetypeVersion")); // NOI18N
        File projDir = desc.getValue(CommonProjectActions.PROJECT_PARENT_FOLDER);
        if (projDir == null) {
            projDir = FileUtil.toFile(desc.getTarget());
        }
        if (projDir == null) {
            throw new IOException(CommonProjectActions.PROJECT_PARENT_FOLDER + " not specified");
        }
        
        Map<String, String> filteredProperties = 
                NbCollections.checkedMapByFilter(archetype, String.class, String.class, false);
        final File toCreate = new File(projDir, pi.artifactId);
        ArchetypeWizards.createFromArchetype(toCreate, pi, arch, filteredProperties, true);

        FileObject fo = FileUtil.toFileObject(toCreate);
        return TemplateUtils.afterTemplateCreation(archetype, desc, null, fo);
    }

    static void mergeProperties(CreateDescriptor desc, Properties archetype) {
        putAllTo(desc.getParameters(), archetype);
        Map<String,?> wizardParams = desc.getValue("wizard"); // NOI18N
        if (wizardParams != null) {
            putAllTo(wizardParams, archetype);
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
