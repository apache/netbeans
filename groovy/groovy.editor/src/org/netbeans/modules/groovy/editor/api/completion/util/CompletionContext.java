/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.groovy.editor.api.completion.util;

import java.util.Iterator;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.completion.inference.GroovyTypeAnalyzer;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId.LITERAL_new;
import static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId.LPAREN;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.completion.AccessLevel;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 * @author Martin Janicek
 */
public final class CompletionContext {

    private final ParserResult parserResult;
    private final FileObject sourceFile;
    
    private String typeName;
    private String prefix;
    private int anchor;
    private boolean nameOnly;
    
    public final int lexOffset;
    public final int astOffset;
    public final BaseDocument doc;
    private boolean staticMembers;
    
    public boolean scriptMode;
    public CaretLocation location;
    public CompletionSurrounding context;
    public AstPath path;
    public ClassNode rawDseclaringClass;
    public ClassNode declaringClass;
    public DotCompletionContext dotContext;
    public Set<AccessLevel> access;
    private int addSortOverride;

    public CompletionContext(
            ParserResult parseResult,
            String prefix,
            int anchor,
            int lexOffset,
            int astOffset,
            BaseDocument doc) {

        this.parserResult = parseResult;
        this.sourceFile = parseResult.getSnapshot().getSource().getFileObject();
        this.prefix = prefix;
        this.anchor = anchor;
        this.lexOffset = lexOffset;
        this.astOffset = astOffset;
        this.doc = doc;
        
        this.path = getPathFromRequest();
        this.location = getCaretLocationFromRequest();

        // now let's figure whether we are in some sort of definition line
        this.context = getCompletionContext();

        // Are we invoked right behind a dot? This is information is used later on in
        // a couple of completions.
        this.dotContext = getDotCompletionContext();
        this.nameOnly = dotContext != null && dotContext.isMethodsOnly();

        ClassNode dc = getBeforeDotDeclaringClass();
        this.rawDseclaringClass = dc;
        this.declaringClass = dc == null ? null : dc.redirect();
    }

    /**
     * @return true, if just static members or meta-members should be considered.
     * @since 1.80
     */
    public boolean isStaticMembers() {
        return staticMembers;
    }

    /**
     * If nonzero, the provider should adjust CompletionItems priority by this amount
     * to sort the items lower/higher than usual.
     * @return adjustment for sort priority.
     * @since 1.80
     */
    public int getAddSortOverride() {
        return addSortOverride;
    }
    
    /**
     * Set an offset to the default sort priority for the provided items.
     * The provider should increase / decrease sort priority of the items
     * @param addSortOverride the delta.
     * @since 1.80
     */
    public void setAddSortOverride(int addSortOverride) {
        this.addSortOverride = addSortOverride;
    }
    
    // TODO: Move this to the constructor and change ContextHelper.getSurroundingClassNode()
    // to prevent NPE caused by leaking this in constructor
    public void init() {
        setDeclaringClass(rawDseclaringClass, this.staticMembers);
    }

    public void setDeclaringClass(ClassNode declaringClass, boolean staticMembers) {
        this.rawDseclaringClass = declaringClass;
        if (declaringClass != null) {
            this.declaringClass = declaringClass.redirect();
            this.access = AccessLevel.create(ContextHelper.getSurroundingClassNode(this), declaringClass);
            this.staticMembers = staticMembers;
        } else {
            this.declaringClass = null;
            this.access = null;
        }
    }

    public ParserResult getParserResult() {
        return parserResult;
    }
    
    public FileObject getSourceFile() {
        return sourceFile;
    }

    public ClassNode getSurroundingClass() {
        return ContextHelper.getSurroundingClassNode(this);
    }

