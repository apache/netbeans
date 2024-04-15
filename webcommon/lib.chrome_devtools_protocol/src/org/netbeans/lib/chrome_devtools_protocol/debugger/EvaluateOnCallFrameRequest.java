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

public final class EvaluateOnCallFrameRequest {

    private String callFrameId;
    private String expression;
    private String objectGroup;
    private Boolean includeCommandLineAPI;
    private Boolean silent;
    private Boolean returnByValue;
    private Boolean generatePreview;
    private Boolean throwOnSideEffect;
    private Integer timeout;

    public EvaluateOnCallFrameRequest() {
    }

    public EvaluateOnCallFrameRequest(String callFrameId, String expression) {
        this.callFrameId = callFrameId;
        this.expression = expression;
    }

    /**
     * Call frame identifier to evaluate on.
     */
    public String getCallFrameId() {
        return callFrameId;
    }

    /**
     * Call frame identifier to evaluate on.
     */
    public void setCallFrameId(String callFrameId) {
        this.callFrameId = callFrameId;
    }

    /**
     * Expression to evaluate.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Expression to evaluate.
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * String object group name to put result into (allows rapid releasing
     * resulting object handles using {@code releaseObjectGroup}).
     */
    public String getObjectGroup() {
        return objectGroup;
    }

    /**
     * String object group name to put result into (allows rapid releasing
     * resulting object handles using {@code releaseObjectGroup}).
     */
    public void setObjectGroup(String objectGroup) {
        this.objectGroup = objectGroup;
    }

    /**
     * Specifies whether command line API should be available to the evaluated
     * expression, defaults to false.
     */
    public Boolean getIncludeCommandLineAPI() {
        return includeCommandLineAPI;
    }

    /**
     * Specifies whether command line API should be available to the evaluated
     * expression, defaults to false.
     */
    public void setIncludeCommandLineAPI(Boolean includeCommandLineAPI) {
        this.includeCommandLineAPI = includeCommandLineAPI;
    }

    /**
     * In silent mode exceptions thrown during evaluation are not reported and
     * do not pause execution. Overrides {@code setPauseOnException} state.
     */
    public Boolean getSilent() {
        return silent;
    }

    /**
     * In silent mode exceptions thrown during evaluation are not reported and
     * do not pause execution. Overrides {@code setPauseOnException} state.
     */
    public void setSilent(Boolean silent) {
        this.silent = silent;
    }

    /**
     * Whether the result is expected to be a JSON object that should be sent by
     * value.
     */
    public Boolean getReturnByValue() {
        return returnByValue;
    }

    /**
     * Whether the result is expected to be a JSON object that should be sent by
     * value.
     */
    public void setReturnByValue(Boolean returnByValue) {
        this.returnByValue = returnByValue;
    }

    /**
     * Whether preview should be generated for the result.
     * <p><strong>Experimental</strong></p>
     */
    public Boolean getGeneratePreview() {
        return generatePreview;
    }

    /**
     * Whether preview should be generated for the result.
     * <p><strong>Experimental</strong></p>
     */
    public void setGeneratePreview(Boolean generatePreview) {
        this.generatePreview = generatePreview;
    }

    /**
     * Whether to throw an exception if side effect cannot be ruled out during
     * evaluation.
     */
    public Boolean getThrowOnSideEffect() {
        return throwOnSideEffect;
    }

    /**
     * Whether to throw an exception if side effect cannot be ruled out during
     * evaluation.
     */
    public void setThrowOnSideEffect(Boolean throwOnSideEffect) {
        this.throwOnSideEffect = throwOnSideEffect;
    }

    /**
     * Terminate execution after timing out (number of milliseconds).
     * <p><strong>Experimental</strong></p>
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Terminate execution after timing out (number of milliseconds).
     * <p><strong>Experimental</strong></p>
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "EvaluateOnCallFrameRequest{" + "callFrameId=" + callFrameId + ", expression=" + expression + ", objectGroup=" + objectGroup + ", includeCommandLineAPI=" + includeCommandLineAPI + ", silent=" + silent + ", returnByValue=" + returnByValue + ", generatePreview=" + generatePreview + ", throwOnSideEffect=" + throwOnSideEffect + ", timeout=" + timeout + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.callFrameId);
        hash = 53 * hash + Objects.hashCode(this.expression);
        hash = 53 * hash + Objects.hashCode(this.objectGroup);
        hash = 53 * hash + Objects.hashCode(this.includeCommandLineAPI);
        hash = 53 * hash + Objects.hashCode(this.silent);
        hash = 53 * hash + Objects.hashCode(this.returnByValue);
        hash = 53 * hash + Objects.hashCode(this.generatePreview);
        hash = 53 * hash + Objects.hashCode(this.throwOnSideEffect);
        hash = 53 * hash + Objects.hashCode(this.timeout);
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
        final EvaluateOnCallFrameRequest other = (EvaluateOnCallFrameRequest) obj;
        if (!Objects.equals(this.callFrameId, other.callFrameId)) {
            return false;
        }
        if (!Objects.equals(this.expression, other.expression)) {
            return false;
        }
        if (!Objects.equals(this.objectGroup, other.objectGroup)) {
            return false;
        }
        if (!Objects.equals(this.includeCommandLineAPI, other.includeCommandLineAPI)) {
            return false;
        }
        if (!Objects.equals(this.silent, other.silent)) {
            return false;
        }
        if (!Objects.equals(this.returnByValue, other.returnByValue)) {
            return false;
        }
        if (!Objects.equals(this.generatePreview, other.generatePreview)) {
            return false;
        }
        if (!Objects.equals(this.throwOnSideEffect, other.throwOnSideEffect)) {
            return false;
        }
        return Objects.equals(this.timeout, other.timeout);
    }

    
}
