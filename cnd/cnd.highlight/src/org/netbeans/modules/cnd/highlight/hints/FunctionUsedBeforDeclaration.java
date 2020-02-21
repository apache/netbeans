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

package org.netbeans.modules.cnd.highlight.hints;

import java.util.EnumSet;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider.EditorEvent;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class FunctionUsedBeforDeclaration extends AbstractCodeAudit {
    
    private FunctionUsedBeforDeclaration(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
    }
    
    @Override
    public boolean isSupportedEvent(EditorEvent kind) {
        return kind == EditorEvent.FileBased;
    }

    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            CsmFileReferences.getDefault().accept(
                    request.getFile(), request.getDocument(), new ReferenceVisitor(request, response),
                    CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
        }
    }

    private class ReferenceVisitor implements CsmFileReferences.Visitor {

        private final CsmErrorProvider.Request request;
        private final CsmErrorProvider.Response response;


        public ReferenceVisitor(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void visit(CsmReferenceContext context) {
            if (!request.isCancelled()) {
                CsmReference ref = context.getReference();
                if (CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DIRECT_USAGE))) {
                    CsmObject referencedObject = ref.getReferencedObject();
                    if (CsmKindUtilities.isFunction(referencedObject)) {
                        CsmFunction fun = (CsmFunction) referencedObject;
                        if (fun.getContainingFile() != ref.getContainingFile()) {
                            return;
                        }
                        if (fun.getStartOffset() <= ref.getStartOffset()) {
                            return;
                        }
                        if (!CsmKindUtilities.isGlobalFunction(fun)) {
                            return;
                        }
                        CsmFunction funDecl = fun.getDeclaration();
                        if(funDecl == null) {
                            return;
                        }
                        if (funDecl.getContainingFile() != ref.getContainingFile()) {
                            return;
                        }
                        if (funDecl.getStartOffset() <= ref.getStartOffset()) {
                            return;
                        }
                        CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                        String message = NbBundle.getMessage(FunctionUsedBeforDeclaration.class, "FunctionUsedBeforDeclaration.message", fun.getName()); // NOI18N
                        if (response instanceof AnalyzerResponse) {
                            ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, ref.getContainingFile().getFileObject(),
                                    new ErrorInfoImpl(CsmHintProvider.NAME, getID(), getName()+"\n"+message, severity, ref.getStartOffset(), ref.getEndOffset())); // NOI18N
                        } else {
                            response.addError(new ErrorInfoImpl(CsmHintProvider.NAME, getID(), message, severity, ref.getStartOffset(), ref.getEndOffset()));
                        }
                        
                    }
                }
            }
        }

        @Override
        public boolean cancelled() {
            return request.isCancelled();
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CsmHintProvider.NAME, service = CodeAuditFactory.class, position = 3000)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(FunctionUsedBeforDeclaration.class, "FunctionUsedBeforDeclaration.name"); // NOI18N
            String description = NbBundle.getMessage(FunctionUsedBeforDeclaration.class, "FunctionUsedBeforDeclaration.description"); // NOI18N
            return new FunctionUsedBeforDeclaration(id, id, description, "warning", false, preferences); // NOI18N
        }
    }
}
