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

package org.openide;

/** Helper class to simplify handling with error/warning/info messages.
 *
 * @author Jiri Rechtacek
 * @see NotifyDescriptor#createNotificationLineSupport
 * @since 7.10
 */
public final class NotificationLineSupport {
    private NotifyDescriptor nd;

    NotificationLineSupport (NotifyDescriptor descriptor) {
        this.nd = descriptor;
    }

    /** Sets a information message.
     *
     * @param msg information message
     */
    public final void setInformationMessage (String msg) {
        nd.setInformationMessage (msg);
    }

    /** Gets a information message.
     * 
     * @return information message or <code>null</code> if other type of message was set
     */
    public final String getInformationMessage () {
        return nd.getInformationMessage ();
    }

    /** Sets a warning message.
     *
     * @param msg warning message
     */
    public final void setWarningMessage (String msg) {
        nd.setWarningMessage (msg);
    }

    /** Gets a warning message.
     *
     * @return warning message or <code>null</code> if other type of message was set
     */
    public final String getWarningMessage () {
        return nd.getWarningMessage ();
    }

    /** Sets a error message.
     *
     * @param msg error message
     */
    public final void setErrorMessage (String msg) {
        nd.setErrorMessage (msg);
    }

    /** Gets a error message.
     *
     * @return error message or <code>null</code> if other type of message was set
     */
    public final String getErrorMessage () {
        return nd.getErrorMessage ();
    }

    /** Clears messages.
     *
     */
    public final void clearMessages () {
        nd.clearMessages ();
    }

}
