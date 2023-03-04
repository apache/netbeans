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
package org.netbeans.modules.java.source.parsing;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class FastJarTest extends NbTestCase {
    
    public FastJarTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFastJar () throws Exception {
        String prop = System.getProperty("sun.boot.class.path");    //NOI18N
        if (prop == null) {
            prop = TestUtil.createRT_JAR(getWorkDir()).getAbsolutePath();
        }
        assertNotNull(prop);
        String[] paths = prop.split(Pattern.quote(System.getProperty("path.separator")));
        for (String path : paths) {
            File f = new File (path);
            if (!f.exists()) {
                continue;
            }
            Iterable<? extends FastJar.Entry> fastEntries = FastJar.list(f);
            Map<String, FastJar.Entry> map = new HashMap<String, FastJar.Entry> ();
            for (FastJar.Entry e : fastEntries) {
                map.put (e.name,e);
            }
            ZipFile zf = new ZipFile (f);
            try {          
                Enumeration<? extends ZipEntry> zipEntries = zf.entries();
                int entryCount = 0;
                while (zipEntries.hasMoreElements())  {
                    ZipEntry zipEntry = zipEntries.nextElement();
                    entryCount++;
                    FastJar.Entry e = map.get (zipEntry.getName());
                    assertNotNull(e);
                    long zipTime = zipEntry.getTime();
                    long eTime = e.getTime();
                    assertEquals(zipTime, eTime);
                    InputStream zs = zf.getInputStream(zipEntry);
                    try {
                        InputStream fs = FastJar.getInputStream(f, e);                                                    
                        try {                   
                            assertEquals(e.name, zs, fs);
                        } finally {
                            fs.close();
                        }
                    } finally {
                        zs.close();
                    }
                }                
                assertEquals (entryCount, map.size());
            } finally {
                zf.close();
            }
        }
    }    
    
    private void assertEquals (String file, InputStream a, InputStream b) throws IOException {
        ByteArrayOutputStream oa = new ByteArrayOutputStream ();
        ByteArrayOutputStream ob = new ByteArrayOutputStream ();
        FileUtil.copy(a, oa);
        FileUtil.copy(b, ob);
        byte[] aa = oa.toByteArray();
        byte[] ab = ob.toByteArray();
        assertEquals ("file: "+ file ,aa.length,ab.length);
        for (int i=0; i< aa.length; i++) {
            assertEquals("file: "+file+ " offset: "+ i, aa[i], ab[i]);
        }
    }
    
}
