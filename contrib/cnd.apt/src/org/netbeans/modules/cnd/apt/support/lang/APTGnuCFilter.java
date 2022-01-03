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
 * filter for GNU C language
 */
final class APTGnuCFilter extends APTStdCFilter {
    
    /** Creates a new instance of APTGnuCFilter */
    public APTGnuCFilter() {
        initialize();
    }
    
    private void initialize() {
        // GNU C extensions 
        filter("__alignof__", APTTokenTypes.LITERAL___alignof__); // NOI18N
        filter("__asm", APTTokenTypes.LITERAL___asm); // NOI18N
        filter("__asm__", APTTokenTypes.LITERAL___asm__); // NOI18N
        filter("__attribute__", APTTokenTypes.LITERAL___attribute__); // NOI18N
        filter("__attribute", APTTokenTypes.LITERAL___attribute); // NOI18N
        filter("__complex__", APTTokenTypes.LITERAL___complex__); // NOI18N
        filter("__const", APTTokenTypes.LITERAL___const); // NOI18N
        filter("__const__", APTTokenTypes.LITERAL___const__); // NOI18N
        filter("__imag__", APTTokenTypes.LITERAL___imag); // NOI18N
        filter("__global", APTTokenTypes.LITERAL___global); // NOI18N
        filter("__hidden", APTTokenTypes.LITERAL___hidden); // NOI18N
        filter("__inline", APTTokenTypes.LITERAL___inline); // NOI18N
        filter("__inline__", APTTokenTypes.LITERAL___inline__); // NOI18N
        filter("__real__", APTTokenTypes.LITERAL___real); // NOI18N
        filter("restrict", APTTokenTypes.LITERAL_restrict); // NOI18N
        filter("__restrict", APTTokenTypes.LITERAL___restrict); // NOI18N
        filter("__restrict__", APTTokenTypes.LITERAL___restrict__); // NOI18N
        filter("__signed", APTTokenTypes.LITERAL___signed); // NOI18N
        filter("__signed__", APTTokenTypes.LITERAL___signed__); // NOI18N
        filter("__symbolic", APTTokenTypes.LITERAL___symbolic); // NOI18N
        filter("__thread", APTTokenTypes.LITERAL___thread); // NOI18N
        filter("__typeof", APTTokenTypes.LITERAL___typeof); // NOI18N
        filter("__typeof__", APTTokenTypes.LITERAL___typeof__); // NOI18N
        filter("__volatile", APTTokenTypes.LITERAL___volatile); // NOI18N
        filter("__volatile__", APTTokenTypes.LITERAL___volatile__); // NOI18N        
    }
}
