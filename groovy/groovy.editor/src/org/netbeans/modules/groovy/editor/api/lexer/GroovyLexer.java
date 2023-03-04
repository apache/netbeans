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

package org.netbeans.modules.groovy.editor.api.lexer;

import groovyjarjarantlr.CharBuffer;
import groovyjarjarantlr.CharQueue;
import groovyjarjarantlr.CharStreamException;
import groovyjarjarantlr.LexerSharedInputState;
import groovyjarjarantlr.TokenStreamException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexer based on old coyote groovy lexer.
 *
 * @todo cannot call lexerInput.readText() in some test, because it goes wrong on some EOFs
 * @todo curly braces in expression in gstring should be resolved as STRING_LITERAL?
 *
 * @author Mila Metelka
 * @author Martin Adamek
 */
public final class GroovyLexer implements Lexer<GroovyTokenId> {

    private static final Logger LOG = Logger.getLogger(GroovyLexer.class.getName());

    private DelegateLexer scanner;
    private LexerInput lexerInput;
    private MyCharBuffer myCharBuffer;
    private TokenFactory<GroovyTokenId> tokenFactory;
    private final GroovyRecognizer parser;

    public GroovyLexer(LexerRestartInfo<GroovyTokenId> info) {
        this.scanner = new DelegateLexer(null);
        scanner.setWhitespaceIncluded(true);
        parser = GroovyRecognizer.make(scanner);
        restart(info);
    }

    private void restart(LexerRestartInfo<GroovyTokenId> info) {
        tokenFactory = info.tokenFactory();
        this.lexerInput = info.input();

        LexerSharedInputState inputState = null;
        if (lexerInput != null) {
            myCharBuffer = new MyCharBuffer(new LexerInputReader(lexerInput));
            inputState = new LexerSharedInputState(myCharBuffer);
        }
        scanner.setInputState(inputState);
        if (inputState != null) {
            scanner.resetText();
        }
        scanner.setState((State) info.state());
    }

    private void scannerConsumeChar() {
        try {
            scanner.consume();
        } catch (CharStreamException e) {
            throw new IllegalStateException();
        }
    }

    private Token<GroovyTokenId> createToken(int tokenIntId, int tokenLength) {
        GroovyTokenId id = getTokenId(tokenIntId);
        LOG.log(Level.FINEST, "Creating token: {0}, length: {1}", new Object[]{id.name(), tokenLength});
        String fixedText = id.fixedText();
        return (fixedText != null) ? tokenFactory.getFlyweightToken(id, fixedText)
                                   : tokenFactory.createToken(id, tokenLength);
    }

    // token index used in nextToken()
    private int index = 1;

    @Override
    public Token<GroovyTokenId> nextToken() {
        LOG.finest("");
        try {
            groovyjarjarantlr.Token antlrToken = parser.LT(index++);
            LOG.log(Level.FINEST, "Received token from antlr: {0}", antlrToken);
            if (antlrToken != null) {
                int intId = antlrToken.getType();

                int len = lexerInput.readLengthEOF() - myCharBuffer.getExtraCharCount();
                if (antlrToken.getText() != null) {
                    len = Math.max(len, antlrToken.getText().length());
                    LOG.log(Level.FINEST, "Counting length from {0} and {1}", new Object[]{lexerInput.readLengthEOF(), myCharBuffer.getExtraCharCount()});
                }
                LOG.log(Level.FINEST, "Length of token to create: {0}", len);

                switch (intId) {
                    case GroovyTokenTypes.STRING_CTOR_START:
                    case GroovyTokenTypes.STRING_CTOR_MIDDLE:
                    case GroovyTokenTypes.STRING_CTOR_END:
                        intId = GroovyTokenTypes.STRING_LITERAL;
                        break;
                    case GroovyTokenTypes.EOF:
                        if (lexerInput.readLength() > 0) {
                            return recovery();
                        }
                        return null;
                }

                return createToken(intId, len);

            } else {
                LOG.finest("Antlr token was null");
                int scannerTextTokenLength = scanner.getText().length();
                if (scannerTextTokenLength > 0) {
                    return createToken(GroovyTokenTypes.WS, scannerTextTokenLength);
                }
                return null;  // no more tokens from tokenManager
            }
        } catch (TokenStreamException e) {
            LOG.log(Level.FINEST, "Caught exception: {0}", e);
            return recovery();
        }
    }

