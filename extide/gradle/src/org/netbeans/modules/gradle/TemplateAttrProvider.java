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

package org.netbeans.modules.gradle;


import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = CreateFromTemplateAttributes.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class TemplateAttrProvider implements  CreateFromTemplateAttributes {

    private static final Logger LOG = Logger.getLogger(TemplateAttrProvider.class.getName());

    private static final String DEFAULT_LICENSE_PREFIX = "/Templates/Licenses/license-"; //NOI18N
    final Project project;

    public TemplateAttrProvider(Project project) {
        this.project = project;
    }

    @Override
    public Map<String, ?> attributesFor(CreateDescriptor desc) {
        Map<String, Object> values = new TreeMap<>();

        GradleBaseProject prj = GradleBaseProject.get(project);
        if (prj != null) {
            values.put("name", prj.getName());
            if (prj.getDescription() != null) {
                values.put("displayName", prj.getDescription());
            }
            if (prj.getGroup() != null) {
                values.put("group", prj.getGroup());
            }
            //Test license against NB license templates
            String license = prj.getLicense();
            String licensePath = findLicensePathInTemplates(license);

            File[] licenseFiles = new File[] {
                new File(prj.getProjectDir(), license),
                new File(prj.getRootDir(), license),
                new File(license)
            };
            for (File licenseFile : licenseFiles) {
                if (licenseFile.isFile()) {
                    licensePath = FileUtil.normalizeFile(licenseFile).toURI().toString();
                    break;
                }
            }
            //Test it as if that were an URL
            if (licensePath == null) {
                license = findLicenseByMavenProjectContent(license);
                licensePath = license != null ? findLicensePathInTemplates(license) : null;
            }
            if (licensePath == null) {
                LOG.log(Level.INFO, "Unable to resolve project license: {0} to project.licensePath", prj.getLicense());
            } else {
                values.put("license", prj.getLicense());
                values.put("licensePath", licensePath);
            }
        }
        return !values.isEmpty() ? Collections.singletonMap("project", values) : null;
    }

    public static String findLicensePathInTemplates(String lic) {
        FileObject fo = FileUtil.getConfigFile("Templates/Licenses/license-" + lic + ".txt"); //NOI18N
        return fo != null ? DEFAULT_LICENSE_PREFIX + lic + ".txt" : null; //NOI18N
    }

    public static String findLicenseByMavenProjectContent(String url) {
        // try to match the project's license URL and the mavenLicenseURL attribute of license template
        FileObject licensesFO = FileUtil.getConfigFile("Templates/Licenses"); //NOI18N
        if (licensesFO == null) {
            return null;
        }
        FileObject[] licenseFiles = licensesFO.getChildren();
        if (url != null) {
            for (FileObject fo : licenseFiles) {
                String str = (String)fo.getAttribute("mavenLicenseURL"); //NOI18N
                if (str != null && Arrays.asList(str.split(" ")).contains(url)) {
                    if (fo.getName().startsWith("license-")) { // NOI18N
                        return fo.getName().substring("license-".length()); //NOI18N
                    } else {
                        Logger.getLogger(TemplateAttrProvider.class.getName()).log(Level.WARNING, "Bad license file name {0} (expected to start with ''license-'' prefix)", fo.getName());
                    }
                    break;
                }
            }
        }
        return null;
    }

}
