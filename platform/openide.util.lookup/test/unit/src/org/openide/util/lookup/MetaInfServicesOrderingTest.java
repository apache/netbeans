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
package org.openide.util.lookup;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class MetaInfServicesOrderingTest extends NbTestCase {

        public MetaInfServicesOrderingTest(String name) {
            super(name);
        }

        public void testServicesOrdering() {
            //1st) Initialize Lookup Class Hierarchy Base<-Ext
            assertEquals(2, Lookup.getDefault().lookupAll(Ext.class).toArray().length);
            final Base[] res = Lookup.getDefault().lookupAll(Base.class).toArray(new Base[0]);
            assertEquals(4, res.length);
            assertEquals(Impl1.class, res[0].getClass());
            assertEquals(Impl2.class, res[1].getClass());
            assertEquals(Impl3.class, res[2].getClass());
            assertEquals(Impl4.class, res[3].getClass());
        }

        public abstract static class Base {
            abstract void op1();
        }

        public abstract static class Ext extends Base {
            abstract void op2();
        }

        @ServiceProvider(service = Base.class, position = 1)
        public static final class Impl1 extends Base {
            @Override
            void op1() {
            }
        }

        @ServiceProvider(service = Ext.class, position = 2)
        public static final class Impl2 extends Ext {
            @Override
            void op1() {
            }
            @Override
            void op2() {
            }
        }

        @ServiceProvider(service = Base.class, position = 3)
        public static final class Impl3 extends Base {
            @Override
            void op1() {
            }
        }

        @ServiceProvider(service = Ext.class, position = 4)
        public static final class Impl4 extends Ext {
            @Override
            void op1() {
            }
            @Override
            void op2() {
            }
        }
}
