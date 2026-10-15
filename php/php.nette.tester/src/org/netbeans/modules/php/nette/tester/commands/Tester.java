/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.nette.tester.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.nette.tester.TesterTestingProvider;
import org.netbeans.modules.php.nette.tester.options.TesterOptions;
import org.netbeans.modules.php.nette.tester.options.TesterOptionsValidator;
import org.netbeans.modules.php.nette.tester.preferences.TesterPreferences;
import org.netbeans.modules.php.nette.tester.preferences.TesterPreferencesValidator;
import org.netbeans.modules.php.nette.tester.run.TapParser;
import org.netbeans.modules.php.nette.tester.run.TestCaseVo;
import org.netbeans.modules.php.nette.tester.run.TestSuiteVo;
import org.netbeans.modules.php.nette.tester.ui.customizer.TesterCustomizer;
import org.netbeans.modules.php.nette.tester.ui.options.TesterOptionsPanelController;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.modules.php.spi.testing.run.TestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Represents <tt>tester</tt>.
 */
public final class Tester {

    private static final Logger LOGGER = Logger.getLogger(Tester.class.getName());

    public static final String TESTER_FILE_NAME = "tester"; // NOI18N

    private static final File COVERAGE_LOG;

    private static final String OUTPUT_PARAM = "-o"; // NOI18N
    private static final String TAP_OUTPUT_PARAM = "tap"; // NOI18N
    private static final String SKIP_INFO_PARAM = "-s"; // NOI18N
    private static final String PHP_INI_PARAM = "-c"; // NOI18N
    private static final String BINARY_EXECUTABLE_PARAM = "-p"; // NOI18N
    private static final String COVERAGE_PARAM = "--coverage"; // NOI18N
    private static final String COVERAGE_SRC_PARAM = "--coverage-src"; // NOI18N
    private static final String DEFINE_INI_ENTRY_PARAM = "-d"; // NOI18N
    private static final String XDEBUG_INI_ENTRY_PARAM = "zend_extension=xdebug." + (Utilities.isWindows() ? "dll" : "so"); // NOI18N

    private final String testerPath;


    static {
        String logDirName = System.getProperty("java.io.tmpdir"); // NOI18N
        COVERAGE_LOG = new File(logDirName, "nb-tester-coverage.xml"); // NOI18N
    }


    private Tester(String testerPath) {
        assert testerPath != null;
        this.testerPath = testerPath;
    }

    public static Tester getDefault() throws InvalidPhpExecutableException {
        String error = validateDefault(true, true);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Tester(TesterOptions.getInstance().getTesterPath());
    }

    @CheckForNull
    public static Tester getForPhpModule(PhpModule phpModule, boolean showCustomizer) {
        if (validatePhpModule(phpModule) != null) {
            if (showCustomizer) {
                UiUtils.invalidScriptProvided(phpModule, TesterCustomizer.IDENTIFIER, null);
            }
            return null;
        }
        // possibly php.ini
        if (!TesterPreferences.isPhpIniEnabled(phpModule)) {
            String error = validateDefault(false, true);
            if (error != null) {
                if (showCustomizer) {
                    UiUtils.invalidScriptProvided(error, TesterOptionsPanelController.OPTIONS_SUB_PATH);
                }
                return null;
            }
        }
        // tester
        String path;
        if (TesterPreferences.isTesterEnabled(phpModule)) {
            // custom tester
            path = TesterPreferences.getTesterPath(phpModule);
        } else {
            // default tester
            String error = validateDefault(true, false);
            if (error != null) {
                if (showCustomizer) {
                    UiUtils.invalidScriptProvided(error, TesterOptionsPanelController.OPTIONS_SUB_PATH);
                }
                return null;
            }
            path = TesterOptions.getInstance().getTesterPath();
        }
        return new Tester(path);
    }

    public static boolean isTestMethod(PhpClass.Method method) {
        return method.getName().startsWith("test"); // NOI18N
    }

    @CheckForNull
    private static String validateDefault(boolean validateTester, boolean validatePhpIni) {
        TesterOptionsValidator validator = new TesterOptionsValidator();
        if (validateTester) {
            validator.validateTesterPath(TesterOptions.getInstance().getTesterPath());
        }
        if (validatePhpIni) {
            validator.validatePhpIniPath(TesterOptions.getInstance().getPhpIniPath());
        }
        return validateResult(validator.getResult());
    }

