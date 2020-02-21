/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
