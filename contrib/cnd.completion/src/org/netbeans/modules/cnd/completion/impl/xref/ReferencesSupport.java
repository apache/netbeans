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
package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionPointerType;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmGotoStatement;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmLabelResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver.Scope;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmDefineHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmIncludeHyperlinkProvider;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver.QueryScope;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetUtilities;
import static org.netbeans.modules.cnd.completion.impl.xref.Bundle.*;
import org.netbeans.modules.cnd.debug.CndDiagnosticProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 *
 */
public final class ReferencesSupport {

    private static ReferencesSupport instance = new ReferencesSupport();

    private ReferencesSupport() {
        progressListener = new CsmProgressAdapter() {

            @Override
            public void fileParsingFinished(CsmFile file) {
                clearFileReferences(file);
            }

            @Override
            public void projectParsingFinished(CsmProject project) {
                clearFileReferences(null);
            }
        };
        CsmListeners.getDefault().addProgressListener(progressListener);
    }

    public static ReferencesSupport instance() {
        return instance;
    }

    /**
     * converts (line, col) into offset. Line and column info are 1-based, so
     * the start of document is (1,1)
     */
    public static int getDocumentOffset(BaseDocument doc, int lineIndex, int colIndex) {
        return LineDocumentUtils.getLineStartFromIndex(doc, lineIndex - 1) + (colIndex - 1);
    }

    public CsmObject findReferencedObject(CsmFile csmFile, BaseDocument doc, int offset) {
        return findReferencedObject(csmFile, doc, offset, null, null);
    }

    /*package*/ static CsmObject findOwnerObject(CsmFile csmFile, int offset, TokenItem<TokenId> token,
            FileReferencesContext fileReferencesContext) {
        CsmContext context = CsmOffsetResolver.findContext(csmFile, offset, fileReferencesContext);
        CsmObject out = context.getLastObject();
        return out;
    }

    /*package*/ static CsmObject findClosestTopLevelObject(CsmFile csmFile, int offset, TokenItem<TokenId> token,
            FileReferencesContext fileReferencesContext) {
        CsmContext context = CsmOffsetResolver.findContext(csmFile, offset, fileReferencesContext);
        CsmObject out = context.getLastObject();
        if (CsmKindUtilities.isType(out) || CsmKindUtilities.isTemplateParameter(out)) {
            out = context.getLastScope();
        }
        return out;
    }

    /*package*/ CsmObject findReferencedObject(CsmFile csmFile, final BaseDocument doc,
            final int offset, TokenItem<TokenId> jumpToken, FileReferencesContext fileReferencesContext) {
        long fileVersionOnStartResolving = CsmFileInfoQuery.getDefault().getFileVersion(csmFile);
        CsmObject csmItem = null;
        // emulate hyperlinks order
        // first ask includes handler if offset in include sring token
        CsmInclude incl = null;
        if (jumpToken == null) {
            doc.readLock();
            try {
                jumpToken = CndTokenUtilities.getTokenCheckPrev(doc, offset);
            } finally {
                doc.readUnlock();
            }
        }
        if (jumpToken != null) {
            final TokenId id = jumpToken.id();
            if(id instanceof CppTokenId) {
                switch ((CppTokenId)id) {
                    case PREPROCESSOR_INCLUDE:
                    case PREPROCESSOR_INCLUDE_NEXT:
                        // look for include directive
                        csmItem = findInclude(csmFile, offset);
                        break;
                    case PREPROCESSOR_SYS_INCLUDE:
                    case PREPROCESSOR_USER_INCLUDE:
                        // look for include file
                        csmItem = findInclude(csmFile, offset);
                        if (csmItem != null) {
                            csmItem = ((CsmInclude)csmItem).getIncludeFile();
                        }
                        break;
                    case PREPROCESSOR_IDENTIFIER:
                        csmItem = findDefine(doc, csmFile, jumpToken, offset);
                        break;
                }
            }
        }

        // if failed => ask declarations handler
        if (csmItem == null && jumpToken != null) {
            int key = jumpToken.offset();
            if (key < 0) {
                key = offset;
            }
            csmItem = getReferencedObject(csmFile, jumpToken, fileVersionOnStartResolving);
            if (csmItem == null) {
                csmItem = findDeclaration(csmFile, doc, jumpToken, key, fileReferencesContext);
                if (csmItem == null) {
                    putReferencedObject(csmFile, jumpToken, ReferencesCache.UNRESOLVED, fileVersionOnStartResolving);
                } else {
                    putReferencedObject(csmFile, jumpToken, csmItem, fileVersionOnStartResolving);
                }
            } else if (csmItem == ReferencesCache.UNRESOLVED) {
                csmItem = null;
            }
        }
        return csmItem;
    }

