/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
