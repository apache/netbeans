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

package org.netbeans.modules.derby;

/**
 *
 * @author pj97932
 */
public class SearchUtil {

    static final int FOUND = -1;

    /** Returns the number of characters from the searchedFor string that are at the end of the buffer,
     * or FOUND if found the whole string
     **/
    static int checkForString(String searchedFor, int searchStart, char[] buf, int bufLen) {
        if (searchedFor.length() == 0)
            throw new IllegalArgumentException();
        if (searchStart > 0) {
            // already have a substring
            int res = checkPosition(searchedFor, searchStart, buf, bufLen, 0);
            if (res == FOUND)
                return FOUND;
        }
        for (int i = 0; i < bufLen; i++) {
            if (buf[i] == searchedFor.charAt(searchStart)) {
                // found the first character
                int res = checkPosition(searchedFor, 0, buf, bufLen, i);
                if (res != 0)
                    return res;
            }
        }
        return 0;
    }

    /** Checks whether the buffer contains a portion of searchedFor from searchStart, starting from buffer
     * position bufFrom.
     *  Returns the number of characters from the searchedFor string that are at the end of the buffer,
     * or FOUND if found the whole string
     */
    static int checkPosition(String searchedFor, int searchStart, char[] buf, int bufLen, int bufFrom) {
        String realSearch = searchedFor.substring(searchStart);
        for (int i = 0; i < realSearch.length(); i++) {
            int bufPos = bufFrom + i;
            if (bufPos >= bufLen) {
                return searchStart + i;
            }
            if (buf[bufPos] != realSearch.charAt(i))
                return 0;
        }
        return FOUND;
    }
        
    
}
