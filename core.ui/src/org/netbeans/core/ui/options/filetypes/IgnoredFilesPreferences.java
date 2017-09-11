/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    static final String DEFAULT_IGNORED_FILES = "^(CVS|SCCS|vssver.?\\.scc|#.*#|%.*%|_svn)$|~$|^\\.(git|hg|svn|cache|DS_Store)$|^Thumbs.db$"; //NOI18N
    static private String syntaxError;

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
