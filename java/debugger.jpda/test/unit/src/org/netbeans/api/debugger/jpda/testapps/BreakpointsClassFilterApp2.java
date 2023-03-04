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
 * Some breakpoints get this in filter class names, causing pauses in here
 * even when submitted for BreakpointsClassFilterApp only.
 * 
 * @author Martin Entlicher
 */
public class BreakpointsClassFilterApp2 {
    
    private double field = 111.111;
    private double field2 = 111.111;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        // Keep the identical line numbers with BreakpointsClassFilterApp
        
        
        
        new BreakpointsClassFilterApp2().test();
    }
    
    public double test() {
        double d = 0;
        int n = new Random(Math.round(field)).nextInt();
        n = n - n;
        try {
            throw new IllegalStateException("TEST");
        } catch (IllegalStateException nasex) {
        }
        d = d + field2 + Math.atan(n) + Math.PI + Math.E;
        try {
            Double dbl = null; d += dbl;
        } catch (NullPointerException npex) {
        }
        return d;
    }
}
