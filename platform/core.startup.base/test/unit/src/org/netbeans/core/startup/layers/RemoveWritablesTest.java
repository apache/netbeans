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
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Stanislav Aubrecht
 */
public class RemoveWritablesTest extends NbTestCase {
    Module myModule;
    File configDir;
    
    private static final String manifest = "Manifest-Version: 1.0\n"
                + "OpenIDE-Module: org.netbeans.modules.foo\n"
                + "OpenIDE-Module-Specification-Version: 1.0\n"
                + "OpenIDE-Module-Implementation-Version: today\n"
                + "OpenIDE-Module-Layer: foo/mf-layer.xml\n";
    private ModuleManager mgr;

    public RemoveWritablesTest(String testName) {
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
                mgr.disable( myModule );
                mgr.delete( myModule );
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }
    }
    
    public void testAddedFile() throws Exception {
        FileObject folder = FileUtil.getConfigFile( "foo" );
        FileObject newFile = folder.createData( "newFile", "ext" );
        
        File writableFile = new File( new File( configDir, "foo"), "newFile.ext" );
        assertTrue( writableFile.exists() );

        assertTrue(newFile.canRevert());
        newFile.revert();
        
        assertFalse( "local file removed", writableFile.exists() );
        assertNull( "FileObject does not exist", FileUtil.getConfigFile( "foo/newFile.ext" ) );
    }
    
    public void testRemovedFile() throws Exception {
        FileObject existingFile = FileUtil.getConfigFile( "foo/test1" );
        
        assertNotNull( existingFile );
        
        existingFile.delete();
        
        File maskFile = new File( new File( configDir, "foo"), "test1_hidden" );
        assertTrue( maskFile.exists() );

        FileObject newFile = FileUtil.getConfigFile("foo");
        assertTrue(newFile.canRevert());
        newFile.revert();
        
        assertFalse( "local file removed", maskFile.exists() );
        assertNotNull( "FileObject exists again", FileUtil.getConfigFile( "foo/test1" ) );
    }
    
    public void testRenamedFile() throws Exception {
        FileObject existingFile = FileUtil.getConfigFile( "foo/test1" );
        
        assertNotNull( existingFile );
        
        FileLock lock = existingFile.lock();
        existingFile.rename( lock, "newName", "newExt" );
        lock.releaseLock();
        
        assertNotNull( FileUtil.getConfigFile( "foo/newName.newExt" ) );
        
        File maskFile = new File( new File( configDir, "foo"), "test1_hidden" );
        assertTrue( maskFile.exists() );
        
        FileObject newFile = FileUtil.getConfigFile("foo");
        assertTrue(newFile.canRevert());
        newFile.revert();
        
        assertFalse( "local file removed", maskFile.exists() );
        assertNotNull( "FileObject exists again", FileUtil.getConfigFile( "foo/test1" ) );
        assertNull( "renamed file is gone", FileUtil.getConfigFile( "foo/newName.newExt" ) );
    }

    public void testFolder() throws Exception {
        FileObject folder = FileUtil.getConfigFile("foo");
        folder.createData("newFile", "ext");
        File writableFolder = new File(configDir, "foo");
        assertTrue(writableFolder.isDirectory());
        assertTrue(folder.canRevert());
        folder.revert();
        assertFalse("local file removed", new File(writableFolder, "newFile.ext").exists());
        assertNull("FileObject does not exist", FileUtil.getConfigFile("foo/newFile.ext"));
        folder = FileUtil.getConfigFile("foo");
        assertNotNull("folder still there in layer", folder);
        assertFalse("no longer considered modified", folder.canRevert());
    }

    private File createModuleJar(String manifest) throws IOException {
        // XXX use TestFileUtils.writeZipFile
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
        os.close();
        
        return jarFile;
    }
}
