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

package org.netbeans.tax;

/**
 * All exceptions in tree are by default unchecked so they must be
 * declared in method signature if they should behave as checked exceptions.
 * <p>
 * At many places it is accurate just mention in JavaDoc that the method
 * may throw it (if passing the right value is at callee reponsibility).
 * It must be declared just at places where callee can not explicitly
 * guarantee that the exception will not occure so it must check for it.
 * <p>
 * It is a folding exception.
 *
 * @author Libor Kramolis
 */
public class TreeException extends Exception {
    
    /** Serial Version UID */
    private static final long serialVersionUID =1949769568282926780L;
    
    //
    // init
    //
    
    /** Create new TreeException. */
    public TreeException (String msg, Exception exception) {
        super (msg);
        if (exception != null) {
            initCause(exception);
        }
    }
    
    
    /** Creates new TreeException with specified detail message.
     * @param msg detail message
     */
    public TreeException (String msg) {
        this (msg, null);
    }
    
    
    /** Creates new TreeException with specified encapsulated exception.
     * @param exc encapsulated exception
     */
    public TreeException (Exception exc) {
        this(exc.toString(), exc);
    }
    
    
    //
    // itself
    //
    
    /** Get the encapsulated exception.
     * @return encapsulated encapsulated
     */
    public Exception getException () {
        return (Exception) getCause();
    }

}
