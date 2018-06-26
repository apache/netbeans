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
package org.netbeans.modules.glassfish.tooling.utils;

import org.netbeans.modules.glassfish.tooling.CommonTest;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Test Cyclic <code>String</code> buffer functionality.
 * <p/>
 * @author Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class CyclicStringBufferTest extends CommonTest{
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CyclicStringBufferTest.class);

    /** String data for testing. */
    private static final String DATA
            = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Test cyclic buffer appends and {@link CyclicStringBuffer#toString()}.
     */
    @Test
    public void testAppend() {
        for (int size = 1; size < 20; size++) {
            CyclicStringBuffer buff = new CyclicStringBuffer(size);
            // Filling buffer.
            for (int i = 0; i < size; i++) {
                buff.append(DATA.charAt(i));
                String template = DATA.substring(0, i + 1);
                String buffStr = buff.toString();
                assertEquals(template, buffStr,
                        "Strings "+template+" and "+buffStr+" are not equal");
            }
            // Overwritting buffer.
            for (int i = size; i < 2 * size; i++) {
                buff.append(DATA.charAt(i));
                String template = DATA.substring(i - size + 1, i + 1);
                String buffStr = buff.toString();
                assertEquals(template, buffStr,
                        "Strings "+template+" and "+buffStr+" are not equal");
            }
        }
    }

    /**
     * Test cyclic buffer prepends {@link CyclicStringBuffer#toString()}.
     */
    @Test
    public void testPrepend() {
        for (int size = 1; size < 20; size++) {
            CyclicStringBuffer buff = new CyclicStringBuffer(size);
            int dataLen = DATA.length();
            // Filling buffer.
            for (int i = 0; i < size; i++) {
                buff.prepend(DATA.charAt(dataLen - i - 1));
                String template = DATA.substring(dataLen - i - 1, dataLen);
                String buffStr = buff.toString();
                assertEquals(template, buffStr,
                        "Strings "+template+" and "+buffStr+" are not equal");
            }
            // Overwritting buffer.
            for (int i = size; i < 2 * size; i++) {
                buff.prepend(DATA.charAt(dataLen - i - 1));
                String template = DATA.substring(
                        dataLen - i - 1, dataLen + size - i - 1);
                String buffStr = buff.toString();
                assertEquals(template, buffStr,
                        "Strings "+template+" and "+buffStr+" are not equal");
            }
        }
    }

    /**
     * Test cyclic buffer {@link CyclicStringBuffer#equals(String)}.
     */
    @Test
    public void testEquals() {
        for (int size = 1; size < 20; size++) {
            CyclicStringBuffer buff = new CyclicStringBuffer(size);
            int dataLen = DATA.length();
            for (int i = 0; i < size; i++) {
                buff.append(DATA.charAt(i));
                String equal = DATA.substring(0, i + 1);
                String differs = DATA.substring(1, i + 2);
                assertTrue(buff.equals(equal),
                        "Strings "+equal+" and "+buff.toString()
                        +" are equal.");
                assertFalse(buff.equals(differs),
                        "Strings "+differs+" and "+buff.toString()
                        +" are not equal.");
            }
        }
    }

}
