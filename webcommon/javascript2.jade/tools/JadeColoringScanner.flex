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

package org.netbeans.modules.javascript2.jade.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


%%

%public
%final
%class JadeColoringLexer
%type JadeTokenId
%unicode
%caseless
%char

%{
    private LexerInput input;
    private boolean canFollowTag = false;
    int parenBalance = 1;
    int braceBalance = 0;
    int bracketBalance = 0;
    int indent = 0;
    int eolPosition = 0;
    boolean dotAfterTag = false;
    int blockIndent = -1;
    boolean hasCssId = false;
    int lastReaded = 0;
    boolean continueJS = false;
    boolean inString = false;
    int whereToGo = 0;

    private static enum TAG_TYPE  { OTHER, SCRIPT, STYLE};
    TAG_TYPE lastTag = TAG_TYPE.OTHER;
    

    public JadeColoringLexer(LexerRestartInfo info) {
        this.input = info.input();

        if(info.state() != null) {
            //reset state
            setState((LexerState)info.state());
        } else {
            //initial state
            zzState = zzLexicalState = YYINITIAL;
        }
    }


    public LexerState getState() {
        if (zzState == YYINITIAL && zzLexicalState == YYINITIAL) {
            return null;
        }
        return new LexerState(zzState, zzLexicalState, canFollowTag, indent, hasCssId, lastTag, braceBalance, parenBalance, bracketBalance);
    }

    public void setState(LexerState state) {
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
        this.canFollowTag = state.canFollowTag;
        this.indent = state.indent;
        this.hasCssId = state.hasCssId;
        this.lastTag = state.lastTag;
        this.braceBalance = state.braceBalance;
        this.parenBalance = state.parenBalance;
        this.bracketBalance = state.bracketBalance;
    }

    public JadeTokenId nextToken() throws java.io.IOException {
        JadeTokenId token = yylex();
        return token;
    }

    public static final class LexerState  {
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;
        final boolean canFollowTag;
        /** indent of the new line */
        final int indent;
        final boolean hasCssId;
        /** last readed tag to switch embeding of js , css or html*/
        final TAG_TYPE lastTag;
        /** balance of brances */
        final int braceBalance;
        final int parenBalance;
        final int bracketBalance;

        LexerState (int zzState, int zzLexicalState, boolean canFollowTag, int indent, boolean hasCssId, TAG_TYPE lastTag, int braceBalance, int parenBalance, int bracketBalance) {
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.canFollowTag = canFollowTag;
            this.indent = indent;
            this.hasCssId = hasCssId;
            this.lastTag = lastTag;
            this.braceBalance = braceBalance;
            this.parenBalance = parenBalance;
            this.bracketBalance = bracketBalance;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LexerState other = (LexerState) obj;
            if (this.zzState != other.zzState) {
                return false;
            }
            if (this.zzLexicalState != other.zzLexicalState) {
                return false;
            }
            if (this.canFollowTag != other.canFollowTag) {
                return false;
            }
            if (this.hasCssId != other.hasCssId) {
                return false;
            }
            if (this.indent != other.indent) {
                return false;
            }
            if (this.lastTag != other.lastTag) {
                return false;
            }
            if ((this.braceBalance != other.braceBalance) || (this.parenBalance != other.parenBalance) || (this.bracketBalance != other.bracketBalance)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + this.zzState;
            hash = 31 * hash + this.zzLexicalState;
            hash = 31 * hash + (this.canFollowTag ? 0 : 1);
            hash = 31 * hash + (this.hasCssId ? 0 : 1);
            hash = 31 * hash + this.indent;
            hash = 31 * hash + this.lastTag.hashCode();
            hash = 31 * hash + this.braceBalance;
            hash = 31 * hash + this.parenBalance;
            hash = 31 * hash + this.bracketBalance;
            return hash;
        }

        @Override
        public String toString() {
            return "LexerState{" + "zzState=" + zzState + ", zzLexicalState=" + zzLexicalState + '}';
        }
    }

    JadeTokenId getTokenIdFromTagType (TAG_TYPE tagType, JadeTokenId defaultId) {
        switch (tagType) {
            case SCRIPT: return JadeTokenId.JAVASCRIPT;
            case STYLE: return JadeTokenId.CSS;
            default: return defaultId;
        }
    }

 // End user code
    boolean checkEndJS(int tokenLength, char ch) {
        if (!continueJS 
                && ((ch == ')' && parenBalance == 0) 
                || (ch != ')' && parenBalance == 1))
                && braceBalance == 0 && bracketBalance == 0) {
            if (lastReaded > 0 && ((tokenLength - lastReaded) > 0)) {
                yypushback(tokenLength - lastReaded);
                yybegin(HTML_ATTRIBUTE);
                return true;
            }
        } 
        lastReaded = tokenLength;
        continueJS = false;
        
        return false;
    }
%}

/* states */
%state AFTER_EOL
%state DOCTYPE
%state AFTER_DOCTYPE
%state DOCTYPE_STRING
%state DOCTYPE_STRING_END
%state AFTER_TAG
%state AFTER_CODE_DELIMITER
%state AFTER_CODE_DELIMITER_WITH_BLOCK_EXPANSION
%state IN_COMMENT
%state IN_COMMENT_AFTER_EOL
%state IN_UNBUFFERED_COMMENT
%state IN_UNBUFFERED_COMMENT_AFTER_EOL
%state TEXT_LINE
%state IN_PLAIN_TEXT_LINE
%state IN_PLAIN_TEXT_BLOCK
%state IN_PLAIN_TEXT_BLOCK_AFTER_EOL
%state AFTER_PLAIN_TEXT_BLOCK_DELIMITER
%state HTML_ATTRIBUTE
%state HTML_ATTRIBUTE_VALUE
%state JAVASCRIPT_VALUE
%state JAVASCRIPT
%state JAVASCRIPT_LINE
%state JAVASCRIPT_EXPRESSION
%state JAVASCRIPT_WITH_BLOCK_EXPANSION
%state JS_SSTRING
%state JS_STRING
%state FILEPATH
%state IN_FILTER_BLOCK
%state IN_FILTER_BLOCK_AFTER_EOL
%state AFTER_INCLUDE
%state AFTER_BLOCK
%state AFTER_COLON_IN_TAG
%state AFTER_EACH
%state JAVASCRIPT_AFTER_EACH
%state AFTER_MIXIN
%state MIXIN_ARGUMENTS
%state AFTER_PLUS_MIXIN
%state MIXIN_CALL_ARGUMENT
%state AFTER_ATTRIBUTES

/* base structural elements */
AnyChar = (.|[\n])
h = [0-9a-f]
nonascii = [\200-\377]
unicode	= \\{h}{1,6}(\r\n|[ \t\r\n\f])?
escape =	{unicode}|\\[ -~\200-\377]
nmstart	 =	[_a-zA-Z]|{nonascii}|{escape}
nmchar	=	[_a-zA-Z0-9-]|{nonascii}|{escape}

HtmlString = [<] [^"\r"|"\n"|"\r\n"|">"|"*"]* [>]?
HtmlIdentifierPart = [[:letter:][:digit:]]+[[:letter:][:digit:]\-]*
HtmlIdentifier = {HtmlIdentifierPart}({HtmlIdentifierPart})*
CssIdentifier = -?{nmstart}{nmchar}*
LineTerminator = \r|\n|\r\n
StringCharacter  = [^\r\n\"\\] | \\{LineTerminator}
WS = [ \t\f\u00A0\u000B]
WhiteSpace = [ \t\f\u00A0\u000B]+
Input = [^\r\n \t\f\u00A0\u000B]+
IdentifierPart = [:jletterdigit:]
Identifier = [:jletter:]{IdentifierPart}*
MixinName = {CssIdentifier}

Comment = "//"
UnbufferedComment = "//-"

%%

/*
    TODO:
        - TagInterPolation http://jade-lang.com/reference/interpolation/
        - check interpolation in the text block
        - mixin default atributes
        - default attributes at all
    
*/
<YYINITIAL> {
    {AnyChar}   {
            yypushback(1);
            indent = 0;
            yybegin(AFTER_EOL);
    }

}

<AFTER_EOL> {
    /* doctype */
    "doctype"                       {   yybegin(AFTER_DOCTYPE);
                                        return JadeTokenId.DOCTYPE; }

    "if"                            {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.KEYWORD_IF;}
    "else"                          {   return JadeTokenId.KEYWORD_ELSE;}
    "unless"                        {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.KEYWORD_UNLESS;}

    "each"                          {   yybegin(AFTER_EACH);
                                        return JadeTokenId.KEYWORD_EACH;}
    "in"                            {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.KEYWORD_IN;}
    "for"                           {   yybegin(AFTER_EACH);
                                        return JadeTokenId.KEYWORD_FOR;}
    "while"                           {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.KEYWORD_WHILE;}                                        
                                
    "case"                          {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.KEYWORD_CASE;}
    "when"                          {   yybegin(AFTER_CODE_DELIMITER_WITH_BLOCK_EXPANSION);
                                        return JadeTokenId.KEYWORD_WHEN;}
    "default"                       {   yybegin(AFTER_TAG); // handling : after the keyword
                                        return JadeTokenId.KEYWORD_DEFAULT;}

    "block"                         {   yybegin(AFTER_BLOCK);
                                        return JadeTokenId.KEYWORD_BLOCK;}
    "extends"                       {   yybegin(FILEPATH);
                                        return JadeTokenId.KEYWORD_EXTENDS;}
    "include"                       {   yybegin(AFTER_INCLUDE);
                                        return JadeTokenId.KEYWORD_INCLUDE;}
    "mixin"                         {   yybegin(AFTER_MIXIN);
                                        return JadeTokenId.KEYWORD_MIXIN; }
    "+"                             {   yybegin(AFTER_PLUS_MIXIN);
                                        return JadeTokenId.OPERATOR_PLUS; }
    "-"|"="|"!="                    {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.CODE_DELIMITER; }
    {WhiteSpace}                    {   indent = tokenLength;
                                        return JadeTokenId.WHITESPACE; }
    "script"                        {   yybegin(AFTER_TAG);
                                        dotAfterTag = true;
                                        hasCssId = false;
                                        lastTag = TAG_TYPE.SCRIPT;
                                        return JadeTokenId.TAG ;}
    "style"                        {    yybegin(AFTER_TAG);
                                        dotAfterTag = true;
                                        hasCssId = false;
                                        lastTag = TAG_TYPE.STYLE;
                                        return JadeTokenId.TAG ;}
    {HtmlIdentifier}                {   yybegin(AFTER_TAG);
                                        dotAfterTag = true;
                                        hasCssId = false;
                                        lastTag = TAG_TYPE.OTHER;
                                        return JadeTokenId.TAG ;}
    {LineTerminator}                {   indent = 0; 
                                        return JadeTokenId.EOL; }
    
    {UnbufferedComment}             {   yybegin(IN_UNBUFFERED_COMMENT);
                                        return JadeTokenId.UNBUFFERED_COMMENT_DELIMITER; }
    
    {Comment}                       {   yybegin(IN_COMMENT); 
                                        return JadeTokenId.COMMENT_DELIMITER; }
    
    [#\.!]                           {  hasCssId = false;
                                        yypushback(1);
                                        yybegin(AFTER_TAG); }
        
    "|"                             {   yybegin(IN_PLAIN_TEXT_LINE);
                                        return JadeTokenId.PLAIN_TEXT_DELIMITER; }
    ":"{Input}                      {   yybegin (IN_FILTER_BLOCK);
                                        blockIndent = -1;
                                        return JadeTokenId.FILTER; }
    "<"                             {   yybegin(IN_PLAIN_TEXT_LINE); }
    "&"                             {   yybegin(IN_PLAIN_TEXT_LINE); }
    .                               {   return JadeTokenId.UNKNOWN;}
    
}

/* TODO - this rure shold be rewrite. I don't like it. Mainly because the dot after tag handling*/
<AFTER_TAG> {
    
    "#"{CssIdentifier}                {   if (!hasCssId) {
                                            hasCssId = true;
                                            return JadeTokenId.CSS_ID;
                                        } else {
                                            // only one css id is allowed in tag
                                            return JadeTokenId.UNKNOWN;
                                        }
                                    }
    "\."{CssIdentifier}                {   return JadeTokenId.CSS_CLASS; }
    "("                             {   yybegin(HTML_ATTRIBUTE);
                                        return JadeTokenId.BRACKET_LEFT_PAREN;
                                    }
    ":"                             {   yybegin(AFTER_COLON_IN_TAG);
                                        return JadeTokenId.OPERATOR_COLON;
                                    }
    {WhiteSpace}                    {   yybegin(TEXT_LINE);
                                        return JadeTokenId.WHITESPACE;
                                    }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        indent = 0;
                                        if (tokenLength > 0) {
                                            return JadeTokenId.EOL;
                                        }
                                    }
    "="|"!="                        {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.CODE_DELIMITER; }
    "/"                             {   return JadeTokenId.OPERATOR_DIVISION;}
    "\."                            {   
                                        yybegin(AFTER_PLAIN_TEXT_BLOCK_DELIMITER);
                                        return JadeTokenId.PLAIN_TEXT_DELIMITER; 
                                        
                                    }
    "#{"|"!{"                       {   yypushback(2);
                                        yybegin(JAVASCRIPT_EXPRESSION);
                                        whereToGo = TEXT_LINE;
                                    }
    "&attributes"                   {   yybegin(AFTER_ATTRIBUTES);
                                        return JadeTokenId.ATTRIBUTE; }
    .                               {   yybegin(TEXT_LINE); }
}

<AFTER_COLON_IN_TAG>                {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE;
                                    }
    {HtmlIdentifier}                {   yybegin(AFTER_TAG);
                                        dotAfterTag = true;
                                        hasCssId = false;
                                        return JadeTokenId.TAG ;}
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        indent = 0;
                                        return JadeTokenId.EOL;
                                    }
    .                               {   
                                        return JadeTokenId.UNKNOWN; }
}

