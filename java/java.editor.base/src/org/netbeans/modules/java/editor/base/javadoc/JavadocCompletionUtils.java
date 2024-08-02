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

package org.netbeans.modules.java.editor.base.javadoc;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 * XXX try to merge with hints.JavadocUtilities
 * 
 * @author Jan Pokorsky
 */
public final class JavadocCompletionUtils {
    
    static final Pattern JAVADOC_LINE_BREAK = Pattern.compile("(\\n[ \\t]*\\**[ \\t]*\\z)|(\\n[ \\t]*///[ \\t]*\\z)"); // NOI18N
    static final Pattern JAVADOC_WHITE_SPACE = Pattern.compile("[^ \\t]"); // NOI18N
    /**
     * javadoc parser considers whatever number of spaces or standalone newline
     * or whatever number of trailing asterisks as empty javadoc.
     * <p>See {@link JavadocCompletionUtilsTest#testIsInvalidDocInstance} for
     * test cases
     */
    static final Pattern JAVADOC_EMPTY = Pattern.compile("(\\s*\\**\\s*\n)*\\s*\\**\\s*\\**"); // NOI18N
    static final Pattern JAVADOC_FIRST_WHITE_SPACE = Pattern.compile("[ \\t]*\\**[ \\t]*"); // NOI18N
    private static Set<JavaTokenId> IGNORE_TOKES = EnumSet.of(
            JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT_LINE_RUN);
    private static final Logger LOGGER = Logger.getLogger(JavadocCompletionUtils.class.getName());
    
    /**
     * Checks if the offset is part of some javadoc block. The javadoc content
     * is considered as everything between <code>/**</code>
     * and <code>&#42;/</code> except
     * indentation prefixes <code>'&#32;&#32;&#32;&#32;*'</code>
     * on each line.
     * <p>Note: the method takes a document lock.</p>
     * 
     * @param doc a document to search
     * @param offset an offset in document
     * @return <code>true</code> if the offset refers to a javadoc content
     */
    public static boolean isJavadocContext(final Document doc, final int offset) {
        final boolean[] result = {false};
        doc.render(new Runnable() {

            public void run() {
                result[0] = isJavadocContext(TokenHierarchy.get(doc), offset);
            }
        });
        return result[0];
    }
    
    public static boolean isJavadocContext(TokenHierarchy hierarchy, int offset) {
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(hierarchy, offset);
        if (!movedToJavadocToken(ts, offset)) {
            return false;
        }
        
        TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
        if (jdts == null) {
            return false;
        } else if (jdts.isEmpty()) {
            return isEmptyJavadoc(ts.token(), offset - ts.offset());
        }
        
        jdts.move(offset);
        if (!jdts.moveNext() && !jdts.movePrevious()) {
            return false;
        }
        
        // this checks /** and */ headers
        return isInsideToken(jdts, offset) && !isInsideIndent(jdts.token(), offset - jdts.offset());
    }
    
    public static TreePath findJavadoc(CompilationInfo javac, int offset) {
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(javac.getTokenHierarchy(), offset);
        if (ts == null || !movedToJavadocToken(ts, offset)) {
            return null;
        }

        int offsetBehindJavadoc = ts.offset() + ts.token().length();

        while (ts.moveNext()) {
            TokenId tid = ts.token().id();
            if (tid == JavaTokenId.BLOCK_COMMENT) {
                if ("/**/".contentEquals(ts.token().text())) { // NOI18N
                    // see #147533
                    return null;
                }
            } else if (tid == JavaTokenId.JAVADOC_COMMENT) {
                if (ts.token().partType() == PartType.COMPLETE) {
                    return null;
                }
            } else if (!IGNORE_TOKES.contains(tid)) {
                offsetBehindJavadoc = ts.offset();
                // it is magic for TreeUtilities.pathFor
                ++offsetBehindJavadoc;
                break;
            }
        }

        TreePath tp = javac.getTreeUtilities().pathFor(offsetBehindJavadoc);
        
        while (!TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()) && tp.getLeaf().getKind() != Kind.METHOD && tp.getLeaf().getKind() != Kind.VARIABLE && tp.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            tp = tp.getParentPath();
            if (tp == null) {
                break;
            }
        }
        
