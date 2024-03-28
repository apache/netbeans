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
package org.netbeans.modules.glassfish.tooling.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.glassfish.tooling.utils.EnumUtils;
import org.openide.util.Parameters;

/**
 * GlassFish server version.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum GlassFishVersion {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish v1. */
    GF_1        ((short) 1, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_1_STR),
    /** GlassFish v2. */
    GF_2        ((short) 2, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_2_STR),
    /** GlassFish v2.1. */
    GF_2_1      ((short) 2, (short) 1, (short) 0, (short) 0, GlassFishVersion.GF_2_1_STR),
    /** GlassFish v2.1.1. */
    GF_2_1_1    ((short) 2, (short) 1, (short) 1, (short) 0, GlassFishVersion.GF_2_1_1_STR),
    /** GlassFish v3. */
    GF_3        ((short) 3, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_3_STR),
    /** GlassFish 3.0.1. */
    GF_3_0_1    ((short) 3, (short) 0, (short) 1, (short) 0, GlassFishVersion.GF_3_0_1_STR),
    /** GlassFish 3.1. */
    GF_3_1      ((short) 3, (short) 1, (short) 0, (short) 0, GlassFishVersion.GF_3_1_STR),
    /** GlassFish 3.1.1. */
    GF_3_1_1    ((short) 3, (short) 1, (short) 1, (short) 0, GlassFishVersion.GF_3_1_1_STR),
    /** GlassFish 3.1.2. */
    GF_3_1_2    ((short) 3, (short) 1, (short) 2, (short) 0, GlassFishVersion.GF_3_1_2_STR),
    /** GlassFish 3.1.2.2. */
    GF_3_1_2_2  ((short) 3, (short) 1, (short) 2, (short) 2, GlassFishVersion.GF_3_1_2_2_STR),
    /** GlassFish 3.1.2.3. */
    GF_3_1_2_3  ((short) 3, (short) 1, (short) 2, (short) 3, GlassFishVersion.GF_3_1_2_3_STR),
    /** GlassFish 3.1.2.4. */
    GF_3_1_2_4  ((short) 3, (short) 1, (short) 2, (short) 4, GlassFishVersion.GF_3_1_2_4_STR),
    /** GlassFish 3.1.2.4. */
    GF_3_1_2_5  ((short) 3, (short) 1, (short) 2, (short) 5, GlassFishVersion.GF_3_1_2_5_STR),
    /** GlassFish 4. */
    GF_4        ((short) 4, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_4_STR),
    /** GlassFish 4.0.1. */
    GF_4_0_1    ((short) 4, (short) 0, (short) 1, (short) 0, GlassFishVersion.GF_4_0_1_STR),
    /** GlassFish 4.1. */
    GF_4_1      ((short) 4, (short) 1, (short) 0, (short) 0, GlassFishVersion.GF_4_1_STR),
    /** GlassFish 4.1.1. */
    GF_4_1_1    ((short) 4, (short) 1, (short) 1, (short) 0, GlassFishVersion.GF_4_1_1_STR),
    /** GlassFish 4.1.2. */
    GF_4_1_2    ((short) 4, (short) 1, (short) 1, (short) 2, GlassFishVersion.GF_4_1_2_STR),
    /** GlassFish 5. */
    GF_5        ((short) 5, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_5_STR),
    /** GlassFish 5.0.1 */
    GF_5_0_1       ((short) 5, (short) 0, (short) 1, (short) 0, GlassFishVersion.GF_5_0_1_STR),
    /** GlassFish 5.1.0 */
    GF_5_1_0        ((short) 5, (short) 1, (short) 0, (short) 0, GlassFishVersion.GF_5_1_0_STR),
    /** GlassFish 6. */
    GF_6       ((short) 6, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_6_STR),
    /** GlassFish 6.1.0 */
    GF_6_1_0       ((short) 6, (short) 1, (short) 0, (short) 0, GlassFishVersion.GF_6_1_0_STR),
    /** GlassFish 6.2.0 */
    GF_6_2_0       ((short) 6, (short) 2, (short) 0, (short) 0, GlassFishVersion.GF_6_2_0_STR),
    /** GlassFish 6.2.1 */
    GF_6_2_1       ((short) 6, (short) 2, (short) 1, (short) 0, GlassFishVersion.GF_6_2_1_STR),
    /** GlassFish 6.2.2 */
    GF_6_2_2       ((short) 6, (short) 2, (short) 2, (short) 0, GlassFishVersion.GF_6_2_2_STR),
    /** GlassFish 6.2.3 */
    GF_6_2_3       ((short) 6, (short) 2, (short) 3, (short) 0, GlassFishVersion.GF_6_2_3_STR),
    /** GlassFish 6.2.4 */
    GF_6_2_4       ((short) 6, (short) 2, (short) 4, (short) 0, GlassFishVersion.GF_6_2_4_STR),
    /** GlassFish 6.2.5 */
    GF_6_2_5       ((short) 6, (short) 2, (short) 5, (short) 0, GlassFishVersion.GF_6_2_5_STR),
    /** GlassFish 7.0.0 */
    GF_7_0_0       ((short) 7, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_7_0_0_STR),
    /** GlassFish 7.0.1 */
    GF_7_0_1       ((short) 7, (short) 0, (short) 1, (short) 0, GlassFishVersion.GF_7_0_1_STR),
    /** GlassFish 7.0.2 */
    GF_7_0_2       ((short) 7, (short) 0, (short) 2, (short) 0, GlassFishVersion.GF_7_0_2_STR),
    /** GlassFish 7.0.3 */
    GF_7_0_3       ((short) 7, (short) 0, (short) 3, (short) 0, GlassFishVersion.GF_7_0_3_STR),
    /** GlassFish 7.0.4 */
    GF_7_0_4       ((short) 7, (short) 0, (short) 4, (short) 0, GlassFishVersion.GF_7_0_4_STR),
    /** GlassFish 7.0.5 */
    GF_7_0_5       ((short) 7, (short) 0, (short) 5, (short) 0, GlassFishVersion.GF_7_0_5_STR),
    /** GlassFish 7.0.6 */
    GF_7_0_6       ((short) 7, (short) 0, (short) 6, (short) 0, GlassFishVersion.GF_7_0_6_STR),
    /** GlassFish 7.0.7 */
    GF_7_0_7       ((short) 7, (short) 0, (short) 7, (short) 0, GlassFishVersion.GF_7_0_7_STR),
    /** GlassFish 7.0.8 */
    GF_7_0_8       ((short) 7, (short) 0, (short) 8, (short) 0, GlassFishVersion.GF_7_0_8_STR),
    /** GlassFish 7.0.9 */
    GF_7_0_9       ((short) 7, (short) 0, (short) 9, (short) 0, GlassFishVersion.GF_7_0_9_STR),
    /** GlassFish 7.0.10 */
    GF_7_0_10       ((short) 7, (short) 0, (short) 10, (short) 0, GlassFishVersion.GF_7_0_10_STR),
    /** GlassFish 7.0.11 */
    GF_7_0_11       ((short) 7, (short) 0, (short) 11, (short) 0, GlassFishVersion.GF_7_0_11_STR),
    /** GlassFish 7.0.12 */
    GF_7_0_12       ((short) 7, (short) 0, (short) 12, (short) 0, GlassFishVersion.GF_7_0_12_STR),
    /** GlassFish 7.0.13 */
    GF_7_0_13       ((short) 7, (short) 0, (short) 13, (short) 0, GlassFishVersion.GF_7_0_13_STR),
    /** GlassFish 8.0.0 */
    GF_8_0_0       ((short) 8, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_8_0_0_STR);
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish version enumeration length. */
    public static final int length = GlassFishVersion.values().length;

    /** Version elements separator character. */
    public static final char SEPARATOR = '.';

    /** Version elements separator REGEX pattern. */
    public static final String SEPARATOR_PATTERN = "\\.";

    /**  A <code>String</code> representation of GF_1 value. */
    static final String GF_1_STR = "1";
    /** Additional <code>String</code> representations of GF_1 value. */
    static final String GF_1_STR_NEXT[] = {"1.0", "1.0.0", "1.0.0.0"};

    /**  A <code>String</code> representation of GF_2 value. */
    static final String GF_2_STR = "2";
    /** Additional <code>String</code> representations of GF_2 value. */
    static final String GF_2_STR_NEXT[] = {"2.0", "2.0.0", "2.0.0.0"};

    /**  A <code>String</code> representation of GF_2_1 value. */
    static final String GF_2_1_STR = "2.1";
    /** Additional <code>String</code> representations of GF_2_1 value. */
    static final String GF_2_1_STR_NEXT[] = {"2.1.0", "2.1.0.0"};

    /**  A <code>String</code> representation of GF_2_1_1 value. */
    static final String GF_2_1_1_STR = "2.1.1";
    /** Additional <code>String</code> representations of GF_2_1_1 value. */
    static final String GF_2_1_1_STR_NEXT[] = {"2.1.1.0"};

    /**  A <code>String</code> representation of GF_3 value. */
    static final String GF_3_STR = "3";
    /** Additional <code>String</code> representations of GF_3 value. */
    static final String GF_3_STR_NEXT[] = {"3.0", "3.0.0", "3.0.0.0"};

    /**  A <code>String</code> representation of GF_3_0_1 value. */
    static final String GF_3_0_1_STR = "3.0.1";
    /** Additional <code>String</code> representations of GF_3_0_1 value. */
    static final String GF_3_0_1_STR_NEXT[] = {"3.0.1.0"};

    /**  A <code>String</code> representation of GF_3_1 value. */
    static final String GF_3_1_STR = "3.1";
    /** Additional <code>String</code> representations of GF_3_1 value. */
    static final String GF_3_1_STR_NEXT[] = {"3.1.0", "3.1.0.0"};

    /**  A <code>String</code> representation of GF_3_1_1 value. */
    static final String GF_3_1_1_STR = "3.1.1";
    /** Additional <code>String</code> representations of GF_3_1_1 value. */
    static final String GF_3_1_1_STR_NEXT[] = {"3.1.1.0"};

    /**  A <code>String</code> representation of GF_3_1_2 value. */
    static final String GF_3_1_2_STR = "3.1.2";
    /** Additional <code>String</code> representations of GF_3_1_2 value. */
    static final String GF_3_1_2_STR_NEXT[] = {"3.1.2.0"};

    /**  A <code>String</code> representation of GF_3_1_2_2 value. */
    static final String GF_3_1_2_2_STR = "3.1.2.2";

    /**  A <code>String</code> representation of GF_3_1_2_3 value. */
    static final String GF_3_1_2_3_STR = "3.1.2.3";

    /**  A <code>String</code> representation of GF_3_1_2_4 value. */
    static final String GF_3_1_2_4_STR = "3.1.2.4";

    /**  A <code>String</code> representation of GF_3_1_2_4 value. */
    static final String GF_3_1_2_5_STR = "3.1.2.5";

    /**  A <code>String</code> representation of GF_4 value. */
    static final String GF_4_STR = "4";
    /** Additional <code>String</code> representations of GF_4 value. */
    static final String GF_4_STR_NEXT[] = {"4.0", "4.0.0", "4.0.0.0"};

    /**  A <code>String</code> representation of GF_4_0_1 value. */
    static final String GF_4_0_1_STR = "4.0.1";
    /** Additional <code>String</code> representations of GF_4_0_1 value. */
    static final String GF_4_0_1_STR_NEXT[] = {"4.0.1.0"};

    /**  A <code>String</code> representation of GF_4_1 value. */
    static final String GF_4_1_STR = "4.1";
    /** Additional <code>String</code> representations of GF_4_1 value. */
    static final String GF_4_1_STR_NEXT[] = {"4.1.0", "4.1.0.0"};

    /**  A <code>String</code> representation of GF_4_1_1 value. */
    static final String GF_4_1_1_STR = "4.1.1";
    /** Additional <code>String</code> representations of GF_4_1 value. */
    static final String GF_4_1_1_STR_NEXT[] = {"4.1.1.0"};

    /**  A <code>String</code> representation of GF_4_1_2 value. */
    static final String GF_4_1_2_STR = "4.1.2";
    /** Additional <code>String</code> representations of GF_4_1_2 value. */
    static final String GF_4_1_2_STR_NEXT[] = {"4.1.2.0"};

    /**  A <code>String</code> representation of GF_5 value. */
    static final String GF_5_STR = "5";
    /** Additional <code>String</code> representations of GF_5 value. */
    static final String GF_5_STR_NEXT[] = {"5.0", "5.0.0", "5.0.0.0"};
    
    /**  A <code>String</code> representation of GF_5_0_1 value. */
    static final String GF_5_0_1_STR = "5.0.1";
    /** Additional <code>String</code> representations of GF_5_0_1 value. */
    static final String GF_5_0_1_STR_NEXT[] = {"5.0.1", "5.0.1.0"};
    
    /**  A <code>String</code> representation of GF_5_1_0 value. */
    static final String GF_5_1_0_STR = "5.1.0";
    /** Additional <code>String</code> representations of GF_5_1_0 value. */
    static final String GF_5_1_0_STR_NEXT[] = {"5.1.0", "5.1.0.0"};
    
    /** A <code>String</code> representation of GF_6 value. */
    static final String GF_6_STR = "6";
    /** Additional <code>String</code> representations of GF_6 value. */
    static final String GF_6_STR_NEXT[] = {"6.0", "6.0.0", "6.0.0.0"};
    
    /** A <code>String</code> representation of GF_6_1_0 value. */
    static final String GF_6_1_0_STR = "6.1.0";
    /** Additional <code>String</code> representations of GF_6_1_0 value. */
    static final String GF_6_1_0_STR_NEXT[] = {"6.1", "6.1.0", "6.1.0.0"};
    
    /** A <code>String</code> representation of GF_6_2_0 value. */
    static final String GF_6_2_0_STR = "6.2.0";
    /** Additional <code>String</code> representations of GF_6_2_0 value. */
    static final String GF_6_2_0_STR_NEXT[] = {"6.2", "6.2.0", "6.2.0.0"};
    
    /** A <code>String</code> representation of GF_6_2_1 value. */
    static final String GF_6_2_1_STR = "6.2.1";
    /** Additional <code>String</code> representations of GF_6_2_1 value. */
    static final String GF_6_2_1_STR_NEXT[] = {"6.2.1", "6.2.1.0"};
    
    /** A <code>String</code> representation of GF_6_2_2 value. */
    static final String GF_6_2_2_STR = "6.2.2";
    /** Additional <code>String</code> representations of GF_6_2_2 value. */
    static final String GF_6_2_2_STR_NEXT[] = {"6.2.2", "6.2.2.0"};
    
    /** A <code>String</code> representation of GF_6_2_3 value. */
    static final String GF_6_2_3_STR = "6.2.3";
    /** Additional <code>String</code> representations of GF_6_2_3 value. */
    static final String GF_6_2_3_STR_NEXT[] = {"6.2.3", "6.2.3.0"};
    
    /** A <code>String</code> representation of GF_6_2_4 value. */
    static final String GF_6_2_4_STR = "6.2.4";
    /** Additional <code>String</code> representations of GF_6_2_4 value. */
    static final String GF_6_2_4_STR_NEXT[] = {"6.2.4", "6.2.4.0"};
    
    /** A <code>String</code> representation of GF_6_2_5 value. */
    static final String GF_6_2_5_STR = "6.2.5";
    /** Additional <code>String</code> representations of GF_6_2_5 value. */
    static final String GF_6_2_5_STR_NEXT[] = {"6.2.5", "6.2.5.0"};

    /** A <code>String</code> representation of GF_7_0_0 value. */
    static final String GF_7_0_0_STR = "7.0.0";
    /** Additional <code>String</code> representations of GF_7_0_0 value. */
    static final String GF_7_0_0_STR_NEXT[] = {"7.0.0", "7.0.0.0"};
    
    /** A <code>String</code> representation of GF_7_0_1 value. */
    static final String GF_7_0_1_STR = "7.0.1";
    /** Additional <code>String</code> representations of GF_7_0_1 value. */
    static final String GF_7_0_1_STR_NEXT[] = {"7.0.1", "7.0.1.0"};
    
    /** A {@code String} representation of GF_7_0_2 value. */
    static final String GF_7_0_2_STR = "7.0.2";
    /** Additional {@code String} representations of GF_7_0_2 value. */
    static final String GF_7_0_2_STR_NEXT[] = {"7.0.2", "7.0.2.0"};

    /** A {@code String} representation of GF_7_0_3 value. */
    static final String GF_7_0_3_STR = "7.0.3";
    /** Additional {@code String} representations of GF_7_0_3 value. */
    static final String GF_7_0_3_STR_NEXT[] = {"7.0.3", "7.0.3.0"};

    /** A {@code String} representation of GF_7_0_4 value. */
    static final String GF_7_0_4_STR = "7.0.4";
    /** Additional {@code String} representations of GF_7_0_4 value. */
    static final String GF_7_0_4_STR_NEXT[] = {"7.0.4", "7.0.4.0"};

    /** A {@code String} representation of GF_7_0_5 value. */
    static final String GF_7_0_5_STR = "7.0.5";
    /** Additional {@code String} representations of GF_7_0_5 value. */
    static final String GF_7_0_5_STR_NEXT[] = {"7.0.5", "7.0.5.0"};

    /** A {@code String} representation of GF_7_0_6 value. */
    static final String GF_7_0_6_STR = "7.0.6";
    /** Additional {@code String} representations of GF_7_0_6 value. */
    static final String GF_7_0_6_STR_NEXT[] = {"7.0.6", "7.0.6.0"};
    
    /** A {@code String} representation of GF_7_0_7 value. */
    static final String GF_7_0_7_STR = "7.0.7";
    /** Additional {@code String} representations of GF_7_0_6 value. */
    static final String GF_7_0_7_STR_NEXT[] = {"7.0.7", "7.0.7.0"};
    
    /** A {@code String} representation of GF_7_0_8 value. */
    static final String GF_7_0_8_STR = "7.0.8";
    /** Additional {@code String} representations of GF_7_0_8 value. */
    static final String GF_7_0_8_STR_NEXT[] = {"7.0.8", "7.0.8.0"};
    
    /** A {@code String} representation of GF_7_0_9 value. */
    static final String GF_7_0_9_STR = "7.0.9";
    /** Additional {@code String} representations of GF_7_0_9 value. */
    static final String GF_7_0_9_STR_NEXT[] = {"7.0.9", "7.0.9.0"};
    
    /** A {@code String} representation of GF_7_0_10 value. */
    static final String GF_7_0_10_STR = "7.0.10";
    /** Additional {@code String} representations of GF_7_0_10 value. */
    static final String GF_7_0_10_STR_NEXT[] = {"7.0.10", "7.0.10.0"};
    
    /** A {@code String} representation of GF_7_0_11 value. */
    static final String GF_7_0_11_STR = "7.0.11";
    /** Additional {@code String} representations of GF_7_0_11 value. */
    static final String GF_7_0_11_STR_NEXT[] = {"7.0.11", "7.0.11.0"};

    /** A {@code String} representation of GF_7_0_12 value. */
    static final String GF_7_0_12_STR = "7.0.12";
    /** Additional {@code String} representations of GF_7_0_12 value. */
    static final String GF_7_0_12_STR_NEXT[] = {"7.0.12", "7.0.12.0"};

    /** A {@code String} representation of GF_7_0_13 value. */
    static final String GF_7_0_13_STR = "7.0.13";
    /** Additional {@code String} representations of GF_7_0_13 value. */
    static final String GF_7_0_13_STR_NEXT[] = {"7.0.13", "7.0.13.0"};

    /** A {@code String} representation of GF_8_0_0 value. */
    static final String GF_8_0_0_STR = "8.0.0";
    /** Additional {@code String} representations of GF_8_0_0 value. */
    static final String GF_8_0_0_STR_NEXT[] = {"8.0.0", "8.0.0.0"};

    /**
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, GlassFishVersion> stringValuesMap
            = new HashMap<>(2 * values().length);

    // Initialize backward String conversion Map.
    static {
        for (GlassFishVersion state : GlassFishVersion.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
        initStringValuesMapFromArray(GF_1, GF_1_STR_NEXT);
        initStringValuesMapFromArray(GF_2, GF_2_STR_NEXT);
        initStringValuesMapFromArray(GF_2_1, GF_2_1_STR_NEXT);
        initStringValuesMapFromArray(GF_2_1_1, GF_2_1_1_STR_NEXT);
        initStringValuesMapFromArray(GF_3, GF_3_STR_NEXT);
        initStringValuesMapFromArray(GF_3_0_1, GF_3_0_1_STR_NEXT);
        initStringValuesMapFromArray(GF_3_1, GF_3_1_STR_NEXT);
        initStringValuesMapFromArray(GF_3_1_1, GF_3_1_1_STR_NEXT);
        initStringValuesMapFromArray(GF_3_1_2, GF_3_1_2_STR_NEXT);
        initStringValuesMapFromArray(GF_4, GF_4_STR_NEXT);
        initStringValuesMapFromArray(GF_4_0_1, GF_4_0_1_STR_NEXT);
        initStringValuesMapFromArray(GF_4_1, GF_4_1_STR_NEXT);
        initStringValuesMapFromArray(GF_4_1_1, GF_4_1_1_STR_NEXT);
        initStringValuesMapFromArray(GF_4_1_2, GF_4_1_2_STR_NEXT);
        initStringValuesMapFromArray(GF_5, GF_5_STR_NEXT);
        initStringValuesMapFromArray(GF_5_1_0, GF_5_1_0_STR_NEXT);
        initStringValuesMapFromArray(GF_6, GF_6_STR_NEXT);
        initStringValuesMapFromArray(GF_6_1_0, GF_6_1_0_STR_NEXT);
        initStringValuesMapFromArray(GF_6_2_0, GF_6_2_0_STR_NEXT);
        initStringValuesMapFromArray(GF_6_2_1, GF_6_2_1_STR_NEXT);
        initStringValuesMapFromArray(GF_6_2_2, GF_6_2_2_STR_NEXT);
        initStringValuesMapFromArray(GF_6_2_3, GF_6_2_3_STR_NEXT);
        initStringValuesMapFromArray(GF_6_2_4, GF_6_2_4_STR_NEXT);
        initStringValuesMapFromArray(GF_6_2_5, GF_6_2_5_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_0, GF_7_0_0_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_1, GF_7_0_1_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_2, GF_7_0_2_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_3, GF_7_0_3_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_4, GF_7_0_4_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_5, GF_7_0_5_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_6, GF_7_0_6_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_7, GF_7_0_7_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_8, GF_7_0_8_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_9, GF_7_0_9_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_10, GF_7_0_10_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_11, GF_7_0_11_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_12, GF_7_0_12_STR_NEXT);
        initStringValuesMapFromArray(GF_7_0_13, GF_7_0_13_STR_NEXT);
        initStringValuesMapFromArray(GF_8_0_0, GF_8_0_0_STR_NEXT);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Helper method to initialize backward String conversion <code>Map</code> with
     * additional values using additional string values arrays.
     * <p/>
     * @param version Target version for additional values.
     * @param values  Array containing source <code>String</code> values.
     */
    private static void initStringValuesMapFromArray(
            final GlassFishVersion version, final String[] values) {
        for (String value : values) {
            stringValuesMap.put(value, version);
        }
    }

    /**
     * Returns a <code>GlassFishVersion</code> with a value represented by the
     * specified <code>String</code>. The <code>GlassFishVersion</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p/>
     * @param versionStr Value containing version <code>String</code>
     *                   representation.
     * @return <code>GlassFishVersion</code> value represented by
     *         <code>String</code> or <code>null</code> if value was
     *         not recognized.
     */
    @CheckForNull
    public static GlassFishVersion toValue(@NonNull final String versionStr) {
        Parameters.notNull("versionStr", versionStr);

        GlassFishVersion version
                = stringValuesMap.get(versionStr.toUpperCase(Locale.ENGLISH));
        if (version == null) {
            List<Integer> versionNumbers = new ArrayList<>(4);

            String[] versionParts = versionStr.split("[^0-9.]", 2);
            String[] versionNumberStrings = versionParts[0].split("\\.");
            for (int i = 0; i < Math.min(4, versionNumberStrings.length); i++) {
                try {
                    int value = Integer.parseInt(versionNumberStrings[i]);
                    versionNumbers.add(value);
                } catch (NumberFormatException ex) {
                    break;
                }
            }

            for (int i = versionNumbers.size(); i < 4; i++ ) {
                versionNumbers.add(0);
            }

            GlassFishVersion candidates[] = values();
            for(int i = candidates.length - 1; i >= 0; i--) {
                if(gfvSmallerOrEqual(candidates[i], versionNumbers)) {
                    version = candidates[i];
                    break;
                }
            }
        }
        return version;
    }

    private static boolean gfvSmallerOrEqual(GlassFishVersion gfv, List<Integer> versionNumbers) {
        if (gfv.getMajor() < versionNumbers.get(0)) {
            return true;
        } else if (gfv.getMajor() == versionNumbers.get(0)) {
            if (gfv.getMinor() < versionNumbers.get(1)) {
                return true;
            } else if (gfv.getMinor() == versionNumbers.get(1)) {
                if(gfv.getUpdate() < versionNumbers.get(2)) {
                    return true;
                } else if (gfv.getUpdate() == versionNumbers.get(2)) {
                    return gfv.getBuild() <= versionNumbers.get(3);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Major version number. */
    private final short major;

    /** Minor version number. */
    private final short minor;

    /** Update version number. */
    private final short update;

    /** Build version number. */
    private final short build;

    private final String value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server version.
     * <p/>
     * @param major  Major version number.
     * @param minor  Minor version number.
     * @param update Update version number.
     * @param build  Build version number.
     */
    private GlassFishVersion(final short major, final short minor,
            final short update, final short build, final String value) {
        this.major = major;
        this.minor = minor;
        this.update = update;
        this.build = build;
        this.value = value;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get major version number.
     * <p/>
     * @return Major version number.
     */
    public short getMajor() {
        return major;
    }

    /**
     * Get minor version number.
     * <p/>
     * @return Minor version number.
     */
    public short getMinor() {
        return minor;
    }

    /**
     * Get update version number.
     * <p/>
     * @return Update version number.
     */
    public short getUpdate() {
        return update;
    }

    /**
     * Get build version number.
     * <p/>
     * @return Build version number.
     */
    public short getBuild() {
        return build;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Compare major and minor parts of version number <code>String</code>s.
     * <p/>
     * @param version GlassFish server version to compare with this object.
     * @return Value of <code>true</code> when major and minor parts
     *         of version numbers are the same or <code>false</code> otherwise.
     */
    public boolean equalsMajorMinor(final GlassFishVersion version) {
        if (version == null) {
            return false;
        } else {
            return this.major == version.major && this.minor == version.minor;
        }
    }

    /**
     * Compare all parts of version number <code>String</code>s.
     * <p/>
     * @param version GlassFish server version to compare with this object.
     * @return Value of <code>true</code> when all parts of version numbers are
     *         the same or <code>false</code> otherwise.
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(final GlassFishVersion version) {
        if (version == null) {
            return false;
        } else {
            return this.major == version.major
                    && this.minor == version.minor
                    && this.update == version.update
                    && this.build == version.build;
        }
    }

    /** {@inheritDoc} */
    public static boolean eq(Enum<GlassFishVersion> v1, Enum<GlassFishVersion> v2) {
        return EnumUtils.eq(v1, v2);
    }

     /** {@inheritDoc} */
    public static boolean ne(Enum<GlassFishVersion> v1, Enum<GlassFishVersion> v2) {
        return EnumUtils.ne(v1, v2);
    }

     /** {@inheritDoc} */
    public static boolean lt(Enum<GlassFishVersion> v1, Enum<GlassFishVersion> v2) {
        return EnumUtils.lt(v1, v2);
    }

     /** {@inheritDoc} */
    public static boolean le(Enum<GlassFishVersion> v1, Enum<GlassFishVersion> v2) {
        return EnumUtils.le(v1, v2);
    }

    /** {@inheritDoc} */
    public static boolean gt(Enum<GlassFishVersion> v1, Enum<GlassFishVersion> v2) {
        return EnumUtils.gt(v1, v2);
    }

     /** {@inheritDoc} */
    public static boolean ge(Enum<GlassFishVersion> v1, Enum<GlassFishVersion> v2) {
        return EnumUtils.ge(v1, v2);
    }

    /**
     * Convert <code>GlassFishVersion</code> value to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Convert <code>GlassFishVersion</code> value to <code>String</code>
     * containing all version numbers.
     * <p/>
     * @return A <code>String</code> representation of the value of this object
     *         containing all version numbers.
     */
    public String toFullString() {
        StringBuilder sb = new StringBuilder(8);
        sb.append(Integer.toString(major));
        sb.append(SEPARATOR);
        sb.append(Integer.toString(minor));
        sb.append(SEPARATOR);
        sb.append(Integer.toString(update));
        sb.append(SEPARATOR);
        sb.append(Integer.toString(build));
        return sb.toString();
    }
    
    /**
     * Convert <code>GlassFishVersion</code> value to <code>int</code>
     * containing all version numbers.
     * <p/>
     * @return A <code>String</code> representation of the value of this object
     *         containing all version numbers.
     */
    public int toFullInteger() {
        StringBuilder sb = new StringBuilder(8);
        sb.append(Integer.toString(major));
        sb.append(Integer.toString(minor));
        sb.append(Integer.toString(update));
        sb.append(Integer.toString(build));
        return Integer.parseInt(sb.toString());
    }

}
