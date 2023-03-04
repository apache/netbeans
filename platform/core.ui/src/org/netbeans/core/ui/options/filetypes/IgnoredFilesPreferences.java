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
package org.netbeans.core.ui.options.filetypes;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;

/** 
 * Loads, stores and checks validity of property with ignored files pattern.
 *
 * @author Jiri Skrivanek
 */
final class IgnoredFilesPreferences {

    /** Files that should be ignored
     *
     * DO NOT CHANGE THIS PROPERTY NAME without checking that
     * this property name was changed also in GlobalVisibilityQueryImpl
     * in module org.netbeans.modules.masterfs.
     */
    private static final String PROP_IGNORED_FILES = "IgnoredFiles"; // NOI18N
    private static final String PROP_IGNORE_HIDDEN_FILES_IN_USER_HOME
            = "IgnoreHiddenFilesInUserHome";                           // NOI18N
    /** Default ignored files pattern. Pattern \.(cvsignore|svn|DS_Store) is covered by ^\..*$ **/
    static final String DEFAULT_IGNORED_FILES = "^(CVS|SCCS|vssver.?\\.scc|#.*#|%.*%|_svn)$|~$|^\\.(git|hg|svn|cache|gradle|DS_Store)$|^Thumbs.db$"; //NOI18N
    private static String syntaxError;

    private IgnoredFilesPreferences() {
    }

    private static Preferences getPreferences() {
        // use this for backward compatibility
        return NbPreferences.root().node("org/netbeans/core");  //NOI18N
    }

    /** Returns ignored files pattern stored in userdir or default pattern.
     * @return ignored files pattern stored in userdir or default pattern.
     */
    static String getIgnoredFiles() {
        return getPreferences().get(PROP_IGNORED_FILES, DEFAULT_IGNORED_FILES);
    }

    /** Sets ignored files pattern property. If it is not valid, inform user.
     * @param ignoredFiles ignored files pattern
     */
    static void setIgnoredFiles(String ignoredFiles) {
        if(isValid(ignoredFiles)) {
            getPreferences().put(PROP_IGNORED_FILES, ignoredFiles);
        } else {
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(syntaxError);
            DialogDisplayer.getDefault().notifyLater(descriptor);
        }
    }

    /**
     * Returns true if hidden files in user's home folder should be ignored
     * (e.i. not displayed in Favorites windows).
     *
     * @return True to ignore hidden files in user's home folder, false to show
     * them.
     */
    static boolean isIgnoreHiddenFilesInUserHome() {
        return getPreferences().getBoolean(
                PROP_IGNORE_HIDDEN_FILES_IN_USER_HOME, true);
    }

    /**
     * Set whether hidden files in home folder should be ignored (i.e. not
     * displayed in Favorites window).
     *
     * @param ignore True to ignore hidden files in user's home folder, false to
     * show them.
     */
    static void setIgnoreHiddenFilesInUserHome(boolean ignore) {
        getPreferences().putBoolean(
                PROP_IGNORE_HIDDEN_FILES_IN_USER_HOME, ignore);
    }

    /** Returns true if ignored files pattern is valid, false otherwise and
     * syntax error message can be obtained by getSyntaxError method.
     * @param ignoredFiles ignored files pattern
     * @return true if ignored files pattern is valid, false otherwise.
     */
    static boolean isValid(String ignoredFiles) {
        try {
            Pattern.compile(ignoredFiles);
        } catch (PatternSyntaxException e) {
            syntaxError = e.getLocalizedMessage();
            return false;
        }
        syntaxError = null;
        return true;
    }

    /** Returns syntax error message if last call of isValid returned false. Otherwise
     * it returns null.
     * @return syntax error message or null
     */
    static String getSyntaxError() {
        return syntaxError;
    }
}
