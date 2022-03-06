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

import java.io.IOException;
import org.netbeans.agent.hooks.api.TrackingHooks;
import org.netbeans.agent.hooks.api.TrackingHooks.Hooks;

/**
 *
 * @author lahvac
 */
public class TestSetSecurityManager {
    public static void main(String... args) throws IOException {
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected void checkSetSecurityManager(Object what) {
                System.err.println("setSecurityManager: " + what);
                throw new SecurityException();
            }
        }, 0, Hooks.SECURITY_MANAGER);
        System.err.println("going to set SecurityManager:");
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public String toString() {
                    return "testing SecurityManager";
                }
            });
        } catch (SecurityException ex) {
            System.err.println("got SecurityException.");
        }
    }
}
