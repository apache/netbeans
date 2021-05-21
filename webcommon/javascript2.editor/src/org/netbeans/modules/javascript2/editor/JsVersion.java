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
    ECMA5(Bundle.LBL_ECMA5()),

    @NbBundle.Messages("LBL_ECMA6=ECMAScript 6")
    ECMA6(Bundle.LBL_ECMA6()),

    @NbBundle.Messages("LBL_ECMA7=ECMAScript 7 (Experimental)")
    ECMA7(Bundle.LBL_ECMA7());

    private final String displayName;

    private JsVersion(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
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
