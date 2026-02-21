/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.el.completion;

import com.sun.el.parser.AstAssign;
import com.sun.el.parser.AstBracketSuffix;
import com.sun.el.parser.AstDeferredExpression;
import com.sun.el.parser.AstDotSuffix;
import com.sun.el.parser.AstDynamicExpression;
import com.sun.el.parser.AstFunction;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstInteger;
import com.sun.el.parser.AstLambdaExpression;
import com.sun.el.parser.AstListData;
import com.sun.el.parser.AstMapData;
import com.sun.el.parser.AstMethodArguments;
import com.sun.el.parser.AstSemiColon;
import com.sun.el.parser.AstString;
import com.sun.el.parser.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.el.AstPath;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.ELVariableResolvers;
import org.netbeans.modules.web.el.NodeUtil;
import org.netbeans.modules.web.el.ResourceBundles;
import org.netbeans.modules.web.el.ResourceBundles.Location;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.netbeans.modules.web.el.spi.Function;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Code completer for Expression Language.
 *
 * @author Erno Mononen
 */
public final class ELCodeCompletionHandler implements CodeCompletionHandler2 {

    private static Set<String> keywordFixedTexts = null;

    /**
     * Gets Set of {@link ELTokenId#fixedText()} values with {@link ELTokenId.ELTokenCategories.KEYWORDS} category.<br/>
     * Result is resolved and stored on first method call into static field {@link #keywordFixedTexts}.
     * On next call is returned the same Set instance.
     * @return Set of {@link ELTokenId#fixedText()} values with {@link ELTokenId.ELTokenCategories.KEYWORDS} category.
     */
    private static synchronized Set<String> getKeywordFixedTexts() {
        if (keywordFixedTexts == null) {
            keywordFixedTexts = new HashSet<>();
            for (ELTokenId elTokenId : ELTokenId.values()) {
                if (ELTokenId.ELTokenCategories.KEYWORDS.hasCategory(elTokenId)) {
                    keywordFixedTexts.add(elTokenId.fixedText());
                }
            }
        }
        return keywordFixedTexts;
    }

