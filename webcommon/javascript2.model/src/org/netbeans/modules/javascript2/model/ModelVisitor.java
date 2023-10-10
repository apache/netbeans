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
package org.netbeans.modules.javascript2.model;


import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import com.oracle.js.parser.Token;
import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.CatchNode;
import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.ExpressionStatement;
import com.oracle.js.parser.ir.ForNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.IndexNode;
import com.oracle.js.parser.ir.JoinPredecessorExpression;
import com.oracle.js.parser.ir.LabelNode;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.ReturnNode;
import com.oracle.js.parser.ir.TernaryNode;
import com.oracle.js.parser.ir.UnaryNode;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.WithNode;
import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.ClassElement;
import com.oracle.js.parser.ir.ExportClauseNode;
import com.oracle.js.parser.ir.ExportNode;
import com.oracle.js.parser.ir.ExportSpecifierNode;
import com.oracle.js.parser.ir.Expression;
import com.oracle.js.parser.ir.FromNode;
import com.oracle.js.parser.ir.ImportClauseNode;
import com.oracle.js.parser.ir.ImportNode;
import com.oracle.js.parser.ir.ImportSpecifierNode;
import com.oracle.js.parser.ir.NameSpaceImportNode;
import com.oracle.js.parser.ir.NamedImportsNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.doc.spi.JsModifier;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

import static org.netbeans.modules.javascript2.model.ModelElementFactory.create;

import org.netbeans.modules.javascript2.model.api.JsArray;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsWith;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Petr Pisl
 */
public class ModelVisitor extends PathNodeVisitor implements ModelResolver {

    private static final Logger LOGGER = Logger.getLogger(ModelVisitor.class.getName());

    private final ModelBuilder modelBuilder;
    private final OccurrenceBuilder occurrenceBuilder;
    /**
     * Keeps the name of the visited properties
     */
    private final ParserResult parserResult;

    // keeps objects that are created as arguments of a function call
    private final Stack<Collection<JsObjectImpl>> functionArgumentStack = new Stack<>();
    private Map<FunctionInterceptor, Collection<FunctionCall>> functionCalls = null;

    private static final String BLOCK_OBJECT_NAME_PREFIX = "block-"; //NOI18N

    public ModelVisitor(ParserResult parserResult, OccurrenceBuilder occurrenceBuilder) {
        super();
        FileObject fileObject = parserResult.getSnapshot().getSource().getFileObject();
        this.modelBuilder = new ModelBuilder(JsFunctionImpl.createGlobal(
                fileObject, Integer.MAX_VALUE, parserResult.getSnapshot().getMimeType()));
        this.occurrenceBuilder = occurrenceBuilder;
        this.parserResult = parserResult;
    }

    @Override
    public void init() {
        final FunctionNode root = parserResult.getLookup().lookup(FunctionNode.class);
        if (root != null) {
            root.accept(this);
        }
    }

    @Override
    public JsObject getGlobalObject() {
        return modelBuilder.getGlobal();
    }

