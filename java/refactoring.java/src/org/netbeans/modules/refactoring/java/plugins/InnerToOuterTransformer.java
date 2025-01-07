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
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class InnerToOuterTransformer extends RefactoringVisitor {

    private Element inner;
    private Element outer;
    private InnerToOuterRefactoring refactoring;
    private boolean isInInnerClass = false;
    private Set<Element> referencedPrivateElement;
    
    private Element getCurrentElement() {
        return workingCopy.getTrees().getElement(getCurrentPath());
    }

    public InnerToOuterTransformer(InnerToOuterRefactoring re) {
        this.refactoring = re;
    }

    @Override
    public void setWorkingCopy(WorkingCopy wc) throws ToPhaseException {
        super.setWorkingCopy(wc);
        this.inner = refactoring.getSourceType().resolveElement(wc);
        outer = wc.getElementUtilities().enclosingTypeElement(inner);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if (JavaPluginUtils.isSyntheticPath(workingCopy, getCurrentPath())) {
            return null;
        }
        Element current = getCurrentElement();
        if (current == null) {
            return null;
        }
        if (inner.equals(current)) {
            Tree newTree = make.setLabel(node, refactoring.getClassName());        
            rewrite(node, newTree);
        } else if (isThisReferenceToOuter() && isThisInInner()) {
            if (current.getModifiers().contains(Modifier.PRIVATE)) {
                referencedPrivateElement.add(current);
            }
            if (!workingCopy.getTypes().isSubtype(inner.asType(), workingCopy.getElementUtilities().enclosingTypeElement(current).asType())) {
                IdentifierTree m;
                if (refactoring.getReferenceName()==null || current.getModifiers().contains(Modifier.STATIC) || current.getKind().isClass() ||  current.getKind().isInterface() ) {
                    m = make.Identifier(outer.getSimpleName().toString() + "." + node.getName().toString()); // NOI18N
                } else {
                    m = make.Identifier(refactoring.getReferenceName() + "." + node.getName().toString()); // NOI18N
                }

                rewrite(node, m);
            }
        } else if (isInInnerClass) {
            GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy); // helper
            // #it is impossible to call GeneratorUtilities.importFQNs
            // for the whole nested class since the method creates new identity
            // of the passed tree
            if(current.getModifiers().contains(Modifier.STATIC) && current.getKind() == ElementKind.METHOD) {
                TreePath path = getCurrentPath();
                Tree parent = path.getParentPath() != null ? path.getParentPath().getLeaf() : null;
                TreePath elementPath = workingCopy.getTrees().getPath(current);
                JavaFileObject sourceFile = elementPath != null ? elementPath.getCompilationUnit().getSourceFile() : null;
                if (   (parent != null && parent.getKind() == Tree.Kind.CASE && ((CaseTree) parent).getExpression() == node && current.getKind() == ElementKind.ENUM_CONSTANT)
                    || path.getCompilationUnit().getSourceFile() == sourceFile) {
                    rewrite(node, make.Identifier(current.getSimpleName()));
                } else {
                    rewrite(node, make.QualIdent(current));
                }
            } else {
                Tree newTree = genUtils.importFQNs(node);
                rewrite(node, newTree);
            }
        }
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitCase(CaseTree node, Element p) {
        // Ignore case expression
        return super.scan(node.getStatements(), p);
    }

    @Override
    public Tree visitNewClass(NewClassTree arg0, Element arg1) {
        Element currentElement = getCurrentElement();
        if (refactoring.getReferenceName()!=null && currentElement!=null && workingCopy.getTypes().isSubtype(workingCopy.getElementUtilities().enclosingTypeElement(currentElement).asType(), inner.asType())) {
            String thisString;
            if (getCurrentClass()==inner) {
                thisString = refactoring.getReferenceName();
            } else if (workingCopy.getTypes().isSubtype(getCurrentClass().asType(),outer.asType())) {
                thisString = "this"; // NOI18N
            } else {
                TypeElement thisOuter = getOuter(getCurrentClass());
                if (thisOuter!=null) {
                    thisString = getOuter(getCurrentClass()).getQualifiedName().toString() + ".this"; // NOI18N
                }
                else {
                    thisString = "this"; // NOI18N
                }
            
            }
            if (thisString != null && currentElement instanceof ExecutableElement) {
                ExecutableElement constr = (ExecutableElement) currentElement;
                ExpressionTree enclosingExpression = arg0.getEnclosingExpression();
                boolean removeEnclosingExpression = false;
                if(enclosingExpression != null) {
                    Element enclosingElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), enclosingExpression));
                    if(enclosingElement != null && workingCopy.getTypes().isSameType(enclosingElement.asType(), outer.asType())) {
                        thisString = enclosingExpression.toString();
                        removeEnclosingExpression = true;
                    }
                }
                
                int index = constr.getParameters().size();
                if (constr.isVarArgs()) {
                    index--;
                }
                NewClassTree newClassTree = make.insertNewClassArgument(arg0, index, make.Identifier(thisString));
                if(removeEnclosingExpression) {
                    newClassTree = make.NewClass(null, (List<? extends ExpressionTree>)newClassTree.getTypeArguments(), newClassTree.getIdentifier(), newClassTree.getArguments(), newClassTree.getClassBody());
                }
                rewrite(arg0, newClassTree);
            }
        } else if (refactoring.getReferenceName() != null && currentElement != null
                // nested class will be moved to new file
                && outer != null && ((TypeElement) outer).getNestingKind() == NestingKind.TOP_LEVEL) {
            // 163852: inner class has to be treated especially; inner == nested + not static
            // translate all new NotMovingInner() -> referenceName.new NotMovingInner() in moved nested class
            // 198186: but only if the inner class is not enclosed in the movingInner
            Element enclElm = currentElement.getEnclosingElement();
            ExpressionTree primary = arg0.getEnclosingExpression();
            Element primaryElm = null;
            if (primary != null) {
                // be aware of Outer.this.new NotMovingInner() -> referenceName.new NotMovingInner()
                // and also new NotMovingInner().new NotMovingInnerInner() >referenceName.new NotMovingInner().new NotMovingInnerInner()
                primaryElm = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), primary));
                primaryElm = primaryElm != null ? workingCopy.getTypes().asElement(primaryElm.asType()) : null;
            }
            Element enclosing = enclElm;
            boolean enclosedByInner = false;
            while(enclosing != null) {
                if(enclosing == inner) {
                    enclosedByInner = true;
                    break;
                } else if(enclosing == outer) {
                    break;
                }
                enclosing = enclosing.getEnclosingElement();
            }
            if (enclElm != null && enclElm.getKind() == ElementKind.CLASS
                    && enclElm != inner && isInInnerClass && !enclosedByInner
                    && !enclElm.getModifiers().contains(Modifier.STATIC)
                    && ((TypeElement) enclElm).getNestingKind() == NestingKind.MEMBER
                    && (primaryElm == null && primary == null || primaryElm == outer)) {
                @SuppressWarnings("unchecked")
                NewClassTree nju = make.NewClass(
                        make.Identifier(refactoring.getReferenceName()),
                        (List<? extends ExpressionTree>) arg0.getTypeArguments(),
                        // new Outer.Inner() -> referenceName.new Inner()
                        make.Identifier(enclElm.getSimpleName()),
                        arg0.getArguments(),
                        arg0.getClassBody());
                rewrite(arg0, nju);
            }
        }
        return super.visitNewClass(arg0, arg1);
    }
    
    private TypeElement getOuter(TypeElement element) {
        while (element != null && !workingCopy.getTypes().isSubtype(element.asType(),outer.asType())) {
            element = workingCopy.getElementUtilities().enclosingTypeElement(element);
        }
        return element;
    }

    @Override
    public Tree visitMethod(MethodTree constructor, Element element) {
        if (constructor.getReturnType()==null) {
            //constructor
            if (refactoring.getReferenceName() != null && !inner.equals(getCurrentClass())) {
                Element current = getCurrentElement();
                if (current != null && workingCopy.getTypes().isSubtype(current.getEnclosingElement().asType(), inner.asType())) {
                    MemberSelectTree arg = make.MemberSelect(make.Identifier(getCurrentClass().getEnclosingElement().getSimpleName()), "this"); // NOI18N
                    MethodInvocationTree superCall = (MethodInvocationTree) ((ExpressionStatementTree)constructor.getBody().getStatements().get(0)).getExpression();
                    int index = hasVarArgs(constructor) ? constructor.getParameters().size() - 1 : 0;
                    MethodInvocationTree newSuperCall = make.insertMethodInvocationArgument(superCall, index, arg);
                    rewrite(superCall, newSuperCall);
                }
            }
            
        }
        return super.visitMethod(constructor, element);
    }

    @Override
    public Tree visitClass(ClassTree classTree, Element element) {
        Element currentElement = workingCopy.getTrees().getElement(getCurrentPath());
        if (currentElement == null) {
            return super.visitClass(classTree, element);
        }
        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy); // helper        
        if (currentElement!=null && currentElement == outer) {
            Element outerouter = outer.getEnclosingElement();
            Tree superVisit = super.visitClass(classTree, element);
            TreePath innerTp = workingCopy.getTrees().getPath(inner);
            if (innerTp==null) {
                //#194346
                return superVisit;
            }
            ClassTree innerClass = (ClassTree) innerTp.getLeaf();

            ClassTree newInnerClass = innerClass;
            newInnerClass = genUtils.importComments(newInnerClass, workingCopy.getCompilationUnit());

            newInnerClass = make.setLabel(newInnerClass, refactoring.getClassName());
            
            newInnerClass = refactorInnerClass(newInnerClass);
            
            TreePath outerPath = workingCopy.getTrees().getPath(outer);
            
            if (outerouter.getKind() == ElementKind.PACKAGE) {
                FileObject sourceRoot=ClassPath.getClassPath(workingCopy.getFileObject(), ClassPath.SOURCE).findOwnerRoot(workingCopy.getFileObject());
                ClassTree outerTree = (ClassTree) workingCopy.getTrees().getTree(outer);
                ClassTree newOuter = make.removeClassMember(outerTree, innerClass);
                rewrite(outerTree, newOuter);
                if(outerPath != null) {
                    JavaRefactoringUtils.cacheTreePathInfo(outerPath, workingCopy);
                }
                CompilationUnitTree compilationUnit = innerTp.getCompilationUnit();
                String relativePath = RefactoringUtils.getPackageName(compilationUnit).replace('.', '/') + '/' + refactoring.getClassName() + ".java"; // NOI18N
                CompilationUnitTree newCompilation = JavaPluginUtils.createCompilationUnit(sourceRoot, relativePath, newInnerClass, workingCopy, make);
                rewrite(null, newCompilation);
                return newOuter;
            } else {
                ClassTree outerTree = (ClassTree) workingCopy.getTrees().getTree(outer);
                if(!outerouter.getKind().isClass()) {
                    outerouter = workingCopy.getElementUtilities().enclosingTypeElement(outerouter);
                }
                ClassTree outerouterTree = (ClassTree) workingCopy.getTrees().getTree(outerouter);
                ClassTree newOuter = make.removeClassMember(outerTree, innerClass);
                ClassTree newOuterOuter = GeneratorUtilities.get(workingCopy).insertClassMember(outerouterTree, newInnerClass);
                rewrite(outerTree, newOuter);
                if(outerPath != null) {
                    JavaRefactoringUtils.cacheTreePathInfo(outerPath, workingCopy);
                }
                rewrite(outerouterTree, newOuterOuter);
                return newOuterOuter;
            }
            
        } else if (refactoring.getReferenceName() != null && currentElement!=null && workingCopy.getTypes().isSubtype(currentElement.asType(), inner.asType()) && currentElement!=inner) {
                VariableTree variable = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), refactoring.getReferenceName(), make.Type(outer.asType()), null);
                for (Tree member:classTree.getMembers()) {
                    if (member.getKind() == Tree.Kind.METHOD) {
                        MethodTree m = (MethodTree) member;
                        if (m.getReturnType()==null) {
                    
                            for( VariableTree var: m.getParameters() ) {
                                if( var.getName().contentEquals(refactoring.getReferenceName()) ) {
                                    problem = MoveTransformer.createProblem(problem, true, NbBundle.getMessage(InnerToOuterTransformer.class, "ERR_InnerToOuter_OuterNameClashSubtype", refactoring.getReferenceName(), refactoring.getClassName(), currentElement.getSimpleName()));
                                }
                            }

                            MethodInvocationTree superCall = (MethodInvocationTree) ((ExpressionStatementTree) m.getBody().getStatements().get(0)).getExpression();
                            List<ExpressionTree> newArgs = new ArrayList<ExpressionTree>(superCall.getArguments());
                            
                            MethodTree newConstructor = null;
                            ExpressionTree exprTree = (ExpressionTree)make.Identifier(variable.getName().toString());
                            if (hasVarArgs(m)) {
                                int index = m.getParameters().size() - 1;
                                newArgs.add(index, exprTree);
                                newConstructor = make.insertMethodParameter(m, index, variable);
                            } else {
                                newArgs.add(exprTree);
                                newConstructor = make.addMethodParameter(m, variable);
                            }
                            MethodInvocationTree method = make.MethodInvocation(
                                    Collections.<ExpressionTree>emptyList(), 
                                    make.Identifier("super"), // NOI18N
                                    newArgs);

                            BlockTree block = make.insertBlockStatement(m.getBody(), 0, make.ExpressionStatement(method));
                            block = make.removeBlockStatement(block, 1);
                            
                            newConstructor = make.Constructor(
                                    make.Modifiers(newConstructor.getModifiers().getFlags(), newConstructor.getModifiers().getAnnotations()),
                                    newConstructor.getTypeParameters(), 
                                    newConstructor.getParameters(),
                                    newConstructor.getThrows(),
                                    block);

                            rewrite(m, newConstructor);
                        }
                    }
                }                
            }

        if (currentElement == inner) {
            try {
                isInInnerClass = true;
                return super.visitClass(classTree, element);
            } finally {
                isInInnerClass = false;
            }
        }
        
        return super.visitClass(classTree, element);
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        this.referencedPrivateElement = new HashSet<Element>();

        Tree result = super.visitCompilationUnit(node, p);

        for (Element privEl : this.referencedPrivateElement) {
            problem = MoveTransformer.createProblem(problem, false, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "WRN_InnerToOuterRefToPrivate", privEl));
        }
        
        Trees trees = workingCopy.getTrees();
        CompilationUnitTree newNode = node;
        for (ImportTree imp : node.getImports()) {
            if(imp.isStatic()) {
                Tree qualIdent = imp.getQualifiedIdentifier();
                TypeElement el = workingCopy.getElements().getTypeElement(qualIdent.toString());
                if(inner.equals(el)) {
                    newNode = make.removeCompUnitImport(newNode, imp);
                }
            }
        }
        if(newNode != node) {
            rewrite(node, newNode);
        }
        return result;
    }
    
    private Problem problem;

    public Problem getProblem() {
        return problem;
    }

    private boolean containsImport(String imp) {
        for (ImportTree et:workingCopy.getCompilationUnit().getImports()) {
            if (et.getQualifiedIdentifier().toString().equals(imp)) {
                return true;
            }
        }
        return false;
    }
    

    @Override
    public Tree visitMemberSelect(MemberSelectTree memberSelect, Element element) {
        Element current = getCurrentElement();
        if (current == null) {
            return super.visitMemberSelect(memberSelect, inner);
        }
        if (inner.equals(current)) {
            ExpressionTree ex = memberSelect.getExpression();
            Tree newTree;
            if (ex.getKind() == Tree.Kind.IDENTIFIER) {
                newTree = make.Identifier(refactoring.getClassName());
                rewrite(memberSelect, newTree);
                TreePath tp = workingCopy.getTrees().getPath(inner);
                if(tp != null) {
                    String innerPackageName = RefactoringUtils.getPackageName(tp.getCompilationUnit());
                    if (!innerPackageName.equals(RefactoringUtils.getPackageName(workingCopy.getCompilationUnit())) &&
                            !containsImport(innerPackageName + ".*")) { //NOI18N
                        String import1 = innerPackageName + "." + refactoring.getClassName(); //NOI18N
                        try {
                            CompilationUnitTree cut = RefactoringUtils.addImports(workingCopy.getCompilationUnit(), Collections.singletonList(import1), make);
                            rewrite(workingCopy.getCompilationUnit(), cut);
                        } catch (IOException ex1) {
                            Exceptions.printStackTrace(ex1);
                        }
                    }
                }
            } else if (ex.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree m = make.MemberSelect(((MemberSelectTree) ex).getExpression(),refactoring.getClassName());
                rewrite(memberSelect,m);
            }
        } else if (isThisReferenceToOuter() && isThisInInner()) {
            if (current.getModifiers().contains(Modifier.PRIVATE)) {
                referencedPrivateElement.add(current);
            }
            if (!"class".equals(memberSelect.getIdentifier().toString()) && !current.getModifiers().contains(Modifier.STATIC)) { //NOI18N)
                if (refactoring.getReferenceName()!=null) {
                    // Outer.this -> outer
                    // Outer.field -> outer.field
                    Tree m = "this".equals(memberSelect.getIdentifier().toString()) // NOI18N
                            ? make.Identifier(refactoring.getReferenceName())
                            : make.MemberSelect(make.Identifier(refactoring.getReferenceName()), memberSelect.getIdentifier());
                    rewrite(memberSelect, m);
                } else {
                    if (inner.getKind()!=ElementKind.INTERFACE && !inner.getModifiers().contains(Modifier.STATIC)) {
                        problem = MoveTransformer.createProblem(problem, true, NbBundle.getMessage(InnerToOuterTransformer.class, "ERR_InnerToOuter_UseDeclareField", memberSelect));
                    }
                }
            }
        } else if (isThisReferenceToInner()) {
            //outer reference to inner class
            //member needn't to be private
            Tree tree = workingCopy.getTrees().getTree(current);
            if (tree != null && tree.getKind() == Tree.Kind.METHOD) {
                MethodTree method = (MethodTree) tree;
                if (method.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                    rewrite(method.getModifiers(), make.removeModifiersModifier(method.getModifiers(), Modifier.PRIVATE));
                }
            } else if (tree != null && tree.getKind() == Tree.Kind.VARIABLE) {
                VariableTree variable = (VariableTree) tree;
                if (variable.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                    rewrite(variable.getModifiers(), make.removeModifiersModifier(variable.getModifiers(), Modifier.PRIVATE));
                }
            }
            
        }
        
        return super.visitMemberSelect(memberSelect, element);
    }

    @Override
    public Tree visitLambdaExpression(LambdaExpressionTree node, Element p) {
        List<? extends VariableTree> params = node.getParameters();
        boolean skipType = params.size() > 0 && (params.get(0).getType() == null
                || workingCopy.getTreeUtilities().isSynthetic(new TreePath(getCurrentPath(), params.get(0).getType())));
        if(skipType) {
            return scan(node.getBody(), p);
        } else {
            return super.visitLambdaExpression(node, p);
        }
    }

    private boolean isThisReferenceToInner() {
        Element cur = getCurrentElement();
        if (cur==null || cur.getKind() == ElementKind.PACKAGE) {
            return false;
        }

        Tree innerTree = workingCopy.getTrees().getTree(inner);

        TreePath path = getCurrentPath();

        while (path!=null) {
            Tree t = path.getLeaf();
            if (t == innerTree) {
                return false;
            }
            path = path.getParentPath();
        }

        TypeElement encl = workingCopy.getElementUtilities().enclosingTypeElement(cur);
        return encl!=null && workingCopy.getTypes().isSubtype(encl.asType(), inner.asType()) ;
    }
    
    private boolean isThisReferenceToOuter() {
        Element cur = getCurrentElement();
        if (cur==null || cur.getKind() == ElementKind.PACKAGE) {
            return false;
        }
        TypeElement encl = workingCopy.getElementUtilities().enclosingTypeElement(cur);
        if (outer.equals(encl)) {
            TypeElement currentClass = getCurrentClass();
            if (currentClass == null) {
                return false;
            }
            if (workingCopy.getTypes().isSubtype(currentClass.asType(), inner.asType())) {
                return true;
            }
            return outer.equals(cur.getEnclosingElement());
        }
        return false;
    }

    private TypeElement getCurrentClass() {
        TreePath treePath = getCurrentPath();
        while (treePath != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(treePath.getLeaf().getKind())) {
                return (TypeElement) workingCopy.getTrees().getElement(treePath);
            } else if (treePath.getLeaf().getKind() == Tree.Kind.IMPORT) {
                return (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            }
            treePath = treePath.getParentPath();
        }
        return null;
    }


    private boolean isIn(Element el) {
        if (el==null) {
            return false;
        }
        Element current = el;
        while (current.getKind() != ElementKind.PACKAGE) {
            if (current.equals(inner)) {
                return true;
            }
            current = current.getEnclosingElement();
        }
        return false;
    }
    
    private boolean hasVarArgs(MethodTree mt) {
        List list = mt.getParameters();
        if (list.isEmpty()) {
            return false;
        }
        VariableTree vt = (VariableTree)list.get(list.size() - 1);
        return vt.toString().indexOf("...") != -1; // [NOI18N] [TODO] temporal hack, will be rewritten
    }
    
    private ClassTree refactorInnerClass(ClassTree innerClass) {
        ClassTree newInnerClass = innerClass;
        String referenceName = refactoring.getReferenceName();
        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
        final TypeMirror outerType;
        if(workingCopy.getElementUtilities().isLocal(outer) && (outer.getKind().isClass() || outer.getKind().isInterface())) {
            TypeElement outerTypeElement = (TypeElement) outer;
            List<? extends TypeMirror> interfaces = outerTypeElement.getInterfaces();
            if(interfaces.isEmpty()) {
                outerType = outerTypeElement.getSuperclass();
            } else {
                outerType = interfaces.get(0);
            }
        } else {
            outerType = outer.asType();
        }
        if (referenceName != null) {
            VariableTree variable = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)), refactoring.getReferenceName(), make.Type(outerType), null);
            newInnerClass = genUtils.insertClassMember(newInnerClass, variable);
        }
        
        ModifiersTree modifiersTree = newInnerClass.getModifiers();
        ModifiersTree newModifiersTree = make.removeModifiersModifier(modifiersTree, Modifier.PRIVATE);
        newModifiersTree = make.removeModifiersModifier(newModifiersTree, Modifier.STATIC);
        newModifiersTree = make.removeModifiersModifier(newModifiersTree, Modifier.PROTECTED);
        if (!outer.getModifiers().contains(Modifier.PUBLIC)) {
            newModifiersTree = make.removeModifiersModifier(newModifiersTree, Modifier.PUBLIC);
        }
        rewrite(modifiersTree, newModifiersTree);

        if (referenceName != null) {
            for (Tree member:newInnerClass.getMembers()) {
                if (member.getKind() == Tree.Kind.METHOD) {
                    MethodTree m = (MethodTree) member;
                    if (m.getName().contentEquals("<init>") || m.getReturnType() == null) {
                        VariableTree parameter = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), refactoring.getReferenceName(), make.Type(outerType), null);
                        MethodTree newConstructor = hasVarArgs(m) ?
                            make.insertMethodParameter(m, m.getParameters().size() - 1, parameter) : 
                            make.addMethodParameter(m, parameter);
                        
                        AssignmentTree assign = make.Assignment(make.Identifier("this."+referenceName), make.Identifier(referenceName)); // NOI18N
                        BlockTree block = make.insertBlockStatement(newConstructor.getBody(), 1, make.ExpressionStatement(assign));
                        Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
                        modifiers.addAll(newConstructor.getModifiers().getFlags());
                        modifiers.remove(Modifier.PRIVATE);
                        newConstructor = make.Constructor(
                                make.Modifiers(modifiers,newConstructor.getModifiers().getAnnotations()),
                                newConstructor.getTypeParameters(), 
                                newConstructor.getParameters(),
                                newConstructor.getThrows(),
                                block);

                        newInnerClass = make.removeClassMember(newInnerClass, m);
                        genUtils.copyComments(m, newConstructor, true);
                        genUtils.copyComments(m, newConstructor, false);
                        newInnerClass = genUtils.insertClassMember(newInnerClass, newConstructor);
                    }
                }
            }
        }
        
        if(innerClass != newInnerClass) {
            genUtils.copyComments(innerClass, newInnerClass, true);
            genUtils.copyComments(innerClass, newInnerClass, false);
        }
        return newInnerClass;
    }

    private boolean isThisInInner() {
        TreePath t=getCurrentPath();
        Tree innerTree = workingCopy.getTrees().getTree(inner);
        while (t!=null) {
            if (t.getLeaf().equals(innerTree)) {
                return true;
            }
            t = t.getParentPath();
        }
        return false;
    }
}
