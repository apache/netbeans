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

package org.netbeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.Places;
import org.openide.modules.api.PlacesTestUtils;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsIdeLessThanPlatformTest extends NbTestCase {
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File nonexist;
    
    public StampsIdeLessThanPlatformTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        nonexist = new File(install, "nonexist7");
        ide = new File(install, "ide");
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + nonexist.getPath());
        PlacesTestUtils.setUserDirectory(userdir);
        
        StampsTest.createModule("org.openide.awt", platform, 50000L);
        StampsTest.createModule("org.openide.nodes", platform, 60000L);
        StampsTest.createModule("org.netbeans.api.languages", ide, 50000L);
        StampsTest.createModule("org.netbeans.modules.logmanagement", userdir, 10000L);
        
        Stamps.main("reset");
        
        Thread.sleep(100);

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);
    }
    
    public void testGenerateTimeStamps() throws IOException {
        long stamp = Stamps.moduleJARs();
        assertEquals("Timestamp is taken from nodes module", 60000L, stamp);
        
        StampsTest.assertStamp(60000L, platform, false, true);
        StampsTest.assertStamp(50000L, ide, false, true);
        StampsTest.assertStamp(-1L, userdir, false, false);


        File checkSum = Places.getCacheSubfile("lastModified/all-checksum.txt");
        assertTrue("Checksum created" , checkSum.isFile());

        byte[] arr = new byte[30000];
        int len = new FileInputStream(checkSum).read(arr);

        String r = new String(arr, 0, len);
        if (r.contains("nonexist")) {
            fail(r);
        }

        int check = 0;
        BufferedReader in = new BufferedReader(new FileReader(checkSum));
        
        for (;;) {
            String line = in.readLine();
            if (line == null) {
                break;
            }
            String[] seg = line.split("=");
            assertEquals("There should be one = in the: " + line, 2, seg.length);
            String s = seg[0];
            if (s.endsWith("platform")) {
                assertEquals("Correct for platform: " + line, "60000", seg[1]);
                check ++;
            }
            if (s.endsWith("ide")) {
                assertEquals("Correct for ide: " + line, "50000", seg[1]);
                check ++;
            }
        }
        assertEquals("Two checks", 2, check);
    }        
    

}
