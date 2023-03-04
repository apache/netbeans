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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * @author Jaroslav Tulach
 */
public class InstanceDataObjectRefreshesOnFileChangeTest extends NbTestCase
implements LookupListener, PropertyChangeListener {
    private FileSystem mem1;
    private FileSystem mem2;
    private int cnt;
    private int pcl;
    
    public InstanceDataObjectRefreshesOnFileChangeTest(String name) {
        super (name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINER;
    }
    
    @Override
    protected void setUp () throws Exception {
        mem1 = FileUtil.createMemoryFileSystem();
        mem2 = FileUtil.createMemoryFileSystem();

        FileObject fo2 = FileUtil.createData(mem2.getRoot(), "Folder/MyInstance.instance");
        fo2.setAttribute("instanceCreate", "NewOne");
        Thread.sleep(300);
        FileObject fo1 = FileUtil.createData(mem1.getRoot(), "Folder/MyInstance.instance");
        fo1.setAttribute("instanceCreate", "OldOne");

        MockServices.setServices(DynamicFS.class);
        DynamicFS dfs = Lookup.getDefault().lookup(DynamicFS.class);
        dfs.setDelegate(mem1);
    }

    public void testSwitchRefreshesIDO() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Folder/MyInstance.instance");
        assertNotNull("File visible in SFS", fo);
        DataObject ido = DataObject.find(fo);
        Lookup lkp = ido.getLookup();
        Result<InstanceCookie> res = lkp.lookupResult(InstanceCookie.class);
        assertEquals("One cookie", 1, res.allItems().size());
        res.addLookupListener(this);
        ido.addPropertyChangeListener(this);

        assertInstance(lkp, "OldOne");

        assertEquals("no lookup change yet", 0, cnt);
        assertEquals("no pcl change yet", 0, pcl);

        DynamicFS dfs = Lookup.getDefault().lookup(DynamicFS.class);
        dfs.setDelegate(mem2);

        assertEquals("one pcl change now", 1, pcl);
        if (cnt == 0) {
            fail("At least one change in lookup shall be notified");
        }

        FileObject fo2 = FileUtil.getConfigFile("Folder/MyInstance.instance");
        assertNotNull("File is still visible in SFS", fo);
        DataObject ido2 = DataObject.find(fo2);

        assertSame("Data object remains", ido, ido2);

        assertInstance(lkp, "NewOne");
    }

    private static void assertInstance(Lookup lkp, Object instance) throws Exception {
        InstanceCookie ic = lkp.lookup(InstanceCookie.class);
        assertNotNull("InstanceCookie found", ic);
        Object o = ic.instanceCreate();
        assertNotNull("Instance created", o);
        assertEquals("Same as expected", instance, o);
    }

    public void resultChanged(LookupEvent ev) {
        cnt++;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (InstanceDataObject.PROP_COOKIE.equals(evt.getPropertyName())) {
            pcl++;
        }
    }

    public static final class DynamicFS extends MultiFileSystem {
        public DynamicFS() {
        }

        public void setDelegate(FileSystem fs) {
            super.setDelegates(fs);
        }
    }
}
