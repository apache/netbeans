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

package org.netbeans.modules.php.editor.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java_cup.runtime.*;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.web.common.api.ByteStack;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING", "EI_EXPOSE_REP2", "UUF_UNUSED_FIELD"})
%%
// Options adn declarations section

%class ASTPHP5Scanner
%implements Scanner
%type Symbol
%function next_token
%public

%eofval{
    return createSymbol(ASTPHP5Symbols.EOF);
%eofval}
%eofclose

%unicode
%caseless

//Turns character counting on
%char
//Turns line counting on
%line
//Turns column counting on
%column


%state ST_IN_SCRIPTING
%state ST_DOUBLE_QUOTES
%state ST_BACKQUOTE
%state ST_HEREDOC
%state ST_START_HEREDOC
%state ST_END_HEREDOC
%state ST_NOWDOC
%state ST_START_NOWDOC
%state ST_END_NOWDOC
%state ST_LOOKING_FOR_PROPERTY
%state ST_LOOKING_FOR_VARNAME
%state ST_VAR_OFFSET
%state ST_COMMENT
%state ST_DOCBLOCK
%state ST_ONE_LINE_COMMENT
%state ST_IN_SHORT_ECHO
%state ST_HALTED_COMPILER
%{
    private final List commentList = new ArrayList();
    private final Deque<HeredocInfo> heredocStack = new ArrayDeque<>();
    private String heredoc = null;
    private int heredocBodyStart = -1;
    private int heredocBodyLength = 0;
    private final StringBuilder heredocBody = new StringBuilder();
    private String nowdoc = null;
    private int nowdocBodyStart = -1;
    private int nowdocBodyLength = 0;
    private final StringBuilder nowdocBody = new StringBuilder();
    private String comment = null;
    private boolean asp_tags = false;
    private boolean short_tags_allowed = true;
    private ByteStack stack = new ByteStack();
    private char yy_old_buffer[] = new char[ZZ_BUFFERSIZE];
    private int yy_old_pushbackPos;
    protected int commentStartPosition;
    private int whitespaceEndPosition;
    private boolean isEndedPhp;
    private final PHPDocCommentParser docParser = new PHPDocCommentParser();
    private final PHPVarCommentParser varParser = new PHPVarCommentParser();

    public ASTPHP5Scanner(java.io.Reader in, boolean short_tags_allowed, boolean asp_tags_allowed) {
        this(in);
        this.asp_tags = asp_tags_allowed;
        this.short_tags_allowed = short_tags_allowed;
    }
    //private AST ast;

    private int bracket = 0;

    /**
     * Returns balance beween '{' and '}'. If it's equesl 0,
     * then number of '{' == number of '}', if > 0 then '{' > '}' and
     * if return number < 0 then '{' < '}'
     */
    public int getCurlyBalance() {
        return bracket;
    }

    public int getWhitespaceEndPosition() {
        return whitespaceEndPosition;
    }

    public boolean isEndedPhp() {
        return isEndedPhp;
    }

    public boolean useAspTagsAsPhp() {
        return asp_tags;
    }

    public void reset(java.io.Reader reader) {
        yyreset(reader);
    }

    public void setState(int state) {
        yybegin(state);
    }

    public int getState() {
        return yystate();
    }

    public void setInScriptingState() {
        yybegin(ST_IN_SCRIPTING);
    }

    public void resetCommentList() {
        commentList.clear();
    }

    public List getCommentList() {
        return commentList;
    }

    protected void addComment(Comment.Type type) {
        int leftPosition = getTokenStartPosition();
        //System.out.println("#####AddCommnet start: " + commentStartPosition + " end: " + (leftPosition + getTokenLength()) + ", type: " + type);
        Comment comm;
        if (type == Comment.Type.TYPE_PHPDOC) {
            comm = docParser.parse(commentStartPosition, leftPosition + getTokenLength(),  comment);
            comment = null;
        }
        else if(type == Comment.Type.TYPE_VARTYPE) {
            comm = varParser.parse(commentStartPosition, leftPosition + getTokenLength(),  comment);
            comment = null;
            if (comm == null) {
                comm = new Comment(commentStartPosition, leftPosition + getTokenLength(), /*ast,*/ type);
            }
        }
        else {
            comm = new Comment(commentStartPosition, leftPosition + getTokenLength(), /*ast,*/ type);
        }
        commentList.add(comm);
    }

    public void setUseAspTagsAsPhp(boolean useAspTagsAsPhp) {
        asp_tags = useAspTagsAsPhp;
    }

    private void pushState(int state) {
        stack.push(zzLexicalState);
        yybegin(state);
    }

    private void popState() {
        yybegin(stack.pop());
    }

    public int getCurrentLine() {
        return yyline;
    }

    protected int getTokenStartPosition() {
        return zzStartRead - zzPushbackPos;
    }

    protected int getTokenLength() {
        return zzMarkedPos - zzStartRead;
    }

    public int getLength() {
        return zzEndRead - zzPushbackPos;
    }

    private void handleCommentStart() {
        commentStartPosition = getTokenStartPosition();
    }

    private void handleLineCommentEnd() {
        addComment(Comment.Type.TYPE_SINGLE_LINE);
    }

    private void handleMultilineCommentEnd() {
        addComment(Comment.Type.TYPE_MULTILINE);
    }

    private void handlePHPDocEnd() {
        addComment(Comment.Type.TYPE_PHPDOC);
    }

    private void handleVarComment() {
        commentStartPosition = getTokenStartPosition();
        addComment(Comment.Type.TYPE_VARTYPE);
    }

    private Symbol createFullSymbol(int symbolNumber) {
        Symbol symbol = createSymbol(symbolNumber);
        symbol.value = yytext();
        return symbol;
    }

    private Symbol createSymbol(int symbolNumber) {
        int leftPosition = getTokenStartPosition();
        Symbol symbol = new Symbol(symbolNumber, leftPosition, leftPosition + getTokenLength());
        return symbol;
    }

    private void updateNowdocBodyInfo() {
        if (nowdocBodyStart == -1) {
            nowdocBodyStart = getTokenStartPosition();
        }
        nowdocBody.append(yytext());
        nowdocBodyLength += getTokenLength();
    }

    private Symbol createFullNowdocBodySymbol() {
        Symbol symbol = new Symbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE, nowdocBodyStart, nowdocBodyStart + nowdocBodyLength);
        symbol.value = nowdocBody.toString();
        resetNowdocBodyInfo();
        return symbol;
    }

    private void updateHeredocBodyInfo() {
        if (heredocBodyStart == -1) {
            heredocBodyStart = getTokenStartPosition();
        }
        heredocBody.append(yytext());
        heredocBodyLength += getTokenLength();
    }

    private void resetHeredocBodyInfo() {
        heredocBodyStart = -1;
        heredocBodyLength = 0;
        heredocBody.delete(0, heredocBody.length());
    }

    private void resetNowdocBodyInfo() {
        nowdocBodyStart = -1;
        nowdocBodyLength = 0;
        nowdocBody.delete(0, nowdocBody.length());
    }

    private void setHeredocInfo(@NullAllowed HeredocInfo info) {
        if (info != null) {
            heredoc = info.getId();
            heredocBody.append(info.getBody());
            heredocBodyStart = info.getBodyStart();
            heredocBodyLength = info.getBodyLength();
        }
    }

    private Symbol createFullHeredocBodySymbol() {
        Symbol symbol = new Symbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE, heredocBodyStart, heredocBodyStart + heredocBodyLength);
        symbol.value = heredocBody.toString();
        resetHeredocBodyInfo();
        return symbol;
    }

    private boolean isLabelChar(char c) {
        return c == '_'
                || (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= 0x7f && c <= 0xff);
    }

    private boolean isEndHereOrNowdoc(String hereOrNowdoc) {
        // check whether ID exists
        String trimedText = yytext().trim();
        boolean isEnd = false;
        if (trimedText.startsWith(hereOrNowdoc)) {
            if (trimedText.length() == hereOrNowdoc.length()) {
                isEnd = true;
            } else if (trimedText.length() > hereOrNowdoc.length()
                    && !isLabelChar(trimedText.charAt(hereOrNowdoc.length()))) {
                // e.g.
                // $test = <<< END
                // ENDING
                // END
                isEnd = true;
            }
        }
        return isEnd;
    }

    public int[] getParamenters(){
        return new int[]{zzMarkedPos, zzPushbackPos, zzCurrentPos, zzStartRead, zzEndRead, yyline};
    }

    private boolean parsePHPDoc(){
        /*final IDocumentorLexer documentorLexer = getDocumentorLexer(zzReader);
        if(documentorLexer == null){
            return false;
        }
        yypushback(zzMarkedPos - zzStartRead);
        int[] parameters = getParamenters();
        documentorLexer.reset(zzReader, zzBuffer, parameters);
        Object phpDocBlock = documentorLexer.parse();
        commentList.add(phpDocBlock);
        reset(zzReader, documentorLexer.getBuffer(), documentorLexer.getParamenters());*/

        //System.out.println("#######ParsePHPDoc()");
        //return true;
        return false;
    }


    /*protected IDocumentorLexer getDocumentorLexer(java.io.Reader  reader) {
        return null;
    }*/

    public void reset(java.io.Reader  reader, char[] buffer, int[] parameters){
        this.zzReader = reader;
        this.zzBuffer = buffer;
        this.zzMarkedPos = parameters[0];
        this.zzPushbackPos = parameters[1];
        this.zzCurrentPos = parameters[2];
        this.zzStartRead = parameters[3];
        this.zzEndRead = parameters[4];
        this.yyline = parameters[5];
        this.yychar = this.zzStartRead - this.zzPushbackPos;
    }

    //~ inner class
    private static final class HeredocInfo {

        private final String id;
        private final String body;
        private final int bodyStart;
        private final int bodyLength;

        public HeredocInfo(String id, String body, int bodyStart, int bodyLength) {
            this.id = id;
            this.body = body;
            this.bodyStart = bodyStart;
            this.bodyLength = bodyLength;
        }

        public String getId() {
            return id;
        }

        public String getBody() {
            return body;
        }

        public int getBodyStart() {
            return bodyStart;
        }

        public int getBodyLength() {
            return bodyLength;
        }
    }
