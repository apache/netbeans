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
