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

import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.test.MockPropertyChangeListener;

/** Test that boolean actions are in fact toggled.
 * @author Jesse Glick
 */
public class BooleanStateActionTest extends NbTestCase {

    public BooleanStateActionTest(String name) {
        super(name);
    }

    /** Self-explanatory, hopefully. */
    public void testToggle() throws Exception {
        BooleanStateAction a1 = (BooleanStateAction)SystemAction.get(SimpleBooleanAction1.class);
        assertTrue(a1.isEnabled());
        BooleanStateAction a2 = (BooleanStateAction)SystemAction.get(SimpleBooleanAction2.class);
        assertTrue(a1.getBooleanState());
        assertFalse(a2.getBooleanState());
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        a1.addPropertyChangeListener(l);
        a1.actionPerformed(null);
        l.expectEvent(BooleanStateAction.PROP_BOOLEAN_STATE, 1500);
        assertFalse(a1.getBooleanState());
        a1.removePropertyChangeListener(l);
        l.reset();//l.gotit = 0;
        a2.addPropertyChangeListener(l);
        a2.actionPerformed(null);
        l.expectEvent(BooleanStateAction.PROP_BOOLEAN_STATE, 1500);
        assertTrue(a2.getBooleanState());
        a2.removePropertyChangeListener(l);
    }
    
    public static final class SimpleBooleanAction1 extends BooleanStateAction {
        public String getName() {
            return "SimpleBooleanAction1";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    public static final class SimpleBooleanAction2 extends BooleanStateAction {
        protected void initialize() {
            super.initialize();
            setBooleanState(false);
        }
        public String getName() {
            return "SimpleBooleanAction2";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
}