    @Override
    public CodeCompletionResult complete(final CodeCompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<>(50);
        CodeCompletionResult result = new DefaultCompletionResult(proposals, false);
        final ELElement element = getElementAt(context.getParserResult(), context.getCaretOffset());
        if (element == null || !element.isValid()) {
            return CodeCompletionResult.NONE;
        }
        final Node target = getTargetNode(element, context.getCaretOffset());
        if(target == null) {
            //completion called outside of the EL content, resp. inside the
            //delimiters #{ or } area
            return CodeCompletionResult.NONE;
        }
        AstPath path = new AstPath(element.getNode());
        final List<Node> rootToNode = path.rootToNode(target);
        if (rootToNode.isEmpty()) {
            return result;
        }
        final PrefixMatcher prefixMatcher = PrefixMatcher.create(target, context);
        if (prefixMatcher == null) {
            return CodeCompletionResult.NONE;
        }
        // see if it is bundle key in the array notation and if so complete them
        if (target instanceof AstString) {
            ResourceBundles bundle = ResourceBundles.get(getFileObject(context));
            String bundleIdentifier = bundle.findResourceBundleIdentifier(path);
            if (bundleIdentifier != null) {
                proposeBundleKeysInArrayNotation(context, prefixMatcher, element, bundleIdentifier, (AstString) target, proposals);
                return proposals.isEmpty() ? CodeCompletionResult.NONE : result;
            }
        }

        final Node nodeToResolve = getNodeToResolve(target, rootToNode);
        final Map<AstIdentifier, Node> assignments = getAssignments(context.getParserResult(), context.getCaretOffset());
        final FileObject file = context.getParserResult().getSnapshot().getSource().getFileObject();
        JavaSource jsource = JavaSource.create(ELTypeUtilities.getElimplExtendedCPI(file));
        try {
            jsource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationContext ccontext = CompilationContext.create(file, info);

                    // assignments to resolve
                    Node node = nodeToResolve instanceof AstIdentifier && assignments.containsKey((AstIdentifier) nodeToResolve) ?
                            assignments.get((AstIdentifier) nodeToResolve) : nodeToResolve;

                    // fetch information from the JSF editor for resolving beans if necessary for cc:interface elements
                    List<VariableInfo> attrsObjects = Collections.<VariableInfo>emptyList();
                    if (ELTypeUtilities.isRawObjectReference(ccontext, node, false)) {
                        attrsObjects = ELVariableResolvers.getRawObjectProperties(ccontext, "attrs", context.getParserResult().getSnapshot()); //NOI18N
                    }

                    Element resolved = null;
                    if (!isInLambda(rootToNode)) {
                        // resolve the element
                        resolved = ELTypeUtilities.resolveElement(ccontext, element, nodeToResolve, assignments, attrsObjects);
                    }

                    if (ELTypeUtilities.isStaticIterableElement(ccontext, node)) {
                        proposeStream(ccontext, context, prefixMatcher, proposals);
                    } else if (ELTypeUtilities.isRawObjectReference(ccontext, node, true)) {
                        proposeRawObjectProperties(ccontext, context, prefixMatcher, node, proposals);
                    } else if (ELTypeUtilities.isScopeObject(ccontext, node)) {
                        // seems to be something like "sessionScope.^", so complete beans from the scope
                        proposeBeansFromScope(ccontext, context, prefixMatcher, element, node, proposals);
                    } else if (ELTypeUtilities.isResourceBundleVar(ccontext, node)) {
                        proposeBundleKeysInDotNotation(ccontext, context, prefixMatcher, element, node, proposals);
                    } else if (resolved == null) {
                        if (target instanceof AstDotSuffix == false && node instanceof AstFunction == false) {
                            proposeFunctions(ccontext, context, prefixMatcher, element, proposals);
                            proposeManagedBeans(ccontext, context, prefixMatcher, element, proposals);
                            proposeBundles(ccontext, context, prefixMatcher, element, proposals);
                            proposeVariables(ccontext, context, prefixMatcher, element, proposals);
                            proposeImpicitObjects(ccontext, context, prefixMatcher, proposals);
                            proposeKeywords(context, prefixMatcher, proposals);
                            proposeAssignements(context, prefixMatcher, assignments, proposals);
                        }
                        if (ELStreamCompletionItem.STREAM_METHOD.equals(node.getImage())) {
                            proposeOperators(ccontext, context, prefixMatcher, element, proposals, rootToNode, isBracketProperty(target, rootToNode));
                        }
                        ELJavaCompletion.propose(ccontext, context, element, target, proposals);
                    } else {
                        proposeMethods(ccontext, context, resolved, prefixMatcher, element, proposals, rootToNode, isBracketProperty(target, rootToNode));
                        if (ELTypeUtilities.isIterableElement(ccontext, resolved)) {
                            proposeStream(ccontext, context, prefixMatcher, proposals);
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        

        return proposals.isEmpty() ? CodeCompletionResult.NONE : result;
    }

    private static boolean isBracketProperty(Node target, List<Node> rootToNode) {
        Node previous = rootToNode.get(rootToNode.size() - 1);
        return (target instanceof AstString && previous instanceof AstBracketSuffix);
    }

    private static Node getNodeToResolve(Node target, List<Node> rootToNode) {
        Node previous = rootToNode.get(rootToNode.size() - 1);
        // due to the ast structure in the case of identifiers we need to try to resolve the type of the identifier,
        // otherwise the type of the preceding node.
        if (target instanceof AstString && previous instanceof AstBracketSuffix) {
            return rootToNode.get(rootToNode.size() - 2);
        } else if (target instanceof AstIdentifier
                && ((previous instanceof AstIdentifier || previous instanceof AstDotSuffix || NodeUtil.isMethodCall(previous))
                    || target.jjtGetParent() instanceof AstSemiColon)) {
            return target;
        } else {
            for (int i = rootToNode.size() - 1; i >= 0; i--) {
                Node node = rootToNode.get(i);
                if (isArrayIndexCall(rootToNode, i)) {
                    // array item call - bean.myArray[0].
                    return node;
                } else if (node instanceof AstMethodArguments) {
                    // prvious node was method call
                    return rootToNode.get(i - 1);
                } else if (node instanceof AstListData || node instanceof AstMapData) {
                    return node;
                } else if (node.jjtGetParent() instanceof AstSemiColon) {
                    return previous;
                } else if (node instanceof AstIdentifier || node instanceof AstDotSuffix) {
                    return node;
                }
            }
            return previous;
        }
    }

    private static boolean isArrayIndexCall(List<Node> rootToNode, int nodeIndex) {
        for (int i = nodeIndex; i >= 0; i--) {
            if (rootToNode.get(nodeIndex) instanceof AstInteger && rootToNode.get(i) instanceof AstBracketSuffix) {
                return true;
            }
        }
        return false;
    }

    private static ELElement getElementAt(ParserResult parserResult, int offset) {
        ELParserResult elParserResult = (ELParserResult) parserResult;
        ELElement result = elParserResult.getElementAt(offset);
        if (result == null || result.isValid()) {
            return result;
        } 
        // try to sanitize
        ELSanitizer sanitizer = new ELSanitizer(result, offset - result.getOriginalOffset().getStart());
        return sanitizer.sanitized();
    }

    private Map<AstIdentifier, Node> getAssignments(ParserResult parserResult, int offset) {
        Map<AstIdentifier, Node> result = new HashMap<>();
        ELParserResult elParserResult = (ELParserResult) parserResult;
        for (ELElement elElement : elParserResult.getElementsTo(offset)) {
            if (elElement.getError() != null) {
                elElement = getElementAt(parserResult, offset);
            }
            if (elElement.getNode() == null) {
                continue;
            }
            AstPath astPath = new AstPath(elElement.getNode());
            for (Node node : astPath.rootToLeaf()) {
                if (node instanceof AstAssign) {
                    Node leftSide = node.jjtGetChild(0);
                    Node leaf = getLastAssigneableLeaf(elElement.getNode());
                    Node targetNode = getTargetNode(elElement, elElement.getOriginalOffset().getStart() + leaf.endOffset());
                    Node rightSide = getNodeToResolve(targetNode, astPath.rootToNode(targetNode, true));
                    if (leftSide instanceof AstIdentifier && rightSide instanceof Node) {
                        result.put((AstIdentifier) leftSide, rightSide);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isInLambda(List<Node> rootToNode) {
        boolean inLambda = false;
        for (int i = rootToNode.size() - 1; i >= 0; i--) {
            Node node = rootToNode.get(i);
            if (node instanceof AstDotSuffix) {
                break;
            } else if (node instanceof AstLambdaExpression) {
                inLambda = true;
            }
        }
        return inLambda;
    }

    private static Node getLastAssigneableLeaf(Node root) {
        AstPath astPath = new AstPath(root);
        for (Node node : astPath.rootToLeaf()) {
            if (node instanceof AstSemiColon) {
                for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                    Node childNode = node.jjtGetChild(i);
                    AstPath path = new AstPath(childNode);
                    return getNodeToResolve(childNode, path.rootToLeaf());
                }
            }
        }
        return getNodeToResolve(root, astPath.rootToLeaf());
    }

    /**
     * @param offset swing <b>document</b> offset
     */
    private Node getTargetNode(ELElement element, int offset) {
        Node result = element.findNodeAt(offset);
        // in EL AST for example #{foo.bar^} the caret is at a deferred expression node, whereas
        // for code completion we need the "bar" property node; the code below tries to accomplish
        // that
        if (result instanceof AstDeferredExpression || result instanceof AstDynamicExpression) {
            //ensure we do not jump out from the expression && do not offer EL stuff in delimiters
            int relativeOffset = offset - element.getOriginalOffset().getStart();
            assert relativeOffset >= 0;
            
            //do the workaround only if the completion called at or after 
            //the opening EL delimiter #{ or ${, not before or inside it
            if(relativeOffset >= 2) { 
                Node realTarget = element.findNodeAt(offset - 1);
                if (realTarget != null) {
                    result = realTarget;
                }
            } else {
                //ensure no EL completion before or inside the delimiters
                return null; 
            }
        }
        return result;
    }

    private FileObject getFileObject(CodeCompletionContext context) {
        return context.getParserResult().getSnapshot().getSource().getFileObject();
    }

    private void proposeOperators(CompilationContext ccontext, CodeCompletionContext context,
            PrefixMatcher prefixMatcher, ELElement element, List<CompletionProposal> proposals, List<Node> rootToNode,
            boolean isBracketProperty) {
        TypeElement streamElement = ccontext.info().getElements().getTypeElement("com.sun.el.stream.Stream"); //NOI18N
        if (streamElement != null) {
            proposeJavaMethodsForElements(ccontext, context, prefixMatcher, element,
                    Arrays.<Element>asList(streamElement), isBracketProperty, proposals);
        }
    }

    private void proposeMethods(CompilationContext info, CodeCompletionContext context, Element resolved,
            PrefixMatcher prefix, ELElement elElement,List<CompletionProposal> proposals, List<Node> rootToNode,
            boolean isBracketProperty) {
        List<Element> allTypes = ELTypeUtilities.getSuperTypesFor(info, resolved, elElement, rootToNode);
        proposeJavaMethodsForElements(info, context, prefix, elElement, allTypes, isBracketProperty, proposals);
    }

    private void proposeJavaMethodsForElements(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<Element> elements, boolean isBracketCall,
            List<CompletionProposal> proposals) {
        for(Element element : elements) {
            for (ExecutableElement enclosed : ElementFilter.methodsIn(element.getEnclosedElements())) {
                //do not propose Object's members
                if(element.getSimpleName().contentEquals("Object")) { //NOI18N
                    //XXX hmm, not an ideal non-fqn check
                    continue;
                }

                if (!enclosed.getModifiers().contains(Modifier.PUBLIC) ||
                        enclosed.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }
                boolean hasParameters = !enclosed.getParameters().isEmpty();

                String methodName = enclosed.getSimpleName().toString();
                String propertyName = RefactoringUtil.getPropertyName(methodName, enclosed.getReturnType(), true);
                if (hasParameters) {
                    propertyName = methodName;
                }
                if (!prefix.matches(propertyName)) {
                    continue;
                }

                // Now check methodName or propertyName is EL keyword.
                if (getKeywordFixedTexts().contains(propertyName) ||
                        getKeywordFixedTexts().contains(methodName) ) {
                    continue;
                }
                if (ELPlugin.Query.isValidProperty(enclosed, context.getParserResult().getSnapshot().getSource(), info, context)) {
                    ELVariableCompletionItem completionItem = new ELVariableCompletionItem(propertyName, ELTypeUtilities.getTypeNameFor(info, enclosed));
                    completionItem.setSmart(true);
                    completionItem.setAnchorOffset(context.getCaretOffset() - prefix.length());

                    if (!contains(proposals, propertyName)) {
                        proposals.add(completionItem);
                    }
                } else {
                    ELJavaCompletionItem completionItem;

                    if (!contains(proposals, propertyName)) {
                        if (!hasParameters) {
                            completionItem = new ELJavaCompletionItem(info, enclosed, elElement, isBracketCall); //
                        } else {
                            completionItem = new ELJavaCompletionItem(info, enclosed, methodName, elElement, isBracketCall);
                        }

                        completionItem.setSmart(false);
                        completionItem.setAnchorOffset(context.getCaretOffset() - prefix.length());

                        proposals.add(completionItem);
                    }
                }
            }
        }
    }

    private void proposeStream(CompilationContext info, CodeCompletionContext context, PrefixMatcher prefix, List<CompletionProposal> proposals) {
        if (prefix.matches(ELStreamCompletionItem.STREAM_METHOD)) {
            proposals.add(new ELStreamCompletionItem(context.getCaretOffset() - prefix.length()));
        }
    }

    private boolean contains(List<CompletionProposal> completionProposals, String proposalName) {
        if (proposalName == null || proposalName.isEmpty()) {
            return true;
        }
        for (CompletionProposal completionProposal : completionProposals) {
            if (proposalName.equals(completionProposal.getName())) {
                return true;
            }
        }
        return false;
    }

    private void proposeImpicitObjects(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, List<CompletionProposal> proposals) {

        for (org.netbeans.modules.web.el.spi.ImplicitObject implicitObject : ELTypeUtilities.getImplicitObjects(info)) {
            if (prefix.matches(implicitObject.getName())) {
                ELImplictObjectCompletionItem item = new ELImplictObjectCompletionItem(implicitObject.getName(), implicitObject.getClazz());
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }

    private void proposeKeywords(CodeCompletionContext context,
            PrefixMatcher prefix, List<CompletionProposal> proposals) {

        for (ELTokenId elToken : ELTokenId.values()) {
            if (!ELTokenId.ELTokenCategories.KEYWORDS.hasCategory(elToken)) {
                continue;
            }
            if (elToken.fixedText() == null) {
                continue;
            }
            if (prefix.matches(elToken.fixedText())) {
                ELKeywordCompletionItem item = new ELKeywordCompletionItem(elToken.fixedText());
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                proposals.add(item);
            }

        }
    }

    private void proposeAssignements(CodeCompletionContext context, PrefixMatcher prefix,
            Map<AstIdentifier, Node> assignments, List<CompletionProposal> proposals) {

        for (Map.Entry<AstIdentifier, Node> entry : assignments.entrySet()) {
            AstIdentifier variable = entry.getKey();
            if (prefix.matches(variable.getImage())) {
                ELAssignedVariableCompletionItem item = new ELAssignedVariableCompletionItem(variable.getImage());
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                proposals.add(item);
            }
        }
    }

    private void proposeManagedBeans(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        for (VariableInfo bean : ELVariableResolvers.getManagedBeans(info, getFileObject(context))) {
            if (!prefix.matches(bean.name)) {
                continue;
            }
            Element element = ELTypeUtilities.getElementForType(info, bean.clazz);
            if(element == null) {
                continue; //unresolvable bean class name
            }
            ELJavaCompletionItem item = new ELJavaCompletionItem(info, element, bean.name, elElement);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            if (!contains(proposals, bean.name)) {
                proposals.add(item);
            }
        }
    }

    private void proposeBeansFromScope(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, Node scopeNode, List<CompletionProposal> proposals) {

        String scope = scopeNode.getImage();
        // this is ugly, but in the JSF model beans
        // are stored to "session", "application" etc (instead of "sessionScope").
        // see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope
        final String scopeString = "Scope";//NOI18N
        if (scope.endsWith(scopeString)) {
            scope = scope.substring(0, scope.length() - scopeString.length());
        }

        for (VariableInfo bean : ELVariableResolvers.getBeansInScope(info, scope, context.getParserResult().getSnapshot())) {
            if (!prefix.matches(bean.name)) {
                continue;
            }
            Element element = ELTypeUtilities.getElementForType(info, bean.clazz);
            ELJavaCompletionItem item = new ELJavaCompletionItem(info, element, elElement);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeRawObjectProperties(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, Node scopeNode, List<CompletionProposal> proposals) {

        for (VariableInfo property : ELVariableResolvers.getRawObjectProperties(info, scopeNode.getImage(), context.getParserResult().getSnapshot())) {
            if (!prefix.matches(property.name)) {
                continue;
            }
            ELRawObjectPropertyCompletionItem item = new ELRawObjectPropertyCompletionItem(property.name, property.clazz);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeVariables(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        for (VariableInfo bean : ELVariableResolvers.getVariables(info, context.getParserResult().getSnapshot(), context.getCaretOffset())) {
            if (!prefix.matches(bean.name)) {
                continue;
            }
            if(bean.clazz == null) {
                //probably a refered (w/o type) variable, just show it in the completion w/o type
                ELVariableCompletionItem item = new ELVariableCompletionItem(bean.name, bean.expression);
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);

            } else {
                //resolved variable
                Element element = ELTypeUtilities.getElementForType(info, bean.clazz);
                if (element == null) {
                    continue;
                }
                ELJavaCompletionItem item = new ELJavaCompletionItem(info, element, elElement);
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }

    private void proposeBundles(CompilationContext ccontext, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        ResourceBundles resourceBundles = ResourceBundles.get(getFileObject(context));
        if (!resourceBundles.canHaveBundles()) {
            return;
        }
        for (ResourceBundle bundle : resourceBundles.getBundles(ccontext.context())) {
            if (!prefix.matches(bundle.getVar())) {
                continue;
            }
            ELResourceBundleCompletionItem item = new ELResourceBundleCompletionItem(ccontext.file(), bundle, resourceBundles);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }

    }

    private void proposeBundleKeysInArrayNotation(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, String bundleKey, AstString target, List<CompletionProposal> proposals) {

        if (target.getImage().isEmpty()
                || elElement.getOriginalOffset(target).getStart() >= context.getCaretOffset()) {
            return;
        }
        ResourceBundles resourceBundles = ResourceBundles.get(getFileObject(context));
        if (!resourceBundles.canHaveBundles()) {
            return;
        }
        ResolverContext resolverContext = new ResolverContext();
        FileObject bundleFile = null;
        List<Location> bundleLocations = resourceBundles.getLocationsForBundleIdent(resolverContext, bundleKey);
        if (!bundleLocations.isEmpty()) {
            bundleFile = bundleLocations.get(0).getFile();
        }
        for (Map.Entry<String, String> entry : resourceBundles.getEntries(resolverContext, bundleKey).entrySet()) {
            if (!prefix.matches(entry.getKey())) {
                continue;
            }
            ELResourceBundleKeyCompletionItem item = new ELResourceBundleKeyCompletionItem(entry.getKey(), entry.getValue(), elElement, bundleFile);
            item.setSmart(true);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }
    }
    
    // "msg.key" notation
    private void proposeBundleKeysInDotNotation(CompilationContext ccontext,
            CodeCompletionContext context,
            PrefixMatcher prefix, 
            ELElement elElement, 
            Node baseObjectNode,
            List<CompletionProposal> proposals) {
        
        String bundleKey = baseObjectNode.getImage();
        ResourceBundles resourceBundles = ResourceBundles.get(getFileObject(context));
        if (!resourceBundles.canHaveBundles()) {
            return;
        }
        FileObject bundleFile = null;
        List<Location> bundleLocations = resourceBundles.getLocationsForBundleIdent(ccontext.context(), bundleKey);
        if (!bundleLocations.isEmpty()) {
            bundleFile = bundleLocations.get(0).getFile();
        }
        for (Map.Entry<String, String> entry : resourceBundles.getEntries(ccontext.context(), bundleKey).entrySet()) {
            if (!prefix.matches(entry.getKey())) {
                continue;
            }
            ELResourceBundleKeyCompletionItem item = new ELResourceBundleKeyCompletionItem(entry.getKey(), entry.getValue(), elElement, bundleFile);
            item.setSmart(true);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }
    }

    private void proposeFunctions(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        for (Function function : ELTypeUtilities.getELFunctions(info)) {
            if (!prefix.matches(function.getName())) {
                continue;
            }
            ELFunctionCompletionItem item = new ELFunctionCompletionItem(
                    function.getName(),
                    function.getReturnType(),
                    function.getParameters(),
                    function.getDescription());
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }
    }

    @Override
    public String document(ParserResult info, final ElementHandle element) {
        Documentation doc = documentElement(info, element, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        });
        if (doc != null) {
            return doc.getContent();
        }
        return null;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        ELElement element = getElementAt(info, caretOffset);
        if (element == null) {
            return null;
        }
        Node node = element.findNodeAt(caretOffset);
        // get the prefix for bundle keys
        if (node instanceof AstString) {
            int startOffset = element.getOriginalOffset(node).getStart();
            int end = caretOffset - startOffset;
            String image = node.getImage();
            if (end > 0 && !image.isEmpty()) {
                // index 0 in AstString#image is either ' or ",
                // so start from 1
                return image.substring(1, end);
            }
        }
        // use the default CSL behavior for getting the prefix
        return null;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        assert typedText.length() > 0;
        char last = typedText.charAt(typedText.length() - 1);
        switch(last) {
            case '.':
                return QueryType.COMPLETION;
        }
        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    @Override
    public Documentation documentElement(ParserResult info, ElementHandle element, Callable<Boolean> cancel) {
        if (!(element instanceof ELElementHandle)) {
            return null;
        }
        return ((ELElementHandle) element).document(info, cancel);
    }

    /*package*/ static class PrefixMatcher {

        private final String prefix;
        private final boolean exact;
        private final boolean caseSensitive;

        private PrefixMatcher(String value, boolean exact, boolean caseSensitive) {
            this.prefix = value;
            this.exact = exact;
            this.caseSensitive = caseSensitive;
        }

        static PrefixMatcher create(Node target, CodeCompletionContext context) {
            String prefix = context.getPrefix() != null ? context.getPrefix() : "";
            boolean isDoc = context.getQueryType() == QueryType.DOCUMENTATION;
            if (isDoc) {
                prefix = getPrefixForDocumentation(target);
            }
            // for documentation we need full prefix
            if (isDoc && prefix.isEmpty()) {
                return null;
            }
            return new PrefixMatcher(prefix, isDoc, context.isCaseSensitive());
        }

        static PrefixMatcher create(String prefix, CodeCompletionContext context) {
            return new PrefixMatcher(prefix, false, context.isCaseSensitive());
        }

        private static String getPrefixForDocumentation(Node target) {
            if (target instanceof AstString) {
                return ((AstString) target).getString();
            }
            return target.getImage() == null ? "" : target.getImage();
        }

        boolean matches(String str) {
            if (exact) {
                return prefix.equals(str);
            } else if (caseSensitive) {
                return str.startsWith(prefix);
            } else {
                return str.toLowerCase().startsWith(prefix.toLowerCase());
            }
        }

        int length() {
            return prefix.length();
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
