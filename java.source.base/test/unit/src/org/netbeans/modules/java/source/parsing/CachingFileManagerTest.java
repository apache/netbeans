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

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CachingFileManagerTest extends NbTestCase {

    public CachingFileManagerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }


    public void testGetFileForInputWithFolderArchive() throws  Exception {
        final File wd = getWorkDir();
        final org.openide.filesystems.FileObject root = FileUtil.createFolder(new File (wd,"src"));
        final org.openide.filesystems.FileObject data = FileUtil.createData(root, "org/me/resources/test.txt");
        final URI expectedURI = data.getURL().toURI();
        doTestGetFileForInput(ClassPathSupport.createClassPath(root),Arrays.asList(
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("","org/me/resources/test.txt"), expectedURI),
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("org.me","resources/test.txt"), expectedURI),
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("org.me","resources/doesnotexist.txt"), null)
        ));

    }

    public void testGetFileForInputWithCachingArchive() throws  Exception {
        final File wd = getWorkDir();
        final File archiveFile = new File (wd, "src.zip");
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archiveFile));
        try {
            out.putNextEntry(new ZipEntry("org/me/resources/test.txt"));
            out.write("test".getBytes());
        } finally {
            out.close();
        }
        final URL archiveRoot = FileUtil.getArchiveRoot(Utilities.toURI(archiveFile).toURL());
        final URI expectedURI = new URL (archiveRoot.toExternalForm()+"org/me/resources/test.txt").toURI();
        doTestGetFileForInput(ClassPathSupport.createClassPath(archiveRoot),
        Arrays.asList(
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("","org/me/resources/test.txt"), expectedURI),
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("org.me","resources/test.txt"), expectedURI),
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("org.me","resources/doesnotexist.txt"), null)
        ));

    }

    private void doTestGetFileForInput(
            final ClassPath cp,
            final List<? extends Pair<Pair<String,String>,URI>> testCases) throws IOException, URISyntaxException {
        final CachingArchiveProvider provider = CachingArchiveProvider.getDefault();
        final CachingFileManager manager = new CachingFileManager(provider, cp, null, false, true);
        for (Pair<Pair<String,String>,URI> testCase : testCases) {
            final Pair<String,String> name = testCase.first();
            final URI expectedURI = testCase.second();
            FileObject fo = manager.getFileForInput(StandardLocation.CLASS_PATH, name.first(), name.second());
            if (expectedURI == null) {
                assertNull(
                    String.format("Lookup: %s/%s expected: null",
                    name.first(),
                    name.second()),
                    fo);
            } else {
                assertEquals(
                    String.format("Lookup: %s/%s expected: %s",
                    name.first(),
                    name.second(),
                    expectedURI),
                    expectedURI,
                    fo.toUri());
            }
        }        
    }
    
}
