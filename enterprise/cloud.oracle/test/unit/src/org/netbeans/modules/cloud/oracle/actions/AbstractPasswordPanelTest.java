/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cloud.oracle.actions;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Jan Horvath
 */
public class AbstractPasswordPanelTest {
    
    public AbstractPasswordPanelTest() {
    }

    /**
     * Test of showDialog method, of class DownloadWalletDialog.
     */
    @Test
    public void testCheckPassword() {
        char[] passwd1 = "abcdefgh".toCharArray();
        AbstractPasswordPanel.checkPasswordLogic(passwd1, passwd1, (m) -> 
                assertEquals("The wallet download password should contain at least 1 special character.", m));
        
        passwd1 = "abcdefgh!".toCharArray();
        AbstractPasswordPanel.checkPasswordLogic(passwd1, passwd1, (m) -> 
                assertEquals("The wallet download password should contain at least 1 number.", m));
        
        passwd1 = "11111".toCharArray();
        AbstractPasswordPanel.checkPasswordLogic(passwd1, passwd1, (m) -> 
                assertEquals("The wallet download password should be at least 8 characters long.", m));
        
        passwd1 = "12345678".toCharArray();
        AbstractPasswordPanel.checkPasswordLogic(passwd1, passwd1, (m) -> 
                assertEquals("The wallet download password should contain at least 1 letter.", m));
        
        char[] passwd2 = "abcdefg1!".toCharArray();
        AbstractPasswordPanel.checkPasswordLogic(passwd2, passwd2, (m) -> 
                assertEquals(m, null));
        
        AbstractPasswordPanel.checkPasswordLogic(passwd2, passwd1, (m) -> 
                assertEquals("Passwords don't match.", m));
    }
    
    @Test
    public void testGeneratePassword() {
        char[] passwd = DownloadWalletDialog.generatePassword();
        System.out.println(new String(passwd));
        AbstractPasswordPanel.checkPasswordLogic(passwd, passwd, (m) -> 
                assertEquals(m, null));
    }
    
}
