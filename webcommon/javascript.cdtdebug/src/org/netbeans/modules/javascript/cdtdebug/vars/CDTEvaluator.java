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

package org.netbeans.modules.javascript.cdtdebug.vars;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameRequest;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;

public class CDTEvaluator {

    public static RemoteObject evaluate(CDTDebugger debugger, String expression) throws EvaluationError {
        final RemoteObject[] valueRef = new RemoteObject[] { null };
        final String[] errRef = new String[] { null };

        EvaluateOnCallFrameRequest eocfr = new EvaluateOnCallFrameRequest();
        eocfr.setCallFrameId(debugger.getCurrentFrame().getCallFrameId());
        eocfr.setExpression(expression);

        debugger.getConnection().getDebugger()
                .evaluateOnCallFrame(eocfr)
                .handle((res, thr) -> {
                    synchronized (valueRef) {
                        if (res != null) {
                            if(res.getResult() != null) {
                                valueRef[0] = res.getResult();
                            } else if (res.getExceptionDetails() != null) {
                                errRef[0] = res.getExceptionDetails().getText();
                            } else {
                                errRef[0] = ">Evaluation failed<";
                            }
                        } else {
                            errRef[0] = thr.getMessage();
                        }
                        valueRef.notifyAll();
                    }
                    return null;
                });

        synchronized (valueRef) {
            if (valueRef[0] == null && errRef[0] == null) {
                try {
                    valueRef.wait();
                } catch (InterruptedException ex) {
                    throw new EvaluationError(ex.getLocalizedMessage());
                }
            }
        }
        if (errRef[0] != null) {
            throw new EvaluationError(errRef[0]);
        }
        return valueRef[0];
    }

    public static String getStringValue(RemoteObject value) {
        if(value == null) {
            return ">NULL<";
        }
        switch (nvl(value.getType(), "")) {
            case "string":
                return "\"" + String.valueOf(value.getValue()) + "\"";
            case "number":
            case "boolean": {
            }
            case "object": {
                switch (nvl(value.getSubtype(), "")) {
                    case "null":
                        return "null";
                    case "":
                    case "array":
                        return value.getDescription() != null
                                ? value.getDescription()
                                : String.valueOf(value.getValue());
                }
            }
            default: {
                return value.getDescription() != null
                        ? value.getDescription()
                        : String.valueOf(value.getValue());
            }
        }
    }

    public static String getStringType(RemoteObject value) {
        if(value == null) {
            return ">NULL<";
        }
        List<String> elements = new ArrayList<>(3);
        if(value.getType() != null && ! value.getType().isBlank()) {
            elements.add(value.getType());
        }
        if(value.getSubtype() != null && ! value.getSubtype().isBlank()) {
            elements.add(value.getSubtype());
        }
        if(value.getClassName() != null && ! value.getClassName().isBlank()) {
            elements.add(value.getClassName());
        }
        return elements.stream().collect(Collectors.joining("/"));
    }

    private static String nvl(String input, String nullValue) {
        if(input == null) {
            return nullValue;
        } else {
            return input;
        }
    }
}
