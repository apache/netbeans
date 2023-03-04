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
package org.netbeans.modules.web.clientproject.sites;

import org.netbeans.modules.web.clientproject.api.sites.SiteHelper;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


public abstract class OnlineSites implements SiteTemplateImplementation {

    private static final Logger LOGGER = Logger.getLogger(OnlineSites.class.getName());

    private final String name;
    private final String url;
    private final File libFile;
    private final String description;
    private final String id;



    protected OnlineSites(String id, String name, String description, String url, File libFile) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.libFile = libFile;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isPrepared() {
        return libFile.isFile();
    }

    @Override
    public void prepare() throws IOException {
        assert !EventQueue.isDispatchThread();
        assert !isPrepared();
        SiteHelper.download(url, libFile, null);
    }

    @Override
    public void configure(ProjectProperties projectProperties) {
        // noop by default
    }

    @Override
    public final void apply(FileObject projectDir, ProjectProperties projectProperties, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        if (!isPrepared()) {
            // not correctly prepared, user has to know about it already
            LOGGER.info("Template not correctly prepared, nothing to be applied"); //NOI18N
            return;
        }
        SiteHelper.unzipProjectTemplate(getTargetDir(projectDir, projectProperties), libFile, handle);
    }

    @Override
    public void cleanup() {
        if (libFile.isFile()) {
            if (!libFile.delete()) {
                libFile.deleteOnExit();
            }
        }
    }

    protected FileObject getTargetDir(FileObject projectDir, ProjectProperties projectProperties) {
        // by default, extract template to site root
        String siteRootFolder = projectProperties.getSiteRootFolder();
        assert siteRootFolder != null;
        return projectDir.getFileObject(siteRootFolder);
    }

    //~ Inner classes

    @ServiceProvider(service = SiteTemplateImplementation.class, position = 150)
    public static class SiteAngularJsSeed extends OnlineSites {

        private static final String SITE_ROOT_FOLDER = "app"; // NOI18N
        private static final String TEST_FOLDER = "test"; // NOI18N
        private static final String TEST_SELENIUM_FOLDER = "e2e-tests"; // NOI18N
        private static final String JS_TESTING_PROVIDER = "Karma"; // NOI18N
        private static final String SELENIUM_TESTING_PROVIDER = "Protractor"; // NOI18N


        @NbBundle.Messages({
            "SiteAngularJsSeed.name=AngularJS Seed",
            "SiteAngularJsSeed.description=Site template for AngularJS projects.\n\n"
                    + "Once created, run \"npm install\" to install dependencies.",
        })
        public SiteAngularJsSeed() {
            super("ANGULAR", Bundle.SiteAngularJsSeed_name(), Bundle.SiteAngularJsSeed_description(), // NOI18N
                    "https://github.com/angular/angular-seed/archive/master.zip", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "angularjs-seed.zip")); // NOI18N
        }

        @Override
        public void configure(ProjectProperties projectProperties) {
            projectProperties.setSiteRootFolder(SITE_ROOT_FOLDER)
                    .setTestFolder(TEST_FOLDER)
                    .setTestSeleniumFolder(TEST_SELENIUM_FOLDER)
                    .setJsTestingProvider(JS_TESTING_PROVIDER)
                    .setSeleniumTestingProvider(SELENIUM_TESTING_PROVIDER);
        }

