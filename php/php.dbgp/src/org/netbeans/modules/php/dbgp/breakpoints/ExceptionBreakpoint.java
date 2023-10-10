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

import org.netbeans.modules.php.dbgp.DebugSession;

/**
 * Represent breakpoint for exceptions.
 *
 *
 */
public class ExceptionBreakpoint extends AbstractBreakpoint {
    private final String exceptionName;

    public ExceptionBreakpoint(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public String getException() {
        return exceptionName;
    }

    @Override
    public boolean isSessionRelated(DebugSession session) {
        return true;
    }

}
