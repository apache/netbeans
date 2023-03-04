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
package org.netbeans.modules.java.hints.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Folder;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Resource;
import org.netbeans.modules.java.hints.spiimpl.batch.ProgressHandleWrapper;
import org.netbeans.modules.java.hints.spiimpl.batch.Scopes;
import org.netbeans.modules.java.hints.spiimpl.options.HintsPanel;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities.ClassPathBasedHintWrapper;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class AnalyzerImpl implements Analyzer {

    public static final String ID_JAVA_HINTS_PREFIX = "text/x-java:";

    private final AtomicBoolean cancel = new AtomicBoolean();
    private final Context ctx;
    private final ClassPathBasedHintWrapper cpHints;

    private AnalyzerImpl(Context ctx) {
        this.ctx = ctx;
        cpHints = new ClassPathBasedHintWrapper();
    }

    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        String singleWarning;
        if (ctx.getSingleWarningId() != null) {
            if (!ctx.getSingleWarningId().startsWith(ID_JAVA_HINTS_PREFIX))
                return Collections.emptyList();
            singleWarning = ctx.getSingleWarningId().substring(ID_JAVA_HINTS_PREFIX.length());
        } else {
            singleWarning = null;
        }
        final List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        ProgressHandleWrapper w = new ProgressHandleWrapper(ctx, 10, 90);
        Collection<HintDescription> hints = new ArrayList<HintDescription>();
        Preferences incomingSettings = ctx.getSettings();
        HintsSettings settings = incomingSettings != null ? HintsSettings.createPreferencesBasedHintsSettings(incomingSettings, false, null) : null;

        for (Entry<? extends HintMetadata, ? extends Collection<? extends HintDescription>> e : Utilities.getBatchSupportedHints(new ClassPathBasedHintWrapper()).entrySet()) {
            if (singleWarning != null) {
                if (!singleWarning.equals(e.getKey().id)) continue;
            } else if (incomingSettings != null) {
                if (!settings.isEnabled(e.getKey())) continue;
            }

            hints.addAll(e.getValue());
        }

        List todo = new ArrayList();

        todo.addAll(ctx.getScope().getSourceRoots());
        todo.addAll(ctx.getScope().getFolders());
        todo.addAll(ctx.getScope().getFiles());

        BatchResult candidates = BatchSearch.findOccurrences(hints, Scopes.specifiedFoldersScope(Folder.convert(todo)), w, settings);
        List<MessageImpl> problems = new LinkedList<>(candidates.problems);
        
        BatchSearch.getVerifiedSpans(candidates, w, new BatchSearch.VerifiedSpansCallBack() {
            public void groupStarted() {}
            public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> inHints) throws Exception {
                result.addAll(inHints);
                return true;
            }
            public void groupFinished() {}
            public void cannotVerifySpan(Resource r) {
            }
        }, problems, cancel);

        w.finish();

        return result;
    }

    @Override
    public boolean cancel() {
        cancel.set(true);
        return true;
    }

    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class AnalyzerFactoryImpl extends AnalyzerFactory {

        @Messages("DN_JavaHints=NetBeans Java Hints")
        public AnalyzerFactoryImpl() {
            super("java.hints", Bundle.DN_JavaHints(), "org/netbeans/modules/java/hints/analyzer/ui/warning-glyph.gif");
        }

        private  static final String HINTS_FOLDER = "org-netbeans-modules-java-hints/rules/hints/";  // NOI18N

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            List<WarningDescription> result = new ArrayList<WarningDescription>();

            for (Entry<? extends HintMetadata, ? extends Collection<? extends HintDescription>> e : Utilities.getBatchSupportedHints(new ClassPathBasedHintWrapper()).entrySet()) {
                if (e.getKey().options.contains(Options.NON_GUI)) continue;
                String displayName = e.getKey().displayName;
                String category = e.getKey().category;
                String categoryDisplayName = Utilities.categoryDisplayName(category);

                result.add(WarningDescription.create(ID_JAVA_HINTS_PREFIX + e.getKey().id, displayName, category, categoryDisplayName));
            }

            return result;
        }

        static String getFileObjectLocalizedName( FileObject fo ) {
            Object o = fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
            if ( o instanceof String ) {
                String bundleName = (String)o;
                try {
                    ResourceBundle rb = NbBundle.getBundle(bundleName);
                    String localizedName = rb.getString(fo.getPath());
                    return localizedName;
                }
                catch(MissingResourceException ex ) {
                    // Do nothing return file path;
                }
            }
            return fo.getPath();
        }

        @Override
        public CustomizerProvider<ClassPathBasedHintWrapper, HintsPanel> getCustomizerProvider() {
            return new CustomizerProvider<ClassPathBasedHintWrapper, HintsPanel>() {
                @Override public ClassPathBasedHintWrapper initialize() {
                    ClassPathBasedHintWrapper w = new ClassPathBasedHintWrapper();

                    w.compute();
                    return w;
                }
                @Override public HintsPanel createComponent(CustomizerContext<ClassPathBasedHintWrapper, HintsPanel> context) {
                    if (context.getPreselectId() == null) {
                        HintsPanel prev = context.getPreviousComponent();
                        if (prev != null) {
                            prev.setOverlayPreferences(HintsSettings.createPreferencesBasedHintsSettings(context.getSettings(), false, Severity.VERIFIER), true);
                            return prev;
                        }
                        return new HintsPanel(context.getSettings(), context.getData(), true);
                    } else {
                        HintMetadata toSelect = null;
                        for (HintMetadata hm : Utilities.getBatchSupportedHints(context.getData()).keySet()) {
                            if (context.getPreselectId().equals(ID_JAVA_HINTS_PREFIX + hm.id)) {
                                toSelect = hm;
                                break;
                            }
                        }

                        for (HintMetadata hm : context.getData().getHints().keySet()) {
                            if (context.getPreselectId().equals(ID_JAVA_HINTS_PREFIX + hm.id)) {
                                toSelect = hm;
                                break;
                            }
                        }

                        HintsPanel prev = context.getPreviousComponent();
                        if (prev != null) {
                            prev.select(toSelect);
                            return prev;
                        }

                        return new HintsPanel(toSelect, context, context.getData());
                    }
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new AnalyzerImpl(context);
        }

    }
}
