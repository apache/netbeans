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

package org.netbeans.modules.javadoc.settings;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/** Options for JavaDoc
*
* @author Petr Hrebejk
* @author Jan Pokorsky
*/
public final class DocumentationSettings {

    private static final String PROP_SEARCH_SORT         = "idxSearchSort";   //NOI18N
    private static final String PROP_SEARCH_NO_HTML      = "idxSearchNoHtml";   //NOI18N
    private static final String PROP_SEARCH_SPLIT        = "idxSearchSplit";       //NOI18N
       
    private static final DocumentationSettings INSTANCE = new DocumentationSettings();

    private DocumentationSettings() {
    }

    /**
     * Gets an instance of documentation settings
     * @return the instance
     */
    public static DocumentationSettings getDefault(){
        return INSTANCE;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(DocumentationSettings.class);
    }

    /** Getter for property idxSearchSort.
     *@return Value of property idxSearchSort.
     */
    public String getIdxSearchSort() {
        Preferences prefs = getPreferences();
        return prefs.get(PROP_SEARCH_SORT, "A"); // NOI18N
    }

    /** Setter for property idxSearchSort.
     *@param idxSearchSort New value of property idxSearchSort.
     */
    public void setIdxSearchSort(String idxSearchSort) {
        Preferences prefs = getPreferences();
        prefs.put(PROP_SEARCH_SORT, idxSearchSort);
    }

    /** Getter for property idxSearchNoHtml.
     *@return Value of property idxSearchNoHtml.
     */
    public boolean isIdxSearchNoHtml() {
        Preferences prefs = getPreferences();
        return prefs.getBoolean(PROP_SEARCH_NO_HTML, false);
    }

    /** Setter for property idxSearchNoHtml.
     *@param idxSearchNoHtml New value of property idxSearchNoHtml.
     */
    public void setIdxSearchNoHtml(boolean idxSearchNoHtml) {
        Preferences prefs = getPreferences();
        prefs.putBoolean(PROP_SEARCH_NO_HTML, idxSearchNoHtml);
    }

    /** Getter for property idxSearchSplit.
     *@return Value of property idxSearchSplit.
     */
    public int getIdxSearchSplit() {
        Preferences prefs = getPreferences();
        return prefs.getInt(PROP_SEARCH_SPLIT, 50);
    }

    /** Setter for property idxSearchSplit.
     *@param idxSearchSplit New value of property idxSearchSplit.
     */
    public void setIdxSearchSplit(int idxSearchSplit) {
        Preferences prefs = getPreferences();
        prefs.putInt(PROP_SEARCH_SPLIT, idxSearchSplit);
    }

}
