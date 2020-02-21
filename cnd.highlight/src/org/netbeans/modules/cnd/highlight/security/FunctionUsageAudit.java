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

import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import static org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit.toSeverity;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.highlight.hints.ErrorInfoImpl;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.NbBundle;

/**
 *
 */
public class FunctionUsageAudit extends AbstractCodeAudit {
    private final FunctionsXmlService.Level level;
    private final FunctionsXmlService.Category category;
    private final String customType;
    
    public FunctionUsageAudit(FunctionsXmlService.Level level, FunctionsXmlService.Category category, String id, String name, String description, String defaultSeverity, String customType, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
        this.level = level;
        this.category = category;
        this.customType = customType;
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
            
            CsmFileReferences.getDefault().accept(request.getFile()
                                                 ,request.getDocument()
                                                 ,new FunctionUsageAudit.ReferenceVisitor(request, response, file)
                                                 ,CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
        }
    }
    
    private class ReferenceVisitor implements CsmFileReferences.Visitor {
        private final CsmErrorProvider.Request request;
        private final CsmErrorProvider.Response response;
        private final CsmFile file;
        
        public ReferenceVisitor(CsmErrorProvider.Request request, CsmErrorProvider.Response response, CsmFile file) {
            this.request = request;
            this.response = response;
            this.file = file;
        }
        
        @Override
        public void visit(CsmReferenceContext context) {
            CsmReference reference = context.getReference();
            if (reference != null) {
                if (!canBeUnsafe(reference.getText())) {
                    return;
                }
                CsmObject referencedObject = reference.getReferencedObject();
                String altText = null;
                if (CsmKindUtilities.isFunction(referencedObject)) {
                    altText = getAlternativesIfUnsafe((CsmFunction) referencedObject);
                } else {
                    Object platformProject = file.getProject().getPlatformProject();
                    if (platformProject instanceof NativeProject) {
                        String platformName = NativeProjectSupport.getPlatformName((NativeProject) platformProject);
                        if (platformName.equals("MacOSX") && CsmKindUtilities.isMacro(referencedObject)) { // NOI18N
                            altText = getAlternativesIfUnsafe((CsmMacro) referencedObject);
                        }
                    }
                }
                if (altText != null) {
                    CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                    String id = level.getLevel() + category.getName(); // NOI18N
                    String name = "(" + level + ") " + ((CsmNamedElement) referencedObject).getName().toString(); // NOI18N
                    String description = (altText.isEmpty())?getDescription():(getDescription()+NbBundle.getMessage(FunctionUsageAudit.class, "FunctionUsageAudit.alternative", altText)); // NOI18N
                    if (response instanceof AnalyzerResponse) {
                        ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                            new ErrorInfoImpl(SecurityCheckProvider.NAME, getName(), id+"\n"+name+"\n"+description, severity, customType, reference.getStartOffset(), reference.getEndOffset())); // NOI18N
                    } else {
                        response.addError(new ErrorInfoImpl(SecurityCheckProvider.NAME, getName(), description, severity, customType, reference.getStartOffset(), reference.getEndOffset()));
                    }
                }
            }
        }
        
        @Override
        public boolean cancelled() {
            return request.isCancelled();
        }
        
    }
    
    private boolean canBeUnsafe(CharSequence functionName) {
        for (FunctionsXmlService.RvsdFunction unsafeFunction : category.getFunctions()) {
            if (CharSequenceUtils.contentEquals(functionName, unsafeFunction.getName())) {
                return true;
            }
        }
        return false;
    }

    private String getAlternativesIfUnsafe(Object function) {
        if (function instanceof CsmOffsetable && function instanceof CsmNamedElement) {
            final CharSequence functionName = ((CsmNamedElement) function).getName();
            for (FunctionsXmlService.RvsdFunction unsafeFunction : category.getFunctions()) {
                if (CharSequenceUtils.contentEquals(functionName, unsafeFunction.getName())) {
                    CsmFile srcFile = ((CsmOffsetable) function).getContainingFile();
                    for (CsmInclude include : CsmFileInfoQuery.getDefault().getIncludeStack(srcFile)) {
                        if (include.getIncludeName().toString().equals(unsafeFunction.getHeader())) {
                            return unsafeFunction.getAlternativesString();
                        }
                    }
                }
            }
        }
        return null;
    }
    
}