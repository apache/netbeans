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
package org.netbeans.modules.javascript2.editor.parser;

import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BaseNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.Expression;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.IndexNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.UnaryNode;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.Token;
import com.oracle.js.parser.ir.JsxAttributeNode;
import com.oracle.js.parser.ir.JsxElementNode;
import java.util.List;

/**
 *
 * @author Petr Pisl
 */
public class AstXmlVisitor extends NodeVisitor {

    private StringBuilder sb;
    private int indent;

    public AstXmlVisitor(LexicalContext lc) {
        super(lc);
        this.sb = new StringBuilder();
        this.sb.append("<!--\n"
                + "\n"
                + "    Licensed to the Apache Software Foundation (ASF) under one\n"
                + "    or more contributor license agreements.  See the NOTICE file\n"
                + "    distributed with this work for additional information\n"
                + "    regarding copyright ownership.  The ASF licenses this file\n"
                + "    to you under the Apache License, Version 2.0 (the\n"
                + "    \"License\"); you may not use this file except in compliance\n"
                + "    with the License.  You may obtain a copy of the License at\n"
                + "\n"
                + "      http://www.apache.org/licenses/LICENSE-2.0\n"
                + "\n"
                + "    Unless required by applicable law or agreed to in writing,\n"
                + "    software distributed under the License is distributed on an\n"
                + "    \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n"
                + "    KIND, either express or implied.  See the License for the\n"
                + "    specific language governing permissions and limitations\n"
                + "    under the License.\n"
                + "\n"
                + "-->\n\n");
        this.indent = 0;
    }

    public String getXmTree() {
        return sb.toString();
    }

    private void createOpenTag(Node node) {
        String indentation = createSpaces();
        sb.append(indentation).append('<');
        sb.append(getNodeName(node));
        appendOffsetInfo(node);
        sb.append(">\n");
        increaseIndent();
    }

