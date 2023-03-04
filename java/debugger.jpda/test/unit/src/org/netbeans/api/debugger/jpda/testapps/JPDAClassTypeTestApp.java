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

        private static final Class multiIntrfc = getPreparedInterface();

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

    private abstract static class SuperImpl implements Intrfc2, Intrfc3, Intrfc4 {

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
