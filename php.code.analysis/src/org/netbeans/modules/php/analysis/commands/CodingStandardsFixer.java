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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.parsers.CodingStandardsFixerReportParser;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.analysis.ui.options.AnalysisOptionsPanelController;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class CodingStandardsFixer {

    static final Logger LOGGER = Logger.getLogger(CodingStandardsFixer.class.getName());

    public static final String NAME = "php-cs-fixer"; // NOI18N
    public static final String LONG_NAME = NAME + ".phar"; // NOI18N

    static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-php-phpcsfixer-log.xml"); // NOI18N

    // commands
    private static final String FIX_COMMAND = "fix"; // NOI18N
    private static final String LIST_COMMAND = "list"; // NOI18N
    private static final String SELF_UPDATE_COMMAND = "self-update"; // NOI18N

    // params
    private static final String ANSI_PARAM = "--ansi"; // NOI18N
    private static final String HELP_PARAM = "--help"; // NOI18N
    private static final String NO_ANSI_PARAM = "--no-ansi"; // NOI18N
    private static final String NO_INTERACTION_PARAM = "--no-interaction"; // NOI18N
    private static final String VERBOSE_PARAM = "--verbose"; // NOI18N
    private static final String DIFF_PARAM = "--diff"; // NOI18N

    private static final String DRY_RUN_PARAM = "--dry-run"; // NOI18N
    private static final String CONFIG_PARAM = "--config=%s"; // NOI18N
    private static final String LEVEL_PARAM = "--level=%s"; // NOI18N
    private static final String FIXERS_PARAM = "--fixers=%s"; // NOI18N
    private static final String FORMAT_XML_PARAM = "--format=xml"; // NOI18N
    private static final List<String> ANALYZE_DEFAULT_PARAMS = Arrays.asList(
            NO_ANSI_PARAM,
            VERBOSE_PARAM,
            DRY_RUN_PARAM,
            DIFF_PARAM,
            FORMAT_XML_PARAM,
            NO_INTERACTION_PARAM);
    private static final List<String> DEFAULT_PARAMS = Arrays.asList(
            ANSI_PARAM,
            NO_INTERACTION_PARAM);

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION", justification = "It is immutable") // NOI18N
    public static final List<String> ALL_LEVEL = Arrays.asList(
            "", // NOI18N
            "psr0", // NOI18N
            "psr1", // NOI18N
            "psr2", // NOI18N
            "symfony" // NOI18N
    );
    // XXX get from help?
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION", justification = "It is immutable") // NOI18N
    public static final List<String> ALL_CONFIG = Arrays.asList(
            "", // NOI18N
            "default", // NOI18N
            "mangento", // NOI18N
            "sf23" // NOI18N
    );

    private final String codingStandardsFixerPath;

    private int analyzeGroupCounter = 1;

    private CodingStandardsFixer(String codingStandardsFixerPath) {
        this.codingStandardsFixerPath = codingStandardsFixerPath;
    }

    public static CodingStandardsFixer getDefault() throws InvalidPhpExecutableException {
        String codingStandardsFixerPath = AnalysisOptions.getInstance().getCodingStandardsFixerPath();
        String error = validate(codingStandardsFixerPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new CodingStandardsFixer(codingStandardsFixerPath);
    }

    @NbBundle.Messages("CodingStandardsFixer.script.label=Coding Standards Fixer")
    public static String validate(String codingStandardsFixerPath) {
        return PhpExecutableValidator.validateCommand(codingStandardsFixerPath, Bundle.CodingStandardsFixer_script_label());
    }

    public void startAnalyzeGroup() {
        analyzeGroupCounter = 1;
    }

    @NbBundle.Messages({
        "# {0} - command",
        "CodingStandardsFixer.script.run=Coding Standards Fixer({0})",
    })
    public Future<Integer> selfUpdate(PhpModule phpModule) {
        return runCommand(phpModule, SELF_UPDATE_COMMAND, Bundle.CodingStandardsFixer_script_run(SELF_UPDATE_COMMAND));
    }

    private Future<Integer> runCommand(PhpModule phpModule, String command, String title) {
        return runCommand(phpModule, command, title, Collections.<String>emptyList());
    }

    private Future<Integer> runCommand(PhpModule phpModule, String command, String title, List<String> params) {
        PhpExecutable phpcsfixer = getExecutable(phpModule, title);
        if (phpcsfixer == null) {
            return null;
        }
        return phpcsfixer
                .additionalParameters(mergeParameters(command, DEFAULT_PARAMS, params))
                .run(getDescriptor(phpModule));
    }

    private List<String> mergeParameters(String command, List<String> defaultParams, List<String> params) {
        List<String> allParams = new ArrayList<>(defaultParams.size() + params.size() + 1);
        allParams.add(command);
        allParams.addAll(params);
        allParams.addAll(defaultParams);
        return allParams;
    }

    @NbBundle.Messages({
        "# {0} - counter",
        "CodingStandardsFixer.analyze=Coding Standards Fixer (analyze #{0})",
    })
    @CheckForNull
    public List<Result> analyze(String level, String conifg, String options, FileObject file) {
        assert file.isValid() : "Invalid file given: " + file;
        try {
            Integer result = getExecutable(Bundle.CodingStandardsFixer_analyze(analyzeGroupCounter++))
                    .additionalParameters(getParameters(level, conifg, options, file))
                    .runAndWait(getDescriptor(), "Running coding standards fixer..."); // NOI18N
            if (result == null) {
                return null;
            }

            return CodingStandardsFixerReportParser.parse(XML_LOG, file);
        } catch (CancellationException ex) {
            // cancelled
            return Collections.emptyList();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, AnalysisOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    private PhpExecutable getExecutable(PhpModule phpModule, String title) {
        return new PhpExecutable(codingStandardsFixerPath)
                .optionsSubcategory(AnalysisOptionsPanelController.OPTIONS_SUB_PATH)
                .displayName(title);
    }

    private PhpExecutable getExecutable(String title) {
        return new PhpExecutable(codingStandardsFixerPath)
                .optionsSubcategory(AnalysisOptionsPanelController.OPTIONS_SUB_PATH)
                .fileOutput(XML_LOG, "UTF-8", false) // NOI18N
                .redirectErrorStream(false)
                .displayName(title);
    }

    private ExecutionDescriptor getDescriptor(PhpModule phpModule) {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(AnalysisOptionsPanelController.OPTIONS_PATH);
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

    private List<String> getParameters(String level, String config, String options, FileObject file) {
        // fix /path/to/{dir|file}
        List<String> params = new ArrayList<>();
        params.add(FIX_COMMAND);
        params.addAll(ANALYZE_DEFAULT_PARAMS);
        if (!StringUtils.isEmpty(level)) {
            params.add(String.format(LEVEL_PARAM, level));
        }
        if (!StringUtils.isEmpty(config)) {
            params.add(String.format(CONFIG_PARAM, config));
        }
        if (!StringUtils.isEmpty(options)) {
            params.addAll(StringUtils.explode(options, " ")); // NOI18N
        }
        params.add(FileUtil.toFile(file).getAbsolutePath());
        return params;
    }

}
