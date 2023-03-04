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

import java.util.Random;

/**
 * Test app for breakpoints class filters.
 * Some breakpoints stop in this class only, some get filter class names,
 * that add BreakpointsClassFilterApp2 as well.
 * 
 * @author Martin Entlicher
 */
public class BreakpointsClassFilterApp {
    
    private double field = 111.111;
    private double field2 = 111.111;

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Thread t = new Thread(BreakpointsClassFilterApp2.class.getName()) {
            @Override public void run() {
                BreakpointsClassFilterApp2.main(args);
            }
        }; t.start();
        new BreakpointsClassFilterApp().test(t);
    }
    
    public double test(Thread t) {
        double d = 0;                                                   // LBREAKPOINT
        int n = new Random(Math.round(field)).nextInt();
        n = n - n;
        try {
            n += new Double[-10].length;
        } catch (NegativeArraySizeException nasex) {
        }
        d = d + field2 + Math.atan(n) + Math.PI + Math.E;                // LBREAKPOINT
        try {
            d += 1/n;
        } catch (ArithmeticException aex) {
        }
        try {
            t.join();
        } catch (InterruptedException ex) {
        }
        return d;
    }
}
