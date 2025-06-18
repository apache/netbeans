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

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Scope.StarImportScope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.text.Document;
import javax.tools.JavaFileObject;

import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.guards.DocumentGuards;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.modules.java.source.GeneratorUtilitiesAccessor;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.parsing.AbstractSourceFileObject;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.ParsingUtils;
import org.netbeans.modules.java.source.query.CommentSet.RelativePosition;
import org.netbeans.modules.java.source.save.DiffContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/**
 *
 * @author Jan Lahoda, Dusan Balek
 * @since 0.20
 */
public final class GeneratorUtilities {

    private final WorkingCopy copy;

    private  GeneratorUtilities(WorkingCopy copy) {
        this.copy = copy;
    }

    /**
     * Returns the instance of this class
     *
     * @param copy
     * @return the {@link GeneratorUtilities} instance
     * @since 0.20
     */
    public static GeneratorUtilities get(WorkingCopy copy) {
        return new GeneratorUtilities(copy);
    }

    /**
     * Create a new CompilationUnitTree from a template.
     *
     * @param sourceRoot a source root under which the new file is created
     * @param path a relative path to file separated by '/'
     * @param kind the kind of Element to use for the template, can be null or
     * CLASS, INTERFACE, ANNOTATION_TYPE, ENUM, PACKAGE
     * @return new CompilationUnitTree created from a template
     * @throws IOException when an exception occurs while creating the template
     * @since 0.101
     */
    public CompilationUnitTree createFromTemplate(FileObject sourceRoot, String path, ElementKind kind) throws IOException {
        String[] nameComponent = FileObjects.getFolderAndBaseName(path, '/');
        JavaFileObject sourceFile = FileObjects.templateFileObject(sourceRoot, nameComponent[0], nameComponent[1]);
        FileObject template = FileUtil.getConfigFile(copy.template(kind));
        FileObject targetFile = copy.doCreateFromTemplate(template, sourceFile);
        CompilationUnitTree templateCUT = ParsingUtils.parseArbitrarySource(copy.impl.getJavacTask(), FileObjects.sourceFileObject(targetFile, targetFile.getParent()));
        CompilationUnitTree importComments = GeneratorUtilities.get(copy).importComments(templateCUT, templateCUT);
        CompilationUnitTree result = copy.getTreeMaker().CompilationUnit(importComments.getPackageAnnotations(),
                sourceRoot,
                path,
                importComments.getImports(),
                importComments.getTypeDecls());
        return result;
    }

    /**
     * Inserts a member to a class. Using the rules specified in the {@link CodeStyle}
     * it finds the proper place for the member and calls {@link TreeMaker#insertClassMember}
     *
     * @param clazz the class to insert the member to
     * @param member the member to add
     * @return the modified class
     * @since 0.20
     */
    public ClassTree insertClassMember(ClassTree clazz, Tree member) {
        assert clazz != null && member != null;
        Document doc = null;
        try {
            doc = copy.getDocument();
            if (doc == null) {
                doc = copy.getSnapshot().getSource().getDocument(true);
            }
        } catch (IOException ioe) {}
        CodeStyle codeStyle = DiffContext.getCodeStyle(copy);
        ClassMemberComparator comparator = new ClassMemberComparator(codeStyle);
        SourcePositions sp = copy.getTrees().getSourcePositions();
        TreeUtilities utils = copy.getTreeUtilities();
        CompilationUnitTree compilationUnit = copy.getCompilationUnit();
        Tree lastMember = null;
        int idx = -1;
        int gsidx = -1;
        String[] gsnames = codeStyle.keepGettersAndSettersTogether() ? correspondingGSNames(member) : null;
        int i = 0;
        
        // minIndex is the latest (textually) depended-on symbol +1
        int minIndex = -1;
        // maxIndex is the earliest symbol which depend on the new member
        int maxIndex = Integer.MAX_VALUE;
        
        FieldRefVisitor v = null;
        if (codeStyle.computeMemberDependencies()) {
            if (member.getKind() == Tree.Kind.VARIABLE) {
                // avoid costly computation for trivial cases - without initializer and
                VariableTree vt = (VariableTree)member;
                v = new FieldRefVisitor(clazz, vt.getModifiers().getFlags().contains(Modifier.STATIC));
                v.registerTreeName(member, vt.getName());
            }

            if (member.getKind() == Tree.Kind.BLOCK) {
                v = new FieldRefVisitor(clazz, ((BlockTree)member).isStatic());
            }
            
            if (v != null) {
                try {

                v.collectNames = true;
                TreePath classTP = new TreePath(new TreePath(compilationUnit), clazz);
                v.scan(classTP, null);

                TreePath memberPath = new TreePath(classTP, member);
                v.collectNames = false;
                v.scan(memberPath, null);

                Collection<Name> deps = v.dependencies.get(member);
                if (deps != null) {
                    for (Name n : v.dependencies.get(member)) {
                        Tree t = v.namedTrees.get(n);
                        if (t == null) {
                            continue;
                        }
                        minIndex = Math.max(minIndex, 1 + clazz.getMembers().indexOf(t));
                    }
                }
                for (Tree t : v.revDependencies) {
                    maxIndex = Math.min(maxIndex, clazz.getMembers().indexOf(t));
                }

                if (minIndex > maxIndex) {
                    // God save us, there's a reference cycle probably
                    minIndex = maxIndex = -1;
                }
                } catch (RuntimeException ex) {
                    Exceptions.printStackTrace(ex);
                    throw ex;
                }
            }
        }
        
        
        for (Tree tree : clazz.getMembers()) {
            if (!utils.isSynthetic(compilationUnit, tree)) {
                if (gsnames != null && gsidx < 0) {
                    for (String name : gsnames) {
                        if (name.equals(name(tree))) {
                            if (isSetter(tree)) {
                                gsidx = codeStyle.sortMembersInGroupsAlphabetically() ? i : i + 1;
                            } else if (isGetter(tree) || isBooleanGetter(tree)) {
                                gsidx = i + 1;
                            }
                        }
                    }
                }
                if (idx < 0 && (codeStyle.getClassMemberInsertionPoint() == CodeStyle.InsertionPoint.FIRST_IN_CATEGORY && comparator.compare(member, tree) <= 0
                        || comparator.compare(member, tree) < 0)) {
                    DocumentGuards guards = LineDocumentUtils.as(doc, DocumentGuards.class);
                    if (doc == null || guards == null) {
                        idx = i;
                        continue;
                    }
                    int pos = (int)(lastMember != null ? sp.getEndPosition(compilationUnit, lastMember) : sp.getStartPosition( compilationUnit,clazz));
                    pos = guards.adjustPosition(pos, true);
                    long treePos = sp.getStartPosition(compilationUnit, tree);
                    if (treePos < 0 || pos <= treePos) {
                        idx = i;
                    }
                }
            }
            i++;
            lastMember = tree;
        }
        if (idx < 0) {
            idx = i;
        }
        idx = gsidx < 0 ? idx : gsidx;
        
        // obey reference rules -- should there be an option for this ??
        if (minIndex >= 0) {
            idx = Math.max(minIndex, idx);
        }
        if (maxIndex < Integer.MAX_VALUE) {
            idx = Math.min(maxIndex, idx);
        }
        return copy.getTreeMaker().insertClassMember(clazz, idx, member);
    }

    /**
     * Inserts members to a class. Using the rules specified in the {@link CodeStyle}
     * it finds the proper place for each of the members and calls {@link TreeMaker#insertClassMember}
     *
     * @param clazz the class to insert the members to
     * @param members the members to insert
     * @param offset the caret location to use for {@code CodeStyle.InsertionPoint.CARET_LOCATION}
     * @return the modified class
     * @since 2.9
     */    
    public ClassTree insertClassMembers(ClassTree clazz, List<? extends Tree> members, int offset) {
        if (members.isEmpty()) {
            return clazz;
        }
        CodeStyle codeStyle = DiffContext.getCodeStyle(copy);
        if (offset < 0 || codeStyle.getClassMemberInsertionPoint() != CodeStyle.InsertionPoint.CARET_LOCATION) {
            return GeneratorUtilities.get(copy).insertClassMembers(clazz, members);
        }
        int index = 0;
        SourcePositions sp = copy.getTrees().getSourcePositions();
        Document doc = null;
        try {
            doc = copy.getDocument();
            if (doc == null) {
                doc = copy.getSnapshot().getSource().getDocument(true);
            }
        } catch (IOException ioe) {}
        Tree lastMember = null;
        Tree nextMember = null;
        for (Tree tree : clazz.getMembers()) {
            if (offset <= sp.getStartPosition(copy.getCompilationUnit(), tree)) {
                DocumentGuards guards = LineDocumentUtils.as(doc, DocumentGuards.class);
                if (doc == null || guards == null) {
                    nextMember = tree;
                    break;
                }
                int pos = (int)(lastMember != null ? sp.getEndPosition(copy.getCompilationUnit(), lastMember) : sp.getStartPosition(copy.getCompilationUnit(), clazz));
                pos = guards.adjustPosition(pos, true);
                if (pos <= sp.getStartPosition(copy.getCompilationUnit(), tree)) {
                    nextMember = tree;
                    break;
                }
            }
            index++;
            lastMember = tree;
        }
        if (lastMember != null) {
            // do not move the comments tied to last member in guarded block:
            moveCommentsAfterOffset(copy, lastMember, members.get(0), offset, doc);
        }
        if (nextMember != null) {
            moveCommentsBeforeOffset(copy, nextMember, members.get(members.size() - 1), offset, doc);
        }
        TreeMaker tm = copy.getTreeMaker();
        ClassTree newClazz = clazz;
        for (int i = members.size() - 1; i >= 0; i--) {
            newClazz = tm.insertClassMember(newClazz, index, members.get(i));
        }
        return newClazz;
    }
    
    /**
     * Inserts a member to a class. Using the rules specified in the {@link CodeStyle}
     * it finds the proper place for the member and calls {@link TreeMaker#insertClassMember}
     *
     * @param clazz the class to insert the member to
     * @param member the member to add
     * @param offset the caret location to use for {@code CodeStyle.InsertionPoint.CARET_LOCATION}
     * @return the modified class
     * @since 2.9
     */
    public ClassTree insertClassMember(ClassTree clazz, Tree member, int offset) {
        return insertClassMembers(clazz, Collections.singletonList(member), offset);
    }
    
