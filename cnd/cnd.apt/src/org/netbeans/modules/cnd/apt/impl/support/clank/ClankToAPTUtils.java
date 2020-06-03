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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import org.clang.basic.IdentifierInfo;
import org.clang.basic.tok;
import org.clang.lex.Preprocessor;
import org.clang.lex.Token;
import org.clank.support.Casts;
import org.clank.support.Native;
import org.clank.support.aliases.char$ptr;
import org.clank.support.aliases.char$ptr$array;
import org.clank.support.char$ptr$CharSequence;
import org.llvm.adt.SmallString;
import org.llvm.adt.StringMapEntryBase;
import org.llvm.adt.StringRef;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.openide.util.CharSequences;

/**
 *
 */
public final class ClankToAPTUtils {

    private ClankToAPTUtils() {
    }

    static int convertClankToAPTTokenKind(/*ushort*/char clankTokenKind) {
        switch (clankTokenKind) {
            //<editor-fold defaultstate="collapsed" desc="long cases">
            // These define members of the tok::* namespace.
            case tok.TokenKind.unknown: // Not a token.
                // FIXME: consider broken token as comment to have better parser recovery
                return APTTokenTypes.COMMENT;
            case tok.TokenKind.eof: // End of file.
                return APTTokenTypes.EOF;
            case tok.TokenKind.eod: // End of preprocessing directive (end of line inside a
            // directive).
            case tok.TokenKind.code_completion: // Code completion marker
/*REMOVED in 3.6            case tok.TokenKind.cxx_defaultarg_end: // C++ default argument end marker*/
                assert false : tok.getTokenName(clankTokenKind) + " [" + Native.$toString(tok.getPunctuatorSpelling(clankTokenKind)) + "]";
                return APTTokenTypes.COMMENT;
            // C99 6.4.9: Comments.
            case tok.TokenKind.comment: // Comment (only in -E -C[C] mode)
                return APTTokenTypes.COMMENT;
            // C99 6.4.2: Identifiers.
            case tok.TokenKind.identifier: // abcde123
            case tok.TokenKind.raw_identifier: // Used only in raw lexing mode.
                return APTTokenTypes.IDENT;
            // C99 6.4.4.1: Integer Constants
            // C99 6.4.4.2: Floating Constants
            case tok.TokenKind.numeric_constant: // 0x123
                return APTTokenTypes.DECIMALINT;
            // C99 6.4.4: Character Constants
            case tok.TokenKind.char_constant: // 'a'
            case tok.TokenKind.wide_char_constant: // L'b'
            // C++1z Character Constants
/*3.6 NEW*/ case tok.TokenKind.utf8_char_constant: // u8'a'
            // C++11 Character Constants
            case tok.TokenKind.utf16_char_constant: // u'a'
            case tok.TokenKind.utf32_char_constant: // U'a'
                return APTTokenTypes.CHAR_LITERAL;

            // C99 6.4.5: String Literals.
            case tok.TokenKind.string_literal: // "foo"
            case tok.TokenKind.wide_string_literal: // L"foo"
            case tok.TokenKind.angle_string_literal: // <foo>

            // C++11 String Literals.
            case tok.TokenKind.utf8_string_literal: // u8"foo"
            case tok.TokenKind.utf16_string_literal: // u"foo"
            case tok.TokenKind.utf32_string_literal: // U"foo"
                return APTTokenTypes.STRING_LITERAL;
            // C99 6.4.6: Punctuators.
            case tok.TokenKind.l_square:
                return APTTokenTypes.LSQUARE;
            case tok.TokenKind.r_square:
                return APTTokenTypes.RSQUARE;
            case tok.TokenKind.l_paren:
                return APTTokenTypes.LPAREN;
            case tok.TokenKind.r_paren:
                return APTTokenTypes.RPAREN;
            case tok.TokenKind.l_brace:
                return APTTokenTypes.LCURLY;
            case tok.TokenKind.r_brace:
                return APTTokenTypes.RCURLY;
            case tok.TokenKind.period:
                return APTTokenTypes.DOT;
            case tok.TokenKind.ellipsis:
                return APTTokenTypes.ELLIPSIS;
            case tok.TokenKind.amp:
                return APTTokenTypes.AMPERSAND;
            case tok.TokenKind.ampamp:
                return APTTokenTypes.AND;
            case tok.TokenKind.ampequal:
                return APTTokenTypes.BITWISEANDEQUAL;
            case tok.TokenKind.star:
                return APTTokenTypes.STAR;
            case tok.TokenKind.starequal:
                return APTTokenTypes.TIMESEQUAL;
            case tok.TokenKind.plus:
                return APTTokenTypes.PLUS;
            case tok.TokenKind.plusplus:
                return APTTokenTypes.PLUSPLUS;
            case tok.TokenKind.plusequal:
                return APTTokenTypes.PLUSEQUAL;
            case tok.TokenKind.minus:
                return APTTokenTypes.MINUS;
            case tok.TokenKind.arrow:
                return APTTokenTypes.POINTERTO;
            case tok.TokenKind.minusminus:
                return APTTokenTypes.MINUSMINUS;
            case tok.TokenKind.minusequal:
                return APTTokenTypes.MINUSEQUAL;
            case tok.TokenKind.tilde:
                return APTTokenTypes.TILDE;
            case tok.TokenKind.exclaim:
                return APTTokenTypes.NOT;
            case tok.TokenKind.exclaimequal:
                return APTTokenTypes.NOTEQUAL;
            case tok.TokenKind.slash:
                return APTTokenTypes.DIVIDE;
            case tok.TokenKind.slashequal:
                return APTTokenTypes.DIVIDEEQUAL;
            case tok.TokenKind.percent:
                return APTTokenTypes.MOD;
            case tok.TokenKind.percentequal:
                return APTTokenTypes.MODEQUAL;
            case tok.TokenKind.less:
                return APTTokenTypes.LESSTHAN;
            case tok.TokenKind.lessless:
                return APTTokenTypes.SHIFTLEFT;
            case tok.TokenKind.lessequal:
                return APTTokenTypes.LESSTHANOREQUALTO;
            case tok.TokenKind.lesslessequal:
                return APTTokenTypes.SHIFTLEFTEQUAL;
            case tok.TokenKind.greater:
                return APTTokenTypes.GREATERTHAN;
            case tok.TokenKind.greatergreater:
                return APTTokenTypes.SHIFTRIGHT;
            case tok.TokenKind.greaterequal:
                return APTTokenTypes.GREATERTHANOREQUALTO;
            case tok.TokenKind.greatergreaterequal:
                return APTTokenTypes.SHIFTRIGHTEQUAL;
            case tok.TokenKind.caret:
                return APTTokenTypes.BITWISEXOR;
            case tok.TokenKind.caretequal:
                return APTTokenTypes.BITWISEXOREQUAL;
            case tok.TokenKind.pipe:
                return APTTokenTypes.BITWISEOR;
            case tok.TokenKind.pipepipe:
                return APTTokenTypes.OR;
            case tok.TokenKind.pipeequal:
                return APTTokenTypes.BITWISEOREQUAL;
            case tok.TokenKind.question:
                return APTTokenTypes.QUESTIONMARK;
            case tok.TokenKind.colon:
                return APTTokenTypes.COLON;
            case tok.TokenKind.semi:
                return APTTokenTypes.SEMICOLON;
            case tok.TokenKind.equal:
                return APTTokenTypes.ASSIGNEQUAL;
            case tok.TokenKind.equalequal:
                return APTTokenTypes.EQUAL;
            case tok.TokenKind.comma:
                return APTTokenTypes.COMMA;
            case tok.TokenKind.hash:
                return APTTokenTypes.SHARP;
            case tok.TokenKind.hashhash:
                return APTTokenTypes.DBL_SHARP;
            case tok.TokenKind.hashat:
                assert false : tok.getTokenName(clankTokenKind) + " [" + Native.$toString(tok.getPunctuatorSpelling(clankTokenKind)) + "]";
                return APTTokenTypes.COMMENT;
            // C++ Support
            case tok.TokenKind.periodstar:
                return APTTokenTypes.DOTMBR;
            case tok.TokenKind.arrowstar:
                return APTTokenTypes.POINTERTOMBR;
            case tok.TokenKind.coloncolon:
                return APTTokenTypes.SCOPE;

            // Objective C support.
            case tok.TokenKind.at:
                return APTTokenTypes.AT;
            // CUDA support.
            case tok.TokenKind.lesslessless:
                assert false : tok.getTokenName(clankTokenKind) + " [" + Native.$toString(tok.getPunctuatorSpelling(clankTokenKind)) + "]";
                return APTTokenTypes.COMMENT;
            case tok.TokenKind.greatergreatergreater:
                assert false : tok.getTokenName(clankTokenKind) + " [" + Native.$toString(tok.getPunctuatorSpelling(clankTokenKind)) + "]";
                return APTTokenTypes.COMMENT;
            case tok.TokenKind.caretcaret:
                assert false : tok.getTokenName(clankTokenKind) + " [" + Native.$toString(tok.getPunctuatorSpelling(clankTokenKind)) + "]";
                return APTTokenTypes.COMMENT;
            // C99 6.4.1: Keywords.  These turn into kw_* tokens.
            // Flags allowed:
            //   KEYALL   - This is a keyword in all variants of C and C++, or it
            //              is a keyword in the implementation namespace that should
            //              always be treated as a keyword
            //   KEYC99   - This is a keyword introduced to C in C99
            //   KEYC11   - This is a keyword introduced to C in C11
            //   KEYCXX   - This is a C++ keyword, or a C++-specific keyword in the
            //              implementation namespace
            //   KEYNOCXX - This is a keyword in every non-C++ dialect.
            //   KEYCXX11 - This is a C++ keyword introduced to C++ in C++11
            //   KEYGNU   - This is a keyword if GNU extensions are enabled
            //   KEYMS    - This is a keyword if Microsoft extensions are enabled
            //   KEYNOMS  - This is a keyword that must never be enabled under
            //              Microsoft mode
            //   KEYOPENCL  - This is a keyword in OpenCL
            //   KEYALTIVEC - This is a keyword in AltiVec
            //   KEYBORLAND - This is a keyword if Borland extensions are enabled
            //   BOOLSUPPORT - This is a keyword if 'bool' is a built-in type
            //   WCHARSUPPORT - This is a keyword if 'wchar_t' is a built-in type
            //
            case tok.TokenKind.kw_auto:
                return APTTokenTypes.LITERAL_auto;
            case tok.TokenKind.kw_break:
                return APTTokenTypes.LITERAL_break;
            case tok.TokenKind.kw_case:
                return APTTokenTypes.LITERAL_case;
            case tok.TokenKind.kw_char:
                return APTTokenTypes.LITERAL_char;
            case tok.TokenKind.kw_const:
                return APTTokenTypes.LITERAL_const;
            case tok.TokenKind.kw_continue:
                return APTTokenTypes.LITERAL_continue;
            case tok.TokenKind.kw_default:
                return APTTokenTypes.LITERAL_default;
            case tok.TokenKind.kw_do:
                return APTTokenTypes.LITERAL_do;
            case tok.TokenKind.kw_double:
                return APTTokenTypes.LITERAL_double;
            case tok.TokenKind.kw_else:
                return APTTokenTypes.LITERAL_else;
            case tok.TokenKind.kw_enum:
                return APTTokenTypes.LITERAL_enum;
            case tok.TokenKind.kw_extern:
                return APTTokenTypes.LITERAL_extern;
            case tok.TokenKind.kw_float:
                return APTTokenTypes.LITERAL_float;
            case tok.TokenKind.kw_for:
                return APTTokenTypes.LITERAL_for;
            case tok.TokenKind.kw_goto:
                return APTTokenTypes.LITERAL_goto;
            case tok.TokenKind.kw_if:
                return APTTokenTypes.LITERAL_if;
            case tok.TokenKind.kw_inline:
                return APTTokenTypes.LITERAL_inline;
            case tok.TokenKind.kw_int:
                return APTTokenTypes.LITERAL_int;
            case tok.TokenKind.kw_long:
                return APTTokenTypes.LITERAL_long;
            case tok.TokenKind.kw_register:
                return APTTokenTypes.LITERAL_register;
            case tok.TokenKind.kw_restrict:
                return APTTokenTypes.LITERAL_restrict;
            case tok.TokenKind.kw_return:
                return APTTokenTypes.LITERAL_return;
            case tok.TokenKind.kw_short:
                return APTTokenTypes.LITERAL_short;
            case tok.TokenKind.kw_signed:
                return APTTokenTypes.LITERAL_signed;
            case tok.TokenKind.kw_sizeof:
                return APTTokenTypes.LITERAL_sizeof;
            case tok.TokenKind.kw_static:
                return APTTokenTypes.LITERAL_static;
            case tok.TokenKind.kw_struct:
                return APTTokenTypes.LITERAL_struct;
            case tok.TokenKind.kw_switch:
                return APTTokenTypes.LITERAL_switch;
            case tok.TokenKind.kw_typedef:
                return APTTokenTypes.LITERAL_typedef;
            case tok.TokenKind.kw_union:
                return APTTokenTypes.LITERAL_union;
            case tok.TokenKind.kw_unsigned:
                return APTTokenTypes.LITERAL_unsigned;
            case tok.TokenKind.kw_void:
                return APTTokenTypes.LITERAL_void;
            case tok.TokenKind.kw_volatile:
                return APTTokenTypes.LITERAL_volatile;
            case tok.TokenKind.kw_while:
                return APTTokenTypes.LITERAL_while;
            case tok.TokenKind.kw__Alignas:
                return APTTokenTypes.LITERAL__Alignas;
            case tok.TokenKind.kw__Alignof:
                return APTTokenTypes.LITERAL__Alignof;
            case tok.TokenKind.kw__Atomic:
                return APTTokenTypes.LITERAL__Atomic;
            case tok.TokenKind.kw__Bool:
                return APTTokenTypes.LITERAL__Bool;
            case tok.TokenKind.kw__Complex:
                return APTTokenTypes.LITERAL__Complex;
            case tok.TokenKind.kw__Generic:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL__Generic;
            case tok.TokenKind.kw__Imaginary:
                return APTTokenTypes.LITERAL__Imaginary;
            case tok.TokenKind.kw__Noreturn:
                return APTTokenTypes.LITERAL__Noreturn;
            case tok.TokenKind.kw__Static_assert:
                return APTTokenTypes.LITERAL__Static_assert;
            case tok.TokenKind.kw__Thread_local:
                return APTTokenTypes.LITERAL__Thread_local;
            case tok.TokenKind.kw___func__:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL___func__;
            case tok.TokenKind.kw___objc_yes:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL___objc_yes;
            case tok.TokenKind.kw___objc_no:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL___objc_no;

            // C++ 2.11p1: Keywords.
            case tok.TokenKind.kw_asm:
                return APTTokenTypes.LITERAL_asm;
            case tok.TokenKind.kw_bool:
                return APTTokenTypes.LITERAL_bool;
            case tok.TokenKind.kw_catch:
                return APTTokenTypes.LITERAL_catch;
            case tok.TokenKind.kw_class:
                return APTTokenTypes.LITERAL_class;
            case tok.TokenKind.kw_const_cast:
                return APTTokenTypes.LITERAL_const_cast;
            case tok.TokenKind.kw_delete:
                return APTTokenTypes.LITERAL_delete;
            case tok.TokenKind.kw_dynamic_cast:
                return APTTokenTypes.LITERAL_dynamic_cast;
            case tok.TokenKind.kw_explicit:
                return APTTokenTypes.LITERAL_explicit;
            case tok.TokenKind.kw_export:
                return APTTokenTypes.LITERAL_export;
            case tok.TokenKind.kw_false:
                return APTTokenTypes.LITERAL_false;
            case tok.TokenKind.kw_friend:
                return APTTokenTypes.LITERAL_friend;
            case tok.TokenKind.kw_mutable:
                return APTTokenTypes.LITERAL_mutable;
            case tok.TokenKind.kw_namespace:
                return APTTokenTypes.LITERAL_namespace;
            case tok.TokenKind.kw_new:
                return APTTokenTypes.LITERAL_new;
            case tok.TokenKind.kw_operator:
                return APTTokenTypes.LITERAL_OPERATOR;
            case tok.TokenKind.kw_private:
                return APTTokenTypes.LITERAL_private;
            case tok.TokenKind.kw_protected:
                return APTTokenTypes.LITERAL_protected;
            case tok.TokenKind.kw_public:
                return APTTokenTypes.LITERAL_public;
            case tok.TokenKind.kw_reinterpret_cast:
                return APTTokenTypes.LITERAL_reinterpret_cast;
            case tok.TokenKind.kw_static_cast:
                return APTTokenTypes.LITERAL_static_cast;
            case tok.TokenKind.kw_template:
                return APTTokenTypes.LITERAL_template;
            case tok.TokenKind.kw_this:
                return APTTokenTypes.LITERAL_this;
            case tok.TokenKind.kw_throw:
                return APTTokenTypes.LITERAL_throw;
            case tok.TokenKind.kw_true:
                return APTTokenTypes.LITERAL_true;
            case tok.TokenKind.kw_try:
                return APTTokenTypes.LITERAL_try;
            case tok.TokenKind.kw_typename:
                return APTTokenTypes.LITERAL_typename;
            case tok.TokenKind.kw_typeid:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL_typeid;
            case tok.TokenKind.kw_using:
                return APTTokenTypes.LITERAL_using;
            case tok.TokenKind.kw_virtual:
                return APTTokenTypes.LITERAL_virtual;
            case tok.TokenKind.kw_wchar_t:
                return APTTokenTypes.LITERAL_wchar_t;

            // C++11 keywords
            case tok.TokenKind.kw_alignas:
                return APTTokenTypes.LITERAL_alignas;
            case tok.TokenKind.kw_alignof:
                return APTTokenTypes.LITERAL_alignof;
            case tok.TokenKind.kw_char16_t:
                return APTTokenTypes.LITERAL_char16_t;
            case tok.TokenKind.kw_char32_t:
                return APTTokenTypes.LITERAL_char32_t;
            case tok.TokenKind.kw_constexpr:
                return APTTokenTypes.LITERAL_constexpr;
            case tok.TokenKind.kw_decltype:
                return APTTokenTypes.LITERAL_decltype;
            case tok.TokenKind.kw_noexcept:
                return APTTokenTypes.LITERAL_noexcept;
            case tok.TokenKind.kw_nullptr:
                return APTTokenTypes.LITERAL_nullptr;
            case tok.TokenKind.kw_static_assert:
                return APTTokenTypes.LITERAL_static_assert;
            case tok.TokenKind.kw_thread_local:
                return APTTokenTypes.LITERAL_thread_local;
/*3.8 NEW*/ case tok.TokenKind.kw_concept:
                return APTTokenTypes.LITERAL_concept;
/*3.8 NEW*/ case tok.TokenKind.kw_requires:
                return APTTokenTypes.LITERAL_requires;
/*3.8 NEW*/ case tok.TokenKind.kw_co_await:
                return APTTokenTypes.LITERAL_co_await;
/*3.8 NEW*/ case tok.TokenKind.kw_co_return:
                return APTTokenTypes.LITERAL_co_return;
/*3.8 NEW*/ case tok.TokenKind.kw_co_yield:
                return APTTokenTypes.LITERAL_co_yield;

            // GNU Extensions (in impl-reserved namespace)
            case tok.TokenKind.kw__Decimal32:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL__Decimal32;
            case tok.TokenKind.kw__Decimal64:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL__Decimal64;
            case tok.TokenKind.kw__Decimal128:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL__Decimal128;
            case tok.TokenKind.kw___null:
                return APTTokenTypes.LITERAL___null;
            case tok.TokenKind.kw___alignof:
                return APTTokenTypes.LITERAL___alignof;
            case tok.TokenKind.kw___attribute:
                return APTTokenTypes.LITERAL___attribute;
            case tok.TokenKind.kw___builtin_choose_expr:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___builtin_choose_expr;
            case tok.TokenKind.kw___builtin_offsetof:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___builtin_offsetof;
            case tok.TokenKind.kw___builtin_types_compatible_p:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___builtin_types_compatible_p;
            case tok.TokenKind.kw___builtin_va_arg:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___builtin_va_arg;
            case tok.TokenKind.kw___extension__:
                return APTTokenTypes.LITERAL___extension__;
            case tok.TokenKind.kw___float128:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___float128;
            case tok.TokenKind.kw___imag:
                return APTTokenTypes.LITERAL___imag;
            case tok.TokenKind.kw___int128:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___int128;
            case tok.TokenKind.kw___label__:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___label__;
            case tok.TokenKind.kw___real:
                return APTTokenTypes.LITERAL___real;
            case tok.TokenKind.kw___thread:
                return APTTokenTypes.LITERAL___thread;
            case tok.TokenKind.kw___FUNCTION__:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___FUNCTION__;
            case tok.TokenKind.kw___PRETTY_FUNCTION__:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___PRETTY_FUNCTION__;
/*3.8 NEW*/ case tok.TokenKind.kw___auto_type:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___auto_type;

            // GNU Extensions (outside impl-reserved namespace)
            case tok.TokenKind.kw_typeof:
                return APTTokenTypes.LITERAL_typeof;

            // MS Extensions
            case tok.TokenKind.kw___FUNCDNAME__:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___FUNCDNAME__;
/*3.6 NEW*/ case tok.TokenKind.kw___FUNCSIG__:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___FUNCSIG__;
            case tok.TokenKind.kw_L__FUNCTION__:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL_L__FUNCTION__;
            case tok.TokenKind.kw___is_interface_class:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___is_interface_class;
            case tok.TokenKind.kw___is_sealed:
                return APTTokenTypes.IDENT; //APTTokenTypes.LITERAL___is_sealed;

            // MSVC12.0 / VS2013 Type Traits
/*3.6 NEW*/ case tok.TokenKind.kw___is_destructible:
            /*3.6 NEW*/ case tok.TokenKind.kw___is_nothrow_destructible:
            /*3.6 NEW*/ case tok.TokenKind.kw___is_nothrow_assignable:
            /*3.6 NEW*/ case tok.TokenKind.kw___is_constructible:
            /*3.6 NEW*/ case tok.TokenKind.kw___is_nothrow_constructible:
            /*3.9 NEW*/ case tok.TokenKind.kw___is_assignable:
                return APTTokenTypes.IDENT;

            // GNU and MS Type Traits
            case tok.TokenKind.kw___has_nothrow_assign:
            case tok.TokenKind.kw___has_nothrow_move_assign:
            case tok.TokenKind.kw___has_nothrow_copy:
            case tok.TokenKind.kw___has_nothrow_constructor:
            case tok.TokenKind.kw___has_trivial_assign:
            case tok.TokenKind.kw___has_trivial_move_assign:
            case tok.TokenKind.kw___has_trivial_copy:
            case tok.TokenKind.kw___has_trivial_constructor:
            case tok.TokenKind.kw___has_trivial_move_constructor:
            case tok.TokenKind.kw___has_trivial_destructor:
            case tok.TokenKind.kw___has_virtual_destructor:
            case tok.TokenKind.kw___is_abstract:
            case tok.TokenKind.kw___is_base_of:
            case tok.TokenKind.kw___is_class:
            case tok.TokenKind.kw___is_convertible_to:
            case tok.TokenKind.kw___is_empty:
            case tok.TokenKind.kw___is_enum:
            case tok.TokenKind.kw___is_final:
            // Tentative name - there's no implementation of std::is_literal_type yet.
            case tok.TokenKind.kw___is_literal:
            // Name for GCC 4.6 compatibility - people have already written libraries using
            // this name unfortunately.
/*REMOVED in 3.6            case tok.TokenKind.kw___is_literal_type: */
            case tok.TokenKind.kw___is_pod:
            case tok.TokenKind.kw___is_polymorphic:
            case tok.TokenKind.kw___is_trivial:
            case tok.TokenKind.kw___is_union:

            // Clang-only C++ Type Traits
            case tok.TokenKind.kw___is_trivially_constructible:
            case tok.TokenKind.kw___is_trivially_copyable:
            case tok.TokenKind.kw___is_trivially_assignable:
            case tok.TokenKind.kw___underlying_type:

            // Embarcadero Expression Traits
            case tok.TokenKind.kw___is_lvalue_expr:
            case tok.TokenKind.kw___is_rvalue_expr:

            // Embarcadero Unary Type Traits
            case tok.TokenKind.kw___is_arithmetic:
            case tok.TokenKind.kw___is_floating_point:
            case tok.TokenKind.kw___is_integral:
            case tok.TokenKind.kw___is_complete_type:
            case tok.TokenKind.kw___is_void:
            case tok.TokenKind.kw___is_array:
            case tok.TokenKind.kw___is_function:
            case tok.TokenKind.kw___is_reference:
            case tok.TokenKind.kw___is_lvalue_reference:
            case tok.TokenKind.kw___is_rvalue_reference:
            case tok.TokenKind.kw___is_fundamental:
            case tok.TokenKind.kw___is_object:
            case tok.TokenKind.kw___is_scalar:
            case tok.TokenKind.kw___is_compound:
            case tok.TokenKind.kw___is_pointer:
            case tok.TokenKind.kw___is_member_object_pointer:
            case tok.TokenKind.kw___is_member_function_pointer:
            case tok.TokenKind.kw___is_member_pointer:
            case tok.TokenKind.kw___is_const:
            case tok.TokenKind.kw___is_volatile:
            case tok.TokenKind.kw___is_standard_layout:
            case tok.TokenKind.kw___is_signed:
            case tok.TokenKind.kw___is_unsigned:

            // Embarcadero Binary Type Traits
            case tok.TokenKind.kw___is_same:
            case tok.TokenKind.kw___is_convertible:
            case tok.TokenKind.kw___array_rank:
            case tok.TokenKind.kw___array_extent:

            // Apple Extension.
            case tok.TokenKind.kw___private_extern__:
            case tok.TokenKind.kw___module_private__:
                return APTTokenTypes.IDENT;

            // Microsoft Extension.
            case tok.TokenKind.kw___declspec:
                return APTTokenTypes.LITERAL___declspec;
            case tok.TokenKind.kw___cdecl:
                return APTTokenTypes.LITERAL___cdecl;
            case tok.TokenKind.kw___stdcall:
                return APTTokenTypes.LITERAL___stdcall;
            case tok.TokenKind.kw___fastcall:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL___fastcall;
            case tok.TokenKind.kw___thiscall:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL___thiscall;
/*3.6 NEW*/ case tok.TokenKind.kw___vectorcall:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL___vectorcall;
            case tok.TokenKind.kw___forceinline:
                return APTTokenTypes.LITERAL___forceinline;
            case tok.TokenKind.kw___unaligned:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL___unaligned;
/*3.6 NEW*/ case tok.TokenKind.kw___super:
                return APTTokenTypes.IDENT;//APTTokenTypes.LITERAL___super;

            // OpenCL-specific keywords
            case tok.TokenKind.kw___global:
            case tok.TokenKind.kw___local:
            case tok.TokenKind.kw___constant:
            case tok.TokenKind.kw___private:
            /*3.6 NEW*/ case tok.TokenKind.kw___generic:
            case tok.TokenKind.kw___kernel:
            case tok.TokenKind.kw___read_only:
            case tok.TokenKind.kw___write_only:
            case tok.TokenKind.kw___read_write:
            case tok.TokenKind.kw___builtin_astype:
            case tok.TokenKind.kw_vec_step:
/*3.9 NEW*/ case tok.TokenKind.kw_image1d_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image1d_array_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image1d_buffer_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image2d_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image2d_array_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image2d_depth_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image2d_array_depth_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image2d_msaa_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image2d_array_msaa_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image2d_msaa_depth_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image2d_array_msaa_depth_t:
/*3.9 NEW*/ case tok.TokenKind.kw_image3d_t:
/*3.8 NEW*/ case tok.TokenKind.kw___builtin_omp_required_simd_align:
/*3.8 NEW*/ case tok.TokenKind.kw_pipe:
            /* REMOVED in 3.6
            case tok.TokenKind.kw_image1d_t:
            case tok.TokenKind.kw_image1d_array_t:
            case tok.TokenKind.kw_image1d_buffer_t:
            case tok.TokenKind.kw_image2d_t:
            case tok.TokenKind.kw_image2d_array_t:
            case tok.TokenKind.kw_image3d_t:
            case tok.TokenKind.kw_sampler_t:
            case tok.TokenKind.kw_event_t:
             */
            // Borland Extensions.
            case tok.TokenKind.kw___pascal:

            // Altivec Extension.
            case tok.TokenKind.kw___vector:
            case tok.TokenKind.kw___pixel:
            /*3.6 NEW*/ case tok.TokenKind.kw___bool:

            // OpenCL Extension.
            case tok.TokenKind.kw_half:

            // Objective-C ARC keywords.
            case tok.TokenKind.kw___bridge:
            case tok.TokenKind.kw___bridge_transfer:
            case tok.TokenKind.kw___bridge_retained:
            case tok.TokenKind.kw___bridge_retain:
/*3.8 NEW*/ case tok.TokenKind.kw___covariant:
/*3.8 NEW*/ case tok.TokenKind.kw___contravariant:
/*3.8 NEW*/ case tok.TokenKind.kw___kindof:
/*3.8 NEW*/ case tok.TokenKind.kw__Nonnull:
/*3.8 NEW*/ case tok.TokenKind.kw__Nullable:
/*3.8 NEW*/ case tok.TokenKind.kw__Null_unspecified:

            // Microsoft extensions which should be disabled in strict conformance mode
            case tok.TokenKind.kw___ptr64:
            case tok.TokenKind.kw___ptr32:
            case tok.TokenKind.kw___sptr:
            case tok.TokenKind.kw___uptr:
            case tok.TokenKind.kw___w64:
            case tok.TokenKind.kw___uuidof:
            case tok.TokenKind.kw___try:
            case tok.TokenKind.kw___finally:
            case tok.TokenKind.kw___leave:
            case tok.TokenKind.kw___int64:
            case tok.TokenKind.kw___if_exists:
            case tok.TokenKind.kw___if_not_exists:
            case tok.TokenKind.kw___single_inheritance:
            case tok.TokenKind.kw___multiple_inheritance:
            case tok.TokenKind.kw___virtual_inheritance:
            case tok.TokenKind.kw___interface:
                return APTTokenTypes.IDENT;

            // Clang Extensions.
            case tok.TokenKind.kw___builtin_convertvector:
            case tok.TokenKind.kw___builtin_available:

            // Clang-specific keywords enabled only in testing.
            case tok.TokenKind.kw___unknown_anytype:
                return APTTokenTypes.IDENT;

            // TODO: What to do about context-sensitive keywords like:
            //       bycopy/byref/in/inout/oneway/out?
            case tok.TokenKind.annot_cxxscope: // annotation for a C++ scope spec, e.g. "::foo::bar::"
            case tok.TokenKind.annot_typename: // annotation for a C typedef name, a C++ (possibly
            // qualified) typename, e.g. "foo::MyClass", or
            // template-id that names a type ("std::vector<int>")
            case tok.TokenKind.annot_template_id: // annotation for a C++ template-id that names a
            // function template specialization (not a type),
            // e.g., "std::swap<int>"
            case tok.TokenKind.annot_primary_expr: // annotation for a primary expression
            case tok.TokenKind.annot_decltype: // annotation for a decltype expression,
            // e.g., "decltype(foo.bar())"

            // Annotation for #pragma unused(...)
            // For each argument inside the parentheses the pragma handler will produce
            // one 'pragma_unused' annotation token followed by the argument token.
            case tok.TokenKind.annot_pragma_unused:

            // Annotation for #pragma GCC visibility...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_vis:

            // Annotation for #pragma pack...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_pack:

            // Annotation for #pragma clang __debug parser_crash...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_parser_crash:

            // Annotation for #pragma clang __debug captured...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_captured:

            // Annotation for #pragma ms_struct...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_msstruct:

            // Annotation for #pragma align...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_align:

            // Annotation for #pragma weak id
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_weak:

            // Annotation for #pragma weak id = id
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_weakalias:

            // Annotation for #pragma redefine_extname...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_redefine_extname:

            // Annotation for #pragma STDC FP_CONTRACT...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_fp_contract:

            // Annotation for #pragma pointers_to_members...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
/*3.6 NEW*/ case tok.TokenKind.annot_pragma_ms_pointers_to_members:

            // Annotation for #pragma vtordisp...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
/*3.6 NEW*/ case tok.TokenKind.annot_pragma_ms_vtordisp:

            // Annotation for all microsoft #pragmas...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
/*3.6 NEW*/ case tok.TokenKind.annot_pragma_ms_pragma:

            // Annotation for #pragma OPENCL EXTENSION...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_opencl_extension:

            // Annotations for OpenMP pragma directives - #pragma omp ...
            // The lexer produces these so that they only take effect when the parser
            // handles #pragma omp ... directives.
            case tok.TokenKind.annot_pragma_openmp:
            case tok.TokenKind.annot_pragma_openmp_end:

            // Annotations for loop pragma directives #pragma clang loop ...
            // The lexer produces these so that they only take effect when the parser
            // handles #pragma loop ... directives.
/*3.6 NEW*/ case tok.TokenKind.annot_pragma_loop_hint:

            // Annotation for module import translated from #include etc.
            case tok.TokenKind.annot_module_include:
/*3.6 NEW*/ case tok.TokenKind.annot_module_begin:
/*3.6 NEW*/ case tok.TokenKind.annot_module_end:
            case tok.TokenKind.NUM_TOKENS:
                assert false : tok.getTokenName(clankTokenKind) + " [" + Native.$toString(tok.getPunctuatorSpelling(clankTokenKind)) + "]";
            //</editor-fold>
        }
        assert false : "non converted token [" + (int)clankTokenKind + "] " + Native.$toString(tok.getTokenName(clankTokenKind)) + 
                " [" + Native.$toString(tok.getPunctuatorSpelling(clankTokenKind)) + "] " + // NOI18N
                (clankTokenKind > 0 ? ("add case after " + Native.$toString(tok.getTokenName((char)(clankTokenKind-1)))) : ""); // NOI18N
        return APTTokenTypes.EOF;
    }

