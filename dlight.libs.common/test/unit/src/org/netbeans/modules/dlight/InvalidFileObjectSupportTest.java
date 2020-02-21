/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight;

import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 */
public class InvalidFileObjectSupportTest {

    static {
        // otherwise we get java.net.MalformedURLException: unknown protocol
        // even if we register via @URLStreamHandlerRegistration annotation
        URL.setURLStreamHandlerFactory(Lookup.getDefault().lookup(URLStreamHandlerFactory.class));
    }    

    @Test
    public void testInvalidFileObjectURL() throws Exception {
        // see #270390 - StackOverflowError at java.io.UnixFileSystem.getBooleanAttributes
        FileSystem dummyFS = InvalidFileObjectSupport.getDummyFileSystem();
        FileObject invalidFO = InvalidFileObjectSupport.getInvalidFileObject(dummyFS, "/inexistent");
        final URL url = invalidFO.getURL();
        FileObject foundFO = URLMapper.findFileObject(url);
        //assertEquals("Invalid and found by URL ", invalidFO, foundFO);
    }

    @Test
    public void testInvalidFileObject() throws Exception {
        File file = File.createTempFile("qwe", "asd");
        FileObject origFo = null;
        String path = null;
        FileSystem fs = null;
        try {
            origFo = FileUtil.toFileObject(FileUtil.normalizeFile(file)); // FileUtil SIC!
            assertNotNull(origFo);
            path = origFo.getPath();
            fs = origFo.getFileSystem();
        } finally {
            file.delete();
        }
        FileObject invalidFo1 = InvalidFileObjectSupport.getInvalidFileObject(fs, path);
        URI uri1 = invalidFo1.toURI(); // just to check that there is no assertions
        URL url1 = invalidFo1.toURL(); // just to check that there is no assertions
        FileObject invalidFo2 = InvalidFileObjectSupport.getInvalidFileObject(fs, path);
        URI uri2 = invalidFo2.toURI(); // just to check that there is no assertions
        URL url2 = invalidFo2.toURL(); // just to check that there is no assertions
        assertTrue(invalidFo1 == invalidFo2);
        assertFalse(invalidFo1.isValid());
        assertEquals(origFo.getName(), invalidFo1.getName());
        assertEquals(origFo.getExt(), invalidFo1.getExt());
        String p1 = origFo.getPath();
        String p2 = invalidFo1.getPath();
        boolean eq = p1.equals(p2);
        assertEquals(origFo.getPath(), invalidFo1.getPath());
        assertEquals(origFo.getNameExt(), invalidFo1.getNameExt());
        assertEquals(origFo.getFileSystem(), invalidFo1.getFileSystem());
        FileObject invalidFo4 = InvalidFileObjectSupport.getInvalidFileObject(fs, "/tmp/foo.bar.cpp");
        assertNotNull(invalidFo4);
        assertEquals("getName()", "foo.bar", invalidFo4.getName());
        assertEquals("getExt()", "cpp", invalidFo4.getExt());
        FileObject invalidFo5 = InvalidFileObjectSupport.getInvalidFileObject(fs, "/tmp/qwe.asd/foo1.bar1.cc");
        assertNotNull(invalidFo5);
        assertEquals("getName()", "foo1.bar1", invalidFo5.getName());
        assertEquals("getExt()", "cc", invalidFo5.getExt());
    }
    
    @Test
    public void testInvalidFileObjectParent() throws Exception {
        File file = File.createTempFile("qwe", "asd");
        FileObject origFo = null;
        String path = null;
        FileSystem fs = null;
        try {
            origFo = FileUtil.toFileObject(file); // FileUtil SIC!
            assertNotNull(origFo);
            path = origFo.getPath();
            fs = origFo.getFileSystem();
        } finally {
            file.delete();
        }
        FileObject invalidFo = InvalidFileObjectSupport.getInvalidFileObject(fs, path);
        assertNotNull(invalidFo);
        FileObject parent = invalidFo.getParent();
        assertNotNull(parent);
        assertTrue(parent.isValid());
    }
    
}
