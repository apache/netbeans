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
package org.netbeans.modules.cnd.completion.csm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery.CsmCompletionResult;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MutableObject;

/**
 *
 */
public class CompletionUtilities {

    /**
     * Constructor is private to prevent instantiation.
     */
    private CompletionUtilities() {}

    public static List<CsmDeclaration> findFunctionLocalVariables(Document doc, int offset, FileReferencesContext fileReferncesContext) {
        CsmFile file = CsmUtilities.getCsmFile(doc, true, false);
        if (file == null || !file.isValid()) {
            return Collections.<CsmDeclaration>emptyList();
        }
        CsmContext context = CsmOffsetResolver.findContext(file, offset, fileReferncesContext);
        return CsmContextUtilities.findFunctionLocalVariables(context);
    }

    public static List<CsmDeclaration> findFileVariables(Document doc, int offset) {
        CsmFile file = CsmUtilities.getCsmFile(doc, true, false);
        if (file == null || !file.isValid()) {
            return Collections.<CsmDeclaration>emptyList();
        }
        CsmContext context = CsmOffsetResolver.findContext(file, offset, null);
        return CsmContextUtilities.findFileLocalVariables(context);
    }

    // TODO: think if we need it?
    public static CsmClass findClassOnPosition(CsmFile file, Document doc, int offset) {
        if (file == null) {
            file = CsmUtilities.getCsmFile(doc, true, false);
        }
        if (file == null || !file.isValid()) {
            return null;
        }
        CsmContext context = CsmOffsetResolver.findContext(file, offset, null);
        CsmClass clazz = CsmContextUtilities.getClass(context, true, false);
        return clazz;
    }

    public static CsmOffsetableDeclaration findFunDefinitionOrClassOnPosition(CsmFile file, Document doc, int offset, FileReferencesContext fileReferncesContext) {
        CsmOffsetableDeclaration out = null;
        if (file == null) {
            file = CsmUtilities.getCsmFile(doc, true, false);
        }
        if (file != null) {
            CsmContext context = CsmOffsetResolver.findContext(file, offset, fileReferncesContext);
            out = CsmContextUtilities.getFunctionDefinition(context);
            if (out == null || !CsmContextUtilities.isInFunctionBodyOrInitializerListOrCastOperatorType(context, offset)) {
                out = CsmContextUtilities.getClass(context, false, false);
            }
        }
        return out;
    }

