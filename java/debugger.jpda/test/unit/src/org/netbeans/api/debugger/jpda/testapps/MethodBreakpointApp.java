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
 * Sample method breakpoints application.
 *
 * @author Maros Sandor
 */
public class MethodBreakpointApp {

    public static void main(String[] args) {
        MethodBreakpointApp sa = new MethodBreakpointApp();
        sa.a();
        sa.b();
        sa.c();
        new InnerStatic().getW();
        new ConcreteInner().compute(); new ConcreteInner().getString();
    }
    static {
        System.currentTimeMillis();     // STOP cinit
    }

    public MethodBreakpointApp() {      // STOP init
        System.currentTimeMillis();
    }

    private void a() {
        b();                // STOP a
        c();
    }

    private void b() {
        c();                // STOP b
    }

    private void c() {
        Inner i = new Inner(); i.getW(); i.getW();                 // STOP c
    }

    private static class InnerStatic {

        private static int q = 0;       // STOP InnerStatic.cinit

        static {
            q ++;
        }

        private int w = 1;

        {
            w ++;
        }

        public InnerStatic() {
            w ++;
        }

        public static int getQ() {
            return q;
        }

        public int getW() {
            return w;   // STOP InnerStatic.getW
        }
    }

    private class Inner {

        private int w = 1;

        {
            w ++;
        }

        public Inner() {        // STOP Inner.init
            w ++;
        }

        public int getW() {
            return w;   // STOP Inner.getW
        }
    }
    
    private abstract static class AbstractInner {
        
        public abstract double compute();
        
    }
    
    private static interface InterfaceInner {
        
        String getString();
        
    }
    
    private static class ConcreteInner extends AbstractInner implements InterfaceInner {
        
        public double compute() {
            double num = Math.PI/2;
            return Math.round(Math.sin(num)*1000)/1000.0;   // STOP Rcompute
        }
        
        public String getString() {
            char[] chars = new char[] { 'H', 'e', 'l', 'l', 'o' };
            return new String(chars);                       // STOP RgetString
        }
        
    }

}
