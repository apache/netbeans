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

package org.openidex.search;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Shareable search history. Known implementations are explorer search
 * dialog and editor find&replace dialog.
 *
 * Typical use case:
 * Editor registers a listener to listen on lastSelected SearchPattern. If user
 * opens explorer's search dialog and perform search, a search expression is added
 * into SearchHistory and lastSelected SearchPattern is setted. The event is fired,
 * editor can retrieve lastSelected SearchPattern and in accordance with its parameters
 * it can highlight(in yellow) all matched patterns. If editor dialog is open,
 * it contains shareable SearchHistory. Another direction is search in editor, that 
 * adds a SearchPattern in SearchHistory, thus the new item is available also in
 * explorer's search dialog.
 *
 * @since  org.openidex.util/3 3.5, NB 4.1
 * @author  Martin Roskanin
 * @author  kaktus
 */
public final class SearchHistory {
    
    /** Support for listeners */
    private PropertyChangeSupport pcs;

    /** Maximum items allowed in searchPatternsList */
    private static final int MAX_SEARCH_PATTERNS_ITEMS = 10;

    /** Shareable SearchPattern history. It is a List of SearchPatterns */
    private List<SearchPattern> searchPatternsList
            = new ArrayList<SearchPattern>(MAX_SEARCH_PATTERNS_ITEMS);

    /** Singleton instance */
    private static SearchHistory INSTANCE = null;
    
    /** Property name for last selected search pattern
     *  Firing: 
     *  oldValue - old selected pattern
     *  newValue - new selected pattern
     *  @deprecated just changes in history
     */
    @Deprecated
    public final static String LAST_SELECTED = "last-selected"; //NOI18N
    
    /** Property name for adding pattern that was not in history
     *  Firing:
     *  oldValue - null
     *  newValue - added pattern
     */
    public final static String ADD_TO_HISTORY = "add-to-history"; //NOI18N

    /** Preferences node for storing history info */
    private static Preferences prefs;
    /** Name of preferences node where we persist history */
    private static final String PREFS_NODE = "SearchHistory";  //NOI18N
    private static final String PROP_SEARCH_PATTERN_PREFIX = "search_";  //NOI18N

    /** Creates a new instance of SearchHistory */
    private SearchHistory() {
        prefs = NbPreferences.forModule(SearchHistory.class).node(PREFS_NODE);
        load();
    }

    /** @return singleton instance of SearchHistory */
    public synchronized static SearchHistory getDefault(){
        if (INSTANCE == null) {
            INSTANCE = new SearchHistory();
        }
        return INSTANCE;
    }

    /**
     *  Loads search history stored in previous system sessions.
     */
    private void load () {
        for(int i=0; i < MAX_SEARCH_PATTERNS_ITEMS; i++){
            SearchPattern pattern = SearchPattern.parseSearchPattern(prefs.get(PROP_SEARCH_PATTERN_PREFIX + i, null));
            if (pattern != null)
                searchPatternsList.add(pattern);
        }
    }

    /** 
     *  @return last selected SearchPattern 
     *  @deprecated Use <code>getSearchPatterns().get(0)</code>
     */
    @Deprecated
    public SearchPattern getLastSelected(){
        return searchPatternsList.get(0);
    }
    
    /** Sets last selected SearchPattern 
     *  @param pattern last selected pattern
     *  @deprecated Use only <code>add(SearchPattern pattern)</code>
     */
    @Deprecated
    public void setLastSelected(SearchPattern pattern){
        SearchPattern oldPattern = searchPatternsList.get(0);
        add(pattern);
        if (pcs != null){
            pcs.firePropertyChange(LAST_SELECTED, oldPattern, pattern);
        }
    }
    
    private synchronized PropertyChangeSupport getPropertyChangeSupport(){
        if (pcs == null){
            pcs = new PropertyChangeSupport(this);
        }
        return pcs;
    }

    /** Adds a property change listener.
     * @param pcl the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        getPropertyChangeSupport().addPropertyChangeListener(pcl);
    }
    
    /** Removes a property change listener.
     * @param pcl the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl){
        if (pcs != null){
            pcs.removePropertyChangeListener(pcl);
        }
    }

    /** @return unmodifiable List of SearchPatterns */
    public synchronized List<SearchPattern> getSearchPatterns(){
        return Collections.unmodifiableList(searchPatternsList);
    }
    
    /** Adds SearchPattern to SearchHistory 
     *  @param pattern the SearchPattern to add
     */
    public synchronized void add(SearchPattern pattern) { 
        if (pattern == null || pattern.getSearchExpression() == null || pattern.getSearchExpression().length() == 0
                || (searchPatternsList.size() > 0 && pattern.equals(searchPatternsList.get(0)))) {
            return;
        }
        
        for (int i = 0; i < searchPatternsList.size(); i++) {
            if (pattern.getSearchExpression().equals(searchPatternsList.get(i).getSearchExpression())) {
                searchPatternsList.remove(i);
                break;
            }
        }
        
        if (searchPatternsList.size() == MAX_SEARCH_PATTERNS_ITEMS){
            searchPatternsList.remove(MAX_SEARCH_PATTERNS_ITEMS-1);
        }
        searchPatternsList.add(0, pattern);
        
        for(int i=0;i < searchPatternsList.size();i++){
            prefs.put(PROP_SEARCH_PATTERN_PREFIX + i, searchPatternsList.get(i).toCanonicalString());
        }
        if (pcs != null) {
            pcs.firePropertyChange(ADD_TO_HISTORY, null, pattern);
        }
    }
    
}