<TEXT_LINE>                         {
    
    [#!]"{"                         {   yypushback(2);
                                        yybegin(JAVASCRIPT_EXPRESSION);
                                        whereToGo = TEXT_LINE;
                                        if (tokenLength > 2) {
                                            return JadeTokenId.TEXT;
                                        }
                                    }
    {LineTerminator}                {   
                                        yypushback(1);
                                        yybegin(AFTER_EOL);
                                        indent = 0;
                                        
                                        if (tokenLength -1 > 0) {
                                            return JadeTokenId.TEXT;
                                        }
                                    }
    {AnyChar}                       {  }
}

<HTML_ATTRIBUTE> {
    {HtmlIdentifier}                {   return JadeTokenId.ATTRIBUTE; }
    "="                             {   yybegin(HTML_ATTRIBUTE_VALUE);
                                        return JadeTokenId.OPERATOR_ASSIGNMENT; }
    "!="                            {   yybegin(HTML_ATTRIBUTE_VALUE);
                                        return JadeTokenId.OPERATOR_NOT_EQUALS; }
    ","                             {   return JadeTokenId.OPERATOR_COMMA; }
    {LineTerminator}                {   return JadeTokenId.EOL; }
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    ")"                             {   yybegin(AFTER_TAG);
                                        return JadeTokenId.BRACKET_RIGHT_PAREN;}
     .                              {   return JadeTokenId.UNKNOWN;}

}

