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

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.CombinationRSSFeed;
import org.netbeans.modules.welcome.content.RSSFeed;
import org.netbeans.modules.welcome.content.RSSFeedReaderPanel;
import org.netbeans.modules.welcome.content.WebLink;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
class ArticlesAndNews extends RSSFeedReaderPanel {

    private RSSFeed feed;

    private static final int MAX_ARTICLES_COUNT = 5;

    public ArticlesAndNews() {
        super( "ArticlesAndNews", true ); // NOI18N
        add( buildBottomContent(), BorderLayout.SOUTH );
    }

    @Override
    protected JComponent buildContent(String url, boolean showProxyButton) {
        final Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate"); // NOI18N
        if( null != p ) {
            String ideId = p.get ("ideIdentity", null); // NOI18N
            if( null != ideId && ideId.length() > 0 ) {
                if( url.contains("?") ) { // NOI18N
                    url += "&unique="; // NOI18N
                } else {
                    url +=  "?unique="; // NOI18N
                }
                url += ideId;
            }
        }
        feed = new ArticlesAndNewsRSSFeed( url, BundleSupport.getURL("News"), showProxyButton ); // NOI18N
        feed.addPropertyChangeListener( RSSFeed.FEED_CONTENT_PROPERTY, this );
        return feed;
    }
    
    protected JComponent buildBottomContent() {
        WebLink news = new WebLink("AllNews", true); // NOI18N
        BundleSupport.setAccessibilityProperties( news, "AllNews" ); //NOI18N
        
        WebLink articles = new WebLink( "AllArticles", true); // NOI18N
        BundleSupport.setAccessibilityProperties( articles, "AllArticles" ); //NOI18N

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque(false);

        panel.add( news, new GridBagConstraints(0,1,1,1,0.0,0.0,
                GridBagConstraints.SOUTHWEST,GridBagConstraints.HORIZONTAL,
                new Insets(5,0,0,15),0,0) );
        panel.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,0),0,0) );
        panel.add( articles, new GridBagConstraints(2,1,1,1,0.0,0.0,
                GridBagConstraints.SOUTHEAST,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,0),0,0) );

        return panel;
    }

    private class ArticlesAndNewsRSSFeed extends CombinationRSSFeed {
        public ArticlesAndNewsRSSFeed( String url1, String url2, boolean showProxyButton ) {
            super( url1, url2, showProxyButton, MAX_ARTICLES_COUNT );
        }
    }
}
