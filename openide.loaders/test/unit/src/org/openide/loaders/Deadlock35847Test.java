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

import org.openide.ErrorManager;

import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;

/** Simulates the deadlock from issue 35847.
 * @author Jaroslav Tulach
 */
public class Deadlock35847Test extends LoggingTestCaseHid {
    private ErrorManager err;

    public Deadlock35847Test(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        registerIntoLookup(new Pool());
    }
    
    public void testLoaderThatStopsToRecognizeWhatItHasRecognized () throws Exception {
        ForgetableLoader l = (ForgetableLoader)ForgetableLoader.getLoader(ForgetableLoader.class);
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[] {
            "folder/f.forget",
            "folder/f.keep"
        });

        // do not recognize anything
        l.forget = true;

        FileObject fo = lfs.findResource("folder");
        DataFolder f = DataFolder.findFolder(fo);


        DataObject[] arr = f.getChildren ();
        assertEquals ("Two child there", 2, arr.length);

        DataObject keep;
        java.lang.ref.WeakReference forget;
        if (arr[0].getPrimaryFile().hasExt ("keep")) {
            keep = arr[0];
            forget = new java.lang.ref.WeakReference (arr[1]);
        } else {
            keep = arr[1];
            forget = new java.lang.ref.WeakReference (arr[0]);
        }

        org.openide.nodes.Node theDelegate = new org.openide.nodes.FilterNode (keep.getNodeDelegate());

        arr = null;
        assertGC ("Forgetable object can be forgeted", forget);

        class P extends org.openide.nodes.NodeAdapter
        implements java.beans.PropertyChangeListener {
            int cnt;
            String name;

            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                name = ev.getPropertyName();
                cnt++;
                err.log("Event arrived: " + ev.getPropertyName());
            }
        }
        P listener = new P ();
        keep.addPropertyChangeListener (listener);
        // in order to trigger listening on the original node and cause deadlock
        theDelegate.addNodeListener(listener);

        // now recognize
        l.forget = false;

        // this will trigger invalidation of keep from Folder Recognizer Thread
        err.log("Beging to get children");
        DataObject[] newArr = f.getChildren ();
        err.log("End of get children");

        assertEquals ("Keep is Invalidated", 1, listener.cnt);
        assertEquals ("Property is PROP_VALID", DataObject.PROP_VALID, listener.name);
    }
    
    public void testLoaderThatStopsToRecognizeWhatItHasRecognizedAndDoesItWhileHoldingChildrenMutex () throws Exception {
        org.openide.nodes.Children.MUTEX.readAccess (new org.openide.util.Mutex.ExceptionAction () {
            public Object run () throws Exception {
                testLoaderThatStopsToRecognizeWhatItHasRecognized ();
                return null;
            }
        });
    }
    

    public static final class ForgetableLoader extends MultiFileLoader {
        public boolean forget;
        
        public ForgetableLoader () {
            super(MultiDataObject.class);
        }
        protected String displayName() {
            return "ForgetableLoader";
        }
        /** Recognizes just two files - .forget and .keep at once, only in non-forgetable mode 
         */
        protected FileObject findPrimaryFile(FileObject fo) {
            if (forget) {
                return null;
            }
            if (fo.hasExt ("forget")) {
                return FileUtil.findBrother (fo, "keep");
            }
            if (fo.hasExt ("keep")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject (primaryFile, this);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    private static final class Pool extends DataLoaderPool {
        public Pool() {
        }
        
        public Enumeration loaders() {
            ForgetableLoader l = (ForgetableLoader)ForgetableLoader.getLoader(ForgetableLoader.class);
            return org.openide.util.Enumerations.singleton(l);
        }
    }
}