        return tp;
    }
    
    public static TokenSequence<JavadocTokenId> findJavadocTokenSequence(CompilationInfo javac, int offset) {
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(javac.getTokenHierarchy(), offset);
        if (ts == null || !movedToJavadocToken(ts, offset)) {
            return null;
        }
        
        TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
        if (jdts == null) {
            return null;
        }
        
        jdts.move(offset);
        return jdts;
    }
    
    /**
     * Finds javadoc token sequence.
     * @param javac compilation info
     * @param e element for which the tokens are queried
     * @return javadoc token sequence or null.
     */
    @SuppressWarnings("fallthrough")
    public static TokenSequence<JavadocTokenId> findJavadocTokenSequence(CompilationInfo javac, Tree tree, Element e) {
        if (e == null || javac.getElementUtilities().isSynthetic(e))
            return null;

        if (tree == null)
            tree = javac.getTrees().getTree(e);
        if (tree == null)
            return null;

        int elementStartOffset = (int) javac.getTrees().getSourcePositions().getStartPosition(javac.getCompilationUnit(), tree);
        TokenSequence<JavaTokenId> s = SourceUtils.getJavaTokenSequence(javac.getTokenHierarchy(), elementStartOffset);
        if (s == null) {
            return null;
        }
        s.move(elementStartOffset);
        Token<JavaTokenId> token = null;
        while (s.movePrevious()) {
            token = s.token();
            switch (token.id()) {
                case BLOCK_COMMENT:
                    // see #147533
                    if (!"/**/".contentEquals(token.text())) { // NOI18N
                        break;
                    }
                case JAVADOC_COMMENT:
                case JAVADOC_COMMENT_LINE_RUN:
                    if (token.partType() == PartType.COMPLETE) {
                        return javac.getElements().getDocComment(e) == null
                                ? null : s.embedded(JavadocTokenId.language());
                    }
                    break;
                case WHITESPACE:
                case LINE_COMMENT:
                    break;
                default:
                    return null;
            }
        }
        return null;
    }

    static boolean isInsideIndent(Token<JavadocTokenId> token, int offset) {
        int indent = -1;
        if (token.id() == JavadocTokenId.OTHER_TEXT) {
            CharSequence text = token.text();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    if (i <= offset) {
                        // new line; reset status
                        indent = -1;
                        if (i < offset) {
                            continue;
                        }
                    }
                    // stop, line inspection is ready
                    break;
                } else if (i == 0) {
                    // token must start with \n otherwise it is not indentation
                    break;
                }

                if (c == '*' && indent < 0) {
                    indent = i;
                    if (offset <= i) {
                        // stop, offset is inside indentation
                        break;
                    }
                }
            }
        }
        return indent >= offset;
    }
    
    /**
     * Is javadoc line break?
     * @param ts a token sequence positioned to the token to test
     * @return {@code true} in case the token is something like {@code "\n\t*"}
     */
    public static boolean isLineBreak(TokenSequence<JavadocTokenId> ts) {
        return isLineBreak(ts, ts.token().length());
    }
    
    /**
     * Tests if the token part before {@code pos} is a javadoc line break.
     * @param ts a token sequence positioned to the token to test
     * @param pos position in the token
     * @return {@code true} in case the token is something like {@code "\n\t* |\n\t*"}
     */
    public static boolean isLineBreak(TokenSequence<JavadocTokenId> ts, int pos) {
        Token<JavadocTokenId> token = ts.token();

        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return ts.isEmpty() || ts.index() == 0;
        }
        try {
            CharSequence text = token.text();
            if (pos < token.length())
                text = text.subSequence(0, pos);
            boolean result = (pos > 0
                    && JAVADOC_LINE_BREAK.matcher(text).find()
                    && (pos == token.length() || !isInsideIndent(token, pos))
                    );
            return result;
        } catch (IndexOutOfBoundsException e) {
            throw (IndexOutOfBoundsException) new IndexOutOfBoundsException("pos: " + pos + ", token.length: " + token.length() + ", token text: " + token.text()).initCause(e);
        }
    }

    public static boolean isWhiteSpace(CharSequence text) {
        return text != null && text.length() > 0 && !JAVADOC_WHITE_SPACE.matcher(text).find();
    }
    
    public static boolean isWhiteSpace(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }

        CharSequence text = token.text();
        boolean result = !JAVADOC_WHITE_SPACE.matcher(text).find();
        return result;
    }
    
    /**
     * enhanced {@link #isWhiteSpace(org.netbeans.api.lexer.Token) isWhiteSpace}
     * @param token "\t" or "\t**\t"
     * @return same value as isWhiteSpace
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=131826">#131826</a>
     */
    public static boolean isFirstWhiteSpaceAtFirstLine(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }

        CharSequence text = token.text();
        boolean result = JAVADOC_FIRST_WHITE_SPACE.matcher(text).matches();
        return result;
    }
    
    public static boolean isWhiteSpaceFirst(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT || token.length() < 1) {
            return false;
        }

        CharSequence text = token.text();
        char c = text.charAt(0);
        return c == ' ' || c == '\t';
    }
    
    public static boolean isWhiteSpaceLast(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT || token.length() < 1) {
            return false;
        }

        CharSequence text = token.text();
        char c = text.charAt(text.length() - 1);
        return c == ' ' || c == '\t';
    }
    
    public static boolean isInlineTagStart(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }

        CharSequence text = token.text();
        boolean result = text.charAt(text.length() - 1) == '{';
        return result;
    }
    
    private static final Set<DocTree.Kind> BLOCK_TAGS =
            EnumSet.of(DocTree.Kind.AUTHOR, DocTree.Kind.DEPRECATED, DocTree.Kind.EXCEPTION,
                       DocTree.Kind.HIDDEN,DocTree.Kind.PARAM, DocTree.Kind.PROVIDES,
                       DocTree.Kind.RETURN, DocTree.Kind.SEE, DocTree.Kind.SERIAL,
                       DocTree.Kind.SERIAL_DATA, DocTree.Kind.SERIAL_FIELD,
                       DocTree.Kind.SINCE, DocTree.Kind.THROWS, DocTree.Kind.USES, DocTree.Kind.VERSION,
                       DocTree.Kind.UNKNOWN_BLOCK_TAG);
    public static boolean isBlockTag(DocTreePath tag) {
        return BLOCK_TAGS.contains(normalizedKind(tag.getLeaf()));
    }

    private static final Set<DocTree.Kind> INLINE_TAGS =
            EnumSet.of(DocTree.Kind.CODE, DocTree.Kind.DOC_ROOT, DocTree.Kind.INDEX,
                       DocTree.Kind.INHERIT_DOC, DocTree.Kind.LINK, DocTree.Kind.LINK_PLAIN,
                       DocTree.Kind.LITERAL, DocTree.Kind.SNIPPET, DocTree.Kind.SUMMARY,
                       DocTree.Kind.SYSTEM_PROPERTY, DocTree.Kind.VALUE, DocTree.Kind.UNKNOWN_INLINE_TAG);
    public static boolean isInlineTag(DocTreePath tag) {
        return INLINE_TAGS.contains(normalizedKind(tag.getLeaf()));
    }

    public static DocTree.Kind normalizedKind(DocTree tag) {
        DocTree.Kind normalizedKind = tag.getKind();
        if (normalizedKind == com.sun.source.doctree.DocTree.Kind.ERRONEOUS) {
            String txt = ((ErroneousTree) tag).getBody().split("\\s")[0];
            switch (txt) {
                case "@author": normalizedKind = DocTree.Kind.AUTHOR; break;
                case "@deprecated": normalizedKind = DocTree.Kind.DEPRECATED; break;
                case "@exception": normalizedKind = DocTree.Kind.EXCEPTION; break;
                case "@hidden": normalizedKind = DocTree.Kind.HIDDEN; break;
                case "@param": normalizedKind = DocTree.Kind.PARAM; break;
                case "@provides": normalizedKind = DocTree.Kind.PROVIDES; break;
                case "@return": normalizedKind = DocTree.Kind.RETURN; break;
                case "@see": normalizedKind = DocTree.Kind.SEE; break;
                case "@serial": normalizedKind = DocTree.Kind.SERIAL; break;
                case "@serialData": normalizedKind = DocTree.Kind.SERIAL_DATA; break;
                case "@serialField": normalizedKind = DocTree.Kind.SERIAL_FIELD; break;
                case "@since": normalizedKind = DocTree.Kind.SINCE; break;
                case "@throws": normalizedKind = DocTree.Kind.THROWS; break;
                case "@uses": normalizedKind = DocTree.Kind.USES; break;
                case "@version": normalizedKind = DocTree.Kind.VERSION; break;
                case "{@code": normalizedKind = DocTree.Kind.CODE; break;
                case "{@docRoot": normalizedKind = DocTree.Kind.DOC_ROOT; break;
                case "{@index": normalizedKind = DocTree.Kind.INDEX; break;
                case "{@inheritDoc": normalizedKind = DocTree.Kind.INHERIT_DOC; break;
                case "{@link": normalizedKind = DocTree.Kind.LINK; break;
                case "{@linkplain": normalizedKind = DocTree.Kind.LINK; break;
                case "{@literal": normalizedKind = DocTree.Kind.LITERAL; break;
                case "{@snippet": normalizedKind = DocTree.Kind.SNIPPET; break;
                case "{@summary": normalizedKind = DocTree.Kind.SUMMARY; break;
                case "{@systemProperty": normalizedKind = DocTree.Kind.SYSTEM_PROPERTY; break;
                case "{@value": normalizedKind = DocTree.Kind.VALUE; break;
                default:
                    if (txt.startsWith("@")) {
                        normalizedKind = DocTree.Kind.UNKNOWN_BLOCK_TAG;
                    } else if (txt.startsWith("{@")) {
                        normalizedKind = DocTree.Kind.UNKNOWN_INLINE_TAG;
                    }
                    break;
            }
        }
        return normalizedKind;
    }

    public static CharSequence getCharSequence(Document doc) {
        CharSequence cs = (CharSequence) doc.getProperty(CharSequence.class);
        if (cs == null) {
            try {
                cs = doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                // throw the same exception as CharSequence.subSequence
                throw (IndexOutOfBoundsException) new IndexOutOfBoundsException().initCause(ex);
            }
        }
        return cs;
    }
    
    public static CharSequence getCharSequence(Document doc, int begin, int end) {
        CharSequence cs = (CharSequence) doc.getProperty(CharSequence.class);
        if (cs != null) {
            cs = cs.subSequence(begin, end);
        } else {
            try {
                cs = doc.getText(begin, end - begin);
            } catch (BadLocationException ex) {
                // throw the same exception as CharSequence.subSequence
                throw (IndexOutOfBoundsException) new IndexOutOfBoundsException().initCause(ex);
            }
        }
        return cs;
    }
    
    private static boolean isInsideToken(TokenSequence<?> ts, int offset) {
        return offset >= ts.offset() && offset <= ts.offset() + ts.token().length();
    }
    
    /** enhanced moveNext & movePrevious */
    private static boolean movedToJavadocToken(TokenSequence<JavaTokenId> ts, int offset) {
        if (ts == null || !ts.moveNext() && !ts.movePrevious()) {
            return false;
        }
        
        if (ts.token().id() != JavaTokenId.JAVADOC_COMMENT &&
            ts.token().id() != JavaTokenId.JAVADOC_COMMENT_LINE_RUN) {
            return false;
        }
        
        return isInsideToken(ts, offset);
    }
    
    /**
     * Checks special case of empty javadoc <code>/**|&#42;/</code>. 
     * @param token javadoc token
     * @param offset offset <B>INSIDE</B> jvadoc token
     * @return <code>true</code> in case of empty javadoc and the proper position
     */
    private static boolean isEmptyJavadoc(Token<JavaTokenId> token, int offset) {
        if (token != null && token.id() == JavaTokenId.JAVADOC_COMMENT) {
            CharSequence text = token.text();
            // check special case /**|*/
            return offset == 3 && "/***/".contentEquals(text); //NOI18N
        }
        if (token != null && token.id() == JavaTokenId.JAVADOC_COMMENT_LINE_RUN) {
            CharSequence text = token.text();
            // check special case ///|\n
            return offset == 3 && "///\n".contentEquals(text); //NOI18N
        }
        return false;
    }

    /**
     * Checks whether Doc instance matches to its token sequence representation.
     * @param javadoc Doc instance of javadoc
     * @param ts javadoc token sequence
     * @return true if it is valid javadoc
     * 
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=139147">139147</a>
     */
    public static boolean isInvalidDocInstance(DocCommentTree javadoc, TokenSequence<JavadocTokenId> ts) {
        if (javadoc == null || javadoc.getFullBody().isEmpty()) {
            if (!ts.isEmpty()) {
                ts.moveStart();
                return !(ts.moveNext() && isTokenOfEmptyJavadoc(ts.token()) && ts.moveNext() == false);
            }
        }
        return false;
    }

    static boolean isTokenOfEmptyJavadoc(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }
        return JAVADOC_EMPTY.matcher(token.text()).matches();
    }
}