%}

LNUM=[0-9]+(_[0-9]+)*
DNUM=({LNUM}?[\.]{LNUM})|({LNUM}[\.]{LNUM}?)
EXPONENT_DNUM=(({LNUM}|{DNUM})[eE][+-]?{LNUM})
HNUM="0x"[0-9a-fA-F]+(_[0-9a-fA-F]+)*
BNUM="0b"[01]+(_[01]+)*
ONUM="0o"[0-7]+(_[0-7]+)* // PHP 8.1: Explicit octal integer literal notation
//LABEL=[a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*
LABEL=([[:letter:]_]|[\u007f-\u00ff])([[:letter:][:digit:]_]|[\u007f-\u00ff])*
NAMESPACE_SEPARATOR=[\\]
QUALIFIED_LABEL=({NAMESPACE_SEPARATOR}?{LABEL})+
WHITESPACE=[ \n\r\t]+
TABS_AND_SPACES=[ \t]*
ANY_CHAR=[^]
NEWLINE=("\r"|"\n"|"\r\n")
DOUBLE_QUOTES_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$\"\\{]|("\\"{ANY_CHAR})))
BACKQUOTE_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$`\\{]|("\\"{ANY_CHAR})))
HEREDOC_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$\n\r\\{]|("\\"[^\n\r])))
HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR=(("{"+[^$\n\r\\{])|("{"*"\\"[^\n\r])|{HEREDOC_LITERAL_DOLLAR})
HEREDOC_NON_LABEL=([^a-zA-Z_\x7f-\xff$\n\r\\{]|{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})
HEREDOC_LABEL_NO_NEWLINE=({LABEL}([^a-zA-Z0-9_\x7f-\xff;$\n\r\\{]|(";"[^$\n\r\\{])|(";"?{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})))
DOUBLE_QUOTES_CHARS=("{"*([^$\"\\{]|("\\"{ANY_CHAR}))|{DOUBLE_QUOTES_LITERAL_DOLLAR})
BACKQUOTE_CHARS=("{"*([^$`\\{]|("\\"{ANY_CHAR}))|{BACKQUOTE_LITERAL_DOLLAR})
HEREDOC_CHARS=([^$\\{]|("\\"{ANY_CHAR}))({HEREDOC_LABEL_NO_NEWLINE} | {HEREDOC_NON_LABEL} | {LABEL})*
NOWDOC_CHARS=({NEWLINE}*(([^a-zA-Z_\x7f-\xff\n\r][^\n\r]*)|({LABEL}[^a-zA-Z0-9_\x7f-\xff;\n\r][^\n\r]*)|({LABEL}[;][^\n\r]+)))

%%

<ST_IN_SHORT_ECHO>"=" {
    yybegin(ST_IN_SCRIPTING);
    return createSymbol(ASTPHP5Symbols.T_ECHO);
}

<ST_IN_SCRIPTING>"exit" {
    return createFullSymbol(ASTPHP5Symbols.T_EXIT);
}

<ST_IN_SCRIPTING>"die" {
    return createFullSymbol(ASTPHP5Symbols.T_EXIT);
}

<ST_IN_SCRIPTING>"fn" {
    // PHP 7.4 Arrow Functions 2.0
    // https://wiki.php.net/rfc/arrow_functions_v2
    return createFullSymbol(ASTPHP5Symbols.T_FN);
}

<ST_IN_SCRIPTING>"function" {
    return createFullSymbol(ASTPHP5Symbols.T_FUNCTION);
}

<ST_IN_SCRIPTING>"const" {
    return createFullSymbol(ASTPHP5Symbols.T_CONST);
}

<ST_IN_SCRIPTING>"return" {
    return createFullSymbol(ASTPHP5Symbols.T_RETURN);
}

// NETBEANS-4443 PHP 8.0: Attribute Syntax
// https://wiki.php.net/rfc/attributes_v2
// https://wiki.php.net/rfc/shorter_attribute_syntax
// https://wiki.php.net/rfc/shorter_attribute_syntax_change
<ST_IN_SCRIPTING>"#[" {
    return createSymbol(ASTPHP5Symbols.T_ATTRIBUTE);
}

<ST_IN_SCRIPTING>"yield"{WHITESPACE}+"from" {
    return createSymbol(ASTPHP5Symbols.T_YIELD_FROM);
}

<ST_IN_SCRIPTING>"yield" {
    return createFullSymbol(ASTPHP5Symbols.T_YIELD);
}

<ST_IN_SCRIPTING>"try" {
    return createFullSymbol(ASTPHP5Symbols.T_TRY);
}

<ST_IN_SCRIPTING>"catch" {
    return createFullSymbol(ASTPHP5Symbols.T_CATCH);
}

<ST_IN_SCRIPTING>"throw" {
    return createFullSymbol(ASTPHP5Symbols.T_THROW);
}

<ST_IN_SCRIPTING>"finally" {
    return createFullSymbol(ASTPHP5Symbols.T_FINALLY);
}

<ST_IN_SCRIPTING>"if" {
    return createFullSymbol(ASTPHP5Symbols.T_IF);
}

<ST_IN_SCRIPTING>"elseif" {
    return createFullSymbol(ASTPHP5Symbols.T_ELSEIF);
}

<ST_IN_SCRIPTING>"endif" {
    return createFullSymbol(ASTPHP5Symbols.T_ENDIF);
}

<ST_IN_SCRIPTING>"else" {
    return createFullSymbol(ASTPHP5Symbols.T_ELSE);
}

<ST_IN_SCRIPTING>"while" {
    return createFullSymbol(ASTPHP5Symbols.T_WHILE);
}

<ST_IN_SCRIPTING>"endwhile" {
    return createFullSymbol(ASTPHP5Symbols.T_ENDWHILE);
}

<ST_IN_SCRIPTING>"do" {
    return createFullSymbol(ASTPHP5Symbols.T_DO);
}

<ST_IN_SCRIPTING>"for" {
    return createFullSymbol(ASTPHP5Symbols.T_FOR);
}

<ST_IN_SCRIPTING>"endfor" {
    return createFullSymbol(ASTPHP5Symbols.T_ENDFOR);
}

<ST_IN_SCRIPTING>"foreach" {
    return createFullSymbol(ASTPHP5Symbols.T_FOREACH);
}

<ST_IN_SCRIPTING>"endforeach" {
    return createFullSymbol(ASTPHP5Symbols.T_ENDFOREACH);
}

<ST_IN_SCRIPTING>"declare" {
    return createFullSymbol(ASTPHP5Symbols.T_DECLARE);
}

<ST_IN_SCRIPTING>"enddeclare" {
    return createFullSymbol(ASTPHP5Symbols.T_ENDDECLARE);
}

<ST_IN_SCRIPTING>"instanceof" {
    return createFullSymbol(ASTPHP5Symbols.T_INSTANCEOF);
}

<ST_IN_SCRIPTING>"insteadof" {
    return createFullSymbol(ASTPHP5Symbols.T_INSTEADOF);
}

<ST_IN_SCRIPTING>"as" {
    return createFullSymbol(ASTPHP5Symbols.T_AS);
}

<ST_IN_SCRIPTING>"switch" {
    return createFullSymbol(ASTPHP5Symbols.T_SWITCH);
}

<ST_IN_SCRIPTING>"endswitch" {
    return createFullSymbol(ASTPHP5Symbols.T_ENDSWITCH);
}

<ST_IN_SCRIPTING>"match" {
    return createFullSymbol(ASTPHP5Symbols.T_MATCH);
}

<ST_IN_SCRIPTING>"case" {
    return createFullSymbol(ASTPHP5Symbols.T_CASE);
}

<ST_IN_SCRIPTING>"default" {
    return createFullSymbol(ASTPHP5Symbols.T_DEFAULT);
}

<ST_IN_SCRIPTING>"break" {
    return createFullSymbol(ASTPHP5Symbols.T_BREAK);
}

<ST_IN_SCRIPTING>"continue" {
    return createFullSymbol(ASTPHP5Symbols.T_CONTINUE);
}

<ST_IN_SCRIPTING>"goto" {
    return createFullSymbol(ASTPHP5Symbols.T_GOTO);
}

<ST_IN_SCRIPTING>"echo" {
    return createFullSymbol(ASTPHP5Symbols.T_ECHO);
}

<ST_IN_SCRIPTING>"print" {
    return createFullSymbol(ASTPHP5Symbols.T_PRINT);
}

<ST_IN_SCRIPTING>"class" {
    return createFullSymbol(ASTPHP5Symbols.T_CLASS);
}

<ST_IN_SCRIPTING>"trait" {
    return createFullSymbol(ASTPHP5Symbols.T_TRAIT);
}

<ST_IN_SCRIPTING>"interface" {
    return createFullSymbol(ASTPHP5Symbols.T_INTERFACE);
}

<ST_IN_SCRIPTING>"enum"{WHITESPACE}("extends"|"implements") {
    yypushback(yylength() - 4); // 4: enum length
    return createFullSymbol(ASTPHP5Symbols.T_STRING);
}

<ST_IN_SCRIPTING>"enum"{WHITESPACE}[a-zA-Z_\x80-\xff] {
    yypushback(yylength() - 4); // 4: enum length
    return createFullSymbol(ASTPHP5Symbols.T_ENUM);
}

<ST_IN_SCRIPTING>"extends" {
    return createFullSymbol(ASTPHP5Symbols.T_EXTENDS);
}

<ST_IN_SCRIPTING>"implements" {
    return createFullSymbol(ASTPHP5Symbols.T_IMPLEMENTS);
}

<ST_IN_SCRIPTING>"->" {
    pushState(ST_LOOKING_FOR_PROPERTY);
    return createSymbol(ASTPHP5Symbols.T_OBJECT_OPERATOR);
}

// NETBEANS-4443 PHP 8.0: Nullsafe operator
// https://wiki.php.net/rfc/nullsafe_operator
<ST_IN_SCRIPTING>"?->" {
    pushState(ST_LOOKING_FOR_PROPERTY);
    return createSymbol(ASTPHP5Symbols.T_NULLSAFE_OBJECT_OPERATOR);
}

<ST_IN_SCRIPTING,ST_LOOKING_FOR_PROPERTY>{WHITESPACE}+ {
    whitespaceEndPosition = getTokenStartPosition() + yylength();
}

<ST_LOOKING_FOR_PROPERTY>"->" {
    return createSymbol(ASTPHP5Symbols.T_OBJECT_OPERATOR);
}

// NETBEANS-4443 PHP 8.0: Nullsafe operator
// https://wiki.php.net/rfc/nullsafe_operator
<ST_LOOKING_FOR_PROPERTY>"?->" {
    return createSymbol(ASTPHP5Symbols.T_NULLSAFE_OBJECT_OPERATOR);
}

<ST_LOOKING_FOR_PROPERTY>{LABEL} {
    popState();
    return createFullSymbol(ASTPHP5Symbols.T_STRING);
}

<ST_LOOKING_FOR_PROPERTY>{ANY_CHAR} {
    yypushback(yylength());
    popState();
}

<ST_IN_SCRIPTING>"::" {
    return createSymbol(ASTPHP5Symbols.T_PAAMAYIM_NEKUDOTAYIM);
}

<ST_IN_SCRIPTING>"namespace"("\\"{LABEL})+ {
    return createFullSymbol(ASTPHP5Symbols.T_NAME_RELATIVE);
}

<ST_IN_SCRIPTING>{LABEL}("\\"{LABEL})+ {
    return createFullSymbol(ASTPHP5Symbols.T_NAME_QUALIFIED);
}

<ST_IN_SCRIPTING>"\\"{LABEL}("\\"{LABEL})* {
    return createFullSymbol(ASTPHP5Symbols.T_NAME_FULLY_QUALIFIED);
}

<ST_IN_SCRIPTING>"\\" {
    return createSymbol(ASTPHP5Symbols.T_NS_SEPARATOR);
}

<ST_IN_SCRIPTING>"new" {
    return createFullSymbol(ASTPHP5Symbols.T_NEW);
}

<ST_IN_SCRIPTING>"clone" {
    return createFullSymbol(ASTPHP5Symbols.T_CLONE);
}

<ST_IN_SCRIPTING>"var" {
    return createFullSymbol(ASTPHP5Symbols.T_VAR);
}

<ST_IN_SCRIPTING>"("{TABS_AND_SPACES}("int"|"integer"){TABS_AND_SPACES}")" {
    return createSymbol(ASTPHP5Symbols.T_INT_CAST);
}

<ST_IN_SCRIPTING>"("{TABS_AND_SPACES}("real"|"double"|"float"){TABS_AND_SPACES}")" {
    return createSymbol(ASTPHP5Symbols.T_DOUBLE_CAST);
}

<ST_IN_SCRIPTING>"("{TABS_AND_SPACES}"string"{TABS_AND_SPACES}")" {
    return createSymbol(ASTPHP5Symbols.T_STRING_CAST);
}

<ST_IN_SCRIPTING>"("{TABS_AND_SPACES}"binary"{TABS_AND_SPACES}")" {
    return createSymbol(ASTPHP5Symbols.T_STRING_CAST);
}

<ST_IN_SCRIPTING>"("{TABS_AND_SPACES}"array"{TABS_AND_SPACES}")" {
    return createSymbol(ASTPHP5Symbols.T_ARRAY_CAST);
}

<ST_IN_SCRIPTING>"("{TABS_AND_SPACES}"object"{TABS_AND_SPACES}")" {
    return createSymbol(ASTPHP5Symbols.T_OBJECT_CAST);
}

<ST_IN_SCRIPTING>"("{TABS_AND_SPACES}("bool"|"boolean"){TABS_AND_SPACES}")" {
    return createSymbol(ASTPHP5Symbols.T_BOOL_CAST);
}

<ST_IN_SCRIPTING>"("{TABS_AND_SPACES}("unset"){TABS_AND_SPACES}")" {
    return createSymbol(ASTPHP5Symbols.T_UNSET_CAST);
}

<ST_IN_SCRIPTING>"eval" {
    return createFullSymbol(ASTPHP5Symbols.T_EVAL);
}

<ST_IN_SCRIPTING>"include" {
    return createFullSymbol(ASTPHP5Symbols.T_INCLUDE);
}

<ST_IN_SCRIPTING>"include_once" {
    return createFullSymbol(ASTPHP5Symbols.T_INCLUDE_ONCE);
}

<ST_IN_SCRIPTING>"require" {
    return createFullSymbol(ASTPHP5Symbols.T_REQUIRE);
}

<ST_IN_SCRIPTING>"require_once" {
    return createFullSymbol(ASTPHP5Symbols.T_REQUIRE_ONCE);
}

<ST_IN_SCRIPTING>"namespace" {
     return createFullSymbol(ASTPHP5Symbols.T_NAMESPACE);
}

<ST_IN_SCRIPTING>"use" {
    return createFullSymbol(ASTPHP5Symbols.T_USE);
}

<ST_IN_SCRIPTING>"global" {
    return createFullSymbol(ASTPHP5Symbols.T_GLOBAL);
}

<ST_IN_SCRIPTING>"isset" {
    return createFullSymbol(ASTPHP5Symbols.T_ISSET);
}

<ST_IN_SCRIPTING>"empty" {
    return createFullSymbol(ASTPHP5Symbols.T_EMPTY);
}

<ST_IN_SCRIPTING>"__halt_compiler();" {
    yybegin(ST_HALTED_COMPILER);
    return createSymbol(ASTPHP5Symbols.T_HALT_COMPILER);
}

<ST_HALTED_COMPILER> {ANY_CHAR}+ {
    return createSymbol(ASTPHP5Symbols.T_INLINE_HTML);
}

<ST_IN_SCRIPTING>"static" {
    return createFullSymbol(ASTPHP5Symbols.T_STATIC);
}

<ST_IN_SCRIPTING>"abstract" {
    return createFullSymbol(ASTPHP5Symbols.T_ABSTRACT);
}

<ST_IN_SCRIPTING>"final" {
    return createFullSymbol(ASTPHP5Symbols.T_FINAL);
}

<ST_IN_SCRIPTING>"private" {
    return createFullSymbol(ASTPHP5Symbols.T_PRIVATE);
}

<ST_IN_SCRIPTING>"protected" {
    return createFullSymbol(ASTPHP5Symbols.T_PROTECTED);
}

<ST_IN_SCRIPTING>"public" {
    return createFullSymbol(ASTPHP5Symbols.T_PUBLIC);
}

<ST_IN_SCRIPTING>"readonly" {
    return createFullSymbol(ASTPHP5Symbols.T_READONLY);
}

<ST_IN_SCRIPTING>"unset" {
    return createFullSymbol(ASTPHP5Symbols.T_UNSET);
}

<ST_IN_SCRIPTING>"=>" {
    return createSymbol(ASTPHP5Symbols.T_DOUBLE_ARROW);
}

<ST_IN_SCRIPTING>"list" {
    return createFullSymbol(ASTPHP5Symbols.T_LIST);
}

<ST_IN_SCRIPTING>"array" {
    return createFullSymbol(ASTPHP5Symbols.T_ARRAY);
}

<ST_IN_SCRIPTING>"callable" {
    return createFullSymbol(ASTPHP5Symbols.T_CALLABLE);
}

<ST_IN_SCRIPTING>"++" {
    return createSymbol(ASTPHP5Symbols.T_INC);
}

<ST_IN_SCRIPTING>"--" {
    return createSymbol(ASTPHP5Symbols.T_DEC);
}

<ST_IN_SCRIPTING>"===" {
    return createSymbol(ASTPHP5Symbols.T_IS_IDENTICAL);
}

<ST_IN_SCRIPTING>"!==" {
    return createSymbol(ASTPHP5Symbols.T_IS_NOT_IDENTICAL);
}

<ST_IN_SCRIPTING>"==" {
    return createSymbol(ASTPHP5Symbols.T_IS_EQUAL);
}

<ST_IN_SCRIPTING>"!="|"<>" {
    return createSymbol(ASTPHP5Symbols.T_IS_NOT_EQUAL);
}

<ST_IN_SCRIPTING>"<=" {
    return createSymbol(ASTPHP5Symbols.T_IS_SMALLER_OR_EQUAL);
}

<ST_IN_SCRIPTING>">=" {
    return createSymbol(ASTPHP5Symbols.T_IS_GREATER_OR_EQUAL);
}

<ST_IN_SCRIPTING>"<=>" {
    return createSymbol(ASTPHP5Symbols.T_SPACESHIP);
}

<ST_IN_SCRIPTING>"+=" {
    return createSymbol(ASTPHP5Symbols.T_PLUS_EQUAL);
}

<ST_IN_SCRIPTING>"-=" {
    return createSymbol(ASTPHP5Symbols.T_MINUS_EQUAL);
}

<ST_IN_SCRIPTING>"*=" {
    return createSymbol(ASTPHP5Symbols.T_MUL_EQUAL);
}

<ST_IN_SCRIPTING>"/=" {
    return createSymbol(ASTPHP5Symbols.T_DIV_EQUAL);
}

<ST_IN_SCRIPTING>".=" {
    return createSymbol(ASTPHP5Symbols.T_CONCAT_EQUAL);
}

<ST_IN_SCRIPTING>"%=" {
    return createSymbol(ASTPHP5Symbols.T_MOD_EQUAL);
}

<ST_IN_SCRIPTING>"<<=" {
    return createSymbol(ASTPHP5Symbols.T_SL_EQUAL);
}

<ST_IN_SCRIPTING>">>=" {
    return createSymbol(ASTPHP5Symbols.T_SR_EQUAL);
}

<ST_IN_SCRIPTING>"&=" {
    return createSymbol(ASTPHP5Symbols.T_AND_EQUAL);
}

<ST_IN_SCRIPTING>"|=" {
    return createSymbol(ASTPHP5Symbols.T_OR_EQUAL);
}

<ST_IN_SCRIPTING>"^=" {
    return createSymbol(ASTPHP5Symbols.T_XOR_EQUAL);
}

<ST_IN_SCRIPTING>"||" {
    return createSymbol(ASTPHP5Symbols.T_BOOLEAN_OR);
}

<ST_IN_SCRIPTING>"&&" {
    return createSymbol(ASTPHP5Symbols.T_BOOLEAN_AND);
}

<ST_IN_SCRIPTING>"OR" {
    return createFullSymbol(ASTPHP5Symbols.T_LOGICAL_OR);
}

<ST_IN_SCRIPTING>"AND" {
    return createFullSymbol(ASTPHP5Symbols.T_LOGICAL_AND);
}

<ST_IN_SCRIPTING>"XOR" {
    return createFullSymbol(ASTPHP5Symbols.T_LOGICAL_XOR);
}

<ST_IN_SCRIPTING>"<<" {
    return createSymbol(ASTPHP5Symbols.T_SL);
}

<ST_IN_SCRIPTING>">>" {
    return createSymbol(ASTPHP5Symbols.T_SR);
}

<ST_IN_SCRIPTING>"**" {
    return createSymbol(ASTPHP5Symbols.T_POW);
}

<ST_IN_SCRIPTING>"**=" {
    return createSymbol(ASTPHP5Symbols.T_POW_EQUAL);
}

<ST_IN_SCRIPTING>"..." {
    return createSymbol(ASTPHP5Symbols.T_ELLIPSIS);
}

<ST_IN_SCRIPTING>"??" {
    return createSymbol(ASTPHP5Symbols.T_COALESCE);
}

<ST_IN_SCRIPTING>"??=" {
    return createSymbol(ASTPHP5Symbols.T_COALESCE_EQUAL);
}

<ST_IN_SCRIPTING>"&"{TABS_AND_SPACES}("$"|"...") {
    yypushback(yylength() - 1);
    return createSymbol(ASTPHP5Symbols.T_REFERENCE);
}

// TOKENS
<ST_IN_SCRIPTING> {
    ";"                     {return createSymbol(ASTPHP5Symbols.T_SEMICOLON);}
    ":"                     {return createSymbol(ASTPHP5Symbols.T_NEKUDOTAIM);}
    ","                     {return createSymbol(ASTPHP5Symbols.T_COMMA);}
    "."                     {return createSymbol(ASTPHP5Symbols.T_NEKUDA);}
    "["                     {return createSymbol(ASTPHP5Symbols.T_OPEN_RECT);}
    "]"                     {return createSymbol(ASTPHP5Symbols.T_CLOSE_RECT);}
    "("                     {return createSymbol(ASTPHP5Symbols.T_OPEN_PARENTHESE);}
    ")"                     {return createSymbol(ASTPHP5Symbols.T_CLOSE_PARENTHESE);}
    "|"                     {return createSymbol(ASTPHP5Symbols.T_OR);}
    "^"                     {return createSymbol(ASTPHP5Symbols.T_KOVA);}
    "&"                     {return createSymbol(ASTPHP5Symbols.T_AMPERSAND_NOT_FOLLOWED_BY_VAR_OR_VARARG);}
    "+"                     {return createSymbol(ASTPHP5Symbols.T_PLUS);}
    "-"                     {return createSymbol(ASTPHP5Symbols.T_MINUS);}
    "/"                     {return createSymbol(ASTPHP5Symbols.T_DIV);}
    "*"                     {return createSymbol(ASTPHP5Symbols.T_TIMES);}
    "="                     {return createSymbol(ASTPHP5Symbols.T_EQUAL);}
    "%"                     {return createSymbol(ASTPHP5Symbols.T_PRECENT);}
    "!"                     {return createSymbol(ASTPHP5Symbols.T_NOT);}
    "~"                     {return createSymbol(ASTPHP5Symbols.T_TILDA);}
    "$"                     {return createSymbol(ASTPHP5Symbols.T_DOLLAR);}
    "<"                     {return createSymbol(ASTPHP5Symbols.T_RGREATER);}
    ">"                     {return createSymbol(ASTPHP5Symbols.T_LGREATER);}
    "?"                     {return createSymbol(ASTPHP5Symbols.T_QUESTION_MARK);}
    "@"                     {return createSymbol(ASTPHP5Symbols.T_AT);}
}

<ST_IN_SCRIPTING>"{" {
    pushState(ST_IN_SCRIPTING);
    bracket++;
    return createSymbol(ASTPHP5Symbols.T_CURLY_OPEN);

}

<ST_DOUBLE_QUOTES,ST_BACKQUOTE,ST_HEREDOC>"${" {
    pushState(ST_LOOKING_FOR_VARNAME);
    return createSymbol(ASTPHP5Symbols.T_DOLLAR_OPEN_CURLY_BRACES);
}

<ST_IN_SCRIPTING>"}" {
    /* This is a temporary fix which is dependant on flex and it's implementation */
    if (!stack.isEmpty()) {
        popState();
    }
    bracket--;
    return createSymbol(ASTPHP5Symbols.T_CURLY_CLOSE);
}

<ST_LOOKING_FOR_VARNAME>{LABEL} {
    popState();
    pushState(ST_IN_SCRIPTING);
    return createFullSymbol(ASTPHP5Symbols.T_STRING_VARNAME);
}

<ST_LOOKING_FOR_VARNAME>{ANY_CHAR} {
    yypushback(yylength());
    popState();
    pushState(ST_IN_SCRIPTING);
}

<ST_IN_SCRIPTING>{ONUM} {
    // PHP 8.1: Explicit octal integer literal notation
    // https://wiki.php.net/rfc/explicit_octal_notation
    return createFullSymbol(ASTPHP5Symbols.T_LNUMBER);
}

<ST_IN_SCRIPTING>{LNUM} {
    return createFullSymbol(ASTPHP5Symbols.T_LNUMBER);
}

<ST_IN_SCRIPTING>{HNUM} {
    return createFullSymbol(ASTPHP5Symbols.T_DNUMBER);
}

<ST_IN_SCRIPTING>{BNUM} {
    return createFullSymbol(ASTPHP5Symbols.T_DNUMBER);
}

<ST_VAR_OFFSET>0|([1-9][0-9]*) {
    return createFullSymbol(ASTPHP5Symbols.T_NUM_STRING);
}

<ST_VAR_OFFSET>{LNUM}|{HNUM}|{BNUM} { /* treat numbers (almost) as strings inside encapsulated strings */
    return createFullSymbol(ASTPHP5Symbols.T_NUM_STRING);
}

<ST_IN_SCRIPTING>{DNUM}|{EXPONENT_DNUM} {
    return createFullSymbol(ASTPHP5Symbols.T_DNUMBER);
}

<ST_IN_SCRIPTING>"__CLASS__" {
    return createFullSymbol(ASTPHP5Symbols.T_CLASS_C);
}

<ST_IN_SCRIPTING>"__TRAIT__" {
    return createFullSymbol(ASTPHP5Symbols.T_TRAIT_C);
}

<ST_IN_SCRIPTING>"__FUNCTION__" {
    return createFullSymbol(ASTPHP5Symbols.T_FUNC_C);
}

<ST_IN_SCRIPTING>"__METHOD__" {
    return createFullSymbol(ASTPHP5Symbols.T_METHOD_C);
}

<ST_IN_SCRIPTING>"__LINE__" {
    return createFullSymbol(ASTPHP5Symbols.T_LINE);
}

<ST_IN_SCRIPTING>"__FILE__" {
    return createFullSymbol(ASTPHP5Symbols.T_FILE);
}

<ST_IN_SCRIPTING>"__DIR__" {
    return createFullSymbol(ASTPHP5Symbols.T_DIR);
}

<ST_IN_SCRIPTING>"__NAMESPACE__" {
    return createFullSymbol(ASTPHP5Symbols.T_NS_C);
}

<YYINITIAL>(([^<]|"<"[^?%s<])+)|"<s"|"<" {
    return createSymbol(ASTPHP5Symbols.T_INLINE_HTML);
}

<YYINITIAL>"<?"|"<script"{WHITESPACE}+"language"{WHITESPACE}*"="{WHITESPACE}*("php"|"\"php\""|"\'php\'"){WHITESPACE}*">" {
    if (short_tags_allowed || yylength()>2) { /* yyleng>2 means it's not <? but <script> */
        yybegin(ST_IN_SCRIPTING);
        //return T_OPEN_TAG;
        //return createSymbol(ASTPHP5Symbols.T_OPEN_TAG);
    } else {
        return createSymbol(ASTPHP5Symbols.T_INLINE_HTML);
    }
}

<YYINITIAL>"<%="|"<?=" {
    String text = yytext();
    if ((text.charAt(1)=='%' && asp_tags)
        || (text.charAt(1)=='?')) {
        yypushback(1);
        yybegin(ST_IN_SHORT_ECHO);
    } else {
        return createSymbol(ASTPHP5Symbols.T_INLINE_HTML);
    }
}

<YYINITIAL>"<%" {
    if (asp_tags) {
        yybegin(ST_IN_SCRIPTING);
        //return T_OPEN_TAG;
        //return createSymbol(ASTPHP5Symbols.T_OPEN_TAG);
    } else {
        return createSymbol(ASTPHP5Symbols.T_INLINE_HTML);
    }
}

<YYINITIAL>"<?php"([ \t]|{NEWLINE}) {
    isEndedPhp = false;
    whitespaceEndPosition = getTokenStartPosition() + yylength();
    yybegin(ST_IN_SCRIPTING);
    //return T_OPEN_TAG;
    //return createSymbol(ASTPHP5Symbols.T_OPEN_TAG);
}

<ST_IN_SCRIPTING,ST_DOUBLE_QUOTES,ST_HEREDOC,ST_BACKQUOTE,ST_VAR_OFFSET>"$"{LABEL} {
    return createFullSymbol(ASTPHP5Symbols.T_VARIABLE);
}

<ST_DOUBLE_QUOTES,ST_HEREDOC,ST_BACKQUOTE>"$"{LABEL}"->"[a-zA-Z_\x7f-\xff] {
    yypushback(3);
    pushState(ST_LOOKING_FOR_PROPERTY);
    return createFullSymbol(ASTPHP5Symbols.T_VARIABLE);
}

<ST_DOUBLE_QUOTES,ST_HEREDOC,ST_BACKQUOTE>"$"{LABEL}"[" {
    yypushback(1);
    pushState(ST_VAR_OFFSET);
    return createFullSymbol(ASTPHP5Symbols.T_VARIABLE);
}

<ST_VAR_OFFSET>"]" {
    popState();
    return createSymbol(ASTPHP5Symbols.T_CLOSE_RECT);
}

//this is instead {TOKENS}|[{}"`]
<ST_VAR_OFFSET> {
    ";"                     {return createSymbol(ASTPHP5Symbols.T_SEMICOLON);}
    ":"                     {return createSymbol(ASTPHP5Symbols.T_NEKUDOTAIM);}
    ","                     {return createSymbol(ASTPHP5Symbols.T_COMMA);}
    "."                     {return createSymbol(ASTPHP5Symbols.T_NEKUDA);}
    "["                     {return createSymbol(ASTPHP5Symbols.T_OPEN_RECT);}
//    "]"                     {return createSymbol(ASTPHP5Symbols.T_CLOSE_RECT);} //we dont need this line because the rule before deals with it
    "("                     {return createSymbol(ASTPHP5Symbols.T_OPEN_PARENTHESE);}
    ")"                     {return createSymbol(ASTPHP5Symbols.T_CLOSE_PARENTHESE);}
    "|"                     {return createSymbol(ASTPHP5Symbols.T_OR);}
    "^"                     {return createSymbol(ASTPHP5Symbols.T_KOVA);}
    "&"                     {return createSymbol(ASTPHP5Symbols.T_REFERENCE);}
    "+"                     {return createSymbol(ASTPHP5Symbols.T_PLUS);}
    "-"                     {return createSymbol(ASTPHP5Symbols.T_MINUS);}
    "/"                     {return createSymbol(ASTPHP5Symbols.T_DIV);}
    "*"                     {return createSymbol(ASTPHP5Symbols.T_TIMES);}
    "="                     {return createSymbol(ASTPHP5Symbols.T_EQUAL);}
    "%"                     {return createSymbol(ASTPHP5Symbols.T_PRECENT);}
    "!"                     {return createSymbol(ASTPHP5Symbols.T_NOT);}
    "~"                     {return createSymbol(ASTPHP5Symbols.T_TILDA);}
    "$"                     {return createSymbol(ASTPHP5Symbols.T_DOLLAR);}
    "<"                     {return createSymbol(ASTPHP5Symbols.T_RGREATER);}
    ">"                     {return createSymbol(ASTPHP5Symbols.T_LGREATER);}
    "?"                     {return createSymbol(ASTPHP5Symbols.T_QUESTION_MARK);}
    "@"                     {return createSymbol(ASTPHP5Symbols.T_AT);}
    "{"                     {bracket++; return createSymbol(ASTPHP5Symbols.T_CURLY_OPEN);}
    "}"                     {bracket--; return createSymbol(ASTPHP5Symbols.T_CURLY_CLOSE);}
    "\""                     {return createSymbol(ASTPHP5Symbols.T_QUATE);}
    "`"                     {return createSymbol(ASTPHP5Symbols.T_BACKQUATE);}
}

<ST_VAR_OFFSET>[ \n\r\t\\'#] {
    yypushback(1);
    popState();
    /*<ST_VAR_OFFSET>[ \n\r\t\\'#]*/
    return createSymbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE);
}

