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
import org.netbeans.modules.php.analysis.commands.PHPStan;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.ui.analyzer.PHPStanCustomizerPanel;
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

public class PHPStanAnalyzerImpl implements Analyzer {

    private static final Logger LOGGER = Logger.getLogger(PHPStanAnalyzerImpl.class.getName());
    private final Context context;
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public PHPStanAnalyzerImpl(Context context) {
        this.context = context;
    }

    @NbBundle.Messages({
        "PHPStanAnalyzerImpl.phpStan.error=PHPStan is not valid",
        "PHPStanAnalyzerImpl.phpStan.error.description=Invalid phpstan set in IDE Options."
    })
    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        Preferences settings = context.getSettings();
        if (settings != null && !settings.getBoolean(PHPStanCustomizerPanel.ENABLED, false)) {
            return Collections.emptyList();
        }

        PHPStan phpStan = getValidPHPStan();
        if (phpStan == null) {
            context.reportAnalysisProblem(
                    Bundle.PHPStanAnalyzerImpl_phpStan_error(),
                    Bundle.PHPStanAnalyzerImpl_phpStan_error_description());
            return Collections.emptyList();
        }

        PHPStanParams phpStanParams = new PHPStanParams()
                .setLevel(getValidPHPStanLevel())
                .setConfiguration(getValidPHPStanConfiguration())
                .setMemoryLimit(getValidPHPStanMemoryLimit());
        Scope scope = context.getScope();

        Map<FileObject, Integer> fileCount = AnalysisUtils.countPhpFiles(scope);
        int totalCount = 0;
        for (Integer count : fileCount.values()) {
            totalCount += count;
        }

        context.start(totalCount);
        try {
            return doAnalyze(scope, phpStan, phpStanParams, fileCount);
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
        "PHPStanAnalyzerImpl.analyze.error=PHPStan analysis error",
        "PHPStanAnalyzerImpl.analyze.error.description=Error occurred during phpstan analysis, review Output window for more information."
    })
    private Iterable<? extends ErrorDescription> doAnalyze(Scope scope, PHPStan phpStan,
            PHPStanParams params, Map<FileObject, Integer> fileCount) {
        List<ErrorDescription> errors = new ArrayList<>();
        int progress = 0;
        phpStan.startAnalyzeGroup();
        for (FileObject root : scope.getSourceRoots()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<org.netbeans.modules.php.analysis.results.Result> results = phpStan.analyze(params, root);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.PHPStanAnalyzerImpl_analyze_error(),
                        Bundle.PHPStanAnalyzerImpl_analyze_error_description());
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
            List<org.netbeans.modules.php.analysis.results.Result> results = phpStan.analyze(params, file);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.PHPStanAnalyzerImpl_analyze_error(),
                        Bundle.PHPStanAnalyzerImpl_analyze_error_description());
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
            List<org.netbeans.modules.php.analysis.results.Result> results = phpStan.analyze(params, folder);
            if (results == null) {
                context.reportAnalysisProblem(
                        Bundle.PHPStanAnalyzerImpl_analyze_error(),
                        Bundle.PHPStanAnalyzerImpl_analyze_error_description());
                return Collections.emptyList();
            }
            errors.addAll(Mappers.map(results));
            progress += fileCount.get(folder);
            context.progress(progress);
        }
        return errors;
    }

    @CheckForNull
    private PHPStan getValidPHPStan() {
        String customizerPHPStanPath = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            customizerPHPStanPath = settings.get(PHPStanCustomizerPanel.PATH, null);
        }
        try {
            if (StringUtils.hasText(customizerPHPStanPath)) {
                return PHPStan.getCustom(customizerPHPStanPath);
            }
            return PHPStan.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    private String getValidPHPStanLevel() {
        String phpStanLevel = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            phpStanLevel = settings.get(PHPStanCustomizerPanel.LEVEL, null);
        }
        if (phpStanLevel == null) {
            phpStanLevel = AnalysisOptions.getInstance().getPHPStanLevel();
        }
        assert phpStanLevel != null;
        return AnalysisOptions.getValidPHPStanLevel(phpStanLevel);
    }

    @CheckForNull
    private FileObject getValidPHPStanConfiguration() {
        String phpStanConfiguration = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            phpStanConfiguration = settings.get(PHPStanCustomizerPanel.CONFIGURATION, null);
        }
        if (phpStanConfiguration == null) {
            phpStanConfiguration = AnalysisOptions.getInstance().getPHPStanConfigurationPath();
        }
        if (StringUtils.isEmpty(phpStanConfiguration)) {
            return null;
        }
        return FileUtil.toFileObject(new File(phpStanConfiguration));
    }

    private String getValidPHPStanMemoryLimit() {
        String memoryLimit;
        Preferences settings = context.getSettings();
        if (settings != null) {
            memoryLimit = settings.get(PHPStanCustomizerPanel.MEMORY_LIMIT, ""); // NOI18N
        } else {
            memoryLimit = String.valueOf(AnalysisOptions.getInstance().getPHPStanMemoryLimit());
        }
        assert memoryLimit != null;
        return memoryLimit;
    }

    //~ Inner class
    @ServiceProvider(service = AnalyzerFactory.class)
    public static final class PHPStanAnalyzerFactory extends AnalyzerFactory {

        @StaticResource
        private static final String ICON_PATH = "org/netbeans/modules/php/analysis/ui/resources/phpstan.png"; // NOI18N

        @NbBundle.Messages("PHPStanAnalyzerFactory.displayName=PHPStan")
        public PHPStanAnalyzerFactory() {
            super("PHPStan", Bundle.PHPStanAnalyzerFactory_displayName(), ICON_PATH);
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return Collections.emptyList();
        }

        @Override
        public CustomizerProvider<Void, PHPStanCustomizerPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, PHPStanCustomizerPanel>() {
                @Override
                public Void initialize() {
                    return null;
                }

                @Override
                public PHPStanCustomizerPanel createComponent(CustomizerContext<Void, PHPStanCustomizerPanel> context) {
                    return new PHPStanCustomizerPanel(context);
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new PHPStanAnalyzerImpl(context);
        }

        @Override
        public void warningOpened(ErrorDescription warning) {
            HintsController.setErrors(warning.getFile(), "phpStanWarning", Collections.singleton(warning)); // NOI18N
        }
    }
}
