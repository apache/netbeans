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
public enum DbxEvalFormat implements FormatOption {
    DEFAULT(Catalog.get("Default_format"), ""), //NOI18N
    HEXADECIMAL4(Catalog.get("l_Hexadecimal"), "-fx"), //NOI18N
    HEXADECIMAL8(Catalog.get("L_Hexadecimal"), "-flx"), //NOI18N
    DECIMAL4(Catalog.get("l_Decimal"), "-fd"), //NOI18N
    DECIMAL8(Catalog.get("L_Decimal"), "-fld"), //NOI18N
    UNSIGNED_DECIMAL4(Catalog.get("l_U_Decimal"), "-fu"), //NOI18N
    UNSIGNED_DECIMAL8(Catalog.get("L_U_Decimal"), "-flu"), //NOI18N
    FLOAT4(Catalog.get("l_Float"), "(float)"), //NOI18N
    FLOAT8(Catalog.get("L_Float"), "(double)"); //NOI18N

    private final String dispName;
    private final String option;
    
    DbxEvalFormat(String dispName, String option) {
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