<HTML_ATTRIBUTE_VALUE> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {LineTerminator}                {   return JadeTokenId.EOL; }
    {AnyChar}                       {   
                                        parenBalance = 1;
                                        lastReaded = bracketBalance = braceBalance = 0;
                                        yypushback(1);
                                        yybegin(JAVASCRIPT_VALUE);}
    
}

<AFTER_ATTRIBUTES> {
    "("                             {   parenBalance = 1;
                                        lastReaded = bracketBalance = braceBalance = 0;
                                        yybegin(JAVASCRIPT_VALUE);
                                        return JadeTokenId.BRACKET_LEFT_PAREN; 
                                    }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        return JadeTokenId.EOL; }
    .                               {   yybegin(AFTER_TAG);
                                        return JadeTokenId.UNKNOWN;}
}

<AFTER_EACH>    {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        return JadeTokenId.EOL; }
    {AnyChar}                       {   yypushback(1);
                                        yybegin(JAVASCRIPT_AFTER_EACH); }
}

<JAVASCRIPT_AFTER_EACH> {
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        return JadeTokenId.EOL; }
    {WS}*"in"({LineTerminator}|{WS}+)   {  int delta = tokenLength - lastReaded;
                                        if (delta > 0) {
                                            yypushback(delta);
                                            yybegin(AFTER_EOL);
                                            if (tokenLength > delta) {
                                                return JadeTokenId.JAVASCRIPT;
                                            }
                                        }
                                        yypushback(tokenLength);
                                        yybegin(AFTER_EOL);
                                    }
    {AnyChar}                       {   lastReaded = tokenLength; }
}

