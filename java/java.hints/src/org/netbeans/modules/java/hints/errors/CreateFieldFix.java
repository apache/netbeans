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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class CreateFieldFix extends ModificationResultBasedFix implements EnhancedFix {
    
    private FileObject targetFile;
    private ElementHandle<TypeElement> target;
    private TypeMirrorHandle proposedType;
    private ClasspathInfo cpInfo;
    private Set<Modifier> modifiers;
    private final boolean remote;
    
    private String name;
    private String inFQN;
    
    public CreateFieldFix(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, TypeMirror proposedType, FileObject targetFile) {
        this.name = name;
        this.inFQN = Utilities.target2String(target);
        this.cpInfo = info.getClasspathInfo();
        this.modifiers = modifiers;
        this.targetFile = targetFile;
        this.target = ElementHandle.create(target);
        if (proposedType.getKind() == TypeKind.NULL || proposedType.getKind() == TypeKind.NONE) {
            TypeElement te = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
            proposedType = te == null ? null : te.asType();
        }
        this.proposedType = proposedType == null ? null : TypeMirrorHandle.create(proposedType);
        this.remote = !org.openide.util.Utilities.compareObjects(info.getFileObject(), targetFile);
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(CreateFieldFix.class, "LBL_FIX_Create_Field", name, inFQN);        
    }

    @Override
    public ChangeInfo implement() throws IOException {
        ModificationResult diff = getModificationResult();
        ChangeInfo ci = Utilities.commitAndComputeChangeInfo(targetFile, diff, null);
        if (remote) {
            return ci;
        } else {
            return null;
        }
    }

    @Override
    public ModificationResult getModificationResult() throws IOException {
        //use the original cp-info so it is "sure" that the proposedType can be resolved:
        JavaSource js = JavaSource.create(cpInfo, targetFile);
        ModificationResult diff = js.runModificationTask(new Task<WorkingCopy>() {
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

                TypeMirror proposedType = CreateFieldFix.this.proposedType.resolve(working);

                if (proposedType == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve proposed type."); // NOI18N
                    return;
                }

                TreeMaker make = working.getTreeMaker();
                TypeMirror tm = proposedType;
                VariableTree var = make.Variable(make.Modifiers(modifiers), name, make.Type(tm), null);
                ClassTree decl = GeneratorUtilities.get(working).insertClassMember(targetTree, var);
                working.rewrite(targetTree, decl);
            }
        });
        return diff;
    }

    String toDebugString(CompilationInfo info) {
        return "CreateFieldFix:" + name + ":" + target.getQualifiedName() + ":" + proposedType.resolve(info).toString() + ":" + modifiers; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CreateFieldFix other = (CreateFieldFix) obj;
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        if (this.modifiers != other.modifiers && (this.modifiers == null || !this.modifiers.equals(other.modifiers))) {
            return false;
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 79 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    @Override
    public CharSequence getSortText() {
        //see usage at org.netbeans.modules.editor.hints.FixData.getSortText(org.netbeans.spi.editor.hints.Fix):java.lang.CharSequence
    
        //creates ordering top to bottom: create local variable>create field>create parameter
        //see org.netbeans.modules.java.hints.errors.AddParameterOrLocalFix.getSortText():java.lang.CharSequence
        return "Create 6000 " + getText();
}
}
