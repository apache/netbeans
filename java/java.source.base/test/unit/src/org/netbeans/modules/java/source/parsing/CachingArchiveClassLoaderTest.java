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
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

public class CachingArchiveClassLoaderTest extends NbTestCase {

    public CachingArchiveClassLoaderTest(String name) {
        super(name);
    }


    public void testClassLoaderGetResourcesDir() throws IOException {
        clearWorkDir();

        File wd = getWorkDir();
        File dir1 = new File(new File(wd, "dir1"), "a");
        assertTrue(dir1.mkdirs());
        File dir2 = new File(new File(wd, "dir2"), "a");
        assertTrue(dir2.mkdirs());
        new FileOutputStream(new File(dir2, "test.txt")).close();

        ClassPath cp = ClassPathSupport.createClassPath(wd.getAbsolutePath());
        ClassLoader loader = CachingArchiveClassLoader.forClassPath(cp, null, null);

        assertEquals(dir1.toURI().toURL(), loader.getResource("dir1/a"));
        assertEquals(dir2.toURI().toURL(), loader.getResource("dir2/a"));

        Enumeration<URL> resource = loader.getResources("dir1/a");
        assertTrue(resource.hasMoreElements());
        assertEquals(dir1.toURI().toURL(), resource.nextElement());
        assertFalse(resource.hasMoreElements());
    }

}
