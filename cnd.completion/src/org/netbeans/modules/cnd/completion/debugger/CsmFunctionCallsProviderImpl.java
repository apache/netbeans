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

package org.netbeans.modules.cnd.completion.debugger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.FunctionCallsProvider;
import org.openide.text.NbDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=FunctionCallsProvider.class)
public class CsmFunctionCallsProviderImpl implements FunctionCallsProvider {
    @Override
    public List<CsmReference> getFunctionCalls(StyledDocument document, int line) {
        CsmCacheManager.enter();
        try {
            if (line < 0 || document == null) {
                return Collections.emptyList();
            }

            CsmFile csmFile = CsmUtilities.getCsmFile(document, false, false);
            if (csmFile == null || !csmFile.isParsed()) {
                return Collections.emptyList();
            }

            final Element lineRootElement = NbDocument.findLineRootElement(document);

            return getFunctionCalls(csmFile, lineRootElement, line);
        } finally {
            CsmCacheManager.leave();
        }
    }

    private static List<CsmReference> getFunctionCalls(final CsmFile csmFile,
                                final Element lineRootElement,
                                final int line) {
        final Element lineElem = lineRootElement.getElement(line);
        if (lineElem == null) {
            return Collections.emptyList();
        }
        int lineStartOffset = lineElem.getStartOffset();
        CsmOffsetable statementStart = getStatement(csmFile, lineStartOffset);
        if (statementStart != null) {
            lineStartOffset = statementStart.getStartOffset();
        }

        int lineEndOffset = lineElem.getEndOffset();
        CsmOffsetable statementEnd = getStatement(csmFile, lineEndOffset);
        if (statementEnd != null) {
            lineEndOffset = statementEnd.getEndOffset();
        }

        final int startOffset = lineStartOffset;
        final int endOffset = lineEndOffset;

        final List<CsmReference> res = new ArrayList<CsmReference>();
        final AtomicBoolean cancelled = new AtomicBoolean(false);

        CsmFileReferences.getDefault().accept(csmFile, null, new CsmFileReferences.Visitor() {
            @Override
            public void visit(CsmReferenceContext context) {
                if (cancelled.get()) {
                    return;
                }
                CsmReference reference = context.getReference();
                    if (startOffset <= reference.getStartOffset() && reference.getEndOffset() <= endOffset) {
                        CsmObject referencedObject = reference.getReferencedObject();
                        if (CsmKindUtilities.isFunction(referencedObject)) {
                            CsmFunction function = (CsmFunction) referencedObject;
                            if (!fromStd(function)) {
                                //String scopeName = ""; //NOI18N
                                CsmScope scope = function.getScope();
                                if (!CsmKindUtilities.isNamespace(scope) || !((CsmNamespace)scope).isGlobal()) {
                                    cancelled.set(true);
                                    return;
                                }
//                                if (CsmKindUtilities.isClass(scope)) {
//                                    scopeName = ((CsmClass)scope).getQualifiedName().toString();
//                                }
                                res.add(reference);
                                //new FunctionCall(function.getSignature().toString(),
//                                    reference.getStartOffset(),
//                                    reference.getEndOffset()));
                            }
                        }
//                          else if (AUTOS_INCLUDE_MACROS && CsmKindUtilities.isMacro(referencedObject)) {
//                            String txt = reference.getText().toString();
//                            int[] macroExpansionSpan = CsmMacroExpansion.getMacroExpansionSpan(document, reference.getStartOffset(), false);
//                            if (macroExpansionSpan != null && macroExpansionSpan[0] != macroExpansionSpan[1]) {
//                                try {
//                                    txt = document.getText(macroExpansionSpan[0], macroExpansionSpan[1] - macroExpansionSpan[0]);
//                                } catch (BadLocationException ex) {
//                                    Exceptions.printStackTrace(ex);
//                                }
//                            }
//                            autos.add(txt);
//                        }
                    }
            }

            @Override
            public boolean cancelled() {
                return false;
            }
        });
        if (cancelled.get()) {
            return Collections.emptyList();
        }
        return res;
    }

    private static CsmOffsetable getStatement(CsmFile csmFile, int offset) {
        CsmContext context = CsmOffsetResolver.findContext(csmFile, offset, null);
        CsmScope scope = context.getLastScope();

        for (CsmScopeElement csmScopeElement : scope.getScopeElements()) {
            if (CsmKindUtilities.isOffsetable(csmScopeElement)) {
                CsmOffsetable offs = (CsmOffsetable) csmScopeElement;
                if (offs.getEndOffset() >= offset) {
                    // avoid invalid and compound statements
                    if ((offs.getStartOffset() > offset) || CsmKindUtilities.isCompoundStatement(offs)) {
                        return null;
                    } else {
                        return offs;
                    }
                }
            }
        }
        return null;
    }

    private static boolean fromStd(CsmScopeElement object) {
        CsmScope scope = object.getScope();
        return CsmKindUtilities.isNamespace(scope) && "std".equals(((CsmNamespace)scope).getQualifiedName().toString()); // NOI18N
    }
}
