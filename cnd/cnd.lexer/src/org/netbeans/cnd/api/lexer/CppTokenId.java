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

package org.netbeans.cnd.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.spi.lexer.CndLexerLanguageEmbeddingProvider;
import org.netbeans.modules.cnd.lexer.CppLexer;
import org.netbeans.modules.cnd.lexer.PreprocLexer;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.lookup.Lookups;

/**
 * Token ids of C/C++ languages defined as enum.
 *
 * @version 1.00
 */
public enum CppTokenId implements TokenId {
    
    // make sure token category names are the same used in the string
    // constants below
    
    ERROR(null, "error"), // NOI18N
    IDENTIFIER(null, "identifier"), // NOI18N

    // C/C++ keywords
    ALIGNOF("alignof", "keyword"), // g++ // NOI18N
    __ALIGNOF("__alignof", "keyword"), // g++ // NOI18N
    __ALIGNOF__("__alignof__", "keyword"), // gcc // NOI18N
    ASM("asm", "keyword-directive"), // gcc and C++ // NOI18N
    _ASM("_asm", "keyword"), // g++ // NOI18N
    __ASM("__asm", "keyword"), // gcc // NOI18N
    __ASM__("__asm__", "keyword"), // gcc // NOI18N    
    AUTO("auto", "keyword"), // NOI18N
    BIT("bit", "keyword"), // NOI18N
    BOOL("bool", "keyword"), // C++ // NOI18N
    BREAK("break", "keyword-directive"), // NOI18N
    CASE("case", "keyword-directive"), // NOI18N
    CATCH("catch", "keyword-directive"), // C++ // NOI18N
    CHAR("char", "keyword"), // NOI18N
    CLASS("class", "keyword"), // C++ // NOI18N
    CONST("const", "keyword"), // NOI18N
    __CONST("__const", "keyword"), // gcc // NOI18N
    __CONST__("__const__", "keyword"), // gcc C only // NOI18N
    CONST_CAST("const_cast", "keyword"), // C++ // NOI18N
    CONTINUE("continue", "keyword-directive"), // NOI18N
    DEFAULT("default", "keyword-directive"), // NOI18N
    DELETE("delete", "keyword"), // C++ // NOI18N
    DO("do", "keyword-directive"), // NOI18N
    DOUBLE("double", "keyword"), // NOI18N
    DYNAMIC_CAST("dynamic_cast", "keyword"), // C++ // NOI18N
    ELSE("else", "keyword-directive"), // NOI18N
    _ENDASM("_endasm", "keyword"), // NOI18N
    ENUM("enum", "keyword"), // NOI18N
    EXPLICIT("explicit", "keyword"), // C++ // NOI18N
    EXPORT("export", "keyword"), // C++ // NOI18N
    EXTERN("extern", "keyword"), // NOI18N
    FINALLY("finally", "keyword-directive"), // C++ // NOI18N
    FLOAT("float", "keyword"), // NOI18N
    FOR("for", "keyword-directive"), // NOI18N
    FRIEND("friend", "keyword"), // C++ // NOI18N
    __FUNC__("__func__", "keyword"), // NOI18N
    GOTO("goto", "keyword-directive"), // NOI18N
    IF("if", "keyword-directive"), // NOI18N
    INLINE("inline", "keyword"), // now in C also // NOI18N
    _INLINE("_inline", "keyword"), // g++ // NOI18N
    __INLINE("__inline", "keyword"), // gcc // NOI18N
    __INLINE__("__inline__", "keyword"), // gcc // NOI18N
    INT("int", "keyword"), // NOI18N
    LONG("long", "keyword"), // NOI18N
    MUTABLE("mutable", "keyword"), // C++ // NOI18N
    NAMESPACE("namespace", "keyword"), // C++ // NOI18N
    NEW("new", "keyword"), // C++ // NOI18N
    OPERATOR("operator", "keyword"), // C++ // NOI18N
    PASCAL("pascal", "keyword"), // g++ // NOI18N
    _PASCAL("_pascal", "keyword"), // g++ // NOI18N
    __PASCAL("__pascal", "keyword"), // g++ // NOI18N
    PRIVATE("private", "keyword"), //C++ // NOI18N
    PROTECTED("protected", "keyword"), //C++ // NOI18N
    PUBLIC("public", "keyword"), // C++ // NOI18N
    REGISTER("register", "keyword"), // NOI18N
    REINTERPRET_CAST("reinterpret_cast", "keyword"), //C++ // NOI18N
    RESTRICT("restrict", "keyword"), // C // NOI18N
    RETURN("return", "keyword-directive"), // NOI18N
    SHORT("short", "keyword"), // NOI18N
    SIGNED("signed", "keyword"), // NOI18N
    __SIGNED("__signed", "keyword"), // gcc // NOI18N
    __SIGNED__("__signed__", "keyword"), // gcc // NOI18N
    SIZEOF("sizeof", "keyword"), // NOI18N
    STATIC("static", "keyword"), // NOI18N
    STATIC_CAST("static_cast", "keyword"), // C++ // NOI18N
    STRUCT("struct", "keyword"), // NOI18N
    SWITCH("switch", "keyword-directive"), // NOI18N
    TEMPLATE("template", "keyword"), // C++ // NOI18N
    THIS("this", "keyword"), // C++ // NOI18N
    THROW("throw", "keyword-directive"), //C++ // NOI18N
    TRY("try", "keyword-directive"), // C++ // NOI18N
    __TRY("__try", "keyword"), // g++ // NOI18N
    TYPEDEF("typedef", "keyword"), // NOI18N
    TYPEID("typeid", "keyword"), // C++ // NOI18N
    TYPENAME("typename", "keyword"), // C++ // NOI18N
    TYPEOF("typeof", "keyword"), // gcc, C++ // NOI18N
    __TYPEOF("__typeof", "keyword"), // gcc // NOI18N
    __TYPEOF__("__typeof__", "keyword"), // gcc // NOI18N
    UNION("union", "keyword"), // NOI18N
    UNSIGNED("unsigned", "keyword"), // NOI18N
    __UNSIGNED__("__unsigned__", "keyword"), // g++ // NOI18N
    USING("using", "keyword"), //C++ // NOI18N
    VIRTUAL("virtual", "keyword"), //C++ // NOI18N
    VOID("void", "keyword"), // NOI18N
    VOLATILE("volatile", "keyword"), // NOI18N
    __VOLATILE("__volatile", "keyword"), // gcc // NOI18N
    __VOLATILE__("__volatile__", "keyword"), // gcc // NOI18N
    WCHAR_T("wchar_t", "keyword"), // C++ // NOI18N
    WHILE("while", "keyword-directive"), // NOI18N
    __ATTRIBUTE__("__attribute__", "keyword"), // gcc // NOI18N
    __ATTRIBUTE("__attribute", "keyword"), // gcc // NOI18N
    __BUILTIN_VA_LIST("__builtin_va_list", "keyword"), // NOI18N
    _BOOL("_Bool", "keyword"), // C // NOI18N
    _CDECL("_cdecl", "keyword"), // g++ // NOI18N
    __CDECL("__cdecl", "keyword"), // g++ // NOI18N
    __CLRCALL("__clrcall", "keyword"), // g++ // NOI18N    
    _COMPLEX("_Complex", "keyword"), // C // NOI18N
    __COMPLEX("__complex", "keyword"), // g++ // NOI18N
    __COMPLEX__("__complex__", "keyword"), // gcc // NOI18N
    _DECLSPEC("_declspec", "keyword"), // g++ // NOI18N
    __DECLSPEC("__declspec", "keyword"), // g++ // NOI18N
    __EXTENSION__("__extension__", "keyword"), // g++ // NOI18N
    _FAR("_far", "keyword"), // g++ // NOI18N
    __FAR("__far", "keyword"), // g++ // NOI18N
    __FINALLY("__finally", "keyword"), // g++ // NOI18N    
    __FORCEINLINE("__forceinline", "keyword"), // g++ // NOI18N    
    __HAS_TRIVIAL_CONSTRUCTOR("__has_trivial_constructor", "keyword"), // g++ // NOI18N     
    __HAS_NOTHROW_ASSIGN("__has_nothrow_assign", "keyword"), // g++ // NOI18N
    __HAS_NOTHROW_COPY("__has_nothrow_copy", "keyword"), // g++ // NOI18N
    __HAS_NOTHROW_CONSTRUCTOR("__has_nothrow_constructor", "keyword"), // g++ // NOI18N
    __HAS_TRIVIAL_ASSIGN("__has_trivial_assign", "keyword"), // g++ // NOI18N
    __HAS_TRIVIAL_COPY("__has_trivial_copy", "keyword"), // g++ // NOI18N
    __HAS_TRIVIAL_DESTRUCTOR("__has_trivial_destructor", "keyword"), // g++ // NOI18N
    __HAS_VIRTUAL_DESTRUCTOR("__has_virtual_destructor", "keyword"), // g++ // NOI18N
    __IS_ABSTRACT("__is_abstract", "keyword"), // g++ // NOI18N
    __IS_EMPTY("__is_empty", "keyword"), // g++ // NOI18N
    __IS_LITERAL_TYPE("__is_literal_type", "keyword"), // g++ // NOI18N
    __IS_POLYMORPHIC("__is_polymorphic", "keyword"), // g++ // NOI18N
    __IS_STANDARD_LAYOUT("__is_standard_layout", "keyword"), // g++ // NOI18N
    __IS_TRIVIAL("__is_trivial", "keyword"), // g++ // NOI18N
    __IS_UNION("__is_union", "keyword"), // g++ // NOI18N
    __UNDERLYING_TYPE("__underlying_type", "keyword"), // g++ // NOI18N    
    _IMAGINARY("_Imaginary", "keyword"), // C // NOI18N
    __IMAG__("__imag__", "keyword"), // gcc // NOI18N
    _INT64("_int64", "keyword"), // g++ // NOI18N
    __INT64("__int64", "keyword"), // g++ // NOI18N
    __INTERRUPT("__interrupt", "keyword"), // g++ // NOI18N
    __IS_CLASS("__is_class", "keyword"), // g++ // NOI18N
    __IS_ENUM("__is_enum", "keyword"), // g++ // NOI18N
    __IS_POD("__is_pod", "keyword"), // g++ // NOI18N
    __IS_BASE_OF("__is_base_of", "keyword"), // g++ // NOI18N
    _NEAR("_near", "keyword"), // g++ // NOI18N
    __NEAR("__near", "keyword"), // g++ // NOI18N
    __NULL("__null", "keyword"), // g++ // NOI18N
    __REAL__("__real__", "keyword"), // gcc // NOI18N
    __RESTRICT("__restrict", "keyword"), // g++ // NOI18N
    __RESTRICT__("__restrict__", "keyword"), // gcc, g++ // NOI18N
    _STDCALL("_stdcall", "keyword"), // g++ // NOI18N
    __STDCALL("__stdcall", "keyword"), // g++ // NOI18N
    __SYMBOLIC("__symbolic", "keyword"), // gcc // NOI18N
    __GLOBAL("__global", "keyword"), // studio // NOI18N
    __HIDDEN("__hidden", "keyword"), // studio // NOI18N
    __THREAD("__thread", "keyword"), // gcc/studio // NOI18N
    __UNUSED__("__unused__", "keyword"), // gcc // NOI18N
    __W64("__w64", "keyword"), // g++ // NOI18N

