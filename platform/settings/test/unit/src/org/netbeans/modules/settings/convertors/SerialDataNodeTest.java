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

package org.netbeans.modules.settings.convertors;

import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author  Jan Pokorsky
 */
public class SerialDataNodeTest extends NbTestCase {

    /** Creates a new instance of SerialDataNodeTest */
    public SerialDataNodeTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }
    
    public void testDisplayName() throws Exception {
        String res = "Settings/org-netbeans-modules-settings-convertors-testDisplayName.settings";
        FileObject fo = FileUtil.getConfigFile(res);
        assertNotNull(res, fo);
        assertNull("name", fo.getAttribute("name"));
        
        DataObject dobj = DataObject.find (fo);
        Node n = dobj.getNodeDelegate();
        assertNotNull(n);
        assertEquals("I18N", n.getDisplayName());
        
        // property sets have to be initialized otherwise the change name would be
        // propagated to the node after some delay (~2s)
        Object garbage = n.getPropertySets();
        
        InstanceCookie ic = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
        assertNotNull (dobj + " does not contain instance cookie", ic);
        
        FooSetting foo = (FooSetting) ic.instanceCreate();
        String newName = "newName";
        foo.setName(newName);
        assertEquals(n.toString(), newName, n.getDisplayName());
        
        newName = "newNameViaNode";
        n.setName(newName);
        assertEquals(n.toString(), newName, n.getDisplayName());
        assertEquals(n.toString(), newName, foo.getName());
    }
}
