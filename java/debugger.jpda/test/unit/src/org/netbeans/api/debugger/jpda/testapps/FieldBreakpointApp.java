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
 * Sample field breakpoints application.
 *
 * @author Maros Sandor
 */
public class FieldBreakpointApp {

    static int x = 1;

    static {
        x ++;
    }

    public static void main(String[] args) {
        FieldBreakpointApp sa = new FieldBreakpointApp();
        x ++;
        sa.m1();
        int isq = InnerStatic.getQ();
        InnerStatic is = new InnerStatic();
        int isw = is.getW();
    }

    private int y = 1;  // STOP FY1

    {
        y++;            // STOP FY2
    }

    public FieldBreakpointApp() {
        y++;            // STOP FY3
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

        private static int q = 1;

        static {
            q ++;
        }

        private int w = 1;  // STOP FW1

        {
            w ++;           // STOP FW2
        }

        public InnerStatic() {
            w ++;           // STOP FW3
        }

        public static int getQ() {
            return q;
        }

        public int getW() {
            return w;
        }
    }

    private class Inner {

        private int w = 1;  // STOP FIW1

        {
            w ++;           // STOP FIW2
        }

        public Inner() {
            w ++;           // STOP FIW3
        }

        public int getW() {
            return w;
        }
    }

}
