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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import org.openide.filesystems.*;
import java.beans.*;
import java.lang.ref.WeakReference;
import org.netbeans.junit.*;

/** Test the complexity of deleting N files in a folder without recognized
 * DataOjects (which may as well be tested on deleting secondary files
 * of a recognized DataObject).
 *
 * The test may need slight tweaking of the timing constants for reliability.
 * 
 * @author  Petr Nejedly
 */
public class DataFolderSlowDeletionTest extends LoggingTestCaseHid {
    private FileObject folder;
    // just holders
    private FileObject[] children;
    private DataFolder df;
    private DataObject do0;
    

    /** Creates new DataFolderSlowDeletionTest */
    public DataFolderSlowDeletionTest (String name) {
        super (name);
    }
    
    /**
     * @return a  speedSuite configured as to allow 2x linear slowdown between
     * 10-fold increase of the paratemer
     */
    public static NbTestSuite suite () {
        return NbTestSuite.linearSpeedSuite(DataFolderSlowDeletionTest.class, 2, 5);
    }
    
    /**
     * Prepares a filesystem with a prepopulated folder of N files, where N
     * is extracted from the test name.
     * @throws java.lang.Exception 
     */
    protected void setUp() throws Exception {
        clearWorkDir();
        TestUtilHid.destroyLocalFileSystem(getName());
        
        int count = getTestNumber ();
        String[] resources = new String[count];       
        for (int i=0; i<resources.length; i++) resources[i] = "folder/file" + i + ".txt";
        FileSystem fs = TestUtilHid.createLocalFileSystem(getWorkDir(), resources);
        folder = fs.findResource("folder");
        
        // convert to masterfs
        folder = FileUtil.toFileObject(FileUtil.toFile(folder));
        
        
        children = folder.getChildren();
        df = DataFolder.findFolder (folder);
        do0 = DataObject.find(children[0]);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        WeakReference<?> ref = new WeakReference<Object>(folder);
        folder = null;
        children = null;
        df = null;
        do0 = null;
        assertGC("Cleanup before next test", ref);
    }
    
    
    /**
     * 
     * @throws java.lang.Exception 
     */
    private void performSlowDeletionTest () throws Exception {
        folder.delete();
    }
    
    /**
     * Preheat the infrastructure so the lower end is measured already JITed
     * @throws java.lang.Exception 
     */
    public void testSlowDeletionPrime1000() throws Exception {
        performSlowDeletionTest();
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    public void testSlowDeletion1000() throws Exception {
        performSlowDeletionTest();
    }

    public void testSlowDeletion3000() throws Exception {
        performSlowDeletionTest();
    }
    
}