    @Override
    public void processCalls(
            org.netbeans.modules.javascript2.model.spi.ModelElementFactory elementFactory,
            Map<String, Map<Integer, List<TypeUsage>>> returnTypesFromFrameworks) {
        final Map<FunctionInterceptor, Collection<ModelVisitor.FunctionCall>> calls = getCallsForProcessing();
        if (calls != null && !calls.isEmpty()) {
            for (Map.Entry<FunctionInterceptor, Collection<ModelVisitor.FunctionCall>> entry : calls.entrySet()) {
                Collection<ModelVisitor.FunctionCall> fncCalls = entry.getValue();
                if (fncCalls != null && !fncCalls.isEmpty()) {
                    for (ModelVisitor.FunctionCall call : fncCalls) {
                        Collection<TypeUsage> returnTypes = entry.getKey().intercept(parserResult.getSnapshot(), call.getName(),
                                getGlobalObject(), call.getScope(), elementFactory, call.getArguments());
                        if (returnTypes != null) {
                            Map<Integer, List<TypeUsage>> functionCalls = returnTypesFromFrameworks.get(call.getName());
                            if (functionCalls == null) {
                                functionCalls = new HashMap<>();
                                returnTypesFromFrameworks.put(call.getName(), functionCalls);
                            }
                            List<TypeUsage> types = functionCalls.get(call.getCallOffset());
                            if (types == null) {
                                types = new ArrayList<>();
                                functionCalls.put(call.getCallOffset(), types);
                            }
                            for (TypeUsage type: returnTypes) {
                                if (!types.contains(type)) {
                                    types.add(type);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Identifier> getASTNodeName(final Object astNode) {
        if (astNode instanceof Node) {
            return getNodeName((Node)astNode, parserResult);
        }
        return Collections.emptyList();
    }


    @Override
    public boolean enterAccessNode(AccessNode accessNode) {
        BinaryNode node = getPath().get(getPath().size() - 1) instanceof BinaryNode
                ? (BinaryNode)getPath().get(getPath().size() - 1) : null;
        if (!(node != null && node.tokenType() == TokenType.ASSIGN)) {
            if (accessNode.getBase() instanceof IdentNode && ModelUtils.THIS.equals(((IdentNode)accessNode.getBase()).getName())) { //NOI18N
                String iNode = accessNode.getProperty();
                JsObject current = modelBuilder.getCurrentDeclarationFunction();
                JsObject property = current.getProperty(iNode);
                if (property == null && current.getParent() != null && (current.getParent().getJSKind() == JsElement.Kind.CONSTRUCTOR
                        || current.getParent().getJSKind() == JsElement.Kind.OBJECT)) {
                    current = current.getParent();
                    property = current.getProperty(iNode);
                    if (property == null && ModelUtils.PROTOTYPE.equals(current.getName())) {
                        current = current.getParent();
                        property = current.getProperty(iNode);
                    }
                }
                if (property == null && current.getParent() == null) {
                    // probably we are in global space and there is used this
                    property = modelBuilder.getGlobal().getProperty(iNode);
                }
                if (property != null && !property.getModifiers().contains(Modifier.PRIVATE)) {
                    // we don't want to add occurrences for cases like var buf = this.buf. See issue #267694
                    ((JsObjectImpl)property).addOccurrence(new OffsetRange(accessNode.getFinish() - iNode.length(), accessNode.getFinish()));
                }
            }
        }
        return super.enterAccessNode(accessNode);
    }

    @Override
    public Node leaveAccessNode(AccessNode accessNode) {
        createJsObject(accessNode, parserResult, modelBuilder);
        return super.leaveAccessNode(accessNode);
    }

    @Override
    public boolean enterBlock(Block block) {
        DeclarationScopeImpl blockScope = (DeclarationScopeImpl)modelBuilder.getCurrentDeclarationScope().getProperty(BLOCK_OBJECT_NAME_PREFIX + block.getStart());
        if ( blockScope!= null) {
            // in this block there are a declarations that we are interested in.
            modelBuilder.setCurrentObject(blockScope);
        }
        return super.enterBlock(block);
    }

    @Override
    public Node leaveBlock(Block block) {
        DeclarationScopeImpl currentScope = modelBuilder.getCurrentDeclarationScope();
        if (currentScope.getJSKind() == JsElement.Kind.BLOCK && currentScope.getName().equals(BLOCK_OBJECT_NAME_PREFIX + block.getStart())) {
            // removing the block as declaration scope from
            modelBuilder.reset();
        }
        return super.leaveBlock(block);
    }

    @Override
    public boolean enterBinaryNode(BinaryNode binaryNode) {
        Node lhs = binaryNode.lhs();
        Node rhs = binaryNode.rhs();
        if (lhs instanceof LiteralNode.ArrayLiteralNode && binaryNode.tokenType() == TokenType.ASSIGN) {
            // case of destructuring assgnment like [a,b] = ....
            LiteralNode.ArrayLiteralNode lan = (LiteralNode.ArrayLiteralNode)lhs;
            if (rhs instanceof LiteralNode.ArrayLiteralNode) {
                // case [a, b] = [1, 2]
                LiteralNode.ArrayLiteralNode ran = (LiteralNode.ArrayLiteralNode)rhs;
                List<Expression> lExpressions = lan.getElementExpressions();
                List<Expression> rExpressions = ran.getElementExpressions();
                for (int i = 0; i < lExpressions.size(); i++) {
                    Expression lExpression = lExpressions.get(i);
                    if (i < rExpressions.size()) {
                        Expression rExpression = rExpressions.get(i);
                        processBinaryNode(lExpression, rExpression, TokenType.ASSIGN);
                    } else {
                        break;
                    }
                }
            } else {
                // other cases
                rhs.accept(this);
            }
        } else if (lhs instanceof ObjectNode && binaryNode.tokenType() == TokenType.ASSIGN) {
            // cases {a, b} = ...
            ObjectNode lObjectNode = (ObjectNode)lhs;
            JsObjectImpl rObject = null;

            // prepare variables that are available in the current scope for later usage
            DeclarationScopeImpl scope = modelBuilder.getCurrentDeclarationScope();
            Collection<? extends JsObject> variables = ModelUtils.getVariables(scope);

            if (rhs instanceof ObjectNode) {
                // case {a, b} = {a:1, b:2}
                // the rhs object we have to put to the model as anonymous object. At least will be colored in the right way
                ObjectNode rObjectNode = (ObjectNode)rhs;
                rObject = ModelElementFactory.createAnonymousObject(parserResult, rObjectNode,  modelBuilder);
                modelBuilder.setCurrentObject(rObject);
                rObject.setJsKind(JsElement.Kind.OBJECT_LITERAL);
                if (!functionArgumentStack.isEmpty()) {
                    functionArgumentStack.peek().add(rObject);
                }
                for (PropertyNode rPropertyNode : rObjectNode.getElements()) {
                    rPropertyNode.accept(this);
                }
                modelBuilder.reset();
            } else {
                rhs.accept(this);
                if (rhs instanceof IdentNode) {
                    // we will try to find the right object literal
                    rObject = (JsObjectImpl)ModelUtils.getScopeVariable(scope, ((IdentNode)rhs).getName());
                }
            }
            if (rObject != null) {
                // find variables that are mentioned on the left site and assign the types from
                // property with the same name from the right site
                for (PropertyNode lPropertyNode : lObjectNode.getElements()) {
                    String variableName = null;
                    if (isKeyAndValueEquals(lPropertyNode)) {
                        // case {p:var1, q:var2} = {p:1, q:2} or {p:var1, q:var2} = objectLiteral
                        variableName = lPropertyNode.getKeyName();
                        lPropertyNode.accept(this);
                    } else if (lPropertyNode.getValue() instanceof IdentNode) {
                        variableName = ((IdentNode)lPropertyNode.getValue()).getName();
                    } else if (lPropertyNode.getValue() instanceof BinaryNode) {
                        BinaryNode bNode = (BinaryNode)lPropertyNode.getValue();
                        if (bNode.tokenType() == TokenType.ASSIGN && bNode.lhs() instanceof IdentNode) {
                            // the default parameter {a=10, b=20} = ....
                            variableName = ((IdentNode)bNode.lhs()).getName();
                        }
                    }
                    if (variableName != null) {
                        JsObject variable = ModelUtils.getScopeVariable(scope, variableName);
                        JsObject rProperty = rObject.getProperty(lPropertyNode.getKeyName());
                        if (variable != null && rProperty != null) {
                            // copy types from the properties
                            for (TypeUsage assignment : rProperty.getAssignments()){
                                variable.addAssignment(assignment, rProperty.getOffset());
                            }
                            if (!isKeyAndValueEquals(lPropertyNode)) {
                                // mark occurrences in case {p:var1, q:var2} = {p:1, q:2} or {p:var1, q:var2} = objectLiteral
                                rProperty.addOccurrence(new OffsetRange(lPropertyNode.getStart(), lPropertyNode.getStart() + lPropertyNode.getKeyName().length()));
                            }
                        }
                    }
                }
                return false;
            }
        }else {
            processBinaryNode(lhs, rhs, binaryNode.tokenType());
        }
        return super.enterBinaryNode(binaryNode);
    }

    private boolean isKeyAndValueEquals(PropertyNode pNode) {
        if (pNode.getKey() instanceof IdentNode && pNode.getValue() instanceof IdentNode) {
            IdentNode key = (IdentNode)pNode.getKey();
            IdentNode value = (IdentNode)pNode.getValue();
            return key.getName().equals(value.getName()) && key.getStart() == value.getStart();
        }
        if (pNode.getKey() instanceof IdentNode && pNode.getValue() instanceof BinaryNode
                && ((BinaryNode)pNode.getValue()).tokenType() == TokenType.ASSIGN && ((BinaryNode)pNode.getValue()).lhs() instanceof IdentNode) {
            IdentNode key = (IdentNode)pNode.getKey();
            IdentNode value = (IdentNode)((BinaryNode)pNode.getValue()).lhs();
            return key.getName().equals(value.getName()) && key.getStart() == value.getStart();
        }
        return false;
    }

    private void processBinaryNode(Node lhs, Node rhs, TokenType tokenType) {
        if (tokenType == TokenType.ASSIGN
                && !(/*rhs instanceof ReferenceNode ||*/ rhs instanceof ObjectNode)
                && (lhs instanceof AccessNode || lhs instanceof IdentNode || lhs instanceof IndexNode)) {
            // TODO probably not only assign
            JsObjectImpl parent = modelBuilder.getCurrentDeclarationFunction();
            if (parent == null) {
                // should not happened
                return;
            }
            String fieldName = null;
            if (lhs instanceof AccessNode) {
                AccessNode aNode = (AccessNode)lhs;
                JsObjectImpl property = null;
                List<Identifier> fqName = getName(aNode);
                if (fqName != null && ModelUtils.THIS.equals(fqName.get(0).getName())) { //NOI18N
                    // a usage of field
                    fieldName = aNode.getProperty();
                    if (rhs instanceof IdentNode) {
                        // resolve occurrence of the indent node sooner, then is created the field.
                        addOccurrence((IdentNode)rhs, fieldName);
                    }
                    property = (JsObjectImpl)createJsObject(aNode, parserResult, modelBuilder);
                } else {
                    // probably a property of an object
                    if (fqName != null) {
                        property = ModelUtils.getJsObject(modelBuilder, fqName, true);
                        if (property.getParent().getJSKind().isFunction() && !property.getModifiers().contains(Modifier.STATIC)) {
                            property.getModifiers().add(Modifier.STATIC);
                        }
                    }
                }
                if (property != null) {
                    String parameter = null;
                    JsFunction function = (JsFunction)modelBuilder.getCurrentDeclarationFunction();
                    if(rhs instanceof IdentNode) {
                        IdentNode iNode = (IdentNode)rhs;
                        if(/*function.getProperty(rhs.getName()) == null &&*/ function.getParameter(iNode.getName()) != null) {
                            parameter = "@param;" + function.getFullyQualifiedName() + ":" + iNode.getName(); //NOI18N
                        }
                    }
                    Collection<TypeUsage> types;
                    if (parameter == null) {
                        types =  ModelUtils.resolveSemiTypeOfExpression(modelBuilder, rhs);
                        Collection<TypeUsage> correctedTypes = new ArrayList<>(types.size());
                        for (TypeUsage type : types) {
                            String typeName = type.getType();
                            // we have to check, whether a variable comming from resolvedr is not a parameter of function where the binary node is
                            if (typeName.startsWith(SemiTypeResolverVisitor.ST_VAR)) {
                                String varName = typeName.substring(SemiTypeResolverVisitor.ST_VAR.length());
                                if (function.getParameter(varName) != null) {
                                    correctedTypes.add(new TypeUsage("@param;" + function.getFullyQualifiedName() + ":" + varName, type.getOffset(), false));
                                } else {
                                    correctedTypes.add(type);
                                }
                            } else {
                                correctedTypes.add(type);
                            }
                        }
                        types = correctedTypes;
                    } else {
                        types = new ArrayList<>();
                        types.add(new TypeUsage(parameter, rhs.getStart(), false));
                    }

                    if (property.getDocumentation() == null) {
                        JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
                        if (docHolder != null) {
                            property.setDocumentation(docHolder.getDocumentation(lhs));
                            property.setDeprecated(docHolder.isDeprecated(lhs));
                            List<Type> returnTypes = docHolder.getReturnType(lhs);
                            if (!returnTypes.isEmpty()) {
                                for (Type type : returnTypes) {
                                    property.addAssignment(new TypeUsage(type.getType(), type.getOffset(), true), lhs.getFinish());
                                }
                            }
                        }
                    }

                    for (TypeUsage type : types) {
                        // plus 5 due to the this.
                        property.addAssignment(type, lhs.getStart() + 5);
                    }
                }

            } else {
                JsObject lObject = null;
                boolean indexNodeReferProperty = false;
                int assignmentOffset = lhs.getFinish();
                if (lhs instanceof IndexNode) {
                    IndexNode iNode = (IndexNode)lhs;
                    if (iNode.getBase() instanceof IdentNode) {
                        lObject = processLhs(ModelElementFactory.create(parserResult, (IdentNode)iNode.getBase()), parent, false);
                        assignmentOffset = iNode.getFinish();
                    }
                    if (lObject != null && iNode.getIndex() instanceof LiteralNode) {
                        LiteralNode lNode = (LiteralNode)iNode.getIndex();
                        if (lNode.isString()) {
                            Identifier newPropName = ModelElementFactory.create(parserResult, lNode);
                            if (newPropName != null) {
                                indexNodeReferProperty = true;
                                if (lObject.getProperty(lNode.getString()) == null) {
                                    JsObject newProperty = new JsObjectImpl(lObject, newPropName, newPropName.getOffsetRange(), true, parserResult.getSnapshot().getMimeType(), null);
                                    lObject.addProperty(newPropName.getName(), newProperty);
                                    assignmentOffset = lNode.getFinish();
                                }
                                lObject = processLhs(newPropName, lObject, true);
                            }
                        }
                    }
                } else if (lhs instanceof IdentNode) {
                    lObject = processLhs(ModelElementFactory.create(parserResult, (IdentNode)lhs), parent, true);
                }

                if (lObject != null && !(rhs instanceof FunctionNode)) {
                    Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, rhs);
                    if (lhs instanceof IndexNode && lObject instanceof JsArrayImpl) {
                        ((JsArrayImpl)lObject).addTypesInArray(types);
                    } else {
                        boolean isIndexNode = lhs instanceof IndexNode;
                        if (!isIndexNode || (isIndexNode && indexNodeReferProperty)) {
                            for (TypeUsage type : types) {
                                lObject.addAssignment(type, assignmentOffset);
                            }
                        }
                    }
                }
            }
            if (fieldName == null && rhs instanceof IdentNode) {
                addOccurence((IdentNode)rhs, false);
            }
        } else if(tokenType != TokenType.ASSIGN
                || (tokenType == TokenType.ASSIGN && lhs instanceof IndexNode)) {
            if (lhs instanceof IdentNode) {
                addOccurence((IdentNode)lhs, tokenType == TokenType.ASSIGN);
            }
            if (rhs instanceof IdentNode) {
                addOccurence((IdentNode)rhs, false);
            }
        }
    }

    @Override
    public Node leaveBinaryNode(BinaryNode binaryNode) {
        Node lhs = binaryNode.lhs();
        Node rhs = binaryNode.rhs();
        if (lhs instanceof IdentNode && rhs instanceof BinaryNode) {
            Node rlhs = ((BinaryNode)rhs).lhs();
            if (rlhs instanceof IdentNode) {
                JsObject origFunction = modelBuilder.getCurrentDeclarationFunction().getProperty(((IdentNode)rlhs).getName());
                if (origFunction != null && origFunction.getJSKind().isFunction()) {
                    JsObject refFunction = modelBuilder.getCurrentDeclarationFunction().getProperty(((IdentNode)lhs).getName());
                    if (refFunction != null && !refFunction.getJSKind().isFunction()) {
                        JsFunctionReference newReference = new JsFunctionReference(refFunction.getParent(), refFunction.getDeclarationName(), (JsFunction)origFunction, true, origFunction.getModifiers());
                        refFunction.getParent().addProperty(newReference.getName(), newReference);
                    }
                }
            }
        }
        return super.leaveBinaryNode(binaryNode);
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        functionArgumentStack.push(new ArrayList<>(3));
        if (callNode.getFunction() instanceof IdentNode) {
            IdentNode iNode = (IdentNode)callNode.getFunction();
            addOccurence(iNode, false, true);
        }
        for (Node argument : callNode.getArgs()) {
            if (argument instanceof IdentNode) {
                addOccurence((IdentNode) argument, false);
            }
        }
        processObjectPropertyAssignment(callNode);
        return super.enterCallNode(callNode);
    }

    @Override
    public Node leaveCallNode(CallNode callNode) {
        Collection<JsObjectImpl> functionArguments = functionArgumentStack.pop();

        Node function = callNode.getFunction();
        if (function instanceof AccessNode || function instanceof IdentNode) {
            List<Identifier> funcName;
            if (function instanceof AccessNode) {
                funcName = getName((AccessNode) function);
            } else {
                funcName = new ArrayList<>();
                funcName.add(new Identifier(((IdentNode) function).getName(), ((IdentNode) function).getStart()));
            }
                if (funcName != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Identifier identifier : funcName) {
                        sb.append(identifier.getName());
                        sb.append(".");
                    }
                    if (functionCalls == null) {
                        functionCalls = new LinkedHashMap<>();
                    }

                    String name = sb.substring(0, sb.length() - 1);
                    List<FunctionInterceptor> interceptorsToUse = new ArrayList<>();
                    for (FunctionInterceptor interceptor : ModelExtender.getDefault().getFunctionInterceptors()) {
                        if (interceptor.getNamePattern().matcher(name).matches()) {
                            interceptorsToUse.add(interceptor);
                        }
                    }


                    for (FunctionInterceptor interceptor : interceptorsToUse) {
                        Collection<FunctionArgument> funcArg = new ArrayList<>();
                        for (int i = 0; i < callNode.getArgs().size(); i++) {
                            Node argument = callNode.getArgs().get(i);
                            createFunctionArgument(argument, i, functionArguments, funcArg);
                        }
                        Collection<FunctionCall> calls = functionCalls.get(interceptor);
                        if (calls == null) {
                            calls = new ArrayList<>();
                            functionCalls.put(interceptor, calls);
                        }
                        int callOffset = callNode.getFunction().getStart();
                        if (callNode.getFunction() instanceof AccessNode) {
                            AccessNode anode = (AccessNode)callNode.getFunction();
                            callOffset = anode.getFinish() - anode.getProperty().length();
                        }
                        calls.add(new FunctionCall(name, modelBuilder.getCurrentDeclarationScope(), funcArg, callOffset));
                    }
                }
            }
        return super.leaveCallNode(callNode);
    }

    private void createFunctionArgument(Node argument, int position, Collection<JsObjectImpl> functionArguments,
            Collection<FunctionArgument> result) {

        if (argument instanceof LiteralNode) {
            LiteralNode ln = (LiteralNode)argument;
            if (ln.isString()) {
                result.add(FunctionArgumentAccessor.getDefault().createForString(
                        position, argument.getStart(), ln.getString()));
            } else if (ln instanceof LiteralNode.ArrayLiteralNode) {
                for (JsObjectImpl jsObject: functionArguments) {
                    if (jsObject.getOffset() == argument.getStart()) {
                        result.add(FunctionArgumentAccessor.getDefault().createForArray(position, jsObject.getOffset(), jsObject));
                        break;
                    }
                }
            }
        } else if (argument instanceof ObjectNode) {
            for (JsObjectImpl jsObject: functionArguments) {
                if (jsObject.getOffset() == argument.getStart()) {
                    result.add(FunctionArgumentAccessor.getDefault().createForAnonymousObject(position, jsObject.getOffset(), jsObject));
                    break;
                }
            }
        } else if (argument instanceof AccessNode) {
            List<String> strFqn = new ArrayList<>();
            if(fillName((AccessNode) argument, strFqn)) {
                result.add(FunctionArgumentAccessor.getDefault().createForReference(
                        position, argument.getStart(), strFqn));
            } else {
                result.add(FunctionArgumentAccessor.getDefault().createForUnknown(position));
            }
        } else if (argument instanceof IndexNode) {
            List<String> strFqn = new ArrayList<>();
            if(fillName((IndexNode) argument, strFqn)) {
                result.add(FunctionArgumentAccessor.getDefault().createForReference(
                        position, argument.getStart(), strFqn));
            } else {
                result.add(FunctionArgumentAccessor.getDefault().createForUnknown(position));
            }
        } else if (argument instanceof IdentNode) {
            IdentNode in = (IdentNode) argument;
            String inName = in.getName();
            result.add(FunctionArgumentAccessor.getDefault().createForReference(
                    position, argument.getStart(),
                    Collections.singletonList(inName)));
        } else if (argument instanceof UnaryNode) {
            // we are handling foo(new Something())
            UnaryNode un = (UnaryNode) argument;
            if (un.tokenType() == TokenType.NEW) {
                CallNode constructor = (CallNode) un.getExpression();
                createFunctionArgument(constructor.getFunction(), position, functionArguments, result);
            }
        } else if (argument instanceof FunctionNode) {
            FunctionNode reference = (FunctionNode) argument;
            result.add(FunctionArgumentAccessor.getDefault().createForReference(
                    position, argument.getStart(),
                    Collections.singletonList(modelBuilder.getFunctionName(reference))));
        } else {
            result.add(FunctionArgumentAccessor.getDefault().createForUnknown(position));
        }
    }

    @Override
    public boolean enterCatchNode(CatchNode catchNode) {
        Identifier exception = catchNode.getException() == null ? null : ModelElementFactory.create(parserResult, catchNode.getException());
        DeclarationScopeImpl inScope = modelBuilder.getCurrentDeclarationScope();
        CatchBlockImpl catchBlock  = new CatchBlockImpl(inScope, exception,
                new OffsetRange(catchNode.getStart(), catchNode.getFinish()), parserResult.getSnapshot().getMimeType());
        inScope.addDeclaredScope(catchBlock);
        modelBuilder.setCurrentObject(catchBlock);
        return super.enterCatchNode(catchNode);
    }

    @Override
    public Node leaveCatchNode(CatchNode catchNode) {
        if (catchNode.getException() == null || !EmbeddingHelper.containsGeneratedIdentifier(catchNode.getException().getName())) {
            modelBuilder.reset();
        }
        return super.leaveCatchNode(catchNode);
    }



    @Override
    public boolean enterClassNode(ClassNode node) {
        IdentNode cnIdent = node.getIdent();
        Node lastNode = getPreviousFromPath(1);
        VarNode varNode = (lastNode instanceof VarNode) ? (VarNode)lastNode : null;
        JsObject parent = modelBuilder.getCurrentObject();
        JsObjectImpl classObject = null;
        Identifier className = null;
        Identifier refName = null;
        if ((varNode != null  && cnIdent != null && varNode.getName().getName().equals(cnIdent.getName()))
            // case1: var Polygon = class Polygon {}
            // case2: class Polygon {}
                || (varNode != null && !varNode.isExport() && cnIdent == null) ) {
            // case 3: var Polygon = class{}
            // we create just one object
            className = ModelElementFactory.create(parserResult, varNode.getName());
        } else if (varNode != null && cnIdent != null && !varNode.getName().getName().equals(cnIdent.getName())) {
            // case 4: var Polygon = class PolygonOther{}
            // The PolygonOther is available just for the inside the class.
            className = ModelElementFactory.create(parserResult, varNode.getName());
            refName = ModelElementFactory.create(parserResult, cnIdent);
        } else if (varNode == null && cnIdent != null) {
            className = ModelElementFactory.create(parserResult, cnIdent);
        }

        if (className != null) {
            // At least for exported classes multiple JsObjectImpls are created
            // and that latest created one is missing the properties. To fix
            // this, an existing object is checked and if it exists its
            // properties are moved to the latest instance, creating a superset
            // of all properties.
            classObject = new JsObjectImpl(
                    parent,
                    className,
                    new OffsetRange(node.getStart(), node.getFinish()),
                    true,
                    parent.getMimeType(),
                    parent.getSourceLabel()
            );
            JsObject origClassObject = parent.getProperty(className.getName());
            if (origClassObject != null) {
                List<JsObject> properties = new ArrayList<>(origClassObject.getProperties().values());
                for (JsObject property : properties) {
                    ModelUtils.moveProperty(classObject, property);
                }
            }
            parent.addProperty(className.getName(), classObject);
            classObject.setJsKind(JsElement.Kind.CLASS);
            if (refName != null) {
                JsObjectReference reference = new JsObjectReference(classObject, refName, classObject, true, EnumSet.of(Modifier.PRIVATE));
                classObject.addProperty(refName.getName(), reference);
                reference.addOccurrence(refName.getOffsetRange());
            }
        }
        if (classObject != null) {
            if (node.getClassHeritage() != null) {
                Expression classHeritage = node.getClassHeritage();
                if (classHeritage instanceof IdentNode) {
                    JsObjectImpl proto = new JsObjectImpl(classObject, ModelUtils.PROTOTYPE, true, OffsetRange.NONE, EnumSet.of(Modifier.PUBLIC), classObject.getMimeType(), classObject.getSourceLabel());
                    classObject.addProperty(ModelUtils.PROTOTYPE, proto);
                    IdentNode type = (IdentNode)classHeritage;
                    proto.addAssignment(new TypeUsage(type.getName(), type.getStart(), true), type.getStart());
                }

            }
            modelBuilder.setCurrentObject(classObject);
            // visit constructor
            node.getConstructor().accept(this);
            // visit rest of declaration
            for (PropertyNode element : node.getClassElements()) {
                element.accept(this);
            }
            modelBuilder.reset();
        }
        return false;
    }



    @Override
    public boolean enterIdentNode(IdentNode identNode) {
        Node previousVisited = getPath().get(getPath().size() - 1);
        if(!(previousVisited instanceof AccessNode
                || previousVisited instanceof VarNode
                || previousVisited instanceof BinaryNode
                || previousVisited instanceof PropertyNode
                || previousVisited instanceof CatchNode
                || previousVisited instanceof LabelNode)) {
            //boolean declared = previousVisited instanceof CatchNode;
            addOccurence(identNode, false);
        }
        return super.enterIdentNode(identNode);
    }

    @Override
    public Node leaveIndexNode(IndexNode indexNode) {
        if (indexNode.getIndex() instanceof LiteralNode) {
            Node base = indexNode.getBase();
            JsObjectImpl parent = null;
            if (base instanceof AccessNode) {
               parent = (JsObjectImpl)createJsObject((AccessNode)base, parserResult, modelBuilder);
            } else if (base instanceof IdentNode) {
                IdentNode iNode = (IdentNode)base;
                if (!ModelUtils.THIS.equals(iNode.getName())) {
                    Identifier parentName = ModelElementFactory.create(parserResult, iNode);
                    if (parentName != null) {
                        List<Identifier> fqName = new ArrayList<>();
                        fqName.add(parentName);
                        parent = ModelUtils.getJsObject(modelBuilder, fqName, false);
                        parent.addOccurrence(parentName.getOffsetRange());
                    }
                }/* else {
                    JsObject current = modelBuilder.getCurrentDeclarationFunction();
                    fromAN = (JsObjectImpl)resolveThis(current);
                }*/
            }
            if (parent != null && indexNode.getIndex() instanceof LiteralNode) {
                LiteralNode literal = (LiteralNode)indexNode.getIndex();
                if (literal.isString()) {
                    String index = literal.getPropertyName();
                    JsObjectImpl property = (JsObjectImpl)parent.getProperty(index);
                    if (property != null) {
                        property.addOccurrence(new OffsetRange(indexNode.getIndex().getStart(), indexNode.getIndex().getFinish()));
                    } else {
                        Identifier name = ModelElementFactory.create(parserResult, (LiteralNode)indexNode.getIndex());
                        if (name != null) {
                            property = new JsObjectImpl(parent, name, name.getOffsetRange(), parserResult.getSnapshot().getMimeType(), null);
                            parent.addProperty(name.getName(), property);
                        }
                    }
                }
            }
        }
        return super.leaveIndexNode(indexNode);
    }

    @Override
    public boolean enterImportNode(ImportNode iNode) {
        ImportClauseNode iClause = iNode.getImportClause();
        FromNode from = iNode.getFrom();
        if (iClause != null) {
            IdentNode defaultBinding = iClause.getDefaultBinding();
            NameSpaceImportNode nameSpaceImport = iClause.getNameSpaceImport();
            NamedImportsNode namedImports = iClause.getNamedImports();
            if (defaultBinding != null) {
                Identifier importedAs = create(parserResult, defaultBinding);
                // create a variable, which have assignment the same variable name  from FromNode
                JsObjectImpl property = createVariableFromImport(importedAs);
                property.addAssignment(new TypeUsage(importedAs.getName()), importedAs.getOffsetRange().getEnd());
            }
            if (nameSpaceImport != null) {
                Identifier importedAs = create(parserResult, nameSpaceImport.getBindingIdentifier());
                // create a variable, which has all properties from FromNode module
                createVariableFromImport(importedAs);
            }
            if (namedImports != null && namedImports.getImportSpecifiers() != null) {
                List<ImportSpecifierNode> importSpecifiers = namedImports.getImportSpecifiers();
                for (ImportSpecifierNode importSpecifier : importSpecifiers) {
                    Identifier importedAs = create(parserResult, importSpecifier.getBindingIdentifier());
                    JsObjectImpl property;
                    property = createVariableFromImport(importedAs);
                    property.addOccurrence(importedAs.getOffsetRange());
                    if (importSpecifier.getIdentifier() != null) {
                        Identifier inModuleName = create(parserResult, importSpecifier.getIdentifier());
                        property.addAssignment(new TypeUsage(inModuleName.getName()), inModuleName.getOffsetRange().getEnd());
                    }

                }
            }
        }
        return false;
    }

    private JsObjectImpl createVariableFromImport(Identifier name) {
        JsFunctionImpl scope = modelBuilder.getCurrentDeclarationFunction();
        JsObject existingProp = scope.getProperty(name.getName());
        JsObjectImpl property = new JsObjectImpl(scope, name, name.getOffsetRange(), true, scope.getMimeType(), scope.getSourceLabel());
        if (existingProp != null) {
            ModelUtils.copyOccurrences(existingProp, property);
        }
        scope.addProperty(name.getName(), property);
        return property;
    }

    @Override
    public boolean enterExportNode(ExportNode exportNode) {
        boolean result = super.enterExportNode(exportNode);
        final ExportClauseNode exportClause = exportNode.getExportClause();
        final FromNode from = exportNode.getFrom();
        final Expression expression = exportNode.getExpression();

        if (exportClause != null) {
            for (ExportSpecifierNode esNode :exportClause.getExportSpecifiers()) {
                IdentNode exported = esNode.getExportIdentifier();
                IdentNode local = esNode.getIdentifier();
                JsObjectImpl property = (JsObjectImpl)modelBuilder.getCurrentDeclarationFunction().getProperty(local.getName());
                if (exported == null) {
                    if (property == null) {
                        property = createVariableFromImport(create(parserResult, local));
                    }
                    property.addOccurrence(getOffsetRange(local));
                } else {
                    addOccurence (local, false);
                    property = createVariableFromImport(create(parserResult, exported));
                }
                if (from != null && property != null) {
                    TypeUsage type = new TypeUsage(local.getName(), local.getFinish());
                    property.addAssignment(type, local.getFinish());
                }
            }
        }
        if (expression != null) {
            addToPath(exportNode);
            expression.accept(this);
            removeFromPathTheLast();
        }
        return result;
    }

    @Override
    public boolean enterForNode(ForNode forNode) {
        if (forNode.getInit() instanceof IdentNode) {
            JsObject parent = modelBuilder.getCurrentObject();
            while (parent instanceof JsWith) {
                parent = parent.getParent();
            }
            IdentNode name = (IdentNode)forNode.getInit();
            JsObjectImpl variable = (JsObjectImpl)parent.getProperty(name.getName());
            if (variable != null) {
                Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, forNode.getModify());
                for (TypeUsage type : types) {
                    if (type.getType().contains(SemiTypeResolverVisitor.ST_VAR)) {
                        int index = type.getType().lastIndexOf(SemiTypeResolverVisitor.ST_VAR);
                        String newType = type.getType().substring(0, index) + SemiTypeResolverVisitor.ST_ARR + type.getType().substring(index + SemiTypeResolverVisitor.ST_VAR.length());
                        type = new TypeUsage(newType, type.getOffset(), false);
                    } else if (type.getType().contains(SemiTypeResolverVisitor.ST_PRO)) {
                        int index = type.getType().lastIndexOf(SemiTypeResolverVisitor.ST_PRO);
                        String newType = type.getType().substring(0, index) + SemiTypeResolverVisitor.ST_ARR + type.getType().substring(index + SemiTypeResolverVisitor.ST_PRO.length());
                        type = new TypeUsage(newType, type.getOffset(), false);
                    }
                    variable.addAssignment(type, forNode.getModify().getStart());
                }
            }
        }
        return super.enterForNode(forNode);
    }

    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        if (isArtificialConstructor(functionNode)) {
            // don't process artificail constructors.
            return false;
        }
        addToPath(functionNode);
        // Find the function in the model. It's has to be already there
        JsFunctionImpl fncParent = modelBuilder.getCurrentDeclarationFunction();
        JsFunctionImpl fncScope = null;
        if (functionNode.isProgram()) {
            fncScope = fncParent;
            if (this.parserResult.getSnapshot().getSource().getFileObject() != null) {
                LOGGER.log(Level.FINE, "Creating model for: {0}", this.parserResult.getSnapshot().getSource().getFileObject().getPath()); //NOI18N
            }
        } else {
            JsObject property = fncParent.getProperty(modelBuilder.getFunctionName(functionNode));
            if (property == null && functionNode.isStrict()) {
                property = modelBuilder.getCurrentDeclarationScope().getProperty(modelBuilder.getFunctionName(functionNode));
            }
            if(!(property instanceof JsFunction)) {
                property = fncParent.getProperty(modelBuilder.getGlobal().getName() + modelBuilder.getFunctionName(functionNode));
            }
            if (property instanceof JsFunction) {
                if (property instanceof JsFunctionReference) {
                    fncScope = (JsFunctionImpl)((JsFunctionReference)property).getOriginal();
                } else {
                    fncScope = (JsFunctionImpl)property;
                }
            }
            if (property == null) {
                LOGGER.log(Level.FINE, "FunctionNode: {0} is not processed, because parent function {1} doesn''t contain such property.", new Object[]{functionNode.toString(), fncParent.toString()}); //NOI18N
                return false;
            }
        }

        // add to the model functions and variables declared in this scope
        // this is needed, to handle usege before declaration
        processDeclarations(fncScope, functionNode);
        fncScope.setStrict(functionNode.isStrict());

        if (!functionNode.isProgram() && !functionNode.isModule()) {
            correctNameAndOffsets(fncScope, functionNode);
            setParent(fncScope, functionNode);
            // set modifiers for the processed function
            setModifiers(fncScope, functionNode);
            modelBuilder.setCurrentObject(fncScope);
        }

        processJsDoc(fncScope, functionNode, JsDocumentationSupport.getDocumentationHolder(parserResult));

        if (functionNode.isModule()) {
            // visit all imports and exports
            List<ImportNode> imports = functionNode.getModule().getImports();
            for (ImportNode moduleImport : imports) {
                moduleImport.accept(this);
            }
            List<ExportNode> exports = functionNode.getModule().getExports();
            for (ExportNode moduleExport : exports) {
                moduleExport.accept(this);
            }
        }

        // visit all statements of the function
        functionNode.getBody().accept(this);

        if (functionNode.getKind() == FunctionNode.Kind.GENERATOR) {
            // set the return type as Generator object
            fncScope.addReturnType(new TypeUsage("Generator", 1, true));
        } else {
            // seting undefinded return type
            if (fncScope.areReturnTypesEmpty()) {
                // the function doesn't have return statement -> returns undefined
                fncScope.addReturnType(new TypeUsage(Type.UNDEFINED, -1, false));
            }
        }

        if (!functionNode.isProgram() && !functionNode.isModule()) {
            processModifiersFromJsDoc(fncScope, functionNode, JsDocumentationSupport.getDocumentationHolder(parserResult));
            if (canBeSingletonPattern(1)) {
                // move all properties to the parent
                JsObject singleton = resolveThisInSingletonPattern(fncScope);
                if (singleton != null) {
                    fncScope.setJsKind(JsElement.Kind.CONSTRUCTOR);
                    // The second guard is necessary to prevent cyclic structures
                    // for constructs like this:
                    //
                    // var Base = new function() {
                    //    function Base() {
                    //    }
                    // };
                    if (fncScope.isAnonymous() && !fncScope.getProperties().containsValue(singleton)) {
                        // TODO we probably should not move the properties, or at least increase offset range
                        // of the singleton to fit offsets of these methods in the singleton object
                        List<JsObject> properties = new ArrayList<>(fncScope.getProperties().values());
                        for (JsObject property : properties) {
                            ModelUtils.moveProperty(singleton, property);
                        }
                    }
                }
            }
            modelBuilder.reset();
        }
        removeFromPathTheLast();
        return false;
    }

    /**
     * The parse tree for classes holds constructors for all classes. For the
     * structure scanner it is relevent to detect whether or not this
     * constructor is generated or defined
     *
     * @param functionNode to check
     * @return true if functionNode is detected to be generated
     */
    private static boolean isArtificialConstructor(FunctionNode functionNode) {
        return functionNode.isClassConstructor() && functionNode.isGenerated();
    }

    private void correctNameAndOffsets(JsFunctionImpl jsFunction, FunctionNode fn) {
        OffsetRange decNameOffset = jsFunction.getDeclarationName().getOffsetRange();
        Node lastVisited = getPreviousFromPath(2);
        Identifier newIdentifier = null;
        if (decNameOffset.getLength() == 0) {
            // the function name is not between function and (
            if (lastVisited instanceof PropertyNode && fn.getKind() != FunctionNode.Kind.ARROW && fn.getKind() != FunctionNode.Kind.CLASS_FIELD_INITIALIZER) {
                PropertyNode pNode = (PropertyNode)lastVisited;
                newIdentifier = new Identifier(pNode.getKeyName(), getOffsetRange(pNode.getKey()));
            } else if ((lastVisited instanceof VarNode) && fn.isAnonymous()) {
                VarNode vNode = (VarNode)lastVisited;
                newIdentifier = new Identifier(vNode.getName().getName(), getOffsetRange(vNode.getName()));
            } else if (fn.isAnonymous() && lastVisited instanceof JoinPredecessorExpression
                    && getPreviousFromPath(3) instanceof BinaryNode
                    && getPreviousFromPath(4) instanceof VarNode) {
                // case var f1 = xxx || function () {}
                VarNode vNode = (VarNode)getPreviousFromPath(4);
                newIdentifier = new Identifier(vNode.getName().getName(), getOffsetRange(vNode.getName()));
            }
        }
        if (newIdentifier != null) {
//            if (fn.getKind() == FunctionNode.Kind.ARROW) {
//                jsFunction.getParent().getProperties().remove(jsFunction.getName());
//                jsFunction.getParent().addProperty(newIdentifier.getName(), jsFunction);
//            }
            jsFunction.setDeclarationName(newIdentifier);
            jsFunction.addOccurrence(newIdentifier.getOffsetRange());

        }
    }

    private void setModifiers(JsFunctionImpl jsFunction, FunctionNode fn) {
        //Node lastVisited = getPreviousFromPath(2);
        boolean isPrivate = false;
        boolean isPrivilage = false;
        boolean isStatic = false;

        Node lastVisited = getPreviousFromPath(2);

        if (!lc.getParentFunction(fn).isProgram()
                && !(lastVisited instanceof PropertyNode || lastVisited instanceof BinaryNode)) {
            // it can be a part of anonymous object
            isPrivate = true;
        }
        if (lastVisited instanceof PropertyNode) {
            PropertyNode pNode = (PropertyNode)lastVisited;
            isStatic = pNode.isStatic();
            if (fn.isClassConstructor() || fn.isSubclassConstructor()) {
                jsFunction.setJsKind(JsElement.Kind.CONSTRUCTOR);
            } else if (fn.isMethod()) {
                if (fn.equals(pNode.getGetter())) {
                    jsFunction.setJsKind(JsElement.Kind.PROPERTY_GETTER);
                } else if (fn.equals(pNode.getSetter())) {
                    jsFunction.setJsKind(JsElement.Kind.PROPERTY_SETTER);
                } else {
                    jsFunction.setJsKind(JsElement.Kind.METHOD);
                }
                if(pNode.getKey() instanceof IdentNode
                        && ((IdentNode) pNode.getKey()).isPrivate()) {
                    isPrivilage = true;
                }
            }
        } else if (lastVisited instanceof BinaryNode) {
            BinaryNode bNode = (BinaryNode)lastVisited;
            if (bNode.getAssignmentDest() instanceof AccessNode) {
                // case like A.f1 = function (){} -> f1 is a public static property
                AccessNode aNode = (AccessNode)bNode.getAssignmentDest();
                List<Identifier> name = getName(aNode);
                if (name != null && ModelUtils.THIS.equals(name.get(0).getName())) {
                    isPrivilage = true;
                } else {
                    if (!ModelUtils.PROTOTYPE.equals(aNode.getProperty()) && jsFunction.getParent().getJSKind().isFunction()) {
                        if (aNode.getBase() instanceof AccessNode) {
                            if (!ModelUtils.PROTOTYPE.equals(((AccessNode)aNode.getBase()).getProperty())) {
                                // case like A.B.f1 = function () {}
                                isStatic = true;
                            }
                        } else {
                            isStatic = true;
                        }
                    }
                }
            }
        } else if (lastVisited instanceof CallNode) {
            if (getPreviousFromPath(3) instanceof UnaryNode) {
                if (getPreviousFromPath(4) instanceof VarNode) {
                    isPrivate = true;
                }
            }
        }

        if (fn.getKind() == FunctionNode.Kind.ARROW) {
            // marking the function as an arrow function
            jsFunction.setJsKind(JsElement.Kind.ARROW_FUNCTION);
        }

        if (fn.getKind() == FunctionNode.Kind.GENERATOR) {
            // marking the function as generator
            jsFunction.setJsKind(JsElement.Kind.GENERATOR);
        }

        Set<Modifier> modifiers = jsFunction.getModifiers();
        if (isPrivate || isPrivilage) {
            modifiers.remove(Modifier.PUBLIC);
            if (isPrivate) {
                modifiers.add(Modifier.PRIVATE);
            } else {
                modifiers.add(Modifier.PROTECTED);
            }
        }
        if (isStatic) {
            modifiers.add(Modifier.STATIC);
        }
        // setting whether the function is anonymous
        if (isFunctionAnonymous(fn)) {
            jsFunction.setAnonymous(true);
        }
    }

    private void setParent(JsFunctionImpl jsFunction, FunctionNode fn) {
        Node lastVisited = getPreviousFromPath(2);
        JsObject parent = jsFunction.getParent();
        if (lastVisited instanceof JoinPredecessorExpression
                && getPreviousFromPath(3) instanceof BinaryNode
                && getPreviousFromPath(4) instanceof VarNode) {
            // this handle case var f1 = xxx || function () {}
            // just skip the binary node and continue like in case var f1 = function (){}
            lastVisited = getPreviousFromPath(4);
        }
        if (lastVisited instanceof PropertyNode) {
            // the parent of the function is the literal object
            parent = modelBuilder.getCurrentObject();
        } else if (lastVisited instanceof VarNode) {
            VarNode varNode = (VarNode)lastVisited;
            if (fn.isNamedFunctionExpression()) {
                // case: var f1 = function fx() {}
                // the fx can be used only in fx, in other cases is unaccessible -> basically private function of f1
                // fx will be feference of f1
                parent.getProperties().remove(modelBuilder.getFunctionName(fn));
                JsObject variable = parent.getProperty(varNode.getName().getName());
                Identifier refName = new Identifier(fn.getIdent().getName(), new OffsetRange(fn.getIdent().getStart(), fn.getIdent().getFinish()));
                JsFunctionReference jsRef = new JsFunctionReference(jsFunction, refName, jsFunction, true,  EnumSet.of(Modifier.PRIVATE));
                jsRef.addOccurrence(jsRef.getDeclarationName().getOffsetRange());
                jsFunction.setDeclarationName(new Identifier(varNode.getName().getName(), getOffsetRange(varNode.getName())));
                if (variable != null) {
                    ModelUtils.copyOccurrences(variable, jsFunction);
                }
                parent.addProperty(jsFunction.getName(), jsFunction);
                jsFunction.addProperty(jsRef.getName(), jsRef);
            } else if ((varNode.isFunctionDeclaration() || fn.isAnonymous())) {
                // correct key name of properties in cases
                // var f1 = function () {}
                // var f1 = function f1() {}
                parent.getProperties().remove(modelBuilder.getFunctionName(fn));
                parent.addProperty(varNode.getName().getName(), jsFunction);
            }
        } else if (lastVisited instanceof BinaryNode) {
            // case like A.f1 = function () {}
            BinaryNode bNode = (BinaryNode)lastVisited;
            List<Identifier> name = getName(bNode, parserResult);
            boolean isPriviliged = false;

            if (name != null && !name.isEmpty()) {
                if (ModelUtils.THIS.equals(name.get(0).getName())) {
                    name.remove(0);
                    isPriviliged = true;
                    parent = (JsObjectImpl)resolveThis(parent);
                    JsObject hParent = parent;
                    while(hParent != null && hParent.getKind() != ElementKind.FILE && hParent.getDeclarationName() != null) {
                        name.add(0, hParent.getDeclarationName());
                        hParent = hParent.getParent();
                    }
                }

                boolean parentHasSameName = false;
                if (name.size() > 1 && name.get(0).getName().equals(jsFunction.getName())) {
                    JsObject property = parent.getProperty(modelBuilder.getFunctionName(fn));
                    if (property != null && property.equals(jsFunction)) {
                        // this handles case like:
                        // theSameName.theSameName = function theSameName () {}
                        parent.getProperties().remove(modelBuilder.getFunctionName(fn));
                        ((JsObjectImpl)property).clearOccurrences();
                        parentHasSameName = true;
                    }
                }
                JsObjectImpl jsObject = ModelUtils.getJsObject(modelBuilder, name, !parentHasSameName);
                if (!isPriviliged) {
                    parent = jsObject.getParent();
                }
                if (fn.isNamedFunctionExpression()) {
                    // case like A.f1 = function f1(){}
                    Identifier refName = new Identifier(fn.getIdent().getName(), new OffsetRange(fn.getIdent().getStart(), fn.getIdent().getFinish()));
                    JsFunctionReference jsRef = new JsFunctionReference(jsFunction, refName, jsFunction, true,  EnumSet.of(Modifier.PRIVATE));
                    jsRef.addOccurrence(jsRef.getDeclarationName().getOffsetRange());
                    jsFunction.addProperty(jsRef.getName(), jsRef);
                }
                jsFunction.setDeclarationName(jsObject.getDeclarationName());
                ModelUtils.copyOccurrences(jsObject, jsFunction);
                if (!parentHasSameName) {
                    String builderName = modelBuilder.getFunctionName(fn);
                    if (jsFunction.getParent().getProperties().remove(builderName) == null) {
                        // we need to check, whether is not declared in a block of the parent for handling cases like:
                        // onreadystatechange = function() {
                        //      if (true) {
                        //          onreadystatechange = function () {console.log("true");};
                        //      } else {
                        //          onreadystatechange = function () {console.log("false");};
                        //      }
                        // };
                        for (JsObject property : jsFunction.getParent().getProperties().values()) {
                            if (property.getJSKind() == JsElement.Kind.BLOCK && property.getProperties().remove(builderName) != null) {
                                if (property.getProperties().isEmpty()) {
                                    // remove the empty block from model
                                    property.getParent().getProperties().remove(property.getName());
                                }
                                break;
                            }
                        }
                    }
                }
                if (parent == null) {
                    parent = jsObject.getParent();
                }
                parent.addProperty(jsObject.getName(), jsFunction);
                jsFunction.setParent(parent);
            }
        } else if (lastVisited instanceof CallNode) {
            if (getPreviousFromPath(3) instanceof UnaryNode) {
                if (getPreviousFromPath(4) instanceof VarNode) {
                    // case var MyLib = new function XXX? () {}
                    VarNode varNode = (VarNode) getPreviousFromPath(4);
                    Expression init = varNode.getInit();
                    Identifier varName = new Identifier(varNode.getName().getName(), getOffsetRange(varNode.getName()));
                    OffsetRange range = varNode.getInit() instanceof ObjectNode ? new OffsetRange(varNode.getName().getStart(), ((ObjectNode)varNode.getInit()).getFinish())
                            : varName.getOffsetRange();

                    JsObject variable = handleArrayCreation(varNode.getInit(), parent, varName);
                    if (variable == null) {
                        JsObjectImpl newObject = new JsObjectImpl(parent, varName, range, jsFunction.getMimeType(), jsFunction.getSourceLabel());
                        newObject.setDeclared(true);
                        variable = newObject;
                    }
                    variable.addOccurrence(varName.getOffsetRange());
                    parent.getProperties().remove(jsFunction.getName());
                    parent.addProperty(varName.getName(), variable);
                    variable.addProperty(jsFunction.getName(), jsFunction);
                    jsFunction.setParent(variable);
//                    Collection<TypeUsage> returns = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, init);
//                    for (TypeUsage type : returns) {
                    variable.addAssignment(new TypeUsage(SemiTypeResolverVisitor.ST_NEW + variable.getName() + '.' + jsFunction.getName(), jsFunction.getDeclarationName().getOffsetRange().getStart()), init.getStart());
//                    }
                    if (fn.isNamedFunctionExpression() && fn.getName().equals(varName.getName())) {
                        // the name of function is the same as the variable
                        // var MyLib = new function MyLib() {};
                        ModelUtils.copyOccurrences(jsFunction, variable);
                    }
                    parent = variable;
                    int index = getPath().size() - 5;
                    while ( index > -1 && !(getPath().get(index) instanceof FunctionNode)) {
                        index--;
                    }
                    if(index > 0) {
                        // the variable is defined in a function -> the object is private
                        variable.getModifiers().remove(Modifier.PUBLIC);
                        variable.getModifiers().add(Modifier.PRIVATE);
                    }
                }
            }
        }

        if (!parent.equals(jsFunction.getParent())) {
            jsFunction.getParent().getProperties().remove(modelBuilder.getFunctionName(fn));
            jsFunction.setParent(parent);
            JsObject property = parent.getProperty(jsFunction.getName());
            if (property != null) {
                ModelUtils.copyOccurrences(property, jsFunction);
            }
            parent.addProperty(jsFunction.getName(), jsFunction);
        }

        DeclarationScopeImpl fnScope = (DeclarationScopeImpl)jsFunction;
        DeclarationScope parentScope = fnScope.getParentScope();
        if (parentScope != null) {
            parentScope.addDeclaredScope(fnScope);
        }
    }

    private boolean isFunctionAnonymous(FunctionNode fn) {
        boolean result = false;
        if (fn.isAnonymous() ) {
            Node lastVisited = getPreviousFromPath(2);
            if (fn.getIdent().getName().startsWith("L:") && !(lastVisited instanceof PropertyNode)) { //NOI18N
                // XXX this depends on the implemenation of parser. Find the better way
                result = true;
            } else if (fn.getIdent().getStart() == fn.getIdent().getFinish()) {
                if (lastVisited instanceof CallNode) {
                    result = true;
                }
            }
        }
        return result;
    }

    private void processJsDoc(JsFunctionImpl jsFunction, FunctionNode fn, JsDocumentationHolder docHolder) {
        if (!fn.isProgram()) {
            // the documentation for the function
            Documentation documentation = docHolder.getDocumentation(fn);
            jsFunction.setDocumentation(documentation);
            // parameters
            List<DocParameter> docParams = docHolder.getParameters(fn);
            for (DocParameter docParameter : docParams) {
                Identifier paramName = docParameter.getParamName();
                if (paramName != null) {
                    String sParamName = paramName.getName();
                    if(sParamName != null && !sParamName.isEmpty()) {
                        JsObjectImpl param = (JsObjectImpl) jsFunction.getParameter(sParamName);
                        if (param != null) {
                            for (Type type : docParameter.getParamTypes()) {
                                param.addAssignment(new TypeUsage(type.getType(), type.getOffset(), true), param.getOffset());
                            }
                            // param occurence in the doc
                            addDocNameOccurence(param);
                        }
                    }
                }
            }
            // mark constructors
            if (docHolder.isClass(fn)) {
                // needs to be marked before going through the nodes
                jsFunction.setJsKind(JsElement.Kind.CONSTRUCTOR);
            }

            jsFunction.setDeprecated(docHolder.isDeprecated(fn));

            // process @extends tag
            List<Type> extendTypes = docHolder.getExtends(fn);
            if (!extendTypes.isEmpty()) {
                JsObject prototype = jsFunction.getProperty(ModelUtils.PROTOTYPE);
                if (prototype == null) {
                    prototype = new JsObjectImpl(jsFunction, ModelUtils.PROTOTYPE, true, OffsetRange.NONE, EnumSet.of(Modifier.PUBLIC), parserResult.getSnapshot().getMimeType(), null);
                    jsFunction.addProperty(ModelUtils.PROTOTYPE, prototype);
                }
                for (Type type : extendTypes) {
                    prototype.addAssignment(new TypeUsage(type.getType(), type.getOffset(), true), type.getOffset());
                }
            }

            // process @returns tag
            List<Type> types = docHolder.getReturnType(fn);
            if (types != null && !types.isEmpty()) {
                for(Type type : types) {
                    jsFunction.addReturnType(new TypeUsage(type.getType(), type.getOffset(), true /*ModelUtils.isKnownGLobalType(type.getType())*/));
                }
            }
        }
        // look for the type defined through comment like @typedef
        Map<Integer, ? extends JsComment> commentBlocks = docHolder.getCommentBlocks();
        for (JsComment comment : commentBlocks.values()) {
            DocParameter definedType = comment.getDefinedType();
            if (definedType != null) {
                    // XXX the param name now can contains names with dot.
                // it would be better if the getParamName returns list of identifiers
                String typeName = definedType.getParamName().getName();
                List<Identifier> fqn = new ArrayList<>();
                JsObject whereOccurrence = getGlobalObject();
                if (typeName.indexOf('.') > -1) {
                    String[] parts = typeName.split("\\.");
                    int offset = definedType.getParamName().getOffsetRange().getStart();
                    int delta = 0;
                    for (int i = 0; i < parts.length; i++) {
                        fqn.add(new Identifier(parts[i], offset + delta));
                        if (whereOccurrence != null) {
                            whereOccurrence = whereOccurrence.getProperty(parts[i]);
                            if (whereOccurrence != null) {
                                whereOccurrence.addOccurrence(new OffsetRange(offset + delta, offset + delta + parts[i].length()));
                            }
                        }
                        delta = delta + parts[i].length() + 1;
                    }
                } else {
                    fqn.add(definedType.getParamName());
                }
                JsObject object = ModelUtils.getJsObject(modelBuilder, fqn, true);
                int assignOffset = definedType.getParamName().getOffsetRange().getEnd();
                List<Type> types = definedType.getParamTypes();

                for (Type type : types) {
                    object.addAssignment(new TypeUsage(type.getType(), type.getOffset()), assignOffset);
                }
                List<Type> assignedTypes = comment.getTypes();
                for (Type type : assignedTypes) {
                    object.addAssignment(new TypeUsage(type.getType(), type.getOffset()), assignOffset);
                }
                List<DocParameter> properties = comment.getProperties();
                for (DocParameter docProperty : properties) {
                    JsObject jsProperty = new JsObjectImpl(object, docProperty.getParamName(), docProperty.getParamName().getOffsetRange(), true, JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                    object.addProperty(jsProperty.getName(), jsProperty);
                    types = docProperty.getParamTypes();
                    jsProperty.setDocumentation(Documentation.create(docProperty.getParamDescription()));
                    assignOffset = docProperty.getParamName().getOffsetRange().getEnd();
                    for (Type type : types) {
                        jsProperty.addAssignment(new TypeUsage(type.getType(), type.getOffset()), assignOffset);
                    }
                }
            }
            Type callBack = comment.getCallBack();
            if (callBack != null) {
                List<Identifier> fqn = fqnFromType(callBack);
                markOccurrences(fqn);
                List<Identifier> parentFqn = new ArrayList<>();
                for (int i = 0; i < fqn.size() - 1; i++) {
                    parentFqn.add(fqn.get(i));
                }
                JsObject parentObject = parentFqn.isEmpty() ? getGlobalObject() : ModelUtils.getJsObject(modelBuilder, parentFqn, true);
                JsFunctionImpl callBackFunction = new JsFunctionImpl(
                        parentObject instanceof DeclarationScope ? (DeclarationScope) parentObject : ModelUtils.getDeclarationScope(parentObject),
                        parentObject, fqn.get(fqn.size() - 1), Collections.emptyList(),
                        callBack.getOffset() > -1 ? new OffsetRange(callBack.getOffset(), callBack.getOffset() + callBack.getType().length()) : OffsetRange.NONE,
                        JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                parentObject.addProperty(callBackFunction.getName(), callBackFunction);
                callBackFunction.setDocumentation(Documentation.create(comment.getDocumentation()));
                callBackFunction.setJsKind(JsElement.Kind.CALLBACK);
                List<DocParameter> docParameters = comment.getParameters();
                for (DocParameter docParameter : docParameters) {
                    ParameterObject parameter = new ParameterObject(callBackFunction, docParameter.getParamName(), JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                    for (Type type : docParameter.getParamTypes()) {
                        parameter.addAssignment(new TypeUsage(type.getType(), type.getOffset(), true), parameter.getOffset());
                    }
                    addDocNameOccurence(parameter);
                    callBackFunction.addParameter(parameter);
                }
            }
        }
    }

    private void processModifiersFromJsDoc(JsFunctionImpl jsFunction, FunctionNode fn, JsDocumentationHolder docHolder) {
        if (!fn.isProgram() && !fn.isModule() && docHolder != null) {
            Set<JsModifier> modifiers = docHolder.getModifiers(fn);
            if (modifiers != null && !modifiers.isEmpty()) {
                Set<Modifier> fnModifiers = jsFunction.getModifiers();
                if (modifiers.contains(JsModifier.PRIVATE)) {
                    fnModifiers.remove(Modifier.PUBLIC);
                    fnModifiers.remove(Modifier.PROTECTED);
                    fnModifiers.add(Modifier.PRIVATE);
                }
                if (modifiers.contains(JsModifier.PUBLIC)) {
                    fnModifiers.remove(Modifier.PRIVATE);
                    fnModifiers.remove(Modifier.PROTECTED);
                    fnModifiers.add(Modifier.PUBLIC);
                }
                if (modifiers.contains(JsModifier.STATIC)) {
                    fnModifiers.add(Modifier.STATIC);
                }
            }
        }
    }

    private void processDeclarations(final JsFunctionImpl parentFn, final FunctionNode inNode) {
        LOGGER.log(Level.FINEST, "in function: {0}, ident: {1}", new Object[]{inNode.getName(), inNode.getIdent()});
        final JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);

        Block block = inNode.getBody();
        PathNodeVisitor visitor = new PathNodeVisitor(lc)  {

            DeclarationScopeImpl currentBlockScope = parentFn;
            private boolean isParameterBlock = false;
            private final List<FunctionNode> declaredFunctions = new ArrayList<>();
            private final List<VarNode> declaredVars = new ArrayList<>();

            private void handleDeclarations() {
                if (!declaredFunctions.isEmpty() || !declaredVars.isEmpty()) {
                    for (FunctionNode fnNode : declaredFunctions) {
                        handleDeclaredFunction(currentBlockScope, parentFn, fnNode);
                    }
                    for (VarNode varNode : declaredVars) {
                        if (varNode.isLet()) {
                            // block scope variable
                            handleDeclaredVariable(currentBlockScope, parentFn, varNode, docHolder);
                        } else {
                            handleDeclaredVariable(parentFn, parentFn, varNode, docHolder);
                        }
                    }
                    declaredFunctions.clear();
                    declaredVars.clear();
                }
            }

            @Override
            public boolean enterBlock(Block block) {
                handleDeclarations();
                if (inNode.isStrict()) {
                    if (!getPath().isEmpty()) {
                        // we are in strict mode -> possible block scope declaration
                        currentBlockScope = new DeclarationScopeImpl(currentBlockScope, currentBlockScope,
                                new Identifier(BLOCK_OBJECT_NAME_PREFIX + block.getStart(), OffsetRange.NONE), new OffsetRange(block.getStart(), block.getFinish()), currentBlockScope.getMimeType() , currentBlockScope.getSourceLabel());
                        currentBlockScope.setJsKind(JsElement.Kind.BLOCK);
                    }
                }
                isParameterBlock = block.isParameterBlock();
                return super.enterBlock(block);
            }

            @Override
            public Node leaveBlock(Block block) {
                if (inNode.isStrict() || getPath().size() == 1) {
                    handleDeclarations();
                    if (getPath().size() > 1) {
                        DeclarationScopeImpl parentScope = (DeclarationScopeImpl)currentBlockScope.getParentScope();
                        if (!((JsObject)currentBlockScope).getProperties().isEmpty()) {
                            // don't keep this empty scope in model
                            parentScope.addDeclaredScope(currentBlockScope);
                            parentScope.addProperty(currentBlockScope.getName(), currentBlockScope);
                        }
                        currentBlockScope = parentScope;
                    }
                }
                return super.leaveBlock(block);
            }

            @Override
            public Node leaveExportNode(ExportNode exportNode) {
                handleDeclarations();
                return super.leaveExportNode(exportNode);
            }


            @Override
            public boolean enterClassNode(ClassNode classNode) {
                if (classNode.getConstructor() != null) {
                    classNode.getConstructor().accept(this);
                }
                if (classNode.getClassElements() != null) {
                    for (PropertyNode pn : classNode.getClassElements()) {
                        pn.accept(this);
                    }
                }
                return false;
            }


            @Override
            public boolean enterFunctionNode(FunctionNode fnNode) {
                declaredFunctions.add(fnNode);
                return false;
            }

            @Override
            public boolean enterVarNode(VarNode varNode) {
                if (!isParameterBlock) {
                    declaredVars.add(varNode);
                }
                return super.enterVarNode(varNode);
            }

        };
        if (inNode.isModule() && inNode.getModule().getExports() != null) {
            for(ExportNode export :inNode.getModule().getExports()) {
                if (!export.isDefault()) {
                    // don't go through the default export node, it appears also as *default* varible node
                    export.accept(visitor);
                } else {
                    Expression expression = export.getExpression();
                    if ((expression instanceof ClassNode && ((ClassNode)expression).getIdent() != null)
                            || (expression instanceof FunctionNode && ((FunctionNode)expression).getIdent() != null)) {
                        export.accept(visitor);
                    }
                }
            }
        }
        block.accept(visitor);
    }

    private void handleDeclaredFunction(DeclarationScopeImpl inScope, JsObject parent,  FunctionNode fnNode) {
        LOGGER.log(Level.FINEST, "       function: {0}", debugInfo(fnNode)); // NOI18N
        String name = fnNode.isAnonymous() ? modelBuilder.getFunctionName(fnNode) : fnNode.getIdent().getName();
        Identifier fnName = new Identifier(name, new OffsetRange(fnNode.getIdent().getStart(), fnNode.getIdent().getFinish()));
        if (isArtificialConstructor(fnNode)) {
            // skip artifical/ syntetic constructor nodes, that are created
            // when a class extends different class
            return;
        }
        // process parameters
        List<Identifier> parameters = new ArrayList<>(fnNode.getParameters().size());
        for(IdentNode node: fnNode.getParameters()) {
            Identifier param = create(parserResult, node);
            if (param != null && !node.isDestructuredParameter()) {
                // can be null, if it's a generated embeding.
                parameters.add(param);
            }
        }
        // The parent can be changed in the later processing
        JsFunctionImpl declaredFn = new JsFunctionImpl(inScope, parent, fnName, parameters, getOffsetRange(fnNode), inScope.getMimeType(), inScope.getSourceLabel());
        inScope.addProperty(modelBuilder.getFunctionName(fnNode), declaredFn);
        if (fnName.getOffsetRange().getLength() > 0 && !fnNode.isNamedFunctionExpression()) {
            declaredFn.addOccurrence(fnName.getOffsetRange());
        }
    }

    private void handleDeclaredVariable(DeclarationScopeImpl parentFn, JsObject parent,  VarNode varNode, JsDocumentationHolder docHolder) {
        LOGGER.log(Level.FINEST, "       variable: {0}", debugInfo(varNode)); // NOI18N
        Expression init = varNode.getInit();
        boolean createVariable = true;
        if (!varNode.isFunctionDeclaration() // we skip syntetic variables created from case: function f1(){}
                && !varNode.isExport()) { // we skip syntetic variables created from export expression
            if (init instanceof FunctionNode && !((FunctionNode) init).isNamedFunctionExpression()) {
                    // case: var f1 = function () {}
                // the function here is already, need to be just fixed the name offsets
                createVariable = false;
            } else if (init instanceof BinaryNode) {
                BinaryNode bNode = (BinaryNode) init;
                if (bNode.isLogical()
                        && ((bNode.rhs() instanceof JoinPredecessorExpression && ((JoinPredecessorExpression) bNode.rhs()).getExpression() instanceof FunctionNode)
                        || (bNode.lhs() instanceof JoinPredecessorExpression && ((JoinPredecessorExpression) bNode.lhs()).getExpression() instanceof FunctionNode))) {
                        // case: var f1 = xxx || function () {}
                    // the function here is already, need to be just fixed the name offsets
                    createVariable = false;
                } else if (bNode.isAssignment()) {
                    createVariable = false;
                    if (parentFn.getProperty(varNode.getName().getName()) == null) {
                        while (bNode.rhs() instanceof BinaryNode && bNode.rhs().isAssignment()) {
                                // the cycle is trying to find out a FunctionNode at the end of assignements
                            // case var f1 = f2 = f3 = f4 = function () {}
                            bNode = (BinaryNode) bNode.rhs();
                        }
                        if (bNode.rhs() instanceof FunctionNode) {
                                // case var f1 = f2 = function (){};
                            // -> the variable will be reference fo the function
                            FunctionNode fNode = (FunctionNode) bNode.rhs();
                            JsObject original = parentFn.getProperty(modelBuilder.getFunctionName(fNode));
                            if (original != null) {
                                Identifier varName = new Identifier(varNode.getName().getName(), getOffsetRange(varNode.getName()));
                                JsFunctionReference variable = new JsFunctionReference(parentFn, varName, (JsFunction) original, true, original.getModifiers());
                                variable.addOccurrence(varName.getOffsetRange());
                                parentFn.addProperty(varName.getName(), variable);
                            }
                        }
                    }
                }
            } else if (parentFn.getProperty(varNode.getName().getName()) != null) {
                // the name is already used by a function.
                if (!(init instanceof CallNode) && !(init instanceof UnaryNode)) {
                        // we skip the var declaration basically, but has to be added occuerences for the existing property
                    // with the same name
                    parentFn.getProperty(varNode.getName().getName()).addOccurrence(getOffsetRange(varNode.getName()));
                }
                createVariable = false;
            }
            if (createVariable) {
                // skip the variables that are syntetic
                Identifier varName = new Identifier(varNode.getName().getName(), getOffsetRange(varNode.getName()));
                OffsetRange range = varNode.getInit() instanceof ObjectNode ? new OffsetRange(varNode.getName().getStart(), ((ObjectNode) varNode.getInit()).getFinish())
                        : varName.getOffsetRange();
                JsObject variable = handleArrayCreation(varNode.getInit(), parentFn, varName);
                if (variable == null) {
                    JsObjectImpl newObject = new JsObjectImpl(parentFn, varName, range, parserResult.getSnapshot().getMimeType(), null);
                    variable = newObject;
                }
                variable.addOccurrence(varName.getOffsetRange());
                parentFn.addProperty(varName.getName(), variable);
                variable.addOccurrence(varName.getOffsetRange());

                if (docHolder != null) {
                    ((JsObjectImpl) variable).setDocumentation(docHolder.getDocumentation(varNode));
                    ((JsObjectImpl) variable).setDeprecated(docHolder.isDeprecated(varNode));
                }
            }
        }
    }


    private List<Identifier> fqnFromType (final Type type) {
        List<Identifier> fqn = new ArrayList<>();
        String typeName = type.getType();
        int offset = type.getOffset();
        if (typeName.indexOf('.') > -1) {
            String[] parts = typeName.split("\\.");
            int delta = 0;
            for (int i = 0; i < parts.length; i++) {
                fqn.add(new Identifier(parts[i], offset + delta));
                delta = delta + parts[i].length() + 1;
            }
        } else {
            fqn.add(new Identifier(typeName, offset));
        }
        return fqn;
    }

    private void markOccurrences (List<Identifier> fqn) {
        JsObject whereOccurrence = getGlobalObject();
        for (Identifier iden: fqn) {
            whereOccurrence = whereOccurrence.getProperty(iden.getName());
            if (whereOccurrence != null) {
                whereOccurrence.addOccurrence(iden.getOffsetRange());
            } else {
                break;
            }
        }
    }

    private JsArray handleArrayCreation(Node initNode, JsObject parent, Identifier name) {
        if (initNode instanceof UnaryNode && parent != null) {
            UnaryNode uNode = (UnaryNode)initNode;
            if (uNode.tokenType() == TokenType.NEW && uNode.getExpression() instanceof CallNode) {
                CallNode cNode = (CallNode)uNode.getExpression();
                if (cNode.getFunction() instanceof IdentNode && "Array".equals(((IdentNode)cNode.getFunction()).getName())) {
                    List<TypeUsage> itemTypes = new ArrayList<>();
                    for (Node node : cNode.getArgs()) {
                        itemTypes.addAll(ModelUtils.resolveSemiTypeOfExpression(modelBuilder, node));
                    }
                    EnumSet<Modifier> modifiers = parent.getJSKind() != JsElement.Kind.FILE ? EnumSet.of(Modifier.PRIVATE) : EnumSet.of(Modifier.PUBLIC);
                    JsArrayImpl result = new JsArrayImpl(parent, name, name.getOffsetRange(), true, modifiers, parserResult.getSnapshot().getMimeType(), null);
                    result.addTypesInArray(itemTypes);
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public boolean enterLiteralNode(LiteralNode lNode) {
        Node lastVisited = getPreviousFromPath(1);
        if (lNode instanceof LiteralNode.ArrayLiteralNode) {
            LiteralNode.ArrayLiteralNode aNode = (LiteralNode.ArrayLiteralNode)lNode;
            List<Identifier> fqName = null;
            int pathSize = getPath().size();
            boolean isDeclaredInParent = false;
            boolean isPrivate = false;
            boolean treatAsAnonymous = false;
            JsObject parent = null;

            if (lastVisited instanceof TernaryNode && pathSize > 1) {
                lastVisited = getPath().get(pathSize - 2);
            }
            int pathIndex = 1;

            while(lastVisited instanceof BinaryNode
                    && (pathSize > pathIndex)
                    && ((BinaryNode)lastVisited).tokenType() != TokenType.ASSIGN) {
                pathIndex++;
                lastVisited = getPath().get(pathSize - pathIndex);
            }
            if ( lastVisited instanceof VarNode) {
                fqName = getName((VarNode)lastVisited);
                isDeclaredInParent = true;
                JsObject declarationScope = ((VarNode)lastVisited).isLet() ? modelBuilder.getCurrentDeclarationScope() : modelBuilder.getCurrentDeclarationFunction();
                parent = declarationScope;
                if (fqName.size() == 1 && !ModelUtils.isGlobal(declarationScope)) {
                    isPrivate = true;
                }
            } else if (lastVisited instanceof PropertyNode) {
                fqName = getName((PropertyNode) lastVisited);
                isDeclaredInParent = true;
            } else if (lastVisited instanceof BinaryNode) {
                BinaryNode binNode = (BinaryNode) lastVisited;
                if (binNode.lhs() instanceof IndexNode) {
                    Node index =  ((IndexNode)binNode.lhs()).getIndex();
                    if (!(index instanceof LiteralNode && ((LiteralNode)index).isString())) {
                        treatAsAnonymous = true;
                    }
                }
                if (!treatAsAnonymous) {
                    if (getPath().size() > 1) {
                        lastVisited = getPath().get(getPath().size() - pathIndex - 1);
                    }
                    fqName = getName(binNode, parserResult);
                    if ((binNode.lhs() instanceof IdentNode)
                            || (binNode.lhs() instanceof AccessNode
                            && ((AccessNode) binNode.lhs()).getBase() instanceof IdentNode
                            && ((IdentNode) ((AccessNode) binNode.lhs()).getBase()).getName().equals(ModelUtils.THIS))) { //NOI18N
                        if (lastVisited instanceof ExpressionStatement && !fqName.get(0).getName().equals(ModelUtils.THIS)) { //NOI18N
                            // try to catch the case: pool = [];
                            List<Identifier> objectName = fqName.size() > 1 ? fqName.subList(0, fqName.size() - 1) : fqName;
                            JsObject existingArray = ModelUtils.getJsObject(modelBuilder, objectName, false);
                            if (existingArray != null) {
                                existingArray.addOccurrence(fqName.get(fqName.size() - 1).getOffsetRange());
                                return super.enterLiteralNode(lNode);
                            }
                        } else {
                            isDeclaredInParent = true;
                            if (!(binNode.lhs() instanceof IdentNode)) {
                                parent = resolveThis(modelBuilder.getCurrentObject());
                            }
                        }
                    }
                }
            } else if (lastVisited instanceof CallNode || lastVisited instanceof LiteralNode.ArrayLiteralNode
                    || lastVisited instanceof ReturnNode || lastVisited instanceof AccessNode) {
                // probably an anonymous array as a parameter of a function call
                // or array in an array: var a = [['a', 10], ['b', 20]];
                // or [1,2,3].join();
                treatAsAnonymous = true;
            }
            if (!isDeclaredInParent) {
                if (lastVisited instanceof FunctionNode) {
                    isDeclaredInParent = ((FunctionNode) lastVisited).getKind() == FunctionNode.Kind.SCRIPT;
                }
            }
            JsArrayImpl array = null;
            if (!treatAsAnonymous) {
//                if (fqName == null || fqName.isEmpty()) {
//                    fqName = new ArrayList<Identifier>(1);
//                    fqName.add(new Identifier("UNKNOWN", //NOI18N
//                            new OffsetRange(lNode.getStart(), lNode.getFinish())));
//                }

                if (fqName != null && !fqName.isEmpty() && fqName.get(0) != null) {
                    if (ModelUtils.THIS.equals(fqName.get(0).getName())) {
                        parent = resolveThis(modelBuilder.getCurrentObject());
                        fqName.remove(0);
                        JsObject tmpObject = parent;
                        while (tmpObject.getParent() != null) {
                            Identifier dName = tmpObject.getDeclarationName();
                            fqName.add(0, dName != null ? tmpObject.getDeclarationName() : new Identifier(tmpObject.getName(), OffsetRange.NONE));
                            tmpObject = tmpObject.getParent();
                        }
                    }
                    array = ModelElementFactory.create(parserResult, aNode, fqName, modelBuilder, isDeclaredInParent, parent);
                    if (array != null && isPrivate) {
                        array.getModifiers().remove(Modifier.PUBLIC);
                        array.getModifiers().add(Modifier.PRIVATE);
                    }
                }
            } else {
                array = ModelElementFactory.createAnonymousObject(parserResult, aNode, modelBuilder);
            }
            if (array != null) {
                int aOffset = fqName == null ? lastVisited.getStart() : fqName.get(fqName.size() - 1).getOffsetRange().getEnd();
                array.addAssignment(ModelUtils.resolveSemiTypeOfExpression(modelBuilder, lNode), aOffset);
                for (Node item : aNode.getElementExpressions()) {
                    array.addTypesInArray(ModelUtils.resolveSemiTypeOfExpression(modelBuilder, item));
                }
                if (!functionArgumentStack.isEmpty()) {
                    functionArgumentStack.peek().add(array);
                }
            }
        }
        return super.enterLiteralNode(lNode);
    }

    @Override
    public boolean enterObjectNode(ObjectNode objectNode) {
        Node previousVisited = getPath().get(getPath().size() - 1);
        if(previousVisited instanceof CallNode
                || previousVisited instanceof LiteralNode.ArrayLiteralNode
                || previousVisited instanceof ExpressionStatement) {
            // TODO there should be handled anonymous object that are going as parameter to a funciton
            //create anonymous object
            JsObjectImpl object = ModelElementFactory.createAnonymousObject(parserResult, objectNode,  modelBuilder);
            modelBuilder.setCurrentObject(object);
            object.setJsKind(JsElement.Kind.OBJECT_LITERAL);
            if (!functionArgumentStack.isEmpty()) {
                functionArgumentStack.peek().add(object);
            }
            return super.enterObjectNode(objectNode);
        } else if (previousVisited instanceof ReturnNode
                 || (previousVisited instanceof BinaryNode && ((BinaryNode)previousVisited).tokenType() == TokenType.COMMARIGHT)) {
            JsObjectImpl objectScope = ModelElementFactory.createAnonymousObject(parserResult, objectNode, modelBuilder);
            modelBuilder.setCurrentObject(objectScope);
            objectScope.setJsKind(JsElement.Kind.OBJECT_LITERAL);
        } else if (previousVisited instanceof ExportNode && ((ExportNode)previousVisited).isDefault()) {
            // we are handling case: export default {}
            // the node should be visible in navigator
            List<Identifier> fqName = new ArrayList<>(1);
            fqName.add(new Identifier("default", OffsetRange.NONE)); // NOI18N
            JsObjectImpl objectScope = ModelElementFactory.create(parserResult, objectNode, fqName, modelBuilder, true); //ModelElementFactory.createAnonymousObject(parserResult, objectNode, modelBuilder);
            modelBuilder.setCurrentObject(objectScope);
            objectScope.setJsKind(JsElement.Kind.OBJECT_LITERAL);
        } else {
            List<Identifier> fqName = null;
            int pathSize = getPath().size();
            boolean isDeclaredInParent = false;
            boolean isDeclaredThroughThis = false;
            boolean isPrivate = false;
            boolean treatAsAnonymous = false;

            int pathIndex = 1;
            Node lastVisited = getPath().get(pathSize - pathIndex);
            VarNode varNode = null;

            if (lastVisited instanceof JoinPredecessorExpression) {
                pathIndex++;
                lastVisited = getPath().get(pathSize - pathIndex);
            }
            if (lastVisited instanceof TernaryNode && pathSize > 1) {
                TernaryNode tNode = (TernaryNode)lastVisited;
                lastVisited = getPath().get(pathSize - pathIndex - 1);
                if (lastVisited instanceof ExpressionStatement || lastVisited instanceof BinaryNode) {
                    JoinPredecessorExpression trueExpression = tNode.getTrueExpression();
                    JoinPredecessorExpression falseExpression = tNode.getFalseExpression();
                    if (trueExpression.getExpression().equals(objectNode) || falseExpression.getExpression().equals(objectNode)) {
                        // now we have to find out, whether we are in parameter block
                        int blockIndex = pathIndex + 1;
                        Block block = null;
                        while (blockIndex < getPath().size() && block == null) {
                            if (getPreviousFromPath(++blockIndex) instanceof Block) {
                                block = (Block)getPreviousFromPath(blockIndex);
                            }
                        }
                        // this is can be case when the object literal is a part of destructure assignman pattern used as parameter
                        // function drawES6Chart({size = 'big', cords = { x: 0, y: 0 , z: 0}, radius = 25} = {}) {}
                        treatAsAnonymous = block != null && block.isParameterBlock();
                    }
                }
            }
            if (lastVisited instanceof BinaryNode) {
                BinaryNode bNode = (BinaryNode)lastVisited;
                if (bNode.lhs().equals(objectNode)) {
                    // case of destructuring assignment { a, b} = ....
                    // we should not create object in the model.
                    return super.enterObjectNode(objectNode);
                } else if (bNode.lhs() instanceof ObjectNode && bNode.rhs().equals(objectNode)) {
                    // case of destructuring assignment {a, b} = {a:1, b:2}
                    // do nothing/ already processed in binary node.
                    return false;
                }
            }

            while(lastVisited instanceof BinaryNode
                    && (pathSize > pathIndex)
                    && ((BinaryNode)lastVisited).tokenType() != TokenType.ASSIGN) {
                pathIndex++;
                lastVisited = getPath().get(pathSize - pathIndex);
            }
            if ( lastVisited instanceof VarNode) {
                fqName = getName((VarNode)lastVisited);
                isDeclaredInParent = true;
                JsObject declarationScope = modelBuilder.getCurrentDeclarationFunction();
                varNode = (VarNode)lastVisited;
                if (fqName.size() == 1 && !ModelUtils.isGlobal(declarationScope)) {
                    isPrivate = true;
                }
            } else if (lastVisited instanceof PropertyNode) {
                fqName = getName((PropertyNode) lastVisited);
                isDeclaredInParent = true;
            } else if (lastVisited instanceof AccessNode) {
                treatAsAnonymous = true;
            } else if (lastVisited instanceof BinaryNode) {
                BinaryNode binNode = (BinaryNode) lastVisited;
                Node binLhs = binNode.lhs();
                if (binLhs instanceof IndexNode) {
                    Node index =  ((IndexNode)binLhs).getIndex();
                    if (!(index instanceof LiteralNode && ((LiteralNode)index).isString())) {
                        treatAsAnonymous = true;
                    }
                }
                if (!treatAsAnonymous) {
                    if (getPath().size() > 1) {
                        lastVisited = getPath().get(getPath().size() - pathIndex - 1);
                        if (lastVisited instanceof VarNode) {
                            varNode = (VarNode) lastVisited;
                        }
                    }
                    fqName = getName(binNode, parserResult);
                    if (binLhs instanceof IdentNode || (binLhs instanceof AccessNode
                            && ((AccessNode) binLhs).getBase() instanceof IdentNode
                            && ((IdentNode) ((AccessNode) binLhs).getBase()).getName().equals(ModelUtils.THIS))) {
                        // if it's not declared throgh the var node, then the variable doesn't have to be declared here
                        isDeclaredInParent = (binLhs instanceof IdentNode &&  varNode != null);
                        if (binLhs instanceof AccessNode) {
                            isDeclaredInParent = true;
                            isDeclaredThroughThis = true;
                        }
                    }
                }
            }
            if (!isDeclaredInParent) {
                if (lastVisited instanceof FunctionNode) {
                    isDeclaredInParent = ((FunctionNode) lastVisited).getKind() == FunctionNode.Kind.SCRIPT;
                }
            }
            if (!treatAsAnonymous) {
                if (fqName == null || fqName.isEmpty()) {
                    fqName = new ArrayList<>(1);
                    fqName.add(new Identifier("UNKNOWN", //NOI18N
                            new OffsetRange(objectNode.getStart(), objectNode.getFinish())));
                }
                JsObjectImpl objectScope;
                if (varNode != null) {
                    objectScope = modelBuilder.getCurrentObject();
                } else {
                    Identifier name = fqName.get(fqName.size() - 1);
                    JsObject alreadyThere = null;
                    if (isDeclaredThroughThis) {
                        JsObject thisIs = resolveThis(modelBuilder.getCurrentObject());
                        alreadyThere = thisIs.getProperty(name.getName());
                    } else {
                        if (isDeclaredInParent) {
                            if (lastVisited instanceof PropertyNode) {
                                alreadyThere = modelBuilder.getCurrentObject().getProperty(name.getName());
                            } else {
                                alreadyThere = ModelUtils.getJsObjectByName(modelBuilder.getCurrentDeclarationFunction(), name.getName());
                            }
                        } else {
                            if (fqName.size() == 1) {
                                alreadyThere = ModelUtils.getScopeVariable(modelBuilder.getCurrentDeclarationScope(), name.getName());
                            }
                            if (alreadyThere == null) {
                                alreadyThere = ModelUtils.getJsObject(modelBuilder, fqName, true);
                            }
                        }
                    }

                    objectScope = (alreadyThere == null)
                            ? ModelElementFactory.create(parserResult, objectNode, fqName, modelBuilder, isDeclaredInParent)
                            : (JsObjectImpl)alreadyThere;
                    if (alreadyThere != null) {
                        ((JsObjectImpl)alreadyThere).addOccurrence(name.getOffsetRange());
                    }
                }
                if (objectScope != null) {
                    objectScope.setJsKind(JsElement.Kind.OBJECT_LITERAL);
                    if (!objectScope.isDeclared()) {
                        // the objec literal is always declared
                        objectScope.setDeclared(true);
                    }
                    modelBuilder.setCurrentObject(objectScope);
                    if (isPrivate) {
                        objectScope.getModifiers().remove(Modifier.PUBLIC);
                        objectScope.getModifiers().add(Modifier.PRIVATE);
                    }
                }
            } else {
                JsObjectImpl objectScope = ModelElementFactory.createAnonymousObject(parserResult, objectNode, modelBuilder);
                modelBuilder.setCurrentObject(objectScope);
            }

        }

        return super.enterObjectNode(objectNode);
    }

    @Override
    public Node leaveObjectNode(ObjectNode objectNode) {
        Node lastVisited = getPreviousFromPath(2);

        if (lastVisited instanceof BinaryNode) {
            BinaryNode bNode = (BinaryNode)lastVisited;
            if (bNode.lhs().equals(objectNode)) {
                // case of destructuring assignment { a, b} = ....
                // we dob't create object in the model, but process the property nodes -> skip reseting modelBuilder
                return super.leaveObjectNode(objectNode);
            }
        }
        modelBuilder.reset();
        return super.leaveObjectNode(objectNode);
    }

    @Override
    public boolean enterPropertyNode(PropertyNode propertyNode) {
        final Expression key = propertyNode.getKey();
        final Expression value = propertyNode.getValue();
        List<Expression> decorators = propertyNode.getDecorators();
        if (decorators != null && !decorators.isEmpty()) {
            for (Expression decorator : decorators) {
                if (decorator instanceof IdentNode) {
                    // in such case, this is probaly a function
                    addOccurence((IdentNode)decorator, false, true);
                } else {
                    decorator.accept(this);
                }
            }
        }
        if ((key instanceof IdentNode || key instanceof LiteralNode)
                && !(value instanceof ObjectNode
                || value instanceof FunctionNode)
                && !propertyNode.isComputed()) {
            final JsObjectImpl parent = modelBuilder.getCurrentObject();
            Identifier name = null;
            if (key instanceof IdentNode) {
                name = ModelElementFactory.create(parserResult, (IdentNode)key);
            } else if (key instanceof LiteralNode) {
                name = ModelElementFactory.create(parserResult, (LiteralNode)key);
            }
            if (name != null) {
                if (key instanceof IdentNode && value instanceof IdentNode) {
                    IdentNode iKey = (IdentNode)key;
                    IdentNode iValue = (IdentNode)value;
                    if (iKey.getName().equals(iValue.getName()) && iKey.getStart() == iValue.getStart() && iKey.getFinish() == iValue.getFinish()) {
                        // it's object initializer shorthand property names
                        // (ES6) var o = { a, b, c }; The variables a, b and c has to exists and properties are references to the orig var
                        JsObject variable = ModelUtils.getScopeVariable(modelBuilder.getCurrentDeclarationScope(), name.getName());
                        if (variable != null) {
                            parent.addProperty(variable.getName(), variable);
                            variable.addOccurrence(name.getOffsetRange());
                            // don't continue.
                            return false;
                        }
                    }
                }
                JsObjectImpl property = (JsObjectImpl)parent.getProperty(name.getName());
                if (property == null) {
                    if (parent.getJSKind() == JsElement.Kind.OBJECT_LITERAL || parent.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT) {
                        property = ModelElementFactory.create(parserResult, propertyNode, name, modelBuilder, true);
                    } else {
                        // the object literal was not created before property node,
                        // so it can be destructive assignment on the left side

                        // find the block node to decide whether's the property node are not
                        // parameters defined via destructive assignment
                        // case function drawES6Chart({size = 'big', cords = { x: 0, y: 0 }, radius = 25} = test) {}
                        int index = 1;
                        Node node = getPreviousFromPath(index);
                        BinaryNode bNode = null;
                        ObjectNode oNode = null;
                        while (index < getPath().size() && !(node instanceof Block)) {
                            if (bNode == null && node instanceof BinaryNode) {
                                bNode = (BinaryNode)node;
                            } else if (oNode == null && node instanceof ObjectNode) {
                                oNode = (ObjectNode)node;
                            }
                            node = getPreviousFromPath(++index);
                        }
                        boolean isDestructiveParam = false;
                        if (node instanceof Block) {
                            Block block = (Block)node;
                            if (block.isParameterBlock() && oNode != null && bNode != null && bNode.lhs().equals(oNode) ) {
                                // we are in parameters defined via destructive assignment
                                JsFunction currentFnImpl = modelBuilder.getCurrentDeclarationFunction();
                                property = (JsObjectImpl)currentFnImpl.getParameter(name.getName());
                                isDestructiveParam = true;
                            }
                        }
                        if (!isDestructiveParam) {
                            property = ModelElementFactory.create(parserResult, propertyNode, name, modelBuilder, true);
                        }
                    }
                } else {
                    // The property can be already defined, via a usage before declaration (see testfiles/model/simpleObject.js - called property)
                    JsObjectImpl newProperty = ModelElementFactory.create(parserResult, propertyNode, name, modelBuilder, true);
                    if (newProperty != null) {
                        newProperty.addOccurrence(property.getDeclarationName().getOffsetRange());
                        for(Occurrence occurrence : property.getOccurrences()) {
                            newProperty.addOccurrence(occurrence.getOffsetRange());
                        }
                        property = newProperty;
                    }
                }

                if (property != null) {
//                    if (propertyNode.getGetter() != null) {
//                        FunctionNode getter = ((FunctionNode)((ReferenceNode)propertyNode.getGetter()).getReference());
//                        property.addOccurrence(new OffsetRange(getter.getIdent().getStart(), getter.getIdent().getFinish()));
//                    }
//
//                    if (propertyNode.getSetter() != null) {
//                        FunctionNode setter = ((FunctionNode)((ReferenceNode)propertyNode.getSetter()).getReference());
//                        property.addOccurrence(new OffsetRange(setter.getIdent().getStart(), setter.getIdent().getFinish()));
//                    }
                    property.getParent().addProperty(name.getName(), property);
                    property.setDeclared(true);
                    if(key instanceof IdentNode
                            && ((IdentNode) key).isPrivate()
                            && property.getModifiers().contains(Modifier.PUBLIC)) {
                        property.getModifiers().remove(Modifier.PUBLIC);
                        property.getModifiers().add(Modifier.PROTECTED);
                    }
                    if(propertyNode.isStatic()) {
                        property.getModifiers().add(Modifier.STATIC);
                    }
                    if(value instanceof CallNode) {
                        // TODO for now, don't continue. There shoudl be handled cases liek
                        // in the testFiles/model/property02.js file
                        //return null;
                    } else {
                        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, value);
                        if (!types.isEmpty()) {
                            property.addAssignment(types, name.getOffsetRange().getStart());
                        }
                        if (value instanceof IdentNode) {
                            IdentNode iNode = (IdentNode)value;
                            if (!iNode.getPropertyName().equals(name.getName())) {
                                addOccurence((IdentNode)value, false);
                            } else {
                                // handling case like property: property
                                if (parent.getParent() != null) {
                                    occurrenceBuilder.addOccurrence(name.getName(), getOffsetRange(iNode), modelBuilder.getCurrentDeclarationScope(), parent.getParent(), modelBuilder.getCurrentWith(), false, false);
                                }
                            }
                        }
                    }
                }
            }
        } if (propertyNode.isComputed()) {
            propertyNode.getKey().accept(this);
        }
        return super.enterPropertyNode(propertyNode);
    }

    @Override
    public boolean enterClassElement(ClassElement classElement) {
        final Expression key = classElement.getKey();
        final Expression value = classElement.getValue();
        List<Expression> decorators = classElement.getDecorators();
        if (decorators != null && !decorators.isEmpty()) {
            for (Expression decorator : decorators) {
                if (decorator instanceof IdentNode) {
                    // in such case, this is probaly a function
                    addOccurence((IdentNode)decorator, false, true);
                } else {
                    decorator.accept(this);
                }
            }
        }
        if ((key instanceof IdentNode || key instanceof LiteralNode)
                && !(value instanceof ObjectNode
                || value instanceof FunctionNode)
                && !classElement.isComputed()) {
            final JsObjectImpl parent = modelBuilder.getCurrentObject();
            Identifier name = null;
            if (key instanceof IdentNode) {
                name = ModelElementFactory.create(parserResult, (IdentNode)key);
            } else if (key instanceof LiteralNode) {
                name = ModelElementFactory.create(parserResult, (LiteralNode)key);
            }
            if (name != null) {
                if (key instanceof IdentNode && value instanceof IdentNode) {
                    IdentNode iKey = (IdentNode)key;
                    IdentNode iValue = (IdentNode)value;
                    if (iKey.getName().equals(iValue.getName()) && iKey.getStart() == iValue.getStart() && iKey.getFinish() == iValue.getFinish()) {
                        // it's object initializer shorthand property names
                        // (ES6) var o = { a, b, c }; The variables a, b and c has to exists and properties are references to the orig var
                        JsObject variable = ModelUtils.getScopeVariable(modelBuilder.getCurrentDeclarationScope(), name.getName());
                        if (variable != null) {
                            parent.addProperty(variable.getName(), variable);
                            variable.addOccurrence(name.getOffsetRange());
                            // don't continue.
                            return false;
                        }
                    }
                }
                JsObjectImpl property = (JsObjectImpl)parent.getProperty(name.getName());
                if (property == null) {
                    if (parent.getJSKind() == JsElement.Kind.OBJECT_LITERAL || parent.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT) {
                        property = ModelElementFactory.create(parserResult, classElement, name, modelBuilder, true);
                    } else {
                        // the object literal was not created before property node,
                        // so it can be destructive assignment on the left side

                        // find the block node to decide whether's the property node are not
                        // parameters defined via destructive assignment
                        // case function drawES6Chart({size = 'big', cords = { x: 0, y: 0 }, radius = 25} = test) {}
                        int index = 1;
                        Node node = getPreviousFromPath(index);
                        BinaryNode bNode = null;
                        ObjectNode oNode = null;
                        while (index < getPath().size() && !(node instanceof Block)) {
                            if (bNode == null && node instanceof BinaryNode) {
                                bNode = (BinaryNode)node;
                            } else if (oNode == null && node instanceof ObjectNode) {
                                oNode = (ObjectNode)node;
                            }
                            node = getPreviousFromPath(++index);
                        }
                        boolean isDestructiveParam = false;
                        if (node instanceof Block) {
                            Block block = (Block)node;
                            if (block.isParameterBlock() && oNode != null && bNode != null && bNode.lhs().equals(oNode) ) {
                                // we are in parameters defined via destructive assignment
                                JsFunction currentFnImpl = modelBuilder.getCurrentDeclarationFunction();
                                property = (JsObjectImpl)currentFnImpl.getParameter(name.getName());
                                isDestructiveParam = true;
                            }
                        }
                        if (!isDestructiveParam) {
                            property = ModelElementFactory.create(parserResult, classElement, name, modelBuilder, true);
                        }
                    }
                } else {
                    // The property can be already defined, via a usage before declaration (see testfiles/model/simpleObject.js - called property)
                    JsObjectImpl newProperty = ModelElementFactory.create(parserResult, classElement, name, modelBuilder, true);
                    if (newProperty != null) {
                        newProperty.addOccurrence(property.getDeclarationName().getOffsetRange());
                        for(Occurrence occurrence : property.getOccurrences()) {
                            newProperty.addOccurrence(occurrence.getOffsetRange());
                        }
                        property = newProperty;
                    }
                }

                if (property != null) {
//                    if (propertyNode.getGetter() != null) {
//                        FunctionNode getter = ((FunctionNode)((ReferenceNode)propertyNode.getGetter()).getReference());
//                        property.addOccurrence(new OffsetRange(getter.getIdent().getStart(), getter.getIdent().getFinish()));
//                    }
//
//                    if (propertyNode.getSetter() != null) {
//                        FunctionNode setter = ((FunctionNode)((ReferenceNode)propertyNode.getSetter()).getReference());
//                        property.addOccurrence(new OffsetRange(setter.getIdent().getStart(), setter.getIdent().getFinish()));
//                    }
                    property.getParent().addProperty(name.getName(), property);
                    property.setDeclared(true);
                    if(key instanceof IdentNode
                            && ((IdentNode) key).isPrivate()
                            && property.getModifiers().contains(Modifier.PUBLIC)) {
                        property.getModifiers().remove(Modifier.PUBLIC);
                        property.getModifiers().add(Modifier.PROTECTED);
                    }
                    if(classElement.isStatic()) {
                        property.getModifiers().add(Modifier.STATIC);
                    }
                    if(value instanceof CallNode) {
                        // TODO for now, don't continue. There shoudl be handled cases liek
                        // in the testFiles/model/property02.js file
                        //return null;
                    } else {
                        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, value);
                        if (!types.isEmpty()) {
                            property.addAssignment(types, name.getOffsetRange().getStart());
                        }
                        if (value instanceof IdentNode) {
                            IdentNode iNode = (IdentNode)value;
                            if (!iNode.getPropertyName().equals(name.getName())) {
                                addOccurence((IdentNode)value, false);
                            } else {
                                // handling case like property: property
                                if (parent.getParent() != null) {
                                    occurrenceBuilder.addOccurrence(name.getName(), getOffsetRange(iNode), modelBuilder.getCurrentDeclarationScope(), parent.getParent(), modelBuilder.getCurrentWith(), false, false);
                                }
                            }
                        }
                    }
                }
            }
        } if (classElement.isComputed()) {
            classElement.getKey().accept(this);
        }
        return super.enterClassElement(classElement);
    }

//
//    @Override
//    public Node enter(ReferenceNode referenceNode) {
//        FunctionNode reference = referenceNode.getReference();
//        if (reference != null) {
//            Node lastNode = getPreviousFromPath(1);
//            if (!((lastNode instanceof VarNode) && !reference.isAnonymous())) {
//                if (lastNode instanceof BinaryNode && !reference.isAnonymous()) {
//                    Node lhs = ((BinaryNode)lastNode).lhs();
//                    List<Identifier> nodeName = getNodeName(lhs, parserResult);
//                    if (nodeName != null && !nodeName.isEmpty()) {
//                        JsObject jsObject = null;
//                        if (ModelUtils.THIS.equals(nodeName.get(0).getName())) { //NOI18N
//                            jsObject = resolveThis(modelBuilder.getCurrentObject());
//                            for (int i = 1; jsObject != null && i < nodeName.size(); i++ ) {
//                                jsObject = jsObject.getProperty(nodeName.get(i).getName());
//                            }
//                        } else {
//                            jsObject = ModelUtils.getJsObject(modelBuilder, nodeName, true);
//                        }
//                        if (jsObject != null) {
//                            Identifier name = nodeName.get(nodeName.size() - 1);
//                            DeclarationScopeImpl ds = modelBuilder.getCurrentDeclarationScope();
//                            String referenceName = reference.getIdent().getName();
//                            JsObject originalFnc = ds.getProperty(referenceName);
//                            while (originalFnc != null && !(originalFnc instanceof JsFunction)) {
//                                if (ds.getParentScope() != null) {
//                                    ds = (DeclarationScopeImpl)ds.getParentScope();
//                                    originalFnc = ds.getProperty(referenceName);
//                                } else {
//                                    originalFnc = null;
//                                }
//                            }
//                            if (originalFnc != null && originalFnc instanceof JsFunction) {
//                                //property contains the definition of the function
//                                JsObject newRef = new JsFunctionReference(jsObject.getParent(), name, (JsFunction)originalFnc, true, jsObject.getModifiers());
//                                jsObject.getParent().addProperty(jsObject.getName(), newRef);
//                                for (Occurrence occurence : jsObject.getOccurrences()) {
//                                    newRef.addOccurrence(occurence.getOffsetRange());
//                                }
//                                if (originalFnc instanceof JsFunctionImpl) {
////                                    ((JsFunctionImpl)originalFnc).setAnonymous(true);
//                                    JsObject parent = jsObject.getParent();
//                                    if (ModelUtils.PROTOTYPE.equals(parent.getName())) {
//                                        parent = parent.getParent();
//                                    }
//                                    if (parent != null) {
//                                        Collection<JsObject> propertiesCopy = new ArrayList(originalFnc.getProperties().values());
//                                        for (JsObject property : propertiesCopy) {
//                                            if (!property.getModifiers().contains(Modifier.PRIVATE)) {
//                                                ModelUtils.moveProperty(parent, property);
//                                            }
//                                        }
//                                    }
//                                }
//
//                            }
//
//                        }
//                    }
//                } else {
//                    addToPath(referenceNode);
//                    reference.accept(this);
//                    removeFromPathTheLast();
//                }
//            }
//            return null;
//        }
//        return super.enter(referenceNode);
//    }
//
    @Override
    public boolean enterReturnNode(ReturnNode returnNode) {
        Node expression = returnNode.getExpression();
        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, expression);
        if (expression == null) {
            types.add(new TypeUsage(Type.UNDEFINED, returnNode.getStart(), true));
        } else {
            if (expression instanceof IdentNode) {
                addOccurence((IdentNode)expression, false);
            }
            if(types.isEmpty()) {
               types.add(new TypeUsage(Type.UNRESOLVED, returnNode.getStart(), true));
            }
        }
        JsFunctionImpl function = modelBuilder.getCurrentDeclarationFunction();
        function.addReturnType(types);
        return super.enterReturnNode(returnNode);
    }

    @Override
    public boolean enterTernaryNode(TernaryNode ternaryNode) {
        if (ternaryNode.getTest() instanceof IdentNode) {
            addOccurence((IdentNode)ternaryNode.getTest(), false);
        }
        if (ternaryNode.getTrueExpression().getExpression() instanceof IdentNode) {
            addOccurence((IdentNode)ternaryNode.getTrueExpression().getExpression(), false);
        }
        if (ternaryNode.getFalseExpression().getExpression() instanceof IdentNode) {
            addOccurence((IdentNode)ternaryNode.getFalseExpression().getExpression(), false);
        }
        return super.enterTernaryNode(ternaryNode);
    }

    @Override
    public boolean enterUnaryNode(UnaryNode unaryNode) {
        if (unaryNode.getExpression() instanceof IdentNode) {
            addOccurence((IdentNode) unaryNode.getExpression(), false);
        }
        return super.enterUnaryNode(unaryNode);
    }

    // Track objects pushed to ModelBuilder from VarNode handling. objects are
    // only conditionally pushed enterVarNode and thus leaveVarNode must only
    // pop that state if it came from enterVarNode. There should be a better
    // solution, but should be ok in the interim
    private final Map<JsObject, VarNode> varNodeScopes = new IdentityHashMap<>();

    @Override
    public boolean enterVarNode(VarNode varNode) {
        Node init = varNode.getInit();
        FunctionNode rNode = null;
        if (init instanceof FunctionNode) {
            rNode = (FunctionNode)init;
        } else if (init instanceof BinaryNode) {
            // this should handle cases like
            // var prom  = another.prom = function prom() {}
            BinaryNode bNode = (BinaryNode)init;
            while (bNode.rhs() instanceof BinaryNode ) {
                bNode = (BinaryNode)bNode.rhs();
            }
            if (bNode.rhs() instanceof FunctionNode) {
                 rNode = (FunctionNode) bNode.rhs();
            }
        } else if (init instanceof UnaryNode && ((UnaryNode)init).getExpression() instanceof CallNode
                    && ((CallNode)((UnaryNode)init).getExpression()).getFunction() instanceof FunctionNode) {
            rNode = (FunctionNode)((CallNode)((UnaryNode)init).getExpression()).getFunction();
//            Identifier varName = new Identifier(varNode.getName().getName(), getOffsetRange(varNode.getName()));
//            OffsetRange range = varNode.getInit() instanceof ObjectNode ? new OffsetRange(varNode.getName().getStart(), ((ObjectNode)varNode.getInit()).getFinish())
//                    : varName.getOffsetRange();
//            JsObjectImpl parentFn = modelBuilder.getCurrentDeclarationFunction();
//            JsObject variable = handleArrayCreation(varNode.getInit(), parentFn, varName);
//            if (variable == null) {
//                JsObjectImpl newObject = new JsObjectImpl(parentFn, varName, range, parserResult.getSnapshot().getMimeType(), null);
//                variable = newObject;
//            }
//            variable.addOccurrence(varName.getOffsetRange());
//            JsObject property = parentFn.getProperty(varName.getName());
//            parentFn.addProperty(varName.getName(), variable);
//            variable.addProperty(property.getName(), property);
//            Collection<TypeUsage> returns = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, init);
//            for (TypeUsage type : returns) {
//                variable.addAssignment(type, init.getStart());
//            }
//            if (rNode.isNamedFunctionExpression() && rNode.getName().equals(varName.getName())) {
//                // the name of function is the same as the variable
//                // var MyLib = new function MyLib() {};
//                ModelUtils.copyOccurrences(property, variable);
//            }

        }
        if (!(init instanceof ObjectNode || rNode != null
                || init instanceof LiteralNode.ArrayLiteralNode
                || init instanceof ClassNode
                || varNode.isExport())) {
            JsObject parent = modelBuilder.getCurrentObject();
            //parent = canBeSingletonPattern(1) ? resolveThis(parent) : parent;
            if (parent instanceof CatchBlockImpl) {
                parent = parent.getParent();
            }
            while (parent instanceof JsWith) {
                parent = parent.getParent();
            }
            JsObjectImpl variable = (JsObjectImpl)parent.getProperty(varNode.getName().getName());
            Identifier name = ModelElementFactory.create(parserResult, varNode.getName());
            if (name != null) {
                if (variable == null) {
                    // variable si not defined, so it has to be from global scope
                    // or from a code structure like for cycle
                    // or it can be from parameter block

                    Node lastVisited = getPreviousFromPath(1);
                    if (parent instanceof JsFunctionImpl && lastVisited instanceof Block && ((Block)lastVisited).isParameterBlock()) {
                        // it's a parameter definition
                        variable = new ParameterObject(parent, name, parent.getMimeType(), parent.getSourceLabel());
                        ((JsFunctionImpl)parent).addParameter(variable);
                    } else {
                        variable = new JsObjectImpl(parent, name, name.getOffsetRange(),
                            true, parserResult.getSnapshot().getMimeType(), null);
                        parent.addProperty(name.getName(), variable);
                    }
                    if (parent.getJSKind() != JsElement.Kind.FILE) {
                        variable.getModifiers().remove(Modifier.PUBLIC);
                        variable.getModifiers().add(Modifier.PRIVATE);
                    }
                    variable.addOccurrence(name.getOffsetRange());
                } else if (!variable.isDeclared()){
                    // the variable was probably created as temporary before, now we
                    // need to replace it with the real one
                    JsObjectImpl newVariable = new JsObjectImpl(parent, name, name.getOffsetRange(),
                            true, parserResult.getSnapshot().getMimeType(), null);
                    newVariable.addOccurrence(name.getOffsetRange());
                    for(String propertyName: variable.getProperties().keySet()) {
                        JsObject property = variable.getProperty(propertyName);
                        if (property instanceof JsObjectImpl) {
                            ((JsObjectImpl)property).setParent(newVariable);
                        }
                        newVariable.addProperty(propertyName, property);
                    }
                    if (parent.getJSKind() != JsElement.Kind.FILE) {
                        newVariable.getModifiers().remove(Modifier.PUBLIC);
                        newVariable.getModifiers().add(Modifier.PRIVATE);
                    }
                    for(TypeUsage type : variable.getAssignments()) {
                        newVariable.addAssignment(type, type.getOffset());
                    }
                    for(Occurrence occurrence: variable.getOccurrences()){
                        newVariable.addOccurrence(occurrence.getOffsetRange());
                    }
                    parent.addProperty(name.getName(), newVariable);
                    variable = newVariable;
                }
                JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
                variable.setDeprecated(docHolder.isDeprecated(varNode));
                variable.setDocumentation(docHolder.getDocumentation(varNode));
                if (docHolder.isConstant(varNode)) {
                    variable.setJsKind(JsElement.Kind.CONSTANT);
                }
                if (init instanceof IdentNode) {
                    IdentNode iNode = (IdentNode)init;
                    if (!iNode.getName().equals(variable.getName())) {
                        addOccurrence((IdentNode)init, variable.getName());
                    } else {
                        // the name of variable is the same as already existing function or var or parameter
                        JsFunctionImpl currentFunction = modelBuilder.getCurrentDeclarationFunction();
                        if (currentFunction != null && currentFunction.getParameter(variable.getName()) != null) {
                            // it's a parameter
                            addOccurrence((IdentNode)init, variable.getName());
                        } else {
                            variable.addOccurrence(getOffsetRange(iNode));
                        }
                    }

                }
                varNodeScopes.put(variable, varNode);
                modelBuilder.setCurrentObject(variable);
                Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, init);
                if (modelBuilder.getCurrentWith() != null) {
                    ((JsWithObjectImpl)modelBuilder.getCurrentWith()).addObjectWithAssignment(variable);
                }
                for (TypeUsage type : types) {
                    variable.addAssignment(type, varNode.getName().getFinish());
                }
                List<Type> returnTypes = docHolder.getReturnType(varNode);
                if (returnTypes != null && !returnTypes.isEmpty()) {
                    for (Type type : returnTypes) {
                        variable.addAssignment(new TypeUsage(type.getType(), type.getOffset(), true), varNode.getName().getFinish());
                    }
                }
                if (varNode.isConst()) {
                    variable.setJsKind(JsElement.Kind.CONSTANT);
                }
            }
        } else if(init instanceof ObjectNode && !varNode.isExport()) {
            JsObjectImpl function = modelBuilder.getCurrentDeclarationFunction();
            Identifier name = ModelElementFactory.create(parserResult, varNode.getName());
            if (name != null) {
                JsObjectImpl variable = (JsObjectImpl)function.getProperty(name.getName());
                if (variable != null) {
                    variable.setDeclared(true);
                } else {
                    List<Identifier> fqName = getName(varNode);
                    variable = ModelElementFactory.create(parserResult, (ObjectNode)varNode.getInit(), fqName, modelBuilder, true);
                }
                if (variable != null) {
                    variable.setJsKind(JsElement.Kind.OBJECT_LITERAL);
                    varNodeScopes.put(variable, varNode);
                    modelBuilder.setCurrentObject(variable);
                }
            }
        } else if(init instanceof ObjectNode && varNode.isExport()) {
            // we are expecting here that the var node is artificial and is created due to: export default {}
            return false;
        } else if (init instanceof ClassNode) {
            ClassNode cNode = (ClassNode) init;
            // process decorators
            List<Expression> decorators = cNode.getDecorators();
            if (decorators != null && !decorators.isEmpty()) {
                for (Expression decorator : decorators) {
                    if (decorator instanceof IdentNode) {
                        // in such case, this is probaly a function
                        addOccurence((IdentNode)decorator, false, true);
                    } else {
                        decorator.accept(this);
                    }
                }
            }
        }
        return super.enterVarNode(varNode);
    }

    @Override
    public Node leaveVarNode(VarNode varNode) {
        Node init = varNode.getInit();
        FunctionNode rNode = null;
        if (init instanceof BinaryNode) {
            // this should handle cases like
            // var prom  = another.prom = function prom() {}
            BinaryNode bNode = (BinaryNode)init;
            while (bNode.rhs() instanceof BinaryNode ) {
                bNode = (BinaryNode)bNode.rhs();
            }
            if (bNode.rhs() instanceof FunctionNode) {
                // this should handle cases like
                // var prom  = another.prom = function prom() {}
                rNode = (FunctionNode) bNode.rhs();
                List<Identifier> name = getNodeName(bNode.lhs(), parserResult);
                if (name != null && !name.isEmpty()) {
                    boolean isPriviliged = name.get(0).getName().equals(ModelUtils.THIS);
                    JsObject parent =  isPriviliged ? resolveThis(modelBuilder.getCurrentObject()) : modelBuilder.getCurrentObject();
                    for (int i = isPriviliged ? 1 : 0; parent != null && i < name.size(); i++) {
                        parent = parent.getProperty(name.get(i).getName());
                    }
                    if (parent instanceof JsFunction) {
                        Identifier propertyName = create(parserResult, varNode.getName());
                        Set<Modifier> modifiers;
                        if (isPriviliged) {
                            modifiers = EnumSet.of(Modifier.PROTECTED);
                        } else if (modelBuilder.getCurrentDeclarationFunction().getJSKind() == JsElement.Kind.FILE) {
                            modifiers = EnumSet.of(Modifier.PUBLIC);
                        } else {
                            modifiers = EnumSet.of(Modifier.PRIVATE);
                        }
                        JsObject property = new JsFunctionReference(modelBuilder.getCurrentObject(), propertyName, (JsFunction)parent, true, modifiers);
                        modelBuilder.getCurrentObject().addProperty(propertyName.getName(), property);
                        property.addOccurrence(propertyName.getOffsetRange());
                    }
                }

            }
//            if (bNode.rhs() instanceof ReferenceNode /*&& bNode.tokenType() == TokenType.ASSIGN*/) {
//                 init = (ReferenceNode) bNode.rhs();
//            }
        } else if (init instanceof FunctionNode) {
            rNode = (FunctionNode)init;
        } else if (init instanceof UnaryNode && ((UnaryNode)init).getExpression() instanceof CallNode
                    && ((CallNode)((UnaryNode)init).getExpression()).getFunction() instanceof FunctionNode) {
            rNode = (FunctionNode)((CallNode)((UnaryNode)init).getExpression()).getFunction();
        }
        if (!(rNode != null || init instanceof LiteralNode.ArrayLiteralNode)
                // XXX can we avoid creation of object ?
                && ModelElementFactory.create(parserResult, varNode.getName()) != null) {
            JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
            List<DocParameter> properties = docHolder.getProperties(varNode);
            for (DocParameter docProperty : properties) {
                String propertyName = docProperty.getParamName().getName();
                String names[];
                int delta = 0;
                if (propertyName.indexOf('.') > 0) {
                    names = propertyName.split("\\.");
                } else {
                    names = new String[]{propertyName};
                }
                JsObject parent = modelBuilder.getCurrentObject();
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];
                    JsObject property = parent.getProperty(name);
                    int startOffset = docProperty.getParamName().getOffsetRange().getStart() + delta;
                    int endOffset = startOffset + name.length();
                    OffsetRange offsetRange = new OffsetRange(startOffset, endOffset);
                    if (property == null) {
                        Identifier iden = new Identifier(name, offsetRange);
                        property = new JsObjectImpl(parent, iden, offsetRange, true, JsTokenId.JAVASCRIPT_MIME_TYPE, null);
                        parent.addProperty(name, property);
                    }
                    property.addOccurrence(offsetRange);
                     if (i == names.length - 1) {
                        for (Type type : docProperty.getParamTypes()) {
                            property.addAssignment(new TypeUsage(type.getType(), endOffset), endOffset);
                        }
                    }
                    delta = delta + name.length() + 1;
                    parent = property;

                }


            }
        }
        if (varNodeScopes.containsKey(modelBuilder.getCurrentObject())) {
            varNodeScopes.remove(modelBuilder.getCurrentObject());
            modelBuilder.reset();
        }
        return super.leaveVarNode(varNode);
    }

    @Override
    public boolean enterWithNode(WithNode withNode) {
        JsObjectImpl currentObject = modelBuilder.getCurrentObject();
        Collection<TypeUsage> types = ModelUtils.resolveSemiTypeOfExpression(modelBuilder, withNode.getExpression());
        JsWithObjectImpl withObject = new JsWithObjectImpl(currentObject, modelBuilder.getUnigueNameForWithObject(), types, new OffsetRange(withNode.getStart(), withNode.getFinish()),
                        new OffsetRange(withNode.getExpression().getStart(), withNode.getExpression().getFinish()), modelBuilder.getCurrentWith(), parserResult.getSnapshot().getMimeType(), null);
        currentObject.addProperty(withObject.getName(), withObject);
//        withNode.getExpression().accept(this); // expression should be visted when the with object is the current object.
        modelBuilder.setCurrentObject(withObject);
        withNode.getBody().accept(this);
        modelBuilder.reset();
        return false;
    }

//--------------------------------End of visit methods--------------------------------------

    public Map<FunctionInterceptor, Collection<FunctionCall>> getCallsForProcessing() {
        return functionCalls;
    }

    private boolean fillName(AccessNode node, List<String> result) {
        List<Identifier> fqn = getName(node);
        if (fqn != null) {
            for (int i = fqn.size() - 1; i >= 0; i--) {
                result.add(0, fqn.get(i).getName());
            }
        }

        JsObject current = modelBuilder.getCurrentObject();
        while (current != null && current.getDeclarationName() != null) {
            if (current != modelBuilder.getGlobal()) {
                result.add(0, current.getDeclarationName().getName());
            }
            current = current.getParent();
        }
        return true;
    }

    private boolean fillName(IndexNode node, List<String> result) {
        Node index = node.getIndex();
        Node base = node.getBase();
        if (index instanceof LiteralNode && base instanceof AccessNode) {
            LiteralNode literal = (LiteralNode) index;
            if (literal.isString()) {
                result.add(0, literal.getString());
                List<Identifier> fqn = getName((AccessNode) base);
                for (int i = fqn.size() - 1; i >= 0; i--) {
                    result.add(0, fqn.get(i).getName());
                }
                return true;
            }
        }
        return false;
    }

    private List<Identifier> getName(PropertyNode propertyNode) {
        List<Identifier> name = new ArrayList<>(1);
        if (propertyNode.getGetter() != null || propertyNode.getSetter() != null) {
            // check whether this is not defining getter or setter of a property.
            Node previousNode = getPreviousFromPath(1);
            if (previousNode instanceof FunctionNode) {
                FunctionNode fNode = (FunctionNode)previousNode;
                String fName = fNode.getIdent().getName();
                if (fName.startsWith("get ") || fName.startsWith("set ")) { //NOI18N
                    name.add(new Identifier(fName,
                        new OffsetRange(fNode.getIdent().getStart(), fNode.getIdent().getFinish())));
                    return name;
                }
            }
        }
        return getName(propertyNode, parserResult);
    }

    private static List<Identifier> getName(PropertyNode propertyNode, ParserResult parserResult) {
        List<Identifier> name = new ArrayList<>(1);
        if (propertyNode.getKey() instanceof IdentNode) {
            IdentNode ident = (IdentNode) propertyNode.getKey();
            name.add(new Identifier(ident.getName(), getOffsetRange(ident)));
        } else if (propertyNode.getKey() instanceof LiteralNode){
            LiteralNode lNode = (LiteralNode)propertyNode.getKey();
            name.add(new Identifier(lNode.getString(),
                    new OffsetRange(lNode.getStart(), lNode.getFinish())));
        }
        return name;
    }

    private static List<Identifier> getName(VarNode varNode) {
        List<Identifier> name = new ArrayList<>();
        name.add(new Identifier(varNode.getName().getName(),
                new OffsetRange(varNode.getName().getStart(), varNode.getName().getFinish())));
        return name;
    }

    private static List<Identifier> getName(BinaryNode binaryNode, ParserResult parserResult) {
        List<Identifier> name = new ArrayList<>();
        Node lhs = binaryNode.lhs();
        if (lhs instanceof AccessNode) {
            name = getName((AccessNode)lhs);
        } else if (lhs instanceof IdentNode) {
            IdentNode ident = (IdentNode) lhs;
            name.add(new Identifier(ident.getName(), getOffsetRange(ident)));
        } else if (lhs instanceof IndexNode) {
            IndexNode indexNode = (IndexNode)lhs;
            if (indexNode.getBase() instanceof AccessNode) {
                List<Identifier> aName = getName((AccessNode)indexNode.getBase());
                if (aName != null) {
                    name.addAll(getName((AccessNode)indexNode.getBase()));
                }
                else {
                    return null;
                }
            } else if (indexNode.getBase() instanceof IdentNode) {
                name.add(create(parserResult, (IdentNode)indexNode.getBase()));
            }
            if (indexNode.getIndex() instanceof LiteralNode) {
                LiteralNode lNode = (LiteralNode)indexNode.getIndex();
                name.add(new Identifier(lNode.getPropertyName(),
                        new OffsetRange(lNode.getStart(), lNode.getFinish())));
            } else {
                // case not covered here:
                // - test[variable]
                // - test[obj.variable]
                // - test[function()]
                // we don't know the name in these cases, thus we need to return
                // null here
                return null;
            }
        }
        return name;
    }

    private static List<Identifier> getName(AccessNode aNode) {
        List<Identifier> name = new ArrayList<>();
        name.add(new Identifier(aNode.getProperty(),
                new OffsetRange(aNode.getFinish() - aNode.getProperty().length(), aNode.getFinish())));
        Node base = aNode.getBase();
        while (base instanceof AccessNode || base instanceof CallNode || base instanceof IndexNode) {
            if (base instanceof CallNode) {
                CallNode cNode = (CallNode)base;
                base = cNode.getFunction();
            } else if (base instanceof IndexNode) {
                IndexNode iNode = (IndexNode) base;
                if (iNode.getIndex() instanceof LiteralNode) {
                    LiteralNode lNode = (LiteralNode)iNode.getIndex();
                    if (lNode.isString()) {
                        name.add(new Identifier(lNode.getPropertyName(),
                                new OffsetRange(lNode.getStart(), lNode.getFinish())));
                    }
                } else {
                    return null;
                }
                base = iNode.getBase();
            }
            if (base instanceof AccessNode) {
                AccessNode aaNode = (AccessNode)base;
                base = aaNode.getBase();
                name.add(new Identifier(aaNode.getProperty(),
                        new OffsetRange(aaNode.getFinish() - aaNode.getProperty().length(), aaNode.getFinish())));
            }
        }
        Identifier baseIdent = null;
        if (base instanceof IdentNode) {
            IdentNode ident = (IdentNode) base;
            baseIdent = new Identifier(ident.getName(), getOffsetRange(ident));
        } else if (base instanceof LiteralNode) {
            // we fake Number object to handel mark occurrences
            LiteralNode lNode= (LiteralNode) base;
            if (lNode.isNumeric()) {
                baseIdent = new Identifier("Number", OffsetRange.NONE); //NOI8N
            }
        }


        if (baseIdent != null) {
            name.add(baseIdent);
            Collections.reverse(name);
            return name;
        } else {
            return null;
        }
    }

    private JsObject createJsObject(AccessNode accessNode, ParserResult parserResult, ModelBuilder modelBuilder) {
        List<Identifier> fqn = getName(accessNode);
        if (fqn == null) {
            return null;
        }
        JsObject object = null;

        Identifier name = fqn.get(0);
        if (!ModelUtils.THIS.equals(fqn.get(0).getName())) {
            if (modelBuilder.getCurrentWith() == null) {
                DeclarationScopeImpl currentDS = modelBuilder.getCurrentDeclarationScope();
                JsObject variable = ModelUtils.getScopeVariable(currentDS, name.getName());
                if (variable != null) {
                    if (variable instanceof ParameterObject || variable.getModifiers().contains(Modifier.PRIVATE)) {
                        object = (JsObjectImpl) variable;
                    } else {
                        DeclarationScope variableDS = ModelUtils.getDeclarationScope(variable);
                        if (!variableDS.equals(currentDS)) {
                            object = (JsObjectImpl) variable;
                        } else if (currentDS.getProperty(name.getName()) != null) {
                            Node lastNode = getPreviousFromPath(2);
                            if (lastNode instanceof BinaryNode) {
                                BinaryNode bNode = (BinaryNode) lastNode;
                                if (bNode.lhs().equals(accessNode)) {
                                    object = (JsObjectImpl) variable;
                                }
                            }
                        }
                    }
                }
                if (object == null) {
                    JsObject global = modelBuilder.getGlobal();
                    object = (JsObjectImpl)global.getProperty(name.getName());
                    if (object == null) {
                        object = new JsObjectImpl(global, name, name.getOffsetRange(), false, global.getMimeType(), global.getSourceLabel());
                        global.addProperty(name.getName(), object);
                    }
                }
            } else {
                JsObject withObject = modelBuilder.getCurrentWith();
                object = (JsObjectImpl)withObject.getProperty(name.getName());
                if (object == null) {
                    object = new JsObjectImpl(withObject, name, name.getOffsetRange(), false, parserResult.getSnapshot().getMimeType(), null);
                    withObject.addProperty(name.getName(), object);
                }
            }
            object.addOccurrence(name.getOffsetRange());
        } else {
            JsObject current = modelBuilder.getCurrentObject();
            object = (JsObjectImpl)resolveThis(current);
            if (object != null) {
                // find out, whether is not defined in prototype
                if (object.getProperty(fqn.get(1).getName()) == null) {
                    JsObject prototype = object.getProperty(ModelUtils.PROTOTYPE);
                    if (prototype != null && prototype.getProperty(fqn.get(1).getName()) != null) {
                        object = prototype;
                    }
                }
            }
            if (object != null && fqn.size() == 2) {
                // try to handle case
                // function MyF() {
                //      this.f1 = f1;
                //      function f1() {};
                // }
                // in such case the name after this has to be equal to the declared function.
                // -> in the model, just change  the f1 from private to privilaged.
                String lastName = fqn.get(1).getName();
                JsObjectImpl property = (JsObjectImpl)object.getProperty(lastName);
                Node lastVisited = getPreviousFromPath(2);
                if (property != null && lastName.equals(property.getName()) && (property.getModifiers().contains(Modifier.PRIVATE) && property.getModifiers().size() == 1)
                        && !(lastVisited instanceof CallNode)) {
                    // if there is CallNode, then it like this.xxx() -> don't change modifiers
                    property.getModifiers().remove(Modifier.PRIVATE);
                    property.getModifiers().add(Modifier.PROTECTED);
                }
            }
        }

        if (object != null) {
            JsObjectImpl property = null;
            for (int i = 1; i < fqn.size(); i++) {
                property = (JsObjectImpl)object.getProperty(fqn.get(i).getName());
                if (property != null) {
                    object = property;
                }
            }
            int pathSize = getPath().size();
            Node lastVisited =  pathSize > 1 ? getPath().get(pathSize - 2) : getPath().get(0);
            boolean onLeftSite = false;
            if (lastVisited instanceof BinaryNode) {
                BinaryNode bNode = (BinaryNode)lastVisited;
                onLeftSite = bNode.tokenType() == TokenType.ASSIGN && bNode.lhs().equals(accessNode);
            }
            String propertyName = accessNode.getProperty();
            // there is a problem in the parser. When there is a line comment after access node, then the finish of access node is finish of the line comment
            int propertyOffsetStart = accessNode.getBase().getFinish() + 1;
            int propertyOffsetEnd = propertyOffsetStart + propertyName.length();
            if (property != null) {
                OffsetRange range = new OffsetRange(propertyOffsetStart, propertyOffsetEnd);
                if(onLeftSite && !property.isDeclared()) {
                    property.setDeclared(true);
                    property.setDeclarationName(new Identifier(property.getName(), range));
                }
                property.addOccurrence(range);
            } else {
                name = ModelElementFactory.create(parserResult, propertyName, propertyOffsetStart, propertyOffsetEnd);
                if (name != null) {
                    if (pathSize > 1 && getPath().get(pathSize - 2) instanceof CallNode) {
                        CallNode cNode = (CallNode)getPath().get(pathSize - 2);
                        if (!cNode.getArgs().contains(accessNode)) {
                            property = ModelElementFactory.createVirtualFunction(parserResult, object, name, cNode.getArgs().size());
                            //property.addOccurrence(name.getOffsetRange());
                        } else {
                            property = new JsObjectImpl(object, name, name.getOffsetRange(), onLeftSite, parserResult.getSnapshot().getMimeType(), null);
                            property.addOccurrence(name.getOffsetRange());
                        }
                    } else {
                        boolean setDocumentation = false;
                        if (isPriviliged(accessNode) && getPath().size() > 1 && (getPreviousFromPath(2) instanceof ExpressionStatement || getPreviousFromPath(1) instanceof ExpressionStatement)) {
                            // google style declaration of properties:  this.buildingID;
                            onLeftSite = true;
                            setDocumentation = true;
                        }
                        property = new JsObjectImpl(object, name, name.getOffsetRange(), onLeftSite, parserResult.getSnapshot().getMimeType(), null);
                        property.addOccurrence(name.getOffsetRange());
                        if (setDocumentation) {
                            JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
                            if (docHolder != null) {
                                property.setDocumentation(docHolder.getDocumentation(accessNode));
                                property.setDeprecated(docHolder.isDeprecated(accessNode));
                                List<Type> returnTypes = docHolder.getReturnType(accessNode);
                                if (!returnTypes.isEmpty()) {
                                    for (Type type : returnTypes) {
                                        property.addAssignment(new TypeUsage(type.getType(), type.getOffset(), true), accessNode.getFinish());
                                    }
                                }
                                setModifiersFromDoc(property, docHolder.getModifiers(accessNode));
                            }
                        }
                    }
                    object.addProperty(name.getName(), property);
                    object = property;
                }
            }
        }
        return object;
    }

    /**
     * Gets the node name if it has any (case of AccessNode, BinaryNode, VarNode, PropertyNode).
     *
     * @param node examined node for getting its name
     * @return name of the node if it supports it
     */
    public static List<Identifier> getNodeName(Node node, ParserResult parserResult) {
        if (node instanceof AccessNode) {
            return getName((AccessNode) node);
        } else if (node instanceof BinaryNode) {
            return getName((BinaryNode) node, parserResult);
        } else if (node instanceof VarNode) {
            return getName((VarNode) node);
        } else if (node instanceof PropertyNode) {
            return getName((PropertyNode) node, parserResult);
        } else if (node instanceof IdentNode) {
            IdentNode ident = ((IdentNode) node);
            return Arrays.<Identifier>asList(new Identifier(
                    ident.getName(), getOffsetRange(ident)));
        } else if (node instanceof FunctionNode) {
            if (((FunctionNode) node).getKind() == FunctionNode.Kind.SCRIPT) {
                return Collections.<Identifier>emptyList();
            }
            IdentNode ident = ((FunctionNode) node).getIdent();
            return Arrays.<Identifier>asList(new Identifier(
                    ident.getName(), getOffsetRange(ident)));
        } else {
            return Collections.<Identifier>emptyList();
        }
    }
//
////    private Variable findVarWithName(final Scope scope, final String name) {
////        Variable result = null;
////        Collection<Variable> variables = ScopeImpl.filter(scope.getElements(), new ScopeImpl.ElementFilter() {
////
////            @Override
////            public boolean isAccepted(ModelElement element) {
////                return element.getJSKind().equals(JsElement.Kind.VARIABLE)
////                        && element.getName().equals(name);
////            }
////        });
////
////        if (!variables.isEmpty()) {
////            result = variables.iterator().next();
////        } else {
////            if (!(scope instanceof FileScope)) {
////                result = findVarWithName((Scope)scope.getInElement(), name);
////            }
////        }
////
////        return result;
////    }
////
////    private Field findFieldWithName(FunctionScope function, final String name) {
////        Field result = null;
////        Collection<? extends Field> fields = function.getFields();
////        result = ModelUtils.getFirst(ModelUtils.getFirst(fields, name));
////        if (result == null && function.getInElement() instanceof FunctionScope) {
////            FunctionScope parent = (FunctionScope)function.getInElement();
////            fields = parent.getFields();
////            result = ModelUtils.getFirst(ModelUtils.getFirst(fields, name));
////        }
////        return result;
////    }
//
    private boolean isInPropertyNode() {
        boolean inFunction = false;
        for (int i = getPath().size() - 1; i > 0 ; i--) {
            final Node node = getPath().get(i);
            if(node instanceof FunctionNode) {
                if (!inFunction) {
                    inFunction = true;
                } else {
                    return false;
                }
            } else if (node instanceof PropertyNode) {
                return true;
            }
        }
        return false;
    }

    private void addOccurence(IdentNode iNode, boolean leftSite) {
        addOccurence(iNode, leftSite, false);
    }

    private void addOccurence(IdentNode iNode, boolean leftSite, boolean isFunction) {
        if (!iNode.isDestructuredParameter()) {
            // skip names of destructured param (it's syntetic)
            addOccurrence(iNode.getName(), getOffsetRange(iNode), leftSite, isFunction);
        }
    }

    private void addOccurrence(String name, OffsetRange range, boolean leftSite, boolean isFunction) {
        if (ModelUtils.THIS.equals(name) || Type.UNDEFINED.equals(name)) {
            // don't process this node and undefined
            return;
        }
        occurrenceBuilder.addOccurrence(name, range, modelBuilder.getCurrentDeclarationScope(), modelBuilder.getCurrentObject(), modelBuilder.getCurrentWith(), isFunction, leftSite);
//        DeclarationScope scope = modelBuilder.getCurrentDeclarationScope();
//        JsObject property = null;
//        JsObject parameter = null;
//        JsObject parent = modelBuilder.getCurrentObject();
//        if (!(parent instanceof JsWith || (parent.getParent() != null && parent.getParent() instanceof JsWith))) {
//            while (scope != null && property == null && parameter == null) {
//                JsFunction function = (JsFunction)scope;
//                property = function.getProperty(name);
//                parameter = function.getParameter(name);
//                scope = scope.getParentScope();
//            }
//            if(parameter != null) {
//                if (property == null) {
//                    property = parameter;
//                } else {
//                    if(property.getJSKind() != JsElement.Kind.VARIABLE) {
//                        property = parameter;
//                    }
//                }
//            }
//        } else {
//            if (!(parent instanceof JsWith) && (parent.getParent() != null && parent.getParent() instanceof JsWith)) {
//                parent = parent.getParent();
//            }
//            property = parent.getProperty(name);
//        }
//
//        if (property != null) {
//
//            // occurence in the doc
//            addDocNameOccurence(((JsObjectImpl)property));
//            addDocTypesOccurence(((JsObjectImpl)property));
//
//            ((JsObjectImpl)property).addOccurrence(range);
//        } else {
//            // it's a new global variable?
//            Identifier nameIden = ModelElementFactory.create(parserResult, name, range.getStart(), range.getEnd());
//            if (nameIden != null) {
//                JsObjectImpl newObject;
//                if (!(parent instanceof JsWith)) {
//                        parent = modelBuilder.getGlobal();
//                }
//                if (!isFunction) {
//                    newObject = new JsObjectImpl(parent, nameIden, nameIden.getOffsetRange(),
//                            leftSite, parserResult.getSnapshot().getMimeType(), null);
//                } else {
//                    FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
//                    newObject = new JsFunctionImpl(fo, parent, nameIden, Collections.EMPTY_LIST,
//                            parserResult.getSnapshot().getMimeType(), null);
//                }
//                newObject.addOccurrence(nameIden.getOffsetRange());
//                parent.addProperty(nameIden.getName(), newObject);
//            }
//        }
    }

    /**
     * Handles adding occurrences in expression like var xxx = xxx or this.xxx = xxx;
     * @param iNode
     * @param name
     */
    private void addOccurrence(IdentNode iNode, String name) {
        String valueName = iNode.getName();
        if (!name.equals(valueName)) {
            addOccurence(iNode, false);
        } else {
            DeclarationScope scope = modelBuilder.getCurrentDeclarationScope();
            JsObject parameter = null;
            if (scope instanceof JsFunction) {
                JsFunction function = (JsFunction)scope;
                parameter = function.getParameter(iNode.getName());
            }
            if (parameter != null) {
                parameter.addOccurrence(getOffsetRange(iNode));
            } else {
                boolean found = false;
                JsObject jsProperty = ((JsObject)scope).getProperty(valueName);
                if (jsProperty != null && jsProperty.isDeclared()) {
                    found = true;
                    jsProperty.addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
                } else {
                    JsObject jsObject = ModelUtils.getScopeVariable(scope.getParentScope(), valueName);
                    if (jsObject != null) {
                        jsObject.addOccurrence(new OffsetRange(iNode.getStart(), iNode.getFinish()));
                        found = true;
                    }
                }
                if (!found) {
                    // new global var?
                    Identifier nameI = ModelElementFactory.create(parserResult, iNode);
                    if (nameI != null) {
                        JsObjectImpl newObject;
                        newObject = new JsObjectImpl(modelBuilder.getGlobal(), nameI, nameI.getOffsetRange(),
                                false, parserResult.getSnapshot().getMimeType(), null);
                        newObject.addOccurrence(nameI.getOffsetRange());
                        modelBuilder.getGlobal().addProperty(nameI.getName(), newObject);
                    }
                }
            }
        }
    }

    private void addDocNameOccurence(JsObjectImpl jsObject) {
        JsDocumentationHolder holder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        JsComment comment = holder.getCommentForOffset(jsObject.getOffset(), holder.getCommentBlocks());
        if (comment != null) {
            for (DocParameter docParameter : comment.getParameters()) {
                Identifier paramName = docParameter.getParamName();
                String name = (docParameter.getParamName() == null) ? "" : docParameter.getParamName().getName(); //NOI18N
                if (name.equals(jsObject.getName())) {
                    jsObject.addOccurrence(paramName.getOffsetRange());
                }
            }
        }
    }

    private boolean belongsTo(JsObject parent, String property) {
        boolean result = parent.getProperty(property) != null;
        if (!result && parent instanceof JsFunction) {
            result = ((JsFunction)parent).getParameter(property) != null;
        }

        return result;
    }

    private JsObject processLhs(Identifier name, JsObject parent, boolean lastOnLeft) {
        JsObject lObject = null;
        if (name != null) {
            if (ModelUtils.THIS.equals(name.getName())) {
                return null;
            }
            final String newVarName = name.getName();
            boolean hasParent = belongsTo(parent, newVarName);
            boolean hasGrandParent = parent.getJSKind() == JsElement.Kind.METHOD && belongsTo(parent.getParent(), newVarName);
            if (!hasParent && !hasGrandParent && modelBuilder.getGlobal().getProperty(newVarName) == null) {
                addOccurrence(name.getName(), name.getOffsetRange(), lastOnLeft, false);
            } else {
                if (hasParent) {
                    lObject = parent.getProperty(newVarName);
                    if(lObject == null && parent instanceof JsFunction) {
                        lObject = ((JsFunction)parent).getParameter(newVarName);
                    }
                } else if (hasGrandParent) {
                    lObject = parent.getParent().getProperty(newVarName);
                    if(lObject == null && parent.getParent() instanceof JsFunction) {
                        lObject = ((JsFunction)parent.getParent()).getParameter(newVarName);
                    }
                }
                if (lObject != null) {
                    ((JsObjectImpl)lObject).addOccurrence(name.getOffsetRange());
                } else {
                    addOccurrence(name.getName(), name.getOffsetRange(), lastOnLeft, false);
                }
            }
//            lObject = (JsObjectImpl)parent.getProperty(newVarName);
            if (lObject == null) {
                // it's not a property of the parent -> try to find in different context
                // FIXME why is model visitor requesting model from PR
                Model model = Model.getModel(parserResult, false);
                Collection<? extends JsObject> variables = model.getVariables(name.getOffsetRange().getStart());
                for(JsObject variable : variables) {
                    if(variable.getName().equals(newVarName)) {
                        lObject = (JsObjectImpl)variable;
                        break;
                    }
                }
                if (lObject == null) {
                    // the object with the name wasn't find yet -> create in global scope
                    JsObject where = modelBuilder.getCurrentWith() == null ? model.getGlobalObject() : modelBuilder.getCurrentWith();
                    lObject = new JsObjectImpl( where, name,
                            name.getOffsetRange(), lastOnLeft, parserResult.getSnapshot().getMimeType(), null);
                    where.addProperty(name.getName(), lObject);
                }
            }
        }
        return lObject;
    }

    public static OffsetRange getOffsetRange(IdentNode node) {
        // because the truffle parser doesn't set correctly the finish offset, when there are comments after the indent node
        return new OffsetRange(node.getStart(), node.getStart() + node.getName().length());
    }

    public static OffsetRange getOffsetRange(Node node) {
        return new OffsetRange(node.getStart(), node.getFinish());
    }

    public static OffsetRange getOffsetRange(FunctionNode node) {
        return new OffsetRange(Token.descPosition(node.getFirstToken()),
                Token.descPosition(node.getLastToken()) + Token.descLength(node.getLastToken()));
    }

    // TODO move this method to the ModelUtils
    /**
     *
     * @param where the declaration context, where this is used
     * @return JsObject that should represent this.
     */
    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public JsObject resolveThis(JsObject where) {
        while(
                (! (where instanceof JsFunction && where.getJSKind() != JsElement.Kind.ARROW_FUNCTION))
                && where != null && where.getJSKind() != JsElement.Kind.CLASS
        ) {
            where = where.getParent();
        }

        JsElement.Kind whereKind = where.getJSKind();
        if (whereKind == JsElement.Kind.FILE) {
            // this is used in global context
            return where;
        }
        if (whereKind == JsElement.Kind.CLASS) {
            return where;
        }
        if (whereKind.isFunction() && where.getModifiers().contains(Modifier.PRIVATE)) {
            // the case where is defined private function in another function
            return where;
        }

        JsObject parent = where.getParent();

        if (parent == null) {
            return where;
        }
        JsElement.Kind parentKind = parent.getJSKind();
        if (parentKind == JsElement.Kind.FILE && !where.isAnonymous()) {
            // this is used in a function that is in the global context
            return where;
        }
        if (ModelUtils.PROTOTYPE.equals(parent.getName())) {
            // this is used in a function defined in prototype object
            return where.getParent().getParent();
        }
        if (whereKind == JsElement.Kind.CONSTRUCTOR) {
            if (parentKind == JsElement.Kind.CLASS) {
                return parent;
            } else {
                return where;
            }
        }
        if (whereKind.isFunction() && !where.getModifiers().contains(Modifier.PRIVATE) && !where.isAnonymous()) {
            // public or protected method
            if (parent.getJSKind() == JsElement.Kind.OBJECT_LITERAL) {
                if (Character.isUpperCase(where.getName().charAt(0))) {
                    return where;
                }
                if (Character.isUpperCase(parent.getName().charAt(0))) {
                    return parent;
                }
            } else {
                if (parent.isDeclared() || modelBuilder.getCurrentWith() != null) {
                    return parent;
                } else {
                    return where;
                }
            }
        }
        if (isInPropertyNode()) {
            // this is used in a method of an object -> this is the object
            return parent;
        }
//        if (where.isAnonymous()) {
//            JsObject result = resolveThisInSingletonPattern(where);
//            if (result != null) {
//                return result;
//            }
//        }
        return where;
    }

    private JsObject resolveThisInSingletonPattern(JsObject where) {
        int pathIndex = 1;
        Node lastNode = getPreviousFromPath(1);
        if (lastNode instanceof FunctionNode && !canBeSingletonPattern(pathIndex)) {
            pathIndex++;
        }
        while (pathIndex < getPath().size() && !(getPreviousFromPath(pathIndex) instanceof FunctionNode)) {
            pathIndex++;
        }
        // trying to find out that it corresponds with patter, where an object is defined via new function:
        // exp: this.pro = new function () { this.field = "";}
        if (canBeSingletonPattern(pathIndex)) {
            UnaryNode uNode = (UnaryNode) getPreviousFromPath(pathIndex + 2);
            if (uNode.tokenType() == TokenType.NEW) {

                String name = null;
                boolean simpleName = true;
                if (getPreviousFromPath(pathIndex + 3) instanceof BinaryNode) {
                    BinaryNode bNode = (BinaryNode) getPreviousFromPath(pathIndex + 3);
                    if (bNode.tokenType() == TokenType.ASSIGN) {
                        if (bNode.lhs() instanceof AccessNode) {
                            List<Identifier> identifier = getName((AccessNode) bNode.lhs());
                            if (identifier != null) {
                                if (!identifier.isEmpty() && ModelUtils.THIS.equals(identifier.get(0).getName())) {
                                    identifier.remove(0);
                                }
                                if (identifier.size() == 1) {
                                    name = identifier.get(0).getName();
                                } else {
                                    StringBuilder sb = new StringBuilder();
                                    for (Identifier part : identifier) {
                                        sb.append(part.getName()).append('.');
                                    }
                                    name = sb.toString().substring(0, sb.length() - 1);
                                    simpleName = false;
                                }
                            }
                        } else if (bNode.lhs() instanceof IdentNode) {
                            name = ((IdentNode) bNode.lhs()).getName();
                        }
                    }
                } else if (getPreviousFromPath(pathIndex + 3) instanceof VarNode) {
                    VarNode vNode = (VarNode)getPreviousFromPath(pathIndex + 3);
                    name = vNode.getName().getName();
                }

                JsObject parent = where.getParent() == null ? where : where.getParent();
                if (name != null) {
                    if (simpleName) {
                        parent = where;
                        while (parent != null && parent.getProperty(name) == null) {
                            parent = parent.getParent();
                        }
                        if (parent != null && parent.getProperty(name) != null) {
                            if (parent.getName().equals(name) && parent.getProperty(name).getJSKind().isFunction()) {
                                return parent;
                            }
                            return parent.getProperty(name);
                        }
                    } else {
                        JsObject property = ModelUtils.findJsObjectByName(ModelUtils.getGlobalObject(parent), name);
                        if (property != null) {
                            return property;
                        }
                    }

                }
            }
        }
        return null;
    }

    private boolean canBeSingletonPattern(int pathIndex) {
       return  (getPath().size() > pathIndex + 3 && getPreviousFromPath(pathIndex) instanceof FunctionNode
                    && getPreviousFromPath(pathIndex + 1) instanceof CallNode
                    && ((CallNode)getPreviousFromPath(pathIndex + 1)).getFunction().equals(getPreviousFromPath(pathIndex))
                    && getPreviousFromPath(pathIndex + 2) instanceof UnaryNode
                    && (getPreviousFromPath(pathIndex + 3) instanceof BinaryNode
                        || getPreviousFromPath(pathIndex + 3) instanceof VarNode));
    }

    private boolean isPriviliged(AccessNode aNode) {
        Node node = aNode.getBase();
        while (node instanceof AccessNode) {
            node = ((AccessNode)node).getBase();
        }
        if (node instanceof IdentNode && ModelUtils.THIS.endsWith(((IdentNode)node).getName())) {
            return true;
        }
        return false;
    }

    private void setModifiersFromDoc(JsObject object, Set<JsModifier> modifiers) {
        if (modifiers != null && !modifiers.isEmpty()) {
            for (JsModifier jsModifier : modifiers) {
                switch (jsModifier) {
                    case PRIVATE:
                        // if the modifier from doc is PRIVATE, keep information about the privilaged or public method anyway.
                        object.getModifiers().add(Modifier.PRIVATE);
                        break;
                    case PUBLIC:
                        object.getModifiers().remove(Modifier.PROTECTED);
                        object.getModifiers().remove(Modifier.PRIVATE);
                        object.getModifiers().add(Modifier.PUBLIC);
                        break;
                    case STATIC:
                        object.getModifiers().add(Modifier.STATIC);
                        break;
                }
            }
        }
    }

    private void processObjectPropertyAssignment(CallNode cNode) {
        if (!(cNode.getFunction() instanceof AccessNode)) {
            return;
        }

        AccessNode aNode = (AccessNode)cNode.getFunction();
        if ("assign".equals(aNode.getProperty())
                && aNode.getBase() instanceof IdentNode
                && "Object".equals(((IdentNode)aNode.getBase()).getName())) {
            // the function call is Object.assign ...
            final List<Expression> args = cNode.getArgs();
            if (args != null && !args.isEmpty()) {
                // first param is the target object
                List<Identifier> targetName = getNodeName(args.get(0), parserResult);
                if (targetName != null && !targetName.isEmpty()) {
                    JsObjectImpl targetObject = ModelUtils.getJsObject(modelBuilder, targetName, false);
                    if (targetObject != null) {
                        for (int i = 1; i < args.size(); i++) {
                            Expression expression = args.get(i);
                            List<Identifier> argName = getNodeName(expression, parserResult);
                            if (argName != null && !argName.isEmpty()) {
                                JsObjectImpl argObject = ModelUtils.getJsObject(modelBuilder, argName, false);
                                if (argObject != null) {
                                    for(JsObject property : argObject.getProperties().values()) {
                                        JsObject copyProperty;
                                        if (property.getJSKind().isFunction()) {
                                            copyProperty = new JsFunctionReference(targetObject, property.getDeclarationName(), (JsFunction)property, true, property.getModifiers());
                                        } else {
                                            copyProperty = new JsObjectReference(targetObject, property.getDeclarationName(), property, true, property.getModifiers());
                                        }
                                        targetObject.addProperty(copyProperty.getName(), copyProperty);
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }
    }

    public static class FunctionCall {

        private final String name;

        private final DeclarationScope scope;

        private final Collection<FunctionArgument> arguments;

        private final int callOffset;

        public FunctionCall(String name, DeclarationScope scope,
                Collection<FunctionArgument> arguments, int callOffset) {
            this.name = name;
            this.scope = scope;
            this.arguments = arguments;
            this.callOffset = callOffset;
        }

        public String getName() {
            return name;
        }

        public DeclarationScope getScope() {
            return scope;
        }

        public Collection<FunctionArgument> getArguments() {
            return arguments;
        }

        public int getCallOffset() {
            return callOffset;
        }
    }

    private String debugInfo(Node node) {
        StringBuilder sb = new StringBuilder();
        if (node instanceof FunctionNode) {
            FunctionNode fn = (FunctionNode)node;
            sb.append("FunctionNode name: ").append(fn.getName());
            sb.append(", Ident: ").append(fn.getIdent());
            if (fn.allVarsInScope()) sb.append(", allVarsInScope");
            if (fn.isAnonymous()) sb.append(", isAnonymous");
            if (fn.isDeclared()) sb.append(", isDeclared");
            if (fn.isMethod()) sb.append(", isMethod");
            if (fn.isNamedFunctionExpression()) sb.append(", isNamedFunctionExpression");
            if (fn.isVarArg()) sb.append(", isVarArg");
            if (fn.hasDeclaredFunctions()) sb.append(", hasDeclaredFunctions");
            if (fn.hasDirectSuper()) sb.append(", hasDirectSuper");
//            if (fn.hasScopeBlock()) sb.append(", hasScoprBlock");
        } else if (node instanceof VarNode) {
            VarNode vn = (VarNode)node;
            sb.append("VarNode ").append(vn.getName());
            if (vn.isBlockScoped()) sb.append(", isBlockScoped");
            if (vn.isConst()) sb.append(", isConst");
            if (vn.isFunctionDeclaration()) sb.append(", isFunctionDeclaration");
            if (vn.isLet()) sb.append(", isLet");
        } else {
            sb.append(node.getClass().getName());
        }
        return sb.toString();
    }


    @ServiceProvider(service = ModelResolver.Provider.class, position = 10_000)
    public static final class Provider implements ModelResolver.Provider {

        @Override
        public ModelResolver create(ParserResult result, OccurrenceBuilder occurrenceBuilder) {
            final ModelVisitor visitor = new ModelVisitor(result, occurrenceBuilder);
            return visitor;
        }

    }
}
