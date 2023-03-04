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

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class URLsAreEqualTest extends NbTestCase {
    public URLsAreEqualTest(String n) {
        super(n);
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }
    
    public void testURLsAreEqual() throws Exception {
        final File wd = new File(getWorkDir(), "work#dir");
        wd.mkdirs();
        
        File jar = new File(wd, "default-package-resource.jar");
        
        URL orig = new URL("jar:" + Utilities.toURI(jar) + "!/package/resource.txt");
        URLConnection conn = orig.openConnection();
        assertFalse("JDK connection: " + conn, conn.getClass().getName().startsWith("org.netbeans"));
        
        
        TestFileUtils.writeZipFile(jar, "package/resource.txt:content", "root.txt:empty");
        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0]);

        URL root = jcl.getResource("root.txt");
        URL u = new URL(root, "/package/resource.txt");
        assertNotNull("Resource found", u);
        URLConnection uC = u.openConnection();
        assertTrue("Our connection: " + uC, uC.getClass().getName().startsWith("org.netbeans"));

        assertEquals("Both URLs are equal", u, orig);
        assertEquals("Equality is symetrical", orig, u);
    }
}
