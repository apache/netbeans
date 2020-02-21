/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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