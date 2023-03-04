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

package org.openide;

import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A stub implementation of org.openide.ErrorManager to enable the tab control
 * to be used as a standalone jar.  Does nothing except print to stderr.
 *
 * @author Tim Boudreau
 */
public final class ErrorManager extends Object {
    private static final ErrorManager INSTANCE = new ErrorManager();

    // XXX note that these levels accidentally used hex rather than binary,
    // so it goes 0, 1, 16, 256, ....
    // Unfortunately too late to change now: public int constants are part of the
    // API - documented, inlined into compiled code, etc.
    
    /**
     * Undefined severity.
     * May be used only in {@link #notify(int, Throwable)}
     * and {@link #annotate(Throwable, int, String, String, Throwable, Date)}.
     */
    public static final int UNKNOWN = 0x00000000;
    /** Message that would be useful for tracing events but which need not be a problem. */
    public static final int INFORMATIONAL = 0x00000001;
    /** Something went wrong in the software, but it is continuing and the user need not be bothered. */
    public static final int WARNING = 0x00000010;
    /** Something the user should be aware of. */
    public static final int USER = 0x00000100;
    /** Something went wrong, though it can be recovered. */
    public static final int EXCEPTION = 0x00001000;
    /** Serious problem, application may be crippled. */
    public static final int ERROR = 0x00010000;

    public static ErrorManager getDefault () {
        return INSTANCE;
    }

    public Throwable annotate (
        Throwable t, int severity,
        String message, String localizedMessage,
        Throwable stackTrace, java.util.Date date
    ) {
        if (stackTrace != null) {
            stackTrace.printStackTrace();
        }
        return stackTrace;
    }

    public void notify (int severity, Throwable t) {
        t.printStackTrace();
    }

    public final void notify (Throwable t) {
        notify(UNKNOWN, t);
    }

    public void log(int severity, String s) {
        System.err.println(s);
    }

    public final void log(String s) {
        log(INFORMATIONAL, s);
    }
    
    public boolean isLoggable (int severity) {
        return true;
    }

    public ErrorManager getInstance(String name) {
        return getDefault();
    }
   
    public final Throwable annotate (
        Throwable t, String localizedMessage
    ) {
        return annotate (t, UNKNOWN, null, localizedMessage, null, null);
    }

    
    public final Throwable annotate (Throwable target, Throwable t) {
        return annotate (target, UNKNOWN, null, null, t, null);        
    }

}
