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

package org.netbeans.modules.debugger.jpda.truffle.access;

import java.io.InvalidObjectException;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * <code>DebugStackFrame.eval()</code>.
 */
public class TruffleEval {
    
    private static final String METHOD_EVALUATE = "evaluate";                   // NOI18N
    private static final String METHOD_EVALUATE_ON_FRAME_SIG =
            "(Lcom/oracle/truffle/api/debug/DebugStackFrame;Ljava/lang/String;)Ljava/lang/Object;"; // NOI18N
    
    private TruffleEval() {}

    @NbBundle.Messages("MSG_NoSuspend=No current suspend location.")
    public static Variable evaluate(JPDADebugger debugger, String expression) throws InvalidExpressionException {
        JPDAThread currentThread = debugger.getCurrentThread();
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(currentThread);
        if (currentPCInfo == null) {
            throw new InvalidExpressionException(Bundle.MSG_NoSuspend());
        }
        ObjectVariable stackFrameInstance = currentPCInfo.getSelectedStackFrame().getStackFrameInstance();
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable mirrorExpression = debugger.createMirrorVar(expression);
            Variable valueVar = debugAccessor.invokeMethod(
                    METHOD_EVALUATE,
                    METHOD_EVALUATE_ON_FRAME_SIG,
                    new Variable[] { stackFrameInstance,
                                     mirrorExpression });
            return valueVar;
        } catch (InvalidObjectException | NoSuchMethodException ex) {
            try {
                return debugger.createMirrorVar(ex.getLocalizedMessage());
            } catch (InvalidObjectException iex) {
                Exceptions.printStackTrace(iex);
                return null;
            }
            //return ex.getLocalizedMessage();
        }
    }

}
