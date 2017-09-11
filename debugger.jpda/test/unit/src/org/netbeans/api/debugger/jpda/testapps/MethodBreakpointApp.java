/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    private static abstract class AbstractInner {
        
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
