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
package org.netbeans.modules.php.atoum;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import org.netbeans.modules.php.api.editor.PhpType.Method;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.atoum.commands.Atoum;
import org.netbeans.modules.php.atoum.coverage.CoverageProvider;
import org.netbeans.modules.php.atoum.create.TestCreator;
import org.netbeans.modules.php.atoum.locate.AtoumTestLocator;
import org.netbeans.modules.php.atoum.run.TestRunner;
import org.netbeans.modules.php.atoum.ui.customizer.AtoumCustomizer;
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

public class AtoumTestingProvider implements PhpTestingProvider {

    public static final String IDENTIFIER = "atoum"; // NOI18N


    private static final AtoumTestingProvider INSTANCE = new AtoumTestingProvider();


    private AtoumTestingProvider() {
    }

    @PhpTestingProvider.Registration(position=200)
    public static AtoumTestingProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @NbBundle.Messages("AtoumTestingProvider.name=atoum")
    @Override
    public String getDisplayName() {
        return Bundle.AtoumTestingProvider_name();
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
        return Atoum.isTestMethod(method);
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
        return new AtoumTestLocator(phpModule);
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
        Matcher matcher = Atoum.LINE_PATTERN.matcher(line);
        if (matcher.find()) {
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
        return new AtoumCustomizer(phpModule);
    }

}
