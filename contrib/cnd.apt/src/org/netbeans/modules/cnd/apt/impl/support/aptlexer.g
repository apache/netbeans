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
// Start of APTLexer.cpp block
header {

package org.netbeans.modules.cnd.apt.impl.support.generated;

import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.structure.APTFile;
}

options {
	language = "Java"; // NOI18N
} 

{
@org.netbeans.api.annotations.common.SuppressWarnings("DLS")
@SuppressWarnings({"unchecked", "cast", "fallthrough"})
final /*final class attribute gives us performance */
}
class APTLexer extends Lexer;

options {
    k = 2;
    exportVocab = APTGenerated;
    testLiterals = false;
    charVocabulary = '\u0001'..'\ufffe';  // NOI18N
}

// DW 4/11/02 put in to support manual hoisting
tokens {
    // tokens with constant text, we put them in small indices to reduce size
        ELLIPSIS;
        DOT;
	ASSIGNEQUAL;
	COLON;
	COMMA;
	QUESTIONMARK;
	SEMICOLON;
	POINTERTO;
	LPAREN;
	RPAREN;
	LSQUARE;
	RSQUARE;
	LCURLY;
	RCURLY;
	EQUAL;
	NOTEQUAL;
	LESSTHANOREQUALTO;
	LESSTHAN;
	GREATERTHANOREQUALTO;
	GREATERTHAN;
	DIVIDE;
	DIVIDEEQUAL;
	PLUS;
	PLUSEQUAL;
	PLUSPLUS;
	MINUS;
	MINUSEQUAL;
	MINUSMINUS;
	STAR;
	TIMESEQUAL;
	MOD;
	MODEQUAL;
	SHIFTRIGHT;
	SHIFTRIGHTEQUAL;
	SHIFTLEFT;
	SHIFTLEFTEQUAL;
	AND;
	NOT;
	OR;
	AMPERSAND;
	BITWISEANDEQUAL;
	TILDE;
	BITWISEOR;
	BITWISEOREQUAL;
	BITWISEXOR;
	BITWISEXOREQUAL;
	POINTERTOMBR;
	DOTMBR;
	SCOPE;
        AT;
        DOLLAR;
        BACK_SLASH;

	DEFINED;
	DBL_SHARP;
	SHARP;
        FUN_LIKE_MACRO_LPAREN;
        GRAVE_ACCENT;

        END_PREPROC_DIRECTIVE;

        // marker for last const text token
        LAST_CONST_TEXT_TOKEN;

        // other tokens
        FLOATONE;
        FLOATTWO;
        HEXADECIMALINT;
        OCTALINT;
        DECIMALINT;

	Whitespace;
	EndOfLine;
	Skip;
	PreProcComment;
	PPLiterals;
	Space;
	PreProcBlockComment;
	PreProcLineComment;
	Comment;
	CPPComment;
	CHAR_LITERAL;
	STRING_LITERAL;
	InterStringWhitespace;
	StringPart;
	Escape;
	Digit;
	Decimal;
	LongSuffix;
	UnsignedSuffix;
	FloatSuffix;
	Exponent;
	Vocabulary;
	NUMBER;
	IDENT;
        BINARYINT;

    // preprocessor specific tokens
    INCLUDE_STRING;
    SYS_INCLUDE_STRING;
//    END_PREPROC_DIRECTIVE; // was moved into const tokens part

    // preprocessor directives
    INCLUDE;
    INCLUDE_NEXT;
    DEFINE;
    UNDEF;
    IFDEF;
    IFNDEF;
    IF;
    ELIF;
    ELSE;
    ENDIF;
    PRAGMA;
    LINE;
    ERROR;
    PREPROC_DIRECTIVE; // unrecongnized #-directive

    FIRST_LITERAL_TOKEN;
    LITERAL_OPERATOR = "operator"; // NOI18N
    LITERAL_alignof="alignof"; // NOI18N
    LITERAL__Alignof="_Alignof"; // NOI18N
    LITERAL___alignof__="__alignof__"; // NOI18N
    LITERAL_typeof="typeof"; // NOI18N
    LITERAL___typeof__="__typeof__"; // NOI18N
    LITERAL___typeof="__typeof"; // NOI18N
    LITERAL_template="template"; // NOI18N
    LITERAL_typedef="typedef"; // NOI18N
    LITERAL_enum="enum"; // NOI18N
    LITERAL_namespace="namespace"; // NOI18N
    LITERAL_extern="extern"; // NOI18N
    LITERAL_inline="inline"; // NOI18N
    LITERAL__inline="_inline"; // NOI18N
    LITERAL___inline__="__inline__"; // NOI18N
    LITERAL___inline="__inline"; // NOI18N
    LITERAL_virtual="virtual"; // NOI18N
    LITERAL_explicit="explicit"; // NOI18N
    LITERAL_friend="friend"; // NOI18N
    LITERAL__stdcall="_stdcall"; // NOI18N
    LITERAL___stdcall="__stdcall"; // NOI18N
    LITERAL_typename="typename"; // NOI18N
    LITERAL_auto="auto"; // NOI18N
    LITERAL_register="register"; // NOI18N
    LITERAL_static="static"; // NOI18N
    LITERAL_mutable="mutable"; // NOI18N
    LITERAL_const="const"; // NOI18N
    LITERAL___const__="__const__"; // NOI18N
    LITERAL___const="__const"; // NOI18N
    LITERAL_const_cast="const_cast"; // NOI18N
    LITERAL_volatile="volatile"; // NOI18N
    LITERAL___volatile__="__volatile__"; // NOI18N
    LITERAL___volatile="__volatile"; // NOI18N
    LITERAL_char="char"; // NOI18N
    LITERAL_wchar_t="wchar_t"; // NOI18N
    LITERAL_bool="bool"; // NOI18N
    LITERAL_short="short"; // NOI18N
    LITERAL_int="int"; // NOI18N
    LITERAL_long="long"; // NOI18N
    LITERAL_signed="signed"; // NOI18N
    LITERAL___signed__="__signed__"; // NOI18N
    LITERAL___signed="__signed"; // NOI18N
    LITERAL_unsigned="unsigned"; // NOI18N
    LITERAL___unsigned__="__unsigned__"; // NOI18N
    LITERAL_float="float"; // NOI18N
    LITERAL_double="double"; // NOI18N
    LITERAL_void="void"; // NOI18N
    LITERAL__declspec="_declspec"; // NOI18N
    LITERAL___declspec="__declspec"; // NOI18N
    LITERAL_class="class"; // NOI18N
    LITERAL_struct="struct"; // NOI18N
    LITERAL_union="union"; // NOI18N
    LITERAL_this="this"; // NOI18N
    LITERAL_true="true"; // NOI18N
    LITERAL_false="false"; // NOI18N
    LITERAL_public="public"; // NOI18N
    LITERAL_protected="protected"; // NOI18N
    LITERAL_private="private"; // NOI18N
    LITERAL_throw="throw"; // NOI18N
    LITERAL_case="case"; // NOI18N
    LITERAL_default="default"; // NOI18N
    LITERAL_if="if"; // NOI18N
    LITERAL_else="else"; // NOI18N
    LITERAL_switch="switch"; // NOI18N
    LITERAL_while="while"; // NOI18N
    LITERAL_do="do"; // NOI18N
    LITERAL_for="for"; // NOI18N
    LITERAL_goto="goto"; // NOI18N
    LITERAL_continue="continue"; // NOI18N
    LITERAL_break="break"; // NOI18N
    LITERAL_return="return"; // NOI18N
    LITERAL_try="try"; // NOI18N
    LITERAL_catch="catch"; // NOI18N
    LITERAL_using="using"; // NOI18N
    LITERAL_export="export"; // NOI18N
    LITERAL_asm="asm"; // NOI18N
    LITERAL__asm="_asm"; // NOI18N
    LITERAL___asm__="__asm__"; // NOI18N
    LITERAL___asm="__asm"; // NOI18N
    LITERAL__endasm="_endasm"; // NOI18N
    LITERAL_sizeof="sizeof"; // NOI18N
    LITERAL_dynamic_cast="dynamic_cast"; // NOI18N
    LITERAL_static_cast="static_cast"; // NOI18N
    LITERAL_reinterpret_cast="reinterpret_cast"; // NOI18N
    LITERAL_new="new"; // NOI18N
    LITERAL__cdecl="_cdecl"; // NOI18N
    LITERAL___cdecl="__cdecl"; // NOI18N
    LITERAL__near="_near"; // NOI18N
    LITERAL___near="__near"; // NOI18N
    LITERAL__far="_far"; // NOI18N
    LITERAL___far="__far"; // NOI18N
    LITERAL___interrupt="__interrupt"; // NOI18N
    LITERAL_pascal="pascal"; // NOI18N
    LITERAL__pascal="_pascal"; // NOI18N
    LITERAL___pascal="__pascal"; // NOI18N
    LITERAL_delete="delete"; // NOI18N
    LITERAL__int64="_int64"; // NOI18N
    LITERAL___int64="__int64"; // NOI18N
    LITERAL___w64="__w64"; // NOI18N
    LITERAL___extension__="__extension__"; // NOI18N
    LITERAL___attribute__="__attribute__"; // NOI18N
    LITERAL_restrict="restrict"; // NOI18N
    LITERAL___restrict="__restrict"; // NOI18N
    LITERAL___complex__="__complex__"; // NOI18N
    LITERAL___imag="__imag__"; // NOI18N
    LITERAL___real="__real__"; // NOI18N
    LITERAL___global="__global"; // NOI18N
    LITERAL__Bool="_Bool"; // NOI18N
    LITERAL__Complex="_Complex"; // NOI18N
    LITERAL___thread="__thread"; // NOI18N
    LITERAL___attribute="__attribute"; // NOI18N
    LITERAL__Imaginary="_Imaginary"; // NOI18N
    LITERAL_bit="bit"; // NOI18N
    LITERAL___symbolic="__symbolic"; // NOI18N
    LITERAL___hidden="__hidden"; // NOI18N
    LITERAL_final="final"; // NOI18N
    LITERAL_override="override"; // NOI18N
    LITERAL_constexpr="constexpr"; // NOI18N
    LITERAL_decltype="decltype"; // NOI18N
    LITERAL_nullptr="nullptr"; // NOI18N
    LITERAL_thread_local="thread_local"; // NOI18N
    LITERAL__Thread_local="_Thread_local"; // NOI18N
    LITERAL_static_assert="static_assert"; // NOI18N
    LITERAL__Static_assert="_Static_assert"; // NOI18N
    LITERAL_alignas="alignas"; // NOI18N
    LITERAL__Alignas="_Alignas"; // NOI18N
    LITERAL_char16_t="char16_t"; // NOI18N
    LITERAL_char32_t="char32_t"; // NOI18N
    LITERAL_noexcept="noexcept"; // NOI18N
    LITERAL___decltype="__decltype"; // NOI18N
    LITERAL___complex="__complex"; // NOI18N
    LITERAL___forceinline="__forceinline"; // NOI18N
    LITERAL___clrcall="__clrcall"; // NOI18N
    LITERAL___try="__try"; // NOI18N
    LITERAL___finally="__finally"; // NOI18N
    LITERAL___null="__null"; // NOI18N
    LITERAL___alignof="__alignof"; // NOI18N
    LITERAL___is_class="__is_class"; // NOI18N
    LITERAL___is_enum="__is_enum"; // NOI18N
    LITERAL___is_pod="__is_pod"; // NOI18N
    LITERAL___is_base_of="__is_base_of"; // NOI18N
    LITERAL___has_trivial_constructor="__has_trivial_constructor"; // NOI18N
    LITERAL___restrict__="__restrict__"; // NOI18N
    LITERAL__Noreturn="_Noreturn"; // NOI18N
    LITERAL__Atomic="_Atomic"; // NOI18N
    LITERAL___has_nothrow_assign="__has_nothrow_assign"; // NOI18N
    LITERAL___has_nothrow_copy="__has_nothrow_copy"; // NOI18N
    LITERAL___has_nothrow_constructor="__has_nothrow_constructor"; // NOI18N
    LITERAL___has_trivial_assign="__has_trivial_assign"; // NOI18N
    LITERAL___has_trivial_copy="__has_trivial_copy"; // NOI18N
    LITERAL___has_trivial_destructor="__has_trivial_destructor"; // NOI18N
    LITERAL___has_virtual_destructor="__has_virtual_destructor"; // NOI18N
    LITERAL___is_abstract="__is_abstract"; // NOI18N
    LITERAL___is_empty="__is_empty"; // NOI18N
    LITERAL___is_literal_type="__is_literal_type"; // NOI18N
    LITERAL___is_polymorphic="__is_polymorphic"; // NOI18N
    LITERAL___is_standard_layout="__is_standard_layout"; // NOI18N
    LITERAL___is_trivial="__is_trivial"; // NOI18N
    LITERAL___is_union="__is_union"; // NOI18N
    LITERAL___underlying_type="__underlying_type"; // NOI18N
    LITERAL___builtin_va_list="__builtin_va_list"; // NOI18N
    
    LITERAL_concept="concept"; // NOI18N
    LITERAL_requires="requires"; // NOI18N
    LITERAL_co_await="co_await"; // NOI18N
    LITERAL_co_return="co_return"; // NOI18N
    LITERAL_co_yield="co_yield"; // NOI18N
    LAST_LITERAL_TOKEN;

    // Extension points
    LITERAL__BUILT_IN_TYPE__; // extra built-in type name
    LITERAL__TYPE_QUALIFIER__; // extra type qualifier 
    LITERAL__STORAGE_CLASS_SPECIFIER__; // extra storage qualifier

    // Fortran tokens

    T_CLOSE;
    T_BLOCK;
    T_GE;
    T_CONTAINS;
    T_ABSTRACT;
    T_CLASS;
    T_NOPASS;
    T_UNFORMATTED;
    T_LESSTHAN;
    T_ENDSUBROUTINE;
    T_GT;
    T_IDENT;
    T_INTERFACE;
    T_RETURN;
    T_XYZ;
    T_EOF;
    T_CALL;
    T_EOS;
    T_GO;
    T_AND;
    T_PERCENT;
    T_PRINT;
    T_ALLOCATE_STMT_1;
    T_SUBROUTINE;
    T_CONTROL_EDIT_DESC;
    T_ENUMERATOR;
    Alphanumeric_Character;
    T_DEFINED_OP;
    T_KIND;
    T_STOP;
    T_GREATERTHAN_EQ;
    T_CHAR_STRING_EDIT_DESC;
    T_ALLOCATABLE;
    T_ENDINTERFACE;
    T_END;
    T_ASTERISK;
    T_PRIVATE;
    T_DOUBLEPRECISION;
    T_CASE;
    T_IMPLICIT;
    T_IF;
    T_THEN;
    T_DIMENSION;
    T_GOTO;
    T_ENDMODULE;
    T_IN;
    T_WRITE;
    T_FORMATTED;
    WS;
    T_DATA;
    T_FALSE;
    T_WHERE;
    T_ENDIF;
    T_SLASH;
    SQ_Rep_Char;
    T_GENERIC;
    T_RECURSIVE;
    DQ_Rep_Char;
    T_ELSEIF;
    T_BLOCKDATA;
    OCTAL_CONSTANT;
    T_SELECTTYPE;
    T_MINUS;
    T_SELECT;
    T_FINAL;
    T_UNDERSCORE;
    T_IMPORT;
    T_USE;
    T_FILE;
    T_RPAREN;
    T_INTENT;
    T_ENDBLOCK;
    T_ASSIGNMENT_STMT;
    T_PAUSE;
    T_BACKSPACE;
    T_ENDFILE;
    T_EQUALS;
    T_NON_INTRINSIC;
    T_SELECTCASE;
    T_DIGIT_STRING;
    T_COLON_COLON;
    T_NON_OVERRIDABLE;
    Special_Character;
    T_INCLUDE;
    T_OPEN;
    T_POWER;
    T_ASSOCIATE;
    T_CHAR_CONSTANT;
    T_OPERATOR;
    T_TO;
    T_ENDASSOCIATE;
    T_EQ;
    T_GREATERTHAN;
    T_DATA_EDIT_DESC;
    T_INQUIRE_STMT_2;
    T_EQV;
    HEX_CONSTANT;
    Digit_String;
    T_ELEMENTAL;
    T_CHARACTER;
    PREPROCESS_LINE;
    T_NULLIFY;
    T_REWIND;
    T_ARITHMETIC_IF_STMT;
    T_FORALL_CONSTRUCT_STMT;
    T_BIND;
    T_ENDFORALL;
    T_DO;
    T_WHERE_STMT;
    T_POINTER;
    T_PROGRAM;
    T_ENDTYPE;
    T_WAIT;
    T_ELSE;
    T_IF_STMT;
    T_RBRACKET;
    T_LPAREN;
    T_EXTENDS;
    T_OPTIONAL;
    T_DOUBLE;
    T_MODULE;
    T_READ;
    T_ALLOCATE;
    T_INTEGER;
    T_OR;
    T_EQUIVALENCE;
    T_PERIOD;
    T_ENTRY;
    T_LABEL_DO_TERMINAL;
    T_REAL;
    T_CYCLE;
    T_PROCEDURE;
    T_EQ_EQ;
    T_SLASH_EQ;
    T_ENDSELECT;
    T_PURE;
    T_TRUE;
    T_NE;
    T_INTRINSIC;
    T_PASS;
    T_REAL_CONSTANT;
    LINE_COMMENT;
    T_PERIOD_EXPONENT;
    T_ENDWHERE;
    MISC_CHAR;
    T_FORMAT;
    T_DEFAULT;
    T_SLASH_SLASH;
    T_NONE;
    T_NAMELIST;
    T_SEQUENCE;
    T_PRECISION;
    T_ASYNCHRONOUS;
    T_COMMA;
    T_RESULT;
    T_ENDBLOCKDATA;
    T_LOGICAL;
    T_VALUE;
    Letter;
    T_FORALL;
    T_SAVE;
    T_HOLLERITH;
    T_FLUSH;
    T_WHILE;
    T_INQUIRE;
    T_DEFERRED;
    T_FORALL_STMT;
    T_ASSIGN;
    T_LBRACKET;
    T_EXTERNAL;
    T_VOLATILE;
    T_OUT;
    CONTINUE_CHAR;
    T_COLON;
    T_COMPLEX;
    T_PLUS;
    T_STMT_FUNCTION;
    T_ONLY;
    T_PROTECTED;
    T_COMMON;
    T_INOUT;
    T_NEQV;
    T_PUBLIC;
    T_ENDDO;
    T_ENDPROGRAM;
    T_ENDFUNCTION;
    T_WHERE_CONSTRUCT_STMT;
    T_ELSEWHERE;
    T_ENUM;
    //Digit;
    T_PARAMETER;
    T_TARGET;
    T_DOUBLECOMPLEX;
    T_PTR_ASSIGNMENT_STMT;
    T_TYPE;
    T_LESSTHAN_EQ;
    T_DEALLOCATE;
    T_LT;
    T_FUNCTION;
    T_EQ_GT;
    T_ENDENUM;
    BINARY_CONSTANT;
    T_LE;
    T_LEN;
    T_CONTINUE;
    T_NOT;
    Rep_Char;
    T_ASSIGNMENT;
    T_EXIT;
}
{
    private boolean reportErrors;
    private APTFile.Kind aptKind;
    private APTLexerCallback callback;

    public interface APTLexerCallback {
        void onMakeToken(int tokType, int startColumn, int startLine);
    }

    public void setCallback(APTLexerCallback callback) {
        this.callback = callback;
    }

    public void init(String filename, int flags, APTFile.Kind aptKind) {
        preprocPossible = true;
        preprocPending = false;
        reportErrors = true;

        setFilename(filename);
        
        this.aptKind = aptKind;

//        if ((flags & CPPParser.CPP_SUPPRESS_ERRORS) > 0) {
//            reportErrors = false;
//        }
    }

    // overriden to avoid class loading
    @Override
    public void setTokenObjectClass(String cl) {
    }

    // Used instead of setTokenObjectClass method to avoid reflection usage
    @Override
    protected APTToken createToken(int type) {
        return APTUtils.createAPTToken(type);
    }

    @Override
    protected void setTokenText(Token _token, char buf[], int start, int count) {
        APTUtils.setTokenText((APTToken)_token, buf, start, count);
    }

    @Override
    public void traceIn(String rname) {
        traceDepth ++;
        traceIndent();
        char c = LA(1);
        Object ch = (c == '\n') ? "\\n" : c == '\t' ? "\\t" : ("" + c); // NOI18N
        System.out.println("> lexer " + rname + "; c==" + ch); // NOI18N
    }

    public void traceOut(String rname) {
        traceIndent();
        char c = LA(1);
        Object ch = c == '\n' ? "\\n" : c == '\t' ? "\\t" : ("" + c); // NOI18N
        System.out.println("< lexer " + rname + "; c==" + ch); // NOI18N
        traceDepth--;
    }

    private int errorCount = 0;

    public int getErrorCount() {
        return errorCount;
    }

    public void reportError(RecognitionException e) {

        if (reportErrors) {
            super.reportError(e);
        }
        errorCount++;
    }

    public void reportError(String s) {
        if (reportErrors) {
            super.reportError(s);
        }
        errorCount++;
    }

    private boolean isCOrCPP() {
        return aptKind == APTFile.Kind.C_CPP;
    }

    private boolean isFortran() {
        return aptKind == APTFile.Kind.FORTRAN_FIXED || aptKind == APTFile.Kind.FORTRAN_FREE;
    }

    private boolean isFreeFormFortran() {
        return aptKind == APTFile.Kind.FORTRAN_FREE;
    }

/*
    protected void printf(String pattern, int i) {
        Printf.printf(pattern, new Object[] { new Integer(i) });
    }

    protected void printf(String pattern, int i, boolean b) {
        Printf.printf(pattern, new Object[] { new Integer(i), Boolean.valueOf(b) });
    }

    protected void printf(String pattern) {
        Printf.printf(pattern, new Object[] {});
    }
*/
	
    private static final int PREPROC_POSSIBLE = 0;

    private static final int PREPROC_PENDING = 1;

    private static final int AFTER_DEFINE = 2;
    /**
     * INCLUDE_STRING token is expected while in this state
     */ 
    private static final int AFTER_INLUDE = 3;


    /**
     *  A '#' character read while in this state would be treated as the
     *  start of a PrprocDirective. Other '#' chars would be treated as
     *  POUND chars.
     */	
    private boolean preprocPossible;
    private boolean isPreprocPossible() {
            return preprocPossible;
    }
    private void setPreprocPossible(boolean possible) {
            this.preprocPossible = possible;
    }

    /**
     *  Flag prevents token creating for APT light.
     */	
    private boolean onlyPreproc = false;
    private boolean isOnlyPreproc() {
        return onlyPreproc;
    }
    public void setOnlyPreproc(boolean onlyPreproc) {
        this.onlyPreproc = onlyPreproc;
    }

    /**
     *  EndOfLine read while in this state whould be treated as the end
     * of a PreprocDirective and token END_PREPROC_DIRECTIVE will be created
     */
    private boolean preprocPending;
    private boolean isPreprocPending() {
        return preprocPending;
    }
    private void setPreprocPending(boolean pending) {
        this.preprocPending = pending;
    }

    private boolean afterInclude = false;
    private boolean isAfterInclude() {
        return afterInclude;
    }
    private void setAfterInclude(boolean afterInclude) {
        this.afterInclude = afterInclude;
    }


    /**
     * ID read while in this state whould be treated as ID_DEFINED, 
     * need for not expanding ID in expresions like:
     * #if defined MACRO
     */
    private boolean afterPPDefined = false;
    private boolean ppDefinedAllowed = true;
    private boolean isAfterPPDefined() {
        return afterPPDefined;
    }
    private void setAfterPPDefined(boolean afterPPDefined) {
        this.afterPPDefined = afterPPDefined;
    }

    private boolean isPPDefinedAllowed() {
        return ppDefinedAllowed;
    }

    private void setPPDefinedAllowed(boolean ppDefinedAllowed) {
        this.ppDefinedAllowed = ppDefinedAllowed;
    }

    /**
     * ID read while in this state whould be treated as ID, but 
     * LA(1) will be checked to switch into "funLikeMacro" state upon 
     * (LA(1) == '(') without leading whitespace 
     * (need for FUN_LIKE_MACRO_LPAREN token)
     */
    private boolean afterDefine = false;
    private boolean isAfterDefine() {
        return afterDefine;
    }
    private void setAfterDefine(boolean afterDefine) {
        this.afterDefine = afterDefine;
    }

    /**
     * FUN_LIKE_MACRO_LPAREN token is expected while in this state
     */
    private boolean funLikeMacro = false;
    private boolean isFunLikeMacro() {
        return funLikeMacro;
    }
    private void setFunLikeMacro(boolean funLikeMacro) {
        this.funLikeMacro = funLikeMacro;
    }
    
    private void clearPrepProcFlags() {
        setFunLikeMacro(false);
        setAfterDefine(false);
        setAfterPPDefined(false);
        setAfterInclude(false);
        setPreprocPending(false);
    }

    @Override
    protected APTToken makeToken(int t) {
        if (callback != null) {
            callback.onMakeToken(t, getTokenStartColumn(), getTokenStartLine());
        }

        if (isOnlyPreproc() && isPreprocPossible()) {
           // do not create token if lexer builds light stream
            if (!(t==Token.EOF_TYPE || t==END_PREPROC_DIRECTIVE)){
                return null;
            }
        }
        // Our literal check
        int literalType = testLiteralsTable(0);
        APTToken k = APTUtils.createAPTToken(t, tokenStartOffset, offset, getTokenStartColumn(), getTokenStartLine(), inputState.getColumn(), inputState.getLine(), literalType);
        // it should be impossible to have preprocessor directive 
        // after valid token. preprocessor directive valid only
        // at start of line @see newline()
        if (t != COMMENT) { // block comment is valid anywhere
            setPreprocPossible(t == END_PREPROC_DIRECTIVE);
        }
        return k;
    }

    public void resetText() {
        super.resetText();
        tokenStartOffset = offset;
    }

    public void consume() {
        super.consume();
        if (guessing == 0) {
            offset++;
        }
    }

/*
    boolean wasTab;
    public void consume() throws CharStreamException {
        wasTab = false;
        super.consume();
        if (!wasTab) {
            offset++;
        }
    }

    public void tab() {
        wasTab = true;
        int c = getColumn();
        super.tab();
        offset += getColumn() - c;
    }
*/
    public int mark() {
        mkOffset = offset;
        return super.mark(); 
    }

    public void rewind(int mark) {
        super.rewind(mark);
        offset = mkOffset;
    }

    /*public int getOffset() {
        return offset;
    }*/

    int offset = 0;
    int tokenStartOffset = 0;
    int mkOffset = 0;

    public void newline() 
    {
        super.newline();
        if (!isPreprocPending()) {
            setPreprocPossible(true);
        }
    }

    private void deferredNewline() 
    {
        super.newline();
    }
}

