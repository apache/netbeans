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

import junit.framework.*;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Jan Becicka
 */
public class FolderRenameHandlerTest extends TestCase {
    static {
        System.setProperty ("org.openide.util.Lookup", Lkp.class.getName()); // NOI18N
    }

    private FileObject fo;
    private Node n;
    private FolderRenameHandlerImpl frh = new FolderRenameHandlerImpl();
    
    public FolderRenameHandlerTest (String testName) {
        super (testName);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        FileObject root = FileUtil.getConfigRoot();
        fo = FileUtil.createFolder (root, "test");// NOI18N
        
        DataObject obj = DataObject.find (fo);
        if (!  (obj instanceof DataFolder)) {
            fail ("It should be DataFolder: " + obj);// NOI18N
        }
        
        assertNotNull(obj);
        n = obj.getNodeDelegate();
        assertNotNull(n);
    }
    
    public void tearDown() throws Exception {
        super.tearDown(); 
        fo.delete();
    }

    public void testRenameHandlerNotCalled () throws Exception {
        ((Lkp) Lkp.getDefault()).register(new Object[]{});
        frh.called = false;
        
        n.setName("blabla");
        assertFalse(frh.called);
    }
    
    public void testRenameHandlerCalled () throws Exception {
        ((Lkp) Lkp.getDefault()).register(new Object[]{frh});
        frh.called = false;
        
        n.setName("foo");// NOI18N
        assertTrue(frh.called);
    }
    
    public static class Lkp extends ProxyLookup {
        private static final Lookup mandatorySerices =
            Lookups.singleton(new NbMutexEventProvider());
        public Lkp() {
            super(new Lookup[]{mandatorySerices});
        }
        public void register(Object[] instances) {
            setLookups(new Lookup[] {
                mandatorySerices,
                Lookups.fixed(instances)
            });
        }
    }    
    private static final class FolderRenameHandlerImpl implements FolderRenameHandler {
        boolean called  = false;
        public void handleRename(DataFolder folder, String newName) throws IllegalArgumentException {
            called = true;
        }
    }
    
}
