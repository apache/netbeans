/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.analysis.commands;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.parsers.CodeSnifferReportParser;
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

public final class CodeSniffer {

    static final Logger LOGGER = Logger.getLogger(CodeSniffer.class.getName());

    public static final String NAME = "phpcs"; // NOI18N
    public static final String LONG_NAME = NAME + FileUtils.getScriptExtension(true);

    static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-php-phpcs-log.xml"); // NOI18N

    private static final String RUNTIME_SET_PARAM = "--runtime-set"; // NOI18N
    private static final String DEFAULT_STANDARD_PARAM = "default_standard"; // NOI18N
    private static final String LIST_STANDARDS_PARAM = "-i"; // NOI18N
    private static final String REPORT_PARAM = "--report=xml"; // NOI18N
    private static final String EXTENSIONS_PARAM = "--extensions=%s"; // NOI18N
    private static final String ENCODING_PARAM = "--encoding=%s"; // NOI18N
    private static final String IGNORE_PARAM = "--ignore=%s"; // NOI18N
    private static final String NO_RECURSION_PARAM = "-l"; // NOI18N

    // cache
    private static final List<String> CACHED_STANDARDS = new CopyOnWriteArrayList<>();

    private final String codeSnifferPath;

    private volatile int analyzeGroupCounter = 1;


    private CodeSniffer(String codeSnifferPath) {
        this.codeSnifferPath = codeSnifferPath;
    }

    /**
     * Get the default, <b>valid only</b> Code Sniffer.
     * @return the default, <b>valid only</b> Code Sniffer.
     * @throws InvalidPhpExecutableException if Code Sniffer is not valid.
     */
    public static CodeSniffer getDefault() throws InvalidPhpExecutableException {
        return getCustom(AnalysisOptions.getInstance().getCodeSnifferPath());
    }

    /**
     * Get the custom, <b>valid only</b> Code Sniffer.
     * @param path custom path
     * @return the custom, <b>valid only</b> Code Sniffer.
     * @throws InvalidPhpExecutableException if Code Sniffer is not valid.
     */
    public static CodeSniffer getCustom(String path) throws InvalidPhpExecutableException {
        String error = validate(path);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new CodeSniffer(path);
    }

    @NbBundle.Messages("CodeSniffer.script.label=Code sniffer")
    public static String validate(String codeSnifferPath) {
        return PhpExecutableValidator.validateCommand(codeSnifferPath, Bundle.CodeSniffer_script_label());
    }

    public static void clearCachedStandards() {
        CACHED_STANDARDS.clear();
    }

    public void startAnalyzeGroup() {
        analyzeGroupCounter = 1;
    }

    @CheckForNull
    public List<Result> analyze(String standard, FileObject file) {
        return analyze(standard, file, false);
    }

