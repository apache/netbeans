/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.autoupdate;

/**
 * Thrown to indicate that operation failed
 * @see OperationSupport
 * @see InstallSupport
 * @author Radek Matous
 */
public final class OperationException extends Exception {
    private ERROR_TYPE error;
    private String msg;
    /**
     * Define the failure
     */
    public static enum ERROR_TYPE {
        /**
         * Problem with proxy configuration
         */
        PROXY,
        /**
         * Installation of custom component failed
         */
        INSTALLER,
        /**
         * Installation of plugin failed
         */
        INSTALL,
        /**
         * Activation of plugin failed
         */
        ENABLE,
        /**
         * Uninstallation of plugin failed
         */
        UNINSTALL,
        /**
         * Lack of write permission to write in installation directory
         * @since 1.33
         */
        WRITE_PERMISSION,
        MODIFIED
    }       
 
    /**
     * Constructs an <code>OperationException</code>
     * @param error the definition of failure
     */
    public OperationException(ERROR_TYPE error) {
        super (/*e.g.message from ERR*/);
        this.error = error;
        msg = error.toString ();
    }
    
    /**
     * Constructs an <code>OperationException</code>
     * @param error the definition of failure
     * @param x the cause (<code>x.getLocalizedMessage</code> is saved for later retrieval by the
     *         {@link #getLocalizedMessage()} method)
     */
    public OperationException(ERROR_TYPE error, Exception x) {
        super (x);
        this.error = error;
        msg = x.getLocalizedMessage ();
    }
    
    /**
     * Constructs an <code>OperationException</code>
     * @param error the definition of failure
     * @param message (is saved for later retrieval by the
     * {@link #getLocalizedMessage()} method)
     */
    public OperationException(ERROR_TYPE error, String message) {
        super (message);
        this.error = error;
        msg = message;
    }
    
    @Override
    public String getLocalizedMessage () {
        return msg;
    }
    
    /**
     * @return the definition of failure
     */
    public ERROR_TYPE getErrorType() {return error;}

    @Override
    public String toString() {
        String s = getClass().getName();
        return (msg != null) ? (s + "[" + error + "]: " + msg) : s;
    }
    
    
    
}
