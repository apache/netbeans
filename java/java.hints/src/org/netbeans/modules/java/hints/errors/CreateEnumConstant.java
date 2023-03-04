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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Creates new Constant inside of an enum
 * 
 * @author Max Sauer
 */
class CreateEnumConstant extends ModificationResultBasedFix implements Fix {

    private String name;
    private String inFQN;
    private ClasspathInfo cpInfo;
    private FileObject targetFile;
    private ElementHandle<TypeElement> target;
    private TypeMirrorHandle<TypeMirror> proposedType;

    public CreateEnumConstant(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, TypeMirror proposedType, FileObject targetFile) {
        this.name = name;
        this.inFQN = target.getQualifiedName().toString();
        this.cpInfo = info.getClasspathInfo();
        this.targetFile = targetFile;
        this.target = ElementHandle.create(target);
        if (proposedType.getKind() == TypeKind.NULL) {
            TypeElement tel = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
            if (tel != null) {
                proposedType = tel.asType();
                this.proposedType = TypeMirrorHandle.create(proposedType);
            } else {
                this.proposedType = null;
            }
        } else {
            this.proposedType = TypeMirrorHandle.create(proposedType);
        }
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(CreateEnumConstant.class, "LBL_FIX_Create_Enum_Constant", name, inFQN);
    }

    @Override
    public ChangeInfo implement() throws Exception {
        ModificationResult diff = getModificationResult();
        return Utilities.commitAndComputeChangeInfo(targetFile, diff, null);
    }

    @Override
    public ModificationResult getModificationResult() throws IOException {
        JavaSource js = JavaSource.create(cpInfo, targetFile);

        return js.runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy working) throws IOException {
                working.toPhase(Phase.RESOLVED);
                TypeElement targetType = target.resolve(working);

                if (targetType == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target."); // NOI18N
                    return;
                }

                ClassTree targetTree = working.getTrees().getTree(targetType);

                if (targetTree == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target tree: " + targetType.getQualifiedName() + "."); // NOI18N
                    return;
                }
                
                TypeMirror proposedType = CreateEnumConstant.this.proposedType.resolve(working);
                TreeMaker make = working.getTreeMaker();

                int mods = 1<<14; //XXX enum flag. Creation of enum constant should be part of TreeMaker
                ModifiersTree modds = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                VariableTree var = make.Variable(modds, name, make.Type(proposedType), null);

                List<? extends Tree> members = targetTree.getMembers();
                ArrayList<Tree> newMembers = new ArrayList<Tree>(members);
                int pos = -1;
                for (Iterator<? extends Tree> it = members.iterator(); it.hasNext();) {
                    Tree t = it.next();
                    if (t.getKind() == Kind.VARIABLE && working.getTreeUtilities().isEnumConstant((VariableTree)t) ) {
                        pos = members.indexOf(t);
                    }
                }

                newMembers.add(pos+1, var);
                ClassTree enumm = make.Enum(targetTree.getModifiers(), targetTree.getSimpleName(), targetTree.getImplementsClause(), newMembers);
//                ClassTree decl = GeneratorUtilities.get(working).insertClassMember(targetTree, var);
                working.rewrite(targetTree, enumm);
            }
        });
    }

    String toDebugString(CompilationInfo info) {
        return "CreateEnumConstant:" + name + ":" + target.getQualifiedName() + ":" + proposedType.resolve(info).toString(); // NOI18N
    }
    
}
