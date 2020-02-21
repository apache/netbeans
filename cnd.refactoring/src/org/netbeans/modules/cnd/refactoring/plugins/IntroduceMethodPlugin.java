/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.plugins;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Position;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.editor.api.FormattingSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.api.IntroduceMethodRefactoring;
import org.netbeans.modules.cnd.refactoring.api.IntroduceMethodRefactoring.IntroduceMethodContext.FunctionKind;
import org.netbeans.modules.cnd.refactoring.introduce.BodyFinder;
import static org.netbeans.modules.cnd.refactoring.plugins.CsmRefactoringPlugin.createProblem;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;

/**
 *
 */
public class IntroduceMethodPlugin extends CsmModificationRefactoringPlugin {

    private final IntroduceMethodRefactoring refactoring;
    // object affected by refactoring
    private BodyFinder referencedMethod;
    private BodyFinder.BodyResult bodyResult;

    public IntroduceMethodPlugin(IntroduceMethodRefactoring refactoring) {
        super(refactoring);
        this.refactoring = refactoring;
    }

    @Override
    protected Collection<CsmFile> getRefactoredFiles() {
        if (bodyResult == null) {
            return Collections.emptySet();
        }
        List<CsmFile> res = new ArrayList<>();
        res.add(bodyResult.getFunction().getContainingFile());
        if (bodyResult.getFunctionKind() == FunctionKind.MethodDefinition){
            CsmFunction functionDeclaration = bodyResult.getFunctionDeclaration();
            CsmFile containingFile = functionDeclaration.getContainingFile();
            if (!res.contains(containingFile)) {
                res.add(containingFile);
            }
        }
        return res;
    }

    @Override
    public Problem fastCheckParameters() {
        Problem p = null;
        String functionName = refactoring.getFunctionName();
        if (!CndLexerUtilities.isCppIdentifier(functionName)) {
            return CsmRefactoringPlugin.createProblem(p, true, NbBundle.getMessage(IntroduceMethodPlugin.class, "ERR_InvalidFunctionName", functionName));
        }
        IntroduceMethodRefactoring.ParameterInfo paramTable[] = refactoring.getParameterInfo();
        for (int i = 0; i < paramTable.length; i++) {
            IntroduceMethodRefactoring.ParameterInfo in = paramTable[i];
            if (in.getType().toString().endsWith("&") && in.isByRef()) { //NOI18N
                return CsmRefactoringPlugin.createProblem(p, true, NbBundle.getMessage(IntroduceMethodPlugin.class, "ERR_DoubleReference", new Object[]{}));
            }
        }
        return p;
    }

