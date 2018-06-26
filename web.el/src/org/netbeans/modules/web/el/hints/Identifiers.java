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
package org.netbeans.modules.web.el.hints;

import com.sun.el.parser.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.el.*;
import org.netbeans.modules.web.el.completion.ELStreamCompletionItem;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.ImplicitObjectType;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Checks for unknown EL identifiers, properties and methods.
 *
 * @author Erno Mononen
 */
public final class Identifiers extends ELRule {

    private static final List<ImplicitObjectType> ALL_NON_RAW_IMPLICIT_TYPES = Arrays.asList(
            ImplicitObjectType.MAP_TYPE,
            ImplicitObjectType.OBJECT_TYPE,
            ImplicitObjectType.SCOPE_TYPE);

    private WebModule webModule;
    private FileObject context;

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(ELHintsProvider.Kind.DEFAULT);
    }

    @Override
    public boolean appliesTo(RuleContext ruleContext) {
        this.context = ruleContext.parserResult.getSnapshot().getSource().getFileObject();
        if (context == null) {
            return false;
        }
        return (webModule = WebModule.getWebModule(context)) != null;
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    protected void run(final CompilationContext info, RuleContext ruleContext, final List<Hint> result) {
        final ELParserResult elResult = (ELParserResult)ruleContext.parserResult;
        final List<ELVariableResolver.VariableInfo> attrsVariables = ELVariableResolvers.getRawObjectProperties(info, "attrs", elResult.getSnapshot()); //NOI18N
        final List<ELVariableResolver.VariableInfo> ccVariables = ELVariableResolvers.getRawObjectProperties(info, "cc", elResult.getSnapshot());       //NOI18N
        for (final ELElement each : elResult.getElements()) {
            if (!each.isValid()) {
                // broken AST, skip
                continue;
            }

            each.getNode().accept(new NodeVisitor() {

                private Node parent;
                private boolean finished;
                private boolean inRawObject;

                @Override
                public void visit(final Node node) {
                    if (finished) {
                        return;
                    }
                    if (node instanceof AstMapData || node instanceof AstListData) {
                        parent = node;
                    } else if (node instanceof AstIdentifier) {
                        if (ELTypeUtilities.resolveElement(info, each, node) == null) {
                            // currently we can't reliably resolve all identifiers, so
                            // if we couldn't resolve the base identifier skip checking properties / methods
                            finished = true;
                        }
                        parent = node;
                    }
                    if (node instanceof AstDotSuffix || NodeUtil.isMethodCall(node)) {
                        if (parent == null) {
                            // parent is not static collection or identifier, don't care about the expression
                            return;
                        }
                        String image = parent.getImage();
                        boolean valid;
                        if (image != null && "cc".equals(image)) { //NOI18N
                            valid = isValidNode(info, each, parent, node, ccVariables);
                            inRawObject = true;
                        } else {
                            if (image != null && "attrs".equals(image)) { //NOI18N
                                inRawObject = false;
                            }
                            valid = isValidNode(info, each, parent, node, attrsVariables);
                        }
                        if (!valid && !inRawObject) {
                            Hint hint = new Hint(Identifiers.this,
                                    getMsg(node),
                                    elResult.getFileObject(),
                                    each.getOriginalOffset(node),
                                    Collections.<HintFix>emptyList(), 200);
                            result.add(hint);
                            finished = true; // warn only about the first unknown property
                        }
                        parent = node;
                    }
                }
            });
        }
    }

    private static boolean isValidNode(CompilationContext info, ELElement element, Node parent, Node node, List<ELVariableResolver.VariableInfo> variables) {
        // valid bean's property/method
        Element resolvedElement = ELTypeUtilities.resolveElement(info, element, node, Collections.<AstIdentifier, Node>emptyMap(), variables);
        if (resolvedElement != null) {
            return true;
        }

        // don't show the hint for implicit objects - it's more useless than helpful to show false warnings (since
        // the implementation class can differ and the many of implicit objects references just some kind of map)
        if (ELTypeUtilities.isImplicitObjectReference(info, parent, ALL_NON_RAW_IMPLICIT_TYPES, false)) {
            return true;
        }

        // don't show the hint for interface's object properties without any type
        if (ELTypeUtilities.isRawObjectReference(info, parent, false)) {
            String rawObject = ELTypeUtilities.getRawObjectName(node.jjtGetParent());
            if (rawObject != null) {
                for (ELVariableResolver.VariableInfo variableInfo : variables) {
                    if (rawObject.equals(variableInfo.name) 
                            && (variableInfo.clazz == null || variableInfo.clazz.equals(Object.class.getName()))) {
                        return true;
                    }
                }
            }
        }

        // EL3.0 stream method
        if (node.getImage() != null && ELStreamCompletionItem.STREAM_METHOD.equals(node.getImage())) {
            if (parent == null) {
                return false;
            } else {
                // valid operator for static list, set, map
                if (parent instanceof AstMapData || parent instanceof AstListData) {
                    return true;
                }
                // valid operator for Iterable or array return type
                if (parent instanceof AstDotSuffix) {
                    Element methodElement = ELTypeUtilities.resolveElement(info, element, parent);
                    if (methodElement == null) {
                        return true;
                    }
                    return ELTypeUtilities.isIterableElement(info, methodElement);
                }
            }
        }
        return false;
    }

    private static String getMsg(Node node) {
        if (node instanceof AstIdentifier) {
            return NbBundle.getMessage(Identifiers.class, "Identifiers_Unknown_Identifier", node.getImage());
        }
        if (NodeUtil.isMethodCall(node)) {
            String methodName = node instanceof AstBracketSuffix ? ELTypeUtilities.getBracketMethodName(node) : node.getImage();
            return NbBundle.getMessage(Identifiers.class, "Identifiers_Unknown_Method", methodName);
        }
        if (node instanceof AstDotSuffix) {
            return NbBundle.getMessage(Identifiers.class, "Identifiers_Unknown_Property", node.getImage());
        }
        assert false;
        return null;
    }
}
