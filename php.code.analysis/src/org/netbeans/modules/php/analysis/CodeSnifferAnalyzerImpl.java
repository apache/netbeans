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
package org.netbeans.modules.php.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.ui.analyzer.CodeSnifferCustomizerPanel;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.analysis.util.Mappers;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

public class CodeSnifferAnalyzerImpl implements Analyzer {

    private static final Logger LOGGER = Logger.getLogger(CodeSnifferAnalyzerImpl.class.getName());

    private final Context context;
    private final AtomicBoolean cancelled = new AtomicBoolean();


    CodeSnifferAnalyzerImpl(Context context) {
        this.context = context;
    }

    @NbBundle.Messages({
        "CodeSnifferAnalyzerImpl.codeSniffer.error=Code sniffer is not valid",
        "CodeSnifferAnalyzerImpl.codeSniffer.error.description=Invalid code sniffer set in IDE Options.",
        "CodeSnifferAnalyzerImpl.codeSniffer.standard.error=Code sniffer standard is not valid",
        "CodeSnifferAnalyzerImpl.codeSniffer.standard.error.description=Invalid code sniffer standard set in IDE Options.",
    })
    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        CodeSniffer codeSniffer = getValidCodeSniffer();
        if (codeSniffer == null) {
            context.reportAnalysisProblem(Bundle.CodeSnifferAnalyzerImpl_codeSniffer_error(), Bundle.CodeSnifferAnalyzerImpl_codeSniffer_error_description());
            return Collections.emptyList();
        }

        String codeSnifferStandard = getValidCodeSnifferStandard();
        if (codeSnifferStandard == null) {
            context.reportAnalysisProblem(Bundle.CodeSnifferAnalyzerImpl_codeSniffer_standard_error(), Bundle.CodeSnifferAnalyzerImpl_codeSniffer_standard_error_description());
            return Collections.emptyList();
        }

        Scope scope = context.getScope();

        Map<FileObject, Integer> fileCount = AnalysisUtils.countPhpFiles(scope);
        int totalCount = 0;
        for (Integer count : fileCount.values()) {
            totalCount += count;
        }

        context.start(totalCount);
        try {
            return doAnalyze(scope, codeSniffer, codeSnifferStandard, fileCount);
        } finally {
            context.finish();
        }
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        // XXX cancel code sniffer?
        return true;
    }

    @NbBundle.Messages({
        "CodeSnifferAnalyzerImpl.analyze.error=Code sniffer analysis error",
        "CodeSnifferAnalyzerImpl.analyze.error.description=Error occurred during code sniffer analysis, review Output window for more information.",
    })
    private Iterable<? extends ErrorDescription> doAnalyze(Scope scope, CodeSniffer codeSniffer, String codeSnifferStandard, Map<FileObject, Integer> fileCount) {
        List<ErrorDescription> errors = new ArrayList<>();
        int progress = 0;
        codeSniffer.startAnalyzeGroup();
        for (FileObject root : scope.getSourceRoots()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<org.netbeans.modules.php.analysis.results.Result> results = codeSniffer.analyze(codeSnifferStandard, root);
            if (results == null) {
                context.reportAnalysisProblem(Bundle.CodeSnifferAnalyzerImpl_analyze_error(), Bundle.CodeSnifferAnalyzerImpl_analyze_error_description());
                return Collections.emptyList();
            }
            errors.addAll(Mappers.map(results));
            progress += fileCount.get(root);
            context.progress(progress);
        }

        for (FileObject file : scope.getFiles()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<org.netbeans.modules.php.analysis.results.Result> results = codeSniffer.analyze(codeSnifferStandard, file);
            if (results == null) {
                context.reportAnalysisProblem(Bundle.CodeSnifferAnalyzerImpl_analyze_error(), Bundle.CodeSnifferAnalyzerImpl_analyze_error_description());
                return Collections.emptyList();
            }
            errors.addAll(Mappers.map(results));
            progress += fileCount.get(file);
            context.progress(progress);
        }

        for (NonRecursiveFolder nonRecursiveFolder : scope.getFolders()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            FileObject folder = nonRecursiveFolder.getFolder();
            List<org.netbeans.modules.php.analysis.results.Result> results = codeSniffer.analyze(codeSnifferStandard, folder, true);
            if (results == null) {
                context.reportAnalysisProblem(Bundle.CodeSnifferAnalyzerImpl_analyze_error(), Bundle.CodeSnifferAnalyzerImpl_analyze_error_description());
                return Collections.emptyList();
            }
            errors.addAll(Mappers.map(results));
            progress += fileCount.get(folder);
            context.progress(progress);
        }
        return errors;
    }

    @CheckForNull
    private CodeSniffer getValidCodeSniffer() {
        try {
            return CodeSniffer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    @CheckForNull
    private String getValidCodeSnifferStandard() {
        String codeSnifferStandard = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            codeSnifferStandard = settings.get(CodeSnifferCustomizerPanel.STANDARD, null);
        }
        if (codeSnifferStandard == null) {
            codeSnifferStandard = AnalysisOptions.getInstance().getCodeSnifferStandard();
        }
        ValidationResult result = new AnalysisOptionsValidator()
                .validateCodeSnifferStandard(codeSnifferStandard)
                .getResult();
        if (result.hasErrors()
                || result.hasWarnings()) {
            return null;
        }
        assert codeSnifferStandard != null;
        return codeSnifferStandard;
    }

    //~ Inner classes

    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class CodeSnifferAnalyzerFactory extends AnalyzerFactory {

        @StaticResource
        private static final String ICON_PATH = "org/netbeans/modules/php/analysis/ui/resources/code-sniffer.png"; // NOI18N


        @NbBundle.Messages("CodeSnifferAnalyzerFactory.displayName=Code Sniffer")
        public CodeSnifferAnalyzerFactory() {
            super("PhpCodeSniffer", Bundle.CodeSnifferAnalyzerFactory_displayName(), ICON_PATH); // NOI18N
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return Collections.emptyList();
        }

        @Override
        public CustomizerProvider<Void, CodeSnifferCustomizerPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, CodeSnifferCustomizerPanel>() {
                @Override
                public Void initialize() {
                    return null;
                }
                @Override
                public CodeSnifferCustomizerPanel createComponent(CustomizerContext<Void, CodeSnifferCustomizerPanel> context) {
                    return new CodeSnifferCustomizerPanel(context);
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new CodeSnifferAnalyzerImpl(context);
        }

        @Override
        public void warningOpened(ErrorDescription warning) {
            HintsController.setErrors(warning.getFile(), "phpCodeSnifferWarning", Collections.singleton(warning)); // NOI18N
        }

    }

}
