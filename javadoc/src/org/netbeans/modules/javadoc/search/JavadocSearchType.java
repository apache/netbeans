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
        overviewLabelFilters = ll.toArray(new Pattern[ll.size()]);
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
