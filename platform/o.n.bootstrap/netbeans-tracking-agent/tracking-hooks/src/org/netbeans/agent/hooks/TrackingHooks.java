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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 * @author lahvac
 */
public abstract class TrackingHooks {

    public static final String HOOK_EXIT = "exit";
    public static final String HOOK_NEW_AWT_WINDOW = "newAWTWindow";

    private static final Map<String, Set<HookDescription>> hook2Delegates = new HashMap<>();

    public static synchronized void register(TrackingHooks delegate, int priority, String... hooks) {
        for (String hook : hooks) {
            Set<HookDescription> existing = hook2Delegates.computeIfAbsent(hook, x -> new TreeSet<>((d1, d2) -> d1.priority - d2.priority));
            existing.add(new HookDescription(delegate, priority));
        }
    }

    private static synchronized Iterable<TrackingHooks> getDelegates(String hook) {
        return hook2Delegates.getOrDefault(hook, Collections.emptySet()).stream().map(d -> d.hooks).collect(Collectors.toList());
    }

    protected void checkExit(int i) {}

    public static void exitCallback(int i) {
        for (TrackingHooks h : getDelegates(HOOK_EXIT)) {
            h.checkExit(i);
        }
    }

    public static void newFileOutputStream(FileOutputStream out, File file) {
        System.err.println("new FileOutputStream(" + out + ", " + file + ")");
    }

    public static void fileOutputStreamClose(FileOutputStream out) {
        System.err.println("close(" + out + ")");
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
