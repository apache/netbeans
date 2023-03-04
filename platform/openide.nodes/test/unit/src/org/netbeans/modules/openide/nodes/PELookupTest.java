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
package org.netbeans.modules.openide.nodes;

import java.beans.PropertyEditorManager;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jan Horvath <jhorvath@netbeans.org>
 */
public class PELookupTest extends NbTestCase {
    
    public PELookupTest(String name) {
        super(name);
    }
    
    static {
        System.setProperty("org.openide.util.Lookup.paths", "Services");
    }
    
    public void testPackageUnregistering() {
        MockLookup.setInstances(new NodesRegistrationSupport.PEPackageRegistration("test1.pkg"));
        NodeOp.registerPropertyEditors();
        MockLookup.setInstances(new NodesRegistrationSupport.PEPackageRegistration("test2.pkg"));
        
        String[] editorSearchPath = PropertyEditorManager.getEditorSearchPath();
        int count = 0;
        for (int i = 0; i < editorSearchPath.length; i++) {
            assertNotSame("test1.pkg", editorSearchPath[i]);
            if ("test2.pkg".equals(editorSearchPath[i])) {
                count++;
            }
        }
        assertEquals(1, count);
        
    }
    
}
