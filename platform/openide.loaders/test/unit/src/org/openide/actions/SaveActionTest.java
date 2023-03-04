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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Tests SaveAction.
 * @author Jaroslav Tulach
 */
public class SaveActionTest extends NbTestCase {
    
    public SaveActionTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(new Class[] {MyStatusDisplayer.class});
        assertNotNull("MyDisplayer is used", Lookup.getDefault().lookup(MyStatusDisplayer.class));
    }
    
    protected boolean runInEQ() {
        return true;
    }

    public void testToStringOfDelegateContainsNameOfOriginalAction() throws Exception {
        SaveAction sa = SaveAction.get(SaveAction.class);
        Action a = sa.createContextAwareInstance(Lookup.EMPTY);
        if (a.toString().indexOf("SaveAction") == -1) {
            fail("We need name of the original action:\n" + a.toString());
        }
    }
    
    /** @see "issue #36616" */
    public void testSaveActionTakesNameOfDataNodeIfAvailable() throws Exception {
        try {
            LocalFileSystem lfs = new LocalFileSystem();
            File workDir = getWorkDir();
            File simpleFile =  new File(workDir, "folder/file.simple");
            if (!simpleFile.exists()) {
                simpleFile.getParentFile().mkdirs();
                simpleFile.createNewFile();
                assertTrue(simpleFile.exists());
            }
            lfs.setRootDirectory(workDir);
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            final DataObject obj = DataObject.find(fo);

            SaveAction sa = SaveAction.get(SaveAction.class);
            
            class MyNode extends FilterNode 
            implements SaveCookie {
                public int cnt;
                
                public MyNode() {
                    super(obj.getNodeDelegate());
                    disableDelegation(
                        FilterNode.DELEGATE_GET_NAME |
                        FilterNode.DELEGATE_GET_DISPLAY_NAME |
                        FilterNode.DELEGATE_GET_SHORT_DESCRIPTION |
                        FilterNode.DELEGATE_SET_NAME |
                        FilterNode.DELEGATE_SET_DISPLAY_NAME |
                        FilterNode.DELEGATE_SET_SHORT_DESCRIPTION
                    );
                    
                    setName("my name");
                }
                
                public Node.Cookie getCookie(Class c) {
                    if (c.isInstance(this)) {
                        return this;
                    }
                    return super.getCookie(c);
                }
                
                public void save() {
                    cnt++;
                }
            }
            
            MyNode myNode = new MyNode();
            Action clone = sa.createContextAwareInstance(Lookups.singleton(myNode));
            
            clone.actionPerformed(new ActionEvent(this, 0, "waitFinished"));
            
            assertEquals("Save called", 1, myNode.cnt);
            assertEquals("One msgs", 1, MyStatusDisplayer.cnt);
            if (MyStatusDisplayer.text.indexOf("file.simple") < 0) {
                fail("Wrong message: " + MyStatusDisplayer.text);
            }
        } finally {
            clearWorkDir();
        }
    }
    
    public static class MyStatusDisplayer extends StatusDisplayer {
        public static int cnt;
        public static String text;
        
        public void addChangeListener(ChangeListener l) {}
        
        public String getStatusText() {
            return text;
        }
        
        public void removeChangeListener(ChangeListener l) {}
        
        public void setStatusText(String msg) {
            cnt++;
            text = msg;
        }
        
    }
    
}
