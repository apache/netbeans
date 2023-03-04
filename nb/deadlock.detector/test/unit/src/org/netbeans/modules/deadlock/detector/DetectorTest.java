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
package org.netbeans.modules.deadlock.detector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;

/**
 *
 * @author David Strupl
 */
public class DetectorTest {

    private static Detector instance;
    
    public DetectorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        instance = new Detector();
        instance.start();
    }
    
    @AfterClass
    public static void tearDownClass() {
        if (instance != null) {
            instance.stop();
        }
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * If you invoke this test manually the functionality of the deadlock
     * detection and reporting can be tried. Please note that this test
     * invokes the reporting facility and so is not suitable for inclusion
     * in the automatic test suite.
     */
    @Test
    public void testDetectDeadlock() throws InterruptedException {
        final Object o1 = new Object();
        final Object o2 = new Object();
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                synchronized (o1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    synchronized (o2) {
                        System.out.println("T1 ok!");
                    }
                }
            }
        };
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                synchronized (o2) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    synchronized (o1) {
                        System.out.println("T2 ok!");
                    }
                }
            }
        };
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        t1.start();
        t2.start();
        Thread.sleep(5000);
        t1.stop();
        t2.stop();
        Thread.sleep(15000);
    }
}