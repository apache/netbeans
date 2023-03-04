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

package org.netbeans.modules.java.hints.perf;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.StringBuffer2Builder", description = "#DESC_org.netbeans.modules.java.hints.perf.StringBuffer2Builder", category="performance", suppressWarnings="StringBufferMayBeStringBuilder",
        minSourceVersion = "5")
public class StringBuffer2Builder {

    @TriggerPattern(value="java.lang.StringBuffer $buffer = new java.lang.StringBuffer($args$);")
    public static ErrorDescription hint(final HintContext ctx) {
        final Element el = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (el == null || el.getKind() != ElementKind.LOCAL_VARIABLE) {
            return null;
        }

        class EscapeFinder extends ErrorAwareTreePathScanner<Boolean, Boolean> {
            @Override
            public Boolean reduce(Boolean r1, Boolean r2) {
                return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
            }

            @Override
            public Boolean visitMethodInvocation(MethodInvocationTree node, Boolean safe) {
                Element invokedOn = ctx.getInfo().getTrees().getElement(new TreePath(getCurrentPath(), node.getMethodSelect()));

                if (!el.equals(invokedOn)) {
                    if (scan(node.getMethodSelect(), true) == Boolean.TRUE) {
                        return true;
                    }
                }

                if (scan(node.getTypeArguments(), false) == Boolean.TRUE) {
                    return true;
                }

                if (scan(node.getArguments(), false) == Boolean.TRUE) {
                    return true;
                }

                return false;
            }

            @Override
            public Boolean visitIdentifier(IdentifierTree node, Boolean safe) {
                Element invokedOn = ctx.getInfo().getTrees().getElement(getCurrentPath());

                return safe != Boolean.TRUE && el.equals(invokedOn);
            }
        }

        boolean escapes = new EscapeFinder().scan(ctx.getPath().getParentPath(), null) == Boolean.TRUE;

        if (escapes) {
            return null;
        }

        String fixDisplayName = NbBundle.getMessage(StringBuffer2Builder.class, "FIX_StringBuffer2Builder");
        TreePath origType = new TreePath(ctx.getPath(), ((VariableTree) ctx.getPath().getLeaf()).getType());
        Fix fix = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "java.lang.StringBuilder $buffer = new java.lang.StringBuilder($args$);");
        String displayName = NbBundle.getMessage(StringBuffer2Builder.class, "ERR_StringBuffer2Builder");
        
        return ErrorDescriptionFactory.forName(ctx, origType, displayName, fix);
    }
}
