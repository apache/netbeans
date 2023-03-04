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
package org.netbeans.core.windows.view.ui.tabcontrol;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.actions.ActionUtils;
import org.netbeans.swing.tabcontrol.ComponentConverter;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.swing.tabcontrol.plaf.EqualPolygon;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 * Implements some common Tabbed functionality.
 * 
 * @author S. Aubrecht
 */
public abstract class AbstractTabbedImpl extends Tabbed {

    protected abstract TabDataModel getTabModel();
    protected abstract SingleSelectionModel getSelectionModel();
    private PropertyChangeListener tooltipListener, weakTooltipListener;
    private final ChangeSupport cs = new ChangeSupport(this);

    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(TabbedAdapter.class);

    @Override
    public final void addTopComponent( String name, javax.swing.Icon icon, TopComponent tc, String toolTip ) {
        insertComponent( name, icon, tc, toolTip, getTabCount() );
    }

    @Override
    public final TopComponent getTopComponentAt( int index ) {
        if( index == -1 || index >= getTabModel().size() ) {
            return null;
        }
        return ( TopComponent ) getTabModel().getTab( index ).getComponent();
    }

    @Override
    public final TopComponent getSelectedTopComponent() {
        int i = getSelectionModel().getSelectedIndex();
        return i == -1 ? null : getTopComponentAt( i );
    }

    @Override
    public final void requestAttention( TopComponent tc ) {
        int idx = indexOf( tc );
        if( idx >= 0 ) {
            requestAttention( idx );
        } else {
            Logger.getAnonymousLogger().fine(
                    "RequestAttention on component unknown to container: " + tc ); //NOI18N
        }
    }

    protected abstract void requestAttention( int tabIndex );

    @Override
    public final void cancelRequestAttention( TopComponent tc ) {
        int idx = indexOf( tc );
        if( idx >= 0 ) {
            cancelRequestAttention( idx );
        } else {
            throw new IllegalArgumentException( "TopComponent " + tc
                    + " is not a child of this container" ); //NOI18N
        }
    }

    protected abstract void cancelRequestAttention( int tabIndex );


    @Override
    public final void setAttentionHighlight( TopComponent tc, boolean highlight ) {
        int idx = indexOf( tc );
        if( idx >= 0 ) {
            setAttentionHighlight( idx, highlight );
        } else {
            throw new IllegalArgumentException( "TopComponent " + tc
                    + " is not a child of this container" ); //NOI18N
        }
    }

    /**
     *
     * @param tabIndex
     * @param highlight
     * @since 2.54
     */
    protected abstract void setAttentionHighlight( int tabIndex, boolean highlight );

    @Override
    public final void insertComponent( String name, javax.swing.Icon icon, Component comp, String toolTip, int position ) {
        TabData td = new TabData( comp, icon, name, toolTip );

        if( DEBUG ) {
            Debug.log( AbstractTabbedImpl.class, "InsertTab: " + name + " hash:" + System.identityHashCode( comp ) ); // NOI18N
        }

        getTabModel().addTab( position, td );
        comp.addPropertyChangeListener( JComponent.TOOL_TIP_TEXT_KEY, getTooltipListener( comp ) );
    }

    @Override
    public final void setSelectedComponent( Component comp ) {
        int i = indexOf( comp );
        if( i == -1 && null != comp ) {
            throw new IllegalArgumentException(
                    "Component not a child of this control: " + comp ); //NOI18N
        } else {
            getSelectionModel().setSelectedIndex( i );
        }
    }

    @Override
    public final TopComponent[] getTopComponents() {
        ComponentConverter cc = getComponentConverter();
        TabData[] td = ( TabData[] ) getTabModel().getTabs().toArray( new TabData[0] );
        TopComponent[] result = new TopComponent[getTabModel().size()];
        for( int i = 0; i < td.length; i++ ) {
            result[i] = ( TopComponent ) cc.getComponent( td[i] );
        }
        return result;
    }

    @Override
    public final void removeComponent( Component comp ) {
        int i = indexOf( comp );
        getTabModel().removeTab( i );
        comp.removePropertyChangeListener( JComponent.TOOL_TIP_TEXT_KEY, getTooltipListener( comp ) );
        if( getTabModel().size() == 0 ) {
            ((JComponent)getComponent()).revalidate();
            ((JComponent)getComponent()).repaint();
        }
    }

