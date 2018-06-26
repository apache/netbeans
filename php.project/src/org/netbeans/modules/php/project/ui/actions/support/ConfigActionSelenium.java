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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.support;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.testrunner.UnitTestRunner;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Action implementation for SELENIUM TEST configuration.
 * It means running and debugging Selenium tests.
 * @author Tomas Mysik
 */
class ConfigActionSelenium extends ConfigActionTest {

    private static final String PHP_UNIT_IDENT = "PhpUnit"; // NOI18N


    protected ConfigActionSelenium(PhpProject project) {
        super(project);
    }

    @Override
    protected List<FileObject> getTestDirectories(boolean showCustomizer) {
        FileObject seleniumDirectory = ProjectPropertiesSupport.getSeleniumDirectory(project, showCustomizer);
        if (seleniumDirectory == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(seleniumDirectory);
    }

    @Override
    protected void runJsTests() {
        // noop
    }

    @Override
    void run(TestRunInfo testRunInfo) {
        PhpTestingProvider phpUnit = findPhpUnit();
        if (phpUnit == null) {
            informUser();
            return;
        }
        new UnitTestRunner(project, testRunInfo, new ConfigActionTest.RerunUnitTestHandler(testRunInfo), Collections.singletonList(phpUnit))
                .run();
    }

    @CheckForNull
    private PhpTestingProvider findPhpUnit() {
        for (PhpTestingProvider provider : PhpTesting.getTestingProviders()) {
            if (PHP_UNIT_IDENT.equals(provider.getIdentifier())) {
                return provider;
            }
        }
        return null;
    }

    @NbBundle.Messages("ConfigActionSelenium.phpunit.missing=PHPUnit support is not installed (use Tools > Plugins).")
    private void informUser() {
        DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(Bundle.ConfigActionSelenium_phpunit_missing(), NotifyDescriptor.INFORMATION_MESSAGE));
    }

}