<ST_IN_SCRIPTING>"define" {
    /* not a keyword, hust for recognize constans.*/
    return createFullSymbol(ASTPHP5Symbols.T_DEFINE);
}

<ST_IN_SCRIPTING,ST_VAR_OFFSET>{LABEL} {
    return createFullSymbol(ASTPHP5Symbols.T_STRING);
}

<ST_IN_SCRIPTING>"#"|"//" {
    handleCommentStart();
    yybegin(ST_ONE_LINE_COMMENT);
//    yymore();
}

<ST_ONE_LINE_COMMENT>"?"|"%"|">" {
    //    yymore();
}

<ST_ONE_LINE_COMMENT>[^\n\r?%>]*(.|{NEWLINE}) {
    String yytext = yytext();
    switch (yytext.charAt(yytext.length() - 1)) {
        case '?':
        case '%':
        case '>':
            yypushback(1);
            break;
        default:
            handleLineCommentEnd();
            yybegin(ST_IN_SCRIPTING);
    }
//    yymore();
}

<ST_ONE_LINE_COMMENT>"?>"|"%>" {
    if (asp_tags || yytext().charAt(0)!='%') { /* asp comment? */
        isEndedPhp = true;
        handleLineCommentEnd();
        yypushback(yylength());
        yybegin(ST_IN_SCRIPTING);
        //return T_COMMENT;
    }
}

