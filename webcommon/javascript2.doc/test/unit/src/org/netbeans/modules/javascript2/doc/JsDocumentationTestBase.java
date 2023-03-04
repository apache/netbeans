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
package org.netbeans.modules.javascript2.doc;

import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.BlockStatement;
import com.oracle.js.parser.ir.BreakNode;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.CaseNode;
import com.oracle.js.parser.ir.CatchNode;
import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.ContinueNode;
import com.oracle.js.parser.ir.DebuggerNode;
import com.oracle.js.parser.ir.EmptyNode;
import com.oracle.js.parser.ir.ErrorNode;
import com.oracle.js.parser.ir.ExpressionStatement;
import com.oracle.js.parser.ir.ForNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.IfNode;
import com.oracle.js.parser.ir.IndexNode;
import com.oracle.js.parser.ir.JoinPredecessorExpression;
import com.oracle.js.parser.ir.LabelNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.ReturnNode;
import com.oracle.js.parser.ir.RuntimeNode;
import com.oracle.js.parser.ir.SwitchNode;
import com.oracle.js.parser.ir.TernaryNode;
import com.oracle.js.parser.ir.ThrowNode;
import com.oracle.js.parser.ir.TryNode;
import com.oracle.js.parser.ir.UnaryNode;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.WhileNode;
import com.oracle.js.parser.ir.WithNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.api.Source;

