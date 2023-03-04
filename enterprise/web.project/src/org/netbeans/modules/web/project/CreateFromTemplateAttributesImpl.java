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
package org.netbeans.modules.web.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public final class CreateFromTemplateAttributesImpl implements CreateFromTemplateAttributes {

    private static final Logger LOGGER = Logger.getLogger(CreateFromTemplateAttributesImpl.class.getName());

    private final AntProjectHelper helper;
    private final CreateFromTemplateAttributesProvider delegate;


    CreateFromTemplateAttributesImpl(AntProjectHelper helper, FileEncodingQueryImplementation encodingQuery) {
        assert helper != null;
        assert encodingQuery != null;
        this.helper = helper;
        delegate = QuerySupport.createTemplateAttributesProvider(helper, encodingQuery);
    }

    @Override
    public Map<String, ?> attributesFor(CreateDescriptor desc) {
        try {
            Map<String, Object> values = (Map<String, Object>) delegate.attributesFor(
                    DataObject.find(desc.getTemplate()), DataFolder.findFolder(desc.getTarget()), desc.getName());
            if (values == null) {
                values = new HashMap<>();
            }
            Map<String, Object> projectValues = (Map<String, Object>) values.get("project"); // NOI18N
            if (projectValues == null) {
                projectValues = new HashMap<>();
                values.put("project", projectValues); // NOI18N
            }
            Project prj = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            assert prj != null;
            projectValues.put("webRootPath", getWebRootPath(prj)); // NOI18N
            return values;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    @CheckForNull
    private static String getWebRootPath(Project project) {
        for (FileObject webRoot : ProjectWebRootQuery.getWebRoots(project)) {
            return FileUtil.getRelativePath(project.getProjectDirectory(), webRoot);
        }
        return null;
    }

}