    // C++11    
    ALIGNAS("alignas", "keyword"), // c++11 // NOI18N
    CHAR16_T("char16_t", "keyword"), // c++11 // NOI18N
    CHAR32_T("char32_t", "keyword"), // c++11 // NOI18N
    CONSTEXPR("constexpr", "keyword"), // c++11 // NOI18N
    DECLTYPE("decltype", "keyword"), // c++11 // NOI18N
    FINAL("final", "keyword"), // c++11 // NOI18N
    NOEXCEPT("noexcept", "keyword"), // c++11 // NOI18N
    NULLPTR("nullptr", "keyword"), // c++11 // NOI18N
    OVERRIDE("override", "keyword"), // c++11 // NOI18N
    STATIC_ASSERT("static_assert", "keyword"), // c++11 // NOI18N
    THREAD_LOCAL("thread_local", "keyword"), // c++11 // NOI18N
    _PRAGMA("_Pragma", "keyword"), // c++11 // NOI18N
    __DECLTYPE("__decltype", "keyword"), // g++ // NOI18N

    // C++17
    PREPROCESSOR___HAS_INCLUDE("__has_include", "preprocessor-keyword-directive"), // c++17 // NOI18N

    // C++20
    CHAR8_T("char8_t", "keyword"), // c++20 // NOI18N
    CONCEPT("concept", "keyword"), // c++20 // NOI18N
    CONSTEVAL("consteval", "keyword"), // c++20 // NOI18N
    CONSTINIT("constinit", "keyword"), // c++20 // NOI18N
    CO_AWAIT("co_await", "keyword"), // c++20 // NOI18N
    CO_RETURN("co_return", "keyword"), // c++20 // NOI18N
    CO_YIELD("co_yeild", "keyword"), // c++20 // NOI18N
    IMPORT("import", "keyword"), // c++20 // NOI18N
    MODULE("module", "keyword"), // c++20 // NOI18N
    REQUIRES("requires", "keyword"), // c++20 // NOI18N
    PREPROCESSOR___HAS_CPP_ATTRIBUTE("__has_cpp_attribute", "preprocessor-keyword-directive"), // c++20 // NOI18N
    PREPROCESSOR_EXPORT("export", "preprocessor-keyword-directive"), // c++20 // NOI18N
    PREPROCESSOR_IMPORT("import", "preprocessor-keyword-directive"), // c++20 // NOI18N
    PREPROCESSOR_MODULE("module", "preprocessor-keyword-directive"), // c++20 // NOI18N