    /**
     * Reparents comments that follow `from' tree and would be separated from that tree by insertion to `offset' position.
     * The comments are removed from the original tree, and attached to the `to' inserted tree.
     * @param wc the working copy
     * @param from the current owner of the comments
     * @param to the generated code
     * @param offset the offset where the new code will be inserted
     * @param doc document instance or {@code null}
     * @return 
     */
    private void moveCommentsAfterOffset(WorkingCopy wc, Tree from, Tree to, int offset, Document doc) {
        List<Comment> toMove = new LinkedList<>();
        int idx = 0;
        int firstToRemove = -1;
        for (Comment comment : wc.getTreeUtilities().getComments(from, false)) {
            if (comment.endPos() <= offset) {
                // not affected by insertion
                idx++;
                continue;
            }
            DocumentGuards guards = LineDocumentUtils.as(doc, DocumentGuards.class);
            if (guards != null) {
                int epAfterBlock = guards.adjustPosition(comment.endPos(), true);
                // comment that ends exactly at the GB boundary cannot be really
                // reassigned from the previous member.
                if (epAfterBlock >= comment.endPos()) {
                    // set new offset, after the guarded block
                    idx++;
                    continue;
                }
            }
            toMove.add(comment);
            if (firstToRemove == -1) {
                firstToRemove = idx;
            }
            idx++;
        }
        if (toMove.isEmpty()) {
            return;
        }
        doMoveComments(wc, from, to, offset, toMove, firstToRemove, idx);
    }
    
