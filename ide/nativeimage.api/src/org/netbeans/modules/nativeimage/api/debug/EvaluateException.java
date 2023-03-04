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
package org.netbeans.modules.nativeimage.api.debug;

/**
 * Thrown when evaluation in the native debugger fails.
 *
 * @since 0.1
 */
public class EvaluateException extends Exception {

    /**
     * Constructs an instance of <code>EvaluateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     * @since 0.1
     */
    public EvaluateException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>EvaluateException</code> from an
     * existing exception.
     *
     * @param t exception.
     * @since 0.1
     */
    public EvaluateException(Throwable t) {
        super(t.getLocalizedMessage(), t);
    }
}
