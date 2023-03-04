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
            PayaraPlatformVersionAPI version, String[] values) {
        for (String value : values) {
            PayaraPlatformVersionAPI pfVersion = PayaraPlatformVersion.toValue(value);
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
        for (PayaraPlatformVersionAPI version : PayaraPlatformVersion.getVersions()) {
            assertTrue(version.toString() != null);
        }
    }

    /**
     * Test that <code>toValue</code> handles all <code>enum</code> values
     * and that sequence of <code>toString</code> and <code>toValue</code>
     * calls ends up with supplied <code>PayaraPlatformVersionAPI</code> version.
     */
    @Test
    public void testToValue() {
        for (PayaraPlatformVersionAPI version : PayaraPlatformVersion.getVersions()) {
            String stringValue = version.toString();
            PayaraPlatformVersionAPI finalVersion = PayaraPlatformVersion.toValue(stringValue);
            assertTrue(version == finalVersion);
        }
    }

    /**
     * Verify <code>toFullString</code> method.
     */
    @Test
    public void testToFullString() {
        for (PayaraPlatformVersionAPI version : PayaraPlatformVersion.getVersions()) {
            String fullVersion = version.toFullString();
            String[] numbers
                    = fullVersion.split(PayaraPlatformVersionAPI.SEPARATOR_PATTERN);
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
