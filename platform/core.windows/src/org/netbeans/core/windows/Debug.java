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


package org.netbeans.core.windows;


import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Utility class for debugging support of window system.
 *
 * @author  Peter Zavadsky
 */
public abstract class Debug {
    /** Creates a new instance of Debug */
    private Debug() {
    }


    public static boolean isLoggable(Class clazz) {
        return Logger.getLogger(clazz.getName()).isLoggable(Level.FINE);
    }

    /** Logs debug message depending whether the logging is required based on class name.
     * @see org.openide.ErrorManager.getInstance(String) */
    public static void log(Class clazz, String message) {
        Logger.getLogger(clazz.getName()).fine(message);
    }
    
    
    public static void dumpStack(Class clazz) {
        // log(Class,String) only has an effect if INFORMATIONAL logging enabled on that prefix
        if(Logger.getLogger(clazz.getName()).isLoggable(Level.FINE)) {
            StringWriter sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            log(clazz, sw.getBuffer().toString());
        }
    }
}
