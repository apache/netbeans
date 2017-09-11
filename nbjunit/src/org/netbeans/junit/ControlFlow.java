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

package org.netbeans.junit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import junit.framework.Assert;

/** Basic skeleton for logging test case.
 *
 * @author  Jaroslav Tulach
 */
final class ControlFlow extends Object {
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
     * @param url the url to read the file from
     * @exception IOException thrown when there is problem reading the url
     */
    static void registerSwitches(Logger listenTo, Logger reportTo, URL url, int timeout) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        for (;;) {
            int ch = is.read ();
            if (ch == -1) break;
            os.write (ch);
        }
        os.close();
        is.close();
        
        registerSwitches(listenTo, reportTo, new String(os.toByteArray(), "utf-8"), timeout);
    }
    
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
    
     */
    static void registerSwitches(Logger listenTo, Logger reportTo, String order, int timeout) {
        LinkedList<Switch> switches = new LinkedList<Switch>();
        
        Map<String,Pattern> exprs = new HashMap<String,Pattern>();
        
        int pos = 0;
        for(;;) {
            int thr = order.indexOf("THREAD:", pos);
            if (thr == -1) {
                break;
            }
            int msg = order.indexOf("MSG:", thr);
            if (msg == -1) {
                Assert.fail("After THREAD: there must be MSG: " + order.substring(thr));
            }
            int end = order.indexOf("THREAD:", msg);
            if (end == -1) {
                end = order.length();
            }
            
            String thrName = order.substring(pos + 7, msg).trim();
            String msgText = order.substring(msg + 4, end).trim();
            
            Pattern p = exprs.get(msgText);
            if (p == null) {
                p = Pattern.compile(msgText);
                exprs.put(msgText, p);
            }
            
            Switch s = new Switch(thrName, p);
            switches.add(s);
            
            pos = end;
        }

        ErrManager h = new ErrManager(switches, listenTo, reportTo, timeout);
        listenTo.addHandler(h);
    }

    //
    // Logging support
    //
    private static final class ErrManager extends Handler {
        private LinkedList<Switch> switches;
        private int timeout;
        /** maps names of threads to their instances*/
        private Map<String,Thread> threads = new HashMap<String,Thread>();

        /** the logger to send internal messages to, if any */
        private Logger msg;


        /** assigned to */
        private Logger assigned;

        public ErrManager(LinkedList<Switch> switches, Logger assigned, Logger msg, int t) {
            this.switches = switches;
            this.msg = msg;
            this.timeout = t;
            this.assigned = assigned;
            setLevel(Level.FINEST);
        }
        
        public void publish (LogRecord record) {
            if (switches == null) {
                assigned.removeHandler(this);
                return;
            }


            String s = record.getMessage();
            if (s != null && record.getParameters() != null) {
                s = MessageFormat.format(s, record.getParameters());
            }

            boolean log = msg != null;
            boolean expectingMsg = false;
            for(;;) {
                synchronized (switches) {
                    if (switches.isEmpty()) {
                        return;
                    }


                    Switch w = switches.getFirst();
                    String threadName = Thread.currentThread().getName();
                    boolean foundMatch = false;

                    if (w.matchesThread()) {
                        if (!w.matchesMessage(s)) {
                            // same thread but wrong message => go on
                            return;
                        }
                        // the correct message from the right thread found
                        switches.removeFirst();
                        if (switches.isEmpty()) {
                            // end of sample, make all run
                            switches.notifyAll();
                            return;
                        }
                        w = switches.getFirst();
                        if (w.matchesThread()) {
                            // next message is also from this thread, go on
                            return;
                        }
                        expectingMsg = true;
                        foundMatch = true;
                    } else {
                        // compute whether we shall wait or not
                        for (Switch check : switches) {
                            if (check.matchesMessage(s)) {
                                expectingMsg = true;
                                break;
                            }
                        }                            
                    }

                    // make it other thread run
                    Thread t = threads.get(w.name);
                    if (t != null) {
                        if (log) {
                            loginternal("t: " + threadName + " interrupts: " + t.getName());
                        }
                        t.interrupt();
                    }
                    threads.put(threadName, Thread.currentThread());

//                        
//                        if (log) {
//                            loginternal("t: " + Thread.currentThread().getName() + " log: " + s + " result: " + m + " for: " + w + "\n");
//                        }
                    if (!expectingMsg) {
                        return;
                    }

                    // clear any interrupt that happend before
                    Thread.interrupted();
                    try {
                        if (log) {
                            loginternal("t: " + threadName + " log: " + s + " waiting");
                        }
                        switches.wait(timeout);
                        if (log) {
                            loginternal("t: " + threadName + " log: " + s + " timeout");
                        }
                        return;
                    } catch (InterruptedException ex) {
                        // ok, we love to be interrupted => go on
                        if (log) {
                            loginternal("t: " + threadName + " log: " + s + " interrupted");
                        }
                        if (foundMatch) {
                            return;
                        }
                    }
                }
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }

        private void loginternal(String string) {
            msg.info(string);
        }
        
    } // end of ErrManager
    
    private static final class Switch {
        private Pattern msg;
        private String name;
        
        public Switch(String n, Pattern m) {
            this.name = n;
            this.msg = m;
        }
        
        /** @return true if the thread name of the caller matches this switch
         */
        public boolean matchesThread() {
            String thr = Thread.currentThread().getName();
            return name.equals(thr);
        }
        
        /** @return true if the message matches the one provided by this switch
         */
        public boolean matchesMessage(String logMsg) {
            return msg.matcher(logMsg).matches();
        }
        
        public String toString() {
            return "Switch[" + name + "]: " + msg;
        }
    }
}
