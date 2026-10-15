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
package org.netbeans.modules.javascript2.model.spi;

import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.BlockStatement;
import com.oracle.js.parser.ir.BreakNode;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.CaseNode;
import com.oracle.js.parser.ir.CatchNode;
import com.oracle.js.parser.ir.ClassElement;
import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.ContinueNode;
import com.oracle.js.parser.ir.DebuggerNode;
import com.oracle.js.parser.ir.EmptyNode;
import com.oracle.js.parser.ir.ErrorNode;
import com.oracle.js.parser.ir.ExportClauseNode;
import com.oracle.js.parser.ir.ExportNode;
import com.oracle.js.parser.ir.ExportSpecifierNode;
import com.oracle.js.parser.ir.ExpressionStatement;
import com.oracle.js.parser.ir.ForNode;
import com.oracle.js.parser.ir.FromNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.IfNode;
import com.oracle.js.parser.ir.ImportClauseNode;
import com.oracle.js.parser.ir.ImportNode;
import com.oracle.js.parser.ir.ImportSpecifierNode;
import com.oracle.js.parser.ir.IndexNode;
import com.oracle.js.parser.ir.JoinPredecessorExpression;
import com.oracle.js.parser.ir.LabelNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.NameSpaceImportNode;
import com.oracle.js.parser.ir.NamedImportsNode;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Petr Pisl
 */
public class PathNodeVisitor extends NodeVisitor {

    private final List<Node> treePath = new ArrayList<>();

    public PathNodeVisitor() {
        this(new LexicalContext());
    }

    public PathNodeVisitor(LexicalContext lc) {
        super(lc);
    }

    public List<? extends Node> getPath() {
        return Collections.unmodifiableList(treePath);
    }

    public void addToPath(Node node) {
        treePath.add(node);
    }

    public void removeFromPathTheLast() {
        treePath.remove(treePath.size() - 1);
    }

    public Node getPreviousFromPath(int back) {
        int size = getPath().size();
        if (size >= back) {
            return getPath().get(size - back);
        }
        return null;
    }
    
    @Override
    public Node leaveClassNode(ClassNode classNode) {
        removeFromPathTheLast();
        return super.leaveClassNode(classNode);
    }

    @Override
    public boolean enterClassNode(ClassNode classNode) {
        addToPath(classNode);
        return super.enterClassNode(classNode);
    }

    @Override
    public Node leaveWithNode(WithNode withNode) {
        removeFromPathTheLast();
        return super.leaveWithNode(withNode);
    }

    @Override
    public boolean enterWithNode(WithNode withNode) {
        addToPath(withNode);
        return super.enterWithNode(withNode);
    }

    @Override
    public Node leaveWhileNode(WhileNode whileNode) {
        removeFromPathTheLast();
        return super.leaveWhileNode(whileNode);
    }

    @Override
    public boolean enterWhileNode(WhileNode whileNode) {
        addToPath(whileNode);
        return super.enterWhileNode(whileNode);
    }

    @Override
    public Node leaveVarNode(VarNode varNode) {
        removeFromPathTheLast();
        return super.leaveVarNode(varNode);
    }

    @Override
    public boolean enterVarNode(VarNode varNode) {
        addToPath(varNode);
        return super.enterVarNode(varNode);
    }

    @Override
    public Node leaveJoinPredecessorExpression(JoinPredecessorExpression expr) {
        removeFromPathTheLast();
        return super.leaveJoinPredecessorExpression(expr);
    }

    @Override
    public boolean enterJoinPredecessorExpression(JoinPredecessorExpression expr) {
        addToPath(expr);
        return super.enterJoinPredecessorExpression(expr);
    }

    @Override
    public Node leaveUnaryNode(UnaryNode unaryNode) {
        removeFromPathTheLast();
        return super.leaveUnaryNode(unaryNode);
    }

    @Override
    public boolean enterUnaryNode(UnaryNode unaryNode) {
        addToPath(unaryNode);
        return super.enterUnaryNode(unaryNode);
    }

    @Override
    public Node leaveTryNode(TryNode tryNode) {
        removeFromPathTheLast();
        return super.leaveTryNode(tryNode);
    }

