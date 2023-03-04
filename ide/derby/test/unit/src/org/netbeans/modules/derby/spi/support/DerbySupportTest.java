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

package org.netbeans.modules.derby.spi.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.derby.DerbyOptions;

/**
 *
 * @author Andrei Badea
 */
public class DerbySupportTest extends NbTestCase {
    public DerbySupportTest(String testName) {
        super(testName);
    }
    
    public void testDefaultSystemHomeWhenNDSHPropertySetIssue76908() {
        // ensure "foo" ist not the "default" derby home
        assert (! DerbySupport.getDefaultSystemHome().equals("foo"));

        // ... but returning it when it is
        System.setProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME, "foo");
        assertEquals("foo", DerbySupport.getDefaultSystemHome());
    }
    
    public void testGetSystemHome() throws IOException {
        System.clearProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME);
        clearWorkDir();

        String origUserHome = System.getProperty("user.home");
        String origOsName = System.getProperty("os.name");
        
        System.setProperty("org.netbeans.modules.derby.spi.support.DerbySupport.overrideAppData", getWorkDirPath());
        
        System.setProperty("user.home", getWorkDirPath());
        
        // This test is only partitially correct (the tested method reads
        // environment variables, so this is a partitial solution) - the idea:
        // On non-windows systems default system home is user.home/.netbeans-derby
        // On windows system default system home has "Derby" as final path part
        
        System.setProperty("os.name", "Linux");
        assertEquals(new File(getWorkDirPath(), ".netbeans-derby").getAbsolutePath(), DerbySupport.getDefaultSystemHome());

        System.setProperty("os.name", "Windows 8");
        assertEquals("Derby", new File(DerbySupport.getDefaultSystemHome()).getName());
        
        Files.createDirectory(new File(getWorkDirPath(), ".netbeans-derby").toPath());
        assertEquals(new File(getWorkDirPath(), ".netbeans-derby").getAbsolutePath(), DerbySupport.getDefaultSystemHome());
        
        System.clearProperty("org.netbeans.modules.derby.spi.support.DerbySupport.overrideAppData");
        System.setProperty("user.home", origUserHome);
        System.setProperty("os.name", origOsName);
    }
}
