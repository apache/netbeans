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

package org.openide.loaders;

import java.lang.ref.WeakReference;
import javax.swing.event.ChangeEvent;
import junit.textui.TestRunner;

import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.util.Lookup;

/** Test basic functionality of data loader pool.
 * @author Vita Stejskal
 */
public class DataLoaderPoolTest extends NbTestCase {
    private FileSystem lfs;
    private DataLoader loaderA;
    private DataLoader loaderB;
    private Pool pool;

    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.loaders.DataLoaderPoolTest$Lkp"); // NOI18N
    }
    
    public DataLoaderPoolTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        DataLoaderPool p = DataLoaderPool.getDefault ();
        assertNotNull (p);
        assertEquals (Pool.class, p.getClass ());
        pool = (Pool)p;
        pool.clear(false);
        
        loaderA = DataLoader.getLoader(SimpleAUniFileLoader.class);
        loaderB = DataLoader.getLoader(SimpleBUniFileLoader.class);

        clearWorkDir();
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "folder/file.simple",
        });
        
    }
    
    protected void tearDown() throws Exception {
        WeakReference ref = new WeakReference(lfs);
        lfs = null;
        assertGC("File system can disappear", ref);
    }
    
    /** Method for subclasses (DataLoaderPoolOnlyEventsTest) to do the 
     * association of preferred loader in different way
     */
    protected void doSetPreferredLoader (FileObject fo, DataLoader loader) throws IOException {
        DataLoaderPool.setPreferredLoader (fo, loader);
    }
    
    /** DataObject should be invalidated after the setPrefferedLoader call sets
     * different loader than used to load the DO.
     */
    public void testSetPrefferedloader () throws Exception {
        assertTrue(Arrays.asList(pool.toArray()).contains(loaderA));
        assertTrue(Arrays.asList(pool.toArray()).contains(loaderB));

        FileObject fo = lfs.findResource("folder/file.simple");
        assertNotNull(fo);

        doSetPreferredLoader (fo, loaderA);
        DataObject doa = DataObject.find (fo);
        assertSame (loaderA, doa.getLoader ());

        doSetPreferredLoader (fo, loaderB);
        DataObject dob = DataObject.find (fo);
        assertTrue ("DataObject wasn't refreshed after the prefered loader has been changed.", dob != doa);
        assertSame (loaderB, dob.getLoader ());
    }

    /** When preferredLoader is set to null, the object should be invalidated
     * and correctly recognized.
     */
    public void testClearPrefferedloader() throws Exception {
        int indxA = Arrays.asList(pool.toArray()).indexOf(loaderA);
        int indxB = Arrays.asList(pool.toArray()).indexOf(loaderB);

        assertTrue ("It is there", indxA != -1);
        assertTrue ("It is there", indxB != -1);
        assertTrue (indxB + " is before " + indxA, indxB < indxA);

        FileObject fo = lfs.findResource("folder/file.simple");
        assertNotNull(fo);

        DataObject initial = DataObject.find(fo);
        assertSame("Loader B is now before loaderA", loaderB, initial.getLoader ());

        doSetPreferredLoader(fo, loaderA);
        DataObject doa = DataObject.find(fo);
        assertTrue("DataObject should refresh itself", initial != doa);
        assertSame("Loader A took over the object", loaderA, doa.getLoader());

        doSetPreferredLoader(fo, null);
        DataObject dob = DataObject.find(fo);
        assertTrue("DataObject should refresh itself", dob != doa);
        assertSame("Againg loader B takes over", loaderB, dob.getLoader());
    }

    public void testChangeIsAlsoReflectedInNodes () throws Exception {
        FileObject fo = lfs.findResource("folder");
        assertNotNull(fo);

        DataFolder folder = DataFolder.findFolder(fo);

        org.openide.nodes.Node n = folder.getNodeDelegate();
        org.openide.nodes.Node[] arr = n.getChildren().getNodes (true);

        assertEquals ("One child is there", 1, arr.length);
        DataObject initial = (DataObject)arr[0].getCookie (DataObject.class);
        assertNotNull ("DataObject is cookie of the node", initial);
        assertSame("Loader B is now before loaderA", loaderB, initial.getLoader());
    
        fo = lfs.findResource("folder/file.simple");
        assertNotNull(fo);
        doSetPreferredLoader(fo, loaderA);

        assertSame ("Changes is reflected on data  object level", loaderA, DataObject.find (fo).getLoader ());
        DataObject[] children = folder.getChildren();
        assertEquals ("There is one", 1, children.length);
        assertSame ("Change is reflected on DataFolder level", loaderA, children[0].getLoader ());
        
        arr = n.getChildren().getNodes(true);
        assertEquals("One child is there", 1, arr.length);
        DataObject newOne = (DataObject)arr[0].getCookie(DataObject.class);
        assertNotNull("DataObject is cookie of the node", newOne);
        assertSame("There has been a change", loaderA, newOne.getLoader());
    }
    
    public void testHowManyTimesWeCallDLPloaders() throws Exception {
        FileObject fo = lfs.findResource("folder/file.simple");
        assertNotNull(fo);

        FileObject f1 = FileUtil.createData(fo.getParent(), "f1.simple");
        FileObject f2 = FileUtil.createData(fo.getParent(), "f2.simple");
        FileObject f3 = FileUtil.createData(fo.getParent(), "f3.simple");
        
        
        FileObject[] all = fo.getParent().getChildren();
        assertEquals("No calls to pool yet", 0, pool.cnt);
        pool.clear(true);
        
        for (int i = 0; i < all.length; i++) {
            DataObject o = DataObject.find(all[i]);
            assertEquals("Only one call even for " + all[i], 1, pool.cnt);
            assertEquals("loaderB is first for " + all[i], loaderB, o.getLoader());
        }
        
        pool.loaders.remove(loaderB);
        pool.fireChangeEvent(new ChangeEvent(pool));
        
        for (int i = 0; i < all.length; i++) {
            DataObject o = DataObject.find(all[i]);
            assertEquals("One more call - " + all[i], 2, pool.cnt);
            assertEquals("loaderA is the only one " + all[i], loaderA, o.getLoader());
        }
        
    }
    
    public static final class SimpleAUniFileLoader extends UniFileLoader {
        public SimpleAUniFileLoader() {
            super(SimpleDataObject.class.getName());
        }
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("simple");
        }
        protected String displayName() {
            return "SimpleA";
        }
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new SimpleDataObject(pf, this);
        }
    }
    public static final class SimpleBUniFileLoader extends UniFileLoader {
        public SimpleBUniFileLoader() {
            super(SimpleDataObject.class.getName());
        }
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("simple");
        }
        protected String displayName() {
            return "SimpleB";
        }
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new SimpleDataObject(pf, this);
        }
    }
    public static final class SimpleDataObject extends MultiDataObject {
        public SimpleDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
            super(pf, loader);
        }
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new Pool ());
            ic.add (new NbMutexEventProvider());
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        List loaders;
        int cnt;
        
        public Pool () {
        }
        
        public void clear(boolean ass) {
            loaders = null;
            cnt = 0;
            fireChangeEvent(new ChangeEvent(this));
            if (ass) {
                assertEquals("No call to loaders", 0, cnt);
            }
            cnt = 0;
        }

        public java.util.Enumeration loaders () {
            cnt++;
            
            if (loaders == null) {
                loaders = new ArrayList ();
                DataLoader loaderA = DataLoader.getLoader(SimpleAUniFileLoader.class);
                DataLoader loaderB = DataLoader.getLoader(SimpleBUniFileLoader.class);
                loaders.add (loaderB);
                loaders.add (loaderA);
            }
            return Collections.enumeration (loaders);
        }
    }
}
