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
package org.netbeans.modules.javafx2.editor;

import java.util.Arrays;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;

/**
 * Error marker attached to an Element produced by {@link XMLLexerParser}.
 */
public final class ErrorMark {
    private boolean error = true;
    
    /**
     * Offset of the error mark
     */
    private final int offset;
    /**
     * Length of the offending text
     */
    private final int len;
    
    /**
     * Type / classification of the error
     */
    private final String errorType;
    
    /**
     * Parameter
     */
    private final Object[]    parameters;
    
    /**
     * Custom message, if any
     */
    private final String message;
    
    /**
     * Target of the error, possibly null for low-level errors
     */
    private final FxNode  target;
    
    public ErrorMark(int offset, int len, String errorType, String message, Object... params) {
        this(null, true, offset, len, errorType, message, params);
    }

    public ErrorMark(FxNode target, int offset, int len, String errorType, String message, Object... params) {
        this(null, true, offset, len, errorType, message, params);
    }

    ErrorMark(FxNode target, boolean error, int offset, int len, String errorType, String message, Object... params) {
        this.target = target;
        this.error = error;
        this.offset = offset;
        this.len = len;
        this.errorType = errorType;
        this.message = message;
        this.parameters = params;
    }

    public static ErrorMark makeError(FxNode target, int offset, int len, String errorType, String message, Object... params) {
        return new ErrorMark(target, true,  offset, len, errorType, message, params);
    }

    public static ErrorMark makeError(int offset, int len, String errorType, String message, Object... params) {
        return new ErrorMark(null, true,  offset, len, errorType, message, params);
    }

    public static ErrorMark makeWarning(int offset, int len, String errorType, String message, Object... params) {
        return new ErrorMark(null, false,  offset, len, errorType, message, params);
    }
    
    public boolean isError() {
        return error;
    }

    public int getOffset() {
        return offset;
    }

    public int getLen() {
        return len;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getParameters() {
        return parameters;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "Error{type: %s, pos: %d, len: %d, message: %s",
                errorType, offset, len, message
        ));
        if (parameters != null) {
            sb.append(", ").append(Arrays.asList(parameters));
        }
        sb.append("}");
        return sb.toString();
    }
}
