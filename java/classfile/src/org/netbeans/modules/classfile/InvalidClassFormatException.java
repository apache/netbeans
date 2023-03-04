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
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

import java.io.IOException;

/**
 * Exception thrown when a classfile with an invalid format is detected.
 *
 * @author Thomas Ball
 */
public final class InvalidClassFormatException extends IOException {
    /**
     * Constructs an <code>InvalidClassFormatException</code> with
     * <code>null</code> as its error detail message.
     */
    InvalidClassFormatException() {
	super();
    }

    /**
     * Constructs an <code>InvalidClassFormatException</code> with the 
     * specified detail message. The error message string <code>s</code> 
     * can later be retrieved by the 
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param   s   the detail message.
     */
    InvalidClassFormatException(String s) {
	super(s);
    }


    /**
     * Constructs an <code>InvalidClassFormatException</code> with the 
     * specified cause, which is used to define the error message.
     *
     * @param cause   the exception which is used to define the error message.
     */
    InvalidClassFormatException(Throwable cause) {
        super(cause.getLocalizedMessage());
        initCause(cause);
    }
    
    private static final long serialVersionUID = -7043855006167696889L;
}

