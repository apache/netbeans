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
package org.netbeans.lib.profiler.results.cpu;

import org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode;

public class ThreadInfo {

    //~ Static fields/initializers -------------------------------------------------------------------------------------------
    // The following variable is used to record the "compensation" value, a difference between the timestamp at the
    // moment user hits "get results" and the timestamp for the method entry into the top stack method. To present
    // results consistenly, we add this value to the TimedCPUCCTNode for the top-stack method. However, when
    // processing of data is resumed, we need to subtract this value back from that node.
    // this is effectively the self time for the last invocation of the top method on stack - if we would not keep
    // it separately, it would not be reported
    // private long diffAtGetResultsMoment; // diff between last methodEntry and current moment timestamp -
    //  we will have to compensate for the processing time

    //~ Instance fields ------------------------------------------------------------------------------------------------------
    private final Object stackLock = new Object();
    public TimedCPUCCTNode[] stack;
    // Simulated stack for this thread - stack starting at root method
    // (or a pseudo node if multiple root methods are called within the thread)
    int inRoot;
    // flag indicating this thread is in a root method initiated code
    int stackTopIdx;
    // Index of the stack top element
    public final int threadId;
    public int totalNNodes;
    // total number of call tree nodes for this thread
    long rootGrossTimeAbs;
    long rootGrossTimeThreadCPU;
    // Accumulated absolute and thread CPU gross time for the root method
    // - blackout data subtracted, calibration data not
    public long rootMethodEntryTimeAbs;
    public long rootMethodEntryTimeThreadCPU;
    // Absoute and thread CPU entry timestamps for the root method.
    // The xxx0 part is used when only absolute or thread CPU time data is collected.
    // Both xxx0 and xx1 parts are used when both timestamps are collected.
    public long topMethodEntryTime0;
    public long topMethodEntryTime1;
    // Entry (or "re-entry" upon return from the callee) time for the topmost method
    public long totalNInv;

    ThreadInfo(int threadId) {
        super();
        stack = new TimedCPUCCTNode[40];
        stackTopIdx = -1;
        inRoot = 0;
        this.threadId = threadId;
    }

    public boolean isInRoot() {
        return inRoot > 0;
    }

    public TimedCPUCCTNode peek() {
        synchronized (stackLock) {
            return (stackTopIdx > -1) ? stack[stackTopIdx] : null;
        }
    }

    public TimedCPUCCTNode pop() {
        TimedCPUCCTNode node = null;
        synchronized (stackLock) {
            if (stackTopIdx >= 0) {
                node = stack[stackTopIdx];
                stack[stackTopIdx] = null;
                stackTopIdx--;
            }
            return node;
        }
    }

    public void push(TimedCPUCCTNode node) {
        synchronized (stackLock) {
            stackTopIdx++;
            if (stackTopIdx >= stack.length) {
                increaseStack();
            }
            stack[stackTopIdx] = node;
            node.addNCalls(1);
            totalNInv++;
        }
    }

    private void increaseStack() {
        synchronized(stackLock) {
            TimedCPUCCTNode[] newStack = new TimedCPUCCTNode[stack.length * 2];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
    }
}
