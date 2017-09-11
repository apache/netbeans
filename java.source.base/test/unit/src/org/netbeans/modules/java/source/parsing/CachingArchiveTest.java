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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.tools.JavaFileObject;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

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
        return suite;
    }

    public void testPutName() throws Exception {
        File archive = null;
        
        String cp = System.getProperty("sun.boot.class.path");
        String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));
        
        for (String path : paths) {
            File f = new File(path);
            
            if (!f.canRead())
                continue;
            
            if (f.getName().endsWith("jar") || f.getName().endsWith("zip")) {
                archive = f;
                break;
            }
        }
        
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
}
