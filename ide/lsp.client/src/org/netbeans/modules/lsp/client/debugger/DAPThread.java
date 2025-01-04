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
package org.netbeans.modules.lsp.client.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.openide.text.Line;


public final class DAPThread implements DVThread {

    public static final String PROP_STACK = "stack";
    public static final String PROP_CURRENT_FRAME = "currentFrame";

    public enum Status {
        CREATED,
        RUNNING,
        SUSPENDED,
        EXITED
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final DAPDebugger debugger;
    private final int id;
    private final AtomicReference<DAPFrame> currentFrame = new AtomicReference<>();
    private final AtomicReference<DAPFrame[]> currentStack = new AtomicReference<>();
    private Status status;
    private String name;

    public DAPThread(DAPDebugger debugger, int id) {
        this.debugger = debugger;
        this.id = id;
        this.name = "Thread #" + id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isSuspended() {
        return status == Status.SUSPENDED;
    }

    @Override
    public void resume() {
        //TODO
    }

    @Override
    public void suspend() {
        //TODO
    }

    @Override
    public void makeCurrent() {
        //TODO
    }

    @Override
    public DVSupport getDVSupport() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<DVThread> getLockerThreads() {
        return null; //TODO
    }

    @Override
    public void resumeBlockingThreads() {
        //TODO
    }

    @Override
    public Breakpoint getCurrentBreakpoint() {
        return null; //TODO
    }

    @Override
    public boolean isInStep() {
        return false; //TODO
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    public void setStack(DAPFrame[] frames) {
        currentStack.set(frames);
        if (frames.length > 0) {
            currentFrame.set(frames[0]);
        } else {
            currentFrame.set(null);
        }

        refreshAnnotations();

        pcs.firePropertyChange(PROP_STACK, null, frames);
        pcs.firePropertyChange(PROP_CURRENT_FRAME, null, getCurrentFrame());
    }

    public DAPFrame[] getStack() {
        return currentStack.get();
    }

    public void setStatus(Status status) {
        boolean wasSuspended = isSuspended();

        this.status = status;

        boolean isSuspended = isSuspended();

        if (wasSuspended != isSuspended) {
            pcs.firePropertyChange(PROP_SUSPENDED, wasSuspended, isSuspended);
        }
    }

    public Status getStatus() {
        return status;
    }

    public String getDetails() {
        return null;
    }

    public DAPFrame getCurrentFrame() {
        return currentFrame.get();
    }

    public void setCurrentFrame(DAPFrame frame) {
        currentFrame.set(frame);
        refreshAnnotations();
        pcs.firePropertyChange(PROP_CURRENT_FRAME, null, getCurrentFrame());
    }

    public Line getCurrentLine() {
        DAPFrame frame = currentFrame.get();

        return frame != null ? frame.location() : null;
    }

    private void refreshAnnotations() {
        DAPFrame[] frames = getStack();

        if (frames.length == 0) {
            DAPUtils.unmarkCurrent();
            return ;
        }

        Line currentLine = getCurrentLine();
        List<Line> stack = new ArrayList<>();

        if (currentLine != null) {
            stack.add(currentLine);
        }

        Arrays.stream(frames)
              .map(f -> f.location())
              .filter(l -> l != null) //TODO
              .filter(l -> l != currentLine)
              .forEach(stack::add);

        DAPUtils.markCurrent(stack.toArray(Line[]::new));
    }

}
