/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
