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

package org.netbeans.modules.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.NamedServicesLookupTest;

/** Test finding services from manifest and .instance files.
 * @author Jaroslav Tulach
 */
public class RecognizeInstanceFilesTest extends NamedServicesLookupTest {
    private FileObject root;
    private Logger LOG;
    
    public RecognizeInstanceFilesTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }
    
    @Override
    protected void setUp() throws Exception {
        if (System.getProperty("netbeans.user") == null) {
            System.setProperty("netbeans.user", new File(getWorkDir(), "ud").getPath());
        }
        
        LOG = Logger.getLogger("Test." + getName());
        
        root = FileUtil.getConfigRoot();
        for (FileObject fo : root.getChildren()) {
            fo.delete();
        }
        
        super.setUp();
    }
    
    public void testLoadFromTheSFS() throws Exception {
        doLoad("inst/sub");
    }
    public void testLoadFromSubdirTheSFS() throws Exception {
        doLoad("inst");
    }
    
    private void doLoad(String lkpName) throws Exception {
        FileObject inst = FileUtil.createData(root, "inst/sub/X.instance");
        inst.setAttribute("instanceCreate", Long.valueOf(1000));
        
        Lookup l = Lookups.forPath(lkpName);
        Long lng = l.lookup(Long.class);
        assertNotNull("A value found", lng);
        
        inst.delete();
        
        assertNull("Now it is null", l.lookup(Long.class));
    }
    
    public void testOrderingAttributes() throws Exception {
        LOG.info("creating instances");
        
        FileObject inst = FileUtil.createData(root, "inst/ordering/X.instance");
        inst.setAttribute("instanceCreate", Long.valueOf(1000));
        FileObject inst2 = FileUtil.createData(root, "inst/ordering/A.instance");
        inst2.setAttribute("instanceCreate", Long.valueOf(500));
        FileObject inst3 = FileUtil.createData(root, "inst/ordering/B.instance");
        inst3.setAttribute("instanceCreate", Long.valueOf(1500));
        FileObject inst4 = FileUtil.createData(root, "inst/ordering/C.instance");
        inst4.setAttribute("instanceCreate", Long.valueOf(700));
        
        LOG.info("Adding attributes to parrent");
        FileObject parent = inst.getParent();
        parent.setAttribute("A.instance/C.instance", Boolean.TRUE);
        parent.setAttribute("C.instance/X.instance", Boolean.TRUE);
        parent.setAttribute("X.instance/B.instance", Boolean.TRUE);
        
        
        LOG.info("About to create lookup");
        Lookup l = Lookups.forPath("inst/ordering");
        LOG.info("querying lookup");
        Collection<? extends Long> lngAll = l.lookupAll(Long.class);
        assertEquals(4, lngAll.size());
        Iterator<? extends Long> lng = lngAll.iterator();
        LOG.info("checking results");
        
        assertEquals(Long.valueOf(500), lng.next());
        assertEquals(Long.valueOf(700), lng.next());
        assertEquals(Long.valueOf(1000), lng.next());
        assertEquals(Long.valueOf(1500), lng.next());
        
        LOG.info("Order is correct");

        Iterator<? extends Lookup.Item<Long>> items = l.lookupResult(Long.class).allItems().iterator();
        
        LOG.info("Checking IDs");
        assertEquals("inst/ordering/A", items.next().getId());
        assertEquals("inst/ordering/C", items.next().getId());
        assertEquals("inst/ordering/X", items.next().getId());
        assertEquals("inst/ordering/B", items.next().getId());
        
        LOG.info("Ids ok");
    }
    
    public void testUnderstandsShadowFiles() throws Exception {
        LOG.info("creating instances");
        
        FileObject inst = FileUtil.createData(root, "inst/real/X.instance");
        inst.setAttribute("instanceCreate", Long.valueOf(1000));

        FileObject shadow = FileUtil.createData(root, "inst/shadow/X.shadow");
        shadow.setAttribute("originalFile", inst.getPath());
        
        Long l = Lookups.forPath("inst/shadow").lookup(Long.class);
        assertEquals("1000 found", Long.valueOf(1000), l);
    }

    public void testNumericOrdering() throws Exception {
        class Tst implements FileSystem.AtomicAction {
            public void run() throws IOException {
                init();
            }
            
            void init() throws IOException {
                FileObject inst = FileUtil.createData(root, "inst/positional/X.instance");
                inst.setAttribute("instanceCreate", Long.valueOf(1000));
                inst.setAttribute("position", 3);
                FileObject inst2 = FileUtil.createData(root, "inst/positional/A.instance");
                inst2.setAttribute("instanceCreate", Long.valueOf(500));
                inst2.setAttribute("position", 1);
                FileObject inst3 = FileUtil.createData(root, "inst/positional/B.instance");
                inst3.setAttribute("instanceCreate", Long.valueOf(1500));
                inst3.setAttribute("position", 4);
                FileObject inst4 = FileUtil.createData(root, "inst/positional/C.instance");
                inst4.setAttribute("instanceCreate", Long.valueOf(700));
                inst4.setAttribute("position", 2);
            }
            
            void verify() {
                Lookup l = Lookups.forPath("inst/positional");
                Iterator<? extends Long> lng = l.lookupAll(Long.class).iterator();
                assertEquals(Long.valueOf(500), lng.next());
                assertEquals(Long.valueOf(700), lng.next());
                assertEquals(Long.valueOf(1000), lng.next());
                assertEquals(Long.valueOf(1500), lng.next());
                Iterator<? extends Lookup.Item<Long>> items = l.lookupResult(Long.class).allItems().iterator();
                assertEquals("inst/positional/A", items.next().getId());
                assertEquals("inst/positional/C", items.next().getId());
                assertEquals("inst/positional/X", items.next().getId());
                assertEquals("inst/positional/B", items.next().getId());
            }
        }
        
        Tst tst = new Tst();
        root.getFileSystem().runAtomicAction(tst);
        tst.verify();
    }

    public void testSupportsClassNameEncodedInAFileName() throws Exception {
        FileObject inst = FileUtil.createData(root, "inst/class/" + Inst.class.getName().replace('.', '-') + ".instance");
        Lookup l = Lookups.forPath("inst/class");
        assertNotNull("Instance created", l.lookup(Inst.class));
    }

    public void testNoItemsForSillyPairs() throws Exception {
        FileObject inst = FileUtil.createData(root, "silly/class/File.silly");
        CharSequence log = Log.enable("org.openide.filesystems", Level.WARNING);
        Lookup l = Lookups.forPath("silly/class");
        Collection<?> c = l.lookupResult(Object.class).allItems();
        assertTrue("No items created: " + c, c.isEmpty());
        assertEquals("No warnings:\n" + log, 0, log.length());
    }
    
    public static final class Inst extends Object {
    }

    public void testNullForFolders() throws Exception {
        FileObject data = FileUtil.createFolder(root, "dir/" + Shared.class.getName().replace('.', '-') + ".instance");
        Shared nul = FileUtil.getConfigObject(data.getPath(), Shared.class);
        assertNull("No object for folders", nul);
    }

    public static final class Shared {}

    public void testDoNotCreateFoldersJustBecauseILookedThemUp() throws Exception {
        assertEquals(0, Lookups.forPath("nonexistent").lookupAll(Object.class).size());
        assertNull(FileUtil.getConfigFile("nonexistent"));
    }

    public interface Doable extends Runnable {}
    public static final class Do implements Doable {
        @Override public void run() {}
        @Override public String toString() {
            return "Do";
        }
    }
    public void testInstanceOf() throws Exception {
        FileObject i1 = FileUtil.createData(FileUtil.getConfigRoot(), "d/one.instance");
        i1.setAttribute("instanceOf", Runnable.class.getName());
        i1.setAttribute("instanceClass", Do.class.getName());
        FileObject i2 = FileUtil.createData(FileUtil.getConfigRoot(), "d/two.instance");
        i2.setAttribute("instanceOf", Runnable.class.getName() + ',' + Doable.class.getName());
        i2.setAttribute("instanceClass", Do.class.getName());
        FileObject i3 = FileUtil.createData(FileUtil.getConfigRoot(), "d/three.instance");
        i3.setAttribute("instanceOf", Runnable.class.getName() + ',' + Doable.class.getName() + ',' + Do.class.getName());
        i3.setAttribute("instanceClass", Do.class.getName());
        assertEquals("[Do]", Lookups.forPath("d").lookupAll(Do.class).toString());
        assertEquals("[Do, Do]", Lookups.forPath("d").lookupAll(Doable.class).toString());
        assertEquals("[Do, Do, Do]", Lookups.forPath("d").lookupAll(Runnable.class).toString());
    }

    public void testAvoidStackTracesForMissingInstanceOf() throws Exception {
        FileObject i = FileUtil.createData(FileUtil.getConfigRoot(), "d/meaningless-name.instance");
        i.setAttribute("instanceOf", "nonexistent.Interface");
        i.setAttribute("instanceClass", "nonexistent.Class");
        doTestAvoidStackTraces();
    }

    public void testAvoidStackTracesForUnloadableInstanceCreate() throws Exception {
        FileObject i = FileUtil.createData(FileUtil.getConfigRoot(), "d/meaningless-name.instance");
        // simulate a methodvalue that cannot be loaded:
        i.setAttribute("instanceCreate", "temp"); // make sure MemoryFileSystem.writeAttribute puts attr name in key set
        i.setAttribute("instanceCreate", null); // but then set the value to null (current impl does not remove entry)
        doTestAvoidStackTraces();
    }

    private void doTestAvoidStackTraces() throws Exception {
        CharSequence log = Log.enable(RecognizeInstanceFiles.class.getName(), Level.INFO);
        Lookup lkp = Lookups.forPath("d");
        assertEquals(null, lkp.lookup(Runnable.class));
        Collection<? extends Lookup.Item<Object>> items = lkp.lookupResult(Object.class).allItems();
        if (!items.isEmpty()) {
            assertEquals(1, items.size());
            Lookup.Item<Object> item = items.iterator().next();
            assertEquals(Void.class, item.getType());
            assertEquals(null, item.getInstance());
        }
        assertEquals("", log.toString());
        // this ignores instanceOf (probably OK) and does print a CNFE stack (also probably OK):
        assertEquals(null, FileUtil.getConfigObject("d/meaningless-name.instance", Object.class));
    }

}
