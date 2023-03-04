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

package org.netbeans.modules.debugger.jpda.truffle.access;

import com.sun.jdi.StringReference;
import java.io.InvalidObjectException;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.truffle.PersistentValues;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
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
        return evaluate(debugger, currentPCInfo, expression);
    }

    public static Variable evaluate(JPDADebugger debugger, CurrentPCInfo currentPCInfo, String expression) throws InvalidExpressionException {
        TruffleStackFrame selectedStackFrame = currentPCInfo.getSelectedStackFrame();
        if (selectedStackFrame == null) {
            throw new InvalidExpressionException(Bundle.MSG_NoSuspend());
        }
        ObjectVariable stackFrameInstance = selectedStackFrame.getStackFrameInstance();
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        PersistentValues values = new PersistentValues(((JPDADebuggerImpl) debugger).getVirtualMachine());
        try {
            StringReference expressionReference = values.mirrorOf(expression);
            Variable mirrorExpression = ((JPDADebuggerImpl) debugger).getVariable(expressionReference);
            Variable valueVar = debugAccessor.invokeMethod(
                    METHOD_EVALUATE,
                    METHOD_EVALUATE_ON_FRAME_SIG,
                    new Variable[] { stackFrameInstance,
                                     mirrorExpression });
            return valueVar;
        } catch (InvalidExpressionException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof InvocationExceptionTranslated) {
                // We do not want to prepend Java exception message:
                ((InvocationExceptionTranslated) targetException).resetInvocationMessage();
            }
            throw ex;
        } catch (InternalExceptionWrapper | NoSuchMethodException | UnsupportedOperationExceptionWrapper ex) {
            try {
                return debugger.createMirrorVar(ex.getLocalizedMessage());
            } catch (InvalidObjectException iex) {
                Exceptions.printStackTrace(iex);
                return null;
            }
            //return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }  finally {
            values.collect();
        }
    }

}
