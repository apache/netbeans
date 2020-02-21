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

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;

/**
 *
 *
 */
public enum DbxMemoryFormat implements FormatOption {
    HEXADECIMAL8(Catalog.get("L_Hexadecimal"), "lX"), //NOI18N
    HEXADECIMAL4(Catalog.get("l_Hexadecimal"), "X"), //NOI18N
    HEXADECIMAL2(Catalog.get("w_Hexadecimal"), "x"), //NOI18N
    DECIMAL(Catalog.get("l_Decimal"), "D"), //NOI18N
    OCTAL(Catalog.get("l_Octal"), "O"), //NOI18N
    FLOAT8(Catalog.get("L_Float"), "F"), //NOI18N
    FLOAT4(Catalog.get("l_Float"), "f"), //NOI18N
    INSTRUCTION(Catalog.get("L_Instructions"), "i"), //NOI18N
    CHARACTER(Catalog.get("L_Characters"), "c"), //NOI18N
    WCHARACTER(Catalog.get("L_WideCharacters"), "w"); //NOI18N

    private final String dispName;
    private final String option;
    
    DbxMemoryFormat(String dispName, String option) {
        this.dispName = dispName;
        this.option = option;
    }

    @Override
    public String toString() {
        return dispName;
    }

    public String getOption() {
        return option;
    }
}
