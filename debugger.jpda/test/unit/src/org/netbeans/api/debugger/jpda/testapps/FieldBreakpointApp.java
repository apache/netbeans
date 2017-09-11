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
