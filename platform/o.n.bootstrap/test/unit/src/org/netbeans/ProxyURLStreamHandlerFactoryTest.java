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
package org.netbeans;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/** 
 * Tests ProxyURLStreamHandlerFactory.
 */
public class ProxyURLStreamHandlerFactoryTest extends NbTestCase {

    public ProxyURLStreamHandlerFactoryTest(String testName) {
        super(testName);
    }

    /** Register ProxyURLStreamHandlerFactory. */
    @Override
    protected void setUp() throws Exception {
        ProxyURLStreamHandlerFactory.register();
        clearWorkDir();
    }

    /** Tests UNC path is correctly treated. On JDK1.5 UNCFileStreamHandler should 
     * be installed in ProxyURLStreamHandlerFactory to workaround JDK bug
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5086147.
     */
    public void testUNCFileURLStreamHandler() throws Exception {
        if(!Utilities.isWindows()) {
            return;
        }
        File uncFile = new File("\\\\computerName\\sharedFolder\\a\\b\\c\\d.txt");
        URI uri = Utilities.toURI(uncFile);
        String expectedURI = "file://computerName/sharedFolder/a/b/c/d.txt";
        assertEquals("Wrong URI from File.toURI.", expectedURI, uri.toString());
        URL url = uri.toURL();
        assertEquals("Wrong URL from URI.toURL", expectedURI, url.toString());
        assertEquals("URL.getAuthority must is now computer name.", "computerName", url.getAuthority());
        uri = url.toURI();
        assertEquals("Wrong URI from URL.toURI.", expectedURI, uri.toString());
    }
    
    public void testHandleSpaceInPathAsProducedByEclipse() throws Exception {
        File d = new File(getWorkDir(), "space in path");
        d.mkdirs();
        File f = new File(d, "x.jar");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(f));
        os.putNextEntry(new JarEntry("test.txt"));
        os.write(10);
        os.close();
        
        URL u = new URL("jar:" + f.toURI().toURL() + "!/test.txt");
        DataInputStream is = new DataInputStream(u.openStream());
        byte[] arr = new byte[100];
        is.readFully(arr, 0, 1);
        assertEquals("One byte", 10, arr[0]);
        is.close();
    }
}
