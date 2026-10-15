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
package org.netbeans.modules.css.lib;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.BlankDebugEventListener;

import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import org.antlr.runtime.NoViableAltException;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.openide.util.NbBundle;
import static org.netbeans.modules.css.lib.Bundle.*;

/**
 * A patched version of ANLR's ParseTreeBuilder 
 * 
 * @author mfukala@netbeans.org
 */
public class NbParseTreeBuilder extends BlankDebugEventListener {

    //ignore 'syncToIdent' rule - the DBG.enter/exit/Rule calls are generated
    //automatically by ANTLR but we do not care about them since 
    //the error recovery implementation in syncToSet(...)
    //calls DBG.enter/exit/Rule("recovery") itself.
    private static final String[] IGNORED_RULES = new String[]{"syncToDeclarationsRule", "syncToFollow"}; //!!! must be sorted alphabetically !!!
    private static final String RECOVERY_RULE_NAME = "recovery";

    private static boolean debug_tokens = false;

    private final CharSequence source;
    private final Stack<RuleNode> callStack = new Stack<>();
    private final Stack<ErrorNode> errorNodes = new Stack<>();
    private final List<CommonToken> hiddenTokens = new ArrayList<>();
    private final Map<CommonToken, Pair<Node>> noViableAltNodes = new HashMap<>();
    private final Collection<RuleNode> leafRuleNodes = new ArrayList<>();
    private final Collection<ProblemDescription> problems = new LinkedHashSet<> ();

    private int backtracking = 0;
    private CommonToken lastConsumedToken;
    private boolean resync;
    private CommonToken unexpectedToken;

    public NbParseTreeBuilder(CharSequence source) {
        this.source = source;
        callStack.push(new RootNode(source));
    }

    public AbstractParseTreeNode getTree() {
        return callStack.elementAt(0);
    }

    /** Backtracking or cyclic DFA, don't want to add nodes to tree */
    @Override
    public void enterDecision(int d, boolean couldBacktrack) {
        backtracking++;
    }

    @Override
    public void exitDecision(int i) {
        backtracking--;
    }

    private boolean isIgnoredRule(String ruleName) {
        return Arrays.binarySearch(IGNORED_RULES, ruleName) >= 0;
    }

    @Override
    public void enterRule(String filename, String ruleName) {
        if (backtracking > 0) {
            return;
        }
        if (isIgnoredRule(ruleName)) {
            return;
        }

        AbstractParseTreeNode parentRuleNode = callStack.peek();
        RuleNode ruleNode = new RuleNode(NodeType.valueOf(ruleName), source);
        addNodeChild(parentRuleNode, ruleNode);
        callStack.push(ruleNode);
    }

    @Override
    public void exitRule(String filename, String ruleName) {
        if (backtracking > 0) {
            return;
        }
        if (isIgnoredRule(ruleName)) {
            return;
        }

        RuleNode ruleNode = callStack.pop();
        if (ruleNode.getChildCount() > 0) {
            //set the rule end offset
            if (lastConsumedToken != null) {
                ruleNode.setLastToken(lastConsumedToken);
            }
        } else {
            //empty node - we cannot remove it right now since an error node
            //may be attached to it later.
            //all the nodes from possiblyEmptyRuleNodes list are checked after
            //the parsing finishes and removed from the parse tree if still empty
            leafRuleNodes.add(ruleNode);
        }
        
        if(RECOVERY_RULE_NAME.equals(ruleName)) {
            
            if(ruleNode.getChildCount() > 0) {
                //create a ProblemDescription for the skipped tokens
                //create a ParsingProblem
                int trimmedSize = 0;
                StringBuilder tokensList = new StringBuilder();
                for(int i = 0; i < ruleNode.getChildCount(); i++) {
                    Node child = (Node)ruleNode.getChild(i);
                    trimmedSize+=child.image().toString().trim().length();
                    
                    tokensList.append('\'');
                    tokensList.append(child.image());
                    tokensList.append('\'');
                    if(i < ruleNode.getChildCount() - 1) {
                        tokensList.append(',');
                    }
                }

                if(trimmedSize > 0) {
                    //do not report skipped whitespaces
                    ProblemDescription problemDescription = new ProblemDescription(
                        ruleNode.from(),
                        ruleNode.to(),
                        MSG_Error_Unexpected_Char(tokensList),
                        ProblemDescription.Keys.PARSING.name(),
                        ProblemDescription.Type.ERROR);

                    problems.add(problemDescription);
                }
            }
            
        }
    }

    @Override
    public void beginResync() {
        super.beginResync();
        resync = true;
    }

