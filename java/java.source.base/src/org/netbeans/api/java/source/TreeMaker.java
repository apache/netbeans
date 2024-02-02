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
package org.netbeans.api.java.source;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.tree.*;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import static com.sun.source.tree.Tree.*;

import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.JavaFileObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

import org.netbeans.api.java.lexer.JavaTokenId;

import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.query.CommentSet;
import org.netbeans.modules.java.source.query.CommentHandler;

import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.modules.java.source.save.PositionEstimator;
import org.openide.util.Parameters;

/**
 * Factory interface for creating new com.sun.source.tree instances.  The
 * parameters for each method correspond as closely as possible to the 
 * accessor methods for each tree interface.<br>
 *
 * You can obtain appropriate instance of this class by getting it from working
 * copy:
 *
 * <pre>{@code
 * CancellableTask task = new CancellableTask<WorkingCopy>() {
 *
 *        public void run(WorkingCopy workingCopy) throws Exception {
 *            <b>TreeMaker make = workingCopy.getTreeMaker()</b>;
 *            ... your modification code here
 *        }
 *        ...
 *    }; 
 * }</pre>
 *
 * @see <a href="https://netbeans.apache.org/wiki/JavaHT_Modification">How do I do modification to a source file?</a>
 *
 * @author Tom Ball
 * @author Pavel Flaska
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.org">RKo</a>)
 * 
 * @since 0.44.0
 */

public final class TreeMaker {
    
    private TreeFactory delegate;
    private CommentHandler handler;
    private final ASTService model;
    private WorkingCopy copy;
    
    TreeMaker(WorkingCopy copy, TreeFactory delegate) {
        this.delegate = delegate;
        this.copy = copy;
        this.handler = CommentHandlerService.instance(copy.impl.getJavacTask().getContext());
        this.model = ASTService.instance(copy.impl.getJavacTask().getContext());
    }
    
    /** 
     * Creates a new AnnotationTree.
     *
     * @param type the annotation type.
     * @param arguments the arguments for this annotation, or an empty list.
     * @see com.sun.source.tree.AnnotationTree
     */
    public AnnotationTree Annotation(Tree type, List<? extends ExpressionTree> arguments) {
        return delegate.Annotation(type, arguments);
    }
    
    /** 
     * Creates a new type AnnotationTree.
     *
     * @param type the annotation type.
     * @param arguments the arguments for this annotation, or an empty list.
     * @see com.sun.source.tree.AnnotationTree
     * @since 0.112
     */
    public AnnotationTree TypeAnnotation(Tree type, List<? extends ExpressionTree> arguments) {
        return delegate.TypeAnnotation(type, arguments);
    }
    
    AnnotatedTypeTree AnnotatedType(Tree underlyingType, List<? extends AnnotationTree> annotations) {
        return delegate.AnnotatedType(annotations, underlyingType);
    }
    
    /**
     * Creates a new ArrayAccessTree.
     *
     * @param array the array expression.
     * @param index the array index.
     * @see com.sun.source.tree.ArrayAccessTree
     */
    public ArrayAccessTree ArrayAccess(ExpressionTree array, ExpressionTree index) {
        return delegate.ArrayAccess(array, index);
    }
    
    /**
     * Creates a new ArrayTypeTree.
     *
     * @param type the array type.
     * @see com.sun.source.tree.ArrayTypeTree
     */
    public ArrayTypeTree ArrayType(Tree type) {
        return delegate.ArrayType(type);
    }
    
    /**
     * Creates a new AssertTree.
     *
     * @param condition the boolean expression to test.
     * @param detail the detail message to include if the assertion fails.
     * @see com.sun.source.tree.AssertTree
     */
    public AssertTree Assert(ExpressionTree condition, ExpressionTree detail) {
        return delegate.Assert(condition, detail);
    }
    
    /**
     * Creates a new AssignmentTree.
     *
     * @param variable the variable the expression is assigned to.
     * @param expression the expression to assign to the variable.
     * @see com.sun.source.tree.AssignmentTree
     */
    public AssignmentTree Assignment(ExpressionTree variable, ExpressionTree expression) {
        return delegate.Assignment(variable, expression);
    }

    /**
     * Creates a new BinaryTree.
     *
     * @param operator the operator for this tree, such as Tree.Kind.PLUS.
     * @param left  the left operand of the tree.
     * @param right the right operand of the tree.
     * @see com.sun.source.tree.BinaryTree
     * @see com.sun.source.tree.Tree.Kind
     */
    public BinaryTree Binary(Kind operator, ExpressionTree left, ExpressionTree right) {
        return delegate.Binary(operator, left, right);
    }

    /**
     * Creates a new BlockTree.
     *
     * @param statements the list of statements to be contained within the block.
     * @param isStatic true if the block defines a static initializer for a class.    ExpressionTree getCondition();
    ExpressionTree getDetail();

     * @see com.sun.source.tree.BlockTree
     */
    public BlockTree Block(List<? extends StatementTree> statements, boolean isStatic) {
        return delegate.Block(statements, isStatic);
    }
    
    /**
     * Creates a new BreakTree.
     *
     * @param label the label to break to, or null if there is no label.
     * @see com.sun.source.tree.BreakTree
     */
    public BreakTree Break(CharSequence label) {
        return delegate.Break(label);
    }
    
    /**
     * Creates a new CaseTree.
     *
     * @param expression the label for this case statement.
     * @param statements the list of statements.
     * @see com.sun.source.tree.CaseTree
     */
    public CaseTree Case(ExpressionTree expression, List<? extends StatementTree> statements) {
        return delegate.Case(expression, statements);
    }
    
    /**
     * Creates a new CaseTree.
     *
     * @param patterns the labels for this case statement.
     * @param statements the list of statements.
     * @see com.sun.source.tree.CaseTree
     * @since 2.39
     */
    public CaseTree CaseMultipleLabels(List<? extends ExpressionTree> patterns, List<? extends StatementTree> statements) {
        return delegate.Case(patterns, statements);
    }
    
    /**
     * Creates a new CaseTree for a rule case (case &lt;constants&gt; -> &lt;body&gt;).
     *
     * @param patterns the labels for this case statement.
     * @param body the case's body
     * @see com.sun.source.tree.CaseTree
     * @since 2.39
     */
    public CaseTree Case(List<? extends ExpressionTree> patterns, Tree body) {
        return delegate.Case(patterns, body);
    }
    
    /**
     * Creates a new CaseTree for a rule case (case &lt;constants&gt; -> &lt;body&gt;).
     *
     * @param patterns the labels for this case statement.
     * @param body the case's body
     * @see com.sun.source.tree.CaseTree
     * @since 2.39
     */
    public CaseTree CasePatterns(List<? extends Tree> patterns, Tree body) {
        return delegate.CaseMultiplePatterns(toCaseLabelTrees(patterns), null, body);
    }
    
    /**
     * Creates a new CaseTree for a rule case (case &lt;constants&gt; -> &lt;body&gt;).
     *
     * @param patterns the labels for this case statement.
     * @param guard the case's guard
     * @param body the case's body
     * @see com.sun.source.tree.CaseTree
     * @since 2.63
     */
    public CaseTree CasePatterns(List<? extends Tree> patterns, ExpressionTree guard, Tree body) {
        return delegate.CaseMultiplePatterns(toCaseLabelTrees(patterns), guard, body);
    }

    /**
     * Creates a new CaseTree.
     *
     * @param patterns the labels for this case statement.
     * @param statements the list of statements.
     * @see com.sun.source.tree.CaseTree
     * @since 2.39
     */
    public CaseTree CasePatterns(List<? extends Tree> patterns, List<? extends StatementTree> statements) {
        return delegate.CaseMultiplePatterns(toCaseLabelTrees(patterns), null, statements);
    }

    /**
     * Creates a new CaseTree.
     *
     * @param patterns the labels for this case statement.
     * @param guard the case's guard
     * @param statements the list of statements.
     * @see com.sun.source.tree.CaseTree
     * @since 2.63
     */
    public CaseTree CasePatterns(List<? extends Tree> patterns, ExpressionTree guard, List<? extends StatementTree> statements) {
        return delegate.CaseMultiplePatterns(toCaseLabelTrees(patterns), guard, statements);
    }

    private List<? extends CaseLabelTree> toCaseLabelTrees(List<? extends Tree> patterns) {
        return patterns.stream().map(p -> {
            if (p instanceof CaseLabelTree) {
                return (CaseLabelTree) p;
            }
            if (p instanceof ExpressionTree) {
                return delegate.ConstantCaseLabel((ExpressionTree) p);
            }
            if (p instanceof PatternTree) {
                return delegate.PatternCaseLabel((PatternTree) p);
            }
            throw new IllegalArgumentException("Invalid pattern kind: " + p.getKind()); //NOI18N
        }).collect(Collectors.toList());
    }
    
    /**
     * Creates a new CatchTree.
     *
     * @param parameter the exception variable declaration.
     * @param block     the block of statements executed by this catch statement.
     * @see com.sun.source.tree.CatchTree
     */
    public CatchTree Catch(VariableTree parameter, BlockTree block) {
        return delegate.Catch(parameter, block);
    }
    
    /** 
     * Creates a new ClassTree.
     *
     * @param modifiers the modifiers declaration
     * @param simpleName        the name of the class without its package, such
     *                          as "String" for the class "java.lang.String".
     * @param typeParameters    the list of type parameters, or an empty list.
     * @param extendsClause     the name of the class this class extends, or null.
     * @param implementsClauses the list of the interfaces this class
     *                          implements, or an empty list.
     * @param memberDecls       the list of fields defined by this class, or an
     *                          empty list.
     * @see com.sun.source.tree.ClassTree
     */
    public ClassTree Class(ModifiersTree modifiers, 
              CharSequence simpleName,
              List<? extends TypeParameterTree> typeParameters,
              Tree extendsClause,
              List<? extends Tree> implementsClauses,
              List<? extends Tree> memberDecls) {
        return delegate.Class(modifiers, simpleName, typeParameters, extendsClause, implementsClauses, memberDecls);
    }
    /**
     * Creates a new ClassTree representing interface.
     * 
     * @param modifiers the modifiers declaration
     * @param simpleName        the name of the class without its package, such
     *                          as "String" for the class "java.lang.String".
     * @param typeParameters    the list of type parameters, or an empty list.
     * @param extendsClauses    the list of the interfaces this class
     *                          extends, or an empty list.
     * @param memberDecls       the list of fields defined by this class, or an
     *                          empty list.
     * @see com.sun.source.tree.ClassTree
     */
    public ClassTree Interface(ModifiersTree modifiers, 
             CharSequence simpleName,
             List<? extends TypeParameterTree> typeParameters,
             List<? extends Tree> extendsClauses,
             List<? extends Tree> memberDecls) {
        return delegate.Interface(modifiers, simpleName, typeParameters, extendsClauses, memberDecls);
    }
    
    /**
     * Creates a new ClassTree representing annotation type.
     * 
     * @param modifiers the modifiers declaration
     * @param simpleName        the name of the class without its package, such
     *                          as "String" for the class "java.lang.String".
     * @param memberDecls       the list of fields defined by this class, or an
     *                          empty list.
     * @see com.sun.source.tree.ClassTree
     */
    public ClassTree AnnotationType(ModifiersTree modifiers, 
             CharSequence simpleName,
             List<? extends Tree> memberDecls) {
        return delegate.AnnotationType(modifiers, simpleName, memberDecls);
    }
    
    /**
     * Creates a new ClassTree representing enum.
     * 
     * @param modifiers the modifiers declaration
     * @param simpleName        the name of the class without its package, such
     *                          as "String" for the class "java.lang.String".
     * @param implementsClauses the list of the interfaces this class
     *                          implements, or an empty list.
     * @param memberDecls       the list of fields defined by this class, or an
     *                          empty list.
     * @see com.sun.source.tree.ClassTree
     */
    public ClassTree Enum(ModifiersTree modifiers, 
             CharSequence simpleName,
             List<? extends Tree> implementsClauses,
             List<? extends Tree> memberDecls) {
        return delegate.Enum(modifiers, simpleName, implementsClauses, memberDecls);
    }
    
    /**
     * Creates a new CompilationUnitTree.
     *
     * @param packageName        a tree representing the package name.
     * @param imports            a list of import statements.
     * @param typeDeclarations   a list of type (class, interface or enum) declarations.
     * @param sourceFile         the source file associated with this compilation unit.
     * @see com.sun.source.tree.CompilationUnitTree
     */
    public CompilationUnitTree CompilationUnit(ExpressionTree packageName,
                                               List<? extends ImportTree> imports,
                                               List<? extends Tree> typeDeclarations,
                                               JavaFileObject sourceFile) {
        return delegate.CompilationUnit(Collections.<AnnotationTree>emptyList(), packageName, imports, typeDeclarations, sourceFile);
    }

    /**
     * Creates a new CompilationUnitTree.
     *
     * @param packageAnnotations package annotations
     * @param packageName        a tree representing the package name.
     * @param imports            a list of import statements.
     * @param typeDeclarations   a list of type (class, interface or enum) declarations.
     * @param sourceFile         the source file associated with this compilation unit.
     * @see com.sun.source.tree.CompilationUnitTree
     * @since 0.66
     */
    public CompilationUnitTree CompilationUnit(List<? extends AnnotationTree> packageAnnotations,
                                        ExpressionTree packageName,
                                        List<? extends ImportTree> imports,
                                        List<? extends Tree> typeDeclarations,
                                        JavaFileObject sourceFile) {
        return delegate.CompilationUnit(packageAnnotations, packageName, imports, typeDeclarations, sourceFile);
    }
     
    /**
     * Creates a new CompilationUnitTree.
     *
     * @param pkg                a tree representing the package statement.
     * @param imports            a list of import statements.
     * @param typeDeclarations   a list of type (class, interface or enum) declarations.
     * @param sourceFile         the source file associated with this compilation unit.
     * @see com.sun.source.tree.CompilationUnitTree
     * @since 2.12
     */
    public CompilationUnitTree CompilationUnit(PackageTree pkg,
                                        List<? extends ImportTree> imports,
                                        List<? extends Tree> typeDeclarations,
                                        JavaFileObject sourceFile) {
        return delegate.CompilationUnit(pkg, imports, typeDeclarations, sourceFile);
    }
         
    /**
     * Creates a new CompilationUnitTree.
     * @param sourceRoot         a source root under which the new file is created
     * @param path               a relative path to file separated by '/'
     * @param imports            a list of import statements.
     * @param typeDeclarations   a list of type (class, interface or enum) declarations.
     * @see com.sun.source.tree.CompilationUnitTree
     */
    public CompilationUnitTree CompilationUnit(FileObject sourceRoot,
                                        String path,
                                        List<? extends ImportTree> imports,
                                        List<? extends Tree> typeDeclarations) {
        String[] nameComponent = FileObjects.getFolderAndBaseName(path,'/');        //NOI18N
        JavaFileObject sourceFile = FileObjects.templateFileObject(sourceRoot, nameComponent[0], nameComponent[1]);
        IdentifierTree pkg = nameComponent[0].length() == 0 ? null : Identifier(nameComponent[0].replace('/', '.'));
        return delegate.CompilationUnit(Collections.<AnnotationTree>emptyList(), pkg, imports, typeDeclarations, sourceFile);
    }

    /**
     * Creates a new CompilationUnitTree.
     * @param packageAnnotations package annotations
     * @param sourceRoot         a source root under which the new file is created
     * @param path               a relative path to file separated by '/'
     * @param imports            a list of import statements.
     * @param typeDeclarations   a list of type (class, interface or enum) declarations.
     * @see com.sun.source.tree.CompilationUnitTree
     * @since 0.66
     */
    public CompilationUnitTree CompilationUnit(List<? extends AnnotationTree> packageAnnotations,
                                        FileObject sourceRoot,
                                        String path,
                                        List<? extends ImportTree> imports,
                                        List<? extends Tree> typeDeclarations) {
        String[] nameComponent = FileObjects.getFolderAndBaseName(path,'/');        //NOI18N
        JavaFileObject sourceFile = FileObjects.templateFileObject(sourceRoot, nameComponent[0], nameComponent[1]);
        IdentifierTree pkg = nameComponent[0].length() == 0 ? null : Identifier(nameComponent[0].replace('/', '.'));
        return delegate.CompilationUnit(packageAnnotations, pkg, imports, typeDeclarations, sourceFile);
    }
    
