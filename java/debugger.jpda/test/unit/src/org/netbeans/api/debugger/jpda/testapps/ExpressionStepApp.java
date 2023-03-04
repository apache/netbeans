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
 * @author Martin Entlicher
 */
public class ExpressionStepApp {

    public static void main(String[] args) {
        x += factorial(10);                     // LBREAKPOINT
        x += factorial(20) + factorial(30);
        x += factorial(40); x += factorial(50);
        ExpressionStepApp exs = new ExpressionStepApp();
        x = exs.m1(exs.m2((int) x));
        x = exs.m3(exs.m1(exs.m2((int) x)), exs.m1((int) x)).intValue();
        x = exs.m1(
                    exs.m2((int)x));
        System.out.println(x);
    }

    public ExpressionStepApp() {
    }
    
    public static long factorial(int n) {
        long f = 1;
        for (int i = 2; i <= n; i++) {
            f *= i;
        }
        return f;
    }
    
    private int m1(int x) {
        int im1 = 10;
        return im1*x;
    }

    private int m2(int x) {
        int im2 = 20;
        return im2*x;
    }

    private Integer m3(int x, int y) {
        int im3 = 30;
        return new Integer(im3 + x + y);
    }

    static long x = 20L;
}
