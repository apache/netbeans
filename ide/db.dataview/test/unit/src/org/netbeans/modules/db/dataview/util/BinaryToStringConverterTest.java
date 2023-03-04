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

package org.netbeans.modules.db.dataview.util;

import java.io.UnsupportedEncodingException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBException;

/**
 *
 * @author jawed
 */
public class BinaryToStringConverterTest extends NbTestCase {
    
    public BinaryToStringConverterTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(BinaryToStringConverterTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of convertToString method, of class BinaryToStringConverter.
     */
    public void testConvertToString() {
        byte[] data = new byte[1];
        int base = 10;
        boolean showAscii = false;
        String expResult = "022";
        data[0] = new Byte(expResult);
        String result = BinaryToStringConverter.convertToString(data, base, showAscii);
        assertEquals(expResult, result);
    }

    /**
     * Test SQL hexadecimal encoding - adapted from
     * http://dev.mysql.com/doc/refman/5.0/en/hexadecimal-literals.html
     */
    public void testConvertToString2() throws UnsupportedEncodingException,
            DBException {
        byte[] data = "\u0000MySQL".getBytes("ASCII");
        int base = 16;
        boolean showAscii = false;
        String expResult = "004d7953514c";
        String result = BinaryToStringConverter.convertToString(
                data, base, showAscii);
        assertEquals(expResult, result);

        data = "Paul".getBytes("ASCII");
        expResult = "5061756c";
        result = BinaryToStringConverter.convertToString(data, base, showAscii);
        assertEquals(expResult, result);
    }
}
