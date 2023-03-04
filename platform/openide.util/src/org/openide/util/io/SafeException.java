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

package org.openide.util.io;

/** Special IOException that is used to signal that the write operation
* failed but the underlaying stream is not corrupted and can be used
* for next operations.
*
*
* @author Jaroslav Tulach, Jesse Glick
*/
public class SafeException extends FoldingIOException {
    private static final long serialVersionUID = 4365154082401463604L;

    /** the exception encapsulated */
    private Exception ex;

    /** Default constructor.
     * @param ex encapsuled exception
    */
    public SafeException(Exception ex) {
        super(ex, null);
        this.ex = ex;
    }

    /** @return the encapsulated exception.
    */
    public Exception getException() {
        return ex;
    }

    public Throwable getCause() {
        return ex;
    }

}
