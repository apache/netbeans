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
package org.netbeans.modules.form.refactoring;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.nbform.FormEditorSupport;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.RenameSupport;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class used to hook form component/variables renaming into refactoring.
 * @author Wade Chandler
 * @version 1.0
 */
@ServiceProvider(service=RenameSupport.Refactoring.class)
public class RADComponentRenameRefactoringSupport implements RenameSupport.Refactoring {

    @Override
    public void renameComponent(FormModel formModel, String currentName, String newName) {
        FormDataObject formDO = FormEditor.getFormDataObject(formModel);
        JavaSource js = JavaSource.forFileObject(formDO.getPrimaryFile());
        MemberVisitor scanner = new MemberVisitor(currentName, true); //privateField);
        try {
            js.runUserActionTask(scanner, true);
            doRenameRefactoring(formDO, newName, scanner.getHandle());
        } catch (IOException e) {
            Logger.getLogger(RADComponentRenameRefactoringSupport.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static void doRenameRefactoring(FormDataObject dao, String newName, TreePathHandle handle) throws IOException {
        if(handle==null){
            //this would only happen if setName were called without the correct component being
            //selected some how...
            return;
        }
        FormEditorSupport fes = (FormEditorSupport)dao.getFormEditorSupport();
        if (fes.isModified()) {
            fes.saveDocument();
        }
        //ok, so we are now ready to actually setup our RenameRefactoring...we need the element TreePathHandle
        Lookup rnl = Lookups.singleton(handle);
        RefactoringSession renameSession = RefactoringSession.create("Change variable name");//NOI18N
        RenameRefactoring refactoring = new RenameRefactoring(rnl);
        Problem pre = refactoring.preCheck();
        if(pre!=null&&pre.isFatal()){
            Logger.getLogger("global").log(Level.WARNING, "There were problems trying to perform the refactoring.");
        }

        Problem p = null;

        if( (!(pre!=null&&pre.isFatal())) && !emptyOrWhite(newName) ){
            refactoring.setNewName(newName);
            p = refactoring.prepare(renameSession);
        }

        if( (!(p!=null&&p.isFatal())) && !emptyOrWhite(newName) ){
            renameSession.doRefactoring(true);
        }
    }

    private static boolean emptyOrWhite(String s){
        return s == null || s.trim().length() == 0;
    }

    private static class MemberVisitor
            extends ErrorAwareTreePathScanner<Void, Void>
            implements CancellableTask<CompilationController>{
        
        private CompilationInfo info;
        private String member = null;
        private TreePathHandle handle = null;

        boolean findUsages;
        private Element variableElement;
        private List<Integer> usagesPositions;

        public TreePathHandle getHandle() {
            return handle;
        }
        
        public void setHandle(TreePathHandle handle) {
            this.handle = handle;
        }
        
        public MemberVisitor(String member, boolean findUsages) {
            this.member = member;
            this.findUsages = findUsages;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            if (variableElement == null) {
                // try to find the component's field variable in the class
                List<? extends Tree> members = (List<? extends Tree>) t.getMembers();
                Iterator<? extends Tree> it = members.iterator();
                while(it.hasNext()){
                    Tree tr = it.next();
                    if (tr.getKind() == Tree.Kind.VARIABLE) {
                        Trees trees = info.getTrees();
                        TreePath path = new TreePath(getCurrentPath(), tr);
                        Element el = trees.getElement(path);
                        if (el != null) { // Issue 185420
                            String sname = el.getSimpleName().toString();
                            if(sname.equals(this.member)){
                                this.handle = TreePathHandle.create(path, info);
                                variableElement = el;
                                if (findUsages) {
                                    usagesPositions = new ArrayList<Integer>();
                                }
                            }
                        }
                    }
                }
            }
            if (findUsages) {
                super.visitClass(t, v);
            }
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Void v) {
            if (findUsages) {
                Element el = info.getTrees().getElement(getCurrentPath());
                if (variableElement != null && variableElement.equals(el)) {
                    int pos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
                    usagesPositions.add(pos);
                }
            }
            return super.visitIdentifier(tree, v);
        }

        @Override
        public void cancel() {
        }
        
        @Override
        public void run(CompilationController parameter) throws IOException {
            this.info = parameter;
            parameter.toPhase(Phase.RESOLVED);
            this.scan(parameter.getCompilationUnit(), null);
        }
    }
}
