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

package org.netbeans.junit;

/*
 * AssertionFileFailedError.java
 *
 * Created on March 21, 2002, 3:05 PM
 */
import junit.framework.AssertionFailedError;
import java.io.PrintWriter;
import java.io.PrintStream;

/** Error containing nested Exception.
 * It describes the failure and holds and print also the original Exception.
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class AssertionFailedErrorException extends AssertionFailedError {

    /** contains Exception that caused AssertionFailedError
     */    
    protected Throwable nestedException;

    /** Creates new AssertionFailedErrorException
     * @param nestedException contains Exception that caused AssertionFailedError
     */
    public AssertionFailedErrorException(Throwable nestedException) {
        this(null, nestedException);
    }

    /** Creates new AssertionFailedErrorException 
     *  @param message The error description menssage.
     *  @param nestedException contains Exception that caused AssertionFailedError
     */
    public AssertionFailedErrorException(String message, Throwable nestedException) {
        super(message);
        this.nestedException = nestedException;
    }
    
    /** prints stack trace of assertion error and nested exception into System.err
     */    
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /** prints stack trace of assertion error and nested exception
     * @param err PrintWriter where to print stack trace
     */    
    public void printStackTrace(PrintWriter err) {
        synchronized (err) {
            super.printStackTrace(err);
            err.println("\nNested Exception is:");
            nestedException.printStackTrace(err);
        }
    }

    /** prints stack trace of assertion error and nested exception
     * @param err PrintStream where to print stack trace
     */    
    public void printStackTrace(PrintStream err) {
        synchronized (err) {
            super.printStackTrace(err);
            err.println("\nNested Exception is:");
            nestedException.printStackTrace(err);
        }
    }
}
