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
package org.netbeans.modules.cnd.makeproject.ui;

import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;

/**
 *
 */
final class BrokenViewItemRefreshSupport {

    private static final BrokenViewItemRefreshSupport inst = new BrokenViewItemRefreshSupport();
    private final Set<BrokenViewItemListener> listeners = new HashSet<>();

    private BrokenViewItemRefreshSupport() {
    }

    public static void addBrokenViewItemListener(BrokenViewItemListener listener) {
        synchronized (inst) {
            inst.listeners.add(listener);
        }
    }

    public static void removeBrokenViewItemListener(BrokenViewItemListener listener) {
        synchronized (inst) {
            inst.listeners.remove(listener);
        }
    }

    static void refreshBrokenItems(Project project) {
        BrokenViewItemListener[] copy;
        synchronized (inst) {
            copy = inst.listeners.toArray(new BrokenViewItemListener[inst.listeners.size()]);
        }
        for (BrokenViewItemListener listener : copy) {
            listener.revalidate(project);
        }
    }

    public interface BrokenViewItemListener extends EventListener {

        public void revalidate(Project project);
    }
}
