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
 * Sample line breakpoints application.
 *
 * @author Maros Sandor
 */
public class LineBreakpointApp {

    static int x = 20;                          // STOP staticx

    static {
        x += 30;                                // STOP staticx2   LBREAKPOINT
    }

    public static void main(String[] args) {
        LineBreakpointApp sa = new LineBreakpointApp();     // STOP M1
        x += sa.m1();                           // LBREAKPOINT

        int isq = InnerStatic.getQ();           //  STOP condition1
        InnerStatic is = new InnerStatic();     //  STOP condition2
        int isw = is.getW();                    // LBREAKPOINT
    }

    private int y = 20;

    {
        y += 10;
    }

    public LineBreakpointApp() {
        y += 100;                               // STOP C1
    }

    private int m1() {
        int im1 = 10;
        m2();
        Inner ic = new Inner();
        int iw = ic.getW();
        return im1;
    }

    private int m2() {
        int im2 = 20;
        m3();
        return im2;
    }

    private int m3() {
        int im3 = 30;
        return im3;
    }

    private static class InnerStatic {

        private static int q = 200;             // STOP IS1

        static {
            q += 40;                            // STOP IS2
        }

        private int w = 70;

        {
            w += 10;
        }

        public InnerStatic() {
            w += 100;
        }

        public static int getQ() {
            return q;                           // STOP IS3   LBREAKPOINT
        }

        public int getW() {
            return w;
        }
    }

    private class Inner {

        private int w = 70;                 // STOP I1

        {
            w += 10;                        // STOP I2
        }

        public Inner() {
            w += 100;                       // LBREAKPOINT
        }

        public int getW() {
            return w;                       // STOP I3
        }
    }

}
