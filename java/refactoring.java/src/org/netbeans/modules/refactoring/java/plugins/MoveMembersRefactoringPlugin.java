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

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Implemented abilities: <ul> <li>Move field(s)</li> <li>Move method(s)</li>
 * </ul>
 *
 * @author Ralph Ruijs
 */
@NbBundle.Messages({"ERR_NothingSelected=Nothing selected to move",
    "ERR_MoveToLibrary=Cannot move to a library",
    "ERR_MoveFromLibrary=Cannot move from a library",
    "ERR_MoveFromClass=Can only move members of a class",
    "ERR_MoveToSameClass=Target can not be the same as the source class",
    "ERR_MoveToSuperClass=Cannot move to a superclass, maybe you need the Pull Up Refactoring?",
    "ERR_MoveToSubClass=Cannot move to a subclass, maybe you need the Push Down Refactoring?",
    "ERR_MoveGenericField=Cannot move a generic field",
    "# {0} - Method name",
    "ERR_MoveAbstractMember=Cannot move abstract method \"{0}\"",
    "# {0} - Method name",
    "ERR_MoveMethodPolymorphic=Cannot move polymorphic method \"{0}\"",
    "WRN_InitNoAccess=Field initializer uses local accessors which will not be accessible",
    "# {0} - File displayname : line number",
    "WRN_NoAccessor=No accessor found to invoke the method from: {0}",
    "TXT_DelegatingMethod=Delegating method"})
public class MoveMembersRefactoringPlugin extends JavaRefactoringPlugin {

    private final MoveRefactoring refactoring;
    private final JavaMoveMembersProperties properties;

    public MoveMembersRefactoringPlugin(MoveRefactoring moveRefactoring) {
        this.refactoring = moveRefactoring;
        this.properties = moveRefactoring.getContext().lookup(JavaMoveMembersProperties.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        TreePathHandle source;
        source = properties.getPreSelectedMembers()[0];
        if(source != null && source.getFileObject() != null) {
            switch(p) {
                case CHECKPARAMETERS:
                case FASTCHECKPARAMETERS:
                case PRECHECK:
                case PREPARE:
                    ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                    return JavaSource.create(cpInfo, source.getFileObject());
            }
        }
        return null;
    }

    @Override
    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
        List<TreePathHandle> handles = new ArrayList<TreePathHandle>(refactoring.getRefactoringSource().lookupAll(TreePathHandle.class));
        Lookup targetLookup = this.refactoring.getTarget();
        if(targetLookup != null) {
            TreePathHandle target = targetLookup.lookup(TreePathHandle.class);
            if(target != null) {
                handles.add(target);
            }
        }
        ClasspathInfo cpInfo;
        if (!handles.isEmpty()) {
            cpInfo = RefactoringUtils.getClasspathInfoFor(handles.toArray(new TreePathHandle[0]));
        } else {
            cpInfo = JavaRefactoringUtils.getClasspathInfoFor((FileObject)properties.getPreSelectedMembers()[0].getFileObject());
        }
        refactoring.getContext().add(cpInfo);
        return cpInfo;
    }

    @Override
    protected Problem preCheck(CompilationController info) throws IOException {
        info.toPhase(JavaSource.Phase.RESOLVED);
        Problem preCheckProblem = isElementAvail(properties.getPreSelectedMembers()[0], info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        Element element = properties.getPreSelectedMembers()[0].resolveElement(info);
        TreePath path = info.getTrees().getPath(element);
        if (path != null) {
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(info, path, true, true, false, true, false);
            if (enclosingClassPath != null) {
                Element typeElement = info.getTrees().getElement(enclosingClassPath);
                if (typeElement == null || !typeElement.getKind().isClass() ||
                        enclosingClassPath.getLeaf().getKind() == Tree.Kind.INTERFACE ||
                        typeElement.getKind() == ElementKind.ENUM) {
                    return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveFromClass"));
                }
            } else {
                return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveFromClass"));
            }
        } else {
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveFromClass"));
        }
        return preCheckProblem;
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        // TODO source method is using something not available at target
        // TODO source using generics not available at target
        // TODO Check if member is static but target is non static inner
        // TODO Check if target is in <default> package but source is not
        return null;
    }

    @Override
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        Collection<? extends TreePathHandle> source = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);

