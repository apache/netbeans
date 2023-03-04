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

package org.openide.util.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.util.actions.CallbackSystemActionTest.SurviveFocusChgCallbackAction;

/** Test CallbackSystemAction: changing performer, focus tracking.
 * @author Jesse Glick
 */
public class CallbackSystemActionConcurrentTest extends NbTestCase {

    static {
        ActionsInfraHid.install();
    }

    private Logger LOG;
    
    public CallbackSystemActionConcurrentTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        LOG.info("setUp");
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 11000; // cf. SimpleCallbackAction.waitInstancesZero
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    // http://www.netbeans.org/nonav/issues/show_bug.cgi?id=144684
    public void testConcurrentModificationException () {
        class MyAction extends AbstractAction {
            public int cntEnabled;
            public int cntPerformed;
            
            @Override
            public boolean isEnabled() {
                cntEnabled++;
                return true;
            }
            
            @Override
            public void actionPerformed(ActionEvent ev) {
                cntPerformed++;
            }
        }
        MyAction myAction = new MyAction();

        
        final ActionMap other = new ActionMap();
        ActionMap tc = new ActionMap() {
            @Override
            public Action get(Object key) {
                ActionsInfraHid.setActionMaps(other, new ActionMap ());
                return super.get (key);
            }
        };
        ActionsInfraHid.setActionMaps(other, new ActionMap ());
        SurviveFocusChgCallbackAction a = SurviveFocusChgCallbackAction.get (SurviveFocusChgCallbackAction.class);
        assertFalse("Disabled, as no a.getActionMapKey() in any map", a.isEnabled());
        other.put(a.getActionMapKey(), myAction);
        ActionsInfraHid.setActionMaps(tc, new ActionMap (), new ActionMap (), new ActionMap ());
        assertTrue("Enabled now", a.isEnabled());
        
    }
        
}
