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

package org.netbeans.modules.cnd.debugger.gdb2.peculiarity;

import junit.framework.TestCase;
import org.junit.Test;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Platform;
import org.netbeans.modules.cnd.debugger.gdb2.GdbVersionPeculiarity;

/**
 *
 */
public class PeculiarityTestCase extends TestCase {

    public PeculiarityTestCase() {
    }
    
    @Test
    public void testGdb70SolarisCommandSet() {
        GdbVersionPeculiarity peculiarity = GdbVersionPeculiarity.create(new GdbVersionPeculiarity.Version(7, 0), Platform.Solaris_x86);
        
        assertTrue(peculiarity.isSupported());
        assertEquals("-environment-cd", peculiarity.environmentCdCommand());
        assertEquals("-exec-step", peculiarity.execStepCommand("0"));
        assertEquals("-exec-next", peculiarity.execNextCommand("0"));
        assertEquals("-exec-step-instruction", peculiarity.execStepInstCommand("0"));
        assertEquals("-exec-next-instruction", peculiarity.execNextInstCommand("0"));
        assertEquals("-exec-finish", peculiarity.execFinishCommand("0"));
        assertEquals("-var-list-children --all-values \"var0\" 1 100", peculiarity.listChildrenCommand("var0", 1, 100));
        assertEquals("-var-create - @ name", peculiarity.createVarCommand("name", "0", "1"));
        assertEquals("-stack-list-frames", peculiarity.stackListFramesCommand("0"));
    }
    
    @Test
    public void testGdb77SolarisCommandSet() {
        GdbVersionPeculiarity peculiarity = GdbVersionPeculiarity.create(new GdbVersionPeculiarity.Version(7, 7), Platform.Solaris_x86);
        
        assertTrue(peculiarity.isSupported());
        assertEquals("-environment-cd", peculiarity.environmentCdCommand());
        assertEquals("-exec-step --thread 0", peculiarity.execStepCommand("0"));
        assertEquals("-exec-next --thread 0", peculiarity.execNextCommand("0"));
        assertEquals("-exec-step-instruction --thread 0", peculiarity.execStepInstCommand("0"));
        assertEquals("-exec-next-instruction --thread 0", peculiarity.execNextInstCommand("0"));
        assertEquals("-exec-finish", peculiarity.execFinishCommand("0"));
        assertEquals("-var-list-children --all-values \"var0\" 1 100", peculiarity.listChildrenCommand("var0", 1, 100));
        assertEquals("-var-create - @ name", peculiarity.createVarCommand("name", "0", "1"));
        assertEquals("-stack-list-frames --thread 0", peculiarity.stackListFramesCommand("0"));
    }
    
    @Test
    public void testGdb710SolarisCommandSet() {
        GdbVersionPeculiarity peculiarity = GdbVersionPeculiarity.create(new GdbVersionPeculiarity.Version(7, 10), Platform.Solaris_x86);
        
        assertTrue(peculiarity.isSupported());
        assertEquals("-environment-cd", peculiarity.environmentCdCommand());
        assertEquals("-exec-step --thread 0", peculiarity.execStepCommand("0"));
        assertEquals("-exec-next --thread 0", peculiarity.execNextCommand("0"));
        assertEquals("-exec-step-instruction --thread 0", peculiarity.execStepInstCommand("0"));
        assertEquals("-exec-next-instruction --thread 0", peculiarity.execNextInstCommand("0"));
        assertEquals("-exec-finish", peculiarity.execFinishCommand("0"));
        assertEquals("-var-list-children --all-values \"var0\" 1 100", peculiarity.listChildrenCommand("var0", 1, 100));
        assertEquals("-var-create - @ name", peculiarity.createVarCommand("name", "0", "1"));
        assertEquals("-stack-list-frames --thread 0", peculiarity.stackListFramesCommand("0"));
    }
    
    @Test
    public void testLldbMiMacCommandSet() {
        System.setProperty("cnd.debugger.lldb","true");
        GdbVersionPeculiarity peculiarity = GdbVersionPeculiarity.create(new GdbVersionPeculiarity.Version(6, 8), Platform.MacOSX_x86);
        System.setProperty("cnd.debugger.lldb","false");
        
        assertTrue(peculiarity.isSupported());
        assertEquals("-environment-cd", peculiarity.environmentCdCommand());
        assertEquals("-exec-step --thread 0", peculiarity.execStepCommand("0"));
        assertEquals("-exec-next --thread 0", peculiarity.execNextCommand("0"));
        assertEquals("-exec-step-instruction --thread 0", peculiarity.execStepInstCommand("0"));
        assertEquals("-exec-next-instruction --thread 0", peculiarity.execNextInstCommand("0"));
        assertEquals("-exec-finish --thread 0", peculiarity.execFinishCommand("0"));
        assertEquals("-var-list-children --all-values var0", peculiarity.listChildrenCommand("var0", 1, 100));
        assertEquals("-var-create - @ name --thread 0 --frame 1", peculiarity.createVarCommand("name", "0", "1"));
        assertEquals("-stack-list-frames --thread 0", peculiarity.stackListFramesCommand("0"));
    }
    
    @Test
    public void testGdbMacCommandSet() {
        GdbVersionPeculiarity peculiarity = GdbVersionPeculiarity.create(new GdbVersionPeculiarity.Version(6, 3), Platform.MacOSX_x86);
        
        assertTrue(peculiarity.isSupported());
        assertEquals("cd", peculiarity.environmentCdCommand());
        assertEquals("-exec-step", peculiarity.execStepCommand("0"));
        assertEquals("-exec-next", peculiarity.execNextCommand("0"));
        assertEquals("-exec-step-instruction", peculiarity.execStepInstCommand("0"));
        assertEquals("-exec-next-instruction", peculiarity.execNextInstCommand("0"));
        assertEquals("-exec-finish", peculiarity.execFinishCommand("0"));
        assertEquals("-var-list-children --all-values \"var0\"", peculiarity.listChildrenCommand("var0", 1, 100));
        assertEquals("-var-create - @ name", peculiarity.createVarCommand("name", "0", "1"));
        assertEquals("-stack-list-frames", peculiarity.stackListFramesCommand("0"));
    }
    
    @Test
    public void testUnsupportedVersion() {
        GdbVersionPeculiarity peculiarity = GdbVersionPeculiarity.create(new GdbVersionPeculiarity.Version(6, 3), Platform.Solaris_x86);
        
        assertFalse(peculiarity.isSupported());
    }
}
