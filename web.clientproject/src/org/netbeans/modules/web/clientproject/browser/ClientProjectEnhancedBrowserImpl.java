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

package org.netbeans.modules.web.clientproject.browser;

import java.util.EnumSet;
import javax.swing.JPanel;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.modules.web.clientproject.ui.browser.BrowserConfigurationPanel;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.EditableProperties;

public class ClientProjectEnhancedBrowserImpl implements ClientProjectEnhancedBrowserImplementation {

    
    final private ClientSideProject project;
    final private WebBrowser browser;
    private BrowserSupport browserSupport;
    private ProjectConfigurationCustomizerImpl cust = null;

    public ClientProjectEnhancedBrowserImpl(ClientSideProject project, WebBrowser browser) {
        this.project = project;
        this.browser = browser;
    }

    @Override
    public void save() {
        if (cust != null && cust.hasDataToSave()) {
            EditableProperties p = project.getProjectHelper().getProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH);
            p.put(ClientSideProjectConstants.PROJECT_AUTO_REFRESH+"."+browser.getId(), Boolean.toString(cust.panel.isAutoRefresh())); //NOI18N
            p.put(ClientSideProjectConstants.PROJECT_HIGHLIGHT_SELECTION+"."+browser.getId(), Boolean.toString(cust.panel.isHighlightSelection())); //NOI18N
            project.getProjectHelper().putProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH, p);
        }
    }

    @Override
    public boolean isAutoRefresh() {
        String val = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_AUTO_REFRESH+"."+browser.getId()); //NOI18N
        if (val != null) {
            return Boolean.parseBoolean(val);
        } else {
            // return true for all browsers so that plain Chrome can do Refresh
            // on Save if plugin is intalled:
            return true;
        }
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        String val = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_HIGHLIGHT_SELECTION+"."+browser.getId()); //NOI18N
        if (val != null) {
            return Boolean.parseBoolean(val);
        } else {
            return true;
        }
    }

    @Override
    public RefreshOnSaveListener getRefreshOnSaveListener() {
        return new RefreshOnSaveListenerImpl(project, getBrowserSupport(), this);
    }

    @Override
    public ActionProvider getActionProvider() {
        return new BrowserActionProvider(project, getBrowserSupport(), this);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer() {
        if (cust == null) {
            cust = new ProjectConfigurationCustomizerImpl();
        }
        return cust;
    }
    
    public BrowserSupport getBrowserSupport() {
        if (browserSupport == null) {
            if (browser.isEmbedded()) {
                browserSupport = BrowserSupport.getDefaultEmbedded();
            } else {
                browserSupport = BrowserSupport.create(browser);
            }
        }
        return browserSupport;
    }

    @Override
    public void deactivate() {
        if (browserSupport != null) {
            getBrowserSupport().close(false);
        }
    }

    @Override
    public void close() {
        if (browserSupport != null) {
            getBrowserSupport().close(true);
        }
    }

    private class ProjectConfigurationCustomizerImpl implements ProjectConfigurationCustomizer {

        private BrowserConfigurationPanel panel;
        
        @Override
        public JPanel createPanel() {
            panel = new BrowserConfigurationPanel(project, 
                    ClientProjectEnhancedBrowserImpl.this, ClientProjectEnhancedBrowserImpl.this.browser);
            return panel;
        }

        @Override
        public EnumSet<HiddenProperties> getHiddenProperties() {
            return EnumSet.noneOf(HiddenProperties.class);
        }

        boolean hasDataToSave() {
            return browser.hasNetBeansIntegration() || browser.getBrowserFamily() == BrowserFamilyId.CHROME;
        }

    }
    
}
