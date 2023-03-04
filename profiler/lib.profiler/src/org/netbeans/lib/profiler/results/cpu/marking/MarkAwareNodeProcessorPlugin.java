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
package org.netbeans.lib.profiler.results.cpu.marking;

import java.util.ArrayDeque;
import java.util.Deque;
import org.netbeans.lib.profiler.global.TransactionalSupport;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode;

/**
 *
 * @author Jaroslav Bachorik
 */
public class MarkAwareNodeProcessorPlugin extends RuntimeCCTNodeProcessor.PluginAdapter implements MarkingEngine.StateObserver {
    volatile boolean resetFlag = false;
    private Mark parentMark = null;
    private Deque<Mark> markStack = new ArrayDeque<Mark>();
    private final TransactionalSupport transaction = new TransactionalSupport();
    
    @Override
    public void onBackout(MarkedCPUCCTNode node) {
        markStack.pop();
        parentMark = (Mark) (markStack.isEmpty() ? null : markStack.peek());
    }

    @Override
    public void onNode(MarkedCPUCCTNode node) {
        parentMark = (Mark) (markStack.isEmpty() ? null : markStack.peek());
        markStack.push(node.getMark());
    }

    @Override
    public void onStart() {
        transaction.beginTrans(true);
        parentMark = null;
        resetFlag = false;
    }

    @Override
    public void onStop() {
        markStack.clear();
        parentMark = null;
        transaction.endTrans();
    }
    
    public void beginTrans(final boolean mutable) {
        transaction.beginTrans(mutable);
    }

    public void endTrans() {
        if (resetFlag) {
            markStack.clear();
            resetFlag = false;
        }

        transaction.endTrans();
    }

    public synchronized void onReset() {
        resetFlag = true;
        transaction.endTrans();
    }

    @Override
    public void stateChanged(MarkingEngine instance) {
        reset();
    }
    
    protected final Mark getCurrentMark() {
        return (Mark) (markStack.isEmpty() ? Mark.DEFAULT : markStack.peek());
    }

    protected final Mark getParentMark() {
        return (parentMark != null) ? parentMark : Mark.DEFAULT;
    }

    protected synchronized boolean isReset() {
        return resetFlag;
    }

    private void reset() {
        resetFlag = true;
    }
}
