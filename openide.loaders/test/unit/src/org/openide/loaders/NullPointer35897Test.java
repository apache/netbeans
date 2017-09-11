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

import org.openide.filesystems.*;
import org.openide.loaders.*;
import java.beans.*;
import java.io.IOException;
import junit.textui.TestRunner;
import org.netbeans.junit.*;

/*
 * Tries to reproduce NPE from #35897 issue.
 */
public class NullPointer35897Test extends NbTestCase {
    private FileSystem lfs;
    private FileObject file;
    private L loader;
    private D obj;

    public NullPointer35897Test (String name) {
        super (name);
    }

    protected void setUp () throws Exception {
        TestUtilHid.destroyLocalFileSystem (getName ());
        String fsstruct [] = new String [] {
            "dir/simple.simple"
        };
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        Repository.getDefault ().addFileSystem (lfs);

        file = lfs.findResource (fsstruct[0]);
        
        loader = (L)DataLoader.getLoader (L.class);
        AddLoaderManuallyHid.addRemoveLoader (loader, true);
    }
    
    //Clear all stuff when the test finish
    protected void tearDown () throws Exception {
        AddLoaderManuallyHid.addRemoveLoader (loader, false);
        TestUtilHid.destroyLocalFileSystem (getName ());
    }
    
    public void test35897 () throws Exception {
        class InitObj implements Runnable {
            public void run () {
                try {
                    obj = (D)DataObject.find (file);
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                    fail ("Unexpected exception");
                }
            }
        }
        InitObj init = new InitObj ();
        
        org.openide.util.RequestProcessor.Task task;
        synchronized (loader) {
            task = org.openide.util.RequestProcessor.getDefault ().post (init);
            loader.wait ();
        }
        
        assertTrue ("The creation of DataObject is blocked in constructor", loader.waitingInConstructor);

        Repository.getDefault ().removeFileSystem (lfs);
        
        synchronized (loader) {
            loader.notifyAll ();
        }
        task.waitFinished ();
        
        assertNotNull ("The object has been finished", obj);
    }
    
    private static class D extends MultiDataObject {
        private boolean constructorFinished;
        
        public D (FileObject pf, L loader) throws IOException {
            super(pf, loader);

            synchronized (loader) {
                try {
                    loader.waitingInConstructor = true;
                    loader.notifyAll ();
                    loader.wait (2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail ("No interruptions please");
                } finally {
                    loader.waitingInConstructor = false;
                    constructorFinished = true;
                }
            }            
        }
        
        

        protected org.openide.nodes.Node createNodeDelegate() {
            return org.openide.nodes.Node.EMPTY;
        }
        
        public java.util.Set files () {
            assertTrue ("This can be called only if the constructor is finished", constructorFinished);
            return super.files ();
        }
        
    }

    private static class L extends MultiFileLoader {
        public boolean waitingInConstructor;
        
        public L () {
            super(D.class.getName());
        }
        protected String displayName() {
            return "L";
        }

        protected FileObject findPrimaryFile (FileObject obj) {
            return obj.hasExt ("simple") ? obj : null;
        }

        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new D(pf, this);
        }


        protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject x, FileObject obj) {
            throw new IllegalStateException ();
        }

        protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject x, FileObject obj) {
            return new org.openide.loaders.FileEntry (x, obj);
        }
    }

    
}
