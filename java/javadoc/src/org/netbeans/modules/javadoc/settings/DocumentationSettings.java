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
