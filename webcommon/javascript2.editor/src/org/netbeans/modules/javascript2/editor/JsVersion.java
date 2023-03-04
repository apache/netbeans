/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.EnumSet;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public enum JsVersion {

    @NbBundle.Messages("LBL_ECMA5=ECMAScript 5.1")
    ECMA5(Bundle.LBL_ECMA5(), 5),

    @NbBundle.Messages("LBL_ECMA6=ECMAScript 6 - 2015")
    ECMA6(Bundle.LBL_ECMA6(), 6),

    @NbBundle.Messages("LBL_ECMA7=ECMAScript 7 - 2016 (Experimental)")
    ECMA7(Bundle.LBL_ECMA7(), 7),

    @NbBundle.Messages("LBL_ECMA8=ECMAScript 8 - 2017 (Experimental)")
    ECMA8(Bundle.LBL_ECMA8(), 8),

    @NbBundle.Messages("LBL_ECMA9=ECMAScript 9 - 2018 (Experimental)")
    ECMA9(Bundle.LBL_ECMA9(), 9),

    @NbBundle.Messages("LBL_ECMA10=ECMAScript 10 - 2019 (Experimental)")
    ECMA10(Bundle.LBL_ECMA10(), 10),

    @NbBundle.Messages("LBL_ECMA11=ECMAScript 11 - 2020 (Experimental)")
    ECMA11(Bundle.LBL_ECMA11(), 11),

    @NbBundle.Messages("LBL_ECMA12=ECMAScript 12 - 2021 (Experimental)")
    ECMA12(Bundle.LBL_ECMA12(), 12),

    @NbBundle.Messages("LBL_ECMANEXT=ES.Next (Experimental)")
    EMCANEXT(Bundle.LBL_ECMANEXT(), Integer.MAX_VALUE);

    private final String displayName;
    private final int ecmascriptEdition;

    private JsVersion(String displayName, int ecmascriptEdition) {
        this.displayName = displayName;
        this.ecmascriptEdition = ecmascriptEdition;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getEcmascriptEdition() {
        return ecmascriptEdition;
    }

    public static JsVersion fromEcmascriptEdition(int edition) {
        for (JsVersion v : EnumSet.allOf(JsVersion.class)) {
            if (v.getEcmascriptEdition() == edition) {
                return v;
            }
        }
        return null;
    }

    @CheckForNull
    public static JsVersion fromString(String str) {
        if (str == null) {
            return null;
        }
        for (JsVersion v : EnumSet.allOf(JsVersion.class)) {
            if (str.equals(v.name()) || str.equals(v.getDisplayName())) {
                return v;
            }
        }
        return null;
    }

}
