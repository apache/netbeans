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

package org.netbeans.lib.profiler.instrumentation;

import java.util.ResourceBundle;


/**
 * An exception thrown when begin line or end line are incorrect during Code Fragment profiling
 *
 * @author Ian Formanek
 */
public class BadLocationException extends Exception {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String CANNOT_FIND_METHOD_CURSOR_MSG;
    private static final String CANNOT_FIND_METHOD_SELECTION_MSG;

    static {
        ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.instrumentation.Bundle"); // NOI18N
        CANNOT_FIND_METHOD_CURSOR_MSG = messages.getString("BadLocationException_CannotFindMethodCursorMsg"); // NOI18N
        CANNOT_FIND_METHOD_SELECTION_MSG = messages.getString("BadLocationException_CannotFindMethodSelectionMsg"); // NOI18N
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public BadLocationException() {
    }

    public BadLocationException(String message) {
        super(message);
    }

    public BadLocationException(int code) {
        super((code == 1) ? CANNOT_FIND_METHOD_CURSOR_MSG : ((code == 2) ? CANNOT_FIND_METHOD_SELECTION_MSG : null));
    }
}
