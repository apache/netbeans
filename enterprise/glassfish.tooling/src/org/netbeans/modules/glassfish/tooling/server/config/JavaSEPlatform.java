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
package org.netbeans.modules.glassfish.tooling.server.config;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaSE platforms supported by GlassFish.
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
    /** JavaSE 1.7. */
    v1_7,
    /** JavaSE 1.8. */
    v1_8,
    /** JavaSE 11. */
    v11,
    /** JavaSE 12. */
    v12,
    /** JavaSE 13. */
    v13,
    /** JavaSE 14. */
    v14,
    /** JavaSE 15. */
    v15,
    /** JavaSE 16. */
    v16,
    /** JavaSE 17. */
    v17,
    /** JavaSE 18. */
    v18,
    /** JavaSE 19. */
    v19,
    /** JavaSE 20. */
    v20,
    /** JavaSE 21. */
    v21,
    /** JavaSE 22. */
    v22,
    /** JavaSE 23. */
    v23;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish JavaEE platform enumeration length. */
    public static final int length = JavaSEPlatform.values().length;

    /** JavaEE platform version elements separator character. */
    public static final char SEPARATOR = '.';

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
        String n = name();
        return n.startsWith("v1_") ? "1." + n.substring(3) : n.substring(1);
    }
}