    @Override
    public Object state() {
        return scanner.getState();
    }

    @Override
    public void release() {
    }

    private Token<GroovyTokenId> recovery() {
        int len = lexerInput.readLength() - myCharBuffer.getExtraCharCount();
        int tokenLength = lexerInput.readLength();

        scanner.resetText();

        while (len < tokenLength) {
            LOG.finest("Consuming character");
            scannerConsumeChar();
            len++;
        }
        return tokenLength > 0 ? createToken(GroovyTokenId.ERROR.ordinal(), tokenLength) : null;
    }

    private static class MyCharBuffer extends CharBuffer {
        public MyCharBuffer(Reader reader) {
            super(reader);
            queue = new MyCharQueue(1);
        }

        public int getExtraCharCount() {
            syncConsume();
            return ((MyCharQueue) queue).getNbrEntries();
        }

    }

    private static class MyCharQueue extends CharQueue {
        public MyCharQueue(int minSize) {
            super(minSize);
        }

        public int getNbrEntries() {
            return nbrEntries;
        }
    }

    private static class LexerInputReader extends Reader {
        private LexerInput input;

        LexerInputReader(LexerInput input) {
            this.input = input;
        }

        @Override
        public int read(char[] buf, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                int c = input.read();
                if (c == LexerInput.EOF) {
                    return -1;
                }
                buf[i + off] = (char) c;
            }
            return len;
        }

