/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
            c = new Color( 28, 82, 157 );
        COL_BANNER_LEFT = c;

        c = UIManager.getColor( "nb.startpage.contentheader.color2" ); //NOI18N
        if( null == c )
            c = new Color( 41, 62, 109 );
        COL_BANNER_RIGHT = c;
    };

    private final JLabel lblTitle = new JLabel();

    private final Color COL_GRADIENT_START = new Color( 249, 255, 249 );
    private final Color COL_GRADIENT_END = new Color( 237, 241, 244 );

    public ContentHeader( String title ) {
        setLayout( new BorderLayout() );
        lblTitle.setText( title );
        lblTitle.setFont( CONTENT_HEADER_FONT );
        lblTitle.setForeground( Color.white );
        add( lblTitle, BorderLayout.WEST );
        setBorder( BorderFactory.createEmptyBorder( 12+18, 34, 15, 34 ) );
    }

    @Override
    protected void paintComponent( Graphics g ) {
        Graphics2D g2d = ( Graphics2D ) g;
        int width = getWidth();
        int height = getHeight();

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