/* Comments: */

FORTRAN_COMMENT options { constText=true; } :
    {isFortran() && (inputState.getColumn() == 1 && (LA(2)=='\r' || LA(2)=='\n' || LA(2)==' ') || (isFreeFormFortran() && LA(1) == '!') )}?
    ('!' | ('c'|'C') | '*')
    (~('\n' | '\r'))*
    {$setType(FORTRAN_COMMENT);}
    ;


/* Operators: */

COMMA          options { constText=true; } : ',' ;
QUESTIONMARK   options { constText=true; } : '?' ;
SEMICOLON      options { constText=true; } : ';' ;

/*
// DOT & ELLIPSIS are commented out since they are generated as part of
// the Number rule below due to some bizarre lexical ambiguity shme.
// DOT  :       '.' ;
// ELLIPSIS      : "..." ;
*/

LPAREN  options { constText=true; }        : '(' 
                    { 
                        if (isFunLikeMacro()) {
                            setFunLikeMacro(false);
                            $setType(FUN_LIKE_MACRO_LPAREN);
                        }
                    }
                ;
RPAREN options { constText=true; } : ')' ;
LSQUARE options { constText=true; }        : '[' ;
RSQUARE options { constText=true; }        : ']' ;
LCURLY	options { constText=true; }	: '{' ;
RCURLY	options { constText=true; }	: '}' ;