    @Override
    public final void setTopComponents( TopComponent[] tcs, TopComponent selected ) {
        // #100144 - correct null selection and log, probably some problem in
        // winsys model consistency, but without reproduction no chance to find out
        if( selected == null && tcs.length > 0 ) {
            selected = tcs[0];
            Logger.getLogger( TabbedAdapter.class.getName() ).warning(
                    "Selected component is null although open components are "
                    + Arrays.asList( tcs ) );
        }
        int sizeBefore = getTabModel().size();

        detachTooltipListeners( getTabModel().getTabs() );

        TabData[] data = new TabData[tcs.length];
        int toSelect = -1;
        for( int i = 0; i < tcs.length; i++ ) {
            TopComponent tc = tcs[i];
            Image icon = tc.getIcon();
            String displayName = WindowManagerImpl.getInstance().getTopComponentDisplayName( tc );
            data[i] = new TabData(
                    tc,
                    icon == null ? null : ImageUtilities.image2Icon( icon ),
                    displayName == null ? "" : displayName, // NOI18N
                    tc.getToolTipText() );
            if( selected == tcs[i] ) {
                toSelect = i;
            }
            tc.addPropertyChangeListener( JComponent.TOOL_TIP_TEXT_KEY, getTooltipListener( tc ) );
        }

        //DO NOT DELETE THIS ASSERTION AGAIN!
        //If it triggered, it means there is a problem in the state of the
        //window system's model.  If it is just diagnostic logging, there
        //*will* be an exception later, it just won't contain any useful
        //information. See issue 39914 for what happens if it is deleted.
        assert selected != null && toSelect != -1 : "Tried to set a selected component that was "
                + " not in the array of open components. ToSelect: " + selected + " ToSelectName=" + selected.getDisplayName()
                + " ToSelectClass=" + selected.getClass()
                + " open components: " + Arrays.asList( tcs );

        getTabModel().setTabs( data );

        if( toSelect != -1 ) {
            getSelectionModel().setSelectedIndex( toSelect );
        } else if( selected != null ) {
            //Assertions are off
            Logger.getAnonymousLogger().warning( "Tried to"
                    + "set a selected component that was not in the array of open "
                    + "components.  ToSelect: " + selected + " components: "
                    + Arrays.asList( tcs ) );
        }
        int sizeNow = getTabModel().size();
        if( sizeBefore != 0 && sizeNow == 0 ) {
            //issue 40076, ensure repaint if the control has been emptied.
            ((JComponent)getComponent()).revalidate();
            ((JComponent)getComponent()).repaint();
        }
    }
    // DnD>>

    /**
     * Finds tab which contains x coordinate of given location point.
     *
     * @param location The point for which a constraint is required
     * @return Integer object representing found tab index. Returns null if no
     * such tab can be found.
     */
    @Override
    public final Object getConstraintForLocation( Point location, boolean attachingPossible ) {
        //#47909
        // first process the tabs when mouse is inside the tabs area..
        int tab = tabForCoordinate( location );
        if( tab != -1 ) {
            int index = dropIndexOfPoint( location );
            return index < 0 ? null : Integer.valueOf( index );
        }
        // ----
        if( attachingPossible ) {
            String s = getSideForLocation( location );
            if( s != null ) {
                return s;
            }
        }
        int index = dropIndexOfPoint( location );
        return index < 0 ? null : Integer.valueOf( index );
    }

    protected abstract int dropIndexOfPoint( Point location );


    private String getSideForLocation(Point location) {
        Rectangle bounds = getComponent().getBounds();
        bounds.setLocation(0, 0);

        final int TOP_HEIGHT = 10;
        final int BOTTOM_HEIGHT = (int)(0.25 * bounds.height);

        final int LEFT_WIDTH = Math.max (getComponent().getWidth() / 8, 40);
        final int RIGHT_WIDTH = LEFT_WIDTH;

        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("TOP_HEIGHT    =" + TOP_HEIGHT); // NOI18N
            debugLog("BOTTOM_HEIGHT =" + BOTTOM_HEIGHT); // NOI18N
            debugLog("LEFT_WIDTH    =" + LEFT_WIDTH); // NOI18N
            debugLog("RIGHT_WIDTH   =" + RIGHT_WIDTH); // NOI18N
        }

