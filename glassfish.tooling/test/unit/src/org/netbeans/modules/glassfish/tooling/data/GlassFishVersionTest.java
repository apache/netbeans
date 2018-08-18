/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * https://java.net/projects/gf-tooling/pages/License or LICENSE.TXT.
 * See the License for the specific language governing permissions
 * and limitations under the License.  When distributing the software,
 * include this License Header Notice in each file and include the License
 * file at LICENSE.TXT. Oracle designates this particular file as subject
 * to the "Classpath" exception as provided by Oracle in the GPL Version 2
 * section of the License file that accompanied this code. If applicable,
 * add the following below the License Header, with the fields enclosed
 * by brackets [] replaced by your own identifying information:
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
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    public static void verifyToValueFromAdditionalArray(
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
    }

    /**
     * Verify some incomplete <code>toValue</code> resolutions.
     */
    @Test
    public void testToValueIncomplete() {
        GlassFishVersion versions[] = {
            GlassFishVersion.GF_1,
            GlassFishVersion.GF_2,
            GlassFishVersion.GF_2_1,
            GlassFishVersion.GF_2_1_1,
            GlassFishVersion.GF_3,
            GlassFishVersion.GF_3_1_2,
            GlassFishVersion.GF_3_1_2_2,
            GlassFishVersion.GF_3_1_2_3,
            GlassFishVersion.GF_3_1_2_4
        };
        String strings[] = {
            "1.0.1.4",
            "2.0.1.5",
            "2.1.0.3",
            "2.1.1.7",
            "3.0.0.1",
            "3.1.2.1",
            "3.1.2.2",
            "3.1.2.3",
            "3.1.2.4"
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
