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

package org.netbeans.core.startup.layers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

public class DelayFSEventsTest extends NbTestCase implements FileChangeListener {
    Module myModule;
    File configDir;
    
    private static final String manifest = "Manifest-Version: 1.0\n"
                + "OpenIDE-Module: org.netbeans.modules.foo\n"
                + "OpenIDE-Module-Specification-Version: 1.0\n"
                + "OpenIDE-Module-Module-Dependencies: org.openide.modules\n"
                + "OpenIDE-Module-Implementation-Version: today\n"
                + "OpenIDE-Module-Install: org/netbeans/core/startup/layers/DelayFSInstaller.class\n"
                + "OpenIDE-Module-Layer: foo/mf-layer.xml\n";
    private ModuleManager mgr;
    private int cnt;

    public DelayFSEventsTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 15000;
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        
        File u = new File(getWorkDir(), "userdir");
        File uc = new File(u, "config");
        uc.mkdirs();
        System.setProperty("netbeans.user", u.toString());
        
        File h = new File(getWorkDir(), "nb/installdir");
        new File(h, "config").mkdirs();
        System.setProperty("netbeans.home", h.toString());

        File moduleJar = createModuleJar( manifest );
        mgr = Main.getModuleSystem().getManager();
        System.setProperty("installer.done", "false");
        FileObject[] arr = FileUtil.getConfigRoot().getChildren();
        FileUtil.getConfigRoot().addFileChangeListener(this);
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            myModule = mgr.create( moduleJar, null, true, false, false );
            mgr.enable( myModule );
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        
        assertNotNull("Module layer is installed", FileUtil.getConfigFile( "foo" ) );
        
        configDir = FileUtil.toFile( FileUtil.getConfigRoot() );//new File( getWorkDir(), "userdir/config" );
    }

    protected @Override void tearDown() throws Exception {
        if( null != myModule ) {
            try {
                mgr.mutexPrivileged().enterWriteAccess();
                System.setProperty("installer.done", "false");
                mgr.disable( myModule );
                mgr.delete( myModule );
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }
    }
    
    public void testWasCalled() throws Exception {
        if (cnt == 0) {
            fail("File change events delivered");
        }
    }
    
    private File createModuleJar(String manifest) throws IOException {
        File jarFile = new File( getWorkDir(), "mymodule.jar" );
        
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jarFile), new Manifest(
            new ByteArrayInputStream(manifest.getBytes())
        ));
        JarEntry entry = new JarEntry("foo/mf-layer.xml");
        os.putNextEntry( entry );
        
        File l3 = new File(new File(new File(getDataDir(), "layers"), "data"), "layer3.xml");
        InputStream is = new FileInputStream(l3);
        FileUtil.copy( is, os );
        is.close();
        os.putNextEntry(new JarEntry("org/netbeans/core/startup/layers/DelayFSInstaller.class"));
        is = DelayFSEventsTest.class.getResourceAsStream("DelayFSInstaller.class");
        FileUtil.copy (is, os);
        
        os.close();
        
        return jarFile;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        assertLocks();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        assertLocks();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        assertLocks();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        assertLocks();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        assertLocks();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        assertLocks();
    }
    
    private void assertLocks() {
        assertTrue("System property is already set", Boolean.getBoolean("installer.done"));
        cnt++;
    }
}
