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

package org.netbeans.modules.cnd.debugger.gdb2;

import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;
import org.openide.util.NbBundle;

/**
 *
 */
public enum GdbMemoryFormat implements FormatOption {
    HEXADECIMAL(NbBundle.getMessage(GdbMemoryFormat.class, "Format_Hexadecimal"), "x"), //NOI18N
    DECIMAL(NbBundle.getMessage(GdbMemoryFormat.class, "Format_Decimal"), "d"), //NOI18N
    UNSIGNED_DECIMAL(NbBundle.getMessage(GdbMemoryFormat.class, "Format_UnsignedDecimal"), "u"), //NOI18N
    OCTAL(NbBundle.getMessage(GdbMemoryFormat.class, "Format_Octal"), "o"), //NOI18N
    BINARY(NbBundle.getMessage(GdbMemoryFormat.class, "Format_Binary"), "t"), //NOI18N
    ADDRESS(NbBundle.getMessage(GdbMemoryFormat.class, "Format_Address"), "a"), //NOI18N
    CHARACTER(NbBundle.getMessage(GdbMemoryFormat.class, "Format_Character"), "c"), //NOI18N
    FLOAT(NbBundle.getMessage(GdbMemoryFormat.class, "Format_Float"), "f"), //NOI18N
    STRING(NbBundle.getMessage(GdbMemoryFormat.class, "Format_String"), "s"); //NOI18N

    private final String dispName;
    private final String option;
    
    GdbMemoryFormat(String dispName, String option) {
        this.dispName = dispName;
        this.option = option;
    }
    
    @Override
    public String toString() {
        return dispName;
    }

    @Override
    public String getOption() {
        return option;
    }
}
