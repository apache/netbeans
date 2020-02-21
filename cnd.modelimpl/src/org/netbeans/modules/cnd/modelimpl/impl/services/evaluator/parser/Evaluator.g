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

grammar Evaluator;

options {
    tokenVocab = APTTokenTypes;
}

@header {
package org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.parser.generated;

import java.util.HashMap;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.VariableProvider;
}

@members {
    /** Map variable name to Integer object holding value */
    HashMap memory = new HashMap();

    VariableProvider vp;

    public void setVariableProvider(VariableProvider vp) {
        this.vp = vp;
    }

    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        // do nothing
    }

    private boolean checkParams(int...params) {
        for (int param : params) {
            if (param == Integer.MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    private int safePlus(int first, int second) {
        return checkParams(first, second) ? first + second : Integer.MAX_VALUE;
    }

    private int safeMinus(int first, int second) {
        return checkParams(first, second) ? first - second : Integer.MAX_VALUE;
    }

    private int safeMul(int first, int second) {
        return checkParams(first, second) ? first * second : Integer.MAX_VALUE;
    }

    private int safeParseInt(String text) {
        if (text != null) {
            try {
              return Integer.parseInt(text.replaceAll("[a-z,A-Z,_].*", ""));
            } catch (NumberFormatException ex) {
              return Integer.MAX_VALUE;
            }
        }
        return 0;
    }
}

prog: expr;

expr returns [int value]
    :   e=equalityExpr {$value = $e.value;}
        (   PLUS e=equalityExpr {$value = safePlus($value, $e.value);}
        |   MINUS e=equalityExpr {$value = safeMinus($value, $e.value);}
        )*
    ;

equalityExpr returns [int value]
    :
        e = multExpr {$value = $e.value;}
        ( EQUAL e = multExpr {$value = ($value == $e.value) ? 1 : 0;}
        | NOTEQUAL e = multExpr {$value = ($value != $e.value) ? 1 : 0;}
        )*
    ;

multExpr returns [int value]
    :   e=unaryExpr {$value = $e.value;} (STAR e=unaryExpr {$value = safeMul($value, $e.value);})*
    ;

unaryExpr returns [int value]
    :   e=atom {$value = $e.value;}
    |   NOT e=unaryExpr {$value = ($e.value == 0 ? 1 : 0);}
    ;

atom returns [int value]
    :   
        DECIMALINT {$value = safeParseInt($DECIMALINT.text); }
    |   (function_call) => function_call {$value = $function_call.value;}
    |   variable {$value = $variable.value;}
    |   LPAREN expr RPAREN {$value = $expr.value;}
    |   LITERAL_sizeof balance_lparen_rparen {$value = vp==null ? 0 : vp.getSizeOfValue($balance_lparen_rparen.s);}
    |   LITERAL_static_cast LESSTHAN (~GREATERTHAN)* GREATERTHAN LPAREN expr RPAREN {$value = $expr.value;}
    |   LITERAL_true
        {
            $value = vp==null?0:vp.getValue($LITERAL_true.text);
        }
    |   LITERAL_false
        {
            $value = vp==null?0:vp.getValue($LITERAL_false.text);
        }
    ;

function_call returns [int value]
    :
        id = qualified_id 
        args = balance_lparen_rparen
        { $value = vp==null ? 0 : vp.getFunCallValue($id.q + $args.s); }
    ;

variable returns [int value]
    :
        (LITERAL_const)*
        id = qualified_id
        { $value = vp==null ? 0 : vp.getValue($id.q); }
    ;

qualified_id returns [String q = ""] 
    :
        so = scope_override
        { q += ($so.s != null)? $so.s : ""; }
        (
            IDENT
            {q += $IDENT.text;}
            (
                inner = balance_less_greater {q += $inner.s;}
            )?
        )
    ;

scope_override returns [String s = ""]
    :
        (
            SCOPE { s += "::";}
        )?
        (
            (IDENT (balance_less_greater)? SCOPE)=> sp = scope_override_part
            {
                    s += ($sp.s != null) ? $sp.s : "";
            }
        )?
    ;

scope_override_part returns [String s = ""]
    :
        IDENT
        {
            s += $IDENT.text;
        }
        (
            inner = balance_less_greater {s += $inner.s;}
        )?
        SCOPE
        {
            s += "::";
        }

        ((IDENT (balance_less_greater)? SCOPE)=> sp = scope_override_part)?
        {
            s += ($sp.s != null) ? $sp.s : "";
        }
    ;

balance_less_greater returns [String s = ""]
    :
        LESSTHAN {s += "<";}
        (
            (LESSTHAN)=> inner = balance_less_greater {s += $inner.s;}
        |
            other = (~GREATERTHAN) {s += $other.text;}
        )*
        GREATERTHAN {s += "> ";}
    ;

balance_lparen_rparen returns [String s = ""]
    :
        LPAREN {s += "(";}
        (
            (LPAREN)=> inner = balance_lparen_rparen {s += $inner.s;}
        |
            // TODO: Evaluator doesn't support operators "<<" and ">>", so 
            // add space after "> " without condition
            (GREATERTHAN)=> GREATERTHAN {s += "> ";}
        |
            other = (~RPAREN) {s += $other.text;}
        )*
        RPAREN {s += ")";}
    ;

// Suppressing warnings "no lexer rule corresponding to token"
fragment IDENT: ' ';
fragment DECIMALINT: ' ';

fragment PLUS: ' ';
fragment MINUS: ' ';
fragment STAR: ' ';
fragment LESSTHAN: ' ';
fragment GREATERTHAN: ' ';
fragment NOT: ' ';
fragment EQUAL: ' ';
fragment NOTEQUAL: ' ';

fragment SCOPE: ' ';

fragment LPAREN: ' ';
fragment RPAREN: ' ';

fragment LITERAL_sizeof: ' ';
fragment LITERAL_static_cast: ' ';
fragment LITERAL_true: ' ';
fragment LITERAL_false: ' ';

fragment LITERAL_const: ' ';

