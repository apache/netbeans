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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 *
 * @author David Strupl
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.StaticNonFinalUsedInInitialization", description = "#DESC_org.netbeans.modules.java.hints.StaticNonFinalUsedInInitialization", category="initialization", suppressWarnings={"StaticNonFinalUsedInInitialization", "", "NonFinalStaticVariableUsedInClassInitialization"}, options=Options.QUERY)
public class StaticNonFinalUsedInInitialization {
    public StaticNonFinalUsedInInitialization() {
    }

    @TriggerTreeKind(Tree.Kind.IDENTIFIER)
    public static ErrorDescription hint(HintContext ctx) {
        IdentifierTree it = (IdentifierTree) ctx.getPath().getLeaf();
        CompilationInfo info = ctx.getInfo();

        Element e = info.getTrees().getElement(ctx.getPath());
        if (e == null || !e.getKind().isField()) {
            return null;
        }
        if (!e.getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        if (e.getModifiers().contains(Modifier.FINAL)) {
            return null;
        }
        TreePath previous = ctx.getPath();
        TreePath current  = previous.getParentPath();

        OUTER: while (true) {
            if (current == null) {
                return null;
            }
            switch (current.getLeaf().getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case METHOD:
                    return null;
                case BLOCK:
                    if (((BlockTree) current.getLeaf()).isStatic()) {
                        break OUTER;
                    }
                    break;
                case VARIABLE:
                    if (((VariableTree) current.getLeaf()).getInitializer() != previous.getLeaf()) {
                        return null;//TODO: most likely not needed
                    }
                    
                    Element el = info.getTrees().getElement(current);

                    if (el != null && el.getKind().isField()) {
                        if (el.getModifiers().contains(Modifier.STATIC)) {
                            break OUTER;
                        } else {
                            break;
                        }
                    }
                    break;
                case ASSIGNMENT:
                    if (((AssignmentTree) current.getLeaf()).getExpression() != previous.getLeaf()) {
                        return null;
                    }
                    break;
            }

            previous = current;
            current = current.getParentPath();
        }

        return ErrorDescriptionFactory.forName(ctx, it,
                NbBundle.getMessage(
                    StaticNonFinalUsedInInitialization.class,
                    "MSG_org.netbeans.modules.java.hints.StaticNonFinalUsedInInitialization"));
    }
}
