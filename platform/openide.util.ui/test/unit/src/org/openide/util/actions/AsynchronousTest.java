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
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/** Test general aspects of system actions.
 * Currently, just the icon.
 * @author Jesse Glick
 */
public class AsynchronousTest extends NbTestCase {

    private CharSequence err;
    
    public AsynchronousTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() {
        err = Log.enable("", Level.ALL);
    }
    
    public void testExecutionOfActionsThatDoesNotOverrideAsynchronousIsAsynchronousButWarningIsPrinted() throws Exception {
        DoesNotOverride action = (DoesNotOverride)DoesNotOverride.get(DoesNotOverride.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Not yet finished", action.finished);
            action.wait();
            assertTrue("The asynchronous action is finished", action.finished);
        }
        
        if (err.toString().indexOf(DoesNotOverride.class.getName() + " should override") < 0) {
            fail("There should be warning about not overriding asynchronous: " + err);
        }
    }
    
    public void testExecutionCanBeAsynchronous() throws Exception {
        DoesOverrideAndReturnsTrue action = (DoesOverrideAndReturnsTrue)DoesOverrideAndReturnsTrue.get(DoesOverrideAndReturnsTrue.class);
        
        synchronized (action) {
            action.finished = false;
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Not yet finished", action.finished);
            action.wait();
            assertTrue("The asynchronous action is finished", action.finished);
        }
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeSynchronous() throws Exception {
        DoesOverrideAndReturnsFalse action = (DoesOverrideAndReturnsFalse)DoesOverrideAndReturnsFalse.get(DoesOverrideAndReturnsFalse.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            assertTrue("The synchronous action is finished immediatelly", action.finished);
        }
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeForcedToBeSynchronous() throws Exception {
        DoesOverrideAndReturnsTrue action = (DoesOverrideAndReturnsTrue)DoesOverrideAndReturnsTrue.get(DoesOverrideAndReturnsTrue.class);
        
        synchronized (action) {
            action.actionPerformed(new ActionEvent(this, 0, "waitFinished"));
            assertTrue("When asked for synchronous the action is finished immediatelly", action.finished);
        }
        
        if (err.toString().indexOf(DoesOverrideAndReturnsTrue.class.getName()) >= 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public static class DoesNotOverride extends CallableSystemAction {
        boolean finished;
        
        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        @Override
        public String getName() {
            return "Should warn action";
        }
        
        @Override
        public synchronized void performAction() {
            notifyAll();
            finished = true;
        }
        
    }
    
    public static class DoesOverrideAndReturnsTrue extends DoesNotOverride {
        @Override
        public boolean asynchronous() {
            return true;
        }
    }
    
    public static final class DoesOverrideAndReturnsFalse extends DoesOverrideAndReturnsTrue {
        @Override
        public boolean asynchronous() {
            return false;
        }
    }
}
