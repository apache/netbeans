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
