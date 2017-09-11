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

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/*
 * UseSuperTypeRefactoringPlugin.java
 *
 * Created on June 22, 2005
 *
 * @author Bharath Ravi Kumar
 */
/**
 * The plugin that performs the actual work on
 * behalf of the use super type refactoring
 */
public class UseSuperTypeRefactoringPlugin extends JavaRefactoringPlugin {

    private final UseSuperTypeRefactoring refactoring;

    /**
     * Creates a new instance of UseSuperTypeRefactoringPlugin
     * @param refactoring The refactoring to be used by this plugin
     */
    public UseSuperTypeRefactoringPlugin(UseSuperTypeRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    /**
     * Prepares the underlying where used query & checks
     * for the visibility of the target type.
     */
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        TreePathHandle subClassHandle = refactoring.getTypeElement();
        replaceSubtypeUsages(subClassHandle, refactoringElements);
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
            default:
                return JavaSource.forFileObject(refactoring.getTypeElement().getFileObject());
        }
    }

    /**
     *Checks whether the candidate element is a valid Type.
     *@return Problem The problem instance indicating that an invalid element was selected.
     */
    @Override
    public Problem preCheck() {
        cancelRequest = false;
        cancelRequested.set(false);
        //        Element subType = refactoring.getTypeElement();
        //        if(!(subType instanceof JavaClass)){
        //            String errMsg = NbBundle.getMessage(UseSuperTypeRefactoringPlugin.class,
        //                    "ERR_UseSuperType_InvalidElement"); // NOI18N
        //            return new Problem(true, errMsg);
        //        }
        return null;
    }

    /**
     * @return A problem indicating that no super type was selected.
     */
    @Override
    public Problem fastCheckParameters() {
        if (refactoring.getTargetSuperType() == null) {
            return new Problem(true, NbBundle.getMessage(UseSuperTypeRefactoringPlugin.class, "ERR_UseSuperTypeNoSuperType"));
        }
        return null;
    }

    /**
     * A no op. Returns null
     */
    @Override
    public Problem checkParameters() {
        return null;
    }

    //---------private  methods follow--------

    private void replaceSubtypeUsages(final TreePathHandle subClassHandle, final RefactoringElementsBag elemsBag) {
        JavaSource javaSrc = JavaSource.forFileObject(subClassHandle.getFileObject());
        try {
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>() {
                @Override
                public void cancel() { }
                @Override
                public void run(CompilationController complController) throws IOException {
                    complController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                    FileObject fo = subClassHandle.getFileObject();
                    ClasspathInfo classpathInfo = RefactoringUtils.getClasspathInfoFor(true, true, fo) ;
                    
                    ClassIndex clsIndx = classpathInfo.getClassIndex();
                    TypeElement javaClassElement = (TypeElement) subClassHandle.
                            resolveElement(complController);
                    EnumSet<ClassIndex.SearchKind> typeRefSearch = EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES);
                    Set<FileObject> refFileObjSet = clsIndx.getResources(ElementHandle.create(javaClassElement), typeRefSearch, EnumSet.of(ClassIndex.SearchScope.SOURCE));

                    if (!refFileObjSet.isEmpty()) {
                        fireProgressListenerStart(AbstractRefactoring.PREPARE, refFileObjSet.size());
                        try{
                            Collection<ModificationResult> results = processFiles(refFileObjSet, new FindRefTask(subClassHandle, refactoring.getTargetSuperType()));
                            elemsBag.registerTransaction(createTransaction(results));
                            for (ModificationResult result : results) {
                                for (FileObject fileObj : result.getModifiedFileObjects()) {
                                    for (Difference diff : result.getDifferences(fileObj)) {
                                        String old = diff.getOldText();
                                        if (old != null) {
                                            elemsBag.add(refactoring, DiffElement.create(diff, fileObj, result));
                                        }
                                    }
                                }
                            }
                        }finally{
                            fireProgressListenerStop();
                        }
                    }
                }
            }, false);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    private final class FindRefTask implements CancellableTask<WorkingCopy> {

        private final TreePathHandle subClassHandle;
        private final ElementHandle superClassHandle;

        private FindRefTask(TreePathHandle subClassHandle, ElementHandle superClassHandle) {
            this.subClassHandle = subClassHandle;
            this.superClassHandle = superClassHandle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(WorkingCopy compiler) throws Exception {
            try {
                if (compiler.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                    return;
                }
                CompilationUnitTree cu = compiler.getCompilationUnit();
                if (cu == null) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler); // NOI18N
                    return;
                }
                Element subClassElement = subClassHandle.resolveElement(compiler);
                Element superClassElement = superClassHandle.resolve(compiler);
                if (superClassElement == null) {
                    //superClassElement is not resolvable in this project
                    //do not replace
                    //#202030
                    return;
                }
                assert subClassElement != null;
                ReferencesVisitor findRefVisitor = new ReferencesVisitor(compiler, subClassElement, superClassElement);
                findRefVisitor.scan(compiler.getCompilationUnit(), subClassElement);
            } finally {
                fireProgressListenerStep();
            }
        }

    }

    private static class ReferencesVisitor extends RefactoringVisitor {

        private final TypeElement superTypeElement;
        private final TypeElement subTypeElement;

        private ReferencesVisitor(WorkingCopy workingCopy, Element subClassElement, Element superClassElement) {
            try {
                setWorkingCopy(workingCopy);
            } catch (ToPhaseException phase) {
                //should never be thrown;
                Exceptions.printStackTrace(phase);
            }
            this.superTypeElement = (TypeElement) superClassElement;
            this.subTypeElement = (TypeElement) subClassElement;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree memSelTree, Element elemToFind) {
            Element elem = asElement(memSelTree);
            
            if ((elem != null) && isStatic(elem)) {
                Element expreElem = asElement(memSelTree.getExpression());
                //If a static member was referenced using the object instead 
                //of the class, don't handle it here.
                if(expreElem == null || ! (ElementKind.CLASS.equals(expreElem.getKind()) || 
                        ElementKind.INTERFACE.equals(expreElem.getKind()))){
                    return super.visitMemberSelect(memSelTree, elemToFind);
                }
                TypeElement type = (TypeElement) expreElem;
                if (!subTypeElement.equals(type)) {
                    return super.visitMemberSelect(memSelTree, elemToFind);
                }
                if (hidesSupTypeMember(elem, superTypeElement)) {
                    replaceType(memSelTree, superTypeElement);
                }
            }
            return super.visitMemberSelect(memSelTree, elemToFind);
        }

        
        @Override
        public Tree visitVariable(VariableTree varTree, Element elementToMatch) {
            TreePath treePath = getCurrentPath();
            VariableElement varElement = (VariableElement) workingCopy.
                    getTrees().getElement(treePath);


            //This check shouldn't be needed (ideally).
            if (varElement == null) {
                return super.visitVariable(varTree, elementToMatch);
            }
            TreePath parentPath = treePath.getParentPath();
            if(parentPath != null && parentPath.getLeaf().getKind() == Tree.Kind.CATCH) {
                // Do not change in catch statement
                return super.visitVariable(varTree, elementToMatch);
            }

            Types types = workingCopy.getTypes();
            TypeMirror varTypeErasure = types.erasure(varElement.asType());
            TypeMirror elToMatchErasure = types.erasure(subTypeElement.asType());
        
            if (types.isSameType(varTypeErasure, elToMatchErasure)) {
                
                //Check for overloaded methods
                boolean clashWithOverload = false;
                if(parentPath != null && parentPath.getLeaf().getKind() == Tree.Kind.METHOD) {
                    Trees trees = workingCopy.getTrees();
                    ExecutableElement parent = (ExecutableElement) trees.getElement(parentPath);
                    TreePath enclosing = JavaRefactoringUtils.findEnclosingClass(workingCopy, parentPath, true, true, true, true, true);
                    TypeElement typeEl = (TypeElement) (enclosing == null? null : trees.getElement(enclosing));
                    if(parent != null && typeEl != null) {
                        Name simpleName = parent.getSimpleName();
                        int size = parent.getParameters().size();
                        OUTER: for (ExecutableElement method : ElementFilter.methodsIn(workingCopy.getElements().getAllMembers(typeEl))) {
                            if (method != parent &&
                                method.getKind() == parent.getKind() &&
                                size == method.getParameters().size() &&
                                simpleName.contentEquals(method.getSimpleName())) {
                                for (int i = 0; i < parent.getParameters().size(); i++) {
                                    VariableElement par = parent.getParameters().get(i);
                                    TypeMirror parErasure = types.erasure(par.asType());
                                    TypeMirror par2Erasure = types.erasure(method.getParameters().get(i).asType());
                                    if(!types.isSameType(parErasure, par2Erasure)) {
                                        if(types.isAssignable(types.erasure(superTypeElement.asType()), par2Erasure)) {
                                            clashWithOverload = true;
                                            break OUTER;
                                        }
                                        if(types.isSubtype(parErasure, par2Erasure)) {
                                            clashWithOverload = true;
                                            break OUTER;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!clashWithOverload && isReplaceCandidate(varElement)) {
                    replaceWithSuperType(treePath, varTree, varElement, superTypeElement);
                }
            }
            return super.visitVariable(varTree, elementToMatch);
        }

        // handles only simple situations like
        // ArrayList list = (ArrayList) getList();
        // where ArrayList is being substituted by a supertype
        @Override
        public Tree visitTypeCast(TypeCastTree castTree, Element elementToMatch) {
            TreePath path = getCurrentPath();
            Types types = workingCopy.getTypes();
            TypeMirror castTypeErasure = types.erasure(workingCopy.getTrees().getTypeMirror(path));
            TypeMirror elToMatchErasure = types.erasure(subTypeElement.asType());
            path = path.getParentPath();
            Element element = workingCopy.getTrees().getElement(path);
            if (element instanceof VariableElement && types.isSameType(castTypeErasure, elToMatchErasure)) {
                VariableElement varElement = (VariableElement)element;
                TypeMirror varTypeErasure = types.erasure(varElement.asType());
                if (types.isSameType(varTypeErasure, elToMatchErasure) && isReplaceCandidate(varElement)) {
                    TypeCastTree newTree = make.TypeCast(
                        make.Identifier(superTypeElement), castTree.getExpression());
                    rewrite(castTree, newTree);
                }
            }
            return super.visitTypeCast(castTree, elementToMatch);
        }
        
        private boolean hidesSupTypeMember(Element methElement, TypeElement superTypeElement) {
            Elements elements = workingCopy.getElements();
            List<? extends Element> containedElements = elements.getAllMembers(superTypeElement);
            for (Element elem : containedElements) {
                boolean isPresentInSuperType = methElement.equals(elem) || 
                        elements.hides(methElement, elem);
                if ((elem != null) && isStatic(elem) && isPresentInSuperType) {
                    return true;
                }
            }
            return false;
        }

        private boolean isReplaceCandidate(VariableElement varElement) {
            VarUsageVisitor varUsagesVisitor = new VarUsageVisitor(subTypeElement,
                    workingCopy, superTypeElement);
            varUsagesVisitor.scan(workingCopy.getCompilationUnit(), varElement);
            return varUsagesVisitor.isReplaceCandidate();
        }

        private boolean isStatic(Element element) {
            Set<Modifier> modifiers = element.getModifiers();
            return modifiers.contains(Modifier.STATIC);
        }

        private void replaceType(MemberSelectTree memSelTree, Element superTypeElement) {
            MemberSelectTree newTree = make.MemberSelect(
                    make.Identifier(superTypeElement), memSelTree.getIdentifier());
            rewrite(memSelTree, newTree);
        }

        private void replaceWithSuperType(TreePath path, VariableTree oldVarTree, VariableElement varElement, Element superTypeElement) {
            Types types = workingCopy.getTypes();
            TypeMirror supTypeErasure = types.erasure(superTypeElement.asType());
            DeclaredType varType = (DeclaredType) varElement.asType();
            TypeMirror theType = null;
            List<TypeMirror> supertypes = new LinkedList(types.directSupertypes(varType));
            while(!supertypes.isEmpty()) {
                TypeMirror supertype = supertypes.remove(0);
                if(types.isSameType(types.erasure(supertype), supTypeErasure)) {
                    theType = supertype;
                    break;
                }
                supertypes.addAll(types.directSupertypes(supertype));
            }
            
            if(theType == null) {
                theType = supTypeErasure;
            }
            Tree superTypeTree = make.Type(theType);
  
            ExpressionTree oldInitTree = oldVarTree.getInitializer();
            ModifiersTree oldModifiers = oldVarTree.getModifiers();
            Tree newTree = make.Variable(oldModifiers, oldVarTree.getName(), 
                    superTypeTree, oldInitTree);
            rewrite(oldVarTree, newTree);
        }

        private Element asElement(Tree tree) {
            Trees treeUtil = workingCopy.getTrees();
            TreePath treePath = treeUtil.getPath(workingCopy.getCompilationUnit(), tree);
            Element element = treeUtil.getElement(treePath);
            return element;
        }

    }
}
