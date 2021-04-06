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
/*
 *
 * Parser for preprocessor expressions
 */
header {

package org.netbeans.modules.cnd.apt.impl.support.generated;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import org.netbeans.modules.cnd.antlr.*;
import org.netbeans.modules.cnd.antlr.collections.*;
import org.netbeans.modules.cnd.antlr.debug.misc.*;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
}

options {
	language = "Java"; // NOI18N
} 

{
@org.netbeans.api.annotations.common.SuppressWarnings({"UCF", "MS"})
@SuppressWarnings({"unchecked", "cast", "fallthrough"})
final /*final class attribute gives us performance */
}
class APTExprParser extends Parser;

options {
//	k = 2;
	importVocab = APTGenerated;
	codeGenMakeSwitchThreshold = 2;
	codeGenBitsetTestThreshold = 3;
//	noConstructors = true;
	buildAST = false;
}

{
    // value for ID with text "true" (i.e. it is 1 in C++ and 0 in C langs)
    private long trueIDValue = 0;
    private final static String TRUE = "true";// NOI18N
    private APTMacroCallback callback = null;
    private boolean bigValuesInUse = false;
    private static final long MAX_INT = (long)Integer.MAX_VALUE;
    private static final long MIN_INT = (long)Integer.MIN_VALUE;

    public APTExprParser(TokenStream lexer, APTMacroCallback callback, long trueIDValue) {
        super(lexer, 1, 16);
        tokenNames = _tokenNames;
        this.callback = callback;
        this.trueIDValue = trueIDValue;
    }

    private void checkBigValues(long r) {
        if (r >= MAX_INT || r <= MIN_INT) {
            this.bigValuesInUse = true;
        }
    }

    public boolean areBigValuesUsed() {
        return this.bigValuesInUse;
    }

    private boolean isDefined(Token id) {
        if (id != null && callback != null) {
            return callback.isDefined((APTToken)id);
        }
        return false;
    }

    private boolean toBoolean(long r) {
        return r == 0 ? false : true;
    }

    // Fixup: workaround is added due to bug in jdk6 Update 10 (see IZ#150693)
    private static long one = 1;
    private static long zero = 0;
    private long toLong(boolean b) {
        return b ? one : zero;
    }

    private long toLong(String str) {
        long val = Long.MAX_VALUE;
        try {
            val = Long.decode(remSuffix(str)).longValue();
        } catch (NumberFormatException ex) {
            //ex.printStackTrace(System.err);
        }
        return val;
    }

    private String remSuffix(String num) {
        int len = num.length();
        boolean stop;
        do {
            stop = true;
            if (len > 0) {
                char last = num.charAt(len - 1);
                // remove postfix like u, U, l, L
                if (last == 'u' || last == 'U' || last == 'l' || last == 'L') {
                    num = num.substring(0, len - 1);
                    len--;
                    stop = false;
                }
            }
        } while (!stop);
        return num;
    }

    private long binaryToLong(String str) {
        str = remSuffix(str);
        long val = 0;
        for (int i = 2; i < str.length(); i++) {
            val = val*2 + ((str.charAt(i) == '0') ? 0 : 1);
        }
        return val;
    }
    
    private long charToLong(CharSequence str) {
        long val;
        int len = str.length();
        if (len <= 2) {
            // empty '' or incorrect char
            val = 0;
        } else if (len == 3) {
            val = str.charAt(1);
        } else if (len == 4 && str.charAt(1) == '\\') {
            switch (str.charAt(2)) {
                case 'b':
                    val = '\b';
                    break;
                case 'f':
                    val = '\f';
                    break;
                case 'n':
                    val = '\n';
                    break;
                case 'r':
                    val = '\r';
                    break;
                case 't':
                    val = '\t';
                    break;
//                case '"':
//                    val = '\"';
//                    break;
//                case '\'':
//                    val = '\'';
//                    break;
//                case '\\':
//                    val = '\\';
//                    break;
                default:
                    // what to do with '\e'; '\v'; '\?'; ?
                    // for other like '\'' '\\' '\"' use symbol as is
                    val = str.charAt(2);
                    break;
            }
        } else {
            // for now just use the first char as well
            APTUtils.LOG.log(Level.INFO, "use fallback when convert character [{0}] to long\n", str); // NOI18N
            val = str.charAt(1);
        }
        if (APTUtils.LOG.isLoggable(Level.FINE)) {
            APTUtils.LOG.log(Level.FINE, "convert char [{0}] to long {1}\n", new Object[] { str, val}); // NOI18N
        }
        return val;
    }

    private long evalID(Token id) {
        // each not expanded ID in expression is '0' by specification
        // but 'true' can be treated differently:
        //  in C++ as 1
        //  in C as 0
        if (id != null && TRUE.equals(id.getText())) {
            return trueIDValue;
        }
        return 0;
    }

}

imaginaryTokenDefinitions :
   SIGN_MINUS
   SIGN_PLUS
;

