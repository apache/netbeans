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
package org.netbeans.modules.cnd.apt.support.lang;

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.SplitShiftRightTokenFilter;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

/**
 * Copied from Cpp11 filter
 * 
 */
public class APTGnuCpp14Filter extends APTBaseLanguageFilter {
    private final boolean is17;
    
    public APTGnuCpp14Filter(boolean is17) {
        super(false);
        this.is17 = is17;
        initialize();
    }
    
    @Override
    public TokenStream getFilteredStream(TokenStream origStream) {
        // FIXME: it's better to handle '>>' in grammar instead, but as we don't 
        // have real rules for expression hack lexer instead
        return new SplitShiftRightTokenFilter(super.getFilteredStream(origStream));
    }
    
    private void initialize() {
        // TODO clean up!
        filter("operator", APTTokenTypes.LITERAL_OPERATOR); // NOI18N
        filter("alignof", APTTokenTypes.LITERAL_alignof); // NOI18N
        filter("__alignof__", APTTokenTypes.LITERAL___alignof__); // NOI18N
        filter("typeof", APTTokenTypes.LITERAL_typeof); // NOI18N
        filter("__typeof", APTTokenTypes.LITERAL___typeof); // NOI18N
        filter("__typeof__", APTTokenTypes.LITERAL___typeof__); // NOI18N
        filter("template", APTTokenTypes.LITERAL_template); // NOI18N
        filter("typedef", APTTokenTypes.LITERAL_typedef); // NOI18N
        filter("enum", APTTokenTypes.LITERAL_enum); // NOI18N
        filter("namespace", APTTokenTypes.LITERAL_namespace); // NOI18N
        filter("extern", APTTokenTypes.LITERAL_extern); // NOI18N
        filter("inline", APTTokenTypes.LITERAL_inline); // NOI18N
        filter("_inline", APTTokenTypes.LITERAL__inline); // NOI18N
        filter("__inline", APTTokenTypes.LITERAL___inline); // NOI18N
        filter("__inline__", APTTokenTypes.LITERAL___inline__); // NOI18N
        filter("virtual", APTTokenTypes.LITERAL_virtual); // NOI18N
        filter("explicit", APTTokenTypes.LITERAL_explicit); // NOI18N
        filter("friend", APTTokenTypes.LITERAL_friend); // NOI18N
        filter("_stdcall", APTTokenTypes.LITERAL__stdcall); // NOI18N
        filter("__stdcall", APTTokenTypes.LITERAL___stdcall); // NOI18N
        filter("typename", APTTokenTypes.LITERAL_typename); // NOI18N
        filter("auto", APTTokenTypes.LITERAL_auto); // NOI18N
        filter("register", APTTokenTypes.LITERAL_register); // NOI18N
        filter("static", APTTokenTypes.LITERAL_static); // NOI18N
        filter("mutable", APTTokenTypes.LITERAL_mutable); // NOI18N
        filter("const", APTTokenTypes.LITERAL_const); // NOI18N
        filter("__const", APTTokenTypes.LITERAL___const); // NOI18N
        filter("__const__", APTTokenTypes.LITERAL___const__); // NOI18N
        filter("const_cast", APTTokenTypes.LITERAL_const_cast); // NOI18N
        filter("volatile", APTTokenTypes.LITERAL_volatile); // NOI18N
        filter("__volatile", APTTokenTypes.LITERAL___volatile); // NOI18N
        filter("__volatile__", APTTokenTypes.LITERAL___volatile__); // NOI18N
        filter("char", APTTokenTypes.LITERAL_char); // NOI18N
        filter("wchar_t", APTTokenTypes.LITERAL_wchar_t); // NOI18N
        filter("bool", APTTokenTypes.LITERAL_bool); // NOI18N
        filter("short", APTTokenTypes.LITERAL_short); // NOI18N
        filter("int", APTTokenTypes.LITERAL_int); // NOI18N
        filter("long", APTTokenTypes.LITERAL_long); // NOI18N
        filter("signed", APTTokenTypes.LITERAL_signed); // NOI18N
        filter("__signed", APTTokenTypes.LITERAL___signed); // NOI18N
        filter("__signed__", APTTokenTypes.LITERAL___signed__); // NOI18N
        filter("unsigned", APTTokenTypes.LITERAL_unsigned); // NOI18N
        filter("__unsigned__", APTTokenTypes.LITERAL___unsigned__); // NOI18N
        filter("float", APTTokenTypes.LITERAL_float); // NOI18N
        filter("double", APTTokenTypes.LITERAL_double); // NOI18N
        filter("void", APTTokenTypes.LITERAL_void); // NOI18N
        filter("_declspec", APTTokenTypes.LITERAL__declspec); // NOI18N
        filter("__declspec", APTTokenTypes.LITERAL___declspec); // NOI18N
        filter("class", APTTokenTypes.LITERAL_class); // NOI18N
        filter("struct", APTTokenTypes.LITERAL_struct); // NOI18N
        filter("union", APTTokenTypes.LITERAL_union); // NOI18N        
        filter("this", APTTokenTypes.LITERAL_this); // NOI18N
        filter("true", APTTokenTypes.LITERAL_true); // NOI18N
        filter("false", APTTokenTypes.LITERAL_false); // NOI18N
        filter("public", APTTokenTypes.LITERAL_public); // NOI18N
        filter("protected", APTTokenTypes.LITERAL_protected); // NOI18N
        filter("private", APTTokenTypes.LITERAL_private); // NOI18N
        filter("throw", APTTokenTypes.LITERAL_throw); // NOI18N
        filter("case", APTTokenTypes.LITERAL_case); // NOI18N
        filter("default", APTTokenTypes.LITERAL_default); // NOI18N
        filter("if", APTTokenTypes.LITERAL_if); // NOI18N
        filter("else", APTTokenTypes.LITERAL_else); // NOI18N
        filter("switch", APTTokenTypes.LITERAL_switch); // NOI18N
        filter("while", APTTokenTypes.LITERAL_while); // NOI18N
        filter("do", APTTokenTypes.LITERAL_do); // NOI18N
        filter("for", APTTokenTypes.LITERAL_for); // NOI18N
        filter("goto", APTTokenTypes.LITERAL_goto); // NOI18N
        filter("continue", APTTokenTypes.LITERAL_continue); // NOI18N
        filter("break", APTTokenTypes.LITERAL_break); // NOI18N
        filter("return", APTTokenTypes.LITERAL_return); // NOI18N
        filter("try", APTTokenTypes.LITERAL_try); // NOI18N
        filter("catch", APTTokenTypes.LITERAL_catch); // NOI18N
        filter("using", APTTokenTypes.LITERAL_using); // NOI18N
        filter("asm", APTTokenTypes.LITERAL_asm); // NOI18N
        filter("_asm", APTTokenTypes.LITERAL__asm); // NOI18N
        filter("__asm", APTTokenTypes.LITERAL___asm); // NOI18N
        filter("__asm__", APTTokenTypes.LITERAL___asm__); // NOI18N
        filter("sizeof", APTTokenTypes.LITERAL_sizeof); // NOI18N
        filter("dynamic_cast", APTTokenTypes.LITERAL_dynamic_cast); // NOI18N
        filter("static_cast", APTTokenTypes.LITERAL_static_cast); // NOI18N
        filter("reinterpret_cast", APTTokenTypes.LITERAL_reinterpret_cast); // NOI18N
        filter("new", APTTokenTypes.LITERAL_new); // NOI18N
        filter("_cdecl", APTTokenTypes.LITERAL__cdecl); // NOI18N
        filter("__cdecl", APTTokenTypes.LITERAL___cdecl); // NOI18N
        filter("_near", APTTokenTypes.LITERAL__near); // NOI18N
        filter("__near", APTTokenTypes.LITERAL___near); // NOI18N
        filter("_far", APTTokenTypes.LITERAL__far); // NOI18N
        filter("__far", APTTokenTypes.LITERAL___far); // NOI18N
        filter("__interrupt", APTTokenTypes.LITERAL___interrupt); // NOI18N
        filter("pascal", APTTokenTypes.LITERAL_pascal); // NOI18N
        filter("_pascal", APTTokenTypes.LITERAL__pascal); // NOI18N
        filter("__pascal", APTTokenTypes.LITERAL___pascal); // NOI18N
        filter("delete", APTTokenTypes.LITERAL_delete); // NOI18N
        filter("_int64", APTTokenTypes.LITERAL__int64); // NOI18N
        filter("__int64", APTTokenTypes.LITERAL___int64); // NOI18N
        filter("__w64", APTTokenTypes.LITERAL___w64); // NOI18N
        filter("__extension__", APTTokenTypes.LITERAL___extension__); // NOI18N
        filter("__attribute__", APTTokenTypes.LITERAL___attribute__); // NOI18N
        filter("__attribute", APTTokenTypes.LITERAL___attribute); // NOI18N
        filter("__restrict", APTTokenTypes.LITERAL___restrict); // NOI18N
        filter("__restrict__", APTTokenTypes.LITERAL___restrict__); // NOI18N
        filter("__complex__", APTTokenTypes.LITERAL___complex__); // NOI18N
        filter("__imag__", APTTokenTypes.LITERAL___imag); // NOI18N
        filter("__real__", APTTokenTypes.LITERAL___real); // NOI18N 
        filter("export", APTTokenTypes.LITERAL_export); // NOI18N
        filter("__thread", APTTokenTypes.LITERAL___thread); // NOI18N
        filter("__global", APTTokenTypes.LITERAL___global); // NOI18N
        filter("__hidden", APTTokenTypes.LITERAL___hidden); // NOI18N
        filter("__symbolic", APTTokenTypes.LITERAL___symbolic); // NOI18N
        filter("__decltype", APTTokenTypes.LITERAL___decltype); // NOI18N
        filter("__complex", APTTokenTypes.LITERAL___complex); // NOI18N
        filter("__forceinline", APTTokenTypes.LITERAL___forceinline); // NOI18N
        filter("__clrcall", APTTokenTypes.LITERAL___clrcall); // NOI18N
        filter("__try", APTTokenTypes.LITERAL___try); // NOI18N
        filter("__finally", APTTokenTypes.LITERAL___finally); // NOI18N
        filter("__null", APTTokenTypes.LITERAL___null); // NOI18N
        filter("__alignof", APTTokenTypes.LITERAL___alignof); // NOI18N
        filter("__is_class", APTTokenTypes.LITERAL___is_class); // NOI18N
        filter("__is_enum", APTTokenTypes.LITERAL___is_enum); // NOI18N
        filter("__is_pod", APTTokenTypes.LITERAL___is_pod); // NOI18N
        filter("__is_base_of", APTTokenTypes.LITERAL___is_base_of); // NOI18N
        filter("__has_trivial_constructor", APTTokenTypes.LITERAL___has_trivial_constructor); // NOI18N        
        filter("__has_nothrow_assign", APTTokenTypes.LITERAL___has_nothrow_assign); // NOI18N
        filter("__has_nothrow_copy", APTTokenTypes.LITERAL___has_nothrow_copy); // NOI18N
        filter("__has_nothrow_constructor", APTTokenTypes.LITERAL___has_nothrow_constructor); // NOI18N
        filter("__has_trivial_assign", APTTokenTypes.LITERAL___has_trivial_assign); // NOI18N
        filter("__has_trivial_copy", APTTokenTypes.LITERAL___has_trivial_copy); // NOI18N
        filter("__has_trivial_destructor", APTTokenTypes.LITERAL___has_trivial_destructor); // NOI18N
        filter("__has_virtual_destructor", APTTokenTypes.LITERAL___has_virtual_destructor); // NOI18N
        filter("__is_abstract", APTTokenTypes.LITERAL___is_abstract); // NOI18N
        filter("__is_empty", APTTokenTypes.LITERAL___is_empty); // NOI18N
        filter("__is_literal_type", APTTokenTypes.LITERAL___is_literal_type); // NOI18N
        filter("__is_polymorphic", APTTokenTypes.LITERAL___is_polymorphic); // NOI18N
        filter("__is_standard_layout", APTTokenTypes.LITERAL___is_standard_layout); // NOI18N
        filter("__is_trivial", APTTokenTypes.LITERAL___is_trivial); // NOI18N
        filter("__is_union", APTTokenTypes.LITERAL___is_union); // NOI18N
        filter("__underlying_type", APTTokenTypes.LITERAL___underlying_type); // NOI18N                
        filter("__builtin_va_list", APTTokenTypes.LITERAL___builtin_va_list); // NOI18N        
        
        // C++11
        filter("final", APTTokenTypes.LITERAL_final); // NOI18N
        filter("override", APTTokenTypes.LITERAL_override); // NOI18N
        filter("constexpr", APTTokenTypes.LITERAL_constexpr); // NOI18N
        filter("decltype", APTTokenTypes.LITERAL_decltype); // NOI18N
        filter("nullptr", APTTokenTypes.LITERAL_nullptr); // NOI18N
        filter("thread_local", APTTokenTypes.LITERAL_thread_local); // NOI18N
        filter("static_assert", APTTokenTypes.LITERAL_static_assert); // NOI18N
        filter("alignas", APTTokenTypes.LITERAL_alignas); // NOI18N
        filter("char16_t", APTTokenTypes.LITERAL_char16_t); // NOI18N
        filter("char32_t", APTTokenTypes.LITERAL_char32_t); // NOI18N
        filter("noexcept", APTTokenTypes.LITERAL_noexcept); // NOI18N
        if (is17) {
            // DO WE NEED NEW FILTER?
            // C++ 17
            filter("concept", APTTokenTypes.LITERAL_concept); // NOI18N
            filter("requires", APTTokenTypes.LITERAL_requires); // NOI18N
            filter("co_await", APTTokenTypes.LITERAL_co_await); // NOI18N
            filter("co_yield", APTTokenTypes.LITERAL_co_yield); // NOI18N
            filter("co_return", APTTokenTypes.LITERAL_co_return); // NOI18N
        }
        
        filter("and", APTTokenTypes.AND); // NOI18N
        filter("bitor", APTTokenTypes.BITWISEOR); // NOI18N
        filter("or", APTTokenTypes.OR); // NOI18N
        filter("xor", APTTokenTypes.BITWISEXOR); // NOI18N
        filter("compl", APTTokenTypes.TILDE); // NOI18N
        filter("bitand", APTTokenTypes.AMPERSAND); // NOI18N
        filter("and_eq", APTTokenTypes.BITWISEANDEQUAL); // NOI18N
        filter("or_eq", APTTokenTypes.BITWISEOREQUAL); // NOI18N
        filter("xor_eq", APTTokenTypes.BITWISEXOREQUAL); // NOI18N
        filter("not", APTTokenTypes.NOT); // NOI18N
        filter("not_eq", APTTokenTypes.NOTEQUAL); // NOI18N
    }
}
