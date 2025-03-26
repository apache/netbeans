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
/*
 * Contributor(s): Lyle Franklin <lylejfranklin@gmail.com>
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@NbBundle.Messages({
    "FIX_ConvertToMemberReference=Use member reference", //NOI18N
    "FIX_ConvertToLambda=Use lambda expression" //NOI18N    
})
@Hint(displayName = "#DN_Javac_canUseLambda", description = "#DESC_Javac_canUseLambda", id = ConvertToLambda.ID, category = "rules15", suppressWarnings="Convert2Lambda",
        minSourceVersion = "8") //NOI18N
public class ConvertToLambda {

    public static final String ID = "Javac_canUseLambda"; //NOI18N
    public static final Set<String> CODES = new HashSet<>(Arrays.asList("compiler.warn.potential.lambda.found")); //NOI18N

    static final boolean DEF_PREFER_MEMBER_REFERENCES = true;

    @BooleanOption(displayName = "#LBL_Javac_canUseLambda_preferMemberReferences", tooltip = "#TP_Javac_canUseLambda_preferMemberReferences", defaultValue=DEF_PREFER_MEMBER_REFERENCES) //NOI18N
    static final String KEY_PREFER_MEMBER_REFERENCES = "prefer-member-references"; //NOI18N

    @TriggerPatterns({
        @TriggerPattern("new $clazz($params$) { $method; }") //NOI18N
    })
    @NbBundle.Messages("MSG_AnonymousConvertibleToLambda=This anonymous inner class creation can be turned into a lambda expression.")
    public static ErrorDescription computeAnnonymousToLambda(HintContext ctx) {
        ConvertToLambdaPreconditionChecker preconditionChecker =
                new ConvertToLambdaPreconditionChecker(ctx.getPath(), ctx.getInfo());
        if (!preconditionChecker.passesFatalPreconditions()) {
            return null;
        }

        FixImpl fix = new FixImpl(ctx.getInfo(), ctx.getPath(), false);
        if (ctx.getPreferences().getBoolean(KEY_PREFER_MEMBER_REFERENCES, DEF_PREFER_MEMBER_REFERENCES)
                && (preconditionChecker.foundMemberReferenceCandidate() || preconditionChecker.foundConstructorReferenceCandidate())) {
            return ErrorDescriptionFactory.forTree(ctx, ((NewClassTree) ctx.getPath().getLeaf()).getIdentifier(), Bundle.MSG_AnonymousConvertibleToLambda(),
                    new FixImpl(ctx.getInfo(), ctx.getPath(), true).toEditorFix(), fix.toEditorFix());
        }
        return ErrorDescriptionFactory.forTree(ctx, ((NewClassTree) ctx.getPath().getLeaf()).getIdentifier(), 
                Bundle.MSG_AnonymousConvertibleToLambda(), fix.toEditorFix());
    }

    private static final class FixImpl extends JavaFix {

        private final boolean useMemberReference;

        public FixImpl(CompilationInfo info, TreePath path, boolean useMemberReference) {
            super(info, path);
            this.useMemberReference = useMemberReference;
        }

        @Override
        public String getText() {
            return useMemberReference ? Bundle.FIX_ConvertToMemberReference(): Bundle.FIX_ConvertToLambda();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws IOException {

            WorkingCopy copy = ctx.getWorkingCopy();
            copy.toPhase(Phase.RESOLVED);

            TreePath tp = ctx.getPath();

            if (tp.getLeaf().getKind() != Kind.NEW_CLASS) {
                //XXX: warning
                return;
            }

            ConvertToLambdaConverter converter = new ConvertToLambdaConverter(tp, copy);
            if (useMemberReference) {
                converter.performRewriteToMemberReference();
            } else {
                converter.performRewriteToLambda();
            }
        }
    }
}
