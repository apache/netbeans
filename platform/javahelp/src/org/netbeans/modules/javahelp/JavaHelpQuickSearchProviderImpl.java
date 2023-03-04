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

package org.netbeans.modules.javahelp;

import java.awt.Toolkit;
import java.net.URL;
import java.util.List;
import javax.help.SearchTOCItem;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.Lookup;
import org.netbeans.api.javahelp.Help;

/**
 * QuickSearch provider for IDE's JavaHelp topics.
 * 
 * @author S. Aubrecht
 */
public class JavaHelpQuickSearchProviderImpl implements SearchProvider {

    private JavaHelpQuery query;
    
    public void evaluate(SearchRequest request, SearchResponse response) {
        synchronized( this ) {
            if( null == query ) {
                query = JavaHelpQuery.getDefault();
            }
        }
        List<SearchTOCItem> res = query.search( request.getText() );
        for( SearchTOCItem item : res ) {
            if( !response.addResult( createAction( item.getURL() ), item.getName() ) )
                return;
        }
    }

    private Runnable createAction( final URL url ) {
        return new Runnable() {
            public void run() {
                Help h = (Help)Lookup.getDefault().lookup(Help.class);
                if (h instanceof JavaHelp) {
                    JavaHelp jh = (JavaHelp)h;
                    jh.showHelp(url);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        };
    }
}
