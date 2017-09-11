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


package org.netbeans.modules.j2ee.jpa.refactoring.rename;


import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Position;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.cookies.EditorCookie;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Handles usage search of attribute used in mappedBy
 * @author Sergey Petrov
 */
public final class RelationshipMappingRename extends JavaRefactoringPlugin {
    
    private final RenameRefactoring rename;
    private final static Logger LOG = Logger.getLogger(RelationshipMappingRename.class.getName());
    
    public RelationshipMappingRename(RenameRefactoring rename) {
        this.rename = rename;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElementsBag) {
        final TreePathHandle handle = rename.getRefactoringSource().lookup(TreePathHandle.class);
        if (handle == null) {
            return null;
        }
        if (handle.getKind() == Kind.VARIABLE) {
            final ClasspathInfo cpInfo = getClasspathInfo(rename);
            final Set<ElementHandle<TypeElement>> set = new HashSet<ElementHandle<TypeElement>>();
            JavaSource source = JavaSource.create(cpInfo, new FileObject[]{handle.getFileObject()});
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                    public void cancel() {
                    }
                    @Override
                    public void run(CompilationController ci) throws Exception {
                        ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Element resElement = handle.resolveElement(ci);
                        VariableElement var = (VariableElement) resElement;
                        Element mainEntTmp = var.getEnclosingElement();
                        TypeElement mainEnt = mainEntTmp instanceof TypeElement ? (TypeElement) mainEntTmp : null;
                        if (mainEnt == null) {
                            return;
                        }
                        List<? extends AnnotationMirror> ans = var.getAnnotationMirrors();
                        boolean checkFoMappedBy = false;
                        for (AnnotationMirror an : ans) {
                            String tp = an.getAnnotationType().toString();
                            if ("javax.persistence.ManyToOne".equals(tp) || "javax.persistence.ManyToMany".equals(tp)) {//NOI18N
                                checkFoMappedBy = true;
                                break;
                            }
                        }
                        if (checkFoMappedBy) {
                            TypeMirror tm = var.asType();
                            TypeElement oppEntEl = ci.getElements().getTypeElement(tm.toString());
                            if (oppEntEl != null) {

                            } else {
                                LOG.log(Level.INFO, "Can't resolve {0}", tm.toString());//NOI18N
                            }
                        }
                    }
                }, false);
                for (final ElementHandle<TypeElement> elHandle : set) {
                    FileObject fo = SourceUtils.getFile(elHandle, cpInfo);
                    JavaSource usage = JavaSource.create(cpInfo, fo);
                    usage.runUserActionTask(new CancellableTask<CompilationController>() {
                        @Override
                        public void cancel() {
                        }

                        @Override
                        public void run(CompilationController ci) throws Exception {
                            ci.toPhase(JavaSource.Phase.RESOLVED);
                            Element resElement = handle.resolveElement(ci);
                            VariableElement var = (VariableElement) resElement;
                            Element mainEntTmp = var != null ? var.getEnclosingElement() : null;
                            TypeElement mainEnt = mainEntTmp instanceof TypeElement ? (TypeElement) mainEntTmp : null;
                            if (mainEnt == null) {
                                return;
                            }
                            TypeElement oppEntEl = elHandle.resolve(ci);
                            for (VariableElement field : ElementFilter.fieldsIn(oppEntEl.getEnclosedElements())) {
                                String fqn = field.asType().toString();
                                if (fqn.endsWith("<" + mainEnt + ">") && fqn.startsWith("java.util.")) { //it's 99% some generic collection
                                    List<? extends AnnotationMirror> ans = field.getAnnotationMirrors();
                                    for (AnnotationMirror an : ans) {
                                        String tp = an.getAnnotationType().toString();
                                        if ("javax.persistence.OneToMany".equals(tp) || "javax.persistence.ManyToMany".equals(tp)) {//NOI18N
                                            for (ExecutableElement el : an.getElementValues().keySet()) {
                                                if (el.getSimpleName().toString().equals("mappedBy")) {
                                                    if (an.getElementValues().get(el).getValue().toString().equals(var.getSimpleName().toString())) {
                                                        FileObject fo;
                                                        ElementHandle<VariableElement> elHandle = ElementHandle.create(field);
                                                        fo = SourceUtils.getFile(elHandle, cpInfo);
                                                        //it's usage
                                                        TreePath path = ci.getTrees().getPath(field);
                                                        if (path == null || fo == null) {
                                                            LOG.log(Level.INFO, "Can''t get fileobject.{0} : {1}", new Object[]{path, fo});//NOI18N
                                                            continue;
                                                        }
                                                        CompilationUnitTree unit = path.getCompilationUnit();
                                                        Tree t = path.getLeaf();
                                                        List<? extends AnnotationTree> aList = ((VariableTree) t).getModifiers().getAnnotations();
                                                        AnnotationTree at0 = null;
                                                        for (AnnotationTree at : aList) {
                                                            for (ExpressionTree et : at.getArguments()) {
                                                                if (et instanceof AssignmentTree) {
                                                                    AssignmentTree ast = ((AssignmentTree) et);
                                                                    if (ast.toString().startsWith("mappedBy")) {//NOI18N
                                                                        t = ast.getExpression();
                                                                        at0 = at;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        SourcePositions sp = ci.getTrees().getSourcePositions();
                                                        sp.getStartPosition(unit, t);
                                                        refactoringElementsBag.add(rename, new RelationshipAnnotationRenameRefactoringElement(fo, field, at0, an, var.getSimpleName().toString(), (int) sp.getStartPosition(unit, t), (int) sp.getEndPosition(unit, t)));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }, false);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
        
    private class RelationshipAnnotationRenameRefactoringElement extends SimpleRefactoringElementImplementation{
        private final AnnotationMirror annotation;
        private final String attrValue;
        private final FileObject fo;
        private final VariableElement var;
        private final AnnotationTree at;
        
        RelationshipAnnotationRenameRefactoringElement(FileObject fo, VariableElement var, AnnotationTree at, AnnotationMirror annotation, String attrValue, int start, int end) {
            this.fo = fo;
            this.annotation = annotation;
            this.attrValue = attrValue;
            if((end-start)>attrValue.length()){//handle quotes
                start++;
                end--;
            }
            loc = new int[]{start, end};
            this.at = at;
            this.var = var;
        }
        
        @Override
        public String getText() {
            return getDisplayText();
        }

        @Override
        public String getDisplayText() {
            return annotation.toString().replace("\""+attrValue+"\"", "\"<b>"+attrValue+"</b>\"");
        }

        @Override
        public void performChange() {
            try {
                JavaSource source = JavaSource.forFileObject(fo);
                source.runModificationTask(new CancellableTask<WorkingCopy>() {

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void run(WorkingCopy workingCopy) throws Exception {

                        workingCopy.toPhase(JavaSource.Phase.RESOLVED);

                        for (AnnotationMirror annotation : var.getAnnotationMirrors()) {

  
                            TreeMaker make = workingCopy.getTreeMaker();
                            AnnotationTree oldAt = at;
                            List<? extends ExpressionTree> arguments = oldAt.getArguments();
                            ArrayList<ExpressionTree> newArguments = new ArrayList<ExpressionTree>();
                            for (ExpressionTree argument : arguments) {
                                ExpressionTree tmp = argument;
                                if (argument instanceof AssignmentTree) {
                                    AssignmentTree assignment = (AssignmentTree) argument;
                                    ExpressionTree expression = assignment.getExpression();
                                    ExpressionTree variable = assignment.getVariable();
                                    if (variable.toString().equals("mappedBy") && expression instanceof LiteralTree) {//NOI18N
                                        LiteralTree literal = (LiteralTree) expression;
                                        String value = literal.getValue().toString();
                                        if (attrValue.equals(value)) {
                                            tmp = make.Assignment(variable, make.Literal(rename.getNewName()));
                                        }
                                    }
                                }
                                newArguments.add(tmp);
                            }
                            TypeElement typeElement = workingCopy.getElements().getTypeElement(annotation.getAnnotationType().toString());
                            AnnotationTree newAt = make.Annotation(make.QualIdent(typeElement), newArguments);

                            workingCopy.rewrite(oldAt, newAt);
                        }

                    }
                }).commit();

            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookups.singleton((fo));
        }

        @Override
        public FileObject getParentFile() {
            return fo;
        }
        
        private int[] loc; // cached
        @Override
         public PositionBounds getPosition() {
            try {
                DataObject dobj = DataObject.find(getParentFile());
                if (dobj != null) {
                    EditorCookie.Observable obs = (EditorCookie.Observable)dobj.getLookup().lookup(EditorCookie.Observable.class);
                    if (obs != null && obs instanceof CloneableEditorSupport) {
                        CloneableEditorSupport supp = (CloneableEditorSupport)obs;

                    PositionBounds bounds = new PositionBounds(
                            supp.createPositionRef(loc[0], Position.Bias.Forward),
                            supp.createPositionRef(Math.max(loc[0], loc[1]), Position.Bias.Forward)
                            );

                    return bounds;
                }
                }
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.INFO, "Can't resolve", ex);//NOI18N
            }
            return null;
        }

    
    }
}
