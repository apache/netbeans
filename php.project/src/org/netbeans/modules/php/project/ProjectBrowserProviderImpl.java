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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;

public final class ProjectBrowserProviderImpl implements ProjectBrowserProvider, PropertyChangeListener {

    private final PhpProject project;
    final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    private ProjectBrowserProviderImpl(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    public static ProjectBrowserProvider create(PhpProject project) {
        ProjectBrowserProviderImpl projectBrowserProviderImpl = new ProjectBrowserProviderImpl(project);
        project.getEvaluator().addPropertyChangeListener(projectBrowserProviderImpl);
        return projectBrowserProviderImpl;
    }

    @Override
    public Collection<WebBrowser> getBrowsers() {
        return WebBrowsers.getInstance().getAll(false, true, true);
    }

    @Override
    public WebBrowser getActiveBrowser() {
        String browserId = project.getEvaluator().getProperty(PhpProjectProperties.BROWSER_ID);
        if (browserId == null) {
            return BrowserUISupport.getDefaultBrowserChoice(true);
        }
        return BrowserUISupport.getBrowser(browserId);
    }

    @Override
    public void setActiveBrowser(WebBrowser browser) throws IOException {
        PhpProjectProperties.save(project, Collections.<String, String>emptyMap(), Collections.singletonMap(PhpProjectProperties.BROWSER_ID, browser.getId()));
        propertyChangeSupport.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        PhpProjectUtils.openCustomizer(project, CompositePanelProviderImpl.BROWSER);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        propertyChangeSupport.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        propertyChangeSupport.removePropertyChangeListener(lst);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PhpProjectProperties.BROWSER_ID.equals(evt.getPropertyName())) {
            propertyChangeSupport.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
        }
    }

}
