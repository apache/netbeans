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
package org.netbeans.core.windows.view.ui.tabcontrol.tabbedpane;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import org.netbeans.swing.tabcontrol.*;
import org.netbeans.swing.tabcontrol.customtabs.TabbedType;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;

/**
 * A Tabbed Container based on JTabbedPane that can be used as a replacement for
 *
 * @see TabbedContainer. The benefit of this is easy integration with third
 * party look and feels without the need to provide UI delegates.
 *
 * Compared to TabbedContainer NBTabbedPane is more configurable. It is possible
 * to set the TabLayoutPolicy ( JTabbedPane.SCROLL_TAB_LAYOUT or
 * JTabbedPane.WRAP_TAB_LAYOUT ) and tab placement ( SwingConstants.TOP, ...).
 *
 * @author eppleton
 * @author S. Aubrecht
 */
public class NBTabbedPane extends JTabbedPane {

    private final WinsysInfoForTabbedContainer winsysInfo;
    private final TabDataModel dataModel;
    /**
     * The type of this container, which determines how the tabs are displayed
     */
    protected final TabbedType type;
    private transient List<ActionListener> actionListenerList;
    private boolean active;
    private ComponentConverter converter;

    /**
     * Create a new pane with the specified model, displayer type and extra
     * information from winsys
     */
    public NBTabbedPane( TabDataModel model, TabbedType type, WinsysInfoForTabbedContainer winsysInfo ) {
        switch( type ) {
            case VIEW:
            case EDITOR:
                break;
            default:
                throw new IllegalArgumentException( "Unsupported UI type: " + type ); //NOI18N
        }
        if( model == null ) {
            model = new DefaultTabDataModel();
        }
        this.dataModel = model;
        this.type = type;

        this.winsysInfo = winsysInfo;

        updateUI();

        setFocusable( false );
    }

    public WinsysInfoForTabbedContainer getWinsysInfoForTabbedContainer() {
        return winsysInfo;
    }

    /**
     * Used by Controller to post action events for selection and close
     * operations. If the event is consumed, the UI should take no action to
     * change the selection or close the tab, and will presume that the receiver
     * of the event is handling performing whatever action is appropriate.
     *
     * @param event The event to be fired
     */
    protected final void postActionEvent( TabActionEvent event ) {
        List<ActionListener> list;
        synchronized( this ) {
            if( actionListenerList == null ) {
                return;
            }
            list = Collections.unmodifiableList( actionListenerList );
        }
        for( ActionListener l : list ) {
            l.actionPerformed( event );
        }
    }

    /**
     * Get the type of this displayer - it is either TYPE_EDITOR or TYPE_VIEW.
     * This property is set in the constructor and is immutable
     */
    public final TabbedType getType() {
        return type;
    }

    /**
     * Get the index of a component
     */
    public int indexOf( Component comp ) {
        if( null == comp )
            return -1;
        return indexOfComponent( comp );
    }

    /**
     * Get the component converter which is used to fetch a component
     * corresponding to an element in the data model. If the value has not been
     * set, it will use ComponentConverter.DEFAULT, which simply delegates to
     * TabData.getComponent().
     */
    public final ComponentConverter getComponentConverter() {
        if( converter != null ) {
            return converter;
        }
        return ComponentConverter.DEFAULT;
    }

    /**
     * Set the converter that converts user objects in the data model into
     * components to display. If set to null (the default), the user object at
     * the selected index in the data model will be cast as an instance of
     * JComponent when searching for what to show for a given tab. <p> For use
     * cases where a single component is to be displayed for more than one tab,
     * just reconfigured when the selection changes, simply supply a
     * ComponentConverter.Fixed with the component that should be used for all
     * tabs.
     */
    public final void setComponentConverter( ComponentConverter cc ) {
        ComponentConverter old = converter;
        converter = cc;
        if( old instanceof ComponentConverter.Fixed && cc instanceof ComponentConverter.Fixed ) {
            List<TabData> l = getDataModel().getTabs();
            if( !l.isEmpty() ) {
                TabData[] td = l.toArray( new TabData[0] );
                getDataModel().setTabs( new TabData[0] );
                getDataModel().setTabs( td );
            }
        }
    }

