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
