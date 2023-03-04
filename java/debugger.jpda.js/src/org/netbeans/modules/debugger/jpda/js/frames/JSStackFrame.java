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

package org.netbeans.modules.debugger.jpda.js.frames;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.CallStackFrame;

/**
 *
 * @author Martin
 */
public final class JSStackFrame {
    
    private static final Map<CallStackFrame, Reference<JSStackFrame>> framesCache = new WeakHashMap<>();
    
    private CallStackFrame csf;
    
    public static JSStackFrame get(CallStackFrame csf) {
        JSStackFrame frame = null;
        synchronized (framesCache) {
            Reference<JSStackFrame> frameRef = framesCache.get(csf);
            if (frameRef != null) {
                frame = frameRef.get();
            }
            if (frame == null) {
                frame = new JSStackFrame(csf);
                frameRef = new WeakReference<>(frame);
                framesCache.put(csf, frameRef);
            }
        }
        return frame;
    }
    
    public static JSStackFrame getExisting(CallStackFrame csf) {
        synchronized (framesCache) {
            Reference<JSStackFrame> frameRef = framesCache.get(csf);
            if (frameRef != null) {
                return frameRef.get();
            }
        }
        return null;
    }
    
    private JSStackFrame(CallStackFrame csf) {
        this.csf = csf;
    }
    
    public CallStackFrame getJavaFrame() {
        return csf;
    }
    
}
