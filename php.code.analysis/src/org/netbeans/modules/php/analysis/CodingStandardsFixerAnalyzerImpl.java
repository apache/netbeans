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
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.ui.analyzer.CodingStandardsFixerCustomizerPanel;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.analysis.util.Mappers;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

public final class CodingStandardsFixerAnalyzerImpl implements Analyzer {

    private static final Logger LOGGER = Logger.getLogger(CodingStandardsFixerAnalyzerImpl.class.getName());

    private final Context context;
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public CodingStandardsFixerAnalyzerImpl(Context context) {
        this.context = context;
    }

    @NbBundle.Messages({
        "CodingStandardsFixerAnalyzerImpl.codingStandardsFixer.error=Coding Standards Fixer is not valid",
        "CodingStandardsFixerAnalyzerImpl.codingStandardsFixer.error.description=Invalid coding standards fixer set in IDE Options.",
    })
    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        CodingStandardsFixer codingStandardsFixer = getValidCodingStandardsFixer();
        if (codingStandardsFixer == null) {
            context.reportAnalysisProblem(
                    Bundle.CodingStandardsFixerAnalyzerImpl_codingStandardsFixer_error(),
                    Bundle.CodingStandardsFixerAnalyzerImpl_codingStandardsFixer_error_description());
            return Collections.emptyList();
        }
        String level = getValidCodingStandardsFixerLevel();
        String config = getValidCodingStandardsFixerConfig();
        String options = getValidCodingStandardsFixerOptions();
        Scope scope = context.getScope();

        Map<FileObject, Integer> fileCount = AnalysisUtils.countPhpFiles(scope);
        int totalCount = 0;
        for (Integer count : fileCount.values()) {
            totalCount += count;
        }

        context.start(totalCount);
        try {
            return doAnalyze(scope, codingStandardsFixer, level, config, options, fileCount);
        } finally {
            context.finish();
        }
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        // XXX cancel coding standards fixer?
        return true;
    }

    @NbBundle.Messages({
        "CodingStandardsFixerAnalyzerImpl.analyze.error=Coding standards fixer analysis error",
        "CodingStandardsFixerAnalyzerImpl.analyze.error.description=Error occurred during coding standards fixer analysis, review Output window for more information.",
    })
    private Iterable<? extends ErrorDescription> doAnalyze(Scope scope, CodingStandardsFixer codingStandardsFixer,
            String level, String conifg, String options, Map<FileObject, Integer> fileCount) {
        List<ErrorDescription> errors = new ArrayList<>();
        int progress = 0;
        codingStandardsFixer.startAnalyzeGroup();
        for (FileObject root : scope.getSourceRoots()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<org.netbeans.modules.php.analysis.results.Result> results = codingStandardsFixer.analyze(level, conifg, options, root);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.CodingStandardsFixerAnalyzerImpl_analyze_error(),
                        Bundle.CodingStandardsFixerAnalyzerImpl_analyze_error_description());
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
            List<org.netbeans.modules.php.analysis.results.Result> results = codingStandardsFixer.analyze(level, conifg, options, file);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.CodingStandardsFixerAnalyzerImpl_analyze_error(),
                        Bundle.CodingStandardsFixerAnalyzerImpl_analyze_error_description());
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
            List<org.netbeans.modules.php.analysis.results.Result> results = codingStandardsFixer.analyze(level, conifg, options, folder);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.CodingStandardsFixerAnalyzerImpl_analyze_error(),
                        Bundle.CodingStandardsFixerAnalyzerImpl_analyze_error_description());
                return Collections.emptyList();
            }
            errors.addAll(Mappers.map(results));
            progress += fileCount.get(folder);
            context.progress(progress);
        }
        return errors;
    }

    @CheckForNull
    private CodingStandardsFixer getValidCodingStandardsFixer() {
        try {
            return CodingStandardsFixer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    @CheckForNull
    private String getValidCodingStandardsFixerLevel() {
        String codingStandardsFixerLevel = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            codingStandardsFixerLevel = settings.get(CodingStandardsFixerCustomizerPanel.LEVEL, null);
        }
        if (codingStandardsFixerLevel == null) {
            codingStandardsFixerLevel = AnalysisOptions.getInstance().getCodingStandardsFixerLevel();
        }
        assert codingStandardsFixerLevel != null;
        return codingStandardsFixerLevel;

    }

    @CheckForNull
    private String getValidCodingStandardsFixerConfig() {
        String codingStandardsFixerConfig = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            codingStandardsFixerConfig = settings.get(CodingStandardsFixerCustomizerPanel.CONFIG, null);
        }
        if (codingStandardsFixerConfig == null) {
            codingStandardsFixerConfig = AnalysisOptions.getInstance().getCodingStandardsFixerConfig();
        }
        assert codingStandardsFixerConfig != null;
        return codingStandardsFixerConfig;
    }

    @CheckForNull
    private String getValidCodingStandardsFixerOptions() {
        String codingStandardsFixerOptions = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            codingStandardsFixerOptions = settings.get(CodingStandardsFixerCustomizerPanel.OPTIONS, null);
        }
        if (codingStandardsFixerOptions == null) {
            codingStandardsFixerOptions = AnalysisOptions.getInstance().getCodingStandardsFixerOptions();
        }
        assert codingStandardsFixerOptions != null;
        return codingStandardsFixerOptions;
    }

    //~ Inner class
    @ServiceProvider(service = AnalyzerFactory.class)
    public static final class CodingStandardsFixerAnalyzerFactory extends AnalyzerFactory {

        @StaticResource
        private static final String ICON_PATH = "org/netbeans/modules/php/analysis/ui/resources/coding-standards-fixer.png"; // NOI18N

        @NbBundle.Messages("CodingStandardsFixerAnalyzerFactory.displayName=Coding Standards Fixer")
        public CodingStandardsFixerAnalyzerFactory() {
            super("PhpCodingStandardsFixer", Bundle.CodingStandardsFixerAnalyzerFactory_displayName(), ICON_PATH);
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return Collections.emptyList();
        }

        @Override
        public CustomizerProvider<Void, CodingStandardsFixerCustomizerPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, CodingStandardsFixerCustomizerPanel>() {
                @Override
                public Void initialize() {
                    return null;
                }

                @Override
                public CodingStandardsFixerCustomizerPanel createComponent(CustomizerContext<Void, CodingStandardsFixerCustomizerPanel> context) {
                    return new CodingStandardsFixerCustomizerPanel(context);
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new CodingStandardsFixerAnalyzerImpl(context);
        }

    }
}
