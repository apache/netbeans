/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.openide.explorer.view;

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 * This class checks that an object can be garbage collected, without affecting
 * event queue thread.
 * It's expected that the event queue thread have nothing to do with the GC process.
 * 
 * @author Martin Entlicher
 */
public class EQFriendlyGC {
    
    /**
     * Asserts that the object can be garbage collected.
     * It suspends event queue thread to assure that OutOfMemoryError is not
     * thrown in it instead of the current thread.
     * @param text the text to show when test fails.
     * @param ref the referent to object that should be GCed
     * @see NbTestCase#assertGC(java.lang.String, java.lang.ref.Reference)
     */
    static void assertGC(String text, Reference<?> ref) {
        final Object LOCK = new Object();
        blockEQ(LOCK);
        try {
            NbTestCase.assertGC(text, ref);
        } finally {
            // Resume EQ:
            synchronized (LOCK) {
                LOCK.notifyAll();
            }
        }
    }

    private static void blockEQ(final Object LOCK) {
        final AtomicBoolean eqStarted = new AtomicBoolean(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (eqStarted) {
                                eqStarted.set(true);
                                eqStarted.notifyAll();
                            }
                            synchronized (LOCK) {
                                try {
                                    LOCK.wait();
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }.start();
        synchronized (eqStarted) {
            while(!eqStarted.get()) {
                try {
                    eqStarted.wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
