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

package org.netbeans.modules.quicksearch.web;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse raw HTML from Google search
 * 
 * @author S. Aubrecht
 */
final class Result {

    private List<Item> items = new ArrayList<Item>(Query.MAX_NUM_OF_RESULTS);
    private boolean searchFinished = false;

    Result() {
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    public boolean isSearchFinished() {
        return searchFinished;
    }
    
    void parse( String html, int currentSearchOffset ) {
        searchFinished = true;
        items.clear();
        html = new String(html.getBytes(), StandardCharsets.UTF_8);
        Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"(.*?)\"[^>]*>(.*?)</a>", //NOI18N
                Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
        Matcher m = p.matcher(html);
        while( m.find() ) {
            String url = m.group(1);
            String title = m.group(2);
            if( url.startsWith("/") ) {//NOI18N
                //look for previous/next links
                int searchOffset = findSearchOffset( url );
                if( searchOffset > currentSearchOffset )
                    searchFinished = false;
                continue; 
            }
            if( url.contains("google.com") ) {//NOI18N
                continue;
            }
            title = "<html>" + title; //NOI18N
            Item si = new Item(url, title, null);
            items.add( si );
        }
    }
    
    public void filterUrl( String[] urlPatterns ) {
        if( null == urlPatterns || urlPatterns.length == 0 )
            return;
        List<Item> filteredItems = new ArrayList<Item>(items.size());
        for( Item item : items ) {
            for( int i=0; i<urlPatterns.length; i++ ) {
                if( urlPatterns[i].length() == 0 )
                    continue;
                if( item.getUrl().toLowerCase(Locale.ENGLISH).matches( urlPatterns[i] ) ) {
                    filteredItems.add(item);
                    break;
                }
            }
        }
        items = filteredItems;
    }
    
    private int findSearchOffset( String url ) {
        int startIndex = url.indexOf( "&amp;start=" );  //NOI18N
        if( startIndex < 0 )
            return -1;
        
        int endIndex = url.indexOf( "&amp;", startIndex+1 );  //NOI18N
        if( endIndex < 0 )
            endIndex = url.length();
        if( endIndex < startIndex )
            return -1;
        String offset = url.substring( startIndex, endIndex );
        try {
            return Integer.parseInt( offset );
        } catch( NumberFormatException nfE ) {
            //ignore
        }
        return -1;
    }
}
