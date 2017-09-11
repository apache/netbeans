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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.debugger.jpda.testapps;

/**
 *
 * @author Martin Entlicher
 */
public class MonitorAndDeadlockApp {
    
    private final Object lock1 = new String("Lock1");
    private final Object lock2 = new String("Lock2");
    
    /** Creates a new instance of HeapWalkApp */
    public MonitorAndDeadlockApp() {
    }
    
    private boolean takeLock1() {
        synchronized (lock1) {
            try {
                lock1.wait();
            } catch (InterruptedException iex) {
                return false;
            }
        }
        return true;
    }
    
    private void releaseLock1() {
        synchronized (lock1) {
            lock1.notify();
        }
    }
    
    private boolean takeLock2() {
        synchronized (lock2) {
            try {
                lock2.wait();
            } catch (InterruptedException iex) {
                return false;
            }
        }
        return true;
    }
    
    private void releaseLock2() {
        synchronized (lock2) {
            lock2.notify();
        }
    }
    
    private void acquireLocks() {
        "Before acquire".length();                  // LBREAKPOINT
        synchronized (lock1) {
            "I'm synchronized".toString();          // LBREAKPOINT
            synchronized (lock2) {
                "I'm double synchronized".toString();// LBREAKPOINT
            }
            "I've just lock 1 again".toCharArray(); // LBREAKPOINT
        }
        return ;                                    // LBREAKPOINT
    }
    
    private void createDeadlock() {
        new Thread("Deadlock1") {
            public void run() {
                synchronized (lock1) {
                //takeLock1();
                    try { Thread.sleep(1000); } catch (InterruptedException iex) {}
                    takeLock2();
                }
            }
        }.start();
        new Thread("Deadlock2") {
            public void run() {
                synchronized (lock2) {
                //takeLock2();
                    try { Thread.sleep(1000); } catch (InterruptedException iex) {}
                    takeLock1();
                }
            }
        }.start();
    }
    
    public static void main(String[] args) {
        MonitorAndDeadlockApp app = new MonitorAndDeadlockApp();
        app.acquireLocks();
        if (args.length > 0 && args[0].equals("deadlock")) {
            app.createDeadlock();   // LBREAKPOINT
        }
    }
    
}
