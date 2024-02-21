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

package org.netbeans.modules.welcome.content;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.welcome.content.RSSFeed.ErrorCatcher;
import org.netbeans.modules.welcome.content.RSSFeed.FeedHandler;
import org.netbeans.modules.welcome.content.RSSFeed.FeedItem;
import org.openide.util.NbPreferences;
import org.openide.xml.XMLUtil;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Combines two RSS feeds into one.
 * 
 * @author S. Aubrecht
 */
public class CombinationRSSFeed extends RSSFeed {

    private String url1;
    private String url2;
    private int maxItemCount;

    /** Creates a new instance of CombinationRSSFeed */
    public CombinationRSSFeed( String url1, String url2, boolean showProxyButton, int maxItemCount ) {
        super( showProxyButton );
        this.maxItemCount = maxItemCount;
        this.url1 = url1;
        this.url2 = url2;
    }
    
    @Override
    protected List<FeedItem> buildItemList() throws SAXException, ParserConfigurationException, IOException {
        XMLReader reader = XMLUtil.createXMLReader( false, true );
        FeedHandler handler = new FeedHandler( getMaxItemCount() );
        reader.setContentHandler( handler );
        reader.setEntityResolver( new RSSEntityResolver() );
        reader.setErrorHandler( new ErrorCatcher() );
        reader.parse( findInputSource(new URL(url1)) );

        ArrayList<FeedItem> res = new ArrayList<FeedItem>( 2*getMaxItemCount() );
        res.addAll( handler.getItemList() );

        handler = new FeedHandler( getMaxItemCount() );
        reader.setContentHandler( handler );
        reader.parse( findInputSource(new URL(url2)) );

        res.addAll( handler.getItemList() );

        List<FeedItem> items = sortNodes( res );
        if( items.size() > getMaxItemCount() ) {
            items = items.subList( 0, getMaxItemCount() );
        }
        return items;
    }

    private ArrayList<FeedItem> sortNodes( ArrayList<FeedItem> res ) {
        res.sort(new DateFeedItemComparator());
        return res;
    }

    @Override
    protected void clearCache() {
        try {
            NbPreferences.forModule( RSSFeed.class ).remove( url2path( new URL(url1))) ;
            NbPreferences.forModule( RSSFeed.class ).remove( url2path( new URL(url2))) ;
        } catch( MalformedURLException mE ) {
            //ignore
        }
    }

    @Override
    protected int getMaxItemCount() {
        return this.maxItemCount;
    }

    private static class DateFeedItemComparator implements Comparator<FeedItem> {
    private static DateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH ); // NOI18N
    private static DateFormat dateFormatLong = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH ); // NOI18N
    private static DateFormat dateFormatShort = new SimpleDateFormat( "EEE, dd MMM yyyy", Locale.ENGLISH ); // NOI18N
        
    public int compare(FeedItem item1, FeedItem item2) {
            Date date1 = extractDate( item1 );
            Date date2 = extractDate( item2 );

            if( null == date1 && null == date2 )
                return 0;
            else if( null == date1 )
                return 1;
            else if( null == date2 )
                return -1;
            if( date1.after( date2 ) ) {
                return -1;
            } else if( date1.before( date2 ) ) {
                return 1;
            }
            return 0;
        }

        private Date extractDate( FeedItem item ) {
            try {
                if( null != item.dateTime )
                    return dateFormat.parse( item.dateTime );
            } catch( ParseException pE ) {
                try {     
                    return dateFormatShort.parse( item.dateTime );
                } catch( ParseException otherPE ) {
                    try {
                        return dateFormatLong.parse( item.dateTime );
                    } catch( ParseException e ) {
                        //ignore
                    }
                }
            }
            return null;
        }
    }
}
