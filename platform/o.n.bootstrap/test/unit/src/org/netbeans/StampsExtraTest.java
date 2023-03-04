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

import java.io.File;
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
public class StampsExtraTest extends NbTestCase {
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File extra;
    
    public StampsExtraTest(String testName) {
        super(testName);
    }            

    public void testTimeStampsWhenAddingCluster() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        platform.mkdirs();
        ide = new File(install, "ide");
        ide.mkdirs();
        extra = new File(install, "extra");
        userdir = new File(getWorkDir(), "tmp");
        userdir.mkdirs();
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + extra.getPath());
        PlacesTestUtils.setUserDirectory(userdir);

        touch(platform, ".lastModified", 50000L);
        touch(ide, ".lastModified", 90000L);
        assertFalse("One cluster does not exists", extra.isDirectory());
        
        Stamps.main("init");
        
        Thread.sleep(100);

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);

        long stamp = Stamps.moduleJARs();
        assertEquals("Timestamp is taken from api.languages module", 90000L, stamp);

        Stamps.main("clear");

        CountingSecurityManager.initialize(install.getPath());

        long newStamp = Stamps.moduleJARs();

        CountingSecurityManager.assertCounts("Just few accesses to installation", 6);
        assertEquals("Stamps are the same", stamp, newStamp);

        assertFalse("File has not been created for non-existing cluster", Places.getCacheSubfile("lastModified/extra").canRead());

        extra.mkdirs();
        File lastModified = new File(extra, ".lastModified");
        lastModified.createNewFile();
        lastModified.setLastModified(200000L);
        assertEquals("Correct last modified", 200000L, lastModified.lastModified());

        Stamps.main("clear");
        stamp = Stamps.moduleJARs();
        if (stamp < 200000L) {
            fail("lastModified has not been updated: " + stamp);
        }
    }

    private static void touch(File root, String rel, long time) throws IOException {
        File f = new File(root, rel.replace('/', File.separatorChar));
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        f.setLastModified(time);
        assertEquals("Correct last modified for " + f, time, f.lastModified());
    }
}