TILDE  options { constText=true; }    : '~' ;

FIRST_ASSIGN options { constText=true; } :
    '=' ({$setType(ASSIGNEQUAL);}           //ASSIGNEQUAL     : '=' ;
    | '=' {$setType(EQUAL);});              //EQUAL           : "==" ;

FIRST_DIVIDE :
    '/' ( {$setType(DIVIDE);}               //DIVIDE          : '/' ;
    | '=' {$setType(DIVIDEEQUAL);} )        //DIVIDEEQUAL     : "/=" ;
    | {isCOrCPP()}? COMMENT {$setType(COMMENT);}
    | {isCOrCPP()}? CPP_COMMENT {$setType(CPP_COMMENT);};

FIRST_STAR options { constText=true; } :
    '*' ( {$setType(STAR);}                 //STAR            : '*' ;
    | '=' {$setType(TIMESEQUAL);});         //TIMESEQUAL      : "*=" ;

FIRST_MOD options { constText=true; } :
    '%' ( {$setType(MOD);}                  //MOD             : '%' ;
    | '=' {$setType(MODEQUAL);}             //MODEQUAL        : "%=" ;
    | '>' {$setType(RCURLY);}               //RCURLY          : "%>" ;
    | ':' ( {isPreprocPending()}? {$setType(SHARP);}
        | {isPreprocPending()}? '%' ':' {$setType(DBL_SHARP);}
        | {!isPreprocPossible()}? {$setType(SHARP);}
        | {isPreprocPossible()}?
            {
                $setType(PREPROC_DIRECTIVE);
                setPreprocPossible(false);
                setPreprocPending(true);
                setPPDefinedAllowed(true);
            }
            (options{greedy = true;}:Space|COMMENT)*
            (  // lexer has no token labels
              ("include" PostPPKwdChar) => "include" { $setType(INCLUDE); setAfterInclude(true); setPPDefinedAllowed(false); } 
            | ("include_next" PostPPKwdChar) => "include_next" { $setType(INCLUDE_NEXT); setAfterInclude(true); setPPDefinedAllowed(false); } 
            | ("define" PostPPKwdChar) => "define" { $setType(DEFINE); setAfterDefine(true); setPPDefinedAllowed(false);}
            | ("ifdef" PostPPKwdChar) => "ifdef" { $setType(IFDEF); setPPDefinedAllowed(false);}
            | ("ifndef" PostPPKwdChar) => "ifndef" { $setType(IFNDEF); setPPDefinedAllowed(false);}
            | ("if" PostPPKwdChar) =>  "if"   { $setType(IF); }
            | ("undef" PostPPKwdChar) => "undef"  { $setType(UNDEF); setPPDefinedAllowed(false); }
            | ("elif" PostPPKwdChar) => "elif"  { $setType(ELIF);  }
            | ("else" PostPPKwdChar) =>  "else" { $setType(ELSE); }
            | ("endif" PostPPKwdChar) => "endif" { $setType(ENDIF); }
            | ("pragma" PostPPKwdChar) => "pragma" { $setType(PRAGMA); setPPDefinedAllowed(false); }
            | ("error" PostPPKwdChar) => "error" { $setType(ERROR); } DirectiveBody
            | ("line" PostPPKwdChar) => "line" { $setType(LINE); } DirectiveBody
            | DirectiveBody)
            // Do not need this here, can be skipped
            (options{greedy = true;}:Space)*
        ));

