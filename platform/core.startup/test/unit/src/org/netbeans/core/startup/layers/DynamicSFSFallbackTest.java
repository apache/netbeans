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
