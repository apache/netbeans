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
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 */
public class MoveClassTransformer extends RefactoringVisitor {

    private Problem problem;
    private final ElementHandle<TypeElement> elementHandle;
    private final URL targetURL;
    private boolean inMovingClass;
    private String targetPackageName;
    private String targetFqn;
    private boolean moveToDefaulPackageProblem = false;
    private String originalPackage;
    private Map<Tree, Tree> original2Translated;
    private Set<ImportTree> importToRemove;
    private boolean isThisFileReferencingOldPackage = false;
    private final ElementHandle<TypeElement> targetHandle;
    private final FileObject targetFile;
    private final FileObject sourceFile;
    private boolean deleteFile;

    public MoveClassTransformer(TreePathHandle elementHandle, URL targetURL) {
        this(elementHandle.getElementHandle(), targetURL, null, null, null);
        this.targetPackageName = RefactoringUtils.getPackageName(targetURL);
    }
    
    public MoveClassTransformer(TreePathHandle elementHandle, ElementHandle<TypeElement> targetHandle, FileObject targetFile, FileObject sourceFile) {
        this(elementHandle.getElementHandle(), null, targetHandle, targetFile, sourceFile);
    }
    
    public MoveClassTransformer(ElementHandle<TypeElement> elementHandle, ElementHandle<TypeElement> targetHandle, FileObject targetFile, FileObject sourceFile) {
        this(elementHandle, null, targetHandle, targetFile, sourceFile);
    }
    
    private MoveClassTransformer(ElementHandle<TypeElement> elementHandle, URL targetURL, ElementHandle<TypeElement> targetHandle, FileObject targetFile, FileObject sourceFile) {
        this.elementHandle = elementHandle;
        this.targetURL = targetURL;
        this.targetHandle = targetHandle;
        this.targetFile = targetFile;
        this.sourceFile = sourceFile;
    }

    Problem getProblem() {
        return problem;
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        if (sourceFile != null) {
            SourceUtils.forceSource(workingCopy, sourceFile);
        }
        super.setWorkingCopy(workingCopy);
        final TypeElement element = this.elementHandle.resolve(workingCopy);
        this.originalPackage = getPackageOf(element).getQualifiedName().toString();
        if(this.targetHandle != null) {
            final TypeElement targetElement = this.targetHandle.resolve(workingCopy);
            this.targetFqn = targetElement.getQualifiedName().toString() + "." + element.getSimpleName();
            this.targetPackageName = getPackageOf(targetElement).getQualifiedName().toString();
        } else {
            this.targetFqn = RefactoringUtils.getPackageName(targetURL) + "." + element.getSimpleName();
        }
        this.importToRemove = new HashSet<ImportTree>();
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        original2Translated = new HashMap<Tree, Tree>();
        boolean deleteThisFile = false;
        
        Tree result = super.visitCompilationUnit(node, p);

        List<? extends Tree> typeDecls = node.getTypeDecls();
        CompilationUnitTree cut = node;
        for (Tree clazz : typeDecls) {
            TypeMirror type = workingCopy.getTrees().getTypeMirror(new TreePath(getCurrentPath(), clazz));
            TypeMirror sourceType = elementHandle.resolve(workingCopy).asType();
            if (type != null) {
                if(sourceType != null && workingCopy.getTypes().isSameType(type, sourceType)) {
                    if(typeDecls.size() > 1) {
                        cut = make.removeCompUnitTypeDecl(cut, clazz);
                    } else {
                        deleteFile = deleteThisFile = true;
                    }
                    if (targetURL != null) {
                        createNewFileForType(cut, clazz, node);
                    }
                }
            }
        }
        final TreeUtilities treeUtilities = workingCopy.getTreeUtilities();

        if (deleteThisFile || treeUtilities.isSynthetic(getCurrentPath())) {
            return result;
        }
        
        cut = (CompilationUnitTree) treeUtilities.translate(cut, original2Translated);

        List<? extends ImportTree> imports = cut.getImports();
        if (!importToRemove.isEmpty()) {
            List<ImportTree> temp = new ArrayList<ImportTree>(imports);
            temp.removeAll(importToRemove);
            imports = temp;
        }
        if (!importToRemove.isEmpty()) {
            cut = make.CompilationUnit(cut.getPackageName(), imports, cut.getTypeDecls(), cut.getSourceFile());
        }
        
        rewrite(node, cut);
        
        return result;
    }

