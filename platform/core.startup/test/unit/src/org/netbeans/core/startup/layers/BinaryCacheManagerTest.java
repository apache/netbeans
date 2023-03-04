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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/** Test layer cache manager.
 * @author Jesse Glick
 * @see "#20628"
 */
public class BinaryCacheManagerTest extends CacheManagerTestBaseHid 
implements CacheManagerTestBaseHid.ManagerFactory {
    
    public BinaryCacheManagerTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        
        clearWorkDir();
        
        System.setProperty("netbeans.user", getWorkDirPath());
        
        // Suppress "Inefficient to include an empty layer" and "use of inline CDATA text contents" warnings:
        LayerCacheManager.err.setLevel(Level.SEVERE);
    }

    //
    // Manager factory methods
    //
    @Override
    public LayerCacheManager createManager() throws Exception {
        return new BinaryCacheManager();
    }

    @Override
    public boolean supportsTimestamps() {
        // returns times stamps equals to Stamps value
        return false;
    }
    
    static FileSystem store(LayerCacheManager m, List<URL> urls) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        FileSystem prev = m.createEmptyFileSystem();
        m.store(prev, urls, os);
        return m.load(prev, ByteBuffer.wrap(os.toByteArray()).order(ByteOrder.LITTLE_ENDIAN));
    }
    
    //
    // new test methods
    //
    
    /** Test issue 140061 - need to update ParsingLayerCacheManager when increasing version of DTD Filesystem.*/
    public void testDTD1_2() throws SAXException, IOException {
        BinaryCacheManager m = new BinaryCacheManager();
        List<URL> urls = new ArrayList<URL>(Arrays.asList(loadResource("data/layer1.2.xml")));
        try {
            store(m, urls);
        } catch(Exception e) {
            e.printStackTrace();
            fail("DTD Filesystem 1.2 not resolved");
        }
        String pubid = "-//NetBeans//DTD Filesystem 1.2//EN";
        String sysid = "http://www.netbeans.org/dtds/filesystem-1_2.dtd";
        InputSource is = m.resolveEntity(pubid, sysid);
        assertNotNull("DTD Filesystem 1.2 not resolved.", is);
    }
    public void testJustAttributes() throws SAXException, IOException {
        BinaryCacheManager m = new BinaryCacheManager();
        List<URL> urls = new ArrayList<URL>(Arrays.asList(loadResource("data/attribsonly.xml")));
        FileSystem fs = store(m, urls);
        assertEquals(Boolean.TRUE, fs.getRoot().getAttribute("myAttr"));
    }
    
    public void testFastReplacement() throws Exception {
        clearWorkDir();
        LayerCacheManager m = new BinaryCacheManager();
        // layer2.xml should override layer1.xml where necessary:
        List<URL> urls = new ArrayList<URL>(Arrays.asList(
            loadResource("data/layer2.xml"),
            loadResource("data/layer1.xml")));
        
        FileSystem f = store(m, urls);
        FileSystem base = FileUtil.createMemoryFileSystem();
        FileUtil.createData(base.getRoot(), "baz/thongy");
        final MFS mfs = new MFS(new FileSystem[] {base, f});
        FileObject baz = mfs.findResource("baz");
        assertNotNull(baz);
        assertEquals(2, baz.getChildren().length);
        FileObject thingy = mfs.findResource("baz/thingy");
        assertNotNull(thingy);
        L l = new L();
        baz.addFileChangeListener(l);
        //L l2 = new L();mfs.addFileChangeListener(l2);
        urls.remove(0);
        f = store(m, urls);
        final FileSystem[] fss = {base, f};
        mfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() {
                mfs._setDelegates(fss);
            }
        });
        assertEquals(2, baz.getChildren().length);
        assertTrue(thingy.isValid());
        assertEquals(0, l.ac);
        assertEquals(0, l.c);
        assertEquals(0, l.dc);
        assertEquals(0, l.d);
        assertEquals(0, l.fc);
        assertEquals(0, l.r);
        urls.remove(0);
        f = store(m, urls);
        final FileSystem[] fss2 = new FileSystem[] {base, f};
        mfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() {
                mfs._setDelegates(fss2);
            }
        });
        assertEquals(1, baz.getChildren().length);
        assertFalse(thingy.isValid());
        assertEquals(0, l.ac);
        assertEquals(0, l.c);
        assertEquals(0, l.dc);
        assertEquals(1, l.d);
        assertEquals(0, l.fc);
        assertEquals(0, l.r);
    }
    
    // Make setDelegates public:
    private static final class MFS extends MultiFileSystem {
        public MFS(FileSystem[] fss) {
            super(fss);
        }
        public void _setDelegates(FileSystem[] fss) {
            setDelegates(fss);
        }
    }
        
    private static final class L implements FileChangeListener {
        public int ac = 0, c = 0, dc = 0, d = 0, fc = 0, r = 0;
        public void fileAttributeChanged(FileAttributeEvent fe) {
//            System.err.println("ac: " + fe.getFile().getPath());
            ac++;
        }
        public void fileChanged(FileEvent fe) {
//            System.err.println("c: " + fe.getFile().getPath());
            c++;
        }
        public void fileDataCreated(FileEvent fe) {
//            System.err.println("dc: " + fe.getFile().getPath());
            dc++;
        }
        public void fileDeleted(FileEvent fe) {
//            System.err.println("d: " + fe.getFile().getPath());
            d++;
        }
        public void fileFolderCreated(FileEvent fe) {
//            System.err.println("fc: " + fe.getFile().getPath());
            fc++;
        }
        public void fileRenamed(FileRenameEvent fe) {
//            System.err.println("r: " + fe.getFile().getPath());
            r++;
        }
    }
    
}
