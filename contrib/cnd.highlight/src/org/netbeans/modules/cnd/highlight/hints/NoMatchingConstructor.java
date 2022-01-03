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
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import static org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit.toSeverity;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.NbBundle;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.modules.cnd.refactoring.api.ui.CsmRefactoringActionsFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 */
class NoMatchingConstructor extends AbstractCodeAudit {    
    private NoMatchingConstructor(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
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
        for (CsmOffsetableDeclaration decl : decls) {
            if (CsmKindUtilities.isClass(decl)) {
                visit((CsmClass) decl, request, response);
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                visit(((CsmNamespaceDefinition) decl).getDeclarations(), request, response);
            }
        }
    }
    
    private void visit(CsmClass csmClass, CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        if (!request.getFile().equals(csmClass.getContainingFile())) {
            return;
        }
        
        for (CsmMember member : csmClass.getMembers()) {
            if (CsmKindUtilities.isConstructor(member)) {
                return;
            }
        }
        
        boolean willProduceError = false;
        for (CsmInheritance parent : csmClass.getBaseClasses()) {
            CsmClass parentClass = CsmInheritanceUtilities.getCsmClass(parent);
            if (parentClass != null) {
                boolean flag = false;
                for (CsmMember member : parentClass.getMembers()) {
                    if (CsmKindUtilities.isConstructor(member)) {
                        CsmConstructor testedCtor = (CsmConstructor) member;
                        Collection<CsmParameter> parameters = testedCtor.getParameters();
                        if (parameters.isEmpty()) {
                            CsmFunctionDefinition definition = testedCtor.getDefinition();
                            if (definition != null) {
                                flag = (definition.getDefinitionKind() == CsmFunctionDefinition.DefinitionKind.DELETE);
                            }
                            break;
                        } else {
                            CsmParameter first = parameters.iterator().next();
                            if (first.getInitialValue() != null || first.isVarArgs()) {
                                CsmFunctionDefinition definition = testedCtor.getDefinition();
                                flag = (definition.getDefinitionKind() == CsmFunctionDefinition.DefinitionKind.DELETE);
                            } else {
                                flag = true;
                            }
                        }
                    }
                }
                if (!willProduceError) {
                    willProduceError = flag;
                }
                break;
            }
        }
        
        if (willProduceError && CsmFileInfoQuery.getDefault().isCpp11OrLater(request.getFile())) {
            for (CsmMember member : csmClass.getMembers()) {
                if (member.getKind().equals(CsmDeclaration.Kind.USING_DECLARATION) && CsmKindUtilities.isUsingDeclaration(member)) {
                    CsmDeclaration referencedDeclaration = ((CsmUsingDeclaration)member).getReferencedDeclaration();
                    if (CsmKindUtilities.isConstructor(referencedDeclaration)) {
                        CsmClass ctorClass = ((CsmConstructor) referencedDeclaration).getContainingClass();
                        for (CsmInheritance parent : csmClass.getBaseClasses()) {
                            CsmClass parentClass = CsmInheritanceUtilities.getCsmClass(parent);
                            if (parentClass != null && parentClass.equals(ctorClass)) {
                                willProduceError = false;
                                break;
                            }
                        }
                    }
                }
                if (!willProduceError) {
                    break;
                }
            }
        }
        
        if (willProduceError) {
            String message = NbBundle.getMessage(NoMatchingConstructor.class, "NoMatchingConstructor.description"); // NOI18N
            CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
            if (response instanceof AnalyzerResponse) {
                ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, csmClass.getContainingFile().getFileObject(),
                    new NoMatchingConstructor.NoMatchingConstructorErrorInfoImpl(request.getDocument(), csmClass, CsmHintProvider.NAME, getID(), getName()+"\n"+message, severity, csmClass.getStartOffset(), csmClass.getLeftBracketOffset()));  // NOI18N
            } else {
                response.addError(new NoMatchingConstructor.NoMatchingConstructorErrorInfoImpl(request.getDocument(), csmClass, CsmHintProvider.NAME, getID(), message, severity, csmClass.getStartOffset(), csmClass.getLeftBracketOffset()));
            }
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CsmHintProvider.NAME, service = CodeAuditFactory.class, position = 6000)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(NoMatchingConstructor.class, "NoMatchingConstructor.name");  // NOI18N
            String description = NbBundle.getMessage(NoMatchingConstructor.class, "NoMatchingConstructor.description");  // NOI18N
            return new NoMatchingConstructor(id, id, description, "error", true, preferences);  // NOI18N
        }
    }
    
    private static final class NoMatchingConstructorErrorInfoImpl extends ErrorInfoImpl {
        private final BaseDocument doc;
        private final CsmClass clazz;
        
        public NoMatchingConstructorErrorInfoImpl(Document doc, CsmClass csmClass, String providerName, String audutName, String message, CsmErrorInfo.Severity severity, int startOffset, int endOffset) {
            super(providerName, audutName, message, severity, startOffset, endOffset);
            this.doc = (BaseDocument) doc;
            this.clazz = csmClass;
        }
    } 
    
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1300)
    public static final class NoMatchingConstructorFixProvider extends CsmErrorInfoHintProvider {

        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof NoMatchingConstructorErrorInfoImpl) {
                alreadyFound.addAll(createFixes((NoMatchingConstructorErrorInfoImpl) info));
            }
            return alreadyFound;
        }
        
        private List<? extends Fix> createFixes(NoMatchingConstructorErrorInfoImpl info) {
            try {
                return Collections.singletonList(new AddConstructor(info.doc, info.clazz, info.getStartOffset(), info.clazz.getEndOffset()));
            } catch (BadLocationException ex) {
                return Collections.emptyList();
            }
        }
    }
    
    private static final class AddConstructor implements Fix {
        private final BaseDocument doc;
        private final int startOffset;
        private final int endOffset;
        private final CsmClass clazz;

        public AddConstructor(BaseDocument doc, CsmClass csmClass, int startOffset, int endOffset) throws BadLocationException {
            this.doc = doc;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.clazz = csmClass;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(NoMatchingConstructor.class, "NoMatchingConstructor.fix");  // NOI18N
        }

        @Override
        public ChangeInfo implement() throws Exception {
            int carretOffset = 0;
            
            // get carret offset
            for (CsmMember member : clazz.getMembers()) {
                if (member.getVisibility() == CsmVisibility.PUBLIC) {
                    int memberOffset = member.getStartOffset();
                    carretOffset = memberOffset - CsmFileInfoQuery.getDefault().getLineColumnByOffset(clazz.getContainingFile(), memberOffset)[1];
                }
            }
            
            CsmContext context = CsmContext.create(doc, startOffset, endOffset, carretOffset);
            InstanceContent ic = new InstanceContent();
            ic.add(context);
            ic.add(EditorRegistry.lastFocusedComponent());
            Lookup lookup = new AbstractLookup(ic);
            
            CsmCacheManager.enter();
            CsmRefactoringActionsFactory.showConstructorsGenerator(lookup);
            CsmCacheManager.leave();
            return null;
        }
    }
}
