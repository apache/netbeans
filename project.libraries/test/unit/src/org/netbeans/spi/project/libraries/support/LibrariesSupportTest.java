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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.spi.project.libraries.support;

import java.io.File;
import java.net.URI;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public class LibrariesSupportTest extends NbTestCase {

    public LibrariesSupportTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test of convertFilePathToURL method, of class LibrariesSupport.
     */
    public void testConvertFilePathToURI() throws Exception {
        String path = getWorkDirPath()+"/aa/bb/c c.ext".replace('/', File.separatorChar);
        URI u = LibrariesSupport.convertFilePathToURI(path);
        assertEquals(Utilities.toURI(FileUtil.normalizeFile(new File(path))), u);
        path = "../zz/re l.ext".replace('/', File.separatorChar);
        u = LibrariesSupport.convertFilePathToURI(path);
        assertEquals("../zz/re%20l.ext", u.toString());
    }

    /**
     * Test of convertURLToFilePath method, of class LibrariesSupport.
     */
    public void testConvertURIToFilePath() throws Exception{
        URI u = Utilities.toURI(getWorkDir());
        String path = LibrariesSupport.convertURIToFilePath(u);
        assertEquals(getWorkDir().getPath(), path);
        u = new URI(null, null, "../zz/re l.ext", null);
        path = LibrariesSupport.convertURIToFilePath(u);
        assertEquals("../zz/re l.ext".replace('/', File.separatorChar), path);
    }

    /**
     * Test of resolveLibraryEntryFileObject method, of class LibrariesSupport.
     */
    public void testResolveLibraryEntryFileObject() throws Exception {
        File f = new File(this.getWorkDir(), "knihovna.properties");
        File f2 = new File(this.getWorkDir(), "bertie.jar");
        f.createNewFile();
        f2.createNewFile();
        FileObject fo = LibrariesSupport.resolveLibraryEntryFileObject(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "bertie.jar", null));
        assertEquals(f2.getPath(), FileUtil.toFile(fo).getPath());
        fo = LibrariesSupport.resolveLibraryEntryFileObject(
                null, 
                Utilities.toURI(f2));
        assertEquals(f2.getPath(), FileUtil.toFile(fo).getPath());
    }

    public void testResolveLibraryEntryURI() throws Exception {
        File f = new File(this.getWorkDir(), "knihovna.properties");
        File f2 = new File(this.getWorkDir(), "ber tie.jar");
        f.createNewFile();
        f2.createNewFile();
        URI u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "ber tie.jar", null));
        assertEquals(Utilities.toURI(new File(f.getParentFile(), f2.getName())), u);
        u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                Utilities.toURI(f2));
        assertEquals(Utilities.toURI(f2), u);
        u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "ber tie.jar!/main/ja va", null));
        assertEquals(new URI("jar:"+ (Utilities.toURI(new File(f.getParentFile(), f2.getName())).toString()) + "!/main/ja%20va"), u);
        u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "../"+getWorkDir().getName()+"/ber tie.jar!/main/ja va", null));
        assertEquals(new URI("jar:"+ (Utilities.toURI(new File(f.getParentFile(), f2.getName())).toString()) + "!/main/ja%20va"), u);
        u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "../a folder/", null));
        assertEquals(new URI(Utilities.toURI(new File(getWorkDir().getParentFile(), "a folder")).toString() + "/"), u);
        // UNC paths
        URI uncBaseURI = URI.create("file://computerName/sharedFolder/a/b/c/d.properties");
        URI uncEntryURI = URI.create("e/e.jar");
        URI expectedURI = URI.create("file://computerName/sharedFolder/a/b/c/e/e.jar");
        URI resolvedURI = LibrariesSupport.resolveLibraryEntryURI(uncBaseURI.toURL(), uncEntryURI);
        assertEquals("UNC entry wrongly resolved.", expectedURI, resolvedURI);
        uncEntryURI = new URI(null, null, "e/e.jar!/f f/f", null);
        expectedURI = URI.create("jar:file://computerName/sharedFolder/a/b/c/e/e.jar!/f%20f/f");
        resolvedURI = LibrariesSupport.resolveLibraryEntryURI(uncBaseURI.toURL(), uncEntryURI);
        assertEquals("UNC jar entry wrongly resolved.", expectedURI, resolvedURI);
        uncEntryURI = new URI(null, null, "e/e.jar!/", null);
        expectedURI = URI.create("jar:file://computerName/sharedFolder/a/b/c/e/e.jar!/");
        resolvedURI = LibrariesSupport.resolveLibraryEntryURI(uncBaseURI.toURL(), uncEntryURI);
        assertEquals("UNC jar entry wrongly resolved.", expectedURI, resolvedURI);   
    }

}