FIRST_NOT options { constText=true; } :
    '!' ( {$setType(NOT);}                  //NOT             : '!' ;
    | '=' {$setType(NOTEQUAL);});           //NOTEQUAL        : "!=" ;

FIRST_AMPERSAND options { constText=true; } :
    '&' ( {$setType(AMPERSAND);}            //AMPERSAND       : '&' ;
    | '&' {$setType(AND);}                  //AND             : "&&" ;
    | '=' {$setType(BITWISEANDEQUAL);});    //BITWISEANDEQUAL : "&=" ;


/* Comments: */

protected COMMENT :
		"/*"
		( options {greedy=false;}:
			EndOfLine {deferredNewline();}
                        | . )*
		"*/"
	;

protected CPP_COMMENT
	:
		"//" ( '\\' EndOfLine {deferredNewline();}
                     |  ~('\n' | '\r')
                     )*
	;

FIRST_OR options { constText=true; } :
    '|' ({$setType(BITWISEOR);}             //BITWISEOR       : '|' ;
    | '=' {$setType(BITWISEOREQUAL);}       //BITWISEOREQUAL  : "|=" ;
    | '|' {$setType(OR);});                 //OR              : "||" ;

FIRST_BITWISEXOR options { constText=true; } :
    '^' ( {$setType(BITWISEXOR);}           //BITWISEXOR      : '^' ;
    | '=' {$setType(BITWISEXOREQUAL);} );   //BITWISEXOREQUAL : "^=" ;

