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

package org.netbeans.lib.v8debug.connection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test of parsing unicode characters.
 * 
 * @author Martin Entlicher
 */
public class UnicodeJSONParserTest {
    
    public UnicodeJSONParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class JSONParser.
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        String js1 = "{ \"name\": \"";
        String js2 = "\" }";
        int[] codePoints = new int[] { 0 };
        JSONParser parser = new JSONParser();
        for (int c = 32; c <= 0x10FFFF; c++) {
            if ('\"' == c || '\\' == c) {
                continue;
            }
            codePoints[0] = c;
            String str = new String(codePoints, 0, 1);
            String msg = js1 + str + js2;
            Object result;
            try {
                result = parser.parse(msg);
            } catch (ParseException pex) {
                System.err.println("Error parsing "+msg);
                throw pex;
            }
            assertTrue(result.toString(), result instanceof JSONObject);
            Object value = ((JSONObject) result).get("name");
            assertEquals(str, (String) value);
        }
    }
    
}
