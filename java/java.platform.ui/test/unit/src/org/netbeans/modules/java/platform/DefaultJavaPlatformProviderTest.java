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
package org.netbeans.modules.java.platform;

import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public class DefaultJavaPlatformProviderTest extends NbTestCase {

    private static final int TIMEOUT = 500;
    private static FileSystem fs;
    private static Repository rp;


    public DefaultJavaPlatformProviderTest(final String name) {
        super(name);
        fs = FileUtil.createMemoryFileSystem();
        fs.getRoot();
        rp = new Repository(fs);
        System.setProperty("org.openide.util.Lookup", DefaultJavaPlatformProviderTest.Lkp.class.getName()); //NOI18N
        assertEquals(DefaultJavaPlatformProviderTest.Lkp.class, Lookup.getDefault().getClass());
    }

    public void testMissingLayer202479() throws Exception {
        final FileObject services = fs.getRoot().createFolder("Services");    //NOI18N
        final Listener l = new Listener();
        final JavaPlatformProvider jp = new DefaultJavaPlatformProvider();
        jp.addPropertyChangeListener(l);
        assertEquals(0,jp.getInstalledPlatforms().length);
        fs.getRoot().createFolder("Platforms"); //NOI18N
        assertFalse(l.expect(1, TIMEOUT));
        services.createFolder("Test");  //NOI18N
        assertFalse(l.expect(1, TIMEOUT));
        final FileObject platforms = services.createFolder("Platforms");  //NOI18N
        assertTrue(l.expect(1, TIMEOUT));
        assertEquals(0,jp.getInstalledPlatforms().length);
        platforms.createFolder("Test");  //NOI18N
        assertFalse(l.expect(1, TIMEOUT));
        final FileObject j2se = platforms.createFolder("org-netbeans-api-java-Platform");  //NOI18N
        assertTrue(l.expect(1, TIMEOUT));
        assertEquals(0,jp.getInstalledPlatforms().length);
        InstanceDataObject.create(
             DataFolder.findFolder(j2se),
             "DummyPlatform",   //NOI18N
             ConvertAsJavaBeanPlatformTest.TestPlatform.class);
        assertTrue(l.expect(1, TIMEOUT));
        assertEquals(1,jp.getInstalledPlatforms().length);
    }


    public static class Lkp extends ProxyLookup {
        public Lkp() {
            setLookups(Lookups.fixed(rp));
        }
    }

    public static class Listener implements PropertyChangeListener {

        private final Semaphore sem = new Semaphore(0);

        public boolean expect(int permits, long timeOut) throws InterruptedException {
            return sem.tryAcquire(permits, timeOut, TimeUnit.MILLISECONDS);
        }


        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            sem.release();
        }

    }
}
