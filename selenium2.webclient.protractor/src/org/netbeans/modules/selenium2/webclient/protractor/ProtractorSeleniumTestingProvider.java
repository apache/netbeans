/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.protractor;

import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders;
import org.netbeans.modules.selenium2.webclient.protractor.preferences.ProtractorPreferences;
import org.netbeans.modules.selenium2.webclient.spi.SeleniumTestingProviderImplementation;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service = SeleniumTestingProviderImplementation.class, path = SeleniumTestingProviders.SELENIUM_TESTING_PATH, position = 10)
public class ProtractorSeleniumTestingProvider implements SeleniumTestingProviderImplementation {
    
    private static final Logger LOGGER = Logger.getLogger(ProtractorSeleniumTestingProvider.class.getName());

    @Override
    public String getIdentifier() {
        return CustomizerProtractorPanel.IDENTIFIER;
    }

    @Override
    public String getDisplayName() {
        return Bundle.CustomizerProtractorPanel_displayName();
    }

    @Override
    public boolean isEnabled(Project project) {
        return ProtractorPreferences.isEnabled(project);
    }

    @Override
    public boolean isCoverageSupported(Project project) {
        return false;
    }

    @Override
    public CustomizerPanelImplementation createCustomizerPanel(Project project) {
        return new CustomizerProtractorPanel(project);
    }

    @Override
    public void notifyEnabled(Project project, boolean enabled) {
        ProtractorPreferences.setEnabled(project, enabled);
    }

    @Override
    public void projectOpened(Project project) {
        // noop
    }

    @Override
    public void projectClosed(Project project) {
        // noop
    }

    @Override
    public void runTests(FileObject[] activatedFOs) {
        ProtractorRunner.runTests(activatedFOs);
    }

    @Override
    public void debugTests(FileObject[] activatedFOs) {
        ProtractorRunner.debugTests(activatedFOs);
    }
    
}