<JAVASCRIPT_VALUE> {
    \'                              {   yybegin(JS_SSTRING); }
    \"                              {   yybegin(JS_STRING); }
    [\+\-\.&\*/%|=!]"="?            {   continueJS = true; lastReaded = tokenLength; }
    "["                             {   braceBalance++; lastReaded = tokenLength; }
    "]"                             {   braceBalance--; lastReaded = tokenLength; }
    "{"                             {   bracketBalance++; lastReaded = tokenLength; }
    "}"                             {   bracketBalance--; lastReaded = tokenLength; }
    "("                             {   parenBalance++; lastReaded = tokenLength;}
    ")"                             {   parenBalance--; 
                                        
                                        if (checkEndJS(tokenLength, (char)zzInput)) {
                                            return JadeTokenId.JAVASCRIPT; 
                                        }
                                   }
    {WS}+                           {   } 
    ","                             {                   
                                        if (checkEndJS(tokenLength, (char)zzInput)) {
                                            return JadeTokenId.JAVASCRIPT; 
                                        }
                                    }
    {HtmlIdentifier}                {
                                        if (zzInput == ')') parenBalance--;
                                        if (checkEndJS(tokenLength, (char)zzInput)) {
                                            return JadeTokenId.JAVASCRIPT; 
                                        }
                                        if (zzInput == ')') parenBalance++;  // ned to return back 
    }
    
    {AnyChar}                         { lastReaded = tokenLength; /*continueJS = false;*/}
    
}

