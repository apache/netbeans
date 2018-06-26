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
package org.netbeans.modules.glassfish.tooling.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
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
    /**  GlassFish v2. */
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
    /** GlassFish 5. */
    GF_5        ((short) 5, (short) 0, (short) 0, (short) 0, GlassFishVersion.GF_5_STR);

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

    /**  A <code>String</code> representation of GF_5 value. */
    static final String GF_5_STR = "5";
    /** Additional <code>String</code> representations of GF_5 value. */
    static final String GF_5_STR_NEXT[] = {"5.0", "5.0.0", "5.0.0.0"};

    /** 
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, GlassFishVersion> stringValuesMap
            = new HashMap(2 * values().length);

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
        initStringValuesMapFromArray(GF_5, GF_5_STR_NEXT);
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
            String[] versionNumbers = versionStr.split("\\"+SEPARATOR);
            for (int i = versionNumbers.length - 1;
                    version == null && i > 0; i--) {
                int versionStrLen = i - 1;
                for (int j = 0; j < i; j++) {
                    versionStrLen += versionNumbers[j].length();
                }
                StringBuilder sb = new StringBuilder(versionStrLen);
                for (int j = 0; j < i; j++) {
                    if (j > 0) {
                        sb.append(SEPARATOR);
                    }
                    try {
                        Integer.parseInt(versionNumbers[j]);
                        sb.append(versionNumbers[j]);
                    } catch (NumberFormatException ex) {
                        break;
                    }
                }
                version = stringValuesMap.get(sb.toString().toUpperCase(Locale.ENGLISH));
            }
            if (version == null) {
                // fallback attempt
                int dot = versionStr.indexOf('.');
                if (dot > 0) {
                    try {
                        int major = Integer.parseInt(versionStr.substring(0, dot));
                        // this needs enum to be properly ordered latest versions last
                        for (GlassFishVersion v : values()) {
                            if (v.major <= major) {
                                version = v;
                            } else if (v.major > major) {
                                break;
                            }
                        }
                    } catch (NumberFormatException ex) {
                        // noop
                    }
                }
            }
        }
        return version;
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

}