    public static Collection<CsmObject> findItemsReferencedAtCaretPos(JTextComponent target, Document doc, CsmCompletionQuery query, int dotPos) {
        Collection<CsmObject> out = new ArrayList<CsmObject>();
        try {
            BaseDocument baseDoc = null;
            if (doc instanceof BaseDocument) {
                baseDoc = (BaseDocument) doc;
            }
            baseDoc = baseDoc != null ? baseDoc : (BaseDocument) target.getDocument();

            boolean searchFunctionsOnly = false;
            boolean searchSpecializationsOnly = false;
            int[] idBlk = getIdentifierAndMethodBlock(baseDoc, dotPos);
            searchFunctionsOnly = (idBlk != null) ? (idBlk.length == 3) : false;
            if (idBlk == null || idBlk.length == 2) {
                idBlk = getIdentifierAndInstantiationBlock(baseDoc, dotPos);
                searchSpecializationsOnly = (idBlk != null) ? (idBlk.length == 3) : false;
            }
            if (idBlk == null) {
                idBlk = new int[]{dotPos, dotPos};
            }
            CsmFile currentFile = query.getCsmFile();
            if (currentFile == null) {
                currentFile = CsmUtilities.getCsmFile(doc, false, false);
            }
            for (int ind = idBlk.length - 1; ind >= 1; ind--) {
                CsmCompletionResult result = query.query(target, baseDoc, idBlk[ind], true, false, false);
                if (result != null && !result.getItems().isEmpty()) {
                    List<CsmObject> filtered = getAssociatedObjects(result.getItems(), searchFunctionsOnly, currentFile, dotPos);
                    out = !filtered.isEmpty() ? filtered : getAssociatedObjects(result.getItems(), false, currentFile, dotPos);
                    if (filtered.size() > 1 && searchFunctionsOnly) {
                        // It is overloaded method, lets check for the right one
                        int endOfMethod = findEndOfMethod(baseDoc, idBlk[ind] - 1);
                        if (endOfMethod > -1) {
                            CsmCompletionResult resultx = query.query(target, baseDoc, endOfMethod, true, false, false);
                            if (resultx != null && !resultx.getItems().isEmpty()) {
                                out = getAssociatedObjects(resultx.getItems(), false, currentFile, dotPos);
                            }
                        }
                    }
                    if (filtered.size() > 1 && searchSpecializationsOnly) {
                        int endOfMethod = findEndOfInstantiation(baseDoc, idBlk[ind] - 1);
                        if (endOfMethod > -1) {
                            CsmCompletionResult resultx = query.query(target, baseDoc, endOfMethod, true, false, false);
                            if (resultx != null && !resultx.getItems().isEmpty()) {
                                out = getAssociatedObjects(resultx.getItems(), false, currentFile, dotPos);
                            }
                        }
                    }
                    break;
                } else if (searchFunctionsOnly && (ind == idBlk.length - 1)) {
                    // This is for argument-dependent lookup
                    int endOfMethod = findEndOfMethod(baseDoc, idBlk[ind] - 1);
                    if (endOfMethod > -1) {
                        CsmCompletionResult resultx = query.query(target, baseDoc, endOfMethod, true, false, false);
                        if (resultx != null && !resultx.getItems().isEmpty()) {
                            out = getAssociatedObjects(resultx.getItems(), false, currentFile, dotPos);
                            break;
                        }
                    }
                }
            }
        } catch (BadLocationException e) {
        }
        return out;
    }

    private static int[] getIdentifierBlock(BaseDocument doc, int offset) 
    throws BadLocationException {
        int[] ret = null;
        if (offset == 0) {
            // we use this branch only for zero offset, because for ID spanned as
            // [ID-start, ID-end] when offset is ID-start, then
            // LineDocumentUtils.getWordStart(doc, offset) returns the prev word start, not ID-start;
            // when offset is ID-end, then LineDocumentUtils.getWordEnd(doc, offset) 
            // returns end of the next word, not ID-end
            // BUT for Zero it is OK.
            int wordStart = LineDocumentUtils.getWordStart(doc, offset);
            // find word end using word start
            int wordEnd = LineDocumentUtils.getWordEnd(doc, wordStart);
            if (wordStart >= 0 && wordEnd >= 0) {
                ret = new int[] {wordStart, wordEnd};
            }
        } else if (offset > 0) {
            ret = Utilities.getIdentifierBlock(doc, offset);
        }
        return ret;
    }
    