        // Size of area which indicates creation of new split.
//        int delta = Constants.DROP_AREA_SIZE;
        Rectangle top = new Rectangle(0, 0, bounds.width, BOTTOM_HEIGHT);
        if(top.contains(location)) {
            return Constants.TOP;
        }

        Polygon left = new EqualPolygon(
            new int[] {0, LEFT_WIDTH, LEFT_WIDTH, 0},
            new int[] {TOP_HEIGHT, TOP_HEIGHT, bounds.height - BOTTOM_HEIGHT, bounds.height},
            4
        );
        if(left.contains(location)) {
            return Constants.LEFT;
        }

        Polygon right = new EqualPolygon(
            new int[] {bounds.width - RIGHT_WIDTH, bounds.width, bounds.width, bounds.width - RIGHT_WIDTH},
            new int[] {TOP_HEIGHT, TOP_HEIGHT, bounds.height, bounds.height - BOTTOM_HEIGHT},
            4
        );
        if(right.contains(location)) {
            return Constants.RIGHT;
        }

        Polygon bottom = new EqualPolygon(
            new int[] {LEFT_WIDTH, bounds.width - RIGHT_WIDTH, bounds.width, 0},
            new int[] {bounds.height - BOTTOM_HEIGHT, bounds.height - BOTTOM_HEIGHT, bounds.height, bounds.height},
            4
        );
        if(bottom.contains(location)) {
            return Constants.BOTTOM;
        }

