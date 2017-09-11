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

package org.netbeans.modules.javahelp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.help.SearchHit;
import javax.help.SearchTOCItem;
import javax.help.search.SearchEngine;
import javax.help.search.SearchEvent;
import javax.help.search.SearchItem;
import javax.help.search.SearchListener;
import javax.help.search.SearchQuery;
import org.openide.util.Lookup;
import org.netbeans.api.javahelp.Help;
import org.openide.util.NbBundle;

/**
 * Search Java Help for given string.
 * 
 * @author S. Aubrecht
 */
class JavaHelpQuery implements Comparator<SearchTOCItem> {

    private static JavaHelpQuery theInstance;
    private SearchEngine engine;
    
    private static final Logger LOG =
            Logger.getLogger(JavaHelpQuery.class.getName());

    private JavaHelpQuery() {
    }
    
    public static JavaHelpQuery getDefault() {
        if( null == theInstance )
            theInstance = new JavaHelpQuery();
        return theInstance;
    }
    
    @Override
    public int compare(SearchTOCItem o1, SearchTOCItem o2) {
        int res = o2.hitCount() - o1.hitCount() ;
        if( 0 == res ) {
            double conf1 = o1.getConfidence();
            double conf2 = o2.getConfidence();
            if( conf1 < conf2 )
                res = -11;
            else if( conf1 > conf2 )
                res = 1;
        }
        return res;
    }

    public synchronized List<SearchTOCItem> search(String searchString) {
        if( null == engine ) {
            engine = createSearchEngine();
        }
        List<SearchTOCItem> res = new ArrayList<SearchTOCItem>();
        Thread searchThread = new Thread(createSearch(searchString, res),
                "JavaHelpQuery.search["+searchString+"]"); // NOI18N
        searchThread.start();
        try {
            //the first search can take a moment before all the helpsets are merged
            searchThread.join(60*1000);
        } catch( InterruptedException iE ) {
            //ignore
        }
        return res;
    }
    
    private Runnable createSearch(final String searchString,
                                  final List<SearchTOCItem> items) {
        Runnable res = new Runnable() {

            @Override
            public void run() {
                if( null == engine ) {
                    return;
                }
                SynchronizedSearchListener ssl =
                        new SynchronizedSearchListener(items);
                SearchQuery query = engine.createQuery();
                query.addSearchListener(ssl);
                query.start(searchString, Locale.getDefault());
                synchronized(ssl) {
                    while(!ssl.isSearchDone()) { // #148850
                        try {
                            ssl.wait();
                        } catch (InterruptedException ex) {
                            //ignore
                        }
                    }
                }
                //sort the result by their relevance
                Collections.sort( items, JavaHelpQuery.this );
            }
        };
        return res;
    }
    
    private SearchEngine createSearchEngine() {
        SearchEngine se = null;
        Help h = (Help)Lookup.getDefault().lookup(Help.class);
        if (h != null && h instanceof JavaHelp ) {
            JavaHelp jh = (JavaHelp)h;
            se = jh.createSearchEngine();
            if( null == se ) {
                LOG.log(Level.INFO,
                        NbBundle.getMessage(JavaHelpQuery.class,
                        "Err_CreateJavaHelpSearchEngine")); //NOI18N
                se = new DummySearchEngine();
            }
        }
        return se;
    }
    
    private static class DummySearchEngine extends SearchEngine {
        @Override
        public SearchQuery createQuery() throws IllegalStateException {
            return new DummySearchQuery( this );
        }
    } // DummySearchEngine
    
    private static class DummySearchQuery extends SearchQuery {
        
        private List<SearchListener> listeners = new ArrayList<SearchListener>(1);
        
        public DummySearchQuery( DummySearchEngine se ) {
            super( se );
        }

        @Override
        public void addSearchListener(SearchListener arg0) {
            listeners.add( arg0 );
        }

        @Override
        public void removeSearchListener(SearchListener arg0) {
            listeners.remove( arg0 );
        }

        @Override
        public void start(String arg0, Locale arg1)
                        throws IllegalArgumentException, IllegalStateException {
            SearchEvent se = new SearchEvent( this, "", false );
            for( SearchListener sl : listeners ) {
                sl.searchStarted(se);
                sl.searchFinished(se);
            }
        }

        @Override
        public void stop() throws IllegalStateException {
            //do nothing
        }
        
        @Override
        public boolean isActive() {
            return false;
        }
        
    } // DummySearchQuery

    private class SynchronizedSearchListener implements SearchListener {

        private final List<SearchTOCItem> items;
        private boolean searchDone = false;

        public SynchronizedSearchListener(final List<SearchTOCItem> items) {
            this.items = items;
        }

        @Override
        public void itemsFound(SearchEvent se) {
            addItemsToList(se.getSearchItems(), items);
        }

        @Override
        public void searchStarted(SearchEvent se) {
        }

        @Override
        public void searchFinished(SearchEvent se) {
            synchronized(this) {
                searchDone = true;
                notifyAll();
            }
        }

        public boolean isSearchDone() {
            return searchDone;
        }

        private void addItemsToList(Enumeration searchItems,
                                    List<SearchTOCItem> results) {
            if( null == searchItems )
                return;
            while( searchItems.hasMoreElements() ) {
                SearchItem si = (SearchItem) searchItems.nextElement();
                URL url;
                try {
                    url = new URL(si.getBase(), si.getFilename());
                } catch( MalformedURLException murlE ) {
                    LOG.log(Level.FINE,
                            "Invalid URL in SearchItem: " + si.getTitle(),
                            murlE); //NOI18N
                    continue;
                }
                boolean foundToc = false;
                for( SearchTOCItem toc : results ) {
                    URL testURL = toc.getURL();
                    if (testURL != null && url != null && url.sameFile(testURL)) {
                        toc.addSearchHit( new SearchHit(si.getConfidence(),
                                                        si.getBegin(),
                                                        si.getEnd()) );
                        foundToc = true;
                        break;
                    }
                }
                if( !foundToc ) {
                    SearchTOCItem toc = new SearchTOCItem(si);
                    results.add( toc );
                }
            }
        }

    } // SynchronizedSearchListener

}