FIRST_COLON options { constText=true; } :
    ':' ( {$setType(COLON);}                //COLON   : ':' ;
    | ':' {$setType(SCOPE);}                //SCOPE   : "::"  ;
    | '>' {$setType(RSQUARE);} );           //RSQUARE : ":>" ;


FIRST_LESS :
    ( 
        // C++11 standard - 2.5 p3, bullet 2
        ("<::" ~(':'|'>')) => '<' {$setType(LESSTHAN);}
    | 
        ('<' (options{generateAmbigWarnings = false;}:
            {isAfterInclude()}? H_char_sequence ('>')? {$setType(SYS_INCLUDE_STRING);setAfterInclude(false);}
            | '=' {$setType(LESSTHANOREQUALTO);}            //LESSTHANOREQUALTO     : "<=" ;
            | '%' {$setType(LCURLY);}                       //LCURLY                : "<%" ;
            | ':' {$setType(LSQUARE);}                      //LSQUARE               : "<:" ;
            | {$setType(LESSTHAN);}                         //LESSTHAN              : "<" ;
            | '<' ({$setType(SHIFTLEFT);}                   //SHIFTLEFT             : "<<" ;
                   | '=' {$setType(SHIFTLEFTEQUAL);}))      //SHIFTLEFTEQUAL        : "<<=" ;
        )
    );

