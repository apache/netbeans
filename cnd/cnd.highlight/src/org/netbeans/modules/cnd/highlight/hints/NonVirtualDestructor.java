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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
class NonVirtualDestructor extends AbstractCodeAudit {
    private NonVirtualDestructor(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
    }
    
    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }
    
    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            visit(file.getDeclarations(), request, response);
        }
    }

    private void visit(Collection<? extends CsmOffsetableDeclaration> decls, CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        for(CsmOffsetableDeclaration decl : decls) {
            if (request.isCancelled()) {
                return;
            }
            if (CsmKindUtilities.isClassMember(decl)) {
                visit((CsmMember) decl, request, response);
            } else if (CsmKindUtilities.isClass(decl)) {
                visit((CsmClass) decl, request, response);
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                visit(((CsmNamespaceDefinition) decl).getDeclarations(), request, response);
            }
        }
    }
    
    private void visit(CsmMember csmMember, CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        if (CsmKindUtilities.isDestructor(csmMember)) {
            CsmMethod method = (CsmMethod)csmMember;
            if (!request.getFile().equals(method.getContainingFile())) {
                return;
            }
            if (!CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                if (!CsmTypeHierarchyResolver.getDefault().getSubTypes(method.getContainingClass(), true).isEmpty()) {
                    String message = NbBundle.getMessage(NonVirtualDestructor.class, "NonVirtualDestructor.message"); // NOI18N
                    CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                    if (response instanceof AnalyzerResponse) {
                        ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, method.getContainingFile().getFileObject(),
                                new NonVritualDestructorErrorInfoImpl(request.getDocument(), method, CsmHintProvider.NAME, getID(), getName()+"\n"+message, severity, method.getStartOffset(), method.getParameterList().getEndOffset())); // NOI18N
                    } else {
                        response.addError(new NonVritualDestructorErrorInfoImpl(request.getDocument(), method, CsmHintProvider.NAME, getID(), message, severity, method.getStartOffset(), method.getParameterList().getEndOffset()));
                    }
                }
            }
        }
    }
    private void visit(CsmClass csmClass, CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        visit(csmClass.getMembers(), request, response);
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CsmHintProvider.NAME, service = CodeAuditFactory.class, position = 1000)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(NonVirtualDestructor.class, "NonVirtualDestructor.name"); // NOI18N
            String description = NbBundle.getMessage(NonVirtualDestructor.class, "NonVirtualDestructor.description"); // NOI18N
            return new NonVirtualDestructor(id, id, description, "hint", true, preferences); // NOI18N
        }
    }
    
    private static final class NonVritualDestructorErrorInfoImpl extends ErrorInfoImpl {
        private final BaseDocument doc;
        public NonVritualDestructorErrorInfoImpl(Document doc, CsmMethod method, String providerName, String audutName, String message, Severity severity, int startOffset, int endOffset) {
            super(providerName, audutName, message, severity, startOffset, endOffset);
            this.doc = (BaseDocument) doc;
        }
    }    
    
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1400)
    public static final class VirtualDestructorFixProvider extends CsmErrorInfoHintProvider {

        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof NonVritualDestructorErrorInfoImpl) {
                alreadyFound.addAll(createFixes((NonVritualDestructorErrorInfoImpl) info));
            }
            return alreadyFound;
        }
        
        private List<? extends Fix> createFixes(NonVritualDestructorErrorInfoImpl info) {
            try {
                return Collections.singletonList(new AddVirtualKeyvord(info.doc, info.getStartOffset(), info.getEndOffset()));
            } catch (BadLocationException ex) {
                return Collections.emptyList();
            }
        }
    }
    
    private static final class AddVirtualKeyvord extends SafeFix {
        private final BaseDocument doc;
        private final Position start;

        public AddVirtualKeyvord(BaseDocument doc, int startOffset, int endOffset) throws BadLocationException {
            this.doc = doc;
            this.start = NbDocument.createPosition(doc, startOffset, Position.Bias.Forward);
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(NonVirtualDestructor.class, "NonVirtualDestructor.fix"); // NOI18N
        }

        @Override
        public ChangeInfo performFix() throws BadLocationException, Exception {
            String text = "virtual "; //NOI18N
            doc.insertString(start.getOffset(), text, null);
            return null;
        }
    }
}
