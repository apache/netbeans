/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
class CreateEnumConstant implements Fix {

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

    public String getText() {
        return NbBundle.getMessage(CreateEnumConstant.class, "LBL_FIX_Create_Enum_Constant", name, inFQN);
    }

    public ChangeInfo implement() throws Exception {
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

        return Utilities.commitAndComputeChangeInfo(targetFile, diff, null);
    }

    String toDebugString(CompilationInfo info) {
        return "CreateEnumConstant:" + name + ":" + target.getQualifiedName() + ":" + proposedType.resolve(info).toString(); // NOI18N
    }
    
}
