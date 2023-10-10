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
package org.netbeans.modules.java.editor.base.semantic;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.tools.Diagnostic;

import com.sun.source.tree.ModifiersTree;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {
    
    private static final Logger LOG = Logger.getLogger(Utilities.class.getName());
    
    @Deprecated
    private static final boolean DEBUG = false;
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }
    
    private static Token<JavaTokenId> findTokenWithText(CompilationInfo info, String text, int start, int end) {
        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language()).subSequence(start, end);
        
        while (ts.moveNext()) {
            Token<JavaTokenId> t = ts.token();
            
            if (t.id() == JavaTokenId.IDENTIFIER) {
                boolean nameMatches;
                
                if (!(nameMatches = text.equals(info.getTreeUtilities().decodeIdentifier(t.text()).toString()))) {
                    ExpressionTree expr = info.getTreeUtilities().parseExpression(t.text().toString(), new SourcePositions[1]);
                    
                    nameMatches = expr.getKind() == Kind.IDENTIFIER && text.contentEquals(((IdentifierTree) expr).getName());
                }
                
                if (nameMatches) {
                    return t;
                }
            }
        }
        
        return null;
    }
    
    private static Tree normalizeLastLeftTree(Tree lastLeft) {
        while (lastLeft != null && lastLeft.getKind() == Kind.ARRAY_TYPE) {
            lastLeft = ((ArrayTypeTree) lastLeft).getType();
        }
        
        return lastLeft;
    }
    
    private static Token<JavaTokenId> findIdentifierSpanImpl(CompilationInfo info, Tree decl, Tree lastLeft, List<? extends Tree> firstRight, String name, CompilationUnitTree cu, SourcePositions positions) {
        int declStart = (int) positions.getStartPosition(cu, decl);
        
        lastLeft = normalizeLastLeftTree(lastLeft);
        
        int start = lastLeft != null ? (int)positions.getEndPosition(cu, lastLeft) : declStart;
        
        if (start == (-1)) {
            start = declStart;
            if (start == (-1)) {
                return null;
            }
        }
        
        int end = (int)positions.getEndPosition(cu, decl);

        for (Tree t : firstRight) {
            if (t == null)
                continue;

            int proposedEnd = (int)positions.getStartPosition(cu, t);

            if (proposedEnd != (-1) && proposedEnd < end)
                end = proposedEnd;
        }

        if (end == (-1)) {
            return null;
        }

        if (start > end) {
            //may happend in case:
            //public static String s() [] {}
            //(meaning: method returning array of Strings)
            //use a conservative start value:
            start = (int) positions.getStartPosition(cu, decl);
        }

        if (start == end) {
            //may happen for enum constants, would use an empty tokensequence
            end = start + 1;
        }

        return findTokenWithText(info, name, start, end);
    }
    
    private static Token<JavaTokenId> findIdentifierSpanImpl(CompilationInfo info, MemberSelectTree tree, CompilationUnitTree cu, SourcePositions positions) {
        int start = (int)positions.getStartPosition(cu, tree);
        int endPosition = (int)positions.getEndPosition(cu, tree);
        
        if (start == (-1) || endPosition == (-1))
            return null;

        String member = tree.getIdentifier().toString();

        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());

        if (ts.move(endPosition) == Integer.MAX_VALUE) {
            return null;
        }

        if (ts.moveNext()) {
            while (ts.offset() >= start) {
                Token<JavaTokenId> t = ts.token();

                if (t.id() == JavaTokenId.IDENTIFIER && member.equals(info.getTreeUtilities().decodeIdentifier(t.text()).toString())) {
                    return t;
                }

                if (!ts.movePrevious())
                    break;
            }
        }
        return null;
    }
    
    private static Token<JavaTokenId> findIdentifierSpanImpl(CompilationInfo info, MemberReferenceTree tree, CompilationUnitTree cu, SourcePositions positions) {
        int start = (int)positions.getStartPosition(cu, tree);
        int endPosition = (int)positions.getEndPosition(cu, tree);
        
        if (start == (-1) || endPosition == (-1))
            return null;

        String member = tree.getName().toString();

        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());

        if (ts.move(endPosition) == Integer.MAX_VALUE) {
            return null;
        }

        if (ts.moveNext()) {
            while (ts.offset() >= start) {
                Token<JavaTokenId> t = ts.token();

                if (t.id() == JavaTokenId.IDENTIFIER && member.equals(info.getTreeUtilities().decodeIdentifier(t.text()).toString())) {
                    return t;
                }

                if (!ts.movePrevious())
                    break;
            }
        }
        return null;
    }
    
    private static Token<JavaTokenId> findIdentifierSpanImpl(CompilationInfo info, IdentifierTree tree, CompilationUnitTree cu, SourcePositions positions) {
        int start = (int)positions.getStartPosition(cu, tree);
        int endPosition = (int)positions.getEndPosition(cu, tree);
        
        if (start == (-1) || endPosition == (-1))
            return null;

        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());

        if (ts.move(start) == Integer.MAX_VALUE) {
            return null;
        }

        if (ts.moveNext()) {
            if (ts.offset() >= start) {
                Token<JavaTokenId> t = ts.token();
                return t;
            }
        }
        
        return null;
    }

    private static final Map<Class, List<Kind>> class2Kind;
    
    static {
        class2Kind = new HashMap<Class, List<Kind>>();
        
        for (Kind k : Kind.values()) {
            Class c = k.asInterface();
            List<Kind> kinds = class2Kind.get(c);
            
            if (kinds == null) {
                class2Kind.put(c, kinds = new ArrayList<Kind>());
            }
            
            kinds.add(k);
        }
    }
    
    private static Token<JavaTokenId> findIdentifierSpanImpl(CompilationInfo info, TreePath decl) {
        if (info.getTreeUtilities().isSynthetic(decl))
            return null;
        
        Tree leaf = decl.getLeaf();
        
        if (class2Kind.get(MethodTree.class).contains(leaf.getKind())) {
            MethodTree method = (MethodTree) leaf;
            List<Tree> rightTrees = new ArrayList<Tree>();

            rightTrees.addAll(method.getParameters());
            rightTrees.addAll(method.getThrows());
            rightTrees.add(method.getBody());

            Name name = method.getName();
            
            if (method.getReturnType() == null)
                name = ((ClassTree) decl.getParentPath().getLeaf()).getSimpleName();
            
            return findIdentifierSpanImpl(info, leaf, method.getReturnType(), rightTrees, name.toString(), info.getCompilationUnit(), info.getTrees().getSourcePositions());
        }
        if (class2Kind.get(VariableTree.class).contains(leaf.getKind())) {
            VariableTree var = (VariableTree) leaf;
            // see #240912 - lambda implicit-typed parameter has synthetic type, shouldn't be searched.
            boolean typeSynthetic = var.getType() == null || info.getTreeUtilities().isSynthetic(new TreePath(decl, var.getType()));
            return findIdentifierSpanImpl(info, leaf, 
                    typeSynthetic ? null : var.getType(), 
                    Collections.singletonList(var.getInitializer()), var.getName().toString(), info.getCompilationUnit(), info.getTrees().getSourcePositions());
        }
        if (class2Kind.get(MemberSelectTree.class).contains(leaf.getKind())) {
            return findIdentifierSpanImpl(info, (MemberSelectTree) leaf, info.getCompilationUnit(), info.getTrees().getSourcePositions());
        }
        if (class2Kind.get(MemberReferenceTree.class).contains(leaf.getKind())) {
            return findIdentifierSpanImpl(info, (MemberReferenceTree) leaf, info.getCompilationUnit(), info.getTrees().getSourcePositions());
        }
        if (class2Kind.get(ClassTree.class).contains(leaf.getKind())) {
            String name = ((ClassTree) leaf).getSimpleName().toString();
            
            if (name.length() == 0)
                return null;
            
            SourcePositions positions = info.getTrees().getSourcePositions();
            CompilationUnitTree cu = info.getCompilationUnit();
            ModifiersTree mods = ((ClassTree) leaf).getModifiers();
            
            int start = mods != null ? (int)positions.getEndPosition(cu, mods) : -1;
            if (start == (-1))
                start = (int)positions.getStartPosition(cu, leaf);
            int end = (int)positions.getEndPosition(cu, leaf);

            if (start == (-1) || end == (-1)) {
                return null;
            }
            
            return findTokenWithText(info, name, start, end);
        }
        if (class2Kind.get(IdentifierTree.class).contains(leaf.getKind())) {
            return findIdentifierSpanImpl(info, (IdentifierTree) leaf, info.getCompilationUnit(), info.getTrees().getSourcePositions());
        }
        if (class2Kind.get(ParameterizedTypeTree.class).contains(leaf.getKind())) {
            return findIdentifierSpanImpl(info, new TreePath(decl, ((ParameterizedTypeTree) leaf).getType()));
        }
        if (class2Kind.get(AnnotatedTypeTree.class).contains(leaf.getKind())) {
            return findIdentifierSpanImpl(info, new TreePath(decl, ((AnnotatedTypeTree) leaf).getUnderlyingType()));
        }
        if (class2Kind.get(BreakTree.class).contains(leaf.getKind())) {
            Name name = ((BreakTree) leaf).getLabel();
            
            if (name == null || name.length() == 0)
                return null;
            
            SourcePositions positions = info.getTrees().getSourcePositions();
            CompilationUnitTree cu = info.getCompilationUnit();
            int start = (int)positions.getStartPosition(cu, leaf);
            int end   = (int)positions.getEndPosition(cu, leaf);
            
            if (start == (-1) || end == (-1)) {
                return null;
            }
            
           return findTokenWithText(info, name.toString(), start, end);
        }
        if (class2Kind.get(ContinueTree.class).contains(leaf.getKind())) {
            Name name = ((ContinueTree) leaf).getLabel();
            
            if (name == null || name.length() == 0)
                return null;
            
            SourcePositions positions = info.getTrees().getSourcePositions();
            CompilationUnitTree cu = info.getCompilationUnit();
            int start = (int)positions.getStartPosition(cu, leaf);
            int end   = (int)positions.getEndPosition(cu, leaf);
            
            if (start == (-1) || end == (-1)) {
                return null;
            }
            
           return findTokenWithText(info, name.toString(), start, end);
        }
        if (class2Kind.get(LabeledStatementTree.class).contains(leaf.getKind())) {
            Name name = ((LabeledStatementTree) leaf).getLabel();
            
            if (name == null || name.length() == 0)
                return null;
            
            SourcePositions positions = info.getTrees().getSourcePositions();
            CompilationUnitTree cu = info.getCompilationUnit();
            int start = (int)positions.getStartPosition(cu, leaf);
            int end   = (int)positions.getStartPosition(cu, ((LabeledStatementTree) leaf).getStatement());
            
            if (start == (-1) || end == (-1)) {
                return null;
            }
            
           return findTokenWithText(info, name.toString(), start, end);
        }
        throw new IllegalArgumentException("Only MethodDecl, VariableDecl, MemberSelectTree, IdentifierTree, ParameterizedTypeTree, AnnotatedTypeTree, ClassDecl, BreakTree, ContinueTree, LabeledStatementTree and BindingPatternTree are accepted by this method. Got: " + leaf.getKind());
    }

    public static int[] findIdentifierSpan( final TreePath decl, final CompilationInfo info, final Document doc) {
        final int[] result = new int[] {-1, -1};
        Runnable r = new Runnable() {
            public void run() {
                Token<JavaTokenId> t = findIdentifierSpan(info, doc, decl);
                if (t != null) {
                    result[0] = t.offset(null);
                    result[1] = t.offset(null) + t.length();
                }
            }
        };
        if (doc != null) {
            doc.render(r);
        } else {
            r.run();
        }
        return result;
    }
    
    public static Token<JavaTokenId> findIdentifierSpan(final CompilationInfo info, final Document doc, final TreePath decl) {
        @SuppressWarnings("unchecked")
        final Token<JavaTokenId>[] result = new Token[1];
        Runnable r = new Runnable() {
            public void run() {
                result[0] = findIdentifierSpanImpl(info, decl);
            }
        };
        if (doc != null) {
            doc.render(r);
        } else {
            r.run();
        }
        return result[0];
    }
    
    /**
     * Finds end position among supplied Trees. The method accepts Tree, or List&lt? extends Tree> as the untyped
     * vararg parameter. It's assumed that the caller passes varargs list is sorted starting from the rightmost tree or forest, so
     * mutual relationships of items in the vararg list are not checked.
     * 
     * The method returns end position of the first found tree; synthetic trees are ignored.
     * 
     * @param cu compilation unit
     * @param pos position information
     * @param treeSets trees or forests
     * @return end position or -1, if no suitable subtree was found.
     */
    private static int findSubtreeEnd(CompilationUnitTree cu, SourcePositions pos, Object... treeSets) {
        for (Object o : treeSets) {
            if (o == null) {
                continue;
            }
            if (o instanceof Tree) {
                int offset = (int)pos.getEndPosition(cu, (Tree)o);
                if (offset >= 0) {
                    return offset;
                }
            } else { 
                List<? extends Tree> set = (List<? extends Tree>)o;
                if (!set.isEmpty()) {
                    // assume that the compiler will fake a single item in otherwise empty list; it should not add a fake item after some real Tree items.
                    Tree t = set.get(set.size() - 1);
                    int offset = (int)pos.getEndPosition(cu, t);
                    if (offset >= 0) {
                        return offset;
                    }
                }
            }
        }
        return -1;
    }
    
    private static int findBodyStartImpl(CompilationInfo info, Tree cltree, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int start = (int)positions.getStartPosition(cu, cltree);
        int end   = (int)positions.getEndPosition(cu, cltree);
        
        if (start == (-1) || end == (-1)) {
            return -1;
        }
        int startPos = -1;
        switch (cltree.getKind()) {
            case CLASS: case INTERFACE: case ENUM: {
                ClassTree ct = (ClassTree)cltree;
                startPos = findSubtreeEnd(cu, positions,
                        ct.getImplementsClause(),
                        ct.getExtendsClause(),
                        ct.getTypeParameters(),
                        ct.getModifiers()
                );
                break;
            }
                
            case METHOD:  {
                // if the method contains some parameters, skip to the end of the parameter list
                MethodTree mt = (MethodTree)cltree;
                startPos = findSubtreeEnd(cu, positions, mt.getDefaultValue(), mt.getThrows(), mt.getParameters(), mt.getModifiers());
                break;
            }
        }
        if (startPos > start) {
            start = startPos;
        }
        
        if (start > doc.getLength() || end > doc.getLength()) {
            if (DEBUG) {
                System.err.println("Log: position outside document: ");
                System.err.println("decl = " + cltree);
                System.err.println("startOffset = " + start);
                System.err.println("endOffset = " + end);
                Thread.dumpStack();
            }
            
            return (-1);
        }
        TokenHierarchy<JavaTokenId> th = (TokenHierarchy<JavaTokenId>)info.getTokenHierarchy();
        TokenSequence<JavaTokenId> seq = (TokenSequence<JavaTokenId>)th.tokenSequence();
        seq.move(start);
        while (seq.moveNext()) {
            if (seq.token().id() == JavaTokenId.LBRACE) {
                return seq.offset();
            }
        }
        return (-1);
    }
    
    public static int findBodyStart(final CompilationInfo info, final Tree cltree, final CompilationUnitTree cu, final SourcePositions positions, final Document doc) {
        Kind kind = cltree.getKind();
        if (!TreeUtilities.CLASS_TREE_KINDS.contains(kind) && kind != Kind.METHOD && !cltree.getKind().toString().equals("RECORD"))
            throw new IllegalArgumentException("Unsupported kind: "+ kind);
        final int[] result = new int[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = findBodyStartImpl(info, cltree, cu, positions, doc);
            }
        });
        
        return result[0];
    }
    
    private static int findLastBracketImpl(Tree tree, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int start = (int)positions.getStartPosition(cu, tree);
        int end   = (int)positions.getEndPosition(cu, tree);
        
        if (start == (-1) || end == (-1)) {
            return -1;
        }
        
        if (start > doc.getLength() || end > doc.getLength()) {
            if (DEBUG) {
                System.err.println("Log: position outside document: ");
                System.err.println("decl = " + tree);
                System.err.println("startOffset = " + start);
                System.err.println("endOffset = " + end);
                Thread.dumpStack();
            }
            
            return (-1);
        }
        
        try {
            String text = doc.getText(end - 1, 1);
            
            if (text.charAt(0) == '}')
                return end - 1;
        } catch (BadLocationException e) {
            LOG.log(Level.INFO, null, e);
        }
        
        return (-1);
    }
    
    public static int findLastBracket(final Tree tree, final CompilationUnitTree cu, final SourcePositions positions, final Document doc) {
        final int[] result = new int[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = findLastBracketImpl(tree, cu, positions, doc);
            }
        });
        
        return result[0];
    }
    
    private static Token<JavaTokenId> createHighlightImpl(CompilationInfo info, Document doc, TreePath tree) {
        Tree leaf = tree.getLeaf();
        SourcePositions positions = info.getTrees().getSourcePositions();
        CompilationUnitTree cu = info.getCompilationUnit();
        
        //XXX: do not use instanceof:
        if (leaf instanceof MethodTree || leaf instanceof VariableTree || leaf instanceof ClassTree
                || leaf instanceof MemberSelectTree || leaf instanceof AnnotatedTypeTree || leaf instanceof MemberReferenceTree
                || "BINDING_PATTERN".equals(leaf.getKind().name())) {
            return findIdentifierSpan(info, doc, tree);
        }
        
        int start = (int) positions.getStartPosition(cu, leaf);
        int end = (int) positions.getEndPosition(cu, leaf);
        
        if (start == Diagnostic.NOPOS || end == Diagnostic.NOPOS) {
            return null;
        }
        
        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());
        
        if (ts.move(start) == Integer.MAX_VALUE) {
            return null;
        }
        
        if (ts.moveNext()) {
            Token<JavaTokenId> token = ts.token();
            if (ts.offset() == start && token != null) {
                final JavaTokenId id = token.id();
                if (id == JavaTokenId.IDENTIFIER) {
                    return token;
                }
                if (id == JavaTokenId.THIS || id == JavaTokenId.SUPER) {
                    return ts.offsetToken();
                }
            }
        }
        
        return null;
    }
    
    public static Token<JavaTokenId> getToken(final CompilationInfo info, final Document doc, final TreePath tree) {
        @SuppressWarnings("unchecked")
        final Token<JavaTokenId>[] result = new Token[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = createHighlightImpl(info, doc, tree);
            }
        });
        
        return result[0];
    }
    
    private static final Set<String> keywords;
    private static final Set<String> nonCtorKeywords;
    
    static {
        keywords = new HashSet<String>();
        
        keywords.add("true");
        keywords.add("false");
        keywords.add("null");
        keywords.add("this");
        keywords.add("super");
        keywords.add("class");

        nonCtorKeywords = new HashSet<String>(keywords);
        nonCtorKeywords.remove("this");
        nonCtorKeywords.remove("super");

    }
    
    public static boolean isKeyword(Tree tree) {
        if (tree.getKind() == Kind.IDENTIFIER) {
            return keywords.contains(((IdentifierTree) tree).getName().toString());
        }
        if (tree.getKind() == Kind.MEMBER_SELECT) {
            return keywords.contains(((MemberSelectTree) tree).getIdentifier().toString());
        }
        
        return false;
    }

    public static boolean isNonCtorKeyword(Tree tree) {
        if (tree.getKind() == Kind.IDENTIFIER) {
            return nonCtorKeywords.contains(((IdentifierTree) tree).getName().toString());
        }
        if (tree.getKind() == Kind.MEMBER_SELECT) {
            return nonCtorKeywords.contains(((MemberSelectTree) tree).getIdentifier().toString());
        }

        return false;
    }

    private static final Set<ElementKind> LOCAL_ELEMENT_KINDS = EnumSet.of(ElementKind.PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.EXCEPTION_PARAMETER, ElementKind.RESOURCE_VARIABLE, ElementKind.BINDING_VARIABLE);
    
    public static boolean isPrivateElement(Element el) {
        return LOCAL_ELEMENT_KINDS.contains(el.getKind()) || el.getModifiers().contains(Modifier.PRIVATE);
    }

    public static Element toRecordComponent(Element el) {
        if (el == null || el.getKind() != ElementKind.FIELD) {
            return el;
        }
        TypeElement owner = (TypeElement) el.getEnclosingElement();
        if (!ElementKind.RECORD.equals(owner.getKind())) {
            return el;
        }
        for (Element encl : owner.getEnclosedElements()) {
            if (encl.getKind() == ElementKind.RECORD_COMPONENT &&
                encl.getSimpleName().equals(el.getSimpleName())) {
                return encl;
            }
        }
        return el;
    }

}
