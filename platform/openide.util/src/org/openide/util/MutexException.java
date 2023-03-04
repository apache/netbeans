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

package org.openide.util;

/** Encapsulates other exceptions thrown from a mutex method.
*
* @see Mutex.ExceptionAction
* @see Mutex#readAccess(Mutex.ExceptionAction)
* @see Mutex#writeAccess(Mutex.ExceptionAction)
*
* @author Jaroslav Tulach
*/
public class MutexException extends Exception {
    static final long serialVersionUID = 2806363561939985219L;

    /** encapsulate exception*/
    private Exception ex;

    /** Create an encapsulated exception.
    * @param ex the exception
    */
    public MutexException(Exception ex) {
        super(ex.toString());
        this.ex = ex;
    }

    /** Get the encapsulated exception.
    * @return the exception
    */
    public Exception getException() {
        return ex;
    }

    @Override
    public Throwable getCause() {
        return ex;
    }

}
