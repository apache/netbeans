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

package org.netbeans.modules.apisupport.jnlplauncher;

import junit.framework.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.security.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

/** Can we specify ${user.home} in name of userdirectory?
 *
 * @author Jaroslav Tulach
 */
public class ReplaceUserDirTest extends TestCase {

    public ReplaceUserDirTest(String testName) {
        super(testName);
    }

    public void testGetUserDir() {
        System.setProperty("netbeans.user", "${user.home}/mine");
        
        String expResult = System.getProperty("user.home") + File.separator + "mine";
        Main.fixNetBeansUser();
        String result = System.getProperty("netbeans.user");
        assertEquals(expResult, result);
    }

}