    public static CharSequence getIdentifierText(IdentifierInfo II) {
        assert II != null;
        StringMapEntryBase entry = II.getEntry();
        assert entry != null;
        return CharSequences.create(new ByteBasedCharSequence(entry.getKeyArray(), entry.getKeyArrayIndex(), entry.getKeyLength()));
    }

    public static CharSequence getTokenText(Token token, Preprocessor PP, SmallString spell) {
        // all remainings
        CharSequence textID;
        IdentifierInfo II = token.is(tok.TokenKind.raw_identifier) ? null : token.getIdentifierInfo();
        if (II != null) {
            textID = getIdentifierText(II);
        } else {
            textID = null;
            char$ptr SpellingData = null;
            int SpellingLen = 0;
            if (token.isLiteral()) {
                char$ptr literalData = token.getLiteralData();
                if (literalData == null) {
                    // i.e. the case of lazy calculated DATE and TIME based strings
                    StringRef spelling = PP.getSpelling(token, spell);
                    SpellingData = spelling.begin();
                    SpellingLen = spelling.size();
                    spell.set_size(0);
                } else {
                    SpellingData = literalData;
                    SpellingLen = token.getLength();
                }
            } else if (token.is(tok.TokenKind.raw_identifier)) {
                byte[] $CharPtrData = token.$CharPtrData();
                if ($CharPtrData != null) {
                    textID = CharSequences.create(new ByteBasedCharSequence($CharPtrData, token.$CharPtrDataIndex(), token.getLength()));
                } else {
                    SpellingData = token.getRawIdentifierData();
                    SpellingLen = token.getLength();
                }
            }
            if (textID == null) {
                if (SpellingData == null) {
                    StringRef spelling = PP.getSpelling(token, spell);
                    SpellingData = spelling.begin();
                    SpellingLen = spelling.size();
                    spell.set_size(0);
                }
                assert SpellingData != null : "" + token;
                if (SpellingData instanceof char$ptr$array) {
                    textID = CharSequences.create(new ByteBasedCharSequence(SpellingData.$array(), SpellingData.$index(), SpellingLen));
                } else if (SpellingData instanceof char$ptr$CharSequence) {
                    char$ptr$CharSequence cssd = (char$ptr$CharSequence) SpellingData;
                    int idx = cssd.$index();
                    CharSequence subseq = cssd.getCharSequence().subSequence(idx, idx + SpellingLen);
                    textID = CharSequences.create(subseq);                    
                } else {
                    textID = Casts.toCharSequence(SpellingData, SpellingLen);
                }
            }
        }
        assert textID != null : "" + token;
        return textID;
    }
    
    private static final class ByteBasedCharSequence implements CharSequence {
        private final byte buf[];
        private final int zeroIndex;
        private final int length;

        public ByteBasedCharSequence(byte[] buf, int start, int count) {
            this.buf = buf;
            this.zeroIndex = start;
            this.length = count;
        }
                        
        @Override
        public int length() {
            return length;
        }

        @Override
        public char charAt(int index) {
            return Casts.$char(buf[zeroIndex + index]);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new ByteBasedCharSequence(buf, zeroIndex + start, end-start);
        }
        
    }
}