/*
DOLLAR options { constText=true; }  :  '$' ;
*/

AT  options { constText=true; }     :  '@' ;

GRAVE_ACCENT options { constText=true; }:  '`';

FIRST_GREATER options { constText=true; } : 
    '>' ( {$setType(GREATERTHAN);}                  //GREATERTHAN           : ">" ;
    | '=' {$setType(GREATERTHANOREQUALTO);}         //GREATERTHANOREQUALTO  : ">=" ;
    | '>' ( {$setType(SHIFTRIGHT);}                 //SHIFTRIGHT            : ">>" ;
            | '=' {$setType(SHIFTRIGHTEQUAL);}));   //SHIFTRIGHTEQUAL       : ">>=" ;

FIRST_MINUS options { constText=true; } :
    '-' ( {$setType(MINUS);}                        //MINUS           : '-' ;
    | '=' {$setType(MINUSEQUAL);}                   //MINUSEQUAL      : "-=" ;
    | '-' {$setType(MINUSMINUS);}                   //MINUSMINUS      : "--" ;
    | '>' ( {$setType(POINTERTO);}                  //POINTERTO       : "->" ;
            | '*' {$setType(POINTERTOMBR);}));      //POINTERTOMBR    : "->*" ;

FIRST_PLUS options { constText=true; } : 
    '+' ( {$setType(PLUS);}             //PLUS            : '+' ;
    | '=' {$setType(PLUSEQUAL);}        //PLUSEQUAL       : "+=" ;
    | '+' {$setType(PLUSPLUS);});       //PLUSPLUS        : "++" ;


// Whitespace
Whitespace options {checkSkip=true;} :	
                { 
                        $setType(Token.SKIP);
                }
                (	(' ' |'\t' | '\f') 
			// handle newlines
		|	(	"\r\n"  {offset--;} // MS
			|	'\r'    // Mac
			|	'\n'    // Unix 
			)	
                        { 
                            if (isPreprocPending()) {
                                $setType(END_PREPROC_DIRECTIVE);
                                clearPrepProcFlags();
                            }
                            newline(); 
                        }
			// handle continuation lines
		|	'\\' 
                        ( {$setType(BACK_SLASH);} |
                            (	"\r\n" {offset--;} // MS
                            |	"\r"    // Mac
                            |	"\n"    // Unix 
                            )	{$setType(Token.SKIP); deferredNewline();}
                        )
		)	
	;

protected
EndOfLine
	:	(	options{generateAmbigWarnings = false;}:
			"\r\n"  {offset--;}// MS
		|	'\r'    // Mac
		|	'\n'    // Unix
		) 
	;

