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

package org.netbeans.api.sendopts;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/** Signals that something is wrong when processing the command line arguments.
 *
 * @author Jaroslav Tulach
 */
public final class CommandException extends Exception {
    private final int exitCode;
    private final String locMsg;
    
    /** Simple constructor for the CommandException to indicate that a 
     * processing error occurred. The provided <code>exitCode</code> represents
     * the value to be usually send to as a return value to {@link System#exit}.
     * 
     * @param exitCode the value, should be different than zero
     */
    public CommandException(int exitCode) {
        this("Error code: " + exitCode, exitCode, null); // NOI18N
    }

    /** Creates new exception with a localised message assigned to it.
     * @param exitCode exit code to report from the exception
     * @param locMsg localised message
     */
    public CommandException(int exitCode, String locMsg) {
        this("Error code: " + exitCode, exitCode, locMsg); // NOI18N
    }
    
    
    /** Creates a new instance of CommandException */
    CommandException(String msg, int exitCode, String locMsg) {
        super(msg);
        this.exitCode = exitCode;
        this.locMsg = locMsg;
    }
    /** Creates a new instance of CommandException */
    CommandException(String msg, int exitCode) {
        this(msg, exitCode, null);
    }

    /** Returns an exit code for this exception.
     * @return integer exit code, zero if exited correctly
     */
    public int getExitCode() {
        return exitCode;
    }

    /** Localized message describing the problem that is usually printed
     * to the user.
     */
    @Override
    public String getLocalizedMessage() {
        if (locMsg != null) {
            return locMsg;
        }
        if (getCause() != null) {
            return getCause().getLocalizedMessage();
        }
        return super.getLocalizedMessage();
    }
}
