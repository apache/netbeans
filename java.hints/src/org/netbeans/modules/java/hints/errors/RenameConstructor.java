/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Name;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Dusan Balek
 */
public class RenameConstructor implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = Collections.singleton("compiler.err.invalid.meth.decl.ret.type.req"); // NOI18N

    @Override
    public Set<String> getCodes() {
        return ERROR_CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (treePath.getLeaf().getKind() == Kind.METHOD) {
            MethodTree mt = (MethodTree) treePath.getLeaf();
            TreePath parentPath = treePath.getParentPath();
            ClassTree ct = (ClassTree) parentPath.getLeaf();
            Trees trees = compilationInfo.getTrees();
            Types types = compilationInfo.getTypes();
            TreeUtilities tu = compilationInfo.getTreeUtilities();
            TypeMirror type = types.erasure(trees.getTypeMirror(treePath));
            if (!Utilities.isValidType(type)) {
                return null;
            }
            for (Tree member : ct.getMembers()) {
                TreePath memberPath = new TreePath(parentPath, member);
                if (member.getKind() == Kind.METHOD && "<init>".contentEquals(((MethodTree)member).getName()) //NOI18N
                        && !tu.isSynthetic(memberPath) && types.isSameType(types.erasure(trees.getTypeMirror(memberPath)), type)) {
                    return null;
                }
            }
            RenameConstructorFix fix = new RenameConstructorFix(compilationInfo.getSnapshot().getSource(), TreePathHandle.create(treePath, compilationInfo), offset, mt.getName(), ct.getSimpleName());
            return Collections.<Fix>singletonList(fix);
        }
        return null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public String getId() {
        return RenameConstructor.class.getName();
    }

    @Messages("LBL_RenameConstructor=Rename Constructor")
    @Override
    public String getDisplayName() {
        return Bundle.LBL_RenameConstructor();
    }

    @Messages("DSC_RenameConstructor=Rename Constructor")
    public String getDescription() {
        return Bundle.DSC_RenameConstructor();
    }

    static final class RenameConstructorFix implements Fix {

        private Source source;
        private TreePathHandle pathHandle;
        private int offset;
        private Name oldConstructorName;
        private Name newConstructorName;

        private RenameConstructorFix(Source source, TreePathHandle pathHandle, int offset, Name oldConstructorName, Name newConstructorName) {
            this.source = source;
            this.pathHandle = pathHandle;
            this.offset = offset;
            this.oldConstructorName = oldConstructorName;
            this.newConstructorName = newConstructorName;
        }

        @Messages({
            "# {0} - old name",
            "# {1} - new name",
            "FIX_RenameConstructor=Rename {0} to {1}"})
        @Override
        public String getText() {
            return Bundle.FIX_RenameConstructor(oldConstructorName, newConstructorName);
        }

        @Override
        public ChangeInfo implement() throws Exception {
            try {
                ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        WorkingCopy wc = WorkingCopy.get(resultIterator.getParserResult(offset));
                        wc.toPhase(Phase.PARSED);
                        TreePath path = pathHandle.resolve(wc);
                        if (path != null && path.getLeaf().getKind() == Kind.METHOD) {
                            MethodTree mt = (MethodTree) path.getLeaf();
                            wc.rewrite(mt, wc.getTreeMaker().setLabel(mt, newConstructorName));
                        }
                    }
                }).commit();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        public String toDebugString() {
            return "[RenameConstructorFix:" + oldConstructorName + ":" + newConstructorName + "]"; //NOI18N
        }
    }
}
