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

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 *
 * @author S. Aubrecht
 */
public interface Constants {

    static final String COLOR_SECTION_HEADER = "SectionHeaderColor"; //NOI18N
    static final String COLOR_BIG_BUTTON = "BigButtonColor"; //NOI18N
    static final String COLOR_BOTTOM_BAR = "BottomBarColor"; //NOI18N
    static final String COLOR_BORDER = "BorderColor"; //NOI18N
    static final String COLOR_TAB_BACKGROUND = "TabBackgroundColor"; //NOI18N
    static final String COLOR_TAB_BORDER1 = "TabBorder1Color"; //NOI18N
    static final String COLOR_TAB_BORDER2 = "TabBorder2Color"; //NOI18N
    static final String COLOR_TAB_FOREGROUND = "TabForegroundColor"; //NOI18N

    static final String COLOR_RSS_DATE = "RssDateTimeColor"; //NOI18N
    static final String COLOR_RSS_DETAILS = "RssDetailsColor"; //NOI18N
    static final String COLOR_HEADER = "HeaderForegroundColor"; //NOI18N
    
    static final int FONT_SIZE = Utils.getDefaultFontSize();
    static final String FONT_NAME = BundleSupport.getLabel( "FONT_NAME" ); //NOI18N
    static final Font BUTTON_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE+1 );
    static final Font RSS_DESCRIPTION_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE-1 );
    static final Font TAB_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE+1 ); //NOI18N
    static final Font SECTION_HEADER_FONT = new Font( FONT_NAME, Font.BOLD, FONT_SIZE+7 ); //NOI18N
    static final Font GET_STARTED_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE+1 ) ; //NOI18N
    static final Font CONTENT_HEADER_FONT = new Font( FONT_NAME, Font.BOLD, FONT_SIZE+13 ) ; //NOI18N

    static final String APACHE_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/apache_feather.png"; // NOI18N
    static final String INCUBATOR_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/incubator_logo.png"; // NOI18N
    static final String NETBEANS_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/nb_logo.png"; // NOI18N

    static final String IMAGE_CONTENT_BANNER = "org/netbeans/modules/welcome/resources/content_banner.png"; // NOI18N

    static final String IMAGE_TAB_SELECTED = "org/netbeans/modules/welcome/resources/tab_selected.png"; // NOI18N
    static final String IMAGE_TAB_ROLLOVER = "org/netbeans/modules/welcome/resources/tab_rollover.png"; // NOI18N

    static final String BROKEN_IMAGE = "org/netbeans/modules/welcome/resources/broken_image.gif"; // NOI18N
    static final String IMAGE_PICTURE_FRAME = "org/netbeans/modules/welcome/resources/picture_frame.png"; // NOI18N

    static final Stroke LINK_IN_FOCUS_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_BEVEL, 0, new float[] {0, 2}, 0);
    static final String LINK_IN_FOCUS_COLOR = "LinkInFocusColor"; //NOI18N
    static final String LINK_COLOR = "LinkColor"; //NOI18N
    static final String VISITED_LINK_COLOR = "VisitedLinkColor"; //NOI18N

    static final int RSS_FEED_TIMER_RELOAD_MILLIS = 60*60*1000;

    static final int TEXT_INSETS_LEFT = 10;
    static final int TEXT_INSETS_RIGHT = 10;

    static final Border HEADER_TEXT_BORDER = BorderFactory.createEmptyBorder( 1, TEXT_INSETS_LEFT, 1, TEXT_INSETS_RIGHT );
    
    static final int START_PAGE_MIN_WIDTH = 700;

    static final Logger USAGE_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.projects"); //NOI18N
}
