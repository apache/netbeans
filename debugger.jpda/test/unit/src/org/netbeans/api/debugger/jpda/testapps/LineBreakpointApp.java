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
