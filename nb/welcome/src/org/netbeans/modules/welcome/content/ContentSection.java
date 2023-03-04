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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author S. Aubrecht
 */
public class ContentSection extends JPanel implements Constants {

    private static final int PANEL_MAX_WIDTH = 800;
    private boolean maxSize;

    public ContentSection( String title, JComponent content, boolean maxSize ) {
        this( content, maxSize, 0 );
        JLabel lblTitle = new JLabel( title );
        lblTitle.setFont( SECTION_HEADER_FONT );

        lblTitle.setBorder( BorderFactory.createEmptyBorder(0, 0, 20, 0) );
        lblTitle.setForeground( Utils.getColor( COLOR_SECTION_HEADER ) );
        add( lblTitle, new GridBagConstraints(0,0,1,1,0.0,0.0,
                GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(6,0,0,0),0,0) );
    }

    public ContentSection( JComponent titleComponent, JComponent content, boolean maxSize ) {
        this( content, maxSize, 8 );
        if( null != titleComponent ) {
            add( titleComponent, new GridBagConstraints(0,0,1,1,1.0,0.0,
                    GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,0,15,0),0,0) );
        }
    }

    public ContentSection( JComponent content, boolean maxSize ) {
        this( content, maxSize, 0 );
    }

    private ContentSection( JComponent content, boolean maxSize, int leftInsets ) {
        super( new GridBagLayout() );
        setOpaque(false);
        this.maxSize = maxSize;
        add( content, new GridBagConstraints(0,1,2,1,1.0,1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,leftInsets,0,0),0,0) );
        
        setBorder( BorderFactory.createEmptyBorder(22,35,15,35) );
    }

    @Override
    public void setSize(Dimension d) {
        if( maxSize && d.width > PANEL_MAX_WIDTH ) {
            d = new Dimension( d );
            d.width = PANEL_MAX_WIDTH;
        }
        super.setSize(d);
    }

    @Override
    public void setBounds(Rectangle r) {
        if( maxSize && r.width > PANEL_MAX_WIDTH ) {
            r = new Rectangle( r );
            r.width = PANEL_MAX_WIDTH;
        }
        super.setBounds(r);
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        if( maxSize && w > PANEL_MAX_WIDTH ) {
            w = PANEL_MAX_WIDTH;
        }
        super.setBounds(x,y,w,h);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if( maxSize && d.width > PANEL_MAX_WIDTH ) {
            d = new Dimension( d );
            d.width = PANEL_MAX_WIDTH;
        }
        return d;
    }
}
