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

import org.openide.cookies.OpenCookie;

import org.openide.filesystems.*;
import org.netbeans.junit.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import org.openide.*;
import org.openide.util.Enumerations;

/**
 * @author Jaroslav Tulach
 */
public class MultiDataObjectTest extends NbTestCase {
    FileSystem fs;
    DataObject one;
    DataFolder from;
    DataFolder to;
    ErrorManager err;
    
    
    /** Creates new DataObjectTest */
    public MultiDataObjectTest (String name) {
        super (name);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected int timeOut() {
        return 45000;
    }

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        
        super.setUp();
        
        MockServices.setServices(Pool.class);
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        fs = lfs;
        FileUtil.createData(fs.getRoot(), "from/x.prima");
        FileUtil.createData(fs.getRoot(), "from/x.seconda");
        FileUtil.createFolder(fs.getRoot(), "to/");
        
        one = DataObject.find(fs.findResource("from/x.prima"));
        assertEquals(SimpleObject.class, one.getClass());
        
        from = one.getFolder();
        to = DataFolder.findFolder(fs.findResource("to/"));
        
        assertEquals("Nothing there", 0, to.getPrimaryFile().getChildren().length);
    }
    
    public void testTheSetOfSecondaryEntriesIsSaidToGetInconsistent() throws Exception {
        for (int i = 0; i < 10; i++) {
            err.log(i + " getting children of to");
            DataObject[] to1 = to.getChildren();
            err.log(i + " getting children of from");
            DataObject[] from1 = from.getChildren();
            err.log(i + " getting files of object1");
            Object[] arr1 = one.files().toArray();
            err.log(i + " moving the object");
            one.move(to);
            err.log(i + " 2nd children of to");
            DataObject[] to2 = to.getChildren();
            err.log(i + " 2nd children of from");
            DataObject[] from2 = from.getChildren();
            err.log(i + " 2nd  files of object1");
            Object[] arr2 = one.files().toArray();
            err.log(i + " checking results");
            
            assertEquals("Round " + i + " To is empty: " + Arrays.asList(to1), 0, to1.length);
            assertEquals("Round " + i + " From has one:" + Arrays.asList(from1), 1, from1.length);
            assertEquals("Round " + i + " One has two files" + Arrays.asList(arr1), 2, arr1.length);
            
            assertEquals("Round " + i + " From is empty after move: " + Arrays.asList(from2), 0, from2.length);
            assertEquals("Round " + i + " To has one:" + Arrays.asList(to2), 1, to2.length);
            assertEquals("Round " + i + " One still has two files" + Arrays.asList(arr1), 2, arr1.length);
            
            err.log(i + " moving back");
            one.move(from);
            err.log(i + " end of cycle");
        }
    }

    public void testConsistencyWithABitOfAsynchronicity() throws Exception {
        err.log(" getting children of to");
        DataObject[] to1 = to.getChildren();
        err.log(" getting children of from");
        DataObject[] from1 = from.getChildren();
        
        
        for (int i = 0; i < 10; i++) {
            err.log(i + " getting files of object1");
            Object[] arr1 = one.files().toArray();
            err.log(i + " moving the object");
            one.move(to);
            Object[] arr2 = one.files().toArray();
            err.log(i + " checking results");
            
            assertEquals("Round " + i + " One has two files" + Arrays.asList(arr1), 2, arr1.length);
            
            assertEquals("Round " + i + " One still has two files" + Arrays.asList(arr1), 2, arr1.length);
            
            err.log(i + " moving back");
            one.move(from);
            err.log(i + " end of cycle");
        }
    }

    public void testConsistencyWithABitOfAsynchronicityAndNoObservationsThatWouldMangeTheState() throws Exception {
        err.log(" getting children of to");
        DataObject[] to1 = to.getChildren();
        err.log(" getting children of from");
        DataObject[] from1 = from.getChildren();
        
        
        for (int i = 0; i < 10; i++) {
            err.log(i + " moving the object");
            one.move(to);
            err.log(i + " moving back");
            one.move(from);
            err.log(i + " end of cycle");
        }
    }
    

    public void testAdditionsToCookieSetAreVisibleInLookup() throws Exception {
        assertTrue(this.one instanceof SimpleObject);
        SimpleObject s = (SimpleObject)this.one;
        
        class Open implements OpenCookie {
            public void open() {
            }
        }
        Open openCookie = new Open();
        
        
        s.getCookieSet().add(openCookie);
        
        assertSame("Cookie is in the lookup", openCookie, one.getLookup().lookup(OpenCookie.class));
    }

    public void testRenameTemplate() throws IOException {
        FileObject template = FileUtil.createData(fs.getRoot(), "template.prima");
        FileUtil.createData(fs.getRoot(), "template.seconda");
        template.setAttribute("template", Boolean.TRUE);
        template.setAttribute("templateWizardURL", "testURL");
        template.setAttribute("templateCategory", "testCategory");
        template.setAttribute("instantiatingIterator", "testIterator");
        DataObject templateData = DataObject.find(template);
        templateData.rename("templateNewName.tpl");
        assertEquals("testURL", templateData.getPrimaryFile().getAttribute(
                "templateWizardURL"));
        assertEquals("testCategory", templateData.getPrimaryFile().getAttribute(
                "templateCategory"));
        assertEquals("testIterator", templateData.getPrimaryFile().getAttribute(
                "instantiatingIterator"));
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration loaders() {
            return Enumerations.singleton(SimpleLoader.getLoader(SimpleLoader.class));
        }
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class);
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder()) {
                // emulate the behaviour of form data object
                
                /* emulate!? this one is written too well ;-)
                FileObject primary = FileUtil.findBrother(fo, "prima");
                FileObject secondary = FileUtil.findBrother(fo, "seconda");
                
                if (primary == null || secondary == null) {
                    return null;
                }
                
                if (primary != fo && secondary != fo) {
                    return null;
                }
                 */
                
                // here is the common code for the worse behaviour
                if (fo.hasExt("prima")) {
                    return FileUtil.findBrother(fo, "seconda") != null ? fo : null;
                }
                
                if (fo.hasExt("seconda")) {
                    return FileUtil.findBrother(fo, "prima");
                }
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            assertFalse(
                "Don't call registerEntry under lock", Thread.holdsLock(DataObjectPool.getPOOL())
            );
            return new FileEntry(obj, secondaryFile);
        }

        private void afterMove(FileObject f, FileObject retValue) {
            firePropertyChange("afterMove", null, null);
        }
    }
    
    private static final class FE extends FileEntry {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }

        @Override
        public FileObject move(FileObject f, String suffix) throws IOException {
            FileObject retValue;
            retValue = super.move(f, suffix);
            
            SimpleLoader l = (SimpleLoader)getDataObject().getLoader();
            l.afterMove(f, retValue);
            
            return retValue;
        }
        
        
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
    }

}
