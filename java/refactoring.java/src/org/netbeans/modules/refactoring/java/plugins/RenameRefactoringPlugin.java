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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Jan Becicka
 * @author Martin Matula
 * @author Pavel Flaska
 * @author Daniel Prusa
 */
public class RenameRefactoringPlugin extends JavaRefactoringPlugin {
    
    private Set<ElementHandle<ExecutableElement>> allMethods = new HashSet<ElementHandle<ExecutableElement>>();
    private boolean doCheckName = true;
    private Integer overriddenByMethodsCount = null;
    private Integer overridesMethodsCount = null;
    private RenameRefactoring refactoring;
    private TreePathHandle treePathHandle = null;
    private DocTreePathHandle docTreePathHandle = null;
    
    /** Creates a new instance of RenameRefactoring */
    public RenameRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        TreePathHandle tph = rename.getRefactoringSource().lookup(TreePathHandle.class);
        DocTreePathHandle dtph = rename.getRefactoringSource().lookup(DocTreePathHandle.class);
        if (tph!=null) {
            treePathHandle = tph;
        } else if(dtph!=null) {
            docTreePathHandle = dtph;
        } else {
            JavaSource source = JavaSource.forFileObject(rename.getRefactoringSource().lookup(FileObject.class));
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                    public void cancel() {
                    }
                    
                    @Override
                    public void run(CompilationController co) throws Exception {
                        co.toPhase(JavaSource.Phase.RESOLVED);
                        CompilationUnitTree cut = co.getCompilationUnit();
                        for (Tree t: cut.getTypeDecls()) {
                            Element e = co.getTrees().getElement(TreePath.getPath(cut, t));
                            if (e!=null && e.getSimpleName().toString().equals(co.getFileObject().getName())) {
                                treePathHandle = TreePathHandle.create(TreePath.getPath(cut, t), co);
                                break;
                            }
                        }
                    }
                }, false);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
        private void addMethods(ExecutableElement e, Set set, CompilationInfo info, ClassIndex idx) {
            ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
            set.add(SourceUtils.getFile(handle, info.getClasspathInfo()));
            ElementHandle<TypeElement> encl = ElementHandle.create(info.getElementUtilities().enclosingTypeElement(e));
            set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
            allMethods.add(handle);
        }
    
        private Problem checkMethodForOverriding(ExecutableElement m, String newName, Problem problem, CompilationInfo info) {
            ElementUtilities ut = info.getElementUtilities();
            //problem = willBeOverridden(m, newName, argTypes, problem);
            fireProgressListenerStep();
            problem = willOverride(m, newName, problem, info);
            fireProgressListenerStep();
            return problem;
        }
    
    @Override
    protected Problem checkParameters(CompilationController info) throws IOException {
        Problem checkProblem = null;
        int steps = 0;
        if (overriddenByMethodsCount != null) {
            steps += overriddenByMethodsCount;
        }
        if (overridesMethodsCount != null) {
            steps += overridesMethodsCount;
        }
        
        fireProgressListenerStart(RenameRefactoring.PARAMETERS_CHECK, 8 + 3*steps);
        
        info.toPhase(JavaSource.Phase.RESOLVED);
        if(treePathHandle != null && treePathHandle.getKind() == Tree.Kind.LABELED_STATEMENT) {
            
        } else {
            Element element = docTreePathHandle != null? ((DocTrees)info.getTrees()).getElement(docTreePathHandle.resolve(info))
                                                       : treePathHandle.resolveElement(info);
            fireProgressListenerStep();
            checkProblem = isElementAvail(element, info);
            if (checkProblem != null) {
                return checkProblem;
            }
            checkProblem = JavaPluginUtils.isSourceElement(element, info);
            if (checkProblem != null) {
                return checkProblem;
            }
            fireProgressListenerStep();
            String msg;
            if (element.getKind() == ElementKind.METHOD) {
                checkProblem = checkMethodForOverriding((ExecutableElement)element, refactoring.getNewName(), checkProblem, info);
                fireProgressListenerStep();
                fireProgressListenerStep();
            } else if (element.getKind().isField()) {
                fireProgressListenerStep();
                fireProgressListenerStep();
                Element hiddenField = hides(element, refactoring.getNewName(), info);
                fireProgressListenerStep();
                fireProgressListenerStep();
                fireProgressListenerStep();
                if (hiddenField != null) {
                    if (RefactoringUtils.isWeakerAccess(element.getModifiers(), hiddenField.getModifiers())) {
                        msg = getString("ERR_WillHidePrivate", RefactoringUtils.getAccess(element.getModifiers()),
                                RefactoringUtils.getAccess(hiddenField.getModifiers()),
                                info.getElementUtilities().enclosingTypeElement(hiddenField).toString()
                        );
                    } else {
                        msg = new MessageFormat(getString("ERR_WillHide")).format(
                                new Object[]{info.getElementUtilities().enclosingTypeElement(hiddenField).toString()});
                    }
                    checkProblem = createProblem(checkProblem, false, msg);
                }
            }
        }
        fireProgressListenerStop();
        return checkProblem;
    }
    
        private String clashes(TreePath path, final String newName, CompilationInfo info) {
            TreePath parent = path.getParentPath();
            while(parent != null) {
                if(parent.getLeaf().getKind() == Tree.Kind.LABELED_STATEMENT) {
                    LabeledStatementTree parentLabel = (LabeledStatementTree) parent.getLeaf();
                    if(newName.equals(parentLabel.getLabel().toString())) {
                        return NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_LabelClash", newName);
                    }
                }
                parent = parent.getParentPath();
            }
            final String[] result = new String[1];
            new ErrorAwareTreeScanner<Void, Void>() {
                
                @Override
                public Void visitLabeledStatement(LabeledStatementTree tree, Void p) {
                    if(newName.equals(tree.getLabel().toString())) {
                        result[0] = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_LabelClash", newName);
                    }
                    return super.visitLabeledStatement(tree, p);
                }
            }.scan(path.getLeaf(), null);
            return result[0];
        }
    
    private String clashes(Element feature, String newName, CompilationInfo info) {
        ElementUtilities utils = info.getElementUtilities();
        Element dc = feature.getEnclosingElement();
        ElementKind kind = feature.getKind();
        if (kind.isClass() || kind.isInterface()) {
            for (Element current:ElementFilter.typesIn(dc.getEnclosedElements())) {
                if (current.getSimpleName().toString().equals(newName)) {
                    return new MessageFormat(getString("ERR_InnerClassClash")).format(
                            new Object[] {newName, dc.getSimpleName()}
                    );
                }
            }
        } else if (kind==ElementKind.METHOD) {
            if (utils.alreadyDefinedIn((CharSequence) newName, (ExecutableType) feature.asType(), (TypeElement) dc)) {
                return new MessageFormat(getString("ERR_MethodClash")).format(
                        new Object[] {newName, dc.getSimpleName()}
                );
            }
        } else if (kind.isField()) {
            for (Element current:ElementFilter.fieldsIn(dc.getEnclosedElements())) {
                if (current.getSimpleName().toString().equals(newName)) {
                    return new MessageFormat(getString("ERR_FieldClash")).format(
                            new Object[] {newName, dc.getSimpleName()}
                    );
                }
            }
        }
        return null;
    }
    
    @Override
    protected Problem fastCheckParameters(CompilationController info) throws IOException {
        Problem fastCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        TreePath treePath = treePathHandle != null? treePathHandle.resolve(info) : null;
        String newName = refactoring.getNewName();
        Element element;
        ElementKind kind;
        String oldName;
        if(treePath != null && treePath.getLeaf().getKind() == Tree.Kind.LABELED_STATEMENT) {
            element = null;
            kind = null;
            LabeledStatementTree lst = (LabeledStatementTree) treePath.getLeaf();
            oldName = lst.getLabel().toString();
            if (oldName.equals(newName)) {
                fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged"));
                return fastCheckProblem;
            }
        } else {
            element = docTreePathHandle != null? ((DocTrees)info.getTrees()).getElement(docTreePathHandle.resolve(info))
                                               : treePathHandle.resolveElement(info);
            fastCheckProblem = isElementAvail(element, info);
            if (fastCheckProblem != null) {
                return fastCheckProblem;
            }
            treePath = info.getTrees().getPath(element);
            kind = element.getKind();
            oldName = element.getSimpleName().toString();

            if (oldName.equals(newName)) {
                boolean nameNotChanged = true;
                if (kind.isClass()) {
                    if (!((TypeElement) element).getNestingKind().isNested()) {
                        nameNotChanged = info.getFileObject().getName().contentEquals(((TypeElement) element).getSimpleName());
                    }
                }
                if (nameNotChanged) {
                    JavaRenameProperties renameProps = refactoring.getContext().lookup(JavaRenameProperties.class);
                    if (renameProps == null || !renameProps.isNoChangeOK()) {
                        fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged"));
                        return fastCheckProblem;
                    }
                }

            }
        }
        
        if (!Utilities.isJavaIdentifier(newName)) {
            String s = kind == ElementKind.PACKAGE? getString("ERR_InvalidPackage"):getString("ERR_InvalidIdentifier"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        
        if (kind != null && (kind.isClass() || kind.isInterface()) && !((TypeElement) element).getNestingKind().isNested()) {
            TypeElement typeElement = (TypeElement) element;
            ElementHandle<TypeElement> handle = ElementHandle.create(typeElement);
            FileObject primFile = SourceUtils.getFile(handle, info.getClasspathInfo());
            FileObject folder = primFile.getParent();
            if (doCheckName) {
                String oldfqn = typeElement.getQualifiedName().toString();
                String newFqn = oldfqn.substring(0, oldfqn.lastIndexOf(oldName));
                
                String pkgname = oldfqn;
                int i = pkgname.indexOf('.');
                if (i>=0) {
                    pkgname = pkgname.substring(0,i);
                }
                else {
                    pkgname = "";
                }
                
//                String fqn = "".equals(pkgname) ? newName : pkgname + '.' + newName;
//                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                if (RefactoringUtils.typeExists(newFqn, info)) {
                    String msg = new MessageFormat(getString("ERR_ClassClash")).format(
                            new Object[] {newName, pkgname}
                    );
                    fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                    return fastCheckProblem;
                }
                Enumeration<? extends FileObject> enumeration = folder.getFolders(false);
                while (enumeration.hasMoreElements()) {
                    FileObject subfolder = enumeration.nextElement();
                    if (subfolder.getName().equals(newName)) {
                        String msg = new MessageFormat(getString("ERR_ClassPackageClash")).format(
                                new Object[] {newName, pkgname}
                        );
                        fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                        return fastCheckProblem;
                    }
                }
            }
            FileObject existing = folder.getFileObject(newName, primFile.getExt());
            if (existing != null && primFile != existing) {
                // primFile != existing is check for case insensitive filesystems; #136434
                String msg = NbBundle.getMessage(RenameRefactoringPlugin.class,
                        "ERR_ClassClash", newName, folder.getPath());
                fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            }
        } else if (kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.PARAMETER) {
            String msg = RefactoringUtils.variableClashes(newName,treePath, info);
            if (msg != null) {
                fastCheckProblem = createProblem(fastCheckProblem, true, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_LocVariableClash", msg));
                return fastCheckProblem;
            }
        } else if(element != null) {
            String msg = clashes(element, newName, info);
            if (msg != null) {
                fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                return fastCheckProblem;
            }
        } else if(treePathHandle.getKind() == Tree.Kind.LABELED_STATEMENT) {
            String msg = clashes(treePath, newName, info);
            if (msg != null) {
                fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                return fastCheckProblem;
            }
        }
        
        if (newName.contains("$")) {
            fastCheckProblem = createProblem(fastCheckProblem, false, org.openide.util.NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_DollarWarning"));
        }
        if (kind != null && (kind.isClass() || kind.isInterface()) && !Character.isUpperCase(newName.charAt(0)) ) {
            fastCheckProblem = createProblem(fastCheckProblem, false, org.openide.util.NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_UpperCaseWarning"));
        }
        return fastCheckProblem;
    }
    
    @Override
    protected JavaSource getJavaSource(Phase p) {
        if (treePathHandle == null && docTreePathHandle == null) {
            return null;
        }
        switch (p) {
            case PRECHECK:
            case FASTCHECKPARAMETERS:
                return JavaSource.forFileObject(docTreePathHandle != null? docTreePathHandle.getTreePathHandle().getFileObject()
                                                                         : treePathHandle.getFileObject());
            case PREPARE:
            case CHECKPARAMETERS:
                if(treePathHandle != null && treePathHandle.getKind() == Tree.Kind.LABELED_STATEMENT) {
                    return JavaSource.forFileObject(treePathHandle.getFileObject());
                }
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                JavaSource source = JavaSource.create(cpInfo, docTreePathHandle != null? docTreePathHandle.getTreePathHandle().getFileObject()
                                                                         : treePathHandle.getFileObject());
                return source;
                
        }
        throw new IllegalStateException();
    }

    @Override
    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
        if(treePathHandle != null) {
            return super.getClasspathInfo(refactoring);
        } else {
            ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
            if (cpInfo==null) {
                cpInfo = RefactoringUtils.getClasspathInfoFor(docTreePathHandle.getTreePathHandle().getFileObject());
                refactoring.getContext().add(cpInfo);
            }
            return cpInfo;
        }
    }
    
    private Set<FileObject> getRelevantFiles() {
            final Set<FileObject> set = new LinkedHashSet<>();
            if(treePathHandle != null && treePathHandle.getKind() == Tree.Kind.LABELED_STATEMENT) {
                set.add(treePathHandle.getFileObject());
            } else {
                JavaSource source = getJavaSource(Phase.PREPARE);
                
                try {
                    source.runUserActionTask(new CancellableTask<CompilationController>() {
                        
                        @Override
                        public void cancel() {
                            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                        }
                        
                        @Override
                        public void run(CompilationController info) throws Exception {
                            final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                            info.toPhase(JavaSource.Phase.RESOLVED);
                            Element el = docTreePathHandle != null? ((DocTrees)info.getTrees()).getElement(docTreePathHandle.resolve(info))
                                                       : treePathHandle.resolveElement(info);
                            ElementKind kind = el.getKind();
                            ElementHandle<TypeElement> enclosingType;
                            if (el instanceof TypeElement) {
                                enclosingType = ElementHandle.create((TypeElement)el);
                            } else {
                                enclosingType = ElementHandle.create(info.getElementUtilities().enclosingTypeElement(el));
                            }
                            set.add(SourceUtils.getFile(el, info.getClasspathInfo()));
                            if (el.getModifiers().contains(Modifier.PRIVATE)) {
                                if (kind == ElementKind.METHOD) {
                                    //add all references of overriding methods
                                    allMethods.add(ElementHandle.create((ExecutableElement)el));
                                }
                            } else {
                                if (kind.isField()) {
                                    set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                                } else if (el instanceof TypeElement) {
                                    set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                                } else if (kind == ElementKind.METHOD) {
                                    //add all references of overriding methods
                                    allMethods.add(ElementHandle.create((ExecutableElement)el));
                                    for (ExecutableElement e:JavaRefactoringUtils.getOverridingMethods((ExecutableElement)el, info, cancelRequested)) {
                                        addMethods(e, set, info, idx);
                                    }
                                    //add all references of overriden methods
                                    for (ExecutableElement ov: JavaRefactoringUtils.getOverriddenMethods((ExecutableElement)el, info)) {
                                        addMethods(ov, set, info, idx);
                                        for (ExecutableElement e:JavaRefactoringUtils.getOverridingMethods( ov,info, cancelRequested)) {
                                            addMethods(e, set, info, idx);
                                        }
                                    }
                                    set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
                                }
                            }
                        }
                    }, true);
                } catch (IOException ioe) {
                    throw new RuntimeException (ioe);
                }
            }
            return set;
        }

    private Element hides(Element field, String name, CompilationInfo info) {
        Elements elements = info.getElements();
        ElementUtilities utils = info.getElementUtilities();
        TypeElement jc = utils.enclosingTypeElement(field);
        for (Element el:elements.getAllMembers(jc)) {
//TODO:
//            if (utils.willHide(el, field, name)) {
//                return el;
//            }
            if (el.getKind().isField()) {
                if (el.getSimpleName().toString().equals(name)) {
                    if (!el.getEnclosingElement().equals(field.getEnclosingElement())) {
                        return el;
                    }
                }
            }
        }
        return null;
    }
    
    private Problem isElementAvail(Element el, CompilationInfo info) {
        if (el==null) {
            //element is null or is not valid.
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        } else {
            String elName = el != null ? el.getSimpleName().toString() : null;
            if (el.asType().getKind() == TypeKind.ERROR || "<error>".equals(elName)) { // NOI18N
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElementNotResolved"));
            }
            
            if ("this".equals(elName) || "super".equals(elName)) { // NOI18N
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "ERR_CannotRefactorThis", el.getSimpleName()));
            }
            
            // element is still available
            return null;
        }
    }
    
    @Override
    protected Problem preCheck(CompilationController info) throws IOException {
        JavaRenameProperties properties = refactoring.getContext().lookup(JavaRenameProperties.class);
        if (properties==null) {
            properties = new JavaRenameProperties();
            refactoring.getContext().add(properties);
        }
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 4);
        info.toPhase(JavaSource.Phase.RESOLVED);
        if(treePathHandle != null && treePathHandle.getKind() == Tree.Kind.LABELED_STATEMENT) {
            preCheckProblem = JavaPluginUtils.isSourceFile(treePathHandle.getFileObject(), info);
            if (preCheckProblem != null) {
                return preCheckProblem;
            }
            fireProgressListenerStep();
            
            
        } else {
            Element el = docTreePathHandle != null? ((DocTrees)info.getTrees()).getElement(docTreePathHandle.resolve(info))
                                                       : treePathHandle.resolveElement(info);
            preCheckProblem = isElementAvail(el, info);
            if (preCheckProblem != null) {
                return preCheckProblem;
            }
            
            preCheckProblem = JavaPluginUtils.isSourceElement(el, info);
            if (preCheckProblem != null) {
                return preCheckProblem;
            }
            
            switch(el.getKind()) {
                case METHOD:
                    fireProgressListenerStep();
                    fireProgressListenerStep();
                    Collection<ExecutableElement> overriddenByMethods; // methods that override the method to be renamed
                    overriddenByMethods = JavaRefactoringUtils.getOverridingMethods((ExecutableElement)el, info, cancelRequested);
                    overriddenByMethodsCount = overriddenByMethods.size();
                    fireProgressListenerStep();
                    if (el.getModifiers().contains(Modifier.NATIVE)) {
                        preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", el));
                    }
                    if (!overriddenByMethods.isEmpty()) {
                        String msg = new MessageFormat(getString("ERR_IsOverridden")).format(
                                new Object[] {info.getElementUtilities().enclosingTypeElement(el).getSimpleName().toString()});
                        preCheckProblem = createProblem(preCheckProblem, false, msg);
                        for (ExecutableElement method : overriddenByMethods) {
                            if(JavaRefactoringUtils.getOverriddenMethods(method, info).size() > 1) {
                                preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_IsOverriddenOverrides", method));
                                break;
                            }
                        }
                    }
                    for (ExecutableElement e : overriddenByMethods) {
                        if (e.getModifiers().contains(Modifier.NATIVE)) {
                            preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", e));
                        }
                    }
                    Collection<ExecutableElement> overridesMethods; // methods that are overridden by the method to be renamed
                    overridesMethods = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement)el, info);
                    overridesMethodsCount = overridesMethods.size();
                    fireProgressListenerStep();
                    if (!overridesMethods.isEmpty()) {
                        boolean fatal = false;
                        for (Iterator iter = overridesMethods.iterator();iter.hasNext();) {
                            ExecutableElement method = (ExecutableElement) iter.next();
                            if (method.getModifiers().contains(Modifier.NATIVE)) {
                                preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", method));
                            }
                            ElementHandle<ExecutableElement> handle = ElementHandle.create(method);
                            if (RefactoringUtils.isFromLibrary(handle, info.getClasspathInfo())) {
                                fatal = true;
                                break;
                            }
                        }
                        String msg = fatal?getString("ERR_Overrides_Fatal"):getString("ERR_Overrides");
                        preCheckProblem = createProblem(preCheckProblem, fatal, msg);
                    }
                    break;
                case FIELD:
                case ENUM_CONSTANT:
                    fireProgressListenerStep();
                    fireProgressListenerStep();
                    Element hiddenField = hides(el, el.getSimpleName().toString(), info);
                    fireProgressListenerStep();
                    fireProgressListenerStep();
                    if (hiddenField != null) {
                        String msg = new MessageFormat(getString("ERR_Hides")).format(
                                new Object[] {info.getElementUtilities().enclosingTypeElement(hiddenField)}
                        );
                        preCheckProblem = createProblem(preCheckProblem, false, msg);
                    }
                    break;
                case PACKAGE:
                    //TODO: any prechecks?
                    break;
                case LOCAL_VARIABLE:
                    //TODO: any prechecks for formal parametr or local variable?
                    break;
                case CLASS:
                case INTERFACE:
                case RECORD:
                case ANNOTATION_TYPE:
                case ENUM:
                    //TODO: any prechecks for JavaClass?
                    break;
                default:
                    //                if (!((jmiObject instanceof Resource) && ((Resource)jmiObject).getClassifiers().isEmpty()))
                    //                    result = createProblem(result, true, NbBundle.getMessage(RenameRefactoring.class, "ERR_RenameWrongType"));
            }
        }
        fireProgressListenerStop();
        return preCheckProblem;
    }
    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        if (treePathHandle == null && docTreePathHandle == null) {
            return null;
        }
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(AbstractRefactoring.PREPARE, a.size());
        TransformTask transform = new TransformTask(new RenameTransformer(treePathHandle, docTreePathHandle, refactoring, allMethods, refactoring.isSearchInComments()), treePathHandle != null && treePathHandle.getKind() == Tree.Kind.LABELED_STATEMENT ? null : treePathHandle);
        Problem problem = createAndAddElements(a, transform, elements, refactoring,getClasspathInfo(refactoring));
        fireProgressListenerStop();
        return problem;
    }
    
    private Problem willOverride(ExecutableElement method, String name, Problem problem, CompilationInfo info) {
        boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
        TypeElement jc = (TypeElement) method.getEnclosingElement();
        LinkedList supertypes = new LinkedList();
        
        ElementUtilities ut = info.getElementUtilities();
        Elements elements = info.getElements();
        ExecutableElement m = null;
        
        for (ExecutableElement ee:ElementFilter.methodsIn(elements.getAllMembers(jc))) {
            if (ee.getSimpleName().contentEquals(name) &&
                    info.getTypes().isSubsignature((ExecutableType) ee.asType(), (ExecutableType) method.asType())) {
                m = ee;
                break;
            }    
        }
        if (m!=null) {
            if (m.getModifiers().contains(Modifier.FINAL)) {
                String msg = new MessageFormat(getString("ERR_WillOverride_final")).format(
                        new Object[] {
                            method.getSimpleName(),
                            method.getEnclosingElement().getSimpleName(),
                            m.getSimpleName(),
                            m.getEnclosingElement().getSimpleName()
                        }
                );
                return createProblem(problem, true, msg);
            } else if (getAccessLevel(m) > getAccessLevel(method)) {
                String msg = new MessageFormat(getString("ERR_WillOverride_access")).format(
                        new Object[] {
                            method.getSimpleName(),
                            method.getEnclosingElement().getSimpleName(),
                            m.getSimpleName(),
                            m.getEnclosingElement().getSimpleName()
                        }
                );
                return createProblem(problem, true, msg);
            } else if (m.getModifiers().contains(Modifier.STATIC)!= method.getModifiers().contains(Modifier.STATIC)) {
                String msg = new MessageFormat(getString("ERR_WillOverride_static")).format(
                        new Object[] {
                            isStatic ? getString("LBL_static") : getString("LBL_instance"),
                            method.getSimpleName(),
                            method.getEnclosingElement().getSimpleName(),
                            m.getModifiers().contains(Modifier.STATIC) ? getString("LBL_static") : getString("LBL_instance"),
                            m.getSimpleName(),
                            m.getEnclosingElement().getSimpleName()
                        }
                );
                return createProblem(problem, true, msg);
            } else {
                String msg = new MessageFormat(getString("ERR_WillOverride")).format(
                        new Object[] {
                            method.getSimpleName(),
                            method.getEnclosingElement().getSimpleName(),
                            m.getSimpleName(),
                            m.getEnclosingElement().getSimpleName()
                        }
                );
                return createProblem(problem, false, msg);
            }
        } else {
            return problem;
        }
    }

    private static int getAccessLevel(Element e) {
        Set<Modifier> access = e.getModifiers();
        if (access.contains(Modifier.PUBLIC)) {
            return 3;
        } else if (access.contains(Modifier.PROTECTED)) {
            return 2;
        } else if (!access.contains(Modifier.PRIVATE)) {
            return 1;
        } else {
            return 0;
        }
    }
    
    
    private static String getString(String... value) {
        return NbBundle.getMessage(RenameRefactoringPlugin.class, value[0], Arrays.copyOfRange(value, 1, value.length));
    }
}
