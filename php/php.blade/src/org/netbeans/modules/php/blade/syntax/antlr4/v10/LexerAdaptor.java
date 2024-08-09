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
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.misc.Interval;

/**
 *
 * @author bogdan
 */
public abstract class LexerAdaptor extends Lexer {

    private int _currentRuleType = Token.INVALID_TYPE;
    public int roundParenBalance = 0;
    public int squareParenBalance = 0;
    public int curlyParenBalance = 0;
    public int exitIfModePosition = 0;
    public boolean compomentTagOpen = false;

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

    /**
     * eager check to see if the character position in a line is at the start
     *
     * @return
     */
    public boolean IsNewLineOrStart() {
        return this._tokenStartCharPositionInLine <= 2;
    }

    public boolean peekNextChar(char peekChar) {
        return (char) this._input.LA(1) == peekChar;
    }

    public boolean peekNextChars(char peekChar, int number) {
        for (int i = 1; i < number; i++) {
            if ((char) this._input.LA(i) != peekChar) {
                return false;
            }
        }
        return true;
    }

    public void increaseRoundParenBalance() {
        this.roundParenBalance++;
    }

    public void decreaseRoundParenBalance() {
        this.roundParenBalance--;
    }

    public boolean endsWith(char ch1, char ch2) {
        return this._input.LA(1) == ch1 && this._input.LA(2) == ch2;
    }

    public boolean endsWith(char ch1, char ch2, char ch3) {
        return this._input.LA(1) == ch1
                && this._input.LA(2) == ch2
                && this._input.LA(3) == ch3;
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

    
    public void consumeHtmlIdentifier(){
        if (this.compomentTagOpen == true) {
            this.setType(BladeAntlrLexer.HTML_IDENTIFIER);
        } else {
            this.setType(BladeAntlrLexer.HTML);
        }
    }
}