    private static int[] getIdentifierAndMethodBlock(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] idBlk = getIdentifierBlock(doc, offset);
        if (idBlk != null) {
            int[] funBlk = getFunctionBlock(doc, idBlk);
            if (funBlk != null) {
                return new int[] { idBlk[0], idBlk[1], funBlk[1] };
            }
        }
        return idBlk;
    }

    private static int[] getFunctionBlock(BaseDocument doc, int[] identifierBlock) throws BadLocationException {
        if (identifierBlock != null) {
            int nwPos = LineDocumentUtils.getNextNonWhitespace(doc, identifierBlock[1]);
            if ((nwPos >= 0) && (doc.getChars(nwPos, 1)[0] == '(')) {
                return new int[] { identifierBlock[0], nwPos + 1 };
            }
            if ((nwPos >= 0) && (doc.getChars(nwPos, 1)[0] == '<')) {
                int eoi = findEndOfInstantiation(doc, nwPos);
                if (eoi >= 0) {
                nwPos = LineDocumentUtils.getNextNonWhitespace(doc, eoi);
                    if ((nwPos >= 0) && (doc.getChars(nwPos, 1)[0] == '(')) {
                        return new int[] { identifierBlock[0], nwPos + 1 };
                    }
                }
            }
        }
        return null;
    }

    private static int[] getIdentifierAndInstantiationBlock(BaseDocument doc, int offset) throws BadLocationException {
        int[] idBlk = getIdentifierBlock(doc, offset);
        if (idBlk != null) {
            int[] instBlk = getInstantiationBlock(doc, idBlk);
            if (instBlk != null) {
                return new int[] { idBlk[0], idBlk[1], instBlk[1] };
            }
        }
        return idBlk;
    }

    private static int[] getInstantiationBlock(BaseDocument doc, int[] identifierBlock) throws BadLocationException {
        if (identifierBlock != null) {
            int nwPos = LineDocumentUtils.getNextNonWhitespace(doc, identifierBlock[1]);
            if ((nwPos >= 0) && (doc.getChars(nwPos, 1)[0] == '<')) {
                return new int[] { identifierBlock[0], nwPos + 1 };
            }
        }
        return null;
    }


    private static List<CsmObject> getAssociatedObjects(List items, boolean wantFuncsOnly, CsmFile contextFile, int offset) {
        List<CsmObject> visible = new ArrayList<CsmObject>();
        List<CsmObject> all = new ArrayList<CsmObject>();
        List<CsmObject> funcs = new ArrayList<CsmObject>();
        List<CsmObject> visibleFuncs = new ArrayList<CsmObject>();

        for (Object item : items) {
            if (item instanceof CsmResultItem) {
                CsmObject ret = getAssociatedObject(item);
                boolean isVisible = contextFile == null ? false : CsmIncludeResolver.getDefault().isObjectVisible(contextFile, ret);
                boolean isFunc = CsmKindUtilities.isFunction(ret);
                if (isFunc) {
                    if (isVisible) {
                        visibleFuncs.add(ret);
                    } else {
                        funcs.add(ret);
                    }
                }
                if (isVisible) {
                    visible.add(ret);
                } else {
                    all.add(ret);
                }
            }
        }
        List<CsmObject> out;
        if (wantFuncsOnly) {
            out = !visibleFuncs.isEmpty() ? visibleFuncs : funcs;
        } else {
            out = computeCandidatesList(visible, all, contextFile);
        }
        return out;
    }

    /**
     * Computes candidates list using visible and invisible items.
     *
     * @param visible
     * @param all
     * @param contextFile
     * @return list of candidates
     */
    private static List<CsmObject> computeCandidatesList(List<CsmObject> visible, List<CsmObject> invisible, CsmFile contextFile) {
        List<CsmObject> result;

        if (!visible.isEmpty()) {
            result = visible;

            Map<CharSequence, List<CsmClassifier>> clsMap = null;

            for (int i = 0; i < result.size(); i++) {
                CsmObject candidate = result.get(i);
                if (CsmKindUtilities.isTypedef(candidate)) {

                    // Initilize classifiers map if needed
                    if (clsMap == null) {
                        clsMap = new HashMap<CharSequence, List<CsmClassifier>>();

                        for (CsmObject obj : invisible) {
                            if (CsmKindUtilities.isClass(obj) || CsmKindUtilities.isEnum(obj)) {
                                CsmClassifier cls = (CsmClassifier) obj;

                                List<CsmClassifier> classifiers;

                                if (clsMap.containsKey(cls.getQualifiedName())) {
                                    classifiers = clsMap.get(cls.getQualifiedName());
                                } else {
                                    classifiers = new ArrayList<CsmClassifier>();
                                    clsMap.put(cls.getQualifiedName(), classifiers);
                                }

                                classifiers.add(cls);
                            }
                        }
                    }

                    // Handle typedef
                    CsmTypedef td = (CsmTypedef) candidate;

                    if (clsMap.containsKey(td.getQualifiedName())) {
                        List<CsmClassifier> classifiers = clsMap.get(td.getQualifiedName());

                        for (CsmClassifier cls : classifiers) {
                            CsmFile clsFile = ((CsmOffsetable) cls).getContainingFile();
                            if (clsFile != null &&
                                (CsmIncludeResolver.getDefault().isObjectVisible(clsFile, contextFile) ||
                                 CsmIncludeResolver.getDefault().isObjectVisible(contextFile, clsFile)))
                            {
                                visible.add(i++, cls);
                            }
                        }
                    }
                }
            }
        } else {
            result = invisible;
        }

        return result;
    }

