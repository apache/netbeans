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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import junit.framework.Test;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/** Test layering of filesystems installed via lookup.
 *
 * @author Jaroslav Tulach
 */
public class DynamicSFSFallbackTest extends NbTestCase
implements InstanceContent.Convertor<FileSystem,FileSystem> {
    FileSystem fs1;
    FileSystem fs2;
    
    public DynamicSFSFallbackTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return
            NbModuleSuite.emptyConfiguration().
            addTest(DynamicSFSFallbackTest.class).
            clusters("org-netbeans-core-ui.*")
        .suite();
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        MainLookup.unregister(fs1, this);
        MainLookup.unregister(fs2, this);
    }
    


    public void testDynamicSystemsCanAlsoBeBehindLayers() throws Exception {
        FileObject global = FileUtil.getConfigFile("Toolbars/Standard.xml");
        assertNotNull("File Object installed: " + global, global);
        if (global.asText().indexOf("<Toolbar name=") == -1) {
            fail("Expecting toolbar definition: " + global.asText());
        }

        final LocalFileSystem lfs1 = new LocalFileSystem();
        File dir1 = new File(getWorkDir(), "dir1");
        dir1.mkdirs();
        lfs1.setRootDirectory(dir1);
        lfs1.getRoot().setAttribute("fallback", Boolean.TRUE);
        assertEquals("Position attribute is there", Boolean.TRUE, lfs1.getRoot().getAttribute("fallback"));
        fs1 = lfs1;
        fs2 = FileUtil.createMemoryFileSystem();

        FileObject fo1 = FileUtil.createData(fs1.getRoot(), global.getPath());
        fo1.setAttribute("one", 1);
        write(fo1, "fileone");

        FileObject fo11 = FileUtil.createData(fs1.getRoot(), "test-fs-is-there.txt");
        write(fo11, "hereIam");

        MainLookup.register(fs1, this);
        MainLookup.register(fs2, this);

        Iterator<? extends FileSystem> it = Lookup.getDefault().lookupAll(FileSystem.class).iterator();
        assertTrue("At least One", it.hasNext());
        assertEquals("first is fs1", fs1, it.next());
        assertTrue("At least two ", it.hasNext());
        assertEquals("first is fs2", fs2, it.next());

        if (global.asText().indexOf("<Toolbar name=") == -1) {
            fail("Still Expecting toolbar definition: " + global.asText());
        }
        assertTrue("Still valid", global.isValid());

        FileObject fo = FileUtil.getConfigFile("test-fs-is-there.txt");
        assertNotNull("File found: " + Arrays.toString(FileUtil.getConfigRoot().getChildren()), fo);
        assertEquals("Text is correct", "hereIam", fo.asText());
    }
    
    private static void write(FileObject fo, String txt) throws IOException {
        OutputStream os = fo.getOutputStream();
        os.write(txt.getBytes());
        os.close();
    }
    
    public FileSystem convert(FileSystem obj) {
        return obj;
    }

    public Class<? extends FileSystem> type(FileSystem obj) {
        return obj.getClass();
    }

    public String id(FileSystem obj) {
        return obj.getDisplayName();
    }

    public String displayName(FileSystem obj) {
        return obj.getDisplayName();
    }
}
