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
