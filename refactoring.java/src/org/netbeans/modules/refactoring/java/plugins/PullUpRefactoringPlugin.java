/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/** Plugin that implements the core functionality of Pull Up refactoring.
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public final class PullUpRefactoringPlugin extends JavaRefactoringPlugin {
    /** Reference to the parent refactoring instance */
    private final PullUpRefactoring refactoring;
    private TreePathHandle treePathHandle;

    
    /** Creates a new instance of PullUpRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    PullUpRefactoringPlugin(PullUpRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getSourceType();
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
            default:
                return JavaSource.forFileObject(treePathHandle.getFileObject());
        }
    }

    @Override
    protected Problem preCheck(CompilationController cc) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 4);
        try {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            Problem problem = isElementAvail(treePathHandle, cc);
            if (problem != null) {
                // fatal error -> don't continue with further checks
                return problem;
            }

            // increase progress (step 1)
            fireProgressListenerStep();
            final Element elm = treePathHandle.resolveElement(cc);
            problem = JavaPluginUtils.isSourceElement(elm, cc);
            if (problem != null) {
                return problem;
            }
            if (!(elm instanceof TypeElement)) {
                return new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_PushDown_InvalidSource", treePathHandle, elm)); // NOI18N
            }
            TypeElement e  = (TypeElement) elm;
            Collection<TypeElement> superTypes = JavaRefactoringUtils.getSuperTypes(e, cc, true);
            List<MemberInfo> minfo = new LinkedList<MemberInfo>();
            for (TypeElement el: superTypes) {
                MemberInfo<ElementHandle<TypeElement>> memberInfo = MemberInfo.create(el, cc);
                if(memberInfo.getElementHandle().resolve(cc) != null) { // #200200 - Error in pulling up to a interface with cyclic inheritance error
                    minfo.add(memberInfo);
                }
            }
            if (minfo.isEmpty()) {
                return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_NoSuperTypes")); // NOI18N
            }
            // increase progress (step 2)
            fireProgressListenerStep();
            // #2 - check if there are any members to pull up
            for (Element element : e.getEnclosedElements()) {
                if (element.getKind() != ElementKind.CONSTRUCTOR) {
                    return null;
                }
            }
            if (!e.getInterfaces().isEmpty()) {
                return null;
            }
            problem = new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_NoMembers")); // NOI18N
            // increase progress (step 3)
            fireProgressListenerStep();
            return problem;
        } finally {
            fireProgressListenerStop();
        }
    }

    
    @Override
    public Problem fastCheckParameters() {
        MemberInfo<ElementHandle<? extends Element>>[] info = refactoring.getMembers();
        // #1 - check whether there are any members to pull up
        if (info.length == 0) {
            return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_NoMembersSelected")); // NOI18N
        }

        if (info.length > 1) {
            for (int i=0; i<info.length - 1; i++) {
                for (int j = i + 1; j < info.length; j++) {
                    if (info[i].equals(info[j])) {
                        return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_CannotPullupDuplicateMembers"));
                    }
                }
            }
        }

        // #2 - check if the targed type is not null
        if (refactoring.getTargetType() == null) {
            return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_NoTargetType")); // NOI18N
        }

        Problem p=null;
        if (refactoring.getTargetType().getKind().isInterface()) {
            for (MemberInfo i:info) {
                if (!i.getModifiers().contains(Modifier.PUBLIC)) {
                    p = createProblem(p, false, NbBundle.getMessage(PullUpRefactoringPlugin.class,"ERR_PullupNonPublicToInterface" ,i.getName()));
                }
                
                if (i.getModifiers().contains(Modifier.STATIC)) {
                    p = createProblem(p, true, NbBundle.getMessage(PullUpRefactoringPlugin.class,"ERR_PullupStaticToInterface" ,i.getName()));
                }
            }
        }
        
        for (MemberInfo<ElementHandle<? extends Element>> i : info) {
            if(i.getElementHandle().signatureEquals(refactoring.getTargetType())) {
                p = createProblem(p, true, NbBundle.getMessage(PullUpRefactoringPlugin.class,"ERR_PullUp_MemberTargetType" ,i.getName()));
            }
        }

        return p;
    }

    @Override
    protected Problem checkParameters(CompilationController cc) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 4);
        try {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement sourceType = (TypeElement) refactoring.getSourceType().resolveElement(cc);
            Collection<TypeElement> supers = JavaRefactoringUtils.getSuperTypes(sourceType, cc, false);
            TypeElement targetType = refactoring.getTargetType().resolve(cc);
            MemberInfo<ElementHandle<? extends Element>>[] members = refactoring.getMembers();

            fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, members.length + 1);
            // #1 - check whether the target type is a legal super type
            if (!supers.contains(targetType)) {
                return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_IllegalTargetType")); // NOI18N
            }

            fireProgressListenerStep();

            // TODO: what the hell is this check
            // #2 - check whether all the members are legal members that can be pulled up
            //                    HashSet visitedSources = new HashSet();
            //                    //            HashSet allMembers = new HashSet(Arrays.asList(members));
            Problem problems = null;
            //                    visitedSources.add(refactoring.getSourceType());
            for (int i = 0; i < members.length; i++) {
                Element cls;
                Element member = members[i].getElementHandle().resolve(cc);
                if (members[i].getGroup()!=MemberInfo.Group.IMPLEMENTS) {
                    //                            // member is a feature (inner class, field or method)
                    //                            cls = member.getEnclosingElement();
                    //                        } else {
                    //                            // member is an interface from implements clause
                    //                            MultipartId ifcName = (MultipartId) member;
                    //                            // get parent of the element (should be class if this is really
                    //                            // a name from implements clause
                    //                            Object parent = ifcName.refImmediateComposite();
                    //                            // if parent is not a class, member is invalid
                    //                            if (!(parent instanceof JavaClass)) {
                    //                                cls = null;
                    //                            } else {
                    //                                // check if the parent class contains this MultipartId
                    //                                // in interfaceNames
                    //                                if (!((JavaClass) parent).getInterfaceNames().contains(ifcName)) {
                    //                                    cls = null;
                    //                                } else {
                    //                                    cls = (ClassDefinition) parent;
                    //                                }
                    //                            }
                    //                        }
                    //                        // if the declaring class has not been visited yet, perform checks on it
                    //                        if (visitedSources.add(cls)) {
                    //                            // if the declaring class of a feature is not a JavaClass,
                    //                            // or if it is not from the set of source type's supertypes
                    //                            // or if the declaring class is not a subtype of target class
                    //                            // then this member is illegal
                    //                            if (!(cls instanceof JavaClass) || !supers.contains(cls) || cls.equals(targetType) || !cls.isSubTypeOf(targetType)) {
                    //                                return createProblem(problems, true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_IllegalMember", member.getName())); // NOI18N
                    //                            }
                    //                        }
                    // #3 - check if the member already exists in the target class

                    if (RefactoringUtils.elementExistsIn(targetType, member, cc)) {
                        return createProblem(problems, true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_MemberAlreadyExists", member.getSimpleName())); // NOI18N
                    }

                    // #4 - check if the field does not use something that is not going to be pulled up
                    //                Resource sourceResource = refactoring.getSourceType().getResource();
                    //                Resource targetResource = targetType.getResource();
                    //                if (!sourceResource.equals(targetResource)) {
                    //                    problems = checkUsedByElement(member, allMembers, problems,
                    //                            !sourceResource.equals(targetResource),
                    //                            !sourceResource.getPackageName().equals(targetResource.getPackageName()));
                    //                }

                    fireProgressListenerStep();
                }

                // TODO: implement non-fatal checks
            }
        } finally {
            fireProgressListenerStop();
        }
        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);

        Set<FileObject> a = new HashSet<FileObject>();
        a.addAll(getSuperTypesFiles(refactoring.getSourceType()));
        a.add(RefactoringUtils.getFileObject(treePathHandle));
        fireProgressListenerStart(AbstractRefactoring.PREPARE, a.size());
        TransformTask task = new TransformTask(new PullUpTransformer(refactoring), treePathHandle);
        Problem problem = createAndAddElements(a, task, refactoringElements, refactoring, cpInfo);
        fireProgressListenerStop();
        return problem;
    }

    protected FileObject getFileObject() {
        return treePathHandle.getFileObject();
    }

    /**
     * @param handle
     * @return
     */
    private static Collection<FileObject> getSuperTypesFiles(TreePathHandle handle) {
        try {
            SuperTypesTask ff;
            JavaSource source = JavaSource.forFileObject(handle.getFileObject());
            source.runUserActionTask(ff = new SuperTypesTask(handle), true);
            return ff.getFileObjects();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class SuperTypesTask implements CancellableTask<CompilationController> {

        private Collection<FileObject> files;
        TreePathHandle handle;

        SuperTypesTask(TreePathHandle handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController cc) {
            try {
                cc.toPhase(JavaSource.Phase.RESOLVED);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Element el = handle.resolveElement(cc);
            files = RefactoringUtils.elementsToFile(JavaRefactoringUtils.getSuperTypes((TypeElement) el, cc, true), cc.getClasspathInfo());
        }

        public Collection<FileObject> getFileObjects() {
            return files;
        }
    }
}
