/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.welcome.content;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
        reader.setEntityResolver( org.openide.xml.EntityCatalog.getDefault() );
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
        Collections.sort( res, new DateFeedItemComparator() );
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