    // C++23
    PREPROCESSOR_ELIFDEF("elifdef", "preprocessor-keyword-directive"), // c++23 // NOI18N
    PREPROCESSOR_ELIFNDEF("elifndef", "preprocessor-keyword-directive"), // c++23 // NOI18N

    // C11
    _ALIGNAS("_Alignas", "keyword"), // c11 // NOI18N
    _ALIGNOF("_Alignof", "keyword"), // c11 // NOI18N
    _ATOMIC("_Atomic", "keyword"), // c11 // NOI18N
    _GENERIC("_Generic", "keyword"), // c11 // NOI18N
    _NORETURN("_Noreturn", "keyword"), // c11 // NOI18N
    _STATIC_ASSERT("_Static_assert", "keyword"), // c11 // NOI18N
    _THREAD_LOCAL("_Thread_local", "keyword"), // c11 // NOI18N

    // C23
    TYPEOF_UNQUAL("typeof_unqual", "keyword"), // c23 // NOI18N
    _BITINT("_BitInt", "keyword"), // c23 // NOI18N
    _DECIMAL32("_Decimal32", "keyword"), // c23 // NOI18N
    _DECIMAL64("_Decimal64", "keyword"), // c23 // NOI18N
    _DECIMAL128("_Decimal128", "keyword"), // c23 // NOI18N
    PREPROCESSOR_EMBED("embed", "preprocessor-keyword-directive"), // c23 // NOI18N
    PREPROCESSOR___HAS_C_ATTRIBUTE("__has_c_attribute", "preprocessor-keyword-directive"), // c23 // NOI18N
    PREPROCESSOR___HAS_EMBED("__has_embed", "preprocessor-keyword-directive"), // c23 // NOI18N

