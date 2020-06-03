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

package org.netbeans.modules.cnd.highlight.hints;

import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;

/**
 *
 */
public class ErrorInfoImpl implements CsmErrorInfo, DisableHintFix.CodeAuditInfo {
    private final String providerID;
    private final String audutID;
    private final String message;
    private final Severity severity;
    private final String customType;
    private final int startOffset;
    private final int endOffset;

    public ErrorInfoImpl(String providerName, String audutName, String message, Severity severity, int startOffset, int endOffset) {
        this.providerID = providerName;
        this.audutID = audutName;
        this.message = message;
        this.severity = severity;
        this.customType = null;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
    
    public ErrorInfoImpl(String providerName, String audutName, String message, Severity severity, String customType, int startOffset, int endOffset) {
        this.providerID = providerName;
        this.audutID = audutName;
        this.message = message;
        this.severity = severity;
        this.customType = customType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public String getProviderID() {
        return providerID;
    }
    
    @Override
    public String getAuditID() {
        return audutID;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }
    
    public String getCustomType() {
        return customType;
    }

    @Override
    public int getStartOffset() {
        return startOffset < 0 ? 0 : startOffset;
    }

    @Override
    public int getEndOffset() {
        return startOffset < 0 ? 1 : endOffset;
    }
}
