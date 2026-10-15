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
package org.netbeans.modules.php.dbgp.breakpoints;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.dbgp.DebugSession;

/**
 * Represent breakpoint for exceptions.
 *
 *
 */
public class ExceptionBreakpoint extends AbstractBreakpoint {
    private static final String FONT_GRAY_COLOR = "<font color=\"#999999\">"; //NOI18N
    private static final String CLOSE_FONT = "</font>"; //NOI18N
    private final String exceptionName;
    @NullAllowed
    private volatile String message;
    @NullAllowed
    private volatile String code;

    public ExceptionBreakpoint(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public String getException() {
        return exceptionName;
    }

    public void setExceptionMessage(String message) {
        this.message = message;
    }

    @CheckForNull
    public String getExceptionMessage() {
        return buildText(message);
    }

    public void setExceptionCode(String code) {
        this.code = code;
    }

    @CheckForNull
    public String getExceptionCode() {
        return buildText(code);
    }

    @CheckForNull
    private String buildText(String text) {
        if (!StringUtils.isEmpty(text)) {
            StringBuilder builder = new StringBuilder()
                .append(FONT_GRAY_COLOR)
                .append(text)
                .append(CLOSE_FONT);
            return builder.toString();
        }

        return null;
    }

    @Override
    public boolean isSessionRelated(DebugSession session) {
        return true;
    }

}
