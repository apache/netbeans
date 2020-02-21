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

package org.netbeans.modules.cnd.highlight.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.analysis.api.AbstractAnalyzer;
import org.netbeans.modules.cnd.analysis.api.AbstractHintsPanel;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class SecurityAnalyzerImpl extends AbstractAnalyzer {
    private static final String PREFIX = "cnd-"; //NOI18N

    private SecurityAnalyzerImpl(Context ctx) {
        super(ctx);
    }

    @Override
    protected CsmErrorProvider getErrorProvider(Preferences preferences) {
        if (preferences != null) {
            return new SecurityCheckProvider(preferences);
        } else {
            return (CsmErrorProvider) SecurityCheckProvider.getInstance();
        }
    }

    @Override
    protected Collection<? extends ErrorDescription> doRunImpl(final FileObject sr, final Context ctx, final CsmErrorProvider provider, final AtomicBoolean cancel) {
        final CsmFile csmFile = CsmUtilities.getCsmFile(sr, false, false);
        if (csmFile == null) {
            return Collections.<ErrorDescription>emptyList();
        }
        CsmErrorProvider.Request request = new AbstractAnalyzer.RequestImpl(csmFile, ctx, cancel);
        final ArrayList<ErrorDescription> res = new ArrayList<>();
        CsmErrorProvider.Response responce = new ResponseImpl(sr, res, cancel);
        provider.getErrors(request, responce);
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
            String messages[] = errorInfo.getMessage().split("\n");
            if (messages.length >=3 ) {
                String abbr = messages[0];
                LazyFixList list = new AbstractAnalyzer.LazyFixListImpl();
                StringBuilder buf = new StringBuilder("<pre>"); //NOI18N
                boolean first = true;
                for(int i = 2; i < messages.length; i++) {
                    if (first) {
                        first = false;
                    } else {
                        buf.append("<br>"); //NOI18N
                    }
                    buf.append(messages[i]);
                }
                buf.append("</pre>"); //NOI18N
                String message = messages[1];
                return ErrorDescriptionFactory.createErrorDescription(PREFIX+abbr
                                                                     ,Severity.ERROR
                                                                     ,message
                                                                     ,buf.toString()
                                                                     ,list
                                                                     ,fo
                                                                     ,errorInfo.getStartOffset()
                                                                     ,errorInfo.getStartOffset());
            }
            return null;
        }
    }
    
    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class AnalyzerFactoryImpl extends AnalyzerFactory {
        
        public AnalyzerFactoryImpl() {
            super(SecurityCheckProvider.NAME,
                  NbBundle.getMessage(SecurityCheckProvider.class, "SecurityCheck_DESCRIPTION"), //NOI18N
                  ImageUtilities.loadImage("org/netbeans/modules/cnd/highlight/resources/bugs.png")); //NOI18N
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            List<WarningDescription> result = new ArrayList<>();
            final SecurityCheckProvider provider = (SecurityCheckProvider)SecurityCheckProvider.getInstance();
            for(CodeAudit audit : provider.getAudits()) {
                result.add(WarningDescription.create(PREFIX+audit.getID(), audit.getName(), SecurityCheckProvider.NAME, SecurityCheckProvider.NAME));
            }
            return result;
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
                return new SecurityCheckProvider(preferences);
            } else {
                return (CodeAuditProvider)SecurityCheckProvider.getInstance();
            }
        }
        
        @Override
        public Analyzer createAnalyzer(Context context) {
            return new SecurityAnalyzerImpl(context);
        }
    }
}
