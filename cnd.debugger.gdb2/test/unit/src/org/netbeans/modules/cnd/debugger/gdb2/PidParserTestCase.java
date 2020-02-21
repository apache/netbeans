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
