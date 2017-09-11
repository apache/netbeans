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

package org.openide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;
import junit.framework.AssertionFailedError;
import junit.framework.TestResult;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/** Basic skeleton for logging test case.
 *
 * @author  Jaroslav Tulach
 */
public abstract class LoggingTestCaseHid extends NbTestCase {
    static {
        MockServices.setServices(new Class[] {ErrManager.class});
    }

    protected LoggingTestCaseHid (String name) {
        super (name);
    }
    
    /** If execution fails we wrap the exception with 
     * new log message.
     */
    protected void runTest () throws Throwable {
        
        assertNotNull ("ErrManager has to be in lookup", Lookup.getDefault().lookup(ErrManager.class));
        
        ErrManager.clear(getName(), getLog());
        
        try {
            super.runTest ();
        } catch (AssertionFailedError ex) {
            AssertionFailedError ne = new AssertionFailedError (ex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (ex.getStackTrace ());
            throw ne;
        } catch (IOException iex) {//#66208
            IOException ne = new IOException (iex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (iex.getStackTrace ());
            throw ne;	    
	} finally {
            // do not write to log files anymore
            ErrManager.clear(getName(), System.err);
        }
    }
    
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
     * @param url the url to read the file from
     * @exception IOException thrown when there is problem reading the url
     */
    protected final void registerSwitches(URL url, int timeout) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        for (;;) {
            int ch = is.read ();
            if (ch == -1) break;
            os.write (ch);
        }
        os.close();
        is.close();
        
        registerSwitches(new String(os.toByteArray(), "utf-8"), timeout);
    }
    
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
    
     */
    protected final void registerSwitches(String order, int timeout) {
        ErrManager.timeout = timeout;
        
        LinkedList switches = new LinkedList();
        
        HashMap exprs = new HashMap();
        
        int pos = 0;
        for(;;) {
            int thr = order.indexOf("THREAD:", pos);
            if (thr == -1) {
                break;
            }
            int msg = order.indexOf("MSG:", thr);
            if (msg == -1) {
                fail("After THREAD: there must be MSG: " + order.substring(thr));
            }
            int end = order.indexOf("THREAD:", msg);
            if (end == -1) {
                end = order.length();
            }
            
            String thrName = order.substring(pos + 7, msg).trim();
            String msgText = order.substring(msg + 4, end).trim();
            
            Pattern p = (Pattern)exprs.get(msgText);
            if (p == null) {
                p = Pattern.compile(msgText);
                exprs.put(msgText, p);
            }
            
            Switch s = new Switch(thrName, p);
            switches.add(s);
            
            pos = end;
        }
        
        ErrManager.switches = switches;
    }

    //
    // Logging support
    //
    public static final class ErrManager extends ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        static java.io.PrintStream log = System.err;
        
        private String prefix;

        private static LinkedList switches;
        private static int timeout;
        /** maps names of threads to their instances*/
        private static java.util.Map threads = new java.util.HashMap();
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public ErrorManager getInstance (String name) {
            if (
                true
            ) {
                return new ErrManager ('[' + name + "] ");
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        public void log (int severity, String s) {
            StringBuffer oneMsg = new StringBuffer();
            if (prefix != null) {
                oneMsg.append(prefix);
            } else {
                oneMsg.append("[default] ");
            }
            oneMsg.append("THREAD:");
            oneMsg.append(Thread.currentThread().getName());
            oneMsg.append(" MSG:");
            oneMsg.append(s);


            messages.append(oneMsg.toString());
            messages.append ('\n');

            if (messages.length() > 40000) {
                messages.delete(0, 20000);
            }

            log.println(oneMsg.toString());
            
            if (switches != null) {
                boolean log = true;
                boolean expectingMsg = false;
                for(;;) {
                    synchronized (switches) {
                        if (switches.isEmpty()) {
                            return;
                        }


                        Switch w = (Switch)switches.getFirst();
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
                            w = (Switch)switches.getFirst();
                            if (w.matchesThread()) {
                                // next message is also from this thread, go on
                                return;
                            }
                            expectingMsg = true;
                            foundMatch = true;
                        } else {
                            // compute whether we shall wait or not
                            java.util.Iterator it = switches.iterator();
                            while (it.hasNext()) {
                                Switch check = (Switch)it.next();
                                if (check.matchesMessage(s)) {
                                    expectingMsg = true;
                                    break;
                                }
                            }                            
                        }

                        // make it other thread run
                        Thread t = (Thread)threads.get(w.name);
                        if (t != null) {
                            if (log) {
                                messages.append("t: " + threadName + " interrupts: " + t.getName() + "\n");
                            }
                            t.interrupt();
                        }
                        threads.put(threadName, Thread.currentThread());
                        
//                        
//                        if (log) {
//                            messages.append("t: " + Thread.currentThread().getName() + " log: " + s + " result: " + m + " for: " + w + "\n");
//                        }
                        if (!expectingMsg) {
                            return;
                        }

                        // clear any interrupt that happend before
                        Thread.interrupted();
                        try {
                            if (log) {
                                messages.append("t: " + threadName + " log: " + s + " waiting\n");
                            }
                            switches.wait(timeout);
                            if (log) {
                                messages.append("t: " + threadName + " log: " + s + " timeout\n");
                            }
                            return;
                        } catch (InterruptedException ex) {
                            // ok, we love to be interrupted => go on
                            if (log) {
                                messages.append("t: " + threadName + " log: " + s + " interrupted\n");
                            }
                            if (foundMatch) {
                                return;
                            }
                        }
                    }
                }
            }
        }
        
        public void notify (int severity, Throwable t) {
            log (severity, t.getMessage ());
        }
        
        public boolean isNotifiable (int severity) {
            return prefix != null;
        }
        
        public boolean isLoggable (int severity) {
            return prefix != null;
        }

        private static void clear(String n, PrintStream printStream) {
            ErrManager.log = printStream;
            ErrManager.messages.setLength(0);
            ErrManager.messages.append ("Starting test ");
            ErrManager.messages.append (n);
            ErrManager.messages.append ('\n');
            threads.clear();
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
