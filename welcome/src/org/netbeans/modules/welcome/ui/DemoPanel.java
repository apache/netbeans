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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.text.View;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.RSSFeed;
import org.netbeans.modules.welcome.content.RSSFeedReaderPanel;
import org.netbeans.modules.welcome.content.Utils;
import org.netbeans.modules.welcome.content.WebLink;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
class DemoPanel extends RSSFeedReaderPanel {

    public DemoPanel() {
        super( BundleSupport.getURL( "Demo" ) ); //NOI18N
    }

    @Override
    protected JComponent buildContent(String url, boolean showProxyButton) {
        JPanel res = new JPanel( new GridBagLayout() );
        res.setOpaque(false);
        
        DemoRSSFeed feed = new DemoRSSFeed( url );
        res.add( feed, new GridBagConstraints(0,0,1,1,0.0,0.0
                ,GridBagConstraints.NORTHWEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        res.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,1.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
        
        return res;
    }

    class DemoRSSFeed extends RSSFeed {
        public DemoRSSFeed( String url ) {
            super( url, false );
        }

        @Override
        protected Component createFeedItemComponent(FeedItem item) {
            JPanel panel = new JPanel( new GridBagLayout() );
            panel.setOpaque(false);
            int row = 0;

            if( item.isValid() ) {
                if( item.enclosureUrl != null ) {
                    panel.add( new ImageLabel( item.link, getImage( item.enclosureUrl ), item.description ),
                            new GridBagConstraints(0,row++,1,1,0.0,0.0,
                            GridBagConstraints.WEST,GridBagConstraints.NONE,
                            new Insets(0,0,5,5),0,0 ) );
                } else {
                    JLabel label = new JLabel( BundleSupport.getLabel("NoScreenShot") ); //NOI18N
                    label.setHorizontalAlignment( JLabel.CENTER );
                    label.setVerticalAlignment( JLabel.CENTER );
                    panel.add( label, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                            GridBagConstraints.CENTER,GridBagConstraints.NONE,
                            new Insets(0,0,5,5),0,0 ) );
                }

                WebLink linkButton = new WebLink( item.title, item.link, false );
                linkButton.getAccessibleContext().setAccessibleName(
                        BundleSupport.getAccessibilityName( "WebLink", item.title ) ); //NOI18N
                linkButton.getAccessibleContext().setAccessibleDescription(
                        BundleSupport.getAccessibilityDescription( "WebLink", item.link ) ); //NOI18N
                linkButton.setFont( BUTTON_FONT );
                panel.add( linkButton, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                        GridBagConstraints.WEST,GridBagConstraints.NONE,
                        new Insets(0,0,5,5),0,0 ) );
            } else {
                panel.add( new JLabel(BundleSupport.getLabel("ErrLoadingFeed")),  // NOI18N
                        new GridBagConstraints(0,row++,1,1,0.0,0.0,
                        GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
                JButton button = new JButton();
                Mnemonics.setLocalizedText( button, BundleSupport.getLabel( "Reload" ) );  // NOI18N
                button.setOpaque( false );
                button.addActionListener( new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        lastReload = 0;
                        reload();
                    }
                });
                panel.add( button, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                        GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
            }
            
            return panel;
        }

        @Override
        protected int getMaxItemCount() {
            return 1;
        }
        
        ImageIcon getImage( String urlString ) {
            URL url = null;
            try {
                url = new URL( urlString );
            } catch( MalformedURLException mfuE ) {
                mfuE.printStackTrace();
            }
            ImageIcon image = null;
            if( isContentCached() ) {
                ObjectInputStream input = null;
                try {
                    input = new ObjectInputStream( new FileInputStream( getCacheFilePath() ) );
                    image = (ImageIcon)input.readObject();
                    Logger.getLogger( DemoPanel.class.getName() ).log( Level.FINE, 
                            "Demo image loaded from: " + getCacheFilePath() ); //NOI18N
                }catch( Exception e ) {
                    image = null;
                } finally {
                    if( null != input )
                        try { input.close(); } catch( IOException e ) {}
                }
            }

            if( null == image ) {
                ObjectOutputStream output = null;
                try {
                    URLConnection conn = url.openConnection();
                    boolean defCache = conn.getDefaultUseCaches();
                    conn.setDefaultUseCaches(true);
                    if( conn instanceof HttpURLConnection ) {
                        HttpURLConnection httpConn = ( HttpURLConnection ) conn;
                        httpConn.connect();
                        if( httpConn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP ) {
                            String newUrl = httpConn.getHeaderField( "Location"); //NOI18N
                            if( null != newUrl && !newUrl.isEmpty() ) {
                                return getImage(newUrl );
                            }
                            throw new IOException( "Invalid redirection" ); //NOI18N
                        }
                        RSSFeed.initSSL( (HttpURLConnection)conn );
                    }
                    image = new ImageIcon( url );
                    conn.setDefaultUseCaches(defCache);

                    output = new ObjectOutputStream( new FileOutputStream( getCacheFilePath() ) );
                    output.writeObject( image );
                } catch( Exception e ) {
                    Logger.getLogger( DemoPanel.class.getName() ).log( Level.FINE, 
                            "Error while caching Welcome Page demo image", e ); //NOI18N
                    image = ImageUtilities.loadImageIcon(Constants.BROKEN_IMAGE, false);
                } finally {
                    if( null != output ) {
                        try { output.close(); } catch( IOException e ) {}
                    }
                }
            }
            
            return image;
        }
        
        private File getCacheFilePath() throws IOException {
            File cacheStore = Utils.getCacheStore();
            cacheStore = new File(cacheStore, "demoimage" ); //NOI18N
            cacheStore.getParentFile().mkdirs();
            cacheStore.createNewFile();
            return cacheStore;
        }
    }
    
    private static class ImageLabel extends JLabel implements Constants, MouseListener {
        private String url;
        private boolean visited = false;
        
        public ImageLabel( String url, ImageIcon img, String description ) {
            super( new MaxSizeImageIcon(img.getImage()) );
            this.url = url;
            if( null != description )
                setToolTipText( "<html>" + description ); //NOI18N
            setOpaque( false );
            setBorder( BorderFactory.createEmptyBorder(1,1,1,1) );
            addMouseListener( this );
            setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if( !e.isPopupTrigger() ) {
                visited = true;
                Utils.showURL( url );
                mouseEntered( null );
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            Color borderColor = visited ? Utils.getVisitedLinkColor() : Utils.getLinkColor();
            setBorder( BorderFactory.createLineBorder(borderColor, 1) );
            StatusDisplayer.getDefault().setStatusText( url );
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setBorder( BorderFactory.createEmptyBorder(1,1,1,1) );
            StatusDisplayer.getDefault().setStatusText( "" );
        }

        @Override
        public JToolTip createToolTip() {
            return new MyTooltip();
        }
    }
    
    private static class MaxSizeImageIcon implements Icon, Constants {
        private static final int MAX_IMAGE_WIDTH = 202;
        private static final int MAX_IMAGE_HEIGHT = 142;
        private Image content;
        private Image frame;
        
        public MaxSizeImageIcon( Image content ) {
            this.content = content;
            frame = ImageUtilities.loadImage( IMAGE_PICTURE_FRAME );
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            try {
                int imgX = x;
                int imgY = y;
                if( content.getWidth(null) > MAX_IMAGE_WIDTH )
                    imgX += (content.getWidth(null) - MAX_IMAGE_WIDTH) / 2;
                if( content.getHeight(null) > MAX_IMAGE_HEIGHT )
                    imgY += (content.getHeight(null) - MAX_IMAGE_HEIGHT) / 2;
                g.drawImage(content, x, y, x+Math.min(MAX_IMAGE_WIDTH, content.getWidth(null)),
                                           y+Math.min(MAX_IMAGE_HEIGHT, content.getHeight(null)),
                        imgX, imgY, imgX+Math.min(MAX_IMAGE_WIDTH, content.getWidth(null)),
                                    imgY+Math.min(MAX_IMAGE_HEIGHT, content.getHeight(null)), null);
                g.drawImage(frame, x, y, c);
            } catch( ThreadDeath td ) {
                throw td;
            } catch( Throwable e ) {
                //#135448 - don't let corrupt image downloads break the painting of the whole IDE window
                Logger.getLogger(DemoPanel.class.getName()).log(Level.FINE,
                        "Error while painting demo image.", e); //NOI18N
            }
        }

        @Override
        public int getIconWidth() {
            return MAX_IMAGE_WIDTH;
        }

        @Override
        public int getIconHeight() {
            return MAX_IMAGE_HEIGHT;
        }
    }

    private static class MyTooltip extends JToolTip {

        @Override
        public void setTipText(String tipText) {
            super.setTipText(tipText);
            if( getPreferredSize().width > 400 ) {
                View v = (View) getClientProperty("html"); //NOI18N
                if( null != v )
                    v.setSize(300.0f, 300.0f);
            }
        }

    }
}
