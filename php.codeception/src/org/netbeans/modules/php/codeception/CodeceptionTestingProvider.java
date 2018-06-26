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
package org.netbeans.modules.php.codeception;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.codeception.coverage.CoverageProvider;
import org.netbeans.modules.php.codeception.create.TestCreator;
import org.netbeans.modules.php.codeception.locate.CodeceptionTestLocator;
import org.netbeans.modules.php.codeception.run.TestRunner;
import org.netbeans.modules.php.codeception.ui.customizer.CodeceptionCustomizer;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Testing provider for Codeception.
 */
public final class CodeceptionTestingProvider implements PhpTestingProvider {

    public static final String IDENTIFIER = "Codeception"; // NOI18N

    private static final CodeceptionTestingProvider INSTANCE = new CodeceptionTestingProvider();


    @PhpTestingProvider.Registration(position = 400)
    public static CodeceptionTestingProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @NbBundle.Messages("CodeceptionTestingProvider.displayName=Codeception")
    @Override
    public String getDisplayName() {
        return Bundle.CodeceptionTestingProvider_displayName();
    }

    @Override
    public boolean isTestFile(PhpModule phpModule, FileObject fileObject) {
        if (!Codecept.isCodeceptionTestFile(fileObject.getNameExt())) {
            return false;
        }
        for (FileObject testDirectory : phpModule.getTestDirectories()) {
            if (FileUtil.isParentOf(testDirectory, fileObject)) {
                return true;
            }
        }
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        return sourceDirectory != null
                && FileUtil.isParentOf(sourceDirectory, fileObject);
    }

    @Override
    public boolean isTestCase(PhpModule phpModule, PhpType.Method method) {
        boolean isTest = Codecept.isTestClass(method.getPhpType().getName());
        if (!isTest
                && !Codecept.isCestClass(method.getPhpType().getName())) {
            return false;
        }
        return Codecept.isTestMethod(method.getName(), isTest);
    }

    @Override
    public void runTests(PhpModule phpModule, TestRunInfo runInfo, TestSession testSession) throws TestRunException {
        new TestRunner(phpModule).runTests(runInfo, testSession);
        if (runInfo.isCoverageEnabled()) {
            testSession.setCoverage(new CoverageProvider().getCoverage());
        }
    }

    @Override
    public TestLocator getTestLocator(PhpModule phpModule) {
        return new CodeceptionTestLocator(phpModule);
    }

    @Override
    public CreateTestsResult createTests(PhpModule phpModule, List<FileObject> files, Map<String, Object> configurationPanelProperties) {
        return new TestCreator(phpModule).createTests(files, configurationPanelProperties);
    }

    @Override
    public boolean isCoverageSupported(PhpModule phpModule) {
        return true;
    }

    @Override
    public Locations.Line parseFileFromOutput(String line) {
        Matcher matcher = Codecept.LINE_PATTERN.matcher(line);
        if (matcher.matches()) {
            File file = new File(matcher.group(1));
            if (file.isFile()) {
                FileObject fo = FileUtil.toFileObject(file);
                assert fo != null;
                return new Locations.Line(fo, Integer.parseInt(matcher.group(2)));
            }
        }
        return null;
    }

    @Override
    public ProjectCustomizer.CompositeCategoryProvider createCustomizer(PhpModule phpModule) {
        return new CodeceptionCustomizer(phpModule);
    }

}
