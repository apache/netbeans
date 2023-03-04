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