    /**
     * Returns list of problems. For the change function signature, there are two
     * possible warnings - if the method is overridden or if it overrides
     * another method.
     *
     * @return  overrides or overridden problem or both
     */
    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 5);
        //CsmRefactoringUtils.waitParsedAllProjects();
        fireProgressListenerStep();
        try {
            CsmContext editorContext = getEditorContext();
            AtomicBoolean canceled = new AtomicBoolean();
            referencedMethod = new BodyFinder(editorContext.getDocument(), editorContext.getFileObject(), editorContext.getFile(),
                    editorContext.getCaretOffset(), editorContext.getStartOffset(), editorContext.getEndOffset(), canceled);
            refactoring.getRefactoringSource();
            // check if resolved element
            bodyResult = referencedMethod.findBody();
            if (bodyResult == null) {
                return new Problem(true, NbBundle.getMessage(IntroduceMethodPlugin.class, "ERR_BadSelection"));
            }
            if (!bodyResult.isApplicable(new AtomicBoolean(false))) {
                return new Problem(true, NbBundle.getMessage(IntroduceMethodPlugin.class, "ERR_BadSelection"));
            }
            refactoring.setIntroduceMethodContext(bodyResult);
        } finally {
            fireProgressListenerStop();
        }
        return preCheckProblem;
    }

    @Override
    protected final void processFile(CsmFile csmFile, ModificationResult mr, AtomicReference<Problem> outProblem) {
        CsmFile containingFile = bodyResult.getFunction().getContainingFile();
        if (csmFile.equals(containingFile)) {
            ArrayList<ModificationResult.Difference> diffs = new ArrayList<>();
            // definition
            FileObject fo = CsmUtilities.getFileObject(csmFile);
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);

            String method = FormattingSupport.getIndentedText(bodyResult.getDocument(), bodyResult.getInsetionOffset(), refactoring.getMethodDefinition()).toString();
            PositionRef startPos = ces.createPositionRef(bodyResult.getInsetionOffset(), Position.Bias.Forward);
            PositionRef endPos = ces.createPositionRef(bodyResult.getInsetionOffset(), Position.Bias.Backward);
            String message;
            if (bodyResult.getFunctionKind() == FunctionKind.Function) {
                message = NbBundle.getMessage(IntroduceMethodPlugin.class, "LBL_Preview_FunctionDefinition"); // NOI18N
            } else {
                message = NbBundle.getMessage(IntroduceMethodPlugin.class, "LBL_Preview_MethodDefinition"); // NOI18N
            }
            ModificationResult.Difference diff = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, startPos, endPos, "", method, message);
            diffs.add(diff);

            String methodCall =refactoring.getMethodCall();
            startPos = ces.createPositionRef(bodyResult.getSelectionFrom(), Position.Bias.Forward);
            endPos = ces.createPositionRef(bodyResult.getSelectionTo(), Position.Bias.Backward);
            if (bodyResult.getFunctionKind() == FunctionKind.Function) {
                message = NbBundle.getMessage(IntroduceMethodPlugin.class, "LBL_Preview_FunctionCall"); // NOI18N
            } else {
                message = NbBundle.getMessage(IntroduceMethodPlugin.class, "LBL_Preview_MethodCall"); // NOI18N
            }
            diff = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, startPos, endPos, "", methodCall, message);
            diffs.add(diff);

            if (bodyResult.getFunctionKind() == FunctionKind.MethodDefinition){
                CsmFunction functionDeclaration = bodyResult.getFunctionDeclaration();
                CsmFile containingFile2 = functionDeclaration.getContainingFile();
                if (containingFile.equals(containingFile2)) {
                    String decl = FormattingSupport.getIndentedText(CsmUtilities.openDocument(ces), refactoring.getDeclarationInsetOffset(), refactoring.getMethodDeclaration()).toString();
                    startPos = ces.createPositionRef(refactoring.getDeclarationInsetOffset(), Position.Bias.Forward);
                    endPos = ces.createPositionRef(refactoring.getDeclarationInsetOffset(), Position.Bias.Backward);
                    diff = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, startPos, endPos, "", decl,
                            NbBundle.getMessage(IntroduceMethodPlugin.class, "LBL_Preview_MethodDeclaration")); // NOI18N
                    diffs.add(diff);
                }
            }
            Collections.sort(diffs, new Comparator<ModificationResult.Difference>(){
                @Override
                public int compare(ModificationResult.Difference o1, ModificationResult.Difference o2) {
                    return o1.getStartPosition().getOffset() - o2.getStartPosition().getOffset();
                }
            });
            for (ModificationResult.Difference df : diffs) {
                mr.addDifference(fo, df);
            }
        } else {
            containingFile = bodyResult.getFunctionDeclaration().getContainingFile();
            if (csmFile.equals(containingFile)) {
                FileObject fo = CsmUtilities.getFileObject(csmFile);
                CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
                String decl = FormattingSupport.getIndentedText(CsmUtilities.openDocument(ces), refactoring.getDeclarationInsetOffset(), refactoring.getMethodDeclaration()).toString();
                PositionRef startPos = ces.createPositionRef(refactoring.getDeclarationInsetOffset(), Position.Bias.Forward);
                PositionRef endPos = ces.createPositionRef(refactoring.getDeclarationInsetOffset(), Position.Bias.Backward);
                ModificationResult.Difference diff = new ModificationResult.Difference(ModificationResult.Difference.Kind.INSERT, startPos, endPos, "", decl,
                        NbBundle.getMessage(IntroduceMethodPlugin.class, "LBL_Preview_MethodDeclaration")); // NOI18N
                mr.addDifference(fo, diff);
            }
        }
    }
}
