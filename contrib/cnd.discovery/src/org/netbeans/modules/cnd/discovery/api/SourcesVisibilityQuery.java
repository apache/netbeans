/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.discovery.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.ChangeSupport;

/**
 *
 */
public class SourcesVisibilityQuery implements ChangeListener {

    private final ChangeSupport cs = new ChangeSupport(this);
    private static final SourcesVisibilityQuery INSTANCE = new SourcesVisibilityQuery();
    private Pattern acceptedFilesPattern = null;
    private static final String DEFAULT_IGNORE_BYNARY_PATTERN = ".*\\.(il|o|a|dll|dylib|lib|lo|la|Po|Plo|so(\\.[0-9]*)*)$"; // NOI18N
    private final Pattern ignoredFilesPattern = Pattern.compile(DEFAULT_IGNORE_BYNARY_PATTERN);
    
    /** Default instance for lookup. */
    private SourcesVisibilityQuery() {
        MIMEExtensions.get(MIMENames.C_MIME_TYPE).addChangeListener(this);
        MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).addChangeListener(this);
        MIMEExtensions.get(MIMENames.FORTRAN_MIME_TYPE).addChangeListener(this);
    }

    public static SourcesVisibilityQuery getDefault() {
        return INSTANCE;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        acceptedFilesPattern = null; // This will reset filter
        cs.fireChange();
    }

    boolean isVisible(final String fileName) {
        Pattern pattern = getAcceptedFilesPattern();
        return (pattern != null) ? pattern.matcher(fileName).find() : true;
    }

    public boolean isIgnored(String fileName) {
        return ignoredFilesPattern.matcher(fileName).find();
    }

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    private List<Collection<String>> getAcceptedFilesExtensions() {
        List<Collection<String>> suffixes = new ArrayList<>();
        suffixes.add(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        suffixes.add(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        suffixes.add(MIMEExtensions.get(MIMENames.FORTRAN_MIME_TYPE).getValues());
        return suffixes;
    }

    private Pattern getAcceptedFilesPattern() {
        if (acceptedFilesPattern == null) {
            List<Collection<String>> acceptedFileExtensions = getAcceptedFilesExtensions();
            StringBuilder pat = new StringBuilder();
            for (Collection<String> col : acceptedFileExtensions) {
                for (String s : col) {
                    if (pat.length() > 0) {
                        pat.append('|');
                    }
                    if (s.indexOf('+') >= 0) {
                        s = s.replace("+", "\\+"); // NOI18N
                    }
                    pat.append(s);
                }
                String ignoredFiles = ".*\\.(" + pat.toString() + ")$"; //NOI18N;
                acceptedFilesPattern = Pattern.compile(ignoredFiles);
            }
        }
        return acceptedFilesPattern;
    }
}
