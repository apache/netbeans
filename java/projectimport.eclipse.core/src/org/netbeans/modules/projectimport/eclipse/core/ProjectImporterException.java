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

package org.netbeans.modules.projectimport.eclipse.core;

/**
 * Indicates that the parsed project is invalid.
 *
 * @author mkrauskopf
 */
public class ProjectImporterException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>ProjectImporterException</code> without
     * detail message.
     */
    public ProjectImporterException() {/*empty constructor*/}

    /**
     * Constructs an instance of <code>ProjectImporterException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ProjectImporterException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>ProjectImporterException</code> with the
     * specified cause.
     *
     * @param cause the cause exception
     */
    public ProjectImporterException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructs an instance of <code>ProjectImporterException</code> with the
     * specified detail message and cause.
     *
     * @param msg the detail message.
     * @param cause the cause exception
     */
    public ProjectImporterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
