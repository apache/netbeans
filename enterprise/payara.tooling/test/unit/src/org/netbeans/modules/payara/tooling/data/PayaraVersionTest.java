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
package org.netbeans.modules.payara.tooling.data;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;


/**
 * Common Payara IDE SDK Exception functional test.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(groups = {"unit-tests"})
public class PayaraVersionTest {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test that <code>toValue</code> handles addition values for given version
     * and additional values array.
     */
    public static void verifyToValueFromAdditionalArray(
            PayaraVersion version, String[] values) {
        for (String value : values) {
            PayaraVersion pfVersion = PayaraVersion.toValue(value);
            assertTrue(pfVersion == version);
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
        for (PayaraVersion version : PayaraVersion.values()) {
            assertTrue(version.toString() != null);
        }
    }

    /**
     * Test that <code>toValue</code> handles all <code>enum</code> values
     * and that sequence of <code>toString</code> and <code>toValue</code>
     * calls ends up with supplied <code>PayaraVersion</code> version.
     */
    @Test
    public void testToValue() {
        for (PayaraVersion version : PayaraVersion.values()) {
            String stringValue = version.toString();
            PayaraVersion finalVersion = PayaraVersion.toValue(stringValue);
            assertTrue(version == finalVersion);
        }
        verifyToValueFromAdditionalArray(PayaraVersion.PF_4_1_144,
                PayaraVersion.PF_4_1_144_STR_NEXT);
        verifyToValueFromAdditionalArray(PayaraVersion.PF_4_1_1_154,
                PayaraVersion.PF_4_1_1_154_STR_NEXT);
        verifyToValueFromAdditionalArray(PayaraVersion.PF_4_1_2_181,
                PayaraVersion.PF_4_1_2_181_STR_NEXT);
        verifyToValueFromAdditionalArray(PayaraVersion.PF_5_183,
                PayaraVersion.PF_5_183_STR_NEXT);
    }

    /**
     * Verify some incomplete <code>toValue</code> resolutions.
     */
    @Test
    public void testToValueIncomplete() {
        PayaraVersion versions[] = {
            PayaraVersion.PF_4_1_144,
            PayaraVersion.PF_4_1_1_154,
            PayaraVersion.PF_4_1_2_181,
            PayaraVersion.PF_5_183
        };
        String strings[] = {
            "4.1.144",
            "4.1.1.154",
            "4.1.2.181",
            "5.183"
        };
        for (int i = 0; i < versions.length; i++) {
            PayaraVersion version = PayaraVersion.toValue(strings[i]);
            assertTrue(versions[i].equals(version));
        }
    }

    /**
     * Verify <code>toFullString</code> method.
     */
    @Test
    public void testToFullString() {
        for (PayaraVersion version : PayaraVersion.values()) {
            String fullVersion = version.toFullString();
            String[] numbers
                    = fullVersion.split(PayaraVersion.SEPARATOR_PATTERN);
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
