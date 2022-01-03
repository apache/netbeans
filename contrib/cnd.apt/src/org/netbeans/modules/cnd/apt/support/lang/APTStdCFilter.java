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

import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

/**
 * filter for Std C language
 */
class APTStdCFilter extends APTBaseLanguageFilter {
    
    /**
     * Creates a new instance of APTStdCFilter
     */
    public APTStdCFilter() {
        super(false);
        initialize();
    }

    private void initialize() {
        // TODO: clean up!!!
        filter("alignof", APTTokenTypes.LITERAL_alignof); // NOI18N
        filter("typeof", APTTokenTypes.LITERAL_typeof); // NOI18N      
        filter("typedef", APTTokenTypes.LITERAL_typedef); // NOI18N
        filter("enum", APTTokenTypes.LITERAL_enum); // NOI18N       
        filter("extern", APTTokenTypes.LITERAL_extern); // NOI18N
        filter("inline", APTTokenTypes.LITERAL_inline); // NOI18N
        filter("_inline", APTTokenTypes.LITERAL__inline); // NOI18N        
        filter("_stdcall", APTTokenTypes.LITERAL__stdcall); // NOI18N
        filter("__stdcall", APTTokenTypes.LITERAL___stdcall); // NOI18N        
        filter("auto", APTTokenTypes.LITERAL_auto); // NOI18N
        filter("register", APTTokenTypes.LITERAL_register); // NOI18N
        filter("static", APTTokenTypes.LITERAL_static); // NOI18N        
        filter("const", APTTokenTypes.LITERAL_const); // NOI18N
        filter("__const", APTTokenTypes.LITERAL___const); // NOI18N
        filter("const_cast", APTTokenTypes.LITERAL_const_cast); // NOI18N
        filter("volatile", APTTokenTypes.LITERAL_volatile); // NOI18N
        filter("char", APTTokenTypes.LITERAL_char); // NOI18N
        filter("short", APTTokenTypes.LITERAL_short); // NOI18N
        filter("int", APTTokenTypes.LITERAL_int); // NOI18N
        filter("long", APTTokenTypes.LITERAL_long); // NOI18N
        filter("signed", APTTokenTypes.LITERAL_signed); // NOI18N
        filter("__signed__", APTTokenTypes.LITERAL___signed__); // NOI18N
        filter("unsigned", APTTokenTypes.LITERAL_unsigned); // NOI18N
        filter("__unsigned__", APTTokenTypes.LITERAL___unsigned__); // NOI18N
        filter("float", APTTokenTypes.LITERAL_float); // NOI18N
        filter("double", APTTokenTypes.LITERAL_double); // NOI18N
        filter("void", APTTokenTypes.LITERAL_void); // NOI18N
        filter("_declspec", APTTokenTypes.LITERAL__declspec); // NOI18N
        filter("__declspec", APTTokenTypes.LITERAL___declspec); // NOI18N   
        filter("struct", APTTokenTypes.LITERAL_struct); // NOI18N
        filter("union", APTTokenTypes.LITERAL_union); // NOI18N   
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
        filter("asm", APTTokenTypes.LITERAL_asm); // NOI18N
        filter("_asm", APTTokenTypes.LITERAL__asm); // NOI18N
        filter("sizeof", APTTokenTypes.LITERAL_sizeof); // NOI18N
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
        filter("_int64", APTTokenTypes.LITERAL__int64); // NOI18N
        filter("__int64", APTTokenTypes.LITERAL___int64); // NOI18N
        filter("__w64", APTTokenTypes.LITERAL___w64); // NOI18N
        filter("__builtin_va_list", APTTokenTypes.LITERAL___builtin_va_list); // NOI18N
        filter("__extension__", APTTokenTypes.LITERAL___extension__); // NOI18N
        filter("__restrict", APTTokenTypes.LITERAL___restrict); // NOI18N    
        filter("_Bool", APTTokenTypes.LITERAL__Bool); // NOI18N
        filter("_Complex", APTTokenTypes.LITERAL__Complex); // NOI18N
        filter("_Imaginary", APTTokenTypes.LITERAL__Imaginary); // NOI18N
        filter("_Noreturn", APTTokenTypes.LITERAL__Noreturn); // NOI18N        
        filter("_Atomic", APTTokenTypes.LITERAL__Atomic); // NOI18N        
        filter("_Alignof", APTTokenTypes.LITERAL__Alignof); // NOI18N
        filter("_Alignas", APTTokenTypes.LITERAL__Alignas); // NOI18N
        filter("_Thread_local", APTTokenTypes.LITERAL__Thread_local); // NOI18N
        filter("_Static_assert", APTTokenTypes.LITERAL__Static_assert); // NOI18N
    }    
}
