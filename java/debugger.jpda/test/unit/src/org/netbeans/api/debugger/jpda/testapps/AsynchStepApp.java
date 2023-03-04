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

package org.netbeans.api.debugger.jpda.testapps;

/**
 * Sample step application.
 *
 * @author Maros Sandor
 */
public class AsynchStepApp {

    public static void main(String[] args) {
        AsynchStepApp sa = new AsynchStepApp(); // LBREAKPOINT
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
    }

    public AsynchStepApp() {    // STOP Into1
    }                           // STOP Over1

    private int m1() {
        int im1 = 10;           // STOP Into2
        m2();                   // STOP Over2
        return im1;
    }

    private int m2() {
        int im2 = 20;           // STOP Into3
        m3();
        return im2;
    }

    private int m3() {
        int im3 = 30;
        return im3;
    }

    static int x = 20;

    private int longMethod() {
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException iex) {}
        return 0;
    }

}
