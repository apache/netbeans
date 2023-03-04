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

package org.netbeans.modules.javascript.v8debug.frames;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8ScriptValue;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.ReferencedValues;
import org.netbeans.modules.javascript.v8debug.V8Debugger;

/**
 *
 * @author Martin Entlicher
 */
public final class CallStack {
    
    public static final CallStack EMPTY = new CallStack(null, new V8Frame[] {}, new ReferencedValue[] {});
    
    private final V8Debugger dbg;
    private final V8Frame[] frames;
    private final ReferencedValues rvals;
    private CallFrame[] callFrames;
    private CallFrame topFrame;
    
    public CallStack(V8Debugger dbg, V8Frame[] frames, ReferencedValue[] referencedValues) {
        this.dbg = dbg;
        this.frames = frames;
        this.rvals = new ReferencedValues(referencedValues);
    }
    
    public boolean isEmpty() {
        return frames.length == 0;
    }
    
    public V8Frame[] getFrames() {
        return frames;
    }
    
    public CallFrame[] getCallFrames() {
        synchronized (this) {
            if (callFrames == null) {
                CallFrame[] cfs = new CallFrame[frames.length];
                if (frames.length > 0) {
                    if (topFrame == null) {
                        topFrame = new CallFrame(dbg, frames[0], rvals, true);
                    }
                    cfs[0] = topFrame;
                }
                for (int i = 1; i < frames.length; i++) {
                    cfs[i] = new CallFrame(dbg, frames[i], rvals, false);
                }
                callFrames = cfs;
            }
            return callFrames;
        }
    }
    
//    private CallFrame[] createCallFrames() {
//        CallFrame[] cfs = new CallFrame[frames.length];
//        for (int i = 0; i < frames.length; i++) {
//            cfs[i] = new CallFrame(frames[i], rvals, i == 0);
//        }
//        return cfs;
//    }
    
    public @CheckForNull CallFrame getTopFrame() {
        if (frames.length > 0) {
            synchronized (this) {
                if (topFrame == null) {
                    topFrame = new CallFrame(dbg, frames[0], rvals, true);
                }
                return topFrame;
            }
        } else {
            return null;
        }
    }
    
    public V8Script getScript(V8Frame frame) {
        long ref = frame.getScriptRef();
        V8Value val = rvals.getReferencedValue(ref);
        if (val instanceof V8ScriptValue) {
            return ((V8ScriptValue) val).getScript();
        } else {
            return null;
        }
    }
    
}
