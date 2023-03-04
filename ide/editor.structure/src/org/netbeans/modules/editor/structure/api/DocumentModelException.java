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

package org.netbeans.modules.editor.structure.api;

/**
 * DocumentModelException is thrown from the DocumentModel's methods.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class DocumentModelException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>DocumentModelException</code> without detail message.
     */
    public DocumentModelException() {
    }

    /**
     * Constructs an instance of <code>DocumentModelException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DocumentModelException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>DocumentModelException</code> with the specified detail message and cause throwable.
     * @param message the detail message.
     * @param t the cause of the exception
     */
    public DocumentModelException(String message, Throwable t) {
        super(message, t);
    }
}
