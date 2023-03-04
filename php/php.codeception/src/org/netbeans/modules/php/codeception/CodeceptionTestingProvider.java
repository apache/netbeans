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
