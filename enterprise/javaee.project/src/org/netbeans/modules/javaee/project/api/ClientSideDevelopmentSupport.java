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
package org.netbeans.modules.javaee.project.api;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.spi.FrameworkServerURLMapping;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.spi.PageInspectorCustomizer;
import org.netbeans.modules.web.browser.spi.URLDisplayerImplementation;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.spi.ServerURLMappingImplementation;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Janicek, David Konecny
 */
public final class ClientSideDevelopmentSupport implements
        ServerURLMappingImplementation,
        URLDisplayerImplementation,
        PageInspectorCustomizer {

    private final Project project;
    private Project webProject;
    private volatile String projectRootURL;
    private volatile FileObject webDocumentRoot;
    // @GuardedBy("this")
    private BrowserSupport browserSupport = null;
    // @GuardedBy("this")
    private boolean browserSupportInitialized = false;
    // @GuardedBy("this")
    private boolean initialized = false;
    private final UsageLogger browserUsageLogger;
    private final String projectType;
    
    public static ClientSideDevelopmentSupport createInstance(Project project, String projectType, String usageLoggerName) {
        return new ClientSideDevelopmentSupport(project, projectType, usageLoggerName);
    }
    
    private ClientSideDevelopmentSupport(Project project, String projectType, String usageLoggerName) {
        this.project = project;
        this.webProject = project;
        this.browserUsageLogger = UsageLogger.projectBrowserUsageLogger(usageLoggerName);
        this.projectType = projectType;
    }

    /**
     * This method should be called only from EAR project.
     */
    public synchronized void setWebProject(Project webProject) {
        this.webProject = webProject;
        webDocumentRoot = null;
    }

    private synchronized Project getWebProject() {
        return webProject;
    }

    @Override
    public void showURL(URL applicationRootURL, URL urlToOpenInBrowser, FileObject context) {
        projectRootURL = applicationRootURL == null ? null : WebUtils.urlToString(applicationRootURL);
        if (projectRootURL != null && !projectRootURL.contains(".") && !projectRootURL.endsWith("/")) {
            projectRootURL += "/";
        }
        if (project.getProjectDirectory().equals(context) && webProject != null) {
            // this is scenario of EAR project executing its Web Project; use
            // Web Project as context instead of EAR here:
            context = webProject.getProjectDirectory();
        }
        // let browser update URL if necessary:
        WebBrowser browser = getWebBrowser();
        if (browser != null) {
            urlToOpenInBrowser = browser.toBrowserURL(getWebProject(), context, urlToOpenInBrowser);
            browserUsageLogger.log(projectType, browser.getId(), browser.getBrowserFamily().name());
        } else {
            WebBrowser wb = BrowserUISupport.getDefaultBrowserChoice(true);
            browserUsageLogger.log(projectType, wb.getId(), wb.getBrowserFamily().name());
        }
        BrowserSupport bs = getBrowserSupport();
        if (bs != null) {
            bs.load(urlToOpenInBrowser, context);
        } else {
            HtmlBrowser.URLDisplayer.getDefault().showURL(urlToOpenInBrowser);
        }
    }

    @Override
    public URL toServer(int projectContext, FileObject projectFile) {
        init();
        if (projectRootURL == null || webDocumentRoot == null) {
            return null;
        }
        String relPath = FileUtil.getRelativePath(webDocumentRoot, projectFile);
        for (FrameworkServerURLMapping mapping : lookupFrameworkMappings()) {
            relPath = mapping.convertFileToRelativeURL(projectFile, relPath);
        }
        // #233748 - disable using Servlet URL mapping for now:
        // relPath = applyServletPattern(relPath);
        try {
            URL u = new URL(projectRootURL + relPath);
            WebBrowser browser = getWebBrowser();
            if (browser != null) {
                u = browser.toBrowserURL(getWebProject(), projectFile, u);
            }
            return u;
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public FileObject fromServer(int projectContext, URL serverURL) {
        String query = serverURL.getQuery();
        // #219339 - strip down query and/or fragment:
        serverURL = WebUtils.stringToUrl(WebUtils.urlToString(serverURL, true));
        if (serverURL == null) {
            return null;
        }

        init();
        if (projectRootURL == null || webDocumentRoot == null) {
            return null;
        }
        WebBrowser browser = getWebBrowser();
        if (browser != null) {
            serverURL = browser.fromBrowserURL(getWebProject(), serverURL);
        }
        String u = WebUtils.urlToString(serverURL);
        if (u.startsWith(projectRootURL)) {
            String name = u.substring(projectRootURL.length());
            if (name.isEmpty()) {
                // name is empty - try to map server URL to one of the welcome files:
                return getExistingWelcomeFile();
            } else {
                // use servlet mappings to map server URL to a project file:
                return convertServerURLToProjectFile(name, query);
            }
        }
        return null;
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // noop
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // noop
    }

    public void close() {
        BrowserSupport support = getBrowserSupport();
        if (support != null) {
            support.close(true);
        }
    }

    private void init() {
        if (webDocumentRoot == null) {
            webDocumentRoot = getWebRoot();
        }
        readWebAppMetamodelData();
    }

    private FileObject getWebRoot() {
        WebModule webModule = getWebModule();
        return webModule != null ? webModule.getDocumentBase() : null;
    }
    
    private WebModule getWebModule() {
        if (getWebProject() != null) {
            return WebModule.getWebModule(getWebProject().getProjectDirectory());
        }
        return null;
    }

    public boolean canReload() {
        BrowserSupport bs = getBrowserSupport();
        if (bs != null) {
            return bs.canReload();
        }
        return false;
    }

    public void reload(FileObject fo) {
        BrowserSupport bs = getBrowserSupport();
        if (bs == null) {
            return;
        }
        if (bs.ignoreChange(fo)) {
            return;
        }
        URL u = bs.getBrowserURL(fo, true);
        if (u == null) {
            // check if given file is one of the welcome files and therefore
            // project folder should be used for reload instead of welcome file:
            if (isWelcomeFile(fo)) {
                u = bs.getBrowserURL(project.getProjectDirectory(), true);
            }
        }
        if (u != null && bs.canReload(u)) {
            bs.reload(u);
        }
    }

    public synchronized void resetBrowserSupport() {
        if (browserSupport != null) {
            browserSupport.close(false);
        }
        browserSupport = null;
        browserSupportInitialized = false;
        browserUsageLogger.reset();
    }

    @CheckForNull private synchronized BrowserSupport getBrowserSupport() {
        if (browserSupportInitialized) {
            return browserSupport;
        }
        browserSupportInitialized = true;
        WebBrowser browser = getWebBrowser();
        if (browser == null) {
            browserSupport = null;
            return null;
        }
        browserSupport = BrowserSupport.create(browser);
        return browserSupport;
    }

    @CheckForNull private WebBrowser getWebBrowser() {
        String selectedBrowser = JavaEEProjectSettings.getBrowserID(project);
        if (selectedBrowser == null) {
            return null;
        }
        return BrowserUISupport.getBrowser(selectedBrowser);
    }

    private final List<Pattern> servletURLPatterns = new CopyOnWriteArrayList<>();
    private final List<String> welcomeFiles = new CopyOnWriteArrayList<>();

    private synchronized void readWebAppMetamodelData() {
        if (initialized) {
            return;
        }
        initialized = true;
        final WebModule webModule = getWebModule();
        if (webModule == null) {
            return;
        }
        try {
            webModule.getMetadataModel().runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
                
                @Override
                public Void run(WebAppMetadata metadata) throws Exception {
                    List<Pattern> l = new ArrayList<>();
                    for (ServletInfo si : metadata.getServlets()) {
                        for (String pattern : si.getUrlPatterns()) {
                            // only some patterns are currently handled;
                            // see comments in convertServerURLToProjectFile method
                            if (pattern.endsWith("*")) { // NOI18N
                                // /faces/*
                                String pat = pattern.substring(0, pattern.length() - 1);
                                l.add(new Pattern(Pattern.Type.PREFIX, pat.startsWith("/") ? pat.substring(1) : pat));
                            } else if (pattern.startsWith("*")) { //NOI18N
                                // *.xhtml
                                l.add(new Pattern(Pattern.Type.SUFFIX, pattern.substring(1)));
                            }
                        }
                    }
                    // WelcomeList file is not available in merged WebAppMetadata;
                    // below code will also ignore WelcomeList from web-fragment.xml which
                    // on the other hand should be OK most of the time - a framework/web library
                    // should not define what welcome files an application is going to have
                    FileObject fo = webModule.getDeploymentDescriptor();
                    if (fo != null) {
                        WebApp ddRoot = DDProvider.getDefault().getDDRoot(fo);
                        if (ddRoot != null) {
                            fo.addFileChangeListener(new FileChangeAdapter() {
                                @Override
                                public void fileChanged(FileEvent fe) {
                                    synchronized (ClientSideDevelopmentSupport.this) {
                                        initialized = false;
                                        welcomeFiles.clear();
                                        servletURLPatterns.clear();
                                    }
                                }
                            });
                            if (ddRoot.getSingleWelcomeFileList() != null) {
                                welcomeFiles.addAll(Arrays.asList(ddRoot.getSingleWelcomeFileList().getWelcomeFile()));
                            }
                        }
                    }
                    welcomeFiles.add("index.html"); // NOI18N
                    welcomeFiles.add("index.htm"); // NOI18N
                    welcomeFiles.add("index.jsp"); // NOI18N
                    servletURLPatterns.addAll(l);
                    return null;
                }
            });
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private FileObject getExistingWelcomeFile() {
        // try to map it to welcome-file-list:
        for (String welcomeFile : welcomeFiles) {
            for (Pattern pattern : servletURLPatterns) {
                if (welcomeFile.startsWith(pattern.getPattern())) {
                    FileObject fo = webDocumentRoot.getFileObject(welcomeFile.substring(pattern.getPattern().length()));
                    if (fo != null) {
                        return fo;
                    }
                }
            }
            FileObject fo = webDocumentRoot.getFileObject(welcomeFile);
            if (fo != null) {
                return fo;
            }
        }
        return null;
    }

    private FileObject convertServerURLToProjectFile(String name, String query) {
        // bellow code is limited to understand following simple usecases:
        // pattern "/faces/*" means that URL /faces/index.anything maps to
        // file web-root/index.anything and vice versa, pattern "*.xhtml"
        // means that URL /index.anything maps to file web-root/index.anything
        for (Pattern servletURLPattern : servletURLPatterns) {
            String pattern = servletURLPattern.getPattern();
            switch (servletURLPattern.getType()) {
                case PREFIX:
                    if (name.startsWith(pattern)) {
                        for (FrameworkServerURLMapping mapping : lookupFrameworkMappings()) {
                            FileObject file = mapping.convertURLtoFile(webDocumentRoot, servletURLPattern, name, query);
                            if (file != null) {
                                return file;
                            }
                        }
                        FileObject fo = webDocumentRoot.getFileObject(name.substring(pattern.length()));
                        if (fo != null) {
                            return fo;
                        }
                    }
                    break;
                case SUFFIX:
                    name = truncatePathForSessionId(name);
                    if (name.endsWith(pattern)) {
                        for (FrameworkServerURLMapping mapping : lookupFrameworkMappings()) {
                            FileObject file = mapping.convertURLtoFile(webDocumentRoot, servletURLPattern, name, query);
                            if (file != null) {
                                return file;
                            }
                        }
                    }
                    break;
            }
        }
        FileObject result = webDocumentRoot.getFileObject(name);
        if (result == null) {
            String tryName = null;
            if (name.endsWith(".jsf")) { // NOI18N
                tryName = name.substring(0, name.length()-3);
            }
            if (name.endsWith(".faces")) { // NOI18N
                tryName = name.substring(0, name.length()-5);
            }
            if (tryName != null) {
                result = webDocumentRoot.getFileObject(tryName + "xhtml"); // NOI18N
                if (result == null) {
                    result = webDocumentRoot.getFileObject(tryName + "jsp"); // NOI18N
                }
            }
        }
        return result;
    }

    private static Collection<? extends FrameworkServerURLMapping> lookupFrameworkMappings() {
        return Lookup.getDefault().lookupAll(FrameworkServerURLMapping.class);
    }

    private boolean isWelcomeFile(FileObject context) {
        for (String welcomeFile : welcomeFiles) {
            for (Pattern servletURLPattern : servletURLPatterns) {
                String pattern = servletURLPattern.getPattern();
                if (welcomeFile.startsWith(pattern)) {
                    FileObject fo = webDocumentRoot.getFileObject(welcomeFile.substring(pattern.length()));
                    if (fo != null && fo.equals(context)) {
                        return true;
                    }
                }
            }
            FileObject fo = webDocumentRoot.getFileObject(welcomeFile);
            if (fo != null && fo.equals(context)) {
                return true;
            }
        }
        return false;
    }

    // TODO: below code works well for JSF framework but could broke impl
    // of ServerURLMappingImplementation.toServer for a custom servlet; if
    // this turns to be a problem then readWebAppMetamodelData() should be
    // changed to read servlet URL patterns only from a well-known servlets
    // like JSF.
    private String applyServletPattern(String relPath) {
        for (Pattern pattern : servletURLPatterns) {
            return pattern.getPattern() + relPath;
        }
        return relPath;
    }

    private static String truncatePathForSessionId(String name) {
        int semicolonOffset = name.indexOf(";"); //NOI18N
        if (semicolonOffset == -1) {
            return name;
        } else {
            return name.substring(0, semicolonOffset);
        }
    }

    public static class Pattern {

        private final Type type;
        private final String pattern;

        public Pattern(Type type, String pattern) {
            this.type = type;
            this.pattern = pattern;
        }

        public Type getType() {
            return type;
        }

        public String getPattern() {
            return pattern;
        }

        public static enum Type {
            PREFIX,
            SUFFIX
        }
    }
}