    public String getTypeName() {
        return typeName;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getAnchor() {
        return anchor;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setAnchor(int anchor) {
        this.anchor = anchor;
    }
    
    public boolean isBehindDot() {
        return dotContext != null;
    }

    public boolean isNameOnly() {
        return nameOnly;
    }

    /**
     * Calculate an AstPath from a given request or null if we can not get a
     * AST root-node from the request.
     *
     * @return a freshly created AstPath object for the offset given in the request
     */
    private AstPath getPathFromRequest() {
        ASTNode root = ASTUtils.getRoot(parserResult);

        // in some cases we can not repair the code, therefore root == null
        // therefore we can not complete. See # 131317
        if (root == null) {
            return null;
        }

        return new AstPath(root, astOffset, doc);
    }

    private AstPath getPath(ParserResult parseResult, BaseDocument doc, int astOffset) {
        return getPath(parseResult, doc, astOffset, false);
    }
    
    private AstPath getPath(ParserResult parseResult, BaseDocument doc, int astOffset, boolean outermost) {
        ASTNode root = ASTUtils.getRoot(parseResult);

        // in some cases we can not repair the code, therefore root == null
        // therefore we can not complete. See # 131317
        if (root == null) {
            return null;
        }
        return new AstPath(root, astOffset, doc, outermost);
    }
    
    /**
     * Figure-out, where we are in the code (comment, CU, class, method, etc.).
     *
     * @return concrete caret location type
     */
    private CaretLocation getCaretLocationFromRequest() {
        int position = lexOffset;
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, position);

        // are we living inside a comment?

        ts.move(position);

        if (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
            Token<GroovyTokenId> t = ts.token();

            if (t.id() == GroovyTokenId.LINE_COMMENT || t.id() == GroovyTokenId.BLOCK_COMMENT) {
                return CaretLocation.INSIDE_COMMENT;
            }

            if (t.id() == GroovyTokenId.STRING_LITERAL) {
                return CaretLocation.INSIDE_STRING;
            }
            // This is a special case. If we have a NLS right behind a LINE_COMMENT it
            // should be treated as a CaretLocation.INSIDE_COMMENT. Therefore we have to rewind.

            if (t.id() == GroovyTokenId.NLS) {
                if ((ts.isValid() && ts.movePrevious() && ts.offset() >= 0)) {
                    Token<GroovyTokenId> tparent = ts.token();
                    if (tparent.id() == GroovyTokenId.LINE_COMMENT) {
                        return CaretLocation.INSIDE_COMMENT;
                    }
                }
            }
        }


        // Are we above the package statement?
        // We try to figure this out by moving down the lexer Stream

        ts.move(position);

        while (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
            Token<GroovyTokenId> t = ts.token();

            if (t.id() == GroovyTokenId.LITERAL_package) {
                return CaretLocation.ABOVE_PACKAGE;
            } else if (t.id() == GroovyTokenId.LITERAL_class || t.id() == GroovyTokenId.LITERAL_def
                    || t.id() == GroovyTokenId.LPAREN || t.id() == GroovyTokenId.LBRACE) {
                break;
            }
        }

        // Are we before the first class or interface statement?
        // now were heading to the beginning to the document ...

        boolean classDefBeforePosition = false;
        boolean openBraceBeforePosition = false;
        // is there package statement?
        boolean afterPackagePosition = false;
        boolean canBeImport = true;
        
        ts.move(position);

        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<GroovyTokenId> t = ts.token();
            if (t.id() == GroovyTokenId.LBRACE) {
                openBraceBeforePosition = true;
                canBeImport = false;
            } else if (t.id() == GroovyTokenId.LITERAL_class || t.id() == GroovyTokenId.LITERAL_interface || t.id() == GroovyTokenId.LITERAL_trait) {
                classDefBeforePosition = true;
                break;
            } else if (t.id() == GroovyTokenId.LITERAL_package) {
                afterPackagePosition = true;
                break;
            } else if (canBeImport && t.id() == GroovyTokenId.LITERAL_import) {
                return CaretLocation.INSIDE_IMPORT;
            }
            
            if (canBeImport && !(t.id() == GroovyTokenId.DOT || t.id() == GroovyTokenId.IDENTIFIER
                    || t.id() == GroovyTokenId.WHITESPACE || t.id() == GroovyTokenId.LITERAL_static)) {
                canBeImport = false;
            }
        }

        if (afterPackagePosition) {
            // are we in the package statement?
            ts.move(position);  // back on the caret position
            boolean isPackageStatement = true;
            boolean blockComment = false;
            int countOfWhitespaces = 0;
            while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0 && isPackageStatement) {
                Token<GroovyTokenId> t = ts.token();
                if (t.id() == GroovyTokenId.LITERAL_package) {
                    break;
                } else if (t.id() == GroovyTokenId.WHITESPACE) {
                    countOfWhitespaces++;
                } else if (t.id() == GroovyTokenId.BLOCK_COMMENT) {
                    // cases: 
                    //      package /* comment */ org.apache.something
                    //      package /* comment */org.apache.something
                    //      package/* comment */org.apache.something
                    blockComment = true;
                } else {
                    isPackageStatement = (t.id() == GroovyTokenId.DOT || t.id() == GroovyTokenId.IDENTIFIER);
                }
            }
            if (isPackageStatement && (countOfWhitespaces == 1 || (blockComment && countOfWhitespaces < 3))) {
                // we are just behind the package keyword
                return CaretLocation.INSIDE_PACKAGE;
            }
        } 

