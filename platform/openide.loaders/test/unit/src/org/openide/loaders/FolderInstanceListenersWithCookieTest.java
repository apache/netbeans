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

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.filesystems.*;
import org.openide.cookies.*;
import org.openide.util.*;

import org.netbeans.junit.*;
import java.util.Enumeration;

public class FolderInstanceListenersWithCookieTest extends NbTestCase {
    private Logger err;

    public FolderInstanceListenersWithCookieTest() {
        super("");
    }
    
    public FolderInstanceListenersWithCookieTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }

    private static void setSystemProp(String key, String value) {
        java.util.Properties prop = System.getProperties();
        if (prop.get(key) != null) return;
        prop.put(key, value);
    }

    @Override
    protected void setUp () throws Exception {
        MockServices.setServices(Pool.class);
        
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        assertEquals (Pool.class, pool.getClass ());
        
        Pool.setExtra(null);
        
        clearWorkDir ();
        
        err = Logger.getLogger("TEST-" + getName());
    }

    /** Checks whether only necessary listeners are attached to the objects.
     * Initial object has cookie.
     */
    public void testListenersCountWithCookie () throws Exception {
        doTestListenersCount (true);
    }
        
    /** Because listeners have different code for objects with cookie and 
     * without cookie, we add this utility test and run it twice.
     *
     * @param cookie add cookie or not
     */
    private void doTestListenersCount (boolean cookie) throws Exception {  
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[0]);

        FileObject bb = lfs.findResource("/AA");
        err.info("Found resource: " + bb);
        if (bb != null) {
            bb.delete ();
        }
        err.info("Resource deleted");
        FileObject theFile = FileUtil.createData (lfs.getRoot (), "/AA/A.simple");
        err.info("Found the file: " + theFile);
        bb = FileUtil.createFolder(lfs.getRoot (), "/AA");
        err.info("Found the folder: " + bb);
        assertTrue("Is file", theFile.isData());
        err.info("Confirmed, its the data: " + theFile);
        
        
        DataFolder folder = DataFolder.findFolder (bb);
        

        DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
        err.info("Add loader: " + l);
        Pool.setExtra(l);
        err.info("Loader added");
        try {
            FileObject aa = lfs.findResource("/AA/A.simple");
            DataObject tmp = DataObject.find (aa);
            assertEquals ("Is of the right type", DataLoaderOrigTest.SimpleDataObject.class, tmp.getClass ());
            DataLoaderOrigTest.SimpleDataObject obj = (DataLoaderOrigTest.SimpleDataObject)tmp;
            
            err.info("simple object found: " + obj);
            
            if (cookie) {
                err.info("Adding cookie");
                obj.cookieSet().add (new InstanceSupport.Instance (new Integer (100)));
                err.info("Cookie added");
            }
            
            
            F instance = new F (folder);
            err.info("Instance for " + folder + " created");
            Object result = instance.instanceCreate ();
            err.info("instanceCreate called. Result: " + result);
            
            Enumeration en = obj.listeners ();
            
            err.info("Asking for listeners of " + obj);
            
            assertTrue ("Folder instance should have add one listener", en.hasMoreElements ());
            en.nextElement ();
            assertTrue ("But there should be just one", !en.hasMoreElements ());
            
            err.info("Successfully tested for one listener, creating B.simple");
            
            folder.getPrimaryFile().createData("B.simple");
            err.info("B.simple created");
            assertEquals ("DO created", folder.getChildren ().length, 2);
            err.info("Children obtained correctly");
            
            // wait to finish processing
            result = instance.instanceCreate ();
            err.info("instanceCreate finished, with result: " + result);

            en = obj.listeners ();
            err.info("Asking for listeners once again");
            assertTrue ("Folder instance should not change the amount of listeners", en.hasMoreElements ());
            en.nextElement ();
            assertTrue ("And there still should be just one", !en.hasMoreElements ());
            err.info("Successfully tested for listeners");
        } finally {
            err.info("Clearing data loader");
            Pool.setExtra(null);
            err.info("Loader cleared");
        }
    }
   
    private static class F extends FolderInstance {
        /** count number of changes. */
        private int count;
        
        public F (DataFolder f) {
            super (f);
        }
        
        /** Getter to number of changes of this folder instance.
         */
        public synchronized int getCount () {
            int c = count;
            count = 0;
            return c;
        }
            
        
        /** Accepts folder.
         */
        @Override
        protected InstanceCookie acceptFolder (DataFolder f) {
            return new F (f);
        }
        
        protected Object createInstance (InstanceCookie[] arr) 
        throws java.io.IOException, ClassNotFoundException {
            synchronized (this) {
                count++;
            }
            LinkedList ll = new LinkedList ();
            for (int i = 0; i < arr.length; i++) {
                Object obj = arr[i].instanceCreate ();
                if (obj instanceof Collection) {
                    ll.addAll ((Collection)obj);
                } else {
                    ll.add (obj);
                }
            }
            return ll;
        }
        @Override
        protected Task postCreationTask (Runnable run) {
            //super.postCreationTask (run);
            
            run.run ();
            return null;
        }
    }   

    public static final class Numb extends Object implements Serializable {
        public Numb () {
        }
    }
    
    
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            ic.add(new Pool ());
        }
    }
    
    public static final class Pool extends org.openide.loaders.DataLoaderPool {
        private static DataLoader extra;
        
        
        protected Enumeration loaders () {
            if (getExtra() == null) {
                return Enumerations.empty ();
            } else {
                return Enumerations.singleton (getExtra());
            }
        }

        public static DataLoader getExtra () {
            return extra;
        }

        public static void setExtra (DataLoader aExtra) {
            if (extra != null && aExtra != null) {
                fail ("Both are not null: " + extra + " aExtra: " + aExtra);
            }
            extra = aExtra;
            Pool p = (Pool)DataLoaderPool.getDefault ();
            p.fireChangeEvent (new javax.swing.event.ChangeEvent (p));
        }
    }
}
