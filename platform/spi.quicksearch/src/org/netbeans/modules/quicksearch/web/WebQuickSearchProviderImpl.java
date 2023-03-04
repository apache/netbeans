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

import java.awt.Toolkit;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public class WebQuickSearchProviderImpl implements SearchProvider {

    private Query query;
    
    public void evaluate(SearchRequest request, SearchResponse response) {
        if( null == query ) {
            query = Query.getDefault();
        }
        Result res = query.search( request.getText() );
        do {
            for( Item item : res.getItems() ) {
                if( !response.addResult( createAction( item.getUrl() ), item.getTitle() ) )
                    return;
            }
            res = query.searchMore( request.getText() );
        } while( !res.isSearchFinished() );
    }

    private static Runnable createAction( final String url ) {
        return new Runnable() {
            public void run() {
                String extendedUrl = appendId( url );
                try {
                    HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault();
                    if (displayer != null) {
                        displayer.showURL(new URL(extendedUrl));
                    }
                } catch (Exception e) {
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(WebQuickSearchProviderImpl.class, "Err_CannotDisplayURL", extendedUrl) ); //NOI18N
                    Toolkit.getDefaultToolkit().beep();
                    Logger.getLogger(WebQuickSearchProviderImpl.class.getName()).log(Level.FINE, null, e);
                }
            }
        };
    }
    
    private static String appendId( String url ) {
        StringBuffer res = new StringBuffer(url);
        if( url.contains("?") ) { //NOI18N
            res.append('&'); //NOI18N
        } else {
            res.append('?'); //NOI18N
        }
        res.append("cid=925878");
        return res.toString();
    }
}