/**
 * Base of class for doc unit tests.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class JsDocumentationTestBase extends CslTestBase {

    public JsDocumentationTestBase(String testName) {
        super(testName);
    }

    /**
     * Gets {@code DocumentationProvider} for given parse result.
     *
     * @param parserResult parser result of the JS file
     * @return appropriate {@code JsDocumentationHolder} to given source
     */
    public JsDocumentationHolder getDocumentationHolder(ParserResult parserResult) {
        return JsDocumentationSupport.getDocumentationHolder(parserResult);
    }

    /**
     * Gets {@code DocumentationProvider} for given parse result.
     *
     * @param parserResult parser result of the JS file
     * @param provider which provider should be used to create the {@code JsDocumentationHolder}
     * @return requested type of {@code JsDocumentationHolder}
     */
    public JsDocumentationHolder getDocumentationHolder(ParserResult parserResult, JsDocumentationProvider provider) {
        return provider.createDocumentationHolder(parserResult.getSnapshot());
    }

    /**
     * Gets node for given offset.
     *
     * @param parserResult parser result of the JS file
     * @param offset offset of examined node
     * @return {@code Node} which correspond to given offset
     */
    public Node getNodeForOffset(ParserResult parserResult, int offset) {
        Node nearestNode = null;
        int nearestNodeDistance = Integer.MAX_VALUE;
        FunctionNode root = parserResult.getLookup().lookup(FunctionNode.class);
        OffsetVisitor offsetVisitor = new OffsetVisitor(offset);
        root.accept(offsetVisitor);
        for (Node node : offsetVisitor.getNodes()) {
            if (offset - node.getStart() < nearestNodeDistance) {
                nearestNodeDistance = offset - node.getStart();
                nearestNode = node;
            }
        }
        return nearestNode;
    }

    /**
     * Return the offset of the given position, indicated by ^ in the line fragment from the text got from given Source.
     *
     * @param source source for counting the offset
     * @param caretLine line
     * @return offset of ^ in the given source
     */
    public int getCaretOffset(Source source, String caretLine) {
        return getCaretOffset(source.createSnapshot().getText().toString(), caretLine);
    }


    private static class OffsetVisitor extends NodeVisitor {

        private final int offset;
        private final List<Node> nodes = new LinkedList<Node>();

        public OffsetVisitor(int offset) {
            super(new LexicalContext());
            this.offset = offset;
        }

        private void processNode(Node node) {
            if (offset >= node.getStart() && offset <= node.getFinish()) {
                nodes.add(node);
            }
        }

        public List<Node> getNodes() {
            return nodes;
        }

        @Override
        public boolean enterClassNode(ClassNode classNode) {
            processNode(classNode);
            return super.enterClassNode(classNode);
        }

        @Override
        public boolean enterWithNode(WithNode withNode) {
            processNode(withNode);
            return super.enterWithNode(withNode);
        }

        @Override
        public boolean enterWhileNode(WhileNode whileNode) {
            processNode(whileNode);
            return super.enterWhileNode(whileNode);
        }

        @Override
        public boolean enterVarNode(VarNode varNode) {
            processNode(varNode);
            return super.enterVarNode(varNode);
        }

        @Override
        public boolean enterJoinPredecessorExpression(JoinPredecessorExpression expr) {
            processNode(expr);
            return super.enterJoinPredecessorExpression(expr);
        }

        @Override
        public boolean enterUnaryNode(UnaryNode unaryNode) {
            processNode(unaryNode);
            return super.enterUnaryNode(unaryNode);
        }

        @Override
        public boolean enterTryNode(TryNode tryNode) {
            processNode(tryNode);
            return super.enterTryNode(tryNode);
        }

        @Override
        public boolean enterThrowNode(ThrowNode throwNode) {
            processNode(throwNode);
            return super.enterThrowNode(throwNode);
        }

        @Override
        public boolean enterTernaryNode(TernaryNode ternaryNode) {
            processNode(ternaryNode);
            return super.enterTernaryNode(ternaryNode);
        }

        @Override
        public boolean enterSwitchNode(SwitchNode switchNode) {
            processNode(switchNode);
            return super.enterSwitchNode(switchNode);
        }

        @Override
        public boolean enterRuntimeNode(RuntimeNode runtimeNode) {
            processNode(runtimeNode);
            return super.enterRuntimeNode(runtimeNode);
        }

        @Override
        public boolean enterReturnNode(ReturnNode returnNode) {
            processNode(returnNode);
            return super.enterReturnNode(returnNode);
        }

        @Override
        public boolean enterPropertyNode(PropertyNode propertyNode) {
            processNode(propertyNode);
            return super.enterPropertyNode(propertyNode);
        }

        @Override
        public boolean enterObjectNode(ObjectNode objectNode) {
            processNode(objectNode);
            return super.enterObjectNode(objectNode);
        }

        @Override
        public boolean enterLiteralNode(LiteralNode literalNode) {
            processNode(literalNode);
            return super.enterLiteralNode(literalNode);
        }

        @Override
        public boolean enterLabelNode(LabelNode labelNode) {
            processNode(labelNode);
            return super.enterLabelNode(labelNode);
        }

        @Override
        public boolean enterIndexNode(IndexNode indexNode) {
            processNode(indexNode);
            return super.enterIndexNode(indexNode);
        }

        @Override
        public boolean enterIfNode(IfNode ifNode) {
            processNode(ifNode);
            return super.enterIfNode(ifNode);
        }

        @Override
        public boolean enterIdentNode(IdentNode identNode) {
            processNode(identNode);
            return super.enterIdentNode(identNode);
        }

        @Override
        public boolean enterFunctionNode(FunctionNode functionNode) {
            processNode(functionNode);
            return super.enterFunctionNode(functionNode);
        }

        @Override
        public boolean enterForNode(ForNode forNode) {
            processNode(forNode);
            return super.enterForNode(forNode);
        }

        @Override
        public boolean enterBlockStatement(BlockStatement blockStatement) {
            processNode(blockStatement);
            return super.enterBlockStatement(blockStatement);
        }

        @Override
        public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
            processNode(expressionStatement);
            return super.enterExpressionStatement(expressionStatement);
        }

        @Override
        public boolean enterErrorNode(ErrorNode errorNode) {
            processNode(errorNode);
            return super.enterErrorNode(errorNode);
        }

        @Override
        public boolean enterEmptyNode(EmptyNode emptyNode) {
            processNode(emptyNode);
            return super.enterEmptyNode(emptyNode);
        }

        @Override
        public boolean enterDebuggerNode(DebuggerNode debuggerNode) {
            processNode(debuggerNode);
            return super.enterDebuggerNode(debuggerNode);
        }

        @Override
        public boolean enterContinueNode(ContinueNode continueNode) {
            processNode(continueNode);
            return super.enterContinueNode(continueNode);
        }

        @Override
        public boolean enterCatchNode(CatchNode catchNode) {
            processNode(catchNode);
            return super.enterCatchNode(catchNode);
        }

        @Override
        public boolean enterCaseNode(CaseNode caseNode) {
            processNode(caseNode);
            return super.enterCaseNode(caseNode);
        }

        @Override
        public boolean enterCallNode(CallNode callNode) {
            processNode(callNode);
            return super.enterCallNode(callNode);
        }

        @Override
        public boolean enterBreakNode(BreakNode breakNode) {
            processNode(breakNode);
            return super.enterBreakNode(breakNode);
        }

        @Override
        public boolean enterBinaryNode(BinaryNode binaryNode) {
            processNode(binaryNode);
            return super.enterBinaryNode(binaryNode);
        }

        @Override
        public boolean enterBlock(Block block) {
            processNode(block);
            return super.enterBlock(block);
        }

        @Override
        public boolean enterAccessNode(AccessNode accessNode) {
            processNode(accessNode);
            return super.enterAccessNode(accessNode);
        }
    }
}