    @Override
    public boolean enterTryNode(TryNode tryNode) {
        addToPath(tryNode);
        return super.enterTryNode(tryNode);
    }

    @Override
    public Node leaveThrowNode(ThrowNode throwNode) {
        removeFromPathTheLast();
        return super.leaveThrowNode(throwNode);
    }

    @Override
    public boolean enterThrowNode(ThrowNode throwNode) {
        addToPath(throwNode);
        return super.enterThrowNode(throwNode);
    }

    @Override
    public Node leaveTernaryNode(TernaryNode ternaryNode) {
        removeFromPathTheLast();
        return super.leaveTernaryNode(ternaryNode);
    }

    @Override
    public boolean enterTernaryNode(TernaryNode ternaryNode) {
        addToPath(ternaryNode);
        return super.enterTernaryNode(ternaryNode);
    }

    @Override
    public Node leaveSwitchNode(SwitchNode switchNode) {
        removeFromPathTheLast();
        return super.leaveSwitchNode(switchNode);
    }

    @Override
    public boolean enterSwitchNode(SwitchNode switchNode) {
        addToPath(switchNode);
        return super.enterSwitchNode(switchNode);
    }

    @Override
    public Node leaveRuntimeNode(RuntimeNode runtimeNode) {
        removeFromPathTheLast();
        return super.leaveRuntimeNode(runtimeNode);
    }

    @Override
    public boolean enterRuntimeNode(RuntimeNode runtimeNode) {
        addToPath(runtimeNode);
        return super.enterRuntimeNode(runtimeNode);
    }

    @Override
    public Node leaveReturnNode(ReturnNode returnNode) {
        removeFromPathTheLast();
        return super.leaveReturnNode(returnNode);
    }

    @Override
    public boolean enterReturnNode(ReturnNode returnNode) {
        addToPath(returnNode);
        return super.enterReturnNode(returnNode);
    }

    @Override
    public Node leavePropertyNode(PropertyNode propertyNode) {
        removeFromPathTheLast();
        return super.leavePropertyNode(propertyNode);
    }

    @Override
    public boolean enterPropertyNode(PropertyNode propertyNode) {
        addToPath(propertyNode);
        return super.enterPropertyNode(propertyNode);
    }

    @Override
    public Node leaveObjectNode(ObjectNode objectNode) {
        removeFromPathTheLast();
        return super.leaveObjectNode(objectNode);
    }

    @Override
    public boolean enterObjectNode(ObjectNode objectNode) {
        addToPath(objectNode);
        return super.enterObjectNode(objectNode);
    }

    @Override
    public Node leaveLiteralNode(LiteralNode literalNode) {
        removeFromPathTheLast();
        return super.leaveLiteralNode(literalNode);
    }

    @Override
    public boolean enterLiteralNode(LiteralNode literalNode) {
        addToPath(literalNode);
        return super.enterLiteralNode(literalNode);
    }

    @Override
    public Node leaveLabelNode(LabelNode labelNode) {
        removeFromPathTheLast();
        return super.leaveLabelNode(labelNode);
    }

    @Override
    public boolean enterLabelNode(LabelNode labelNode) {
        addToPath(labelNode);
        return super.enterLabelNode(labelNode);
    }

    @Override
    public Node leaveIndexNode(IndexNode indexNode) {
        removeFromPathTheLast();
        return super.leaveIndexNode(indexNode);
    }

    @Override
    public boolean enterIndexNode(IndexNode indexNode) {
        addToPath(indexNode);
        return super.enterIndexNode(indexNode);
    }

    @Override
    public Node leaveIfNode(IfNode ifNode) {
        removeFromPathTheLast();
        return super.leaveIfNode(ifNode);
    }

    @Override
    public boolean enterIfNode(IfNode ifNode) {
        addToPath(ifNode);
        return super.enterIfNode(ifNode);
    }

    @Override
    public Node leaveIdentNode(IdentNode identNode) {
        removeFromPathTheLast();
        return super.leaveIdentNode(identNode);
    }

    @Override
    public boolean enterIdentNode(IdentNode identNode) {
        addToPath(identNode);
        return super.enterIdentNode(identNode);
    }

