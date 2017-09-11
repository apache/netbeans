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
