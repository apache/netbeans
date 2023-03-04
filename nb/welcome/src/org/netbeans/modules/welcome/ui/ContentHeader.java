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
package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
public class ContentHeader extends JPanel implements Constants {

    private static final Image IMG_BANNER;
    private static final Color COL_BANNER_LEFT;
    private static final Color COL_BANNER_RIGHT;
    
    static {
        String imgName = UIManager.getString( "nb.startpage.imagename.contentheader"); //NOI18N
        if( null == imgName )
            imgName = IMAGE_CONTENT_BANNER;
        IMG_BANNER = ImageUtilities.loadImage(imgName, true);
        Color c = UIManager.getColor( "nb.startpage.contentheader.color1" ); //NOI18N
        if( null == c )
            c = new Color(235, 0, 71);
        COL_BANNER_LEFT = c;

        c = UIManager.getColor( "nb.startpage.contentheader.color2" ); //NOI18N
        if( null == c )
            c = new Color(0, 116, 226);
        COL_BANNER_RIGHT = c;
    };

    private final JLabel lblTitle = new JLabel();

    private final Color COL_GRADIENT_START = new Color( 249, 255, 249 );
    private final Color COL_GRADIENT_END = new Color( 237, 241, 244 );

    public ContentHeader( String title ) {
        setLayout( new BorderLayout() );
        lblTitle.setText( title );
        lblTitle.setFont( CONTENT_HEADER_FONT );
        lblTitle.setForeground( Utils.getTopBarForeground() );
        add( lblTitle, BorderLayout.WEST );
        setBorder( BorderFactory.createEmptyBorder( 12+18, 34, 15, 34 ) );
    }

    @Override
    protected void paintComponent( Graphics g ) {
        Graphics2D g2d = ( Graphics2D ) g;
        int width = getWidth();
        int height = getHeight();

        RenderingHints rh = new RenderingHints(
             RenderingHints.KEY_INTERPOLATION,
             RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2d.setRenderingHints(rh);
        g2d.setColor( Utils.getBorderColor() );
        g2d.drawRect( 0, 0, width, 12 );

        if( UIManager.getBoolean( "nb.startpage.defaultbackground" ) ) { //NOI18N
            g2d.setColor( UIManager.getColor("Tree.background") ); //NOI18N
        } else {
            g2d.setPaint( new GradientPaint( 0, 0, COL_GRADIENT_START, 0, 12, COL_GRADIENT_END ) );
        }
        g2d.fillRect( 1, 0, width-2, 12 );

        int imgWidth = IMG_BANNER.getWidth( this );
        int imgX = (width - imgWidth)/2;
        g2d.drawImage( IMG_BANNER, imgX, 13, imgWidth, height-13, this );
        if( imgX > 0 ) {
            g2d.setPaint( COL_BANNER_LEFT );
            g2d.fillRect( 0, 13, imgX, height-13 );
            g2d.setPaint( COL_BANNER_RIGHT );
            g2d.fillRect( width-imgX-1, 13, imgX+1, height-13 );
        }
    }
}