    @Override
    public Node leaveFunctionNode(FunctionNode functionNode) {
        removeFromPathTheLast();
        return super.leaveFunctionNode(functionNode);
    }

    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        addToPath(functionNode);
        return super.enterFunctionNode(functionNode);
    }

    @Override
    public Node leaveForNode(ForNode forNode) {
        removeFromPathTheLast();
        return super.leaveForNode(forNode);
    }

    @Override
    public boolean enterForNode(ForNode forNode) {
        addToPath(forNode);
        return super.enterForNode(forNode);
    }

    @Override
    public Node leaveBlockStatement(BlockStatement blockStatement) {
        removeFromPathTheLast();
        return super.leaveBlockStatement(blockStatement);
    }

    @Override
    public boolean enterBlockStatement(BlockStatement blockStatement) {
        addToPath(blockStatement);
        return super.enterBlockStatement(blockStatement);
    }

    @Override
    public Node leaveExpressionStatement(ExpressionStatement expressionStatement) {
        removeFromPathTheLast();
        return super.leaveExpressionStatement(expressionStatement);
    }

    @Override
    public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
        addToPath(expressionStatement);
        return super.enterExpressionStatement(expressionStatement);
    }

    @Override
    public Node leaveErrorNode(ErrorNode errorNode) {
        removeFromPathTheLast();
        return super.leaveErrorNode(errorNode);
    }

    @Override
    public boolean enterErrorNode(ErrorNode errorNode) {
        addToPath(errorNode);
        return super.enterErrorNode(errorNode);
    }

    @Override
    public Node leaveEmptyNode(EmptyNode emptyNode) {
        removeFromPathTheLast();
        return super.leaveEmptyNode(emptyNode);
    }

    @Override
    public boolean enterEmptyNode(EmptyNode emptyNode) {
        addToPath(emptyNode);
        return super.enterEmptyNode(emptyNode);
    }

    @Override
    public Node leaveDebuggerNode(DebuggerNode debuggerNode) {
        removeFromPathTheLast();
        return super.leaveDebuggerNode(debuggerNode);
    }

    @Override
    public boolean enterDebuggerNode(DebuggerNode debuggerNode) {
        addToPath(debuggerNode);
        return super.enterDebuggerNode(debuggerNode);
    }

    @Override
    public Node leaveContinueNode(ContinueNode continueNode) {
        removeFromPathTheLast();
        return super.leaveContinueNode(continueNode);
    }

    @Override
    public boolean enterContinueNode(ContinueNode continueNode) {
        addToPath(continueNode);
        return super.enterContinueNode(continueNode);
    }

    @Override
    public Node leaveCatchNode(CatchNode catchNode) {
        removeFromPathTheLast();
        return super.leaveCatchNode(catchNode);
    }

    @Override
    public boolean enterCatchNode(CatchNode catchNode) {
        addToPath(catchNode);
        return super.enterCatchNode(catchNode);
    }

    @Override
    public Node leaveCaseNode(CaseNode caseNode) {
        removeFromPathTheLast();
        return super.leaveCaseNode(caseNode);
    }

    @Override
    public boolean enterCaseNode(CaseNode caseNode) {
        addToPath(caseNode);
        return super.enterCaseNode(caseNode);
    }

    @Override
    public Node leaveCallNode(CallNode callNode) {
        removeFromPathTheLast();
        return super.leaveCallNode(callNode);
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        addToPath(callNode);
        return super.enterCallNode(callNode);
    }

    @Override
    public Node leaveBreakNode(BreakNode breakNode) {
        removeFromPathTheLast();
        return super.leaveBreakNode(breakNode);
    }

    @Override
    public boolean enterBreakNode(BreakNode breakNode) {
        addToPath(breakNode);
        return super.enterBreakNode(breakNode);
    }

    @Override
    public Node leaveBinaryNode(BinaryNode binaryNode) {
        removeFromPathTheLast();
        return super.leaveBinaryNode(binaryNode);
    }

    @Override
    public boolean enterBinaryNode(BinaryNode binaryNode) {
        addToPath(binaryNode);
        return super.enterBinaryNode(binaryNode);
    }

    @Override
    public Node leaveBlock(Block block) {
        removeFromPathTheLast();
        return super.leaveBlock(block);
    }

    @Override
    public boolean enterBlock(Block block) {
        addToPath(block);
        return super.enterBlock(block);
    }

    @Override
    public Node leaveAccessNode(AccessNode accessNode) {
        removeFromPathTheLast();
        return super.leaveAccessNode(accessNode);
    }

    @Override
    public boolean enterAccessNode(AccessNode accessNode) {
        addToPath(accessNode);
        return super.enterAccessNode(accessNode);
    }

    @Override
    public boolean enterExportClauseNode(ExportClauseNode exportClauseNode) {
        addToPath(exportClauseNode);
        return super.enterExportClauseNode(exportClauseNode);
    }

    @Override
    public Node leaveExportClauseNode(ExportClauseNode exportClauseNode) {
        removeFromPathTheLast();
        return super.leaveExportClauseNode(exportClauseNode);
    }

    @Override
    public boolean enterExportNode(ExportNode exportNode) {
        addToPath(exportNode);
        return super.enterExportNode(exportNode); 
    }

    @Override
    public Node leaveExportNode(ExportNode exportNode) {
        removeFromPathTheLast();
        return super.leaveExportNode(exportNode);
    }

    @Override
    public boolean enterExportSpecifierNode(ExportSpecifierNode exportSpecifierNode) {
        addToPath(exportSpecifierNode);
        return super.enterExportSpecifierNode(exportSpecifierNode);
    }

    @Override
    public Node leaveExportSpecifierNode(ExportSpecifierNode exportSpecifierNode) {
        removeFromPathTheLast();
        return super.leaveExportSpecifierNode(exportSpecifierNode);
    }

    @Override
    public boolean enterFromNode(FromNode fromNode) {
        addToPath(fromNode);
        return super.enterFromNode(fromNode);
    }

    @Override
    public Node leaveFromNode(FromNode fromNode) {
        removeFromPathTheLast();
        return super.leaveFromNode(fromNode);
    }

    @Override
    public boolean enterImportClauseNode(ImportClauseNode importClauseNode) {
        addToPath(importClauseNode);
        return super.enterImportClauseNode(importClauseNode);
    }

    @Override
    public Node leaveImportClauseNode(ImportClauseNode importClauseNode) {
        removeFromPathTheLast();
        return super.leaveImportClauseNode(importClauseNode);
    }

    @Override
    public boolean enterImportNode(ImportNode importNode) {
        addToPath(importNode);
        return super.enterImportNode(importNode);
    }

    @Override
    public Node leaveImportNode(ImportNode importNode) {
        removeFromPathTheLast();
        return super.leaveImportNode(importNode);
    }

    @Override
    public boolean enterImportSpecifierNode(ImportSpecifierNode importSpecifierNode) {
        addToPath(importSpecifierNode);
        return super.enterImportSpecifierNode(importSpecifierNode); 
    }

    @Override
    public Node leaveImportSpecifierNode(ImportSpecifierNode importSpecifierNode) {
        removeFromPathTheLast();
        return super.leaveImportSpecifierNode(importSpecifierNode); 
    }

    @Override
    public boolean enterNameSpaceImportNode(NameSpaceImportNode nameSpaceImportNode) {
        addToPath(nameSpaceImportNode);
        return super.enterNameSpaceImportNode(nameSpaceImportNode); 
    }

    @Override
    public Node leaveNameSpaceImportNode(NameSpaceImportNode nameSpaceImportNode) {
        removeFromPathTheLast();
        return super.leaveNameSpaceImportNode(nameSpaceImportNode); 
    }

    @Override
    public boolean enterNamedImportsNode(NamedImportsNode namedImportsNode) {
        addToPath(namedImportsNode);
        return super.enterNamedImportsNode(namedImportsNode); 
    }

    @Override
    public Node leaveNamedImportsNode(NamedImportsNode namedImportsNode) {
        removeFromPathTheLast();
        return super.leaveNamedImportsNode(namedImportsNode); 
    }

    @Override
    public Node leaveClassElement(ClassElement classElement) {
        removeFromPathTheLast();
        return super.leaveClassElement(classElement);
    }

    @Override
    public boolean enterClassElement(ClassElement classElement) {
        addToPath(classElement);
        return super.enterClassElement(classElement);
    }
}
