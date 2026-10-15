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
package org.netbeans.lib.chrome_devtools_protocol.runtime;

import java.net.URI;
import java.util.Objects;

public final class ExceptionDetails {
    private int exceptionId;
    private String text;
    private int lineNumber;
    private int columnNumber;
    private String scriptId;
    private URI url;
    private StackTrace stackTrace;
    private RemoteObject exception;
    private Integer executionContextId;
    private Object exceptionMetaData;

    public ExceptionDetails() {
    }

    /**
     * Exception id.
     */
    public int getExceptionId() {
        return exceptionId;
    }

    /**
     * Exception id.
     */
    public void setExceptionId(int exceptionId) {
        this.exceptionId = exceptionId;
    }

    /**
     * Exception text, which should be used together with exception object when
     * available.
     */
    public String getText() {
        return text;
    }

    /**
     * Exception text, which should be used together with exception object when
     * available.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Line number of the exception location (0-based).
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Line number of the exception location (0-based).
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Column number of the exception location (0-based).
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Column number of the exception location (0-based).
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Script ID of the exception location.
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * Script ID of the exception location.
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    /**
     * URL of the exception location, to be used when the script was not
     * reported.
     */
    public URI getUrl() {
        return url;
    }

    /**
     * URL of the exception location, to be used when the script was not
     * reported.
     */
    public void setUrl(URI url) {
        this.url = url;
    }

    /**
     * JavaScript stack trace if available.
     */
    public StackTrace getStackTrace() {
        return stackTrace;
    }

    /**
     * JavaScript stack trace if available.
     */
    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * Exception object if available.
     */
    public RemoteObject getException() {
        return exception;
    }

    /**
     * Exception object if available.
     */
    public void setException(RemoteObject exception) {
        this.exception = exception;
    }

    /**
     * Identifier of the context where exception happened.
     */
    public Integer getExecutionContextId() {
        return executionContextId;
    }

    /**
     * Identifier of the context where exception happened.
     */
    public void setExecutionContextId(Integer executionContextId) {
        this.executionContextId = executionContextId;
    }

    /**
     * Dictionary with entries of meta data that the client associated with this
     * exception, such as information about associated network requests, etc.
     * <p><strong>Experimental</strong></p>
     */
    public Object getExceptionMetaData() {
        return exceptionMetaData;
    }

    /**
     * Dictionary with entries of meta data that the client associated with this
     * exception, such as information about associated network requests, etc.
     * <p><strong>Experimental</strong></p>
     */
    public void setExceptionMetaData(Object exceptionMetaData) {
        this.exceptionMetaData = exceptionMetaData;
    }

    @Override
    public String toString() {
        return "ExceptionDetails{" + "exceptionId=" + exceptionId + ", text=" + text + ", lineNumber=" + lineNumber + ", columnNumber=" + columnNumber + ", scriptId=" + scriptId + ", url=" + url + ", stackTrace=" + stackTrace + ", exception=" + exception + ", executionContextId=" + executionContextId + ", exceptionMetaData=" + exceptionMetaData + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.exceptionId;
        hash = 67 * hash + Objects.hashCode(this.text);
        hash = 67 * hash + this.lineNumber;
        hash = 67 * hash + this.columnNumber;
        hash = 67 * hash + Objects.hashCode(this.scriptId);
        hash = 67 * hash + Objects.hashCode(this.url);
        hash = 67 * hash + Objects.hashCode(this.stackTrace);
        hash = 67 * hash + Objects.hashCode(this.exception);
        hash = 67 * hash + Objects.hashCode(this.executionContextId);
        hash = 67 * hash + Objects.hashCode(this.exceptionMetaData);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExceptionDetails other = (ExceptionDetails) obj;
        if (this.exceptionId != other.exceptionId) {
            return false;
        }
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        if (this.columnNumber != other.columnNumber) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (!Objects.equals(this.scriptId, other.scriptId)) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.stackTrace, other.stackTrace)) {
            return false;
        }
        if (!Objects.equals(this.exception, other.exception)) {
            return false;
        }
        if (!Objects.equals(this.executionContextId, other.executionContextId)) {
            return false;
        }
        return Objects.equals(this.exceptionMetaData, other.exceptionMetaData);
    }

    
}
