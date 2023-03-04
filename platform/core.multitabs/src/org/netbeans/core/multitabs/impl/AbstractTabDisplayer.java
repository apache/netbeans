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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.multitabs.ButtonFactory;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.core.multitabs.Settings;
import org.netbeans.core.multitabs.TabDisplayer;
import org.netbeans.core.multitabs.TabbedImpl;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;

/**
 *
 * @author S. Aubrecht
 */
abstract class AbstractTabDisplayer extends TabDisplayer implements MouseWheelListener, Autoscroll {

    protected Controller controller;
    protected final JScrollPane scrollPane;
    protected final int orientation;

    private final JToolBar controls;

    private final JLabel lblFullPath = new JLabel();

    private final ScrollAction scrollLeft;
    private final ScrollAction scrollRight;

    private final ChangeListener fullPathListener;
    private final ChangeListener projectsListener;

    public AbstractTabDisplayer( final TabDataModel tabModel, int tabsLocation ) {
        super( tabModel );
        setLayout( new BorderLayout( 3, 3 ) );
        this.orientation = tabsLocation == JTabbedPane.TOP || tabsLocation == JTabbedPane.BOTTOM ? JTabbedPane.HORIZONTAL : JTabbedPane.VERTICAL;
        scrollPane = new JScrollPane();
        controls = new ControlsToolbar();
        lblFullPath.setBorder( BorderFactory.createEmptyBorder( 0, 3, 2, 3) );
        Font defaultFont = lblFullPath.getFont();
        lblFullPath.setFont( defaultFont.deriveFont( defaultFont.getSize2D()-2 ) );
        JPanel controlsPanel = new JPanel( new BorderLayout() );
        controlsPanel.setOpaque( false );
        if( TabTableUI.IS_AQUA ) {
            Color backColor = UIManager.getColor( "NbSplitPane.background" ); //NOI18N
            if( null != backColor ) {
                setBackground( backColor );
                setOpaque( true );
            }
            Color white = Color.white;
            white = white.darker();
            lblFullPath.setForeground(white);
        }
        switch( tabsLocation ) {
            case JTabbedPane.TOP:
            case JTabbedPane.BOTTOM:
                add( scrollPane, BorderLayout.CENTER );
                controlsPanel.add( controls, BorderLayout.NORTH );
                add( controlsPanel, BorderLayout.EAST );
                if( Settings.getDefault().isShowFullPath() )
                    add( lblFullPath, BorderLayout.SOUTH );
                break;
            case JTabbedPane.LEFT:
            case JTabbedPane.RIGHT:
                add( scrollPane, BorderLayout.CENTER );
                controlsPanel.add( controls, BorderLayout.EAST );
                add( controlsPanel, BorderLayout.NORTH );
                break;
            default:
                throw new IllegalArgumentException( "Invalid orientation: " + tabsLocation );
        }
        configureScrollPane( scrollPane );
        scrollLeft = new ScrollAction( scrollPane, tabsLocation, true );
        scrollRight = new ScrollAction( scrollPane, tabsLocation, false );
        controls.add( ButtonFactory.createScrollLeftButton( scrollLeft ) );
        controls.add( ButtonFactory.createScrollRightButton( scrollRight ) );
        addMouseWheelListener( this );

        projectsListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                repaint();
            }
        };

        fullPathListener = new ChangeListener() {
            @Override
            public void stateChanged( ChangeEvent e ) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateFullPath();
                    }
                });
            }
        };
        tabModel.addChangeListener(fullPathListener);
    }

    private void configureScrollPane( JScrollPane scrollPane ) {
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_NEVER );
        scrollPane.setBorder( BorderFactory.createEmptyBorder() );
        scrollPane.setOpaque( false );
        scrollPane.getViewport().setOpaque( false );
        scrollPane.setFocusable( false );
        scrollPane.setWheelScrollingEnabled( false );
        scrollPane.setViewportBorder( BorderFactory.createEmptyBorder() );

        scrollPane.addComponentListener( new ComponentListener() {

            @Override
            public void componentResized( ComponentEvent e ) {
                showSelectedTab();
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
    }

    @Override
    public Color getBackground() {
        Color activeBackground = UIManager.getColor("nb.multitabs.activeBackground"); // NOI18N
        if (activeBackground != null && TabbedImpl.isActive(this)) {
            return activeBackground;
        }

        Color background = UIManager.getColor("nb.multitabs.background"); // NOI18N
        if (background != null) {
            return background;
        }

        return super.getBackground();
    }

    @Override
    public void attach( final Controller controller ) {
        this.controller = controller;
        controls.add( ButtonFactory.createDropDownButton( controller ) );
        controls.add( ButtonFactory.createMaximizeButton( controller ) );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ProjectSupport projects = ProjectSupport.getDefault();
        if( projects.isEnabled() ) {
            projects.addChangeListener(projectsListener);
        }
        if( null != controller )
            controller.addSelectionChangeListener( fullPathListener );
        updateFullPath();
    }

    @Override
    public void removeNotify() {
        ProjectSupport.getDefault().removeChangeListener(projectsListener);
        super.removeNotify();
        if( null != controller )
            controller.removeSelectionChangeListener( fullPathListener );
    }

    private void updateFullPath() {
        if( !lblFullPath.isVisible() || null == controller )
            return;
        String text = null;
        int selIndex = controller.getSelectedIndex();
        if( selIndex >= 0 && selIndex < tabModel.size() ) {
            TabData tab = tabModel.getTab( selIndex );
            if( null != tab ) {
                text = tab.getTooltip();
            }
        }
        lblFullPath.setText( text );
    }

    @Override
    public final void mouseWheelMoved( MouseWheelEvent e ) {
        scrollLeft.mouseWheelMoved( e );
        if( e.isConsumed() )
            return;
        scrollRight.mouseWheelMoved( e );
    }

    private void showSelectedTab() {
        if( null == controller )
            return;
        int selIndex = controller.getSelectedIndex();
        if( selIndex < 0 || selIndex >= controller.getTabModel().size() )
            return;
        Rectangle rect = getTabBounds( selIndex );
        if( null == rect )
            return;
        scrollPane.getViewport().scrollRectToVisible( rect );
    }

    @Override
    public Rectangle getTabsArea() {
        return scrollPane.getBounds();
    }

    @Override
    public Insets getAutoscrollInsets() {
        if( orientation == JTabbedPane.HORIZONTAL ) {
            return new Insets( 0, 25, 0, 25 );
        } else {
            return new Insets( 25, 0, 25, 0 );
        }
    }

    @Override
    public void autoscroll( Point cursorLocn ) {
        Rectangle tabsArea = getTabsArea();
        if( !tabsArea.contains( cursorLocn ) ) {
            autoscroller.stop();
            return;
        }
        if( orientation == JTabbedPane.HORIZONTAL ) {
            if( cursorLocn.x < tabsArea.x+25 ) {
                autoscroller.start( true );
            } else if( cursorLocn.x > tabsArea.x+tabsArea.width-25 ) {
                autoscroller.start( false );
            } else {
                autoscroller.stop();
            }
        } else {
            if( cursorLocn.y < tabsArea.y+25 ) {
                autoscroller.start( true );
            } else if( cursorLocn.y > tabsArea.y+tabsArea.height-25 ) {
                autoscroller.start( false );
            } else {
                autoscroller.stop();
            }
        }
    }


    private final Autoscroller autoscroller = new Autoscroller();

    private class Autoscroller implements ActionListener {

        private int direction = 0;
        private Timer timer;

        public void start( boolean scrollLeft ) {
            int newDirection = scrollLeft ? -1 : 1;
            if( null == timer || !timer.isRunning() || direction != newDirection ) {
                if( null == timer ) {
                    timer = new Timer( 300, this );
                    timer.setRepeats( true );
                }
                this.direction = newDirection;
                timer.start();
            }
        }

        public void stop() {
            if( null != timer ) {
                timer.stop();
            }
            direction = 0;
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            if( direction < 0 ) {
                if( scrollLeft.isEnabled() ) {
                    scrollLeft.actionPerformed( e );
                } else {
                    timer.stop();
                }
            } else if( direction > 0 ) {
                if( scrollRight.isEnabled() ) {
                    scrollRight.actionPerformed( e );
                } else {
                    timer.stop();
                }
            }
        }
    }
}
