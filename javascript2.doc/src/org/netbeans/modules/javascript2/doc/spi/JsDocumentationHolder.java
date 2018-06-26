/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.doc.spi;

import com.oracle.js.parser.ir.Node;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.DocumentationUtils;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationPrinter;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Holds processed JavaScript comments and serves all data to the JS model.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class JsDocumentationHolder {

    private final Snapshot snapshot;

    private final JsDocumentationProvider provider;

    private Map<String, List<OffsetRange>> occurencesMap = null;

    public JsDocumentationHolder(JsDocumentationProvider provider, Snapshot snapshot) {
        this.provider = provider;
        this.snapshot = snapshot;
    }

    public abstract Map<Integer, ? extends JsComment> getCommentBlocks();


    public final Map<String, List<OffsetRange>> getOccurencesMap() {
        if (occurencesMap == null) {
            occurencesMap = new HashMap<String, List<OffsetRange>>();
            for (Map.Entry<Integer, ? extends JsComment> entry : getCommentBlocks().entrySet()) {
                JsComment comment = entry.getValue();
                for (DocParameter docParameter : comment.getParameters()) {
                    for (Type type : docParameter.getParamTypes()) {
                        insertIntoOccurencesMap(type);
                    }
                }
                DocParameter returnType = comment.getReturnType();
                if (returnType != null) {
                    for (Type type : returnType.getParamTypes()) {
                        insertIntoOccurencesMap(type);
                    }
                }
                DocParameter definedType = comment.getDefinedType();
                if (definedType != null) {
                    for (Type type : definedType.getParamTypes()) {
                        insertIntoOccurencesMap(type);
                    }
                }
                List<Type> types = comment.getTypes();
                if (types != null) {
                    for (Type type : types) {
                        insertIntoOccurencesMap(type);
                    }
                }
                
                List<DocParameter> properties = comment.getProperties();
                if (properties != null) {
                    for (DocParameter property : properties) {
                        for (Type type : property.getParamTypes()) {
                            insertIntoOccurencesMap(type);
                        }
                    }
                }
            }
        }
        return occurencesMap;
    }

    private void insertIntoOccurencesMap(Type type) {
        if (type.getType().trim().isEmpty()) {
            return;
        }
        String[] typeParts = type.getType().split("\\.");
        if (typeParts.length > 1) {
            StringBuilder sb = new StringBuilder();
            int offsetDelta = 0; 
            int typeOffset = DocumentationUtils.getOffsetRange(type).getStart();
            for (int i = 0; i < typeParts.length; i++) {
                sb.append(typeParts[i]);
                String name = sb.toString();
                if (!occurencesMap.containsKey(name)) {
                    occurencesMap.put(name, new LinkedList<OffsetRange>());
                }
                occurencesMap.get(name).add(new OffsetRange(typeOffset + offsetDelta, typeOffset + offsetDelta + typeParts[i].length()));
                offsetDelta += name.length();
                sb.append('.');
                offsetDelta++;
            }
        } else {
            if (!occurencesMap.containsKey(type.getType())) {
                occurencesMap.put(type.getType(), new LinkedList<OffsetRange>());
            }
            occurencesMap.get(type.getType()).add(DocumentationUtils.getOffsetRange(type));
            }
    }

    /**
     * Gets the {@link JsDocumentationProvider} which creates this holder.
     * @return JsDocumentationProvider
     */
    public JsDocumentationProvider getProvider() {
        return provider;
    }

    /**
     * Gets possible return types get for the node.
     * @param node of the javaScript code
     * @return list of potential return types, never {@code null}
     */
    public List<Type> getReturnType(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null && comment.getReturnType() != null) {
            return comment.getReturnType().getParamTypes();
        }
        return Collections.<Type>emptyList();
    }

    /**
     * Gets parameters of the method.
     * @param node of the javaScript code
     * @return list of parameters, never {@code null}
     */
    public List<DocParameter> getParameters(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.getParameters();
        }
        return Collections.<DocParameter>emptyList();
    }
    
    /**
     * Gets properties of the object from comment.
     * @param node of the javaScript code
     * @return list of properties defined in the comment, never {@code null}
     */
    public List<DocParameter> getProperties(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.getProperties();
        }
        return Collections.<DocParameter>emptyList();
    }

    /**
     * Gets documentation for given Node.
     * @param node of the javaScript code
     * @return documentation text if any {@code null} otherwise
     */
    public Documentation getDocumentation(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            String content = JsDocumentationPrinter.printDocumentation(comment);
            if (!content.isEmpty()) {
                return Documentation.create(content);
            }
        }
        return null;
    }

    /**
     * Says whether is code at given code depricated or not.
     * @param node examined node
     * @return {@code true} if the comment says "it's deprecated", {@code false} otherwise
     */
    public boolean isDeprecated(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.getDeprecated() != null;
        }
        return false;
    }

    /**
     * Says whether is examined node (probably function node) class, constructor or not.
     * @param node examined node
     * @return {@code true} if the comment says "it's a class", {@code false} otherwise
     */
    public boolean isClass(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.isClass();
        }
        return false;
    }
    
    /**
     * Says whether is examined node constant.
     * @param node examined node
     * @return {@code true} if the comment says "it's a constant", {@code false} otherwise
     */
    public boolean isConstant(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.isConstant();
        }
        return false;
    }

    /**
     * Gets list of super classes defined by documentation for the given node.
     * @param node examined node
     * @return {@code List} of super classes, never {@code null}
     */
    public List<Type> getExtends(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.getExtends();
        }
        return Collections.<Type>emptyList();
    }

    /**
     * Gets the set of modifiers attached to given node.
     * @param node examined node
     * @return {@code Set} of modifiers, never {@code null}
     */
    public Set<JsModifier> getModifiers(Node node) {
        JsComment comment = getCommentForOffset(node.getStart(), getCommentBlocks());
        if (comment != null) {
            return comment.getModifiers();
        }
        return Collections.<JsModifier>emptySet();
    }

    /**
     * Answers whether given token is whitespace or not.
     * @param token examined token
     * @return {@code true} if the token is whitespace, {@code false} otherwise
     */
    public boolean isWhitespaceToken(Token<? extends JsTokenId> token) {
        return token.id() == JsTokenId.EOL || token.id() == JsTokenId.WHITESPACE
                || token.id() == JsTokenId.BLOCK_COMMENT || token.id() == JsTokenId.DOC_COMMENT
                || token.id() == JsTokenId.LINE_COMMENT;
    }

    /**
     * Gets the closest documentation comment block to the given offset if any.
     * @param offset where to start searching for the doc comment block
     * @return documentation block
     */
    public JsComment getCommentForOffset(int offset, Map<Integer, ? extends JsComment> comments) {
        int endOffset = getEndOffsetOfAssociatedComment(offset);
        if (endOffset > 0) {
            return comments.get(endOffset);
        }
        return null;
    }

    /**
     * 
     * @return snapshot
     */
    public Snapshot getSnapshot() {
        return this.snapshot;
    }
    
    @SuppressWarnings("empty-statement")
    private int getEndOffsetOfAssociatedComment(int offset) {
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(tokenHierarchy, offset);
        if (ts != null) {
            ts.move(offset);
            ts.moveNext();

            // get to first EOL
            while (ts.movePrevious()
                    && ts.token().id() != JsTokenId.DOC_COMMENT
                    && ts.token().id() != JsTokenId.BLOCK_COMMENT
                    && ts.token().id() != JsTokenId.BRACKET_RIGHT_CURLY
                    && ts.token().id() != JsTokenId.BRACKET_LEFT_CURLY
                    && ts.token().id() != JsTokenId.OPERATOR_SEMICOLON) {
                // do nothing - just search for interesting tokens
            }

            if (ts.token() != null && ts.token().id() == JsTokenId.DOC_COMMENT) {
                return ts.token().offset(tokenHierarchy) + ts.token().length();
            }

            // search for DOC_COMMENT
            while (ts.movePrevious()) {
                if (ts.token().id() == JsTokenId.DOC_COMMENT) {
                    return ts.token().offset(tokenHierarchy) + ts.token().length();
                } else if (isWhitespaceToken(ts.token())) {
                    continue;
                } else {
                    return -1;
                }
            }
        }

        return -1;
    }
}
