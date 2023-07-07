/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.agent;

import java.lang.reflect.AccessibleObject;
import org.netbeans.agent.hooks.api.TrackingHooks;
import org.netbeans.agent.hooks.api.TrackingHooks.Hooks;

/**
 *
 * @author lahvac
 */
public class TestSetAccessible {
    public static void main(String... args) throws Exception {
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected void checkSetAccessible(AccessibleObject what) {
                System.err.print("checkSetAccessible: ");
                System.err.println(what.getClass().getName());
            }
        }, 0, Hooks.ACCESSIBLE);
        Class<AccessibleTest> clazz = AccessibleTest.class;
        System.err.println("going to make a field accessible:");
        clazz.getDeclaredField("accessibleTest").setAccessible(true);
        System.err.println("going to make a constructor accessible:");
        clazz.getDeclaredConstructors()[0].setAccessible(true);
        System.err.println("going to make a method accessible:");
        clazz.getDeclaredMethod("accessibleTest").setAccessible(true);
    }

    private static final class AccessibleTest {
        private int accessibleTest = 0;
        private AccessibleTest() {
        }
        private void accessibleTest() {}
    }
}
