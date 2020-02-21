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
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIParser;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;

/**
 *
 */
public class MemParserTestCase extends TestCase {

    public MemParserTestCase() {
    }
    
    private MIRecord prepareRecord(String testLine){
        MIParser parser = new MIParser("Cp1251");
        parser.setup(testLine);
        return parser.parse();
    }
    
    @Test
    public void testHexMemParsing() {
        MIRecord res = prepareRecord("^done,addr=\"0x08058fea\",nr-bytes=\"22\",total-bytes=\"32\",next-row=\"0x08058ffa\",prev-row=\"0x08058fda\",next-page=\"0x0805900a\",prev-page=\"0x08058fca\",memory=[{addr=\"0x08058fea\",data=[\"0x42\",\"0x0d\",\"0x05\",\"0x4f\",\"0xc5\",\"0x0c\",\"0x04\",\"0x04\",\"0x00\",\"0x00\",\"0x1c\",\"0x00\",\"0x00\",\"0x00\",\"0x44\",\"0x01\"],ascii=\"B..O..........D.\"},{addr=\"0x08058ffa\",data=[\"0x00\",\"0x00\",\"0x4a\",\"0xca\",\"0xfe\",\"0xff\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\"],ascii=\"..J...XXXXXXXXXX\"}]");
        
        String expRes = "0x08058fea: 0x42 0x0d 0x05 0x4f 0xc5 0x0c 0x04 0x04 0x00 0x00 0x1c 0x00 0x00 0x00 0x44 0x01 \"B..O..........D.\"\n";
        assertEquals(expRes, GdbDebuggerImpl.parseMem(res).get(0));
        
        expRes = "0x08058ffa: 0x00 0x00 0x4a 0xca 0xfe 0xff  N/A  N/A  N/A  N/A  N/A  N/A  N/A  N/A  N/A  N/A \"..J...XXXXXXXXXX\"\n";
        assertEquals(expRes, GdbDebuggerImpl.parseMem(res).get(1));
    }
    
    @Test
    public void testBinMemParsing() {
        MIRecord res = prepareRecord("^done,addr=\"0x08058fea\",nr-bytes=\"22\",total-bytes=\"32\",next-row=\"0x08058ffa\",prev-row=\"0x08058fda\",next-page=\"0x0805900a\",prev-page=\"0x08058fca\",memory=[{addr=\"0x08058fea\",data=[\"01000010\",\"00001101\",\"00000101\",\"01001111\",\"11000101\",\"00001100\",\"00000100\",\"00000100\",\"00000000\",\"00000000\",\"00011100\",\"00000000\",\"00000000\",\"00000000\",\"01000100\",\"00000001\"],ascii=\"B..O..........D.\"},{addr=\"0x08058ffa\",data=[\"00000000\",\"00000000\",\"01001010\",\"11001010\",\"11111110\",\"11111111\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\"],ascii=\"..J...XXXXXXXXXX\"}]");

        String expRes = "0x08058fea: 01000010 00001101 00000101 01001111 11000101 00001100 00000100 00000100 00000000 00000000 00011100 00000000 00000000 00000000 01000100 00000001 \"B..O..........D.\"\n";
        assertEquals(expRes, GdbDebuggerImpl.parseMem(res).get(0));
        
        expRes = "0x08058ffa: 00000000 00000000 01001010 11001010 11111110 11111111      N/A      N/A      N/A      N/A      N/A      N/A      N/A      N/A      N/A      N/A \"..J...XXXXXXXXXX\"\n";
        assertEquals(expRes, GdbDebuggerImpl.parseMem(res).get(1));
    }
    
    @Test
    public void testDecMemParsing() {
        MIRecord res = prepareRecord("^done,addr=\"0x08058fea\",nr-bytes=\"22\",total-bytes=\"32\",next-row=\"0x08058ffa\",prev-row=\"0x08058fda\",next-page=\"0x0805900a\",prev-page=\"0x08058fca\",memory=[{addr=\"0x08058fea\",data=[\"66\",\"13\",\"5\",\"79\",\"-59\",\"12\",\"4\",\"4\",\"0\",\"0\",\"28\",\"0\",\"0\",\"0\",\"68\",\"1\"],ascii=\"B..O..........D.\"},{addr=\"0x08058ffa\",data=[\"0\",\"0\",\"74\",\"-54\",\"-2\",\"-1\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\"],ascii=\"..J...XXXXXXXXXX\"}]");

        String expRes = "0x08058fea: 66 13  5  79 -59 12   4   4   0   0  28   0   0   0  68   1 \"B..O..........D.\"\n";
        assertEquals(expRes, GdbDebuggerImpl.parseMem(res).get(0));
        
        expRes = "0x08058ffa:  0  0 74 -54  -2 -1 N/A N/A N/A N/A N/A N/A N/A N/A N/A N/A \"..J...XXXXXXXXXX\"\n";
        assertEquals(expRes, GdbDebuggerImpl.parseMem(res).get(1));
    }
    
    @Test
    public void testAddrMemParsing() {
        MIRecord res = prepareRecord("^done,addr=\"0x08058fea\",nr-bytes=\"22\",total-bytes=\"32\",next-row=\"0x08058ffa\",prev-row=\"0x08058fda\",next-page=\"0x0805900a\",prev-page=\"0x08058fca\",memory=[{addr=\"0x08058fea\",data=[\"0x42\",\"0xd\",\"0x5\",\"0x4f\",\"0xffffffc5\",\"0xc\",\"0x4\",\"0x4\",\"0x0\",\"0x0\",\"0x1c\",\"0x0\",\"0x0\",\"0x0\",\"0x44\",\"0x1\"],ascii=\"B..O..........D.\"},{addr=\"0x08058ffa\",data=[\"0x0\",\"0x0\",\"0x4a\",\"0xffffffca\",\"0xfffffffe\",\"0xffffffff\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\",\"N/A\"],ascii=\"..J...XXXXXXXXXX\"}]");

        String expRes = "0x08058fea: 0x42 0xd  0x5       0x4f 0xffffffc5        0xc 0x4 0x4 0x0 0x0 0x1c 0x0 0x0 0x0 0x44 0x1 \"B..O..........D.\"\n";
        assertEquals(expRes, GdbDebuggerImpl.parseMem(res).get(0));
        
        expRes = "0x08058ffa:  0x0 0x0 0x4a 0xffffffca 0xfffffffe 0xffffffff N/A N/A N/A N/A  N/A N/A N/A N/A  N/A N/A \"..J...XXXXXXXXXX\"\n";
        assertEquals(expRes, GdbDebuggerImpl.parseMem(res).get(1));
    }
}