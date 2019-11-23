/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.awt;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.SwingUtilities;

/** Test that cookie actions are in fact sensitive to the correct cookies in the
 * correct numbers, and that changes to either node selection or cookies on the
 * selected nodes trigger a change in the selected state.
 * @author Jesse Glick
 */
public class ContextActionNonAWTTest extends ContextActionTest {
    public ContextActionNonAWTTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    protected boolean getIsEnabled(final Action a1) throws InterruptedException, InvocationTargetException {
        assertFalse("Not in AWT", EventQueue.isDispatchThread());
        
        class R implements Runnable {
            boolean is;
            public void run() {
                is = a1.isEnabled();
            }
        }
        R run = new R();
        SwingUtilities.invokeAndWait(run);
        return run.is;
    }

    @Override
    protected boolean getIsChecked(final Action a1) throws InterruptedException, InvocationTargetException {
        assertFalse("Not in AWT", EventQueue.isDispatchThread());
        
        class R implements Runnable {
            boolean is;
            public void run() {
                is = Boolean.TRUE.equals(a1.getValue(Action.SELECTED_KEY));
            }
        }
        R run = new R();
        SwingUtilities.invokeAndWait(run);
        return run.is;
    }

    protected void doActionPerformed(final Action a1, final ActionEvent ev) throws InterruptedException, InvocationTargetException {
        assertFalse("Not in AWT", EventQueue.isDispatchThread());
        
        class R implements Runnable {
            public void run() {
                a1.actionPerformed(ev);
            }
        }
        R run = new R();
        SwingUtilities.invokeAndWait(run);
    }
    
}

