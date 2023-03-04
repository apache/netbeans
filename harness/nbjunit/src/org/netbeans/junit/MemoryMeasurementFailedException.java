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


/*
 * MemoryMeasurementFailedException.java
 *
 * Created on August 11, 2003, 3:43 PM
 */

package org.netbeans.junit;

/** Throws when MemoryMeasurement methods are having some problem
 * @author Martin Brehovsky
 */
public class MemoryMeasurementFailedException extends java.lang.RuntimeException {

    /**
     * Creates a new instance of <code>InitializationException</code> without detail message.
     */
    public MemoryMeasurementFailedException() {
    }


    /**
     * Constructs an instance of <code>InitializationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MemoryMeasurementFailedException(String msg) {
        super(msg);
    }
    
    /** Constructs an instance of <code>InitializationException</code> with the specified detail message.
     * @param cause Cause of the exception
     * @param msg the detail message.
     */
    public MemoryMeasurementFailedException(String msg, Throwable cause) {
        super(msg,cause);
    }    
}