    /**
     * Creates a new CompoundAssignmentTree.
     *
     * @param operator the operator for this tree, such as Tree.Kind.PLUS_ASSIGNMENT.
     * @param variable the variable the expression is assigned to.
     * @param expression the expression to assign to the variable.
     * @see com.sun.source.tree.CompoundAssignmentTree
     */
    public CompoundAssignmentTree CompoundAssignment(Kind operator, 
                                              ExpressionTree variable, 
                                              ExpressionTree expression) {
        return delegate.CompoundAssignment(operator, variable, expression);
    }
   
    /**
     * Creates a new ConditionalExpressionTree.
     *
     * @param condition       the boolean expression to test.
     * @param trueExpression  the expression to be executed when the 
     *                        condition is true.
     * @param falseExpression the expression to be executed when the
     *                        condition is false.
     * @see com.sun.source.tree.ConditionalExpressionTree
     */
    public ConditionalExpressionTree ConditionalExpression(ExpressionTree condition,
                                                    ExpressionTree trueExpression,
                                                    ExpressionTree falseExpression) {
        return delegate.ConditionalExpression(condition, trueExpression, falseExpression);
    }

    /**
     * Creates a new MethodTree representing constructor.
     * 
     * @param modifiers the modifiers of this method.
     * @param typeParameters the list of generic type parameters, or an empty list.
     * @param parameters the list of parameters, or an empty list.
     * @param throwsList the list of throws clauses, or an empty list.
     * @param body the method's code block.
     * @see com.sun.source.tree.MethodTree
     */
    public MethodTree Constructor(ModifiersTree modifiers,
                             List<? extends TypeParameterTree> typeParameters,
                             List<? extends VariableTree> parameters,
                             List<? extends ExpressionTree> throwsList,
                             BlockTree body) {
        return delegate.Method(modifiers, "<init>", null, typeParameters, parameters, throwsList, body, null);
    }
    
    /**
     * Creates a new MethodTree representing constructor.
     * 
     * @param modifiers the modifiers of this method.
     * @param typeParameters the list of generic type parameters, or an empty list.
     * @param parameters the list of parameters, or an empty list.
     * @param throwsList the list of throws clauses, or an empty list.
     * @param bodyText the method's code block provided as a plain text
     * @see com.sun.source.tree.MethodTree
     */
    public MethodTree Constructor(ModifiersTree modifiers,
                             List<? extends TypeParameterTree> typeParameters,
                             List<? extends VariableTree> parameters,
                             List<? extends ExpressionTree> throwsList,
                             String bodyText) {
        return Method(modifiers, "<init>", null, typeParameters, parameters, throwsList, bodyText, null);
    }
    
    /**
     * Creates a new ContinueTree.
     *
     * @param label the label to break to, or null if there is no label.
     * @see com.sun.source.tree.ContinueTree
     */
    public ContinueTree Continue(CharSequence label) {
        return delegate.Continue(label);
    }

    /**
     * Creates new UnionTypeTree.
     *
     * @param typeComponents components from which the DisjunctiveTypeTree should be created.
     *                       The components should either be {@link ExpressionTree} (qualified or unqualified identifier),
     *                       {@link PrimitiveTypeTree}, {@link WildcardTree}, {@link ParameterizedTypeTree} or {@link ArrayTypeTree}.
     * @return newly created DisjunctiveTypeTree
     * @since 0.70
     */
    public UnionTypeTree UnionType(List<? extends Tree> typeComponents) {
        return delegate.UnionType(typeComponents);
    }
    
    /** Creates a new DoWhileLoopTree.
     *
     * @param condition the boolean expression to test.
     * @param statement the statement to execute while the condition is true.
     * @see com.sun.source.tree.DoWhileLoopTree
     */
    public DoWhileLoopTree DoWhileLoop(ExpressionTree condition, StatementTree statement) {
        return delegate.DoWhileLoop(condition, statement);
    }

    /**
     * Creates a new EmptyStatementTree.
     *
     * @see com.sun.source.tree.EmptyStatementTree
     */
    public EmptyStatementTree EmptyStatement() {
        return delegate.EmptyStatement();
    }
    
    /**
     * Creates a new EnhancedForLoopTree.
     *
     * @param variable the loop variable declaration.
     * @param expression the expression to be iterated.
     * @param statement the statement to execute each iteration.
     * @see com.sun.source.tree.EnhancedForLoopTree
     */
    public EnhancedForLoopTree EnhancedForLoop(VariableTree variable, 
                                        ExpressionTree expression,
                                        StatementTree statement) {
        return delegate.EnhancedForLoop(variable, expression, statement);
    }
    
    /**
     * Creates a new ErroneousTree.
     *
     * @param errorTrees a list of trees with possible errors.
     * @see com.sun.source.tree.ErroneousTree
     */
    public ErroneousTree Erroneous(List<? extends Tree> errorTrees) {
        return delegate.Erroneous(errorTrees);
    }
    
    /**
     * Creates a new ExportsTree.
     *
     * @param qualId qualified name of the exported package.
     * @param moduleNames names of the modules the package is exported to.
     * @see com.sun.source.tree.ExportsTree
     * @since 2.23
     */
    public ExportsTree Exports(ExpressionTree qualId, List<? extends ExpressionTree> moduleNames) {
        return delegate.Exports(qualId, moduleNames);
    }
    
    /**
     * Creates a new ExpressionStatementTree.
     *
     * @param expression the expression body for this statement.
     * @see com.sun.source.tree.ExpressionStatementTree
     */
    public ExpressionStatementTree ExpressionStatement(ExpressionTree expression) {
        return delegate.ExpressionStatement(expression);
    }
    
    /**
     * Creates a new ForLoopTree.
     *
     * @param initializer a list of initializer statements, or an empty list.
     * @param condition   the condition to evaluate after each iteration.
     * @param update      the statements to execute after each iteration.
     * @param statement   the statement to execute for each iteration.
     * @see com.sun.source.tree.ForLoopTree
     */
    public ForLoopTree ForLoop(List<? extends StatementTree> initializer, 
                        ExpressionTree condition,
                        List<? extends ExpressionStatementTree> update,
                        StatementTree statement) {
        return delegate.ForLoop(initializer, condition, update, statement);
    }
    
    /**
     * Creates a new IdentifierTree. The generated code will contain given {@code name}
     * exactly as passed to this method. No checks will be performed on the given {@code name}.
     * Imports won't we resolved for fully qualified names. Consider using {@link #QualIdent(Element)},
     * {@link #QualIdent(String)}, {@link #Type(String)} or {@link #Type(TypeMirror)} to
     * get automatically resolved imports for fully qualified names or types.
     *
     * @param name the name of the identifier.
     * @see com.sun.source.tree.IdentifierTree
     * @see #QualIdent(Element)
     * @see #QualIdent(String)
     * @see #Type(String)
     * @see #Type(TypeMirror)
     */
    public IdentifierTree Identifier(CharSequence name) {
        return delegate.Identifier(name);
    }
    
    /**
     * Creates a new IdentifierTree from an Element.
     *
     * @param element the element from which to extract the identifier name.
     * @see com.sun.source.tree.IdentifierTree
     * @see javax.lang.model.element.Element
     */
    public IdentifierTree Identifier(Element element) {
        return delegate.Identifier(element);
    }
     
    
    /** Creates a new IfTree.
     *
     * @param condition the boolean expression to test.
     * @param thenStatement the statement to execute if the condition is true.
     * @param elseStatement the statement to execute if the condition if false.
     *                      A null value should be used if there is no else 
     *                      statement.
     * @see com.sun.source.tree.IfTree
     */
    public IfTree If(ExpressionTree condition, StatementTree thenStatement, StatementTree elseStatement) {
        return delegate.If(condition, thenStatement, elseStatement);
    }
    
    /**
     * Creates a new ImportTree.
     *
     * @param qualid fully qualified identifier.
     * @param importStatic true if static import statement.
     * @see com.sun.source.tree.ImportTree
     */
    public ImportTree Import(Tree qualid, boolean importStatic) {
        return delegate.Import(qualid, importStatic);
    }
    
    /**
     * Creates a new InstanceOfTree.
     *
     * @param expression the expression whose type is being checked.
     * @param type       the type to compare to.
     * @see com.sun.source.tree.InstanceOfTree
     */
    public InstanceOfTree InstanceOf(ExpressionTree expression, Tree type) {
        return delegate.InstanceOf(expression, type);
    }
    
    /**
     * Creates a new LabeledStatementTree.
     *
     * @param label the label string.
     * @param statement the statement being labeled.
     * @see com.sun.source.tree.LabeledStatementTree
     */
    public LabeledStatementTree LabeledStatement(CharSequence label, StatementTree statement) {
        return delegate.LabeledStatement(label, statement);
    }

    /**Creates a new LambdaExpressionTree
     * 
     * @param parameters the lambda's formal arguments
     * @param body the lambda's body
     * @return the LambdaExpressionTree
     * @since 0.112
     */
    public LambdaExpressionTree LambdaExpression(List<? extends VariableTree> parameters, Tree body) {
        return delegate.LambdaExpression(parameters, body);
    }

    /**
     * Creates a new LiteralTree.  Only literals which are wrappers for 
     * primitive types (Integer, Boolean, etc.) and String instances can
     * be literals.
     *
     * @param value the value of the literal.
     * @throws IllegalArgumentException for illegal literal values.
     * @see com.sun.source.tree.LiteralTree
     */
    public LiteralTree Literal(Object value) {
        return delegate.Literal(value);
    }
    
    /**Creates a new MemberReferenceTree.
     * 
     * @param refMode the desired {@link ReferenceMode reference mode}
     * @param expression the class (or expression), from which a member is to be referenced
     * @param name the name of the referenced member
     * @param typeArguments the reference's type arguments
     * @return the MemberReferenceTree
     * @since 0.112
     */
    public MemberReferenceTree MemberReference(ReferenceMode refMode, ExpressionTree expression, CharSequence name, List<? extends ExpressionTree> typeArguments) {
        return delegate.MemberReference(refMode, name, expression, typeArguments);
    }
    
    /**
     * Creates a new MemberSelectTree.  A MemberSelectTree consists of an
     * expression and an identifier.  Valid expressions include things like
     * packages, class and field references, etc., while the identifier is a
     * "child" of the expression.  For example, "System.out" is represented by 
     * MemberSelectTree which has an ExpressionTree representing "System" and
     * an identifier of "out".
     *
     * @param expression the expression the identifier is part of.
     * @param identifier the element to select.
     * @see com.sun.source.tree.MemberSelectTree
     */
    public MemberSelectTree MemberSelect(ExpressionTree expression, CharSequence identifier) {
        return delegate.MemberSelect(expression, identifier);
    }

    /**
     * Creates a new MemberSelectTree from an expression and an element.
     *
     * @param expression the expression the identifier is part of.
     * @param element the element that provides the identifier name.
     * @see com.sun.source.tree.MemberSelectTree
     * @see javax.lang.model.element.Element
     */
    public MemberSelectTree MemberSelect(ExpressionTree expression, Element element) {
        return delegate.MemberSelect(expression, element);
    }
    
    /**
     * Creates a new MethodInvocationTree.
     *
     * @param typeArguments the list of generic type arguments, or an empty list.
     * @param method the method to be invoked.
     * @param arguments the list of arguments to pass to the method, or an empty list.
     * @see com.sun.source.tree.MethodInvocationTree
     */
    public MethodInvocationTree MethodInvocation(List<? extends ExpressionTree> typeArguments, 
                                          ExpressionTree method, 
                                          List<? extends ExpressionTree> arguments) {
        return delegate.MethodInvocation(typeArguments, method, arguments);
    }
    
    /**
     * Creates a new MethodTree.
     *
     * @param modifiers the modifiers of this method.
     * @param name the name of the method.
     * @param returnType the return type for this method.
     * @param typeParameters the list of generic type parameters, or an empty list.
     * @param parameters the list of parameters, or an empty list.
     * @param throwsList the list of throws clauses, or an empty list.
     * @param body the method's code block.
     * @param defaultValue the default value, used by annotation types.
     * @see com.sun.source.tree.MethodTree
     */
    public MethodTree Method(ModifiersTree modifiers,
                      CharSequence name,
                      Tree returnType,
                      List<? extends TypeParameterTree> typeParameters,
                      List<? extends VariableTree> parameters,
                      List<? extends ExpressionTree> throwsList,
                      BlockTree body,
                      ExpressionTree defaultValue) {
        return Method(modifiers, name, returnType, typeParameters, parameters, throwsList, body, defaultValue, false);
    }
    
    /**
     * Creates a new MethodTree.
     *
     * @param modifiers the modifiers of this method.
     * @param name the name of the method.
     * @param returnType the return type for this method.
     * @param typeParameters the list of generic type parameters, or an empty list.
     * @param parameters the list of parameters, or an empty list.
     * @param throwsList the list of throws clauses, or an empty list.
     * @param body the method's code block.
     * @param defaultValue the default value, used by annotation types.
     * @param isVarArg true if this method has variable number of  parameters.
     * @see com.sun.source.tree.MethodTree
     */
    public MethodTree Method(ModifiersTree modifiers,
                      CharSequence name,
                      Tree returnType,
                      List<? extends TypeParameterTree> typeParameters,
                      List<? extends VariableTree> parameters,
                      List<? extends ExpressionTree> throwsList,
                      BlockTree body,
                      ExpressionTree defaultValue,
                      boolean isVarArg) {
        return delegate.Method(modifiers, name, returnType, typeParameters, parameters, throwsList, body, defaultValue, isVarArg);
    }
    
    /**
     * Creates a new MethodTree from an ExecutableElement and a BlockTree.
     *
     * @param element the executable element of this method.
     * @param body    the method's code block, or null for native, abstract,
     *                and interface methods.
     * @see com.sun.source.tree.MethodTree
     * @see javax.lang.model.element.ExecutableElement
     * @deprecated this method produces different output than usually expected -
     *             use one of the other "Method" methods in this class or {@link GeneratorUtilities}.
     */
    @Deprecated
    public MethodTree Method(ExecutableElement element, BlockTree body) {
        return delegate.Method(element, body);
    }

    /**
     * Creates a new ModuleTree with a new qualified name and directives.
     *
     * @param modifiers module modifiers
     * @param kind module kind
     * @param qualid module name
     * @param directives a list of module directives, or an empty list.
     * @see com.sun.source.tree.ModuleTree
     * @since 2.25
     */
    public ModuleTree Module(ModifiersTree modifiers, ModuleTree.ModuleKind kind, ExpressionTree qualid, List<? extends DirectiveTree> directives) {
        return delegate.Module(modifiers, kind, qualid, directives);
    }

    /**
     * Creates a new ModifiersTree with a new set of flags and annotations.
     *
     * @param flags the set of modifier flags
     * @param annotations a list of annotations, or an empty list.
     * @see com.sun.source.tree.ModifiersTree
     * @see javax.lang.model.element.Modifier
     */
    public ModifiersTree Modifiers(Set<Modifier> flags, List<? extends AnnotationTree> annotations) {
        return delegate.Modifiers(flags, annotations);
    }
    
    /**
     * Creates a new ModifiersTree with a new flags and annotation.
     *
     * @param flags modifier flags
     * @see com.sun.source.tree.ModifiersTree
     */
    public ModifiersTree Modifiers(long flags, List<? extends AnnotationTree> annotations) {
        return delegate.Modifiers(flags, annotations);
    }
    
    /**
     * Creates a new ModifiersTree without any annotations specified.
     *
     * @param flags the set of modifier flags
     * @see com.sun.source.tree.ModifiersTree
     * @see javax.lang.model.element.Modifier
     */
    public ModifiersTree Modifiers(Set<Modifier> flags) {
        return delegate.Modifiers(flags);
    }
    
