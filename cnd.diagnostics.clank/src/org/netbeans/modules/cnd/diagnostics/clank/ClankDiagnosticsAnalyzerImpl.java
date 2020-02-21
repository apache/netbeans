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
package org.netbeans.modules.cnd.diagnostics.clank;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import org.clang.tools.services.ClankDiagnosticEnhancedFix;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.clang.tools.services.checkers.api.ClankWarningsProvider;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.cnd.analysis.api.AbstractAnalyzer;
import org.netbeans.modules.cnd.analysis.api.AbstractHintsPanel;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public final class ClankDiagnosticsAnalyzerImpl extends AbstractAnalyzer {
    private static final String PREFIX = "cnd-"; //NOI18N

    private ClankDiagnosticsAnalyzerImpl(Context ctx) {
        super(ctx);
    }

    @Override
    protected CsmErrorProvider getErrorProvider(Preferences preferences) {
        if (preferences != null) {
            return new ClankDiagnoticsErrorProvider(preferences);
        } else {
            return (CsmErrorProvider) ClankDiagnoticsErrorProvider.getInstance();
        }
    }

    @Override
    protected Collection<? extends ErrorDescription> doRunImpl(final FileObject sr, final Context ctx, 
            final CsmErrorProvider provider, final AtomicBoolean cancel) {
        final CsmFile csmFile = CsmUtilities.getCsmFile(sr, false, false);
        if (csmFile == null) {
            return Collections.<ErrorDescription>emptyList();
        }
        CsmErrorProvider.Request request = new AbstractAnalyzer.RequestImpl(csmFile, ctx, cancel);
        final ArrayList<ErrorDescription> res = new ArrayList<>();
        CsmErrorProvider.Response response = new ResponseImpl(sr, res, cancel);
        provider.getErrors(request, response);
        return res;
    }

    @Override
    protected boolean isCompileUnitBased() {
        return false;
    }
    
    private static class ResponseImpl extends AbstractAnalyzer.AbstractResponse {

        public ResponseImpl(FileObject sr, ArrayList<ErrorDescription> res, AtomicBoolean cancel) {
            super(sr, res, cancel);
        }
        
        @Override
        protected ErrorDescription addErrorImpl(CsmErrorInfo errorInfo, FileObject fo) {
            if (!(errorInfo instanceof ClankCsmErrorInfo)) {
                return null;
            }
            final ClankCsmErrorInfo info
                    = (ClankCsmErrorInfo) errorInfo;
            String message = info.getMessage();
            // String messages[] = errorInfo.getMessage().split("\n");
//            if (messages.length >=3 ) {
//                String abbr = messages[0];
            final List<Fix> fixes = new ArrayList<>();
            final ClankDiagnosticInfo fix = ((ClankCsmErrorInfo) info).getDelegate();
            if (!fix.fixes().isEmpty()) {
                try {
                    ClankEnhancedFix fixImpl
                            = new ClankEnhancedFix(((ClankCsmErrorInfo) info).getCsmFile(),  fix.fixes());
                    fixes.add(fixImpl);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            LazyFixList list = new LazyFixList() {
                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {

                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {

                }

                @Override
                public boolean probablyContainsFixes() {
                    return info.getDelegate().hasFixes();
                }

                @Override
                public List<Fix> getFixes() {
                    return fixes;
                }

                @Override
                public boolean isComputed() {
                    return true;
                }
            };
            String category = fix.getCategory();
            List<String> asList = Arrays.asList(ClankWarningsProvider.getCategories());
            if (category == null || category.trim().isEmpty() || !asList.contains(category)) {
                category = "clank_unknown";//NOI18N
            }
            return ErrorDescriptionFactory.createErrorDescription(category,
                     Severity.ERROR,
                     list.probablyContainsFixes() ? "clank-diagnostics-annotations-fixable" : "clank-diagnostics-annotations",//NOI18N
                     message,
                     message,
                     list,
                     fo,
                     info.getDelegate().getStartOffsets(),
                     info.getDelegate().getEndOffsets());
//            }
//            return null;
        }
    }
   
    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class AnalyzerFactoryImpl extends AnalyzerFactory {

        private static final List<WarningDescription> WARNING_DESCRIPTIONS = new ArrayList<>();
        static {
//            WARNING_DESCRIPTIONS.add(WarningDescription.create("clank", NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_DESCRIPTION") ,//NOI18N
//                    "clank", NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_DESCRIPTION")));//NOI18N
            WARNING_DESCRIPTIONS.add(WarningDescription.create("clank_unknown", NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "clank_unknown") ,//NOI18N
                    "clank", NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_DESCRIPTION")));//NOI18N
            for (String category : ClankWarningsProvider.getCategories()) {
                WARNING_DESCRIPTIONS.add(WarningDescription.create(category, category ,//NOI18N
                        "clank", NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_DESCRIPTION")));//NOI18N
            }
        }
        public AnalyzerFactoryImpl() {
            super(ClankDiagnoticsErrorProvider.NAME,
                  NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_DESCRIPTION"), //NOI18N
                  ImageUtilities.loadImage("org/netbeans/modules/cnd/diagnostics/clank/resources/bugs.png")); //NOI18N
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return WARNING_DESCRIPTIONS;
        }

        @Override
        public CustomizerProvider<Void, AbstractHintsPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, AbstractHintsPanel>() {

                @Override
                public Void initialize() {
                    return null;
                }

                @Override
                public AbstractHintsPanel createComponent(CustomizerContext<Void, AbstractHintsPanel> context) {
                    AbstractHintsPanel result = null;
                    if (context != null) {
                        result = context.getPreviousComponent();
                    }

                    if (result == null) {
                        result = AbstractAnalyzer.createComponent(getErrorProvider(context.getSettings()));
                    } else {
                        if (context != null) {
                            result.setSettings(getErrorProvider(context.getSettings()).getPreferences().getPreferences());
                        }
                    }

                    return result;
                }
            };
        }

        private CodeAuditProvider getErrorProvider(Preferences preferences) {
            if (preferences != null) {
                return new ClankDiagnoticsErrorProvider(preferences);
            } else {
                return (CodeAuditProvider)ClankDiagnoticsErrorProvider.getInstance();
            }
        }
        
        @Override
        public Analyzer createAnalyzer(Context context) {
            return new ClankDiagnosticsAnalyzerImpl(context);
        }
    }
    
}
