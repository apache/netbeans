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

package org.netbeans.api.debugger.jpda;


/**
 * Notification about bad expression.
 *
 * @author   Jan Jancura
 */
public class InvalidExpressionException extends Exception {

    private final String message;
    private final boolean isFromApp;

    /**
     * Constructs a InvalidExpressionException with given message.
     *
     * @param message a exception message
     */
    public InvalidExpressionException (String message) {
        this(message, null);
    }

    /**
     * Constructs a InvalidExpressionException for a given target exception.
     *
     * @param t a target exception
     */
    public InvalidExpressionException (Throwable t) {
        this(null, t);
    }
    
    /**
     * Constructs a InvalidExpressionException for a given target exception.
     *
     * @param t a target exception
     * @param isFromApp <code>true</code> when the target exception is a mirror
     *                  of an application-level exception, <code>false</code>
     *                  otherwise.
     * @since 3.7
     */
    public InvalidExpressionException (Throwable t, boolean isFromApp) {
        this(null, t, isFromApp);
    }

    /**
     * Constructs a InvalidExpressionException with given message and target exception.
     *
     * @param message a exception message
     * @param t a target exception
     * @since 3.1
     */
    public InvalidExpressionException (String message, Throwable t) {
        this(message, t, false);
    }

    /**
     * Constructs a InvalidExpressionException with given message and target exception.
     *
     * @param message a exception message
     * @param t a target exception
     * @param isFromApp <code>true</code> when the target exception is a mirror
     *                  of an application-level exception, <code>false</code>
     *                  otherwise.
     * @since 3.7
     */
    public InvalidExpressionException (String message, Throwable t, boolean isFromApp) {
        super(message, t);
        // Assert that application-level exceptions have the appropriate mirror:
        assert isFromApp && t != null || !isFromApp;
        this.message = message;
        this.isFromApp = isFromApp;
    }

    @Override
    public String getMessage() {
        Throwable cause = getCause();
        if (cause != null &&
            (message == null || message.trim().isEmpty())) {
            
            return cause.getMessage();
        }
        return message;
    }
    
    
    
    /**
     * Get the thrown target exception.
     *
     * @return the thrown target exception
     */
    public Throwable getTargetException () {
        return getCause();
    }

    /**
     * Test whether the target exception is a mirror of an application-level
     * exception.
     * @return <code>true</code> when the target exception represents an
     *         exception in the application code, <code>false</code> otherwise.
     * @since 3.7
     */
    public final boolean hasApplicationTarget() {
        return isFromApp;
    }
}

