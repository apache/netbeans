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

package org.netbeans.modules.maven.j2ee.appclient;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.client.DDProvider;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.client.AppClientMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation2;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.j2ee.BaseEEModuleImpl;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Implementation of Application client functionality
 * 
 * @author Martin Janicek
 */
public class AppClientImpl extends BaseEEModuleImpl implements CarImplementation2 {
    
    private MetadataModel<AppClientMetadata> appClientMetadataModel;
    
    
    AppClientImpl(Project project, AppClientModuleProviderImpl provider) {
        super(project, provider, "application-client.xml", J2eeModule.CLIENT_XML); // NOI18N
    }
       
    @Override
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.CAR;
    }
    
    @Override
    public FileObject getArchive() throws IOException {
        return getArchive(Constants.GROUP_APACHE_PLUGINS, "maven-acr-plugin", "acr", "jar"); // NOI18N
    }
    
    @Override
    public Profile getJ2eeProfile() {
        Profile profile = JavaEEProjectSettings.getProfile(project);
        if (profile != null) {
            return profile;
        }
        Profile pomProfile = MavenProjectSupport.getProfileFromPOM(project);
        if (pomProfile != null) {
            return pomProfile;
        }
        return Profile.JAKARTA_EE_8_FULL;
    }
    
    @Override
    public String getModuleVersion() {
        DDProvider prov = DDProvider.getDefault();
        FileObject dd = getDeploymentDescriptor();
        if (dd != null) {
            try {
                AppClient ac = prov.getDDRoot(dd);
                String acVersion = ac.getVersion().toString();
                return acVersion;
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
        }
        return AppClient.VERSION_8_0;
    }

    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == AppClientMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        }
        return null;
    }
    
    public synchronized MetadataModel<AppClientMetadata> getMetadataModel() {
        if (appClientMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            appClientMetadataModel = AppClientMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return appClientMetadataModel;
    }
}
