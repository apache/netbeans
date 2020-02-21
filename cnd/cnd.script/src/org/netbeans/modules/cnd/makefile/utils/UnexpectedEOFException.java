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

package org.netbeans.modules.cnd.makefile.utils;

import java.io.IOException;

/**
 *  Exception thrown by FortranReader when EOF is raised at an unexpected time. This
 *  usually means in the middle of a line of source.
 */
public class UnexpectedEOFException extends IOException {

    /**
     * Constructs an <code>UnexpectedEOFException</code> with <code>null</code>
     * as its error detail message.
     */
    public UnexpectedEOFException() {
	super();
    }

    /**
     * Constructs an <code>UnexpectedEOFException</code> with the specified detail
     * message. The error message string <code>s</code> can later be
     * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param   s   the detail message.
     */
    public UnexpectedEOFException(String s) {
	super(s);
    }
}