    @Override
    public void endResync() {
        super.endResync();
        resync = false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void consumeToken(Token token) {
        if (backtracking > 0 || resync) {
            return;
        }

        if (debug_tokens) {
            CommonToken ct = (CommonToken) token;
            int[] ctr = CommonTokenUtil.getCommonTokenOffsetRange(ct);
            System.out.println(token + "(" + ctr[0] + "-" + ctr[1] + ")");
        }

        //ignore the closing EOF token, we do not want it
        //it the parse tree
        if (token.getType() == Css3Lexer.EOF) {
            return;
        }

        //also ignore error tokens - they are added as children of ErrorNode-s in the recognitionException(...) method
        if (token.getType() == Token.INVALID_TOKEN_TYPE) {
            return;
        }

        lastConsumedToken = (CommonToken) token;

        RuleNode ruleNode = callStack.peek();
        TokenNode elementNode = new TokenNode(source, (CommonToken) token);
        elementNode.hiddenTokens = (List<Token>) (List<?>) this.hiddenTokens;
        hiddenTokens.clear();
        ruleNode.addChild(elementNode);

        updateFirstTokens(ruleNode, lastConsumedToken);
    }

    //set first token for all RuleNode-s in the stack without the first token set
    @SuppressWarnings("AssignmentToMethodParameter")
    private void updateFirstTokens(RuleNode ruleNode, CommonToken token) {
        while (true) {

            if (ruleNode.from() != -1) {
                break;
            }
            ruleNode.setFirstToken(token);
            ruleNode = (RuleNode) ruleNode.getParent();
            if (ruleNode == null) {
                break;
            }
        }
    }

    @Override
    public void consumeHiddenToken(Token token) {
        if (backtracking > 0 || resync) {
            return;
        }

        if (debug_tokens) {
            CommonToken ct = (CommonToken) token;
            int[] ctr = CommonTokenUtil.getCommonTokenOffsetRange(ct);
            System.out.println(token + "(" + ctr[0] + "-" + ctr[1] + ")");
        }

        hiddenTokens.add((CommonToken) token);
    }

    @Override
    @NbBundle.Messages({
        "# {0} - the unexpected token", 
        "MSG_Error_Unexpected_Token=Unexpected token {0} found", 
        "MSG_Error_Premature_EOF=Premature end of file"})
    public void recognitionException(RecognitionException e) {
        if (backtracking > 0) {
            return;
        }
        
        RuleNode ruleNode = callStack.peek();

        String message;
        int from, to;

        assert e.token != null;

        //invalid token found int the stream
        unexpectedToken = (CommonToken) e.token;
        int unexpectedTokenCode = e.getUnexpectedType();
        CssTokenId unexpectedTokenId = CssTokenId.forTokenTypeCode(unexpectedTokenCode);

        assert unexpectedTokenId != null : "No CssTokenId for " + unexpectedToken;

        //special handling for EOF token - it has lenght == 1 !
        if(unexpectedTokenId == CssTokenId.EOF) {
            from = CommonTokenUtil.getCommonTokenOffsetRange(unexpectedToken)[0];
            to = from;
        } else {
            //normal tokens
            from = CommonTokenUtil.getCommonTokenOffsetRange(unexpectedToken)[0]; 
            to = CommonTokenUtil.getCommonTokenOffsetRange(unexpectedToken)[1];
        }
      
        if (unexpectedTokenId == CssTokenId.EOF) {
            message = MSG_Error_Premature_EOF();
        } else {
            message = MSG_Error_Unexpected_Token(unexpectedTokenId.name());
        }
        
        //create a ParsingProblem
        ProblemDescription problemDescription = new ProblemDescription(
                from,
                to,
                message,
                ProblemDescription.Keys.PARSING.name(),
                ProblemDescription.Type.ERROR);

        problems.add(problemDescription);
        
        //create an error node and add it to the parse tree
        ErrorNode errorNode = new ErrorNode(from, to, problemDescription, source);

        //add the unexpected token as a child of the error node
        TokenNode tokenNode = new TokenNode(source, unexpectedToken);
        addNodeChild(errorNode, tokenNode);
        
        if(e instanceof NoViableAltException) {
            //error during predicate - the unexpected token may or may not be
            //reported later as an error. To handle this,
            //store the error node and the ruleNode where the error node should be added
            noViableAltNodes.put(unexpectedToken, new Pair<>(ruleNode, errorNode));
            errorNodes.push(errorNode);
        } else {
            //possibly remove the unexpectedToken from the noViableAltNodes map
            
            //NOTICE:
            //Uncomment the following line if you want the parse tree not to produce
            //multiple error nodes for the same token. If the line is active, there 
            //wont be error nodes for semantic predicates if the unexpected token
            //is matched by another error rule later.
//            noViableAltNodes.remove(unexpectedToken);
            
            addNodeChild(ruleNode, errorNode);
            errorNodes.push(errorNode);

            //create and artificial error token so the rules on stack can properly set their ranges
            lastConsumedToken = new CommonToken(Token.INVALID_TOKEN_TYPE);
            lastConsumedToken.setStartIndex(from);
            lastConsumedToken.setStopIndex(to - 1); // ... ( -1 => last *char* index )
        }


    }

    @Override
    public void terminate() {
        super.terminate();

        //process unreported errors from NoViableAltException
        for(Pair<Node> pair : noViableAltNodes.values()) {
            RuleNode ruleNode = (RuleNode)pair.n1;
            ErrorNode errorNode = (ErrorNode)pair.n2;
            
            ruleNode.addChild(errorNode);
            errorNode.setParent(ruleNode);
        }
        
        //Finally after the parsing is done fix the error nodes and their predecessors.
        //This fixes the problem with rules where RecognitionException happened
        //but the errorneous or missing token has been matched in somewhere further
        for (ErrorNode en : errorNodes) {
            synchronizeAncestorsBoundaries(en);
        }
        
        //clean parse tree from empty rule nodes 
        //empty rule node == rule node without a single _token node_ child
        for(RuleNode node : leafRuleNodes) {
            removeLeafRuleNodes(node);
        }

    }
    
    //removes all empty rule nodes in the tree path from the given node to the parse tree root
    @SuppressWarnings("AssignmentToMethodParameter")
    private void removeLeafRuleNodes(RuleNode node) {
        for(;;) {
            if(node.children().isEmpty()) {
                RuleNode parent = (RuleNode)node.parent();
                if(parent == null) {
                    return ;
                }
                parent.deleteChild(node);
                node = parent;
            } else {
                break;
            }
        }
    }
    
    private void synchronizeAncestorsBoundaries(RuleNode en) {
        RuleNode n = en;
            for (;;) {
                if (n == null) {
                    break;
                }
                
                //adjust the parent nodes ranges to the errorNode
                if (n.from() == -1 || n.from() > en.from()) {
                    n.from = en.from();
                }
                if(n.to() == -1 || n.to() < en.to()) {                    
                    n.to = en.to();
                }
                
                n = (RuleNode) n.parent();
            }
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public Collection<ProblemDescription> getProblems() {
        return problems;
    }

    //note: it would be possible to handle this all in consumeToken since it is called from the
    //BaseRecognizer.consumeUntil(...) {   input.consume();   } but for the better usability
    //it is done this way. So the beginResyn/endResync doesn't have to be used.
    //the NbParseTreeBuilder.consumeToken() method ignores tokens with ERROR type so they
    //won't be duplicated in the parse tree
    
    //creates a "recovery" node with all the skipped tokens as children
    void consumeSkippedTokens(List<Token> tokens) {
        if(tokens.isEmpty()) {
            return ;
        }

        CommonToken first = (CommonToken)tokens.get(0);
        CommonToken last = (CommonToken)tokens.get(tokens.size() - 1);
        
        

        //if there's just one recovered token and the token is the same as the unexpectedToken just skip the 
        //recovery node creation, the parse tree for the errorneous piece of code is already complete
        boolean ignoreFirstToken = unexpectedToken  == first;
        if(ignoreFirstToken && tokens.size() == 1) {
            return ;
        }
        
        //do not add the first token as children of the recovery node if it has been already
        //added as a child of the error node created for the RecognitionException
        if(ignoreFirstToken) {
            first = (CommonToken)tokens.get(1); //use second
        }
        
        //find last error which triggered this recovery and add the skipped tokens to it
//        ErrorNode errorNode = errorNodes.peek();
//        RuleNode peek = callStack.peek();
//        if(!(peek instanceof ErrorNode)) {
        
        RuleNode peek = errorNodes.peek();
        
            RuleNode node = new RuleNode(NodeType.recovery, source);
            peek.addChild(node);
            node.setParent(peek);
            peek = node;
            
//        }
            
        
        //set first and last token
        peek.setFirstToken(first);
        peek.setLastToken(last);
        
        synchronizeAncestorsBoundaries(peek);
        
        //set range
        peek.from = CommonTokenUtil.getCommonTokenOffsetRange(first)[0]; 
        peek.to = CommonTokenUtil.getCommonTokenOffsetRange(last)[1]; 
        
        //set the error tokens as children of the error node
        for(int i = (ignoreFirstToken ? 1 : 0); i < tokens.size(); i++) {
            CommonToken token = (CommonToken)tokens.get(i);
            TokenNode tokenNode = new TokenNode(source, token);
            addNodeChild(peek, tokenNode);
        }
        
        //create and artificial error token so the rules on stack can properly set their ranges
        lastConsumedToken = new CommonToken(Token.INVALID_TOKEN_TYPE);
        lastConsumedToken.setStartIndex(first.getStartIndex());
        lastConsumedToken.setStopIndex(last.getStopIndex()); 
                
    }
    
    
    private void addNodeChild(AbstractParseTreeNode parent, AbstractParseTreeNode child) {
        parent.addChild(child);
        child.setParent(parent);
    }
    
    private static class Pair<T> {
        final T n1, n2;
        public Pair(T n1, T n2) {
            this.n1 = n1;
            this.n2 = n2;
        }
    }
    
}
