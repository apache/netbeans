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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.test.MockLookup;

public class FileObjectInLookupTest extends NbTestCase {
    FileObject root;
    
    public FileObjectInLookupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(new OwnDataLoaderPool());
        clearWorkDir ();
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), new String[] {
            "adir/",
            "adir/file.txt",
            "adir/file.own"
        });
        
        root = FileUtil.toFileObject(FileUtil.toFile(lfs.getRoot()));
        
        Enumeration<?> en = DataLoaderPool.getDefault().allLoaders();
        while (en.hasMoreElements()) {
            if (en.nextElement() instanceof OwnDataLoader) {
                return;
            }
        }
        fail("OwnDataLoader shall be registered");
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testFOInsideFolder() throws Exception {
        DataFolder f = DataFolder.findFolder(root.getFileObject("adir"));
        assertFileObjects(f);
        f.rename("bdir");
        assertFileObjects(f);
    }
    
    public void testFOInsideADefaultDataObject() throws Exception {
        DataObject obj = DataObject.find(root.getFileObject("adir/file.txt"));
        assertFileObjects(obj);
        obj.rename("kuk");
        assertFileObjects(obj);
        obj.move(obj.getFolder().getFolder());
        assertFileObjects(obj);
    }

    public void testOwnLoader() throws Exception {
        DataObject obj = DataObject.find(root.getFileObject("adir/file.own"));
        assertEquals(OwnDataLoader.class, obj.getLoader().getClass());
        assertFileObjects(obj);
        obj.rename("kuk");
        assertFileObjects(obj);
        obj.move(obj.getFolder().getFolder());
        assertFileObjects(obj);
    }

    @RandomlyFails
    public void testShadow() throws Exception {
        DataObject obj = DataObject.find(root.getFileObject("adir/file.own"));
        DataShadow shadow = obj.createShadow(obj.getFolder().getFolder());
        assertEquals(OwnDataLoader.class, obj.getLoader().getClass());
        
        assertEquals("DataObject for the shadow is the shadow", shadow, shadow.getCookie(DataObject.class));
        
        assertFileObjects(obj);
        assertFileObjects("However FileObject of a shadow are delegated to the original", shadow, obj.files());
        obj.rename("kuk");
        assertFileObjects(obj);
        assertFileObjects("However FileObject of a shadow are delegated to the original", shadow, obj.files());
        obj.move(obj.getFolder().getFolder());
        assertFileObjects(obj);
        assertFileObjects("However FileObject of a shadow are delegated to the original", shadow, obj.files());
        shadow.rename("somenewshadow");
        assertFileObjects(obj);
        assertFileObjects("However FileObject of a shadow are delegated to the original", shadow, obj.files());
        obj.delete();
        /*
        DataObject broken = DataObject.find(shadow.getPrimaryFile());
        if (shadow == broken) {
            fail("They should be different: " + shadow + " != " + broken);
        }
        assertEquals("DataObject for the shadow is now the shadow", broken, broken.getCookie(DataObject.class));
        assertFileObjects(broken);
         */
    }
    
    private static void assertFileObjects(DataObject obj) {
        assertFileObjects("", obj, obj.files());
    }
    
    private static void assertFileObjects(String msg, DataObject obj, Collection<? extends FileObject> expect) {
        Collection<? extends FileObject> allcol = obj.getNodeDelegate().getLookup().lookupAll(FileObject.class);
        List<FileObject> all = new ArrayList<FileObject>(allcol);
        Enumeration<? extends FileObject> files = Collections.enumeration(expect);
        int i = 0;
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            if (i >= all.size()) {
                fail(msg + "\nThere should be more elements, but there is only " + all.size() + "\nAll: " + all + "\nCurrent: " + fo);
            }
            
            if (fo.equals(all.get(i))) {
                i++;
                continue;
            }
            fail(msg + "\nError at position " + i + " expected: " + fo 
                    + "@" + Integer.toHexString(fo.hashCode()) + 
                    " but was: " + all.get(i) + "@" + Integer.toHexString(all.get(i).hashCode()) 
                    + " \nAll: " + all
            );
        }
    }
    
    private static final class OwnDataLoaderPool extends DataLoaderPool {
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(OwnDataLoader.getLoader(OwnDataLoader.class));
        }
    }


    private static class OwnDataLoader extends UniFileLoader {
        private static final long serialVersionUID = 1L;

        public OwnDataLoader() {
            super("org.openide.loaders.FileObjectInLookupTest$OwnDataObject");
        }

        @Override
        protected String defaultDisplayName() {
            return NbBundle.getMessage(OwnDataLoader.class, "LBL_Own_loader_name");
        }

        @Override
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("own");
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new OwnDataObject(primaryFile, this);
        }
    }
    private static class OwnDataObject extends MultiDataObject implements Lookup.Provider {

        public OwnDataObject(FileObject pf, OwnDataLoader loader) throws DataObjectExistsException, IOException {
            super(pf, loader);
            CookieSet cookies = getCookieSet();
            cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        }

        @Override
        protected Node createNodeDelegate() {
            return new OwnDataNode(this, getLookup());
        }

        @Override
        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }
    }
    
    static class OwnDataNode extends DataNode {
        private OwnDataNode(OwnDataObject obj, Lookup lookup) {
            super(obj, Children.LEAF, lookup);
        }

    }

}
