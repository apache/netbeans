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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * Search Google for given text.
 * 
 * @author S. Aubrecht
 */
final class Query {

    private Thread searchThread;
    private static Query theInstance;
    private int searchOffset;
    
    //restrict search for this site only
    private static final String SITE_SEARCH = getSiteSearch();
    //maximum number of search results requested
    static final int MAX_NUM_OF_RESULTS = 50;
    //google doesn't allow searching in site subfolders, so we must make sure
    //the links we found have the following patterns in their URLs
    private static final String[] URL_PATTERNS = getUrlPatterns();
    
    private Query() {
    }
    
    public static Query getDefault() {
        if( null == theInstance )
            theInstance = new Query();
        return theInstance;
    }
    
    public Result search( String searchString ) {
        abort();
        Result res = new Result();
        searchThread = new Thread( createSearch( searchString, res, 0 ) );
        searchThread.start();
        try {
            searchThread.join(20*1000);
        } catch( InterruptedException iE ) {
            //ignore
        }
        return res;
    }
    
    public Result searchMore( String searchString ) {
        searchOffset += MAX_NUM_OF_RESULTS;
        Result res = new Result();
        Thread searchMoreThread = new Thread( createSearch( searchString, res, searchOffset ) );
        searchMoreThread.start();
        try {
            searchMoreThread.join(20*1000);
        } catch( InterruptedException iE ) {
            //ignore
        }
        return res;
    }
    
    private void abort() {
        if( null == searchThread ) {
            return;
        }
        
        searchOffset = 0;
        searchThread.interrupt();
        searchThread = null;
    }
    
    private Runnable createSearch( final String searchString, final Result result, final int searchOffset ) {
        Runnable res = new Runnable() {

            public void run() {
                String query = searchString;
                query = query.replace( " ", "+" ); //NOI18N //NOI18N
                query = query.replace( "#", "%23" ); //NOI18N //NOI18N
                query += "&num=" + MAX_NUM_OF_RESULTS; //NOI18N
                query += "&hl=" + Locale.getDefault().getLanguage(); //NOI18N
                if( null != SITE_SEARCH )
                    query += "&sitesearch=" + SITE_SEARCH; //NOI18N
                if( searchOffset > 0 ) {
                    query += "&start=" + searchOffset; //NOI18N
                }
                try {
                    Socket s = new Socket("google.com",80); //NOI18N
                    PrintStream p = new PrintStream(s.getOutputStream());
                    p.print("GET /search?q=" + query + " HTTP/1.0\r\n"); //NOI18N //NOI18N
                    //fake browser headers
                    p.print("User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1) Gecko/20061010 Firefox/2.0\r\n"); //NOI18N
                    p.print("Connection: close\r\n\r\n"); //NOI18N
                    //TODO proxy
                    InputStreamReader in = new InputStreamReader(s.getInputStream());

                    BufferedReader buffer = new BufferedReader(in);

                    String line;
                    StringBuffer rawHtml = new StringBuffer();
                    while ((line = buffer.readLine()) != null) {
                        rawHtml.append(line);
                    }

                    in.close();            
                    result.parse( rawHtml.toString(), searchOffset );
                    result.filterUrl( URL_PATTERNS );
                } catch( IOException ioE ) {
                    Logger.getLogger(Query.class.getName()).log(Level.INFO, null, ioE);
                }
            }
        };
        return res;
    }
    
    private static String getSiteSearch() {
        String res = null;
        try {
            res = NbBundle.getMessage(Query.class, "quicksearch.web.site" ); //NOI18N
        } catch( MissingResourceException mrE ) {
            //ignore
        }
        return res;
    }
    
    private static String[] getUrlPatterns() {
        try {
            String patterns = NbBundle.getMessage(Query.class, "quicksearch.web.url_patterns" ); //NOI18N
            return patterns.split("\\s|,|;|:" );
        } catch( MissingResourceException mrE ) {
            //ignore
        }
        return null;
    }
}
