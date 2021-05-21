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
package org.netbeans.modules.payara.tooling.utils;

import org.netbeans.modules.payara.tooling.CommonTest;
import org.netbeans.modules.payara.tooling.logging.Logger;
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