    @CheckForNull
    private static String validatePhpModule(PhpModule phpModule) {
        ValidationResult result = new TesterPreferencesValidator()
                .validate(phpModule)
                .getResult();
        return validateResult(result);
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getErrors().get(0).getMessage();
        }
        return result.getWarnings().get(0).getMessage();
    }

    @CheckForNull
    public File getCoverageLog() {
        if (COVERAGE_LOG.isFile()) {
            return COVERAGE_LOG;
        }
        return null;
    }

    @CheckForNull
    public Integer runTests(PhpModule phpModule, TestRunInfo runInfo, final TestSession testSession) throws TestRunException {
        PhpExecutable tester = getExecutable(phpModule);
        List<String> params = new ArrayList<>();
        params.add(OUTPUT_PARAM);
        params.add(TAP_OUTPUT_PARAM);
        params.add(SKIP_INFO_PARAM);
        addBinaryExecutable(phpModule, params);
        addPhpIni(phpModule, params);
        if (runInfo.isCoverageEnabled()) {
            // delete the old file
            if (COVERAGE_LOG.isFile()) {
                if (!COVERAGE_LOG.delete()) {
                    LOGGER.info("Cannot delete Nette Tester coverage log file");
                }
            }
            // add params
            params.add(DEFINE_INI_ENTRY_PARAM);
            params.add(XDEBUG_INI_ENTRY_PARAM);
            params.add(COVERAGE_PARAM);
            params.add(COVERAGE_LOG.getAbsolutePath());
            String coverageSourcePath = null;
            if (TesterPreferences.isCoverageSourcePathEnabled(phpModule)) {
                coverageSourcePath = TesterPreferences.getCoverageSourcePath(phpModule);
            } else {
                FileObject sourceDirectory = phpModule.getSourceDirectory();
                if (sourceDirectory != null) {
                    coverageSourcePath = FileUtil.toFile(sourceDirectory).getAbsolutePath();
                }
            }
            if (coverageSourcePath != null) {
                params.add(COVERAGE_SRC_PARAM);
                params.add(coverageSourcePath);
            }
        }
        // custom tests
        List<TestRunInfo.TestInfo> customTests = runInfo.getCustomTests();
        if (customTests.isEmpty()) {
            for (FileObject startFile : runInfo.getStartFiles()) {
                params.add(FileUtil.toFile(startFile).getAbsolutePath());
            }
        } else {
            for (TestRunInfo.TestInfo testInfo : customTests) {
                String location = testInfo.getLocation();
                assert location != null : testInfo;
                params.add(new File(location).getAbsolutePath());
            }
        }
        tester.additionalParameters(params);
        try {
            if (runInfo.getSessionType() == TestRunInfo.SessionType.TEST) {
                return tester.runAndWait(getDescriptor(), new ParsingFactory(testSession), "Running tester tests..."); // NOI18N
            }
            List<FileObject> startFiles = runInfo.getStartFiles();
            assert startFiles.size() == 1 : "Exactly one file expected for debugging but got " + startFiles;
            return tester.debug(startFiles.get(0), getDescriptor(), new ParsingFactory(testSession));
        } catch (CancellationException ex) {
            // canceled
            LOGGER.log(Level.FINE, "Test running cancelled", ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            if (TesterPreferences.isTesterEnabled(phpModule)) {
                // custom tester script
                UiUtils.processExecutionException(ex, phpModule, TesterCustomizer.IDENTIFIER);
            } else {
                UiUtils.processExecutionException(ex, TesterOptionsPanelController.OPTIONS_SUB_PATH);
            }
            throw new TestRunException(ex);
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Tester.run.title=Nett Tester ({0})",
    })
    private PhpExecutable getExecutable(PhpModule phpModule) {
        // backward compatibility, simply return the first test directory
        FileObject testDirectory = phpModule.getTestDirectory(null);
        assert testDirectory != null : "Test directory not found for " + phpModule.getName();
        return new PhpExecutable(testerPath)
                .optionsSubcategory(TesterOptionsPanelController.OPTIONS_SUB_PATH)
                .workDir(FileUtil.toFile(testDirectory))
                .displayName(Bundle.Tester_run_title(phpModule.getDisplayName()));
    }

    private ExecutionDescriptor getDescriptor() {
        // #236397 - cannot be controllable
        return new ExecutionDescriptor()
                .optionsPath(TesterOptionsPanelController.OPTIONS_PATH)
                .showProgress(true)
                .outLineBased(true)
                .errLineBased(true);
    }

    private void addBinaryExecutable(PhpModule phpModule, List<String> params) {
        String binaryExecutable;
        if (TesterPreferences.isBinaryEnabled(phpModule)) {
            binaryExecutable = TesterPreferences.getBinaryExecutable(phpModule);
        } else {
            binaryExecutable = TesterOptions.getInstance().getBinaryExecutable();
        }
        if (StringUtils.hasText(binaryExecutable)) {
            params.add(BINARY_EXECUTABLE_PARAM);
            params.add(binaryExecutable);
        }
    }

    private void addPhpIni(PhpModule phpModule, List<String> params) {
        String phpIniPath;
        if (TesterPreferences.isPhpIniEnabled(phpModule)) {
            phpIniPath = TesterPreferences.getPhpIniPath(phpModule);
        } else {
            phpIniPath = TesterOptions.getInstance().getPhpIniPath();
        }
        if (StringUtils.hasText(phpIniPath)) {
            params.add(PHP_INI_PARAM);
            params.add(phpIniPath);
        }
    }

    //~ Inner classes

    private static final class ParsingFactory implements ExecutionDescriptor.InputProcessorFactory2 {

        private final TestSession testSession;


        private ParsingFactory(TestSession testSession) {
            assert testSession != null;
            this.testSession = testSession;
        }

        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new ParsingProcessor(testSession));
        }

    }

    private static final class ParsingProcessor implements LineProcessor {

        private static final Logger LOGGER = Logger.getLogger(ParsingProcessor.class.getName());

        private final TestSession testSession;
        private final StringBuilder buffer = new StringBuilder();

        private TestSuite testSuite = null;
        private long currentMillis = currentMillis();
        private long testSuiteTime = 0;


        public ParsingProcessor(TestSession testSession) {
            assert testSession != null;
            this.testSession = testSession;
        }

        private static long currentMillis() {
            return System.currentTimeMillis();
        }

        @Override
        public void processLine(String line) {
            LOGGER.log(Level.FINEST, "Processing line: {0}", line);
            if (TapParser.isTestCaseStart(line)) {
                process(buffer.toString());
                buffer.setLength(0);
            }
            buffer.append(line);
            buffer.append("\n"); // NOI18N
        }

        @Override
        public void reset() {
            LOGGER.fine("Resetting processor");
            finish();
        }

        @Override
        public void close() {
            LOGGER.fine("Closing processor");
            finish();
        }

        private void finish() {
            process(buffer.toString());
            if (testSuite != null) {
                LOGGER.log(Level.FINE, "Test suite {0} found, finishing", testSuite);
                testSuite.finish(testSuiteTime);
            }
        }

        public void process(String input) {
            LOGGER.log(Level.FINEST, "Parsing input:\n{0}", input);
            TestSuiteVo suite = new TapParser()
                    .parse(input, currentMillis() - currentMillis);
            LOGGER.log(Level.FINE, "Parsed test suites: {0}", suite);
            // XXX remove once the output TAP format is perfectly known
            try {
                process(suite);
            } catch (Throwable throwable) {
                LOGGER.log(Level.WARNING, null, throwable);
            }
            currentMillis = currentMillis();
        }

        private void process(TestSuiteVo suite) {
            if (testSuite == null) {
                testSuite = testSession.addTestSuite(suite.getName(), getFileObject(suite.getFile()));
            }
            addTestCases(suite.getTestCases());
        }

        private FileObject getFileObject(String path) {
            if (path == null) {
                return null;
            }
            FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(new File(path)));
            assert fileObject != null : "Cannot find file object for: " + path;
            return fileObject;
        }

        private void addTestCases(List<TestCaseVo> testCases) {
            for (TestCaseVo kase : testCases) {
                String name = kase.getName();
                LOGGER.log(Level.FINE, "Adding new test case {0}", name);
                TestCase testCase = testSuite.addTestCase(name, TesterTestingProvider.IDENTIFIER);
                // XXX remove once the output TAP format is perfectly known
                try {
                    map(kase, testCase);
                } catch (Throwable throwable) {
                    LOGGER.log(Level.WARNING, null, throwable);
                }
                testSuiteTime += kase.getTime();
            }
        }

        private void map(TestCaseVo kase, TestCase testCase) {
            testCase.setStatus(kase.getStatus());
            mapLocation(kase, testCase);
            mapFailureInfo(kase, testCase);
            testCase.setTime(kase.getTime());
        }

        private void mapLocation(TestCaseVo kase, TestCase testCase) {
            // XXX - see https://github.com/nette/tester/issues/46
            if (true) return;
            String file = kase.getFile();
            if (file == null) {
                return;
            }
            FileObject fileObject = FileUtil.toFileObject(new File(file));
            assert fileObject != null : "Cannot find file object for file: " + file;
            testCase.setLocation(new Locations.Line(fileObject, kase.getLine()));
        }

        private void mapFailureInfo(TestCaseVo kase, TestCase testCase) {
            if (isPass(kase.getStatus())) {
                if (kase.getMessage() != null) {
                    // skipped test with message
                    mapFailureInfoInternal(kase, testCase);
                }
                assert kase.getDiff() == null : kase.getDiff();
                return;
            }
            mapFailureInfoInternal(kase, testCase);
        }

        @NbBundle.Messages("ParsingProcessor.failure.unknown=Unknown failure")
        private void mapFailureInfoInternal(TestCaseVo kase, TestCase testCase) {
            String message = kase.getMessage();
            // #257477
            if (message == null) {
                message = Bundle.ParsingProcessor_failure_unknown();
            }
            List<String> stackTrace = kase.getStackTrace();
            if (stackTrace == null) {
                stackTrace = Collections.emptyList();
            }
            TestCase.Diff diff = kase.getDiff();
            if (diff == null) {
                diff = TestCase.Diff.NOT_KNOWN;
            }
            testCase.setFailureInfo(message, stackTrace.toArray(new String[0]), isError(kase.getStatus()), diff);
        }

        private boolean isPass(TestCase.Status status) {
            return status == TestCase.Status.PASSED
                    || status == TestCase.Status.SKIPPED;
        }

        private boolean isError(TestCase.Status status) {
            return status == TestCase.Status.ERROR;
        }

    }

}
