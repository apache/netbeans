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

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsTimeShiftTest extends NbTestCase implements Stamps.Updater{
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File extra;
    
    public StampsTimeShiftTest(String testName) {
        super(testName);
    }            

    public void testTimeStampsWhenSystemTimeDifferentThanFileSystem() throws Exception {
        clearWorkDir();

        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        platform.mkdirs();
        new File(platform, ".lastModified").createNewFile();
        ide = new File(install, "ide");
        ide.mkdirs();
        new File(ide, ".lastModified").createNewFile();
        extra = new File(install, "extra");
        userdir = new File(getWorkDir(), "tmp");
        userdir.mkdirs();
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + extra.getPath());
        System.setProperty("netbeans.user", userdir.getPath());

        assertFalse("One cluster does not exists", extra.isDirectory());

        Thread.sleep(500);
        long between = System.currentTimeMillis();
        Thread.sleep(500);

        
        Stamps.main("init");
        
        Stamps.getModulesJARs().scheduleSave(this, "test-cache", false);
        Stamps.getModulesJARs().waitFor(true);
        int[] arr = { 0 };
        File f = Stamps.getModulesJARs().file("test-cache", arr);
        assertNotNull("Cache found", f);
        assertEquals("Stamps of caches shall be the same as stamps of .lastModified",
            f.lastModified(), Stamps.moduleJARs()
        );


        Thread.sleep(500);

        extra.mkdirs();
        File lastModified = new File(extra, ".lastModified");
        lastModified.createNewFile();
        lastModified.setLastModified(between);
        long now = System.currentTimeMillis();
        if (lastModified.lastModified() >= now) {
            fail("Last modified shall be set to past: " + lastModified.lastModified() + "\nbut was: " + now);
        }
        Stamps.main("clear");
        assertNull("Cache invalidated", Stamps.getModulesJARs().asByteBuffer("test-cache"));
    }

    public void flushCaches(DataOutputStream os) throws IOException {
        os.write(1);
    }

    public void cacheReady() {
    }
}
