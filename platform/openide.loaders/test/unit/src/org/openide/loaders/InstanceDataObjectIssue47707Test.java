/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.loaders;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.cookies.*;


import org.netbeans.junit.*;
import org.openide.util.test.MockLookup;

/** Simulate deadlock from issue 47707.
 *
 * @author Radek Matous, Jaroslav Tulach
 */
public class InstanceDataObjectIssue47707Test extends NbTestCase {
    /** folder to create instances in */
    private DataObject inst;
    /** filesystem containing created instances */
    private FileSystem lfs;
    
    public InstanceDataObjectIssue47707Test(String name) {
        super (name);
    }
    
    protected @Override void setUp() throws Exception {
        MockLookup.setInstances(new EP());
        
        String fsstruct [] = new String [] {
            "A.settings",
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("A.settings");
        
        inst = DataObject.find (bb);
    }

    public void testGetCookieCanBeCalledTwice () throws Exception {
        Object cookie = inst.getCookie (org.openide.cookies.InstanceCookie.class);
        
        assertNotNull ("There is at least data object", cookie);
        assertEquals ("Of right type", LkpForDO.class, cookie.getClass ());
        
    }
    
    private static final class EP implements Environment.Provider {
        public org.openide.util.Lookup getEnvironment (DataObject obj) {
            return new LkpForDO (new org.openide.util.lookup.InstanceContent (), obj);
        }
    }
    
    private static final class LkpForDO extends org.openide.util.lookup.AbstractLookup
    implements org.openide.cookies.InstanceCookie, Runnable {
        private boolean triedToDeadlock;
        private DataObject obj;
        
        private LkpForDO (org.openide.util.lookup.InstanceContent ic, DataObject obj) {
            super (ic);
            ic.add (this);
            this.obj = obj;
        }
        
        public void run () {
            // tries to query instance data object from other thread
            Object o = obj.getCookie (InstanceCookie.class);
            assertNotNull ("Cookie is there", o);
        }

        protected @Override void beforeLookup(Template template) {
            if (!triedToDeadlock) {
                triedToDeadlock = true;
                org.openide.util.RequestProcessor.getDefault ().post (this).waitFinished ();
            }
        }
        
        
        public String instanceName () {
            return getClass ().getName ();
        }

        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return getClass ();
        }

        public Object instanceCreate ()
        throws java.io.IOException, ClassNotFoundException {
            return this;
        }

        public @Override String toString() {
            return getClass().getName();
        }
    } // end LkpForDO
    
}
