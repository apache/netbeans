/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.gdb2;

import java.util.Collections;
import junit.framework.TestCase;
import org.junit.Test;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.TestMICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.TestMIRecord;

/**
 *
 */
public class PidParserTestCase extends TestCase {

    public PidParserTestCase() {
    }
    
    private static MIRecord createRecord(String consoleStream) {
        TestMICommand cmd = new TestMICommand(0, "Test command");
        cmd.recordConsoleStream(Collections.singletonList(consoleStream));
        TestMIRecord res  = new TestMIRecord();
        res.setCommand(cmd);
        return res;
    }
    
    @Test
    public void testPidParsing() {
        MIRecord res = createRecord("process 12345 flags:\n");
        assertEquals(12345, GdbDebuggerImpl.extractPid1(res));
    }
    
    @Test
    public void test196768() {
        MIRecord res = createRecord("Current language:  auto\nThe current source language is \"auto; currently asm\".\nprocess 12345\ncmdline = '/home/irad/private/work/edgeci-lib/reuters-analyser/trunk/projects/hotspot_tester/build_linux/source/hotspot_tester'\ncwd = '/home/irad/private/work/edgeci-lib/reuters-analyser/trunk/projects/hotspot_tester'\nexe = '/home/irad/private/work/edgeci-lib/reuters-analyser/trunk/projects/hotspot_tester/build_linux/source/hotspot_tester'\n");
        assertEquals(12345, GdbDebuggerImpl.extractPid1(res));
    }
    
    @Test
    public void test204711() {
        MIRecord res = createRecord("  Id   Target Id         Frame \n  2    Thread 1920.0xc7c 0x77a9f8f5 in ntdll!RtlUpdateClonedSRWLock () from /cygdrive/c/Windows/system32/ntdll.dll\n* 1    Thread 1920.0xf34 main (argc=1, argv=0xf99eb0) at main.c:319\n");
        assertEquals(1920, GdbDebuggerImpl.extractPidThreads(res));
    }
}
