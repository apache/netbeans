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


import java.awt.Button;
import java.awt.Color;
import java.beans.*;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import javax.swing.JButton;
import junit.framework.Test;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;

public class InstanceDataObjectLookupWarningTest extends NbTestCase {
    /** folder to create instances in */
    private DataFolder folder;
    /** filesystem containing created instances */
    private FileSystem lfs;
    
    /** Creates new DataFolderTest */
    public InstanceDataObjectLookupWarningTest(String name) {
        super (name);
    }
    
    
    protected void setUp () throws Exception {
        TestUtilHid.destroyLocalFileSystem (getName());
        clearWorkDir();
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), new String[0]);

    }

    /** #28118, win sys relies that instance data object fires cookie 
     * changes when its settings file removed, it gets into corruped state otherwise. */
    public void testNoWarnings() throws Exception {
        CharSequence log = Log.enable("org.openide.loaders", Level.WARNING);
        
        FileObject fo = FileUtil.createData(lfs.getRoot(), "x.instance");
        fo.setAttribute("instanceClass", "javax.swing.JButton");
        DataObject obj = DataObject.find(fo);
        assertEquals("IDO", InstanceDataObject.class, obj.getClass());
        
        InstanceCookie ic = obj.getLookup().lookup(InstanceCookie.class);
        assertNotNull("We have cookie", ic);
        
        Object o = ic.instanceCreate();
        assertNotNull("Obj created", o);
        
        assertEquals("button", JButton.class, o.getClass());
        
        if (log.length() > 0) {
            fail("No warnings, but: " + log);
        }
    }
}
