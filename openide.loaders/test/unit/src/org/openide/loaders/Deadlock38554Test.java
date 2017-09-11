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

import junit.textui.TestRunner;

import org.openide.filesystems.*;
import org.openide.util.Lookup;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import java.beans.PropertyChangeListener;

/** Simulates the deadlock from issue 38554.
 * @author Jaroslav Tulach
 */
public class Deadlock38554Test extends NbTestCase implements FileChangeListener {
    private FileSystem lfs;
    private DataObject src;
    private DataFolder tgz;
    private Uni loader;

    public Deadlock38554Test (String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[] {
            "folder/",
            "folder/f.uni",
            "target/"
        });
        
        loader = (Uni)Uni.getLoader (Uni.class);
        AddLoaderManuallyHid.addRemoveLoader (loader, true);
        
        src = DataObject.find (lfs.findResource("folder/f.uni"));
        tgz = DataFolder.findFolder(lfs.findResource("target"));
        lfs.addFileChangeListener (this);
    }
    
    protected void tearDown() throws Exception {
        lfs.removeFileChangeListener (this);
        TestUtilHid.destroyLocalFileSystem(getName());
        AddLoaderManuallyHid.addRemoveLoader (loader, false);
    }
    
    public void testEvilSynchronziedFileChangeListenerWhenCopy () throws Exception {
        src.copy (tgz);
    }
    public void testEvilSynchronziedFileChangeListenerWhenMove () throws Exception {
        src.move (tgz);
    }
    public void testEvilSynchronziedFileChangeListenerWhenCreateFromTemplate () throws Exception {
        src.createFromTemplate (tgz);
    }
    public void testEvilSynchronziedFileChangeListenerWhenCreateFromTemplateWithName () throws Exception {
        src.createFromTemplate (tgz, "myNewNameOfTheObject");
    }
    public void testEvilSynchronziedFileChangeListenerWhenDelete () throws Exception {
        src.delete ();
    }
    public void testEvilSynchronziedFileChangeListenerWhenRename () throws Exception {
        src.rename ("myNewName");
    }
    
    //
    // synchronized listeners
    //
    
    private void block (final FileObject fo) {
        class OtherThreadAccess implements Runnable {
            public void run () {
                try {
                    if (fo.isValid ()) {
                        DataObject.find (fo); // should deadlock
                    }
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                    fail ("Error");
                }
            }
        }
        
        OtherThreadAccess other = new OtherThreadAccess ();
        org.openide.util.RequestProcessor.getDefault ().post (other).waitFinished (); // should deadlock
    }
    
    public void fileAttributeChanged (FileAttributeEvent fe) {
        block (fe.getFile ());
    }    

    public void fileChanged (FileEvent fe) {
        block (fe.getFile ());
    }
    
    public void fileDataCreated (FileEvent fe) {
        block (fe.getFile ());
    }
    
    public void fileDeleted (FileEvent fe) {
        block (fe.getFile ());
    }
    
    public void fileFolderCreated (FileEvent fe) {
        block (fe.getFile ());
    }
    
    public void fileRenamed (FileRenameEvent fe) {
        block (fe.getFile ());
    }
    
    public static final class Uni extends UniFileLoader {
        public Uni () {
            super(MultiDataObject.class);
        }
        protected String displayName() {
            return "UniLoader";
        }
        /** Recognizes just two files - .forget and .keep at once, only in non-forgetable mode 
         */
        protected synchronized FileObject findPrimaryFile(FileObject fo) {
            return fo.hasExt ("uni") ? fo : null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new NotifyObject (primaryFile, this);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }
        
        private class NotifyObject extends MultiDataObject {
			public NotifyObject (FileObject pf, Uni uni) throws DataObjectExistsException {
                super (pf, uni);
            }
            
            
            protected FileObject handleRename (String name) throws IOException {
                return getPrimaryEntry ().rename (name);
            }
            
            protected void handleDelete () throws IOException {
                getPrimaryEntry ().delete ();
            }
            
            protected org.openide.loaders.DataObject handleCopy (org.openide.loaders.DataFolder f) throws IOException {
                FileObject retValue = getPrimaryEntry ().copy (f.getPrimaryFile (), "suffix");
                return new NotifyObject (retValue, (Uni)getLoader ());
            }
            
            protected org.openide.loaders.DataShadow handleCreateShadow (org.openide.loaders.DataFolder f) throws IOException {
                DataShadow retValue;
                retValue = super.handleCreateShadow (f);
                return retValue;
            }
            
            protected org.openide.loaders.DataObject handleCreateFromTemplate (org.openide.loaders.DataFolder df, String name) throws IOException {
                FileObject retValue = getPrimaryEntry ().createFromTemplate (df.getPrimaryFile (), name);
                return new NotifyObject (retValue, (Uni)getLoader ());
            }
            
            protected FileObject handleMove (org.openide.loaders.DataFolder df) throws IOException {
                FileObject retValue = FileUtil.moveFile (getPrimaryFile (), df.getPrimaryFile (), getPrimaryFile ().getNameExt ());
                return retValue;
            }
        }
    }
    
  
}
