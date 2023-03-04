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

package org.openide.loaders;


import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.util.logging.Level;
import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.actions.EditAction;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Check what can be done when registering loaders in layer.
 * @author Jaroslav Tulach
 */
@RandomlyFails
public class DataLoaderInLayerTest extends NbTestCase {
    static Logger LOG;

    public DataLoaderInLayerTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }
    
    protected FileSystem createFS(String... resources) throws IOException {
        return TestUtilHid.createLocalFileSystem(getWorkDir(), resources);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LOG = Logger.getLogger("test." + getName());
        FileUtil.setMIMEType("simple", "text/plain");
        FileUtil.setMIMEType("ant", "text/ant+xml");
        LOG.info("setUp is over");
    }

    @Override
    protected void tearDown() throws Exception {
        LOG.info("tearDown");
    }
    
    private static void addRemoveLoader(DataLoader l, boolean add) throws Exception {
        addRemoveLoader("text/plain", l, add);
    }
    private static void addRemoveLoader(String mime, DataLoader l, boolean add) throws Exception {
        addRemove(mime, l.getClass(), add);
    }
    private static <F extends DataObject.Factory> void addRemove(String mime, final Class<F> clazz, final boolean add) throws Exception {
        final Lookup lkp = Lookups.forPath("Loaders/" + mime + "/Factories");
        final String res = "Loaders/" + mime + "/Factories/" + clazz.getSimpleName().replace('.', '-') + ".instance";
        final FileObject root = FileUtil.getConfigRoot();
        class R implements FileSystem.AtomicAction {
            @Override
            public void run() throws IOException {
                if (add) {
                    FileObject fo = FileUtil.createData(root, res);
                    fo.setAttribute("instanceClass", clazz.getName());
                } else {
                    FileObject fo = root.getFileObject(res);
                    if (fo != null) {
                        fo.delete();
                    }
                }
            }
        }
        LOG.log(Level.INFO, "Wait before modifying {0}", res);
        Collection<? extends F> tmpRes = lkp.lookupAll(clazz);
        FolderLookup.ProxyLkp.DISPATCH.waitFinished();
        LOG.log(Level.INFO, "Modifying {0}", res);
        FileUtil.runAtomicAction(new R());
        LOG.info("Modification done");
        for (int i = 0; i < 100; i++) {
            Object f = lkp.lookup(clazz);
            FolderLookup.ProxyLkp.DISPATCH.waitFinished();
            LOG.log(Level.INFO, "waiting for {0} at #{1} result: {2}", new Object[]{add ? "add" : "remove", i, f});
            if (add == (f != null)) {
                break;
            }
            Thread.sleep(100);
        }
        LOG.info("OK, addRemove finished");
        // XXX: Probably DataLoaderPool shall listen on changes under Loaders/.../.../Factories and revalidate
        // automatically
        DataObjectPool.getPOOL().revalidate();
        LOG.info("revalidating finished");
        tmpRes = lkp.lookupAll(clazz);
        FolderLookup.ProxyLkp.DISPATCH.waitFinished();
        LOG.info("wait after revalidating");
    }
    private static <F extends DataObject.Factory> void addRemove(String mime, F factory, boolean add) throws IOException {
        LOG.log(Level.INFO, "addRemove {0} factory: {1} add: {2}", new Object[]{mime, factory, add});
        String res = "Loaders/" + mime + "/Factories/" + factory.getClass().getSimpleName().replace('.', '-') + ".instance";
        FileObject root = FileUtil.getConfigRoot();
        if (add) {
            FileObject fo = FileUtil.createData(root, res);
            fo.setAttribute("instanceCreate", factory);
            assertSame("No serialization, just memory fs is used", factory, fo.getAttribute("instanceCreate"));
        } else {
            FileObject fo = root.getFileObject(res);
            if (fo != null) {
                fo.delete();
            }
        }
        for (int cnt = 0;; cnt++) {
            Object f = Lookups.forPath("Loaders/" + mime + "/Factories").lookup(factory.getClass());
            if (cnt > 5) {
                LOG.log(Level.WARNING, "Waiting cnt: {0} factory: {1}", new Object[]{cnt, f});
            }
            FolderLookup.ProxyLkp.DISPATCH.waitFinished();
            if (add == (f != null)) {
                break;
            }
        }
        LOG.log(Level.INFO, "done addRemove {0} factory: {1} add: {2}", new Object[]{mime, factory, add});
    }
    
    public void testSimpleGetChildren() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            FileSystem lfs = createFS("folder/file.simple");
            FileObject fo = lfs.findResource("folder");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object: " + Arrays.toString(arr), 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());

            DataObject copied = dob.copy(df);
            assertEquals(SimpleDataObject.class, copied.getClass());

            LOG.info("Before createFromTemplate");
            DataObject templ = dob.createFromTemplate(df, "ahoj");
            assertEquals(SimpleDataObject.class, templ.getClass());
            assertEquals("ahoj", templ.getName());

            LOG.info("before copyRename");
            DataObject ren = dob.copyRename(df, "kuk", "simple");
            assertEquals(SimpleDataObject.class, ren.getClass());
            assertEquals("kuk", ren.getName());
            LOG.info("OK");
        } finally {
            addRemoveLoader(l, false);
        }
    }

    public void testFactoryRegistrationWorksAsWell() throws Exception {
        addRemove("text/plain", SimpleFactory.class, true);
        try {
            FileSystem lfs = createFS("folderF/file.simple");
            FileObject fo = lfs.findResource("folderF");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());

            DataObject copied = dob.copy(df);
            assertEquals(SimpleDataObject.class, copied.getClass());

            DataObject templ = dob.createFromTemplate(df, "ahoj");
            assertEquals(SimpleDataObject.class, templ.getClass());
            assertEquals("ahoj", templ.getName());

            DataObject ren = dob.copyRename(df, "kuk", "simple");
            assertEquals(SimpleDataObject.class, ren.getClass());
            assertEquals("kuk", ren.getName());
        } finally {
            addRemove("text/plain", SimpleFactory.class, false);
        }
    }

    public void testFactoryInstanceRegistrationWorksAsWell() throws Exception {
        URL u = DataLoaderInLayerTest.class.getResource("/org/openide/loaders/saveAll.gif");
        Image img = Toolkit.getDefaultToolkit().createImage(u);
        
        DataObject.Factory f = DataLoaderPool.factory(SimpleDataObject.class, "text/simplefactory", img);
        
        addRemove("text/plain", f, true);
        try {
            FileSystem lfs = createFS("folderFKK/file.simple");
            FileObject fo = lfs.findResource("folderFKK");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object: " + Arrays.toString(arr), 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());
            
            FileObject root = FileUtil.getConfigRoot();
            FileObject edit = FileUtil.createData(root, "/Loaders/text/simplefactory/Actions/org-openide-actions-EditAction.instance");
            
            Node node = dob.getNodeDelegate();
            Action[] actions = node.getActions(true);
            assertEquals("One action is present: " + Arrays.asList(actions), 1, actions.length);
            assertEquals("It is the edit one", EditAction.class, actions[0].getClass());
            
            assertSame("Icon is propagated for open", img, node.getOpenedIcon(0));
            assertSame("Icon is propagated", img, node.getIcon(0));
            
            Reference<DataFolder> ref = new WeakReference<DataFolder>(df);
            df = null;
            assertGC("Folder can go away", ref);
            
            df = DataFolder.findFolder(fo);
            arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            assertEquals("Object is the same", dob, arr[0]);

            DataObject copied = dob.copy(df);
            assertEquals(SimpleDataObject.class, copied.getClass());

            DataObject templ = dob.createFromTemplate(df, "ahoj");
            assertEquals(SimpleDataObject.class, templ.getClass());
            assertEquals("ahoj", templ.getName());

            DataObject ren = dob.copyRename(df, "kuk", "simple");
            assertEquals(SimpleDataObject.class, ren.getClass());
            assertEquals("kuk", ren.getName());
        } finally {
            addRemove("text/plain", f, false);
        }
    }
    
    public void testFactoryInstanceRegistrationWorksAsWellNowFromLayer() throws Exception {
        URL u = DataLoaderInLayerTest.class.getResource("/org/openide/loaders/saveAll.gif");
        FileObject root = FileUtil.getConfigRoot();
        FileObject instance = FileUtil.createData(root, "TestLoaders/text/L.instance");
        instance.setAttribute("dataObjectClass", SimpleDataObject.class.getName());
        instance.setAttribute("mimeType", "text/simplefactory");
        instance.setAttribute("SystemFileSystem.icon", u);
        
        
        Image img = ImageUtilities.loadImage("org/openide/loaders/saveAll.gif");
        
        DataObject.Factory f = DataLoaderPool.factory(instance);
        
        addRemove("text/plain", f, true);
        try {
            FileSystem lfs = createFS("folderQ/file.simple");
            FileObject fo = lfs.findResource("folderQ");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());
            
            FileObject edit = FileUtil.createData(root, "/Loaders/text/simplefactory/Actions/org-openide-actions-EditAction.instance");
            
            Node node = dob.getNodeDelegate();
            Action[] actions = node.getActions(true);
            assertEquals("One action is present: " + Arrays.asList(actions), 1, actions.length);
            assertEquals("It is the edit one", EditAction.class, actions[0].getClass());
            
            assertImage("Icon is propagated for open", img, node.getOpenedIcon(0));
            assertImage("Icon is propagated", img, node.getIcon(0));
            
            Reference<DataFolder> ref = new WeakReference<DataFolder>(df);
            df = null;
            assertGC("Folder can go away", ref);
            
            df = DataFolder.findFolder(fo);
            arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            assertEquals("Object is the same", dob, arr[0]);
        } finally {
            addRemove("text/plain", f, false);
        }
    }

    public void testSimpleLoader() throws Exception {
        FileSystem lfs = createFS("folder/file.simple");
        FileObject fo = lfs.findResource("folder/file.simple");
        assertNotNull(fo);
        assertFalse("No folder", fo.isFolder());
        assertTrue("Real data", fo.isData());
        DataObject first = DataObject.find(fo);
        LOG.log(Level.INFO, "default data object created: {0}", first);
        assertEquals("Realy default", DefaultDataObject.class, first.getClass());
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            DataObject dob = DataObject.find(fo);
            LOG.log(Level.INFO, "Object created: {0}", dob);
            assertEquals("Checking the right type", SimpleDataObject.class, dob.getClass());
            LOG.info("Check ok");
        } finally {
            LOG.warning("Check failed, removing loader");
            addRemoveLoader(l, false);
        }
    }

    public void testDataObjectFind() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            FileSystem lfs = createFS("folder/file.simple");
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            
            DataObject jdo = DataObject.find(fo);
            for (int i = 0; i < 5000; i++) {
                FileObject primary = jdo.getPrimaryFile();
                jdo.setValid(false);
                jdo = DataObject.find(primary);
                assertNotNull(jdo);
                assertTrue(jdo.isValid());
            }
            
        } finally {
            addRemoveLoader(l, false);
        }
    }

    public void testAntAsAntSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("text/xml", l3, true);
        try {
            FileSystem lfs = createFS(new String[] {
                "folder/file.ant",
            });
            FileObject fo = lfs.findResource("folder/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(l2, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("text/xml", l3, false);
        }
    }
    public void testAntWithoutAntSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        //DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        //addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("text/xml", l3, true);
        try {
            FileSystem lfs = createFS("folder2/file.ant");
            FileObject fo = lfs.findResource("folder2/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            MultiFileLoader xmlL = DataLoader.getLoader(XMLDataObject.Loader.class);
            assertEquals("No special handling for XML", xmlL, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        //addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("text/xml", l3, false);
        }
    }

    public void testAntAsUnknownSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        //DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        //addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("content/unknown", l3, true);
        try {
            FileSystem lfs = createFS("folder3/file.ant");
            FileObject fo = lfs.findResource("folder3/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(l3, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        //addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("content/unknown", l3, false);
        }
    }

    public void testManifestRegistrationsTakePreceedence() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        addRemoveLoader("text/ant+xml", l2, true);
        AddLoaderManuallyHid.addRemoveLoader(l3, true);
        try {
            FileSystem lfs = createFS("folder4/file.ant");
            FileObject fo = lfs.findResource("folder4/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals("Old registration of l3 takes preceedence", l3, dob.getLoader());
        } finally {
            addRemoveLoader(l1, false);
            addRemoveLoader("text/ant+xml", l2, false);
            AddLoaderManuallyHid.addRemoveLoader(l3, false);
        }
    }
    
    public static final class XMLUniFileLoader extends SimpleUniFileLoader {
        @Override
        protected void initialize() {
            getExtensions().addMimeType("text/xml");
            getExtensions().addMimeType("text/ant+xml");
        }
    }
    public static final class AntUniFileLoader extends SimpleUniFileLoader {
        @Override
        protected void initialize() {
            getExtensions().addMimeType("text/xml");
            getExtensions().addMimeType("text/ant+xml");
        }
    }
    public static class SimpleUniFileLoader extends UniFileLoader {
        public SimpleUniFileLoader() {
            super(SimpleDataObject.class.getName());
        }
        @Override
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("simple");
        }
        protected String displayName() {
            return "Simple";
        }
        @Override
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new SimpleDataObject(pf, this);
        }
    }
    public static final class SimpleFactory implements DataObject.Factory {
        @Override
        public DataObject findDataObject(FileObject fo, Set<? super FileObject> recognized) throws IOException {
            return SimpleUniFileLoader.findObject(SimpleUniFileLoader.class, true).findDataObject(fo, recognized);
        }
    }
    
    public static final class SimpleDataObject extends MultiDataObject {
        private ArrayList supp = new ArrayList ();
        
        public SimpleDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
            super(pf, loader);
        }
        
        /** Access method to modify cookies 
         * @return cookie set of this data object
         */
        public final org.openide.nodes.CookieSet cookieSet () {
            return getCookieSet ();
        }
        
        /** Getter for list of listeners attached to the data object.
         */
        public final Enumeration listeners () {
            return Collections.enumeration (supp);
        }
        
        @Override
        public void addPropertyChangeListener (PropertyChangeListener l) {
            super.addPropertyChangeListener (l);
            supp.add (l);
        }

        @Override
        public void removePropertyChangeListener (PropertyChangeListener l) {
            super.removePropertyChangeListener (l);
            supp.remove (l);
        }        
    }

    private static void assertImage(String msg, Image img1, Image img2) {
        ImageObserver obs = new ImageObserver() {
            @Override
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                fail("Already updated, hopefully");
                return true;
            }
        };
        
        int h, w;
        assertEquals("Width: " + msg, w = img1.getWidth(obs), img2.getWidth(obs));
        assertEquals("Height: " + msg, h = img1.getHeight(obs), img2.getHeight(obs));
        
        
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                //assertEquals("Pixel " + i + ", " + j + " same: " + msg, img1.get)
            }
        }
        
    }
}