FIRST_QUOTATION :
        '"' (
            {isAfterInclude()}? 
            Q_char_sequence '"' 
            {setAfterInclude(false);$setType(INCLUDE_STRING);}
            |STRING_LITERAL_BODY {$setType(STRING_LITERAL);}
            )
;

// preprocessor expressions

protected H_char_sequence : (~('>'|'\r'|'\n'))* ;

protected Q_char_sequence : (~('\"'|'\r'|'\n'))* ;

PREPROC_DIRECTIVE :
         '#'
                (   
                    {isPreprocPending()}? {$setType(SHARP);}
                 |
                    {isPreprocPending()}? '#' {$setType(DBL_SHARP);}
                 | 
                    {!isPreprocPossible()}? {$setType(SHARP);}
                 |
                    {isPreprocPossible()}? 
                    {
                        $setType(PREPROC_DIRECTIVE);
                        setPreprocPossible(false);
                        setPreprocPending(true);
                        setPPDefinedAllowed(true);
                    }
                    (options{greedy = true;}:Space|COMMENT)*
                    (  // lexer has no token labels
                      ("include" PostPPKwdChar) => "include" { $setType(INCLUDE); setAfterInclude(true); setPPDefinedAllowed(false); } 
                    | ("include_next" PostPPKwdChar) => "include_next" { $setType(INCLUDE_NEXT); setAfterInclude(true); setPPDefinedAllowed(false); } 
                    | ("define" PostPPKwdChar) => "define" { $setType(DEFINE); setAfterDefine(true); setPPDefinedAllowed(false);}
                    | ("ifdef" PostPPKwdChar) => "ifdef" { $setType(IFDEF); setPPDefinedAllowed(false);}
                    | ("ifndef" PostPPKwdChar) => "ifndef" { $setType(IFNDEF); setPPDefinedAllowed(false);}
                    | ("if" PostPPKwdChar) =>  "if"   { $setType(IF); }
                    | ("undef" PostPPKwdChar) => "undef"  { $setType(UNDEF); setPPDefinedAllowed(false); }
                    | ("elif" PostPPKwdChar) => "elif"  { $setType(ELIF);  }
                    | ("else" PostPPKwdChar) =>  "else" { $setType(ELSE); }
                    | ("endif" PostPPKwdChar) => "endif" { $setType(ENDIF); }
                    | ("pragma" PostPPKwdChar) => "pragma" { $setType(PRAGMA); setPPDefinedAllowed(false); }
                    | ("error" PostPPKwdChar) => "error" { $setType(ERROR); } DirectiveBody
                    | ("line" PostPPKwdChar) => "line" { $setType(LINE); } DirectiveBody
                    | DirectiveBody)
                    // Do not need this here, can be skipped
                    (options{greedy = true;}:Space)*
                )
	;

/*protected
AfterPragma:DirectiveBody;

protected
AfterError:DirectiveBody;

protected
AfterLine:DirectiveBody;*/

// eat everything till the end of line
protected
DirectiveBody
        :
		( 
                        options{warnWhenFollowAmbig = false; }:
                        '\\'
                        (	"\r\n"  {offset--;} // MS 
			|	"\r"     // MAC
			|	"\n"     // Unix
			)	{deferredNewline();}
		|	~('\r' | '\n' )
		)*
        ;

protected  Space : (options {combineChars=true;}:' ' | '\t' | '\f');

/* Literals: */

/*
 * Note that we do NOT handle tri-graphs nor multi-byte sequences.
 */

CHAR_LITERAL
        :   
            '\'' CHAR_LITERAL_BODY
        ;

protected CHAR_LITERAL_BODY
        :   
		(       
                        '\\'                        
                        (   options{greedy=true;}:
                            (	"\r\n" {offset--;} // MS 
                            |	"\r"     // MAC
                            |	"\n"     // Unix
                            ) {deferredNewline();}
                        | 
                            '\''
                        |   
                            '\\'    
                        )?
		|	
                         ~('\'' | '\r' | '\n' | '\\')
		)*
            ('\'' //(Suffix)? // correct ending of char literal
                |  {LA(1)=='\r'||LA(1)=='\n'}? // error char literal doesn't have closing quote
            )
        ;

protected STRING_LITERAL
        :
            '"' STRING_LITERAL_BODY            
        ;


protected STRING_LITERAL_BODY :
		(       
                        '\\'                        
                        (   options{greedy=true;}:
                            (	"\r\n" {offset--;} // MS 
                            |	"\r"     // MAC
                            |	"\n"     // Unix
                            ) {deferredNewline();}
                        | 
                            '"'
                        |   
                            '\\'    
                        )?
		|	
                         ~('"' | '\r' | '\n' | '\\')
		)*
            ('"' //(Suffix)? // correct ending of string
                |  {LA(1)=='\r'||LA(1)=='\n'}? // error string doesn't have closing quote
            )
        ;

protected RAW_STRING_LITERAL
        :
            '"' RAW_STRING_LITERAL_BODY            
        ;

protected RAW_STRING_LITERAL_BODY 
{
    boolean end = false;
    StringBuilder s1 = new StringBuilder();
    StringBuilder s2 = null; 
}   
    :
    ((~('"' | '\r' | '\n' | '\\' | '\t' | '(' | ')') {s1.append(LA(0));} )*)
    '('
        (options{greedy=true;}:   
            (   "\r"
                    (options{greedy=true;}: "\n" {offset--;} // MS 
                    |   // MAC
                    )
            |   "\n"     // Unix
            ) {deferredNewline();}
        |	
               (')' (~('"' | '\r' | '\n' | '\\' | '\t' | '(' | ')') )* ) =>
                {s2 = new StringBuilder();}
                ')' (options{greedy=true;}: ~('"' | '\r' | '\n' | '\\' | '\t' | '(' | ')') {s2.append(LA(0));})*

                ({ LA(1)=='"' && !s1.toString().equals(s2.toString())}? 
                    '"'
                |
                    {end = LA(1)=='"';}
                )
                {s2 = null;}
            | 
                ~('\r' | '\n' | '"')                         
            |   
                { !end }? '"' 
            
        )*
    ('"' //(Suffix)? // correct ending of string
        |  {LA(1)=='\r'||LA(1)=='\n'}? // error string doesn't have closing quote
    )
    ;

