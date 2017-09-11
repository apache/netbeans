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

package org.netbeans.modules.editor.errorstripe.privatespi;

import java.awt.Color;
import java.text.MessageFormat;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**Represents a status of a {@link Mark}.
 *
 * @author Jan Lahoda
 */
public final class Status implements Comparable {

    /**Status OK.
     */
    private static final int STATUS_OK_NUMBER = 0;

    /**Status warning.
     */
    private static final int STATUS_WARNING_NUMBER = 1;

    /**Status error.
     */
    private static final int STATUS_ERROR_NUMBER = 2;
    
    /**Status OK.
     */
    public static final Status STATUS_OK = new Status(STATUS_OK_NUMBER);
    
    /**Status warning.
     */
    public static final Status STATUS_WARNING = new Status(STATUS_WARNING_NUMBER);
    
    /**Status error.
     */
    public static final Status STATUS_ERROR = new Status(STATUS_ERROR_NUMBER);
    
    private static final Status[] VALUES = new Status[] {STATUS_OK, STATUS_WARNING, STATUS_ERROR};
    
    private static final Color[] DEFAULT_STATUS_COLORS = new Color[] {Color.GREEN, new Color(0xE1AA00), new Color(0xFF2A1C)};
    
    private int status;
    
    /**Creates a Status with a given status value.
     *
     * @param status status value to use
     * @see #STATUS_ERROR
     * @see #STATUS_WARNING
     * @see #STATUS_OK
     * @throws IllegalArgumentException if one of the provided statuses is something
     *                               else then {@link #STATUS_ERROR},
     *                                         {@link #STATUS_WARNING} and
     *                                         {@link #STATUS_OK}
     */
    private Status(int status) throws IllegalArgumentException {
        if (status != STATUS_ERROR_NUMBER && status != STATUS_WARNING_NUMBER && status != STATUS_OK_NUMBER)
            throw new IllegalArgumentException("Invalid status provided: " + status); // NOI18N
        this.status = status;
    }
    
    /**Returns the numerical status assigned to this {@link Status}.
     *
     * @return numerical status
     */
    private int getStatus() {
        return status;
    }

    /**{@inheritDoc}*/
    public int compareTo(Object o) {
        Status remote = (Status) o;
        
        if (status > remote.status) {
            return 1;
        }
        
        if (status < remote.status) {
            return -1;
        }
        
        return 0;
    }
    
    /**{@inheritDoc}*/
    public boolean equals(Object o) {
        if (!(o instanceof Status)) {
            return false;
        }
        
        Status remote = (Status) o;
        
        return    status == remote.status;
    }
    
    /**{@inheritDoc}*/
    public int hashCode() {
        return 43 ^ status;
    }
    
    private static String[] STATUS_NAMES = new String[] {
        "OK", "WARNING", "ERROR" // NOI18N
    };
    
    /**Returns a {@link String} representation of the {@link Status}.
     * The format of the {@link String} is not specified.
     * This method should only be used for debugging purposes.
     *
     * @return {@link String} representation of this object
     */
    public String toString() {
        return "[Status: " + STATUS_NAMES[getStatus()] + "]"; // NOI18N
    }
    
    /**Return the more important status out of the two given statuses.
     * The statuses are ordered as follows:
     * {@link #STATUS_ERROR}&gt;{@link #STATUS_WARNING}&gt;{@link #STATUS_OK}.
     *
     * @param first one provided status
     * @param second another provided status
     * @return the more important status out of the two provided statuses
     * @throws IllegalArgumentException if one of the provided statuses is something
     *                               else then {@link #STATUS_ERROR},
     *                                         {@link #STATUS_WARNING} and
     *                                         {@link #STATUS_OK}
     */
    public static Status getCompoundStatus(Status first, Status second) throws IllegalArgumentException {
        if (first != STATUS_ERROR && first != STATUS_WARNING && first != STATUS_OK)
            throw new IllegalArgumentException("Invalid status provided: " + first); // NOI18N
        
        if (second != STATUS_ERROR && second != STATUS_WARNING && second != STATUS_OK)
            throw new IllegalArgumentException("Invalid status provided: " + second); // NOI18N
        
        return VALUES[Math.max(first.getStatus(), second.getStatus())];
    }

    /**Returns default {@link Color} for a given {@link Status}.
     *
     * @param s {@link Status} for which default color should be found
     * @return default {@link Color} for a given {@link Status}
     */
    public static Color getDefaultColor(Status s) {
        return DEFAULT_STATUS_COLORS[s.getStatus()];
    }
}
