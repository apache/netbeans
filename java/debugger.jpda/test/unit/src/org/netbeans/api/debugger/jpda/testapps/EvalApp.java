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
 * Sample application used in expression evaluator unit tests.
 * DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class EvalApp {

    /* **************************************************************************************************
        The following code must stay where it is, on same line numbers, else all unit tests will fail.
    ************************************************************************************************** */
    public static void main(String[] args) {
        ix += 10;                       // LBREAKPOINT
        EvalApp app = new EvalApp();
        app.m1();
        app.m2();
    }

    public EvalApp() {
        m0();
    }

    private void m0() {
    }

    public int m2() {
        return 20;
    }

    private int m1() {
        return 5;
    }

    private float m3() {
        return 4.3f;
    }

    private static int      ix = 74;
    private static float    fx = 10.0f;
    private static double   dx = 10.0;
    private static boolean  bx = true;
    private static short    sx = 10;
    private static char     cix = 'a';
}
