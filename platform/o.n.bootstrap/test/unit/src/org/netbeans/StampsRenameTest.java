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

package org.netbeans;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsRenameTest extends NbTestCase implements Stamps.Updater{
    private File install;
    private File userdir;
    
    public StampsRenameTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    public void testTimeStampsWhenSystemTimeDifferentThanFileSystem() throws Exception {
        clearWorkDir();

        install = new File(getWorkDir(), "install");
        userdir = new File(getWorkDir(), "tmp");
        userdir.mkdirs();
        setNetBeansProperties(install, userdir, -1);


        Thread.sleep(500);
        long between = System.currentTimeMillis();
        Thread.sleep(500);

        
        Stamps.main("init");
        
        Stamps.getModulesJARs().scheduleSave(this, "test-cache", false);
        Stamps.getModulesJARs().waitFor(true);
        int[] arr = { 0 };
        File f = Stamps.getModulesJARs().file("test-cache", arr);
        assertNotNull("Cache found", f);
        final long previousLastModified = Stamps.moduleJARs();
        assertEquals("Stamps of caches shall be the same as stamps of .lastModified",
            f.lastModified(), previousLastModified
        );

        File i2 = new File(getWorkDir(), "inst2");
        assertTrue("Rename of the dir successful", install.renameTo(i2));
        if (BaseUtilities.isWindows()) {
          // See comment in org.netbeans.Stamps.
          assertTrue("Renamed back to install to permit workaround for NETBEANS-1914",
              i2.renameTo(install));
          i2 = install;
        }
        
        setNetBeansProperties(i2, userdir, Stamps.moduleJARs());
        
        Thread.sleep(500);

        Stamps.main("clear");
        
        //assertEquals("Modified time remains the same", previousLastModified, Stamps.moduleJARs());
        File newF = Stamps.getModulesJARs().file("test-cache", arr);
        assertNotNull("Cache can still be found", newF);
    }

    private static void setNetBeansProperties(File install, File userdir, long stamp) throws IOException {
        File platform = new File(install, "platform");
        final File platformLast = new File(platform, ".lastModified");
        if (stamp == -1) {
            platform.mkdirs();
            platformLast.createNewFile();
        } else {
            platformLast.setLastModified(stamp);
        }
        File ide = new File(install, "ide");
        final File ideLast = new File(ide, ".lastModified");
        if (stamp == -1) {
            ide.mkdirs();
            ideLast.createNewFile();
        } else {
            ideLast.setLastModified(stamp);
        }
        File extra = new File(install, "extra");
        assertFalse("One cluster does not exists", extra.isDirectory());
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + extra.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        os.write(1);
    }

    @Override
    public void cacheReady() {
    }
}
