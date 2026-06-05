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
package org.netbeans.modules.languages.jflex.grammar.antlr4.coloring;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public abstract class ColoringLexerAdaptor extends Lexer {

    private boolean ruleDefined = false;
    private boolean inRuleList = false;
    private int sqbracketBalance = 0;
    private int parenBalance = 0;
    private int curlyBalance = 0;
    private boolean inMacroAssign = false;

    public ColoringLexerAdaptor(CharStream input) {
        super(input);
    }

    @Override
    public void reset() {
        ruleDefined = false;
        sqbracketBalance = 0;
        parenBalance = 0;
        curlyBalance = 0;
        inMacroAssign = false;
        super.reset();
    }

    /* square brackets */
    public void incrementSQBracket() {
        this.sqbracketBalance++;
    }
    
    public void decrementSQBracket() {
        this.sqbracketBalance--;
    }
    
    public int getSQBracketBalance() {
        return sqbracketBalance;
    }
    
    public void setSQBracketBalance(int sqbracketBalance) {
        this.sqbracketBalance = sqbracketBalance;
    }

    /* parenthesis */
    public void openParenthesis() {
        this.parenBalance=1;
    }
    
    public void incrementParenthesis() {
        this.parenBalance++;
    }
    
    public void decrementParenthesis() {
        this.parenBalance--;
    }
    
    public int getParenthesisBalance() {
        return parenBalance;
    }
    
    public void setParenthesisBalance(int parenBalance) {
        this.parenBalance = parenBalance;
    }
    
    public void resetBrackets() {
        this.sqbracketBalance = 0;
        this.parenBalance = 0;
    }
    
    /* curly */
    public void incrementCurlyBracket() {
        this.curlyBalance++;
    }
    
    public void decrementCurlyBracket() {
        this.curlyBalance--;
    }
    
    public int getCurlyBracketBalance() {
        return curlyBalance;
    }
    
    public void setCurlyBracketBalance(int curlyBalance) {
        this.curlyBalance = curlyBalance;
    }
    
    /* rule definition status */
    public void setRuleDefined(boolean state) {
        ruleDefined = state;
    }

    public boolean isRuleDefined() {
        return ruleDefined;
    }

        
    /* rule definition status */
    public void setInRuleList(boolean state) {
        inRuleList = state;
    }

    public boolean isInRuleList() {
        return inRuleList;
    }
    
    /* in macro asign (after '=') */
    public void setInMacroAssign(boolean state) {
        inMacroAssign = state;
    }

    public boolean isInMacroAssign() {
        return inMacroAssign;
    }
}
