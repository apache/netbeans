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
package org.netbeans.modules.remote.support;

import java.util.logging.Level;
import javax.swing.SwingUtilities;

public class RemoteLogger {

    private static final java.util.logging.Logger instance =
            java.util.logging.Logger.getLogger("remote.support.logger"); // NOI18N
    
    private static boolean assertionsEnabled = false;
    
    /** for test purposes */
    private static volatile Exception lastAssertion;

    static {
        assert (assertionsEnabled = true);
    }

    public static Exception getLastAssertion() {
        return lastAssertion;
    }
    
    public static boolean isDebugMode() {
        return assertionsEnabled;
    }

    private RemoteLogger() {}


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

    public static void assertNonUiThread(String message) {
        if (assertionsEnabled && SwingUtilities.isEventDispatchThread()) {
            instance.log(Level.SEVERE, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertNonUiThread() {
        assertNonUiThread("Should not be called from UI thread"); //NOI18N
    }

    public static void finest(Exception exception) {
        instance.log(Level.FINEST, "FYI:", exception);
    }
}
