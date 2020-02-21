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
package org.netbeans.modules.cnd.debugger.gdb2;

import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 */
public class RedirectionPathTestCase extends TestCase {

    public RedirectionPathTestCase() {
    }
    
    private void assertRedirPaths(String runargs,
            String expInArg,
            String expOutArg,
            String expParStr) {
        
        String[] res = GdbDebuggerSettingsBridge.detectRedirect(runargs, "<");
        String inArg = res[0];
        runargs = res[1];
        
        res = GdbDebuggerSettingsBridge.detectRedirect(runargs, ">");
        String outArg = res[0];
        runargs = res[1];
        
        assertEquals(expInArg, inArg);
        assertEquals(expOutArg, outArg);
        assertEquals(expParStr, runargs);
    }
    
    @Test
    public void testRedirectionPath() {
        assertRedirPaths("\"${OUTPUT_PATH}\" \"arg1\"  </tmp/in \"arg\" >    /tmp/out \"arg u\"",
                "/tmp/in",
                "/tmp/out",
                "\"${OUTPUT_PATH}\" \"arg1\"  \"arg\" \"arg u\"");
    }
    
    @Test
    public void testRedirectionPath2() {
        assertRedirPaths("\"${OUTPUT_PATH}\" >/tmp/out \"arg\" <    /tmp/in",
                "/tmp/in",
                "/tmp/out",
                "\"${OUTPUT_PATH}\" \"arg\" ");
    }
    
    @Test
    public void testRedirectionPath3() {
        assertRedirPaths("\"${OUTPUT_PATH}\" >/tmp/out \"arg\" <   ",
                null,
                "/tmp/out",
                "\"${OUTPUT_PATH}\" \"arg\" ");
    }
    
    @Test
    public void testRedirectionPath4() {
        assertRedirPaths("\"${OUTPUT_PATH}\" < /tmp/in \"arg\" >",
                "/tmp/in",
                null,
                "\"${OUTPUT_PATH}\" \"arg\" ");
    }
    
    @Test
    public void testRedirectionPath5() {
        assertRedirPaths("\"${OUTPUT_PATH}\" <   \"/tmp/i n\" arg >",
                "\"/tmp/i n\"",
                null,
                "\"${OUTPUT_PATH}\" arg ");
    }
    
    @Test
    public void testRedirectionPathUnclosedQuote() {
        assertRedirPaths("\"${OUTPUT_PATH}\" <   \"/tmp/i n\" arg > \"/xxx",
                "\"/tmp/i n\"",
                "\"/xxx",
                "\"${OUTPUT_PATH}\" arg ");
    }
    
    @Test
    public void testRedirectionPathWithoutRedirections() {
        assertRedirPaths("\"${OUTPUT_PATH}\" \"arg 1\" \"arg 2\" \"arg 3\" \"arg 4\"",
                null,
                null,
                "\"${OUTPUT_PATH}\" \"arg 1\" \"arg 2\" \"arg 3\" \"arg 4\"");
    }
}