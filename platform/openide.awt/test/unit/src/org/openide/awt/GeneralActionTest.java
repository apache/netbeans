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

package org.openide.awt;

import java.awt.event.ActionListener;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class GeneralActionTest extends NbTestCase {
    private FileObject folder;
    
    public GeneralActionTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        folder = FileUtil.getConfigFile("actions/support/test");
        assertNotNull("testing layer is loaded: ", folder);
    }
    
    protected void tearDown() throws Exception {
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testKeyMustBeProvided() {
        String key = null;
        Action defaultDelegate = null;
        Lookup context = Lookup.EMPTY;
        
        ContextAwareAction expResult = null;
        try {
            ContextAwareAction result = GeneralAction.callback(key, defaultDelegate, context, false, false);
            fail("Shall fail as key is null");
        } catch (NullPointerException ex) {
            // ok
        }
    }

    public void testAttributesOnClone() throws Exception { // #182601
        Action a = GeneralAction.callback("whatever", null, Lookup.EMPTY, false, false);
        a.putValue("attr", "value");
        Action a2 = ((ContextAwareAction) a).createContextAwareInstance(Lookup.EMPTY);
        assertEquals("value", a2.getValue("attr"));
    }
    
    private static int myListenerCounter;
    private static ActionListener myListener() {
        myListenerCounter++;
        return null;
    }
    private static int myIconResourceCounter;
    private static String myIconResource() {
        myIconResourceCounter++;
        return "/org/netbeans/modules/actions/support/TestIcon.png";
    }
    
    
    private Action readAction(String fileName) throws Exception {
        FileObject fo = this.folder.getFileObject(fileName);
        assertNotNull("file " + fileName, fo);
        
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("File object has not null instanceCreate attribute", obj);
        
        if (!(obj instanceof Action)) {
            fail("Object needs to be action: " + obj);
        }
        
        return (Action)obj;
    }
}
