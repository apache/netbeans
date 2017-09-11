/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
