/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): Lyle Franklin <lylejfranklin@gmail.com>
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.tools.Diagnostic;
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
    public static final Set<String> CODES = new HashSet<String>(Arrays.asList("compiler.warn.potential.lambda.found")); //NOI18N

    static final boolean DEF_PREFER_MEMBER_REFERENCES = true;

    @BooleanOption(displayName = "#LBL_Javac_canUseLambda_preferMemberReferences", tooltip = "#TP_Javac_canUseLambda_preferMemberReferences", defaultValue=DEF_PREFER_MEMBER_REFERENCES) //NOI18N
    static final String KEY_PREFER_MEMBER_REFERENCES = "prefer-member-references"; //NOI18N

    @TriggerPatterns({
        @TriggerPattern("new $clazz($params$) { $method; }") //NOI18N
    })
    @NbBundle.Messages("MSG_AnonymousConvertibleToLambda=This anonymous inner class creation can be turned into a lambda expression.")
    public static ErrorDescription computeAnnonymousToLambda(HintContext ctx) {
        ClassTree clazz = ((NewClassTree) ctx.getPath().getLeaf()).getClassBody();
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
