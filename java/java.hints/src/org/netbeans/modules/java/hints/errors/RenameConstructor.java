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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Name;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import org.netbeans.api.java.lexer.JavaTokenId;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenSequence;
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
                if (mt != member && member.getKind() == Kind.METHOD && "<init>".contentEquals(((MethodTree)member).getName()) //NOI18N
                        && !tu.isSynthetic(memberPath) && types.isSameType(types.erasure(trees.getTypeMirror(memberPath)), type)) {
                    return null;
                }
            }
            CompilationUnitTree cut = treePath.getCompilationUnit();
            int startPos = (int) trees.getSourcePositions().getStartPosition(cut, mt);
            int modEndPos = (int) trees.getSourcePositions().getEndPosition(cut, mt.getModifiers());
            int typeEndPos = (int) trees.getSourcePositions().getEndPosition(cut, mt.getReturnType());
            int namePos = typeEndPos != (-1) ? typeEndPos
                                             : modEndPos != (-1) ? modEndPos
                                                                 : startPos;
            String originalName = mt.getName().toString();
            //XXX!!!
            TokenSequence<?> ts = compilationInfo.getTokenHierarchy().tokenSequence();
            int end = (int) trees.getSourcePositions().getEndPosition(cut, mt);
            ts.move(namePos);
            while (ts.moveNext() && ts.offset() < end) {
                if (ts.token().id() == JavaTokenId.IDENTIFIER) {
                    originalName = ts.token().text().toString();
                    break;
                }
            }
            RenameConstructorFix fix = new RenameConstructorFix(compilationInfo.getSnapshot().getSource(), TreePathHandle.create(treePath, compilationInfo), offset, originalName, ct.getSimpleName().toString());
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
        private String oldConstructorName;
        private String newConstructorName;

        private RenameConstructorFix(Source source, TreePathHandle pathHandle, int offset, String oldConstructorName, String newConstructorName) {
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
