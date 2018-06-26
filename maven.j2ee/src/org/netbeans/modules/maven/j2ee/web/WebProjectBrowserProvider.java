/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import static org.netbeans.modules.web.browser.spi.ProjectBrowserProvider.PROP_BROWSER_ACTIVE;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service = {
        ProjectBrowserProvider.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR
    }
)
public class WebProjectBrowserProvider implements ProjectBrowserProvider {

    private final Map<PropertyChangeListener, PreferenceChangeListener> mapper;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Project project;
    private Preferences preferences;

    
    public WebProjectBrowserProvider(final Project project) {
        this.project = project;
        this.mapper = new HashMap<PropertyChangeListener, PreferenceChangeListener>();
    }
    
    private Preferences getPreferences() {
        if (preferences == null) {
            preferences = ProjectUtils.getPreferences(project, MavenProjectSupport.class, false);
            preferences.addPreferenceChangeListener(new PreferenceChangeListener() {

                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if (MavenJavaEEConstants.SELECTED_BROWSER.equals(evt.getKey())) {
                        pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
                    }
                }
            });
        }
        return preferences;
    }

    @Override
    public Collection<WebBrowser> getBrowsers() {
        return WebBrowsers.getInstance().getAll(false, true, true);
    }

    @Override
    public WebBrowser getActiveBrowser() {
        String selectedBrowser = JavaEEProjectSettings.getBrowserID(project);
        if (selectedBrowser == null) {
            return null;
        } else {
            return BrowserUISupport.getBrowser(selectedBrowser);
        }
    }

    @Override
    public void setActiveBrowser(final WebBrowser browser) throws IllegalArgumentException, IOException {
        ProjectManager.mutex().writeAccess(new Runnable() {

            @Override
            public void run() {
                JavaEEProjectSettings.setBrowserID(project, browser.getId());
            }
        });
        pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        project.getLookup().lookup(CustomizerProvider2.class).showCustomizer("run", null);
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener propertyListener) {
        PreferenceChangeListener preferencesListener = new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                propertyListener.propertyChange(new PropertyChangeEvent(evt.getNode(), evt.getKey(), null, evt.getNewValue()));
            }
        };
        
        pcs.addPropertyChangeListener(propertyListener);
        getPreferences().addPreferenceChangeListener(preferencesListener);
        
        mapper.put(propertyListener, preferencesListener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyListener) {
        PreferenceChangeListener preferencesListener = mapper.get(propertyListener);
        
        pcs.removePropertyChangeListener(propertyListener);
        getPreferences().removePreferenceChangeListener(preferencesListener);
        
        mapper.remove(propertyListener);
    }
}