        @Override
        public void close() throws IOException {
        }
    }

    /**
     * Thin wrapper around lexer to have acces to internal state of lexer itself
     */
    @SuppressWarnings("unchecked")
    private static class DelegateLexer extends org.codehaus.groovy.antlr.parser.GroovyLexer {

        public DelegateLexer(LexerSharedInputState state) {
            super(state);
        }

        public State getState() {
            if (stringCtorState > 0 || !parenLevelStack.isEmpty()) {
                return new State(stringCtorState, parenLevelStack);
            }
            return null;
        }

        public void setState(State d) {
            if (d != null) {
                stringCtorState = d.stringCtorState;
                parenLevelStack = new ArrayList<>(d.parenLevelStack);
            }
        }
    }

    /**
     * Holds state of lexer, which is needed to recover in incremental parsing
     * in expressions used in GStrings
     */
    @SuppressWarnings("unchecked")
    private static class State {

        private final int stringCtorState;
        private final List parenLevelStack;

        public State(int stringCtorState, ArrayList parenLevelStack) {
            this.stringCtorState = stringCtorState;
            this.parenLevelStack = new ArrayList<>(parenLevelStack);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final State other = (State) obj;
            if (this.stringCtorState != other.stringCtorState) {
                return false;
            }
            if (this.parenLevelStack == null || !this.parenLevelStack.equals(other.parenLevelStack)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + this.stringCtorState;
            hash = 67 * hash + (this.parenLevelStack != null ? this.parenLevelStack.hashCode() : 0);
            return hash;
        }
    }

    private GroovyTokenId getTokenId(int token) {
        switch (token) {
            case GroovyTokenTypes.ABSTRACT:
                    return GroovyTokenId.LITERAL_abstract;
            case GroovyTokenTypes.ANNOTATION_ARRAY_INIT:
                    return GroovyTokenId.ANNOTATION_ARRAY_INIT;
            case GroovyTokenTypes.ANNOTATION_DEF:
                    return GroovyTokenId.ANNOTATION_DEF;
            case GroovyTokenTypes.ANNOTATION_FIELD_DEF:
                    return GroovyTokenId.ANNOTATION_FIELD_DEF;
            case GroovyTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR:
                    return GroovyTokenId.ANNOTATION_MEMBER_VALUE_PAIR;
            case GroovyTokenTypes.ANNOTATION:
                    return GroovyTokenId.ANNOTATION;
            case GroovyTokenTypes.ANNOTATIONS:
                    return GroovyTokenId.ANNOTATIONS;
            case GroovyTokenTypes.ARRAY_DECLARATOR:
                    return GroovyTokenId.ARRAY_DECLARATOR;
            case GroovyTokenTypes.ASSIGN:
                    return GroovyTokenId.ASSIGN;
            case GroovyTokenTypes.AT:
                    return GroovyTokenId.AT;
            case GroovyTokenTypes.BAND_ASSIGN:
                    return GroovyTokenId.BAND_ASSIGN;
            case GroovyTokenTypes.BAND:
                    return GroovyTokenId.BAND;
            case GroovyTokenTypes.BIG_SUFFIX:
                    return GroovyTokenId.BIG_SUFFIX;
            case GroovyTokenTypes.BLOCK:
                    return GroovyTokenId.BLOCK;
            case GroovyTokenTypes.BNOT:
                    return GroovyTokenId.BNOT;
            case GroovyTokenTypes.BOR_ASSIGN:
                    return GroovyTokenId.BOR_ASSIGN;
            case GroovyTokenTypes.BOR:
                    return GroovyTokenId.BOR;
            case GroovyTokenTypes.BSR_ASSIGN:
                    return GroovyTokenId.BSR_ASSIGN;
            case GroovyTokenTypes.BSR:
                    return GroovyTokenId.BSR;
            case GroovyTokenTypes.BXOR_ASSIGN:
                    return GroovyTokenId.BXOR_ASSIGN;
            case GroovyTokenTypes.BXOR:
                    return GroovyTokenId.BXOR;
            case GroovyTokenTypes.CASE_GROUP:
                    return GroovyTokenId.CASE_GROUP;
            case GroovyTokenTypes.CLASS_DEF:
                    return GroovyTokenId.CLASS_DEF;
            case GroovyTokenTypes.CLOSABLE_BLOCK:
                    return GroovyTokenId.CLOSED_BLOCK;
            case GroovyTokenTypes.CLOSABLE_BLOCK_OP:
                return GroovyTokenId.CLOSED_BLOCK_OP;
            case GroovyTokenTypes.CLOSURE_LIST:
                    return GroovyTokenId.CLOSURE_OP;
            case GroovyTokenTypes.COLON:
                    return GroovyTokenId.COLON;
            case GroovyTokenTypes.COMMA:
                    return GroovyTokenId.COMMA;
            case GroovyTokenTypes.COMPARE_TO:
                    return GroovyTokenId.COMPARE_TO;
            case GroovyTokenTypes.CTOR_CALL:
                    return GroovyTokenId.CTOR_CALL;
            case GroovyTokenTypes.CTOR_IDENT:
                    return GroovyTokenId.CTOR_IDENT;
            case GroovyTokenTypes.DEC:
                    return GroovyTokenId.DEC;
            case GroovyTokenTypes.DIGIT:
                    return GroovyTokenId.DIGIT;
            case GroovyTokenTypes.DIGITS_WITH_UNDERSCORE:
                    return GroovyTokenId.DIGITS_WITH_UNDERSCORE;
            case GroovyTokenTypes.DIGITS_WITH_UNDERSCORE_OPT:
                    return GroovyTokenId.DIGITS_WITH_UNDERSCORE_OPT;
            case GroovyTokenTypes.DIV_ASSIGN:
                    return GroovyTokenId.DIV_ASSIGN;
            case GroovyTokenTypes.DIV:
                    return GroovyTokenId.DIV;
            case GroovyTokenTypes.DOLLAR:
                    return GroovyTokenId.DOLLAR;
            case GroovyTokenTypes.DOLLAR_REGEXP_CTOR_END:
                    return GroovyTokenId.DOLLAR_REGEXP_CTOR_END;
            case GroovyTokenTypes.DOLLAR_REGEXP_LITERAL:
                    return GroovyTokenId.DOLLAR_REGEXP_LITERAL;
            case GroovyTokenTypes.DOLLAR_REGEXP_SYMBOL:
                    return GroovyTokenId.DOLLAR_REGEXP_SYMBOL;
            case GroovyTokenTypes.DOT:
                    return GroovyTokenId.DOT;
            case GroovyTokenTypes.DYNAMIC_MEMBER:
                    return GroovyTokenId.DYNAMIC_MEMBER;
            case GroovyTokenTypes.ELIST:
                    return GroovyTokenId.ELIST;
            case GroovyTokenTypes.ELVIS_OPERATOR:
                return GroovyTokenId.ELVIS_OPERATOR;
            case GroovyTokenTypes.EMPTY_STAT:
                    return GroovyTokenId.EMPTY_STAT;
            case GroovyTokenTypes.ENUM_CONSTANT_DEF:
                    return GroovyTokenId.ENUM_CONSTANT_DEF;
            case GroovyTokenTypes.ENUM_DEF:
                    return GroovyTokenId.ENUM_DEF;
            case GroovyTokenTypes.EOF:
                    return GroovyTokenId.EOF;
            case GroovyTokenTypes.EQUAL:
                    return GroovyTokenId.EQUAL;
            case GroovyTokenTypes.ESC:
                    return GroovyTokenId.ESC;
            case GroovyTokenTypes.ESCAPED_DOLLAR:
                    return GroovyTokenId.ESCAPED_DOLLAR;
            case GroovyTokenTypes.ESCAPED_SLASH:
                    return GroovyTokenId.ESCAPED_SLASH;
            case GroovyTokenTypes.EXPONENT:
                    return GroovyTokenId.EXPONENT;
            case GroovyTokenTypes.EXPR:
                    return GroovyTokenId.EXPR;
            case GroovyTokenTypes.EXTENDS_CLAUSE:
                    return GroovyTokenId.EXTENDS_CLAUSE;
            case GroovyTokenTypes.FINAL:
                    return GroovyTokenId.LITERAL_final;
            case GroovyTokenTypes.FLOAT_SUFFIX:
                    return GroovyTokenId.FLOAT_SUFFIX;
            case GroovyTokenTypes.FOR_CONDITION:
                    return GroovyTokenId.FOR_CONDITION;
            case GroovyTokenTypes.FOR_EACH_CLAUSE:
                    return GroovyTokenId.FOR_EACH_CLAUSE;
            case GroovyTokenTypes.FOR_IN_ITERABLE:
                    return GroovyTokenId.FOR_IN_ITERABLE;
            case GroovyTokenTypes.FOR_INIT:
                    return GroovyTokenId.FOR_INIT;
            case GroovyTokenTypes.FOR_ITERATOR:
                    return GroovyTokenId.FOR_ITERATOR;
            case GroovyTokenTypes.GE:
                    return GroovyTokenId.GE;
            case GroovyTokenTypes.GT:
                    return GroovyTokenId.GT;
            case GroovyTokenTypes.HEX_DIGIT:
                    return GroovyTokenId.HEX_DIGIT;
            case GroovyTokenTypes.IDENT:
                    return GroovyTokenId.IDENTIFIER;
            case GroovyTokenTypes.IDENTICAL:
                    return GroovyTokenId.IDENTICAL;
            case GroovyTokenTypes.IMPLEMENTS_CLAUSE:
                    return GroovyTokenId.IMPLEMENTS_CLAUSE;
            case GroovyTokenTypes.IMPLICIT_PARAMETERS:
                    return GroovyTokenId.IMPLICIT_PARAMETERS;
            case GroovyTokenTypes.IMPORT:
                    return GroovyTokenId.IMPORT;
            case GroovyTokenTypes.INC:
                    return GroovyTokenId.INC;
            case GroovyTokenTypes.INDEX_OP:
                    return GroovyTokenId.INDEX_OP;
            case GroovyTokenTypes.INSTANCE_INIT:
                    return GroovyTokenId.INSTANCE_INIT;
            case GroovyTokenTypes.INTERFACE_DEF:
                    return GroovyTokenId.INTERFACE_DEF;
            case GroovyTokenTypes.LABELED_ARG:
                    return GroovyTokenId.LABELED_ARG;
            case GroovyTokenTypes.LABELED_STAT:
                    return GroovyTokenId.LABELED_STAT;
            case GroovyTokenTypes.LAND:
                    return GroovyTokenId.LAND;
            case GroovyTokenTypes.LBRACK:
                    return GroovyTokenId.LBRACKET;
            case GroovyTokenTypes.LCURLY:
                    return GroovyTokenId.LBRACE;
            case GroovyTokenTypes.LE:
                    return GroovyTokenId.LE;
            case GroovyTokenTypes.LETTER:
                    return GroovyTokenId.LETTER;
            case GroovyTokenTypes.LIST_CONSTRUCTOR:
                    return GroovyTokenId.LIST_CONSTRUCTOR;
            case GroovyTokenTypes.LITERAL_as:
                    return GroovyTokenId.LITERAL_as;
            case GroovyTokenTypes.LITERAL_assert:
                    return GroovyTokenId.LITERAL_assert;
            case GroovyTokenTypes.LITERAL_boolean:
                    return GroovyTokenId.LITERAL_boolean;
            case GroovyTokenTypes.LITERAL_break:
                    return GroovyTokenId.LITERAL_break;
            case GroovyTokenTypes.LITERAL_byte:
                    return GroovyTokenId.LITERAL_byte;
            case GroovyTokenTypes.LITERAL_case:
                    return GroovyTokenId.LITERAL_case;
            case GroovyTokenTypes.LITERAL_catch:
                    return GroovyTokenId.LITERAL_catch;
            case GroovyTokenTypes.LITERAL_char:
                    return GroovyTokenId.LITERAL_char;
            case GroovyTokenTypes.LITERAL_class:
                    return GroovyTokenId.LITERAL_class;
            case GroovyTokenTypes.LITERAL_continue:
                    return GroovyTokenId.LITERAL_continue;
            case GroovyTokenTypes.LITERAL_def:
                    return GroovyTokenId.LITERAL_def;
            case GroovyTokenTypes.LITERAL_default:
                    return GroovyTokenId.LITERAL_default;
            case GroovyTokenTypes.LITERAL_double:
                    return GroovyTokenId.LITERAL_double;
            case GroovyTokenTypes.LITERAL_else:
                    return GroovyTokenId.LITERAL_else;
            case GroovyTokenTypes.LITERAL_enum:
                    return GroovyTokenId.LITERAL_enum;
            case GroovyTokenTypes.LITERAL_extends:
                    return GroovyTokenId.LITERAL_extends;
            case GroovyTokenTypes.LITERAL_false:
                    return GroovyTokenId.LITERAL_false;
            case GroovyTokenTypes.LITERAL_finally:
                    return GroovyTokenId.LITERAL_finally;
            case GroovyTokenTypes.LITERAL_float:
                    return GroovyTokenId.LITERAL_float;
            case GroovyTokenTypes.LITERAL_for:
                    return GroovyTokenId.LITERAL_for;
            case GroovyTokenTypes.LITERAL_if:
                    return GroovyTokenId.LITERAL_if;
            case GroovyTokenTypes.LITERAL_implements:
                    return GroovyTokenId.LITERAL_implements;
            case GroovyTokenTypes.LITERAL_import:
                    return GroovyTokenId.LITERAL_import;
            case GroovyTokenTypes.LITERAL_in:
                    return GroovyTokenId.LITERAL_in;
            case GroovyTokenTypes.LITERAL_instanceof:
                    return GroovyTokenId.LITERAL_instanceof;
            case GroovyTokenTypes.LITERAL_int:
                    return GroovyTokenId.LITERAL_int;
            case GroovyTokenTypes.LITERAL_interface:
                    return GroovyTokenId.LITERAL_interface;
            case GroovyTokenTypes.LITERAL_long:
                    return GroovyTokenId.LITERAL_long;
            case GroovyTokenTypes.LITERAL_native:
                    return GroovyTokenId.LITERAL_native;
            case GroovyTokenTypes.LITERAL_new:
                    return GroovyTokenId.LITERAL_new;
            case GroovyTokenTypes.LITERAL_null:
                    return GroovyTokenId.LITERAL_null;
            case GroovyTokenTypes.LITERAL_package:
                    return GroovyTokenId.LITERAL_package;
            case GroovyTokenTypes.LITERAL_private:
                    return GroovyTokenId.LITERAL_private;
            case GroovyTokenTypes.LITERAL_protected:
                    return GroovyTokenId.LITERAL_protected;
            case GroovyTokenTypes.LITERAL_public:
                    return GroovyTokenId.LITERAL_public;
            case GroovyTokenTypes.LITERAL_return:
                    return GroovyTokenId.LITERAL_return;
            case GroovyTokenTypes.LITERAL_short:
                    return GroovyTokenId.LITERAL_short;
            case GroovyTokenTypes.LITERAL_static:
                    return GroovyTokenId.LITERAL_static;
            case GroovyTokenTypes.LITERAL_super:
                    return GroovyTokenId.LITERAL_super;
            case GroovyTokenTypes.LITERAL_switch:
                    return GroovyTokenId.LITERAL_switch;
            case GroovyTokenTypes.LITERAL_synchronized:
                    return GroovyTokenId.LITERAL_synchronized;
            case GroovyTokenTypes.LITERAL_this:
                    return GroovyTokenId.LITERAL_this;
            case GroovyTokenTypes.LITERAL_threadsafe:
                    return GroovyTokenId.LITERAL_threadsafe;
            case GroovyTokenTypes.LITERAL_throw:
                    return GroovyTokenId.LITERAL_throw;
            case GroovyTokenTypes.LITERAL_throws:
                    return GroovyTokenId.LITERAL_throws;
            case GroovyTokenTypes.LITERAL_trait:
                    return GroovyTokenId.LITERAL_trait;
            case GroovyTokenTypes.LITERAL_transient:
                    return GroovyTokenId.LITERAL_transient;
            case GroovyTokenTypes.LITERAL_true:
                    return GroovyTokenId.LITERAL_true;
            case GroovyTokenTypes.LITERAL_try:
                    return GroovyTokenId.LITERAL_try;
            case GroovyTokenTypes.LITERAL_void:
                    return GroovyTokenId.LITERAL_void;
            case GroovyTokenTypes.LITERAL_volatile:
                    return GroovyTokenId.LITERAL_volatile;
            case GroovyTokenTypes.LITERAL_while:
                    return GroovyTokenId.LITERAL_while;
            case GroovyTokenTypes.LNOT:
                    return GroovyTokenId.LNOT;
            case GroovyTokenTypes.LOR:
                    return GroovyTokenId.LOR;
            case GroovyTokenTypes.LPAREN:
                    return GroovyTokenId.LPAREN;
            case GroovyTokenTypes.LT:
                    return GroovyTokenId.LT;
            case GroovyTokenTypes.MAP_CONSTRUCTOR:
                    return GroovyTokenId.MAP_CONSTRUCTOR;
            case GroovyTokenTypes.MEMBER_POINTER:
                    return GroovyTokenId.MEMBER_POINTER;
            case GroovyTokenTypes.METHOD_CALL:
                    return GroovyTokenId.METHOD_CALL;
            case GroovyTokenTypes.METHOD_DEF:
                    return GroovyTokenId.METHOD_DEF;
            case GroovyTokenTypes.MINUS_ASSIGN:
                    return GroovyTokenId.MINUS_ASSIGN;
            case GroovyTokenTypes.MINUS:
                    return GroovyTokenId.MINUS;
            case GroovyTokenTypes.ML_COMMENT:
                    return GroovyTokenId.BLOCK_COMMENT;
            case GroovyTokenTypes.MOD_ASSIGN:
                    return GroovyTokenId.MOD_ASSIGN;
            case GroovyTokenTypes.MOD:
                    return GroovyTokenId.MOD;
            case GroovyTokenTypes.MODIFIERS:
                    return GroovyTokenId.MODIFIERS;
            case GroovyTokenTypes.MULTICATCH:
                    return GroovyTokenId.MULTICATCH;
            case GroovyTokenTypes.MULTICATCH_TYPES:
                    return GroovyTokenId.MULTICATCH_TYPES;
            case GroovyTokenTypes.NLS:
                    return GroovyTokenId.NLS;
            case GroovyTokenTypes.NOT_EQUAL:
                    return GroovyTokenId.NOT_EQUAL;
            case GroovyTokenTypes.NOT_IDENTICAL:
                return GroovyTokenId.NOT_IDENTICAL;
            case GroovyTokenTypes.NULL_TREE_LOOKAHEAD:
                    return GroovyTokenId.NULL_TREE_LOOKAHEAD;
            case GroovyTokenTypes.NUM_BIG_DECIMAL:
                    return GroovyTokenId.NUM_BIG_DECIMAL;
            case GroovyTokenTypes.NUM_BIG_INT:
                    return GroovyTokenId.NUM_BIG_INT;
            case GroovyTokenTypes.NUM_DOUBLE:
                    return GroovyTokenId.NUM_DOUBLE;
            case GroovyTokenTypes.NUM_FLOAT:
                    return GroovyTokenId.NUM_FLOAT;
            case GroovyTokenTypes.NUM_INT:
                    return GroovyTokenId.NUM_INT;
            case GroovyTokenTypes.NUM_LONG:
                    return GroovyTokenId.NUM_LONG;
            case GroovyTokenTypes.OBJBLOCK:
                    return GroovyTokenId.OBJBLOCK;
            case GroovyTokenTypes.ONE_NL:
                    return GroovyTokenId.ONE_NL;
            case GroovyTokenTypes.OPTIONAL_DOT:
                    return GroovyTokenId.OPTIONAL_DOT;
            case GroovyTokenTypes.PACKAGE_DEF:
                    return GroovyTokenId.PACKAGE_DEF;
            case GroovyTokenTypes.PARAMETER_DEF:
                    return GroovyTokenId.PARAMETER_DEF;
            case GroovyTokenTypes.PARAMETERS:
                    return GroovyTokenId.PARAMETERS;
            case GroovyTokenTypes.PLUS_ASSIGN:
                    return GroovyTokenId.PLUS_ASSIGN;
            case GroovyTokenTypes.PLUS:
                    return GroovyTokenId.PLUS;
            case GroovyTokenTypes.POST_DEC:
                    return GroovyTokenId.POST_DEC;
            case GroovyTokenTypes.POST_INC:
                    return GroovyTokenId.POST_INC;
            case GroovyTokenTypes.QUESTION:
                    return GroovyTokenId.QUESTION;
            case GroovyTokenTypes.RANGE_EXCLUSIVE:
                    return GroovyTokenId.RANGE_EXCLUSIVE;
            case GroovyTokenTypes.RANGE_INCLUSIVE:
                    return GroovyTokenId.RANGE_INCLUSIVE;
            case GroovyTokenTypes.RCURLY:
                    return GroovyTokenId.RBRACE;
            case GroovyTokenTypes.RBRACK:
                    return GroovyTokenId.RBRACKET;
            case GroovyTokenTypes.REGEXP_CTOR_END:
                    return GroovyTokenId.REGEXP_CTOR_END;
            case GroovyTokenTypes.REGEXP_LITERAL:
                    return GroovyTokenId.REGEXP_LITERAL;
            case GroovyTokenTypes.REGEXP_SYMBOL:
                    return GroovyTokenId.REGEXP_SYMBOL;
            case GroovyTokenTypes.REGEX_FIND:
                    return GroovyTokenId.REGEX_FIND;
            case GroovyTokenTypes.REGEX_MATCH:
                    return GroovyTokenId.REGEX_MATCH;
            case GroovyTokenTypes.RPAREN:
                    return GroovyTokenId.RPAREN;
            case GroovyTokenTypes.SELECT_SLOT:
                    return GroovyTokenId.SELECT_SLOT;
            case GroovyTokenTypes.SEMI:
                    return GroovyTokenId.SEMI;
            case GroovyTokenTypes.SH_COMMENT:
                    return GroovyTokenId.LINE_COMMENT;
            case GroovyTokenTypes.SL_ASSIGN:
                    return GroovyTokenId.SL_ASSIGN;
            case GroovyTokenTypes.SL_COMMENT:
                    return GroovyTokenId.LINE_COMMENT;
            case GroovyTokenTypes.SL:
                    return GroovyTokenId.SL;
            case GroovyTokenTypes.SLIST:
                    return GroovyTokenId.SLIST;
            case GroovyTokenTypes.SPREAD_ARG:
                    return GroovyTokenId.SPREAD_ARG;
            case GroovyTokenTypes.SPREAD_DOT:
                    return GroovyTokenId.SPREAD_DOT;
            case GroovyTokenTypes.SPREAD_MAP_ARG:
                    return GroovyTokenId.SPREAD_MAP_ARG;
            case GroovyTokenTypes.SR_ASSIGN:
                    return GroovyTokenId.SR_ASSIGN;
            case GroovyTokenTypes.SR:
                    return GroovyTokenId.SR;
            case GroovyTokenTypes.STAR_ASSIGN:
                    return GroovyTokenId.STAR_ASSIGN;
            case GroovyTokenTypes.STAR_STAR_ASSIGN:
                    return GroovyTokenId.STAR_STAR_ASSIGN;
            case GroovyTokenTypes.STAR_STAR:
                    return GroovyTokenId.STAR_STAR;
            case GroovyTokenTypes.STAR:
                    return GroovyTokenId.STAR;
            case GroovyTokenTypes.STATIC_IMPORT:
                    return GroovyTokenId.STATIC_IMPORT;
            case GroovyTokenTypes.STATIC_INIT:
                    return GroovyTokenId.STATIC_INIT;
            case GroovyTokenTypes.STRICTFP:
                    return GroovyTokenId.STRICTFP;
            case GroovyTokenTypes.STRING_CONSTRUCTOR:
                    return GroovyTokenId.STRING_CONSTRUCTOR;
            case GroovyTokenTypes.STRING_CTOR_END:
                    return GroovyTokenId.STRING_CTOR_END;
            case GroovyTokenTypes.STRING_CTOR_MIDDLE:
                    return GroovyTokenId.STRING_CTOR_MIDDLE;
            case GroovyTokenTypes.STRING_CTOR_START:
                    return GroovyTokenId.STRING_CTOR_START;
            case GroovyTokenTypes.STRING_CH:
                    return GroovyTokenId.STRING_CH;
            case GroovyTokenTypes.STRING_LITERAL:
                    return GroovyTokenId.STRING_LITERAL;
            case GroovyTokenTypes.STRING_NL:
                    return GroovyTokenId.STRING_NL;
            case GroovyTokenTypes.SUPER_CTOR_CALL:
                    return GroovyTokenId.SUPER_CTOR_CALL;
            case GroovyTokenTypes.TRIPLE_DOT:
                    return GroovyTokenId.TRIPLE_DOT;
            case GroovyTokenTypes.TRAIT_DEF:
                    return GroovyTokenId.TRAIT_DEF;
            case GroovyTokenTypes.TYPE_ARGUMENT:
                    return GroovyTokenId.TYPE_ARGUMENT;
            case GroovyTokenTypes.TYPE_ARGUMENTS:
                    return GroovyTokenId.TYPE_ARGUMENTS;
            case GroovyTokenTypes.TYPE_LOWER_BOUNDS:
                    return GroovyTokenId.TYPE_LOWER_BOUNDS;
            case GroovyTokenTypes.TYPE_PARAMETER:
                    return GroovyTokenId.TYPE_PARAMETER;
            case GroovyTokenTypes.TYPE_PARAMETERS:
                    return GroovyTokenId.TYPE_PARAMETERS;
            case GroovyTokenTypes.TYPE_UPPER_BOUNDS:
                    return GroovyTokenId.TYPE_UPPER_BOUNDS;
            case GroovyTokenTypes.TYPE:
                    return GroovyTokenId.TYPE;
            case GroovyTokenTypes.TYPECAST:
                    return GroovyTokenId.TYPECAST;
            case GroovyTokenTypes.UNARY_MINUS:
                    return GroovyTokenId.UNARY_MINUS;
            case GroovyTokenTypes.UNARY_PLUS:
                    return GroovyTokenId.UNARY_PLUS;
            case GroovyTokenTypes.UNUSED_CONST:
                    return GroovyTokenId.UNUSED_CONST;
            case GroovyTokenTypes.UNUSED_DO:
                    return GroovyTokenId.UNUSED_DO;
            case GroovyTokenTypes.UNUSED_GOTO:
                    return GroovyTokenId.UNUSED_GOTO;
            case GroovyTokenTypes.VARIABLE_DEF:
                    return GroovyTokenId.VARIABLE_DEF;
            case GroovyTokenTypes.VARIABLE_PARAMETER_DEF:
                    return GroovyTokenId.VARIABLE_PARAMETER_DEF;
            case GroovyTokenTypes.VOCAB:
                    return GroovyTokenId.VOCAB;
            case GroovyTokenTypes.WILDCARD_TYPE:
                    return GroovyTokenId.WILDCARD_TYPE;
            case GroovyTokenTypes.WS:
                    return GroovyTokenId.WHITESPACE;
            default:
                return GroovyTokenId.IDENTIFIER;
        }
    }

}
