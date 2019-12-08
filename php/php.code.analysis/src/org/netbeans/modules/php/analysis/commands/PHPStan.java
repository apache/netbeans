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
package org.netbeans.modules.php.analysis.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.analysis.PHPStanParams;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.parsers.PHPStanReportParser;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.analysis.ui.options.AnalysisOptionsPanelController;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class PHPStan {

    public static final String NAME = "phpstan"; // NOI18N
    public static final String LONG_NAME = NAME + ".phar"; // NOI18N
    public static final String MAX_LEVEL = "max"; // NOI18N
    static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-php-phpstan-log.xml"); // NOI18N
    private static final Logger LOGGER = Logger.getLogger(PHPStan.class.getName());

    // commands
    private static final String ANALYSE_COMMAND = "analyse"; // NOI18N

    // params
    private static final String CONFIGURATION_PARAM = "--configuration=%s"; // NOI18N
    private static final String LEVEL_PARAM = "--level=%s"; // NOI18N
    private static final String MEMORY_LIMIT_PARAM = "--memory-limit=%s"; // NOI18N
    private static final String ERROR_FORMAT_PARAM = "--error-format=checkstyle"; // NOI18N Or json, raw, table
    private static final String NO_PROGRESS_PARAM = "--no-progress"; // NOI18N
    private static final String NO_INTERACTION_PARAM = "--no-interaction"; // NOI18N
    private static final String ANSI_PARAM = "--ansi"; // NOI18N
    private static final String NO_ANSI_PARAM = "--no-ansi"; // NOI18N
    private static final String VERSION_PARAM = "--version"; // NOI18N
    private static final String VERBOSE_PARAM = "--verbose"; // NOI18N
    private static final List<String> ANALYZE_DEFAULT_PARAMS = Arrays.asList(
            NO_ANSI_PARAM,
            NO_PROGRESS_PARAM,
            NO_INTERACTION_PARAM,
            ERROR_FORMAT_PARAM
    );

    private final String phpStanPath;
    private int analyzeGroupCounter = 1;

    private PHPStan(String phpStanPath) {
        this.phpStanPath = phpStanPath;
    }

    public static PHPStan getDefault() throws InvalidPhpExecutableException {
        String phpStanPath = AnalysisOptions.getInstance().getPHPStanPath();
        String error = validate(phpStanPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new PHPStan(phpStanPath);
    }

    @NbBundle.Messages("PHPStan.script.label=PHPStan")
    public static String validate(String codeSnifferPath) {
        return PhpExecutableValidator.validateCommand(codeSnifferPath, Bundle.PHPStan_script_label());
    }

    public void startAnalyzeGroup() {
        analyzeGroupCounter = 1;
    }

    @NbBundle.Messages({
        "# {0} - counter",
        "PHPStan.analyze=PHPStan (analyze #{0})"
    })
    @CheckForNull
    public List<Result> analyze(PHPStanParams params, FileObject file) {
        assert file.isValid() : "Invalid file given: " + file;
        try {
            FileObject workDir = findWorkDir(file);
            Integer result = getExecutable(Bundle.PHPStan_analyze(analyzeGroupCounter++), workDir == null ? null : FileUtil.toFile(workDir))
                    .additionalParameters(getParameters(params, file))
                    .runAndWait(getDescriptor(), "Running phpstan..."); // NOI18N
            if (result == null) {
                return null;
            }

            return PHPStanReportParser.parse(XML_LOG, file, workDir);
        } catch (CancellationException ex) {
            // cancelled
            return Collections.emptyList();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, AnalysisOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    /**
     * Finds project directory for the given file since it can contain
     * {@code phpstan.neon}, {@code phpstan.neon.dist}.
     *
     * @param file file to find project directory for
     * @return project directory or {@code null}
     */
    @CheckForNull
    private FileObject findWorkDir(FileObject file) {
        assert file != null;
        Project project = FileOwnerQuery.getOwner(file);
        FileObject workDir = null;
        if (project != null) {
            workDir = project.getProjectDirectory();
            if (LOGGER.isLoggable(Level.FINE)) {
                if (workDir != null) {
                    LOGGER.log(Level.FINE, "Project directory for {0} is found in {1}", new Object[]{FileUtil.toFile(file), workDir}); // NOI18N
                } else {
                    // the file/directory may not be in a PHP project
                    LOGGER.log(Level.FINE, "Project directory for {0} is not found", FileUtil.toFile(file)); // NOI18N
                }
            }
        }
        return workDir;
    }

    private PhpExecutable getExecutable(String title, @NullAllowed File workDir) {
        PhpExecutable executable = new PhpExecutable(phpStanPath)
                .optionsSubcategory(AnalysisOptionsPanelController.OPTIONS_SUB_PATH)
                .fileOutput(XML_LOG, "UTF-8", false) // NOI18N
                .redirectErrorStream(false)
                .displayName(title);
        if (workDir != null) {
            executable.workDir(workDir);
        }
        return executable;
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(AnalysisOptionsPanelController.OPTIONS_PATH)
                .frontWindowOnError(false)
                .inputVisible(false)
                .preExecution(() -> {
                    if (XML_LOG.isFile()) {
                        if (!XML_LOG.delete()) {
                            LOGGER.log(Level.INFO, "Cannot delete log file {0}", XML_LOG.getAbsolutePath());
                        }
                    }
                });
    }

    private List<String> getParameters(PHPStanParams parameters, FileObject file) {
        // analyse /path/to/{dir|file}
        List<String> params = new ArrayList<>();
        params.add(ANALYSE_COMMAND);
        params.addAll(ANALYZE_DEFAULT_PARAMS);
        String level = parameters.getLevel();
        if (!StringUtils.isEmpty(level)) {
            params.add(String.format(LEVEL_PARAM, level));
        }
        FileObject configuration = parameters.getConfiguration();
        if (configuration != null) {
            params.add(String.format(CONFIGURATION_PARAM, FileUtil.toFile(configuration).getAbsolutePath()));
        }
        String memoryLimit = parameters.getMemoryLimit();
        if (!StringUtils.isEmpty(memoryLimit)) {
            params.add(String.format(MEMORY_LIMIT_PARAM, memoryLimit));
        }
        params.add(FileUtil.toFile(file).getAbsolutePath());
        return params;
    }

}
