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
