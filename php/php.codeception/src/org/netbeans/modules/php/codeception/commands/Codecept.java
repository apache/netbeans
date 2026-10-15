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
package org.netbeans.modules.php.codeception.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.codeception.options.CodeceptionOptions;
import org.netbeans.modules.php.codeception.options.CodeceptionOptionsValidator;
import org.netbeans.modules.php.codeception.preferences.CodeceptionPreferences;
import org.netbeans.modules.php.codeception.preferences.CodeceptionPreferencesValidator;
import org.netbeans.modules.php.codeception.ui.CodeceptRunParametersPanel;
import org.netbeans.modules.php.codeception.ui.customizer.CodeceptionCustomizer;
import org.netbeans.modules.php.codeception.ui.options.CodeceptionOptionsPanelController;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Codeception 2.1+ support.
 */
public final class Codecept {

    private static final Logger LOGGER = Logger.getLogger(Codecept.class.getName());

    public enum GenerateCommand {

        // test commands
        Cept("cept"), // NOI18N
        Cest("cest"), // NOI18N
        Phpunit("phpunit"), // NOI18N
        Test("test"); // NOI18N

        private final String command;


        private GenerateCommand(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        public String getFullCommand() {
            return GENERATE_COMMAND + ":" + command; // NOI18N
        }

        @Override
        public String toString() {
            return command;
        }

    }

    public static final String SCRIPT_NAME = "codecept"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);
    public static final String SCRIPT_NAME_PHAR = SCRIPT_NAME + ".phar"; // NOI18N
    // suffix
    public static final String TEST_CLASS_SUFFIX = "Test"; // NOI18N
    private static final String TEST_FILE_SUFFIX = TEST_CLASS_SUFFIX + ".php"; // NOI18N
    public static final String CEST_CLASS_SUFFIX = "Cest"; // NOI18N
    private static final String CEST_FILE_SUFFIX = CEST_CLASS_SUFFIX + ".php"; // NOI18N
    public static final String CEPT_CLASS_SUFFIX = "Cept"; // NOI18N
    private static final String CEPT_FILE_SUFFIX = CEPT_CLASS_SUFFIX + ".php"; // NOI18N
    private static final String SUITE_CONFIG_SUFFIX = ".suite.yml"; // NOI18N
    private static final String SUITE_DIST_CONFIG_SUFFIX = ".suite.dist.yml"; // NOI18N
    private static final List<String> SUITE_CONFIG_SUFFIXES = Arrays.asList(SUITE_CONFIG_SUFFIX, SUITE_DIST_CONFIG_SUFFIX);
    // test method prefix
    public static final String TEST_METHOD_PREFIX = "test"; // NOI18N

    // commands
    private static final String RUN_COMMAND = "run"; // NOI18N
    private static final String BOOTSTRAP_COMMAND = "bootstrap"; // NOI18N
    private static final String BUILD_COMMAND = "build"; // NOI18N
    private static final String CLEAN_COMMAND = "clean"; // NOI18N
    private static final String GENERATE_COMMAND = "generate"; // NOI18N

    // params
    private static final String ANSI_PARAM = "--ansi"; // NOI18N
    private static final String NO_INTERACTION_PARAM = "--no-interaction"; // NOI18N
    private static final List<String> DEFAULT_PARAMS = Arrays.asList(
            ANSI_PARAM,
            NO_INTERACTION_PARAM
    );
    // run
    private static final String LOG_FILE_NAME = "nb-codeception-log.xml"; // NOI18N
    private static final String XML_PARAM = "--xml=%s"; // NOI18N
    private static final String COVERAGE_LOG_FILE_NAME = "nb-codeception-coverage.xml"; // NOI18N
    private static final String COVERAGE_XML_PARAM = "--coverage-xml=%s"; // NOI18N
    private static final String GROUP_PARAM = "--group"; // NOI18N

    private static final String FAILED_GROUP = "failed"; // NOI18N
    // output files
    public static final File XML_LOG;
    public static final File COVERAGE_LOG;

