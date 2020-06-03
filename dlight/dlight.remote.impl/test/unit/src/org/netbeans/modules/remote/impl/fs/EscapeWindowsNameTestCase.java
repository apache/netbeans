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

package org.netbeans.modules.remote.impl.fs;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.openide.util.Utilities;

/**
 *
 */
public class EscapeWindowsNameTestCase extends NativeExecutionBaseTestCase {

    private static final boolean trace = false;
    
    public EscapeWindowsNameTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        RemoteFileSystemUtils.testSetWindows(true);
    }

    @Override
    protected void tearDown() throws Exception {
        RemoteFileSystemUtils.testSetWindows(Utilities.isWindows());
    }

    private void checkEscapedDifferUnescapedSame(String name) {
        String escaped = RemoteFileSystemUtils.escapeFileName(name);
        if (trace) {
            System.err.printf("%s -> %s\n", name, escaped);
        }
        assertFalse("Escaped name should differ for " + name, escaped.equals(name));
        String unescaped = RemoteFileSystemUtils.unescapeFileName(escaped);
        assertEquals("Unescaped name differ from original one", name, unescaped);
        String escaped2 = RemoteFileSystemUtils.escapeFileName(escaped);
        if (trace) {
            System.err.printf("%s -> %s\n", escaped, escaped2);
        }
        assertFalse("Escaped twice should differ: " + name + " -> " + escaped + " -> " + escaped2, escaped2.equals(escaped));
    }

    private void checkReservedName(String name) {
        checkEscapedDifferUnescapedSame(name);
        checkEscapedDifferUnescapedSame(name + ".dat");
    }

    private void checkEscape(String name, String expectedEscapedName) throws Exception {
        String escaped = RemoteFileSystemUtils.escapeFileName(name);
        assertEquals("Escaped name", expectedEscapedName, escaped);
    }

    public void testReservedChars() throws Exception {
        char[] reserved = new char[] { '<', '>', ':', '"', '/', '\\', '|', '?', '*' };
        for (char c : reserved) {
            checkEscapedDifferUnescapedSame("qwe" + c + "asd");
            checkEscapedDifferUnescapedSame(c + "zxc");
            checkEscapedDifferUnescapedSame("file" + c);
            checkEscapedDifferUnescapedSame("file." + c);
        }
        Map<String, String> escpedToOrig = new HashMap<>();
        for (char c : reserved) {
            String orig = "xxx" + c + "yyy";
            String escaped = RemoteFileSystemUtils.escapeFileName(orig);
            String prev = escpedToOrig.get(escaped);
            assertNull("Escaped coniside for " + orig + " and " + prev, prev);
            escpedToOrig.put(escaped, orig);
        }
    }

    public void testReservedNames() throws Exception {
        checkEscape("CON", "_RCON");
        checkEscape("_RCON", "__RCON");
        checkReservedName("CON");
        checkReservedName("PRN");
        checkReservedName("AUX");
        checkReservedName("NUL");
        checkReservedName("COM1");
        checkEscape("COM1", "_RCOM1");
        checkEscape("_RCOM1", "__RCOM1");
        checkReservedName("COM1");
        checkReservedName("COM2");
        checkReservedName("COM3");
        checkReservedName("COM4");
        checkReservedName("COM5");
        checkReservedName("COM6");
        checkReservedName("COM7");
        checkReservedName("COM8");
        checkReservedName("COM9");
        checkReservedName("LPT1");
        checkEscape("LPT1", "_RLPT1");
        checkEscape("_RLPT1", "__RLPT1");
        checkReservedName("LPT2");
        checkReservedName("LPT3");
        checkReservedName("LPT4");
        checkReservedName("LPT5");
        checkReservedName("LPT6");
        checkReservedName("LPT7");
        checkReservedName("LPT8");
        checkReservedName("LPT9");
    }
}