    private void createOpenTag(Node node, String... attributes) {
        String indentation = createSpaces();
        sb.append(indentation).append('<');
        sb.append(getNodeName(node));
        if (attributes != null && attributes.length > 0) {
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i] != null) {
                    if (attributes[i].charAt(0) != ' ') {
                        sb.append(' ');
                    }
                    sb.append(attributes[i]);
                }
            }
        }
        appendOffsetInfo(node);
        sb.append(">\n");
        increaseIndent();
    }

    private void createCloseTag(Node node) {
        decreaseIndent();
        String indentation = createSpaces();
        sb.append(indentation).append("</").append(getNodeName(node)).append(">\n");
    }

    private void createOpenCloseTag(Node node, String attributes) {
        String indentation = createSpaces();
        sb.append(indentation).append('<');
        sb.append(getNodeName(node));
        if (attributes != null && !attributes.isEmpty()) {
            if (attributes.charAt(0) != ' ') {
                sb.append(' ');
            }
            sb.append(attributes);
        }
        appendOffsetInfo(node);
        sb.append("/>\n");
    }

    private String createTagAttribute(String name, String value){
        if (value == null) {
            return null;
        }
        return name + "='" + value.trim() + "'";
    }
    
    private void createComment(String comment) {
        String indentation = createSpaces();
        sb.append(indentation).append("<!-- ").append(comment).append(" -->\n");
    }

    private String getNodeName(Node node) {
        String canonicalName = node.getClass().getCanonicalName();
        String name = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
        return name;
    }

    private void increaseIndent() {
        indent += 2;
    }

    private void decreaseIndent() {
        indent -= 2;
    }

    private String createSpaces() {
        StringBuilder spaces = new StringBuilder(indent);
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        return spaces.toString();
    }

    private void appendOffsetInfo(Node node) {
        if(node instanceof FunctionNode) {
            appendOffsetInfo((FunctionNode)node);
            return;
        }
        sb.append(" start='").append(node.getStart()).append('\'');
        sb.append(" end='").append(node.getFinish()).append('\'');
    }
    
    private void appendOffsetInfo(FunctionNode node) {
        sb.append(" start='").append(Token.descPosition(node.getFirstToken())).append('\'');
        sb.append(" end='").append(Token.descPosition(node.getLastToken()) + Token.descLength(node.getLastToken())).append('\'');
    }

    
    private boolean processAttribute(final boolean add, final String name) {
        if (add) {
            String indentation = createSpaces();
            sb.append(indentation).append('<').append(name).append("/>\n");
        }
        return add;
    }

    private void createSimpleTag(final String tagName, final String value) {
        String indentation = createSpaces();
        sb.append(indentation).append('<').append(tagName).append('>');
        sb.append(value);
        sb.append(indentation).append("</").append(tagName).append(">\n");
    }

    private void processWithComment(Node node, String comment) {
        if (node != null) {
            createComment(comment);
            node.accept(this);
        }
    }

    private void processWithComment(List<? extends Node> nodes, String comment) {
        if (nodes != null) {
            createComment(comment);
            for (Node node : nodes) {
                node.accept(this);
            }
        }
    }

    @Override
    protected boolean enterDefault(Node node) {
        createOpenTag(node);
        return super.enterDefault(node);
    }

    @Override
    protected Node leaveDefault(Node node) {
        createCloseTag(node);
        return super.leaveDefault(node);
    }

    private void processAttribute(BaseNode node) {
        processAttribute(node.isFunction(), "isFunction");
        processAttribute(node.isIndex(), "isIndex");
        processAttribute(node.isSuper(), "isSuper");
        processAttribute((Expression)node);
    }
    
    private void processAttribute(Block node) {
         processAttribute(node.isBreakableWithoutLabel(), "isBreakableWithoutLabel");
        processAttribute(node.isCatchBlock(), "isCatchBlock");
        processAttribute(node.isFunctionBody(), "isFunctionBody");
        processAttribute(node.isGlobalScope(), "isGlobalSpace");
        processAttribute(node.isParameterBlock(), "isParameterBlock");
        processAttribute(node.isSynthetic(), "isSynthetic");
        processAttribute(node.isTerminal(), "isTerminal");
        processAttribute((Node)node);
    }
    
    private void processAttribute(Expression node) {
        processAttribute(node.isAlwaysFalse(), "isAlwaysFalse");
        processAttribute(node.isAlwaysTrue(), "isAlwaysTrue");
//        processAttribute(node.isOptimistic(), "isOptimistic");
        processAttribute(node.isSelfModifying(), "isSelfModifying");
        processAttribute((Node)node);
    }
    
    private void processAttribute(Node node) {
        processAttribute(node.isAssignment(), "isAssignment");
        processAttribute(node.isLoop(), "isLoop");
    }
    
    @Override
    public boolean enterAccessNode(AccessNode node) {
        createOpenTag(node,
                createTagAttribute("property", node.getProperty()));
        
        processAttribute(node);
        
        processWithComment(node.getBase(), "AccessNode Base");
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterBinaryNode(BinaryNode node) {
        createOpenTag(node,
                createTagAttribute("type", node.tokenType().name()));
        processAttribute(node.isAssignment(), "isAssignment");
        processAttribute(node.isComparison(), "isComparison");
        processAttribute(node.isLogical(), "isLogical");
        processAttribute(node.isRelational(), "isRelational");
        processAttribute(node.isSelfModifying(), "isSelfModifying");
//        processWithComment(node.getAssignmentDest(), "BinaryNode AssignmentDest");
//        processWithComment(node.getAssignmentSource(), "BinaryNode AssignmentSource");
        processWithComment(node.lhs(), "BinaryNode lhs");
        processWithComment(node.rhs(), "BinaryNode rhs");
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterBlock(Block node) {
        createOpenTag(node);
        
        processAttribute(node);
        processWithComment(node.getStatements(), "Block Statements");
       
        createCloseTag(node);
        return false;
    }
    
    @Override
    public boolean enterClassNode(ClassNode node) {
        createOpenTag(node, 
                node.getIdent() != null ? createTagAttribute("ident", node.getIdent().getName()) : null);
        
        processAttribute(node);
        processWithComment(node.getClassHeritage(), "ClassNode Heritage");
        processWithComment(node.getConstructor(), "ClassNode Constructor");
        processWithComment(node.getClassElements(), "ClassNode Elements");
        processWithComment(node.getDecorators(), "ClassNode Decorators");
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterCallNode(CallNode node) {
        createOpenTag(node);
        
//        processAttribute(node.isApplyToCall(), "isApplayToCall");
        processAttribute(node.isEval(), "isEval");
        processAttribute(node.isNew(), "isNew");
        processAttribute(node);
        
        processWithComment(node.getArgs(), "CallNode Arguments");
        processWithComment(node.getFunction(), "CallNode Function");
        createCloseTag(node);
        return false;
    }
    
    

    @Override
    public boolean enterFunctionNode(FunctionNode node) {
        createOpenTag(node,
                createTagAttribute("name", node.getName()),
                createTagAttribute("kind", node.getKind().name()));
        
        processAttribute(node.hasDeclaredFunctions(), "hasDeclaredFunctions");
//        processAttribute(node.hasScopeBlock(), "hasScopeBlock");
//        processAttribute(node.inDynamicContext(), "isDynamicContext");
        processAttribute(node.isAnonymous(), "isAnonymous");
        processAttribute(node.isClassConstructor(), "isClassConstructor");
        processAttribute(node.isDeclared(), "isDeclared");
        processAttribute(node.isMethod(), "isMethod");
        processAttribute(node.isProgram(), "isProgram");
        processAttribute(node.isNamedFunctionExpression(), "isNamedFunctionExpression");
        processAttribute(node.isSubclassConstructor(), "isSubclassConstructor");
        processAttribute(node.isVarArg(), "isVarArg");
        processAttribute(node.isAsync(), "isAsync");
        processAttribute(node);

        if (node.isModule()) {
            node.visitImports(this);
            node.visitExports(this);
        }
        processWithComment(node.getParameters(), "FunctionNode Parameters");
        processWithComment(node.getBody(), "FunctionNode Body");
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterIdentNode(IdentNode node) {
        createOpenTag(node);
        createSimpleTag("name", node.getName());
        if (node.getPropertyName() != null && !node.getName().equals(node.getPropertyName())) {
            createSimpleTag("propertyName", node.getPropertyName());
        }

        processAttribute(node.isDeclaredHere(), "isDeclaredHere");
        processAttribute(node.isDefaultParameter(), "isDefaultParameter");
        processAttribute(node.isDestructuredParameter(), "isDestructuredParameter");
        processAttribute(node.isDirectSuper(), "isDirectSuppert");
        processAttribute(node.isFunction(), "isFunction");
        processAttribute(node.isFutureStrictName(), "isFutureStrictName");
        processAttribute(node.isInitializedHere(), "isInitializedHere");
        processAttribute(node.isInternal(), "isInternal");
        processAttribute(node.isPropertyName(), "isPropertyName");
        processAttribute(node.isProtoPropertyName(), "isProtoPropertyName");
        processAttribute(node.isRestParameter(), "isRestParameter");
        processAttribute(node);
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterIndexNode(IndexNode node) {
        createOpenTag(node);
        
        processAttribute(node);
        
        processWithComment(node.getBase(), "IndexNode Base");
        processWithComment(node.getIndex(), "IndexNode Index");
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterJsxElementNode(JsxElementNode node) {
        createOpenTag(node, createTagAttribute("name", node.getName()));
        
        processWithComment(node.getAttributes(), "JSX Element Attributes");
        processWithComment(node.getChildren(), "JSX Element Children");
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterJsxAttributeNode(JsxAttributeNode node) {
        createOpenTag(node, createTagAttribute("name", node.getName()));
        
        processWithComment(node.getValue(), "JSX Attribute Value");
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterLiteralNode(LiteralNode node) {
        if (!(node instanceof LiteralNode.ArrayLiteralNode)) {
            createOpenCloseTag(node, 
                    node.getValue() != null ? createTagAttribute("value", node.getValue().toString()) : null);
            return false;
        }

        return super.enterLiteralNode(node);
    }

    
            
    @Override
    public boolean enterObjectNode(ObjectNode node) {
        createOpenTag(node);
        processAttribute(node);
        processWithComment(node.getElements(), "ObjectNode Elements");
        createCloseTag(node);
        return false;
    }

    
    @Override
    public boolean enterPropertyNode(PropertyNode node) {
        createOpenTag(node, createTagAttribute("name", node.getKeyName()));
        processAttribute(node.isComputed(), "isComputed");
        processAttribute(node.isStatic(), "isStatic");
        processAttribute(node);
        
        processWithComment(node.getKey(), "PropertyNode Key");
        processWithComment(node.getValue(), "PropertyNode Value");
        processWithComment(node.getGetter(), "PropertyNode Getter");
        processWithComment(node.getSetter(), "PropertyNode Setter");
        processWithComment(node.getDecorators(), "PropertyNode Decorators");
        createCloseTag(node);
        return false;
    }

    @Override
    public boolean enterUnaryNode(UnaryNode node) {
        createOpenTag(node, createTagAttribute("type", node.tokenType().name()));

        processAttribute(node.isAssignment(), "isAssignment");
        if (node.getExpression() != node.getAssignmentDest()) {
            processWithComment(node.getAssignmentDest(), "UnaryNode AssignmentDest");
        }
        if (node.getExpression() != node.getAssignmentSource()) {
            processWithComment(node.getAssignmentSource(), "UnaryNode AssignmentSource");
        }
        processWithComment(node.getExpression(), "UnaryNode Expression");
        
        createCloseTag(node);
        return false;
    }

    
    
    @Override
    public boolean enterVarNode(VarNode node) {
        createOpenTag(node, createTagAttribute("name", node.getName().getName()));
        processAttribute(node.hasInit(), "hasInit");
        processAttribute(node.isAssignment(), "isAssignment");
        processAttribute(node.isBlockScoped(), "isBlockScoped");
        processAttribute(node.isConst(), "isConst");
        processAttribute(node.isFunctionDeclaration(), "isFunctionDeclaration");
        processAttribute(node.isLet(), "isLet");
        processAttribute(node.isExport(), "isExport");
        processAttribute(node.isDestructuring(), "isDestructuring");

        processWithComment(node.getAssignmentDest(), "VarNode Assignment Dest");
        processWithComment(node.getInit(), "VarNode Init");
        if (node.getAssignmentSource() != node.getInit()) {
            processWithComment(node.getAssignmentSource(), "VarNode Assignment Source");
        }
        createCloseTag(node);
        return false;
    }

    
}
