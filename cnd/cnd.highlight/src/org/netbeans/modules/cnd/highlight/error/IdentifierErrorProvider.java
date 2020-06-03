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

import java.util.MissingResourceException;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import static org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit.toSeverity;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider.EditorEvent;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmTemplateBasedReferencedObject;
import org.netbeans.modules.cnd.highlight.hints.ErrorInfoImpl;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides information about unresolved identifiers.
 *
 */
public final class IdentifierErrorProvider extends AbstractCodeAudit {

    private static final boolean SHOW_TIMES = Boolean.getBoolean("cnd.identifier.error.provider.times");

    private static final int MAX_ERROR_LIMIT;
    static {
        String limit = System.getProperty("cnd.highlighting.error.limit"); // NOI18N
        int userInput = 100;
        if (limit != null) {
            try {
                userInput = Integer.parseInt(limit);
            } catch (Exception e) {
                // skip
            }
        }
        MAX_ERROR_LIMIT = userInput;
    }
    private static final int UNRESOLVED = 1;
    private static final int UNRESOLVED_BUILT_IN = 2;
    private static final int UNRESOLVED_TEMPLATE = 3;
    private static final int UNRESOLVED_FORWARD = 4;
    private final int kind;
    private final String message;
    
    private IdentifierErrorProvider(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences preferences,
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
        if (!CsmErrorProvider.disableAsLibraryHeaderFile(request.getFile()) && request.getFile().isParsed()) {
            long start = System.currentTimeMillis();
            Thread currentThread = Thread.currentThread();
            CsmFile file = request.getFile();
            currentThread.setName("Provider "+getName()+" prosess "+file.getAbsolutePath()); // NOI18N
            if (SHOW_TIMES) {
                System.err.println("#@# Error Highlighting update() have started for file " + file.getAbsolutePath());
            }
            CsmFileReferences.getDefault().accept(
                    request.getFile(), request.getDocument(), new ReferenceVisitor(request, response),
                    CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
            if (SHOW_TIMES) {
                System.err.println("#@# Error Highlighting update() done in "+ (System.currentTimeMillis() - start) +"ms for file " + request.getFile().getAbsolutePath());
            }
        }
    }
    
    private class ReferenceVisitor implements CsmFileReferences.Visitor {

        private final CsmErrorProvider.Request request;
        private final CsmErrorProvider.Response response;
        private int foundError = 0;


        public ReferenceVisitor(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void visit(CsmReferenceContext context) {
            if (MAX_ERROR_LIMIT >= 0 && foundError >= MAX_ERROR_LIMIT){
                return;
            }
            if (!request.isCancelled()) {
                CsmReference ref = context.getReference();
                final CsmObject referencedObject = ref.getReferencedObject();
                if (referencedObject == null) {
                    if (CsmFileReferences.isAfterUnresolved(context)) {
                        return;
                    }
                    if (CsmFileReferences.isMacroBased(context)) {
                        return;
                    }
                    int whatKind = UNRESOLVED;
                    if (CsmFileReferences.isBuiltInBased(ref)) {
                        whatKind = UNRESOLVED_BUILT_IN;
                    } else if (CsmFileReferences.isTemplateBased(context)) {
                        whatKind = UNRESOLVED_TEMPLATE;
                    } else if (CsmKindUtilities.isClassForwardDeclaration(ref.getOwner())) { // owner is needed
                        whatKind = UNRESOLVED_FORWARD;
                    } else if (CsmKindUtilities.isEnumForwardDeclaration(ref.getOwner())) { // owner is needed
                        whatKind = UNRESOLVED_FORWARD;
                    }
                    if (whatKind == kind) {
                        addMessage(ref);
                    }
                } else if (referencedObject instanceof CsmTemplateBasedReferencedObject) {
                    if (CsmFileReferences.isAfterUnresolved(context)) {
                        return;
                    }
                    if (CsmFileReferences.isBuiltInBased(ref) || CsmFileReferences.isMacroBased(context)) {
                        return;
                    }
                    if (kind == UNRESOLVED_TEMPLATE) {
                        addMessage(ref);
                    }
                } else if (kind == UNRESOLVED_FORWARD && CsmClassifierResolver.getDefault().isForwardClassifier(referencedObject)) {
                    addMessage(ref);
                }
            }
        }

        private void addMessage(CsmReference ref) throws MissingResourceException {
            foundError++;
            if (response instanceof AnalyzerResponse) {
                String decoratedText = getID()+"\n"+NbBundle.getMessage(IdentifierErrorProvider.class, message, ref.getText().toString()); // NOI18N
                ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, request.getFile().getFileObject(),
                        new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), ref.getStartOffset(), ref.getEndOffset()));
            } else {
                String decoratedText = NbBundle.getMessage(IdentifierErrorProvider.class, message, ref.getText().toString()); // NOI18N
                response.addError(
                        new ErrorInfoImpl(CodeAssistanceHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()), ref.getStartOffset(), ref.getEndOffset()));
            }
        }

        @Override
        public boolean cancelled() {
            return request.isCancelled();
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 4200)
    public static final class UnresolvedFactory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(IdentifierErrorProvider.class, "IdentifierErrorProvider.unresolved.name"); //NOI18N
            String description = NbBundle.getMessage(IdentifierErrorProvider.class, "IdentifierErrorProvider.unresolved.description"); //NOI18N
            String message = "IdentifierErrorProvider.unresolved.message"; // NOI18N
            return new IdentifierErrorProvider(name, name, description, "error", true, preferences, UNRESOLVED, message); // NOI18N
        }
    }

    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 4250)
    public static final class UnresolvedBuildInFactory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(IdentifierErrorProvider.class, "IdentifierErrorProvider.unresolvedBuiltIn.name"); //NOI18N
            String description = NbBundle.getMessage(IdentifierErrorProvider.class, "IdentifierErrorProvider.unresolvedBuiltIn.description"); //NOI18N
            String message = "IdentifierErrorProvider.unresolvedBuiltIn.message"; // NOI18N
            return new IdentifierErrorProvider(name, name, description, "warning", false, preferences, UNRESOLVED_BUILT_IN, message); // NOI18N
        }
    }

    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 4300)
    public static final class UnresolvedTemplateFactory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(IdentifierErrorProvider.class, "IdentifierErrorProvider.unresolvedTemplate.name"); //NOI18N
            String description = NbBundle.getMessage(IdentifierErrorProvider.class, "IdentifierErrorProvider.unresolvedTemplate.description"); //NOI18N
            String message = "IdentifierErrorProvider.unresolvedTemplate.message"; // NOI18N
            return new IdentifierErrorProvider(name, name, description, "warning", true, preferences, UNRESOLVED_TEMPLATE, message); // NOI18N
        }
    }

    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 4400)
    public static final class UnresolvedForwardFactory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(IdentifierErrorProvider.class, "IdentifierErrorProvider.unresolvedForward.name"); //NOI18N
            String description = NbBundle.getMessage(IdentifierErrorProvider.class, "IdentifierErrorProvider.unresolvedForward.description"); //NOI18N
            String message = "IdentifierErrorProvider.unresolvedForward.message"; // NOI18N
            return new IdentifierErrorProvider(name, name, description, "warning", true, preferences, UNRESOLVED_FORWARD, message); // NOI18N
        }
    }
}
