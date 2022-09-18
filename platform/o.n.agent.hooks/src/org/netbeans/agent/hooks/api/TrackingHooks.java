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
package org.netbeans.agent.hooks.api;

import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.lang.reflect.AccessibleObject;
import java.util.List;
import org.netbeans.agent.hooks.TrackingHooksCallback;
import org.netbeans.agent.hooks.TrackingHooksCallback.Accessor;

public abstract class TrackingHooks {

    protected void checkExit(int i) {}

    protected void checkFileWrite(String path) {}
    protected void checkFileRead(String path) {}
    protected void checkDelete(String path) {}

    protected void checkSystemProperty(String property) {}

    protected void checkSetAccessible(AccessibleObject what) {}

    protected void checkSetSecurityManager(Object what) {}

    protected void checkNewAWTWindow(Window w) {}

    protected void checkExec(List<String> command) {}

    protected Clipboard getClipboard() { return null; }

    public static void register(TrackingHooks delegate, int priority, Hooks... hooks) {
        TrackingHooksCallback.register(delegate, priority, hooks);
    }

    public static void unregister(TrackingHooks delegate) {
        TrackingHooksCallback.unregister(delegate);
    }

    public static boolean isInstalled() {
        return TrackingHooksCallback.isInstalled();
    }

    public enum Hooks {
        EXIT,
        IO,
        PROPERTY,
        ACCESSIBLE,
        NEW_AWT_WINDOW,
        SECURITY_MANAGER,
        EXEC,
        CLIPBOARD,
        ;
    }

    static {
        TrackingHooksCallback.setAccessor(new Accessor() {
            @Override
            public void checkExit(TrackingHooks hooks, int i) {
                hooks.checkExit(i);
            }

            @Override
            public void checkFileWrite(TrackingHooks hooks, String path) {
                hooks.checkFileWrite(path);
            }

            @Override
            public void checkFileRead(TrackingHooks hooks, String path) {
                hooks.checkFileRead(path);
            }

            @Override
            public void checkDelete(TrackingHooks hooks, String path) {
                hooks.checkDelete(path);
            }

            @Override
            public void checkSystemProperty(TrackingHooks hooks, String property) {
                hooks.checkSystemProperty(property);
            }

            @Override
            public void checkSetAccessible(TrackingHooks hooks, AccessibleObject what) {
                hooks.checkSetAccessible(what);
            }

            @Override
            public void checkSetSecurityManager(TrackingHooks hooks, Object what) {
                hooks.checkSetSecurityManager(what);
            }

            @Override
            public void checkNewAWTWindow(TrackingHooks hooks, Window w) {
                hooks.checkNewAWTWindow(w);
            }

            @Override
            public void checkExec(TrackingHooks hooks, List<String> command) {
                hooks.checkExec(command);
            }

            @Override
            public Clipboard getClipboard(TrackingHooks hooks) {
                return hooks.getClipboard();
            }
        });
    }
}
