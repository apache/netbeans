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

package org.netbeans.api.autoupdate;

/**
 * Thrown to indicate that operation failed
 * @see OperationSupport
 * @see InstallSupport
 * @author Radek Matous
 */
public final class OperationException extends Exception {
    private ERROR_TYPE error;
    private String msg;
    /**
     * Define the failure
     */
    public static enum ERROR_TYPE {
        /**
         * Problem with proxy configuration
         */
        PROXY,
        /**
         * Installation of custom component failed
         */
        INSTALLER,
        /**
         * Installation of plugin failed
         */
        INSTALL,
        /**
         * Activation of plugin failed
         */
        ENABLE,
        /**
         * Uninstallation of plugin failed
         */
        UNINSTALL,
        /**
         * Lack of write permission to write in installation directory
         * @since 1.33
         */
        WRITE_PERMISSION,
        MODIFIED,
        /** missing and required Unpack200 implementation
         * @since 1.65
         */
        MISSING_UNPACK200
    }       
 
    /**
     * Constructs an <code>OperationException</code>
     * @param error the definition of failure
     */
    public OperationException(ERROR_TYPE error) {
        super (/*e.g.message from ERR*/);
        this.error = error;
        msg = error.toString ();
    }
    
    /**
     * Constructs an <code>OperationException</code>
     * @param error the definition of failure
     * @param x the cause (<code>x.getLocalizedMessage</code> is saved for later retrieval by the
     *         {@link #getLocalizedMessage()} method)
     */
    public OperationException(ERROR_TYPE error, Exception x) {
        super (x);
        this.error = error;
        msg = x.getLocalizedMessage ();
    }
    
    /**
     * Constructs an <code>OperationException</code>
     * @param error the definition of failure
     * @param message (is saved for later retrieval by the
     * {@link #getLocalizedMessage()} method)
     */
    public OperationException(ERROR_TYPE error, String message) {
        super (message);
        this.error = error;
        msg = message;
    }
    
    @Override
    public String getLocalizedMessage () {
        return msg;
    }
    
    /**
     * @return the definition of failure
     */
    public ERROR_TYPE getErrorType() {return error;}

    @Override
    public String toString() {
        String s = getClass().getName();
        return (msg != null) ? (s + "[" + error + "]: " + msg) : s;
    }
    
    
    
}
