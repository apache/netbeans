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
import org.netbeans.modules.php.analysis.commands.MessDetector;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.ui.analyzer.MessDetectorCustomizerPanel;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.analysis.util.Mappers;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

public class MessDetectorAnalyzerImpl implements Analyzer {

    private static final Logger LOGGER = Logger.getLogger(MessDetectorAnalyzerImpl.class.getName());

    private final Context context;
    private final AtomicBoolean cancelled = new AtomicBoolean();


    MessDetectorAnalyzerImpl(Context context) {
        this.context = context;
    }

    @NbBundle.Messages({
        "MessDetectorAnalyzerImpl.messDetector.error=Mess detector is not valid",
        "MessDetectorAnalyzerImpl.messDetector.error.description=Invalid mess detector set in IDE Options.",
        "MessDetectorAnalyzerImpl.messDetector.ruleSets.error=Mess detector rule sets are not valid",
        "MessDetectorAnalyzerImpl.messDetector.ruleSets.error.description=Invalid mess detector rule sets set in IDE Options.",
    })
    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        Preferences settings = context.getSettings();
        if (settings != null && !settings.getBoolean(MessDetectorCustomizerPanel.ENABLED, false)) {
            return Collections.emptyList();
        }

        MessDetector messDetector = getValidMessDetector();
        if (messDetector == null) {
            context.reportAnalysisProblem(Bundle.MessDetectorAnalyzerImpl_messDetector_error(), Bundle.MessDetectorAnalyzerImpl_messDetector_error_description());
            return Collections.emptyList();
        }

        MessDetectorParams messDetectorParams = getValidMessDetectorParams();
        if (messDetectorParams == null) {
            context.reportAnalysisProblem(Bundle.MessDetectorAnalyzerImpl_messDetector_ruleSets_error(), Bundle.MessDetectorAnalyzerImpl_messDetector_ruleSets_error_description());
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
            return doAnalyze(scope, messDetector, messDetectorParams, fileCount);
        } finally {
            context.finish();
        }
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        // XXX cancel mess detector?
        return true;
    }

    @NbBundle.Messages({
        "MessDetectorAnalyzerImpl.analyze.error=Mess detector analysis error",
        "MessDetectorAnalyzerImpl.analyze.error.description=Error occurred during mess detector analysis, review Output window for more information.",
    })
    private Iterable<? extends ErrorDescription> doAnalyze(Scope scope, MessDetector messDetector,
            MessDetectorParams params, Map<FileObject, Integer> fileCount) {
        List<ErrorDescription> errors = new ArrayList<>();
        int progress = 0;
        messDetector.startAnalyzeGroup();
        for (FileObject root : scope.getSourceRoots()) {
            if (cancelled.get()) {
                return Collections.emptyList();
            }
            List<org.netbeans.modules.php.analysis.results.Result> results = messDetector.analyze(params, root);
            if (results == null) {
                context.reportAnalysisProblem(Bundle.MessDetectorAnalyzerImpl_analyze_error(), Bundle.MessDetectorAnalyzerImpl_analyze_error_description());
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
            List<org.netbeans.modules.php.analysis.results.Result> results = messDetector.analyze(params, file);
            if (results == null) {
                context.reportAnalysisProblem(Bundle.MessDetectorAnalyzerImpl_analyze_error(), Bundle.MessDetectorAnalyzerImpl_analyze_error_description());
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
            List<FileObject> dataChildren = new ArrayList<>();
            for (FileObject child : folder.getChildren()) {
                if (child.isData()) {
                    dataChildren.add(child);
                }
            }
            if (dataChildren.isEmpty()) {
                continue;
            }
            List<org.netbeans.modules.php.analysis.results.Result> results = messDetector.analyze(params, dataChildren);
            if (results == null) {
                context.reportAnalysisProblem(Bundle.MessDetectorAnalyzerImpl_analyze_error(), Bundle.MessDetectorAnalyzerImpl_analyze_error_description());
                return Collections.emptyList();
            }
            errors.addAll(Mappers.map(results));
            progress += fileCount.get(folder);
            context.progress(progress);
        }
        return errors;
    }

    @CheckForNull
    private MessDetector getValidMessDetector() {
        Preferences settings = context.getSettings();
        String messDetectorPath = null;
        if (settings != null) {
            messDetectorPath = settings.get(MessDetectorCustomizerPanel.PATH, null);
        }
        try {
            if (StringUtils.hasText(messDetectorPath)) {
                return MessDetector.getCustom(messDetectorPath);
            }
            return MessDetector.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    @CheckForNull
    private MessDetectorParams getValidMessDetectorParams() {
        MessDetectorParams messDetectorParams = new MessDetectorParams()
                .setRuleSets(getValidMessDetectorRuleSets())
                .setRuleSetFile(getValidRuleSetFile())
                .setOptions(getValidOptions());
        ValidationResult result = new AnalysisOptionsValidator()
                .validateMessDetector(messDetectorParams)
                .getResult();
        if (result.hasErrors() || result.hasWarnings()) {
            return null;
        }
        return messDetectorParams;
    }

    private List<String> getValidMessDetectorRuleSets() {
        List<String> messDetectorRuleSets = MessDetectorCustomizerPanel.getRuleSets(context.getSettings());
        if (messDetectorRuleSets == null) {
            messDetectorRuleSets = AnalysisOptions.getInstance().getMessDetectorRuleSets();
        }
        assert messDetectorRuleSets != null;
        if (messDetectorRuleSets.isEmpty()) {
            return null;
        }
        return messDetectorRuleSets;
    }

    @CheckForNull
    private FileObject getValidRuleSetFile() {
        String ruleSetFile = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            ruleSetFile = settings.get(MessDetectorCustomizerPanel.RULE_SET_FILE, null);
        }
        if (ruleSetFile == null) {
            ruleSetFile = AnalysisOptions.getInstance().getMessDetectorRuleSetFilePath();
        }
        if (StringUtils.isEmpty(ruleSetFile)) {
            return null;
        }
        return FileUtil.toFileObject(new File(ruleSetFile));
    }

    @CheckForNull
    private String getValidOptions() {
        String options = null;
        Preferences settings = context.getSettings();
        if (settings != null) {
            options = settings.get(MessDetectorCustomizerPanel.OPTIONS, null);
        }
        if (options == null) {
            options = AnalysisOptions.getInstance().getMessDetectorOptions();
        }
        return options;
    }

    //~ Inner classes

    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class MessDetectorAnalyzerFactory extends AnalyzerFactory {

        @StaticResource
        private static final String ICON_PATH = "org/netbeans/modules/php/analysis/ui/resources/mess-detector.png"; // NOI18N


        @NbBundle.Messages("MessDetectorAnalyzerFactory.displayName=Mess Detector")
        public MessDetectorAnalyzerFactory() {
            super("PhpMessDetector", Bundle.MessDetectorAnalyzerFactory_displayName(), ICON_PATH); // NOI18N
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return Collections.emptyList();
        }

        @Override
        public CustomizerProvider<Void, MessDetectorCustomizerPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, MessDetectorCustomizerPanel>() {
                @Override
                public Void initialize() {
                    return null;
                }
                @Override
                public MessDetectorCustomizerPanel createComponent(CustomizerContext<Void, MessDetectorCustomizerPanel> context) {
                    return new MessDetectorCustomizerPanel(context);
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new MessDetectorAnalyzerImpl(context);
        }

        @Override
        public void warningOpened(ErrorDescription warning) {
            HintsController.setErrors(warning.getFile(), "phpMessDetectorWarning", Collections.singleton(warning)); // NOI18N
        }

    }

}
