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
import java.io.RandomAccessFile;
import java.lang.reflect.AccessibleObject;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TrackingHooks {

    private static final Logger LOG = Logger.getLogger(TrackingHooks.class.getName());
    public static final String HOOK_EXIT = "exit";
    public static final String HOOK_IO = "io";
    public static final String HOOK_PROPERTY = "property";
    public static final String HOOK_ACCESSIBLE = "accessible";
    public static final String HOOK_NEW_AWT_WINDOW = "newAWTWindow";
    public static final String HOOK_SECURITY_MANAGER = "securityManager";
    public static final String HOOK_EXEC = "exec";

    private static final Map<String, Set<HookDescription>> hook2Delegates = new HashMap<>();

    public static synchronized void register(TrackingHooks delegate, int priority, String... hooks) {
        if (hook2Delegates.isEmpty() && hooks.length != 0) {
            try {
                Class<?> agent = Class.forName("org.netbeans.agent.TrackingAgent", false, ClassLoader.getSystemClassLoader());
                agent.getDeclaredMethod("install").invoke(null);
            } catch (ReflectiveOperationException ex) {
                LOG.log(Level.WARNING, "Cannot associate tracking hooks, the application will be unstable"); // NOI18N
                LOG.log(Level.INFO, "Cannot associate tracking hooks, the application will be unstable", ex); // NOI18N
            }
        }
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

    public static synchronized void clear() {
        hook2Delegates.clear();
    }

    public static synchronized void unregister(TrackingHooks toRemove) {
        for (Set<HookDescription> delegates : hook2Delegates.values()) {
            for (Iterator<HookDescription> it = delegates.iterator(); it.hasNext();) {
                HookDescription hd = it.next();
                if (hd.hooks == toRemove) {
                    it.remove();
                }
            }
        }
    }

    public static synchronized  boolean isInstalled() {
        return !hook2Delegates.isEmpty();
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

    public static void write(File file) {
        write(file.getAbsolutePath());
    }

    public static void write(Path path) {
        if (path.getFileSystem() == FileSystems.getDefault()) {
            write(path.toFile());
        }
    }

    public static void write(String name) {
        for (TrackingHooks h : getDelegates(HOOK_IO)) {
            h.checkFileWrite(name);
        }
    }

    public static void read(File file) {
        read(file.getAbsolutePath());
    }

    public static void read(Path path) {
        if (path.getFileSystem() == FileSystems.getDefault()) {
            read(path.toFile());
        }
    }

    public static void read(String name) {
        for (TrackingHooks h : getDelegates(HOOK_IO)) {
            h.checkFileRead(name);
        }
    }

    public static void readWrite(File file, String mode) {
        if (mode.contains("r")) {
            read(file.getAbsolutePath());
        }
        if (mode.contains("w")) {
            write(file.getAbsolutePath());
        }
    }

    public static void readWrite(Path path, Set<OpenOption> options) {
        if (path.getFileSystem() == FileSystems.getDefault()) {
            File file = path.toFile();
            if (options.isEmpty()) {
                read(file);
            } else if (options.contains(StandardOpenOption.READ)) {
                read(file);
                if (options.size() != 1) {
                    write(file);
                }
            } else {
                write(file);
            }
        }
    }

    public static void deleteFile(File file) {
        deleteFile(file.getAbsolutePath());
    }

    public static void deleteFile(Path path) {
        if (path.getFileSystem() == FileSystems.getDefault()) {
            deleteFile(path.toFile());
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

    protected void checkExec(List<String> command) {}

    public static void processBuilderStart(ProcessBuilder builder) {
        List<String> command = null;
        for (TrackingHooks h : getDelegates(HOOK_EXEC)) {
            if (command == null) {
                command = Collections.unmodifiableList(new ArrayList<>(builder.command()));
            }
            h.checkExec(command);
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
