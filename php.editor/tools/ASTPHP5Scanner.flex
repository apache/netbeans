/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.List;
import java_cup.runtime.*;
import org.netbeans.modules.php.editor.*;
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
    private String heredoc = null;
    private String nowdoc = null;
    private int nowdoc_len  = 0;
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

%}

LNUM=[0-9]+
DNUM=([0-9]*[\.][0-9]+)|([0-9]+[\.][0-9]*)
EXPONENT_DNUM=(({LNUM}|{DNUM})[eE][+-]?{LNUM})
HNUM="0x"[0-9a-fA-F]+
BNUM="0b"[01]+
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
HEREDOC_NEWLINE=((({LABEL}";"?((("{"+|"$"+)"\\"?)|"\\"))|(("{"*|"$"*)"\\"?)){NEWLINE})
HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR=(("{"+[^$\n\r\\{])|("{"*"\\"[^\n\r])|{HEREDOC_LITERAL_DOLLAR})
HEREDOC_NON_LABEL=([^a-zA-Z_\x7f-\xff$\n\r\\{]|{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})
HEREDOC_LABEL_NO_NEWLINE=({LABEL}([^a-zA-Z0-9_\x7f-\xff;$\n\r\\{]|(";"[^$\n\r\\{])|(";"?{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})))
DOUBLE_QUOTES_CHARS=("{"*([^$\"\\{]|("\\"{ANY_CHAR}))|{DOUBLE_QUOTES_LITERAL_DOLLAR})
BACKQUOTE_CHARS=("{"*([^$`\\{]|("\\"{ANY_CHAR}))|{BACKQUOTE_LITERAL_DOLLAR})
HEREDOC_CHARS=("{"*([^$\n\r\\{]|("\\"[^\n\r]))|{HEREDOC_LITERAL_DOLLAR}|({HEREDOC_NEWLINE}+({HEREDOC_NON_LABEL}|{HEREDOC_LABEL_NO_NEWLINE})))
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

<ST_IN_SCRIPTING>"function" {
	return createFullSymbol(ASTPHP5Symbols.T_FUNCTION);
}

<ST_IN_SCRIPTING>"const" {
	return createFullSymbol(ASTPHP5Symbols.T_CONST);
}

<ST_IN_SCRIPTING>"return" {
	return createFullSymbol(ASTPHP5Symbols.T_RETURN);
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

<ST_IN_SCRIPTING,ST_LOOKING_FOR_PROPERTY>{WHITESPACE}+ {
    whitespaceEndPosition = getTokenStartPosition() + yylength();
}

<ST_LOOKING_FOR_PROPERTY>"->" {
	return createSymbol(ASTPHP5Symbols.T_OBJECT_OPERATOR);
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
//	yymore();
}

<ST_ONE_LINE_COMMENT>"?"|"%"|">" {
	//	yymore();
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
//	yymore();
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

<ST_IN_SCRIPTING>"/*"{WHITESPACE}*"@var"{WHITESPACE}("$"?){LABEL}("["({LABEL} | "\"" | "'")*"]")*{WHITESPACE}{QUALIFIED_LABEL}("[""]")*([|]{QUALIFIED_LABEL}("[""]")*)*{WHITESPACE}?"*/" {
    comment = yytext();
    handleVarComment();
    // if we want to handle the var comment in  ast, then return the T_VAR_Comment symbol
    // but it needs some changes in parser grammar. see issue #154967
    //return createFullSymbol(ASTPHP5Symbols.T_VAR_COMMENT);
}

<ST_IN_SCRIPTING>"/**"{WHITESPACE}*"@var"{WHITESPACE}{QUALIFIED_LABEL}("[""]")*([|]{QUALIFIED_LABEL}("[""]")*)*{WHITESPACE}("$"){LABEL}("["({LABEL} | "\"" | "'")*"]")*{WHITESPACE}?[^\n\r]*"*/" {
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
//	yymore();
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
        nowdoc_len = yylength()-bprefix-3-2-1-(yytext().charAt(yylength()-2)=='\r'?1:0);
        while ((yytext().charAt(startString) == ' ') || (yytext().charAt(startString) == '\t')) {
            startString++;
            nowdoc_len--;
        }
        // first quate
        startString++;
        nowdoc = yytext().substring(startString,nowdoc_len+startString);
        yybegin(ST_START_NOWDOC);
        return createSymbol(ASTPHP5Symbols.T_START_NOWDOC);
}

<ST_START_NOWDOC>{ANY_CHAR} {
	yypushback(1);
	yybegin(ST_NOWDOC);
}

<ST_START_NOWDOC>{LABEL}";"?[\r\n] {
    String text = yytext();
    int length = text.length() - 1;
    text = text.trim();

    yypushback(1);

    if (text.endsWith(";")) {
        text = text.substring(0, text.length() - 1);
        yypushback(1);
    }
    if (text.equals(nowdoc)) {
        nowdoc = null;
        yybegin(ST_IN_SCRIPTING);
        return createSymbol(ASTPHP5Symbols.T_END_NOWDOC);
    } else {
        yybegin(ST_NOWDOC);
    }
}


<ST_NOWDOC>{NOWDOC_CHARS}*{NEWLINE}+{LABEL}";"?[\n\r] {
    String text = yytext();

    if (text.charAt(text.length() - 2)== ';') {
        text = text.substring(0, text.length() - 2);
        yypushback(1);
    } else {
        text = text.substring(0, text.length() - 1);
    }

    int textLength = text.length();
    int nowdocLength = nowdoc.length();
    if (textLength > nowdocLength && text.substring(textLength - nowdocLength, textLength).equals(nowdoc)) {
        yypushback(2);
        yybegin(ST_END_NOWDOC);
        Symbol sym = createFullSymbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE);
        String value = (String)sym.value;
        sym.value = value.substring(0, value.length() - nowdocLength + 1);
        return sym;
    }
    yypushback(1);
}

<ST_END_NOWDOC>{ANY_CHAR} {
    nowdoc=null;
    nowdoc_len=0;
    yybegin(ST_IN_SCRIPTING);
    return createSymbol(ASTPHP5Symbols.T_END_NOWDOC);
}

<ST_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}({LABEL}|"\""{LABEL}"\""){NEWLINE} {
    int removeChars = (yytext().charAt(0) == 'b')?4:3;
    heredoc = yytext().substring(removeChars).trim();    // for 'b<<<' or '<<<'
    if (heredoc.charAt(0) == '"') {
        heredoc = heredoc.substring(1, heredoc.length()-1);
    }
    yybegin(ST_START_HEREDOC);
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

<ST_START_HEREDOC>{LABEL}";"?[\n\r] {
    String text = yytext();
    int length = text.length() - 1;
    text = text.trim();

    yypushback(1);

    if (text.endsWith(";")) {
        text = text.substring(0, text.length() - 1);
        yypushback(1);
    }
    if (text.equals(heredoc)) {
        heredoc = null;
        yybegin(ST_IN_SCRIPTING);
        return createSymbol(ASTPHP5Symbols.T_END_HEREDOC);
    } else {
    	   yybegin(ST_HEREDOC);
    }
}

<ST_HEREDOC>{HEREDOC_CHARS}*{HEREDOC_NEWLINE}+{LABEL}";"?[\n\r] {
    	String text = yytext();

    if (text.charAt(text.length() - 2)== ';') {
		text = text.substring(0, text.length() - 2);
        	yypushback(1);
    } else {
		text = text.substring(0, text.length() - 1);
    }

	int textLength = text.length();
	int heredocLength = heredoc.length();
	if (textLength > heredocLength && text.substring(textLength - heredocLength, textLength).equals(heredoc)) {
		yypushback(2);
        	yybegin(ST_END_HEREDOC);
        	// we need to remove the closing label from the symbol value.
                /*<ST_HEREDOC>{HEREDOC_CHARS}*{HEREDOC_NEWLINE}+{LABEL}";"?[\n\r]*/
        	Symbol sym = createFullSymbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE);
        	String value = (String)sym.value;
        	sym.value = value.substring(0, value.length() - heredocLength + 1);
	   	return sym;
	}
	yypushback(1);

}

<ST_END_HEREDOC>{ANY_CHAR} {
     heredoc = null;
	yybegin(ST_IN_SCRIPTING);
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

<ST_HEREDOC>{HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)? {
        /*<ST_HEREDOC>{HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)?*/
	return createFullSymbol(ASTPHP5Symbols.T_ENCAPSED_AND_WHITESPACE);
}

/*
The original parsing rule was {HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)?("{"{2,}|"$"{2,})
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_HEREDOC>{HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)?("{""{"+|"$""$"+) {
    yypushback(1);
    /*<ST_HEREDOC>{HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)?("{""{"+|"$""$"+)*/
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
