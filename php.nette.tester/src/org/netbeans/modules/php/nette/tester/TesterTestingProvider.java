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
package org.netbeans.modules.php.nette.tester;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.php.api.editor.PhpType.Method;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.nette.tester.commands.Tester;
import org.netbeans.modules.php.nette.tester.coverage.CoverageProvider;
import org.netbeans.modules.php.nette.tester.create.TestCreator;
import org.netbeans.modules.php.nette.tester.locate.TesterTestLocator;
import org.netbeans.modules.php.nette.tester.run.TapParser;
import org.netbeans.modules.php.nette.tester.run.TestRunner;
import org.netbeans.modules.php.nette.tester.ui.customizer.TesterCustomizer;
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
import org.openide.util.Pair;

public final class TesterTestingProvider implements PhpTestingProvider {

    public static final String IDENTIFIER = "Nette Tester"; // NOI18N


    private static final TesterTestingProvider INSTANCE = new TesterTestingProvider();


    private TesterTestingProvider() {
    }

    @PhpTestingProvider.Registration(position = 300)
    public static TesterTestingProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @NbBundle.Messages("TesterTestingProvider.name=Nette Tester")
    @Override
    public String getDisplayName() {
        return Bundle.TesterTestingProvider_name();
    }

    @Override
    public boolean isTestFile(PhpModule phpModule, FileObject fileObj) {
        if (!FileUtils.isPhpFile(fileObj)) {
            return false;
        }
        for (FileObject testDirectory : phpModule.getTestDirectories()) {
            if (FileUtil.isParentOf(testDirectory, fileObj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isTestCase(PhpModule phpModule, Method method) {
        return Tester.isTestMethod(method);
    }

    @Override
    public void runTests(PhpModule phpModule, TestRunInfo runInfo, TestSession testSession) throws TestRunException {
        new TestRunner(phpModule).runTests(runInfo, testSession);
        if (runInfo.isCoverageEnabled()) {
            testSession.setCoverage(new CoverageProvider(phpModule).getCoverage(runInfo));
        }
    }

    @Override
    public TestLocator getTestLocator(PhpModule phpModule) {
        return new TesterTestLocator(phpModule);
    }

    @Override
    public CreateTestsResult createTests(PhpModule phpModule, List<FileObject> files, Map<String, Object> configurationPanelProperties) {
        return new TestCreator(phpModule).createTests(files);
    }

    @Override
    public boolean isCoverageSupported(PhpModule phpModule) {
        return true;
    }

    @Override
    public Locations.Line parseFileFromOutput(String line) {
        Pair<String, Integer> fileLine = TapParser.getFileLine(line);
        if (fileLine != null) {
            File file = new File(fileLine.first());
            if (file.isFile()) {
                FileObject fo = FileUtil.toFileObject(file);
                assert fo != null;
                return new Locations.Line(fo, fileLine.second());
            }
        }
        return null;
    }

    @Override
    public ProjectCustomizer.CompositeCategoryProvider createCustomizer(PhpModule phpModule) {
        return new TesterCustomizer(phpModule);
    }

}
