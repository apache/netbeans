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
public class StampsJDKChangeTest extends NbTestCase implements Stamps.Updater {
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File extra;
    
    public StampsJDKChangeTest(String testName) {
        super(testName);
    }            

    public void testCacheNotFoundWhenJDKChanged() throws Exception {
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

        Stamps.main("clear");

        int[] arr = { 0 };
        File f1 = Stamps.getModulesJARs().file("test-cache", arr);
        assertNotNull("Cache is found", f1);

        System.setProperty("java.vm.version", 
            "X" + System.getProperty("java.vm.version")
        );
        Stamps.main("clear");
        
        File f2 = Stamps.getModulesJARs().file("test-cache", arr);
        assertNull("Cache is discarded after VM change", f2);
    }

    public void flushCaches(DataOutputStream os) throws IOException {
        os.write(1);
    }

    public void cacheReady() {
    }
}