    // extension points
    BUILT_IN_TYPE(null, "keyword"), // NOI18N
    TYPE_QUALIFIER(null, "keyword"), // NOI18N
    STORAGE_CLASS_SPECIFIER(null, "keyword"), // NOI18N
    
    // 
    INT_LITERAL(null, "number"), // NOI18N
    LONG_LITERAL(null, "number"), // NOI18N
    LONG_LONG_LITERAL(null, "number"), // NOI18N
    FLOAT_LITERAL(null, "number"), // NOI18N
    DOUBLE_LITERAL(null, "number"), // NOI18N
    UNSIGNED_LITERAL(null, "number"), // NOI18N
    UNSIGNED_LONG_LITERAL(null, "number"), // NOI18N
    UNSIGNED_LONG_LONG_LITERAL(null, "number"), // NOI18N
    CHAR_LITERAL(null, "character"), // NOI18N
    STRING_LITERAL(null, "string"), // NOI18N
    RAW_STRING_LITERAL(null, "string"), // NOI18N
    
    TRUE("true", "literal"), // C++ // NOI18N
    FALSE("false", "literal"), // C++ // NOI18N
    NULL("null", "literal"), // NOI18N
    
    LPAREN("(", "separator"), // NOI18N
    RPAREN(")", "separator"), // NOI18N
    LBRACE("{", "separator"), // NOI18N
    RBRACE("}", "separator"), // NOI18N
    LBRACKET("[", "separator"), // NOI18N
    RBRACKET("]", "separator"), // NOI18N
    SEMICOLON(";", "separator"), // NOI18N
    COMMA(",", "separator"), // NOI18N
    DOT(".", "separator"), // NOI18N
    DOTMBR(".*", "separator"), // NOI18N
    SCOPE("::", "separator"), // NOI18N
    ARROW("->", "separator"), // NOI18N
    ARROWMBR("->*", "separator"), // NOI18N
    