<JS_STRING> {
    \"                              {
                                        continueJS = false;
                                        lastReaded = tokenLength;
                                        yybegin(JAVASCRIPT_VALUE);
                                        
                                    }

                                    
    "\\\""                          { }  
  {LineTerminator}               {
                                     yypushback(1);
                                     yybegin(AFTER_EOL);
                                     if (tokenLength -1 > 0) {
                                         return JadeTokenId.UNKNOWN;
                                     }
                                 }
   {AnyChar}                    { }
}

<JS_SSTRING> {
    \'                              {
                                        continueJS = false;
                                        lastReaded = tokenLength;
                                        yybegin(JAVASCRIPT_VALUE);
                                        
                                    }


  "\\'"                          { }                                  
  {LineTerminator}               {
                                     yypushback(1);
                                     yybegin(AFTER_EOL);
                                     if (tokenLength -1 > 0) {
                                         return JadeTokenId.UNKNOWN;
                                     }
                                 }
   {AnyChar}                    { }
}
<AFTER_INCLUDE> {
    ":"{Input}                      {   return JadeTokenId.FILTER; }    
    {AnyChar}                       {   yypushback(1); yybegin(FILEPATH); }
}

<AFTER_BLOCK>    {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {Input}                         {   yybegin(TEXT_LINE);
                                        return JadeTokenId.BLOCK_NAME;}
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        return JadeTokenId.EOL; }
}

<JAVASCRIPT> {
    [\"'{}(),\n\r]                       {  
        switch (zzInput) {
            case '(': parenBalance++; break;
            case '{': braceBalance++; break;
            case '}': braceBalance--; break; 
            case ')':
                parenBalance--;
                break;    
            case ',':
            case '\r':
            case '\n':
                if (parenBalance == 1 && braceBalance == 0) {
                    parenBalance = 0;
                }
                break;
        }
        if (parenBalance == 0 && braceBalance == 0) {
            yypushback(1);
            yybegin(HTML_ATTRIBUTE);
            parenBalance = 1;
            if (tokenLength > 1) {
                return JadeTokenId.JAVASCRIPT;
            }
        }
                                    }
    {AnyChar}                       {}
}

<JAVASCRIPT_EXPRESSION> {
    [#!]"{"                            {   braceBalance = 1; return JadeTokenId.EXPRESSION_DELIMITER_OPEN; }
    "{"                             {   braceBalance++; }
    "}"                             {   braceBalance--;
                                        if (braceBalance == 0) {
                                            yypushback(1);
                                            if (tokenLength > 1) {
                                                return JadeTokenId.JAVASCRIPT;
                                            }
                                        } else if (braceBalance == -1) {
                                            yybegin(whereToGo);
                                            return JadeTokenId.EXPRESSION_DELIMITER_CLOSE; 
                                        }
                                    }
    {LineTerminator}                {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength - 1 > 0) {
                                            return JadeTokenId.JAVASCRIPT;
                                        }
                                    }
    .                               { }
}
<JAVASCRIPT_WITH_BLOCK_EXPANSION>   {
    ":"                             {   yypushback(1);
                                        yybegin(AFTER_TAG);
                                    }
    [^:\r\n]+                       {   return JadeTokenId.JAVASCRIPT; }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        indent = 0;
                                        return JadeTokenId.EOL; }
}

