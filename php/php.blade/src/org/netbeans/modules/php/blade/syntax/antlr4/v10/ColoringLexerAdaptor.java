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
public abstract class ColoringLexerAdaptor extends Lexer {

    private int _currentRuleType = Token.INVALID_TYPE;
    public int rParenBalance = 0;
    public int compAttrQuoteBalance = 0;
    public boolean insideComponentTag = false;

    public ColoringLexerAdaptor(CharStream input) {
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
        //setCurrentRuleType(Token.INVALID_TYPE);
        rParenBalance = 0;
        compAttrQuoteBalance = 0;
        insideComponentTag = false;
        super.reset();
    }

    //blade coloring lexer
    public void consumeEscapedEchoToken() {
        if (this._input.LA(1) == '}' && this._input.LA(2) == '}') {
            this.setType(BladeAntlrColoringLexer.BLADE_PHP_ECHO_EXPR);
        } else {
            this.more();
        }
    }

    //blade coloring lexer
    public void consumeNotEscapedEchoToken() {
        if (this._input.LA(1) == '!' && this._input.LA(2) == '!' && this._input.LA(3) == '}') {
            this.setType(BladeAntlrColoringLexer.BLADE_PHP_ECHO_EXPR);
        } else {
            this.more();
        }
    }
}

