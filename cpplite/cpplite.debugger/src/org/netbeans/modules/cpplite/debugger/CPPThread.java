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
package org.netbeans.modules.cpplite.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.cpplite.debugger.debuggingview.DebuggingViewSupportImpl;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;

import org.openide.util.Pair;

public final class CPPThread implements DVThread {

    public enum Status {
        CREATED,
        RUNNING,
        SUSPENDED,
        EXITED
    }

    private final CPPLiteDebugger debugger;
    private final String id;
    private volatile Pair<String, String> nameDetails;
    private Status status;
    private volatile CPPFrame topFrame;
    private CPPFrame[] stack;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    CPPThread(CPPLiteDebugger debugger, String id) {
        this.debugger = debugger;
        this.id = id;
        this.status = Status.CREATED;
    }

    String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    void setTopFrame(CPPFrame frame) {
        topFrame = frame;
    }

    @CheckForNull
    public CPPFrame getTopFrame() {
        return topFrame;
    }

    @Override
    public String getName() {
        return getNameDetails().first();
    }

    @CheckForNull
    public String getDetails() {
        return getNameDetails().second();
    }

    private Pair<String, String> getNameDetails() {
        Pair<String, String> nd = this.nameDetails;
        if (nd == null) {
            synchronized (this) {
                nd = this.nameDetails;
                if (nd == null) {
                    if (status == Status.CREATED) {
                        return Pair.of(id, null);
                    }
                    MIRecord record;
                    try {
                        record = debugger.sendAndGet("-thread-info " + id, true);
                    } catch (InterruptedException ex) {
                        return null;
                    }
                    if (record.isError() || record.isEmpty()) {
                        this.nameDetails = nd = Pair.of(record.error(), null);
                    } else {
                        MITList infoList = (MITList) record.results().valueOf("threads").asList().get(0);
                        MIValue nameValue = infoList.valueOf("name");
                        if (nameValue == null) {
                            nameValue = infoList.valueOf("target-id");
                        }
                        MIValue detailsValue = infoList.valueOf("details");
                        String name = nameValue.asConst().value();
                        String details = detailsValue != null ? detailsValue.asConst().value() : null;
                        this.nameDetails = nd = Pair.of(name, details);
                    }
                }
            }
        }
        return nd;
    }

    @Override
    public boolean isSuspended() {
        return Status.SUSPENDED == status;
    }

    @Override
    public void resume() {
        if (isSuspended()) {
            notifyRunning();
            debugger.send(new Command("-exec-continue --thread " + id));
        }
    }

    @Override
    public void suspend() {
        if (!isSuspended()) {
            debugger.send(new Command("-exec-interrupt --thread " + id));
        }
    }

    @Override
    public void makeCurrent() {
        debugger.setCurrentThread(this);
    }

    @Override
    public int getFrameCount() {
        CPPFrame[] stack = getStack();
        if (stack != null) {
            return stack.length;
        } else {
            return 0;
        }
    }

    @Override
    public List<DebuggingView.DVFrame> getFrames() {
        CPPFrame[] stack = getStack();
        if (stack != null) {
            return new FrameList(stack, 0, stack.length);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<DebuggingView.DVFrame> getFrames(int from, int to) {
        CPPFrame[] stack = getStack();
        if (stack != null) {
            return new FrameList(stack, from, to);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public DebuggingView.DVSupport getDVSupport() {
        return debugger.getDVSupport();
    }

    @Override
    public List<DVThread> getLockerThreads() {
        return Collections.emptyList();
    }

    @Override
    public void resumeBlockingThreads() {
    }

    @Override
    public Breakpoint getCurrentBreakpoint() {
        return null; // TODO
    }

    @Override
    public boolean isInStep() {
        return false; // TODO
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    @CheckForNull
    public synchronized CPPFrame[] getStack() {
        if (this.status != Status.SUSPENDED) {
            return null;
        }
        CPPFrame[] stack = this.stack;
        if (stack == null) {
            MIRecord record;
            try {
                record = debugger.sendAndGet("-stack-list-frames --thread " + id);
            } catch (InterruptedException ex) {
                return null;
            }
            MITList stackList = record.results().valueOf("stack").asList();
            int l = stackList.size();
            stack = new CPPFrame[l];
            int i = 0;
            if (topFrame != null) {
                stack[0] = topFrame;
                i++;
            }
            for (int li = i; li < l; li++) {
                CPPFrame frame = CPPFrame.create(this, (MITList) ((MIResult) stackList.get(li)).value());
                if (frame != null) {
                    stack[i++] = frame;
                }
            }
            if (i < l) {
                stack = Arrays.copyOf(stack, i);
            }
            this.stack = stack;
        }
        return stack;
    }

    CPPLiteDebugger getDebugger() {
        return debugger;
    }

    synchronized void notifyExited() {
        status = Status.EXITED;
        topFrame = null;
        stack = null;
    }

    void notifyStopped() {
        synchronized (this) {
            status = Status.SUSPENDED;
        }
        pcs.firePropertyChange(PROP_SUSPENDED, false, true);
        ((DebuggingViewSupportImpl) debugger.getDVSupport()).doFirePropertyChange(DebuggingView.DVSupport.PROP_THREAD_SUSPENDED, null, this);
    }

    void notifyRunning() {
        synchronized (this) {
            if (status == Status.RUNNING) {
                return ;
            }
            status = Status.RUNNING;
            topFrame = null;
            stack = null;
        }
        pcs.firePropertyChange(PROP_SUSPENDED, true, false);
        ((DebuggingViewSupportImpl) debugger.getDVSupport()).doFirePropertyChange(DebuggingView.DVSupport.PROP_THREAD_RESUMED, null, this);
    }

    private static final class FrameList extends AbstractList<DebuggingView.DVFrame> {

        private final CPPFrame[] array;
        private final int from;
        private final int to;

        FrameList(CPPFrame[] array, int from, int to) {
            this.array = array;
            this.from = from;
            this.to = Math.min(to, array.length);
        }

        @Override
        public CPPFrame get(int index) {
            return array[index + from];
        }

        @Override
        public int size() {
            return to - from;
        }

    }
}
