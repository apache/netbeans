/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


header {
package org.netbeans.modules.cnd.asm.base.generated;

import org.netbeans.modules.cnd.asm.base.att.*;
import org.netbeans.modules.cnd.asm.base.syntax.*;

}

{
@SuppressWarnings({"unchecked", "cast", "fallthrough"})
}
class IdentScanner extends Lexer;
options {
    k = 3;   
    filter = true;
    testLiterals = false;
    charVocabulary = '\0'..'\ufffe';
}

tokens {
    LabelInst;
}


{
    private int numLines = 0;       

    public int getNumLines() {
        return numLines;
    }         

    private void deferredNewline() {           
        numLines++;
    }    

    protected Token createToken(int type) throws InstantiationException, IllegalAccessException {
        return new org.netbeans.modules.cnd.antlr.CommonToken();
    }
}


protected
EndOfLine
	:	(	options{generateAmbigWarnings = false;}:
			"\r\n"  // MS
		|	'\r'    // Mac
		|	'\n'    // Unix
		) 
	;
 
Whitespace  :  ( ' '
               | '\t' 
               )+
            | ( (EndOfLine)+ { deferredNewline(); } )
            ;


protected
COMMA      options { constText=true; } : ',' ;


protected
STAR        options { constText=true; }  : '*' ;


protected
AT          options { constText=true; }  : '@' ;

protected 
EQ          options { constText=true; }  : '=' ;


protected
PLUS        options { constText=true; }  : '+' ;


protected
TILDE       options { constText=true; }  : '~' ;

protected 
FLSQUARE    options { constText=true; }  : '{' ;

protected 
FRSQUARE    options { constText=true; }  : '}' ;

protected 
DOLLAR      options { constText=true; }  : '$' ;

protected 
PERCENT     options { constText=true; }  : '%' ;

protected 
COLON       options { constText=true; }  : ':' ;

protected
BITWISEOR   options { constText=true; } :  '^' ;

protected 
QUESTIONMARK options { constText=true; }  : '?' ;

protected
AMPERSAND    options { constText=true; }  : '&';

protected 
LESS         options { constText=true; }  : '<' ;

protected 
OR           options { constText=true; }  : '|' ;

protected 
UPPER        options { constText=true; }  : '>' ;

protected
MINUS       options { constText=true; }  : '-' ;


protected
LPAREN      options { constText=true; }  : '(' ;


protected
RPAREN      options { constText=true; }  : ')' ;


protected
LSPAREN     options { constText=true; }  : '[' ;


protected
RSPAREN     options { constText=true; }  : ']' ;

Mark        : RSPAREN | LSPAREN | RPAREN | LPAREN | MINUS | PLUS |
              AT  | STAR | COMMA | EQ | LESS | UPPER | DOLLAR |
              BITWISEOR | AMPERSAND | QUESTIONMARK | PERCENT |
              FRSQUARE | FLSQUARE | OR | COLON
            ;


protected
Digit       : '0'..'9'
            ;

protected
HexDigit   : Digit
           | 'a'..'f'
           | 'A'..'F'
           ;

protected  
OctDigit   : '0'..'7'
           ;

protected 
BinDigit    : '0' | '1'
            ;

protected
DecIntegerLiteral   :  '0'
                       |  ('1'..'9') ('0'..'9')*
                       ;

protected    
HexIntegerLiteral      : '0' ('x' | 'X')  (HexDigit)*                     
                       ;
protected
BinIntegerLiteral      : '0' ('b' | 'B') (BinDigit)+
                       ;
 
                                            
protected
Exponent               : ('E' | 'e') ('-' | '+')? ('0'..'9')+
                       ;
protected
Escape  
        :'\\'
		('a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | '"' | '\'' | '\\' | '?' | 
                    ('0'..'3') (options{greedy=true;}: Digit)? (options{greedy=true;}: Digit)?
		| ('4'..'7') (options{greedy=true;}: Digit)?
		| 'x' (options{greedy=true;}: Digit | 'a'..'f' | 'A'..'F')+
		)
	;

 
CharLiteral
        :   
            '\'' (Escape | ~( '\'' | '\\' ))* '\''
        ;

 
StingLiteral
        :
            '"' StringLiteralBody 
        ;



protected
StringLiteralBody
        :
		(       
                        '\\'                        
                        (   options{greedy=true;}:
                            (	"\r\n" // MS 
                            |	"\r"     // MAC
                            |	"\n"     // Unix
                            ) 
                        | 
                            '"'
                        |   
                            '\\'    
                        )?
		|	
                         ~('"' | '\r' | '\n' | '\\')
		)*
            '"'
        ;

 
protected                      
Ident_                 : ( ('a'..'z' | 'A'..'Z' | '_' | '.')
                           ('a'..'z' | 'A'..'Z' | '_' | '0'..'9' | '.')* 
                         )     
                       ;

protected
IncompleteCComment:  "/*";
                
protected 
CComment: 
		IncompleteCComment
		( options {greedy=false;}:
			EndOfLine
                        | . )*
		"*/"          
	;



Comment     :     (options{generateAmbigWarnings = false;}:  
                    ({LA(1)=='/' && LA(2)=='*'}? CComment) 
            | 
                 ('/' | ';' | '!' | '#')                                     
                 (  ~('\n' | '\r') )* )  
            ;



Ident                  : ('a'..'z' | 'A'..'Z' | '_' | '0'..'9' | '.')+                                                                                                       
                       ;

Register               : '%' Ident_
                       ;