    EQ("=", "operator"), // NOI18N
    GT(">", "operator"), // NOI18N
    LT("<", "operator"), // NOI18N
    NOT("!", "operator"), // NOI18N
    TILDE("~", "operator"), // NOI18N
    QUESTION("?", "operator"), // NOI18N
    COLON(":", "operator"), // NOI18N
    EQEQ("==", "operator"), // NOI18N
    LTEQ("<=", "operator"), // NOI18N
    GTEQ(">=", "operator"), // NOI18N
    NOTEQ("!=","operator"), // NOI18N
    AMPAMP("&&", "operator"), // NOI18N
    BARBAR("||", "operator"), // NOI18N 
    PLUSPLUS("++", "operator"), // NOI18N 
    MINUSMINUS("--","operator"), // NOI18N
    PLUS("+", "operator"), // NOI18N
    MINUS("-", "operator"), // NOI18N
    STAR("*", "operator"), // NOI18N
    SLASH("/", "operator"), // NOI18N
    AMP("&", "operator"), // NOI18N
    BAR("|", "operator"), // NOI18N
    CARET("^", "operator"), // NOI18N
    PERCENT("%", "operator"), // NOI18N
    LTLT("<<", "operator"), // NOI18N
    GTGT(">>", "operator"), // NOI18N
    PLUSEQ("+=", "operator"), // NOI18N
    MINUSEQ("-=", "operator"), // NOI18N
    STAREQ("*=", "operator"), // NOI18N
    SLASHEQ("/=", "operator"), // NOI18N
    AMPEQ("&=", "operator"), // NOI18N
    BAREQ("|=", "operator"), // NOI18N
    CARETEQ("^=", "operator"), // NOI18N
    PERCENTEQ("%=", "operator"), // NOI18N
    LTLTEQ("<<=", "operator"), // NOI18N
    GTGTEQ(">>=", "operator"), // NOI18N

    ALTERNATE_AND("and", "keyword"), // NOI18N
    ALTERNATE_BITOR("bitor", "keyword"), // NOI18N
    ALTERNATE_OR("or", "keyword"), // NOI18N
    ALTERNATE_XOR("xor", "keyword"), // NOI18N
    ALTERNATE_COMPL("compl", "keyword"), // NOI18N
    ALTERNATE_BITAND("bitand", "keyword"), // NOI18N
    ALTERNATE_AND_EQ("and_eq", "keyword"), // NOI18N
    ALTERNATE_OR_EQ("or_eq", "keyword"), // NOI18N
    ALTERNATE_XOR_EQ("xor_eq", "keyword"), // NOI18N
    ALTERNATE_NOT("not", "keyword"), // NOI18N
    ALTERNATE_NOT_EQ("not_eq", "keyword"), // NOI18N

    ELLIPSIS("...", "special"), // NOI18N
    AT("@", "special"), // NOI18N
    GRAVE_ACCENT("`", "special"), // NOI18N
//    DOLLAR("$", "special"), // NOI18N
    SHARP("#", "special"), // NOI18N
    DBL_SHARP("##", "special"), // NOI18N
    BACK_SLASH("\\", "special"), // NOI18N
            
    WHITESPACE(null, "whitespace"), // NOI18N // all spaces except new line
    ESCAPED_LINE(null, "whitespace"), // NOI18N // line escape with \
    ESCAPED_WHITESPACE(null, "whitespace"), // NOI18N // whitespace escape with \ inside it
    NEW_LINE(null, "whitespace"), // NOI18N // new line \n or \r
    LINE_COMMENT(null, "comment"), // NOI18N
    BLOCK_COMMENT(null, "comment"), // NOI18N
    DOXYGEN_COMMENT(null, "comment"), // NOI18N
    DOXYGEN_LINE_COMMENT(null, "comment"), // NOI18N
    
