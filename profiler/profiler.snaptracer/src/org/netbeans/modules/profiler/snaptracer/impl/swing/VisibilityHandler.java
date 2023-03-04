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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.Component;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class VisibilityHandler {

    private Component component;
    private boolean wasVisible;

    private HierarchyListener listener;


    public VisibilityHandler() {}

    public abstract void shown();
    public abstract void hidden();


    public final void handle(Component component) {
        if (component == null)
            throw new NullPointerException("component cannot be null"); // NOI18N

        if (listener != null && component != null)
            component.removeHierarchyListener(listener);

        this.component = component;
        wasVisible = component.isVisible();

        if (listener == null) listener = createListener();
        component.addHierarchyListener(listener);
    }


    private HierarchyListener createListener() {
        return new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    boolean visible = component.isShowing();
                    if (wasVisible == visible) return;

                    wasVisible = visible;

                    if (visible) shown();
                    else hidden();
                }
            }
        };
    }


}
