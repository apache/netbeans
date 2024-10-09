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
package org.netbeans.modules.php.blade.syntax.antlr4.v10;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

/**
 *
 * @author bogdan
 */
public abstract class LexerAdaptor extends Lexer {

    private int _currentRuleType = Token.INVALID_TYPE;
    private int roundParenBalance = 0;
    private int squareParenBalance = 0;
    private int curlyParenBalance = 0;
    private boolean compomentTagOpen = false;

    public LexerAdaptor(CharStream input) {
        super(input);
    }

    public int getCurrentRuleType() {
        return _currentRuleType;
    }

    public void setCurrentRuleType(int ruleType) {
        this._currentRuleType = ruleType;
    }

    @Override
    public Token emit() {
        return super.emit();
    }

    @Override
    public void reset() {
        setCurrentRuleType(Token.INVALID_TYPE);
        super.reset();
    }
    
    public int getRoundParenBalance(){
        return this.roundParenBalance;
    }
    
    public void resetRoundParenBalance(){
        this.roundParenBalance = 0;
    }

    public void increaseRoundParenBalance() {
        this.roundParenBalance++;
    }

    public void decreaseRoundParenBalance() {
        this.roundParenBalance--;
    }
    
    public int getSquareParenBalance(){
        return this.squareParenBalance;
    }
    
    public void increaseSquareParenBalance() {
        this.squareParenBalance++;
    }

    public void decreaseSquareParenBalance() {
        this.squareParenBalance--;
    }

    public void increaseCurlyParenBalance() {
        this.curlyParenBalance++;
    }

    public void decreaseCurlyParenBalance() {
        this.curlyParenBalance--;
    }
    
    public boolean hasNoBladeParamOpenBracket() {
        return this.roundParenBalance == 0
                && this.squareParenBalance == 0
                && this.curlyParenBalance == 0;
    }
    
    public void consumeBladeParamComma(){
        if (this.hasNoBladeParamOpenBracket()){
            this.setType(BladeAntlrLexer.BL_COMMA);
        } else {
            this.setType(BladeAntlrLexer.BL_PARAM_COMMA);
        }
    }
    
    public void consumeRParen(){
        //we start from 0 balance
        this.roundParenBalance--;
        if (this.roundParenBalance < 0) {
            this.roundParenBalance = 0;
            this.setType(BladeAntlrLexer.BLADE_PARAM_RPAREN);
            this.mode(DEFAULT_MODE);
        } else {
             this.setType(BladeAntlrLexer.BLADE_PARAM_EXTRA);
        }
    }

    public void consumeParamRParen(){
        //we start from 0 balance
        this.roundParenBalance--;
        if (this.roundParenBalance < 0) {
            this.roundParenBalance = 0;
            this.setType(BladeAntlrLexer.BLADE_PARAM_RPAREN);
            this.mode(DEFAULT_MODE);
        } else {
             this.setType(BladeAntlrLexer.BLADE_PARAM_EXTRA);
        }
    }
    
    public void consumeExprRParen(){
        //we start from 0 balance
        this.roundParenBalance--;
        this.setType(BladeAntlrLexer.BLADE_EXPR_RPAREN);
        if (this.roundParenBalance < 0) {
            this.roundParenBalance = 0;
            this.mode(DEFAULT_MODE);
        }
    }

    public void setComponentTagOpenStatus(boolean status){
        this.compomentTagOpen = status;
    }
    
    public void consumeHtmlIdentifier(){
        if (this.compomentTagOpen == true) {
            this.setType(BladeAntlrLexer.HTML_IDENTIFIER);
        } else {
            this.setType(BladeAntlrLexer.HTML);
        }
    }
}
