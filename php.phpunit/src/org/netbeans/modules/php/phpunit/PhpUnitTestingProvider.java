/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.phpunit;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.phpunit.commands.PhpUnit;
import org.netbeans.modules.php.phpunit.coverage.CoverageProvider;
import org.netbeans.modules.php.phpunit.create.TestCreator;
import org.netbeans.modules.php.phpunit.locate.PhpUnitTestLocator;
import org.netbeans.modules.php.phpunit.preferences.PhpUnitPreferences;
import org.netbeans.modules.php.phpunit.run.TestRunner;
import org.netbeans.modules.php.phpunit.ui.customizer.PhpUnitCustomizer;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Testing provider for PhpUnit.
 */
public final class PhpUnitTestingProvider implements PhpTestingProvider {

    public static final String IDENTIFIER = "PhpUnit"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(PhpUnitTestingProvider.class.getName());
    private static final PhpUnitTestingProvider INSTANCE = new PhpUnitTestingProvider();


    @PhpTestingProvider.Registration(position=100)
    public static PhpUnitTestingProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @NbBundle.Messages("PhpUnitTestingProvider.name=PHPUnit")
    @Override
    public String getDisplayName() {
        return Bundle.PhpUnitTestingProvider_name();
    }

    @Override
    public boolean isTestFile(PhpModule phpModule, FileObject fileObj) {
        if (!PhpUnit.isTestFile(fileObj.getNameExt())) {
            return false;
        }
        for (FileObject testDirectory : phpModule.getTestDirectories()) {
            if (FileUtil.isParentOf(testDirectory, fileObj)) {
                return true;
            }
        }
        if (!PhpUnitPreferences.getRunAllTestFiles(phpModule)) {
            return false;
        }
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        return sourceDirectory != null
                && FileUtil.isParentOf(sourceDirectory, fileObj);
    }

    @Override
    public boolean isTestCase(PhpModule phpModule, PhpType.Method method) {
        if (!PhpUnit.isTestClass(method.getPhpType().getName())) {
            return false;
        }
        return PhpUnit.isTestMethod(method.getName());
    }

    @Override
    public CreateTestsResult createTests(PhpModule phpModule, List<FileObject> files, Map<String, Object> configurationPanelProperties) {
        return new TestCreator(phpModule).createTests(files);
    }

    @NbBundle.Messages({
        "PhpUnitTestingProvider.coverage.log.error.1=Coverage log not found!",
        "PhpUnitTestingProvider.coverage.log.error.2=Perhaps you need to add \"whitelist\" to your XML configuration?",
    })
    @Override
    public void runTests(PhpModule phpModule, TestRunInfo runInfo, TestSession testSession) throws TestRunException {
        new TestRunner(phpModule).runTests(runInfo, testSession);
        if (runInfo.isCoverageEnabled()) {
            CoverageProvider coverageProvider = new CoverageProvider();
            if (!coverageProvider.loggerFileExists()) {
                // perhaps PHPUnit 5?
                LOGGER.log(Level.INFO, "Coverage log {0} not found for project {1}", new Object[] {PhpUnit.COVERAGE_LOG, phpModule.getDisplayName()});
                testSession.printMessage(Bundle.PhpUnitTestingProvider_coverage_log_error_1(), true);
                testSession.printMessage(Bundle.PhpUnitTestingProvider_coverage_log_error_2(), true);
                testSession.printMessage("", false);
                testSession.setCoverage(null);
            } else {
                testSession.setCoverage(coverageProvider.getCoverage());
            }
        }
    }

    @Override
    public TestLocator getTestLocator(PhpModule phpModule) {
        return new PhpUnitTestLocator(phpModule);
    }

    @Override
    public boolean isCoverageSupported(PhpModule phpModule) {
        return true;
    }

    @Override
    public Locations.Line parseFileFromOutput(String line) {
        Pattern[] patterns = new Pattern[] {
            PhpUnit.OUT_LINE_PATTERN,
            PhpUnit.ERR_LINE_PATTERN,
        };
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                File file = new File(matcher.group(1));
                if (file.isFile()) {
                    FileObject fo = FileUtil.toFileObject(file);
                    assert fo != null;
                    return new Locations.Line(fo, Integer.parseInt(matcher.group(2)));
                }
            }
        }
        return null;
    }

    @Override
    public ProjectCustomizer.CompositeCategoryProvider createCustomizer(PhpModule phpModule) {
        return new PhpUnitCustomizer(phpModule);
    }

}
