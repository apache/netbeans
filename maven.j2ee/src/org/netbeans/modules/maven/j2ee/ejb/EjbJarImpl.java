/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ejb;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.j2ee.BaseEEModuleImpl;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Implementation of EJB functionality
 * 
 * @author Milos Kleint, Martin Janicek
 */
public class EjbJarImpl extends BaseEEModuleImpl implements EjbJarImplementation2 {
    
    private MetadataModel<EjbJarMetadata> ejbJarMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    
    
    EjbJarImpl(Project project, EjbModuleProviderImpl provider) {
        super(project, provider, "ejb-jar.xml", J2eeModule.EJBJAR_XML); //NOI18N
    }
    
       
    @Override
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.EJB;
    }
    
    @Override
    public FileObject getArchive() throws IOException {
        return getArchive(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_EJB, "ejb", "jar"); //NOI18N
    }

    @Override
    public Profile getJ2eeProfile() {
        Profile profile = JavaEEProjectSettings.getProfile(project);
        if (profile != null) {
            return profile;
        }
        String ver = getModuleVersion();
        if (EjbJar.VERSION_2_1.equals(ver)) {
            return Profile.J2EE_14;
        }
        if (EjbJar.VERSION_3_0.equals(ver)) {
            return Profile.JAVA_EE_5;
        }
        if (EjbJar.VERSION_3_1.equals(ver)) {
            return Profile.JAVA_EE_6_FULL;
        }
        return Profile.JAVA_EE_5;
    }
    
    @Override
    public String getModuleVersion() {
        DDProvider prov = DDProvider.getDefault();
        FileObject dd = getDeploymentDescriptor();
        if (dd != null) {
            try {
                EjbJar ejb = prov.getDDRoot(dd);
                String ejbVersion = ejb.getVersion().toString();
                return ejbVersion;
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
        }
        String version = PluginPropertyUtils.getPluginProperty(project,
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_EJB,
                "ejbVersion", "ejb", "ejb.ejbVersion"); //NOI18N
        if (version != null) {
            return version.trim();
        }
        // in case there is no descriptor, we probably have 3.x spec stuff?
        //TODO we cannot differenciate ee5 and ee6 at this point, most cases shall
        // be coved by the previous cases
       return EjbJar.VERSION_3_0;
    }

    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == EjbJarMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        } else if (type == WebservicesMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getWebservicesMetadataModel();
            return model;
        }
        return null;
    }

    @Override
    public synchronized MetadataModel<EjbJarMetadata> getMetadataModel() {
        if (ejbJarMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            ejbJarMetadataModel = EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return ejbJarMetadataModel;
    }
    
    private synchronized MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        if (webservicesMetadataModel == null) {
            FileObject ddFO = getWebServicesDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            webservicesMetadataModel = WebservicesMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return webservicesMetadataModel;
    }

    private FileObject getWebServicesDeploymentDescriptor() {
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            return metaInf.getFileObject("webservices.xml"); //NOI18N
        }
        return null;
    }
}
