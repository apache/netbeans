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
package org.openide.util;


/** Exception that is thrown when the process is about to perform some
* action that requires user confirmation. It can be useful when there
* is a call to a method which cannot open a dialog, but still would like
* to ask the user a question. It can raise this exception and higher level
* parts of the system can/should catch it and present a dialog to the user
* and if the user agrees reinvoke the action again.
* <P>
* The <code>getLocalizedMessage</code> method should return the user question,
* which will be shown to the user in a dialog with OK, Cancel options and
* if the user chooses OK, method <code>ex.confirmed ()</code> will be called.
* <p>
* Since version 8.29 one can just catch the exception and report it to the
* infrastructure of any NetBeans Platform based application (for example
* via {@link Exceptions#printStackTrace(java.lang.Throwable)}) and the
* question dialog will be displayed automatically.
*
* @author Jaroslav Tulach
*/
public abstract class UserQuestionException extends java.io.IOException {
    static final long serialVersionUID = -654358275349813705L;

    /** Creates new exception UserQuestionException
    */
    public UserQuestionException() {
        super();
    }

    /** Creates new exception UserQuestionException with text specified
    * string s.
    * @param s the text describing the exception
    */
    public UserQuestionException(String s) {
        super(s);
    }

    /** Invoke the action if the user confirms the action.
     * @exception java.io.IOException if another I/O problem exists
     */
    public abstract void confirmed() throws java.io.IOException;
}
