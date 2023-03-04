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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.EqualsMethodHint", description = "#DESC_org.netbeans.modules.java.hints.EqualsMethodHint", category="general", id="org.netbeans.modules.java.hints.EqualsMethodHint", suppressWarnings="EqualsWhichDoesntCheckParameterClass", options=Options.QUERY)
public class EqualsMethodHint {

    @TriggerPattern(value="$mods$ boolean equals(java.lang.Object $param) { $statements$; }")
    public static ErrorDescription run(HintContext ctx) {
        TreePath paramPath = ctx.getVariables().get("$param");

        assert paramPath != null;

        Element param = ctx.getInfo().getTrees().getElement(paramPath);
        
        if (param == null || param.getKind() != ElementKind.PARAMETER) {
            return null;
        }
        
        for (TreePath st : ctx.getMultiVariables().get("$statements$")) {
            try {
                new VisitorImpl(ctx.getInfo(), param).scan(st, null);
            } catch (Found f) {
                return null;
            }
        }

        return ErrorDescriptionFactory.forName(ctx,
                ctx.getPath(),
                NbBundle.getMessage(EqualsMethodHint.class, "ERR_EQUALS_NOT_CHECKING_TYPE"));
    }

    private static final class VisitorImpl extends ErrorAwareTreePathScanner<Void, Void> {
        
        private CompilationInfo info;
        private Element parameter;

        public VisitorImpl(CompilationInfo info, Element parameter) {
            this.info = info;
            this.parameter = parameter;
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree node, Void p) {
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));
            
            if (parameter.equals(e)) {
                throw new Found();
            }
            
            return super.visitInstanceOf(node, p);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
            if (node.getArguments().isEmpty()) {
                if (node.getMethodSelect().getKind() == Kind.MEMBER_SELECT) {
                    MemberSelectTree mst = (MemberSelectTree) node.getMethodSelect();
                    Element e = info.getTrees().getElement(new TreePath(new TreePath(getCurrentPath(), mst), mst.getExpression()));

                    if (parameter.equals(e) && mst.getIdentifier().contentEquals("getClass")) { // NOI18N
                        throw new Found();
                    }
                } else if (node.getMethodSelect().getKind() == Kind.IDENTIFIER) {
                    IdentifierTree it = (IdentifierTree) node.getMethodSelect();

                    if (it.getName().contentEquals("getClass")) { // NOI18N
                        throw new Found();
                    }
                }
            }
            
            return super.visitMethodInvocation(node, p);
        }
        
    }

    private static final class Found extends RuntimeException {}
}
