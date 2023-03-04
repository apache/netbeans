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

package org.netbeans.api.debugger.jpda;


/**
 * Notification about problems during debugger start.
 *
 * @author   Jan Jancura
 */
public class DebuggerStartException extends Exception {

    private Throwable throwable;


    /**
     * Constructs a DebuggerStartException with given message.
     *
     * @param message a exception message
     */
    public DebuggerStartException (String message) {
        super (message);
    }

    /**
     * Constructs a DebuggerStartException for a given target exception.
     *
     * @param t a target exception
     */
    public DebuggerStartException (Throwable t) {
        super (t.getMessage ());
        initCause(t);
        throwable = t;
    }
    
    /**
     * Get the thrown target exception.
     *
     * @return the thrown target exception
     */
    public Throwable getTargetException () {
        return throwable;
    }
}