    /**
     * Register an ActionListener. NBTabbedPane guarantees that the type of
     * event fired will always be TabActionEvent. There are two special things
     * about TabActionEvent: <ol> <li>There are methods on TabActionEvent to
     * find the index of the tab the event was performed on, and if present,
     * retrieve the mouse event that triggered it, for clients that wish to
     * provide different handling for different mouse buttons</li>
     * <li>TabActionEvents can be consumed. If a listener consumes the event,
     * the UI will take no action - the selection will not be changed, the tab
     * will not be closed. Consuming the event means taking responsibility for
     * doing whatever would normally happen automatically. This is useful for,
     * for example, showing a dialog and possibly aborting closing a tab if it
     * contains unsaved data, for instance.</li> </ol> Action events will be
     * fired <strong>before</strong> any action has been taken to alter the
     * state of the control to match the action, so that they may be vetoed or
     * modified by consuming the event.
     *
     * @param listener The listener to register.
     */
    public final synchronized void addActionListener( ActionListener listener ) {
        if( actionListenerList == null ) {
            actionListenerList = new ArrayList<ActionListener>();
        }
        actionListenerList.add( listener );
    }

    /**
     * Remove an action listener.
     *
     * @param listener The listener to remove.
     */
    public final synchronized void removeActionListener(
            ActionListener listener ) {
        if( actionListenerList != null ) {
            actionListenerList.remove( listener );
            if( actionListenerList.isEmpty() ) {
                actionListenerList = null;
            }
        }
    }

    public TabDataModel getDataModel() {
        return dataModel;
    }

    /**
     * The index at which a tab should be inserted if a drop operation occurs at
     * this point.
     *
     * @param location A point anywhere on the TabbedContainer
     * @return A tab index, or -1
     */
    public int dropIndexOfPoint( Point location ) {
        int index = indexAtLocation( location.x, location.y );
        if( index < 0 ) {
            index = getTabCount();
        } else if( index == getTabCount()-1 ) {
            Rectangle rect = getBoundsAt( index );
            if( getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM ) {
                if( location.x > rect.x + rect.width/2 )
                    index++;
            } else {
                if( location.y > rect.y + rect.height/2 )
                    index++;
            }
        }

        return index;
    }

    /**
     * Cause the tab at the specified index to blink or otherwise suggest that
     * the user should click it.
     */
    public final void requestAttention( int tab ) {
        startBlinking( tab, Color.RED, Color.BLUE );
    }

    public final void cancelRequestAttention( int tab ) {
        stopBlinking();
    }

    /**
     *
     * @param tabIndex
     * @param highlight
     * @since 2.54
     */
    public final void setAttentionHighlight( int tabIndex, boolean highlight ) {
        //TODO implement
    }

    public final void setActive( boolean active ) {
        if( active != this.active ) {
            this.active = active;
            firePropertyChange( TabbedContainer.PROP_ACTIVE, !active, active );
        }
    }

    public int tabForCoordinate( Point p ) {
        return indexAtLocation( p.x, p.y );
    }

    public Image createImageOfTab( int tabIndex ) {
        //TODO revisit!
        TabData td = getDataModel().getTab( tabIndex );

        JLabel lbl = new JLabel( td.getText() );
        int width = lbl.getFontMetrics( lbl.getFont() ).stringWidth( td.getText() );
        int height = lbl.getFontMetrics( lbl.getFont() ).getHeight();
        width = width + td.getIcon().getIconWidth() + 6;
        height = Math.max( height, td.getIcon().getIconHeight() ) + 5;

        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage image = config.createCompatibleImage( width, height );
        Graphics2D g = image.createGraphics();
        g.setColor( lbl.getForeground() );
        g.setFont( lbl.getFont() );
        td.getIcon().paintIcon( lbl, g, 0, 0 );
        g.drawString( td.getText(), 18, height / 2 );


        return image;
    }

    // flash to requestAttention
    private int _tabIndex;
    private Color _background;
    private Color _foreground;
    private Color _savedBackground;
    private Color _savedForeground;
    private int count;
    private int blinks = 3;
    private Timer timer = new Timer( 1000, new ActionListener() {

        private boolean on = false;

        @Override
        public void actionPerformed( ActionEvent e ) {
            count = 0;
            blink( on );
            on = !on;
        }
    } );

    public void startBlinking( int tabIndex, Color foreground, Color background ) {

        _tabIndex = tabIndex;
        _savedForeground = getForeground();
        _savedBackground = getBackground();
        _foreground = foreground;
        _background = background;
        timer.start();
    }

    private void blink( boolean on ) {
        if( count >= blinks ) {
            stopBlinking();
        }
        count++;
        if( on ) {
            if( _foreground != null ) {
                setForegroundAt( _tabIndex, _foreground );
            }
            if( _background != null ) {
                setBackgroundAt( _tabIndex, _background );
            }
        } else {
            if( _savedForeground != null ) {
                setForegroundAt( _tabIndex, _savedForeground );
            }
            if( _savedBackground != null ) {
                setBackgroundAt( _tabIndex, _savedBackground );
            }
        }
        repaint();
    }

    public void stopBlinking() {
        timer.stop();
        setForegroundAt( _tabIndex, _savedForeground );
        setBackgroundAt( _tabIndex, _savedBackground );
    }
}
