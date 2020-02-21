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

package org.netbeans.modules.cnd.highlight.error;

import java.awt.event.InputEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInclude.IncludeState;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider.EditorEvent;
import org.netbeans.modules.cnd.highlight.hints.ErrorInfoImpl;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public final class IncludeErrorProvider extends AbstractCodeAudit {
    private static final int UNRESOLVED = 1;
    private static final int UNRESOLVED_INSIDE = 2;
    private static final int RECURSIVE = 3;
    private static final int ERRIR_DIRECTIVE = 4;
    private final int kind;
    private final String message;
    
    private IncludeErrorProvider(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences preferences,
            int kind, String message) {
        super(id, name, description, defaultSeverity, defaultEnabled, preferences);
        this.kind =kind;
        this.message = message;
    }

    @Override
    public boolean isSupportedEvent(EditorEvent kind) {
        return kind == EditorEvent.DocumentBased || kind == EditorEvent.FileBased;
    }

    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        Thread currentThread = Thread.currentThread();
        currentThread.setName("Provider "+getName()+" prosess "+file.getAbsolutePath()); // NOI18N
        if (request.isCancelled()) {
            return;
        }
        if (kind == RECURSIVE || kind == UNRESOLVED) {
            for(CsmInclude incl : CsmFileInfoQuery.getDefault().getBrokenIncludes(file)) {
                if (request.isCancelled()) {
                    return;
                }
                if (incl.getIncludeState() == IncludeState.Recursive) {
                    if (kind == RECURSIVE) {
                        if (response instanceof AnalyzerResponse) {
                            String decoratedText = getID()+"\n"+NbBundle.getMessage(IncludeErrorProvider.class, message); // NOI18N
                            ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                                    new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), incl.getStartOffset(), incl.getEndOffset()));
                        } else {
                            String decoratedText = decorateWithExtraHyperlinkTip(NbBundle.getMessage(IncludeErrorProvider.class, message));
                            response.addError(
                                    new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), incl.getStartOffset(), incl.getEndOffset()));
                        }
                    }
                } else if(incl.getIncludeFile() == null) {
                    if (kind == UNRESOLVED) {
                        if (response instanceof AnalyzerResponse) {
                            String decoratedText = getID()+"\n"+NbBundle.getMessage(IncludeErrorProvider.class, message, getIncludeText(incl)); // NOI18N
                            ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                                    new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), incl.getStartOffset(), incl.getEndOffset()));
                        } else {
                            String decoratedText = decorateWithExtraHyperlinkTip(NbBundle.getMessage(IncludeErrorProvider.class, message, getIncludeText(incl)));
                            response.addError(
                                    new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), incl.getStartOffset(), incl.getEndOffset()));
                        }
                    }
                }
            }
        }
        if (request.isCancelled()) {
            return;
        }
        if (kind == ERRIR_DIRECTIVE) {
            for (CsmErrorDirective error : file.getErrors()) {
                if (request.isCancelled()) {
                    return;
                }
                if (response instanceof AnalyzerResponse) {
                    String decoratedText = getID()+"\n"+NbBundle.getMessage(IncludeErrorProvider.class, message); // NOI18N
                    ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                            new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), error.getStartOffset(), error.getEndOffset()));
                } else {
                    String decoratedText = NbBundle.getMessage(IncludeErrorProvider.class, message);
                    response.addError(
                           new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), error.getStartOffset(), error.getEndOffset()));
                }
            }
        }
        if (request.isCancelled()) {
            return;
        }
        if (kind == UNRESOLVED_INSIDE) {
            Collection<CsmFile> visited = new HashSet<>();
            for (CsmInclude incl : file.getIncludes()) {
                if (request.isCancelled()) {
                    return;
                }
                if (incl.getIncludeState() != IncludeState.Recursive) {
                    CsmFile newFile = incl.getIncludeFile();
                    if (newFile != null && hasBrokenIncludes(newFile, visited)) {
                        if (response instanceof AnalyzerResponse) {
                            String decoratedText = getID()+"\n"+NbBundle.getMessage(IncludeErrorProvider.class, message, getIncludeText(incl)); // NOI18N
                            ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                                    new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), incl.getStartOffset(), incl.getEndOffset()));
                        } else {
                            String decoratedText = decorateWithExtraHyperlinkTip(NbBundle.getMessage(IncludeErrorProvider.class, message, getIncludeText(incl)));
                            response.addError(
                                    new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), incl.getStartOffset(), incl.getEndOffset()));
                        }
                    }
                }
            }
        }
    }

    private static String decorateWithExtraHyperlinkTip(String tooltip) {
        Preferences prefs = MimeLookup.getLookup(MIMENames.CPLUSPLUS_MIME_TYPE).lookup(Preferences.class);
        int altShortCut = prefs.getInt(SimpleValueNames.HYPERLINK_ACTIVATION_MODIFIERS, InputEvent.CTRL_DOWN_MASK);
        return NbBundle.getMessage(IncludeErrorProvider.class, "HighlightProvider_HyperlinkActionsHints", tooltip,  InputEvent.getModifiersExText(altShortCut));// NOI18N
    }

    private static String getIncludeText(CsmInclude incl){
        if (incl.isSystem()){
            return "<"+incl.getIncludeName()+">"; // NOI18N
        }
        return "\""+incl.getIncludeName()+"\""; // NOI18N
    }

    private boolean hasBrokenIncludes(CsmFile file, Collection<CsmFile> visited) {
        if (!file.isValid() || visited.contains(file)) {
            return false;
        }
        visited.add(file);
        if (CsmFileInfoQuery.getDefault().hasBrokenIncludes(file)) {
            return true;
        }
        for (CsmInclude incl : file.getIncludes()) {
            CsmFile newFile = incl.getIncludeFile();
            if (newFile != null && hasBrokenIncludes(newFile, visited)) {
                return true;
            }
        }
        return false;
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 4000)
    public static final class UnresolvedFactory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(IncludeErrorProvider.class, "IncludeErrorProvider.unresolved.name"); // NOI18N
            String description = NbBundle.getMessage(IncludeErrorProvider.class, "IncludeErrorProvider.unresolved.description"); // NOI18N
            String message = "IncludeErrorProvider.unresolved.message"; // NOI18N
            return new IncludeErrorProvider(id, id, description, "error", true, preferences, UNRESOLVED, message); // NOI18N
        }
    }
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 4010)
    public static final class InsideUnresolvedFactory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(IncludeErrorProvider.class, "IncludeErrorProvider.insideUnresolved.name"); // NOI18N
            String description = NbBundle.getMessage(IncludeErrorProvider.class, "IncludeErrorProvider.insideUnresolved.description"); // NOI18N
            String message = "IncludeErrorProvider.insideUnresolved.message"; // NOI18N
            return new IncludeErrorProvider(id, id, description, "warning", true, preferences, UNRESOLVED_INSIDE, message); // NOI18N
        }
    }
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 4020)
    public static final class RecursiveFactory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(IncludeErrorProvider.class, "IncludeErrorProvider.recursive.name"); // NOI18N
            String description = NbBundle.getMessage(IncludeErrorProvider.class, "IncludeErrorProvider.recursive.description"); // NOI18N
            String message = "IncludeErrorProvider.recursive.message"; // NOI18N
            return new IncludeErrorProvider(id, id, description, "warning", true, preferences, RECURSIVE, message); // NOI18N
        }
    }
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 4030)
    public static final class ErrorFactory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(IncludeErrorProvider.class, "IncludeErrorProvider.error.name"); // NOI18N
            String description = NbBundle.getMessage(IncludeErrorProvider.class, "IncludeErrorProvider.error.description"); // NOI18N
            String message = "IncludeErrorProvider.error.message"; // NOI18N
            return new IncludeErrorProvider(id, id, description, "error", true, preferences, ERRIR_DIRECTIVE, message); // NOI18N
        }
    }
}