        if (source.isEmpty()) { // [f] nothing is selected
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_NothingSelected")); //NOI18N
        }

        Lookup targetLookup = refactoring.getTarget();
        TreePathHandle target;
        if(targetLookup == null || (target = targetLookup.lookup(TreePathHandle.class)) == null) {
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_NoTarget")); //NOI18N
        }

        if (target.getFileObject() == null || !JavaRefactoringUtils.isOnSourceClasspath(target.getFileObject())) { // [f] target is not on source classpath
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveToLibrary")); //NOI18N
        }
        TreePathHandle sourceTph = source.iterator().next();
        if (sourceTph.getFileObject() == null || !JavaRefactoringUtils.isOnSourceClasspath(sourceTph.getFileObject())) { // [f] source is not on source classpath
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveFromLibrary")); //NOI18N
        }
        
        for (TreePathHandle treePathHandle : source) {
            Element element = treePathHandle.resolveElement(javac);
            if(element.getKind() == ElementKind.FIELD) {
                VariableElement var = (VariableElement) element;
                if(var.asType().getKind() == TypeKind.TYPEVAR) {
                    return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveGenericField"));
                }
            }
            if(element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                if(method.getModifiers().contains(Modifier.ABSTRACT)) {
                    return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveAbstractMember", element.getSimpleName()));
                }
                
                // Method can not be polymorphic
                Collection<ExecutableElement> overridenMethods = JavaRefactoringUtils.getOverriddenMethods(method, javac);
                Collection<ExecutableElement> overridingMethods = JavaRefactoringUtils.getOverridingMethods(method, javac, cancelRequested);
                if (overridenMethods.size() > 0 || overridingMethods.size() > 0) {
                    return new Problem(true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_MoveMethodPolymorphic", method.getSimpleName())); //NOI18N
                }
            }
        }

        Element targetElement = target.resolveElement(javac);
        Element targetClass = targetElement;
        if(targetClass == null) {
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_TargetNotResolved"));
        }
        while (targetClass != null && !targetClass.getKind().isClass() && !targetClass.getKind().isInterface()) {
            targetClass = targetClass.getEnclosingElement();
        }
        if(targetClass == null) {
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_TargetNotResolved"));
        }
        TypeMirror targetType = targetClass.asType();
        if(targetType == null) {
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_TargetNotResolved"));
        }
        Problem p = checkProjectDeps(sourceTph.getFileObject(), target.getFileObject());
        if(p != null) {
            return p;
        }

        TreePath sourceClass = JavaRefactoringUtils.findEnclosingClass(javac, sourceTph.resolve(javac), true, true, false, false, false);
        TypeMirror sourceType = javac.getTrees().getTypeMirror(sourceClass);
        if (sourceType.equals(targetType)) { // [f] target is the same as source
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveToSameClass")); //NOI18N
        }
        if (javac.getTypes().isSubtype(sourceType, targetType)) { // [f] target is a superclass of source
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveToSuperClass")); //NOI18N
        }
        if (javac.getTypes().isSubtype(targetType, sourceType)) { // [f] target is a subclass of source
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveToSubClass")); //NOI18N
        }
        
        PackageElement targetPackage = (PackageElement) javac.getElementUtilities().outermostTypeElement(targetElement).getEnclosingElement();
        Element sourceElement = sourceTph.resolveElement(javac);
        PackageElement sourcePackage = (PackageElement) javac.getElementUtilities().outermostTypeElement(sourceElement).getEnclosingElement();
        if(targetPackage.isUnnamed() && !sourcePackage.isUnnamed()) {
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MovingMemberToDefaultPackage")); //NOI18N
        }
        for (TreePathHandle treePathHandle : source) {
            Element element = treePathHandle.resolveElement(javac);
            List<? extends Element> enclosedElements = targetElement.getEnclosedElements();
            switch(element.getKind()) {
                case FIELD:
                    enclosedElements = ElementFilter.fieldsIn(enclosedElements);
                    break;
                case METHOD:
                    enclosedElements = ElementFilter.methodsIn(enclosedElements);
                    break;
                case CONSTRUCTOR:
                    enclosedElements = ElementFilter.constructorsIn(enclosedElements);
                    break;
                default:
                    enclosedElements = ElementFilter.typesIn(enclosedElements);
                    break;
            }
            for (Element member : enclosedElements) {
                if(element.getSimpleName().contentEquals(member.getSimpleName())) {
                    Problem p2;
                    if(member.getKind() != ElementKind.METHOD) {
                        p = JavaPluginUtils.chainProblems(p, new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_PullUp_MemberAlreadyExists", element.getSimpleName())));
                    } else if((p2 = compareMethodSignatures((ExecutableElement)element, (ExecutableElement)member, targetElement, javac)) != null) {
                        p = JavaPluginUtils.chainProblems(p, p2);
                    }
                }
            }
        }
        return p;
    }

    private Set<FileObject> getRelevantFiles() {
        final Set<FileObject> set = new LinkedHashSet<FileObject>();

        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final ClassIndex idx = cpInfo.getClassIndex();
        final Collection<? extends TreePathHandle> tphs = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        TreePathHandle target = refactoring.getTarget().lookup(TreePathHandle.class);
        FileObject file = target.getFileObject();
        JavaSource source = JavaPluginUtils.createSource(file, cpInfo, target);
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

            public void cancel() {
            }

            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                Set<ClassIndex.SearchScopeType> searchScopeType = new HashSet<ClassIndex.SearchScopeType>(1);
                searchScopeType.add(ClassIndex.SearchScope.SOURCE);

                for (TreePathHandle tph : tphs) {
                    set.add(tph.getFileObject());
                    final Element el = tph.resolveElement(info);
                    if (el == null) {
                        // Similar to #145291 from JavaWhereUsedQueryPlugin
                        throw new NullPointerException(String.format("#222979: Cannot resolve handle: %s\n%s", tph, info.getClasspathInfo())); // NOI18N
                    }
                    if (el.getKind() == ElementKind.METHOD) {
                        // get method references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), searchScopeType)); //?????
                    }
                    if (el.getKind().isField()) {
                        // get field references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), searchScopeType));
                    }
                }
            }
        };
        try {
            source.runUserActionTask(task, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        // Make sure the target is added last. Needed for escalating visibility.
        set.remove(file);
        set.add(file);
        return set;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        fireProgressListenerStart(AbstractRefactoring.PREPARE, -1);

        Set<FileObject> relevantFiles = getRelevantFiles();
        Problem p = null;
        TreePathHandle targetHandle = refactoring.getTarget().lookup(TreePathHandle.class);
        fireProgressListenerStep(relevantFiles.size());
        MoveMembersTransformer transformer = new MoveMembersTransformer(refactoring);
        TransformTask task = new TransformTask(transformer, targetHandle);
        Problem prob = createAndAddElements(relevantFiles, task, refactoringElements, refactoring, getClasspathInfo(refactoring));
        prob = JavaPluginUtils.chainProblems(prob, transformer.getProblem());
        fireProgressListenerStop();
        return prob != null ? prob : JavaPluginUtils.chainProblems(transformer.getProblem(), p);
    }

    @SuppressWarnings("CollectionContainsUrl")
    private Problem checkProjectDeps(FileObject sourceFile, FileObject targetFile) {
        Set<FileObject> sourceRoots = new HashSet<FileObject>();
        ClassPath cp = ClassPath.getClassPath(sourceFile, ClassPath.SOURCE);
        if (cp != null) {
            FileObject root = cp.findOwnerRoot(sourceFile);
            sourceRoots.add(root);
        }

        FileObject targetRoot = null;
        ClassPath targetCp = ClassPath.getClassPath(targetFile, ClassPath.SOURCE);
        if(targetCp != null) {
            targetRoot = targetCp.findOwnerRoot(targetFile);
        }
        
        if(!sourceRoots.isEmpty() && targetRoot != null) {
            URL targetUrl = URLMapper.findURL(targetRoot, URLMapper.EXTERNAL);
            Project targetProject = FileOwnerQuery.getOwner(targetRoot);
            Set<URL> deps = SourceUtils.getDependentRoots(targetUrl);

            for (FileObject sourceRoot : sourceRoots) {
                URL sourceUrl = URLMapper.findURL(sourceRoot, URLMapper.INTERNAL);
                if (!deps.contains(sourceUrl)) {
                    Project sourceProject = FileOwnerQuery.getOwner(sourceRoot);
                    for (FileObject affected : getRelevantFiles()) {
                        if (FileOwnerQuery.getOwner(affected).equals(sourceProject) && !sourceProject.equals(targetProject)) {
                            assert sourceProject != null;
                            assert targetProject != null;
                            String sourceName = ProjectUtils.getInformation(sourceProject).getDisplayName();
                            String targetName = ProjectUtils.getInformation(targetProject).getDisplayName();
                            return new Problem(false, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MemberMissingProjectDeps", sourceName, targetName));
                        }
                    }
                }
            }
        }
        return null;
    }

    private Problem compareMethodSignatures(ExecutableElement method, ExecutableElement exMethod, Element targetElement, CompilationInfo javac) {
        Problem p = null;
        if (!exMethod.equals(method)) {
            if (exMethod.getSimpleName().equals(method.getSimpleName())
                    && exMethod.getParameters().size() == method.getParameters().size()) {
                boolean sameParameters = true;
                boolean wideningConversion = true;
                for (int j = 0; j < exMethod.getParameters().size(); j++) {
                    TypeMirror exType = ((VariableElement) exMethod.getParameters().get(j)).asType();
                    TypeMirror paramType = method.getParameters().get(j).asType();
                    if (!javac.getTypes().isSameType(exType, paramType)) {
                        sameParameters = false;
                        if (exType.getKind().isPrimitive() && paramType.getKind().isPrimitive()) {
                            /*
                             * byte to short, int, long, float, or double
                             * short to int, long, float, or double
                             * char to int, long, float, or double
                             * int to long, float, or double
                             * long to float or double
                             * float to double
                             */
                            switch (exType.getKind()) {
                                case DOUBLE:
                                    if (paramType.getKind() ==TypeKind.FLOAT) {
                                        break;
                                    }
                                case FLOAT:
                                    if (paramType.getKind() == TypeKind.LONG) {
                                        break;
                                    }
                                case LONG:
                                    if (paramType.getKind() == TypeKind.INT) {
                                        break;
                                    }
                                case INT:
                                    if (paramType.getKind() == TypeKind.SHORT) {
                                        break;
                                    }
                                case SHORT:
                                    if (paramType.getKind() == TypeKind.BYTE) {
                                        break;
                                    }
                                case BYTE:
                                    wideningConversion = false;
                                    break;
                            }
                        } else {
                            wideningConversion = false;
                        }
                    }
                }
                if (sameParameters) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_existingMethod", exMethod.toString(), ((TypeElement)targetElement).getQualifiedName())); // NOI18N
                } else if (wideningConversion) {
                    p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_wideningConversion", exMethod.toString(), ((TypeElement)targetElement).getQualifiedName())); // NOI18N
                }
            }
        }
        
        return p;
    }
}