        @Override
        protected FileObject getTargetDir(FileObject projectDir, ProjectProperties projectProperties) {
            return projectDir;
        }

    }

    @NbBundle.Messages("SiteInitializr.description=Site template from initializr.com.")
    @ServiceProvider(service = SiteTemplateImplementation.class, position = 200)
    public static class BootstrapSiteInitializr extends OnlineSites {

        @NbBundle.Messages("BootstrapSiteInitializr.name=Initializr: Bootstrap")
        public BootstrapSiteInitializr() {
            super("BOOTSTRAP", Bundle.BootstrapSiteInitializr_name(), // NOI18N
                    Bundle.SiteInitializr_description(),
                    "http://www.initializr.com/builder?boot-hero&jquerymin&h5bp-iecond&h5bp-chromeframe&h5bp-analytics&h5bp-favicon&h5bp-appletouchicons&modernizrrespond&izr-emptyscript&boot-css&boot-scripts", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "initializr-bootstrap-latest.zip")); // NOI18N
        }

    }

    @ServiceProvider(service = SiteTemplateImplementation.class, position = 210)
    public static class ClassicSiteInitializr extends OnlineSites {

        @NbBundle.Messages("ClassicSiteInitializr.name=Initializr: Classic")
        public ClassicSiteInitializr() {
            super("INIT.CLASSIC", Bundle.ClassicSiteInitializr_name(), // NOI18N
                Bundle.SiteInitializr_description(),
                "http://www.initializr.com/builder?h5bp-content&modernizr&jquerymin&h5bp-chromeframe&h5bp-analytics&h5bp-htaccess&h5bp-favicon&h5bp-appletouchicons&h5bp-scripts&h5bp-robots&h5bp-humans&h5bp-404&h5bp-adobecrossdomain&h5bp-css&h5bp-csshelpers&h5bp-mediaqueryprint&h5bp-mediaqueries&simplehtmltag", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "initializr-classic-latest.zip")); // NOI18N
        }

    }

    @ServiceProvider(service = SiteTemplateImplementation.class, position = 220)
    public static class ResponsiveSiteInitializr extends OnlineSites {

        @NbBundle.Messages("ResponsiveSiteInitializr.name=Initializr: Responsive")
        public ResponsiveSiteInitializr() {
            super("INIT.RESP", Bundle.ResponsiveSiteInitializr_name(), // NOI18N
                    Bundle.SiteInitializr_description(),
                    "http://www.initializr.com/builder?izr-responsive&jquerymin&h5bp-iecond&h5bp-chromeframe&h5bp-analytics&h5bp-favicon&h5bp-appletouchicons&modernizrrespond&h5bp-css&h5bp-csshelpers&h5bp-mediaqueryprint&izr-emptyscript", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "initializr-responsive-latest.zip")); // NOI18N
        }

    }

    @ServiceProvider(service = SiteTemplateImplementation.class, position = 300)
    public static class SiteHtml5Boilerplate extends OnlineSites {

        @NbBundle.Messages({
            "SiteHtml5Boilerplate.name=HTML5 Boilerplate 5.3.0",
            "SiteHtml5Boilerplate.description=Site template from html5boilerplate.com.",
        })
        public SiteHtml5Boilerplate() {
            super("INIT.BOILER", Bundle.SiteHtml5Boilerplate_name(), Bundle.SiteHtml5Boilerplate_description(), // NOI18N
                    "https://github.com/h5bp/html5-boilerplate/releases/download/5.3.0/html5-boilerplate_v5.3.0.zip", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "html5-boilerplate-530.zip")); // NOI18N
        }

    }

    @ServiceProvider(service = SiteTemplateImplementation.class, position = 400)
    public static class SiteTwitterBootstrap extends OnlineSites {

        @NbBundle.Messages({
            "SiteTwitterBootstrap.name=Twitter Bootstrap 3.3.6",
            "SiteTwitterBootstrap.description=Site template from getbootstrap.com.",
        })
        public SiteTwitterBootstrap() {
            super("TWITTER", Bundle.SiteTwitterBootstrap_name(), Bundle.SiteTwitterBootstrap_description(), // NOI18N
                    "https://github.com/twbs/bootstrap/releases/download/v3.3.6/bootstrap-3.3.6-dist.zip", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "twitter-bootstrap-336.zip")); // NOI18N
        }

    }

    @ServiceProvider(service = SiteTemplateImplementation.class, position = 500)
    public static class SiteMobileBoilerplate extends OnlineSites {

        @NbBundle.Messages({
            "SiteMobileBoilerplate.name=Mobile Boilerplate 4.1.2",
            "SiteMobileBoilerplate.description=Site template from html5boilerplate.com/mobile.",
        })
        public SiteMobileBoilerplate() {
            super("INIT.BOILER.MOBILE", Bundle.SiteMobileBoilerplate_name(), Bundle.SiteMobileBoilerplate_description(), // NOI18N
                    "https://github.com/h5bp/mobile-boilerplate/zipball/v4.1.2", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "mobile-boilerplate-412.zip")); // NOI18N
        }

    }

}
