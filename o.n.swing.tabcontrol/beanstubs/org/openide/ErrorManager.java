/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
