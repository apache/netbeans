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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.commons;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.WARNING;

/**
 * Stores mappings between {@code File}s and bugtracking repositories.
 *
 * @author Marian Petras
 */
public class FileToRepoMappingStorage {

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.util.FileToRepoMappingStorage"); //NOI18N

    private static final String REPOSITORY_FOR_FILE_PREFIX = "repository for "; //NOI18N
    private static final Boolean FIRM_ASSOCIATION = TRUE;
    private static final Boolean LOOSE_ASSOCIATION = FALSE;

    private static FileToRepoMappingStorage instance;

    public synchronized static FileToRepoMappingStorage getInstance() {
        if (instance == null) {
            instance = new FileToRepoMappingStorage();
        }
        return instance;
    }

    public void setFirmAssociation(File file, String repositoryUrl) {
        setAssociation(file, repositoryUrl, true);
    }

    public boolean setLooseAssociation(File file, String repositoryUrl) {
        String firmlyAssociated = getFirmlyAssociatedRepository(file);
        if (firmlyAssociated == null) {
            setAssociation(file, repositoryUrl, false);
            return true;
        } else {
            return false;
        }
    }

    public String getRepository(File file) {
        return getAssociatedRepository(file, null);
    }

    public String getFirmlyAssociatedRepository(File file) {
        return getAssociatedRepository(file, FIRM_ASSOCIATION);
    }

    public String getLooselyAssociatedRepository(File file) {
        return getAssociatedRepository(file, LOOSE_ASSOCIATION);
    }

    public Collection<String> getAllFirmlyAssociatedUrls() {
        HashSet<String> associatedUrls = new HashSet<String>(10);
        try {
            Preferences prefs = getPreferences();
            String[] keys = prefs.keys();
            for (String key : keys) {
                if (key.startsWith(REPOSITORY_FOR_FILE_PREFIX)) { // found an association
                    String value = prefs.get(key, null);
                    if (value != null && value.length() > 0 && value.charAt(0) == '!') { // found a firm association
                        associatedUrls.add(value.substring(1));
                    }
                }
            }
        } catch (BackingStoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return associatedUrls;
    }

    private String getAssociatedRepository(File file, Boolean reqAssociationType) {
        String key = getPath(file);
        if (key == null) {
            return null;
        }

        String value = getValueForKey(key);
        if ((value == null) || (value.length() == 0)) {
            return null;
        }

        boolean matches;
        final char firstChar = value.charAt(0);
        switch (firstChar) {
            case '!':
                matches = (reqAssociationType != LOOSE_ASSOCIATION);
                break;
            case '?':
                matches = (reqAssociationType != FIRM_ASSOCIATION);
                break;
            default:
                if (LOG.isLoggable(WARNING)) {
                    LOG.warning("unexpected first char of value in mapping: " //NOI18N
                                + key + '=' + value
                                + " (expected: '?' or '!')");           //NOI18N
                }
                matches = false;
        }

        return matches ? value.substring(1) : null;
    }

    private void setAssociation(File file, String repositoryUrl, boolean firm) {
        String key = getPath(file);
        if (key == null) {
            return;
        }

        repositoryUrl = cutTrailingSlashes(repositoryUrl);
        String value = new StringBuilder(1 + repositoryUrl.length())
                .append(firm ? '!' : '?')
                .append(repositoryUrl)
                .toString();

        storeKeyValuePair(key, value);
    }

    private static String getPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            LOG.throwing(FileToRepoMappingStorage.class.getCanonicalName(),
                         "storeMappingToPrefs",                         //NOI18N
                         ex);
            return null;
        }
    }

    private void storeKeyValuePair(String key, String value) {
        getPreferences().put(REPOSITORY_FOR_FILE_PREFIX + key, value);
    }

    private String getValueForKey(String key) {
        return getPreferences().get(REPOSITORY_FOR_FILE_PREFIX + key, null);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(FileToRepoMappingStorage.class);
    }

    public static String cutTrailingSlashes(String url) {
        int endIndex = url.length();
        while ((endIndex > 1) && url.charAt(endIndex - 1) == '/') {
            endIndex--;
        }

        return (endIndex == url.length()) ? url
                                          : url.substring(0, endIndex);
    }

}
