/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
