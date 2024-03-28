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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;


/**
 * Common GlassFish IDE SDK Exception functional test.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(groups = {"unit-tests"})
public class GlassFishVersionTest {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test that <code>toValue</code> handles addition values for given version
     * and additional values array.
     */
    private static void verifyToValueFromAdditionalArray(
            GlassFishVersion version, String[] values) {
        for (String value : values) {
            GlassFishVersion gfVersion = GlassFishVersion.toValue(value);
            assertTrue(gfVersion == version);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test that <code>toString</code> handles all <code>enum</code> values.
     */
    @Test
    public void testToString() {
        for (GlassFishVersion version : GlassFishVersion.values()) {
            assertTrue(version.toString() != null);
        }
    }

    /**
     * Test that <code>toValue</code> handles all <code>enum</code> values
     * and that sequence of <code>toString</code> and <code>toValue</code>
     * calls ends up with supplied <code>GlassFishVersion</code> version.
     */
    @Test
    public void testToValue() {
        for (GlassFishVersion version : GlassFishVersion.values()) {
            String stringValue = version.toString();
            GlassFishVersion finalVersion = GlassFishVersion.toValue(stringValue);
            assertTrue(version == finalVersion);
        }
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_1,
                GlassFishVersion.GF_1_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_2,
                GlassFishVersion.GF_2_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_2_1,
                GlassFishVersion.GF_2_1_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_3,
                GlassFishVersion.GF_3_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_3_1,
                GlassFishVersion.GF_3_1_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_4,
                GlassFishVersion.GF_4_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_4_1_2,
                GlassFishVersion.GF_4_1_2_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_5,
                GlassFishVersion.GF_5_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_5_1_0,
                GlassFishVersion.GF_5_1_0_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_6,
                GlassFishVersion.GF_6_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_6_1_0,
                GlassFishVersion.GF_6_1_0_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_6_2_5,
                GlassFishVersion.GF_6_2_5_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_0,
                GlassFishVersion.GF_7_0_0_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_1,
                GlassFishVersion.GF_7_0_1_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_2,
                GlassFishVersion.GF_7_0_2_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_3,
                GlassFishVersion.GF_7_0_3_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_4,
                GlassFishVersion.GF_7_0_4_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_5,
                GlassFishVersion.GF_7_0_5_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_6,
                GlassFishVersion.GF_7_0_6_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_7,
                GlassFishVersion.GF_7_0_7_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_8,
                GlassFishVersion.GF_7_0_8_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_9,
                GlassFishVersion.GF_7_0_9_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_10,
                GlassFishVersion.GF_7_0_10_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_11,
                GlassFishVersion.GF_7_0_11_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_12,
                GlassFishVersion.GF_7_0_12_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_7_0_13,
                GlassFishVersion.GF_7_0_13_STR_NEXT);
        verifyToValueFromAdditionalArray(GlassFishVersion.GF_8_0_0,
                GlassFishVersion.GF_8_0_0_STR_NEXT);
    }

    /**
     * Verify some incomplete <code>toValue</code> resolutions.
     */
    @Test
    public void testToValueIncomplete() {
        GlassFishVersion versions[] = {
            GlassFishVersion.GF_1, GlassFishVersion.GF_2,
            GlassFishVersion.GF_2_1, GlassFishVersion.GF_2_1_1,
            GlassFishVersion.GF_3, GlassFishVersion.GF_3_1_2,
            GlassFishVersion.GF_3_1_2_2, GlassFishVersion.GF_3_1_2_3,
            GlassFishVersion.GF_3_1_2_4, GlassFishVersion.GF_3_1_2_5,
            GlassFishVersion.GF_4, GlassFishVersion.GF_4_0_1,
            GlassFishVersion.GF_4_1, GlassFishVersion.GF_4_1_1,
            GlassFishVersion.GF_4_1_2, GlassFishVersion.GF_5,
            GlassFishVersion.GF_5_0_1, GlassFishVersion.GF_5_1_0,
            GlassFishVersion.GF_6, GlassFishVersion.GF_6_1_0,
            GlassFishVersion.GF_6_2_0, GlassFishVersion.GF_6_2_1,
            GlassFishVersion.GF_6_2_2, GlassFishVersion.GF_6_2_3,
            GlassFishVersion.GF_6_2_4, GlassFishVersion.GF_6_2_5,
            GlassFishVersion.GF_7_0_0, GlassFishVersion.GF_7_0_1,
            GlassFishVersion.GF_7_0_2, GlassFishVersion.GF_7_0_3,
            GlassFishVersion.GF_7_0_4, GlassFishVersion.GF_7_0_5,
            GlassFishVersion.GF_7_0_6, GlassFishVersion.GF_7_0_7,
            GlassFishVersion.GF_7_0_8, GlassFishVersion.GF_7_0_9,
            GlassFishVersion.GF_7_0_10, GlassFishVersion.GF_7_0_11,
            GlassFishVersion.GF_7_0_12, GlassFishVersion.GF_7_0_13,
            GlassFishVersion.GF_8_0_0
        };
        String strings[] = {
            "1.0.1.4", "2.0.1.5", "2.1.0.3", "2.1.1.7",
            "3.0.0.1", "3.1.2.1", "3.1.2.2", "3.1.2.3",
            "3.1.2.4", "3.1.2.5", "4.0.0.0", "4.0.1.0",
            "4.1.0.0", "4.1.1.0", "4.1.2.0", "5.0.0.0",
            "5.0.1.0", "5.1.0.0", "6.0.0.0", "6.1.0.0",
            "6.2.0.0", "6.2.1.0", "6.2.2.0", "6.2.3.0",
            "6.2.4.0", "6.2.5.0", "7.0.0.0", "7.0.1.0",
            "7.0.2.0", "7.0.3.0", "7.0.4.0", "7.0.5.0",
            "7.0.6.0", "7.0.7.0", "7.0.8.0", "7.0.9.0",
            "7.0.10.0", "7.0.11.0", "7.0.12.0", "7.0.13.0",
            "8.0.0.0"
        };
        for (int i = 0; i < versions.length; i++) {
            GlassFishVersion version = GlassFishVersion.toValue(strings[i]);
            assertTrue(versions[i].equals(version));
        }
    }

    /**
     * Verify <code>toFullString</code> method.
     */
    @Test
    public void testToFullString() {
        for (GlassFishVersion version : GlassFishVersion.values()) {
            String fullVersion = version.toFullString();
            String[] numbers
                    = fullVersion.split(GlassFishVersion.SEPARATOR_PATTERN);
            assertTrue(numbers != null && numbers.length == 4,
                    "Invalid count of version numbers");
            short major, minor, update, build;
            try {
                major  = Short.parseShort(numbers[0]);
                minor  = Short.parseShort(numbers[1]);
                update = Short.parseShort(numbers[2]);
                build  = Short.parseShort(numbers[3]);
                assertTrue(major == version.getMajor()
                        && minor == version.getMinor()
                        && update == version.getUpdate()
                        && build == version.getBuild());
            } catch (NumberFormatException nfe) {
                fail("Could not parse version number");
            }

        }
    }

}
