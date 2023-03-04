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

import java.io.IOException;


/** Encapsulates an exception.
*
* @author Ales Novak
*/
public class FoldingIOException extends IOException {
    static final long serialVersionUID = 1079829841541926901L;

    /** Foreign exception */
    private Throwable t;

    /**
    * @deprecated Better to create a new <code>IOException</code> and use its {@link #initCause} method.
    * @param t a foreign folded Throwable
    */
    @Deprecated
    public FoldingIOException(Throwable t) {
        super(t.getMessage());
        this.t = t;
    }
    
    /** Constructor for SafeException which extends FoldingIOException
     * and is not deprecated.
     */
    FoldingIOException(Throwable t, Object nothing) {
        this(t);
    }

    /** Prints stack trace of the foreign exception */
    @Override
    public void printStackTrace() {
        t.printStackTrace();
    }

    /** Prints stack trace of the foreign exception */
    @Override
    public void printStackTrace(java.io.PrintStream s) {
        t.printStackTrace(s);
    }

    /** Prints stack trace of the foreign exception */
    @Override
    public void printStackTrace(java.io.PrintWriter s) {
        t.printStackTrace(s);
    }

    /**
    * @return toString of the foreign exception
    */
    @Override
    public String toString() {
        return t.toString();
    }

    /**
    * @return getLocalizedMessage of the foreign exception
    */
    @Override
    public String getLocalizedMessage() {
        return t.getLocalizedMessage();
    }
}
