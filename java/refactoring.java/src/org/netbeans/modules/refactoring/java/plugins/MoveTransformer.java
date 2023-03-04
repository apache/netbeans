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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.tree.*;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.SourceUtilsEx;
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
public class MoveTransformer extends RefactoringVisitor {

    private FileObject originalFolder;
    private MoveFileRefactoringPlugin move;
    private Set<Element> elementsToImport;
    private Set<ImportTree> importToRemove;
    private Set<String> importToAdd;
    private boolean isThisFileMoving;
    private boolean isThisFileReferencingOldPackage = false;
    private Problem problem;
    private boolean moveToDefaulPackageProblem = false;
    private String originalPackage;
    private SourceUtilsEx.Cache cacheOfSrcFiles = new SourceUtilsEx.Cache();
    private final Set<ElementHandle<TypeElement>> classes2Move;

    public Problem getProblem() {
        return problem;
    }

    public MoveTransformer(MoveFileRefactoringPlugin move) {
        super(true);
        this.move = move;
        classes2Move = move.classes;
    }
    
    @Override
    public void setWorkingCopy(WorkingCopy copy) throws ToPhaseException {
        super.setWorkingCopy(copy);
        originalFolder = workingCopy.getFileObject().getParent();
        originalPackage = RefactoringUtils.getPackageName(originalFolder);
        isThisFileMoving = move.filesToMove.contains(workingCopy.getFileObject());
        elementsToImport = new HashSet<Element>();
        isThisFileReferencingOldPackage = false;
        importToRemove = new HashSet<ImportTree>();
        importToAdd = new HashSet<String>();
    }
    
    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        if (!JavaPluginUtils.isSyntheticPath(workingCopy, getCurrentPath())) {
            final Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el != null) {
                if (isElementMoving(el)) {
                    //elementsAlreadyImported.add(el);
                    String newPackageName = getTargetPackageName(el);
                    
                    if (!"".equals(newPackageName)) { //
                        Tree nju = make.MemberSelect(make.Identifier(newPackageName), el);
                        rewrite(node, nju);
                    } else {
                        if (!moveToDefaulPackageProblem) {
                            problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_MovingClassToDefaultPackage"));
                            moveToDefaulPackageProblem = true;
                        }
                    }
                } else {
                    if (isThisFileMoving) {
                        if (el.getKind() != ElementKind.PACKAGE) {
                            Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);
                            
                            EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                            if(enclosingClassPath != null) {
                                Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                                if(enclosingTypeElement != null && enclosingClass != null
                                        && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                    neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                                }
                            }
                            
                            if(getPackageOf(el).toString().equals(originalPackage)
                                && (!(containsAnyOf(el, neededMods))
                                || (enclosingTypeElement!=null? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                && !move.filesToMove.contains(getFileObject(el))) {
                            problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature2",workingCopy.getFileObject().getName(),el, getTypeElement(el).getSimpleName()));
                        }
                        }
                    } else {
                        if (el.getKind()!=ElementKind.PACKAGE && el.getKind() != ElementKind.MODULE) {
                            Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);

                            EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                            if(enclosingClassPath != null) {
                                Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                                if(enclosingTypeElement != null && enclosingClass != null
                                        && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                    neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                                }
                            }

                            if(getPackageOf(el).toString().equals(originalPackage)
                                && (!(containsAnyOf(el, neededMods))
                                || (enclosingTypeElement!=null? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                && move.filesToMove.contains(getFileObject(el))) {
                            problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature",workingCopy.getFileObject().getName(),el, getTypeElement(el).getSimpleName()));
                        }
                        }
                    }
                }
            } else if (isPackageRename() && "*".equals(node.getIdentifier().toString())) { // NOI18N
                ExpressionTree exprTree = node.getExpression();
                TreePath exprPath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), exprTree);
                Element elem = workingCopy.getTrees().getElement(exprPath);
                if (elem != null && elem.getKind() == ElementKind.PACKAGE && isThisPackageMoving((PackageElement) elem)) {
                    String newPackageName = getTargetPackageName(elem);
                    Tree nju = make.MemberSelect(make.Identifier(newPackageName), "*"); // NOI18N
                    rewrite(node, nju);
                }
            }
        }
        return super.visitMemberSelect(node, p);
    }
    
    @Override
    public DocTree visitReference(ReferenceTree node, Element p) {
        DocTrees trees = workingCopy.getDocTrees();
        DocTreePath currentDocPath = getCurrentDocPath();
        Element el = currentDocPath != null ? trees.getElement(currentDocPath) : null;
        if(el != null) {
            if (isElementMoving(el)) {
                ReferenceTree newRef;
                ExpressionTree classReference = workingCopy.getTreeUtilities().getReferenceClass(currentDocPath);
                if(classReference != null && classReference.getKind() == Tree.Kind.MEMBER_SELECT) {
                    Name memberName = workingCopy.getTreeUtilities().getReferenceName(currentDocPath);
                    List<? extends Tree> methodParameters = workingCopy.getTreeUtilities().getReferenceParameters(currentDocPath);
                    String newPackageName = getTargetPackageName(el);
                    MemberSelectTree nju = make.MemberSelect(make.Identifier(newPackageName), el);
                    newRef = make.Reference(nju, memberName, methodParameters);
                    rewrite(currentDocPath.getTreePath().getLeaf(), node, newRef);
                }
            } else if (isPackageRename()
                    && el.getKind() == ElementKind.PACKAGE
                    && ((PackageElement) el).getQualifiedName().contentEquals(originalPackage)) {
                ReferenceTree newRef = make.Reference((ExpressionTree) make.Type(getTargetPackageName(el)),null, null);
                rewrite(currentDocPath.getTreePath().getLeaf(), node, newRef);
            }
        }
        return super.visitReference(node, p);
    }
    
    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if (!JavaPluginUtils.isSyntheticPath(workingCopy, getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el != null) {
                if (!isThisFileMoving) {
                    if (isElementMoving(el)) {
                        String targetPackageName = getTargetPackageName(el);
                        if (!RefactoringUtils.getPackageName(workingCopy.getCompilationUnit()).equals(targetPackageName)) {
                            elementsToImport.add(el);
                        }
                } else if (el.getKind() != ElementKind.PACKAGE && el.getKind() != ElementKind.MODULE) {
                        Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);
                        
                        EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                        if(enclosingClassPath != null) {
                            Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                            if(enclosingTypeElement != null && enclosingClass != null
                                    && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                            }
                        }

                        if (getPackageOf(el).toString().equals(originalPackage)
                                && (!(containsAnyOf(el, neededMods))
                                || (enclosingTypeElement!=null? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                && move.filesToMove.contains(getFileObject(el))) {
                            problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature", workingCopy.getFileObject().getName(), el, getTypeElement(el).getSimpleName()));
                        }
                    }
                } else {
                    Boolean[] isElementMoving = new Boolean[1];
                    if (isTopLevelClass(el) && !isElementMoving(el, isElementMoving)
                            && getPackageOf(el).toString().equals(originalPackage)) {
                        importToAdd.add(el.toString());
                        isThisFileReferencingOldPackage = true;
                    }
                    if (el.getKind() != ElementKind.PACKAGE) {
                        Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);
                        
                        EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                        if (enclosingClassPath != null) {
                            Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                            if (enclosingTypeElement != null && enclosingClass != null
                                    && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                            }
                        }
                        
                        if (getPackageOf(el).toString().equals(originalPackage)
                                && (!(containsAnyOf(el, neededMods))
                                || (enclosingTypeElement!=null? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                && !isElementMoving(el, isElementMoving)
                                && !move.filesToMove.contains(getFileObject(el))) {
                            problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature2", workingCopy.getFileObject().getName(), el, getTypeElement(el).getSimpleName()));
                        }
                    }
                }
            }
        }
        
        return super.visitIdentifier(node, p);
    }

    private FileObject getFileObject(Element el) {
        return SourceUtilsEx.getFile(el, workingCopy.getClasspathInfo(), cacheOfSrcFiles);
    }

    private boolean isThisPackageMoving(PackageElement el) {
        return move.packageNames.contains(el.getQualifiedName().toString());
    }

    private String getTargetPackageName(Element el) {
        return move.getTargetPackageName(getFileObject(el));
    }
    
    private TypeElement getTypeElement(Element e) {
        TypeElement t = workingCopy.getElementUtilities().enclosingTypeElement(e);
        if (t==null && (e.getKind().isClass() || e.getKind().isInterface())) {
            return (TypeElement) e;
        }
        return t;
    }
    static final Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        }
        problem.setNext(result);
        return problem;
    }
    
    
    private PackageElement getPackageOf(Element el) {
        //return workingCopy.getElements().getPackageOf(el);
        while (el.getKind() != ElementKind.PACKAGE) {
            el = el.getEnclosingElement();
        }
        return (PackageElement) el;
    }

    private boolean isPackageRename() {
        return move.isRenameRefactoring;
    }
    
    private boolean isElementMoving(Element el, Boolean[] cache) {
        if (cache[0] == null) {
            cache[0] = isElementMoving(el);
        }

        return cache[0];
    }

    private boolean isElementMoving(Element el) {
        ElementKind kind = el.getKind();
        if (!(kind.isClass() || kind.isInterface())) {
            return false;
        }
        ElementHandle<Element> elHandle = ElementHandle.create(el);
        return classes2Move.contains(elHandle);
    }
    
    private boolean isTopLevelClass(Element el) {
        return (el.getKind().isClass() || 
                el.getKind().isInterface()) &&
                el.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }
    
    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        Tree result = super.visitCompilationUnit(node, p);
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return result;
        }
        CompilationUnitTree cut = node;
        List<? extends ImportTree> imports = cut.getImports();
        if (!importToRemove.isEmpty()) {
            List<ImportTree> temp = new ArrayList<ImportTree>(imports);
            temp.removeAll(importToRemove);
            imports = temp;
        }
        if (isThisFileMoving) {
            // change package statement if old and new package exist, i.e.
            // neither old nor new package is default
            String newPckg = move.getTargetPackageName(workingCopy.getFileObject());
            if (node.getPackageName() != null && !"".equals(newPckg)) {
                if (importToRemove.isEmpty()) {
                    rewrite(node.getPackageName(), make.Identifier(newPckg));
                } else {
                    cut = make.CompilationUnit(node.getPackageAnnotations(), make.Identifier(newPckg), imports, node.getTypeDecls(), node.getSourceFile());
                }
            } else {
                // in order to handle default package, we have to rewrite whole
                // compilation unit:
                cut = make.CompilationUnit(
                        node.getPackageAnnotations(),
                        "".equals(newPckg) ? null : make.Identifier(newPckg),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
            }
            if (isThisFileReferencingOldPackage) {
                //add import to old package
                ExpressionTree newPackageName = cut.getPackageName();
                if (newPackageName != null) {
                    try {
                        cut = RefactoringUtils.addImports(cut, new LinkedList<String>(importToAdd), make);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    if (!moveToDefaulPackageProblem) {
                        problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_MovingClassToDefaultPackage"));
                        moveToDefaulPackageProblem = true;
                    }
                }
                      
            }
        } else if (!importToRemove.isEmpty()) {
            cut = make.CompilationUnit(node.getPackageName(), imports, node.getTypeDecls(), node.getSourceFile());
        }

        for (Element el:elementsToImport) {
            String newPackageName = getTargetPackageName(el);
            if (!"".equals(newPackageName)) { // NOI18N
                cut = insertImport(cut, newPackageName + "." +el.getSimpleName(), el, newPackageName); // NOI18N
            }
        }
        rewrite(node, cut);
        return result;
    }

    private CompilationUnitTree insertImport(CompilationUnitTree node, String imp, Element orig, String targetPkgOfOrig) {
        for (ImportTree tree: node.getImports()) {
            if (tree.getQualifiedIdentifier().toString().equals(imp) || tree.getQualifiedIdentifier().toString().equals(((TypeElement) orig).getQualifiedName().toString())) {
                return node;
            }
            if (orig!=null) {
                if (tree.getQualifiedIdentifier().toString().equals(getPackageOf(orig).getQualifiedName()+".*") && isPackageRename()) { // NOI18N
                    rewrite(tree.getQualifiedIdentifier(), make.Identifier(targetPkgOfOrig + ".*")); // NOI18N
                    return node;
                }
            }
        }
        CompilationUnitTree nju = make.insertCompUnitImport(node, 0, make.Import(make.Identifier(imp), false));
        return nju;
    }

    @Override
    public Tree visitImport(ImportTree node, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Tree qualifiedIdentifier = node.getQualifiedIdentifier();
            final Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), node.getQualifiedIdentifier()));
            if (el != null) {
                if (isElementMoving(el)) {
                    String newPackageName = getTargetPackageName(el);

                    if (!"".equals(newPackageName)) {
                        String cuPackageName = RefactoringUtils.getPackageName(workingCopy.getCompilationUnit());
                        if (cuPackageName.equals(newPackageName)) { //remove newly created import from same package
                            importToRemove.add(node);
                            return node;
                        }
                    }
                }
            } else if(qualifiedIdentifier.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree memberSelect = (MemberSelectTree) qualifiedIdentifier;
                if(memberSelect.getIdentifier().contentEquals("*")) {
                    Element packageElement = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), memberSelect.getExpression()));
                    if(packageElement != null && packageElement.getKind() == ElementKind.PACKAGE) {
                        PackageElement pakketje = (PackageElement) packageElement;
                        if(isThisPackageMoving(pakketje)) {
                            importToRemove.add(node);
                        } else if(move.packages.contains(ElementHandle.create(pakketje))) {
                            boolean packageWillBeEmpty = true;
                            List<? extends Element> enclosedElements = pakketje.getEnclosedElements();
                            for (Element element : enclosedElements) {
                                if(!isElementMoving(element)) {
                                    packageWillBeEmpty = false;
                                    break;
                                } else {
                                    String targetPackageName = getTargetPackageName(element);
                                    if(pakketje.getQualifiedName().contentEquals(targetPackageName)) {
                                        packageWillBeEmpty = false;
                                        break;
                                    }
                                }
                            }
                            if(packageWillBeEmpty) {
                                importToRemove.add(node);
                            }
                        }
                    }
                }
            }
        }
        return super.visitImport(node, p);
    }

    @Override
    public Tree visitExports(ExportsTree node, Element p) {
        renamePackage(node.getPackageName());
        return super.visitExports(node, p);
    }

    @Override
    public Tree visitOpens(OpensTree node, Element p) {
        renamePackage(node.getPackageName());
        return super.visitOpens(node, p);
    }

    private void renamePackage(ExpressionTree packageName) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            final Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), packageName));
            if (el != null && el.getKind() == ElementKind.PACKAGE && isThisPackageMoving((PackageElement)el)) {
                Tree nju = make.Identifier(getTargetPackageName(el));
                rewrite(packageName, nju);
            }
        }
    }

    private boolean containsAnyOf(Element el, EnumSet<Modifier> neededMods) {
        for (Modifier mod : neededMods) {
            if(el.getModifiers().contains(mod)) {
                return true;
            }
        }
        return false;
    }
    
}
