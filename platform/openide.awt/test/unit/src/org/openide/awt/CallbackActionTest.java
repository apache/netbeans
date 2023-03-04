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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach
 */
public class CallbackActionTest extends NbTestCase {
    private FileObject folder;
    
    public CallbackActionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    @Override
    protected void setUp() throws Exception {
        folder = FileUtil.getConfigFile("actions/support/test");
        assertNotNull("testing layer is loaded: ", folder);
    }
    
    public void testKeyMustBeProvided() {
        String key = null;
        Action defaultDelegate = null;
        Lookup context = Lookup.EMPTY;
        
        ContextAwareAction expResult = null;
        try {
            ContextAwareAction result = Actions.callback(key, defaultDelegate, false,
                    "displayName", "org/openide/awt/TestIcon.png", false);
            fail("Shall fail as key is null");
        } catch (NullPointerException ex) {
            // ok
        }
    }
    
    public void testCallback() throws Exception {
        FileObject fo = folder.getFileObject("testCallback.instance");
        
        Object obj = fo.getAttribute("instanceCreate");
        if (!(obj instanceof Action)) {
            fail("Shall create an action: " + obj);
        }
    }

    public void testCopyLikeProblem() throws Exception {
        FileObject fo = folder.getFileObject("testCopyLike.instance");

        Object obj = fo.getAttribute("instanceCreate");
        if (!(obj instanceof Action)) {
            fail("Shall create an action: " + obj);
        }

        InstanceContent ic = new InstanceContent();
        AbstractLookup l = new AbstractLookup(ic);
        ActionMap map = new ActionMap();
        map.put("copy-to-clipboard", new MyAction());
        ic.add(map);

        CntListener list = new CntListener();
        Action clone = ((ContextAwareAction)obj).createContextAwareInstance(l);
        clone.addPropertyChangeListener(list);
        assertTrue("Enabled", clone.isEnabled());

        ic.remove(map);
        assertFalse("Disabled", clone.isEnabled());
        list.assertCnt("one change", 1);
    }
    
    
    public void testWithFallback() throws Exception {
        MyAction myAction = new MyAction();
        MyAction fallAction = new MyAction();
        
        ActionMap other = new ActionMap();
        ActionMap tc = new ActionMap();
        tc.put("somekey", myAction);
        
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        ic.add(tc);
        
        ContextAwareAction a = callback("somekey", fallAction, al, false);
        CntListener l = new CntListener();
        a.addPropertyChangeListener(l);

        assertTrue("My action is on", myAction.isEnabled());
        assertTrue("Callback is on", a.isEnabled());
        
        l.assertCnt("No change yet", 0);
        
        ic.remove(tc);
        assertTrue("fall is on", fallAction.isEnabled());
        assertTrue("My is on as well", a.isEnabled());

        l.assertCnt("Still enabled, so no change", 0);
        
        fallAction.setEnabled(false);
        
        l.assertCnt("Now there was one change", 1);
        
        assertFalse("fall is off", fallAction.isEnabled());
        assertFalse("My is off as well", a.isEnabled());
        
        
        Action a2 = a.createContextAwareInstance(Lookup.EMPTY);
        assertEquals("Both actions are equal", a, a2);
        assertEquals("and have the same hash", a.hashCode(), a2.hashCode());
    }
    
    static ContextAwareAction callback(String key, AbstractAction fallAction, Lookup al, boolean b) {
        return GeneralAction.callback(key, fallAction, al, b, false);
    }
    
    private static final class CntListener extends Object
            implements PropertyChangeListener {
        private int cnt;
        
        public void propertyChange(PropertyChangeEvent evt) {
            cnt++;
        }
        
        public void assertCnt(String msg, int count) {
            assertEquals(msg, count, this.cnt);
            this.cnt = 0;
        }
    } // end of CntListener

    class MyAction extends AbstractAction {

        public int cntEnabled;
        public int cntPerformed;

        @Override
        public boolean isEnabled() {
            cntEnabled++;
            return super.isEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            cntPerformed++;
        }
    } // end of MyAction
    
}
