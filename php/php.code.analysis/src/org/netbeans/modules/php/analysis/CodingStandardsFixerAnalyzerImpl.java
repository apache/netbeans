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
import org.netbeans.modules.php.api.util.StringUtils;
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
        Preferences settings = context.getSettings();
        if (settings != null && !settings.getBoolean(CodingStandardsFixerCustomizerPanel.ENABLED, false)) {
            return Collections.emptyList();
        }

        CodingStandardsFixer codingStandardsFixer = getValidCodingStandardsFixer();
        if (codingStandardsFixer == null) {
            context.reportAnalysisProblem(
                    Bundle.CodingStandardsFixerAnalyzerImpl_codingStandardsFixer_error(),
                    Bundle.CodingStandardsFixerAnalyzerImpl_codingStandardsFixer_error_description());
            return Collections.emptyList();
        }

        String version = getValidCodingStandardsFixerVersion();
        String level = getValidCodingStandardsFixerLevel();
        String config = getValidCodingStandardsFixerConfig();
        String options = getValidCodingStandardsFixerOptions();
        CodingStandardsFixerParams codingStandardsFixerParams = new CodingStandardsFixerParams()
                .setVersion(version)
                .setLevel(level)
                .setConfig(config)
                .setOptions(options);
        Scope scope = context.getScope();

        Map<FileObject, Integer> fileCount = AnalysisUtils.countPhpFiles(scope);
        int totalCount = 0;
        for (Integer count : fileCount.values()) {
            totalCount += count;
        }

        context.start(totalCount);
        try {
            return doAnalyze(scope, codingStandardsFixer, codingStandardsFixerParams, fileCount);
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
            CodingStandardsFixerParams params, Map<FileObject, Integer> fileCount) {
        List<ErrorDescription> errors = new ArrayList<>();
        int progress = 0;
        codingStandardsFixer.startAnalyzeGroup();
        for (FileObject root : scope.getSourceRoots()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<org.netbeans.modules.php.analysis.results.Result> results = codingStandardsFixer.analyze(params, root);
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
            List<org.netbeans.modules.php.analysis.results.Result> results = codingStandardsFixer.analyze(params, file);
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
            List<org.netbeans.modules.php.analysis.results.Result> results = codingStandardsFixer.analyze(params, folder);
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
        Preferences settings = context.getSettings();
        String codingStandardsFixerPath = null;
        if (settings != null) {
            codingStandardsFixerPath = settings.get(CodingStandardsFixerCustomizerPanel.PATH, null);
        }
        try {
            if (StringUtils.hasText(codingStandardsFixerPath)) {
                return CodingStandardsFixer.getCustom(codingStandardsFixerPath);
            }
            return CodingStandardsFixer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    private String getValidCodingStandardsFixerVersion() {
        String codingStandardsFixerVersion = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            codingStandardsFixerVersion = settings.get(CodingStandardsFixerCustomizerPanel.VERSION, null);
        }
        if (codingStandardsFixerVersion == null) {
            codingStandardsFixerVersion = AnalysisOptions.getInstance().getCodingStandardsFixerVersion();
        }
        assert codingStandardsFixerVersion != null;
        return codingStandardsFixerVersion;
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