<JAVASCRIPT_LINE> {
    .+                              {   return JadeTokenId.JAVASCRIPT; }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        indent = 0;
                                        return JadeTokenId.EOL; }
}

<AFTER_CODE_DELIMITER_WITH_BLOCK_EXPANSION> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {AnyChar}                       {   yypushback(1);
                                        yybegin(JAVASCRIPT_WITH_BLOCK_EXPANSION);
                                    }
}

<AFTER_CODE_DELIMITER> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {AnyChar}                       {   yypushback(1);
                                        yybegin(JAVASCRIPT_LINE);
                                    }
}

<IN_PLAIN_TEXT_LINE> {
    [#!]"{"                         {   yypushback(2);
                                        yybegin(JAVASCRIPT_EXPRESSION);
                                        whereToGo = IN_PLAIN_TEXT_LINE;
                                        if (tokenLength > 2) {
                                            return getTokenIdFromTagType(lastTag, JadeTokenId.PLAIN_TEXT);
                                        }
                                    }
    {LineTerminator}                {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength - 1 > 0 ) {
                                            return getTokenIdFromTagType(lastTag, JadeTokenId.PLAIN_TEXT);
                                        }
                                    }
    .                              { }
}

<AFTER_PLAIN_TEXT_BLOCK_DELIMITER> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    .*                              {   // the  text will not be renedered
                                        return JadeTokenId.UNKNOWN; 
                                    }
    {LineTerminator}                {   blockIndent = -1;
                                        eolPosition = 0;
                                        yybegin(IN_PLAIN_TEXT_BLOCK_AFTER_EOL);
                                        return JadeTokenId.EOL;
                                    }
}
<IN_PLAIN_TEXT_BLOCK> {
    [#!]"{"                         {   yypushback(2);
                                        yybegin(JAVASCRIPT_EXPRESSION);
                                        whereToGo = IN_PLAIN_TEXT_BLOCK;
                                        if (tokenLength > 2) {
                                            return JadeTokenId.TEXT;
                                        }
                                    }
    {LineTerminator}                {   yybegin(IN_PLAIN_TEXT_BLOCK_AFTER_EOL);
                                        eolPosition = tokenLength;
                                    }
    .                               { }
}

<IN_PLAIN_TEXT_BLOCK_AFTER_EOL> {
    {WhiteSpace}                    {   
                                        int currentIndent = tokenLength - eolPosition;
                                        if (currentIndent <= indent) {
                                            // the block has to have one more space than the tag
                                            yybegin(AFTER_EOL);
                                            indent = currentIndent;
                                            if (tokenLength > currentIndent) {
                                                yypushback(currentIndent);
                                                return getTokenIdFromTagType(lastTag, JadeTokenId.PLAIN_TEXT);
                                            } else {
                                                return JadeTokenId.WHITESPACE;
                                            }
                                        }
                                        if (blockIndent < 0) {
                                            blockIndent = currentIndent;
                                        }
                                        if (blockIndent > currentIndent) {
                                            yypushback(currentIndent);
                                            yybegin(AFTER_EOL);
                                            if (tokenLength > currentIndent) {
                                                return getTokenIdFromTagType(lastTag, JadeTokenId.PLAIN_TEXT);
                                            }
                                        }
                                        yybegin(IN_PLAIN_TEXT_BLOCK);
                                    }
    {LineTerminator}                {}                                
    .                               {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        indent = 0;
                                        if (tokenLength > 1) {
                                            return getTokenIdFromTagType(lastTag, JadeTokenId.PLAIN_TEXT);
                                        }
                                    }
}

<IN_FILTER_BLOCK>   {
    [#!]"{"                         {   yypushback(2);
                                        yybegin(JAVASCRIPT_EXPRESSION);
                                        whereToGo = IN_FILTER_BLOCK;
                                        if (tokenLength > 2) {
                                            return JadeTokenId.FILTER_TEXT;
                                        }
                                    }
    {LineTerminator}                {   yybegin(IN_FILTER_BLOCK_AFTER_EOL);
                                        eolPosition = tokenLength;
                                    }
    .                               {}
}