    /**
     * Creates a new ModifiersTree with a new set of annotations.  The existing
     * flags are copied from the old tree; this preserves private javac flags.
     *
     * @param oldMods the old ModifiersTree, from which the flags are copied.
     * @param annotations a list of annotations, or an empty list.
     * @see com.sun.source.tree.ModifiersTree
     * @see javax.lang.model.element.Modifier
     */
    public ModifiersTree Modifiers(ModifiersTree oldMods, List<? extends AnnotationTree> annotations) {
        return delegate.Modifiers(oldMods, annotations);
    }
    
    /**
     * Creates a new NewArrayTree.
     *
     * @param elemtype the element type.
     * @param dimensions the list of array dimensions.
     * @param initializers the list of initializer statements, or an empty list.
     * @see com.sun.source.tree.NewArrayTree
     */
    public NewArrayTree NewArray(Tree elemtype, 
                          List<? extends ExpressionTree> dimensions,
                          List<? extends ExpressionTree> initializers) {
        return delegate.NewArray(elemtype, dimensions, initializers);
    }

    /**
     * Creates a new NewClassTree.
     *
     * @param enclosingExpression the enclosing expression, or null.
     * @param typeArguments       the list of generic type arguments, or an empty list.
     * @param identifier          the class name expression
     * @param arguments           the list of constructor arguments, or an empty list.
     * @param classBody           the class definition, or null if there is no definition.
     * @see com.sun.source.tree.NewClassTree
     */
    public NewClassTree NewClass(ExpressionTree enclosingExpression, 
                          List<? extends ExpressionTree> typeArguments,
                          ExpressionTree identifier,
                          List<? extends ExpressionTree> arguments,
                          ClassTree classBody) {
        return delegate.NewClass(enclosingExpression, typeArguments, identifier, arguments, classBody);
    }
    
    /**
     * Creates a new OpensTree.
     *
     * @param qualId qualified name of the opened package.
     * @param moduleNames names of the modules the package is opened to.
     * @see com.sun.source.tree.OpensTree
     * @since 2.25
     */
    public OpensTree Opens(ExpressionTree qualId, List<? extends ExpressionTree> moduleNames) {
        return delegate.Opens(qualId, moduleNames);
    }
    
    /**
     * Creates a new ParameterizedTypeTree.
     *
     * @param type          the generic type
     * @param typeArguments the list of generic type arguments, or an empty list.
     * @see com.sun.source.tree.ParameterizedTypeTree
     */
    public ParameterizedTypeTree ParameterizedType(Tree type,
                                            List<? extends Tree> typeArguments) {
        return delegate.ParameterizedType(type, typeArguments);
    }

    /**
     * Creates a new ParenthesizedTree.
     *
     * @param expression the expression within the parentheses.
     * @see com.sun.source.tree.ParenthesizedTree
     */
    public ParenthesizedTree Parenthesized(ExpressionTree expression) {
        return delegate.Parenthesized(expression);
    }
    
    /**
     * Creates a new PrimitiveTypeTree.
     *
     * @param typekind the primitive type.
     * @see com.sun.source.tree.PrimitiveTypeTree
     * @see javax.lang.model.type.TypeKind
     */
    public @NonNull PrimitiveTypeTree PrimitiveType(@NonNull TypeKind typekind) {
        Parameters.notNull("typekind", typekind);
        return delegate.PrimitiveType(typekind);
    }
    
    /**
     * Creates a new ProvidesTree.
     *
     * @param serviceName the name of the provided service
     * @param implNames the names of the implementation classes
     * @see com.sun.source.tree.ProvidesTree
     * @since 2.25
     */
    public ProvidesTree Provides(ExpressionTree serviceName, List<? extends ExpressionTree> implNames) {
        return delegate.Provides(serviceName, implNames);
    }
    
    /**
     * Creates a new RequiresTree.
     *
     * @param isTransitive
     * @param isStatic
     * @param qualId the qualified name of the required module
     * @see com.sun.source.tree.RequiresTree
     * @since 2.25
     */
    public RequiresTree Requires(boolean isTransitive, boolean isStatic, ExpressionTree qualId) {
        return delegate.Requires(isTransitive, isStatic, qualId);
    }
    
    /**
     * Creates a qualified identifier from an element. Simple name will automatically
     * be used if appropriate, adding any needed imports, following user's preferences.
     *
     * @param element the element to use.
     */
    public @NonNull ExpressionTree QualIdent(@NonNull Element element) {
        Parameters.notNull("element", element);
        return delegate.QualIdent(element);
    }

    /**
     * Creates a qualified identifier for a given String. Simple name will automatically
     * be used if appropriate, adding any needed imports, following user's preferences.
     *
     * @param name FQN for which to create the QualIdent
     * @since 0.65
     */
    public @NonNull ExpressionTree QualIdent(@NonNull String name) {
        Parameters.notNull("name", name);
        return delegate.QualIdent(name);
    }
    
    /**
     * Creates a new ReturnTree.
     *
     * @param expression the expression to be returned.
     * @see com.sun.source.tree.ReturnTree
     */
    public ReturnTree Return(ExpressionTree expression) {
        return delegate.Return(expression);
    }
    
    /**
     * Creates a new SwitchTree.
     *
     * @param expression the expression which provides the value to be switched.
     * @param cases the list of cases, or an empty list.
     * @see com.sun.source.tree.SwitchTree
     */
    public SwitchTree Switch(ExpressionTree expression, List<? extends CaseTree> cases) {
        return delegate.Switch(expression, cases);
    }

    /**
     * Creates a new SwitchExpressionTree.
     *
     * @param expression the expression which provides the value to be switched.
     * @param cases the list of cases, or an empty list.
     * @see com.sun.source.tree.SwitchExpressionTree
     * @since 2.41
     */
    public Tree SwitchExpression(ExpressionTree expression, List<? extends CaseTree> cases) {
        return delegate.SwitchExpression(expression, cases);
    }

    /**
     * Creates a new SynchronizedTree.
     *
     * @param expression the expression defining the object being synchronized.
     * @param block      the block of statements executed by this statement.
     * @see com.sun.source.tree.SynchronizedTree
     */
    public SynchronizedTree Synchronized(ExpressionTree expression, BlockTree block) {
        return delegate.Synchronized(expression, block);
    }
    
    /**
     * Creates a new ThrowTree.
     *
     * @param expression the exception to be thrown.
     * @see com.sun.source.tree.ThrowTree
     */
    public ThrowTree Throw(ExpressionTree expression) {
        return delegate.Throw(expression);
    }
    
    /**
     * Creates a new TryTree.
     *
     * @param tryBlock     the statement block in the try clause.
     * @param catches      the list of catch clauses, or an empty list.
     * @param finallyBlock the finally clause, or null.
     * @see com.sun.source.tree.TryTree
     */
    public TryTree Try(BlockTree tryBlock, 
                List<? extends CatchTree> catches, 
                BlockTree finallyBlock) {
        return Try(Collections.<Tree>emptyList(), tryBlock, catches, finallyBlock);
    }

    /**
     * Creates a new TryTree.
     *
     * @param resources     the resources of the try clause. The elements of the list
     *                     should either be {@link VariableTree}s or {@link ExpressionTree}s.
     * @param tryBlock     the statement block in the try clause.
     * @param catches      the list of catch clauses, or an empty list.
     * @param finallyBlock the finally clause, or null.
     * @see com.sun.source.tree.TryTree
     * @since 0.67
     */
    public TryTree Try(List<? extends Tree> resources,
                BlockTree tryBlock,
                List<? extends CatchTree> catches,
                BlockTree finallyBlock) {
        return delegate.Try(resources, tryBlock, catches, finallyBlock);
    }
    
    /**
     * Creates a new Tree for a given TypeMirror.
     *
     * @param type       TypeMirror for which a Tree should be created
     * @see com.sun.source.tree.ExpressionTree
     */
    public @NonNull Tree Type(@NonNull TypeMirror type) {
        Parameters.notNull("type", type);
        return delegate.Type(type);
    }

    /**
     * Creates a new Tree for a given String type specification.
     *
     * @param type       String type specification
     * @see com.sun.source.tree.ExpressionTree
     * @since 0.65
     */
    public @NonNull Tree Type(@NonNull String type) {
        Parameters.notNull("type", type);

        Tree typeTree = copy.getTreeUtilities().parseType(type);
        final Map<Tree, Tree> translate = new HashMap<Tree, Tree>();

        new ErrorAwareTreeScanner<Void, Void>() {
            @Override
            public Void visitMemberSelect(MemberSelectTree node, Void p) {
                translate.put(node, QualIdent(node.toString()));
                return null;
            }
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                translate.put(node, QualIdent(node.toString()));
                return null;
            }
        }.scan(typeTree, null);

