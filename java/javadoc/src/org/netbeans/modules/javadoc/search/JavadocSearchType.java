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

package org.netbeans.modules.javadoc.search;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

/**
 * Service that knows how to parse doclet output and can
 * search in it.
 *
 * @author  Petr Suchomel
 */
public abstract class JavadocSearchType {

    /** Tries to find javadoc index files in given directory
     * @param apidocRoot Folder where to look for index files where to find index files
     * @return File object containing index-files e.g index-files directory
     *         or index-all.html.
     */
    public abstract URL getDocFileObject(URL apidocRoot);
    
    private Pattern[]  overviewLabelFilters;

    private synchronized void prepareOverviewFilter() {
        if (overviewLabelFilters != null) {
            return;
        }
        String filter = NbBundle.getMessage(JavadocSearchType.class, "FILTER_OverviewIndiceLabel"); // NOI18N
        StringTokenizer tok = new StringTokenizer(filter, "\n"); // NOI18N
        List<Pattern> ll = new LinkedList<Pattern>();
        while (tok.hasMoreTokens()) {
            try {
                String expr = tok.nextToken();
                Pattern re = Pattern.compile(expr);
                ll.add(re);
            } catch (PatternSyntaxException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        overviewLabelFilters = ll.toArray(new Pattern[0]);
    }
    
    /**
     * This method is supposed to strip generic parts ("Overview (...)" or "... - Overview")
     * from the overview page's title. The default implementation does nothing,
     * returns the title unfiltered.
     *
     * @since
     */
    public String getOverviewTitleBase(String overviewTitle) {
        prepareOverviewFilter();
        Matcher match;
        String t = overviewTitle.trim();
        
        for (int i = 0; i < overviewLabelFilters.length; i++) {
            match = overviewLabelFilters[i].matcher(t);
            if  (match.matches()) {
                return match.group(1);
            }
        }
        return overviewTitle;
    }

    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     */    
    public abstract IndexSearchThread getSearchThread(String toFind, URL fo, IndexSearchThread.DocIndexItemConsumer diiConsumer);
    

    /**
     * Returns true if the JavadocSearchType accepts the given apidocRoot.
     * @param apidocRoot root of the javadoc
     * @param encoding of the javadoc, may be null if the javadoc has no encoding
     */
    public abstract boolean accepts(URL apidocRoot, String encoding);

}