    @NbBundle.Messages({
        "# {0} - counter",
        "CodeSniffer.analyze=Code Sniffer (analyze #{0})",
    })
    @CheckForNull
    public List<Result> analyze(String standard, FileObject file, boolean noRecursion) {
        assert file.isValid() : "Invalid file given: " + file;
        try {
            Integer result = getExecutable(Bundle.CodeSniffer_analyze(analyzeGroupCounter++), findWorkDir(file))
                    .additionalParameters(getParameters(ensureStandard(standard), file, noRecursion))
                    .runAndWait(getDescriptor(), "Running code sniffer..."); // NOI18N
            if (result == null) {
                return null;
            }
            // #239232
            if (!XML_LOG.isFile()
                    && result == 0) {
                return Collections.emptyList();
            }
            return CodeSnifferReportParser.parse(XML_LOG);
        } catch (CancellationException ex) {
            // cancelled
            return Collections.emptyList();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, AnalysisOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    @NbBundle.Messages("CodeSniffer.listStandards=Code Sniffer (standards)")
    @CheckForNull
    public List<String> getStandards() {
        if (!CACHED_STANDARDS.isEmpty()) {
            return Collections.unmodifiableList(CACHED_STANDARDS);
        }
        StandardsOutputProcessorFactory standardsProcessorFactory = new StandardsOutputProcessorFactory();
        try {
            getExecutable(Bundle.CodeSniffer_listStandards(), null)
                    .additionalParameters(Collections.singletonList(LIST_STANDARDS_PARAM))
                    .runAndWait(getDescriptor(), standardsProcessorFactory, "Fetching standards..."); // NOI18N
            if (!standardsProcessorFactory.hasStandards()
                    && standardsProcessorFactory.hasOutput) {
                // some error
                return null;
            }
            List<String> standards = standardsProcessorFactory.getStandards();
            CACHED_STANDARDS.addAll(standards);
            return standards;
        } catch (CancellationException ex) {
            // canceled
            LOGGER.log(Level.FINE, "Fetching standards cancelled", ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, AnalysisOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    /**
     * Finds project directory for the given file
     * since it can contain {@code phpcs.xml{,.dist}}.
     * @param file file to find project directory for
     * @return project directory or {@code null}
     */
    @CheckForNull
    private File findWorkDir(FileObject file) {
        assert file != null;
        Project project = FileOwnerQuery.getOwner(file);
        if (project != null) {
            File projectDir = FileUtil.toFile(project.getProjectDirectory());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Project directory for {0} found in {1}", new Object[] {FileUtil.toFile(file), projectDir});
            }
            return projectDir;
        }
        return null;
    }

    private PhpExecutable getExecutable(String title, @NullAllowed File workDir) {
        PhpExecutable executable = new PhpExecutable(codeSnifferPath)
                .fileOutput(XML_LOG, "UTF-8", false) // NOI18N
                .optionsSubcategory(AnalysisOptionsPanelController.OPTIONS_SUB_PATH)
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

    private List<String> getParameters(String standard, FileObject file, boolean noRecursion) {
        Charset encoding = FileEncodingQuery.getEncoding(file);
        List<String> params = new ArrayList<>();
        params.add(RUNTIME_SET_PARAM);
        params.add(DEFAULT_STANDARD_PARAM);
        params.add(standard);
        params.add(REPORT_PARAM);
        params.add(String.format(EXTENSIONS_PARAM, StringUtils.implode(FileUtil.getMIMETypeExtensions(FileUtils.PHP_MIME_TYPE), ","))); // NOI18N
        params.add(String.format(ENCODING_PARAM, encoding.name()));
        addIgnoredFiles(params, file);
        if (noRecursion) {
            params.add(NO_RECURSION_PARAM);
        }
        params.add(FileUtil.toFile(file).getAbsolutePath());
        return params;
    }

    private String ensureStandard(String standard) {
        if (standard != null) {
            return standard;
        }
        List<String> standards = getStandards();
        if (standards == null) {
            // fallback
            return "PEAR"; // NOI18N
        }
        return standards.get(0);
    }

    private void addIgnoredFiles(List<String> params, FileObject file) {
        Collection<FileObject> ignoredFiles = Queries.getVisibilityQuery(PhpModule.Factory.forFileObject(file)).getCodeAnalysisExcludeFiles();
        if (ignoredFiles.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (FileObject fileObject : ignoredFiles) {
            if (sb.length() > 0) {
                sb.append(","); // NOI18N
            }
            // more safe to use '/' even on windows (otherwise preg_match() fails)
            sb.append(FileUtil.getFileDisplayName(fileObject).replace(File.separatorChar, '/')); // NOI18N
            sb.append("/*"); // NOI18N
        }
        params.add(String.format(IGNORE_PARAM, sb.toString()));
    }

    //~ Inner classes

    static final class StandardsOutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory2 {

        static final String LINE_START = "The installed coding standards are "; // NOI18N

        private final List<String> standards = new CopyOnWriteArrayList<>();

        private volatile boolean hasOutput = false;


        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {
                @Override
                public void processLine(String line) {
                    hasOutput = true;
                    if (line.startsWith(LINE_START)) {
                        List<String> parsed = parseStandards(line);
                        if (parsed != null) {
                            standards.addAll(parsed);
                        }
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

        public List<String> getStandards() {
            return standards;
        }

        public boolean hasStandards() {
            return !standards.isEmpty();
        }

        public boolean hasOutput() {
            return hasOutput;
        }

        @CheckForNull
        public static List<String> parseStandards(String line) {
            assert line.startsWith(LINE_START) : line;
            line = line.substring(LINE_START.length());
            List<String> standards = new ArrayList<>();
            List<String> tmp = StringUtils.explode(line, " and "); // NOI18N
            if (tmp.isEmpty()) {
                LOGGER.log(Level.WARNING, "Standards cannot be parsed from: {0}", line);
                return null;
            }
            if (tmp.size() != 2) {
                LOGGER.log(Level.WARNING, "Unexpected standards in: {0}", line);
                return null;
            }
            standards.add(tmp.get(1));
            String rest = tmp.get(0);
            tmp = StringUtils.explode(rest, ", "); // NOI18N
            if (tmp.isEmpty()) {
                LOGGER.log(Level.WARNING, "Standards cannot be parsed from: {0}", rest);
            } else {
                standards.addAll(tmp);
            }
            Collections.sort(standards);
            return standards;
        }

    }

}
