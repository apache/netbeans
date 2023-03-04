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
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.io.IOException;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk, jlahoda
 */
@Hint(displayName = "#DN_AssignmentToItself", description = "#DESC_AssignmentToItself", category="general", id="AssignmentToItself", suppressWarnings="SillyAssignment", options=Options.QUERY)
public class AssignmentToItself {

    @TriggerPattern(value="$var = $var")
    public static ErrorDescription hint(HintContext ctx) {
        return ErrorDescriptionFactory.forTree(
                    ctx,
                    ctx.getPath(),
                    NbBundle.getMessage(AssignmentToItself.class, "ERR_AssignmentToItself"));
    }

    private static class ATIFix implements Fix, Task<WorkingCopy> {

        private static final int REMOVE = 0;
        private static final int QUALIFY = 1;
        private static final int NEW_PARAMETER = 2;
        private static final int NEW_FIELD = 3;
        
        private int kind;
        private TreePath treePath;
        private FileObject file;

        public ATIFix(int kind, TreePath treePath, FileObject file) {
            this.kind = kind;
            this.treePath = treePath;
            this.file = file;
        }
        
        public String getText() {
            
            switch( kind ) {
                case REMOVE:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_Remove_FIX"); // NOI18N
                case QUALIFY:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_Qualify_FIX"); // NOI18N
                case NEW_PARAMETER:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_NewParameter_FIX"); // NOI18N
                case NEW_FIELD:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_NewField_FIX"); // NOI18N
            } 
            
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public ChangeInfo implement() throws Exception {
            JavaSource js = JavaSource.forFileObject(file);
            try {
                js.runModificationTask(this).commit();
            }
            catch( IOException e ) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        public void run(WorkingCopy workingCopy) throws Exception {
            /*
            workingCopy.toPhase(Phase.RESOLVED);
            TreeMaker treeMaker = workingCopy.getTreeMaker();

            TreeUtilities treeUtilities = workingCopy.getTreeUtilities();

            AssignmentTree assignmentTree = (AssignmentTree)getEnclosingTreeOfKind(treePath, Tree.Kind.ASSIGNMENT);

            TreePath tpVar = new TreePath( treePath, assignmentTree.getVariable() );
            Element eVar = workingCopy.getTrees().getElement(tpVar);
            VariableTree vt = (VariableTree) workingCopy.getTrees().getTree(eVar); // XXX test iof


            VariableElement var = (VariableElement)eVar; // XXX test iof 

            MethodTree methodTree = (MethodTree)getEnclosingTreeOfKind(treePath, Tree.Kind.METHOD);

            MethodTree newMethod = treeMaker.addMethodParameter(methodTree, treeMaker.Variable(
                                    treeMaker.Modifiers(
                                        Collections.<Modifier>emptySet(),
                                        Collections.<AnnotationTree>emptyList()
                                    ),
                                    eVar.getSimpleName().toString(),
                                    vt.getType(),
                                    null
                                ));

            workingCopy.rewrite(methodTree, newMethod);
        
        */
        }
    }

    
}