<ST_IN_SCRIPTING>"/*"{WHITESPACE}*"@var"{WHITESPACE}("$"?){LABEL}("["({LABEL} | "\"" | "'")*"]")*{WHITESPACE}("?"?)("("?){QUALIFIED_LABEL}("[""]")*([|&]("("?){QUALIFIED_LABEL}("[""]")*(")"?))*{WHITESPACE}?"*/" {
    comment = yytext();
    handleVarComment();
    // if we want to handle the var comment in  ast, then return the T_VAR_Comment symbol
    // but it needs some changes in parser grammar. see issue #154967
    //return createFullSymbol(ASTPHP5Symbols.T_VAR_COMMENT);
}

<ST_IN_SCRIPTING>"/**"{WHITESPACE}*"@var"{WHITESPACE}("?"?)("("?){QUALIFIED_LABEL}("[""]")*([|&]("("?){QUALIFIED_LABEL}("[""]")*(")"?))*{WHITESPACE}("$"){LABEL}("["({LABEL} | "\"" | "'")*"]")*{WHITESPACE}?[^\n\r]*"*/" {
    comment = yytext();
    handleVarComment();
}

<ST_IN_SCRIPTING>"/**" {
    if (!parsePHPDoc()) {
        handleCommentStart();
        yybegin(ST_DOCBLOCK);
    }
}

