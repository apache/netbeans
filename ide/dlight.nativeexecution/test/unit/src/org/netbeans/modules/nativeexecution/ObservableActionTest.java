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
package org.netbeans.modules.nativeexecution;

import org.netbeans.modules.nativeexecution.support.ObservableAction;
import org.netbeans.modules.nativeexecution.support.ObservableActionListener;
import javax.swing.Action;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class ObservableActionTest {

    public ObservableActionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of invokeAndWait method, of class ObservableAction.
     */
    @Test
    public void doTest() {
        try {
            System.out.println("invokeAndWait"); // NOI18N
            ObservableAction<Integer> action = new ObservableAction<Integer>("My Action") { // NOI18N

                @Override
                protected Integer performAction() {
                    System.out.println("Performed!"); // NOI18N
                    return 10;
                }
            };
            action.addObservableActionListener(new ObservableActionListener<Integer>() {

                public void actionStarted(Action source) {
                    System.out.println("Started!"); // NOI18N
                }

                public void actionCompleted(Action source, Integer result) {
                    System.out.println("Finished ! " + result); // NOI18N
                }
            });

            Thread.sleep(100);
//            action.invokeAndWait();

            action.actionPerformed(null);
            Thread.sleep(100);
            action.actionPerformed(null);
            action.actionPerformed(null);
            action.actionPerformed(null);
            try {
//            Thread.sleep(100);
//            action.actionPerformed(null);
//            Thread.sleep(100);
//            assertEquals(new Integer(10), action.getLastResult());

//                assertEquals(new Integer(10), action.actionPerformed(null));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
