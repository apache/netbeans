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
import java.util.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.netbeans.junit.*;
import org.openide.filesystems.*;

/** Delete of an folder is said to be slow due to
 * poor implementation of DataShadow validate functionality.
 * @author Jaroslav Tulach
 */
public class DataShadowSlowness39981Test extends NbTestCase implements OperationListener {
    /** List of DataObject */
    private List shadows, brokenShadows;
    /** folder to work with */
    private DataFolder folder;
    /** fs we work on */
    private FileSystem lfs;
    /** start time of the test */
    private long time;
    /** number of created objects */
    private int createdObjects;
    /** created children */
    private DataObject[] arr;
    
    public DataShadowSlowness39981Test (String name) {
        super(name);
    }
    
    public static NbTestSuite suite () {
        return NbTestSuite.speedSuite (DataShadowSlowness39981Test.class, 20, 5);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "shadows/",
            "brokenshadows",
            "folder/original.txt",
            "folder/orig.txt",
            "modify/"
        });
        FileObject root = FileUtil.toFileObject(FileUtil.toFile(lfs.getRoot()));
        
        int count = getTestNumber ();
        
        shadows = createShadows (
            DataObject.find (root.getFileObject("folder/original.txt")), 
            DataFolder.findFolder (root.getFileObject("shadows")),
            count
        );
        
        brokenShadows = /*Collections.EMPTY_LIST; */createShadows (
            DataObject.find (root.getFileObject("folder/orig.txt")), 
            DataFolder.findFolder (root.getFileObject("shadows")),
            count
        );
        
        DataObject.find (root.getFileObject("folder/orig.txt")).delete ();
        
        ListIterator it = brokenShadows.listIterator ();
        while (it.hasNext ()) {
            DataObject obj = (DataObject)it.next ();
            assertFalse ("Is not valid", obj.isValid ());
            assertTrue ("Used to be shadow", obj instanceof DataShadow);
            DataObject newObj = DataObject.find (obj.getPrimaryFile ());
            assertTrue ("They are different", newObj != obj);
            assertFalse ("It is not shadow, as it is broken", newObj instanceof DataShadow);
            
            it.set (newObj);
        }
        
        FileObject files = root.getFileObject("modify");
        for (int i = 0; i < 200; i++) {
            FileUtil.createData (files, "empty" + i + ".txt");
        }
        
        assertEquals ("Children created", 200, files.getChildren ().length);
        
        folder = DataFolder.findFolder (files);
        time = System.currentTimeMillis ();
    }
    
    private static List createShadows (DataObject original, DataFolder target, int count) throws java.io.IOException {
        ArrayList list = new ArrayList (count);
        for (int i = 0; i < count; i++) {
            DataShadow shad = DataShadow.create(target, original.getName()+i, original, "shadow");
            list.add (shad);
        }
        return list;
    }
    
    protected void tearDown() throws Exception {
        ArrayList weaks = new ArrayList();
        addWeakRefs(Arrays.asList(arr), weaks);
        addWeakRefs(brokenShadows, weaks);
        addWeakRefs(shadows, weaks);
        addWeakRefs(Collections.singleton(lfs), weaks);
        addWeakRefs(Collections.singleton(folder), weaks);
        
        arr = null;
        brokenShadows = null;
        shadows = null;
        lfs = null;
        folder = null;
        
        
        WeakReference[] refArr = (WeakReference[])weaks.toArray(new WeakReference[0]);
        for (int i = 0; i < refArr.length; i++) {
            assertGC(i + " - gc(" + refArr[i].get() + "): ", refArr[i]);
        }
        
        
    }
    
    private static void addWeakRefs(Collection objects, List weakRefsToAdd) {
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            weakRefsToAdd.add(new WeakReference(obj));
        }
    }
    
    private void createChildren () {
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        pool.addOperationListener (this);
        
        arr = folder.getChildren ();
        
        pool.removeOperationListener (this);
        
        if (arr.length > createdObjects) {
            fail ("The children of the folder should not be created before the getChildren method is called. Children: " + arr.length + " created: " + createdObjects);
        }
    }
    
    public void test0 () throws Exception {
        createChildren ();
    }
    
    public void test10 () throws java.io.IOException {
        createChildren ();
    }
    
    public void test99 () throws java.io.IOException {
        createChildren ();
    }

    public void test245 () throws java.io.IOException {
        createChildren ();
    }
    
    public void test552 () throws java.io.IOException {
        createChildren ();
    }
    
//    public void test987 () throws Exception {
//        createChildren ();
//    }
    
    
    public void operationCopy (org.openide.loaders.OperationEvent.Copy ev) {
    }
    
    public void operationCreateFromTemplate (org.openide.loaders.OperationEvent.Copy ev) {
    }
    
    public void operationCreateShadow (org.openide.loaders.OperationEvent.Copy ev) {
    }
    
    public void operationDelete (OperationEvent ev) {
    }
    
    public void operationMove (org.openide.loaders.OperationEvent.Move ev) {
    }
    
    public void operationPostCreate (OperationEvent ev) {
        createdObjects++;
    }
    
    public void operationRename (org.openide.loaders.OperationEvent.Rename ev) {
    }
    
}
