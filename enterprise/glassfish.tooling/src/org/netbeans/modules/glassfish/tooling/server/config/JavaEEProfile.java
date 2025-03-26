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
 * JavaEE profiles supported by Glassfish.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum JavaEEProfile {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** JavaEE 1.2. */
    v1_2(Version.v1_2, Type.FULL, "1.2"),

    /** JavaEE 1.3. */
    v1_3(Version.v1_3, Type.FULL, "1.3"),

    /** JavaEE 1.4. */
    v1_4(Version.v1_4, Type.FULL, "1.4"),

    /** JavaEE 1.5. */
    v1_5(Version.v1_5, Type.FULL, "1.5"),

    /** JavaEE 1.6 web profile. */
    v1_6_web(Version.v1_6, Type.WEB, "1.6-web"),

    /** JavaEE 1.6 full profile. */
    v1_6(Version.v1_6, Type.FULL, "1.6"),

    /** JavaEE 1.7 web profile. */
    v1_7_web(Version.v1_7, Type.WEB, "1.7-web"),

    /** JavaEE 1.7 full profile. */
    v1_7(Version.v1_7, Type.FULL, "1.7"),

    /** JavaEE 1.8 web profile. */
    v1_8_web(Version.v1_8, Type.WEB, "1.8-web"),

    /** JavaEE 1.8 full profile. */
    v1_8(Version.v1_8, Type.FULL, "1.8"),

    /** JakartaEE 8 web profile. */
    v8_0_0_web(Version.v8_0_0, Type.WEB, "8.0.0-web"),

    /** JakartaEE 8 full profile. */
    v8_0_0(Version.v8_0_0, Type.FULL, "8.0.0"),

    /** JakartaEE 9 web profile. */
    v9_0_0_web(Version.v9_0_0, Type.WEB, "9.0.0-web"),

    /** JakartaEE 9 full profile. */
    v9_0_0(Version.v9_0_0, Type.FULL, "9.0.0"),
    
    /** JakartaEE 9.1 web profile. */
    v9_1_0_web(Version.v9_1_0, Type.WEB, "9.1.0-web"),

    /** JakartaEE 9.1 full profile. */
    v9_1_0(Version.v9_1_0, Type.FULL, "9.1.0"),
    
    /** JakartaEE 10 web profile. */
    v10_0_0_web(Version.v10_0_0, Type.WEB, "10.0.0-web"),

    /** JakartaEE 10 full profile. */
    v10_0_0(Version.v10_0_0, Type.FULL, "10.0.0"),
    
    /** JakartaEE 11 web profile. */
    v11_0_0_web(Version.v11_0_0, Type.WEB, "11.0.0-web"),

    /** JakartaEE 11 full profile. */
    v11_0_0(Version.v11_0_0, Type.FULL, "11.0.0");

    ////////////////////////////////////////////////////////////////////////////
    // Inner enums                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** JavaEE profile type. */
    public enum Type {
        /** Web profile. */
        WEB("web"),
        /** Full profile. */
        FULL("full");

        /** JavaEE profile type name. */
        private final String name;

        /**
         * Creates an instance of JavaEE profile type.
         * <p/>
         * @param name JavaEE profile type name.
         */
        private Type(final String name) {
            this.name = name;
        }

        /**
         * Converts JavaEE profile type value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value
         *         of this object.
         */
        @Override
        public String toString() {
            return this.name;
        }
    }

    /** JavaEE version. */
    public enum Version {
        /** JavaEE 1.2. */
        v1_2("1.2"),
        /** JavaEE 1.3. */
        v1_3("1.3"),
        /** JavaEE 1.4. */
        v1_4("1.4"),
        /** JavaEE 1.5. */
        v1_5("1.5"),
        /** JavaEE 1.6. */
        v1_6("1.6"),
        /** JavaEE 1.7. */
        v1_7("1.7"),
        /** JavaEE 1.8. */
        v1_8("1.8"),
        /** JakartaEE 8.0. */
        v8_0_0("8.0.0"),
        /** JakartaEE 9.0. */
        v9_0_0("9.0.0"),
        /** JakartaEE 9.1. */
        v9_1_0("9.1.0"),
        /** JakartaEE 10 */
        v10_0_0("10.0.0"),
        /** JakartaEE 11 */
        v11_0_0("11.0.0");

        /** JavaEE profile type name. */
        private final String name;

        /**
         * Creates an instance of JavaEE profile type.
         * <p/>
         * @param name JavaEE profile type name.
         */
        private Version(final String name) {
            this.name = name;
        }

        /**
         * Converts JavaEE profile type value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value
         *         of this object.
         */
        @Override
        public String toString() {
            return this.name;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish JavaEE profile enumeration length. */
    public static final int length = JavaEEProfile.values().length;

    /** JavaEE profile type element separator character. */
    public static final char TYPE_SEPARATOR = '-';

    /**
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, JavaEEProfile> stringValuesMap
            = new HashMap<>(values().length);

    // Initialize backward String conversion Map.
    static {
        for (JavaEEProfile profile : JavaEEProfile.values()) {
            String[] names = createNames(profile);
            for (String name : names) {
                stringValuesMap.put(name.toUpperCase(), profile);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create JavaEE profile names to be recognized.
     * <p/>
     * @param profile JavaEE profile.
     * @return Array of names.
     * @throws ServerConfigException when JavaEE profile type is not recognized.
     */
    private static String[] createNames(final JavaEEProfile profile)
            throws ServerConfigException {
        String[] names;
        StringBuilder sb = new StringBuilder(profile.version.toString().length()
                + 1 + profile.type.toString().length());
        sb.append(profile.version.toString());
        sb.append(TYPE_SEPARATOR);
        sb.append(profile.type.toString());
        switch(profile.type) {
            // Full profile should recognize version base name and full name.
            case FULL:
                names = new String[2];
                names[0] = profile.version.toString();
                names[1] = sb.toString();
                break;
            // Web profile should regognize full name only .
            case WEB:
                names = new String[1];
                names[0] = sb.toString();
                break;
            // This is unrechable in regular conditions.
            default:
                throw new ServerConfigException(
                        ServerConfigException.INVALID_EE_PLATFORM_TYPE);
        }
        return names;
    }

    /**
     * Returns a <code>JavaEEProfile</code> with a value represented by the
     * specified <code>String</code>. The <code>JavaEEProfile</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param name Value containing <code>JavaEEProfile</code>
     *                    <code>toString</code> representation.
     * @return <code>JavaEEProfile</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static JavaEEProfile toValue(final String name) {
        if (name != null) {
            return (stringValuesMap.get(name.toUpperCase()));
        } else {
            return null;
        }
    }

    /**
     * Returns a <code>JavaEEProfile</code> with a value represented by the
     * specified <code>version</code> and <code>type</code>
     * <code>String</code>s. The <code>JavaEEProfile</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param version Value containing <code>JavaEEProfile</code> version
     *                <code>toString</code> representation.
     * @param type    Value containing <code>JavaEEProfile</code> type
     *                <code>toString</code> representation.
     * @return <code>JavaEEProfile</code> value represented
     *         by code>version</code> and <code>type</code> <code>String</code>
     *         or <code>null</code> if value was not recognized.
     */
    public static JavaEEProfile toValue(
            final String version, final String type) {
        if (version != null && type != null) {
            StringBuilder sb
                    = new StringBuilder(version.length() + 1 + type.length());
            sb.append(version);
            sb.append(TYPE_SEPARATOR);
            sb.append(type);
            return (stringValuesMap.get(sb.toString().toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** JavaEE profile version. */
    private final Version version;

    /** JavaEE profile type. */
    private final Type type;

    /** Name of JavaEE profile value. */
    private final String name;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of JavaEE profiles supported by Glassfish.
     * <p/>
     * @param version JavaEE profile version.
     * @param type    JavaEE profile type.
     * @param name    Name of JavaEE profile value.
     */
    private JavaEEProfile(
            final Version version, final Type type, final String name) {
        this.version = version;
        this.type = type;
        this.name = name;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Converts JavaEE profile version value to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Get profile type.
     * <p/>
     * @return Profile type.
     */
    public Type getType() {
        return type;
    }

}
