/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.api.debugger.jpda.testapps;

import java.util.EventListener;
import javax.swing.SwingConstants;

/**
 *
 * @author Martin Entlicher
 */
public class JPDAClassTypeTestApp implements EventListener, SwingConstants {

    private JPDAClassTypeTestApp() {

    }

    public static void main(String[] args) {
        JPDAClassTypeTestApp app = new JPDAClassTypeTestApp();
        MultiImpl mimpl = new MultiImpl();
        MultiImpl mimpl1 = new MultiImplSubClass1();
        MultiImpl mimpl2 = new MultiImplSubClass2();
        MultiImpl mimpl3 = new MultiImplSubClass3();
        mimpl1 = new MultiImpl();
        mimpl2 = new MultiImpl();
        mimpl3 = new MultiImpl();
        testClasses(app, mimpl);
        int hc = mimpl1.hashCode() +
                 mimpl2.hashCode() +
                 mimpl3.hashCode();
    }

    private static int testClasses(Object o1, Object o2) {
        return o1.hashCode() + o2.hashCode();  // LBREAKPOINT
    }

    static class MultiImpl extends SuperImpl implements Runnable, Intrfc1, Intrfc2 {

        private final static Class multiIntrfc = getPreparedInterface();

        public static Class getPreparedInterface() {
            new Intrfc6() {
                @Override public void run() {}
            };
            return Intrfc6.class;
        }

        public MultiImpl() {
        }

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static abstract class SuperImpl implements Intrfc2, Intrfc3, Intrfc4 {

        public SuperImpl() {
        }
    }

    private static interface Intrfc1 {
    }

    private static interface Intrfc2 {
    }

    private static interface Intrfc3 extends Runnable {
    }

    private static interface Intrfc4 extends Intrfc1, Intrfc2 {
    }

    private static interface Intrfc5 extends Intrfc2, Intrfc3 {
    }

    private static interface Intrfc6 extends Runnable, Intrfc1, Intrfc4, Intrfc5 {
    }

    private static class MultiImplSubClass1 extends MultiImpl {
    }

    private static class MultiImplSubClass2 extends MultiImpl {
    }

    private static class MultiImplSubClass3 extends MultiImplSubClass1 {
    }
}
