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

package org.netbeans.modules.j2ee.ejbjar;

import java.io.File;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 * Base class for web module project tests.
 * @author Pavel Buzek
 */
public class CustomProviderTest extends NbTestCase {
    
    static {
        CustomProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public CustomProviderTest (String name) {
        super(name);
    }
    
    private FileObject datadir;
    
    protected void setUp() throws Exception {
        super.setUp();
        File f = getDataDir();
        assertTrue("example dir exists", f.exists());
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (f);
        Repository.getDefault ().addFileSystem (lfs);
        datadir = FileUtil.toFileObject (f);
        assertNotNull ("no FileObject", datadir);
    }

    public void testProviders () throws Exception {
        Lookup.Result res = Lookup.getDefault ().lookup (new Lookup.Template (EjbJarProvider.class));
        assertEquals ("there should be 2 instances - one from j2ee/ejbapi and one from tests", 2, res.allInstances ().size ());
    }
    
    public void testGetEjbJar () throws Exception {
        FileObject foo = datadir.getFileObject ("a.foo");
        FileObject bar = datadir.getFileObject ("b.bar");
        EjbJar wm1 = EjbJar.getEjbJar (foo);
        assertNotNull ("found ejb module", wm1);
        EjbJar wm2 = EjbJar.getEjbJar (bar);
        assertNull ("no ejb module", wm2);
    }
}
