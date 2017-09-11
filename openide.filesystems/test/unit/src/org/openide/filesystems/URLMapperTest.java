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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.filesystems;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.openide.util.BaseUtilities;

/**
 * Test functionality of URLMapper.
 * @author Jesse Glick
 */
public class URLMapperTest extends NbTestCase {

    public URLMapperTest(String name) {
        super(name);
    }
    
    public void testPlusInName() throws IOException, PropertyVetoException {
        clearWorkDir();
        File plus = new File(getWorkDir(), "plus+plus");
        plus.createNewFile();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        Repository.getDefault().addFileSystem(lfs);
        
        URL uPlus = BaseUtilities.toURI(plus).toURL();
        FileObject fo = URLMapper.findFileObject(uPlus);
        assertNotNull("File object found", fo);
        assertEquals("plus+plus", fo.getNameExt());
        
        URL back = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        assertTrue("plus+plus is there", back.getPath().endsWith("plus+plus"));
    }

    /**
     * Check that jar: URLs are correctly mapped back into JarFileSystem resources.
     * @see "#39190"
     */
    public void testJarMapping() throws Exception {
        clearWorkDir();
        File workdir = getWorkDir();
        File jar = new File(workdir, "test.jar");
        String textPath = "x.txt";
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = new JarOutputStream(os);
            jos.setMethod(ZipEntry.STORED);
            JarEntry entry = new JarEntry(textPath);
            entry.setSize(0L);
            entry.setTime(System.currentTimeMillis());
            entry.setCrc(new CRC32().getValue());
            jos.putNextEntry(entry);
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
        assertTrue("JAR was created", jar.isFile());
        assertTrue("JAR is not empty", jar.length() > 0L);
        JarFileSystem jfs = new JarFileSystem();
        jfs.setJarFile(jar);
        Repository.getDefault().addFileSystem(jfs);
        FileObject rootFO = jfs.getRoot();
        FileObject textFO = jfs.findResource(textPath);
        assertNotNull("JAR contains a/b.txt", textFO);
        String rootS = "jar:" + BaseUtilities.toURI(jar) + "!/";
        URL rootU = new URL(rootS);
        URL textU = new URL(rootS + textPath);
        assertEquals("correct FO -> URL for root", rootU, URLMapper.findURL(rootFO, URLMapper.EXTERNAL));
        assertEquals("correct FO -> URL for " + textPath, textU, URLMapper.findURL(textFO, URLMapper.EXTERNAL));
        assertTrue("correct URL -> FO for root", Arrays.asList(URLMapper.findFileObjects(rootU)).contains(rootFO));
        assertTrue("correct URL -> FO for " + textPath, Arrays.asList(URLMapper.findFileObjects(textU)).contains(textFO));
    }

    public void testNbhostURLs() throws Exception { // #207690
        FileObject r = FileUtil.getConfigRoot();
        check(r);
        FileObject d = r.createFolder("some folder");
        check(d);
        FileObject f = d.createData("file #1");
        check(f);
        new XMLFileSystem().getRoot().toURI(); // anonymous, cannot round-trip
    }
    private static void check(FileObject f) throws Exception {
        URL u = f.toURL();
        assertEquals(u.toURI(), f.toURI());
        assertEquals(u.toString(), f, URLMapper.findFileObject(u));
    }
    
}