    public static final String CODECEPTION_CONFIG_FILE_NAME = "codeception.yml"; // NOI18N
    public static final String CODECEPTION_DIST_CONFIG_FILE_NAME = "codeception.dist.yml"; // NOI18N
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION_PKGPROTECT", justification = "Immutable collection")
    public static final List<String> CODECEPTION_CONFIG_FILE_NAMES = Arrays.asList(CODECEPTION_CONFIG_FILE_NAME, CODECEPTION_DIST_CONFIG_FILE_NAME);
    public static final Pattern LINE_PATTERN = Pattern.compile("(?:.+\\(\\) )?(.+):(\\d+)"); // NOI18N

    private final String codeceptPath;


    static {
        String logDirName = System.getProperty("java.io.tmpdir"); // NOI18N
        String userLogDirName = System.getProperty("nb.php.codeception.logdir"); // NOI18N
        if (userLogDirName != null) {
            LOGGER.log(Level.INFO, "Custom directory for Codeception logs provided: {0}", userLogDirName);
            File userLogDir = new File(userLogDirName);
            if (userLogDir.isDirectory()
                    && FileUtils.isDirectoryWritable(userLogDir)) {
                logDirName = userLogDirName;
            } else {
                LOGGER.log(Level.WARNING, "Directory for Codeception logs {0} is not writable directory", userLogDirName);
            }
        }
        LOGGER.log(Level.FINE, "Directory for Codeception logs: {0}", logDirName);
        XML_LOG = new File(logDirName, LOG_FILE_NAME);
        COVERAGE_LOG = new File(logDirName, COVERAGE_LOG_FILE_NAME);
    }

    private Codecept(String codeceptPath) {
        assert codeceptPath != null;
        this.codeceptPath = codeceptPath;
    }

    public static Codecept getDefault() throws InvalidPhpExecutableException {
        String error = validateDefault();
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Codecept(CodeceptionOptions.getInstance().getCodeceptionPath());
    }

    @CheckForNull
    public static Codecept getForPhpModule(PhpModule phpModule, boolean showCustomizer) {
        if (validatePhpModule(phpModule) != null) {
            if (showCustomizer) {
                UiUtils.invalidScriptProvided(phpModule, CodeceptionCustomizer.IDENTIFIER, null);
            }
            return null;
        }
        // codcept
        String path;
        if (CodeceptionPreferences.isCustomCodeceptEnabled(phpModule)) {
            // custom
            path = CodeceptionPreferences.getCustomCodeceptPath(phpModule);
        } else {
            // default
            String error = validateDefault();
            if (error != null) {
                if (showCustomizer) {
                    UiUtils.invalidScriptProvided(error, CodeceptionOptionsPanelController.OPTIONS_SUB_PATH);
                }
                return null;
            }
            path = CodeceptionOptions.getInstance().getCodeceptionPath();
        }
        assert path != null;
        return new Codecept(path);
    }

    @CheckForNull
    private static String validatePhpModule(PhpModule phpModule) {
        ValidationResult result = new CodeceptionPreferencesValidator()
                .validate(phpModule)
                .getResult();
        return validateResult(result);
    }

