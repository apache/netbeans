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
 * A sample application where we combine stepping and breakpoints with continue.
 *
 * @author Martin Entlicher
 */
public class StepAndContinueApp {

    public static void main(String[] args) {
        StepAndContinueApp sa = new StepAndContinueApp();
        int x = sa.m1();        // STOP Over4
        x += sa.m2();           // STOP Over5
        x += sa.m3();           // STOP Out7
    }

    private int m1() {
        int im1 = 10;           // LBREAKPOINT
        m2();                   // STOP Over1
        m3();                   // STOP Over2
        return im1;             // STOP Over3
    }

    private int m2() {
        int im2 = 20;           // STOP Into6
        m3();
        return im2;
    }

    private int m3() {
        int im3 = 30;           // STOP Into8
        return im3;             // LBREAKPOINT
    }

}