<IN_FILTER_BLOCK_AFTER_EOL> {
    {WhiteSpace}                    {   int indentInBlock = tokenLength - eolPosition;
                                        if (blockIndent < 0) {
                                            blockIndent = indentInBlock;
                                        }
                                        if (blockIndent > indentInBlock) {
                                            yypushback(indentInBlock);
                                            yybegin(AFTER_EOL);
                                            if (tokenLength > indentInBlock) {
                                                return JadeTokenId.FILTER_TEXT;
                                            }
                                        }
                                        yybegin(IN_FILTER_BLOCK);
                                    }
    {LineTerminator}                {}                                
    .                               {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength - 1 > 0) { 
                                            return JadeTokenId.FILTER_TEXT;
                                        }
                                    }
}

<AFTER_MIXIN> { 
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        return JadeTokenId.EOL; }
    {MixinName}                     {   return JadeTokenId.MIXIN_NAME; }
    "("                             {   yybegin(MIXIN_ARGUMENTS); 
                                        return JadeTokenId.BRACKET_LEFT_PAREN;}
    .                               {   return JadeTokenId.UNKNOWN; }
                 
}

<MIXIN_ARGUMENTS> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {Identifier}                    {   return JadeTokenId.IDENTIFIER; }
    ","                             {   return JadeTokenId.OPERATOR_COMMA; }
    "..."                           {   return JadeTokenId.OPERATOR_REST_ARGUMENTS; }
    ")"                             {   return JadeTokenId.BRACKET_RIGHT_PAREN;}
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        return JadeTokenId.EOL; }
    {AnyChar}                       {   // expect declaration of parameters
                                        return JadeTokenId.UNKNOWN; }
}

<AFTER_PLUS_MIXIN> {    
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {MixinName}                     {   return JadeTokenId.MIXIN_NAME; }
    "("                             {   yybegin(MIXIN_CALL_ARGUMENT);
                                        parenBalance = 1; braceBalance = 0;
                                        return JadeTokenId.BRACKET_LEFT_PAREN;}
    ","                             {   yybegin(MIXIN_CALL_ARGUMENT);
                                        parenBalance = 1; braceBalance = 0;
                                        return JadeTokenId.OPERATOR_COMMA; }
    ")"                             {   yybegin(AFTER_TAG);
                                        return JadeTokenId.BRACKET_LEFT_PAREN;}
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        return JadeTokenId.EOL; }
    .                               {   return JadeTokenId.UNKNOWN; }
}

<MIXIN_CALL_ARGUMENT> {
    [(){},]                       {  
        switch (zzInput) {
            case '(': parenBalance++; break;
            case '{': braceBalance++; break;
            case '}': braceBalance--; break; 
            case ')':
                parenBalance--;
                break;    
            case ',':
                if (parenBalance == 1 && braceBalance == 0) {
                    parenBalance = 0;
                }
                break;
        }
        if (parenBalance == 0 && braceBalance == 0) {
            yypushback(1);
            yybegin(AFTER_PLUS_MIXIN);
            parenBalance = 1;
            if (tokenLength > 1) {
                return JadeTokenId.JAVASCRIPT;
            }
        }
                                    }
    {AnyChar}                       {}
}

/* This is help rule. Read all until end of line and remember the number of read chars. */
<IN_COMMENT> {
    .*                              { }
    {LineTerminator}                {   yybegin(IN_COMMENT_AFTER_EOL);
                                        eolPosition = tokenLength;
                                    }
}

/* Scan the begining of line in commnet. 
    If there is a whitespace, we need to find out, if the indentation says that the commment
    continues or finished already. */
<IN_COMMENT_AFTER_EOL>              {
    {WhiteSpace}                    {   int indentInComment = tokenLength - eolPosition;
                                        if (indent >= indentInComment) {
                                            yypushback(indentInComment + 1);  // return back also the EOL
                                            yybegin(AFTER_EOL);
                                            if (tokenLength > (indentInComment + 1)) {
                                                return JadeTokenId.COMMENT;
                                            }
                                        } else {
                                            yybegin(IN_COMMENT);
                                        }
                                    }
    {LineTerminator}                {}                                
    .                               {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength > 1) {
                                            return JadeTokenId.COMMENT;
                                        }
                                    }   
}

/* Copy of the normal comment. Just return the appropriate tokens */
<IN_UNBUFFERED_COMMENT> {
    .*                              { }
    {LineTerminator}                {   yybegin(IN_UNBUFFERED_COMMENT_AFTER_EOL);
                                        eolPosition = tokenLength;
                                    }
}