<ST_DOCBLOCK>"*/" {
    handlePHPDocEnd();
    yybegin(ST_IN_SCRIPTING);
}

<ST_DOCBLOCK>~"*/" {
    int len = yylength();
    yypushback(2); // go back to mark end of comment in the next token
    comment = yytext();
}

<ST_DOCBLOCK> <<EOF>> {
    if (yytext().length() > 0) {
        yypushback(1);  // backup eof
        comment = yytext();
    }
    else {
        return createSymbol(ASTPHP5Symbols.EOF);
    }

}

<ST_IN_SCRIPTING>"/**/" {
    handleCommentStart();
}

<ST_IN_SCRIPTING>"/*" {
    handleCommentStart();
    yybegin(ST_COMMENT);
}

<ST_COMMENT>[^*]+ {
}

<ST_COMMENT>"*/" {
    handleMultilineCommentEnd();
    yybegin(ST_IN_SCRIPTING);
}

<ST_COMMENT>"*" {
//    yymore();
}

<ST_IN_SCRIPTING>("?>"|"</script"{WHITESPACE}*">"){NEWLINE}? {
    isEndedPhp = true;
    yybegin(YYINITIAL);
    return createSymbol(ASTPHP5Symbols.T_SEMICOLON);  /* implicit ';' at php-end tag */
}

