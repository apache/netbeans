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

package org.openide.filesystems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;

public class FileSystemTestHid extends TestBaseHid {
    private FileObject root;
    private static String[] resources = new String [] {
        "atleastone"
    };

    public FileSystemTestHid(String testName) {
        super(testName);
    }
    private static int abacus = 0;


    protected String[] getResources (String testName) {
        return resources;
    }
    public void testAddFileStatusListener () {
        if (!(this.testedFS instanceof TestUtilHid.StatusFileSystem))
            return;
        FileStatusListener[]  fsListeners =  new FileStatusListener [10];
        abacus = 0;

        for (int i = 0; i < fsListeners.length; i++) {
            fsListeners[i] = createFileStatusListener ();
            this.testedFS.addFileStatusListener(fsListeners[i]);
        }
        this.testedFS.fireFileStatusChanged(new FileStatusEvent (this.testedFS, true, true) );
        fsAssert("failure: not all FileStstausListeners invoked: " + abacus,  abacus == fsListeners.length);

        abacus = 0;        
        this.testedFS.removeFileStatusListener(fsListeners[0]);
        this.testedFS.fireFileStatusChanged(new FileStatusEvent (this.testedFS, true, true) );        
        fsAssert("failure: not all FileStstausListeners invoked",  abacus == (fsListeners.length -1));
    }

    
    public void testAddVetoableChangeListener () {
        VetoableChangeListener[]  vListeners =  new VetoableChangeListener[10];
        abacus = 0;

        for (int i = 0; i < vListeners.length; i++) {
            vListeners[i] = createVetoableChangeListener ();
            this.testedFS.addVetoableChangeListener(vListeners[i]);            
        }
        try {
            this.testedFS.fireVetoableChange("test", "old", "new");
        } catch (PropertyVetoException pex) {
            fsFail("unexpected veto exception");
        }
        fsAssert("failure: not all VetoableChangeListeners invoked",  abacus == vListeners.length);
        
        abacus = 0;        
        this.testedFS.removeVetoableChangeListener(vListeners[0]);
        try {
            this.testedFS.fireVetoableChange("test", "old", "new");        
        } catch (PropertyVetoException pex) {
            fsFail("unexpected veto exception");            
        }            
        fsAssert("failure: not all VetoableChangeListeners invoked",  abacus == (vListeners.length -1));
    }
    
    private FileStatusListener createFileStatusListener () {
        return new FileStatusListener () {
            public void annotationChanged (FileStatusEvent ev) {
                abacus++;
            }
        };

    }

    private VetoableChangeListener createVetoableChangeListener () {
        return new VetoableChangeListener  () {
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                    abacus++;            
            }
        };

    }
    
    public void testFsfileFolderCreated() throws IOException {
        FileSystem fs = this.testedFS;
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (fs);            
            root.createFolder("testtset");
            fileFolderCreatedAssert ("unexpecetd event count",1);
        }
    }
    
    public void testFsfileDataCreated() throws IOException {
        FileSystem fs = this.testedFS;
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (fs);            
            FileObject newF = root.createData("testfile","txe");
            fileDataCreatedAssert ("unexpecetd event count",1);            
        }
        
    }

    public void testFsfileRenamed() throws IOException {
        FileSystem fs = this.testedFS;
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (fs);            
            FileObject newF = root.createData("testfile","txe");
            FileLock fLock = newF.lock();            
            try {
                newF.rename(fLock,"obscure","uni");                               
            } finally {
                fLock.releaseLock();               
            }

            fileRenamedAssert("unexpecetd event count",1);                                    
        }
        
    }

    public void testFsfileDeleted() throws IOException {
        FileSystem fs = this.testedFS;
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (fs);            
            FileObject newF = root.createData("testfile","txe");
            FileLock fLock = newF.lock();            
            try {
                newF.delete(fLock);                               
            } finally {
                fLock.releaseLock();               
            }

            fileDeletedAssert("unexpecetd event count",1);                                    
        }
        
    }
    
    /** Test of isValid method, of class org.openide.filesystems.FileSystem. */
    public void testIsValid() {        
        Repository r = new Repository(new LocalFileSystem ());
        // 
        fsAssert("file system, which is not assigned to the repository, should be invalid", 
        !testedFS.isValid());
        
        // assign to empty repository -> become valid
        r.addFileSystem(testedFS);
        if (!testedFS.getSystemName().equals(""))
            fsAssert("assign to empty repository -> become valid" , testedFS.isValid());
        
        // remove from repo -> become invalid
        r.removeFileSystem(testedFS);
        fsAssert("remove from repo -> become invalid", !testedFS.isValid());
    }

    protected void setUp() throws Exception {
        super.setUp();
        root = testedFS.findResource(getResourcePrefix());
    }
    
}
