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
package org.netbeans.modules.php.analysis;

import java.io.File;
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
import org.netbeans.modules.php.analysis.commands.Psalm;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.ui.analyzer.PsalmCustomizerPanel;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.analysis.util.Mappers;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

public class PsalmAnalyzerImpl implements Analyzer {

    private static final Logger LOGGER = Logger.getLogger(PsalmAnalyzerImpl.class.getName());
    private final Context context;
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public PsalmAnalyzerImpl(Context context) {
        this.context = context;
    }

    @NbBundle.Messages({
        "PsalmAnalyzerImpl.psalm.error=Psalm is not valid",
        "PsalmAnalyzerImpl.psalm.error.description=Invalid psalm set in IDE Options."
    })
    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        Preferences settings = context.getSettings();
        if (settings != null && !settings.getBoolean(PsalmCustomizerPanel.ENABLED, false)) {
            return Collections.emptyList();
        }

        Psalm psalm = getValidPsalm();
        if (psalm == null) {
            context.reportAnalysisProblem(
                    Bundle.PsalmAnalyzerImpl_psalm_error(),
                    Bundle.PsalmAnalyzerImpl_psalm_error_description());
            return Collections.emptyList();
        }

        PsalmParams psalmParams = new PsalmParams()
                .setLevel(getValidPsalmLevel())
                .setConfiguration(getValidPsalmConfiguration())
                .setMemoryLimit(getValidPsalmMemoryLimit());
        Scope scope = context.getScope();

        Map<FileObject, Integer> fileCount = AnalysisUtils.countPhpFiles(scope);
        int totalCount = 0;
        for (Integer count : fileCount.values()) {
            totalCount += count;
        }

        context.start(totalCount);
        try {
            return doAnalyze(scope, psalm, psalmParams, fileCount);
        } finally {
            context.finish();
        }
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        return true;
    }

    @NbBundle.Messages({
        "PsalmAnalyzerImpl.analyze.error=Psalm analysis error",
        "PsalmAnalyzerImpl.analyze.error.description=Error occurred during psalm analysis, review Output window for more information."
    })
    private Iterable<? extends ErrorDescription> doAnalyze(Scope scope, Psalm psalm,
            PsalmParams params, Map<FileObject, Integer> fileCount) {
        List<ErrorDescription> errors = new ArrayList<>();
        int progress = 0;
        psalm.startAnalyzeGroup();
        for (FileObject root : scope.getSourceRoots()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<org.netbeans.modules.php.analysis.results.Result> results = psalm.analyze(params, root);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.PsalmAnalyzerImpl_analyze_error(),
                        Bundle.PsalmAnalyzerImpl_analyze_error_description());
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
            List<org.netbeans.modules.php.analysis.results.Result> results = psalm.analyze(params, file);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.PsalmAnalyzerImpl_analyze_error(),
                        Bundle.PsalmAnalyzerImpl_analyze_error_description());
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
            List<org.netbeans.modules.php.analysis.results.Result> results = psalm.analyze(params, folder);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.PsalmAnalyzerImpl_analyze_error(),
                        Bundle.PsalmAnalyzerImpl_analyze_error_description());
                return Collections.emptyList();
            }
            errors.addAll(Mappers.map(results));
            progress += fileCount.get(folder);
            context.progress(progress);
        }
        return errors;
    }

    @CheckForNull
    private Psalm getValidPsalm() {
        String customizerPsalmPath = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            customizerPsalmPath = settings.get(PsalmCustomizerPanel.PATH, null);
        }
        try {
            if (StringUtils.hasText(customizerPsalmPath)) {
                return Psalm.getCustom(customizerPsalmPath);
            }
            return Psalm.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    private String getValidPsalmLevel() {
        String psalmLevel = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            psalmLevel = settings.get(PsalmCustomizerPanel.LEVEL, null);
        }
        if (psalmLevel == null) {
            psalmLevel = AnalysisOptions.getInstance().getPsalmLevel();
        }
        assert psalmLevel != null;
        return AnalysisOptions.getValidPsalmLevel(psalmLevel);
    }

    @CheckForNull
    private FileObject getValidPsalmConfiguration() {
        String psalmConfiguration = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            psalmConfiguration = settings.get(PsalmCustomizerPanel.CONFIGURATION, null);
        }
        if (psalmConfiguration == null) {
            psalmConfiguration = AnalysisOptions.getInstance().getPsalmConfigurationPath();
        }
        if (StringUtils.isEmpty(psalmConfiguration)) {
            return null;
        }
        return FileUtil.toFileObject(new File(psalmConfiguration));
    }

    private String getValidPsalmMemoryLimit() {
        String memoryLimit;
        Preferences settings = context.getSettings();
        if (settings != null) {
            memoryLimit = settings.get(PsalmCustomizerPanel.MEMORY_LIMIT, ""); // NOI18N
        } else {
            memoryLimit = String.valueOf(AnalysisOptions.getInstance().getPsalmMemoryLimit());
        }
        assert memoryLimit != null;
        return memoryLimit;
    }

    //~ Inner class
    @ServiceProvider(service = AnalyzerFactory.class)
    public static final class PsalmAnalyzerFactory extends AnalyzerFactory {

        @StaticResource
        private static final String ICON_PATH = "org/netbeans/modules/php/analysis/ui/resources/psalm.png"; // NOI18N

        @NbBundle.Messages("PsalmAnalyzerFactory.displayName=Psalm")
        public PsalmAnalyzerFactory() {
            super("Psalm", Bundle.PsalmAnalyzerFactory_displayName(), ICON_PATH);
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return Collections.emptyList();
        }

        @Override
        public CustomizerProvider<Void, PsalmCustomizerPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, PsalmCustomizerPanel>() {
                @Override
                public Void initialize() {
                    return null;
                }

                @Override
                public PsalmCustomizerPanel createComponent(CustomizerContext<Void, PsalmCustomizerPanel> context) {
                    return new PsalmCustomizerPanel(context);
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new PsalmAnalyzerImpl(context);
        }

        @Override
        public void warningOpened(ErrorDescription warning) {
            HintsController.setErrors(warning.getFile(), "psalmWarning", Collections.singleton(warning)); // NOI18N
        }
    }
}