<ST_IN_SCRIPTING>"%>"{NEWLINE}? {
    if (asp_tags) {
        yybegin(YYINITIAL);
        return createSymbol(ASTPHP5Symbols.T_SEMICOLON);  /* implicit ';' at php-end tag */
    } else {
        return createSymbol(ASTPHP5Symbols.T_INLINE_HTML);
    }
}

<ST_IN_SCRIPTING>(b?[\"]{DOUBLE_QUOTES_CHARS}*("{"*|"$"*)[\"]) {
    return createFullSymbol(ASTPHP5Symbols.T_CONSTANT_ENCAPSED_STRING);
}

<ST_IN_SCRIPTING>(b?[']([^'\\]|("\\"{ANY_CHAR}))*[']) {
    return createFullSymbol(ASTPHP5Symbols.T_CONSTANT_ENCAPSED_STRING);
}

<ST_IN_SCRIPTING>b?[\"] {
    yybegin(ST_DOUBLE_QUOTES);
    return createSymbol(ASTPHP5Symbols.T_QUATE);
}

<ST_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}[']{LABEL}[']{NEWLINE} {
    int bprefix = (yytext().charAt(0) != '<') ? 1 : 0;
    int startString=3+bprefix;
    /* 3 is <<<, 2 is quotes, 1 is newline */
    int nowdoc_len = yylength() - bprefix - 3 - 2 - 1 - (yytext().charAt(yylength() - 2) == '\r' ? 1 : 0);
    while ((yytext().charAt(startString) == ' ') || (yytext().charAt(startString) == '\t')) {
        startString++;
        nowdoc_len--;
    }
    // first quate
    startString++;
    nowdoc = yytext().substring(startString, nowdoc_len + startString);
    yybegin(ST_START_NOWDOC);
    return createSymbol(ASTPHP5Symbols.T_START_NOWDOC);
}

<ST_START_NOWDOC>{ANY_CHAR} {
    yypushback(1);
    yybegin(ST_NOWDOC);
}

<ST_START_NOWDOC>{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\r\n]? {
    /* <ST_START_NOWDOC>{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\r\n]? */
    // there is no [\r\n] if it is the last line
    // i.e. not [\r\n] but EOF, so check not [\r\n] but [\r\n]?
    if (isEndHereOrNowdoc(nowdoc)) {
        int indexOfNowdocId = yytext().indexOf(nowdoc);
        int back = yylength() - indexOfNowdocId - nowdoc.length();
        yypushback(back);

        nowdoc = null;
        resetNowdocBodyInfo();
        yybegin(ST_IN_SCRIPTING);
        return createSymbol(ASTPHP5Symbols.T_END_NOWDOC);
    } else {
        yypushback(1); // [\r\n] length
        yybegin(ST_NOWDOC);
        updateNowdocBodyInfo();
    }
}


<ST_NOWDOC> {
    {NEWLINE}+{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\r\n]? {
        /* <ST_NOWDOC>{NEWLINE}+{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\r\n]? */
        if (isEndHereOrNowdoc(nowdoc)) {
            String yytext = yytext();
            int newlineLength = 0;
            for (int i = 0; i < yylength(); i++) {
                char c = yytext.charAt(i);
                if (c != '\n' && c != '\r') {
                    break;
                }
                newlineLength++;
            }
            int back = yylength() - newlineLength;
            yypushback(back);
            updateNowdocBodyInfo();
            yybegin(ST_END_NOWDOC);
            if (nowdocBodyLength > 0) {
                return createFullNowdocBodySymbol();
            }
        } else {
            yypushback(1);
            updateNowdocBodyInfo();
        }
    }

    {NOWDOC_CHARS}|{NEWLINE} {
        /* <ST_NOWDOC>{NOWDOC_CHARS}|{NEWLINE} */
        updateNowdocBodyInfo();
    }
}

<ST_END_NOWDOC>{TABS_AND_SPACES}{LABEL}";"? {
    /* <ST_END_NOWDOC>{LABEL}";"?[\n\r] */
    nowdoc = null;
    resetNowdocBodyInfo();
    yybegin(ST_IN_SCRIPTING);
    int back = 0;
    if (yytext().charAt(yylength() - 1)==';') {
        back++;
    }
    yypushback(back);
    return createSymbol(ASTPHP5Symbols.T_END_NOWDOC);
}

<ST_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}({LABEL}|"\""{LABEL}"\""){NEWLINE} {
    int removeChars = (yytext().charAt(0) == 'b')?4:3;
    if (heredoc != null) {
        heredocStack.push(new HeredocInfo(heredoc, heredocBody.toString(), heredocBodyStart, heredocBodyLength));
    }
    heredoc = yytext().substring(removeChars).trim();    // for 'b<<<' or '<<<'
    if (heredoc.charAt(0) == '"') {
        heredoc = heredoc.substring(1, heredoc.length()-1);
    }
    if (!heredocStack.isEmpty()) {
        pushState(ST_START_HEREDOC);
    } else {
        yybegin(ST_START_HEREDOC);
    }
    return createSymbol(ASTPHP5Symbols.T_START_HEREDOC);
}

