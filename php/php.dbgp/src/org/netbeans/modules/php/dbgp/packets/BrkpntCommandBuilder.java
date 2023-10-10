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
package org.netbeans.modules.php.dbgp.packets;

import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.ExceptionBreakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.LineBreakpoint;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand.Types;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 * @author ads
 *
 */
public final class BrkpntCommandBuilder {

    private BrkpntCommandBuilder() {
    }

    public static BrkpntSetCommand buildLineBreakpoint(SessionId id, String transactionId, FileObject localFile, int lineNumber) {
        return buildLineBreakpoint(id, transactionId, localFile, lineNumber, null);
    }

    public static BrkpntSetCommand buildLineBreakpoint(SessionId id, String transactionId, FileObject localFile, int lineNumber, String condition) {
        if (localFile == null) {
            // #251806
            return null;
        }
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        String uri = id.toWebServerURI(localFile);
        if (uri == null) {
            return null;
        }
        command.setType(Types.LINE);
        command.setFile(uri);
        command.setLineNumber(lineNumber);
        command.setExpression(condition);
        return command;
    }

    public static BrkpntSetCommand buildLineBreakpoint(SessionId id, String transactionId, LineBreakpoint breakpoint) {
        Line line = breakpoint.getLine();
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        BrkpntSetCommand command = buildLineBreakpoint(id, transactionId, fileObject, line.getLineNumber(), breakpoint.getCondition());
        if (command != null) {
            command.setBreakpoint(breakpoint);
        }
        return command;
    }

    public static BrkpntSetCommand buildCallBreakpoint(String transactionId, String funcName) {
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        command.setType(Types.CALL);
        command.setFunction(funcName);
        return command;
    }

    public static BrkpntSetCommand buildCallBreakpoint(String transactionId, FunctionBreakpoint functionBreakpoint) {
        String func = functionBreakpoint.getFunction();
        BrkpntSetCommand command = buildCallBreakpoint(transactionId, func);
        command.setBreakpoint(functionBreakpoint);
        return command;
    }

    public static BrkpntSetCommand buildReturnBreakpoint(String transactionId, String funcName) {
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        command.setType(Types.RETURN);
        command.setFunction(funcName);
        return command;
    }

    public static BrkpntSetCommand buildReturnBreakpoint(String transactionId, FunctionBreakpoint functionBreakpoint) {
        String func = functionBreakpoint.getFunction();
        BrkpntSetCommand command = buildReturnBreakpoint(transactionId, func);
        command.setBreakpoint(functionBreakpoint);
        return command;
    }

    public static BrkpntSetCommand buildExceptionBreakpoint(String transactionId, String excName) {
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        command.setType(Types.EXCEPTION);
        command.setException(excName);
        return command;
    }

    public static BrkpntSetCommand buildExceptionBreakpoint(String transactionId, ExceptionBreakpoint exceptionBreakpoint) {
        String exceptionName = exceptionBreakpoint.getException();
        BrkpntSetCommand command = buildExceptionBreakpoint(transactionId, exceptionName);
        command.setBreakpoint(exceptionBreakpoint);
        return command;
    }

    public static BrkpntSetCommand buildConditionalBreakpoint(String transactionId, String expression) {
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        command.setType(Types.WATCH);
        command.setExpression(expression);
        return command;
    }

}
