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
package org.netbeans.modules.php.atoum.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.PhpOptions;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.atoum.AtoumTestingProvider;
import org.netbeans.modules.php.atoum.options.AtoumOptions;
import org.netbeans.modules.php.atoum.options.AtoumOptionsValidator;
import org.netbeans.modules.php.atoum.preferences.AtoumPreferences;
import org.netbeans.modules.php.atoum.preferences.AtoumPreferencesValidator;
import org.netbeans.modules.php.atoum.run.TapParser;
import org.netbeans.modules.php.atoum.run.TestCaseVo;
import org.netbeans.modules.php.atoum.run.TestSuiteVo;
import org.netbeans.modules.php.atoum.ui.customizer.AtoumCustomizer;
import org.netbeans.modules.php.atoum.ui.options.AtoumOptionsPanelController;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.modules.php.spi.testing.run.TestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;


/**
 * Represents <tt>atoum</tt> or <tt>mageekguy.atoum.phar</tt>.
 */
public final class Atoum {

    private static final Logger LOGGER = Logger.getLogger(Atoum.class.getName());

    public static final String PHAR_FILE_NAME = "mageekguy.atoum.phar"; // NOI18N
    public static final String ATOUM_FILE_NAME = "atoum"; // NOI18N
    public static final String BOOTSTRAP_FILE_NAME = ".bootstrap.atoum.php"; // NOI18N
    public static final String CONFIGURATION_FILE_NAME = ".atoum.php"; // NOI18N
    private static final String COVERAGE_SCRIPT_RELATIVE_PATH = "scripts/coverage.php"; //NOI18N

    public static final Pattern LINE_PATTERN = Pattern.compile("(.+):(\\d+)"); // NOI18N

    private static final String TAP_FORMAT_PARAM = "-utr"; // NOI18N
    private static final String DIRECTORY_PARAM = "-d"; // NOI18N
    private static final String FILE_PARAM = "-f"; // NOI18N
    private static final String FILTER_PARAM = "-m"; // NOI18N
    private static final String BOOTSTRAP_PARAM = "-bf"; // NOI18N
    private static final String CONFIGURATION_PARAM = "-c"; // NOI18N
    private static final String INIT_PARAM = "--init"; // NOI18N
    private static final String XDEBUG_CONFIG_PARAM = "-xc"; // NOI18N
    private static final String IDE_KEY_PARAM = "idekey=%s"; // NOI18N
    private static final String OUTPUT_PARAM = "--output"; //NOI18N
    private static final String USE_PARAM = "--use"; //NOI18N
    private static final String COVERAGE_PARAM = "coverage"; //NOI18N

    private static final File COVERAGE_LOG;

    private final String atoumPath;


    static {
        String logDirName = System.getProperty("java.io.tmpdir"); // NOI18N
        COVERAGE_LOG = new File(logDirName, "nb-atoum-coverage.xml"); // NOI18N
    }


    private Atoum(String atoum) {
        assert atoum != null;
        this.atoumPath = atoum;
    }

    public static Atoum getDefault() throws InvalidPhpExecutableException {
        String error = validateDefault();
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Atoum(AtoumOptions.getInstance().getAtoumPath());
    }

    @CheckForNull
    public static Atoum getForPhpModule(PhpModule phpModule, boolean showCustomizer) {
        if (validatePhpModule(phpModule) != null) {
            if (showCustomizer) {
                UiUtils.invalidScriptProvided(phpModule, AtoumCustomizer.IDENTIFIER, null);
            }
            return null;
        }
        // atoum
        String path;
        if (AtoumPreferences.isAtoumEnabled(phpModule)) {
            // custom atoum
            path = AtoumPreferences.getAtoumPath(phpModule);
        } else {
            // default atoum
            String error = validateDefault();
            if (error != null) {
                if (showCustomizer) {
                    UiUtils.invalidScriptProvided(error, AtoumOptionsPanelController.OPTIONS_SUB_PATH);
                }
                return null;
            }
            path = AtoumOptions.getInstance().getAtoumPath();
        }
        return new Atoum(path);
    }

    @CheckForNull
    private static String validateDefault() {
        ValidationResult result = new AtoumOptionsValidator()
                .validate(AtoumOptions.getInstance().getAtoumPath())
                .getResult();
        return validateResult(result);
    }