    @Override
    public Tree visitClass(ClassTree node, Element p) {
        final TreePath currentPath = getCurrentPath();
        Element element = workingCopy.getTrees().getElement(currentPath);
        if (isTopLevelClass(element)) {
            inMovingClass = element == elementHandle.resolve(workingCopy);
        }
        if(targetHandle != null) {
            TypeMirror type = workingCopy.getTrees().getTypeMirror(currentPath);
            TypeMirror targetType = targetHandle.resolve(workingCopy).asType();
            if(targetType != null && workingCopy.getTypes().isSameType(type, targetType)) {
                final Element resolved = elementHandle.resolve(workingCopy);
                if(resolved != null) {
                    final TreePath resolvedPath = workingCopy.getTrees().getPath(resolved);
                    final GeneratorUtilities get = GeneratorUtilities.get(workingCopy);
                    
                    ClassTree classTree = (ClassTree) workingCopy.getTrees().getTree(resolved);
                    ClassTree origTree = get.importComments(classTree, workingCopy.getTrees().getPath(resolved).getCompilationUnit());
                    final Map<Tree, Tree> org2trans = new HashMap<Tree, Tree>();
                    ErrorAwareTreeScanner<Object, Element> scanner= new ErrorAwareTreeScanner<Object, Element>() {

                        @Override
                        public Object visitIdentifier(IdentifierTree node, Element p) {
                            Element el = workingCopy.getTrees().getElement(new TreePath(resolvedPath, node));
                            if(el != null) {
                                if (isElementMoving(el)) {
                                    ExpressionTree newIdent = make.QualIdent(targetFqn);
                                    org2trans.put(node, newIdent);
                                } else if(isParentElementMoving(el)) {
//                                    TreePath path = new TreePath(currentPath, node);
//                                    Element ele = workingCopy.getTrees().getElement(path);
//                                    if(ele != null) {
//                                        rewrite(node, make.QualIdent(ele));
//                                    }
//                                    ExpressionTree newIdent = make.MemberSelect(make.QualIdent(targetFqn), node.getName());
                                    org2trans.put(node, make.Identifier(node.getName()));
                                }
                                if (isTopLevelClass(el) && !isElementMoving(el) && getPackageOf(el).toString().equals(originalPackage)) {
                                    isThisFileReferencingOldPackage = true;
                                }
                            }
                            return super.visitIdentifier(node, p); //To change body of generated methods, choose Tools | Templates.
                        }
                    };
                    
                    scanner.scan(classTree, p);
                    
                    if(!org2trans.isEmpty()) {
                        classTree = (ClassTree) workingCopy.getTreeUtilities().translate(classTree, org2trans);
                    }
                    
                    ClassTree newClass = classTree;
                    ModifiersTree modifiers = newClass.getModifiers();
                    EnumSet<Modifier> flags = EnumSet.noneOf(Modifier.class);
                    if(!modifiers.getFlags().isEmpty()) {
                        flags.addAll(modifiers.getFlags());
                    }
                    flags.add(Modifier.STATIC);
                    switch(newClass.getKind()) {
                        case CLASS:
                            newClass = make.Class(
                                    make.Modifiers(flags, modifiers.getAnnotations()),
                                    newClass.getSimpleName(),
                                    newClass.getTypeParameters(),
                                    newClass.getExtendsClause(),
                                    newClass.getImplementsClause(),
                                    newClass.getPermitsClause(),
                                    newClass.getMembers());
                            break;
                        case INTERFACE:
                            newClass = make.Interface(
                                    make.Modifiers(flags, modifiers.getAnnotations()),
                                    newClass.getSimpleName(),
                                    newClass.getTypeParameters(),
                                    newClass.getImplementsClause(),
                                    newClass.getPermitsClause(),
                                    newClass.getMembers());
                            break;
                        case ENUM:
                            newClass = make.Enum(
                                    make.Modifiers(flags, modifiers.getAnnotations()),
                                    newClass.getSimpleName(),
                                    newClass.getImplementsClause(),
                                    newClass.getMembers());
                            break;
                        case ANNOTATION_TYPE:
                            newClass = make.AnnotationType(
                                    make.Modifiers(flags, modifiers.getAnnotations()),
                                    newClass.getSimpleName(),
                                    newClass.getMembers());
                            break;
                    }
                    get.copyComments(origTree, newClass, true);
                    get.copyComments(origTree, newClass, false);
                    newClass = get.importFQNs(newClass);
                    newClass = get.insertClassMember(node, newClass);
                    original2Translated.put(node, newClass);
                }
            }
        }
        return super.visitClass(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        if (!JavaPluginUtils.isSyntheticPath(workingCopy, getCurrentPath())) {
            final Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el != null) {
                if (isElementMoving(el)) {
                    String newPackageName = targetPackageName;
                    if (!"".equals(newPackageName)) {
                        Tree nju = make.QualIdent(targetFqn);
                        original2Translated.put(node, nju);
                    } else {
                        if (!moveToDefaulPackageProblem) {
                            problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_MovingClassToDefaultPackage")));
                            moveToDefaulPackageProblem = true;
                        }
                    }
                } else {
                    if (inMovingClass) {
                        if (el.getKind() != ElementKind.PACKAGE) {
                            Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);

                            EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                            Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                            if (enclosingTypeElement != null && enclosingClass != null
                                    && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                            }

                            TypeElement outermostTypeElement = workingCopy.getElementUtilities().outermostTypeElement(el);
                            if (outermostTypeElement == null && (el.getKind().isClass() || el.getKind().isInterface())) {
                                outermostTypeElement = (TypeElement) el;
                            }
                            if (!targetPackageName.equals(originalPackage)
                                    && getPackageOf(el).toString().equals(originalPackage)
                                    && (!(containsAnyOf(el, neededMods))
                                    || (enclosingTypeElement != null ? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                    && !isElementMoving(outermostTypeElement)) {
                                problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature2", workingCopy.getFileObject().getName(), el, outermostTypeElement.getSimpleName())));
                            }
                        }
                    } else {
                        if (el.getKind() != ElementKind.PACKAGE) {
                            Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);

                            EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                            Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                            if (enclosingTypeElement != null && enclosingClass != null
                                    && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                            }

                            TypeElement outermostTypeElement = workingCopy.getElementUtilities().outermostTypeElement(el);
                            if (outermostTypeElement == null && (el.getKind().isClass() || el.getKind().isInterface())) {
                                outermostTypeElement = (TypeElement) el;
                            }
                            if (!targetPackageName.equals(originalPackage)
                                    && getPackageOf(el).toString().equals(originalPackage)
                                    && (!(containsAnyOf(el, neededMods))
                                    || (enclosingTypeElement != null ? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                    && isElementMoving(outermostTypeElement)) {
                                problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature", workingCopy.getFileObject().getName(), el, outermostTypeElement.getSimpleName())));
                            }
                        }
                    }
                }
            }
        }
        return super.visitMemberSelect(node, p);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if (!inMovingClass) {
            if (!JavaPluginUtils.isSyntheticPath(workingCopy, getCurrentPath())) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                if (el != null) {
                    if (isElementMoving(el)) {
                        ExpressionTree newIdent = make.QualIdent(targetFqn);
                        original2Translated.put(node, newIdent);
                    } else if (el.getKind() != ElementKind.PACKAGE) {
                        Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);

                        EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                        Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                        if (enclosingTypeElement != null && enclosingClass != null
                                && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                            neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                        }

                        TypeElement outermostTypeElement = workingCopy.getElementUtilities().outermostTypeElement(el);
                        if (outermostTypeElement == null && (el.getKind().isClass() || el.getKind().isInterface())) {
                            outermostTypeElement = (TypeElement) el;
                        }
                        if (!targetPackageName.equals(originalPackage)
                                && getPackageOf(el).toString().equals(originalPackage)
                                && (!(containsAnyOf(el, neededMods))
                                || (enclosingTypeElement != null ? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                && isElementMoving(outermostTypeElement)) {
                            problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature", workingCopy.getFileObject().getName(), el, outermostTypeElement.getSimpleName())));
                        }
                    }
                }
            }
        }

        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitImport(ImportTree node, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            final Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), node.getQualifiedIdentifier()));
            if (el != null) {
                if (isElementMoving(el)) {
//                    if (!"".equals(targetPackageName)) {
//                        String cuPackageName = RefactoringUtils.getPackageName(workingCopy.getCompilationUnit());
//                        if (cuPackageName.equals(targetPackageName)) { //remove newly created import from same package
                            importToRemove.add(node);
                            return node;
//                        }
//                    }
                }
            }
        }
        return super.visitImport(node, p);
    }
    
    private boolean isElementMoving(Element el) {
        if (el == null) {
            return false;
        }
        ElementKind kind = el.getKind();
        if (!(kind.isClass() || kind.isInterface())) {
            return false;
        }
        TypeElement resolved = elementHandle.resolve(workingCopy);
        return el == resolved;
    }

    private boolean isParentElementMoving(Element el) {
        TypeElement outermostTypeElement = workingCopy.getElementUtilities().outermostTypeElement(el);
        return isElementMoving(outermostTypeElement);
    }

    private PackageElement getPackageOf(Element el) {
        return workingCopy.getElements().getPackageOf(el);
    }

    private boolean containsAnyOf(Element el, EnumSet<Modifier> neededMods) {
        for (Modifier mod : neededMods) {
            if (el.getModifiers().contains(mod)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTopLevelClass(Element el) {
        return el != null && (el.getKind().isClass()
                || el.getKind().isInterface())
                && el.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }

    /**
     * creates or finds FileObject according to
     *
     * @param url
     * @return FileObject
     */
    private FileObject getOrCreateFolder(URL url) throws IOException {
        try {
            FileObject result = URLMapper.findFileObject(url);
            if (result != null) {
                return result;
            }
            File f = Utilities.toFile(url.toURI());

            result = FileUtil.createFolder(f);
            return result;
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public boolean deleteFile() {
        return deleteFile;
    }

    private void createNewFileForType(CompilationUnitTree cut, Tree clazz, CompilationUnitTree node) throws MissingResourceException {
        try {
            FileObject targetRoot = RefactoringUtils.getClassPathRoot(targetURL);
            FileObject target = getOrCreateFolder(targetURL);
            String relativePath = FileUtil.getRelativePath(targetRoot, target);
            if (isThisFileReferencingOldPackage) {
                ExpressionTree newPackageName = cut.getPackageName();
                if (newPackageName != null && !moveToDefaulPackageProblem) {
                    problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_MovingClassToDefaultPackage")));
                    moveToDefaulPackageProblem = true;
                }
            }

            GeneratorUtilities.get(workingCopy).importComments(clazz, node);
            String cuPath = relativePath + "/" + ((ClassTree) clazz).getSimpleName() + ".java";
            CompilationUnitTree compilationUnit = JavaPluginUtils.createCompilationUnit(targetRoot, cuPath, clazz, workingCopy, make);
            compilationUnit = GeneratorUtilities.get(workingCopy).importFQNs(compilationUnit);
            rewrite(null, compilationUnit);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