<ST_IN_SCRIPTING>[`] {
    yybegin(ST_BACKQUOTE);
    return createSymbol(ASTPHP5Symbols.T_BACKQUATE);
}

<ST_START_HEREDOC>{ANY_CHAR} {
    yypushback(1);
    yybegin(ST_HEREDOC);
}

<ST_START_HEREDOC>{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\n\r]? {
    /* <ST_START_HEREDOC>{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\n\r]? */
    int trailingNewLineLength = 1;
    int labelLength = yylength() - trailingNewLineLength;
    int back = trailingNewLineLength;
    String yytext = yytext();

    if (yytext.charAt(labelLength - 1) == ';') {
        labelLength--;
        back++;
    }

    if (isEndHereOrNowdoc(heredoc)) {
        int indexOfHeredocId = yytext().indexOf(heredoc);
        back += labelLength - indexOfHeredocId;
        yypushback(back);
        yybegin(ST_END_HEREDOC);
    } else {
        yypushback(yylength());
        yybegin(ST_HEREDOC);
    }
}

<ST_HEREDOC> {
    {NEWLINE}{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\n\r]? {
        /* {NEWLINE}{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\n\r]? */
        int trailingNewLineLength = 1;
        int labelLength = yylength() - trailingNewLineLength;
        int back = trailingNewLineLength;

        if (yytext().charAt(labelLength-1) == ';') {
           labelLength--;
           back++;
        }

        if (isEndHereOrNowdoc(heredoc)) {
            int indexOfHeredocId = yytext().indexOf(heredoc);
            back += labelLength - indexOfHeredocId;
            yypushback(back);
            yybegin(ST_END_HEREDOC);
        } else {
            // handle variable
            char previousChar = ' ';
            int indexOfVariable = -1;
            for (int i = 0; i < yylength(); i++) {
                char currentChar = yytext().charAt(i);
                if (currentChar == '$' && previousChar == '{') {
                    indexOfVariable = i - 1;
                    break;
                }
                if (currentChar == '$' && previousChar != '\\') {
                    indexOfVariable = i;
                    break;
                }
                previousChar = currentChar;
            }

            if (indexOfVariable == -1) {
                yypushback(trailingNewLineLength);
            } else {
                yypushback(yylength() - indexOfVariable);
                updateHeredocBodyInfo();
                return createFullHeredocBodySymbol();
            }
        }
        updateHeredocBodyInfo();
        if (yystate() == ST_END_HEREDOC) {
            return createFullHeredocBodySymbol();
        }
    }

    {HEREDOC_CHARS} {
        /* <ST_HEREDOC> {HEREDOC_CHARS} */
        int indexOfNewline = yytext().indexOf("\r");
        if (indexOfNewline == -1) {
            indexOfNewline = yytext().indexOf("\n");
        }
        if (indexOfNewline > 0) {
            // if index equals 0, don't pushback (infinite loop)
            yypushback(yylength() - indexOfNewline);
        }
        updateHeredocBodyInfo();
    }

    {HEREDOC_CHARS}("{$" | "${") {
        /* <ST_HEREDOC> {HEREDOC_CHARS}("{$" | "${") */
        yypushback(2);
        updateHeredocBodyInfo();
        return createFullHeredocBodySymbol();
    }

    {HEREDOC_CHARS}"$"{LABEL}"["? {
        /* <ST_HEREDOC> {HEREDOC_CHARS}"$"{LABEL}"["? */
        String text = yytext();
        int lastIndexOfDollar = text.lastIndexOf('$');
        yypushback(text.length() - lastIndexOfDollar);
        updateHeredocBodyInfo();
        return createFullHeredocBodySymbol();
    }

    "$" | "{" {
        /* <ST_HEREDOC> "$" | "{" */
        updateHeredocBodyInfo();
    }
}

<ST_END_HEREDOC>{TABS_AND_SPACES}{LABEL}";"? {
    /* <ST_END_HEREDOC>{TABS_AND_SPACES}{LABEL}";"? */
    heredoc = null;
    resetHeredocBodyInfo();
    HeredocInfo info = heredocStack.pollFirst();
    setHeredocInfo(info);
    if (heredoc != null) {
        popState();
    } else {
        yybegin(ST_IN_SCRIPTING);
    }
    int back = 0;
    // mark just the label
    if (yytext().charAt(yylength() - 1)==';') {
        back++;
    }
    yypushback(back);
    return createSymbol(ASTPHP5Symbols.T_END_HEREDOC);
}

<ST_DOUBLE_QUOTES,ST_BACKQUOTE,ST_HEREDOC>"{$" {
    pushState(ST_IN_SCRIPTING);
    yypushback(yylength()-1);
    bracket++;
    return createSymbol(ASTPHP5Symbols.T_CURLY_OPEN_WITH_DOLAR);
}

<ST_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}+ {
    /*<ST_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}+*/
    return createFullSymbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE);
}

/*
The original parsing rule was {DOUBLE_QUOTES_CHARS}*("{"{2,}|"$"{2,}|(("{"+|"$"+)[\"]))
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[\"])) {
    yypushback(1);
    /*<ST_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[\"]))*/
    return createFullSymbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE);
}

<ST_BACKQUOTE>{BACKQUOTE_CHARS}+ {
    /*<ST_BACKQUOTE>{BACKQUOTE_CHARS}+*/
    return createFullSymbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE);
}

/*
The original parsing rule was {BACKQUOTE_CHARS}*("{"{2,}|"$"{2,}|(("{"+|"$"+)[`]))
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_BACKQUOTE>{BACKQUOTE_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[`])) {
    yypushback(1);
    /*<ST_BACKQUOTE>{BACKQUOTE_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[`]))*/
    return createFullSymbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE);
}

<ST_DOUBLE_QUOTES>[\"] {
    yybegin(ST_IN_SCRIPTING);
    return createSymbol(ASTPHP5Symbols.T_QUATE);
}

<ST_BACKQUOTE>[`] {
    yybegin(ST_IN_SCRIPTING);
    return createSymbol(ASTPHP5Symbols.T_BACKQUATE);
}

<ST_IN_SCRIPTING,YYINITIAL,ST_DOUBLE_QUOTES,ST_BACKQUOTE,ST_HEREDOC,ST_START_HEREDOC,ST_END_HEREDOC, ST_NOWDOC,ST_START_NOWDOC,ST_END_NOWDOC,ST_VAR_OFFSET, ST_DOCBLOCK>{ANY_CHAR} {
    // do nothing
}
