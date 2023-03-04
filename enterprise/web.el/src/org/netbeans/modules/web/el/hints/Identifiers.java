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
