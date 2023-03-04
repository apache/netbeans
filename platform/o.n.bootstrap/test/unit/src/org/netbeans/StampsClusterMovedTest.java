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
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsClusterMovedTest extends NbTestCase implements Stamps.Updater{
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File mainCluster;
    
    public StampsClusterMovedTest(String testName) {
        super(testName);
    }            

    public void testMoveOfAClusterIsDetected() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        platform.mkdirs();
        new File(platform, ".lastModified").createNewFile();
        ide = new File(install, "ide");
        ide.mkdirs();
        new File(ide, ".lastModified").createNewFile();
        mainCluster = new File(install, "extra");
        mainCluster.mkdirs();
        assertTrue("Extra cluster exists", mainCluster.isDirectory());
        new File(mainCluster, ".lastModified").createNewFile();
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + mainCluster.getPath());
        System.setProperty("netbeans.user", userdir.getPath());


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

        File subDir = new File(getWorkDir(), "subdir");
        subDir.mkdirs();
        final File newExtra = new File(subDir, mainCluster.getName());
        boolean renRes = mainCluster.renameTo(newExtra);
        assertTrue("Rename succeeded", renRes);
        assertTrue("Extra renamed: " + newExtra, newExtra.isDirectory());
        
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + newExtra.getPath());
        
        Stamps.main("init");
        
        assertNull("Cache invalidated as relative location of clusters changed", 
            Stamps.getModulesJARs().asByteBuffer("test-cache")
        );
    }
    
    public void testChangeOfClustersIsDetectedInSharedConfig() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        platform.mkdirs();
        new File(platform, ".lastModified").createNewFile();
        ide = new File(install, "ide");
        ide.mkdirs();
        new File(ide, ".lastModified").createNewFile();
        mainCluster = new File(install, "extra");
        mainCluster.mkdirs();
        assertTrue("Extra cluster exists", mainCluster.isDirectory());
        new File(mainCluster, ".lastModified").createNewFile();
        userdir = new File(getWorkDir(), "tmp");
        userdir.mkdirs();
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        // generate the cache to mainCluster directory
        System.setProperty("netbeans.user", mainCluster.getPath());


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
        
        File lmdir = new File(new File(new File(mainCluster, "var"), "cache"), "lastModified");
        assertTrue(lmdir + " is dir", lmdir.isDirectory());
        lmdir.renameTo(new File(lmdir.getParentFile(), "ignore"));
        assertFalse(lmdir + " is no longer dir", lmdir.isDirectory());

        Thread.sleep(500);

        System.setProperty("netbeans.user", userdir.getPath());
        // use mainCluster as cluster
        System.setProperty("netbeans.dirs", mainCluster.getPath() + File.pathSeparator + ide.getPath());
        
        Stamps.main("init");
        
        assertNull("Cache invalidated set of clusters changed", 
            Stamps.getModulesJARs().asByteBuffer("test-cache")
        );
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        os.write(1);
    }

    @Override
    public void cacheReady() {
    }
}
