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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

/**
 * Interface for creating "commands" needed to send to debuggers in order
 * to create and alter breakpoints.
 *
 * HandlerExpert's also are used as Handler factories, which ususlly take
 * debugger-specific input and can't be factored here.
 * The exception is childHandler().
 */

public interface HandlerExpert {

    public enum ReplacementPolicy {
	INPLACE,	// engine-side handler object mutates
	EXPLICIT	// UI needs to delete old and create new
    };

    /**
     * Return the "command form" to be passed to engine.
     */
    public HandlerCommand commandFormNew(NativeBreakpoint breakpoint);


    /**
     * Return the "command form" to be passed to engine.
     */

    public HandlerCommand commandFormCustomize(NativeBreakpoint editedBreakpoint,
			               NativeBreakpoint targetBreakpoint);

    /**
     * Create a Handler/Breakpoint pair by cloning 'bpt'.
     * 'bpt' may be a top-level or sub-bpt.
     */
    public Handler childHandler(NativeBreakpoint bpt);

    public ReplacementPolicy replacementPolicy();
}

