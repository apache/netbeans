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
package org.netbeans.core.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author tomas
 */
public class NoMVCLookupTest extends NbTestCase {
    private long TIMEOUT = 10000;

    public NoMVCLookupTest(String name) throws IOException {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NoMVCLookupTest.class, "platform", ".*");
    }
    
    public void testNoDOInLookup() throws IOException, InterruptedException, InvocationTargetException {
        final L l = new L();
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(l);
        
        l.init();                             // initialize the listener
        createAndOpen(l, "file-1");
        assertTrue(l.opened); 
//        assertTrue(l.dataObjectFoundInLookup);   // fails
        closeTC(l.tc);
        
        l.init();
        createAndOpen(l, "file-2");
        assertTrue(l.opened);
        assertTrue(l.dataObjectFoundInLookup);     // fails unless the previously opened TC wasn't closed 
//        closeTC(l.tc); 
        
        l.init();
        createAndOpen(l, "file-3");
        assertTrue(l.opened);
        assertTrue(l.dataObjectFoundInLookup);     // fails unless the previously opened TC wasn't closed 
        closeTC(l.tc);
        
        l.init();
        createAndOpen(l, "file-4");
        assertTrue(l.opened);
        assertTrue(l.dataObjectFoundInLookup);     // fails unless the previously opened TC wasn't closed 
        
    }

    private void closeTC(final TopComponent tc) throws InvocationTargetException, InterruptedException {
        if(tc == null) return;
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                tc.close();
            }
        });
    }

    private void createAndOpen(L l, String fileName) throws IOException, InterruptedException {
        
        // 1. create a file
        File f = new File(getWorkDir(), fileName + "-" + System.currentTimeMillis() + ".txt");
        f.createNewFile();
        FileObject fo = FileUtil.toFileObject(f);
        DataObject data = DataObject.find(fo);

        l.init();
        
        // 2. open and ...
        EditCookie ec1 = data.getCookie(EditCookie.class);
        ec1.edit();
        // 3. ... wait for the PROP_TC_OPENED event
        long t = System.currentTimeMillis();
        while((!l.opened || !l.dataObjectFoundInLookup) && System.currentTimeMillis() - t < TIMEOUT) {
            Thread.sleep(200);
        }
    }
    
    private class L implements PropertyChangeListener {
//        DataObject data;
        boolean dataObjectFoundInLookup = false;
//        boolean dataObjectAfterTCOpen = false;
        boolean opened = false;
        TopComponent tc;
        
        public L() {
        }
        
        void init() {
//            this.data = data;
            opened = false;
            dataObjectFoundInLookup = false;
            tc = null;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(TopComponent.Registry.PROP_TC_OPENED.equals(evt.getPropertyName())) {
                Object obj = evt.getNewValue();
                if(obj instanceof TopComponent) {
                    final TopComponent openedTC = (TopComponent) obj;
                    Lookup lookup = openedTC.getLookup();
                    DataObject openedDataObject = lookup.lookup(DataObject.class);
                    dataObjectFoundInLookup = openedDataObject != null;
//                    if(openedDataObject != null) {
//                        dataObjectOnTCOpen = data == openedDataObject;
//                    } 
//                    else {
//                        Result<DataObject> r = lookup.lookupResult(DataObject.class);
//                        r.addLookupListener(new LookupListener() {
//                            @Override
//                            public void resultChanged(LookupEvent ev) {
//                                Lookup lookup = openedTC.getLookup();
//                                DataObject openedDataObject = lookup.lookup(DataObject.class);
//                                dataObjectAfterTCOpen = data == openedDataObject;
//                            }
//                        });
//                    }
                    this.tc = openedTC;
                }
                opened = true;
            }
        }
    }
    
}
