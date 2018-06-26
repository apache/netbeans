/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.server.config;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaSE platforms supported by Glassfish.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum JavaSEPlatform {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** JavaSE 1.1. */
    v1_1,
    /** JavaSE 1.2. */
    v1_2,
    /** JavaSE 1.3. */
    v1_3,
    /** JavaSE 1.4. */
    v1_4,
    /** JavaSE 1.5. */
    v1_5,
    /** JavaSE 1.6. */
    v1_6,
    /** JavaEE 1.7. */
    v1_7,
    /** JavaEE 1.8. */
    v1_8;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish JavaEE platform enumeration length. */
    public static final int length = JavaSEPlatform.values().length;
    
    /** JavaEE platform version elements separator character. */
    public static final char SEPARATOR = '.';

    /**  A <code>String</code> representation of v1_1 value. */
    static final String V1_1_STR = "1.1";

    /**  A <code>String</code> representation of v1_2 value. */
    static final String V1_2_STR = "1.2";

    /**  A <code>String</code> representation of v1_3 value. */
    static final String V1_3_STR = "1.3";

    /**  A <code>String</code> representation of v1_4 value. */
    static final String V1_4_STR = "1.4";

    /**  A <code>String</code> representation of v1_5 value. */
    static final String V1_5_STR = "1.5";

    /**  A <code>String</code> representation of v1_6 value. */
    static final String V1_6_STR = "1.6";

    /**  A <code>String</code> representation of v1_7 value. */
    static final String V1_7_STR = "1.7";

    /**  A <code>String</code> representation of v1_8 value. */
    static final String V1_8_STR = "1.8";

    /** 
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, JavaSEPlatform> stringValuesMap
            = new HashMap<>(values().length);

    // Initialize backward String conversion Map.
    static {
        for (JavaSEPlatform platform : JavaSEPlatform.values()) {
            stringValuesMap.put(platform.toString().toUpperCase(), platform);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>JavaSEPlatform</code> with a value represented by the
     * specified <code>String</code>. The <code>JavaSEPlatform</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param platformName Value containing <code>JavaSEPlatform</code> 
     *                     <code>toString</code> representation.
     * @return <code>JavaSEPlatform</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static JavaSEPlatform toValue(final String platformName) {
        if (platformName != null) {
            return (stringValuesMap.get(platformName.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert JavaEE platform version value to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
            case v1_1:     return V1_1_STR;
            case v1_2:     return V1_2_STR;
            case v1_3:     return V1_3_STR;
            case v1_4:     return V1_4_STR;
            case v1_5:     return V1_5_STR;
            case v1_6:     return V1_6_STR;
            case v1_7:     return V1_7_STR;
            case v1_8:     return V1_8_STR;
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default:   throw new ServerConfigException(
                        ServerConfigException.INVALID_SE_PLATFORM_VERSION);
        }
    }
}
