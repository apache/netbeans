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