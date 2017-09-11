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
package org.netbeans.spi.search;

import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static junit.framework.Assert.assertEquals;
import org.junit.Test;
import org.netbeans.api.search.provider.SearchInfo;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class SearchScopeDefinitionTest {

    public SearchScopeDefinitionTest() {
    }

    /**
     * Bug 233192 - java.util.ConcurrentModificationException at
     * java.util.ArrayList$Itr.checkForComodification.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testThreadSafe() throws InterruptedException {
        final Semaphore semaphoreAddExtraListener = new Semaphore(0);
        final Semaphore semaphoreContinueIterating = new Semaphore(0);
        final MySSDefinition mssd = new MySSDefinition();

        // Count notified listeners.
        final int[] notified = new int[1];

        // Add listeners. After the first listener is notified, a new listener
        // can be added from another thread. Then, the second listener can be
        // notified.
        for (int i = 0; i < 10; i++) {
            final int j = i;
            mssd.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    if (j == 0) {
                        // allow the new listener to be added
                        semaphoreAddExtraListener.release();
                    } else if (j == 1) {
                        try {
                            // wait until the new listener added
                            semaphoreContinueIterating.acquire();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    notified[0]++;
                }
            });
        }

        // Thead that adds a new listener while the listener list is being
        // iterated over.
        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // wait until listeners are iterated
                    semaphoreAddExtraListener.acquire();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                mssd.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                    }
                });
                // the iteration can continue
                semaphoreContinueIterating.release();
            }
        });

        t1.start();
        mssd.runNotifyListeners();

        t1.join();
        assertEquals("All listeners should be notified", 10, notified[0]);
    }

    /**
     * A dummy implementation of SearchScopeDefinition, that does nothing, but
     * provides access to inherited SearchScopeDefinition support for change
     * listeners.
     */
    private static final class MySSDefinition extends SearchScopeDefinition {

        @Override
        public String getTypeId() {
            return "MySSDefinition";
        }

        @Override
        public String getDisplayName() {
            return "My Search Scope Definition";
        }

        @Override
        public boolean isApplicable() {
            return true;
        }

        @Override
        public SearchInfo getSearchInfo() {
            return null;
        }

        @Override
        public int getPriority() {
            return 1000;
        }

        @Override
        public void clean() {
        }

        public void runNotifyListeners() {
            notifyListeners();
        }
    }
}