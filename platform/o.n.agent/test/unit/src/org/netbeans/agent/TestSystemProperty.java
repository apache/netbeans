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
import org.netbeans.agent.hooks.TrackingHooks;

/**
 *
 * @author lahvac
 */
public class TestSystemProperty {
    public static void main(String... args) throws IOException {
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected void checkSystemProperty(String property) {
                System.err.println("checkSystemProperty: " + property);
            }
        }, 0, TrackingHooks.HOOK_PROPERTY);
        System.err.println("going to read property without default:");
        System.getProperty("property");
        System.err.println("going to read property with default:");
        System.getProperty("property", "");
        System.err.println("going to clear property:");
        System.clearProperty("property");
    }
}
