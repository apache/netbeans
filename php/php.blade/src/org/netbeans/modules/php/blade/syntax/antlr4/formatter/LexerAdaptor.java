/*
Licensed to the Apache Software Foundation (ASF)
 */
package org.netbeans.modules.php.blade.syntax.antlr4.formatter;

import org.netbeans.modules.php.blade.syntax.antlr4.v10.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

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

    public void consumeDirectiveArgLParen() {
        if (this.roundParenBalance == 0) {
            this.setType(BladeAntlrFormatterLexer.D_ARG_LPAREN);
        } else {
            this.skip();
        }
        this.roundParenBalance++;
    }

    public void consumeDirectiveArgRParen() {
        //we start from 0 balance
        this.roundParenBalance--;
        System.out.println("balance " + this.roundParenBalance);
        if (this.roundParenBalance <= 0) {
            this.setType(BladeAntlrFormatterLexer.D_ARG_RPAREN);
            this.roundParenBalance = 0;
            this.mode(DEFAULT_MODE);
        } else {
            this.skip();
        }
    }

    public void consumeBladeParamComma() {
        if (this.hasNoBladeParamOpenBracket()) {
            this.setType(BladeAntlrFormatterLexer.PARAM_COMMA);
        } else {
            this.skip();
        }
    }

    public boolean hasNoBladeParamOpenBracket() {
        return this.roundParenBalance == 1
                && this.squareParenBalance == 0
                && this.curlyParenBalance == 0;
    }
}