    public static CsmObject findDefine(Document doc, CsmFile csmFile, TokenItem<TokenId> tokenUnderOffset, int offset) {
        assert (csmFile != null);
        assert (doc != null);
        DefineImpl define = getDefine(doc, csmFile, offset);
        if(define != null) {
            for (DefineParameterImpl param : define.getParams()) {
                if(param.getText().equals(tokenUnderOffset.text())) {
                    return param;
                }
            }
        }
        return null;
    }

    public static CsmErrorDirective findErrorDirective(CsmFile csmFile, int offset) {
        assert (csmFile != null);
        return CsmOffsetUtilities.findObject(csmFile.getErrors(), null, offset);
    }

    public static CsmInclude findInclude(CsmFile csmFile, int offset) {
        assert (csmFile != null);
        return CsmOffsetUtilities.findObject(csmFile.getIncludes(), null, offset);
    }

    public static CsmObject findDeclaration(final CsmFile csmFile, final Document doc,
            TokenItem<TokenId> tokenUnderOffset, final int offset) {
        return findDeclaration(csmFile, doc, tokenUnderOffset, offset, null);
    }

    private static CsmObject findDeclaration(final CsmFile csmFile, final Document doc,
            TokenItem<TokenId> tokenUnderOffset, final int offset, FileReferencesContext fileReferencesContext) {
        final String oldName = Thread.currentThread().getName();
        boolean restoreThreadName = false;
        try {
            // Add resolve position to the thread name for logging purposes
            if (csmFile != null && CndUtils.isDebugMode()) {
                try {
                    String position = "[" + offset + "]"; // NOI18N
                    if (doc instanceof LineDocument) {
                        LineDocument lineDoc = (LineDocument) doc;
                        position = "[" + (LineDocumentUtils.getLineIndex(lineDoc, offset) + 1) + "," // NOI18N
                                   + (offset - LineDocumentUtils.getLineStart(lineDoc, offset) + 1) + "]"; // NOI18N
                    }
                    Thread.currentThread().setName(
                        oldName + " (find declaration at "  // NOI18N
                        + csmFile.getAbsolutePath() + position
                        + ", token \"" + (tokenUnderOffset != null ? tokenUnderOffset.text() : "<unknown>") + "\")" // NOI18N
                    );
                    restoreThreadName = true;
                } catch (BadLocationException ex) {
                    // Just ignore it
                }
            }

            // fast check, if possible
            // macros have max priority in file
            List<CsmReference> macroUsages = CsmFileInfoQuery.getDefault().getMacroUsages(csmFile, doc, Interrupter.DUMMY);
            CsmObject csmItem = findMacro(macroUsages, offset);
            if (csmItem != null) {
                return csmItem;
            }
            CsmObject objUnderOffset = CsmOffsetResolver.findObject(csmFile, offset, fileReferencesContext);
            // TODO: it would be great to check position in named element, but we don't
            // support this information yet, so

            // fast check for enumerators
            if (CsmKindUtilities.isEnumerator(objUnderOffset)) {
                CsmEnumerator enmrtr = (CsmEnumerator) objUnderOffset;
                if (enmrtr.getExplicitValue() == null) {
                    csmItem = enmrtr;
                }
            } else if (CsmKindUtilities.isLabel(objUnderOffset)) {
                csmItem = objUnderOffset;
            } else if (CsmKindUtilities.isGotoStatement(objUnderOffset)) {
                CsmGotoStatement csmGoto = (CsmGotoStatement) objUnderOffset;
                CsmScope scope = csmGoto.getScope();
                while (scope != null && CsmKindUtilities.isScopeElement(scope) && !CsmKindUtilities.isFunctionDefinition(scope)) {
                    scope = ((CsmScopeElement) scope).getScope();
                }
                if (CsmKindUtilities.isFunctionDefinition(scope)) {
                    Collection<CsmReference> labels = CsmLabelResolver.getDefault().getLabels(
                            (CsmFunctionDefinition) scope, csmGoto.getLabel(),
                            EnumSet.of(CsmLabelResolver.LabelKind.Definiton));
                    if (!labels.isEmpty()) {
                        csmItem = labels.iterator().next().getReferencedObject();
                    }
                }
    // Commented because goto statements could be expression based.
    // In such case there are inner identifiers
    //            if (csmItem == null) {
    //                // Exit now, don't look for variables, types and etc.
    //                return null;
    //            }
            } else if (CsmKindUtilities.isVariable(objUnderOffset) || CsmKindUtilities.isTypedef(objUnderOffset) || CsmKindUtilities.isTypeAlias(objUnderOffset)) {
                CsmType type = CsmKindUtilities.isVariable(objUnderOffset) ? ((CsmVariable) objUnderOffset).getType() : ((CsmTypedef) objUnderOffset).getType();
                CsmParameter parameter = null;
                boolean repeat;
                do {
                    repeat = false;
                    if (CsmOffsetUtilities.isInObject(type, offset)) {
                        parameter = null;
                    }
                    if (CsmKindUtilities.isFunctionPointerType(type)) {
                        CsmParameter deeperParameter = CsmOffsetUtilities.findObject(
                                ((CsmFunctionPointerType) type).getParameters(), null, offset);
                        if (deeperParameter != null) {
                            parameter = deeperParameter;
                            type = deeperParameter.getType();
                            repeat = true;
                        }
                    }
                    if (type != null && !type.getInstantiationParams().isEmpty()) {
                        CsmSpecializationParameter param = CsmOffsetUtilities.findObject(type.getInstantiationParams(), null, offset);
                        if (param != null && !CsmOffsetUtilities.sameOffsets(type, param)) {
                            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                                type = ((CsmTypeBasedSpecializationParameter) param).getType();
                                repeat = true;
                            }
                        }
                    }
                } while (repeat);
                csmItem = parameter;

                if (csmItem == null && CsmKindUtilities.isVariableDeclaration(objUnderOffset)) {
                    if (tokenUnderOffset == null && doc instanceof AbstractDocument) {
                        ((AbstractDocument) doc).readLock();
                        try {
                            tokenUnderOffset = CndTokenUtilities.getTokenCheckPrev(doc, offset);
                        } finally {
                            ((AbstractDocument) doc).readUnlock();
                        }
                    }
                    if (tokenUnderOffset != null) {
                        // turned off, due to the problems like
                        // Cpu MyCpu(type, 0, amount);
                        // initialization part is part of variable => we need info about name position exactly
                        CsmVariable var = (CsmVariable) objUnderOffset;
                        CharSequence name = var.getName();
                        if (name != null) {
                            if (name.length() > 0 && tokenUnderOffset.text().toString().equals(name.toString()) && !var.isExtern()) {
                                // not work yet for arrays declarations IZ#130678
                                // not work yet for elements with init value IZ#130684
                                if ((var.getInitialValue() == null) && (var.getType() != null) && (var.getType().getArrayDepth() == 0)) {
                                    csmItem = var;
                                }
                            }
                        }
                    }
                }
            } else if (CsmKindUtilities.isType(objUnderOffset)) {
                CsmType type = (CsmType)objUnderOffset;
                CsmParameter parameter = null;
                boolean repeat;
                do {
                    repeat = false;
                    if (CsmOffsetUtilities.isInObject(type, offset)) {
                        parameter = null;
                    }
                    if (CsmKindUtilities.isFunctionPointerType(type)) {
                        CsmParameter deeperParameter = CsmOffsetUtilities.findObject(
                                ((CsmFunctionPointerType) type).getParameters(), null, offset);
                        if (deeperParameter != null) {
                            parameter = deeperParameter;
                            type = deeperParameter.getType();
                            repeat = true;
                        }
                    }
                    if (!type.getInstantiationParams().isEmpty()) {
                        CsmSpecializationParameter param = CsmOffsetUtilities.findObject(type.getInstantiationParams(), null, offset);
                        if (param != null && !CsmOffsetUtilities.sameOffsets(type, param)) {
                            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                                type = ((CsmTypeBasedSpecializationParameter) param).getType();
                                repeat = true;
                            }
                        }
                    }
                } while (repeat);
                csmItem = parameter;
            }
            if (csmItem == null) {
                int[] idFunBlk = null;
                try {
                    if (doc instanceof BaseDocument) {
                        idFunBlk = NbEditorUtilities.getIdentifierAndMethodBlock((BaseDocument) doc, offset);
                    }
                } catch (BadLocationException ex) {
                    // skip it
                }
                // check but not for function call
                if (idFunBlk != null && idFunBlk.length != 3) {
                    csmItem = findDeclaration(csmFile, doc, tokenUnderOffset, offset, QueryScope.SMART_QUERY, fileReferencesContext);
                }
            }
            if (csmItem == null || !CsmIncludeResolver.getDefault().isObjectVisible(csmFile, csmItem)) {
                // then full check
                CsmObject other = findDeclaration(csmFile, doc, tokenUnderOffset, offset, QueryScope.GLOBAL_QUERY, fileReferencesContext);
                if (other != null) {
                    csmItem = other;
                }
            }
            return csmItem;
        } finally {
            if (restoreThreadName) {
                Thread.currentThread().setName(oldName);
            }
        }
    }

    private static CsmObject findDeclaration(final CsmFile csmFile, final Document doc,
            TokenItem<TokenId> tokenUnderOffset, final int offset, final QueryScope queryScope, FileReferencesContext fileReferencesContext) {
        assert csmFile != null;
        if (tokenUnderOffset == null && doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).readLock();
            try {
                tokenUnderOffset = CndTokenUtilities.getTokenCheckPrev(doc, offset);
            } finally {
                ((AbstractDocument) doc).readUnlock();
            }
        }
        // no token in document under offset position
        if (tokenUnderOffset == null) {
            return null;
        }
        CsmObject csmObject = null;
        if (tokenUnderOffset.id() == CppTokenId.OPERATOR) {
            // support for overloaded operators
            CsmObject foundObject = CsmOffsetResolver.findObject(csmFile, offset, fileReferencesContext);
            csmObject = foundObject;
            if (CsmKindUtilities.isFunction(csmObject)) {
                CsmFunction decl = null;
                if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                    decl = ((CsmFunctionDefinition) csmObject).getDeclaration();
                } else if (CsmKindUtilities.isFriendMethod(csmObject)) {
                    decl = ((CsmFriendFunction) csmObject).getReferencedFunction();
                }
                if (decl != null) {
                    csmObject = decl;
                }
            } else {
                csmObject = null;
            }
        } /*else if (tokenUnderOffset.id() == CppTokenId.AUTO) {
            // support for auto keyword
            CsmObject foundObject = CsmOffsetResolver.findObject(csmFile, offset, fileReferencesContext);
            if (CsmKindUtilities.isVariable(foundObject)) {
                CsmVariable var = (CsmVariable) foundObject;
                CsmType varType = var.getType();
                if (varType != null) {
                    CsmClassifier varClassifier = (varType != null) ? varType.getClassifier() : null;

                }
            }
        }*/
        if (csmObject == null) {
            // try with code completion engine
            Collection<CsmObject> objs = CompletionUtilities.findItemsReferencedAtCaretPos(null, doc, CsmCompletionProvider.createCompletionResolver(csmFile, queryScope, fileReferencesContext), offset);
            csmObject = extractBestReferencedObject(objs, csmObject);
        }
        return csmObject;
    }

    private static CsmObject extractBestReferencedObject(Collection<CsmObject> objs, CsmObject csmObject) {
        CsmObject out = null;
        CsmObject fwd = null;
        for (CsmObject cur : objs) {
            if (CsmKindUtilities.isClassForwardDeclaration(cur) || CsmClassifierResolver.getDefault().isForwardClassifier(cur)) {
                if (fwd == null) {
                    fwd = cur;
                }
            } else {
                out = cur;
                break;
            }
        }
        if (out == null) {
            out = fwd;
        }
        return out;
    }

    /*package*/ static ReferenceImpl createReferenceImpl(final CsmFile file, final BaseDocument doc, final int offset) {
        ReferenceImpl ref = null;
        doc.readLock();
        try {
            TokenItem<TokenId> token = CndTokenUtilities.getTokenCheckPrev(doc, offset);
            if (isSupportedToken(token)) {
                ref = createReferenceImpl(file, doc, offset, token, null);
            }
        } finally {
            doc.readUnlock();
        }
        return ref;
    }

    public static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, int offset, TokenItem<TokenId> token, CsmReferenceKind kind) {
        assert token != null;
        assert file != null : "null file for document " + doc + " on offset " + offset + " " + token;
        if (token.id() == CppTokenId.THIS) {
            return new ThisReferenceImpl(file, doc, offset, token, kind);
        } else {
            return new ReferenceImpl(file, doc, offset, token, kind);
        }
    }

    private static boolean isSupportedToken(TokenItem<TokenId> token) {
        return token != null &&
                (CsmIncludeHyperlinkProvider.isSupportedToken(token, HyperlinkType.GO_TO_DECLARATION) ||
                CsmHyperlinkProvider.isSupportedToken(token, HyperlinkType.GO_TO_DECLARATION) ||
                CsmDefineHyperlinkProvider.isSupportedToken(token, HyperlinkType.GO_TO_DECLARATION));
    }

    public static Scope fastCheckScope(CsmReference ref) {
        Parameters.notNull("ref", ref); // NOI18N
        CsmObject target = getTargetIfPossible(ref);
        if (target == null) {
            // try to resolve using only local context
            int offset = getRefOffset(ref);
            BaseDocument doc = getRefDocument(ref);
            if (doc != null) {
                TokenItem<TokenId> token = getRefTokenIfPossible(ref);
                target = findDeclaration(ref.getContainingFile(), doc, token, offset, QueryScope.LOCAL_QUERY, null);
                setResolvedInfo(ref, target);
            }
        }
        return getTargetScope(target);
    }

    private static Scope getTargetScope(CsmObject obj) {
        if (obj == null) {
            return Scope.UNKNOWN;
        }
        if (isLocalElement(obj)) {
            return Scope.LOCAL;
        } else if (isFileLocalElement(obj)) {
            return Scope.FILE_LOCAL;
        } else {
            return Scope.GLOBAL;
        }
    }

    private static CsmObject getTargetIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl) ref).getTarget();
        }
        return null;
    }

    /*package*/
    static TokenItem<TokenId> getRefTokenIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl) ref).getToken();
        } else {
            return null;
        }
    }

    private static CsmReferenceKind getRefKindIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl) ref).getKindImpl();
        } else {
            return null;
        }
    }

    private static BaseDocument getRefDocument(CsmReference ref) {
        if (ref instanceof DocOffsetableImpl) {
            return ((DocOffsetableImpl) ref).getDocument();
        } else {
            CsmFile file = ref.getContainingFile();
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
            Document doc = CsmUtilities.openDocument(ces);
            return doc instanceof BaseDocument ? (BaseDocument) doc : null;
        }
    }

    private static int getRefOffset(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl) ref).getOffset();
        } else {
            return (ref.getStartOffset() + ref.getEndOffset() + 1) / 2;
        }
    }

    private static void setResolvedInfo(CsmReference ref, CsmObject target) {
        if (target != null && (ref instanceof ReferenceImpl)) {
            ((ReferenceImpl) ref).setTarget(target);
        }
    }

    private static boolean isLocalElement(CsmObject decl) {
        assert decl != null;
        CsmObject scopeElem = decl;
        while (CsmKindUtilities.isScopeElement(scopeElem)) {
            CsmScope scope = ((CsmScopeElement) scopeElem).getScope();
            if (CsmKindUtilities.isFunction(scope)) {
                return true;
            } else if (CsmKindUtilities.isScopeElement(scope)) {
                scopeElem = ((CsmScopeElement) scope);
            } else {
                break;
            }
        }
        return false;
    }

    private static boolean isFileLocalElement(CsmObject decl) {
        assert decl != null;
        if (CsmBaseUtilities.isDeclarationFromUnnamedNamespace(decl)) {
            return true;
        } else if (CsmKindUtilities.isFileLocalVariable(decl)) {
            return true;
        } else if (CsmKindUtilities.isFunction(decl)) {
            return CsmBaseUtilities.isFileLocalFunction(((CsmFunction) decl));
        }
        return false;
    }

    static CsmReferenceKind getReferenceUsageKind(final CsmReference ref) {
        CsmReferenceKind kind = CsmReferenceKind.DIRECT_USAGE;
        if (ref instanceof ReferenceImpl) {
            CsmReferenceKind implKind = getRefKindIfPossible(ref);
            if (implKind != null) {
                return implKind;
            }
        }
        return kind;
    }
    private final CsmProgressListener progressListener;
    private final ReferencesCache cache = new ReferencesCache();

    private CsmObject getReferencedObject(CsmFile file, TokenItem<TokenId> offset, long callTimeVersion) {
        return cache.getReferencedObject(file, offset, callTimeVersion);
    }

    private void putReferencedObject(CsmFile file, TokenItem<TokenId> offset, CsmObject object, long fileVersionOnStartResolving) {
        cache.putReferencedObject(file, offset, object, fileVersionOnStartResolving);
    }

    private void clearFileReferences(CsmFile file) {
        cache.clearFileReferences(file);
    }

    /**
     * Searches for macro.
     *
     * @param macroUsages - SORTED (!) list of macros
     * @param offset - macro offset
     * @return macro
     */
    public static CsmObject findMacro(List<CsmReference> macroUsages, final int offset) {
        int index = Collections.binarySearch(macroUsages, new RefOffsetKey(offset), new Comparator<CsmReference>() {
            @Override
            public int compare(CsmReference o1, CsmReference o2) {
                if (o1 instanceof RefOffsetKey) {
                    if (o2.getStartOffset() <= o1.getStartOffset() &&
                            o1.getEndOffset() <= o2.getEndOffset()) {
                        return 0;
                    }
                } else if (o2 instanceof RefOffsetKey) {
                    if (o1.getStartOffset() <= o2.getStartOffset() &&
                            o2.getEndOffset() <= o1.getEndOffset()) {
                        return 0;
                    }
                }
                return o1.getStartOffset() - o2.getStartOffset();
            }
        });
        if (index >= 0) {
            CsmReference macroRef = macroUsages.get(index);
            CsmObject csmItem = macroRef.getReferencedObject();
            if (csmItem == null) {
                CndUtils.assertTrueInConsole(false, "referenced macro is null. ref " + macroRef + ", file " + macroRef.getContainingFile() + ", name " + macroRef.getText());
            }
            return csmItem;
        }
        return null;
    }

    private static final class RefOffsetKey implements CsmReference {

        private final int offset;

        private RefOffsetKey(int offset) {
            this.offset = offset;
        }

        @Override
        public CsmReferenceKind getKind() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CsmObject getReferencedObject() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CsmObject getOwner() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public int getStartOffset() {
            return offset;
        }

        @Override
        public int getEndOffset() {
            return offset;
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            throw new UnsupportedOperationException("Not supported.");// NOI18N
        }
    }

    @ServiceProvider(service=CndDiagnosticProvider.class, position=2000)
    public static final class RefSupportDiagnostic implements CndDiagnosticProvider {

        @NbBundle.Messages({"RefSupportDiagnostic.displayName=Cache of xRef for file"})
        @Override
        public String getDisplayName() {
            return RefSupportDiagnostic_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            ReferencesSupport inst = ReferencesSupport.instance;
            inst.cache.dumpInfo(printOut);
        }
    }

    private static DefineImpl getDefine(final Document doc, final CsmFile file, final int offset) {

        final DefineTarget dt = new DefineTarget();

        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).render(new Runnable() {
                @Override
                public void run() {
                    TokenSequence<TokenId> ts;
                    ts = CndLexerUtilities.getCppTokenSequence(doc, 0, false, false);

                    int start = getDefineStartOffset(ts, offset);

                    if(start == -1) {
                        return;
                    }

                    ts.move(start);
                    if (!ts.moveNext()) {
                        return;
                    }
                    if(ts.token().id().equals(CppTokenId.PREPROCESSOR_DIRECTIVE)) {
                        ts = (TokenSequence<TokenId>)ts.embedded();
                        if(ts == null) {
                            return;
                        }
                        if (!ts.moveNext()) {
                            return;
                        }
                        if(!ts.token().id().equals(CppTokenId.PREPROCESSOR_START)) {
                            return;
                        }
                        if (!ts.moveNext()) {
                            return;
                        }
                        while (ts.token().id().equals(CppTokenId.WHITESPACE) ||
                                ts.token().id().equals(CppTokenId.NEW_LINE) ||
                                ts.token().id().equals(CppTokenId.BLOCK_COMMENT)) {
                            if (!ts.moveNext()) {
                                return;
                            }
                        }
                        if(!ts.token().id().equals(CppTokenId.PREPROCESSOR_DEFINE)) {
                            return;
                        }
                        if (!ts.moveNext()) {
                            return;
                        }
                        while (ts.token().id().equals(CppTokenId.WHITESPACE) ||
                                ts.token().id().equals(CppTokenId.NEW_LINE) ||
                                ts.token().id().equals(CppTokenId.BLOCK_COMMENT)) {
                            if (!ts.moveNext()) {
                                return;
                            }
                        }
                        if(!ts.token().id().equals(CppTokenId.PREPROCESSOR_IDENTIFIER)) {
                            return;
                        }
                        CharSequence name = ts.token().text();
                        if (!ts.moveNext()) {
                            return;
                        }
                        List<DefineParameterImpl> params = new ArrayList<DefineParameterImpl>();
                        if(ts.token().id().equals(CppTokenId.LPAREN)) {
                            while(!ts.token().id().equals(CppTokenId.RPAREN)) {
                                if(ts.token().id().equals(CppTokenId.PREPROCESSOR_IDENTIFIER)) {
                                    params.add(new DefineParameterImpl(ts.token().text(), file, ts.offset()));
                                }
                                if (!ts.moveNext()) {
                                    break;
                                }
                            }
                        }
                        int end = ts.offset();
                        dt.setDefine(new DefineImpl(name, params, file, start, offset));
                    }

                }
            });
        }
        return dt.getDefine();
    }

    private static int getDefineStartOffset(TokenSequence<TokenId> ts, int offset) {
        if (!ts.moveNext()) {
            return -1;
        }
        int lastDefineOffset = -1;
        int currentOffset = ts.offset();
        while(currentOffset < offset) {
            if (ts.token().id().equals(CppTokenId.PREPROCESSOR_DIRECTIVE)) {
                lastDefineOffset = ts.offset();
            }
            if (!ts.moveNext()) {
                break;
            }
            currentOffset = ts.offset();
        }
        return lastDefineOffset;
    }


    private static class DefineParameterImpl implements CsmOffsetable, CsmNamedElement {
        private final CharSequence name;
        private final CsmFile file;
        private final int startOffset;

        public DefineParameterImpl(CharSequence name, CsmFile file, int startOffset) {
            this.name = CharSequences.create(name);
            this.file = file;
            this.startOffset = startOffset;
        }

        @Override
        public CharSequence getName() {
            return name;
        }

        @Override
        public CsmFile getContainingFile() {
            return file;
        }

        @Override
        public int getStartOffset() {
            return startOffset;
        }

        @Override
        public int getEndOffset() {
            return startOffset + name.length();
        }

        @Override
        public Position getStartPosition() {
            return DUMMY_POSITION;
        }

        @Override
        public Position getEndPosition() {
            return DUMMY_POSITION;
        }

        @Override
        public CharSequence getText() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 97 * hash + (this.file != null ? this.file.hashCode() : 0);
            hash = 97 * hash + this.startOffset;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DefineParameterImpl other = (DefineParameterImpl) obj;
            if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
                return false;
            }
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            if (this.startOffset != other.startOffset) {
                return false;
            }
            return true;
        }

    }

    private static class DefineTarget {
        private DefineImpl define;

        public DefineImpl getDefine() {
            return define;
        }

        public void setDefine(DefineImpl define) {
            this.define = define;
        }
    }

    private static class DefineImpl implements CsmOffsetable {
        private final int startOffset;
        private final int endOffset;
        private final CsmFile file;
        private final CharSequence name;
        private final List<DefineParameterImpl> params;

        public DefineImpl(CharSequence name, List<DefineParameterImpl> params, CsmFile file, int startOffset, int endOffset) {
            this.name = CharSequences.create(name);
            this.file = file;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.params = params;
        }

        @Override
        public CsmFile getContainingFile() {
            return file;
        }

        @Override
        public int getStartOffset() {
            return startOffset;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public Position getStartPosition() {
            return DUMMY_POSITION;
        }

        @Override
        public Position getEndPosition() {
            return DUMMY_POSITION;
        }

        @Override
        public CharSequence getText() {
            return name;
        }

        public List<DefineParameterImpl> getParams() {
            return params;
        }


    }

    private static final CsmOffsetable.Position DUMMY_POSITION = new CsmOffsetable.Position() {

        @Override
        public int getOffset() {
            return -1;
        }

        @Override
        public int getLine() {
            return -1;
        }

        @Override
        public int getColumn() {
            return -1;
        }
    };

}
