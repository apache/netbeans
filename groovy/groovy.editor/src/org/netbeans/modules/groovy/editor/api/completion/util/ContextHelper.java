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

package org.netbeans.modules.groovy.editor.api.completion.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedField;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 * Utility class which provides various methods related to {@link CompletionContext}.
 *
 * @see CompletionContext
 * @author Martin Janicek
 */
public final class ContextHelper {

    protected static final Logger LOG = Logger.getLogger(ContextHelper.class.getName());

    private ContextHelper() {
    }


    /**
     * Returns all declared {@link ClassNode}'s for the given request.
     *
     * @param request completion request
     * @return list of declared {@link ClassNode}'s
     */
    public static List<ClassNode> getDeclaredClasses(CompletionContext request) {
        if (request.path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return Collections.emptyList();
        }

        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();) {
            ASTNode current = it.next();

            if (current instanceof ModuleNode) {
                return ((ModuleNode) current).getClasses();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the next enclosing {@link ClassNode} for the given request.
     *
     * @param request completion request which includes position information
     * @return the next surrounding {@link ClassNode}
     */
    public static ClassNode getSurroundingClassNode(CompletionContext request) {
        if (request.path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof ClassNode) {
                ClassNode classNode = (ClassNode) current;
                LOG.log(Level.FINEST, "Found surrounding Class: {0}", classNode.getName()); // NOI18N
                return classNode;
            }
        }
        return null;
    }

    /**
     * Returns the enclosing {@link ModuleNode} for the given request.
     *
     * @param request completion request
     * @return the surrounding {@link ModuleNode} or {@code null} if it wasn't found
     */
    public static ModuleNode getSurroundingModuleNode(CompletionContext request) {
        AstPath path = request.path;
        if (path != null) {
            for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
                ASTNode current = it.next();
                if (current instanceof ModuleNode) {
                    LOG.log(Level.FINEST, "Found ModuleNode");
                    return (ModuleNode) current;
                }
            }
        }
        return null;
    }

    /**
     * Returns the next enclosing {@link ModuleNode} for the given request.
     * 
     * @param request completion request which includes position information
     * @return the next surrounding {@link ModuleNode}
     */
    public static ASTNode getSurroundingMethodOrClosure(CompletionContext request) {
        if (request.path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        LOG.log(Level.FINEST, "getSurroundingMethodOrClosure() ----------------------------------------");
        LOG.log(Level.FINEST, "Path : {0}", request.path);

        for (Iterator<ASTNode> it = request.path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof MethodNode) {
                MethodNode mn = (MethodNode) current;
                LOG.log(Level.FINEST, "Found Method: {0}", mn.getName()); // NOI18N
                return mn;
            } else if (current instanceof FieldNode) {
                FieldNode fn = (FieldNode) current;
                if (fn.isClosureSharedVariable()) {
                    LOG.log(Level.FINEST, "Found Closure(Field): {0}", fn.getName()); // NOI18N
                    return fn;
                }
            } else if (current instanceof ClosureExpression) {
                LOG.log(Level.FINEST, "Found Closure(Expr.): {0}", ((ClosureExpression) current).getText()); // NOI18N
                return current;
            }
        }
        return null;
    }

    /**
     * Finds out if the given {@link CompletionContext} is a complete-constructor call.
     * 
     * @param request completion request
     * @return {@code true} if it's constructor call, {@code false} otherwise
     */
    public static boolean isConstructorCall(CompletionContext request) {
        if (request.getPrefix().length() > 0) {
            if (isEqualsNew(request.context.before1)) {
                return true; // new String|
            }
            if (isEqualsNew(request.context.before2)) {
                return true; // new String|("abc");
            }
        }
        return false;
    }

    private static boolean isEqualsNew(Token<GroovyTokenId> token) {
        if (token != null && token.text().toString().equals("new")) {
            return true;
        }
        return false;
    }

    /**
     * Finds out if the given {@link CompletionContext} is after {@link GroovyTokenId#COMMA}.
     *
     * @param request completion request
     * @return {@code true} if we are after comma, {@code false} otherwise
     */
    public static boolean isAfterComma(CompletionContext request) {
        if (request.context.before2.id() == GroovyTokenId.COMMA || // This is in situation like ...(aa: 0, bb^
            request.context.before1.id() == GroovyTokenId.COMMA ||
            request.context.active.id() == GroovyTokenId.COMMA) {

            return true;
        }
        return false;
    }

    /**
     * Finds out if the given {@link CompletionContext} is after {@link GroovyTokenId#LPAREN}.
     *
     * @param request completion request
     * @return {@code true} if we are after left parenthesis, {@code false} otherwise
     */
    public static boolean isAfterLeftParenthesis(CompletionContext request) {
        if (request.context.before2.id() == GroovyTokenId.LPAREN || // This is in situation like ...(aa^
            request.context.before1.id() == GroovyTokenId.LPAREN ||
            request.context.active.id() == GroovyTokenId.LPAREN) {

            return true;
        }
        return false;
    }

