/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
