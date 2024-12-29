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

package org.netbeans.modules.lsp.client.debugger.models;

import java.beans.PropertyChangeListener;
import java.util.function.Supplier;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.lsp.client.debugger.DAPDebugger;
import org.netbeans.modules.lsp.client.debugger.DAPFrame;
import org.netbeans.modules.lsp.client.debugger.DAPThread;

import org.netbeans.spi.debugger.ContextProvider;

import org.openide.util.WeakListeners;

public class CurrentFrameTracker {

    protected final DAPDebugger       debugger;
    private final ChangeListener        threadListener;
    private final PropertyChangeListener frameListener;
    private volatile DAPThread          currentThread;
    private volatile DAPFrame           currentFrame;


    public CurrentFrameTracker (ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, DAPDebugger.class);
        currentThread = debugger.getCurrentThread();
        Supplier<DAPFrame> getCurrentThreadFrame = () -> {
            DAPThread cachedCurrentThread = currentThread;
            return cachedCurrentThread != null ? cachedCurrentThread.getCurrentFrame()
                                               : null;
        };
        currentFrame = getCurrentThreadFrame.get();

        Runnable frameChanged = () -> {
            DAPFrame prevFrame = currentFrame;
            DAPFrame newFrame = getCurrentThreadFrame.get();

            if (prevFrame != newFrame) {
                currentFrame = newFrame;
                frameChanged();
            }
        };
        frameListener = evt -> {
            if (evt.getPropertyName() == null ||
                DAPThread.PROP_CURRENT_FRAME.equals(evt.getPropertyName())) {
                frameChanged.run();
            }
        };
        threadListener = evt -> {
            DAPThread prevThread;
            DAPThread newThread;
            boolean changed;

            synchronized (this) {
                prevThread = currentThread;
                newThread = debugger.getCurrentThread();

                if (changed = (prevThread != newThread)) {
                    currentThread = newThread;
                    if (prevThread != null) {
                        prevThread.removePropertyChangeListener(frameListener);
                    }
                    if (newThread != null) {
                        newThread.addPropertyChangeListener(frameListener);
                    }
                }
            }
            if (changed) {
                frameChanged.run();
            }
        };
        debugger.addChangeListener(WeakListeners.change(threadListener, debugger));
    }

    protected final DAPFrame getCurrentFrame() {
        return currentFrame;
    }

    protected void frameChanged() {}
}
