/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.openide.loaders;

import java.awt.EventQueue;
import java.util.concurrent.Semaphore;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AWTTaskTest extends NbTestCase {

    public AWTTaskTest(String name) {
        super(name);
    }
    
    public void testWaitWithTimeOut() throws InterruptedException {
        class Block implements Runnable {
            Task toWait;
            
            @Override
            public synchronized void run() {
                while (toWait == null) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                try {
                    assertTrue("Finished", toWait.waitFinished(300));
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                toWait = null;
                notifyAll();
            }
            
            public synchronized void goOn(Task toWait) throws InterruptedException {
                this.toWait = toWait;
                notifyAll();
                while (this.toWait != null) {
                    wait();
                }
            }
        }
        Block b = new Block();
        EventQueue.invokeLater(b);
        
        class R implements Runnable {
            int run;

            @Override
            public void run() {
                run++;
            }
        }
        R run = new R();
        AWTTask at = new AWTTask(run, null);
        assertFalse("Does not finish", at.waitFinished(1000));
        b.goOn(at);
        assertEquals("Executed once", 1, run.run);
    }
    
    public void testWaitForItself() {
        final Semaphore s = new Semaphore(0);
        class R implements Runnable {
            int cnt;
            volatile Task waitFor;

            @Override
            public void run() {
                cnt++;
                try {
                    s.acquire(); // Ensure waitFor != null.
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                waitFor.waitFinished();
            }
        }
        
        R r = new R();
        r.waitFor = new AWTTask(r, null);
        s.release();
        r.waitFor.waitFinished();
        
        assertEquals("Executed once", 1, r.cnt);
    }
    
    public void testInvokedOnce() {
        assertInvokedOnce(false);
    }
    public void testInvokedOnceWithTimeOut() {
        assertInvokedOnce(true);
    }
    
    private void assertInvokedOnce(final boolean withTimeOut) {
        class Cnt implements Runnable {
            int cnt;

            @Override
            public void run() {
                assertTrue("In AWT", EventQueue.isDispatchThread());
                cnt++;
                AWTTask.flush();
            }
        }
        class CntAndWait implements Runnable {
            Cnt snd;
            int cnt;
            
            @Override
            public void run() {
                snd = new Cnt();
                final AWTTask[] waitFor = { null };
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        waitFor[0] = new AWTTask(snd, null);
                    }
                }).waitFinished();
                
                if (withTimeOut) {
                    try {
                        waitFor[0].waitFinished(1000);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                } else {
                    waitFor[0].waitFinished();
                }
                cnt++;
                assertEquals("Already invoked", 1, snd.cnt);
            }
        }
        CntAndWait first = new CntAndWait();
        new AWTTask(first, null).waitFinished();
        assertEquals("Main invoked", 1, first.cnt);
        Cnt third = new Cnt();
        new AWTTask(third, null).waitFinished();
        assertEquals("Invoked once 3rd", 1, third.cnt);
        assertEquals("Invoked once inner", 1, first.snd.cnt);
    }
}
