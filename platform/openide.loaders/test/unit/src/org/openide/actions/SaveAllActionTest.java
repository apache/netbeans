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
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.LifecycleManager;

/**
 *
 * @author Jaroslav Tulach
 */
public class SaveAllActionTest extends NbTestCase {

    public SaveAllActionTest (String testName) {
        super (testName);
    }

    protected boolean runInEQ () {
        return true;
    }


    protected void setUp () {
        MockServices.setServices(new Class[] {Life.class});
        Life.max = 0;
        Life.cnt = 0;
        Life.executed = 0;
    }

    public void testThatTheActionIsInvokeOutsideOfAWTThreadAndOnlyOnceAtATime () throws Exception {
        SaveAllAction a = SaveAllAction.get(SaveAllAction.class);
        a.setEnabled (true);
        assertTrue ("Is enabled", a.isEnabled ());
        
        ActionEvent ev = new ActionEvent (this, 0, "");
        a.actionPerformed (ev);
        a.actionPerformed (ev);
        a.actionPerformed (ev);
        
        Object life = org.openide.util.Lookup.getDefault ().lookup (LifecycleManager.class);
        synchronized (life) {
            while (Life.executed != 3) {
                life.wait ();
            }
        }
        
        assertEquals ("Maximum is one invocation of saveAll at one time", 1, Life.max);
    }

    public static final class Life extends LifecycleManager {
        static int max;
        static int cnt;
        static int executed;
        
        public synchronized void saveAll () {
            cnt++;
            if (cnt > max) {
                max = cnt;
            }
            try {
                wait (500);
            } catch (Exception ex) {
                
            }
            executed++;
            notifyAll ();
            
            cnt--;
            assertFalse ("No AWT thread: ", javax.swing.SwingUtilities.isEventDispatchThread ());
        }

        public void exit () {
            fail ("Not supported");
        }
        
    }
}
