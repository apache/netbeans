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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import org.junit.Assert;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public class ActionsInfraHid {//implements ContextGlobalProvider {

    /** Prop listener that will tell you if it gets a change.
     */
    public static final class WaitPCL implements PropertyChangeListener, Runnable {
        /** whether a change has been received, and if so count */
        public int gotit = 0;
        /** optional property name to filter by (if null, accept any) */
        private final String prop;
        public WaitPCL(String p) {
            prop = p;
        }
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            Assert.assertTrue("In AWT thread", EventQueue.isDispatchThread());
            
            if (prop == null || prop.equals(evt.getPropertyName())) {
                gotit++;
                notifyAll();
            }
        }
        public boolean changed() {
            synchronized (this) {
                if (gotit > 0) {
                    return true;
                }
            }
            
            if (!EventQueue.isDispatchThread()) {
                try {
                    EventQueue.invokeAndWait(this);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
            return gotit > 0;
        }
        
        public void run() {
        }
    }

}
