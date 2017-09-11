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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.perf;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
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

        class EscapeFinder extends TreePathScanner<Boolean, Boolean> {
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
