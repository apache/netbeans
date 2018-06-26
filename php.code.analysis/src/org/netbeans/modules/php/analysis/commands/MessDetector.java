/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

    // rule sets
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION", justification = "It is immutable") // NOI18N
    public static final List<String> RULE_SETS = Arrays.asList(
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
     * Get the default, <b>valid only</b> Code Sniffer.
     * @return the default, <b>valid only</b> Code Sniffer.
     * @throws InvalidPhpExecutableException if Code Sniffer is not valid.
     */
    public static MessDetector getDefault() throws InvalidPhpExecutableException {
        String messDetectorPath = AnalysisOptions.getInstance().getMessDetectorPath();
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
    public List<Result> analyze(List<String> ruleSets, FileObject... files) {
        return analyze(ruleSets, Arrays.asList(files));
    }

    @NbBundle.Messages({
        "# {0} - counter",
        "MessDetector.analyze=Mess Detector (analyze #{0})",
    })
    @CheckForNull
    public List<Result> analyze(List<String> ruleSets, List<FileObject> files) {
        assert assertValidFiles(files);
        try {
            Integer result = getExecutable(Bundle.MessDetector_analyze(analyzeGroupCounter++))
                    .additionalParameters(getParameters(ruleSets, files))
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

    private List<String> getParameters(List<String> ruleSets, List<FileObject> files) {
        List<String> params = new ArrayList<>();
        // paths
        params.add(joinFilePaths(files));
        // report format
        params.add(REPORT_FORMAT_PARAM);
        // rule sets
        params.add(StringUtils.implode(ruleSets, ",")); // NOI18N
        // extensions
        params.add(SUFFIXES_PARAM);
        params.add(StringUtils.implode(FileUtil.getMIMETypeExtensions(FileUtils.PHP_MIME_TYPE), ",")); // NOI18N
        // exclude
        addIgnoredFiles(params, files);
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