    private static void doMoveComments(WorkingCopy wc, Tree from,  Tree to, int offset, List<Comment> comments, int fromIdx, int toIdx) {
        if (comments.isEmpty()) {
            return;
        }
        TreeMaker tm = wc.getTreeMaker();
        Tree tree = from;
        switch (from.getKind()) {
            case METHOD:
                tree = tm.setLabel(from, ((MethodTree)from).getName());
                break;
            case VARIABLE:
                tree = tm.setLabel(from, ((VariableTree)from).getName());
                break;
            case BLOCK:
                tree = tm.Block(((BlockTree)from).getStatements(), ((BlockTree)from).isStatic());
                GeneratorUtilities gu = GeneratorUtilities.get(wc);
                gu.copyComments(from, tree, true);
                gu.copyComments(from, tree, false);
                break;
        }
        boolean before = (int)wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), from) >= offset;
        if (fromIdx >=0 && toIdx >= 0 && toIdx - fromIdx > 0) {
            for (int i = toIdx - 1; i >= fromIdx; i--) {
                tm.removeComment(tree, i, before);
            }
        }
        wc.rewrite(from, tree);
        for (Comment comment : comments) {
            tm.addComment(to, comment, comment.pos() <= offset);
        }
    }
    
    private static void moveCommentsBeforeOffset(WorkingCopy wc, Tree from, Tree to, int offset, Document doc) {
        List<Comment> toMove = new LinkedList<>();
        int idx = 0;
        for (Comment comment : wc.getTreeUtilities().getComments(from, true)) {
            if (comment.pos() >= offset || comment.endPos() > offset) {
                break;
            }
            
            DocumentGuards guards = LineDocumentUtils.as(doc, DocumentGuards.class);
            if (guards != null) {
                int epAfterBlock = guards.adjustPosition(comment.pos(), true);
                // comment that ends exactly at the GB boundary cannot be really
                // reassigned from the previous member.
                if (epAfterBlock >= comment.endPos()) {
                    // set new offset, after the guarded block
                    break;
                }
            }
            toMove.add(comment);
            idx++;
        }
        if (toMove.size() > 0) {
            doMoveComments(wc, from, to, offset, toMove, 0, idx);
        }
    }
    
    /**
     * Visitor which collects references to class' members. It operates in two modes:
     * if 'collectNames' is set, it collects field names from the class definition. If
     * 'insertedName' is not null, it also collects references to that name in 
     * 'revDependencies'.
     * <p>
     * For the secondPass, set 'collectNames' to false: the visitor will collect
     * dependencies of the scanned node into 'dependencies'. After 2 passes,
     * the revDependencies and dependencies can be used to determine partial order
     * between existing members and the newly inserted one.
     */
    private class FieldRefVisitor extends ErrorAwareTreePathScanner {
        private final ClassTree clazz;
        // collects field names; serves to identify unqualified indentifiers
        // which might refer to field names.
        private Set<Name>     fieldNames = new HashSet<>();
        // controls class members, which are interesting for us
        private boolean       staticFields;
        private boolean       collectNames;
        private Name          insertedName;
        
        private Collection<Tree>    revDependencies = new ArrayList<>();
        private Map<Tree, Collection<Name>> dependencies = new HashMap<>();
        private Map<Name, Tree> namedTrees = new HashMap<>();
        private Tree            member;

        public FieldRefVisitor(ClassTree clazz, boolean stat) {
            this.clazz = clazz;
            this.staticFields = stat;
        }
        
        void registerTreeName(Tree t, Name n) {
            fieldNames.add(n);
            namedTrees.put(n, t);
            insertedName = n;
        }
        
        private void addDependency(Name n) {
            Collection<Name> deps = dependencies.get(member);
            if (deps == null) {
                deps = new ArrayList<>(3);
                dependencies.put(member, deps);
            }
            deps.add(n);
        }

        // the following Nodes would just confuse the Identifier scanner:
        @Override public Object visitAnnotatedType(AnnotatedTypeTree node, Object p) { return null; }
        @Override public Object visitTypeParameter(TypeParameterTree node, Object p) { return null; }
        @Override public Object visitArrayType(ArrayTypeTree node, Object p) { return null; }
        @Override public Object visitInstanceOf(InstanceOfTree node, Object p) { return null; }
        @Override public Object visitBreak(BreakTree node, Object p) { return null; }
        
        @Override public Object visitTypeCast(TypeCastTree node, Object p) {
            return scan(node.getExpression(), p);
        }

        @Override
        public Object visitCase(CaseTree node, Object p) {
            return scan(node.getStatements(), p);
        }

        @Override
        public Object visitLabeledStatement(LabeledStatementTree node, Object p) {
            return scan(node.getStatement(), p);
        }

        @Override public Object visitAnnotation(AnnotationTree node, Object p) { return null; }
        @Override public Object visitParameterizedType(ParameterizedTypeTree node, Object p) { return null; }

        @Override
        public Object visitNewArray(NewArrayTree node, Object p) {
            scan(node.getDimensions(), p);
            return scan(node.getInitializers(), p);
        }

        @Override
        public Object visitNewClass(NewClassTree node, Object p) {
            return scan(node.getArguments(), p);
        }
        
        @Override
        public Object visitIdentifier(IdentifierTree node, Object p) {
            if (!fieldNames.contains(node.getName())) {
                return null;
            }
            // TODO do not track dependencies for method refs
            boolean ok = false;
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            
            switch (parent.getKind()) {
                case MEMBER_SELECT:
                    // dependency is only introduced by dereferencing the identifier.
                    ok = ((MemberSelectTree)parent).getExpression() != node;
                    break;
                case ASSIGNMENT:
                    ok = ((AssignmentTree)parent).getVariable() == node;
                    break;
            }
            
            if (ok) {
                return null;
            }

            if (!ok) {
                if (collectNames) {
                    if (node.getName().equals(insertedName)) {
                        revDependencies.add(member);
                    }
                } else {
                    addDependency(node.getName());
                }
            }
            
            return null;
        }

        // TODO: handle assignment and compound assignment expressions: L-value references are legal.
        @Override
        public Object visitMemberSelect(MemberSelectTree node, Object p) {
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            if (fieldNames.contains(node.getIdentifier())) {
                // NOTE: because of JLS 8.3.3, forward reference which is NOT a simple name (even this.id, or MyClassname.this.id !!)
                // is NOT illegal, so I will not count the appearance as a dependency
            }
            Object o = super.visitMemberSelect(node, p);
            return o;
        }

        @Override
        public Object visitVariable(VariableTree node, Object p) {
            // the other group of fields is not interesting for us.
            if (node.getModifiers().getFlags().contains(Modifier.STATIC) != staticFields) {
                return null;
            }
            if (collectNames) {
                fieldNames.add(node.getName());
                namedTrees.put(node.getName(), node);
            }
            // ignore type
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            Tree.Kind k = parent.getKind();
            if (k == Tree.Kind.CLASS || k == Tree.Kind.INTERFACE || k == Tree.Kind.ENUM) {
                member = node;
            }
            Object o = scan(node.getInitializer(), p);
            return o;
        }

        // the block is either an initializer or 
        @Override public Object visitBlock(BlockTree node, Object p) { 
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            Tree.Kind k = parent.getKind();
            if (k == Tree.Kind.CLASS || k == Tree.Kind.INTERFACE || k == Tree.Kind.ENUM) {
                member = node;
                return super.visitBlock(node, p);
            }
            return p; 
        }

        // blocks (= initializers), methods (incl.ctors) and inner classes (incl. anon classes in expressions)
        // are not important
        @Override public Object visitMethod(MethodTree node, Object p) { return p; }
        @Override public Object visitClass(ClassTree node, Object p) { 
            if (node == clazz) {
                return super.visitClass(node, p);
            }
            return p; 
        }
        
    }
    
    /**
     * Inserts members to a class. Using the rules specified in the {@link CodeStyle}
     * it finds the proper place for each of the members and calls {@link TreeMaker#insertClassMember}
     *
     * @param clazz the class to insert the members to
     * @param members the members to insert
     * @return the modified class
     * @since 0.20
     */
    public ClassTree insertClassMembers(ClassTree clazz, Iterable<? extends Tree> members) {
        assert members != null;
        for (Tree member : members)
            clazz = insertClassMember(clazz, member);
        return clazz;
    }

    /**
     * Creates implementations of the all abstract methods within a class.
     *
     * @param clazz the class to create the implementations within
     * @return the abstract method implementations
     * @since 0.20
     */
    public List<? extends MethodTree> createAllAbstractMethodImplementations(TypeElement clazz) {
        return createAbstractMethodImplementations(clazz, copy.getElementUtilities().findUnimplementedMethods(clazz));
    }

    /**
     * Creates implementations of abstract methods within a class.
     *
     * @param clazz the class to create the implementations within
     * @param methods the abstract methods to implement
     * @return the abstract method implementations
     * @since 0.20
     */
    public List<? extends MethodTree> createAbstractMethodImplementations(TypeElement clazz, Iterable<? extends ExecutableElement> methods) {
        assert methods != null;
        List<MethodTree> ret = new ArrayList<MethodTree>();
        for(ExecutableElement method : methods)
            ret.add(createAbstractMethodImplementation(clazz, method));
        
        tagFirst(ret);
        return ret;
    }

    /**
     * Creates an implementation of an abstract method within a class.
     *
     * @param clazz the class to create the implementation within
     * @param method the abstract method to implement
     * @return the abstract method implementation
     * @since 0.20
     */
    public MethodTree createAbstractMethodImplementation(TypeElement clazz, ExecutableElement method) {
        assert clazz != null && method != null;
        return createMethod(method, clazz, true);
    }

    /**
     * Creates overriding methods within a class.
     *
     * @param clazz the class to create the methods within
     * @param methods the methods to override
     * @return the overriding methods
     * @since 0.20
     */
    public List<? extends MethodTree> createOverridingMethods(TypeElement clazz, Iterable<? extends ExecutableElement> methods) {
        assert methods != null;
        List<MethodTree> ret = new ArrayList<MethodTree>();
        for(ExecutableElement method : methods)
            ret.add(createOverridingMethod(clazz, method));

        tagFirst(ret);
        return ret;
    }

    /**
     * Creates an overriding method within a class.
     *
     * @param clazz the class to create the method within
     * @param method the method to override
     * @return the overriding method
     * @since 0.20
     */
    public MethodTree createOverridingMethod(TypeElement clazz, ExecutableElement method) {
        assert clazz != null && method != null;
        return createMethod(method, clazz, false);
    }

    /**Create a new method tree for the given method element. The method will be created as if it were member of {@code asMemberOf} type
     * (see also {@link Types#asMemberOf(javax.lang.model.type.DeclaredType,javax.lang.model.element.Element)}).
     * The new method will have an empty body.
     *
     * @param asMemberOf create the method as if it were member of this type
     * @param method method to create
     * @return a newly created method
     * @see Types#asMemberOf(javax.lang.model.type.DeclaredType,javax.lang.model.element.Element)
     * @since 0.34
     */
    public MethodTree createMethod(DeclaredType asMemberOf, ExecutableElement method) {
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = method.getModifiers();
        Set<Modifier> flags = mods.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(mods);
        flags.remove(Modifier.ABSTRACT);
        flags.remove(Modifier.NATIVE);
        flags.remove(Modifier.DEFAULT);

        ExecutableType et = (ExecutableType) method.asType();
        try {
            et = (ExecutableType) copy.getTypes().asMemberOf(asMemberOf, method);
        } catch (IllegalArgumentException iae) {
        }
        List<TypeParameterTree> typeParams = new ArrayList<TypeParameterTree>();
        for (TypeVariable typeVariable : et.getTypeVariables()) {
            List<ExpressionTree> bounds = new ArrayList<ExpressionTree>();
            TypeMirror bound = typeVariable.getUpperBound();
            if (bound.getKind() != TypeKind.NULL) {
                if (bound.getKind() == TypeKind.DECLARED) {
                    ClassSymbol boundSymbol = (ClassSymbol) ((DeclaredType) bound).asElement();
                    if (boundSymbol.getSimpleName().length() == 0 && (boundSymbol.flags() & Flags.COMPOUND) != 0) {
                        bounds.add((ExpressionTree) make.Type(boundSymbol.getSuperclass()));
                        for (Type iface : boundSymbol.getInterfaces()) {
                            bounds.add((ExpressionTree) make.Type(iface));
                        }
                    } else if (!boundSymbol.getQualifiedName().contentEquals("java.lang.Object")) { //NOI18N
                        //if the bound is java.lang.Object, do not generate the extends clause:

                        bounds.add((ExpressionTree) make.Type(bound));
                    }
                } else {
                    bounds.add((ExpressionTree) make.Type(bound));
                }
            }
            typeParams.add(make.TypeParameter(typeVariable.asElement().getSimpleName(), bounds));
        }

        Tree returnType = make.Type(et.getReturnType());

        List<VariableTree> params = new ArrayList<VariableTree>();
        boolean isVarArgs = method.isVarArgs();
        Iterator<? extends VariableElement> formArgNames = method.getParameters().iterator();
        Iterator<? extends TypeMirror> formArgTypes = et.getParameterTypes().iterator();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        while (formArgNames.hasNext() && formArgTypes.hasNext()) {
            VariableElement formArgName = formArgNames.next();
            TypeMirror formArgType = formArgTypes.next();
            if (isVarArgs && !formArgNames.hasNext()) {
                parameterModifiers = make.Modifiers(1L << 34,
                        Collections.<AnnotationTree>emptyList());
            }
            String paramName = addParamPrefixSuffix(removeParamPrefixSuffix(formArgName, cs), cs);
            params.add(make.Variable(parameterModifiers, paramName, resolveWildcard(formArgType), null));
        }

        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        for (TypeMirror tm : et.getThrownTypes()) {
            throwsList.add((ExpressionTree) make.Type(tm));
        }

        ModifiersTree mt = make.Modifiers(flags, Collections.<AnnotationTree>emptyList());

        return make.Method(mt, method.getSimpleName(), returnType, typeParams, params, throwsList, "{}", null);
    }

    /**
     * Creates a class constructor.
     *
     * @param clazz the class to create the constructor for
     * @param fields fields to be initialized by the constructor
     * @param constructor inherited constructor to be called
     * @return the constructor
     * @since 0.20
     */
    public MethodTree createConstructor(TypeElement clazz, Iterable<? extends VariableElement> fields, ExecutableElement constructor) {
        return createConstructor(clazz, fields, constructor, false);
    }
    
    /**
     * Creates a class default constructor. Fields and the inherited constructor
     * are initialized/called with default values.
     *
     * @param clazz the class to create the constructor for
     * @param fields fields to be initialized by the constructor
     * @param constructor inherited constructor to be called
     * @return the constructor
     * @since 0.126
     */
    public MethodTree createDefaultConstructor(TypeElement clazz, Iterable<? extends VariableElement> fields, ExecutableElement constructor) {
        return createConstructor(clazz, fields, constructor, true);
    }

    private MethodTree createConstructor(TypeElement clazz, Iterable<? extends VariableElement> fields, ExecutableElement constructor, boolean isDefault) {
        assert clazz != null && fields != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(clazz.getKind() == ElementKind.ENUM ? Modifier.PRIVATE : Modifier.PUBLIC);
        List<VariableTree> parameters = new ArrayList<VariableTree>();
        LinkedList<StatementTree> statements = new LinkedList<StatementTree>();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        List<ExpressionTree> throwsList = new LinkedList<ExpressionTree>();
        List<TypeParameterTree> typeParams = new LinkedList<TypeParameterTree>();
        for (VariableElement ve : fields) {
            TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), ve);
            if (isDefault) {
                statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Literal(defaultValue(type))))); //NOI18N
            } else {
                String paramName = addParamPrefixSuffix(removeFieldPrefixSuffix(ve, cs), cs);
                parameters.add(make.Variable(parameterModifiers, paramName, make.Type(type), null));
                statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(paramName)))); //NOI18N
            }
        }
        if (constructor != null) {
            ExecutableType constructorType = clazz.getSuperclass().getKind() == TypeKind.DECLARED && ((DeclaredType) clazz.getSuperclass()).asElement() == constructor.getEnclosingElement() ? (ExecutableType) copy.getTypes().asMemberOf((DeclaredType) clazz.getSuperclass(), constructor) : null;
            if (!constructor.getParameters().isEmpty()) {
                List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                Iterator<? extends VariableElement> parameterElements = constructor.getParameters().iterator();
                Iterator<? extends TypeMirror> parameterTypes = constructorType != null ? constructorType.getParameterTypes().iterator() : null;
                while (parameterElements.hasNext()) {
                    VariableElement ve = parameterElements.next();
                    TypeMirror type = parameterTypes != null ? parameterTypes.next() : ve.asType();
                    if (isDefault) {
                        arguments.add(make.Literal(defaultValue(type)));
                    } else {
                        String paramName = addParamPrefixSuffix(removeParamPrefixSuffix(ve, cs), cs);
                        parameters.add(make.Variable(parameterModifiers, paramName, make.Type(type), null));
                        arguments.add(make.Identifier(paramName));
                    }
                }
                statements.addFirst(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("super"), arguments))); //NOI18N
            }
            constructorType = constructorType != null ? constructorType : (ExecutableType) constructor.asType();
            for (TypeMirror th : constructorType.getThrownTypes()) {
                throwsList.add((ExpressionTree) make.Type(th));
            }
            for (TypeParameterElement typeParameterElement : constructor.getTypeParameters()) {
                List<ExpressionTree> boundsList = new LinkedList<ExpressionTree>();
                for (TypeMirror bound : typeParameterElement.getBounds()) {
                    boundsList.add((ExpressionTree) make.Type(bound));
                }
                typeParams.add(make.TypeParameter(typeParameterElement.getSimpleName(), boundsList));
            }
        }
        BlockTree body = make.Block(statements, false);
        return make.Method(make.Modifiers(mods), "<init>", null, typeParams, parameters, throwsList, body, null, constructor!= null ? constructor.isVarArgs() : false); //NOI18N
    }

    /**
     * Creates a class constructor.
     *
     * @param clazz the class to create the constructor for
     * @param fields fields to be initialized by the constructor
     * @return the constructor
     * @since 0.20
     */
    public MethodTree createConstructor(ClassTree clazz, Iterable<? extends VariableTree> fields) {
        assert clazz != null && fields != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(copy.getTreeUtilities().isEnum(clazz) ? Modifier.PRIVATE : Modifier.PUBLIC);
        List<VariableTree> parameters = new ArrayList<VariableTree>();
        List<StatementTree> statements = new ArrayList<StatementTree>();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        for (VariableTree vt : fields) {
            String paramName = addParamPrefixSuffix(removeFieldPrefixSuffix(vt, cs), cs);
            parameters.add(make.Variable(parameterModifiers, paramName, vt.getType(), null));
            statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), vt.getName()), make.Identifier(paramName)))); //NOI18N
        }
        BlockTree body = make.Block(statements, false);
        return make.Method(make.Modifiers(mods), "<init>", null, Collections.<TypeParameterTree> emptyList(), parameters, Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
    }

    /**
     * Creates a getter method for a field.
     *
     * @param clazz the class to create the getter within
     * @param field field to create getter for
     * @return the getter method
     * @since 0.20
     */
    public MethodTree createGetter(TypeElement clazz, VariableElement field) {
        assert clazz != null && field != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        if (isStatic) {
            mods.add(Modifier.STATIC);
        }
        TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), field);
        boolean isBoolean = type.getKind() == TypeKind.BOOLEAN;
        String getterName = CodeStyleUtils.computeGetterName(field.getSimpleName(), isBoolean, isStatic, cs);
        BlockTree body = make.Block(Collections.singletonList(make.Return(make.Identifier(field.getSimpleName()))), false);
        return make.Method(make.Modifiers(mods), getterName, make.Type(type), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null);
    }

    /**
     * Creates a getter method for a field.
     *
     * @param field field to create getter for
     * @return the getter method
     * @since 0.20
     */
    public MethodTree createGetter(VariableTree field) {
        assert field != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().getFlags().contains(Modifier.STATIC);
        if (isStatic) {
            mods.add(Modifier.STATIC);
        }
        Tree type = field.getType();
        boolean isBoolean = type.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree) type).getPrimitiveTypeKind() == TypeKind.BOOLEAN;
        String getterName = CodeStyleUtils.computeGetterName(field.getName(), isBoolean, isStatic, cs);
        BlockTree body = make.Block(Collections.singletonList(make.Return(make.Identifier(field.getName()))), false);
        return make.Method(make.Modifiers(mods), getterName, type, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null);
    }

    /**
     * Creates a setter method for a field.
     *
     * @param clazz the class to create the setter within
     * @param field field to create setter for
     * @return the setter method
     * @since 0.20
     */
    public MethodTree createSetter(TypeElement clazz, VariableElement field) {
        assert clazz != null && field != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        if (isStatic) {
            mods.add(Modifier.STATIC);
        }
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), field);
        String setterName = CodeStyleUtils.computeSetterName(field.getSimpleName(), isStatic, cs);
        String paramName = addParamPrefixSuffix(removeFieldPrefixSuffix(field, cs), cs);
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), paramName, make.Type(type), null));
        BlockTree body = make.Block(Collections.singletonList(make.ExpressionStatement(make.Assignment(make.MemberSelect(isStatic? make.Identifier(field.getEnclosingElement().getSimpleName()) : make.Identifier("this"), name), make.Identifier(paramName)))), false); //NOI18N
        return make.Method(make.Modifiers(mods), setterName, make.Type(copy.getTypes().getNoType(TypeKind.VOID)), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null);
    }

    /**
     * Creates a setter method for a field.
     *
     * @param clazz the class to create the setter within
     * @param field field to create setter for
     * @return the setter method
     * @since 0.20
     */
    public MethodTree createSetter(ClassTree clazz, VariableTree field) {
        assert clazz != null && field != null;
        TreeMaker make = copy.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().getFlags().contains(Modifier.STATIC);
        if (isStatic)
            mods.add(Modifier.STATIC);
        CharSequence name = field.getName();
        assert name.length() > 0;
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        String propName = removeFieldPrefixSuffix(field, cs);
        String setterName = CodeStyleUtils.computeSetterName(field.getName(), isStatic, cs);
        String paramName = addParamPrefixSuffix(propName, cs);
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), paramName, field.getType(), null));
        BlockTree body = make.Block(Collections.singletonList(make.ExpressionStatement(make.Assignment(make.MemberSelect(isStatic? make.Identifier(clazz.getSimpleName()) : make.Identifier("this"), name), make.Identifier(paramName)))), false); //NOI18N
        return make.Method(make.Modifiers(mods), setterName, make.Type(copy.getTypes().getNoType(TypeKind.VOID)), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null);
    }
    
    /**
     * Creates a default lambda body.
     *
     * @param lambda a lambda to generate body for
     * @param method a method of a functional interface to be implemented by the lambda expression
     * @return the lambda body
     * @since 2.19
     */
    public BlockTree createDefaultLambdaBody(LambdaExpressionTree lambda, ExecutableElement method) {
        try {
            String bodyTemplate = "{" + readFromTemplate(LAMBDA_BODY, createBindings(null, method)) + "\n}"; //NOI18N
            return copy.getTreeMaker().createLambdaBody(lambda, bodyTemplate);
        } catch (Exception e) {}
        return copy.getTreeMaker().Block(Collections.emptyList(), false);
    }

    /**
     * Creates a default lambda expression.
     *
     * @param lambda a lambda to generate the expression for
     * @param method a method of a functional interface to be implemented by the lambda expression
     * @return the lambda expression
     * @since 2.57
     */
    public ExpressionTree createDefaultLambdaExpression(LambdaExpressionTree lambda, ExecutableElement method) {
        try {
            String bodyTemplate = readFromTemplate(LAMBDA_EXPRESSION, createBindings(null, method)); //NOI18N
            return copy.getTreeMaker().createLambdaExpression(lambda, bodyTemplate);
        } catch (Exception e) {}
        return null;
    }
    
    private boolean isStarImport(ImportTree imp) {
        Tree qualIdent = imp.getQualifiedIdentifier();        
        boolean isStar = qualIdent.getKind() == Tree.Kind.MEMBER_SELECT && ((MemberSelectTree)qualIdent).getIdentifier().contentEquals("*"); // NOI18N
        return isStar;
    }
    
    /**
     * Adds import statements for given elements to a compilation unit. The import section of the
     * given compilation unit is modified according to the rules specified in the {@link CodeStyle}.
     * <p><strong>Use TreeMaker.QualIdent, TreeMaker.Type or GeneratorUtilities.importFQNs
     * instead of this method if possible. These methods will correctly resolve imports according
     * to the user's preferences.</strong></p>
     *
     * @param cut the compilation unit to insert imports to
     * @param toImport the elements to import. 
     * @return the modified compilation unit
     * @since 0.86
     */
    public CompilationUnitTree addImports(CompilationUnitTree cut, Set<? extends Element> toImport) {
        return addImports(cut, cut.getImports(), toImport);
    }

    private CompilationUnitTree addImports(CompilationUnitTree cut, List<? extends ImportTree> cutImports, Set<? extends Element> toImport) {
        assert cut != null && toImport != null && toImport.size() > 0;

        ArrayList<Element> elementsToImport = new ArrayList<Element>(toImport.size());
        Set<String> staticImportNames = new HashSet<String>();
        for (Element e : toImport) {
            if (e == null) {
                continue;
            }
            switch (e.getKind()) {
                case METHOD:
                case ENUM_CONSTANT:
                case FIELD:
                    String name = new StringBuilder(((TypeElement)e.getEnclosingElement()).getQualifiedName()).append('.').append(e.getSimpleName()).toString();
                    if (!staticImportNames.add(name))
                        break;
                default:
                    elementsToImport.add(e);
            }
        }

        Trees trees = copy.getTrees();
        Elements elements = copy.getElements();
        ElementUtilities elementUtilities = copy.getElementUtilities();
                
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        
        // check weather any conversions to star imports are needed
        int treshold = cs.useSingleClassImport() ? cs.countForUsingStarImport() : 1;
        int staticTreshold = cs.countForUsingStaticStarImport();        
        Map<PackageElement, Integer> pkgCounts = new LinkedHashMap<PackageElement, Integer>();
        PackageElement pkg = elements.getPackageElement("java.lang"); //NOI18N
        if (pkg != null) {
            pkgCounts.put(pkg, -2);
        }
        ExpressionTree packageName = cut.getPackageName();
        pkg = packageName != null ? (PackageElement)trees.getElement(TreePath.getPath(cut, packageName)) : null;
        if (pkg == null && packageName != null) {
            pkg = elements.getPackageElement(elements.getName(packageName.toString()));
        }
        if (pkg == null) {
            pkg = elements.getPackageElement(elements.getName("")); //NOI18N
        }
        pkgCounts.put(pkg, -2);
        Map<TypeElement, Integer> typeCounts = new LinkedHashMap<TypeElement, Integer>();
        // initially the import scope has no symbols. We must fill it in by:
        // existing CUT named imports, package members AND then star imports, in this specific order
        JCCompilationUnit jcut = (JCCompilationUnit)cut;
        StarImportScope importScope = new StarImportScope((Symbol)pkg);
        if (jcut.starImportScope != null) {
            importScope.prependSubScope(((JCCompilationUnit)cut).starImportScope);
        }
        if (jcut.packge != null) {
            importScope.prependSubScope(jcut.packge.members_field);
        }
        for (Element e : elementsToImport) {
            boolean isStatic = false;
            Element el = null;
            switch (e.getKind()) {
                case PACKAGE:
                    el = e;
                    break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                    if (e.getEnclosingElement().getKind() == ElementKind.PACKAGE)
                        el = e.getEnclosingElement();
                    break;
                case METHOD:
                case ENUM_CONSTANT:
                case FIELD:
                    isStatic = true;
                    el = e.getEnclosingElement();
                    break;
                default:
                    assert false : "Illegal element kind: " + e.getKind(); //NOI18N
            }
            if (el != null) {
                Integer cnt = isStatic ? typeCounts.get((TypeElement)el) : pkgCounts.get((PackageElement)el);
                if (cnt == null)
                    cnt = 0;
                if (cnt >= 0) {
                    if (el == e) {
                        cnt = -1;
                    } else {
                        cnt++;
                        if (isStatic) {
                            if (cnt >= staticTreshold)
                                cnt = -1;
                        } else if (cnt >= treshold || checkPackagesForStarImport(((PackageElement)el).getQualifiedName().toString(), cs)) {
                            cnt = -1;
                        }
                    }
                }
                if (isStatic) {
                    typeCounts.put((TypeElement)el, cnt);
                } else {
                    pkgCounts.put((PackageElement)el, cnt);
                }
            }
        }
        List<ImportTree> imports = new ArrayList<ImportTree>(cutImports);
        for (ImportTree imp : imports) {
            Element e = getImportedElement(cut, imp);
            if (!elementsToImport.contains(e)) {
                if (imp.isStatic()) {
                    if (e.getKind().isClass() || e.getKind().isInterface()) {
                        Element el = e;
                        while (el != null) {
                            Integer cnt = typeCounts.get((TypeElement)el);
                            if (cnt != null && staticTreshold == Integer.MAX_VALUE) {
                                typeCounts.put((TypeElement)el, -2);
                            }
                            TypeMirror tm = ((TypeElement)el).getSuperclass();
                            el = tm.getKind() == TypeKind.DECLARED ? ((DeclaredType)tm).asElement() : null;
                        }
                    } else {
                        Element el = elementUtilities.enclosingTypeElement(e);
                        if (el != null) {
                            Integer cnt = typeCounts.get((TypeElement)el);
                            if (cnt != null) {
                                if (cnt >= 0) {
                                    cnt++;
                                    if (cnt >= staticTreshold)
                                        cnt = -1;
                                }
                                typeCounts.put((TypeElement)el, cnt);
                            }
                        }
                    }
                } else {
                    Element el = e.getKind() == ElementKind.PACKAGE ? e : (e.getKind().isClass() || e.getKind().isInterface()) && e.getEnclosingElement().getKind() == ElementKind.PACKAGE ? e.getEnclosingElement() : null;
                    if (el != null) {
                        Integer cnt = pkgCounts.get((PackageElement)el);
                        if (cnt != null) {
                            if (el == e) { // this is only true for package element, that is for package-star import.
                                if (treshold == Integer.MAX_VALUE) {
                                    // do not touch the star import
                                    cnt = -2;
                                }
                            } else if (cnt >= 0) {
                                cnt++;
                                if (cnt >= treshold)
                                    cnt = -1;
                            }
                            pkgCounts.put((PackageElement)el, cnt);
                        }
                    }
                }
            } else if (treshold == Integer.MAX_VALUE || staticTreshold == Integer.MAX_VALUE) {
                // disable any manipulations (optimization) for existing star imports iff the "Count" feature is disabled.
                int threshold = imp.isStatic() ? staticTreshold : treshold;
                if (isStarImport(imp) && threshold == Integer.MAX_VALUE) {
                    Map map = imp.isStatic() ? typeCounts : pkgCounts;
                    Integer cnt = (Integer)map.get(e);
                    if (cnt != null) {
                        map.put(e, -2);
                    }
                }
            }
        }
        // remove those star imports that do not satisfy the thresholds
        for (Iterator<ImportTree> ii = imports.iterator(); ii.hasNext();) {
            ImportTree imp = ii.next();
            if (!isStarImport(imp)) {
                continue;
            }
            Element e = getImportedElement(cut, imp);
            Integer cnt;
            if (imp.isStatic()) {
                cnt = typeCounts.get(e);
            } else {
                cnt = pkgCounts.get(e);
            }
            if (cnt != null && cnt >= 0) {
                ii.remove();
            }
        }
        
        // check for possible name clashes originating from adding the package imports
        Set<Element> explicitNamedImports = new HashSet<Element>();
        for (Element element : elementsToImport) {
            if (element.getEnclosingElement() != pkg && (element.getKind().isClass() || element.getKind().isInterface())) {
                for (Symbol sym : importScope.getSymbolsByName((com.sun.tools.javac.util.Name)element.getSimpleName())) {
                    if (sym.getKind().isClass() || sym.getKind().isInterface()) {
                        if (sym != element) {
                            explicitNamedImports.add(element);
                            break;// break if explicitNameImport was added
                        }
                    }
                }
            }
        }
        Map<Name, TypeElement> usedTypes = null;
        for (Map.Entry<PackageElement, Integer> entry : pkgCounts.entrySet()) {
            if (entry.getValue() == -1) {
                for (Element element : entry.getKey().getEnclosedElements()) {
                    if (element.getKind().isClass() || element.getKind().isInterface()) {
                        for (Symbol sym : importScope.getSymbolsByName((com.sun.tools.javac.util.Name)element.getSimpleName())) {
                            if (sym != element) {
                                TypeElement te = null;
                                for (Element e : elementsToImport) {
                                    if ((e.getKind().isClass() || e.getKind().isInterface()) && element.getSimpleName() == e.getSimpleName()) {
                                        te = (TypeElement) e;
                                        break;
                                    }
                                }
                                if (te != null) {
                                    explicitNamedImports.add(te);
                                } else {
                                    if (usedTypes == null) {
                                        usedTypes = getUsedTypes(cut);
                                    }
                                    if (te != null) {
                                        explicitNamedImports.add(te);
                                    } else {
                                        if (usedTypes == null) {
                                            usedTypes = getUsedTypes(cut);
                                        }
                                        te = usedTypes.get(element.getSimpleName());
                                        if (te != null) {
                                            elementsToImport.add(te);
                                            explicitNamedImports.add(te);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (entry.getValue() < 0 && entry.getKey() instanceof Symbol)
                importScope.prependSubScope(((Symbol)entry.getKey()).members());
        }

        // sort the elements to import
        ImportsComparator comparator = new ImportsComparator(cs);
        elementsToImport.sort(comparator);
        
        // merge the elements to import with the existing import statemetns
        TreeMaker make = copy.getTreeMaker();
        int currentToImport = elementsToImport.size() - 1;
        int currentExisting = imports.size() - 1;
        while (currentToImport >= 0) {
            Element currentToImportElement = elementsToImport.get(currentToImport);
            boolean isStatic = false;
            Element el = null;
            switch (currentToImportElement.getKind()) {
                case PACKAGE:
                    el = currentToImportElement;
                    break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                    if (currentToImportElement.getEnclosingElement().getKind() == ElementKind.PACKAGE)
                        el = currentToImportElement.getEnclosingElement();
                    break;
                case METHOD:
                case ENUM_CONSTANT:
                case FIELD:
                    isStatic = true;
                    el = currentToImportElement.getEnclosingElement();
                    break;
            }
            Integer cnt = el == null ? Integer.valueOf(0) : isStatic ? typeCounts.get((TypeElement)el) : pkgCounts.get((PackageElement)el);
            if (explicitNamedImports.contains(currentToImportElement))
                cnt = 0;
            if (cnt == -2) {
                currentToImport--;
            } else {
                if (cnt == -1) {
                    currentToImportElement = el;
                    if (isStatic) {
                        typeCounts.put((TypeElement)el, -2);
                    } else {
                        pkgCounts.put((PackageElement)el, -2);
                    }
                }
                boolean isStar = currentToImportElement.getKind() == ElementKind.PACKAGE
                        || isStatic && (currentToImportElement.getKind().isClass() || currentToImportElement.getKind().isInterface());
                ExpressionTree qualIdent = qualIdentFor(currentToImportElement);
                if (isStar) {
                    qualIdent = make.MemberSelect(qualIdent, elements.getName("*")); //NOI18N
                }
                ImportTree nImport = make.Import(qualIdent, isStatic);
                while (currentExisting >= 0) {
                    ImportTree imp = imports.get(currentExisting);
                    Element impElement = getImportedElement(cut, imp);
                    el = imp.isStatic()
                            ? impElement.getKind().isClass() || impElement.getKind().isInterface() ? impElement : elementUtilities.enclosingTypeElement(impElement)
                            : impElement.getKind() == ElementKind.PACKAGE ? impElement : (impElement.getKind().isClass() || impElement.getKind().isInterface()) && impElement.getEnclosingElement().getKind() == ElementKind.PACKAGE ? impElement.getEnclosingElement() : null;
                    if (isStatic == imp.isStatic() && (currentToImportElement == impElement || isStar && currentToImportElement == el)) {
                        imports.remove(currentExisting);                        
                    } else {
                        if (comparator.compare(nImport, imp) > 0) {
                            break;
                        }
                    }
                    currentExisting--;
                }
                imports.add(currentExisting + 1, nImport);
                currentToImport--;
            }
        }
        
        // return a copy of the unit with changed imports section
        return make.CompilationUnit(cut.getPackage(), imports, cut.getTypeDecls(), cut.getSourceFile());
    }

    /**
     * Take a tree as a parameter, replace resolved fully qualified names with
     * simple names and add imports to compilation unit during task commit.
     *
     * @param  original  resolved FQNs in the tree will be imported
     * @return the new tree containing simple names (QualIdents). Imports for
     *         them will be added during task commit.
     */
    public <T extends Tree> T importFQNs(T original) {
        return TranslateIdentifier.importFQNs(copy, original);
    }

    public <T extends Tree> T importComments(T original, CompilationUnitTree cut) {
        return importComments(copy, original, cut);
    }

    static <T extends Tree> T importComments(CompilationInfo info, T original, CompilationUnitTree cut) {
        try {
            CommentSetImpl comments = CommentHandlerService.instance(info.impl.getJavacTask().getContext()).getComments(original);

            if (comments.areCommentsMapped()) {
                //optimalization, if comments are already mapped, do not even try to
                //map them again, would not be attached anyway:
                return original;
            }
            
            JCTree.JCCompilationUnit unit = (JCCompilationUnit) cut;
            TokenHierarchy<?> tokens =   unit.getSourceFile() instanceof AbstractSourceFileObject
                                       ? ((AbstractSourceFileObject) unit.getSourceFile()).getTokenHierarchy()
                                       : TokenHierarchy.create(unit.getSourceFile().getCharContent(true), JavaTokenId.language());
            TokenSequence<JavaTokenId> seq = tokens.tokenSequence(JavaTokenId.language());
            TreePath tp = TreePath.getPath(cut, original);
            Tree toMap = original;
            Tree mapTarget = null;
            
            if (tp != null && original.getKind() != Kind.COMPILATION_UNIT) {
                // find some 'nice' place like method/class/field so the comments get an appropriate contents
                // Javadocs or other comments may be assigned inappropriately with wider surrounding contents.
                TreePath p2 = tp;
                boolean first = true;
                B: while (p2 != null) {
                    Tree.Kind k = p2.getLeaf().getKind();
                    if (StatementTree.class.isAssignableFrom(k.asInterface())) {
                        mapTarget = p2.getLeaf();
                        p2 = p2.getParentPath();
                        break;
                    }
                   switch (p2.getLeaf().getKind()) {
                       case CLASS: case INTERFACE: case ENUM: case RECORD:
                       case METHOD:
                       case BLOCK:
                       case VARIABLE:
                           if (mapTarget == null) {
                               mapTarget = p2.getLeaf();
                           }
                           if (first) {
                               p2 = p2 = p2.getParentPath();
                           }
                           break B;
                   } 
                   first = false;
                   p2 = p2.getParentPath();
                }
                if (p2 != null) {
                    toMap = p2.getLeaf();
                }
                if (toMap == tp.getLeaf()) {
                    // go at least one level up in a hope it's sufficient.
                    toMap = tp.getParentPath().getLeaf();
                }
            }
            if (mapTarget == null) {
                mapTarget = original;
            }
            AssignComments translator = new AssignComments(info, mapTarget, seq, unit);
            
            translator.scan(toMap, null);

            return original;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return original;
    }

    /**
     * Copy comments from source tree to target tree. Copying a given comment twice will be ignored.
     *
     * @param source tree to copy comments from
     * @param target tree to copy comments to
     * @param preceding true iff preceding comments should be copied
     * @since 0.51
     */
    public void copyComments(Tree source, Tree target, boolean preceding) {
        CommentHandlerService handler = CommentHandlerService.instance(copy.impl.getJavacTask().getContext());
        CommentSetImpl s = handler.getComments(source);

        TreeUtilities.ensureCommentsMapped(copy, source, s);

        CommentSetImpl t = handler.getComments(target);

        if (preceding) {
            t.addComments(RelativePosition.PRECEDING, copy.useComments(s.getComments(RelativePosition.PRECEDING)));
            t.addComments(RelativePosition.INNER, copy.useComments(s.getComments(RelativePosition.INNER)));
        } else {
            t.addComments(RelativePosition.INLINE, copy.useComments(s.getComments(RelativePosition.INLINE)));
            t.addComments(RelativePosition.TRAILING, copy.useComments(s.getComments(RelativePosition.TRAILING)));
        }
    }

    /**Ensures that the given {@code modifiers} contains annotation of the given type,
     * which has attribute name {@code attributeName}, which contains values {@code attributeValuesToAdd}.
     * The annotation or the attribute will be added as needed, as will be the attribute value
     * converted from a single value into an array.
     *
     * The typical trees passed as {@code attributeValuesToAdd} are:
     * <table>
     * <caption>Typical tree</caption>
     *     <tr>
     *         <th>attribute type</th>
     *         <th>expected tree type</th>
     *     </tr>
     *     <tr>
     *         <td>primitive type</td>
     *         <td>{@link LiteralTree} created by {@link TreeMaker#Literal(java.lang.Object) }</td>
     *     </tr>
     *     <tr>
     *         <td>{@code java.lang.String}</td>
     *         <td>{@link LiteralTree} created by {@link TreeMaker#Literal(java.lang.Object) }</td>
     *     </tr>
     *     <tr>
     *         <td>{@code java.lang.Class}</td>
     *         <td>{@link MemberSelectTree} created by {@link TreeMaker#MemberSelect(com.sun.source.tree.ExpressionTree, java.lang.CharSequence)  },
     *             with identifier {@code class} and expression created by {@link TreeMaker#QualIdent(javax.lang.model.element.Element) }</td>
     *     </tr>
     *     <tr>
     *         <td>enum constant</td>
     *         <td>{@link MemberSelectTree}, with identifier representing the enum constant
     *             and expression created by {@link TreeMaker#QualIdent(javax.lang.model.element.Element) }</td>
     *     </tr>
     *     <tr>
     *         <td>annotation type</td>
     *         <td>{@link AnnotationTree} created by {@link TreeMaker#Annotation(com.sun.source.tree.Tree, java.util.List) }</td>
     *     </tr>
     *     <tr>
     *         <td>array (of a supported type)</td>
     *         <td>{@link NewArrayTree} created by {@link TreeMaker#NewArray(com.sun.source.tree.Tree, java.util.List, java.util.List) },
     *             where {@code elemtype} is {@code null}, {@code dimensions} is {@code Collections.<ExpressionTree>emptyList()},
     *             {@code initializers} should contain the elements that should appear in the array</td>
     *     </tr>
     * </table>
     *
     * @param modifiers into which the values should be added
     * @param annotation the annotation type that should be added or augmented
     * @param attributeName the attribute that should be added or augmented
     * @param attributeValuesToAdd values that should be added into the given attribute of the given annotation
     * @return {@code modifiers} augmented in such a way that it contains the given annotation, with the given values
     * @since 0.99
     */
    public ModifiersTree appendToAnnotationValue(ModifiersTree modifiers, TypeElement annotation, String attributeName, ExpressionTree... attributeValuesToAdd) {
        return (ModifiersTree) appendToAnnotationValue((Tree) modifiers, annotation, attributeName, attributeValuesToAdd);
    }

    /**Ensures that the given {@code compilationUnit} contains annotation of the given type,
     * which has attribute name {@code attributeName}, which contains values {@code attributeValuesToAdd}.
     * The annotation or the attribute will be added as needed, as will be the attribute value
     * converted from a single value into an array. This method is intended to be called on
     * {@link CompilationUnitTree} from {@code package-info.java}.
     *
     * The typical trees passed as {@code attributeValuesToAdd} are:
     * <table>
     * <caption>Typical tree</caption>
     *     <tr>
     *         <th>attribute type</th>
     *         <th>expected tree type</th>
     *     </tr>
     *     <tr>
     *         <td>primitive type</td>
     *         <td>{@link LiteralTree} created by {@link TreeMaker#Literal(java.lang.Object) }</td>
     *     </tr>
     *     <tr>
     *         <td>{@code java.lang.String}</td>
     *         <td>{@link LiteralTree} created by {@link TreeMaker#Literal(java.lang.Object) }</td>
     *     </tr>
     *     <tr>
     *         <td>{@code java.lang.Class}</td>
     *         <td>{@link MemberSelectTree} created by {@link TreeMaker#MemberSelect(com.sun.source.tree.ExpressionTree, java.lang.CharSequence)  },
     *             with identifier {@code class} and expression created by {@link TreeMaker#QualIdent(javax.lang.model.element.Element) }</td>
     *     </tr>
     *     <tr>
     *         <td>enum constant</td>
     *         <td>{@link MemberSelectTree}, with identifier representing the enum constant
     *             and expression created by {@link TreeMaker#QualIdent(javax.lang.model.element.Element) }</td>
     *     </tr>
     *     <tr>
     *         <td>annotation type</td>
     *         <td>{@link AnnotationTree} created by {@link TreeMaker#Annotation(com.sun.source.tree.Tree, java.util.List) }</td>
     *     </tr>
     *     <tr>
     *         <td>array (of a supported type)</td>
     *         <td>{@link NewArrayTree} created by {@link TreeMaker#NewArray(com.sun.source.tree.Tree, java.util.List, java.util.List) },
     *             where {@code elemtype} is {@code null}, {@code dimensions} is {@code Collections.<ExpressionTree>emptyList()},
     *             {@code initializers} should contain the elements that should appear in the array</td>
     *     </tr>
     * </table>
     *
     * @param compilationUnit into which the values should be added
     * @param annotation the annotation type that should be added or augmented
     * @param attributeName the attribute that should be added or augmented
     * @param attributeValuesToAdd values that should be added into the given attribute of the given annotation
     * @return {@code compilationUnit} augmented in such a way that it contains the given annotation, with the given values
     * @since 0.99
     */
    public CompilationUnitTree appendToAnnotationValue(CompilationUnitTree compilationUnit, TypeElement annotation, String attributeName, ExpressionTree... attributeValuesToAdd) {
        return (CompilationUnitTree) appendToAnnotationValue((Tree) compilationUnit, annotation, attributeName, attributeValuesToAdd);
    }

    private Tree appendToAnnotationValue(Tree/*CompilationUnitTree|ModifiersTree*/ modifiers, TypeElement annotation, String attributeName, ExpressionTree... attributeValuesToAdd) {
        TreeMaker make = copy.getTreeMaker();

        //check for already existing SuppressWarnings annotation:
        List<? extends AnnotationTree> annotations = null;

        if (modifiers.getKind() == Kind.MODIFIERS) {
            annotations = ((ModifiersTree) modifiers).getAnnotations();
        } else if (modifiers.getKind() == Kind.COMPILATION_UNIT) {
            annotations = ((CompilationUnitTree) modifiers).getPackageAnnotations();
        } else {
            throw new IllegalStateException();
        }

        for (AnnotationTree at : annotations) {
            TreePath tp = new TreePath(new TreePath(copy.getCompilationUnit()), at.getAnnotationType());
            Element  e  = copy.getTrees().getElement(tp);

            if (annotation.equals(e)) {
                //found SuppressWarnings:
                List<? extends ExpressionTree> arguments = at.getArguments();

                for (ExpressionTree et : arguments) {
                    ExpressionTree expression;

                    if (et.getKind() == Kind.ASSIGNMENT) {
                        AssignmentTree assignment = (AssignmentTree) et;

                        if (!((IdentifierTree) assignment.getVariable()).getName().contentEquals(attributeName)) continue;

                        expression = assignment.getExpression();
                    } else if ("value".equals(attributeName)) {
                        expression = et;
                    } else {
                        continue;
                    }

                    List<? extends ExpressionTree> currentValues;

                    if (expression.getKind() == Kind.NEW_ARRAY) {
                        currentValues = ((NewArrayTree) expression).getInitializers();
                    } else {
                        currentValues = Collections.singletonList(expression);
                    }

                    assert currentValues != null;

                    List<ExpressionTree> values = new ArrayList<ExpressionTree>(currentValues);

                    values.addAll(Arrays.asList(attributeValuesToAdd));

                    NewArrayTree newAssignment = make.NewArray(null, Collections.<ExpressionTree>emptyList(), values);

                    return copy.getTreeUtilities().translate(modifiers, Collections.singletonMap(expression, newAssignment));
                }

                AnnotationTree newAnnotation = make.addAnnotationAttrValue(at, make.Assignment(make.Identifier(attributeName), make.NewArray(null, Collections.<ExpressionTree>emptyList(), Arrays.asList(attributeValuesToAdd))));

                return copy.getTreeUtilities().translate(modifiers, Collections.singletonMap(at, newAnnotation));
            }
        }

        ExpressionTree attribute;

        if (attributeValuesToAdd.length > 1 ) {
            attribute = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Arrays.asList(attributeValuesToAdd));
        }
        else {
            attribute = attributeValuesToAdd[0];
        }

        ExpressionTree attributeAssignmentTree;

        if ("value".equals(attributeName)) {
            attributeAssignmentTree = attribute;
        } else {
            attributeAssignmentTree = make.Assignment(make.Identifier(attributeName), attribute);
        }
        
        AnnotationTree newAnnotation = make.Annotation(make.QualIdent(annotation), Collections.singletonList(attributeAssignmentTree));
        
        if (modifiers.getKind() == Kind.MODIFIERS) {
            return make.addModifiersAnnotation((ModifiersTree) modifiers, newAnnotation);
        } else if (modifiers.getKind() == Kind.COMPILATION_UNIT) {
            return make.addPackageAnnotation((CompilationUnitTree) modifiers, newAnnotation);
        } else {
            throw new IllegalStateException();
        }
    }
    
    // private implementation --------------------------------------------------

    private MethodTree createMethod(final ExecutableElement element, final TypeElement clazz, final boolean isImplement) {
        final TreeMaker make = copy.getTreeMaker();
        MethodTree prototype = createMethod((DeclaredType)clazz.asType(), element);
        ModifiersTree mt = prototype.getModifiers();

        if (supportsOverride(copy)) {
            //add @Override annotation:
            if (copy.getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0) {
                boolean generate = true;

                if (copy.getSourceVersion().compareTo(SourceVersion.RELEASE_5) == 0) {
                    generate = !element.getEnclosingElement().getKind().isInterface();
                }

                if (generate) {
                   mt = make.addModifiersAnnotation(prototype.getModifiers(), make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList()));
                }
            }
        }
        
        String bodyTemplate = null;
        if (isImplement && clazz.getKind().isInterface()) {
            mt = make.addModifiersModifier(mt, Modifier.DEFAULT);                                
        }
        boolean replaceable = copy.getTrees().getTree(clazz).getKind() == Kind.RECORD && copy.getElementUtilities().isSynthetic(element);
        boolean isAbstract = element.getModifiers().contains(Modifier.ABSTRACT) || replaceable;
        if (isImplement || clazz.getKind().isClass() && (!isAbstract || !clazz.getModifiers().contains(Modifier.ABSTRACT))) {
            try {
                bodyTemplate = "{" + readFromTemplate(isAbstract ? GENERATED_METHOD_BODY : OVERRIDDEN_METHOD_BODY, createBindings(clazz, element)) + "\n}"; //NOI18N
            } catch (Exception e) {
                bodyTemplate = "{}"; //NOI18N
            }
        } else if (clazz.getKind().isClass()) {
            mt = make.addModifiersModifier(mt, Modifier.ABSTRACT);
        }
        MethodTree method = make.Method(mt, prototype.getName(), prototype.getReturnType(), prototype.getTypeParameters(), prototype.getParameters(), prototype.getThrows(), bodyTemplate, null);
        if (method.getBody() != null) {
            if (containsErrors(method.getBody())) {
                copy.rewrite(method.getBody(), make.Block(Collections.<StatementTree>emptyList(), false));
            } else {
                Trees trees = copy.getTrees();
                TreePath path = trees.getPath(clazz);
                if (path == null) {
                    path = new TreePath(copy.getCompilationUnit());
                }
                Scope s = trees.getScope(path);
                BlockTree body = method.getBody();
                copy.getTreeUtilities().attributeTree(body, s);
                body = importFQNs(body);
                copy.rewrite(method.getBody(), body);
            }
        }
        
        return method;
    }

    private static Object defaultValue(TypeMirror type) {
        switch(type.getKind()) {
            case BOOLEAN:
                return false;
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return 0;
        }
        return null;
    }

    private static boolean supportsOverride(CompilationInfo info) {
        return info.getElements().getTypeElement("java.lang.Override") != null;
    }

    private Tree resolveWildcard(TypeMirror type) {
        TreeMaker make = copy.getTreeMaker();
        Tree result;

        if (type != null && type.getKind() == TypeKind.WILDCARD) {
            WildcardType wt = (WildcardType) type;
            TypeMirror bound = wt.getSuperBound();

            if (bound == null) {
                bound = wt.getExtendsBound();
            }

            if (bound == null) {
                return make.Type("java.lang.Object");
            }

            result = make.Type(bound);
        } else {
            result = make.Type(type);
        }

        final Map<Tree, Tree> translate = new IdentityHashMap<Tree, Tree>();
        new ErrorAwareTreeScanner<Void, Void>() {
            @Override public Void visitWildcard(WildcardTree node, Void p) {
                Tree bound = node.getBound();

                if (bound != null && (bound.getKind() == Kind.EXTENDS_WILDCARD || bound.getKind() == Kind.SUPER_WILDCARD)) {
                    translate.put(bound, ((WildcardTree) bound).getBound());
                }
                return super.visitWildcard(node, p);
            }
        }.scan(result, null);

        return copy.getTreeUtilities().translate(result, translate);
    }
    
    private Element getImportedElement(CompilationUnitTree cut, ImportTree imp) {
        Trees trees = copy.getTrees();
        Tree qualIdent = imp.getQualifiedIdentifier();        
        if (qualIdent.getKind() != Tree.Kind.MEMBER_SELECT) {
            Element element = trees.getElement(TreePath.getPath(cut, qualIdent));
            if (element == null) {
                String fqn = qualIdent.toString();
                if (fqn.endsWith(".*")) //NOI18N
                    fqn = fqn.substring(0, fqn.length() - 2);
                element = getElementByFQN(cut, fqn);
            }
            return element;
        }
        Name name = ((MemberSelectTree)qualIdent).getIdentifier();
        if ("*".contentEquals(name)) { //NOI18N
            Element element = trees.getElement(TreePath.getPath(cut, ((MemberSelectTree)qualIdent).getExpression()));
            if (element == null)
                element = getElementByFQN(cut, ((MemberSelectTree)qualIdent).getExpression().toString());
            return element;
        }
        if (imp.isStatic()) {
            Element parent = trees.getElement(TreePath.getPath(cut, ((MemberSelectTree)qualIdent).getExpression()));
            if (parent == null)
                parent = getElementByFQN(cut, ((MemberSelectTree)qualIdent).getExpression().toString());
            if (parent != null && (parent.getKind().isClass() || parent.getKind().isInterface())) {
                Scope s = trees.getScope(new TreePath(copy.getCompilationUnit()));
                for (Element e : parent.getEnclosedElements()) {
                    if (name == e.getSimpleName() && e.getModifiers().contains(Modifier.STATIC) && trees.isAccessible(s, e, (DeclaredType)parent.asType()))
                        return e;
                }
                return parent;
            }
        }
        TreePath found = TreePath.getPath(cut, qualIdent);
        if (found == null) {
            found = new TreePath(new TreePath(new TreePath(cut), imp), qualIdent);
        }
        Element element = trees.getElement(found);
        if (element == null)
            element = getElementByFQN(cut, qualIdent.toString());
        return element;
    }
    
    private Element getElementByFQN(CompilationUnitTree cut, String fqn) {
        Elements elements = copy.getElements();
        Element element = elements.getTypeElement(fqn);
        if (element == null)
            element = elements.getPackageElement(fqn);
        if (element == null)
            element = Symtab.instance(copy.impl.getJavacTask().getContext()).enterClass(
                    Modules.instance(copy.impl.getJavacTask().getContext()).getDefaultModule(),
                    (com.sun.tools.javac.util.Name)elements.getName(fqn));
        return element;
    }
    
    private Map<Name, TypeElement> getUsedTypes(final CompilationUnitTree cut) {
        final Trees trees = copy.getTrees();
        final Map<Name, TypeElement> map = new HashMap<Name, TypeElement>();
        new ErrorAwareTreePathScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                if (!map.containsKey(node.getName())) {
                    Element element = trees.getElement(getCurrentPath());
                    if (element != null && (element.getKind().isClass() || element.getKind().isInterface()) && element.asType().getKind() != TypeKind.ERROR) {
                        map.put(node.getName(), (TypeElement) element);
                    }
                }
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                return scan(node.getTypeDecls(), p);
            }
        }.scan(cut, null);
        return map;
    }
    
    private ExpressionTree qualIdentFor(Element e) {
        TreeMaker tm = copy.getTreeMaker();
        if (e.getKind() == ElementKind.PACKAGE) {
            String name = ((PackageElement)e).getQualifiedName().toString();
            if (e instanceof Symbol) {
                int lastDot = name.lastIndexOf('.');
                if (lastDot < 0)
                    return tm.Identifier(e);
                return tm.MemberSelect(qualIdentFor(name.substring(0, lastDot)), e);
            }
            return qualIdentFor(name);
        }
        Element ee = e.getEnclosingElement();
        if (e instanceof Symbol)
            return ee.getSimpleName().length() > 0 ? tm.MemberSelect(qualIdentFor(ee), e) : tm.Identifier(e);
        return ee.getSimpleName().length() > 0 ? tm.MemberSelect(qualIdentFor(ee), e.getSimpleName()) : tm.Identifier(e.getSimpleName());
    }
    
    private ExpressionTree qualIdentFor(String name) {
        Elements elements = copy.getElements();
        TreeMaker tm = copy.getTreeMaker();
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0)
            return tm.Identifier(elements.getName(name));
        return tm.MemberSelect(qualIdentFor(name.substring(0, lastDot)), elements.getName(name.substring(lastDot + 1)));
    }

    private Map<String, Object> createBindings(TypeElement clazz, ExecutableElement element) {
        CodeStyle cs = DiffContext.getCodeStyle(copy);       
        Map<String, Object> bindings = new HashMap<>();
        if (clazz != null) {
            bindings.put(CLASS_NAME, clazz.getQualifiedName().toString());
            bindings.put(SIMPLE_CLASS_NAME, clazz.getSimpleName().toString());
            bindings.put(CLASS_KIND, clazz.getKind().toString());
        }
        if (element != null) {
            bindings.put(METHOD_NAME, element.getSimpleName().toString());
            bindings.put(METHOD_RETURN_TYPE, element.getReturnType().toString()); //NOI18N
            Object value;
            switch(element.getReturnType().getKind()) {
                case BOOLEAN:
                    value = "false"; //NOI18N
                    break;
                case BYTE:
                case CHAR:
                case DOUBLE:
                case FLOAT:
                case INT:
                case LONG:
                case SHORT:
                    value = 0;
                    break;
                default:
                    value = "null"; //NOI18N
            }
            if (clazz != null && clazz.getKind() == ElementKind.RECORD) {
                bindings.put(DEFAULT_RETURN_TYPE_VALUE, element.getSimpleName());
            } else {
                bindings.put(DEFAULT_RETURN_TYPE_VALUE, value);
            }
        }
        if (clazz != null && element != null) {
            StringBuilder sb = new StringBuilder();
            if (element.isDefault() && element.getEnclosingElement().getKind().isInterface()) {
                Types types = copy.getTypes();
                Context ctx = ((JavacTaskImpl) copy.impl.getJavacTask()).getContext();
                com.sun.tools.javac.code.Types typesImpl = com.sun.tools.javac.code.Types.instance(ctx);
                TypeMirror enclType = typesImpl.asSuper((Type)clazz.asType(), ((Type)element.getEnclosingElement().asType()).tsym);
                if (!types.isSubtype(clazz.getSuperclass(), enclType)) {
                    TypeMirror selected = enclType;
                    for (TypeMirror iface : clazz.getInterfaces()) {
                        if (types.isSubtype(iface, selected) &&
                            !types.isSameType(iface, enclType)) {
                            selected = iface;
                            break;
                        }
                    }
                    sb.append(((DeclaredType)selected).asElement().getSimpleName()).append('.');
                }
            }
            sb.append("super.").append(element.getSimpleName()).append('('); //NOI18N
            for (Iterator<? extends VariableElement> it = element.getParameters().iterator(); it.hasNext();) {
                VariableElement ve = it.next();
                sb.append(addParamPrefixSuffix(removeParamPrefixSuffix(ve, cs), cs));
                if (it.hasNext())
                    sb.append(","); //NOI18N
            }
            sb.append(')'); //NOI18N
            bindings.put(SUPER_METHOD_CALL, sb);
        }
        return bindings;
    }

    private static String name(Tree tree) {
        switch (tree.getKind()) {
            case VARIABLE:
                return ((VariableTree)tree).getName().toString();
            case METHOD:
                return ((MethodTree)tree).getName().toString();
            case CLASS:
                return ((ClassTree)tree).getSimpleName().toString();
            case IDENTIFIER:
                return ((IdentifierTree)tree).getName().toString();
            case MEMBER_SELECT:
                return name(((MemberSelectTree)tree).getExpression()) + '.' + ((MemberSelectTree)tree).getIdentifier();
        }
        return ""; //NOI18N
    }

    private static String[] correspondingGSNames(Tree member) {
        if (isSetter(member)) {
            String name = name(member);
            VariableTree param = ((MethodTree)member).getParameters().get(0);
            if (param.getType().getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree)param.getType()).getPrimitiveTypeKind() == TypeKind.BOOLEAN) {
                return new String[] {'g' + name.substring(1), "is" + name.substring(3)};
            }
            return new String[] {'g' + name.substring(1)};
        }
        if (isGetter(member)) {
            return new String[] {'s' + name(member).substring(1)};
        }
        if (isBooleanGetter(member)) {
            return new String[] {"set" + name(member).substring(2)}; //NOI18N
        }
        return null;
    }

    private static boolean isSetter(Tree member) {
        return member.getKind() == Tree.Kind.METHOD
                && name(member).startsWith("set") //NOI18N
                && ((MethodTree)member).getParameters().size() == 1
                && ((MethodTree)member).getReturnType().getKind() == Tree.Kind.PRIMITIVE_TYPE
                && ((PrimitiveTypeTree)((MethodTree)member).getReturnType()).getPrimitiveTypeKind() == TypeKind.VOID;
    }

    private static boolean isGetter(Tree member) {
        return member.getKind() == Tree.Kind.METHOD
                && name(member).startsWith("get") //NOI18N
                && ((MethodTree)member).getParameters().isEmpty()
                && (((MethodTree)member).getReturnType().getKind() != Tree.Kind.PRIMITIVE_TYPE
                || ((PrimitiveTypeTree)((MethodTree)member).getReturnType()).getPrimitiveTypeKind() != TypeKind.VOID);
    }

    private static boolean isBooleanGetter(Tree member) {
        return member.getKind() == Tree.Kind.METHOD
                && name(member).startsWith("is") //NOI18N
                && ((MethodTree)member).getParameters().isEmpty()
                && ((MethodTree)member).getReturnType().getKind() == Tree.Kind.PRIMITIVE_TYPE
                && ((PrimitiveTypeTree)((MethodTree)member).getReturnType()).getPrimitiveTypeKind() == TypeKind.BOOLEAN;
    }

    private static String removeFieldPrefixSuffix(VariableElement var, CodeStyle cs) {
        boolean isStatic = var.getModifiers().contains(Modifier.STATIC);
        return CodeStyleUtils.removePrefixSuffix(var.getSimpleName(),
                isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
    }

    private static String removeFieldPrefixSuffix(VariableTree var, CodeStyle cs) {
        boolean isStatic = var.getModifiers().getFlags().contains(Modifier.STATIC);
        return CodeStyleUtils.removePrefixSuffix(var.getName(),
                isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
    }

    private static String addParamPrefixSuffix(CharSequence name, CodeStyle cs) {
        return CodeStyleUtils.addPrefixSuffix(name,
                cs.getParameterNamePrefix(),
                cs.getParameterNameSuffix());
    }

    private static String removeParamPrefixSuffix(VariableElement var, CodeStyle cs) {
        return CodeStyleUtils.removePrefixSuffix(var.getSimpleName(), cs.getParameterNamePrefix(), cs.getParameterNameSuffix());
    }

    private static class ClassMemberComparator implements Comparator<Tree> {

        private final CodeStyle.MemberGroups groups;
        private final boolean sortMembersAlpha;
        private final boolean keepGASTogether;

        public ClassMemberComparator(CodeStyle cs) {
            this.groups = cs.getClassMemberGroups();
            this.sortMembersAlpha = cs.sortMembersInGroupsAlphabetically();
            this.keepGASTogether = cs.keepGettersAndSettersTogether();
        }

        @Override
        public int compare(Tree tree1, Tree tree2) {
            if (tree1 == tree2)
                return 0;
            int diff = groups.getGroupId(tree1) - groups.getGroupId(tree2);
            if (diff == 0 && sortMembersAlpha) {
                String name1 = name(tree1);
                String name2 = name(tree2);
                if (keepGASTogether) {
                    if (isSetter(tree1)) {
                        name1 = "g" + name1.substring(1) + "+1"; //NOI18N
                    }
                    if (isSetter(tree2)) {
                        name2 = "g" + name2.substring(1) + "+1"; //NOI18N
                    }
                }
                diff = name1.compareTo(name2);
            }
            return diff;
        }
    }

    private static class ImportsComparator implements Comparator<Object> {

        private final CodeStyle.ImportGroups groups;
        
        private ImportsComparator(CodeStyle cs) {
            this.groups = cs.getImportGroups();
        }

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 == o2)
                return 0;
            
            boolean isStatic1 = false;
            StringBuilder sb1 = new StringBuilder();
            if (o1 instanceof ImportTree) {
                isStatic1 = ((ImportTree)o1).isStatic();
                sb1.append(((ImportTree)o1).getQualifiedIdentifier().toString());
            } else if (o1 instanceof Element) {
                Element e1 = (Element)o1;
                if (e1.getKind().isField() || e1.getKind() == ElementKind.METHOD) {
                    sb1.append('.').append(e1.getSimpleName());
                    e1 = e1.getEnclosingElement();
                    isStatic1 = true;
                }
                if (e1.getKind().isClass() || e1.getKind().isInterface()) {
                    sb1.insert(0, ((TypeElement)e1).getQualifiedName());
                } else if (e1.getKind() == ElementKind.PACKAGE) {
                    sb1.insert(0, ((PackageElement)e1).getQualifiedName());
                }
            }
            String s1 = sb1.toString();
                
            boolean isStatic2 = false;
            StringBuilder sb2 = new StringBuilder();
            if (o2 instanceof ImportTree) {
                isStatic2 = ((ImportTree)o2).isStatic();
                sb2.append(((ImportTree)o2).getQualifiedIdentifier().toString());
            } else if (o2 instanceof Element) {
                Element e2 = (Element)o2;
                if (e2.getKind().isField() || e2.getKind() == ElementKind.METHOD) {
                    sb2.append('.').append(e2.getSimpleName());
                    e2 = e2.getEnclosingElement();
                    isStatic2 = true;
                }
                if (e2.getKind().isClass() || e2.getKind().isInterface()) {
                    sb2.insert(0, ((TypeElement)e2).getQualifiedName());
                } else if (e2.getKind() == ElementKind.PACKAGE) {
                    sb2.insert(0, ((PackageElement)e2).getQualifiedName());
                }
            }
            String s2 = sb2.toString();

            int bal = groups.getGroupId(s1, isStatic1) - groups.getGroupId(s2, isStatic2);

            return bal == 0 ? s1.compareTo(s2) : bal;
        }
    }
    
    /**
     * Tags first method in the list, in order to select it later inside editor
     * @param methods list of methods to be implemented/overridden
     */
    private void tagFirst(List<MethodTree> methods) {
        //tag first method body, if any
        if (methods.size() > 0) {
            BlockTree body = methods.get(0).getBody();
            if (body != null && !body.getStatements().isEmpty()) {
                copy.tag(body.getStatements().get(0), "methodBodyTag"); // NOI18N
            }
        }
    }
    
    static boolean checkPackagesForStarImport(String pkgName, CodeStyle cs) {
        for (String s : cs.getPackagesForStarImport()) {
            if (s.endsWith(".*")) { //NOI18N
                s = s.substring(0, s.length() - 2);
                if (pkgName.startsWith(s))
                    return true;
            } else if (pkgName.equals(s)) {
                return true;
            }           
        }
        return false;
    }
    
    private static final String GENERATED_METHOD_BODY = "Templates/Classes/Code/GeneratedMethodBody"; //NOI18N
    private static final String OVERRIDDEN_METHOD_BODY = "Templates/Classes/Code/OverriddenMethodBody"; //NOI18N
    private static final String LAMBDA_BODY = "Templates/Classes/Code/LambdaBody"; //NOI18N
    private static final String LAMBDA_EXPRESSION = "Templates/Classes/Code/LambdaExpression"; //NOI18N
    private static final String METHOD_RETURN_TYPE = "method_return_type"; //NOI18N
    private static final String DEFAULT_RETURN_TYPE_VALUE = "default_return_value"; //NOI18N
    private static final String SUPER_METHOD_CALL = "super_method_call"; //NOI18N
    private static final String METHOD_NAME = "method_name"; //NOI18N
    private static final String CLASS_NAME = "class_name"; //NOI18N
    private static final String SIMPLE_CLASS_NAME = "simple_class_name"; //NOI18N
    private static final String CLASS_KIND = "class_kind"; //NOI18N
    private static final String SCRIPT_ENGINE_ATTR = "javax.script.ScriptEngine"; //NOI18N    
    private static final String STRING_OUTPUT_MODE_ATTR = "com.sun.script.freemarker.stringOut"; //NOI18N
    private static ScriptEngineManager manager;

    private static String readFromTemplate(String pathToTemplate, Map<String, Object> values) throws IOException, ScriptException {
        FileObject template = FileUtil.getConfigFile(pathToTemplate);
        Charset sourceEnc = FileEncodingQuery.getEncoding(template);

        ScriptEngine eng = engine(template);
        ScriptContext context = eng.getContext();
        context.getBindings(ScriptContext.ENGINE_SCOPE).putAll(values);
        context.setAttribute(FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE);
        context.setAttribute(ScriptEngine.FILENAME, template.getNameExt(), ScriptContext.ENGINE_SCOPE);
        context.setAttribute(STRING_OUTPUT_MODE_ATTR, true, ScriptContext.ENGINE_SCOPE);

        try (Reader is = new InputStreamReader(template.getInputStream(), sourceEnc)) {
            return (String)eng.eval(is);
        }
    }

    private static ScriptEngine engine(FileObject fo) {
        Object obj = fo.getAttribute(SCRIPT_ENGINE_ATTR); // NOI18N
        if (obj instanceof ScriptEngine) {
            return (ScriptEngine) obj;
        }
        if (obj instanceof String) {
            synchronized (GeneratorUtilities.class) {
                if (manager == null) {
                    manager = Scripting.createManager();
                }
            }
            return manager.getEngineByName((String) obj);
        }
        return null;
    }
    
    private static boolean containsErrors(Tree tree) {
        Boolean b = new ErrorAwareTreeScanner<Boolean, Boolean>() {
            @Override
            public Boolean visitErroneous(ErroneousTree node, Boolean p) {
                return true;
            }

            @Override
            public Boolean reduce(Boolean r1, Boolean r2) {
                if (r1 == null)
                    r1 = false;
                if (r2 == null)
                    r2 = false;
                return r1 || r2;
            }

            @Override
            public Boolean scan(Tree node, Boolean p) {
                return p ? p : super.scan(node, p);
            }
        }.scan(tree, false);
        return b != null ? b : false;
    }

    static {
        GeneratorUtilitiesAccessor.setInstance(new GeneratorUtilitiesAccessor() {
            @Override
            public CompilationUnitTree addImports(GeneratorUtilities gu, CompilationUnitTree cut, List<? extends ImportTree> cutImports, Set<? extends Element> toImport) {
                return gu.addImports(cut, cutImports, toImport);
            }
        });
    }
}
