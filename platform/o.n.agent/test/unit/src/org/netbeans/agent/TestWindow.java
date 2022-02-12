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

import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import org.netbeans.agent.hooks.TrackingHooks;

/**
 *
 * @author lahvac
 */
public class TestWindow {
    public static void main(String... args) throws IOException {
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected void checkNewAWTWindow(Window w) {
                System.err.println("checkNewAWTWindow");
            }
        }, 0, TrackingHooks.HOOK_NEW_AWT_WINDOW);
        System.err.println("going to create new Window(Frame):");
        new Window((Frame) null);
        System.err.println("going to create new Window(Window):");
        new Window((Window) null);
        System.err.println("going to create new Window(Window, GraphicsConfiguration):");
        new Window(null, null);
    }
}