        return null;
    }

    /**
     * Computes and returns feedback indication shape for given location point.
     * TBD - extend for various feedback types
     *
     * @return Shape representing feedback indication
     */
    @Override
    public final Shape getIndicationForLocation( Point location,
            TopComponent startingTransfer, Point startingPoint, boolean attachingPossible ) {

        Rectangle rect = getComponent().getBounds();
        rect.setLocation( 0, 0 );

        TopComponent draggedTC = startingTransfer;
        //#47909
        int tab = tabForCoordinate( location );
        // first process the tabs when mouse is inside the tabs area..
        // need to process before the side resolution.
        if( tab != -1 ) {
            Shape s = getDropIndication( draggedTC, location );
            if( s != null ) {
                return s;
            }
        }

        String side;
        if( attachingPossible ) {
            side = getSideForLocation( location );
        } else {
            side = null;
        }

        double ratio = Constants.DROP_TO_SIDE_RATIO;
        if( side == Constants.TOP ) {
            return new Rectangle( 0, 0, rect.width, ( int ) (rect.height * ratio) );
        } else if( side == Constants.LEFT ) {
            return new Rectangle( 0, 0, ( int ) (rect.width * ratio), rect.height );
        } else if( side == Constants.RIGHT ) {
            return new Rectangle( rect.width - ( int ) (rect.width * ratio), 0, ( int ) (rect.width * ratio), rect.height );
        } else if( side == Constants.BOTTOM ) {
            return new Rectangle( 0, rect.height - ( int ) (rect.height * ratio), rect.width, ( int ) (rect.height * ratio) );
        }

        // #47909 now check shape again.. when no sides were checked, assume changing tabs when in component center.
        Shape s = getDropIndication( draggedTC, location );
        if( s != null ) {
            return s;
        }

        if( null != startingTransfer && startingPoint != null
                && indexOf( startingTransfer ) != -1 ) {
            return getStartingIndication( startingPoint, location );
        }

        return rect;
    }

    private Shape getStartingIndication(Point startingPoint, Point location) {
        Rectangle rect = getComponent().getBounds();
        rect.setLocation(location.x - startingPoint.x, location.y - startingPoint.y);
        return rect;
    }

    /**
     * Add action for enabling auto hide of views
     */
    @Override
    public final Action[] getPopupActions( Action[] defaultActions, int tabIndex ) {
        if( tabIndex < 0 ) {
            ModeImpl mode = getModeImpl();
            if( null != mode ) {
                return ActionUtils.createDefaultPopupActions( mode );
            }
            return null;
        }
        return defaultActions;
    }

    private ModeImpl getModeImpl() {
        TopComponent[] topComponents = getTopComponents();
        if( topComponents.length < 1 )
            return null;
        return ( ModeImpl ) WindowManagerImpl.getInstance().findMode( topComponents[0] );
    }

    /**
     * Registers ChangeListener to receive events.
     *
     * @param listener The listener to register.
     *
     */
    @Override
    public final void addChangeListener( ChangeListener listener ) {
        cs.addChangeListener( listener );
    }

    /**
     * Removes ChangeListener from the list of listeners.
     *
     * @param listener The listener to remove.
     *
     */
    @Override
    public final void removeChangeListener( ChangeListener listener ) {
        cs.removeChangeListener( listener );
    }

    @Override
    public boolean isBusy( TopComponent tc ) {
        return WindowManagerImpl.getInstance().isTopComponentBusy( tc );
    }

    protected abstract ComponentConverter getComponentConverter();

    /** Returns instance of weak property change listener used to listen to
     * tooltip changes. Weak listener is needed, in some situations (close of
     * whole mode), our class is not notified from winsys.
     */
    PropertyChangeListener getTooltipListener(Component comp) {
        if (tooltipListener == null) {
            tooltipListener = new ToolTipListener();
            weakTooltipListener = WeakListeners.propertyChange(tooltipListener, comp);
        }
        return weakTooltipListener;
    }

    protected abstract Shape getDropIndication( TopComponent draggedTC, Point location );

    /** Listening to changes of tooltips of currently asociated top components */
    private class ToolTipListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (JComponent.TOOL_TIP_TEXT_KEY.equals(evt.getPropertyName())) {
                java.util.List tabs = getTabModel().getTabs();
                JComponent curComp;
                int index = 0;
                for (Iterator iter = tabs.iterator(); iter.hasNext(); index++) {
                    curComp = (JComponent)((TabData)iter.next()).getComponent();
                    if (curComp == evt.getSource() && index < getTabCount()) {
                        setToolTipTextAt(index, (String)evt.getNewValue());
                        break;
                    }
                }
            }
        }

    }

    /** Removes tooltip listeners from given tabs */
    private void detachTooltipListeners(java.util.List tabs) {
        JComponent curComp;
        for (Iterator iter = tabs.iterator(); iter.hasNext(); ) {
            curComp = (JComponent)((TabData)iter.next()).getComponent();
            curComp.removePropertyChangeListener(JComponent.TOOL_TIP_TEXT_KEY,
                                                 getTooltipListener(curComp));
        }
    }

    /** Notifies all registered listeners about the event. */
    protected final void fireStateChanged() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Logger.getAnonymousLogger().warning(
                "All state changes to the tab component must happen on the event thread!"); //NOI18N
            Exception e = new Exception();
            e.fillInStackTrace();
            Logger.getAnonymousLogger().warning(e.getStackTrace()[1].toString());
        }

        //Note: Firing the events while holding the tree lock avoids many
        //gratuitous repaints that slow down switching tabs.  To demonstrate this,
        //comment this code out and run the IDE with -J-Dawt.nativeDoubleBuffering=true
        //so you'll really see every repaint.  When switching between a form
        //tab and an editor tab, you will see the property sheet get repainted
        //8 times due to changes in the component hierarchy, before the
        //selected node is even changed to the appropriate one for the new tab.
        //Synchronizing here ensures that never happens.

        // [dafe]: Firing under tree lock is bad practice and causes deadlocking,
        // see http://www.netbeans.org/issues/show_bug.cgi?id=120874
        // Comments above seems not to be valid anymore in JDK 1.5 and
        // newer. Even if running with -J-Dawt.nativeDoubleBuffering=true, there
        // is no difference in number of repaints no matter if holding the tree
        // lock or not. Repeated redrawing seems to be coalesced well, so removing
        // AWT tree lock

        cs.fireChange();
    }

    private static void debugLog(String message) {
        Debug.log(TabbedAdapter.class, message);
    }
}
