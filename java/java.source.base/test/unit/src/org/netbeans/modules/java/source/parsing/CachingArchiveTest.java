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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.tools.JavaFileObject;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.TestUtil;

/**
 *
 * @author Jan Lahoda
 */
public class CachingArchiveTest extends NbTestCase {
    
    public CachingArchiveTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite ();
        suite.addTest(new CachingArchiveTest("testPutName"));
        suite.addTest(new CachingArchiveTest("testJoin"));
        suite.addTest(new CachingArchiveTest("testGetDirectory"));
        return suite;
    }

    public void testPutName() throws Exception {
        clearWorkDir();

        File archive = TestUtil.createRT_JAR(getWorkDir());
        
        assertNotNull(archive);
        
        CachingArchive a = new CachingArchive(archive, false);
        
        a.doInit();
        
        a.putName(new byte[65536]);
        
        a.clear();
        a.doInit();
        
        a.putName(new byte[1]);
    }

    public void testJoin() throws Exception {
        long smallLong = ((long) Integer.MAX_VALUE) + 1;
        
        for (long mtime : Arrays.asList(3003611096031047874L, new Long(Integer.MAX_VALUE), smallLong, new Long(Integer.MIN_VALUE))) {
            int  higher = (int)(mtime >> 32);
            int  lower = (int)(mtime & 0xFFFFFFFF);
            
            assertEquals(mtime, CachingArchive.join(higher, lower));
        }
    }
    
    //By default turned off (takes long time)
    public void testCachingArchive () throws Exception {
        String cp = System.getProperty("sun.boot.class.path");
        String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));
        for (String path : paths) {
            File testFile = new File (path);
            if (!testFile.canRead()) {
                continue;
            }
            CachingArchive a = new CachingArchive (testFile, false);
            ZipFile zf = new ZipFile (testFile);
            try {
                Enumeration<? extends ZipEntry> entries = zf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    int i = name.lastIndexOf('/');
                    String dirname = i == -1 ? "" : name.substring(0, i /* +1 */);
                    String basename = name.substring(i+1);
                    if (basename.length() == 0) {
                        continue;
                    }
                    Iterable<? extends JavaFileObject> res = a.getFiles(dirname, null, null, null, false);
                    for (JavaFileObject jfo : res) {
                        if (jfo.toUri().toString().endsWith('/'+basename)) {
                            assertEquals (entry.getTime(),jfo.getLastModified());
                        }
                    }
                }

                a = new CachingArchive (testFile, true);
                entries = zf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    int i = name.lastIndexOf('/');
                    String dirname = i == -1 ? "" : name.substring(0, i /* +1 */);
                    String basename = name.substring(i+1);
                    if (basename.length() == 0) {
                        continue;
                    }
                    Iterable<? extends JavaFileObject> res = a.getFiles(dirname, null, null, null, false);
                    for (JavaFileObject jfo : res) {
                        if (jfo.toUri().toString().endsWith('/'+basename)) {
                            assertEquals (entry.getTime(),jfo.getLastModified());
                        }
                    }
                }
            } finally {
                zf.close();
            }                        
        }
    }

    public void testGetDirectory() throws Exception {
        clearWorkDir();

        File archive = new File(getWorkDir(), "rt.jar");

        try (OutputStream binOut = new FileOutputStream(archive);
             JarOutputStream out = new JarOutputStream(binOut)) {
            out.putNextEntry(new ZipEntry(("dir1/a/")));
            out.putNextEntry(new ZipEntry(("dir2/a/test.txt")));
        }

        Archive a = new CachingArchive(archive, false);

        assertEquals("jar:" + archive.toURI().toString() + "!/dir1/a", a.getDirectory("dir1/a").toString());
        assertEquals("jar:" + archive.toURI().toString() + "!/dir2/a", a.getDirectory("dir2/a").toString());
    }
}
