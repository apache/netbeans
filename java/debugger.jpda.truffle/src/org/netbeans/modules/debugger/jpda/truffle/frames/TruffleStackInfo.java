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

package org.netbeans.modules.debugger.jpda.truffle.frames;

import com.sun.jdi.StringReference;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public final class TruffleStackInfo {
    
    private static final String METHOD_GET_FRAMES_INFO = "getFramesInfo";       // NOI18N
    private static final String METHOD_GET_FRAMES_INFO_SIG = "([Lcom/oracle/truffle/api/debug/DebugStackFrame;Z)[Ljava/lang/Object;";   // NOI18N
    
    private final JPDADebugger debugger;
    private final JPDAThread thread;
    private final ObjectVariable stackTrace;
    private TruffleStackFrame[] stackFrames;
    private boolean includedInternalFrames;
    private boolean areInternalFrames;

    public TruffleStackInfo(JPDADebugger debugger, JPDAThread thread, ObjectVariable stackTrace) {
        this.debugger = debugger;
        this.thread = thread;
        this.stackTrace = stackTrace;
    }

    public TruffleStackFrame[] getStackFrames(boolean includeInternal) {
        if (stackFrames == null || includedInternalFrames != includeInternal) {
            stackFrames = loadStackFrames(includeInternal);
            this.includedInternalFrames = includeInternal;
        }
        return stackFrames;
    }
    
    public boolean hasInternalFrames() {
        return areInternalFrames;
    }
        
    private TruffleStackFrame[] loadStackFrames(boolean includeInternal) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable internalVar = debugger.createMirrorVar(includeInternal, true);
            Variable framesVar = debugAccessor.invokeMethod(METHOD_GET_FRAMES_INFO,
                                                            METHOD_GET_FRAMES_INFO_SIG,
                                                            new Variable[] { stackTrace,
                                                                             internalVar });
            Field[] framesInfos = ((ObjectVariable) framesVar).getFields(0, Integer.MAX_VALUE);
            String framesDesc = (String) framesInfos[0].createMirrorObject();
            Field[] codes = ((ObjectVariable) framesInfos[1]).getFields(0, Integer.MAX_VALUE);
            Field[] thiss = ((ObjectVariable) framesInfos[2]).getFields(0, Integer.MAX_VALUE);
            areInternalFrames = false;
            if (!includeInternal) {
                areInternalFrames = (Boolean) framesInfos[3].createMirrorObject();
            }
            int i1 = 0;
            int i2;
            int depth = 1;
            List<TruffleStackFrame> truffleFrames = new ArrayList<>();
            while ((i2 = framesDesc.indexOf("\n\n", i1)) > 0) {
                StringReference codeRef = (StringReference) ((JDIVariable) codes[depth-1]).getJDIValue();
                ObjectVariable frameInstance = (ObjectVariable) stackTrace.getFields(0, Integer.MAX_VALUE)[depth - 1];
                TruffleStackFrame tsf = new TruffleStackFrame(
                        debugger, thread, depth, frameInstance, framesDesc.substring(i1, i2),
                        codeRef, null, (ObjectVariable) thiss[depth-1], includeInternal);
                truffleFrames.add(tsf);
                if (includeInternal && tsf.isInternal()) {
                    areInternalFrames = true;
                }
                i1 = i2 + 2;
                depth++;
            }
            return truffleFrames.toArray(new TruffleStackFrame[truffleFrames.size()]);
        } catch (InvalidExpressionException | NoSuchMethodException | InvalidObjectException ex) {
            Exceptions.printStackTrace(ex);
            return new TruffleStackFrame[] {};
        }
    }
    
}
