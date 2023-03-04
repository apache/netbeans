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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JarURLStreamHandlerTest extends NbTestCase {
    private File jar;

    public JarURLStreamHandlerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        jar = new File(getWorkDir(), "x.jar");
        JarOutputStream os = new JarOutputStream(
            new FileOutputStream(jar)
        );
        os.putNextEntry(new ZipEntry("fldr/plain.txt"));
        os.write("Ahoj\n".getBytes());
        os.closeEntry();
        os.close();
        JarClassLoader registerJarSource = new JarClassLoader(
            Collections.nCopies(1, jar),
            new ClassLoader[] { getClass().getClassLoader() }
        );
        assertNotNull("Registered", registerJarSource);
    }

    public void testNormalHandler() throws Exception {
        URL root = new URL("jar:" + Utilities.toURI(jar) + "!/");
        URL plain = new URL(root, "/fldr/plain.txt", ProxyURLStreamHandlerFactory.originalJarHandler());
        assertTrue("Contains the plain.txt part: " + plain, plain.toExternalForm().contains("fldr/plain.txt"));
        assertContent("Ahoj", plain);
    }

    public void testNbHandler() throws Exception {
        URL root = new URL("jar:" + Utilities.toURI(jar) + "!/");
        URL plain = new URL(root, "/fldr/plain.txt", new JarClassLoader.JarURLStreamHandler(null));
        assertTrue("Contains the plain.txt part: " + plain, plain.toExternalForm().contains("fldr/plain.txt"));
        assertContent("Ahoj", plain);
    }

    private void assertContent(String ahoj, URL plain) throws IOException {
        DataInputStream is = new DataInputStream(plain.openStream());
        byte[] arr = new byte[100];
        is.readFully(arr, 0, ahoj.length());
        assertEquals("Expected", ahoj, new String(arr, 0, ahoj.length()));
    }
    
}
