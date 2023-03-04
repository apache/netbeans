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

package org.netbeans.modules.maven;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.License;
import org.apache.maven.model.Organization;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=CreateFromTemplateAttributesProvider.class, projectType="org-netbeans-modules-maven")
public class TemplateAttrProvider implements CreateFromTemplateAttributesProvider {
    private static final Logger LOG = Logger.getLogger(TemplateAttrProvider.class.getName());

    private final Project project;

    public TemplateAttrProvider(Project prj) {
        project = prj;
    }

    public @Override Map<String,?> attributesFor(DataObject template, DataFolder target, String name) {
        Map<String,Object> values = new TreeMap<String,Object>();
        AuxiliaryProperties auxProps = project.getLookup().lookup(AuxiliaryProperties.class);
        String licensePath = auxProps.get(Constants.HINT_LICENSE_PATH, true); //NOI18N
        if (licensePath != null) {
            ExpressionEvaluator eval = PluginPropertyUtils.createEvaluator(project);

            try {
                Object no = eval.evaluate(licensePath);
                if (no != null) {
                    licensePath = no.toString();
                }
            } catch (ExpressionEvaluationException ex) {
                Exceptions.printStackTrace(ex);
            }
            File path = FileUtil.normalizeFile(FileUtilities.resolveFilePath(FileUtil.toFile(project.getProjectDirectory()), licensePath));
            if (path.exists() && path.isAbsolute()) { //is this necessary? should prevent failed license header inclusion
                URI uri = Utilities.toURI(path);
                licensePath = uri.toString();
                values.put("licensePath", licensePath);
            } else {
               LOG.log(Level.INFO, "project.licensePath value not accepted - " + licensePath);
            }
        }

        String license = auxProps.get(Constants.HINT_LICENSE, true); //NOI18N
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        assert nbMavenProject != null;
        MavenProject mp = nbMavenProject.getMavenProject();
        if (license == null) {
            license = findLicenseByMavenProjectContent(mp);
        }
        if (license != null) {
            values.put("license", license); // NOI18N
        }

        Organization organization = mp.getOrganization();
        if (organization != null) {
            String organizationName = organization.getName();
            if (organizationName != null) {
                values.put("organization", organizationName); // NOI18N
            }
        }

        FileEncodingQueryImplementation enc = project.getLookup().lookup(FileEncodingQueryImplementation.class);
        Charset charset = enc.getEncoding(target.getPrimaryFile());
        String encoding = (charset != null) ? charset.name() : null;
        if (encoding != null) {
            values.put("encoding", encoding); // NOI18N
        }

        ProjectInformation pi = ProjectUtils.getInformation(project);
        values.put("name", mp.getArtifactId()); // NOI18N
        values.put("displayName", pi.getDisplayName()); // NOI18N

        //#206321
        if (mp.getProperties() != null) {
            Map<String, Object> props = new HashMap<String, Object>();
            for (String prop : mp.getProperties().stringPropertyNames()) {
                String[] split = prop.split("\\.");
                String value = mp.getProperties().getProperty(prop);
                putProp(split, props, value);
            }
            if (props.size() > 0) {
                values.put("property", props);
            }
        }

        // #251780
        if (NbMavenProject.TYPE_WAR.equalsIgnoreCase(nbMavenProject.getPackagingType())) {
            values.put("webRootPath", getWebRootPath(project)); // NOI18N
        }

        if (values.size() > 0) {
            return Collections.singletonMap("project", values); // NOI18N
        } else {
            return null;
        }
    }

    @CheckForNull
    private static String getWebRootPath(Project project) {
        for (FileObject webRoot : ProjectWebRootQuery.getWebRoots(project)) {
            return FileUtil.getRelativePath(project.getProjectDirectory(), webRoot);
        }
        return null;
    }

    private void putProp(String[] split, Map<String, Object> props, String value) {
        if (split.length > 0) {
            if (split.length == 1) {
                props.put(split[0], value);
            } else {
                Object valu = props.get(split[0]);
                Map<String, Object> childProp;
                if (valu == null) {
                    childProp = new HashMap<String, Object>();
                    props.put(split[0], childProp);
                } else {
                    if (valu instanceof Map) {
                        childProp = (Map<String, Object>) valu;
                    } else {
                        //cannot have both maven.test and maven.test.skip properties defined :(
                        return;
                    }
                }
                putProp(Arrays.copyOfRange(split, 1, split.length), childProp, value);
            }
        }
    }

    public static String findLicenseByMavenProjectContent(MavenProject mp) {
        // try to match the project's license URL and the mavenLicenseURL attribute of license template
        FileObject licensesFO = FileUtil.getConfigFile("Templates/Licenses"); //NOI18N
        if (licensesFO == null) {
            return null;
        }
        FileObject[] licenseFiles = licensesFO.getChildren();
        for (License license : mp.getLicenses()) {
            String url = license.getUrl();
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
        }
        return null;
    }
}
