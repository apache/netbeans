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
package org.netbeans.modules.php.analysis.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.analysis.MessDetectorParams;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.parsers.MessDetectorReportParser;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.analysis.ui.options.AnalysisOptionsPanelController;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.queries.Queries;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class MessDetector {

    static final Logger LOGGER = Logger.getLogger(MessDetector.class.getName());

    public static final String NAME = "phpmd"; // NOI18N
    public static final String LONG_NAME = NAME + FileUtils.getScriptExtension(true);

    static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-php-phpmd-log.xml"); // NOI18N

    private static final String REPORT_FORMAT_PARAM = "xml"; // NOI18N
    private static final String EXCLUDE_PARAM = "--exclude"; // NOI18N
    private static final String SUFFIXES_PARAM = "--suffixes"; // NOI18N
    public static final String EMPTY_RULE_SET = "-"; // NOI18N

    // rule sets
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION", justification = "It is immutable") // NOI18N
    public static final List<String> RULE_SETS = Arrays.asList(
            EMPTY_RULE_SET,
            "codesize", // NOI18N
            "controversial", // NOI18N
            "design", // NOI18N
            "naming", // NOI18N
            "unusedcode"); // NOI18N

    private final String messDetectorPath;

    private volatile int analyzeGroupCounter = 1;


    private MessDetector(String messDetectorPath) {
        this.messDetectorPath = messDetectorPath;
    }

    /**
     * Get the default, <b>valid only</b> Mess Detector.
     * @return the default, <b>valid only</b> Mess Detector.
     * @throws InvalidPhpExecutableException if Mess Detector is not valid.
     */
    public static MessDetector getDefault() throws InvalidPhpExecutableException {
        return getCustom(AnalysisOptions.getInstance().getMessDetectorPath());
    }

    /**
     * Get the custom, <b>valid only</b> Mess Detector.
     * @param path custom path
     * @return the custom, <b>valid only</b> Mess Detector.
     * @throws InvalidPhpExecutableException if Mess Detector is not valid.
     */
    public static MessDetector getCustom(String messDetectorPath) throws InvalidPhpExecutableException {
        String error = validate(messDetectorPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new MessDetector(messDetectorPath);
    }

    @NbBundle.Messages("MessDetector.script.label=Mess detector")
    public static String validate(String messDetectorPath) {
        return PhpExecutableValidator.validateCommand(messDetectorPath, Bundle.MessDetector_script_label());
    }

    public void startAnalyzeGroup() {
        analyzeGroupCounter = 1;
    }

    @CheckForNull
    public List<Result> analyze(MessDetectorParams params, FileObject... files) {
        return analyze(params, Arrays.asList(files));
    }

    @NbBundle.Messages({
        "# {0} - counter",
        "MessDetector.analyze=Mess Detector (analyze #{0})",
    })
    @CheckForNull
    public List<Result> analyze(MessDetectorParams params, List<FileObject> files) {
        assert assertValidFiles(files);
        try {
            Integer result = getExecutable(Bundle.MessDetector_analyze(analyzeGroupCounter++))
                    .additionalParameters(getParameters(params, files))
                    .runAndWait(getDescriptor(), "Running mess detector..."); // NOI18N
            if (result == null) {
                return null;
            }
            return MessDetectorReportParser.parse(XML_LOG);
        } catch (CancellationException ex) {
            // cancelled
            return Collections.emptyList();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, AnalysisOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    private PhpExecutable getExecutable(String title) {
        return new PhpExecutable(messDetectorPath)
                .optionsSubcategory(AnalysisOptionsPanelController.OPTIONS_SUB_PATH)
                .fileOutput(XML_LOG, "UTF-8", false) // NOI18N
                .displayName(title);
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(AnalysisOptionsPanelController.OPTIONS_PATH)
                .frontWindowOnError(false)
                .inputVisible(false)
                .preExecution(new Runnable() {
                    @Override
                    public void run() {
                        if (XML_LOG.isFile()) {
                            if (!XML_LOG.delete()) {
                                LOGGER.log(Level.INFO, "Cannot delete log file {0}", XML_LOG.getAbsolutePath());
                            }
                        }
                    }
                });
    }

    private List<String> getParameters(MessDetectorParams parameters, List<FileObject> files) {
        List<String> params = new ArrayList<>();
        // paths
        params.add(joinFilePaths(files));
        // report format
        params.add(REPORT_FORMAT_PARAM);
        // rule sets
        params.add(joinRuleSets(parameters.getRuleSets(), parameters.getRuleSetFile()));
        // extensions
        params.add(SUFFIXES_PARAM);
        params.add(StringUtils.implode(FileUtil.getMIMETypeExtensions(FileUtils.PHP_MIME_TYPE), ",")); // NOI18N
        // exclude
        addIgnoredFiles(params, files);
        String options = parameters.getOptions();
        if (StringUtils.hasText(options)) {
            params.addAll(StringUtils.explode(options, " ")); // NOI18N
        }
        return params;
    }

    private boolean assertValidFiles(List<FileObject> files) {
        for (FileObject file : files) {
            assert file.isValid() : "Invalid file given: " + file;
        }
        return true;
    }

    private String joinFilePaths(List<FileObject> files) {
        StringBuilder paths = new StringBuilder(200);
        for (FileObject file : files) {
            if (paths.length() > 0) {
                paths.append(","); // NOI18N
            }
            paths.append(FileUtil.toFile(file).getAbsolutePath());
        }
        return paths.toString();
    }

    private String joinRuleSets(List<String> ruleSets, FileObject ruleSetFile) {
        StringBuilder ruleSetsBuilder = new StringBuilder(200);
        if (ruleSets != null) {
            for (String ruleSet : ruleSets) {
                if (ruleSet.equals(EMPTY_RULE_SET)) {
                    continue;
                }
                if (ruleSetsBuilder.length() > 0) {
                    ruleSetsBuilder.append(","); // NOI18N
                }
                ruleSetsBuilder.append(ruleSet);
            }
        }
        if (ruleSetFile != null) {
            if (ruleSetsBuilder.length() > 0) {
                ruleSetsBuilder.append(","); // NOI18N
            }
            ruleSetsBuilder.append(FileUtil.toFile(ruleSetFile).getAbsolutePath());
        }
        return ruleSetsBuilder.toString();
    }

    private void addIgnoredFiles(List<String> params, List<FileObject> files) {
        Collection<String> ignoredFiles = new HashSet<>();
        for (FileObject file : files) {
            for (FileObject fileObject : Queries.getVisibilityQuery(PhpModule.Factory.forFileObject(file)).getCodeAnalysisExcludeFiles()) {
                String ignoredName = FileUtil.getFileDisplayName(fileObject);
                ignoredFiles.add(ignoredName + File.separator + "*"); // NOI18N
            }
        }
        if (ignoredFiles.isEmpty()) {
            return;
        }
        params.add(EXCLUDE_PARAM);
        params.add(StringUtils.implode(ignoredFiles, ",")); // NOI18N
    }

}
