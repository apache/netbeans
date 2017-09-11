/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
