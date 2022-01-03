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
package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmFunctionDefinitionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.OverridesPopup;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ui.PopupUtil;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.util.Exceptions;

/**
 * Implementation of the hyperlink provider for C/C++ language.
 * <br>
 * The hyperlinks are constructed for identifiers.
 * <br>
 * The click action corresponds to performing the goto-declaration action.
 *
 */
public class CsmHyperlinkProvider extends CsmAbstractHyperlinkProvider {

    public CsmHyperlinkProvider() {
    }

    @Override
    protected void performAction(final Document doc, final JTextComponent target, final int offset, final HyperlinkType type) {
        goToDeclaration(doc, target, offset, type);
    }

    @Override
    protected boolean isValidToken(TokenItem<TokenId> token, HyperlinkType type) {
        if (!isSupportedToken(token, type)) {
            // Here we can support tokens for which only hyperlink should be available
            // (No occurences, refactorings, etc)
            if (token != null && token.id() instanceof CppTokenId) {
                switch ((CppTokenId)token.id()) {
                    case AUTO: // TODO: should be allowed only in cpp11
                        return true;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean isSupportedToken(TokenItem<TokenId> token, HyperlinkType type) {
        if (token != null) {
            if (type == HyperlinkType.ALT_HYPERLINK) {
                if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory()) ||
                        CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                    return false;
                }
            }
            if(token.id() instanceof CppTokenId) {
                switch ((CppTokenId)token.id()) {
                    case LTLT:
                    case IDENTIFIER:
                    case OPERATOR:
                    case DELETE:
                    case PROC_DIRECTIVE:
                        return true;
                    case PREPROCESSOR_INCLUDE:
                    case PREPROCESSOR_INCLUDE_NEXT:
                    case PREPROCESSOR_SYS_INCLUDE:
                    case PREPROCESSOR_USER_INCLUDE:
                        return false;
                    // position dependent keywords could be identifiers as well
                    case FINAL: 
                    case OVERRIDE: 
                        return true;
                }
            }
        }
        return false;
    }

    public boolean goToDeclaration(Document doc, JTextComponent target, int offset, HyperlinkType type) {
        if (!preJump(doc, target, offset, "opening-csm-element", type)) { //NOI18N
            return false;
        }
        CsmCacheManager.enter();
        try {
            TokenItem<TokenId> jumpToken = getJumpToken();
            CsmObject primary;
            CsmFile csmFile = CsmUtilities.getCsmFile(doc, true, false);
            if (jumpToken.id() == CppTokenId.DELETE) {
                primary = null;
                String deleteExpr = getRestExpression(doc, offset);
                CsmType resolvedType = CsmExpressionResolver.resolveType(deleteExpr, csmFile, offset, null);
                if (resolvedType != null) {
                    CsmClassifier classifier = CsmClassifierResolver.getDefault().getTypeClassifier(resolvedType, csmFile, offset, true);
                    if (CsmKindUtilities.isClass(classifier)) {
                        primary = CsmVirtualInfoQuery.getDefault().getFirstDestructor((CsmClass)classifier);
                    }
                }
            } else if (jumpToken.id() == CppTokenId.LTLT) {
                primary = null;
                int startExpression = getStartExpression(doc, offset);
                String rightOperand = getRightOperandExpression(doc, offset, CppTokenId.LTLT);
                CsmType rightType = CsmExpressionResolver.resolveType(rightOperand, csmFile, startExpression, null);
                if (rightType != null) {
                    CsmClassifier rightClassifier = CsmClassifierResolver.getDefault().getTypeClassifier(rightType, csmFile, offset, true);
                    if (CsmKindUtilities.isClass(rightClassifier)) {
                        for(CsmFriend friend : ((CsmClass)rightClassifier).getFriends()) {
                            if (CsmKindUtilities.isOperator(friend)) {
                                if (friend.getName().toString().endsWith(CppTokenId.LTLT.fixedText())) {
                                    primary = friend;
                                    break;
                                }
                            }
                        }
                    }
                    if (primary == null) {
                        String leftOperand = getLeftmostOperandExpression(doc, offset, CppTokenId.LTLT);
                        CsmType leftType = CsmExpressionResolver.resolveType(leftOperand, csmFile, startExpression, null);
                        if (leftType != null) {
                            CsmClassifier leftClassifier = CsmClassifierResolver.getDefault().getTypeClassifier(leftType, csmFile, offset, true);
                            if (CsmKindUtilities.isClass(leftClassifier)) {
                                for(CsmMember member : ((CsmClass)leftClassifier).getMembers()) {
                                    if (CsmKindUtilities.isOperator(member)) {
                                        if (member.getName().toString().endsWith(CppTokenId.LTLT.fixedText())) {
                                            Collection<CsmParameter> parameters = ((CsmFunction)member).getParameters();
                                            if (parameters.size()==1) {
                                                CsmParameter par = parameters.iterator().next();
                                                CsmType aType = par.getType();
                                                if (isTypeEquals(aType, rightType)) {
                                                    primary = member;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                primary = findTargetObject(doc, jumpToken, offset, false);
            }
            CsmOffsetable item = toJumpObject(primary, csmFile, offset);
            if (type == HyperlinkType.ALT_HYPERLINK) {
                if (CsmKindUtilities.isFunction(item)) {
                    CsmFunction decl = CsmBaseUtilities.getFunctionDeclaration((CsmFunction) item);
                    Collection<CsmOffsetableDeclaration> baseTemplates = CsmInstantiationProvider.getDefault().getBaseTemplate(decl);
                    Collection<CsmOffsetableDeclaration> templateSpecializations = CsmInstantiationProvider.getDefault().getSpecializations(decl);
                    boolean inDeclaration = isInDeclaration(decl, csmFile, offset);
                    Collection<? extends CsmMethod> baseMethods = new ArrayList<CsmMethod>(0);
                    Collection<? extends CsmMethod> overriddenMethods = new ArrayList<CsmMethod>(0);
                    if (CsmKindUtilities.isMethod(decl)) {
                        CsmMethod meth = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration(decl);
                        if (inDeclaration) {
                            baseMethods = CsmVirtualInfoQuery.getDefault().getFirstBaseDeclarations(meth);
                        }
                        if (!baseMethods.isEmpty() || CsmVirtualInfoQuery.getDefault().isVirtual(meth)) {
                            overriddenMethods = CsmVirtualInfoQuery.getDefault().getOverriddenMethods(meth, false);
                        }
                        baseMethods.remove(meth); // in the case CsmVirtualInfoQuery added function itself (which was previously the case)
                    }
                    if (showOverridesPopup(inDeclaration ? null : decl, baseMethods, overriddenMethods, baseTemplates, templateSpecializations, inDeclaration ? CsmKindUtilities.isFunctionDefinition(item) : true, target, offset)) {
                        UIGesturesSupport.submit("USG_CND_HYPERLINK_METHOD", type); //NOI18N
                        return true;
                    }
                } else if (CsmKindUtilities.isClass(item)) {
                    CsmClass cls = (CsmClass) item;
                    Collection<CsmOffsetableDeclaration> baseTemplates = CsmInstantiationProvider.getDefault().getBaseTemplate(cls);
                    Collection<CsmOffsetableDeclaration> templateSpecializations = CsmInstantiationProvider.getDefault().getSpecializations(cls);
                    Collection<CsmClass> subClasses = new ArrayList<CsmClass>(0);

                    Collection<CsmReference> subRefs = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, false);
                    if (!subRefs.isEmpty()) {
                        for (CsmReference ref : subRefs) {
                            CsmObject obj = ref.getReferencedObject();
                            CndUtils.assertTrue(obj == null || (obj instanceof CsmClass), "getClassifier() should return either null or CsmClass"); //NOI18N
                            if (CsmKindUtilities.isClass(obj)) {
                                subClasses.add((CsmClass) obj);
                            }
                        }
                    }
                    if (showOverridesPopup(null, Collections.<CsmClass>emptyList(), subClasses, baseTemplates, templateSpecializations, false, target, offset)) {
                        UIGesturesSupport.submit("USG_CND_HYPERLINK_CLASS", type); //NOI18N
                        return true;
                    }
                }
            }
            UIGesturesSupport.submit("USG_CND_HYPERLINK", type); //NOI18N
            return postJump(item, "goto_source_source_not_found", "cannot-open-csm-element"); //NOI18N
        } finally {
            CsmCacheManager.leave();
        }            
    }

    String getRightOperandExpression(final Document doc, final int offset, final CppTokenId endToken) {
        final AtomicReference<String> out = new AtomicReference<String>();
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, true, false);
                if (cppTokenSequence == null) {
                    return;
                }
                cppTokenSequence.move(offset);
                if (cppTokenSequence.moveNext() && cppTokenSequence.moveNext()) {
                    int start = cppTokenSequence.offset();
                    while(cppTokenSequence.moveNext()) {
                        Token<TokenId> token = cppTokenSequence.token();
                        if (token.id() == CppTokenId.SEMICOLON) {
                            break;
                        } else if (token.id() == endToken) {
                            break;
                        }
                    }
                    int end = cppTokenSequence.offset();
                    try {
                        out.set(doc.getText(start, end-start).trim());
                    } catch (BadLocationException ex) {
                    }
                }
            }
        });
        return out.get();
    }

    String getLeftmostOperandExpression(final Document doc, final int offset, final CppTokenId endToken) {
        final AtomicReference<String> out = new AtomicReference<String>();
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, true, false);
                if (cppTokenSequence == null) {
                    return;
                }
                cppTokenSequence.move(offset);
                if (cppTokenSequence.movePrevious()) {
                    int end = cppTokenSequence.offset();
                    while(cppTokenSequence.movePrevious()) {
                        Token<TokenId> token = cppTokenSequence.token();
                        if (token.id() == CppTokenId.SEMICOLON || token.id() == CppTokenId.LBRACE || token.id() == CppTokenId.RBRACE) {
                            break;
                        } else if (token.id() == endToken) {
                            end = cppTokenSequence.offset();
                        }
                    }
                    int start = cppTokenSequence.offset();
                    try {
                        out.set(doc.getText(start+1, end -start - 1).trim());
                    } catch (BadLocationException ex) {
                    }
                }
            }
        });
        return out.get();
    }

    int getStartExpression(final Document doc, final int offset) {
        final AtomicReference<Integer> out = new AtomicReference<Integer>();
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, true, false);
                if (cppTokenSequence == null) {
                    return;
                }
                cppTokenSequence.move(offset);
                while (cppTokenSequence.movePrevious()) {
                    Token<TokenId> token = cppTokenSequence.token();
                    if (token.id() == CppTokenId.SEMICOLON || token.id() == CppTokenId.LBRACE || token.id() == CppTokenId.RBRACE) {
                        break;
                    }
                }
                out.set(cppTokenSequence.offset()+1);
            }
        });
        return out.get();
    }

    private boolean isTypeEquals(CsmType type1, CsmType type2) {
        if (type1 != null && type2 != null) {
            CsmClassifier cls1 = type1.getClassifier();
            if (CsmKindUtilities.isTypedef(cls1)) {
                CsmType aType = ((CsmTypedef) cls1).getType();
                if (aType != null) {
                    type1 = aType;
                }
            }
            CsmClassifier cls2 = type2.getClassifier();
            if (CsmKindUtilities.isTypedef(cls2)) {
                CsmType aType = ((CsmTypedef) cls2).getType();
                if (aType != null) {
                    type2 = aType;
                }
            }
            String canonicalText1 = type1.getCanonicalText().toString().replace("const const", "const"); //NOI18N
            String canonicalText2 = type2.getCanonicalText().toString().replace("const const", "const"); //NOI18N
            return canonicalText1.equals(canonicalText2);
        }
        return false;
    }
    
    String getRestExpression(final Document doc, final int offset) {
        final AtomicReference<String> out = new AtomicReference<String>();
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, true, false);
                if (cppTokenSequence == null) {
                    return;
                }
                cppTokenSequence.move(offset);
                if (cppTokenSequence.moveNext() && cppTokenSequence.moveNext()) {
                    int start = cppTokenSequence.offset();
                    while(cppTokenSequence.moveNext()) {
                        Token<TokenId> token = cppTokenSequence.token();
                        if (token.id() == CppTokenId.SEMICOLON) {
                            break;
                        }
                    }
                    int end = cppTokenSequence.offset();
                    try {
                        out.set(doc.getText(start, end-start));
                    } catch (BadLocationException ex) {
                    }
                }
            }
        });
        return out.get();
    }
    
    private boolean showOverridesPopup(CsmOffsetableDeclaration mainDeclaration,
            Collection<? extends CsmOffsetableDeclaration> baseDeclarations,
            Collection<? extends CsmOffsetableDeclaration> descendantDeclarations,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates,
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations,
            boolean gotoDefinitions,
            JTextComponent target, int offset) {
        if (!baseDeclarations.isEmpty() || !descendantDeclarations.isEmpty() || !baseDeclarations.isEmpty() || !templateSpecializations.isEmpty()) {
            try {
                final OverridesPopup popup = new OverridesPopup(null, mainDeclaration, baseDeclarations, descendantDeclarations, baseTemplates, templateSpecializations, gotoDefinitions);
                Rectangle rect = target.modelToView(offset);
                final Point point = new Point((int) rect.getX(), (int)(rect.getY() + rect.getHeight()));
                SwingUtilities.convertPointToScreen(point, target);
                Runnable runner = new Runnable() {
                    @Override
                    public void run() {
                        PopupUtil.showPopup(popup, null, point.x, point.y, true, 0);
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    runner.run();
                } else {
                    SwingUtilities.invokeLater(runner);
                }
                return true;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    /*package*/ CsmObject findTargetObject(final Document doc, final TokenItem<TokenId> jumpToken, final int offset, boolean toOffsetable) {
        CsmObject item = null;
        assert jumpToken != null;
        CsmFile file = CsmUtilities.getCsmFile(doc, true, false);
        CsmObject csmObject = file == null ? null : ReferencesSupport.findDeclaration(file, doc, jumpToken, offset);
        if (csmObject != null) {
            // convert to jump object
            item = toOffsetable ? toJumpObject(csmObject, file, offset) : csmObject;
        }
        return item;
    }

    private boolean isInDeclaration(CsmFunction func, CsmFile csmFile, int offset) {
        CsmFunctionDefinition def;
        CsmFunction decl;
        if (CsmKindUtilities.isFunctionDefinition(func)) {
            def = (CsmFunctionDefinition) func;
            decl = def.getDeclaration();
        } else {
            decl = func;
            def = func.getDefinition();
        }
        if (def != null) {
            if (csmFile.equals(def.getContainingFile()) &&
                    (def.getStartOffset() <= offset &&
                    offset <= def.getBody().getStartOffset())) {
                return true;
            }
        }
        if (decl != null) {
            // just declaration
            if (csmFile.equals(decl.getContainingFile()) &&
                    (decl.getStartOffset() <= offset &&
                    offset <= decl.getEndOffset())) {
                return true;
            }
        }
        return false;
    }

    protected CsmOffsetable toJumpObject(CsmObject csmObject, CsmFile csmFile, int offset) {
        CsmOffsetable item = null;
        if (CsmKindUtilities.isOffsetable(csmObject)) {
            item = (CsmOffsetable) csmObject;
            if (CsmKindUtilities.isFunctionDeclaration(csmObject)) {
                // check if we are in function definition name => go to declaration
                // else it is more useful to jump to definition of function
                CsmFunctionDefinition definition = ((CsmFunction) csmObject).getDefinition();
                if (definition != null) {
                    if (csmFile.equals(definition.getContainingFile()) &&
                            (definition.getStartOffset() <= offset &&
                            offset <= definition.getBody().getStartOffset())) {
                        // it is ok to jump to declaration
                        if (definition.getDeclaration() != null) {
                            item = definition.getDeclaration();
                        } else if (csmObject.equals(definition)) {
                            item = (CsmOffsetable) csmObject;
                        }
                    } else {
                        // it's better to jump to definition
                        item = definition;
                    }
                } else {
                    CsmReference ref = CsmFunctionDefinitionResolver.getDefault().getFunctionDefinition((CsmFunction) csmObject);
                    if (ref != null) {
                        item = ref;
                    }
                }
            } else if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                CsmFunctionDefinition definition = (CsmFunctionDefinition) csmObject;
                if (csmFile.equals(definition.getContainingFile()) &&
                        (definition.getStartOffset() <= offset &&
                        offset <= definition.getBody().getStartOffset())) {
                    // it is ok to jump to declaration
                    if (definition.getDeclaration() != null) {
                        item = definition.getDeclaration();
                    } else {
                        item = definition;
                    }
                }
            } else if (CsmKindUtilities.isVariableDeclaration(csmObject)) {
                // check if we are in variable definition name => go to declaration
                CsmVariableDefinition definition = ((CsmVariable) csmObject).getDefinition();
                if (definition != null) {
                    item = definition;
                    if (csmFile.equals(definition.getContainingFile()) &&
                            (definition.getStartOffset() <= offset &&
                            offset <= definition.getEndOffset())) {
                        item = (CsmVariable) csmObject;
                    }
                }
            } else if (CsmClassifierResolver.getDefault().isForwardClassifier(csmObject)) {
                CsmClassifier cls = CsmClassifierResolver.getDefault().getOriginalClassifier((CsmClassifier)csmObject, csmFile);
                if (CsmKindUtilities.isOffsetable(cls)) {
                    item = (CsmOffsetable) cls;
                }
            } else if (CsmKindUtilities.isTypedef(csmObject)) {
                CsmTypedef td  = (CsmTypedef) csmObject;
                CsmType type = td.getType();
                CsmClassifier typeCls = type != null ? type.getClassifier() : null;
                if (CsmKindUtilities.isOffsetable(typeCls)) {
                    if (CsmKindUtilities.isClassForwardDeclaration(typeCls) || CsmKindUtilities.isEnumForwardDeclaration(typeCls)) {
                        // Special case: typedef is a synonim of the class with the same name
                        if (td.getQualifiedName().equals(typeCls.getQualifiedName())) {
                            CsmClassifier cls = CsmClassifierResolver.getDefault().getOriginalClassifier(typeCls, csmFile);
                            if (CsmKindUtilities.isOffsetable(cls)) {
                                item = (CsmOffsetable) cls;
                            }
                        }
                    }
                }
            }
        } else if (CsmKindUtilities.isNamespace(csmObject)) {
            // get all definitions of namespace, but prefer the definition in this file
            CsmNamespace nmsp = (CsmNamespace) csmObject;
            Collection<CsmNamespaceDefinition> defs = nmsp.getDefinitions();
            CsmNamespaceDefinition bestDef = null;
            for (CsmNamespaceDefinition def : defs) {
                if (bestDef == null) {
                    // first time initialization
                    bestDef = def;
                }
                CsmFile container = def.getContainingFile();
                if (csmFile.equals(container)) {
                    // this is the best choice
                    bestDef = def;
                    break;
                }
            }
            item = bestDef;
        }
        return item;
    }

    @Override
    protected String getTooltipText(Document doc, TokenItem<TokenId> token, int offset, HyperlinkType type) {
        CsmCacheManager.enter();
        try {
            CsmObject item = findTargetObject(doc, token, offset, false);
            CharSequence msg = item == null ? null : CsmDisplayUtilities.getTooltipText(item);
            if (msg != null) {
                if (CsmKindUtilities.isMacro(item)) {
                    msg = getAlternativeHyperlinkTip(doc, "AltMacroHyperlinkHint", msg); // NOI18N
                } else if (CsmKindUtilities.isMethod(item)) {
                    msg = getAlternativeHyperlinkTip(doc, "AltMethodHyperlinkHint", msg); // NOI18N
                } else if (CsmKindUtilities.isClass(item)) {
                    msg = getAlternativeHyperlinkTip(doc, "AltClassHyperlinkHint", msg); // NOI18N
                }
            }
            return msg == null ? null : msg.toString();
        } finally {
            CsmCacheManager.leave();
        }
    }
}
