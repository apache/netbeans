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

package org.netbeans.modules.openide.loaders;

import java.util.logging.Level;

import java.awt.EventQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.openide.loaders.FolderInstance;
import org.openide.util.Mutex;
import org.openide.util.Task;

/** A special task designed to run in AWT thread.
 * It will fire itself immediately.
 */
public final class AWTTask extends org.openide.util.Task {
    static final LinkedBlockingQueue<AWTTask> PENDING = new LinkedBlockingQueue<AWTTask>();
    private static final EDT WAKE_UP = new EDT();
    private static final Runnable PROCESSOR = new Processor();

    private final Object id;
    private boolean executed;
    private final Logger LOG = Logger.getLogger("org.openide.awt.Toolbar");
    public AWTTask (Runnable r, FolderInstance id) {
        super (r);
        this.id = id;
        PENDING.add(this);
        Mutex.EVENT.readAccess (PROCESSOR);
    }

    @Override
    public void run () {
        if (!executed) {
            long l = System.currentTimeMillis();
            try {
                super.run ();
            } catch (ThreadDeath t) {
                throw t;
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "Error in AWT task", t); // NOI18N
            } finally {
                executed = true;
                long took = System.currentTimeMillis() - l;
                Level level = Level.FINER;
                if (took > 100) {
                    level = Level.FINE;
                }
                if (took > 500) {
                    level = Level.INFO;
                }
                if (took > 3000) {
                    level = Level.WARNING;
                }
                LOG.log(level, "Too long AWTTask: {0} ms for {1}", new Object[]{took, id}); // NOI18N
            }
        }
    }

    @Override
    public void waitFinished () {
        if (EventQueue.isDispatchThread ()) {
            if (PENDING.remove(this)) {
                run ();
            }
        } else {
            WAKE_UP.wakeUp();
            super.waitFinished ();
        }
    }

    @Override
    public boolean waitFinished(long milliseconds) throws InterruptedException {
        if (EventQueue.isDispatchThread()) {
            PENDING.remove(this);
            run();
            return true;
        } else {
            WAKE_UP.wakeUp();
            synchronized (this) {
                if (isFinished()) {
                    return true;
                }
                wait(milliseconds);
                return isFinished();
            }
        }
    }
    
    
    
    public static boolean waitFor(Task t) {
        assert EventQueue.isDispatchThread();
        if (!PENDING.isEmpty()) {
            PROCESSOR.run();
            return false;
        }
        Thread previous = null;
        try {
            previous = WAKE_UP.enter();
            if (!t.waitFinished(10000)) {
                flush();
                return false;
            }
        } catch (InterruptedException ex) {
            flush();
            return false;
        } finally {
            WAKE_UP.exit(previous);
        }
        return true;
    }

    static void flush() {
        PROCESSOR.run();
    }

    private static final class Processor implements Runnable {
        private static final Logger LOG = Logger.getLogger(Processor.class.getName());
        @Override
        public void run() {
            assert EventQueue.isDispatchThread();
            for(;;) {
                AWTTask t = PENDING.poll();
                if (t == null) {
                    LOG.log(Level.FINER, " processing finished");
                    return;
                }
                LOG.log(Level.FINER, " processing {0}", t);
                t.run();
            }
        }
    }

    /** Monitor that holds pointer to current AWT dispatch thread
     * and can wake it up, as soon as somebody starts to wait on AWTTask.
     */
    private static final class EDT {
        private Thread awt;
        
        public synchronized Thread enter() {
            assert EventQueue.isDispatchThread();
            Thread p = awt;
            awt = Thread.currentThread();
            return p;
        }
        
        public synchronized void exit(Thread previous) {
            assert EventQueue.isDispatchThread();
            assert awt == Thread.currentThread() : "awt = " + awt;
            awt = previous;
            // clean up interrupted status
            Thread.interrupted();
        }
        
        public synchronized void wakeUp() {
            if (awt != null) {
                awt.interrupt();
            }
        }
    }
}
