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
package org.netbeans.spi.java.hints;

import org.netbeans.spi.editor.hints.Severity;

/** Severity of hint
 *  <ul>
 *  <li><code>ERROR</code>  - will show up as error
 *  <li><code>WARNING</code>  - will show up as warning
 *  <li><code>CURRENT_LINE_WARNING</code>  - will only show up when the caret is placed in the erroneous element
 *  </ul>
 * @author Petr Hrebejk
 */
public enum HintSeverity {
    ERROR,
    WARNING,
    CURRENT_LINE_WARNING;

    public Severity toEditorSeverity() {
        switch ( this ) {
            case ERROR:
                return Severity.ERROR;
            case WARNING:
                return Severity.VERIFIER;
            case CURRENT_LINE_WARNING:
                return Severity.HINT;
            default:
                return null;
        }
    }
}
