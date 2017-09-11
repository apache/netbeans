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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.SetupHid;
import org.netbeans.core.startup.layers.SystemFileSystem;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/** Test layering of filesystems installed via lookup.
 *
 * @author Jaroslav Tulach
 */
public class SystemFileSystemTest extends SetupHid
implements InstanceContent.Convertor<FileSystem,FileSystem>, FileChangeListener {
    FileSystem fs1 = FileUtil.createMemoryFileSystem();
    FileSystem fs2 = FileUtil.createMemoryFileSystem();
    private List<FileEvent> events;
    
    public SystemFileSystemTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        for (FileObject fo : FileUtil.getConfigRoot().getChildren()) {
            fo.delete();
        }
        events = new LinkedList<FileEvent>();
        
        FileUtil.getConfigRoot().getFileSystem().addFileChangeListener(this);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        MainLookup.unregister(fs1, this);
        MainLookup.unregister(fs2, this);
    }
    
    public void testNPEIssue122654() throws Exception {
        SystemFileSystem sfs = SystemFileSystem.create(null, getWorkDir(), new File[0]);
        assertNotNull("Created", sfs);
    }

    public void testUserHasPreferenceOverFSs() throws Exception {
        FileObject global = FileUtil.createData(FileUtil.getConfigRoot(), "dir/file.txt");
        global.setAttribute("global", 3);
        write(global, "global");
        
        FileObject fo1 = FileUtil.createData(fs1.getRoot(), "dir/file.txt");
        fo1.setAttribute("one", 1);
        write(fo1, "fileone");
        
        FileObject fo2 = FileUtil.createData(fs2.getRoot(), "dir/file.txt");
        fo2.setAttribute("two", 2);
        write(fo2, "two");

        events.clear();
        
        MainLookup.register(fs1, this);
        MainLookup.register(fs2, this);
        
        Enumeration<String> en = global.getAttributes();
        TreeSet<String> t = new TreeSet<String>();
        while (en.hasMoreElements()) {
            t.add(en.nextElement());
        }
        
        assertEquals("three elements: " + t, 3, t.size());
        assertTrue(t.contains("two"));
        assertTrue(t.contains("one"));
        assertTrue(t.contains("global"));
        
        assertEquals(1, global.getAttribute("one"));
        assertEquals(2, global.getAttribute("two"));
        assertEquals(3, global.getAttribute("global"));

        assertEquals("contains global", 6, global.getSize());
        assertEquals("global", read(global));
        
        
        assertTrue("no events: " + events, events.isEmpty());
    }

    public void testUserHasPreferenceOverFSsButGeneratesAnEvent() throws Exception {
        FileObject fo1 = FileUtil.createData(fs1.getRoot(), "dir/file.txt");
        fo1.setAttribute("one", 1);
        write(fo1, "fileone");
        
        FileObject fo2 = FileUtil.createData(fs2.getRoot(), "dir/file.txt");
        fo2.setAttribute("two", 2);
        write(fo2, "two");

        events.clear();
        
        MainLookup.register(fs1, this);
        MainLookup.register(fs2, this);

        assertFalse("not empty", events.isEmpty());
        events.clear();
        
        FileObject global = FileUtil.createData(FileUtil.getConfigRoot(), "dir/file.txt");
        global.setAttribute("global", 3);
        write(global, "global");
        
        assertFalse("yet another set", events.isEmpty());
        
        Enumeration<String> en = global.getAttributes();
        TreeSet<String> t = new TreeSet<String>();
        while (en.hasMoreElements()) {
            t.add(en.nextElement());
        }

        
        
        assertEquals("three elements: " + t, 3, t.size());
        assertTrue(t.contains("two"));
        assertTrue(t.contains("one"));
        assertTrue(t.contains("global"));
        
        assertEquals(1, global.getAttribute("one"));
        assertEquals(2, global.getAttribute("two"));
        assertEquals(3, global.getAttribute("global"));

        assertEquals("contains global", 6, global.getSize());
        assertEquals("global", read(global));
    }
    
    public void testPreferenceOfLayersNowDynamicSystemsCanHideWhatComesFromLayers() throws Exception {
        ModuleManager mgr = Main.getModuleSystem ().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
        FileObject global;
        try {
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            mgr.enable(m1);
            global = FileUtil.getConfigFile("foo/file2.txt");
            assertNotNull("File Object installed: " + global, global);
            assertEquals("base contents", read(global));
            
            FileObject fo1 = FileUtil.createData(fs1.getRoot(), global.getPath());
            fo1.setAttribute("one", 1);
            write(fo1, "fileone");

            FileObject fo2 = FileUtil.createData(fs2.getRoot(), global.getPath());
            fo2.setAttribute("two", 2);
            write(fo2, "two");

            events.clear();

            MainLookup.register(fs1, this);
            MainLookup.register(fs2, this);
            
            Iterator<? extends FileSystem> it = Lookup.getDefault().lookupAll(FileSystem.class).iterator();
            assertTrue("At least One", it.hasNext());
            assertEquals("first is fs1", fs1, it.next());
            assertTrue("At least two ", it.hasNext());
            assertEquals("first is fs2", fs2, it.next());
            
            assertEquals("fileone", read(global));
        } finally {
            mgr.disable(m1);
            mgr.delete(m1);
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertTrue("Still valid", global.isValid());
        assertEquals("fileone", read(global));
    }
    
    public void testCanHideFilesFromModules() throws Exception {
        ModuleManager mgr = Main.getModuleSystem ().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
        try {
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            mgr.enable(m1);
            FileObject global = FileUtil.getConfigFile("foo/file2.txt");
            assertNotNull("File Object installed: " + global, global);
            assertEquals("base contents", read(global));
            
            FileObject fo1 = FileUtil.createData(fs1.getRoot(), global.getPath() + "_hidden");

            events.clear();
            MainLookup.register(fs1, this);

            assertNull("No longer findable", global.getFileSystem().findResource(global.getPath()));
            assertFalse("Is not valid anymore", global.isValid());
        } finally {
            mgr.disable(m1);
            mgr.delete(m1);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    private static void write(FileObject fo, String txt) throws IOException {
        OutputStream os = fo.getOutputStream();
        os.write(txt.getBytes());
        os.close();
    }
    
    private static String read(FileObject fo) throws IOException {
        byte[] arr = new byte[(int)fo.getSize()];
        InputStream is = fo.getInputStream();
        int len = is.read(arr);
        assertEquals("Not enough read", arr.length, len);
        return new String(arr);
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

    public void fileFolderCreated(FileEvent fe) {
        events.add(fe);
    }

    public void fileDataCreated(FileEvent fe) {
        events.add(fe);
    }

    public void fileChanged(FileEvent fe) {
        events.add(fe);
    }

    public void fileDeleted(FileEvent fe) {
        events.add(fe);
    }

    public void fileRenamed(FileRenameEvent fe) {
        events.add(fe);
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        events.add(fe);
    }
}