    // Prerpocessor 
    //   - on top level
    PREPROCESSOR_DIRECTIVE(null, "preprocessor"), // NOI18N
    //   - tokens
    PREPROCESSOR_START("#", "preprocessor"), // NOI18N
    PREPROCESSOR_START_ALT("%:", "preprocessor"), // NOI18N alternative start preprocess token
    PREPROCESSOR_IF("if", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IFDEF("ifdef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IFNDEF("ifndef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ELSE("else", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ELIF("elif", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ENDIF("endif", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_DEFINE("define", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_UNDEF("undef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_INCLUDE("include", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_INCLUDE_NEXT("include_next", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_LINE("line", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IDENT("ident", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_PRAGMA("pragma", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_WARNING("warning", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ERROR("error", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_DEFINED("defined", "preprocessor-keyword"), // NOI18N
    
    PREPROCESSOR_USER_INCLUDE(null, "preprocessor-user-include-literal"), // NOI18N
    PREPROCESSOR_SYS_INCLUDE(null, "preprocessor-system-include-literal"), // NOI18N
    PREPROCESSOR_IDENTIFIER(null, "preprocessor-identifier"), // NOI18N

    // OpenMP
    PRAGMA_OMP(null, "pragma-omp"), // NOI18N
    PRAGMA_OMP_START("omp", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_PARALLEL("parallel", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SECTIONS("sections", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_WORKSHARE("workshare", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_NOWAIT("nowait", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_ORDERED("ordered", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SCHEDULE("schedule", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DYNAMIC("dynamic", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_GUIDED("guided", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_RUNTIME("runtime", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SECTION("section", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SINGLE("single", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_MASTER("master", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_CRITICAL("critical", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_BARRIER("barrier", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_ATOMIC("atomic", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SEQ_CST("seq_cst", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_CAPTURE("capture", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_READ("read", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_WRITE("write", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_FLUSH("flush", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_THREADPRIVATE("threadprivate", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_PRIVATE("private", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_FIRSTPRIVATE("firstprivate", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_LASTPRIVATE("lastprivate", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_LINEAR("linear", "pragma-omp-keyword-directive"), // NOI18N   
    PRAGMA_OMP_VAL("val", "pragma-omp-keyword-directive"), // NOI18N   
    PRAGMA_OMP_UVAL("uval", "pragma-omp-keyword-directive"), // NOI18N   
    PRAGMA_OMP_REF("ref", "pragma-omp-keyword-directive"), // NOI18N   
    PRAGMA_OMP_SHARED("shared", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_NONE("none", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_REDUCTION("reduction", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_COPYIN("copyin", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TASK("task", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_FINAL("final", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_UNTIED("untied", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_MERGEABLE("mergeable", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_IN_REDUCTION("in_reduction", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DEPEND("depend", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SOURCE("source", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SINK("sink", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_IN("in", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_OUT("out", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_INOUT("inout", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_PRIORITY("priority", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TASKWAIT("taskwait", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TASKGROUP("taskgroup", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TASKLOOP("taskloop", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_GRAINSIZE("grainsize", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_NUM_TASKS("num_tasks", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_NOGROUP("nogroup", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TASKYIELD("taskyield", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TARGET("target", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DATA("data", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DEVICE("device", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_MAP("map", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_USE_DEVICE_PTR("use_device_ptr", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_ENTER("enter", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_EXIT("exit", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_IS_DEVICE_PTR("is_device_ptr", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_UPDATE("update", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TO("to", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_FROM("from", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TOFROM("tofrom", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SCALAR("scalar", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_ALLOC("alloc", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_RELEASE("release", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DELETE("delete", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_ALWAYS("always", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DEFAULTMAP("defaultmap", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TEAMS("teams", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_NUM_TEAMS("num_teams", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_THREAD_LIMIT("thread_limit", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DISTRIBUTE("distribute", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DIST_SCHEDULE("dist_schedule", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_COLLAPSE("collapse", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_COPYPRIVATE("copyprivate", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DEFAULT("default", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_STATIC("static", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_IF("if", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_FOR("for", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DO("do", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_AUTO("auto", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_NUM_THREADS("num_threads", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SIMD("simd", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SAFELEN("safelen", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_SIMDLEN("simdlen", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_ALIGNED("aligned", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_DECLARE("declare", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_UNIFORM("uniform", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_INBRANCH("inbranch", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_NOTINBRANCH("notinbranch", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_CANCEL("cancel", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_CANCELLATION("cancellation", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_POINT("point", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_CONDITIONAL("conditional", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_TASK_REDUCTION("task_reduction", "pragma-omp-keyword-directive"), // NOI18N
    PRAGMA_OMP_INITIALIZER("initializer", "pragma-omp-keyword-directive"), // NOI18N
    
    // Pro*C
    PROC_DIRECTIVE(null, "proc"), // NOI18N
    PROC_SQL("sql", "proc-keyword-directive"), // NOI18N
    
    // Errors
    INVALID_COMMENT_END("*/", "error"), // NOI18N
    FLOAT_LITERAL_INVALID(null, "number"), // NOI18N
    
    // special EOF marker
    EOF(null, "whitespace"); // NOI18N
    
    // make sure string names are the same used in the tokenIds above
    public static final String IDENTIFIER_CATEGORY = "identifier"; // NOI18N
    public static final String WHITESPACE_CATEGORY = "whitespace"; // NOI18N
    public static final String COMMENT_CATEGORY = "comment"; // NOI18N
    public static final String KEYWORD_CATEGORY = "keyword"; // NOI18N
    public static final String KEYWORD_DIRECTIVE_CATEGORY = "keyword-directive"; // NOI18N
    public static final String ERROR_CATEGORY = "error"; // NOI18N
    public static final String NUMBER_CATEGORY = "number"; // NOI18N
    public static final String LITERAL_CATEGORY = "literal"; // NOI18N
    public static final String CHAR_CATEGORY = "character"; // NOI18N
    public static final String STRING_CATEGORY = "string"; // NOI18N
    public static final String SEPARATOR_CATEGORY = "separator"; // NOI18N
    public static final String OPERATOR_CATEGORY = "operator"; // NOI18N
    public static final String SPECIAL_CATEGORY = "special"; // NOI18N
    public static final String PREPROCESSOR_CATEGORY = "preprocessor"; // NOI18N
    public static final String PREPROCESSOR_KEYWORD_CATEGORY = "preprocessor-keyword"; // NOI18N
    public static final String PREPROCESSOR_KEYWORD_DIRECTIVE_CATEGORY = "preprocessor-keyword-directive"; // NOI18N
    public static final String PREPROCESSOR_IDENTIFIER_CATEGORY = "preprocessor-identifier"; // NOI18N
    public static final String PREPROCESSOR_USER_INCLUDE_CATEGORY = "preprocessor-user-include-literal"; // NOI18N
    public static final String PREPROCESSOR_SYS_INCLUDE_CATEGORY = "preprocessor-system-include-literal"; // NOI18N
    public static final String PRAGMA_OMP_CATEGORY = "pragma-omp"; // NOI18N
    public static final String PRAGMA_OMP_KEYWORD_DIRECTIVE_CATEGORY = "pragma-omp-keyword-directive"; // NOI18N
  
    private final String fixedText;

    private final String primaryCategory;

    CppTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<CppTokenId> languageHeader;
    private static final Language<CppTokenId> languageC;
    private static final Language<CppTokenId> languageCpp;
    private static final Language<CppTokenId> languagePreproc;
    
    static {
        languageHeader = CppHierarchy.createHeaderLanguage();
        languageC = CppHierarchy.createCLanguage();
        languageCpp = CppHierarchy.createCppLanguage();
        languagePreproc = CppHierarchy.createPreprocLanguage();
    }

    public static Language<CppTokenId> languageC() {
        return languageC;
    }

    public static Language<CppTokenId> languageCpp() {
        return languageCpp;
    }
    
    public static Language<CppTokenId> languagePreproc() {
        return languagePreproc;
    }

    public static Language<CppTokenId> languageHeader() {
        return languageHeader;
    }
    
    private static final class CppHierarchy extends LanguageHierarchy<CppTokenId> {
        private final String mimeType;
        private CppHierarchy(String mimeType) {
            this.mimeType = mimeType;
        }

        private static Language<CppTokenId> createHeaderLanguage() {
            return new CppHierarchy(MIMENames.HEADER_MIME_TYPE).language();
        }
        
        private static Language<CppTokenId> createCppLanguage() {
            return new CppHierarchy(MIMENames.CPLUSPLUS_MIME_TYPE).language();
        }
        
        private static Language<CppTokenId> createCLanguage() {
            return new CppHierarchy(MIMENames.C_MIME_TYPE).language();
        }
        
        private static Language<CppTokenId> createPreprocLanguage() {
            return new CppHierarchy(MIMENames.PREPROC_MIME_TYPE).language();
        }
        
        @Override
        protected String mimeType() {
            return mimeType;
        }

        @Override
        protected Collection<CppTokenId> createTokenIds() {
            return EnumSet.allOf(CppTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<CppTokenId>> createTokenCategories() {
            Map<String,Collection<CppTokenId>> cats = new HashMap<String,Collection<CppTokenId>>();
            // Additional literals being a lexical error
            cats.put(ERROR_CATEGORY, EnumSet.of(
                CppTokenId.FLOAT_LITERAL_INVALID
            ));
            // Literals category
            EnumSet<CppTokenId> l = EnumSet.of(
                CppTokenId.INT_LITERAL,
                CppTokenId.LONG_LITERAL,
                CppTokenId.LONG_LONG_LITERAL,
                CppTokenId.FLOAT_LITERAL,
                CppTokenId.DOUBLE_LITERAL,
                CppTokenId.UNSIGNED_LITERAL,
                CppTokenId.CHAR_LITERAL,
                CppTokenId.RAW_STRING_LITERAL,
                CppTokenId.STRING_LITERAL
            );
            cats.put(LITERAL_CATEGORY, l);
        
            return cats;
        }

        @Override
        protected Lexer<CppTokenId> createLexer(LexerRestartInfo<CppTokenId> info) {
            if (MIMENames.PREPROC_MIME_TYPE.equals(this.mimeType)) {
                return new PreprocLexer(CndLexerUtilities.getDefatultFilter(true), info);
            } else if (MIMENames.C_MIME_TYPE.equals(this.mimeType)) {
                return new CppLexer(CndLexerUtilities.getDefatultFilter(false), info);
            } else { // for header and C++
                return new CppLexer(CndLexerUtilities.getDefatultFilter(true), info);
            }
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<CppTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment and string literal
            switch (token.id()) {
                case BLOCK_COMMENT:
                    return LanguageEmbedding.create(DoxygenTokenId.language(), 2,
                            (token.partType() == PartType.COMPLETE) ? 2 : 0);
                case LINE_COMMENT:
                    return LanguageEmbedding.create(DoxygenTokenId.language(), 2, 0);
                case DOXYGEN_COMMENT:
                    return LanguageEmbedding.create(DoxygenTokenId.language(), 3,
                            (token.partType() == PartType.COMPLETE) ? 2 : 0);
                case DOXYGEN_LINE_COMMENT:
                    return LanguageEmbedding.create(DoxygenTokenId.language(), 3, 0);
                case RAW_STRING_LITERAL:
                    return LanguageEmbedding.create(CppStringTokenId.languageRawString(), 0, 0);
                case STRING_LITERAL:
                    return LanguageEmbedding.create(CppStringTokenId.languageDouble(), 0, 0);
                case CHAR_LITERAL:
                    return LanguageEmbedding.create(CppStringTokenId.languageSingle(), 0, 0);
                case PREPROCESSOR_DIRECTIVE:
                    return LanguageEmbedding.create(languagePreproc, 0, 0);
            }

            if (!CndLexerEmbeddingProviders.providers.isEmpty()) {
                for (org.netbeans.cnd.spi.lexer.CndLexerLanguageEmbeddingProvider provider : CndLexerEmbeddingProviders.providers) {
                    LanguageEmbedding<?> embedding = provider.createEmbedding(token, languagePath, inputAttributes);
                    if (embedding != null) {
                        return embedding;
                    }
                }
            }
            return null; // No embedding
        }
        
        private final static class CndLexerEmbeddingProviders {
            private final static Collection<? extends CndLexerLanguageEmbeddingProvider> providers = Lookups.forPath(CndLexerLanguageEmbeddingProvider.REGISTRATION_PATH).lookupAll(CndLexerLanguageEmbeddingProvider.class);
        }

    }
}
