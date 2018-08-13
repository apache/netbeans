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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.InternalException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * Reports JDI exceptions and log JDI method calls.
 * 
 * @author Martin Entlicher
 */
public final class JDIExceptionReporter {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdi");    // NOI18N

    public static final Object RET_VOID = new Object();

    private static final ThreadLocal<Long> callStartTime = new ThreadLocal<Long>();

    private JDIExceptionReporter() {}

    public static void report(InternalException iex) {
        iex = (InternalException) Exceptions.attachMessage(iex,
                "An unexpected internal exception occured in debug interface layer. Please submit this to your JVM vendor.");
        Exceptions.printStackTrace(iex);
    }

    public static boolean isLoggable() {
        return logger.isLoggable(java.util.logging.Level.FINER);
    }

    public static void logCallStart(String className, String methodName, String msg, Object[] args) {
        try {
            logger.log(java.util.logging.Level.FINER, msg, args);
        } catch (Exception exc) {
            logger.log(java.util.logging.Level.FINER, "Logging of "+className+"."+methodName+"() threw exception:", exc);
        }
        callStartTime.set(System.nanoTime());
    }

    public static void logCallEnd(String className, String methodName, Object ret) {
        long t2 = System.nanoTime();
        long t1 = callStartTime.get();
        callStartTime.remove();
        logger.log(java.util.logging.Level.FINER, "          {0}.{1}() returned after {2} ns, return value = {3}",
                   new Object[] {className, methodName, (t2 - t1), (RET_VOID == ret) ? "void" : ret});
        if (ret instanceof Throwable) {
            logger.log(Level.FINER, "", (Throwable) ret);
        }
    }

}
