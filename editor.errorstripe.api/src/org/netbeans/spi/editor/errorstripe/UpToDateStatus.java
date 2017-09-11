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

package org.netbeans.spi.editor.errorstripe;

/**Up-to-date status enumeration. See {@link UpToDateStatusProvider#getUpToDate}
 * for more information.
 *
 * @author Jan Lahoda
 */
public final class UpToDateStatus implements Comparable {

    /**Up-to-date status saying everything is up-to-date.
     */
    private static final int UP_TO_DATE_OK_VALUE = 0;

    /**Up-to-date status saying that the list of marks is
     * not up-to-date, but a up-to-date list of marks is currently
     * being found.
     */
    private static final int UP_TO_DATE_PROCESSING_VALUE = 1;
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, and nothing is currently done in order to
     * get the up-to-date list.
     */
    private static final int UP_TO_DATE_DIRTY_VALUE = 2;

    /**Up-to-date status saying everything is up-to-date.
     */
    public static final UpToDateStatus UP_TO_DATE_OK = new UpToDateStatus (UP_TO_DATE_OK_VALUE);
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, but a up-to-date list of marks is currently
     * being found.
     */
    public static final UpToDateStatus UP_TO_DATE_PROCESSING = new UpToDateStatus (UP_TO_DATE_PROCESSING_VALUE);
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, and nothing is currently done in order to
     * get the up-to-date list.
     */
    public static final UpToDateStatus UP_TO_DATE_DIRTY = new UpToDateStatus (UP_TO_DATE_DIRTY_VALUE);

    private int status;
    
    /** Creates a new instance of UpToDateStatus */
    private UpToDateStatus(int status) {
        this.status = status;
    }
    
    private int getStatus() {
        return status;
    }
    
    public int compareTo(Object o) {
        UpToDateStatus remote = (UpToDateStatus) o;
        
        return status - remote.status;
    }
    
    public int hashCode() {
        return 73 ^ status;
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof UpToDateStatus))
            return false;
        
        return compareTo(obj) == 0;
    }
    
    private static final String[] statusNames = new String[] {
        "OK",
        "PROCESSING",
        "DIRTY",
    };
    
    public String toString() {
        return statusNames[getStatus()];
    }
}
