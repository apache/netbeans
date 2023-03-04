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
package org.netbeans.modules.settings.convertors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;

/** Integration test to verify the mime type of .settings file can be
 * found without opening the input stream.
 */
public class MimeTypeTest extends NbTestCase {

    public MimeTypeTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.allModules(MimeTypeTest.class);
    }
    
    public void testIsMimeTypeRegistered() throws Exception {
        class LFS extends LocalFileSystem {
            @Override
            protected InputStream inputStream(String name) throws FileNotFoundException {
                fail("Don't call me: " + name);
                throw new FileNotFoundException();
            }
        }
        
        clearWorkDir();
        
        new File(getWorkDir(), "x.settings").createNewFile();
        
        LFS lfs = new LFS();
        lfs.setRootDirectory(getWorkDir());
        
        FileObject fo = lfs.findResource("x.settings");
        assertNotNull("File found", fo);
        assertEquals("mime type is correct without opening the content", "application/x-nbsettings", fo.getMIMEType());
    }
}