    /**
     * Here we test, whether the provided {@link CompletionContext} is likely to become
     * a variable definition. At this point in time we can not check whether we
     * live in a "DeclarationExpression" since this is not yet created.
     *
     * We have basically three cases:
     *
     * 1.) "def" - keyword in front, then it's a definition but we can not propose a varname
     * 2.) "int, char, long, ..." primitive type. It's a definition and we propose a single char
     * 3.) Lexer token IDENTIFIER: Then we have to decide whether it's a type or a method:
     *     For example it could be:
     *     println variable
     *     StringBuilder variable
     *
     * We have to check for:
     *
     * a) Methods
     * b) closures
     *
     * todo: figuring out whether the IDENTIFIER is a method or a type.
     * @param ctx completion request
     * @return {@code true} if we are on variable definition line, {@code false} otherwise
     */
    public static boolean isVariableNameDefinition(CompletionContext request) {
        LOG.log(Level.FINEST, "checkForVariableDefinition()"); //NOI18N
        CompletionSurrounding ctx = request.context;

        if (ctx == null || ctx.before1 == null) {
            return false;
        }

        GroovyTokenId id = ctx.before1.id();

        switch (id) {
            case LITERAL_boolean:
            case LITERAL_byte:
            case LITERAL_char:
            case LITERAL_double:
            case LITERAL_float:
            case LITERAL_int:
            case LITERAL_long:
            case LITERAL_short:
            case LITERAL_def:
                LOG.log(Level.FINEST, "LITERAL_* discovered"); //NOI18N
                return true;
            case IDENTIFIER:
                // now comes the tricky part, i have to figure out
                // whether I'm dealing with a ClassExpression here.
                // Otherwise it's a call which will or won't succeed.
                // But this could only be figured at runtime.
                ASTNode node = getASTNodeForToken(ctx.before1, request);
                LOG.log(Level.FINEST, "getASTNodeForToken(ASTNode) : {0}", node); //NOI18N

                if (node != null && (node instanceof ClassExpression || node instanceof DeclarationExpression)) {
                    LOG.log(Level.FINEST, "ClassExpression or DeclarationExpression discovered"); //NOI18N
                    return true;
                }

                return false;
            default:
                LOG.log(Level.FINEST, "default:"); //NOI18N
                return false;
        }
    }

    /**
     * Here we test, whether the provided CompletionContext is likely to become
     * a field definition.
     *
     * We have basically four cases:
     *   1) Property definition:
     *      1a) With already prefixed identifier (e.g. String st^)
     *      1b) Without any identifier yet (e.g. String ^)
     *   2) Field definition:
     *      2a) With already prefixed identifier (e.g. private String st^)
     *      2b) Without any identifier yet (e.g. private String ^)
     *      2ab) Both previous cases can have more than one modifier (e.g. private static ...)
     *
     * @param request completion request
     * @return true if it's field/property definition line, false otherwise
     */
    public static boolean isFieldNameDefinition(CompletionContext request) {
        LOG.log(Level.FINEST, "isFieldDefinitionLine()"); //NOI18N
        CompletionSurrounding ctx = request.context;

        if (ctx == null || ctx.before1 == null) {
            return false;
        }

        ASTNode node = getASTNodeForToken(ctx.before1, request);
        if (node != null) {
            // 1a)
            if (node instanceof PropertyNode) {
                return true;
            }

            // 1b) In that case getASTNodeForToken() will return ClassNode and we
            // have to check out (from CompletionContext) if it's really field
            if (node instanceof ClassNode &&
                ctx.before2 == null &&
                ctx.after1 == null &&
                ctx.after2 == null &&
                ctx.afterLiteral == null) {

                // Still might be for example 'private ^' situation in which we should return false
                if ("keyword".equals(ctx.before1.id().primaryCategory())) {
                    return false;
                }

                return true;
            }

            // 2a) + 2b)
            if (node instanceof FieldNode && ctx.before2 != null) {
                if (ctx.before1.id() == GroovyTokenId.IDENTIFIER || ctx.before2.id() == GroovyTokenId.IDENTIFIER) {
                    return true;
                }
                // 2ab) 'private static Stri^' or 'private static ^'
                return false;
            }
        }
        return false;
    }

    private static ASTNode getASTNodeForToken(Token<GroovyTokenId> tokenId, CompletionContext request) {
        LOG.log(Level.FINEST, "getASTNodeForToken()"); //NOI18N
        TokenHierarchy<Document> th = TokenHierarchy.get((Document) request.doc);
        int position = tokenId.offset(th);

        ModuleNode rootNode = ASTUtils.getRoot(request.getParserResult());
        if (rootNode == null) {
            return null;
        }
        int astOffset = ASTUtils.getAstOffset(request.getParserResult(), position);
        if (astOffset == -1) {
            return null;
        }

        BaseDocument document = (BaseDocument) request.getParserResult().getSnapshot().getSource().getDocument(false);
        if (document == null) {
            LOG.log(Level.FINEST, "Could not get BaseDocument. It's null"); //NOI18N
            return null;
        }

        final AstPath path = new AstPath(rootNode, astOffset, document);
        final ASTNode node = path.leaf();

        LOG.log(Level.FINEST, "path = {0}", path); //NOI18N
        LOG.log(Level.FINEST, "node: {0}", node); //NOI18N

        return node;
    }
    
    public static List<String> getProperties(CompletionContext context) {
        FileObject f = context.getParserResult().getSnapshot().getSource().getFileObject();
        if (f == null) {
            return Collections.<String>emptyList();
        }

        GroovyIndex index = GroovyIndex.get(QuerySupport.findRoots(f,
                Collections.singleton(ClassPath.SOURCE), Collections.<String>emptySet(), Collections.<String>emptySet()));

        List<String> result = new ArrayList<String>();
        
        for (IndexedField indexedField : index.getAllFields(context.getTypeName())) {
            if (!indexedField.isStatic() && indexedField.isProperty()) {
                result.add(indexedField.getName());
            }
        }

        return result;
    }
}