        boolean classDefAfterPosition = false;

        ts.move(position);

        while (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
            Token<GroovyTokenId> t = ts.token();
            if (t.id() == GroovyTokenId.LITERAL_class || t.id() == GroovyTokenId.LITERAL_interface || t.id() == GroovyTokenId.LITERAL_trait) {
                classDefAfterPosition = true;
                break;
            }
        }

         if (path != null) {
            ASTNode node = path.root();
            if (node instanceof ModuleNode) {
                ModuleNode module = (ModuleNode) node;
                String name = null;
                for (ClassNode clazz : module.getClasses()) {
                    if (clazz.isScript()) {
                        name = clazz.getName();
                        scriptMode = true;
                        break;
                    }
                }

                // we have a script class - lets see if there is another
                // non-script class with same name that would mean we are just
                // broken class, not a script
                if (name != null) {
                    for (ClassNode clazz : module.getClasses()) {
                        if (!clazz.isScript() && name.equals(clazz.getName())) {
                            scriptMode = false;
                            break;
                        }
                    }
                }
            }
        }

        if (classDefBeforePosition && !openBraceBeforePosition) {
            return null;
        }

        if (!scriptMode && !classDefBeforePosition && classDefAfterPosition) {
            return CaretLocation.ABOVE_FIRST_CLASS;
        }

        // If there's *no* class definition in the file we are running in a
        // script with synthetic wrapper class and wrapper method: run().
        // See GINA, ch. 7

        if (!classDefBeforePosition && scriptMode) {
            return CaretLocation.INSIDE_METHOD;
        }


        if (path == null) {
            return null;
        }



        /* here we loop from the tail of the path (innermost element)
        up to the root to figure out where we are. Some of the trails are:

        In main method:
        Path(4)=[ModuleNode:ClassNode:MethodNode:ConstantExpression:]

        In closure, which sits in a method:
        Path(7)=[ModuleNode:ClassNode:MethodNode:DeclarationExpression:DeclarationExpression:VariableExpression:ClosureExpression:]

        In closure directly attached to class:
        Path(4)=[ModuleNode:ClassNode:PropertyNode:FieldNode:]

        In a class, outside method, right behind field declaration:
        Path(4)=[ModuleNode:ClassNode:PropertyNode:FieldNode:]

        Right after a class declaration:
        Path(2)=[ModuleNode:ClassNode:]

        Inbetween two classes:
        [ModuleNode:ConstantExpression:]

        Outside of any class:
        Path(1)=[ModuleNode:]

        Start of Parameter-list:
        Path(4)=[ModuleNode:ClassNode:MethodNode:Parameter:]

         */

