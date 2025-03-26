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
package org.netbeans.modules.java.editor.javadoc;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.tree.Scope;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.java.completion.Utilities;
import org.netbeans.modules.java.editor.base.javadoc.JavadocCompletionUtils;
import org.netbeans.modules.java.editor.javadoc.TagRegistery.TagEntry;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Pair;

public class JavadocCompletionTask<T> extends UserTask {

    public static <I> JavadocCompletionTask<I> create(final int caretOffset, @NonNull final ItemFactory<I> factory, boolean isAllQueryType, @NullAllowed final Callable<Boolean> cancel) {
        return new JavadocCompletionTask<>(caretOffset, factory, isAllQueryType, cancel);
    }

    public static interface ItemFactory<T> {
        T createTagItem(String name, int startOffset);
        T createNameItem(String name, int startOffset);
        T createJavadocExecutableItem(CompilationInfo info, ExecutableElement e, ExecutableType et, int startOffset, boolean isInherited, boolean isDeprecated);
        T createJavadocTypeItem(CompilationInfo info, TypeElement elem, int startOffset, boolean isDeprecated);
        T createJavaTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int startOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean smartType);
        T createLazyTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int startOffset, ReferencesCount referencesCount, Source source);
        T createJavaVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int startOffset, boolean isInherited, boolean isDeprecated);
        T createPackageItem(String pkgFQN, int startOffset);
    }

    private static final EnumSet<ElementKind> EXECUTABLE = EnumSet.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR);
    private static final EnumSet<ElementKind> TYPE_KINDS = EnumSet.of(
            ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM, ElementKind.RECORD, ElementKind.ANNOTATION_TYPE);

    private static final String CLASS_KEYWORD = "class"; //NOI18N

    private final List<T> items = new ArrayList<>();
    private final int caretOffset;
    private final ItemFactory<T> factory;
    private final boolean isAllQueryType;
    private final Callable<Boolean> cancel;
    private int anchorOffset = -1;
    private boolean hasAdditionalItems = false;

    private JavadocCompletionTask(int caretOffset, ItemFactory<T> factory, boolean isAllQueryType, Callable<Boolean> cancel) {
        this.caretOffset = caretOffset;
        this.factory = factory;
        this.isAllQueryType = isAllQueryType;
        this.cancel = cancel;
    }

    @Override
    public void run(ResultIterator resultIterator) throws Exception {
        Parser.Result result = resultIterator.getParserResult(caretOffset);
        CompilationController controller = result != null ? CompilationController.get(result) : null;
        if (controller != null && (cancel == null || !cancel.call())) {
            if (!JavadocCompletionUtils.isJavadocContext(controller.getTokenHierarchy(), caretOffset)) {
                return;
            }
            JavaSource.Phase toPhase = controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            if (toPhase != JavaSource.Phase.ELEMENTS_RESOLVED) {
                return;
            }
            JavadocContext jdctx = new JavadocContext(controller);
            if (resolveContext(controller, jdctx)) {
                analyzeContext(jdctx);
            }
        }
    }

    public List<T> getResults() {
        return items;
    }

    public boolean hasAdditionalItems() {
        return hasAdditionalItems;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    private boolean resolveContext(CompilationInfo javac, JavadocContext jdctx) throws IOException {
        jdctx.doc = javac.getDocument();
        // find class context: class, method, ...
        DocTrees trees = javac.getDocTrees();
        TreePath javadocFor = JavadocCompletionUtils.findJavadoc(javac, this.caretOffset);
        if (javadocFor == null) {
            return false;
        }
        jdctx.javadocFor = javadocFor;
        DocCommentTree docCommentTree = trees.getDocCommentTree(javadocFor);
        if (docCommentTree == null) {
            return false;
        }
        jdctx.comment = docCommentTree;
        Element elm = trees.getElement(javadocFor);
        if (elm == null) {
            return false;
        }
        jdctx.handle = ElementHandle.create(elm);
        jdctx.commentFor = elm;
        jdctx.jdts = JavadocCompletionUtils.findJavadocTokenSequence(javac, this.caretOffset);
        if (jdctx.jdts == null) {
            return false;
        }
        jdctx.positions = (DocSourcePositions) trees.getSourcePositions();
        return jdctx.positions != null;
    }

    private void analyzeContext(JavadocContext jdctx) {
        TokenSequence<JavadocTokenId> jdts = jdctx.jdts;
        if (jdts == null) {
            return;
        }
        jdts.move(this.caretOffset);
        JavadocTokenId javadocId;
        if (!jdts.moveNext() && !jdts.movePrevious()) {
            // XXX solve /***/
            // provide block tags, inline tags, html
            // XXX: for Markdown, continuing
            javadocId = JavadocTokenId.OTHER_TEXT;
        } else {
            if (this.caretOffset - jdts.offset() == 0) {
                // if position in token == 0 resolve CC according to previous token
                jdts.movePrevious();
            }
            javadocId = jdts.token().id();
        }
        switch (javadocId) {
            case TAG:
                resolveTagToken(jdctx);
                break;
            case IDENT:
                resolveIdent(jdctx);
                break;
            case DOT:
                resolveDotToken(jdctx);
                break;
            case HASH:
                resolveHashToken(jdctx);
                break;
            case OTHER_TEXT:
                resolveOtherText(jdctx, jdts);
                break;
            case HTML_TAG:
                resolveHTMLToken(jdctx);
                break;
        }
    }

    void resolveTagToken(JavadocContext jdctx) {
        assert jdctx.jdts.token() != null;
        assert jdctx.jdts.token().id() == JavadocTokenId.TAG;
        DocTreePath tag = getTag(jdctx, caretOffset);
        if (tag == null) {
            // eg * description @
            return;
        }
        if (JavadocCompletionUtils.isBlockTag(tag)) {
            resolveBlockTag(tag, jdctx);
        } else {
            resolveInlineTag(tag, jdctx);
        }
    }

    void resolveBlockTag(DocTreePath tag, JavadocContext jdctx) {
        int pos;
        String prefix;
        if (tag != null) {
            pos = (int) jdctx.positions.getStartPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, tag.getLeaf());
            prefix = JavadocCompletionUtils.getCharSequence(jdctx.doc, pos, caretOffset).toString();
        } else {
            prefix = ""; // NOI18N
            pos = caretOffset;
        }
        addBlockTagItems(jdctx.handle.getKind(), prefix, pos);
        anchorOffset = pos;
    }

    void resolveInlineTag(DocTreePath tag, JavadocContext jdctx) {
        int pos;
        String prefix;
        if (tag != null) {
            pos = (int) jdctx.positions.getStartPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, tag.getLeaf()) + 1;
            prefix = JavadocCompletionUtils.getCharSequence(jdctx.doc, pos, caretOffset).toString();
            anchorOffset = pos;
        } else {
            pos = caretOffset;
            prefix = ""; // NOI18N
            anchorOffset = pos;
        }
        addInlineTagItems(jdctx.handle.getKind(), prefix, pos);
    }

    private int skipWhitespacesBackwards(final JavadocContext jdctx, final int offset) {
        if (jdctx.jdts.move(offset) == 0 || !jdctx.jdts.moveNext()) {
            if (!jdctx.jdts.movePrevious()) {
                return offset;
            }
        }
        do {
            Token t = jdctx.jdts.token();
            if (t.id() != JavadocTokenId.OTHER_TEXT) {
                return jdctx.jdts.offset();
            }
            CharSequence text = t.text();
            for (int i = 0; i < text.length(); i++) {
                if (!Character.isWhitespace(text.charAt(i))) {
                    //XXX: does not handle the leading '*' correctly
                    return jdctx.jdts.offset();
                }
            }
        } while (jdctx.jdts.movePrevious());
        return jdctx.jdts.moveNext() ? jdctx.jdts.offset() : offset;
    }

    private DocTreePath getTag(final JavadocContext jdctx, final int offset) {
        final DocTreePath[] result = new DocTreePath[1];
        final int normalizedOffset = skipWhitespacesBackwards(jdctx, offset);
        new DocTreePathScanner<Void, Void>() {
            @Override
            public Void scan(DocTree node, Void p) {
                long endPos = jdctx.positions.getEndPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, node);
                long startPos = jdctx.positions.getStartPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, node);
                if (node.getKind() == Kind.ERRONEOUS && getCurrentPath() != null) {
                    String text = jdctx.javac.getText().substring((int) startPos, (int) endPos);
                    if (text.length() > 0 && text.charAt(0) == '{' && text.charAt(text.length() - 1) != '}') {
                        endPos = jdctx.positions.getEndPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, getCurrentPath().getLeaf());
                    }
                }
                if (node != null && startPos <= normalizedOffset && endPos >= normalizedOffset) {
                    final DocTreePath docTreePath = new DocTreePath(getCurrentPath(), node);
                    if (JavadocCompletionUtils.isBlockTag(docTreePath) || JavadocCompletionUtils.isInlineTag(docTreePath)) {
                        result[0] = docTreePath;
                    }
                    return super.scan(node, p);
                }
                return null;
            }
        }.scan(new DocTreePath(jdctx.javadocFor, jdctx.comment), null);
        return result[0];
    }

    void resolveIdent(JavadocContext jdctx) {
        TokenSequence<JavadocTokenId> jdts = jdctx.jdts;
        assert jdts.token() != null;
        assert jdts.token().id() == JavadocTokenId.IDENT;
        // @see package.Class[.NestedClass]#member[()]
        // START -> TAG OT(WS+) MEMBER_SELECT|CLASS_SELECT
        // CLASS_SELECT -> IDENT | IDENT DOT CLASS_SELECT | IDENT MEMBER_SELECT
        // MEMBER_SELECT -> HASH IDENT OT('(')
        // @see org.Clazz#meth(int p, int q)
        // TAG OT(' ') IDENT DOT IDENT HASH IDENT OT('(') IDENT OT(' ') IDENT OT(', ') IDENT OT(' ') IDENT OT(')\n...')
        // @see Clazz#meth(int p, int q)
        // @see #meth(int p, int q)
        // @see Clazz.NestedClazz
        // Parenthesis content:
        // param types not neccessary to be imported or fqn!!!
        // param types may be fqn
        // param types never contains generics
        // params may contain name but they not necessary match the real names
        // param list may contain spaces
        // no space allowed between member and parenthesis
        DocTreePath tag = getTag(jdctx, caretOffset);
        if (tag != null) {
            insideTag(tag, jdctx);
        }
    }

    void resolveDotToken(JavadocContext jdctx) {
        assert jdctx.jdts.token() != null;
        assert jdctx.jdts.token().id() == JavadocTokenId.DOT;
        DocTreePath tag = getTag(jdctx, caretOffset);
        if (tag != null) {
            insideTag(tag, jdctx);
        }
    }

    void resolveHashToken(JavadocContext jdctx) {
        assert jdctx.jdts.token() != null;
        assert jdctx.jdts.token().id() == JavadocTokenId.HASH;
        DocTreePath tag = getTag(jdctx, caretOffset);
        if (tag != null) {
            insideTag(tag, jdctx);
        }
    }

    void resolveHTMLToken(JavadocContext jdctx) {
        assert jdctx.jdts.token() != null;
        assert jdctx.jdts.token().id() == JavadocTokenId.HTML_TAG;
        DocTreePath tag = getTag(jdctx, caretOffset);
        if (tag != null && JavadocCompletionUtils.normalizedKind(tag.getLeaf()) == DocTree.Kind.PARAM) {
            // type param
            insideParamTag(tag, jdctx);
        }
    }

    private void insideTag(DocTreePath tag, JavadocContext jdctx) {
        switch (JavadocCompletionUtils.normalizedKind(tag.getLeaf())) {
            case IDENTIFIER:
                if (tag.getParentPath() == null || tag.getParentPath().getLeaf().getKind() != DocTree.Kind.PARAM) {
                    break;
                }
                tag = tag.getParentPath();
        //intentional fall-through:
            case PARAM:
                insideParamTag(tag, jdctx);
                break;
            case SEE:
            case THROWS:
            case VALUE:
            case LINK:
            case LINK_PLAIN:
                //XXX: was only unclosed???
                insideSeeTag(tag, jdctx);
                break;
            case REFERENCE:
                insideReference(tag, jdctx);
                break;
            case SNIPPET:
                insideSnippet(tag, jdctx);
                break;
        }
    }

    private void insideSeeTag(DocTreePath tag, JavadocContext jdctx) {
        TokenSequence<JavadocTokenId> jdts = jdctx.jdts;
        assert jdts.token() != null;
        int start = (int) jdctx.positions.getStartPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, tag.getLeaf());
        boolean isThrowsKind = JavadocCompletionUtils.normalizedKind(tag.getLeaf()) == DocTree.Kind.THROWS;
        if (isThrowsKind && !(EXECUTABLE.contains(jdctx.commentFor.getKind()))) {
            // illegal tag in this context
            return;
        }
        jdts.move(start + (JavadocCompletionUtils.isBlockTag(tag) ? 0 : 1));
        // @see|@link|@throws
        if (!jdts.moveNext() || caretOffset <= jdts.offset() + jdts.token().length()) {
            return;
        }
        // white space
        if (!jdts.moveNext() || caretOffset <= jdts.offset()) {
            return;
        }
        boolean noPrefix = false;
        if (caretOffset <= jdts.offset() + jdts.token().length()) {
            int pos = caretOffset - jdts.offset();
            CharSequence cs = jdts.token().text();
            cs = pos < cs.length() ? cs.subSequence(0, pos) : cs;
            if (JavadocCompletionUtils.isWhiteSpace(cs) || JavadocCompletionUtils.isLineBreak(jdts, pos)) {
                noPrefix = true;
            } else {
                // broken syntax
                return;
            }
        } else if (!(JavadocCompletionUtils.isWhiteSpace(jdts.token()) || JavadocCompletionUtils.isLineBreak(jdts))) {
            // not java reference
            return;
        } else if (jdts.moveNext()) {
            int end = (int) jdctx.positions.getEndPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, tag.getLeaf());
            insideReference(JavadocCompletionUtils.normalizedKind(tag.getLeaf()), jdts.offset(), end, jdctx);
        }
        if (noPrefix) {
            // complete all types + members
            if (isThrowsKind) {
                completeThrowsOrPkg(null, "", caretOffset, jdctx); // NOI18N
            } else {
                completeClassOrPkg(null, "", caretOffset, jdctx); // NOI18N
            }
            anchorOffset = caretOffset;
        }
    }

    private void insideReference(DocTreePath tag, JavadocContext jdctx) {
        ReferenceTree ref = (ReferenceTree) tag.getLeaf();
        DocTree.Kind kind = tag.getParentPath().getLeaf().getKind();
        int start = (int) jdctx.positions.getStartPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, ref);
        int end = (int) jdctx.positions.getEndPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, ref);
        insideReference(kind, start, end, jdctx);
    }

    private void insideReference(DocTree.Kind enclosingKind, int start, int end, JavadocContext jdctx) {
        // complete type
        CharSequence cs = JavadocCompletionUtils.getCharSequence(jdctx.doc, start, end);
        StringBuilder sb = new StringBuilder();
        for (int i = caretOffset - start - 1; i >= 0; i--) {
            char c = cs.charAt(i);
            if (c == '#') {
                // complete class member
                String prefix = sb.toString();
                int substitutionOffset = caretOffset - sb.length();
                anchorOffset = substitutionOffset;
                if (i == 0) {
                    if (enclosingKind == DocTree.Kind.VALUE) {
                        addLocalConstants(jdctx, prefix, substitutionOffset);
                    } else {
                        addLocalMembersAndVars(jdctx, prefix, substitutionOffset);
                    }
                } else {
                    TypeElement scopeType = jdctx.commentFor.getKind().isClass() || jdctx.commentFor.getKind().isInterface() ? (TypeElement) jdctx.commentFor : jdctx.javac.getElementUtilities().enclosingTypeElement(jdctx.commentFor);
                    TypeMirror type = jdctx.javac.getTreeUtilities().parseType(cs.subSequence(0, i).toString(), scopeType);
                    if (enclosingKind == DocTree.Kind.VALUE) {
                        addMemberConstants(jdctx, prefix, substitutionOffset, type);
                    } else {
                        addMembers(jdctx, prefix, substitutionOffset, type, jdctx.javac.getTypes().asElement(type), EnumSet.<ElementKind>of(ElementKind.ENUM_CONSTANT, ElementKind.FIELD, ElementKind.METHOD, ElementKind.CONSTRUCTOR), null);
                    }
                }
                return;
            } else if (c == '.') {
                // complete class or package
                String prefix = sb.toString();
                String fqn = cs.subSequence(0, i).toString();
                int substitutionOffset = caretOffset - sb.length();
                anchorOffset = substitutionOffset;
                if (enclosingKind == DocTree.Kind.THROWS) {
                    completeThrowsOrPkg(fqn, prefix, substitutionOffset, jdctx);
                } else {
                    completeClassOrPkg(fqn, prefix, substitutionOffset, jdctx);
                }
                return;
            } else {
                sb.insert(0, c);
            }
        }
        // complete class or package
        String prefix = sb.toString();
        String fqn = null;
        int substitutionOffset = caretOffset - sb.length();
        anchorOffset = substitutionOffset;
        if (enclosingKind == DocTree.Kind.THROWS) {
            completeThrowsOrPkg(fqn, prefix, substitutionOffset, jdctx);
        } else {
            completeClassOrPkg(fqn, prefix, substitutionOffset, jdctx);
        }
    }

    private void insideParamTag(DocTreePath tag, JavadocContext jdctx) {
        TokenSequence<JavadocTokenId> jdts = jdctx.jdts;
        assert jdts.token() != null;
        int start = (int) jdctx.positions.getStartPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, tag.getLeaf());
        jdts.move(start);
        // @param
        if (!jdts.moveNext() || caretOffset <= jdts.offset() + jdts.token().length()) {
            return;
        }
        // white space
        if (!jdts.moveNext() || caretOffset <= jdts.offset()) {
            return;
        }
        if (caretOffset <= jdts.offset() + jdts.token().length()) {
            int pos = caretOffset - jdts.offset();
            CharSequence cs = jdts.token().text();
            cs = pos < cs.length() ? cs.subSequence(0, pos) : cs;
            if (JavadocCompletionUtils.isWhiteSpace(cs) || JavadocCompletionUtils.isLineBreak(jdts, pos)) {
                // none prefix
                anchorOffset = caretOffset;
                completeParamName(tag, "", caretOffset, jdctx); // NOI18N
                return;
            } else {
                // broken syntax
                return;
            }
        }
        jdts.moveNext(); // param name
        if (!(jdts.token().id() == JavadocTokenId.IDENT || jdts.token().id() == JavadocTokenId.HTML_TAG)) {
            // broken syntax
            return;
        }
        if (caretOffset <= jdts.offset() + jdts.token().length()) {
            CharSequence prefix = jdts.token().text().subSequence(0, caretOffset - jdts.offset());
            anchorOffset = jdts.offset();
            completeParamName(tag, prefix.toString(), jdts.offset(), jdctx);
        }
    }

    private void completeParamName(DocTreePath tag, String prefix, int substitutionOffset, JavadocContext jdctx) {
        if (EXECUTABLE.contains(jdctx.commentFor.getKind())) {
            List<? extends DocTree> blockTags = jdctx.comment.getBlockTags();
            ExecutableElement method = (ExecutableElement) jdctx.commentFor;
            for (VariableElement param : method.getParameters()) {
                String name = param.getSimpleName().toString();
                if (!containsParam(blockTags, name) && name.startsWith(prefix)) {
                    items.add(factory.createNameItem(name, substitutionOffset));
                }
            }
            completeTypeVarName(jdctx.commentFor, prefix, substitutionOffset);
        } else if (jdctx.commentFor.getKind().isClass() || jdctx.commentFor.getKind().isInterface()) {
            completeTypeVarName(jdctx.commentFor, prefix, substitutionOffset);
        }
    }

    private boolean containsParam(List<? extends DocTree> blockTags, String name) {
        for (DocTree blockTag : blockTags) {
            if (blockTag.getKind() == DocTree.Kind.PARAM) {
                ParamTree param = (ParamTree) blockTag;
                if (name.contentEquals(param.getName().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void completeTypeVarName(Element forElement, String prefix, int substitutionOffset) {
        if (prefix.length() > 0) {
            if (prefix.charAt(0) == '<') {
                prefix = prefix.substring(1);
            } else {
                // not type param
                return;
            }
        }
        List<? extends TypeParameterElement> tparams = (forElement.getKind().isClass() || forElement.getKind().isInterface()) ? ((TypeElement) forElement).getTypeParameters() : ((ExecutableElement) forElement).getTypeParameters();
        for (TypeParameterElement typeVariable : tparams) {
            String name = typeVariable.getSimpleName().toString();
            if (name.startsWith(prefix)) {
                items.add(factory.createNameItem('<' + name + '>', substitutionOffset));
            }
        }
    }

    private void completeClassOrPkg(String fqn, String prefix, int substitutionOffset, JavadocContext jdctx) {
        String pkgPrefix;
        if (fqn == null) {
            pkgPrefix = prefix;
            addTypes(TYPE_KINDS, null, null, prefix, substitutionOffset, jdctx);
        } else {
            pkgPrefix = fqn + '.' + prefix;
            PackageElement pkgElm = jdctx.javac.getElements().getPackageElement(fqn);
            if (pkgElm != null) {
                addPackageContent(pkgElm, TYPE_KINDS, null, null, prefix, substitutionOffset, jdctx);
            }
            TypeElement typeElm = jdctx.javac.getElements().getTypeElement(fqn);
            if (typeElm != null) {
                // inner classes
                addInnerClasses(typeElm, TYPE_KINDS, null, null, prefix, substitutionOffset, jdctx);
            }
        }
        for (String pkgName : jdctx.javac.getClasspathInfo().getClassIndex().getPackageNames(pkgPrefix, true, EnumSet.allOf(ClassIndex.SearchScope.class))) {
            if (pkgName.length() > 0 && !Utilities.isExcluded(pkgName + ".")) {
                items.add(factory.createPackageItem(pkgName, substitutionOffset));
            }
        }
    }

    private void completeThrowsOrPkg(String fqn, String prefix, int substitutionOffset, JavadocContext jdctx) {
        final Elements elements = jdctx.javac.getElements();
        final Set<Element> excludes = new HashSet<Element>();
        String pkgPrefix;
        // add declared Throwables
        ExecutableElement method = (ExecutableElement) jdctx.commentFor;
        for (TypeMirror type : method.getThrownTypes()) {
            if (type.getKind() != TypeKind.DECLARED) {
                continue;
            }
            TypeElement clazz = (TypeElement) ((DeclaredType) type).asElement();
            String typeName = clazz.getSimpleName().toString();
            if (startsWith(typeName, prefix)) {
                String qualTypeName = clazz.getQualifiedName().toString();
                TypeElement typeElement = elements.getTypeElement(qualTypeName);
                if (typeElement == null) {
                    continue;
                }
                items.add(factory.createJavaTypeItem(jdctx.javac, typeElement, (DeclaredType) typeElement.asType(), substitutionOffset, /*XXX:*/ typeName != qualTypeName ? jdctx.getReferencesCount() : null, elements.isDeprecated(typeElement), true));
                excludes.add(typeElement);
            }
        }
        // add other Throwables
        if (fqn == null) {
            pkgPrefix = prefix;
            addTypes(EnumSet.<ElementKind>of(ElementKind.CLASS), findDeclaredType("java.lang.Throwable", elements), // NOI18N
            excludes, prefix, substitutionOffset, jdctx);
        } else {
            pkgPrefix = fqn + '.' + prefix;
            PackageElement pkgElm = elements.getPackageElement(fqn);
            if (pkgElm != null) {
                addPackageContent(pkgElm, EnumSet.<ElementKind>of(ElementKind.CLASS), findDeclaredType("java.lang.Throwable", elements), // NOI18N
                excludes, prefix, substitutionOffset, jdctx);
            }
            TypeElement typeElm = elements.getTypeElement(fqn);
            if (typeElm != null) {
                // inner classes
                addInnerClasses(typeElm, EnumSet.<ElementKind>of(ElementKind.CLASS), findDeclaredType("java.lang.Throwable", elements), // NOI18N
                excludes, prefix, substitutionOffset, jdctx);
            }
        }
        // add packages
        for (String pkgName : jdctx.javac.getClasspathInfo().getClassIndex().getPackageNames(pkgPrefix, true, EnumSet.allOf(ClassIndex.SearchScope.class))) {
            if (pkgName.length() > 0) {
                items.add(factory.createPackageItem(pkgName, substitutionOffset));
            }
        }
    }

    private DeclaredType findDeclaredType(CharSequence fqn, Elements elements) {
        TypeElement re = elements.getTypeElement(fqn);
        if (re != null) {
            TypeMirror asType = re.asType();
            if (asType.getKind() == TypeKind.DECLARED) {
                return (DeclaredType) asType;
            }
        }
        return null;
    }

    private void addBlockTagItems(ElementKind kind, String prefix, int startOffset) {
        List<TagEntry> tags = TagRegistery.getDefault().getTags(kind, false);
        for (TagEntry tagEntry : tags) {
            if (tagEntry.name.startsWith(prefix)) {
                items.add(factory.createTagItem(tagEntry.name, startOffset));
            }
        }
    }

    private void addInlineTagItems(ElementKind kind, String prefix, int startOffset) {
        List<TagEntry> tags = TagRegistery.getDefault().getTags(kind, true);
        for (TagEntry tagEntry : tags) {
            if (tagEntry.name.startsWith(prefix)) {
                items.add(factory.createTagItem(tagEntry.name, startOffset));
            }
        }
    }

    private void addMembers(final JavadocContext env, final String prefix, final int substitutionOffset, final TypeMirror type, final Element elem, final EnumSet<ElementKind> kinds, final DeclaredType baseType) {
        //        Set<? extends TypeMirror> smartTypes = /*queryType == COMPLETION_QUERY_TYPE ? env.getSmartTypes() :*/ null;
        final CompilationInfo controller = env.javac;
        final Trees trees = controller.getTrees();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final TreeUtilities tu = controller.getTreeUtilities();
        final ElementUtilities eu = controller.getElementUtilities();
        TypeElement typeElem = type.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType) type).asElement() : null;
        //        final boolean isStatic = elem != null && (elem.getKind().isClass() || elem.getKind().isInterface() || elem.getKind() == TYPE_PARAMETER);
        //        final boolean isSuperCall = elem != null && elem.getKind().isField() && elem.getSimpleName().contentEquals(SUPER_KEYWORD);
        Element docelm = env.handle.resolve(controller);
        TreePath docpath = docelm != null ? trees.getPath(docelm) : null;
        final Scope scope = docpath != null ? trees.getScope(docpath) : tu.scopeFor(caretOffset);
        TypeElement enclClass = scope.getEnclosingClass();
        //        final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror t) {
                switch (e.getKind()) {
                    case FIELD:
                        String name = e.getSimpleName().toString();
                        return Utilities.startsWith(name, prefix) && !CLASS_KEYWORD.equals(name) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e));
                //                                &&
                //                                isOfKindAndType(asMemberOf(e, t, types), e, kinds, baseType, scope, trees, types) &&
                //                                tu.isAccessible(scope, e, isSuperCall && enclType != null ? enclType : t) &&
                //                                (!isStatic || e.getModifiers().contains(Modifier.STATIC)) &&
                //                                (isStatic || !e.getSimpleName().contentEquals(THIS_KEYWORD)) &&
                //                                ((isStatic && !inImport) /*|| !e.getSimpleName().contentEquals(CLASS_KEYWORD)*/);
                    case ENUM_CONSTANT:
                        return Utilities.startsWith(e.getSimpleName().toString(), prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && //                                isOfKindAndType(asMemberOf(e, t, types), e, kinds, baseType, scope, trees, types) &&
                        trees.isAccessible(scope, e, (DeclaredType) t);
                    case METHOD:
                        String sn = e.getSimpleName().toString();
                        return Utilities.startsWith(sn, prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && (!Utilities.isExcludeMethods() || !Utilities.isExcluded(eu.getElementName(e.getEnclosingElement(), true) + "." + sn)); //NOI18N
                //                                &&
                //                                isOfKindAndType(((ExecutableType)asMemberOf(e, t, types)).getReturnType(), e, kinds, baseType, scope, trees, types) &&
                //                                (isSuperCall && e.getModifiers().contains(PROTECTED) || tu.isAccessible(scope, e, isSuperCall && enclType != null ? enclType : t)) &&
                //                                (!isStatic || e.getModifiers().contains(Modifier.STATIC));
                //                    case CLASS:
                //                    case ENUM:
                //                    case INTERFACE:
                //                    case ANNOTATION_TYPE:
                //                        return Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                //                                (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) &&
                //                                isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types) &&
                //                                tu.isAccessible(scope, e, t) && isStatic;
                    case CONSTRUCTOR:
                        return (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && //                                isOfKindAndType(e.getEnclosingElement().asType(), e, kinds, baseType, scope, trees, types) &&
                        (trees.isAccessible(scope, e, (DeclaredType) t) || (elem.getModifiers().contains(Modifier.ABSTRACT) && !e.getModifiers().contains(Modifier.PRIVATE)));
                        //                                &&
                        //                                isStatic;
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getMembers(type, acceptor)) {
            switch (e.getKind()) {
                case ENUM_CONSTANT:
                case FIELD:
                    TypeMirror tm = type.getKind() == TypeKind.DECLARED ? types.asMemberOf((DeclaredType) type, e) : e.asType();
                    items.add(factory.createJavaVariableItem(controller, (VariableElement) e, tm, substitutionOffset, typeElem != e.getEnclosingElement(), elements.isDeprecated(e)));
                    break;
                case CONSTRUCTOR:
                case METHOD:
                    ExecutableType et = (ExecutableType) (type.getKind() == TypeKind.DECLARED ? types.asMemberOf((DeclaredType) type, e) : e.asType());
                    //                    items.add(JavaCompletionItem.createExecutableItem((ExecutableElement) e, et, substitutionOffset, typeElem != e.getEnclosingElement(), elements.isDeprecated(e), inImport, /*isOfSmartType(env, et.getReturnType(), smartTypes)*/false));
                    items.add(factory.createJavadocExecutableItem(controller, (ExecutableElement) e, et, substitutionOffset, typeElem != e.getEnclosingElement(), elements.isDeprecated(e)));
                    break;
                    //                case CLASS:
                    //                case ENUM:
                    //                case INTERFACE:
                    //                case ANNOTATION_TYPE:
                    //                    DeclaredType dt = (DeclaredType)(type.getKind() == TypeKind.DECLARED ? types.asMemberOf((DeclaredType)type, e) : e.asType());
                    //                    results.add(JavaCompletionItem.createTypeItem((TypeElement)e, dt, anchorOffset, false, elements.isDeprecated(e), insideNew, false));
                    //                    break;
            }
        }
    }

    private void addMemberConstants(final JavadocContext env, final String prefix, final int substitutionOffset, final TypeMirror type) {
        final CompilationInfo controller = env.javac;
        final Trees trees = controller.getTrees();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final TreeUtilities tu = controller.getTreeUtilities();
        TypeElement typeElem = type.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType) type).asElement() : null;
        Element docelm = env.handle.resolve(controller);
        TreePath docpath = docelm != null ? trees.getPath(docelm) : null;
        final Scope scope = docpath != null ? trees.getScope(docpath) : tu.scopeFor(caretOffset);
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror t) {
                switch (e.getKind()) {
                    case FIELD:
                        String name = e.getSimpleName().toString();
                        return ((VariableElement) e).getConstantValue() != null && Utilities.startsWith(name, prefix) && !CLASS_KEYWORD.equals(name) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e));
                    case ENUM_CONSTANT:
                        return ((VariableElement) e).getConstantValue() != null && Utilities.startsWith(e.getSimpleName().toString(), prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && trees.isAccessible(scope, e, (DeclaredType) t);
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getMembers(type, acceptor)) {
            switch (e.getKind()) {
                case ENUM_CONSTANT:
                case FIELD:
                    TypeMirror tm = type.getKind() == TypeKind.DECLARED ? types.asMemberOf((DeclaredType) type, e) : e.asType();
                    items.add(factory.createJavaVariableItem(controller, (VariableElement) e, tm, substitutionOffset, typeElem != e.getEnclosingElement(), elements.isDeprecated(e)));
                    break;
            }
        }
    }

    private void addLocalConstants(final JavadocContext env, final String prefix, final int substitutionOffset) {
        final CompilationInfo controller = env.javac;
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final TreeUtilities tu = controller.getTreeUtilities();
        final Trees trees = controller.getTrees();
        Element docelm = env.handle.resolve(controller);
        TreePath docpath = docelm != null ? trees.getPath(docelm) : null;
        final Scope scope = docpath != null ? trees.getScope(docpath) : tu.scopeFor(caretOffset);
        final TypeElement enclClass = scope.getEnclosingClass();
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror t) {
                switch (e.getKind()) {
                    case FIELD:
                        String name = e.getSimpleName().toString();
                        return ((VariableElement) e).getConstantValue() != null && Utilities.startsWith(name, prefix) && !CLASS_KEYWORD.equals(name) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e));
                    case ENUM_CONSTANT:
                        return ((VariableElement) e).getConstantValue() != null && Utilities.startsWith(e.getSimpleName().toString(), prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && trees.isAccessible(scope, e, (DeclaredType) t);
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
            switch (e.getKind()) {
                case ENUM_CONSTANT:
                    items.add(factory.createJavaVariableItem(controller, (VariableElement) e, e.asType(), substitutionOffset, scope.getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                    break;
                case FIELD:
                    TypeMirror tm = asMemberOf(e, enclClass != null ? enclClass.asType() : null, types);
                    items.add(factory.createJavaVariableItem(controller, (VariableElement) e, tm, substitutionOffset, scope.getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                    break;
            }
        }
    }

    private void addLocalMembersAndVars(final JavadocContext env, final String prefix, final int substitutionOffset) {
        final CompilationInfo controller = env.javac;
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final TreeUtilities tu = controller.getTreeUtilities();
        final Trees trees = controller.getTrees();
        Element docelm = env.handle.resolve(controller);
        TreePath docpath = docelm != null ? trees.getPath(docelm) : null;
        final Scope scope = docpath != null ? trees.getScope(docpath) : tu.scopeFor(caretOffset);
        //        Set<? extends TypeMirror> smartTypes = null;
        //        if (queryType == COMPLETION_QUERY_TYPE) {
        //            smartTypes = env.getSmartTypes();
        //            if (smartTypes != null) {
        //                for (TypeMirror st : smartTypes) {
        //                    if (st.getKind().isPrimitive())
        //                        st = types.boxedClass((PrimitiveType)st).asType();
        //                    if (st.getKind() == TypeKind.DECLARED) {
        //                        final DeclaredType type = (DeclaredType)st;
        //                        final TypeElement element = (TypeElement)type.asElement();
        //                        if (withinScope(env, element))
        //                            continue;
        //                        final boolean isStatic = element.getKind().isClass() || element.getKind().isInterface();
        //                        final Set<? extends TypeMirror> finalSmartTypes = smartTypes;
        //                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
        //                            public boolean accept(Element e, TypeMirror t) {
        //                                return (!isStatic || e.getModifiers().contains(STATIC)) &&
        //                                        Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
        //                                        tu.isAccessible(scope, e, t) &&
        //                                        (e.getKind().isField() && isOfSmartType(env, ((VariableElement)e).asType(), finalSmartTypes) || e.getKind() == METHOD && isOfSmartType(env, ((ExecutableElement)e).getReturnType(), finalSmartTypes));
        //                            }
        //                        };
        //                        for (Element ee : controller.getElementUtilities().getMembers(type, acceptor)) {
        //                            if (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(ee))
        //                                results.add(JavaCompletionItem.createStaticMemberItem(type, ee, types.asMemberOf(type, ee), anchorOffset, elements.isDeprecated(ee)));
        //                        }
        //                    }
        //                }
        //            }
        //        }
        final TypeElement enclClass = scope.getEnclosingClass();
        //        final boolean isStatic = enclClass == null ? false :
        //            (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree)env.getPath().getLeaf()).isStatic()));
        //        final Collection<? extends Element> illegalForwardRefs = env.getForwardReferences();
        //        final ExecutableElement method = scope.getEnclosingMethod();
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror t) {
                switch (e.getKind()) {
                    case CONSTRUCTOR:
                        return Utilities.startsWith(e.getEnclosingElement().getSimpleName().toString(), prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && //                                (!isStatic || e.getModifiers().contains(STATIC)) &&
                        trees.isAccessible(scope, e, (DeclaredType) t);
                //                    case LOCAL_VARIABLE:
                //                    case EXCEPTION_PARAMETER:
                //                    case PARAMETER:
                //                        return Utilities.startsWith(e.getSimpleName().toString(), prefix) &&
                //                                (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL) ||
                //                                (method == null && (e.getEnclosingElement().getKind() == INSTANCE_INIT ||
                //                                e.getEnclosingElement().getKind() == STATIC_INIT))) &&
                //                                !illegalForwardRefs.contains(e);
                    case FIELD:
                        //                        if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD))
                        //                            return Utilities.startsWith(e.getSimpleName().toString(), prefix) && !isStatic;
                        String name = e.getSimpleName().toString();
                        return Utilities.startsWith(name, prefix) && !CLASS_KEYWORD.equals(name) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e));
                //                        String name = e.getSimpleName().toString();
                //                        return !name.equals(THIS_KEYWORD) && !name.equals(SUPER_KEYWORD)
                //                                && Utilities.startsWith(name, prefix);
                    case ENUM_CONSTANT:
                        return Utilities.startsWith(e.getSimpleName().toString(), prefix) && //                                !illegalForwardRefs.contains(e) &&
                        //                                (!isStatic || e.getModifiers().contains(STATIC)) &&
                        (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && trees.isAccessible(scope, e, (DeclaredType) t);
                    case METHOD:
                        return Utilities.startsWith(e.getSimpleName().toString(), prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && //                                (!isStatic || e.getModifiers().contains(STATIC)) &&
                        trees.isAccessible(scope, e, (DeclaredType) t);
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
            switch (e.getKind()) {
                case ENUM_CONSTANT:
                    items.add(factory.createJavaVariableItem(controller, (VariableElement) e, e.asType(), substitutionOffset, scope.getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                    break;
                case FIELD:
                    String name = e.getSimpleName().toString();
                    TypeMirror tm = asMemberOf(e, enclClass != null ? enclClass.asType() : null, types);
                    items.add(factory.createJavaVariableItem(controller, (VariableElement) e, tm, substitutionOffset, scope.getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                    break;
                case CONSTRUCTOR:
                case METHOD:
                    ExecutableType et = (ExecutableType) asMemberOf(e, enclClass != null ? enclClass.asType() : null, types);
                    //                    items.add(JavaCompletionItem.createExecutableItem((ExecutableElement)e, et, substitutionOffset, scope.getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e), false, false/*isOfSmartType(env, et.getReturnType(), smartTypes)*/));
                    items.add(factory.createJavadocExecutableItem(controller, (ExecutableElement) e, et, substitutionOffset, scope.getEnclosingClass() != e.getEnclosingElement(), elements.isDeprecated(e)));
                    break;
            }
        }
    }

    private TypeMirror asMemberOf(Element element, TypeMirror type, Types types) {
        TypeMirror ret = element.asType();
        TypeMirror enclType = element.getEnclosingElement().asType();
        if (enclType.getKind() == TypeKind.DECLARED) {
            enclType = types.erasure(enclType);
        }
        while (type != null && type.getKind() == TypeKind.DECLARED) {
            if (types.isSubtype(type, enclType)) {
                ret = types.asMemberOf((DeclaredType) type, element);
                break;
            }
            type = ((DeclaredType) type).getEnclosingType();
        }
        return ret;
    }

    private void addTypes(EnumSet<ElementKind> kinds, DeclaredType baseType, Set<? extends Element> toExclude, String prefix, int substitutionOffset, JavadocContext jdctx) {
        if (isAllQueryType) {
            if (baseType == null) {
                addAllTypes(jdctx, kinds, toExclude, prefix, substitutionOffset);
            } else {
                Elements elements = jdctx.javac.getElements();
                for (DeclaredType subtype : getSubtypesOf(baseType, prefix, jdctx)) {
                    TypeElement elem = (TypeElement) subtype.asElement();
                    if ((toExclude == null || !toExclude.contains(elem)) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(elem))) {
                        items.add(factory.createJavaTypeItem(jdctx.javac, elem, subtype, substitutionOffset, jdctx.getReferencesCount(), elements.isDeprecated(elem), false));
                    }
                }
            }
        } else {
            addLocalAndImportedTypes(jdctx, kinds, baseType, toExclude, prefix, substitutionOffset);
            hasAdditionalItems = true;
        }
    }

    private void addLocalAndImportedTypes(final JavadocContext env, final EnumSet<ElementKind> kinds, final DeclaredType baseType, final Set<? extends Element> toExclude, final String prefix, int substitutionOffset) {
        final CompilationInfo controller = env.javac;
        final Trees trees = controller.getTrees();
        final Elements elements = controller.getElements();
        final Types types = controller.getTypes();
        final TreeUtilities tu = controller.getTreeUtilities();
        Element docelm = env.handle.resolve(controller);
        TreePath docpath = docelm != null ? trees.getPath(docelm) : null;
        final Scope scope = docpath != null ? trees.getScope(docpath) : tu.scopeFor(caretOffset);
        final TypeElement enclClass = scope.getEnclosingClass();
        final boolean isStatic = enclClass == null ? false : tu.isStaticContext(scope);
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror t) {
                if ((toExclude == null || !toExclude.contains(e)) && (e.getKind().isClass() || e.getKind().isInterface() || e.getKind() == ElementKind.TYPE_PARAMETER)) {
                    String name = e.getSimpleName().toString();
                    return name.length() > 0 && !Character.isDigit(name.charAt(0)) && startsWith(name, prefix) && (!isStatic || e.getModifiers().contains(Modifier.STATIC)) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types);
                }
                return false;
            }
        };
        for (Element e : controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor)) {
            switch (e.getKind()) {
                case CLASS:
                case ENUM:
                case INTERFACE:
                case ANNOTATION_TYPE:
                    items.add(factory.createJavadocTypeItem(env.javac, (TypeElement) e, substitutionOffset, elements.isDeprecated(e)));
                    break;
            }
        }
        acceptor = new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror t) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    return (toExclude == null || !toExclude.contains(e)) && startsWith(e.getSimpleName().toString(), prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && trees.isAccessible(scope, (TypeElement) e) && isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types);
                }
                return false;
            }
        };
        for (TypeElement e : controller.getElementUtilities().getGlobalTypes(acceptor)) {
            items.add(factory.createJavadocTypeItem(env.javac, e, substitutionOffset, elements.isDeprecated(e)));
        }
    }

    private void addAllTypes(JavadocContext env, EnumSet<ElementKind> kinds, Set<? extends Element> toExclude, String prefix, int substitutionOffset) {
        //        String prefix = env.getPrefix();
        CompilationInfo controller = env.javac;
        boolean isCaseSensitive = false;
        ClassIndex.NameKind kind = isCaseSensitive ? ClassIndex.NameKind.PREFIX : ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
        //        ClassIndex.NameKind kind = env.isCamelCasePrefix() ?
        //            Utilities.isCaseSensitive() ? ClassIndex.NameKind.CAMEL_CASE : ClassIndex.NameKind.CAMEL_CASE_INSENSITIVE :
        //            Utilities.isCaseSensitive() ? ClassIndex.NameKind.PREFIX : ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
        Set<ElementHandle<Element>> excludeHandles = null;
        if (toExclude != null) {
            excludeHandles = new HashSet<>(toExclude.size());
            for (Element el : toExclude) {
                excludeHandles.add(ElementHandle.create(el));
            }
        }
        for (ElementHandle<TypeElement> name : controller.getClasspathInfo().getClassIndex().getDeclaredTypes(prefix, kind, EnumSet.allOf(ClassIndex.SearchScope.class))) {
            if ((excludeHandles == null || !excludeHandles.contains(name)) && !isAnnonInner(name)) {
                items.add(factory.createLazyTypeItem(name, kinds, substitutionOffset, env.getReferencesCount(), controller.getSnapshot().getSource()));
            }
        }
    }

    private void addInnerClasses(TypeElement te, EnumSet<ElementKind> kinds, DeclaredType baseType, Set<? extends Element> toExclude, String prefix, int substitutionOffset, JavadocContext jdctx) {
        CompilationInfo controller = jdctx.javac;
        Element srcEl = jdctx.handle.resolve(controller);
        Elements elements = controller.getElements();
        Types types = controller.getTypes();
        Trees trees = controller.getTrees();
        TreeUtilities tu = controller.getTreeUtilities();
        TreePath docpath = srcEl != null ? trees.getPath(srcEl) : null;
        Scope scope = docpath != null ? trees.getScope(docpath) : tu.scopeFor(caretOffset);
        for (Element e : controller.getElementUtilities().getMembers(te.asType(), null)) {
            if ((e.getKind().isClass() || e.getKind().isInterface()) && (toExclude == null || !toExclude.contains(e))) {
                String name = e.getSimpleName().toString();
                if (Utilities.startsWith(name, prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && trees.isAccessible(scope, (TypeElement) e) && isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types)) {
                    items.add(factory.createJavadocTypeItem(jdctx.javac, (TypeElement) e, substitutionOffset, elements.isDeprecated(e)));
                }
            }
        }
    }

    private void addPackageContent(PackageElement pe, EnumSet<ElementKind> kinds, DeclaredType baseType, Set<? extends Element> toExclude, String prefix, int substitutionOffset, JavadocContext jdctx) {
        CompilationInfo controller = jdctx.javac;
        Element srcEl = jdctx.handle.resolve(controller);
        Elements elements = controller.getElements();
        Types types = controller.getTypes();
        Trees trees = controller.getTrees();
        TreeUtilities tu = controller.getTreeUtilities();
        ElementUtilities eu = controller.getElementUtilities();
        TreePath docpath = srcEl != null ? trees.getPath(srcEl) : null;
        Scope scope = docpath != null ? trees.getScope(docpath) : tu.scopeFor(caretOffset);
        for (Element e : pe.getEnclosedElements()) {
            if ((e.getKind().isClass() || e.getKind().isInterface()) && (toExclude == null || !toExclude.contains(e))) {
                String name = e.getSimpleName().toString();
                if (Utilities.startsWith(name, prefix) && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e)) && trees.isAccessible(scope, (TypeElement) e) && isOfKindAndType(e.asType(), e, kinds, baseType, scope, trees, types) && !Utilities.isExcluded(eu.getElementName(e, true))) {
                    items.add(factory.createJavadocTypeItem(jdctx.javac, (TypeElement) e, substitutionOffset, elements.isDeprecated(e)));
                }
            }
        }
    }

    private boolean isOfKindAndType(TypeMirror type, Element e, EnumSet<ElementKind> kinds, TypeMirror base, Scope scope, Trees trees, Types types) {
        if (type.getKind() != TypeKind.ERROR && kinds.contains(e.getKind())) {
            if (base == null) {
                return true;
            }
            if (types.isSubtype(type, base)) {
                return true;
            }
        }
        if ((e.getKind().isClass() || e.getKind().isInterface()) && (kinds.contains(ElementKind.ANNOTATION_TYPE) || kinds.contains(ElementKind.CLASS) || kinds.contains(ElementKind.ENUM) || kinds.contains(ElementKind.INTERFACE))) {
            DeclaredType dt = (DeclaredType) e.asType();
            for (Element ee : e.getEnclosedElements()) {
                if (trees.isAccessible(scope, ee, dt) && isOfKindAndType(ee.asType(), ee, kinds, base, scope, trees, types)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAnnonInner(ElementHandle<TypeElement> elem) {
        String name = elem.getQualifiedName();
        int idx = name.lastIndexOf('.'); //NOI18N
        String simpleName = idx > -1 ? name.substring(idx + 1) : name;
        return simpleName.length() == 0 || Character.isDigit(simpleName.charAt(0));
    }

    /* copied from JavaCompletionProvider */
    private List<DeclaredType> getSubtypesOf(DeclaredType baseType, String prefix, JavadocContext jdctx) {
        if (((TypeElement) baseType.asElement()).getQualifiedName().contentEquals("java.lang.Object")) {
            return Collections.emptyList();
        }
        LinkedList<DeclaredType> subtypes = new LinkedList<>();
        CompilationInfo controller = jdctx.javac;
        Types types = controller.getTypes();
        Trees trees = controller.getTrees();
        TreeUtilities tu = controller.getTreeUtilities();
        Element resolvedElm = jdctx.handle.resolve(controller);
        TreePath docpath = resolvedElm != null ? trees.getPath(resolvedElm) : null;
        Scope scope = docpath != null ? trees.getScope(docpath) : tu.scopeFor(caretOffset);
        if (prefix != null && prefix.length() > 2 && baseType.getTypeArguments().isEmpty()) {
            // XXX resolve camels
            //            ClassIndex.NameKind kind = env.isCamelCasePrefix() ?
            ClassIndex.NameKind kind = false ? Utilities.isCaseSensitive() ? ClassIndex.NameKind.CAMEL_CASE : ClassIndex.NameKind.CAMEL_CASE_INSENSITIVE : Utilities.isCaseSensitive() ? ClassIndex.NameKind.PREFIX : ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
            for (ElementHandle<TypeElement> handle : controller.getClasspathInfo().getClassIndex().getDeclaredTypes(prefix, kind, EnumSet.allOf(ClassIndex.SearchScope.class))) {
                TypeElement te = handle.resolve(controller);
                if (te != null && trees.isAccessible(scope, te) && types.isSubtype(types.getDeclaredType(te), baseType)) {
                    subtypes.add(types.getDeclaredType(te));
                }
            }
        } else {
            HashSet<TypeElement> elems = new HashSet<>();
            LinkedList<DeclaredType> bases = new LinkedList<>();
            bases.add(baseType);
            ClassIndex index = controller.getClasspathInfo().getClassIndex();
            while (!bases.isEmpty()) {
                DeclaredType head = bases.remove();
                TypeElement elem = (TypeElement) head.asElement();
                if (!elems.add(elem)) {
                    continue;
                }
                if (startsWith(elem.getSimpleName().toString(), prefix) && trees.isAccessible(scope, elem)) {
                    subtypes.add(head);
                }
                List<? extends TypeMirror> tas = head.getTypeArguments();
                boolean isRaw = !tas.iterator().hasNext();
                subtypes:
                for (ElementHandle<TypeElement> eh : index.getElements(ElementHandle.create(elem), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class))) {
                    TypeElement e = eh.resolve(controller);
                    if (e != null) {
                        if (trees.isAccessible(scope, e)) {
                            if (isRaw) {
                                DeclaredType dt = types.getDeclaredType(e);
                                bases.add(dt);
                            } else {
                                HashMap<Element, TypeMirror> map = new HashMap<>();
                                TypeMirror sup = e.getSuperclass();
                                if (sup.getKind() == TypeKind.DECLARED && ((DeclaredType) sup).asElement() == elem) {
                                    DeclaredType dt = (DeclaredType) sup;
                                    Iterator<? extends TypeMirror> ittas = tas.iterator();
                                    Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                    while (it.hasNext() && ittas.hasNext()) {
                                        TypeMirror basetm = ittas.next();
                                        TypeMirror stm = it.next();
                                        if (basetm != stm) {
                                            if (stm.getKind() == TypeKind.TYPEVAR) {
                                                map.put(((TypeVariable) stm).asElement(), basetm);
                                            } else {
                                                continue subtypes;
                                            }
                                        }
                                    }
                                    if (it.hasNext() != ittas.hasNext()) {
                                        continue subtypes;
                                    }
                                } else {
                                    for (TypeMirror tm : e.getInterfaces()) {
                                        if (((DeclaredType) tm).asElement() == elem) {
                                            DeclaredType dt = (DeclaredType) tm;
                                            Iterator<? extends TypeMirror> ittas = tas.iterator();
                                            Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                            while (it.hasNext() && ittas.hasNext()) {
                                                TypeMirror basetm = ittas.next();
                                                TypeMirror stm = it.next();
                                                if (basetm != stm) {
                                                    if (stm.getKind() == TypeKind.TYPEVAR) {
                                                        map.put(((TypeVariable) stm).asElement(), basetm);
                                                    } else {
                                                        continue subtypes;
                                                    }
                                                }
                                            }
                                            if (it.hasNext() != ittas.hasNext()) {
                                                continue subtypes;
                                            }
                                            break;
                                        }
                                    }
                                }
                                bases.add(getDeclaredType(e, map, types));
                            }
                        }
                    } else {
                        Logger.getLogger("global").log(Level.FINE, String.format("Cannot resolve: %s on bootpath: %s classpath: %s sourcepath: %s\n", eh.toString(), controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT), controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE), controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE)));
                    }
                }
            }
        }
        return subtypes;
    }

    private DeclaredType getDeclaredType(TypeElement e, HashMap<? extends Element, ? extends TypeMirror> map, Types types) {
        List<? extends TypeParameterElement> tpes = e.getTypeParameters();
        TypeMirror[] targs = new TypeMirror[tpes.size()];
        int i = 0;
        for (Iterator<? extends TypeParameterElement> it = tpes.iterator(); it.hasNext();) {
            TypeParameterElement tpe = it.next();
            TypeMirror t = map.get(tpe);
            targs[i++] = t != null ? t : tpe.asType();
        }
        Element encl = e.getEnclosingElement();
        if ((encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement) encl).getTypeParameters().isEmpty()) {
            return types.getDeclaredType(getDeclaredType((TypeElement) encl, map, types), e, targs);
        }
        return types.getDeclaredType(e, targs);
    }

    private boolean startsWith(String theString, String prefix) {
        // XXX isCamelCasePrefix
        return /*env.isCamelCasePrefix()*/ false ? Utilities.isCaseSensitive() ? Utilities.startsWithCamelCase(theString, prefix) : Utilities.startsWithCamelCase(theString, prefix) || Utilities.startsWith(theString, prefix) : Utilities.startsWith(theString, prefix);
    }

    void resolveOtherText(JavadocContext jdctx, TokenSequence<JavadocTokenId> jdts) {
        Token<JavadocTokenId> token = jdts.token();
        assert token == null || token.id() == JavadocTokenId.OTHER_TEXT;
        CharSequence text = token == null ? "" : token.text();
        int pos = token == null ? 0 : caretOffset - jdts.offset();
        DocTreePath tag = token == null ? null : getTag(jdctx, caretOffset);

        if (pos > 0 && pos <= text.length() && text.charAt(pos - 1) == '{') {
            if (tag != null && !JavadocCompletionUtils.isBlockTag(tag)) {
                int start = (int) jdctx.positions.getStartPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, tag.getLeaf());
                if (start + 1 != caretOffset) {
                    return;
                }
            }
            resolveInlineTag(null, jdctx);
            return;
        }
        if (tag != null) {
            insideTag(tag, jdctx);
            if (JavadocCompletionUtils.isBlockTag(tag) && JavadocCompletionUtils.isLineBreak(jdts, pos)) {
                resolveBlockTag(null, jdctx);
            }
        } else if (JavadocCompletionUtils.isLineBreak(jdts, pos)) {
            resolveBlockTag(null, jdctx);
        }
    }

    void insideSnippet(DocTreePath tag, JavadocContext jdctx) {
        int startPos = (int) jdctx.positions.getStartPosition(jdctx.javac.getCompilationUnit(), jdctx.comment, tag.getLeaf());
        String subStr = JavadocCompletionUtils.getCharSequence(jdctx.doc, startPos, caretOffset).toString();
        int index = subStr.lastIndexOf("\n");
        String markupLine = JavadocCompletionUtils.getCharSequence(jdctx.doc, (index + startPos), caretOffset).toString();
        insideInlineSnippet(markupLine);
    }

    private static final List<String> SNIPPET_TAGS = Collections.unmodifiableList(Arrays.asList(
            "@highlight",
            "@replace",
            "@link",
            "@start",
            "@end"
    ));

    private static final Pattern TAG_PATTERN = Pattern.compile("@\\b\\w{1,}\\b\\s+(?!.*@\\b\\w{1,}\\b\\s+)");

    void insideInlineSnippet(String subStr) {
        if (subStr.contains("//")) {
            int lastAt = subStr.lastIndexOf('@');
            if (lastAt != (-1)) {
                String suffix = subStr.substring(lastAt);
                if (!suffix.contains(" ")) {
                    for (String str : SNIPPET_TAGS) {
                        if (str.startsWith(suffix)) {
                            items.add(factory.createNameItem(str.substring(1), this.caretOffset));
                        }
                    }
                    return ;
                }
            }
            Matcher match = TAG_PATTERN.matcher(subStr);
            if (match.find()) {
                String tag = match.group(0);
                if (SNIPPET_TAGS.contains(tag.trim())) {
                    completeInlineMarkupTag(tag.trim(), new ArrayList() {
                        {
                            add("substring");
                            add("regex");
                            add("region");
                        }
                    });
                }
            }
        }
    }

    private void completeInlineMarkupTag(String str, List<String> attr) {
        String value = " = \"<value>\"";
        switch (str) {
            case "@highlight":
                attr.add("type");
                break;
            case "@replace":
                attr.add("replacement");
                break;
            case "@link":
                attr.add("target");
                attr.add("type");
                break;
            case "@start":
            case "@end":
                attr.clear();
                attr.add("region");
                break;
            default:
                break;
        }
        if (!attr.isEmpty()) {
            for (String entry : attr) {
                items.add(factory.createNameItem(entry + value, this.caretOffset));
            }
        }
    }

    private static class JavadocContext {

        private final CompilationInfo javac;
        private ElementHandle<Element> handle;
        private Element commentFor;
        private DocCommentTree comment;
        private DocSourcePositions positions;
        private TokenSequence<JavadocTokenId> jdts;
        private TokenSequence<JavaTokenId> javats;
        private Document doc;
        private ReferencesCount count;
        private TreePath javadocFor;

        private JavadocContext(CompilationInfo javac) {
            this.javac = javac;
        }

        private ReferencesCount getReferencesCount() {
            if (count == null)
                count = ReferencesCount.get(javac.getClasspathInfo());
            return count;
        }
    }
}
