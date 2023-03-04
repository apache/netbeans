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
package org.netbeans.modules.web.clientproject.ant;

import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.ClientSideProjectSources;
import org.netbeans.modules.web.clientproject.ClientSideProjectType;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.env.Env;
import org.netbeans.modules.web.clientproject.env.Licenses;
import org.netbeans.modules.web.clientproject.env.Values;
import org.netbeans.modules.web.clientproject.env.References;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Bridges calls into Ant.
 */
public final class AntServices extends Env {
    private AntServices() {
    }
    
    @Override
    public CommonProjectHelper createProject(FileObject dirFO, String type) throws IOException {
        return new AntProjectHelperImpl(ProjectGenerator.createProject(dirFO, type));
    }

    @Override
    public String getUsablePropertyName(String displayName) {
        return PropertyUtils.getUsablePropertyName(displayName);
    }

    @Override
    public File resolveFile(File dir, String relative) {
        return PropertyUtils.resolveFile(dir, relative);
    }

    @Override
    public Values createEvaluator(CommonProjectHelper aph, FileObject dir) {
        AntProjectHelperImpl impl = (AntProjectHelperImpl) aph;
        org.netbeans.spi.project.support.ant.AntProjectHelper h = impl.delegate;
        
        org.netbeans.spi.project.support.ant.PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                h.getStockPropertyPreprovider(),
                h.getPropertyProvider(org.netbeans.spi.project.support.ant.AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        org.netbeans.spi.project.support.ant.PropertyEvaluator pe = PropertyUtils.sequentialPropertyEvaluator(
                h.getStockPropertyPreprovider(),
                h.getPropertyProvider(org.netbeans.spi.project.support.ant.AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                PropertyUtils.userPropertiesProvider(baseEval2,
                        "user.properties.file", FileUtil.toFile(dir)), // NOI18N
                h.getPropertyProvider(org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_PROPERTIES_PATH));
        
        return new PropertyEvaluatorImpl(pe);
    }

    @Override
    public String relativizeFile(File base, File relative) {
        return PropertyUtils.relativizeFile(base, relative);
    }

    @Override
    public Sources initSources(Project project, CommonProjectHelper h, Values e) {
        AntProjectHelperImpl ih = (AntProjectHelperImpl) h;
        PropertyEvaluatorImpl ip = (PropertyEvaluatorImpl) e;
        SourcesHelper sourcesHelper = new SourcesHelper(project, ih.delegate, ip.delegate);
        sourcesHelper.sourceRoot("${" + ClientSideProjectConstants.PROJECT_SOURCE_FOLDER + "}") //NOI18N
                .displayName(org.openide.util.NbBundle.getMessage(ClientSideProjectSources.class, "SOURCES"))
                .add() // adding as principal root, continuing configuration
                .type(WebClientProjectConstants.SOURCES_TYPE_HTML5).add(); // adding as typed root
        sourcesHelper.sourceRoot("${" + ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER + "}") //NOI18N
                .displayName(org.openide.util.NbBundle.getMessage(ClientSideProjectSources.class, "SITE_ROOT"))
                .add() // adding as principal root, continuing configuration
                .type(WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT).add(); // adding as typed root
        sourcesHelper.sourceRoot("${" + ClientSideProjectConstants.PROJECT_TEST_FOLDER + "}") //NOI18N
                .displayName(org.openide.util.NbBundle.getMessage(ClientSideProjectSources.class, "UNIT_TESTS"))
                .add() // adding as principal root, continuing configuration
                .type(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST).add(); // adding as typed root
        sourcesHelper.sourceRoot("${" + ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER + "}") //NOI18N
                .displayName(org.openide.util.NbBundle.getMessage(ClientSideProjectSources.class, "SELENIUM_TESTS"))
                .add() // adding as principal root, continuing configuration
                .type(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST_SELENIUM).add(); // adding as typed root
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        return sourcesHelper.createSources();
    }

    

    @Override
    public References newReferenceHelper(CommonProjectHelper helper, AuxiliaryConfiguration configuration, Values eval) {
        AntProjectHelperImpl ih = (AntProjectHelperImpl) helper;
        PropertyEvaluatorImpl ip = (PropertyEvaluatorImpl) eval;
        org.netbeans.spi.project.support.ant.ReferenceHelper orig;
        orig = new org.netbeans.spi.project.support.ant.ReferenceHelper(
                ih.delegate, configuration, ip.delegate
        );
        return new ReferenceHelperImpl(orig);
    }

    @Override
    public JComponent createLicenseHeaderCustomizerPanel(ProjectCustomizer.Category category, Licenses licenseSupport) {
        LicensePanelSupportImpl li = (LicensePanelSupportImpl) licenseSupport;
        return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, li);
    }

    @Override
    public Licenses newLicensePanelSupport(
        Values evaluator, CommonProjectHelper projectHelper, String p1, String p2
    ) {
        return new LicensePanelSupportImpl(evaluator, projectHelper, p1, p2);
    }
    
    public static Env newServices() {
        return new AntServices();
    }
    
    @AntBasedProjectRegistration(
        type=ClientSideProjectType.TYPE,
        iconResource=ClientSideProject.HTML5_PROJECT_ICON,
        sharedNamespace=ClientSideProjectType.PROJECT_CONFIGURATION_NAMESPACE,
        privateNamespace=ClientSideProjectType.PRIVATE_CONFIGURATION_NAMESPACE
    )
    public static ClientSideProject factory(org.netbeans.spi.project.support.ant.AntProjectHelper helper) {
        return new ClientSideProject(
            new AntProjectHelperImpl(helper), 
            newServices()
        );
    }
}