expr      returns [long r] {r=0;} : r=ternCondExpr | EOF;
// ternCondExpr uses * because ? generates incorrect code in ANTLR 2.7.5
// don't want to use guessing, because it slows down code
ternCondExpr returns [long r] : {long b,c;}   r=orExpr 
                (options{generateAmbigWarnings = false;}:
                    QUESTIONMARK^ b=ternCondExpr COLON! c=ternCondExpr { r = toBoolean(r)?b:c;}
                )*
        ;
//rule        :   QUESTIONMARK^ ternCondExpr COLON! ternCondExpr;
orExpr    returns [long r] : {long b;}  r=andExpr (OR^ b=andExpr {r=toLong(toBoolean(r) || toBoolean(b));})*;
andExpr   returns [long r] : {long b;}  r=borExpr (AND^ b=borExpr {r=toLong(toBoolean(r) && toBoolean(b));})*;
borExpr   returns [long r] : {long b;}  r=xorExpr (BITWISEOR^ b=xorExpr {r=r|b;})*;
xorExpr   returns [long r] : {long b;}  r=bandExpr (BITWISEXOR^ b=bandExpr {r=r^b;})*;
bandExpr  returns [long r] : {long b;}  r=eqExpr  (AMPERSAND^ b=eqExpr {r=r&b;})*;
eqExpr    returns [long r] : {long b;}  r=relExpr (EQUAL^ b=relExpr {r= toLong(r == b);} 
                                                 | NOTEQUAL^ b=relExpr {r= toLong(r != b);})*;
relExpr   returns [long r] : {long b;}  r=shiftExpr (LESSTHAN^ b=shiftExpr { r= toLong(r < b); }
                                                    |LESSTHANOREQUALTO^ b=shiftExpr { r= toLong(r <= b); }
                                                    |GREATERTHAN^ b=shiftExpr { r= toLong(r > b); }
                                                    |GREATERTHANOREQUALTO^ b=shiftExpr { r= toLong(r >= b); })*;
shiftExpr returns [long r] : {long b;}  r=sumExpr (SHIFTLEFT^ b=sumExpr { r= r << b; }
                                                  |SHIFTRIGHT^ b=sumExpr { r= r >> b; })*;
sumExpr   returns [long r] : {long b;}  r=prodExpr (PLUS^ b=prodExpr { r= r + b; }
                                                   |MINUS^ b=prodExpr { r= r - b; })* ;
prodExpr  returns [long r] : {long b;}  r=signExpr (STAR^ b=signExpr { r=r*b; }
                                                   |DIVIDE^ b=signExpr { r=r/b; }
                                                   |MOD^ b=signExpr { r=r%b; } )* ;
signExpr  returns [long r] {r=0;}:   
                      MINUS^ r=atom { r=-1*r; }
                    | PLUS^  r=atom { r= (r<0) ? 0-r : r; }
                    | NOT^ r=atom { r=toLong(!toBoolean(r)); }
                    | TILDE^ r=atom { r=~r; }
                | r=atom ;
atom returns [long r]  {r=0;}     : r=constant | r=defined | (LPAREN^ r=expr RPAREN!) ;
//atom        : constant | NUMBER | defined | ID | (LPAREN^ expr RPAREN!) ;

defined returns [long r] {r=0;} : 
        DEFINED^
        (
            (LPAREN! id_1:ID_DEFINED RPAREN!) { r = toLong(isDefined(id_1)); }
            | id_2:ID_DEFINED { r = toLong(isDefined(id_2)); }
        )
;

constant returns [long r] {r=0;}
            :	
            (
                LITERAL_true { r=toLong(true);}
            |	LITERAL_false { r=toLong(false);}
            |   id:IDENT {r=evalID(id);}
            |   r = ud_constant
//          | f1: FLOATONE {r=Integer.parseInt(f1.getText());}
//          | f2: FLOATTWO {r=Integer.parseInt(f2.getText());}
            )
            {checkBigValues(r);}
	;

ud_constant returns [long r] {r=0;}
    :
    (
        // Use syntactic predicate to avoid match error on EOF token after ud_constant_value
        (ud_constant_value IDENT)=> r = ud_constant_value IDENT
        | r = ud_constant_value
    )
    ;

ud_constant_value returns [long r] {r=0;}
    :
    (
        n:NUMBER {r=toLong(n.getText());}
        | o:OCTALINT {r=toLong(o.getText());}
        | d:DECIMALINT {r=toLong(d.getText());}
        | x:HEXADECIMALINT {r=toLong(x.getText());}
        | b:BINARYINT {r=binaryToLong(b.getText());}
        | c:CHAR_LITERAL {r=charToLong(c.getText()); }
    )
    ;

