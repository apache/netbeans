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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.libs.common;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 */
public class DLightLibsCommonLogger {

    private static final Logger instance = Logger.getLogger("dlight.libs.common.logger"); // NOI18N

    private static boolean assertionsEnabled = false;
    
    /** for test purposes */
    private static volatile Throwable lastAssertion;
    private static final Set<StackElementArray> toStringStacks;

    static {
        assert (assertionsEnabled = true);
        toStringStacks = assertionsEnabled ? StackElementArray.createSet() : null;
    }
    
    public static Throwable getLastAssertion() {
        return lastAssertion;
    }
    
    public static boolean isDebugMode() {
        return assertionsEnabled;
    }

    private DLightLibsCommonLogger() {}


    public static java.util.logging.Logger getInstance() {
        return instance;
    }

    public static void assertTrueInConsole(boolean value, String message) {
        if (assertionsEnabled && !value) {
            instance.log(Level.INFO, message);
        }
    }

    public static void assertTrue(boolean value) {
        if (assertionsEnabled && !value) {
            String message = "Assertion error"; //NOI18N
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertTrue(boolean value, String message) {
        if (assertionsEnabled && !value) {
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertFalse(boolean value) {
        if (assertionsEnabled && value) {
            String message = "Assertion error"; //NOI18N
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertFalse(boolean value, String message) {
        if (assertionsEnabled && value) {
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }
    
    public static void printStackTraceOnce(Throwable cause, Level level, boolean once, int stackCompareSize) {
        if (assertionsEnabled) {
            if (!once || StackElementArray.addStackIfNew(cause.getStackTrace(), toStringStacks, stackCompareSize)) {
                instance.log(level, cause.getMessage(), lastAssertion = cause);
            }
        }
    }    
    
    public static void printStackTraceOnce(Throwable cause, Level level, boolean once) {
        printStackTraceOnce(cause, level, once, 6);
    }

    public static void assertNonUiThread(String message, Level level, boolean once) {
        if (assertionsEnabled && SwingUtilities.isEventDispatchThread()) {
            if (!once || StackElementArray.addStackIfNew(toStringStacks, 8)) {
                instance.log(level, message, lastAssertion = new Exception(message));
            }
        }
    }
    
    public static void assertNonUiThread() {
        assertNonUiThread("Should not be called from UI thread", Level.SEVERE, false); //NOI18N
    }

    public static void assertNonUiThreadOnce(Level level) {
        assertNonUiThread("Should not be called from UI thread", level, true); //NOI18N
    }

    public static void finest(Exception exception) {
        instance.log(Level.FINEST, "FYI:", exception);
    }
}
