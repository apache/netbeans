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

    public static synchronized FileToRepoMappingStorage getInstance() {
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
