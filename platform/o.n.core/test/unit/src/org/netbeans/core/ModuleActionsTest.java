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

package org.netbeans.core;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

/** Tests behaviour of asynchronous actions and exit dialog.
 */
public class ModuleActionsTest extends NbTestCase {

    public ModuleActionsTest (String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(ModuleActionsTest.class));
    }

    protected boolean runInEQ () {
        return true;
    }

    public void testActionIsListedAsRunning () throws Exception {
        Act act = Act.get(Act.class);
        
        synchronized (act) {
            act.actionPerformed (new java.awt.event.ActionEvent (this, 0, ""));
            act.wait ();
        }
        
        java.util.Collection col = ModuleActions.getDefaultInstance ().getRunningActions ();
        java.util.Iterator it = col.iterator ();
        while (it.hasNext ()) {
            javax.swing.Action a = (javax.swing.Action)it.next ();
            if (a.getValue (javax.swing.Action.NAME) == act.getName ()) {
                return;
            }
        }
        fail ("Act should be running: " + col);
    }

    public static class Act extends org.openide.util.actions.CallableSystemAction {
        
        public org.openide.util.HelpCtx getHelpCtx () {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        public String getName () {
            return getClass().getName ();
        }
        
        public synchronized void performAction () {
            notifyAll ();
            try {
                wait ();
            } catch (InterruptedException ex) {
                fail ("Shall not be interupted");
            }
        }
        
        protected boolean asynchronous () {
            return true;
        }
        
    }
    
}
