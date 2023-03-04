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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Specifies license header for module and suite projects.
 */
public class TemplateAttributesProvider implements CreateFromTemplateAttributesProvider {
    private final NbModuleProject project;
    private final AntProjectHelper helper;
    private final boolean netbeansOrg;

    private static final Logger LOG = Logger.getLogger(TemplateAttributesProvider.class.getName());

    public TemplateAttributesProvider(NbModuleProject p, AntProjectHelper helper, boolean netbeansOrg) {
        this.project = p;
        this.helper = helper;
        this.netbeansOrg = netbeansOrg;
    }

    @Override
    public Map<String,?> attributesFor(DataObject template, DataFolder target, String name) {
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String license = props.getProperty("project.license"); // NOI18N
        String licensePath = props.getProperty("project.licensePath"); // NOI18N

        if (license == null && netbeansOrg) {
            license = "apache20-asf"; // NOI18N
        }
        if (license == null && licensePath == null && project != null) {
            SuiteProject sp;
            try {
                sp = SuiteUtils.findSuite(project);
                if (sp != null) {
                    TemplateAttributesProvider tap = sp.getLookup().lookup(TemplateAttributesProvider.class);
                    return tap.attributesFor(template, target, name);
                }
            } catch (IOException ex) {
                // OK, ignore
            }
        }
        Map<String, String> values = new HashMap<String, String>();
        if (license != null) {
            values.put("license", license);
        }
        if(licensePath != null) {
            File path = FileUtil.normalizeFile(helper.resolveFile(licensePath));
            if (path.exists() && path.isAbsolute()) { //is this necessary? should prevent failed license header inclusion
                values.put("licensePath", path.getAbsolutePath());
            } else {
                LOG.log(Level.INFO, "project.licensePath value not accepted - " + license);
            }
        }
        values.put("encoding", "UTF-8"); // NOI18N
        try {
            Project prj = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            ProjectInformation info = ProjectUtils.getInformation(prj);
            if (info != null) {
                String pname = info.getName();
                if (pname != null) {
                    values.put("name", pname);// NOI18N
                }
                String pdname = info.getDisplayName();
                if (pdname != null) {
                    values.put("displayName", pdname);// NOI18N
                }
            }
        } catch (Exception ex) {
            //not really important, just log.
            LOG.log(Level.FINE, "", ex);
        }
        return Collections.singletonMap("project", values); // NOI18N
    }

}
