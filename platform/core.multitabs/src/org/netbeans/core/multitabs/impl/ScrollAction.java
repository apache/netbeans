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
package org.netbeans.core.multitabs.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Scrolls the content of a TabDisplayer.
 *
 * @author S. Aubrecht
 */
class ScrollAction extends AbstractAction implements ChangeListener, MouseWheelListener {

    private final JScrollPane scrollPane;
    private final boolean isHorizontal;
    private final boolean isScrollLeft;

    public ScrollAction( JScrollPane scrollPane, int orientation, boolean isScrollLeft ) {
        this.isScrollLeft = isScrollLeft;
        this.scrollPane = scrollPane;
        this.isHorizontal = orientation == JTabbedPane.TOP || orientation == JTabbedPane.BOTTOM;
        scrollPane.getViewport().addChangeListener( this );
        scrollPane.addComponentListener( new ComponentListener() {

            @Override
            public void componentResized( ComponentEvent e ) {
                updateEnabled();
            }

            @Override
            public void componentMoved( ComponentEvent e ) {
            }

            @Override
            public void componentShown( ComponentEvent e ) {
            }

            @Override
            public void componentHidden( ComponentEvent e ) {
            }
        });
        updateEnabled();
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        int increment = getDefaultIncrement();

        scroll( increment );
    }

    private void updateEnabled() {
        boolean enable = true;
        Component content = scrollPane.getViewport().getView();
        if( null != content ) {
            Dimension size = content.getSize();
            Dimension viewSize = scrollPane.getViewport().getExtentSize();
            Point position = scrollPane.getViewport().getViewPosition();
            if( isHorizontal ) {
                if( isScrollLeft ) {
                    enable = position.x > 0;
                } else {
                    enable = position.x + viewSize.width < size.width;
                }
            } else {
                if( isScrollLeft ) {
                    enable = position.y > 0;
                } else {
                    enable = position.y + viewSize.height < size.height;
                }
            }
        }
        setEnabled( enable );
    }

    @Override
    public void stateChanged( ChangeEvent e ) {
        updateEnabled();
    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent e ) {
        if( !isEnabled() )
            return;
        int rotation = e.getWheelRotation();
        if( (rotation < 0 && isScrollLeft)
                || (rotation > 0 && !isScrollLeft ) ) {
            int increment = getDefaultIncrement();
            increment *= Math.abs( rotation );
            scroll( increment );
            e.consume();
        }
    }

    private int getDefaultIncrement() {
        Component content = scrollPane.getViewport().getView();
        Point position = scrollPane.getViewport().getViewPosition();
        int increment = isHorizontal ? 30 : 8;
        Dimension viewSize = scrollPane.getViewport().getExtentSize();
        if( content instanceof Scrollable ) {
            increment = ((Scrollable)content).getScrollableUnitIncrement( new Rectangle( position, viewSize ),
                    isHorizontal ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL,
                    isScrollLeft ? -1 : 1 );
        }
        return increment;
    }

    private void scroll( int increment ) {
        Component content = scrollPane.getViewport().getView();
        Dimension size = content.getSize();
        Dimension viewSize = scrollPane.getViewport().getExtentSize();
        Point position = scrollPane.getViewport().getViewPosition();

        if( isHorizontal ) {
            position.x += increment;
            position.x = Math.max( position.x, 0 );
            position.x = Math.min( position.x, size.width-viewSize.width );
        } else {
            position.y += increment;
            position.y = Math.max( position.y, 0 );
            position.y = Math.min( position.y, size.height-viewSize.height );
        }

        scrollPane.getViewport().setViewPosition( position );
    }
}
