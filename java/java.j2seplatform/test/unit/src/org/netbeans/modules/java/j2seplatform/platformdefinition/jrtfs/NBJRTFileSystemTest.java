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

package org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs;

import org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs.NBJRTFileSystem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author lahvac
 */
public class NBJRTFileSystemTest extends NbTestCase {

    public NBJRTFileSystemTest(String name) {
        super(name);
    }

    public void testOpen() throws IOException {
        String jigsawHome = System.getProperty("jigsaw.home");
        if (jigsawHome == null)
            return;
        File jdkHome = new File(jigsawHome);
        FileSystem fs = NBJRTFileSystem.create(jdkHome);
        assertNotNull(fs);
        FileObject jlObjectClass = fs.getRoot().getFileObject("java.base/java/lang/Object.class");
        assertNotNull(jlObjectClass);

        try (InputStream in = jlObjectClass.getInputStream()) {
            while (in.read() != (-1))
                ;
        }

        //list all:
        Enumeration<? extends FileObject> list = fs.getRoot().getChildren(true);

        while (list.hasMoreElements())
            list.nextElement();
    }

    public void testToPath() throws IOException {
        final String jigsawHome = System.getProperty("jigsaw.home");    //NOI18N
        if (jigsawHome == null) {
            return;
        }
        final File jdkHome = new File(jigsawHome);
        final FileSystem fs = NBJRTFileSystem.create(jdkHome);
        assertNotNull(fs);
        final FileObject jlObjectClass = fs.getRoot().getFileObject("java.base/java/lang/Object.class");    //NOI18N
        assertNotNull(jlObjectClass);
        final Object path = jlObjectClass.getAttribute(Path.class.getName());
        assertNotNull(path);
        assertTrue (path instanceof Path);
        assertEquals("/java.base/java/lang/Object.class", path.toString()); //NOI18N
    }

}
