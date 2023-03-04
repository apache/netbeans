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
package org.netbeans.modules.autoupdate.ui;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class UtilitiesTest extends NbTestCase {

    private ConcurrentModificationException exp;

    public UtilitiesTest(String n) {
        super(n);
    }

    public void testConcurrentModificationException() throws InterruptedException {
        Runnable addingRunnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    List<String> accepted = new LinkedList<String>();
                    for (int j = 1; j < 300; j++) {
                        accepted.add("licence" + i + j);
                    }
                    System.out.println("Adding " + i);
                    Utilities.addAcceptedLicenseIDs(accepted);
                    try {
                        Thread.sleep(18);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        Thread addingThread = new Thread(addingRunnable);

        Runnable storingRunnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        Utilities.storeAcceptedLicenseIDs();
                        System.out.println("Storing " + i);
                        try {
                            Thread.sleep(22);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    } catch (ConcurrentModificationException ex) {
                        ex.printStackTrace();
                        exp = ex;
                    }
                }
            }
        };
        Thread storingThread = new Thread(storingRunnable);

        addingThread.start();
        storingThread.start();
        addingThread.join();
        storingThread.join();

        assertNull("ConcurrentModificationException thrown.", exp);
    }
}