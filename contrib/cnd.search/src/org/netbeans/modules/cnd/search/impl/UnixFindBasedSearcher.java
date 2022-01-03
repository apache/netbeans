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
package org.netbeans.modules.cnd.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.modules.cnd.search.MatchingFileData;
import org.netbeans.modules.cnd.search.SearchParams;
import org.netbeans.modules.cnd.search.Searcher;

/**
 *
 */
public final class UnixFindBasedSearcher implements Searcher {

    private static final Pattern grepOutPattern = Pattern.compile("^([0-9]+):(.*)"); // NOI18N
    private final SearchParams params;
    private final SearchRoot root;
    private String rootPath;
    private List<MatchingFileData.Entry> entries;

    public UnixFindBasedSearcher(SearchRoot root, SearchParams params) {
        this.params = params;
        this.root = root;
    }

    @Override
    public String getCommand() {
        return "find"; // NOI18N
    }

    @Override
    public String[] getCommandArguments() {
        List<String> args = new ArrayList<String>();
        rootPath = root.getFileObject().getPath();

        args.add(rootPath);
        args.add("-type"); // NOI18N
        args.add("f"); // NOI18N

        String fileNamePattern = params.getFileNamePattern();

        if (fileNamePattern != null && !fileNamePattern.isEmpty()) {
            args.add("-name"); // NOI18N
            args.add(fileNamePattern);
        }
        
        SearchPattern sp = params.getSearchPattern();
        
        String searchText = sp.getSearchExpression();
        if (searchText != null && !searchText.isEmpty()) {
            args.add("-exec"); // NOI18N
            args.add("grep"); // NOI18N
            if (!sp.isMatchCase()) {
                args.add("-i"); // NOI18N
            }
            if (sp.isWholeWords()) {
                args.add("-w"); // NOI18N
            }
            args.add("-n"); // NOI18N
            args.add(searchText);
            args.add("{}"); // NOI18N
            args.add(";"); // NOI18N
        }

        args.add("-ls"); // NOI18N

        return args.toArray(new String[args.size()]);
    }

    @Override
    public MatchingFileData processOutputLine(String line) {
        Matcher m = grepOutPattern.matcher(line);
        if (m.matches()) {
            Integer lineNo = Integer.parseInt(m.group(1));
            String context = m.group(2);

            if (entries == null) {
                entries = new ArrayList<MatchingFileData.Entry>(10);
            }

            entries.add(new MatchingFileData.Entry(lineNo, context));
            return null;
        }

        String[] data = line.split("[ \t]+", 11); // NOI18N

        if (data.length != 11) {
            return null;
        }

        String fname = data[10];
        if (fname.contains(" -> ")) { // NOI18N
            /// TODO ...
            fname = fname.substring(0, fname.indexOf(" -> ")); // NOI18N
        }

        MatchingFileData result = new MatchingFileData(params, fname);
        
        int fileSize = -1;
        try {
            fileSize = Integer.parseInt(data[6]);
        } catch (NumberFormatException ex) {
        }
        
        result.setFileSize(fileSize);

        result.setEntries(entries);
        entries = null;

        return result;
    }
}
