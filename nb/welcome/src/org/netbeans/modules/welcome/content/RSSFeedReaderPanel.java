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

package org.netbeans.modules.welcome.content;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author S. Aubrecht
 */
public class RSSFeedReaderPanel extends JPanel implements PropertyChangeListener {

    private static final int FEED_PANEL_MAX_WIDTH = 600;

    /** Creates a new instance of AbstractFeedReaderPanel */
    public RSSFeedReaderPanel( String url ) {
        super( new BorderLayout() );
        setOpaque(false);
        add( buildContent( url, false ), BorderLayout.CENTER );
        setMaximumSize( new Dimension(FEED_PANEL_MAX_WIDTH, Integer.MAX_VALUE) );
    }

    /** Creates a new instance of AbstractFeedReaderPanel */
    public RSSFeedReaderPanel( String key, boolean showProxyButton ) {
        super( new BorderLayout() );
        setOpaque(false);
        add( buildContent( BundleSupport.getURL( key ), showProxyButton ), BorderLayout.CENTER );
    }

    protected JComponent buildContent( String url, boolean showProxyButton ) {
        RSSFeed feed = new RSSFeed( url, showProxyButton );
        feed.addPropertyChangeListener( RSSFeed.FEED_CONTENT_PROPERTY, this );
        return feed;
    }
    
    protected void feedContentLoaded() {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( RSSFeed.FEED_CONTENT_PROPERTY.equals( evt.getPropertyName() ) ) {
            feedContentLoaded();
        }
    }
}
