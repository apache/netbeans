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
package org.netbeans.modules.debugger.jpda.truffle.breakpoints.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleEval;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.openide.util.NbBundle;

/**
 * Prints text specified in the Truffle breakpoint when it is hits.
 *
 * @see TruffleLineBreakpoint#setPrintText(java.lang.String)
 */
public class TruffleBreakpointOutput {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\{=(.*?)\\}");  // NOI18N

    private TruffleBreakpointOutput() {
    }

    public static void breakpointsHit(HitBreakpointInfo[] breakpointInfos, CurrentPCInfo cpi) {
        for (HitBreakpointInfo breakpointInfo : breakpointInfos) {
            breakpointHit(breakpointInfo, cpi);
        }
    }

    private static void breakpointHit(HitBreakpointInfo breakpointInfo, CurrentPCInfo cpi) {
        JSLineBreakpoint breakpoint = breakpointInfo.getBreakpoint();
        if (!(breakpoint instanceof TruffleLineBreakpoint)) {
            return;
        }
        String printText = ((TruffleLineBreakpoint)  breakpoint).getPrintText();
        if (printText == null || printText.isEmpty()) {
            return;
        }
        ObjectVariable conditionException = breakpointInfo.getConditionException();
        String exceptionMessage = null;
        if (conditionException != null && conditionException.getUniqueID() != 0) {
            exceptionMessage = new InvocationExceptionTranslated(conditionException, (JPDADebuggerImpl) breakpointInfo.getDebugger()).getLocalizedMessage();
        }
        substituteAndPrintText(printText, exceptionMessage, cpi, breakpointInfo.getDebugger());
    }

    private static void substituteAndPrintText(String printText, String exceptionMessage, CurrentPCInfo cpi, JPDADebugger debugger) {
        printText = substitute(printText, exceptionMessage, cpi, debugger);
        ((JPDADebuggerImpl) debugger).getConsoleIO().println(printText, null);
    }

    @NbBundle.Messages({"# {0} - Expression",
                        "# {1} - Error message",
                        "MSG_EvaluateError=Cannot evaluate expression ''{0}'' : {1}"})
    private static String substitute(String printText, String exceptionMessage, CurrentPCInfo cpi, JPDADebugger debugger) {
        // 5) resolve all expressions {=expression}
        for (;;) {
            Matcher m = EXPRESSION_PATTERN.matcher (printText);
            if (!m.find ()) {
                break;
            }
            String expression = m.group (1);
            String value = "";
            try {
                Variable varValue = TruffleEval.evaluate(debugger, cpi, expression);
                if (varValue != null) {
                    TruffleVariable tv = TruffleVariable.get(varValue);
                    if (tv != null) {
                        value = tv.getDisplayValue();
                    } else {
                        if (varValue instanceof ObjectVariable) {
                            value = ((ObjectVariable) varValue).getToStringValue();
                        } else {
                            value = varValue.getValue();
                        }
                    }
                }
            } catch (InvalidExpressionException e) {
                // expression is invalid or cannot be evaluated
                String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                ((JPDADebuggerImpl) debugger).getConsoleIO().println(
                        Bundle.MSG_EvaluateError(expression, msg),
                        null
                );
            }
            printText = m.replaceFirst(value);
        }
        if (exceptionMessage != null) {
            printText = printText + "\n***\n"+ exceptionMessage + "\n***\n";    // NOI18N
        }
        return printText;
    }
}
