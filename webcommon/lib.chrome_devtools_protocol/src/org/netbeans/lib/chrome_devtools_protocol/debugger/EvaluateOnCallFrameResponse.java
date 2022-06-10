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
package org.netbeans.lib.chrome_devtools_protocol.debugger;

import java.util.Objects;
import org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;

public final class EvaluateOnCallFrameResponse {
    private RemoteObject result;
    private ExceptionDetails exceptionDetails;

    public EvaluateOnCallFrameResponse() {
    }

    /**
     * Object wrapper for the evaluation result.
     */
    public RemoteObject getResult() {
        return result;
    }

    /**
     * Object wrapper for the evaluation result.
     */
    public void setResult(RemoteObject result) {
        this.result = result;
    }

    /**
     * Exception details.
     */
    public ExceptionDetails getExceptionDetails() {
        return exceptionDetails;
    }

    /**
     * Exception details.
     */
    public void setExceptionDetails(ExceptionDetails exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    @Override
    public String toString() {
        return "EvaluateOnCallFrameResult{" + "result=" + result + ", exceptionDetails=" + exceptionDetails + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.result);
        hash = 67 * hash + Objects.hashCode(this.exceptionDetails);
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
        final EvaluateOnCallFrameResponse other = (EvaluateOnCallFrameResponse) obj;
        if (!Objects.equals(this.result, other.result)) {
            return false;
        }
        return Objects.equals(this.exceptionDetails, other.exceptionDetails);
    }

    
}