    @CheckForNull
    private static String validatePhpModule(PhpModule phpModule) {
        ValidationResult result = new AtoumPreferencesValidator()
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

    public static boolean isTestMethod(PhpClass.Method method) {
        return method.getName().startsWith("test"); // NOI18N
    }

    @CheckForNull
    public static File getDefaultBootstrap(PhpModule phpModule) {
        FileObject testDirectory = phpModule.getTestDirectory(null);
        if (testDirectory == null) {
            return null;
        }
        File testDir = FileUtil.toFile(testDirectory);
        assert testDir != null : testDirectory;
        return new File(testDir, BOOTSTRAP_FILE_NAME);
    }

    @CheckForNull
    public static File getDefaultConfiguration(PhpModule phpModule) {
        FileObject testDirectory = phpModule.getTestDirectory(null);
        if (testDirectory == null) {
            return null;
        }
        File testDir = FileUtil.toFile(testDirectory);
        assert testDir != null : testDirectory;
        return new File(testDir, CONFIGURATION_FILE_NAME);
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
        boolean coverageEnabled = runInfo.isCoverageEnabled();
        // executable
        String command = atoumPath;
        boolean phar = atoumPath.toLowerCase().contains(".phar"); // NOI18N
        if (coverageEnabled) {
            if (!phar) {
                File atoumDir = new File(atoumPath)
                        .getParentFile() // bin/
                        .getParentFile(); // atoum dir
                if (!"atoum".equals(atoumDir.getName())) { // NOI18N
                    // vendor/bin/atoum?
                    atoumDir = new File(atoumDir, "atoum/atoum"); // NOI18N
                }
                command = new File(atoumDir, COVERAGE_SCRIPT_RELATIVE_PATH).getAbsolutePath();
                assert new File(command).isFile() : "Coverage script should exist: " + command;
            }
        }
        PhpExecutable atoum = getExecutable(command, phpModule);
        // params
        List<String> params = new ArrayList<>();
        if (coverageEnabled) {
            if (phar) {
                params.add(USE_PARAM);
                params.add(COVERAGE_PARAM);
            }
            params.add(OUTPUT_PARAM);
            params.add(COVERAGE_LOG.getAbsolutePath());
        }
        // custom tests
        List<TestRunInfo.TestInfo> customTests = runInfo.getCustomTests();
        if (!customTests.isEmpty()) {
            StringBuilder buffer = new StringBuilder(200);
            for (TestRunInfo.TestInfo test : customTests) {
                if (buffer.length() > 1) {
                    buffer.append(" "); // NOI18N
                }
                String className = test.getClassName();
                assert className != null : "No classname for test: " + test.getName();
                buffer.append(sanitizeClassName(className));
                buffer.append("::"); // NOI18N
                buffer.append(test.getName());
            }
            params.add(FILTER_PARAM);
            params.add(buffer.toString());
        }
        if (runInfo.getSessionType() == TestRunInfo.SessionType.DEBUG) {
            params.add(XDEBUG_CONFIG_PARAM);
            params.add(String.format(IDE_KEY_PARAM, Lookup.getDefault().lookup(PhpOptions.class).getDebuggerSessionId()));
        }
        addStartFile(runInfo, params);
        params.add(TAP_FORMAT_PARAM);
        addBootstrap(phpModule, params);
        addConfiguration(phpModule, params);
        atoum.additionalParameters(params);
        // run
        if (coverageEnabled) {
            // delete the old file
            if (COVERAGE_LOG.isFile()) {
                if (!COVERAGE_LOG.delete()) {
                    LOGGER.info("Cannot delete atoum coverage log file");
                }
            }
        }
        try {
            if (runInfo.getSessionType() == TestRunInfo.SessionType.TEST) {
                return atoum.runAndWait(getDescriptor(), new ParsingFactory(testSession), "Running atoum tests..."); // NOI18N
            }
            List<FileObject> startFiles = runInfo.getStartFiles();
            assert startFiles.size() == 1 : "Exactly one file expected for debugging but got " + startFiles;
            return atoum.debug(startFiles.get(0), getDescriptor(), new ParsingFactory(testSession));
        } catch (CancellationException ex) {
            // cancelled
            LOGGER.log(Level.FINE, "Test running cancelled", ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            if (AtoumPreferences.isAtoumEnabled(phpModule)) {
                // custom atoum script
                UiUtils.processExecutionException(ex, phpModule, AtoumCustomizer.IDENTIFIER);
            } else {
                UiUtils.processExecutionException(ex, AtoumOptionsPanelController.OPTIONS_SUB_PATH);
            }
            throw new TestRunException(ex);
        }
        return null;
    }

    @CheckForNull
    public Pair<File, File> init(PhpModule phpModule) {
        PhpExecutable atoum = getExecutable(phpModule);
        List<String> params = new ArrayList<>();
        addBootstrap(phpModule, params);
        addConfiguration(phpModule, params);
        params.add(INIT_PARAM);
        atoum.additionalParameters(params);
        try {
            Integer result = atoum.runAndWait(getDescriptor().inputVisible(true), "Running atoum init..."); // NOI18N
            if (result == null
                    || result != 0) {
                return null;
            }
            return Pair.of(getDefaultBootstrap(phpModule), getDefaultConfiguration(phpModule));
        } catch (CancellationException ex) {
            // cancelled
            LOGGER.log(Level.FINE, "Init cancelled", ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, AtoumOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    private PhpExecutable getExecutable(PhpModule phpModule) {
        return getExecutable(atoumPath, phpModule);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Atoum.run.title=atoum ({0})",
    })
    private PhpExecutable getExecutable(String command, PhpModule phpModule) {
        // backward compatibility, simply return the first test directory
        FileObject testDirectory = phpModule.getTestDirectory(null);
        assert testDirectory != null : "Test directory not found for " + phpModule.getName();
        return new PhpExecutable(command)
                .optionsSubcategory(AtoumOptionsPanelController.OPTIONS_SUB_PATH)
                .workDir(FileUtil.toFile(testDirectory))
                .redirectErrorStream(true)
                .noDebugConfig(true)
                .displayName(Bundle.Atoum_run_title(phpModule.getDisplayName()));
    }

    private ExecutionDescriptor getDescriptor() {
        // #236397 - cannot be controllable
        return new ExecutionDescriptor()
                .optionsPath(AtoumOptionsPanelController.OPTIONS_PATH)
                .showProgress(true)
                .outLineBased(true)
                .errLineBased(true);
    }

    private String sanitizeClassName(String className) {
        if (className.startsWith("\\")) { // NOI18N
            return className.substring(1);
        }
        return className;
    }

    private void addBootstrap(PhpModule phpModule, List<String> params) {
        if (AtoumPreferences.isBootstrapEnabled(phpModule)) {
            params.add(BOOTSTRAP_PARAM);
            params.add(AtoumPreferences.getBootstrapPath(phpModule));
        }
    }

    private void addConfiguration(PhpModule phpModule, List<String> params) {
        if (AtoumPreferences.isConfigurationEnabled(phpModule)) {
            params.add(CONFIGURATION_PARAM);
            params.add(AtoumPreferences.getConfigurationPath(phpModule));
        }
    }

    private void addStartFile(TestRunInfo runInfo, List<String> params) {
        for (FileObject startFile : runInfo.getStartFiles()) {
            if (startFile.isData()) {
                params.add(FILE_PARAM);
            } else {
                params.add(DIRECTORY_PARAM);
            }
            params.add(FileUtil.toFile(startFile).getAbsolutePath());
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

        private String testSuiteName = null;
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
                LOGGER.log(Level.FINE, "Test suite {0} found, finishing", testSuiteName);
                testSuite.finish(testSuiteTime);
                testSuite = null;
            }
        }

        private void process(String input) {
            LOGGER.log(Level.FINEST, "Parsing input:\n{0}", input);
            List<TestSuiteVo> suites = new TapParser()
                    .parse(input, currentMillis() - currentMillis);
            LOGGER.log(Level.FINE, "Parsed test suites: {0}", suites);
            // XXX remove once the output TAP format is perfectly known
            try {
                process(suites);
            } catch (Throwable throwable) {
                LOGGER.log(Level.WARNING, null, throwable);
            }
            currentMillis = currentMillis();
        }

        private void process(List<TestSuiteVo> suites) {
            for (TestSuiteVo suite : suites) {
                String name = suite.getName();
                if (testSuiteName == null
                        || !testSuiteName.equals(name)) {
                    if (testSuite != null) {
                        LOGGER.log(Level.FINE, "Finishing the current suite {0}", testSuiteName);
                        testSuite.finish(testSuiteTime);
                        testSuiteTime = 0;
                    }
                    testSuiteName = name;
                    LOGGER.log(Level.FINE, "Adding new test suite {0}", name);
                    testSuite = testSession.addTestSuite(name, getFileObject(suite.getFile()));
                }
                addTestCases(suite.getTestCases());
            }
        }

        private FileObject getFileObject(String path) {
            if (path == null) {
                return null;
            }
            FileObject fileObject = FileUtil.toFileObject(new File(path));
            assert fileObject != null : "Cannot find file object for: " + path;
            return fileObject;
        }

        private void addTestCases(List<TestCaseVo> testCases) {
            for (TestCaseVo kase : testCases) {
                String name = kase.getName();
                LOGGER.log(Level.FINE, "Adding new test case {0}", name);
                TestCase testCase = testSuite.addTestCase(name, AtoumTestingProvider.IDENTIFIER);
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
            testCase.setClassName(testSuiteName);
            testCase.setStatus(kase.getStatus());
            mapLocation(kase, testCase);
            mapFailureInfo(kase, testCase);
            testCase.setTime(kase.getTime());
        }

        private void mapLocation(TestCaseVo kase, TestCase testCase) {
            String file = kase.getFile();
            if (file == null) {
                return;
            }
            FileObject fileObject = FileUtil.toFileObject(new File(file));
            assert fileObject != null : "Cannot find file object for file: " + file;
            testCase.setLocation(new Locations.Line(fileObject, kase.getLine()));
        }

        @NbBundle.Messages("ParsingProcessor.message.no=<no message>")
        private void mapFailureInfo(TestCaseVo kase, TestCase testCase) {
            if (isPass(kase.getStatus())) {
                assert kase.getMessage() == null : kase.getMessage();
                assert kase.getDiff() == null : kase.getDiff();
                return;
            }
            String message = kase.getMessage();
            // #235482
            if (!StringUtils.hasText(message)) {
                message = Bundle.ParsingProcessor_message_no();
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
            return status == TestCase.Status.PASSED;
        }

        private boolean isError(TestCase.Status status) {
            return status == TestCase.Status.ERROR;
        }

    }

}
