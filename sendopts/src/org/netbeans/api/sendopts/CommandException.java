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

package org.netbeans.api.sendopts;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/** Signals that something is wrong when processing the command line arguments.
 *
 * @author Jaroslav Tulach
 */
public final class CommandException extends Exception {
    private final int exitCode;
    private final String locMsg;
    
    /** Simple constructor for the CommandException to indicate that a 
     * processing error occurred. The provided <code>exitCode</code> represents
     * the value to be usually send to as a return value to {@link System#exit}.
     * 
     * @param exitCode the value, should be different than zero
     */
    public CommandException(int exitCode) {
        this("Error code: " + exitCode, exitCode, null); // NOI18N
    }

    /** Creates new exception with a localised message assigned to it.
     * @param exitCode exit code to report from the exception
     * @param locMsg localised message
     */
    public CommandException(int exitCode, String locMsg) {
        this("Error code: " + exitCode, exitCode, locMsg); // NOI18N
    }
    
    
    /** Creates a new instance of CommandException */
    CommandException(String msg, int exitCode, String locMsg) {
        super(msg);
        this.exitCode = exitCode;
        this.locMsg = locMsg;
    }
    /** Creates a new instance of CommandException */
    CommandException(String msg, int exitCode) {
        this(msg, exitCode, null);
    }

    /** Returns an exit code for this exception.
     * @return integer exit code, zero if exited correctly
     */
    public int getExitCode() {
        return exitCode;
    }

    /** Localized message describing the problem that is usually printed
     * to the user.
     */
    public String getLocalizedMessage() {
        if (locMsg != null) {
            return locMsg;
        }
        if (getCause() != null) {
            return getCause().getLocalizedMessage();
        }
        return super.getLocalizedMessage();
    }
}