        boolean insideBlock = false;  // we need to distinquish, whether are we inside 
                                      // a method or in method declaration part
        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof ClosureExpression) {
                return CaretLocation.INSIDE_CLOSURE;
            } else if (current instanceof FieldNode) {
                FieldNode fn = (FieldNode) current;
                if (fn.isClosureSharedVariable()) {
                    return CaretLocation.INSIDE_CLOSURE;
                }
            } else if (current instanceof MethodNode) {
                if (insideBlock) {
                    return CaretLocation.INSIDE_METHOD;
                }
            } else if (current instanceof BlockStatement) {
                insideBlock = true;
            } else if (current instanceof ClassNode) {
                return CaretLocation.INSIDE_CLASS;
            } else if (current instanceof ModuleNode) {
                return CaretLocation.OUTSIDE_CLASSES;
            } else if (current instanceof Parameter) {
                return CaretLocation.INSIDE_PARAMETERS;
            } else if (current instanceof ConstructorCallExpression || current instanceof NamedArgumentListExpression) {
                ts.move(position);

                boolean afterLeftParen = false;

                WHILE_CYCLE:
                while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
                    Token<GroovyTokenId> t = ts.token();
                    switch (t.id()) {
                        case LPAREN:
                            afterLeftParen = true;
                            break WHILE_CYCLE;
                        case LITERAL_new:
                            break WHILE_CYCLE;
                    }
                }

                ts.move(position);
                boolean beforeRightParen = false;

                WHILE_CYCLE_2:
                while (ts.isValid() && ts.moveNext() && ts.offset() >= 0) {
                    Token<GroovyTokenId> t = ts.token();
                    switch (t.id()) {
                        case RPAREN:
                            beforeRightParen = true;
                            break WHILE_CYCLE_2;
                        case SEMI:
                            break WHILE_CYCLE_2;
                    }
                }

                // We are almost certainly inside of constructor call
                if (afterLeftParen && beforeRightParen) {
                    return CaretLocation.INSIDE_CONSTRUCTOR_CALL;
                }
            }
        }
        return CaretLocation.UNDEFINED;
    }

    /**
     * Computes an CompletionContext which surrounds the request.
     * Three tokens in front and three after the request.
     *
     * @return completion context
     */
    private CompletionSurrounding getCompletionContext() {
        int position = lexOffset;

        Token<GroovyTokenId> beforeLiteral = null;
        Token<GroovyTokenId> before2 = null;
        Token<GroovyTokenId> before1 = null;
        Token<GroovyTokenId> active = null;
        Token<GroovyTokenId> after1 = null;
        Token<GroovyTokenId> after2 = null;
        Token<GroovyTokenId> afterLiteral = null;

        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, position);

        int difference = ts.move(position);

        // get the active token:

        if (ts.isValid() && ts.moveNext() && ts.offset() >= 0) {
            active = ts.token();
        }

        // if we are right at the end of a line, a separator or a whitespace we gotta rewind.

        // 1.) NO  str.^<NLS>
        // 2.) NO  str.^subString
        // 3.) NO  str.sub^String
        // 4.) YES str.subString^<WHITESPACE-HERE>
        // 5.) YES str.subString^<NLS>
        // 6.) YES str.subString^()


        if (active != null) {
            if ((active.id() == GroovyTokenId.WHITESPACE && difference == 0)) {
                ts.movePrevious();
            } else if (active.id() == GroovyTokenId.NLS ) {
                ts.movePrevious();
                if (ts.token().id() == GroovyTokenId.AT ||
                    ts.token().id() == GroovyTokenId.DOT ||
                    ts.token().id() == GroovyTokenId.SPREAD_DOT ||
                    ts.token().id() == GroovyTokenId.OPTIONAL_DOT ||
                    ts.token().id() == GroovyTokenId.MEMBER_POINTER ||
                    ts.token().id() == GroovyTokenId.ELVIS_OPERATOR) {
                    ts.moveNext();
                }
            }
        }


        // Travel to the beginning to get before2 and before1

        int stopAt = 0;

        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<GroovyTokenId> t = ts.token();
            if (t.id() == GroovyTokenId.NLS) {
                break;
            } else if (t.id() != GroovyTokenId.WHITESPACE) {
                if (stopAt == 0) {
                    before1 = t;
                } else if (stopAt == 1) {
                    before2 = t;
                } else if (stopAt == 2) {
                    break;
                }

                stopAt++;
            }
        }

        // Move to the beginning (again) to get the next left-hand-sight literal

        ts.move(position);

        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<GroovyTokenId> t = ts.token();
            if (t.id() == GroovyTokenId.NLS ||
                t.id() == GroovyTokenId.LBRACE) {
                break;
            } else if (t.id().primaryCategory().equals("keyword")) {
                beforeLiteral = t;
                break;
            }
        }

        // now looking for the next right-hand-sight literal in the opposite direction

        ts.move(position);

        while (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
            Token<GroovyTokenId> t = ts.token();
            if (t.id() == GroovyTokenId.NLS ||
                t.id() == GroovyTokenId.RBRACE) {
                break;
            } else if (t.id().primaryCategory().equals("keyword")) {
                afterLiteral = t;
                break;
            }
        }


        // Now we're heading to the end of that stream

        ts.move(position);
        stopAt = 0;

        while (ts.isValid() && ts.moveNext() && ts.offset() < doc.getLength()) {
            Token<GroovyTokenId> t = ts.token();

            if (t.id() == GroovyTokenId.NLS) {
                break;
            } else if (t.id() != GroovyTokenId.WHITESPACE) {
                if (stopAt == 0) {
                    after1 = t;
                } else if (stopAt == 1) {
                    after2 = t;
                } else if (stopAt == 2) {
                    break;
                }
                stopAt++;
            }
        }

        return new CompletionSurrounding(beforeLiteral, before2, before1, active, after1, after2, afterLiteral, ts);
    }

    private DotCompletionContext getDotCompletionContext() {
        if (dotContext != null) {
            return dotContext;
        }

        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, lexOffset);
        ts.move(lexOffset);

        // get the active token:
        Token<GroovyTokenId> active = null;
        if (ts.isValid() && ts.moveNext() && ts.offset() >= 0) {
            active = ts.token();
        }

        // this should move us to dot or whitespace or NLS or prefix
        if (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            GroovyTokenId tokenID = ts.token().id();
            
            if (tokenID != GroovyTokenId.AT &&
                tokenID != GroovyTokenId.DOT &&
                tokenID != GroovyTokenId.NLS &&
                tokenID != GroovyTokenId.WHITESPACE &&
                tokenID != GroovyTokenId.SPREAD_DOT &&
                tokenID != GroovyTokenId.OPTIONAL_DOT &&
                tokenID != GroovyTokenId.MEMBER_POINTER &&
                tokenID != GroovyTokenId.ELVIS_OPERATOR) {

                // is it prefix
                // keyword check is here because of issue #150862
                if (tokenID != GroovyTokenId.IDENTIFIER && !tokenID.primaryCategory().equals("keyword")) {
                    return null;
                } else {
                    ts.movePrevious();
                }
            }
        }

        boolean fieldsOnly = false;
        if (ts.token().id() == GroovyTokenId.AT) {
            // We are either on Java Field Override operator *. or Spread Java Field operator *.@
            // Just move to previous and handle in the same way as DOT/SPREAD_DOT
            ts.movePrevious();
            fieldsOnly = true;
        }
        
        // now we should be on dot or in whitespace or NLS after the dot
        boolean remainingTokens = true;
        if (ts.token().id() != GroovyTokenId.DOT &&
            ts.token().id() != GroovyTokenId.SPREAD_DOT &&
            ts.token().id() != GroovyTokenId.OPTIONAL_DOT &&
            ts.token().id() != GroovyTokenId.MEMBER_POINTER &&
            ts.token().id() != GroovyTokenId.ELVIS_OPERATOR) {

            // travel back on the token string till the token is neither a
            // WHITESPACE nor NLS
            while (ts.isValid() && (remainingTokens = ts.movePrevious()) && ts.offset() >= 0) {
                Token<GroovyTokenId> t = ts.token();
                if (t.id() != GroovyTokenId.WHITESPACE && t.id() != GroovyTokenId.NLS) {
                    break;
                }
            }
        }

        if ((ts.token().id() != GroovyTokenId.DOT &&
             ts.token().id() != GroovyTokenId.SPREAD_DOT &&
             ts.token().id() != GroovyTokenId.OPTIONAL_DOT &&
             ts.token().id() != GroovyTokenId.MEMBER_POINTER &&
             ts.token().id() != GroovyTokenId.ELVIS_OPERATOR)
            || !remainingTokens) {

            return null; // no astpath
        }

        boolean methodsOnly = false;
        if (ts.token().id() == GroovyTokenId.MEMBER_POINTER) {
            methodsOnly = true;
        }

        // travel back on the token string till the token is neither a
        // WHITESPACE nor NLS
        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<GroovyTokenId> t = ts.token();
            if (t.id() != GroovyTokenId.WHITESPACE && t.id() != GroovyTokenId.NLS) {
                break;
            }
        }

        int lexOffset = ts.offset();
        int astOffset = ASTUtils.getAstOffset(parserResult, lexOffset) + ts.token().length() - 1;
        AstPath realPath = getPath(parserResult, doc, astOffset, true);

        return new DotCompletionContext(lexOffset, astOffset, realPath, fieldsOnly, methodsOnly);
    }

    /**
     * Get the ClassNode for the before-dot expression. This is important for
     * field and method completion.
     * <p>
     * If the <code>request.declaringClass</code> is not <code>null</code>
     * this value is immediately returned.
     * <p>
     * Returned value is stored to <code>request.declaringClass</code> too.
     *
     * Here are some sample paths:
     *
     * new String().
     * [ModuleNode:ConstructorCallExpression:ExpressionStatement:ConstructorCallExpression:]
     *
     * new String().[caret] something_unrelated
     * [ModuleNode:ClassNode:MethodCallExpression]
     * for this case we have to go for object expression of the method call
     *
     * s.
     * [ModuleNode:VariableExpression:ExpressionStatement:VariableExpression:]
     *
     * s.spli
     * [ModuleNode:PropertyExpression:ConstantExpression:ExpressionStatement:PropertyExpression:ConstantExpression:]
     *
     * l.
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:VariableExpression:]
     *
     * l.ab
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:PropertyExpression:ConstantExpression:]
     *
     * l.M
     * [ModuleNode:ClassNode:MethodNode:ExpressionStatement:PropertyExpression:VariableExpression:ConstantExpression:]
     *
     * @return a valid ASTNode or null
     */
    private ClassNode getBeforeDotDeclaringClass() {
        if (declaringClass instanceof ClassNode) {
            return declaringClass;
        }
        
        // This basically means we are not interested in classic type interference (because there will
        // be list, map or something like that) and we rather want to know what type is collected there
        if (isAfterSpreadOperator() || isAfterSpreadJavaFieldOperator()) {
            
            if (dotContext.getAstPath().leaf() instanceof Expression) {
                Expression expression = (Expression) dotContext.getAstPath().leaf();

                if (expression instanceof ListExpression) {
                    ListExpression listExpression = (ListExpression) expression;
                    for (Expression expr : listExpression.getExpressions()) {
                        return expr.getType();
                    }
                }
            }
        }

        // FIXME move this up
        DotCompletionContext dotCompletionContext = getDotCompletionContext();

        // FIXME static/script context...
        if (!isBehindDot() && (context.before1 == null || location == CaretLocation.INSIDE_METHOD)
                && (location == CaretLocation.INSIDE_CLOSURE || location == CaretLocation.INSIDE_METHOD)) {
            ASTNode an = ContextHelper.getSurroundingClassMember(this);
            boolean st = 
                    ((an instanceof FieldNode) && ((FieldNode)an).isStatic()) ||
                    ((an instanceof MethodNode) && ((MethodNode)an).isStatic());
            setDeclaringClass(ContextHelper.getSurroundingClassNode(this), st);
            return declaringClass;
        }

        if (dotCompletionContext == null || dotCompletionContext.getAstPath() == null
                || dotCompletionContext.getAstPath().leaf() == null) {
            return null;
        }

        // experimental type inference
        GroovyTypeAnalyzer typeAnalyzer = new GroovyTypeAnalyzer(doc);
        Set<ClassNode> infered = typeAnalyzer.getTypes(dotCompletionContext.getAstPath(),
                dotCompletionContext.getAstOffset());
        // FIXME multiple types
        // FIXME is there any test (?)
        if (!infered.isEmpty()) {
            setDeclaringClass(infered.iterator().next(), false);
            return rawDseclaringClass;
        }

        if (dotCompletionContext.getAstPath().leaf() instanceof VariableExpression) {
            VariableExpression variable = (VariableExpression) dotCompletionContext.getAstPath().leaf();
            if ("this".equals(variable.getName())) { // NOI18N
                setDeclaringClass(ContextHelper.getSurroundingClassNode(this), false);
                return rawDseclaringClass;
            }
            if ("super".equals(variable.getName())) { // NOI18N
                ClassNode thisClass = ContextHelper.getSurroundingClassNode(this);
                setDeclaringClass(declaringClass, false);
                if (declaringClass == null) {
                    return new ClassNode("java.lang.Object", ClassNode.ACC_PUBLIC, null);
                }
                return rawDseclaringClass;
            }
        }

        if (dotCompletionContext.getAstPath().leaf() instanceof Expression) {
            Expression expression = (Expression) dotCompletionContext.getAstPath().leaf();

            // see http://jira.codehaus.org/browse/GROOVY-3050
            if (expression instanceof RangeExpression
                    && "java.lang.Object".equals(expression.getType().getName())) { // NOI18N
                try {
                    expression.setType(
                            new ClassNode(Class.forName("groovy.lang.Range"))); // NOI18N
                } catch (ClassNotFoundException ex) {
                    expression.setType(
                            new ClassNode("groovy.lang.Range", ClassNode.ACC_PUBLIC | ClassNode.ACC_INTERFACE, null)); // NOI18N
                }
            // FIXME report issue
            } else if (expression instanceof ConstantExpression) {
                ConstantExpression constantExpression = (ConstantExpression) expression;
                if (!constantExpression.isNullExpression()) {
                    constantExpression.setType(new ClassNode(constantExpression.getValue().getClass()));
                }
            }
            setDeclaringClass(expression.getType(), false);
        }

        return rawDseclaringClass;
    }
    
    /**
     * Returns true if the code completion were invoked right after the Spread Java
     * Field operator *.@
     * 
     * @return true if invoked after *.@ operator, false otherwise
     */
    private boolean isAfterSpreadJavaFieldOperator() {
        if (context.before1 != null &&
            context.before2 != null &&
            context.before1.id().equals(GroovyTokenId.AT) &&
            context.before2.id().equals(GroovyTokenId.SPREAD_DOT)) {
            
            return true;
        }
        return false;
    }
    
    /**
     * Returns true if the code completion were invoked right after the Spread operator *.
     * 
     * @return true if invoked after *. operator, false otherwise
     */
    private boolean isAfterSpreadOperator() {
        if (context.before1 != null && context.before1.id().equals(GroovyTokenId.SPREAD_DOT)) {
            return true;
        }
        return false;
    }

    /**
     * Check whether this completion request was issued behind an import statement.
     * In such cases we are typically in context of completing packages/types within
     * an import statement. Few examples:
     * <br/><br/>
     * 
     * {@code import java.^}<br/>
     * {@code import java.lan^}<br/>
     * {@code import java.lang.In^}<br/>
     *
     * @return true if we are on the line that starts with an import keyword, false otherwise
     */
    public boolean isBehindImportStatement() {
        int rowStart = 0;
        int nonWhite = 0;

        try {
            rowStart = Utilities.getRowStart(doc, lexOffset);
            nonWhite = Utilities.getFirstNonWhiteFwd(doc, rowStart);

        } catch (BadLocationException ex) {
        }

        Token<GroovyTokenId> importToken = LexUtilities.getToken(doc, nonWhite);

        if (importToken != null && importToken.id() == GroovyTokenId.LITERAL_import) {
            return true;
        }

        return false;
    }
}
