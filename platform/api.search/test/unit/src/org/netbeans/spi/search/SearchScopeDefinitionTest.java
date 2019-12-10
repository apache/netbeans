/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.search;

import java.util.concurrent.Semaphore;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static org.junit.Assert.assertEquals;
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