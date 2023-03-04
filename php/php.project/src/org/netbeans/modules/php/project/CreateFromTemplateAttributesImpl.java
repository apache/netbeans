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

package org.netbeans.modules.php.project;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

// copied from java.api.common
/**
 * Default implementation of {@link CreateFromTemplateAttributes}.
 */
class CreateFromTemplateAttributesImpl implements CreateFromTemplateAttributes {

    private static final Logger LOGGER = Logger.getLogger(CreateFromTemplateAttributesImpl.class.getName());

    private final AntProjectHelper helper;
    private final FileEncodingQueryImplementation encodingQuery;

    public CreateFromTemplateAttributesImpl(AntProjectHelper helper, FileEncodingQueryImplementation encodingQuery) {
        assert helper != null;
        assert encodingQuery != null;

        this.helper = helper;
        this.encodingQuery = encodingQuery;
    }

    @Override
    public Map<String, ?> attributesFor(CreateDescriptor desc) {
        Map<String, String> values = new HashMap<>();
        EditableProperties priv  = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String licensePath = priv.getProperty(PhpProjectProperties.LICENSE_PATH);
        if (licensePath == null) {
            licensePath = props.getProperty(PhpProjectProperties.LICENSE_PATH);
        }
        if (licensePath != null) {
            licensePath = helper.getStandardPropertyEvaluator().evaluate(licensePath);
            if (licensePath != null) {
                File path = FileUtil.normalizeFile(helper.resolveFile(licensePath));
                if (path.exists() && path.isAbsolute()) { // is this necessary? should prevent failed license header inclusion
                    URI uri = Utilities.toURI(path);
                    licensePath = uri.toString();
                    values.put("licensePath", licensePath); // NOI18N
                } else {
                    LOGGER.log(Level.INFO, "project.licensePath value not accepted - {0}", licensePath);
                }
            }
        }
        String license = priv.getProperty(PhpProjectProperties.LICENSE_NAME);
        if (license == null) {
            license = props.getProperty(PhpProjectProperties.LICENSE_NAME);
        }
        if (license != null) {
            values.put("license", license); // NOI18N
        }
        Charset charset = encodingQuery.getEncoding(desc.getTarget());
        String encoding = (charset != null) ? charset.name() : null;
        if (encoding != null) {
            values.put("encoding", encoding); // NOI18N
        }
        try {
            Project prj = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            assert prj != null;
            ProjectInformation info = ProjectUtils.getInformation(prj);
            if (info != null) {
                String pname = info.getName();
                if (pname != null) {
                    values.put("name", pname); // NOI18N
                }
                String pdname = info.getDisplayName();
                if (pdname != null) {
                    values.put("displayName", pdname); // NOI18N
                }
            }
            values.put("webRootPath", getWebRootPath(prj)); // NOI18N
        } catch (Exception ex) {
            // not really important, just log.
            LOGGER.log(Level.FINE, "", ex);
        }

        if (values.isEmpty()) {
            return null;
        }
        return Collections.singletonMap("project", values); // NOI18N
    }

    @CheckForNull
    private static String getWebRootPath(Project project) {
        for (FileObject webRoot : ProjectWebRootQuery.getWebRoots(project)) {
            return FileUtil.getRelativePath(project.getProjectDirectory(), webRoot);
        }
        return null;
    }

}
