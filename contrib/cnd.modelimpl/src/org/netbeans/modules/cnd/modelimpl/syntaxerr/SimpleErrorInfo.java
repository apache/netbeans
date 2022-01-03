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
package org.netbeans.modules.cnd.modelimpl.syntaxerr;

import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;

/**
 * A trivial CsmErrorInfo implementation
 */
public class SimpleErrorInfo implements CsmErrorInfo {

    private int startOffset;
    private int endOffset;
    private String text;
    private Severity severity;

    public SimpleErrorInfo(int startOffset, int endOffset, String text) {
        this(startOffset, endOffset, text, Severity.ERROR);
    }

    public SimpleErrorInfo(int startOffset, int endOffset, String text, Severity severity) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.text = text;
        this.severity = severity;
    }

    @Override
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public String getMessage() {
        return text;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }
}
