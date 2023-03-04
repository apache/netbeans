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
package org.openide.util;

import java.util.HashMap;
import junit.framework.*;
import org.openide.util.MapFormat;

/**
 *
 * @author Jaroslav Tulach
 */
public class MapFormatTest extends TestCase {

    public MapFormatTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    public void testFormatIssue67238() {

        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");

        MapFormat f = new MapFormat(args);
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        String result = f.format("/*_____________________*/\n/*__NAME__*/");
        
        assertEquals("Should be ok: " + result, "/*_____________________*/\n/*Jaroslav*/", result);
    }
    
    public void testExectLineWithTheProblemFromFormatIssue67238 () {
        String s = "/*___________________________________________________________________________*/";
        
        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");

        MapFormat f = new MapFormat(args);
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        String result = f.format(s);
        
        assertEquals("Should be ok: " + result, s, result);
        
    }
    
    public void testIssue67238() {
        final String s = "/*___________________________________________________________________________*/";
        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");
        MapFormat f = new MapFormat(args) {
            protected Object processKey(String key) {
                fail("There is no key in \"" + s + "\", processKey() should not be called with key:" + key);
                return "not defined";
            }
        };
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        f.format(s);
    }
}
