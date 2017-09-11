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

package org.netbeans.modules.masterfs;

import java.io.File;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Radek Matous
 */
public class URLMapperTest extends NbTestCase {
    private static FileSystem mfs;
    public URLMapperTest(String name) {
        super(name);
        mfs = FileBasedFileSystem.getInstance();
    }

    public void testURLMapperCallingFromMetaInfLookup() {
        Lookup lkp = Lookups.metaInfServices(Thread.currentThread().getContextClassLoader());
        Object obj = lkp.lookup(Object.class);
        assertNotNull(obj);
        assertEquals(MyInstance2.class, obj.getClass());
    }
    
    public void testNormalize8_3Names() throws Exception {
        if (!Utilities.isWindows()) {
            return;
        }
        
        clearWorkDir();
        File f = new File(getWorkDir(), "my new temp folder with long name");
        f.mkdirs();
        
        File shrtF = new File(getWorkDir(), "MYNEWT~1");
        assertTrue("Short name exists", shrtF.isDirectory());
        
        final URL u = shrtF.toURI().toURL();
        final FileObject fo = URLMapper.findFileObject(u);
        File found = FileUtil.toFile(fo);
        
        assertEquals("The same file as original", f, found);
    }

    @ServiceProvider(service=Object.class)
    public static class MyInstance2 {
        public MyInstance2() {
            super();
            testURLMapper();
        }

        private static void testURLMapper() {            
            assertNotNull(mfs);
            FileObject[] children = mfs.getRoot().getChildren();
            for (int i = 0; i < children.length; i++) {
                java.io.File file = FileUtil.toFile(children[i]);
                assertNotNull(file);
                assertNotNull(FileUtil.toFileObject(file));
            }
        }

    }

}