<IN_UNBUFFERED_COMMENT_AFTER_EOL>              {
    {WhiteSpace}                    {   int indentInComment = tokenLength - eolPosition;
                                        if (indent >= indentInComment) {
                                            yypushback(indentInComment);
                                            yybegin(AFTER_EOL);
                                            if (tokenLength > indentInComment) {
                                                return JadeTokenId.UNBUFFERED_COMMENT;
                                            }
                                        } else {
                                            yybegin(IN_UNBUFFERED_COMMENT);
                                        }
                                    }
    {LineTerminator}                {}                                    
    .                               {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength > 1) {
                                            return JadeTokenId.UNBUFFERED_COMMENT;
                                        }
                                    }   
}

<AFTER_DOCTYPE> {
    {LineTerminator}                { 
                                        yybegin(AFTER_EOL);
                                        indent = 0;
                                        if (tokenLength > 0) {
                                            return JadeTokenId.EOL;
                                        }
                                    }
    {WhiteSpace}                    { return JadeTokenId.WHITESPACE; }
    {Input}                         { yybegin(DOCTYPE);
                                      return JadeTokenId.DOCTYPE_TEMPLATE; }
    
}

<DOCTYPE> {
    {LineTerminator}                { 
                                        yybegin(AFTER_EOL);
                                        indent = 0;
                                        if (tokenLength > 0) {
                                            return JadeTokenId.EOL;
                                        }
                                    }
    ['\"]                            {   yybegin(DOCTYPE_STRING);
                                        return JadeTokenId.DOCTYPE_STRING_START;}
    {WhiteSpace}                    { return JadeTokenId.WHITESPACE; }
    [^'\"\r\n \t\f\u00A0\u000B]+    { return JadeTokenId.DOCTYPE_ATTRIBUTE; }
    
}

<DOCTYPE_STRING> {
    {LineTerminator}                {   yypushback(1);
                                        yybegin(DOCTYPE);
                                        if (tokenLength > 1) {
                                            return JadeTokenId.UNKNOWN;
                                        }
                                    }
    [\"']                           {   yypushback(1);
                                        yybegin(DOCTYPE_STRING_END);
                                        if (tokenLength > 1) {
                                            return JadeTokenId.DOCTYPE_STRING_END;
                                        }
                                    }
    [^\"'\r\n]+                     {   }
}

<DOCTYPE_STRING_END> {
    [\"']                           {   yybegin(DOCTYPE);
                                        return JadeTokenId.DOCTYPE_STRING_END;}
}

<FILEPATH> {
    {LineTerminator}                {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength - 1 > 0) {
                                            return JadeTokenId.FILE_PATH;
                                        }
                                    }
    [^\r\n]                         { }
}

<TEXT_LINE><<EOF>>  {
    {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.TEXT;
    } else {
        return null;
    }}
}
<IN_UNBUFFERED_COMMENT_AFTER_EOL><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.UNBUFFERED_COMMENT;
    } else {
        return null;
    }}
<IN_UNBUFFERED_COMMENT><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.UNBUFFERED_COMMENT;
    } else {
        return null;
    }}
<IN_COMMENT_AFTER_EOL><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.COMMENT;
    } else {
        return null;
    }}
<IN_COMMENT><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.COMMENT;
    } else {
        return null;
    }}
<IN_FILTER_BLOCK_AFTER_EOL><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.FILTER_TEXT;
    } else {
        return null;
    }}
<IN_FILTER_BLOCK><<EOF>>                         {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.FILTER_TEXT;
    } else {
        return null;
    }}
<IN_PLAIN_TEXT_LINE><<EOF>>                         {
                                                        if (input.readLength() > 0 ) {
                                                            input.backup(1);
                                                            return getTokenIdFromTagType(lastTag, JadeTokenId.PLAIN_TEXT);
                                                        } else {
                                                            return null;
                                                        }
    }
<IN_PLAIN_TEXT_BLOCK_AFTER_EOL><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return getTokenIdFromTagType(lastTag, JadeTokenId.PLAIN_TEXT);
    } else {
        return null;
    }}
<IN_PLAIN_TEXT_BLOCK><<EOF>>                         {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return getTokenIdFromTagType(lastTag, JadeTokenId.PLAIN_TEXT);
    } else {
        return null;
    }}
<<EOF>> {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.UNKNOWN;
    } else {
        return null;
    }
}