//    /**
//     * Checks and filters typedefs which are in candidates list.
//     * (For hyperlink mode)
//     *
//     * @param candidates - list of candidates
//     */
//    private static void filterCandidateTypedefs(List<CsmObject> candidates, CsmFile file, int offset) {
//        for (int i = 0; i < candidates.size(); i++) {
//            CsmObject candidate = candidates.get(i);
//            if (CsmKindUtilities.isTypedef(candidate)) {
//                CsmTypedef td = (CsmTypedef) candidate;
//                CsmType tdType = td.getType();
//                CsmClassifier tdTypeClass = tdType != null ? tdType.getClassifier() : null;
//
//                // 1. Special case: typedef is a synonim of the class with the same name and the click was inside typedef
//                if (CsmKindUtilities.isClassForwardDeclaration(tdTypeClass) || CsmKindUtilities.isEnumForwardDeclaration(tdTypeClass)) {
//                    if (td.getQualifiedName().equals(tdTypeClass.getQualifiedName())) {
//                        if (CsmKindUtilities.isOffsetable(tdTypeClass)) {
//                            CsmOffsetable offsetable = (CsmOffsetable) tdTypeClass;
//                            if (file.equals(offsetable.getContainingFile()) && offsetable.getStartOffset() <= offset && offsetable.getEndOffset() >= offset) {
//                                CsmClassifier originalCls = CsmClassifierResolver.getDefault().getOriginalClassifier(tdTypeClass, offsetable.getContainingFile());
//                                if (originalCls == null) {
//                                    originalCls = tdTypeClass;
//                                }
//
//                                boolean found = false;
//
//                                for (CsmObject obj : candidates) {
//                                    if (obj.equals(originalCls)) {
//                                        found = true;
//                                        break;
//                                    }
//                                }
//
//                                if (found) {
//                                    candidates.remove(i--);
//                                } else {
//                                    candidates.set(i, originalCls);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    private static CsmObject getAssociatedObject(Object item) {
        if (item instanceof CsmResultItem) {
            CsmObject ret = (CsmObject) ((CsmResultItem) item).getAssociatedObject();
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    public static int findEndOfMethod(final Document doc, final int startPos) {
        final MutableObject<Integer> result = new MutableObject<Integer>(-1);
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, startPos, false, startPos > 0);
                if (ts != null) {
                    int parenLevel = 0;
                    int braceLevel = 0;
                    while (ts.token() != null && ts.token().id() instanceof CppTokenId && braceLevel >= 0 && parenLevel >= 0) {
                        Token<TokenId> token = ts.token();
                        CppTokenId cppTokenId = (CppTokenId) token.id();
                        if (braceLevel == 0 && cppTokenId == CppTokenId.SEMICOLON) {
                            break;
                        }
                        if (cppTokenId == CppTokenId.LBRACE) {
                            braceLevel++;
                        }
                        if (cppTokenId == CppTokenId.RBRACE) {
                            braceLevel--;
                        }
                        if (cppTokenId == CppTokenId.LPAREN) {
                            parenLevel++;
                        }
                        if (cppTokenId == CppTokenId.RPAREN) {
                            parenLevel--;
                            if (parenLevel == 0) {
                                result.value = ts.offset() + token.length();
                                break;
                            }
                        }
                        if (!ts.moveNext()) {
                            break;
                        }
                    }
                }
            }
        });
        return result.value;
    }

    public static int findEndOfInstantiation(Document doc, int startPos) {
        int level = 0;
        CharSequence text = DocumentUtilities.getText(doc);
        for (int i = startPos; i < doc.getLength(); i++) {
            char ch = text.charAt(i);
            if (ch == ';') {
                return -1;
            }
            if (ch == '<') {
                level++;
            }
            if (ch == '>') {
                level--;
                if (level == 0) {
                    return i + 1;
                }
            }
        }
        return -1;
    }
}
