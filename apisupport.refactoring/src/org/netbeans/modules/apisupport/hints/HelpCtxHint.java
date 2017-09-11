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

package org.netbeans.modules.apisupport.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle.Messages;

@Hint(category="apisupport", displayName="#HelpCtxHint.displayName", description="#HelpCtxHint.description")
@Messages({
    "HelpCtxHint.displayName=HelpCtx issues",
    "HelpCtxHint.description=Warnings about misuse of org.openide.util.HelpCtx."
})
public class HelpCtxHint {

    @TriggerPatterns({
        @TriggerPattern("new org.openide.util.HelpCtx($1.class)"),
        @TriggerPattern("new org.openide.util.HelpCtx($1.class.getName())")
    })
    @Messages({
        "HelpCtx.onClass.warning=Use of HelpCtx(Class) is deprecated",
        "HelpCtx.onClassName.warning=Use of HelpCtx(Class.name) is hazardous",
        "HelpCtx.onClass.fix=Use constant corresponding to class name"
    })
    public static List<ErrorDescription> onClass(HintContext hctx) {
        String text;
        switch (((NewClassTree) hctx.getPath().getLeaf()).getArguments().get(0).getKind()) {
        case MEMBER_SELECT:
            text = HelpCtx_onClass_warning();
            break;
        case METHOD_INVOCATION:
            text = HelpCtx_onClassName_warning();
            break;
        default:
            throw new IllegalStateException();
        }
        final String name = hctx.getInfo().getElements().getBinaryName((TypeElement) hctx.getInfo().getTrees().getElement(hctx.getVariables().get("$1"))).toString();
        return Collections.singletonList(ErrorDescriptionFactory.forTree(hctx, hctx.getPath(), text, new JavaFix(hctx.getInfo(), hctx.getPath()) {
            @Override protected String getText() {
                return HelpCtx_onClass_fix();
            }
            @Override protected void performRewrite(TransformationContext tctx) throws Exception {
                NewClassTree ctor = (NewClassTree) tctx.getPath().getLeaf();
                WorkingCopy wc = tctx.getWorkingCopy();
                TreeMaker make = wc.getTreeMaker();
                wc.rewrite(ctor, make.NewClass(null, Collections.<ExpressionTree>emptyList(), ctor.getIdentifier(), Collections.singletonList(make.Literal(name)), null));
            }
        }.toEditorFix()));
    }

}