        return copy.getTreeUtilities().translate(typeTree, translate);
    }

    /**
     * Creates a new TypeCastTree.
     *
     * @param type       the class or interface to cast.
     * @param expression the expression being cast.
     * @see com.sun.source.tree.TypeCastTree
     */
    public TypeCastTree TypeCast(Tree type, ExpressionTree expression) {
        return delegate.TypeCast(type, expression);
    }
    
    /**
     * Creates a new TypeParameterTree.
     *
     * @param name  the name of this type parameter.
     * @param bounds the bounds of this parameter.
     * @see com.sun.source.tree.TypeParameterTree
     */
    public TypeParameterTree TypeParameter(CharSequence name, 
                                    List<? extends ExpressionTree> bounds) {
        return delegate.TypeParameter(name, bounds);
    }

    /**
     * Creates a new UnaryTree.
     *
     * @param operator the operator for this tree, such as Tree.Kind.PLUS.
     * @param arg      the operand of the tree.
     * @see com.sun.source.tree.UnaryTree
     * @see com.sun.source.tree.Tree.Kind
     */
    public UnaryTree Unary(Kind operator, ExpressionTree arg) {
        return delegate.Unary(operator, arg);
    }
    
    /**
     * Creates a new UsesTree.
     *
     * @param qualId qualified service name.
     * @see com.sun.source.tree.UsesTree
     * @since 2.23
     */
    public UsesTree Uses(ExpressionTree qualId) {
        return delegate.Uses(qualId);
    }
    
    /**
     * Creates a new VariableTree.
     *
     * @param modifiers the modifiers of this variable.
     * @param name the name of the variable.
     * @param type the type of this variable.
     * @param initializer the initialization expression for this variable, or null.
     * @see com.sun.source.tree.VariableTree
     */
    public VariableTree Variable(ModifiersTree modifiers,
                          CharSequence name,
                          Tree type,
                          ExpressionTree initializer) {
        return delegate.Variable(modifiers, name, type, initializer);
    }
    
    /**
     * Creates a new BindingPatternTree.
     * @deprecated
     * @param name name of the binding variable
     * @param type the type of the pattern
     * @return the newly created BindingPatternTree
     */
    @Deprecated
    public Tree BindingPattern(CharSequence name,
                               Tree type) {
        return delegate.BindingPattern(name, type);
    }
    
      /**
     * Creates a new Tree for a given VariableTree
     * specication : 15.20.2
     * @param vt the VariableTree of the pattern
     * @see com.sun.source.tree.BindingPatternTree
     * @return the newly created BindingPatternTree
     * @since 16
     */
    public Tree BindingPattern(VariableTree vt) {
        return delegate.BindingPattern(vt);
    }

      /**
     * Creates a new Tree for a given DeconstructionPatternTree
     * @param deconstructor deconstructor of record pattern
     * @param nested list of nested patterns
     * @param vt the variable of record pattern. This parameter is currently ignored.
     * @see com.sun.source.tree.DeconstructionPatternTree
     * @return the newly created RecordPatternTree
     * @since 19
     */
    //TODO: overload without VariableTree?
    public DeconstructionPatternTree RecordPattern(ExpressionTree deconstructor, List<PatternTree> nested, VariableTree vt) {
        return delegate.DeconstructionPattern(deconstructor, nested);
    }

    /**
     * Creates a new VariableTree from a VariableElement.
     *
     * @param variable the VariableElement to reference.
     * @param initializer the initialization expression, or null.
     * @see com.sun.source.tree.VariableTree
     * @see javax.lang.model.element.VariableElement
     */
    public VariableTree Variable(VariableElement variable, ExpressionTree initializer) {
        return delegate.Variable(variable, initializer);
    }
    
    /** 
     * Creates a new WhileLoopTree.
     *
     * @param condition the boolean expression to test.
     * @param statement the statement to execute while the condition is true.
     * @see com.sun.source.tree.WhileLoopTree
     */
    public WhileLoopTree WhileLoop(ExpressionTree condition, StatementTree statement) {
        return delegate.WhileLoop(condition, statement);
    }
    
    /**
     * Creates a new WildcardTree.
     *
     * @param kind  the kind of wildcard to create.
     * @param type the type (class, interface or enum) of this wildcard.
     * @see com.sun.source.tree.WildcardTree
     */
    public WildcardTree Wildcard(Kind kind, Tree type) {
        return delegate.Wildcard(kind, type);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // AnnotationTree
    /**
     * Appends specified element <tt>attrValue</tt> to the end of attribute 
     * values list.
     *
     * @param   annotation  annotation tree containing attribute values list.
     * @param   attrValue   element to be appended to attribute values list.
     * @return  annotation tree with modified attribute values.
     */
    public AnnotationTree addAnnotationAttrValue(AnnotationTree annotation, ExpressionTree attrValue) {
        return delegate.addAnnotationAttrValue(annotation, attrValue);
    }
    
    /**
     * Inserts the specified element <tt>attrValue</tt> at the specified 
     * position in attribute values list.
     *
     * @param  annotation  annotation tree with attribute values list.
     * @param  index       index at which the specified element is to be inserted.
     * @param  attrValue   element to be inserted to attribute values list.
     * @return annotation tree with modified attribute values.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public AnnotationTree insertAnnotationAttrValue(AnnotationTree annotation, int index, ExpressionTree attrValue) {
        return delegate.insertAnnotationAttrValue(annotation, index, attrValue);
    }
    
    /**
     * Removes the first occurrence in attribute values list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param annotation  annotation tree with attribute values list.
     * @param attrValue element to be removed from this list, if present.
     * @return  annotation tree with modified attribute values.
     */
    public AnnotationTree removeAnnotationAttrValue(AnnotationTree annotation, ExpressionTree attrValue) {
        return delegate.removeAnnotationAttrValue(annotation, attrValue);
    }
    
    /**
     * Removes the element at the specified position in attribute values list.
     * Returns the modified annotation tree.
     *
     * @param annotation  annotation tree with attribute values list.
     * @param index       the index of the element to be removed.
     * @return  annotation tree with modified attribute values.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         (index &lt; 0 || index &gt;= size()).
     */
    public AnnotationTree removeAnnotationAttrValue(AnnotationTree annotation, int index) {
        return delegate.removeAnnotationAttrValue(annotation, index);
    }
    
    // BlockTree
    /**
     * Appends specified element <tt>statement</tt> to the end of statements
     * list.
     *
     * @param   block      block tree containing statements list.
     * @param   statement   element to be appended to statements list.
     * @return  block tree with modified statements
     */
    public BlockTree addBlockStatement(BlockTree block, StatementTree statement) {
        return delegate.addBlockStatement(block, statement);
    }
    
    /**
     * Inserts the specified element <tt>statement</tt> at the specified 
     * position in statements list.
     *
     * @param  block       block tree with statements list
     * @param  index       index at which the specified element is to be inserted.
     * @param  statement   element to be inserted to statements list.
     * @return block tree with modified statements
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public BlockTree insertBlockStatement(BlockTree block, int index, StatementTree statement) {
        return delegate.insertBlockStatement(block, index, statement);
    }
    
    /**
     * Removes the first occurrence in statements list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param block     block tree with statements list
     * @param statement element to be removed from this list, if present.
     * @return  block tree with modified statements
     */
    public BlockTree removeBlockStatement(BlockTree block, StatementTree statement) {
        return delegate.removeBlockStatement(block, statement);
    }
    
    /**
     * Removes the element at the specified position in statements list.
     * Returns the modified block tree.
     *
     * @param block  block tree with statements list
     * @param index  the index of the element to be removed.
     * @return  block tree with modified statements
     * 
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         (index &lt; 0 || index &gt;= size()).
     */
    public BlockTree removeBlockStatement(BlockTree block, int index) {
        return delegate.removeBlockStatement(block, index);
    }
    
    // CaseTree
    /**
     * Appends specified element <tt>statement</tt> to the end of statements
     * list.
     *
     * @param  kejs      case tree containing statements list.
     * @param  statement element to be appended to statements list.
     * @return case tree with modified statements.
     */
    public CaseTree addCaseStatement(CaseTree kejs, StatementTree statement) {
        return delegate.addCaseStatement(kejs, statement);
    }
    
    /**
     * Inserts the specified element <tt>statement</tt> at the specified 
     * position in statements list.
     *
     * @param  kejs      case tree containing statements list.
     * @param  index     index at which the specified element is to be inserted.
     * @param  statement element to be inserted to statements list.
     * @return case tree with modified statements.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public CaseTree insertCaseStatement(CaseTree kejs, int index, StatementTree statement) {
        return delegate.insertCaseStatement(kejs, index, statement);
    }
    
    /**
     * Removes the first occurrence in statements list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param  kejs      case tree containing statements list.
     * @param statement element to be removed from this list, if present.
     * @return  case tree with modified statements.
     */
    public CaseTree removeCaseStatement(CaseTree kejs, StatementTree statement) {
        return delegate.removeCaseStatement(kejs, statement);
    }
    
    /**
     * Removes the element at the specified position in statements list.
     * Returns the modified case tree.
     *
     * @param  kejs  case tree containing statements list.
     * @param index  the index of the element to be removed.
     * @return  case tree with modified statements.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         (index &lt; 0 || index &gt;= size()).
     */
    public CaseTree removeCaseStatement(CaseTree kejs, int index) {
        return delegate.removeCaseStatement(kejs, index);
    }

    // ModuleTree
    /**
     * Appends specified <tt>directive</tt> to the end of directives list.
     * 
     * @param modle  module tree with directives list
     * @param directive  directive to be added to the list
     * @return module tree with modified directives.
     * @since 2.23
     */
    public ModuleTree addModuleDirective(ModuleTree modle, DirectiveTree directive) {
        return delegate.addModuleDirective(modle, directive);
    }
    
    
    /**
     * Inserts the specified <tt>directive</tt> at the specified position
     * in directives list.
     *
     * @param  modle     module tree with directives list
     * @param  index     index at which the specified directive is to be inserted.
     * @param  directive directive to be inserted to the list.
     * @return module tree with modified directives.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     * @since 2.23
     */
    public ModuleTree insertModuleDirective(ModuleTree modle, int index, DirectiveTree directive) {
        return delegate.insertModuleDirective(modle, index, directive);
    }
    
    /**
     * Removes the first occurrence in directives list of the specified 
     * directive. If this list does not contain the directive, it is
     * unchanged.
     *
     * @param modle   module tree with directives list
     * @param directive  directive to be removed from this list, if present.
     * @return  module tree with modified directives.
     * @since 2.23
     */
    public ModuleTree removeModuleDirective(ModuleTree modle, DirectiveTree directive) {
        return delegate.removeModuleDirective(modle, directive);
    }
    
    /**
     * Removes the directive at the specified position in directives list.
     * Returns the modified module tree.
     *
     * @param modle  module tree with directives list.
     * @param index  the index of the directive to be removed.
     * @return  module tree with modified directives.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         (index &lt; 0 || index &gt;= size()).
     * @since 2.23
     */
    public ModuleTree removeModuleDirective(ModuleTree modle, int index) {
        return delegate.removeModuleDirective(modle, index);
    }
    
    // ClassTree
    /**
     * Appends specified element <tt>member</tt> to the end of members
     * list. Consider you want to add such a method to the end of class:
     * <pre>
     *   public void newlyCreatedMethod(int a, float b) throws java.io.IOException {
     *   }
     * </pre>
     *
     * You can get it e.g. with this code:
     * <pre>{@code
     *   TreeMaker make = workingCopy.getTreeMaker();
     *   ClassTree node = ...;
     *   // create method modifiers
     *    ModifiersTree parMods = make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST);
     *   // create parameters
     *   VariableTree par1 = make.Variable(parMods, "a", make.PrimitiveType(TypeKind.INT), null);
     *   VariableTree par2 = make.Variable(parMods, "b", make.PrimitiveType(TypeKind.FLOAT), null);
     *   List<VariableTree> parList = new ArrayList<VariableTree>(2);
     *   parList.add(par1);
     *   parList.add(par2);
     *   // create method
     *   MethodTree newMethod = make.Method(
     *       make.Modifiers(
     *          Collections.singleton(Modifier.PUBLIC), // modifiers
     *           Collections.EMPTY_LIST // annotations
     *       ), // modifiers and annotations
     *       "newlyCreatedMethod", // name
     *       make.PrimitiveType(TypeKind.VOID), // return type
     *       Collections.EMPTY_LIST, // type parameters for parameters
     *       parList, // parameters
     *       Collections.singletonList(make.Identifier("java.io.IOException")), // throws 
     *       make.Block(Collections.EMPTY_LIST, false), // empty statement block
     *       null // default value - not applicable here, used by annotations
     *   );
     *   // rewrite the original class node with the new one containing newMethod
     *   workingCopy.rewrite(node, <b>make.addClassMember(node, newMethod)</b>);
     * }</pre>
     *
     * @param   clazz    class tree containing members list.
     * @param   member   element to be appended to members list.
     * @return  class tree with modified members.
     */
    public ClassTree addClassMember(ClassTree clazz, Tree member) {
        return delegate.addClassMember(clazz, member);
    }
    
    /**
     * Inserts the specified element <tt>member</tt> at the specified 
     * position in members list.
     *
     * @param  clazz     class tree with members list
     * @param  index     index at which the specified element is to be inserted.
     * @param  member element to be inserted to members list.
     * @return class tree with modified members.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public ClassTree insertClassMember(ClassTree clazz, int index, Tree member) {
        return delegate.insertClassMember(clazz, index, member);
    }
    
    /**
     * Removes the first occurrence in members list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param clazz   class tree with members list
     * @param member  element to be removed from this list, if present.
     * @return  class tree with modified members.
     */
    public ClassTree removeClassMember(ClassTree clazz, Tree member) {
        return delegate.removeClassMember(clazz, member);
    }
    
    /**
     * Removes the element at the specified position in members list.
     * Returns the modified class tree.
     *
     * @param clazz  class tree with members list.
     * @param index  the index of the element to be removed.
     * @return  class tree with modified members.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         (index &lt; 0 || index &gt;= size()).
     */
    public ClassTree removeClassMember(ClassTree clazz, int index) {
        return delegate.removeClassMember(clazz, index);
    }
    
    /**
     * Appends specified element <tt>typeParameter</tt> to the end of type parameters
     * list.
     *
     * @param   clazz    class tree containing type parameters list.
     * @param   typeParameter   element to be appended to type parameters list.
     * @return  class tree with modified type parameters.
     */
    public ClassTree addClassTypeParameter(ClassTree clazz, TypeParameterTree typeParameter) {
        return delegate.addClassTypeParameter(clazz, typeParameter);
    }
    
    /**
     * Inserts the specified element <tt>member</tt> at the specified 
     * position in type parameters list.
     *
     * @param  clazz     class tree with type parameters list
     * @param  index     index at which the specified element is to be inserted.
     * @param  typeParameter element to be inserted to type parameters list.
     * @return class tree with modified type parameters.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public ClassTree insertClassTypeParameter(ClassTree clazz, int index, TypeParameterTree typeParameter) {
        return delegate.insertClassTypeParameter(clazz, index, typeParameter);
    }
    
    /**
     * Removes the first occurrence in type parameters list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param clazz   class tree with type parameters list
     * @param typeParameter  element to be removed from this list, if present.
     * @return  class tree with modified type parameters.
     */
    public ClassTree removeClassTypeParameter(ClassTree clazz, TypeParameterTree typeParameter) {
        return delegate.removeClassTypeParameter(clazz, typeParameter);
    }
    
    /**
     * Removes the element at the specified position in type parameters list.
     * Returns the modified class tree.
     *
     * @param clazz  class tree with type parameters list.
     * @param index  the index of the element to be removed.
     * @return  class tree with modified type parameters.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         (index &lt; 0 || index &gt;= size()).
     */
    public ClassTree removeClassTypeParameter(ClassTree clazz, int index) {
        return delegate.removeClassTypeParameter(clazz, index);
    }
    
    /**
     * Appends specified element <tt>implementsClause</tt> to the end of implements
     * list.
     *
     * @param   clazz    class tree containing implements list.
     * @param   implementsClause   element to be appended to implements list.
     * @return  class tree with modified implements.
     */
    public ClassTree addClassImplementsClause(ClassTree clazz, Tree implementsClause) {
        return delegate.addClassImplementsClause(clazz, implementsClause);
    }
    
    /**
     * Inserts the specified element <tt>implementsClause</tt> at the specified 
     * position in implements list.
     *
     * @param  clazz     class tree with implements list
     * @param  index     index at which the specified element is to be inserted.
     * @param  implementsClause element to be inserted to implements list.
     * @return class tree with modified implements.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public ClassTree insertClassImplementsClause(ClassTree clazz, int index, Tree implementsClause) {
        return delegate.insertClassImplementsClause(clazz, index, implementsClause);
    }
    
    /**
     * Removes the first occurrence in implements list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param clazz   class tree with implements list
     * @param implementsClause  element to be removed from this list, if present.
     * @return  class tree with modified implements.
     */
    public ClassTree removeClassImplementsClause(ClassTree clazz, Tree implementsClause) {
        return delegate.removeClassImplementsClause(clazz, asRemoved(implementsClause));
    }
    
    /**
     * Removes the element at the specified position in implements list.
     * Returns the modified class tree.
     *
     * @param clazz  class tree with implements list.
     * @param index  the index of the element to be removed.
     * @return  class tree with modified implements.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         (index &lt; 0 || index &gt;= size()).
     */
    public ClassTree removeClassImplementsClause(ClassTree clazz, int index) {
        return delegate.removeClassImplementsClause(clazz, index);
    }
        
    // CompilationUnitTree
    /**
     * Appends specified element <tt>typeDeclaration</tt> to the end of type 
     * declarations list.
     *
     * @param  compilationUnit compilation unit tree containing type declarations list.
     * @param  typeDeclaration element to be appended to type declarations list.
     * @return compilation unit tree with modified type declarations.
     */
    public CompilationUnitTree addCompUnitTypeDecl(CompilationUnitTree compilationUnit, Tree typeDeclaration) {
        return delegate.addCompUnitTypeDecl(compilationUnit, typeDeclaration);
    }
    
    /**
     * Inserts the specified element <tt>typeDeclaration</tt> at the specified 
     * position in type declarations list.
     *
     * @param  compilationUnit  compilation unit tree containing type declarations list.
     * @param  index index at which the specified element is to be inserted.
     * @param  typeDeclaration   element to be inserted to type declarations list.
     * @return compilation unit tree with modified type declarations.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public CompilationUnitTree insertCompUnitTypeDecl(CompilationUnitTree compilationUnit, int index, Tree typeDeclaration) {
        return delegate.insertCompUnitTypeDecl(compilationUnit, index, typeDeclaration);
    }
    
    /**
     * Removes the first occurrence in type declarations list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   compilationUnit compilation unit tree containing type declarations list.
     * @param   typeDeclaration element to be removed from this list, if present.
     * @return  compilation unit tree with modified type declarations.
     */
    public CompilationUnitTree removeCompUnitTypeDecl(CompilationUnitTree compilationUnit, Tree typeDeclaration) {
        return delegate.removeCompUnitTypeDecl(compilationUnit, asRemoved(typeDeclaration));
    }
    
    /**
     * Removes the element at the specified position in type declarations list.
     * Returns the modified compilation unit tree.
     *
     * @param   compilationUnit compilation unit tree containing type declarations list.
     * @param   index   the index of the element to be removed.
     * @return  compilation unit tree with modified type declarations.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public CompilationUnitTree removeCompUnitTypeDecl(CompilationUnitTree compilationUnit, int index) {
        return delegate.removeCompUnitTypeDecl(compilationUnit, index);
    }
    
    /**
     * Appends specified element <tt>importt</tt> to the end of imports list.
     *
     * @param  compilationUnit compilation unit tree containing imports list.
     * @param  importt element to be appended to list of imports.
     * @return compilation unit tree with modified imports.
     */
    public CompilationUnitTree addCompUnitImport(CompilationUnitTree compilationUnit, ImportTree importt) {
        return delegate.addCompUnitImport(compilationUnit, importt);
    }
    
    /**
     * Inserts the specified element <tt>importt</tt> at the specified 
     * position in imports list.
     *
     * @param  compilationUnit  compilation unit tree containing imports list.
     * @param  index index at which the specified element is to be inserted.
     * @param  importt element to be inserted to list of imports.
     * @return compilation unit tree with modified imports.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public CompilationUnitTree insertCompUnitImport(CompilationUnitTree compilationUnit, int index, ImportTree importt) {
        return delegate.insertCompUnitImport(compilationUnit, index, importt);
    }
    
    /**
     * Removes the first occurrence in imports list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   compilationUnit compilation unit tree containing import list.
     * @param   importt element to be removed from this list, if present.
     * @return  compilation unit tree with modified imports.
     */
    public CompilationUnitTree removeCompUnitImport(CompilationUnitTree compilationUnit, ImportTree importt) {
        return delegate.removeCompUnitImport(compilationUnit, asRemoved(importt));
    }
    
    /**
     * Removes the element at the specified position in import list.
     * Returns the modified compilation unit tree.
     *
     * @param   compilationUnit compilation unit tree containing import list.
     * @param   index   the index of the element to be removed.
     * @return  compilation unit tree with modified imports.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public CompilationUnitTree removeCompUnitImport(CompilationUnitTree compilationUnit, int index) {
        return delegate.removeCompUnitImport(compilationUnit, index);
    }
    
    /**
     * Appends specified element <tt>annotation</tt> to the end of package annotations
     * list.
     *
     * @param  cut  compilation unit tree containing package annotations list.
     * @param  annotation  element to be appended to annotations list.
     * @return compilation unit tree with modified package annotations.
     * @since 0.66
     */
    public CompilationUnitTree addPackageAnnotation(CompilationUnitTree cut, AnnotationTree annotation) {
        return delegate.addPackageAnnotation(cut, annotation);
    }

    /**
     * Inserts the specified element <tt>annotation</tt> at the specified
     * position in package annotations list.
     *
     * @param  cut  compilation unit tree containing package annotations list.
     * @param  index   index at which the specified element is to be inserted.
     * @param  annotation element to be inserted to the package annotations list.
     * @return compilation unit tree with modified package annotations.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     * @since 0.66
     */
    public CompilationUnitTree insertPackageAnnotation(CompilationUnitTree cut, int index, AnnotationTree annotation) {
        return delegate.insertPackageAnnotation(cut, index, annotation);
    }

    /**
     * Removes the first occurrence in package annotations list of the specified
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   cut  compilation unit tree containing package annotations list.
     * @param   annotation    element to be removed from this list, if present.
     * @return compilation unit tree with modified package annotations.
     * @since 0.66
     */
    public CompilationUnitTree removePackageAnnotation(CompilationUnitTree cut, AnnotationTree annotation) {
        return delegate.removePackageAnnotation(cut, annotation);
    }

    /**
     * Removes the element at the specified position in package annotations list.
     * Returns the modified compilation unit tree.
     *
     * @param   cut  compilation unit tree containing package annotations list.
     * @param   index   the index of the element to be removed.
     * @return compilation unit tree with modified package annotations.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     * @since 0.66
     */
    public CompilationUnitTree removePackageAnnotation(CompilationUnitTree cut, int index) {
        return delegate.removePackageAnnotation(cut, index);
    }

    /** ErroneousTree */
    
    // ForLoopInitializer
    /**
     * Appends specified element <tt>initializer</tt> to the end of initializers
     * list.
     *
     * @param  forLoop    for loop tree containing initializers list.
     * @param  initializer     element to be appended to initializers list.
     * @return for loop tree with modified initializers.
     */
    public ForLoopTree addForLoopInitializer(ForLoopTree forLoop, StatementTree initializer) {
        return delegate.addForLoopInitializer(forLoop, initializer);
    }
    
    /**
     * Inserts the specified element <tt>initializer</tt> at the specified 
     * position in initializers list.
     *
     * @param  forLoop  for loop tree containing initializers list.
     * @param  index   index at which the specified element is to be inserted.
     * @param  initializer   element to be inserted to initializers list.
     * @return for loop tree with modified initializers.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public ForLoopTree insertForLoopInitializer(ForLoopTree forLoop, int index, StatementTree initializer) {
        return delegate.insertForLoopInitializer(forLoop, index, initializer);
    }
    
    /**
     * Removes the first occurrence in initializers list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   forLoop    for loop tree containing initializers list.
     * @param   initializer    element to be removed from this list, if present.
     * @return  for loop tree with modified initializers.
     */
    public ForLoopTree removeForLoopInitializer(ForLoopTree forLoop, StatementTree initializer) {
        return delegate.removeForLoopInitializer(forLoop, initializer);
    }
    
    /**
     * Removes the element at the specified position in initializers list.
     * Returns the modified for loop tree.
     *
     * @param   forLoop  for loop tree containing initializers list.
     * @param   index   the index of the element to be removed.
     * @return  for loop tree with modified initializers.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public ForLoopTree removeForLoopInitializer(ForLoopTree forLoop, int index) {
        return delegate.removeForLoopInitializer(forLoop, index);
    }
    
    // ForLoopUpdate
    /**
     * Appends specified element <tt>update</tt> to the end of updates
     * list.
     *
     * @param  forLoop    for loop tree containing updates list.
     * @param  update     element to be appended to updates list.
     * @return for loop tree with modified updates.
     */
    public ForLoopTree addForLoopUpdate(ForLoopTree forLoop, ExpressionStatementTree update) {
        return delegate.addForLoopUpdate(forLoop, update);
    }
    
    /**
     * Inserts the specified element <tt>update</tt> at the specified 
     * position in updates list.
     *
     * @param  forLoop  for loop tree containing updates list.
     * @param  index   index at which the specified element is to be inserted.
     * @param  update   element to be inserted to updates list.
     * @return for loop tree with modified updates.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public ForLoopTree insertForLoopUpdate(ForLoopTree forLoop, int index, ExpressionStatementTree update) {
        return delegate.insertForLoopUpdate(forLoop, index, update);
    }
    
    /**
     * Removes the first occurrence in updates list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   forLoop    for loop tree containing updates list.
     * @param   update    element to be removed from this list, if present.
     * @return  for loop tree with modified updates.
     */
    public ForLoopTree removeForLoopUpdate(ForLoopTree forLoop, ExpressionStatementTree update) {
        return delegate.removeForLoopUpdate(forLoop, update);
    }
    
    /**
     * Removes the element at the specified position in updates list.
     * Returns the modified for loop tree.
     *
     * @param   forLoop  for loop tree containing updates list.
     * @param   index   the index of the element to be removed.
     * @return  for loop tree with modified updates.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public ForLoopTree removeForLoopUpdate(ForLoopTree forLoop, int index) {
        return delegate.removeForLoopUpdate(forLoop, index);
    }
    
    // MethodInvocation
    /**
     * Appends specified element <tt>argument</tt>.
     *
     * @param  methodInvocation method invocation tree containing arguments list.
     * @param  argument     element to be appended to arguments list.
     * @return method invocation tree with modified arguments and type arguments.
     */
    public MethodInvocationTree addMethodInvocationArgument(MethodInvocationTree methodInvocation, ExpressionTree argument) {
        return delegate.addMethodInvocationArgument(methodInvocation, argument);
    }
    
    /**
     * Inserts the specified element <tt>argument</tt>.
     *
     * @param  methodInvocation method invocation tree containing arguments list.
     * @param  index  index at which the specified elements is to be inserted.
     * @param  argument   element to be inserted to arguments list.
     * @return method invocation tree with modified type arguments and type arguments.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public MethodInvocationTree insertMethodInvocationArgument(MethodInvocationTree methodInvocation, int index, ExpressionTree argument) {
        return delegate.insertMethodInvocationArgument(methodInvocation, index, argument);
    }
    
    /**
     * Removes the first occurrence in arguments list of the specified
     * element. If this list does not contain the element, it is does not
     * change anything.
     *
     * @param   methodInvocation method invocation tree containing arguments list.
     * @param   argument   element to be removed from this list, if present.
     * @return  method invocation tree with modified arguments and type arguments.
     */
    public MethodInvocationTree removeMethodInvocationArgument(MethodInvocationTree methodInvocation, ExpressionTree argument) {
        return delegate.removeMethodInvocationArgument(methodInvocation, argument);
    }
    
    /**
     * Removes the element at the specified position in arguments
     * list. Returns the modified method invocation tree.
     *
     * @param   methodInvocation method invocation tree containing arguments list.
     * @param   index  the index of the element to be removed.
     * @return  method invocation tree with modified arguments and type arguments.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public MethodInvocationTree removeMethodInvocationArgument(MethodInvocationTree methodInvocation, int index) {
        return delegate.removeMethodInvocationArgument(methodInvocation, index);
    }
    
    /**
     * Appends specified element <tt>type argument</tt> 
     * to the end of type arguments list.
     *
     * @param  methodInvocation method invocation tree containing arguments list.
     * @param  typeArgument element to be appended to type arguments list.
     * @return method invocation tree with modified arguments and type arguments.
     */
    public MethodInvocationTree addMethodInvocationTypeArgument(MethodInvocationTree methodInvocation, ExpressionTree typeArgument) {
        return delegate.addMethodInvocationTypeArgument(methodInvocation, typeArgument);
    }
    
    /**
     * Inserts the specified element <tt>typeArgument</tt>
     * at the specified position in type arguments list.
     *
     * @param  methodInvocation method invocation tree containing arguments list.
     * @param  index  index at which the specified elements is to be inserted.
     * @param  typeArgument element to be inserted to type arguments list.
     * @return method invocation tree with modified type arguments and type arguments.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public MethodInvocationTree insertMethodInvocationTypeArgument(MethodInvocationTree methodInvocation, int index, ExpressionTree typeArgument) {
        return delegate.insertMethodInvocationTypeArgument(methodInvocation, index, typeArgument);
    }

    /** 
     * Removes the first occurrence in type arguments list of the specified 
     * elements. If this list does not contain the element, it is method
     * does not change anything.
     *
     * @param   methodInvocation method invocation tree containing arguments list.
     * @param   typeArgument element to be removed from this list, if present.
     * @return  method invocation tree with modified arguments and type arguments.
     */
    public MethodInvocationTree removeMethodInvocationTypeArgument(MethodInvocationTree methodInvocation, ExpressionTree typeArgument) {
        return delegate.removeMethodInvocationTypeArgument(methodInvocation, typeArgument);
    }
    
    /**
     * Removes the element at the specified position in type arguments.
     * Returns the modified method invocation tree.
     *
     * @param   methodInvocation method invocation tree containing arguments list.
     * @param   index  the index of the element to be removed.
     * @return  method invocation tree with modified arguments and type arguments.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public MethodInvocationTree removeMethodInvocationTypeArgument(MethodInvocationTree methodInvocation, int index) {
        return delegate.removeMethodInvocationTypeArgument(methodInvocation, index);
    }
    
    // Method
    /**
     * Appends specified element <tt>parameter</tt>
     * to the end of parameters list.
     *
     * @param  method        method tree containing parameters list.
     * @param  parameter     element to be appended to parameters list.
     * @return method tree with modified parameters.
     */
    public MethodTree addMethodParameter(MethodTree method, VariableTree parameter) {
        return delegate.addMethodParameter(method, parameter);
    }
    
    /**
     * Inserts the specified element <tt>parameter</tt> 
     * at the specified position in parameters list.
     *
     * @param  method method tree containing parameters list.
     * @param  index  index at which the specified elements is to be inserted.
     * @param  parameter   element to be inserted to parameters list.
     * @return method tree with modified parameters.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public MethodTree insertMethodParameter(MethodTree method, int index, VariableTree parameter) {
        return delegate.insertMethodParameter(method, index, parameter);
    }
    
    /**
     * Removes the first occurrence in parameters list of the specified 
     * elements. If this list do not contain the element, it is
     * unchanged.
     *
     * @param   method method tree containing parameters list.
     * @param   parameter   element to be removed from this list, if present.
     * @return  method tree with modified parameters and type parameters.
     */
    public MethodTree removeMethodParameter(MethodTree method, VariableTree parameter) {
        return delegate.removeMethodParameter(method, parameter);
    }
    
    /**
     * Removes the element at the specified position in parameters list.
     * Returns the modified method tree.
     *
     * @param   method method tree containing parameters list.
     * @param   index  the index of the element to be removed.
     * @return  method tree with modified parameters.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public MethodTree removeMethodParameter(MethodTree method, int index) {
        return delegate.removeMethodParameter(method, index);
    }
    
    /**
     * Appends specified element <tt>typeParameter</tt>
     * to the end of type parameters list.
     *
     * @param  method        method tree containing type parameters list.
     * @param  typeParameter element to be appended to type parameters list.
     * @return method tree with modified type parameters.
     */
    public MethodTree addMethodTypeParameter(MethodTree method, TypeParameterTree typeParameter) {
        return delegate.addMethodTypeParameter(method, typeParameter);
    }
    
    /**
     * Inserts the specified element <tt>typeParameter</tt> 
     * at the specified position in type parameters list.
     *
     * @param  method method tree containing parameters list.
     * @param  index  index at which the specified elements is to be inserted.
     * @param  typeParameter element to be inserted to type parameters list.
     * @return method tree with modified type parameters.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public MethodTree insertMethodTypeParameter(MethodTree method, int index, TypeParameterTree typeParameter) {
        return delegate.insertMethodTypeParameter(method, index, typeParameter);
    }
    
    /**
     * Removes the first occurrence in type parameters list of the specified 
     * elements. If this list do not contain the element, it is
     * unchanged.
     *
     * @param   method method tree containing type parameters list.
     * @param   typeParameter element to be removed from this list, if present.
     * @return  method tree with modified type parameters.
     */
    public MethodTree removeMethodTypeParameter(MethodTree method, TypeParameterTree typeParameter) {
        return delegate.removeMethodTypeParameter(method, typeParameter);
    }
    
    /**
     * Removes the element at the specified position in type parameters list.
     * Returns the modified method tree.
     *
     * @param   method method tree containing type parameters list.
     * @param   index  the index of the element to be removed.
     * @return  method tree with modified type parameters.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public MethodTree removeMethodTypeParameter(MethodTree method, int index) {
        return delegate.removeMethodTypeParameter(method, index);
    }
    
    /**
     * Appends specified element <tt>throwz</tt> to the end of throws
     * list.
     *
     * @param  method     method tree containing throws list.
     * @param  throwz     element to be appended to throws list.
     * @return method tree with modified throws.
     */
    public MethodTree addMethodThrows(MethodTree method, ExpressionTree throwz) {
        return delegate.addMethodThrows(method, throwz);
    }
    
    /**
     * Inserts the specified element <tt>throws</tt> at the specified 
     * position in throws list.
     *
     * @param  method  method tree containing throws list.
     * @param  index   index at which the specified element is to be inserted.
     * @param  throwz   element to be inserted to throws list.
     * @return method tree with modified throws.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public MethodTree insertMethodThrows(MethodTree method, int index, ExpressionTree throwz) {
        return delegate.insertMethodThrows(method, index, throwz);
    }
    
    /**
     * Removes the first occurrence in throws list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   method    method tree containing throws list.
     * @param   throwz    element to be removed from this list, if present.
     * @return  method tree with modified throws.
     */
    public MethodTree removeMethodThrows(MethodTree method, ExpressionTree throwz) {
        return delegate.removeMethodThrows(method, throwz);
    }
    
    /**
     * Removes the element at the specified position in throws list.
     * Returns the modified method tree.
     *
     * @param   method  method tree containing throws list.
     * @param   index   the index of the element to be removed.
     * @return  method tree with modified throws.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public MethodTree removeMethodThrows(MethodTree method, int index) {
        return delegate.removeMethodThrows(method, index);
    }
    
    // Modifiers
    /**
     * Appends specified element <tt>annotation</tt> to the end of annotations
     * list.
     *
     * @param  modifiers   modifiers tree containing annotations list.
     * @param  annotation  element to be appended to annotations list.
     * @return modifiers tree with modified annotations.
     */
    public ModifiersTree addModifiersAnnotation(ModifiersTree modifiers, AnnotationTree annotation) {
        return delegate.addModifiersAnnotation(modifiers, annotation);
    }
    
    /**
     * Inserts the specified element <tt>annotation</tt> at the specified 
     * position in annotations list.
     *
     * @param  modifiers  modifiers tree containing annotations list.
     * @param  index   index at which the specified element is to be inserted.
     * @param  annotation   element to be inserted to annotations list.
     * @return modifiers tree with modified annotations.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public ModifiersTree insertModifiersAnnotation(ModifiersTree modifiers, int index, AnnotationTree annotation) {
        return delegate.insertModifiersAnnotation(modifiers, index, annotation);
    }
    
    /**
     * Removes the first occurrence in annotations list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   modifiers    modifiers tree containing annotations list.
     * @param   annotation    element to be removed from this list, if present.
     * @return  modifiers tree with modified annotations.
     */
    public ModifiersTree removeModifiersAnnotation(ModifiersTree modifiers, AnnotationTree annotation) {
        return delegate.removeModifiersAnnotation(modifiers, annotation);
    }
    
    /**
     * Removes the element at the specified position in annotations list.
     * Returns the modified modifiers tree.
     *
     * @param   modifiers  modifiers tree containing annotations list.
     * @param   index   the index of the element to be removed.
     * @return  modifiers tree with modified annotations.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public ModifiersTree removeModifiersAnnotation(ModifiersTree modifiers, int index) {
        return delegate.removeModifiersAnnotation(modifiers, index);
    }
    
    public ModifiersTree addModifiersModifier(ModifiersTree modifiers, Modifier modifier) {
        long c = ((JCModifiers) modifiers).flags & ~Flags.GENERATEDCONSTR;
        switch (modifier) {
            case ABSTRACT: c = c | Flags.ABSTRACT; break;
            case FINAL: c = c | Flags.FINAL; break;
            case NATIVE: c = c | Flags.NATIVE; break;
            case PRIVATE: c = c | Flags.PRIVATE; break;
            case PROTECTED: c = c | Flags.PROTECTED; break;
            case PUBLIC: c = c | Flags.PUBLIC; break;
            case STATIC: c = c | Flags.STATIC; break;
            case STRICTFP: c = c | Flags.STRICTFP; break;
            case SYNCHRONIZED: c = c | Flags.SYNCHRONIZED; break;
            case TRANSIENT: c = c | Flags.TRANSIENT; break;
            case VOLATILE: c = c | Flags.VOLATILE; break;
            case DEFAULT: c = c | Flags.DEFAULT; break;
            default:
                break;
        }
        return Modifiers(c, modifiers.getAnnotations());
    }
    
    public ModifiersTree removeModifiersModifier(ModifiersTree modifiers, Modifier modifier) {
        long c = ((JCModifiers) modifiers).flags & ~Flags.GENERATEDCONSTR;
        switch (modifier) {
            case ABSTRACT: c = c & ~Flags.ABSTRACT; break;
            case FINAL: c = c & ~Flags.FINAL; break;
            case NATIVE: c = c & ~Flags.NATIVE; break;
            case PRIVATE: c = c & ~Flags.PRIVATE; break;
            case PROTECTED: c = c & ~Flags.PROTECTED; break;
            case PUBLIC: c = c & ~Flags.PUBLIC; break;
            case STATIC: c = c & ~Flags.STATIC; break;
            case STRICTFP: c = c & ~Flags.STRICTFP; break;
            case SYNCHRONIZED: c = c & ~Flags.SYNCHRONIZED; break;
            case TRANSIENT: c = c & ~Flags.TRANSIENT; break;
            case VOLATILE: c = c & ~Flags.VOLATILE; break;
            case DEFAULT: c = c & ~Flags.DEFAULT; break;
            default:
                break;
        }
        return Modifiers(c, modifiers.getAnnotations());
    }
    
    // NewArray
    /**
     * Appends specified element <tt>dimension</tt> to the end of dimensions
     * list.
     *
     * @param  newArray   new array tree containing dimensions list.
     * @param  dimension    element to be appended to dimensions list.
     * @return new array tree with modified dimensions.
     */
    public NewArrayTree addNewArrayDimension(NewArrayTree newArray, ExpressionTree dimension) {
        return delegate.addNewArrayDimension(newArray, dimension);
    }
    
    /**
     * Inserts the specified element <tt>dimension</tt> at the specified 
     * position in dimensions list.
     *
     * @param  newArray   new array tree containing dimensions list.
     * @param  index  index at which the specified element is to be inserted.
     * @param  dimension   element to be inserted to dimensions list.
     * @return new array tree with modified dimensions.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public NewArrayTree insertNewArrayDimension(NewArrayTree newArray, int index, ExpressionTree dimension) {
        return delegate.insertNewArrayDimension(newArray, index, dimension);
    }
    
    /**
     * Removes the first occurrence in dimensions list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   newArray  new array tree containing dimensions list.
     * @param   dimension   element to be removed from this list, if present.
     * @return  new array tree with modified dimensions.
     */
    public NewArrayTree removeNewArrayDimension(NewArrayTree newArray, ExpressionTree dimension) {
        return delegate.removeNewArrayDimension(newArray, dimension);
    }
    
    /**
     * Removes the element at the specified position in dimensions list.
     * Returns the modified new array tree.
     *
     * @param   newArray   new array tree containing dimensions list.
     * @param   index  the index of the element to be removed.
     * @return  new array tree with modified dimensions.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public NewArrayTree removeNewArrayDimension(NewArrayTree newArray, int index) {
        return delegate.removeNewArrayDimension(newArray, index);
    }

    // NewArrayTree
    /**
     * Appends specified element <tt>initializer</tt> to the end of initializers
     * list.
     *
     * @param  newArray   new array tree containing initializers list.
     * @param  initializer    element to be appended to initializers list.
     * @return new array tree with modified initializers.
     */
    public NewArrayTree addNewArrayInitializer(NewArrayTree newArray, ExpressionTree initializer) {
        return delegate.addNewArrayInitializer(newArray, initializer);
    }
    
    /**
     * Inserts the specified element <tt>initializer</tt> at the specified 
     * position in initializers list.
     *
     * @param  newArray   new array tree containing initializers list.
     * @param  index  index at which the specified element is to be inserted.
     * @param  initializer   element to be inserted to initializers list.
     * @return new array tree with modified initializers.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public NewArrayTree insertNewArrayInitializer(NewArrayTree newArray, int index, ExpressionTree initializer) {
        return delegate.insertNewArrayInitializer(newArray, index, initializer);
    }
    
    /**
     * Removes the first occurrence in initializers list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   newArray  new array tree containing initializers list.
     * @param   initializer   element to be removed from this list, if present.
     * @return  new array tree with modified initializers.
     */
    public NewArrayTree removeNewArrayInitializer(NewArrayTree newArray, ExpressionTree initializer) {
        return delegate.removeNewArrayInitializer(newArray, initializer);
    }
    
    /**
     * Removes the element at the specified position in initializers list.
     * Returns the modified new array tree.
     *
     * @param   newArray   new array tree containinginitializers list.
     * @param   index  the index of the element to be removed.
     * @return  new array tree with modified initializers.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public NewArrayTree removeNewArrayInitializer(NewArrayTree newArray, int index) {
        return delegate.removeNewArrayInitializer(newArray, index);
    }
    
    // NewClass
    /**
     * Appends specified element <tt>argument</tt> 
     * to the end of arguments list.
     *
     * @param  newClass     new class tree containing arguments list.
     * @param  argument     element to be appended to arguments list.
     * @return new class tree with modified arguments and type arguments.
     */
    public NewClassTree addNewClassArgument(NewClassTree newClass, ExpressionTree argument) {
        return delegate.addNewClassArgument(newClass, argument);
    }
    
    /**
     * Inserts the specified element <tt>argument</tt> 
     * at the specified position in type arguments list.
     *
     * @param  newClass   new class tree containing type arguments list.
     * @param  index  index at which the specified elements is to be inserted.
     * @param  argument   element to be inserted to arguments list.
     * @return new class tree with modified type arguments and type arguments.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public NewClassTree insertNewClassArgument(NewClassTree newClass, int index, ExpressionTree argument) {
        return delegate.insertNewClassArgument(newClass, index, argument);
    }
    
    /**
     * Removes the first occurrence in arguments of the specified elements.
     * If this list does not contain the element, it is unchanged.
     *
     * @param   newClass  new class tree containing type arguments list.
     * @param   argument   element to be removed from this list, if present.
     * @return  new class tree with modified arguments and type arguments.
     */
    public NewClassTree removeNewClassArgument(NewClassTree newClass, ExpressionTree argument) {
        return delegate.removeNewClassArgument(newClass, argument);
    }
    
    /**
     * Removes the element at the specified position in arguments
     * list. Returns the modified new class tree.
     *
     * @param   newClass   new class tree containing type arguments list.
     * @param   index  the index of the element to be removed.
     * @return  new class tree with modified arguments and type arguments.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public NewClassTree removeNewClassArgument(NewClassTree newClass, int index) {
        return delegate.removeNewClassArgument(newClass, index);
    }
    
    /**
     * Appends specified element <tt>typeArgument</tt> 
     * to the end of type arguments list.
     *
     * @param  newClass     new class tree containing arguments list.
     * @param  typeArgument element to be appended to type arguments list.
     * @return new class tree with modified arguments and type arguments.
     */
    public NewClassTree addNewClassTypeArgument(NewClassTree newClass, ExpressionTree typeArgument) {
        return delegate.addNewClassTypeArgument(newClass, typeArgument);
    }
    
    /**
     * Inserts the specified element <tt>typeArgument</tt> 
     * at the specified position in type arguments list.
     *
     * @param  newClass   new class tree containing type arguments list.
     * @param  index  index at which the specified elements is to be inserted.
     * @param  typeArgument element to be inserted to type arguments list.
     * @return new class tree with modified type arguments and type arguments.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public NewClassTree insertNewClassTypeArgument(NewClassTree newClass, int index, ExpressionTree typeArgument) {
        return delegate.insertNewClassTypeArgument(newClass, index, typeArgument);
    }
    
    /**
     * Removes the first occurrence in type arguments list of the specified elements.
     * If this list does not contain the element, it is unchanged.
     *
     * @param   newClass  new class tree containing type arguments list.
     * @param   typeArgument element to be removed from this list, if present.
     * @return  new class tree with modified arguments and type arguments.
     */
    public NewClassTree removeNewClassTypeArgument(NewClassTree newClass, ExpressionTree typeArgument) {
        return delegate.removeNewClassTypeArgument(newClass, typeArgument);
    }
    
    /**
     * Removes the element at the specified position in type arguments
     * list. Returns the modified new class tree.
     *
     * @param   newClass   new class tree containing type arguments list.
     * @param   index  the index of the element to be removed.
     * @return  new class tree with modified arguments and type arguments.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public NewClassTree removeNewClassTypeArgument(NewClassTree newClass, int index) {
        return delegate.removeNewClassTypeArgument(newClass, index);
    }

    // ParameterizedType
    /**
     * Appends specified element <tt>argument</tt> to the end of type arguments
     * list.
     *
     * @param  parameterizedType   parameterized type tree containing type arguments list.
     * @param  argument    element to be appended to type arguments list.
     * @return parameterized type tree with modified type arguments.
     */
    public ParameterizedTypeTree addParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, ExpressionTree argument) {
        return delegate.addParameterizedTypeTypeArgument(parameterizedType, argument);
    }
    
    /**
     * Inserts the specified element <tt>argument</tt> at the specified 
     * position in type arguments list.
     *
     * @param  parameterizedType   parameterized type tree containing type arguments list.
     * @param  index  index at which the specified element is to be inserted.
     * @param  argument   element to be inserted to type arguments list.
     * @return parameterized type tree with modified type arguments.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public ParameterizedTypeTree insertParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, int index, ExpressionTree argument) {
        return delegate.insertParameterizedTypeTypeArgument(parameterizedType, index, argument);
    }
    
    /**
     * Removes the first occurrence in type arguments list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   parameterizedType  parameterized type tree containing type arguments list.
     * @param   argument   element to be removed from this list, if present.
     * @return  parameterized type tree with modified type arguments.
     */
    public ParameterizedTypeTree removeParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, ExpressionTree argument) {
        return delegate.removeParameterizedTypeTypeArgument(parameterizedType, argument);
    }
    
    /**
     * Removes the element at the specified position in type arguments list.
     * Returns the modified parameterized type tree.
     *
     * @param   parameterizedType   parameterized type tree containing type arguments list.
     * @param   index  the index of the element to be removed.
     * @return  parameterized type tree with modified type arguments.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public ParameterizedTypeTree removeParameterizedTypeTypeArgument(ParameterizedTypeTree parameterizedType, int index) {
        return delegate.removeParameterizedTypeTypeArgument(parameterizedType, index);
    }

    // Switch
    /**
     * Appends specified element <tt>kejs</tt> to the end of cases
     * list.
     *
     * @param   swic    switch tree containing cases list.
     * @param   kejs    element to be appended to cases list.
     * @return  switch tree with modified cases.
     */
    public SwitchTree addSwitchCase(SwitchTree swic, CaseTree kejs) {
        return delegate.addSwitchCase(swic, kejs);
    }
    
    /**
     * Inserts the specified element <tt>kejs</tt> at the specified 
     * position in cases list.
     *
     * @param  swic   switch tree containing cases list.
     * @param  index  index at which the specified element is to be inserted.
     * @param  kejs   element to be inserted to cases list.
     * @return switch tree with modified cases.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public SwitchTree insertSwitchCase(SwitchTree swic, int index, CaseTree kejs) {
        return delegate.insertSwitchCase(swic, index, kejs);
    }
    
    /**
     * Removes the first occurrence in cases list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   swic  switch tree containing cases list.
     * @param   kejs   element to be removed from this list, if present.
     * @return  switch tree with modified cases.
     */
    public SwitchTree removeSwitchCase(SwitchTree swic, CaseTree kejs) {
        return delegate.removeSwitchCase(swic, kejs);
    }
    
    /**
     * Removes the element at the specified position in cases list.
     * Returns the modified switch tree.
     *
     * @param   swic   switch tree containing cases list.
     * @param   index  the index of the element to be removed.
     * @return  switch tree with modified cases.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public SwitchTree removeSwitchCase(SwitchTree swic, int index) {
        return delegate.removeSwitchCase(swic, index);
    }
    
    // Try
    /**
     * Appends specified element <tt>kec</tt> to the end of catches
     * list.
     *
     * @param   traj   try tree containing catches list.
     * @param   kec    element to be appended to catches list.
     * @return  try tree with modified catches.
     */
    public TryTree addTryCatch(TryTree traj, CatchTree kec) {
        return delegate.addTryCatch(traj, kec);
    }
    
    /**
     * Inserts the specified element <tt>kec</tt> at the specified 
     * position in catches list.
     *
     * @param  traj   try tree containing catches list.
     * @param  index  index at which the specified element is to be inserted.
     * @param  kec    element to be inserted to catches list.
     * @return try tree with modified catches.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public TryTree insertTryCatch(TryTree traj, int index, CatchTree kec) {
        return delegate.insertTryCatch(traj, index, kec);
    }
    
    /**
     * Removes the first occurrence in catches list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   traj  try tree containing catches list.
     * @param   kec   element to be removed from this list, if present.
     * @return  try tree with modified catches.
     */
    public TryTree removeTryCatch(TryTree traj, CatchTree kec) {
        return delegate.removeTryCatch(traj, kec);
    }
    
    /**
     * Removes the element at the specified position in catches list.
     * Returns the modified try tree.
     *
     * @param   traj   try tree containing catches list.
     * @param   index  the index of the element to be removed.
     * @return  try tree with modified catches.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public TryTree removeTryCatch(TryTree traj, int index) {
        return delegate.removeTryCatch(traj, index);
    }
            
    /**
     * Appends specified element <tt>bound</tt> to the end of bounds
     * list.
     *
     * @param   typeParameter     type parameter tree containing bounds list.
     * @param   bound   element to be appended to bounds list.
     * @return  type parameter tree with modified bounds.
     */
    public TypeParameterTree addTypeParameterBound(TypeParameterTree typeParameter, ExpressionTree bound) {
        return delegate.addTypeParameterBound(typeParameter, bound);
    }
    
    /**
     * Inserts the specified element <tt>bound</tt> at the specified 
     * position in bounds list.
     *
     * @param  typeParameter   type parameter tree containing bounds list.
     * @param  index  index at which the specified element is to be inserted.
     * @param  bound  element to be inserted to bounds list.
     * @return type parameter tree with modified bounds.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public TypeParameterTree insertTypeParameterBound(TypeParameterTree typeParameter, int index, ExpressionTree bound) {
        return delegate.insertTypeParameterBound(typeParameter, index, bound);
    }
    
    /**
     * Removes the first occurrence in bounds list of the specified 
     * element. If this list does not contain the element, it is
     * unchanged.
     *
     * @param   typeParameter  type parameter tree containing bounds list.
     * @param   bound   element to be removed from this list, if present.
     * @return  type parameter tree with modified bounds.
     */
    public TypeParameterTree removeTypeParameterBound(TypeParameterTree typeParameter, ExpressionTree bound) {
        return delegate.removeTypeParameterBound(typeParameter, bound);
    }
    
    /**
     * Removes the element at the specified position in bounds list.
     * Returns the modified type parameter tree.
     *
     * @param   typeParameter   type parameter tree containing bounds list.
     * @param   index  the index of the element to be removed.
     * @return  type parameter tree with modified bounds.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public TypeParameterTree removeTypeParameterBound(TypeParameterTree typeParameter, int index) {
        return delegate.removeTypeParameterBound(typeParameter, index);
    }
    
    /**
     * Replaces the original <tt>node</tt>'s label with new one provided in
     * <tt>aLabel</tt> argument. Throws <tt>IllegalArgumentException</tt> if
     * <tt>node</tt>'s kind is invalid. Valid <tt>node</tt>'s kinds are:<br>
     * BREAK, CLASS, CONTINUE, IDENTIFIER, LABELED_STATEMENT,
     * MEMBER_SELECT, METHOD, TYPE_PARAMETER, VARIABLE, MEMBER_REFERENCE (since 0.112).<p>
     *
     * Consider you want to change name of  method <tt>fooMet</tt> to
     * <tt>fooMethod</tt>:
     *
     * <pre>
     *   public void fooMet() throws java.io.IOException {
     *       ...
     *   }
     * </pre>
     *
     * You can get it e.g. with this code:
     * <pre>
     *   MethodTree footMet = <I>contains footMet tree</I>;
     *   MethodTree fooMethod = make.setLabel(fooMet, "fooMethod");
     *   workingCopy.rewrite(node, njuMethod);
     * </pre>
     *
     * This code will result to:
     * <pre>
     *   public void fooMethod() throws java.io.IOException {
     *       ...
     *   }
     * </pre>
     *
     * @param node    argument will be duplicated and its label replaced
     *                with <tt>aLabel</tt>
     * @param aLabel  represents new <tt>node</tt>'s name or other label
     * @throws java.lang.IllegalArgumentException  if the user provides
     *         illegal <tt>node</tt>'s kind, i.e. if the provided
     *         <tt>node</tt> does not contain any name or <tt>String</tt>.
     * @return  duplicated <tt>node</tt> with a new name
     */
    public <N extends Tree> N setLabel(final N node, final CharSequence aLabel)
            throws IllegalArgumentException {
        N result = asReplacementOf(setLabelImpl(node, aLabel), node, true);

        GeneratorUtilities gu = GeneratorUtilities.get(copy);

        gu.copyComments(node, result, true);
        gu.copyComments(node, result, false);

        return result;
    }

    private <N extends Tree> N setLabelImpl(final N node, final CharSequence aLabel)
            throws IllegalArgumentException
    {
        // todo (#pf): Shouldn't here be check that names are not the same?
        // i.e. node label == aLabel? -- every case branch has to check itself
        // This will improve performance, no change was done by API user.
        switch (node.getKind()) {
            case BREAK: {
                BreakTree t = (BreakTree) node;
                N clone = (N) Break(
                        aLabel
                        );
                return clone;
            }
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
            case RECORD: {
                ClassTree t = (ClassTree) node;
                // copy all the members, for constructor change their name
                // too!
                List<? extends Tree> members = t.getMembers();
                List<Tree> membersCopy = new ArrayList<Tree>();
                for (Tree member : members) {
                    if (member.getKind() == Kind.METHOD) {
                        MethodTree m = (MethodTree) member;
                        // there should be some better mechanism to detect
                        // that it is constructor, there are tree.sym.isConstr()
                        // at level of javac node.
                        if ("<init>".contentEquals(m.getName())) { // NOI18N
                            // ensure we will not do anything with syntetic
                            // constructor -- todo (#pf): one of strange
                            // hacks.
                            if (model.getPos(t) != model.getPos(m)) {
                                MethodTree a = setLabel(m, aLabel);
                                model.setPos(a, model.getPos(m));
                                membersCopy.add(a);
                            } else {
                                membersCopy.add(member);
                            }
                            continue;
                        }
                    }
                    membersCopy.add(member);
                }
                // and continue the same way as other cases
                N clone = (N) Class(
                        t.getModifiers(),
                        aLabel,
                        t.getTypeParameters(),
                        t.getExtendsClause(),
                        (List<ExpressionTree>) t.getImplementsClause(),
                        membersCopy);
                return clone;
            }
            case CONTINUE: {
                ContinueTree t = (ContinueTree) node;
                N clone = (N) Continue(aLabel);
                return clone;
            }
            case IDENTIFIER: {
                IdentifierTree t = (IdentifierTree) node;
                N clone = (N) Identifier(
                        aLabel
                        );
                return clone;
            }
            case LABELED_STATEMENT: {
                LabeledStatementTree t = (LabeledStatementTree) node;
                N clone = (N) LabeledStatement(
                        aLabel,
                        t.getStatement()
                        );
                return clone;
            }
            case MEMBER_SELECT: {
                MemberSelectTree t = (MemberSelectTree) node;
                N clone = (N) MemberSelect(
                        (ExpressionTree) t.getExpression(),
                        aLabel
                        );
                return clone;
            }
            case MEMBER_REFERENCE: {
                MemberReferenceTree t = (MemberReferenceTree) node;
                N clone = (N) MemberReference(
                        t.getMode(),
                        t.getQualifierExpression(),
                        aLabel,
                        t.getTypeArguments()
                        );
                return clone;
            }
            case METHOD: {
                MethodTree t = (MethodTree) node;
                N clone = (N) Method(
                        t.getModifiers(),
                        aLabel,
                        t.getReturnType(),
                        t.getTypeParameters(),
                        (List) t.getParameters(),
                        t.getThrows(),
                        t.getBody(),
                        (ExpressionTree) t.getDefaultValue()
                );
                return clone;
            }
            case TYPE_PARAMETER: {
                TypeParameterTree t = (TypeParameterTree) node;
                N clone = (N) TypeParameter(
                        aLabel,
                        (List<ExpressionTree>) t.getBounds()
                        );
                return clone;
            }
            case VARIABLE: {
                VariableTree t = (VariableTree) node;
                N clone = (N) Variable(
                        (ModifiersTree) t.getModifiers(),
                        aLabel,
                        (Tree) t.getType(),
                        (ExpressionTree) t.getInitializer()
                        );
                model.setPos(clone, model.getPos(t));
                return clone;
            }
        }
        // provided incorrect node's kind, no case branch was used
        throw new IllegalArgumentException("Invalid node's kind. Supported" +
                " kinds are BREAK, CLASS, CONTINUE, IDENTIFIER, LABELED_STATEMENT," +
                " MEMBER_SELECT, METHOD, TYPE_PARAMETER, VARIABLE");
    }


    /**
     * Replaces extends clause in class declaration. Consider you want to make 
     * <code>Matricale</code> class extending class <code>Yerba</code>.
     *
     * You have the class available:
     *
     * <pre>
     *   public class Matricale {
     *       ...
     *   }
     * </pre>
     *
     * Running following code:
     * <pre>
     *   TreeMaker make = workingCopy.getTreeMaker();
     *   ClassTree matricale = <i>contains Matricale class</i>;
     *   ClassTree modified = make.setExtends(matricale, make.Identifier("Yerba"));
     *   workingCopy.rewrite(matricale, modified);
     * </pre>
     *
     * will result to:
     *
     * <pre>
     *   public class Matricale extends Yerba {
     *       ....
     *   }
     * </pre>
     *
     * Note: It does not apply for interface declaration. For interfaces
     * declaration, use implements clause in <code>ClassTree</code> for
     * changed extends clause. It is a workaround allowing to extends more
     * interfaces.
     *
     * @param node     class where the extends clause will be replaced
     * @param extendz  new extends identifier or member select.
     * @return         node's copy with new extends clause
     */
    public ClassTree setExtends(final ClassTree node, final ExpressionTree extendz) {
        @SuppressWarnings("unchecked")
        ClassTree result = Class(
                node.getModifiers(),
                node.getSimpleName(),
                node.getTypeParameters(),
                extendz,
                (List<ExpressionTree>) node.getImplementsClause(), // bug
                node.getMembers()
        );
        return result;
    }
    
    /**
     * Replaces initializer in appropriate element. Allowed types for node
     * are <code>MethodTree</code> and <code>VariableTree</code>. Initial
     * value is available for variables except the parameters. Fields and
     * local variables can be passed to the method. In addition to, annotation
     * attribute represented by <code>MethodTree</code> is also valid value.
     *
     * Consider you have declaration:
     *
     * <pre>
     *   public static String cedron;
     * </pre>
     *
     * Running following code:
     * <pre>
     *   TreeMaker make = workingCopy.getTreeMaker();
     *   VariableTree cedron = <i>contains cedron field</i>;
     *   Literal initialValue = make.Literal("This is a cedron.");
     *   VariableTree modified = make.setInitialValue(cedron, literal);
     *   workingCopy.rewrite(matricale, modified);
     * </pre>
     *
     * will result to:
     *
     * <pre>
     *   public static String cedron = "This is a cedron.";
     * </pre>
     *
     * @param node         replace the initial value in node
     * @param initializer  new initial value
     * @throws java.lang.IllegalArgumentException  if the user provides
     *         illegal <code>node</code>'s kind, i.e. if the provided
     *         <code>node</code> is neither <code>MethodTree</code> nor
     *         <code>VariableTree</code>
     * @return  node's copy with new initializer
     */
    public <N extends Tree> N setInitialValue(final N node, ExpressionTree initializer) {
        switch (node.getKind()) {
            case VARIABLE: {
                VariableTree t = (VariableTree) node;
                @SuppressWarnings("unchecked")
                N clone = (N) Variable(
                    t.getModifiers(),
                    t.getName(),
                    t.getType(),
                    initializer
                );
                return clone;
            }
            case METHOD: {
                MethodTree t = (MethodTree) node;
                @SuppressWarnings("unchecked")
                N clone = (N) Method(
                    t.getModifiers(),
                    t.getName(),
                    t.getReturnType(),
                    t.getTypeParameters(),
                    t.getParameters(),
                    t.getThrows(),
                    t.getBody(),
                    initializer
                );
                return clone;
            }
            default:
                throw new IllegalArgumentException("Invalid kind " + node.getKind());
        }
    }
    
    //comment handling:
    /**Append a comment to the list of comments attached to a given tree.
     *
     * @param tree to which comment should added
     * @param comment to add
     * @param preceding true if preceding comments should be added, false if trailing comments should be added.
     *
     * @throws IllegalStateException if the method is called outside the runModificationTask
     */
    public void addComment(Tree tree, Comment comment, boolean preceding) throws IllegalStateException {
        insertComment(tree, comment, -1, preceding);
    }
    
    /**Insert a comment to the list of comments attached to a given tree (to a specified position).
     *
     * @param tree to which comment should added
     * @param comment to add
     * @param index -1 to add comment to the end of the list or index at which the comment should be added
     * @param preceding true if preceding comments should be added, false if trailing comments should be added.
     *
     * @throws IllegalStateException if the method is called outside the runModificationTask
     */
    public void insertComment(Tree tree, Comment comment, int index, boolean preceding) throws IllegalStateException {
        if (handler == null) {
            throw new IllegalStateException("Cannot modify comments outside runModificationTask.");
        }
        
        CommentSet set = handler.getComments(tree);
        
        if (set == null) {
            if (index != 0 && index != (-1))
                throw new IllegalArgumentException("Index out of bounds: " + index);
            
            handler.addComment(tree, comment);
            
            if (!preceding) {
                set = handler.getComments(tree);
                
                assert set != null;
                
                set.addTrailingComment(comment);
                set.getPrecedingComments().remove(comment);
            }
        } else {
            set.insertComment(preceding ? CommentSet.RelativePosition.PRECEDING : CommentSet.RelativePosition.TRAILING, comment, index);
        }
    }
    
    /**Remove a comment from the list of comments attached to a given tree.
     *
     * @param tree to which comment should added
     * @param index comment to remove
     *
     * @throws IllegalStateException if the method is called outside the runModificationTask
     */
    public void removeComment(Tree tree, int index, boolean preceding) throws IllegalStateException {
        if (handler == null) {
            throw new IllegalStateException("Cannot modify comments outside runModificationTask.");
        }
        
        CommentSet set = handler.getComments(tree);
        
        if (set == null) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        
        List<Comment> comments;
        
        if (preceding) {
            comments = set.getPrecedingComments();
        } else {
            comments = set.getTrailingComments();
        }
        
        if (comments.size() > index) {
            comments.remove(index);
        } else {
            throw new IllegalArgumentException("Index out of bounds, index=" + index + ", length=" + comments.size());
        }
    }
    
    /**
     * Marks the tree as new. This information serves as a hint to code generator, which may choose appropriate
     * formatting for the tree, or suppress comments which might get associated to the tree.
     * 
     * @param <T>
     * @param tree Tree instance
     * @return the tree itself
     */
    public <T extends Tree> T asNew(T tree) {
        if (handler == null) {
            throw new IllegalStateException("Cannot work with comments outside runModificationTask.");
        }
        if (tree == null) {
            throw new NullPointerException("tree");
        }
        copy.associateTree(tree, null, true);
        return tree;
    }
    
    /**
     * Marks a tree as a replacement of some old one. The hint may cause surrounding whitespace to be 
     * carried over to the new tree and comments to be attached to the same (similar) positions
     * as in the old tree. Prevous hints are removed.
     * 
     * @param <T>
     * @param treeNew the new tree instance
     * @param treeOld the old tree instance
     * @return the new tree instance unchanged
     * @see #asReplacementOf(com.sun.source.tree.Tree, com.sun.source.tree.Tree, boolean) 
     * @since 0.137
     */
    public <T extends Tree> T asReplacementOf(T treeNew, Tree treeOld) {
        return asReplacementOf(treeNew, treeOld, false);
    }
    
    /**
     * Marks a tree as a replacement of some old one. The hint may cause surrounding whitespace to be 
     * carried over to the new tree and comments to be attached to the same (similar) positions
     * as in the old tree. 
     * <p>
     * If 'defaultOnly' is true, the hint is only added if no previous hint exists. You generally want
     * to force the hint, in code manipulation operations. Bulk tree transformers should preserve existing
     * hints - the {@link TreeUtilities#translate} preserves existing relationships.
     * 
     * @param <T>
     * @param treeNew
     * @param treeOld
     * @param defaultOnly
     * @return a tree that corresponds to treeNew.
     * @since 0.137
     */
    public <T extends Tree> T asReplacementOf(T treeNew, Tree treeOld, boolean defaultOnly) {
        if (handler == null) {
            throw new IllegalStateException("Cannot work with comments outside runModificationTask.");
        }
        if (treeOld == null) {
            throw new NullPointerException("treeOld");
        }
        if (treeNew != null) {
            copy.associateTree(treeNew, treeOld, !defaultOnly);
        }
        return treeNew;
    }
    
    /**
     * Marks the tree as removed. The mark may affect whitespace and comment handling. For example, if the tree was 
     * rewritten by a different construct, and there's no direct replacement for the tree, the tree node should be marked
     * as removed, so that comments do not end up with the replacement. The caller is then responsible for preserving
     * comments or other whitespace in the removed node.
     * 
     * @param <T>
     * @param tree tree that has been removed.
     * @return the tree.
     * @since 0.137
     */
    public <T extends Tree> T asRemoved(T tree) {
        if (tree == null) {
            throw new NullPointerException("tree");
        }
        copy.associateTree(tree, null, true);
        return tree;
    }
    
    /**
     * Creates a new BlockTree for provided <tt>bodyText</tt>.
     * 
     * @param   method    figures out the scope for attribution.
     * @param   bodyText  text which will be used for method body creation.
     * @return  a new tree for <tt>bodyText</tt>.
     */
    public BlockTree createMethodBody(MethodTree method, String bodyText) {
        SourcePositions[] positions = new SourcePositions[1];
        final TreeUtilities treeUtils = copy.getTreeUtilities();
        StatementTree body = treeUtils.parseStatement(bodyText, positions);
        assert Tree.Kind.BLOCK == body.getKind() : "Not a statement block!";
        Scope scope = copy.getTrees().getScope(TreePath.getPath(copy.getCompilationUnit(), method));
        treeUtils.attributeTree(body, scope);
        mapComments((BlockTree) body, bodyText, copy, handler, positions[0]);
        new TreePosCleaner().scan(body, null);
        return (BlockTree) body;
    }

    /**
     * Creates a new BlockTree for provided <tt>bodyText</tt>.
     * 
     * @param   lambda    figures out the scope for attribution.
     * @param   bodyText  text which will be used for lambda body creation.
     * @return  a new tree for <tt>bodyText</tt>.
     * @since 2.19
     */
    public BlockTree createLambdaBody(LambdaExpressionTree lambda, String bodyText) {
        SourcePositions[] positions = new SourcePositions[1];
        final TreeUtilities treeUtils = copy.getTreeUtilities();
        StatementTree body = treeUtils.parseStatement(bodyText, positions);
        assert Tree.Kind.BLOCK == body.getKind() : "Not a statement block!";
        Scope scope = copy.getTrees().getScope(TreePath.getPath(copy.getCompilationUnit(), lambda));
        treeUtils.attributeTree(body, scope);
        mapComments((BlockTree) body, bodyText, copy, handler, positions[0]);
        new TreePosCleaner().scan(body, null);
        return (BlockTree) body;
    }

    /**
     * Creates a new ExpressionTree for provided <tt>bodyText</tt>.
     * 
     * @param   lambda    figures out the scope for attribution.
     * @param   bodyText  text which will be used for lambda body creation.
     * @return  a new tree for <tt>bodyText</tt>.
     * @since 2.54
     */
    public ExpressionTree createLambdaExpression(LambdaExpressionTree lambda, String bodyText) {
        SourcePositions[] positions = new SourcePositions[1];
        final TreeUtilities treeUtils = copy.getTreeUtilities();
        ExpressionTree body = treeUtils.parseExpression(bodyText, positions);
        Scope scope = copy.getTrees().getScope(TreePath.getPath(copy.getCompilationUnit(), lambda));
        treeUtils.attributeTree(body, scope);
//        mapComments((BlockTree) body, bodyText, copy, handler, positions[0]);
        new TreePosCleaner().scan(body, null);
        return (ExpressionTree) body;
    }

    /**
     * Creates a new MethodTree.
     *
     * @param modifiers the modifiers of this method.
     * @param name the name of the method.
     * @param returnType the return type for this method.
     * @param typeParameters the list of generic type parameters, or an empty list.
     * @param parameters the list of parameters, or an empty list.
     * @param throwsList the list of throws clauses, or an empty list.
     * @param bodyText the method's code block provided as a plain text
     * @param defaultValue the default value, used by annotation types.
     * @see com.sun.source.tree.MethodTree
     * 
     */
    public MethodTree Method(ModifiersTree modifiers,
                      CharSequence name,
                      Tree returnType,
                      List<? extends TypeParameterTree> typeParameters,
                      List<? extends VariableTree> parameters,
                      List<? extends ExpressionTree> throwsList,
                      String bodyText,
                      ExpressionTree defaultValue) 
    {
        SourcePositions[] positions = new SourcePositions[1];
        StatementTree body = bodyText != null ? copy.getTreeUtilities().parseStatement(bodyText, positions) : null;
        if (body != null) {
            assert Tree.Kind.BLOCK == body.getKind() : "Not a statement block!";
            mapComments((BlockTree) body, bodyText, copy, handler, positions[0]);
            new TreePosCleaner().scan(body, null);
        }
        return delegate.Method(modifiers, name, returnType, typeParameters, parameters, throwsList, (BlockTree) body, defaultValue);
    }
    
    private void mapComments(BlockTree block, String inputText, WorkingCopy copy, CommentHandler comments, SourcePositions positions) {
        if (copy.getFileObject() == null) {
            // prevent IllegalStateException thrown form AssignComments constructor below
            return;
        }
        TokenSequence<JavaTokenId> seq = TokenHierarchy.create(inputText, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        AssignComments ti = new AssignComments(copy, block, seq, positions);
        ti.scan(block, null);
        /*List<? extends StatementTree> trees = block.getStatements();
        SourcePositions pos = copy.getTrees().getSourcePositions();
        for (StatementTree statement : trees) {
            seq.move((int) pos.getStartPosition(null, statement));
            PositionEstimator.moveToSrcRelevant(seq, Direction.BACKWARD);
            int indent = NOPOS;
            while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
                switch (seq.token().id()) {
                    case LINE_COMMENT:
                        comments.addComment(statement, Comment.create(Style.LINE, NOPOS, NOPOS, indent, seq.token().toString()));
                        indent = 0;
                        break;
                    case BLOCK_COMMENT:
                        comments.addComment(statement, Comment.create(Style.BLOCK, NOPOS, NOPOS, indent, seq.token().toString()));
                        indent = NOPOS;
                        break;
                    case JAVADOC_COMMENT:
                        comments.addComment(statement, Comment.create(Style.JAVADOC, NOPOS, NOPOS, indent, seq.token().toString()));
                        indent = NOPOS;
                        break;
                    case WHITESPACE:
                        String tokenText = seq.token().toString();
                        comments.addComment(statement, Comment.create(Style.WHITESPACE, NOPOS, NOPOS, NOPOS, tokenText));
                        int newLinePos = tokenText.lastIndexOf('\n');
                        if (newLinePos < 0) {
                            if (indent >= 0)
                                indent += tokenText.length();
                        } else {
                            indent = tokenText.length() - newLinePos - 1;
                        }
                        break;
                }
            }
        }*/
    }

    private static class TreePosCleaner extends ErrorAwareTreeScanner<Void, Void> {

        @Override
        public Void scan(Tree tree, Void p) {
            if (tree != null) ((JCTree) tree).pos = PositionEstimator.NOPOS;
            return super.scan(tree, p);
        }

    }

    /**Creates the DocTree's HTML AttributeTree.
     * 
     * @param name attribute name
     * @param vkind attribute value kid
     * @param value attribute value
     * @return newly created AttributeTree
     * @since 0.124
     */
    public AttributeTree Attribute(CharSequence name, AttributeTree.ValueKind vkind, List<? extends DocTree> value) {
        return delegate.Attribute(name, vkind, value);
    }

    /**Creates the DocTree's AuthorTree.
     * 
     * @param name the content of the tree
     * @return newly created AuthorTree
     * @since 0.124
     */
    public AuthorTree Author(List<? extends DocTree> name) {
        return delegate.Author(name);
    }

    /**Creates the DocTree's HTML EntityTree.
     * 
     * @param name entity name/code
     * @return newly created EntityTree
     * @since 0.124
     */
    public EntityTree Entity(CharSequence name) {
        return delegate.Entity(name);
    }

    /**Creates the DocTree's DeprecatedTree.
     * 
     * @param text the deprecation message
     * @return newly created DeprecatedTree
     * @since 0.124
     */
    public DeprecatedTree Deprecated(List<? extends DocTree> text) {
        return delegate.Deprecated(text);
    }

    /**Creates a new javadoc comment.
     *
     * @param fullBody the entire body of the comment
     * @param tags the block tags of the comment (after the main body)
     * @return newly created DocCommentTree
     * @since 2.62
     */
    public DocCommentTree DocComment(List<? extends DocTree> fullBody, List<? extends DocTree> tags) {
        return delegate.DocComment(fullBody, tags);
    }

    /**Creates a new javadoc comment.
     * 
     * @param firstSentence the javadoc comment's first sentence
     * @param body the main body of the comment
     * @param tags the block tags of the comment (after the main body)
     * @return newly created DocCommentTree
     * @since 0.124
     */
    public DocCommentTree DocComment(List<? extends DocTree> firstSentence, List<? extends DocTree> body, List<? extends DocTree> tags) {
        return delegate.DocComment(firstSentence, body, tags);
    }

    /**Creates the DocTree's ParamTree.
     * 
     * @param isTypeParameter true if and only if the parameter is a type parameter
     * @param name the name of the parameter
     * @param description the description of the parameter
     * @return newly created ParamTree
     * @since 0.124
     */
    public ParamTree Param(boolean isTypeParameter, com.sun.source.doctree.IdentifierTree name, List<? extends DocTree> description) {
        return delegate.Param(isTypeParameter, name, description);
    }

    /**Creates the DocTree's LinkTree.
     * 
     * @param ref the reference linked by the {@code @link}
     * @param label the optional description of the reference
     * @return newly created LinkTree
     * @since 0.124
     */
    public LinkTree Link(ReferenceTree ref, List<? extends DocTree> label) {
        return delegate.Link(ref, label);
    }

    /**Creates the DocTree's LiteralTree.
     * 
     * @param text the literal content
     * @since 0.124
     * @return newly created LiteralTree
     */
    public com.sun.source.doctree.LiteralTree DocLiteral(TextTree text) {
        return delegate.Literal(text);
    }

    /**Creates the DocTree's ReturnTree.
     * 
     * @param description the description of the method's return
     * @return newly created ReturnTree
     * @since 0.124
     */
    public com.sun.source.doctree.ReturnTree DocReturn(List<? extends DocTree> description) {
        return delegate.Return(description);
    }

    /**Creates the DocTree's ReferenceTree.
     * 
     * @param qualExpr the type to to which the reference is being made (the part of the reference before {@code '#'})
     * @param member the member to which the reference is being made (the part of the reference after {@code '#'})
     * @param paramTypes optional parameter type of the methods to which this reference is being made
     * @return newly created ReferenceTree
     * @since 0.124
     */
    public ReferenceTree Reference(@NullAllowed ExpressionTree qualExpr, @NullAllowed CharSequence member, @NullAllowed List<? extends Tree> paramTypes) {
        return delegate.Reference(qualExpr, member, paramTypes);
    }

    /**Creates the DocTree's SeeTree.
     * 
     * @param reference to an element the tag refers to
     * @return newly created SeeTree
     * @since 0.124
     */
    public SeeTree See(List<? extends DocTree> reference) {
        return delegate.See(reference);
    }

    /**Creates the DocTree's SerialTree.
     * 
     * @param description the description of the field
     * @return newly created SerialTree
     * @since 0.124
     */
    public SerialTree Serial(List<? extends DocTree> description) {
        return delegate.Serial(description);
    }

    /**Creates the DocTree's SerialDataTree.
     * 
     * @param description the description of the serial data
     * @return newly created SerialDataTree
     * @since 0.124
     */
    public SerialDataTree SerialData(List<? extends DocTree> description) {
        return delegate.SerialData(description);
    }

    /**Creates the DocTree's SerialFieldTree.
     * 
     * @param name the name of the field
     * @param type the type of the field
     * @param description the description of the field
     * @return newly created SerialFieldTree
     * @since 0.124
     */
    public SerialFieldTree SerialField(com.sun.source.doctree.IdentifierTree name, ReferenceTree type, List<? extends DocTree> description) {
        return delegate.SerialField(name, type, description);
    }

    /**Creates the DocTree's SinceTree.
     * 
     * @param text the content of the tag
     * @return newly created SinceTree
     * @since 0.124
     */
    public SinceTree Since(List<? extends DocTree> text) {
        return delegate.Since(text);
    }

    /**Creates the DocTree's HTML StartElementTree.
     * 
     * @param name name of the element
     * @param attrs elements attributes
     * @param selfClosing if the element is also the closing element (e.g. {@code <br/>}).
     * @return newly created StartElementTree
     * @since 0.124
     */
    public StartElementTree StartElement(CharSequence name, List<? extends DocTree> attrs, boolean selfClosing) {
        return delegate.StartElement(name, attrs, selfClosing);
    }

    /**Creates the DocTree's TextTree.
     * 
     * @param text the text
     * @return newly created TextTree
     * @since 0.124
     */
    public TextTree Text(String text) {
        return delegate.Text(text);
    }

    /**Creates the DocTree's ThrowsTree that will produce @throws.
     * 
     * @param name reference to the documented exception
     * @param description the description of the thrown exception
     * @return newly created ThrowsTree
     * @since 0.124
     */
    public ThrowsTree Throws(ReferenceTree name, List<? extends DocTree> description) {
        return delegate.Throws(name, description);
    }

    /**Creates the DocTree's UnknownBlockTagTree.
     * 
     * @param name the tag's name
     * @param content the tag's content
     * @return newly created UnknownBlockTagTree
     * @since 0.124
     */
    public UnknownBlockTagTree UnknownBlockTag(CharSequence name, List<? extends DocTree> content) {
        return delegate.UnknownBlockTag(name, content);
    }

    /**Creates the DocTree's UnknownInlineTagTree.
     * 
     * @param name the tag's name
     * @param content the tag's content
     * @return newly created UnknownInlineTagTree
     * @since 0.124
     */
    public UnknownInlineTagTree UnknownInlineTag(CharSequence name, List<? extends DocTree> content) {
        return delegate.UnknownInlineTag(name, content);
    }

    /**Creates the DocTree's VersionTree.
     * 
     * @param text the version's text
     * @return newly created VersionTree
     * @since 0.124
     */
    public VersionTree Version(List<? extends DocTree> text) {
        return delegate.Version(text);
    }

    /**Creates the DocTree's ValueTree.
     * 
     * @param ref the value's reference
     * @return newly created ValueTree
     * @since 0.124
     */
    public ValueTree Value(ReferenceTree ref) {
        return delegate.Value(ref);
    }

    /**Creates the DocTree's LiteralTree for a verbatim code.
     * 
     * @param text the text that should be wrapped by {@code @code}
     * @return newly created LiteralTree
     * @since 0.124
     */
    public com.sun.source.doctree.LiteralTree Code(TextTree text) {
        return delegate.Code(text);
    }

    /**Creates the DocTree's HTML CommentTree.
     * 
     * @param text the text that should be wrapped as HTML comment
     * @return newly created CommentTree
     * @since 0.124
     */
    public CommentTree Comment(String text) {
        return delegate.Comment(text);
    }

    /**Creates the DocTree's DocRootTree.
     * 
     * @return newly created DocRootTree
     * @since 0.124
     */
    public DocRootTree DocRoot() {
        return delegate.DocRoot();
    }

    /**Creates the DocTree's HTML EndElementTree.
     * 
     * @param name name of the element to be closed
     * @return newly created EndElementTree
     * @since 0.124
     */
    public EndElementTree EndElement(CharSequence name) {
        return delegate.EndElement(name);
    }

    /**Creates the DocTree's ThrowsTree that will produce @exception.
     * 
     * @param name reference to the documented exception
     * @param description the description of the thrown exception
     * @return newly created Exception
     * @since 0.124
     */
    public ThrowsTree Exception(ReferenceTree name, List<? extends DocTree> description) {
        return delegate.Exception(name, description);
    }

    /**Creates the DocTree's IdentifierTree.
     * 
     * @param name the identifier name
     * @return newly created IdentifierTree
     * @since 0.124
     */
    public com.sun.source.doctree.IdentifierTree DocIdentifier(CharSequence name) {
        return delegate.DocIdentifier(name);
    }

    /**Creates the DocTree's InheritDocTree.
     * 
     * @return newly created InheritDocTree
     * @since 0.124
     */
    public InheritDocTree InheritDoc() {
        return delegate.InheritDoc();
    }

    /**Creates the DocTree's LinkTree for {@code @linkplain}.
     * 
     * @param ref the reference linked by the {@code @link}
     * @param label the optional description of the reference
     * @return newly created LinkTree
     * @since 0.124
     */
    public LinkTree LinkPlain(ReferenceTree ref, List<? extends DocTree> label) {
        return delegate.LinkPlain(ref, label);
    }

    /**
     * Appends specified element <tt>parameter</tt>
     * to the end of parameters list.
     *
     * @param  method        lambda expression tree containing parameters list.
     * @param  parameter     element to be appended to parameters list.
     * @return lambda expression tree with modified parameters.
     * @since 0.112
     */
    public LambdaExpressionTree addLambdaParameter(LambdaExpressionTree method, VariableTree parameter) {
        return delegate.addLambdaParameter(method, parameter);
    }
    
    /**
     * Inserts the specified element <tt>parameter</tt> 
     * at the specified position in parameters list.
     *
     * @param  method lambda expression tree containing parameters list.
     * @param  index  index at which the specified elements is to be inserted.
     * @param  parameter   element to be inserted to parameters list.
     * @return lambda expression  tree with modified parameters.
     *
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     * @since 0.112
     */
    public LambdaExpressionTree insertLambdaParameter(LambdaExpressionTree method, int index, VariableTree parameter) {
        return delegate.insertLambdaParameter(method, index, parameter);
    }
    
    /**
     * Removes the first occurrence in parameters list of the specified 
     * elements. If this list do not contain the element, it is
     * unchanged.
     *
     * @param   method lambda expression tree containing parameters list.
     * @param   parameter   element to be removed from this list, if present.
     * @return  lambda expression tree with modified parameters and type parameters.
     * @since 0.112
     */
    public LambdaExpressionTree removeLambdaParameter(LambdaExpressionTree method, VariableTree parameter) {
        return delegate.removeLambdaParameter(method, parameter);
    }
    
    /**
     * Removes the element at the specified position in parameters list.
     * Returns the modified lambda expression tree.
     *
     * @param   method lambda expression tree containing parameters list.
     * @param   index  the index of the element to be removed.
     * @return  method tree with modified parameters.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     * @since 0.112
     */
    public LambdaExpressionTree removeLambdaParameter(LambdaExpressionTree method, int index) {
        return delegate.removeLambdaParameter(method, index);
    }
    
    /**Creates a new lambda expression as a copy of the given lambda expression
     * with the specified body.
     * 
     * @param method the source lambda expression
     * @param newBody the new body
     * @return new lambda expression tree with the new body
     * @since 0.112
     */
    public LambdaExpressionTree setLambdaBody(LambdaExpressionTree method, Tree newBody) {
        return delegate.setLambdaBody(method, newBody);
    }

    /**Creates a new string template expression from the given parameters.
     *
     * @param processor the processor of the string template
     * @param fragments the template fragments
     * @param expressions the template expressions
     * @return the string template instance
     * @since 2.65
     */
    public StringTemplateTree StringTemplate(ExpressionTree processor, List<String> fragments, List<? extends ExpressionTree> expressions) {
        return delegate.StringTemplate(processor, fragments, expressions);
    }

}
