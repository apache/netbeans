/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cordova.platforms;

import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.spi.project.ActionProvider;

/**
 * @author Jan Becicka
 */
public final class EnhancedBrowserImpl implements ClientProjectEnhancedBrowserImplementation {

    final private Project project;
    private final WebBrowser browser;
    protected BrowserSupport browserSupport;
    private static final Logger LOGGER = Logger.getLogger(EnhancedBrowserImpl.class.getName());
    private BrowserCustomizer browserCustomizer;
    private static final String PROJECT_AUTO_REFRESH = "browser.autorefresh"; //NOI18N
    private static final String PROJECT_HIGHLIGHT_SELECTION = "browser.highlightselection"; //NOI18N
    private final ActionProvider actionProvider;
        

    public EnhancedBrowserImpl(Project project, WebBrowser browser, BrowserSupport support, ActionProvider actionProvider ) {
        this.project = project;
        this.browser = browser;
        this.browserSupport = support;
        this.actionProvider = actionProvider;
    }

    @Override
    public void save() {
        if (browserCustomizer != null && browser.hasNetBeansIntegration()) {
            Preferences p = ProjectUtils.getPreferences(project, EnhancedBrowserImpl.class, false);
            p.put(PROJECT_AUTO_REFRESH+"."+browser.getId(), Boolean.toString(browserCustomizer.isAutoRefresh())); //NOI18N
            p.put(PROJECT_HIGHLIGHT_SELECTION+"."+browser.getId(), Boolean.toString(browserCustomizer.isHighlightSelection())); //NOI18N
        }
    }

    @Override
    public RefreshOnSaveListener getRefreshOnSaveListener() {
        return new RefreshOnSaveListenerImpl(project, browserSupport, this);
    }


    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer() {
        if (browserCustomizer == null) {
            browserCustomizer = new BrowserCustomizer(project, this, browser);
        }
        return browserCustomizer;
    }

    @Override
    public void deactivate() {
        browserSupport.close(false);
    }

    @Override
    public void close() {
        browserSupport.close(true);
    }

    @Override
    public boolean isAutoRefresh() {
        Preferences p = ProjectUtils.getPreferences(project, EnhancedBrowserImpl.class, false);
        return p.getBoolean(PROJECT_AUTO_REFRESH+"."+browser.getId(), true); //NOI18N
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        Preferences p = ProjectUtils.getPreferences(project, EnhancedBrowserImpl.class, false);
        return p.getBoolean(PROJECT_HIGHLIGHT_SELECTION+"."+browser.getId(), browser.hasNetBeansIntegration()); //NOI18N
    }
    
    @Override
    public ActionProvider getActionProvider() {
        return actionProvider;
    }

}