/* APTExpressionWalker is not used any more, because all evaluations are done in APTExprParser
class APTExpressionWalker extends TreeParser;
{
    private APTMacroCallback callback = null;
    public APTExpressionWalker(APTMacroCallback callback) {
        this.callback = callback;
    }

    private boolean isDefined(AST id) {
        if (id != null && callback != null) {
            return callback.isDefined(getToken(id));
        }
        return false;
    }

    private APTToken astToken = new APTBaseToken();
    private APTToken getToken(AST ast) {
        astToken.setType(ast.getType());
        astToken.setText(ast.getText());
        return astToken;
    }

    private long evalID(AST id) {
        // each not expanded ID in expression is '0' by specification
        return 0;
    }

    private boolean toBoolean(long r) {
        return r == 0 ? false : true;
    }

    private long toLong(boolean b) {
        return b ? 1 : 0;
    }

    private long toLong(String str) {
        long val = Long.MAX_VALUE;
        try {
            val = Long.decode(remSuffix(str)).longValue();
        } catch (NumberFormatException ex) {
            //ex.printStackTrace(System.err);
        }
        return val;
    }

    private String remSuffix(String num) {
        int len = num.length();
        boolean stop;
        do {
            stop = true;
            if (len > 0) {
                char last = num.charAt(len - 1);
                // remove postfix like u, U, l, L
                if (last == 'u' || last == 'U' || last == 'l' || last == 'L') {
                    num = num.substring(0, len - 1);
                    len--;
                    stop = false;
                }
            }
        } while (!stop);
        return num;
    }
}

expr returns [long r]
    { 
        long a,b; 
        long q;
        boolean def;
        r=0; 
    }
    : #(QUESTIONMARK q=expr a=expr b=expr) { r = toBoolean(q)?a:b;}
    | #(OR a=expr b=expr) { r=toLong(toBoolean(a)||toBoolean(b)); }
    | #(AND a=expr b=expr) { r=toLong(toBoolean(a)&&toBoolean(b)); }
    | #(BITWISEOR a=expr b=expr) { r= a | b; }  
    | #(BITWISEXOR a=expr b=expr) { r= a ^ b; }  
    | #(AMPERSAND a=expr b=expr) { r= a & b; }  
    | #(EQUAL a=expr b=expr) { r= toLong(a == b); }
    | #(NOTEQUAL a=expr b=expr) { r= toLong(a != b); }
    | #(LESSTHAN a=expr b=expr) { r= toLong(a < b); }
    | #(LESSTHANOREQUALTO a=expr b=expr) { r= toLong(a <= b); }
    | #(GREATERTHAN a=expr b=expr) { r= toLong(a > b); }
    | #(GREATERTHANOREQUALTO a=expr b=expr) { r= toLong(a >= b); }
    | #(SHIFTLEFT a=expr b=expr) { r= a << b; }
    | #(SHIFTRIGHT a=expr b=expr) { r= a >> b; }
    | #(PLUS  a=expr b=expr) { r=a+b; }
    | #(MINUS a=expr b=expr) { r=a-b; }
    | #(STAR   a=expr b=expr) { r=a*b; }
    | #(DIVIDE   a=expr b=expr) 
            {
                try {
                    r=a/b;
                } catch (ArithmeticException ex) {
                    //System.err.println(ex);
                    r = 0;
                }
            }
    | #(MOD   a=expr b=expr) 
            {
                try {
                    r=a%b;
                } catch (ArithmeticException ex) {
                    //System.err.println(ex);
                    r = 0;
                }
            }
    | #(SIGN_MINUS a=expr)   { r=-1*a; } 
    | #(SIGN_PLUS  a=expr)   { r= (a<0) ? 0-a : a; }
    | #(DEFINED def=defined) {r=toLong(def);}
    | #(NOT a=expr)   { r=toLong(!toBoolean(a)); } 
    | #(TILDE a=expr)   { r=~a; } 
    | #(LPAREN a=expr)       { r=a; }
    | LITERAL_true { r=toLong(true);}
    | LITERAL_false { r=toLong(false);}
    | n:NUMBER {r=toLong(n.getText());}
    | id: ID       {r=evalID(id);}
//  | i:constant { r=(double)Integer.parseInt(i.getText()); }
    | o:OCTALINT {r=toLong(o.getText());}
    | d:DECIMALINT {r=toLong(d.getText());}
    | x:HEXADECIMALINT {r=toLong(x.getText());}
    | c: CHAR_LITERAL { r=c.getText().charAt(1); }
//    | f1: FLOATONE {r=Integer.parseInt(f1.getText());}
//    | f2: FLOATTWO {r=Integer.parseInt(f2.getText());}
    | EOF { r = 0; }
  ;

defined returns [boolean r]
  { r=false; } : id: ID_DEFINED { r = isDefined(id); }
;

/*
constant return [long r]
  { r=10; }
    :	#(OCTALINT)
    |	#(DECIMALINT)
    |	#(HEXADECIMALINT)
//    |	CharLiteral
//	|	(StringLiteral)+
    |	#(FLOATONE)
    |	#(FLOATTWO)
    |	#(LITERAL_true)
    |	#(LITERAL_false)
;
*/  