    @NbBundle.Messages({
        "Codecept.run.bootstrap=Codecept (bootstrap)",
        "Codecept.file.exists=codeception.yml already exists",
        "# {0} - path",
        "Codecept.run.bootstrap.confirmation=Initialize Codeception in {0}",
        "Codecept.run.bootstrap.confirmation.title=Initialize Codeception"
    })
    @CheckForNull
    public Future<Integer> bootstrap(PhpModule phpModule) {
        assert phpModule != null;
        List<FileObject> codeceptionYmls = getCodeceptionYmls(phpModule);
        if (!codeceptionYmls.isEmpty()) {
            for (FileObject codeceptionYml : codeceptionYmls) {
                if (CODECEPTION_CONFIG_FILE_NAME.equals(codeceptionYml.getNameExt())) {
                    userWarning(Bundle.Codecept_file_exists());
                    return null;
                }
            }
        }

        // allow only the project directory
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            LOGGER.log(Level.WARNING, "Project {0} may be broken.", phpModule.getName()); // NOI18N
            return null;
        }
        NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(
                Bundle.Codecept_run_bootstrap_confirmation(FileUtil.toFile(sourceDirectory).getAbsolutePath()),
                Bundle.Codecept_run_bootstrap_confirmation_title(),
                NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(confirmation) != NotifyDescriptor.OK_OPTION) {
            return null;
        }
        return runCommand(phpModule, BOOTSTRAP_COMMAND);
    }

    @CheckForNull
    public Future<Integer> clean(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, CLEAN_COMMAND);
    }

    @CheckForNull
    public Future<Integer> build(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, BUILD_COMMAND);
    }

    @CheckForNull
    private Future<Integer> runCommand(PhpModule phpModule, String command) {
        return runCommand(phpModule, command, Collections.<String>emptyList());
    }

    @CheckForNull
    private Future<Integer> runCommand(PhpModule phpModule, String command, List<String> commandParams) {
        PhpExecutable executable = getExecutable(phpModule);
        if (executable == null) {
            return null;
        }
        File workingDirectory = getWorkingDirectory(phpModule);
        if (workingDirectory != null) {
            executable.workDir(workingDirectory);
        }
        return executable
                .additionalParameters(mergeParameters(command, DEFAULT_PARAMS, commandParams))
                .run(getDescriptor(false));
    }

    public FileObject generateTest(PhpModule phpModule, FileObject fo, GenerateCommand command, String suite, String fqName) throws ExecutionException {
        String testPath = fullyQualifiedNameToPath(fqName);
        if (testPath.startsWith("/")) { // NOI18N
            testPath = testPath.substring(1);
        }
        PhpExecutable executable = getExecutable(phpModule);
        if (executable == null) {
            return null;
        }
        File workDir = getWorkingDirectory(phpModule);
        if (workDir != null) {
            executable.workDir(workDir);
        }
        List<String> params = createParams(true);
        params.add(command.getFullCommand());
        params.add(suite);
        params.add(testPath);
        executable.additionalParameters(params);
        GenerateTestOutputFactory generateTestOutputFactory = new GenerateTestOutputFactory();
        try {
            Integer status = executable.runAndWait(getDescriptor(false), generateTestOutputFactory, "Generating test..."); // NOI18N
            if (status != null && status == 0) {
                if (generateTestOutputFactory.isExists()) {
                    return null;
                }
                String filePath = generateTestOutputFactory.getFilePath();
                if (StringUtils.isEmpty(filePath)) {
                    return null;
                }
                File file = new File(filePath);
                return file.exists() ? FileUtil.toFileObject(file) : null;
            }
        } catch (CancellationException ex) {
            // canceled
            LOGGER.log(Level.FINE, "Test creating cancelled", ex); // NOI18N
        }
        return null;
    }

    @CheckForNull
    public Integer runTests(PhpModule phpModule, TestRunInfo runInfo) throws TestRunException {
        PhpExecutable codecept = getExecutable(phpModule);
        if (codecept == null) {
            return null;
        }
        List<String> params = createParams(true);
        List<TestRunInfo.TestInfo> customTests = runInfo.getCustomTests();
        params.add(RUN_COMMAND);
        List<FileObject> startFiles = runInfo.getStartFiles();
        if (!runInfo.allTests()) {
            // codeception can't run multiple tests
            for (FileObject startFile : startFiles) {
                List<FileObject> codeceptionYmls = getCodeceptionYmls(phpModule);
                if (!codeceptionYmls.isEmpty()) {
                    FileObject parent = codeceptionYmls.get(0).getParent();
                    String relativePath = FileUtil.getRelativePath(parent, startFile);
                    if (relativePath != null) {
                        if (startFile.isFolder() && !relativePath.endsWith("/")) { // NOI18N
                            relativePath += "/"; // NOI18N
                        }
                        if (customTests.size() == 1) {
                            relativePath = relativePath + ":" + customTests.get(0).getName(); // NOI18N
                        }
                        params.add(relativePath);
                    }
                }
                // only 1st file
                break;
            }
        }

        // additional parameters for user
        if (CodeceptionPreferences.askForAdditionalParameters(phpModule)) {
            String additionalParams = CodeceptRunParametersPanel.showDialog(phpModule);
            if (additionalParams != null && !additionalParams.isEmpty()) {
                additionalParams = additionalParams.replaceAll(" +", " ").trim(); // NOI18N
                params.addAll(Arrays.asList(additionalParams.split(" "))); // NOI18N
            }
        }

        if (!customTests.isEmpty()) {
            // rerun failed tests
            if (runInfo.isRerun()) {
                if (runInfo.allTests() || customTests.size() > 1) {
                    params.add(GROUP_PARAM);
                    params.add(FAILED_GROUP);
                }
            }
        }
        params.add(String.format(XML_PARAM, XML_LOG.getAbsolutePath()));
        if (runInfo.isCoverageEnabled()) {
            params.add(String.format(COVERAGE_XML_PARAM, COVERAGE_LOG.getAbsolutePath()));
        }
        File workDir = getWorkingDirectory(phpModule);
        if (workDir != null) {
            codecept.workDir(workDir);
        }
        codecept.additionalParameters(params);
        try {
            if (runInfo.getSessionType() == TestRunInfo.SessionType.TEST) {
                return codecept.runAndWait(getDescriptor(true), "Running Codeception tests..."); // NOI18N
            }
            assert startFiles.size() == 1 : "Exactly one file expected for debugging but got " + startFiles;
            return codecept.debug(startFiles.get(0), getDescriptor(true), null);
        } catch (CancellationException ex) {
            // canceled
            LOGGER.log(Level.FINE, "Test running cancelled", ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            if (CodeceptionPreferences.isCustomCodeceptEnabled(phpModule)) {
                // custom codecept
                UiUtils.processExecutionException(ex, phpModule, CodeceptionCustomizer.IDENTIFIER);
            } else {
                UiUtils.processExecutionException(ex, CodeceptionOptionsPanelController.OPTIONS_SUB_PATH);
            }
            throw new TestRunException(ex);
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Codecept.displayName=Codecept ({0})",
    })
    @CheckForNull
    private PhpExecutable getExecutable(PhpModule phpModule) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            org.netbeans.modules.php.codeception.ui.UiUtils.warnNoSources(phpModule.getDisplayName());
            return null;
        }
        return new PhpExecutable(codeceptPath)
                .optionsSubcategory(CodeceptionOptionsPanelController.OPTIONS_SUB_PATH)
                .displayName(Bundle.Codecept_displayName(phpModule.getDisplayName()));
    }

    private ExecutionDescriptor getDescriptor(boolean cleanupLogFiles) {
        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .optionsPath(CodeceptionOptionsPanelController.OPTIONS_PATH);
        if (cleanupLogFiles) {
            // on run tests
            descriptor = descriptor.preExecution(new Runnable() {
                @Override
                public void run() {
                    cleanupLogFiles();
                }
            });
        } else {
            descriptor = descriptor.controllable(true)
                    .frontWindow(true);
        }
        return descriptor;
    }

    private List<String> createParams(boolean withDefaults) {
        List<String> params = new ArrayList<>();
        if (withDefaults) {
            params.addAll(DEFAULT_PARAMS);
        }
        return params;
    }

    @CheckForNull
    private File getWorkingDirectory(PhpModule phpModule) {
        assert phpModule != null;
        List<FileObject> codeceptionYmls = getCodeceptionYmls(phpModule);
        if (!codeceptionYmls.isEmpty()) {
            FileObject parent = codeceptionYmls.get(0).getParent();
            if (parent != null) {
                return FileUtil.toFile(parent);
            }
        }
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory != null) {
            return FileUtil.toFile(sourceDirectory);
        }
        return null;
    }

    private List<String> mergeParameters(String command, List<String> defaultParams, List<String> commandParams) {
        List<String> allParams = new ArrayList<>(defaultParams.size() + commandParams.size() + 1);
        allParams.addAll(defaultParams);
        allParams.add(command);
        allParams.addAll(commandParams);
        return allParams;
    }

    @CheckForNull
    private static String validateDefault() {
        ValidationResult result = new CodeceptionOptionsValidator()
                .validateCodeceptionPath(CodeceptionOptions.getInstance().getCodeceptionPath())
                .getResult();
        return validateResult(result);
    }

    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getErrors().get(0).getMessage();
        }
        return result.getWarnings().get(0).getMessage();
    }

    /**
     * Get configuration files(codeception.yml, codeception.dist.yml).
     *
     * @see http://codeception.com/docs/02-GettingStarted#Configuration
     * @param phpModule
     * @return configuration files(codeception.yml, codeception.dist.yml).
     */
    public static List<FileObject> getCodeceptionYmls(PhpModule phpModule) {
        if (phpModule == null) {
            return Collections.emptyList();
        }
        // custom
        // A PHP Framework may have a codeception.yml in an inner directory.
        // e.g. In case of Yii2 framework, source/tests/codeception.yml
        if (CodeceptionPreferences.isCustomCodeceptionYmlEnabled(phpModule)) {
            String ymlPath = CodeceptionPreferences.getCustomCodeceptionYmlPath(phpModule);
            assert ymlPath != null;
            File file = new File(ymlPath);
            if (file.exists()) {
                return Collections.singletonList(FileUtil.toFileObject(file));
            }
        }
        // default
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            return Collections.emptyList();
        }
        List<FileObject> configFiles = new ArrayList<>();
        for (String configFileName : CODECEPTION_CONFIG_FILE_NAMES) {
            FileObject configFile = sourceDirectory.getFileObject(configFileName);
            if (configFile != null) {
                configFiles.add(configFile);
            }
        }
        return configFiles;
    }

    void cleanupLogFiles() {
        if (Codecept.XML_LOG.exists()) {
            if (!Codecept.XML_LOG.delete()) {
                LOGGER.log(Level.INFO, "Cannot delete Codeception log {0}", Codecept.XML_LOG);
            }
        }
        if (Codecept.COVERAGE_LOG.exists()) {
            if (!Codecept.COVERAGE_LOG.delete()) {
                LOGGER.log(Level.INFO, "Cannot delete code coverage log {0}", Codecept.COVERAGE_LOG);
            }
        }
    }

    private static void userWarning(String warning) {
        NotifyDescriptor.Message message = new NotifyDescriptor.Message(warning, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(message);
    }

    public static Set<String> getSuiteNames(PhpModule phpModule) {
        List<FileObject> testDirectories = phpModule.getTestDirectories();
        Set<String> suites = new TreeSet<>();
        for (FileObject testDirectory : testDirectories) {
            for (FileObject child : testDirectory.getChildren()) {
                if (child.isFolder()) {
                    continue;
                }
                String name = child.getNameExt();
                for (String suffix : SUITE_CONFIG_SUFFIXES) {
                    int lastIndexOfSuiteSuffix = name.lastIndexOf(suffix);
                    if (lastIndexOfSuiteSuffix > 0) {
                        suites.add(name.substring(0, lastIndexOfSuiteSuffix));
                    }
                }
            }
        }
        return suites;
    }

    //~ Static helper methods
    public static boolean isTestFile(String fileName) {
        return !fileName.equals(Codecept.TEST_FILE_SUFFIX)
                && fileName.endsWith(Codecept.TEST_FILE_SUFFIX);
    }

    public static boolean isTestClass(String className) {
        return !className.equals(Codecept.TEST_CLASS_SUFFIX)
                && className.endsWith(Codecept.TEST_CLASS_SUFFIX);
    }

    public static boolean isTestMethod(String methodName, boolean isTest) {
        if (isTest) {
            return !methodName.equals(Codecept.TEST_METHOD_PREFIX)
                    && methodName.startsWith(Codecept.TEST_METHOD_PREFIX);
        }
        // Cest
        return !methodName.startsWith("_"); // NOI18N
    }

    public static boolean isCeptFile(String fileName) {
        return !fileName.equals(Codecept.CEPT_FILE_SUFFIX)
                && fileName.endsWith(Codecept.CEPT_FILE_SUFFIX);
    }

    public static boolean isCeptClass(String className) {
        return !className.equals(Codecept.CEPT_CLASS_SUFFIX)
                && className.endsWith(Codecept.CEPT_CLASS_SUFFIX);
    }

    public static boolean isCestFile(String fileName) {
        return !fileName.equals(Codecept.CEST_FILE_SUFFIX)
                && fileName.endsWith(Codecept.CEST_FILE_SUFFIX);
    }

    public static boolean isCestClass(String className) {
        return !className.equals(Codecept.CEST_CLASS_SUFFIX)
                && className.endsWith(Codecept.CEST_CLASS_SUFFIX);
    }

    public static boolean isCodeceptionTestFile(String fileName) {
        return isTestFile(fileName)
                || isCeptFile(fileName)
                || isCestFile(fileName);
    }

    public static boolean isCodeceptionTestClass(String className) {
        return isTestClass(className)
                || isCeptClass(className)
                || isCestClass(className);
    }

    public static String getTestedClass(String codeceptionTestClass) {
        assert isCodeceptionTestClass(codeceptionTestClass) : "Not Test or Cest or Cept class: " + codeceptionTestClass;
        int lastIndexOf = -1;
        if (isTestClass(codeceptionTestClass)) {
            lastIndexOf = codeceptionTestClass.lastIndexOf(Codecept.TEST_CLASS_SUFFIX);
        } else if (isCestClass(codeceptionTestClass)) {
            lastIndexOf = codeceptionTestClass.lastIndexOf(Codecept.CEST_CLASS_SUFFIX);
        } else if (isCeptClass(codeceptionTestClass)) {
            lastIndexOf = codeceptionTestClass.lastIndexOf(Codecept.CEPT_CLASS_SUFFIX);
        }
        assert lastIndexOf != -1;
        return codeceptionTestClass.substring(0, lastIndexOf);
    }

    public static String makeTestFile(String testedFileName) {
        return testedFileName + Codecept.TEST_FILE_SUFFIX;
    }

    public static String makeTestClass(String testedClass) {
        return testedClass + Codecept.TEST_CLASS_SUFFIX;
    }

    public static String makeCeptFile(String testedFileName) {
        return testedFileName + Codecept.CEPT_FILE_SUFFIX;
    }

    public static String makeCeptClass(String testedClass) {
        return testedClass + Codecept.CEPT_CLASS_SUFFIX;
    }

    public static String makeCestFile(String testedFileName) {
        return testedFileName + Codecept.CEST_FILE_SUFFIX;
    }

    public static String makeCestClass(String testedClass) {
        return testedClass + Codecept.CEST_CLASS_SUFFIX;
    }

    private static String fullyQualifiedNameToPath(@NullAllowed String fqName) {
        if (fqName == null) {
            return null;
        }
        return fqName.replace("\\", "/"); // NOI18N
    }

    //~ Inner classes

    private static final class GenerateTestOutputFactory implements ExecutionDescriptor.InputProcessorFactory2 {

        // we have to fix these patterns if output messages are changed in Codeception
        private static final Pattern CREATED_FILE_PATTERN = Pattern.compile("Test was created in (?<file>.+\\.php)"); // NOI18N
        private static final Pattern EXISTS_FILE_PATTERN = Pattern.compile("Test (?<file>.+\\.php) already exists"); // NOI18N

        static final String FILE_GROUP = "file"; // NOI18N

        volatile String filePath;
        volatile boolean isExists = false;


        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {

                @Override
                public void processLine(String line) {
                    Matcher matcher = CREATED_FILE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        filePath = matcher.group(FILE_GROUP);
                        return;
                    }
                    matcher = EXISTS_FILE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        filePath = matcher.group(FILE_GROUP);
                        isExists = true;
                    }
                }

                @Override
                public void reset() {
                }

                @Override
                public void close() {
                }
            });
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isExists() {
            return isExists;
        }

    }
}
