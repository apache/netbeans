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
package org.netbeans.modules.performance.guitracker;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;

import java.lang.reflect.Method;

import java.util.Stack;

/**
 *
 * @author Tim Boudreau
 */
public class LoggingEventQueue extends EventQueue {

    private static Method popMethod = null;

    static {
        try {
            popMethod = EventQueue.class.getDeclaredMethod("pop", (Class<?>[]) null);
            popMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private ActionTracker tr;

    private EventQueue orig = null;

    /**
     * Creates a new instance of LoggingEventQueue
     *
     * @param tr tracker
     */
    public LoggingEventQueue(ActionTracker tr) {
        this.tr = tr;
    }

    @Override
    public void postEvent(AWTEvent e) {
        tr.add(e);
        super.postEvent(e);
    }

    public boolean isEnabled() {
        return orig != null;
    }

    public void setEnabled(boolean val) {
        if (isEnabled() != val) {
            if (val) {
                enable();
            } else {
                disable();
            }
        }
    }

    private void enable() {
        if (!isEnabled()) {
            orig = Toolkit.getDefaultToolkit().getSystemEventQueue();
            orig.push(this);
            System.err.println("Installed logging event queue"); // XXX use logger?
        }
    }

    private void disable() {
        try {
            if (isEnabled()) {
                Stack<EventQueue> stack = new Stack<EventQueue>();
                EventQueue curr = Toolkit.getDefaultToolkit().getSystemEventQueue();
                while (curr != this) {
                    curr = popQ();
                    if (curr != this) {
                        stack.push(curr);
                    }
                }
                pop();
                curr = orig;
                assert Toolkit.getDefaultToolkit().getSystemEventQueue() == orig;
                while (!stack.isEmpty()) {
                    EventQueue next = stack.pop();
                    curr.push(next);
                    curr = next;
                }
                System.err.println("Uninstalled logging event queue"); // use logger?
            }
        } finally {
            orig = null;
        }
    }

    @Override
    public synchronized void push(EventQueue newEventQueue) {
    }

    private EventQueue popQ() {
        try {
            if (popMethod == null) {
                throw new IllegalStateException("Can't access EventQueue.pop");
            }
            EventQueue result = Toolkit.getDefaultToolkit().getSystemEventQueue();
            popMethod.invoke(result, new Object[]{});
            return result;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new IllegalStateException("Can't invoke EventQueue.pop");
            }
        }
    }
}