/*
 * Handle the various escape sequences.
 *
 * Note carefully that these numeric escape *sequences* are *not* of the
 * same form as the C language numeric *constants*.
 *
 * There is no such thing as a binary numeric escape sequence.
 *
 * Octal escape sequences are either 1, 2, or 3 octal digits exactly.
 *
 * There is no such thing as a decimal escape sequence.
 *
 * Hexadecimal escape sequences are begun with a leading \x and continue
 * until a non-hexadecimal character is found.
 *
 * No real handling of tri-graph sequences, yet.
 */

protected
Escape:
	'\\'
		('a' | 'b' | 'e' | 'f' | 'n' | 'r' | 't' | 'v' | '"' | '\'' | '\\' | '?' |
 /*deprecated escape symbols*/     '%' |
                    ('0'..'3') (options{greedy=true;}: Digit)? (options{greedy=true;}: Digit)?
		| ('4'..'7') (options{greedy=true;}: Digit)?
		| 'x' (options{greedy=true;}: Digit | 'a'..'f' | 'A'..'F')+
		)
	;

/* Numeric Constants: */

protected Digit:	'0'..'9' ;

//protected Decimal:	('0'..'9')+ ;

//protected Suffix:
//    (
//        (options {combineChars=true;} : 'a'..'z'|'A'..'Z'|'_') // '$' added for gcc support
//        (options {combineChars=true;greedy=true;} : 'a'..'z'|'A'..'Z'|'_'|'0'..'9')* // '$' added for gcc support
//    )
//    ;

protected Exponent:	('e' | 'E') ('+' | '-')? (Digit)* ;

//protected Vocabulary:	'\3'..'\377' ;

NUMBER
        :
    (options {greedy=true;} :
		( (Digit)+ ('.' | 'e' | 'E') )=> (Digit)+
		(options {greedy=true;} : '.' (Digit)* (options {greedy=true;} : Exponent)? {$setType(FLOATONE);} //Zuo 3/12/01
		| Exponent                 {$setType(FLOATTWO);} //Zuo 3/12/01
		)
                //(Suffix)?
	|	'.'  (                  {$setType(DOT);}	//TODO: solve "dot & ellipsis"! 
		| 	(Digit)+ (options {greedy=true;} : Exponent)?   
                                        {$setType(FLOATONE);} //Zuo 3/12/01
                        //(Suffix)?
		| '*' {$setType(DOTMBR);}
                | {(LA(2)=='.')}? ".."  {$setType(ELLIPSIS);}
                )

	|	'1'..'9' (Digit)*
                                        {$setType(DECIMALINT);}  
                //(Suffix)?
        |
                (       '0'
                    (   ('x' | 'X') => ('x' | 'X') (options {greedy=true;} : 'a'..'f' | 'A'..'F' | Digit)*
                                        {$setType(HEXADECIMALINT);}   
                    |	('b' | 'B') => ('b' | 'B') (options {greedy=true;} : '0'|'1')*
                                        {$setType(BINARYINT);}
                    |   ('0'..'7')*            
                                        {$setType(OCTALINT);}
                    )
                )
                //(Suffix)?
    )    
    ;

// Everything that can be treated lke ID
ID_LIKE:
        {isPreprocPending()}?
        ({isPPDefinedAllowed()}? "defined")=> "defined" 
           ( 
             (PostPPKwdChar | "(") => {setAfterPPDefined(true); $setType(DEFINED);}
           | 
                {
                    if (isAfterPPDefined()) {
                        setAfterPPDefined(false);
                        $setType(ID_DEFINED);
                    } else {
                        $setType(IDENT); 
                    }
                }
           )
     |
        {!isAfterPPDefined()}?
        Identifier
        {
            if (isAfterDefine()) {
                setAfterDefine(false);
                if (LA(1) == '(') {
                    setFunLikeMacro(true);
                }
            }
            $setType(IDENT);
        }
     |  ('L' 'R' '"') => 'L' 'R' RAW_STRING_LITERAL {$setType(STRING_LITERAL);}
     |  ('u' 'R' '"') => 'u' 'R' RAW_STRING_LITERAL {$setType(STRING_LITERAL);}
     |  ('U' 'R' '"') => 'U' 'R' RAW_STRING_LITERAL {$setType(STRING_LITERAL);}
     |  ('u' '8' 'R' '"') => 'u' '8' 'R' RAW_STRING_LITERAL {$setType(STRING_LITERAL);}
     |  ('u' '8' '"') => 'u' '8' STRING_LITERAL {$setType(STRING_LITERAL);}
     |
        // We have checked opposite above
        //{isAfterPPDefined()}? 
        Identifier 
        {setAfterPPDefined(false);$setType(ID_DEFINED);}
     |  'L' ( CHAR_LITERAL {$setType(CHAR_LITERAL);}
            | STRING_LITERAL {$setType(STRING_LITERAL);})
     |  'u' ( CHAR_LITERAL {$setType(CHAR_LITERAL);}
            | STRING_LITERAL {$setType(STRING_LITERAL);})
     |  'U' ( CHAR_LITERAL {$setType(CHAR_LITERAL);}
            | STRING_LITERAL {$setType(STRING_LITERAL);})
     |  'R' RAW_STRING_LITERAL {$setType(STRING_LITERAL);}
;

// FAKE , just to get the correct type number for this token
protected ID_DEFINED : ;

protected
Identifier      
        :
            // I think this check should have been done before
            //{ LA(1)!='L' || (LA(2)!='\'' && LA(2) != '\"') }? // L"" and L'' are StringLiterals/CharLiterals, not ID
            (
                (options {combineChars=true;} : 'a'..'z'|'A'..'Z'|'_'|'$'|{Character.isJavaIdentifierStart(LA(1))}?'\u00c0' .. '\ufffe') // '$' added for gcc support
		(options {combineChars=true;} : 'a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$'|{Character.isJavaIdentifierPart(LA(1))}?'\u00c0' .. '\ufffe')* // '$' added for gcc support
            )
        ;

protected
PostPPKwdChar: { !Character.isJavaIdentifierPart(LA(1)) }? | EndOfLine | { LA(1) == EOF_CHAR}? ;

protected
PostInclChar: PostPPKwdChar | '\"' | '<' ;

protected
PostIfChar: { !Character.isJavaIdentifierPart(LA(1)) }?;

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!! ALL NEW RULES MUST BE ADDED BEFORE THIS LINE !!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
protected
LAST_LEXER_FAKE_RULE : ;
