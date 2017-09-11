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

package org.openide.filesystems;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Utility methods.
 */
final class ExternalUtil {

    /** Notifies an exception.
     */
    public static void exception(Exception ex) {
        LOG.log(Level.INFO, null, ex);
    }

    /** Copies anotation.
     * @param newEx new exception to annotate
     * @param oldEx old exception to take annotation from
     * @return newEx
     */
    public static Throwable copyAnnotation(Throwable newEx, Throwable oldEx) {
        return newEx.initCause(oldEx);
    }

    /** Annotates the exception with a message.
     */
    public static void annotate(Throwable ex, String msg) {
        Exceptions.attachLocalizedMessage(ex, msg);
    }

    /** Annotates the exception with a message.
     */
    public static Throwable annotate(Throwable ex, Throwable stack) {
        Throwable orig = ex;
        while (ex.getCause() != null) {
            ex = ex.getCause();
        }
        try {
            ex.initCause(stack);
        } catch (IllegalStateException ise) {
            // #164760 - fallback when initCause fails (e.g. for ClassNotFoundException)
            Exception e = new Exception(ex.getMessage(), stack);
            e.setStackTrace(ex.getStackTrace());
            return e;
        }
        return orig;
    }

    final static Logger LOG = Logger.getLogger("org.openide.filesystems"); // NOI18N
    /** Logs a text.
     */
    public static void log(String msg) {
        LOG.fine(msg);
    }

    /** Loads a class of given name
     * @param name name of the class
     * @return the class
     * @exception ClassNotFoundException if class was not found
     */
    public static Class findClass(String name) throws ClassNotFoundException {
        ClassLoader c = Lookup.getDefault().lookup(ClassLoader.class);

        if (c == null) {
            return Class.forName(name);
        } else {
            return Class.forName(name, true, c);
        }
    }

}
