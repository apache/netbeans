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
package org.netbeans.agent.hooks;

import java.awt.Window;
import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class TrackingHooks {

    public static final String HOOK_EXIT = "exit";
    public static final String HOOK_IO = "io";
    public static final String HOOK_PROPERTY = "property";
    public static final String HOOK_ACCESSIBLE = "accessible";
    public static final String HOOK_NEW_AWT_WINDOW = "newAWTWindow";
    public static final String HOOK_SECURITY_MANAGER = "securityManager";

    private static final Map<String, Set<HookDescription>> hook2Delegates = new HashMap<>();

    public static synchronized void register(TrackingHooks delegate, int priority, String... hooks) {
        for (String hook : hooks) {
            Set<HookDescription> existing = hook2Delegates.computeIfAbsent(hook, x -> new TreeSet<>((d1, d2) -> d1.priority - d2.priority));
            existing.add(new HookDescription(delegate, priority));
        }
    }

    private static synchronized Iterable<TrackingHooks> getDelegates(String hook) {
        //bootstrap issues, cannot use lambdas here:
        List<TrackingHooks> result = new ArrayList<>();

        for (HookDescription desc : hook2Delegates.getOrDefault(hook, Collections.emptySet())) {
            result.add(desc.hooks);
        }

        return result;
    }

    //for tests:
    public static synchronized void clear() {
        hook2Delegates.clear();
    }

    protected void checkExit(int i) {}

    public static void exitCallback(int i) {
        for (TrackingHooks h : getDelegates(HOOK_EXIT)) {
            h.checkExit(i);
        }
    }

    protected void checkFileWrite(String path) {}
    protected void checkFileRead(String path) {}
    protected void checkDelete(String path) {}

    public static void newFileOutputStream(File file) {
        newFileOutputStream(file.getPath());
    }

    public static void newFileOutputStream(Path path) {
        File file = path.toFile();
        if (file != null) {
            newFileOutputStream(file);
        }
    }

    public static void newFileOutputStream(String name) {
        for (TrackingHooks h : getDelegates(HOOK_IO)) {
            h.checkFileWrite(name);
        }
    }

    public static void newFileInputStream(File file) {
        newFileInputStream(file.getPath());
    }

    public static void newFileInputStream(Path path) {
        File file = path.toFile();
        if (file != null) {
            newFileInputStream(file);
        }
    }

    public static void newFileInputStream(String name) {
        for (TrackingHooks h : getDelegates(HOOK_IO)) {
            h.checkFileRead(name);
        }
    }

    public static void deleteFile(File file) {
        deleteFile(file.getPath());
    }

    public static void deleteFile(Path path) {
        File file = path.toFile();
        if (file != null) {
            deleteFile(file);
        }
    }

    private static void deleteFile(String name) {
        for (TrackingHooks h : getDelegates(HOOK_IO)) {
            h.checkDelete(name);
        }
    }

    protected void checkSystemProperty(String property) {}

    public static void systemProperty(String property) {
        for (TrackingHooks h : getDelegates(HOOK_PROPERTY)) {
            h.checkSystemProperty(property);
        }
    }

    protected void checkSetAccessible(AccessibleObject what) {}

    public static void setAccessible(AccessibleObject what) {
        for (TrackingHooks h : getDelegates(HOOK_ACCESSIBLE)) {
            h.checkSetAccessible(what);
        }
    }

    protected void checkSetSecurityManager(Object what) {}

    public static void setSecurityManager(Object what) {
        for (TrackingHooks h : getDelegates(HOOK_SECURITY_MANAGER)) {
            h.checkSetSecurityManager(what);
        }
    }

    protected void checkNewAWTWindow(Window w) {}

    public static void newAWTWindowCallback(Window w) {
        for (TrackingHooks h : getDelegates(HOOK_NEW_AWT_WINDOW)) {
            h.checkNewAWTWindow(w);
        }
    }

    private static final class HookDescription {
        private final TrackingHooks hooks;
        private final int priority;

        public HookDescription(TrackingHooks hooks, int priority) {
            this.hooks = hooks;
            this.priority = priority;
        }

    }
}
