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
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/** Verifies asynchronous aspects of actions systems are close to the
 * original behaviour of SystemAction one.
 * Taken from org.openide.util.actions.AsynchronousTest
 * @author Jaroslav Tulach
 */
public class AsynchronousTest extends NbTestCase {

    private CharSequence err;
    
    public AsynchronousTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 5000;
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() {
        err = Log.enable("", Level.WARNING);
        AC.finished = false;
    }
    
    public void testExecutionOfActionsThatDoesNotDefineAsynchronousIsSynchronousNoWarningIsPrinted() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/none.instance").getAttribute("instanceCreate");
        
        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            assertTrue("The synchronous action is finished immediatelly", AC.finished);
        }
        
        if (err.length() > 0) {
            fail("There should be no warning about missing asynchronous: " + err);
        }
    }
    
    public void testExecutionCanBeAsynchronous() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/true.instance").getAttribute("instanceCreate");
        
        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Shall Not be finished yet", AC.finished);
            AsynchronousTest.class.wait();
            assertTrue("The asynchronous action is finished", AC.finished);
        }
        
        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }

    public void testExecutionCanBeAsynchronousForAlways() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/true-always.instance").getAttribute("instanceCreate");

        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Shall Not be finished yet", AC.finished);
            AsynchronousTest.class.wait();
            assertTrue("The asynchronous action is finished", AC.finished);
        }

        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }
    public void testExecutionCanBeSynchronous() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/false.instance").getAttribute("instanceCreate");
        
        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            assertTrue("The synchronous action is finished immediatelly", AC.finished);
        }
        
        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeForcedToBeSynchronous() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/true.instance").getAttribute("instanceCreate");
        
        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, "waitFinished"));
            assertTrue("When asked for synchronous the action is finished immediatelly", AC.finished);
        }
        
        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeAsynchronousForContext() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/true-context.instance").getAttribute("instanceCreate");

        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Shall Not be finished yet", AC.finished);
            AsynchronousTest.class.wait();
            assertTrue("The asynchronous action is finished", AC.finished);
        }

        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }

    public static class AC extends AbstractAction {
        static boolean finished;
        
        public void actionPerformed(ActionEvent ev) {
            synchronized (AsynchronousTest.class) {
                AsynchronousTest.class.notifyAll();
                finished = true;
            }
        }
    }

    public static class CAC extends AC implements ContextAwareAction {
        public Action createContextAwareInstance(Lookup actionContext) {
            return this;
        }
    }
}
