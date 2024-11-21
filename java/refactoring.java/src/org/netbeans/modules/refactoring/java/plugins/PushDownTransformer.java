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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 * @author Ralph Benjamin Ruijs
 */
public class PushDownTransformer extends RefactoringVisitor {

    private final FileObject originFile;
    private final MemberInfo<ElementHandle<? extends Element>>[] members;
    private Problem problem;
    private boolean inSuperClass;
 
    public Problem getProblem() {
        return problem;
    }

    public PushDownTransformer(FileObject originFile, MemberInfo<ElementHandle<? extends Element>> members[]) {
        this.originFile = originFile;
        this.members = members;
        this.translateQueue = new LinkedList<>();
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        SourceUtils.forceSource(workingCopy, originFile);
        super.setWorkingCopy(workingCopy); //To change body of generated methods, choose Tools | Templates.
    }

    private final Deque<Map<Tree, Tree>> translateQueue;
    @Override
    public Tree visitClass(ClassTree node, Element source) {
        final GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
        final TreePath classPath = getCurrentPath();
        
        ClassTree classTree = node;
        translateQueue.addLast(new HashMap<Tree, Tree>());
        
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        inSuperClass = el.equals(source);

        Tree value = super.visitClass(classTree, source);
        
        if (inSuperClass) {
            classTree = rewriteSuperClass(el, classTree, genUtils);
        } else {
            TypeMirror tm = el.asType();
            Types types = workingCopy.getTypes();
            Trees trees = workingCopy.getTrees();
            if (types.isSubtype(types.erasure(tm), types.erasure(source.asType()))) {
                classTree = rewriteSubClass(el, source, genUtils, trees, node);
            }
        }
        Map<Tree, Tree> original2Translated = translateQueue.pollLast();
        classTree = (ClassTree) workingCopy.getTreeUtilities().translate(classTree, original2Translated);
        if (/* final boolean notTopLevel = classPath.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT; */
            !translateQueue.isEmpty()) {
            translateQueue.getLast().put(node, classTree);
        } else {        
            rewrite(node, classTree);
        }
        return value;
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element source) {
        // Check if from visitClass and return changed tree, otherwise rewrite
        final Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (el == null) {
            // fail fast
            return super.visitMemberSelect(node, source);
        }
        for (int i = 0; i<members.length; i++) {
            Element member = members[i].getElementHandle().resolve(workingCopy);
            if (el.equals(member)) {
                String isSuper = node.getExpression().toString();
                if (isSuper.equals("super") || isSuper.endsWith(".super")) { //NOI18N
                    
                    Scope scope = workingCopy.getTrees().getScope(getCurrentPath());
                    Iterable<? extends Element> localMembersAndVars = workingCopy.getElementUtilities().getLocalMembersAndVars(scope, new ElementUtilities.ElementAcceptor() {
                        
                        private final Set<ElementKind> VARIABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
                        private final Set<ElementKind> METHOD_KINDS = EnumSet.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR);
                        
                        @Override
                        public boolean accept(Element e, TypeMirror type) {
                            if(e == el) {
                                return false;
                            }
                            if(VARIABLE_KINDS.contains(el.getKind())) {
                                return VARIABLE_KINDS.contains(e.getKind()) && e.getSimpleName().contentEquals(el.getSimpleName());
                            } else {
                                return METHOD_KINDS.contains(e.getKind()) && e.getSimpleName().contentEquals(el.getSimpleName());
                            }
                        }
                    });
                    String ident = node.getIdentifier().toString();
                    
                    if(localMembersAndVars.iterator().hasNext()) {
                        ident = "this." + ident;
                    }
                    // In inner class?
                    TreePath enclosingMethod = JavaPluginUtils.findMethod(getCurrentPath());
                    Element methodEl = workingCopy.getTrees().getElement(enclosingMethod);
                    TypeElement enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(methodEl);
                    TypeMirror tm = enclosingTypeElement.asType();
                    Types types = workingCopy.getTypes();
                    if (!types.isSubtype(types.erasure(tm), types.erasure(source.asType()))) {
                        while(enclosingTypeElement != null && !types.isSubtype(types.erasure(enclosingTypeElement.asType()), types.erasure(source.asType()))) {
                            enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(enclosingTypeElement);
                        }
                        if(enclosingTypeElement != null) {
                            ident = enclosingTypeElement.getSimpleName().toString() + "." + ident;
                        }
                    }
                    translateQueue.getLast().put(node, make.Identifier(ident));
                }
                break;
            }
        }
        return super.visitMemberSelect(node, source);
    }

    @NbBundle.Messages({"# {0} - Member", "# {1} - Type", "ERR_PushDown_UsedInSuper={0} is referenced by {1}."})
    @Override
    public Tree visitIdentifier(IdentifierTree node, Element source) {
        // Check if from visitClass and return changed tree, otherwise rewrite
        if(inSuperClass) {
            final Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if(el != null) {
                for (int i = 0; i<members.length; i++) {
                    if(members[i].getGroup() != MemberInfo.Group.IMPLEMENTS && !members[i].isMakeAbstract()) {
                        Element member = members[i].getElementHandle().resolve(workingCopy);
                        if (el.equals(member)) {
                            problem = MoveTransformer.createProblem(problem, false, ERR_PushDown_UsedInSuper(member.getSimpleName(), source.getSimpleName()));
                        }
                    }
                }
            }
        }
        return super.visitIdentifier(node, source);
    }

    @Override
    public Tree visitMethod(MethodTree node, Element p) {
        // Do not scan methods that are being moved
        if(inSuperClass) {
            final Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if(el != null) {
                for (int i = 0; i<members.length; i++) {
                    if(members[i].getGroup() != MemberInfo.Group.IMPLEMENTS) {
                        Element member = members[i].getElementHandle().resolve(workingCopy);
                        if (el.equals(member)) {
                            return node;
                        }
                    }
                }
            }
        }
        return super.visitMethod(node, p);
    }

    @NbBundle.Messages({"# {0} - Member", "# {1} - Type", "ERR_PushDown_AlreadyExists={0} already exists in {1}."})
    private ClassTree rewriteSubClass(Element el, Element source, GeneratorUtilities genUtils, Trees trees, ClassTree tree) throws MissingResourceException {
        ClassTree njuClass = tree;
        boolean makeClassAbstract = false;
        for (int i = 0; i<members.length; i++) {
            Element member = members[i].getElementHandle().resolve(workingCopy);
            if (members[i].getGroup()==MemberInfo.Group.IMPLEMENTS) {
                if (((TypeElement) el).getInterfaces().contains(member.asType())) {
                    problem = MoveTransformer.createProblem(problem, false, ERR_PushDown_AlreadyExists(member.getSimpleName(), el.getSimpleName()));
                }
                njuClass = make.addClassImplementsClause(njuClass, make.QualIdent(member));
            } else if (members[i].getGroup()==MemberInfo.Group.METHOD
                    && member.getModifiers().contains(Modifier.ABSTRACT) && el.getKind().isClass() && source.getKind().isInterface()) {
                // moving abstract method from interface to class
                if (RefactoringUtils.elementExistsIn((TypeElement) el, member, workingCopy)) {
                    problem = MoveTransformer.createProblem(problem, false, ERR_PushDown_AlreadyExists(member.getSimpleName(), el.getSimpleName()));
                }
                TreePath path = workingCopy.getTrees().getPath(member);
                MethodTree methodTree = (MethodTree) path.getLeaf();
                methodTree = genUtils.importComments(methodTree, path.getCompilationUnit());
                ModifiersTree mods = RefactoringUtils.makeAbstract(make, methodTree.getModifiers());
                mods = make.addModifiersModifier(mods, Modifier.PUBLIC);
                MethodTree njuMethod = make.Method(
                        mods,
                        methodTree.getName(),
                        methodTree.getReturnType(),
                        methodTree.getTypeParameters(),
                        methodTree.getParameters(),
                        methodTree.getThrows(),
                        (BlockTree) null,
                        null);
                genUtils.copyComments(methodTree, njuMethod, true);
                genUtils.copyComments(methodTree, njuMethod, false);
                njuClass = genUtils.insertClassMember(njuClass, njuMethod);
                makeClassAbstract = true;
            } else {
                if (RefactoringUtils.elementExistsIn((TypeElement) el, member, workingCopy)) {
                    problem = MoveTransformer.createProblem(problem, false, org.openide.util.NbBundle.getMessage(PushDownTransformer.class, "ERR_PushDown_AlreadyExists", member.getSimpleName(), el.getSimpleName()));
                }
                TreePath path = workingCopy.getTrees().getPath(member);
                Tree memberTree = path.getLeaf();
                memberTree = genUtils.importComments(memberTree, path.getCompilationUnit());
                List<Comment> comments = workingCopy.getTreeUtilities().getComments(memberTree, true);
                if(comments.isEmpty()) {
                    comments = workingCopy.getTreeUtilities().getComments(memberTree, false);
                }
                memberTree = genUtils.importFQNs(memberTree);
                if (members[i].isMakeAbstract() && memberTree.getKind() == Tree.Kind.METHOD && member.getModifiers().contains((Modifier.PRIVATE))) {
                    MethodTree oldOne = (MethodTree) memberTree;
                    MethodTree m = make.Method(
                            make.addModifiersModifier(make.removeModifiersModifier(oldOne.getModifiers(), Modifier.PRIVATE), Modifier.PROTECTED),
                            oldOne.getName(),
                            oldOne.getReturnType(),
                            oldOne.getTypeParameters(),
                            oldOne.getParameters(),
                            oldOne.getThrows(),
                            oldOne.getBody(),
                            (ExpressionTree) oldOne.getDefaultValue());
                    genUtils.copyComments(memberTree, m, true);
                    genUtils.copyComments(memberTree, m, false);
                    njuClass = genUtils.insertClassMember(njuClass, m);
                } else if(memberTree.getKind() == Tree.Kind.METHOD) {
                    MethodTree oldOne = (MethodTree) memberTree;
                    Tree returnType = oldOne.getReturnType();
                    TreePath returnTypePath = new TreePath(path, returnType);
                    Element returnEl = trees.getElement(returnTypePath);
                    if(returnEl != null && returnEl.getKind() != ElementKind.TYPE_PARAMETER) {
                        returnType = make.QualIdent(returnEl);
                    }
                    List<ExpressionTree> aThrows = new ArrayList<>(oldOne.getThrows().size());
                    for (ExpressionTree thrw : oldOne.getThrows()) {
                        TreePath thrwPath = new TreePath(path, thrw);
                        Element thrwEl = trees.getElement(thrwPath);
                        if(thrwEl != null && thrwEl.getKind() != ElementKind.TYPE_PARAMETER) {
                            aThrows.add(make.QualIdent(thrwEl));
                        } else {
                            aThrows.add(thrw);
                        }
                    }
                    TreePath mpath = workingCopy.getTrees().getPath(member);
                    ExecutableElement overriddenMethod = workingCopy.getElementUtilities().getOverriddenMethod((ExecutableElement) member);
                    MethodTree m = make.Method(
                            overriddenMethod != null && workingCopy.getElementUtilities().isMemberOf(overriddenMethod, (TypeElement) el)? oldOne.getModifiers() : PullUpTransformer.removeAnnotations(workingCopy, make, oldOne.getModifiers(), mpath),
                            oldOne.getName(), returnType,
                            oldOne.getTypeParameters(),
                            oldOne.getParameters(), aThrows,
                            oldOne.getBody(),
                            (ExpressionTree) oldOne.getDefaultValue());
                    genUtils.copyComments(memberTree, m, true);
                    genUtils.copyComments(memberTree, m, false);
                    njuClass = genUtils.insertClassMember(njuClass, m);
                } else {
                    njuClass = genUtils.insertClassMember(njuClass, memberTree);
                }
                makeClassAbstract |= member.getModifiers().contains(Modifier.ABSTRACT);
            }
        }

        if (makeClassAbstract && !njuClass.getModifiers().getFlags().contains(Modifier.ABSTRACT) && (njuClass.getKind() != Tree.Kind.INTERFACE)) {
            // make enclosing class abstract if necessary
            njuClass = make.Class(RefactoringUtils.makeAbstract(make,
                    njuClass.getModifiers()), njuClass.getSimpleName(),
                    njuClass.getTypeParameters(), njuClass.getExtendsClause(),
                    njuClass.getImplementsClause(), njuClass.getPermitsClause(),
                    njuClass.getMembers());
        }

        return njuClass;
    }

    private ClassTree rewriteSuperClass(Element el, ClassTree tree, GeneratorUtilities genUtils) {
        boolean classIsAbstract = el.getKind().isInterface();
        ClassTree njuClass = tree;
        
        // Remove implements
        for (Tree t: tree.getImplementsClause()) {
            Element currentInterface = workingCopy.getTrees().getElement(TreePath.getPath(getCurrentPath(), t));
            if (currentInterface == null) {
                continue;
            }
            for (int i=0; i<members.length; i++) {
                if (members[i].getGroup()==MemberInfo.Group.IMPLEMENTS && currentInterface.equals(members[i].getElementHandle().resolve(workingCopy))) {
                    njuClass = make.removeClassImplementsClause(njuClass, t);
                }
            }
        }
        
        for (Tree t: njuClass.getMembers()) {
            Element current = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), t));
            for (int i=0; i<members.length; i++) {
                if (members[i].getGroup()!=MemberInfo.Group.IMPLEMENTS && current.equals(members[i].getElementHandle().resolve(workingCopy))) {
                    if (members[i].isMakeAbstract()) {
                        if (el.getKind().isClass()) {
                            
                            if (!classIsAbstract) {
                                classIsAbstract = true;
                                Set<Modifier> mod = EnumSet.of(Modifier.ABSTRACT);
                                mod.addAll(njuClass.getModifiers().getFlags());
                                ModifiersTree modifiers = make.Modifiers(mod);
                                translateQueue.getLast().put(njuClass.getModifiers(), modifiers);
                            }
                            
                            MethodTree method = (MethodTree) t;
                            Set<Modifier> mod = EnumSet.of(Modifier.ABSTRACT);
                            mod.addAll(method.getModifiers().getFlags());

                            if(mod.contains(Modifier.PRIVATE)) {
                                mod.remove(Modifier.PRIVATE);
                                mod.add(Modifier.PROTECTED);
                            }
                            MethodTree nju = make.Method(
                                    make.Modifiers(mod),
                                    method.getName(),
                                    method.getReturnType(),
                                    method.getTypeParameters(),
                                    method.getParameters(),
                                    method.getThrows(),
                                    (BlockTree) null,
                                    (ExpressionTree)method.getDefaultValue());
                            genUtils.copyComments(method, nju, true);
                            genUtils.copyComments(method, nju, false);
                            translateQueue.getLast().put(method, nju);
                        }
                    } else {
                        njuClass = make.removeClassMember(njuClass, t);
                    }
                    fixVisibility(current);
                }
            }
        }
        return njuClass;
    }
    
    void fixVisibility(final Element el) {
        if (el.getKind() != ElementKind.METHOD) {
            return;
        }
        
        new ErrorAwareTreePathScanner() {

            @Override
            public Object visitIdentifier(IdentifierTree node, Object p) {
                check();
                return super.visitIdentifier(node, p);
            }

            @Override
            public Object visitMemberSelect(MemberSelectTree node, Object p) {
                check();
                return super.visitMemberSelect(node, p);
            }

            private void check() throws IllegalArgumentException {
                Element thisElement = workingCopy.getTrees().getElement(getCurrentPath());
                if (thisElement != null && thisElement.getKind()!=ElementKind.PACKAGE && workingCopy.getElementUtilities().enclosingTypeElement(thisElement) == el.getEnclosingElement()) {
                    Tree tree = workingCopy.getTrees().getTree(thisElement);
                    if (thisElement.getKind().isField() && tree!=null) {
                        makeProtectedIfPrivate(((VariableTree) tree).getModifiers());
                    } else if (thisElement.getKind() == ElementKind.METHOD) {
                        makeProtectedIfPrivate(((MethodTree) tree).getModifiers());
                    } else if (thisElement.getKind().isClass() || thisElement.getKind().isInterface()) {
                        makeProtectedIfPrivate(((ClassTree) tree).getModifiers());
                    }
                }
            }
            
            private void makeProtectedIfPrivate(ModifiersTree modTree) {
                if (modTree.getFlags().contains(Modifier.PRIVATE)) {
                    ModifiersTree newMods = workingCopy.getTreeMaker().removeModifiersModifier(modTree, Modifier.PRIVATE);
                    newMods = workingCopy.getTreeMaker().addModifiersModifier(newMods, Modifier.PROTECTED);
                    rewrite(modTree, newMods);
                }
            }
            
        }.scan(workingCopy.getTrees().getPath(el), null);
    }
}

