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
