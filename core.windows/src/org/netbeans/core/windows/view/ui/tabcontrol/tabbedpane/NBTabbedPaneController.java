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

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.customtabs.TabbedType;
import org.netbeans.swing.tabcontrol.event.*;
import org.openide.windows.TopComponent;

/**
 * Syncs the TabDataModel with the JTabbedPane
 *
 * @author eppleton
 * @author S. Aubrecht
 */
public class NBTabbedPaneController {

    protected ComplexListDataListener modelListener = null;
    private NBTabbedPane container;
    protected final Controller controller;

    public NBTabbedPaneController( final NBTabbedPane container ) {
        this.container = container;
        modelListener = new ComplexListDataListener() {

            @Override
            public void indicesAdded( ComplexListDataEvent e ) {

                int[] indices = e.getIndices();
                for( int i = 0; i < indices.length; i++ ) {

                    addToContainer( container.getDataModel().getTab( indices[i] ), 0 );
                }
            }

            @Override
            public void indicesRemoved( ComplexListDataEvent e ) {
                int[] indices = e.getIndices();
                TabData[] removedTabs = e.getAffectedItems();
                Component curComp;
                for( int i = 0; i < indices.length; i++ ) {
                    curComp = toComp( removedTabs[i] );
                    // TBD - add assertion curComp.getParent() != contentDisplayer
                    container.remove( curComp );
                }
            }

            @Override
            public void indicesChanged( ComplexListDataEvent e ) {
                if( e instanceof VeryComplexListDataEvent ) {
                    ArrayDiff dif = (( VeryComplexListDataEvent ) e).getDiff();

                    //Get the deleted and added indices
                    Set<Integer> deleted = dif.getDeletedIndices();
                    Set<Integer> added = dif.getAddedIndices();

                    //Get the TabData array from before the change
                    TabData[] old = dif.getOldData();
                    //Get the TabData array from after the change
                    TabData[] nue = dif.getNewData();

                    //Now we need to fetch the set of components we should end up
                    //displaying.  We need to do this because TabData.equals is only
                    //true if the text *and* the component match.  So if the winsys
                    //called setTabs just to change the title of a tab, we would
                    //end up removing and re-adding the component for no reason.
                    Set<Component> components = new HashSet<Component>();
                    for( int i = 0; i < nue.length; i++ ) {
                        components.add( toComp( nue[i] ) );
                    }
                    boolean changed = false;

                    synchronized( container.getTreeLock() ) {

                        if( added.isEmpty() && deleted.isEmpty() && !dif.getMovedIndices().isEmpty() ) {
                            //just reorder - since there's no way without moving
                            //a tab to a different position without firing removeNotify()
                            //let's just remove all and readd everyting
                            for( int i=0; i<container.getTabCount(); i++ ) {
                                added.add( i );
                                deleted.add( i );
                            }
                            components.clear();
                        }

                        //See if we've got anything to delete
                        if( !deleted.isEmpty() ) {
                            Iterator<Integer> i = deleted.iterator();
                            while( i.hasNext() ) {
                                //Get the index into the old array of a deleted tab
                                Integer idx = i.next();
                                //Find the TabData object for it
                                TabData del = old[idx.intValue()];
                                //Make sure its component is not one we'll be adding
                                if( !components.contains( toComp( del ) ) ) {
                                    //remove it
                                    container.remove( toComp( del ) );
                                    changed = true;
                                }
                            }
                        }

                        //See if we've got anything to add
                        if( !added.isEmpty() ) {
                            Iterator<Integer> i = added.iterator();
                            while( i.hasNext() ) {
                                //Get the index into the new array of the added tab
                                Integer idx = i.next();
                                //Find the TabData object that was added
                                TabData add = nue[idx.intValue()];
                                //Make sure it's not already showing so we don't do
                                //extra work
                                if( !container.isAncestorOf(
                                        toComp( add ) ) ) {
                                    addToContainer( add, idx );
                                    changed = true;
                                }
                            }
                        }
                    }
                    //repaint
                    if( changed ) {
                        container.revalidate();
                        container.repaint();
                    }
                }
            }

            @Override
            public void intervalAdded( ListDataEvent e ) {
                Component curC = null;
                for( int i = e.getIndex0(); i <= e.getIndex1(); i++ ) {
                    curC = toComp( container.getDataModel().getTab( i ) );
                    addToContainer( container.getDataModel().getTab( i ), 0 );
                }
            }

            @Override
            public void intervalRemoved( ListDataEvent e ) {
                //  we know that it must be complex data event
                ComplexListDataEvent clde = ( ComplexListDataEvent ) e;
                TabData[] removedTabs = clde.getAffectedItems();
                Component curComp;
                for( int i = 0; i < removedTabs.length; i++ ) {
                    curComp = toComp( removedTabs[i] );
                    container.remove( curComp );
                }
            }

            /**
             * This method is called to scroll the selected tab into view if its
             * title changes (it may be scrolled offscreen). NetBeans' editor
             * uses this to ensure that the user can see what file they're
             * editing when the user starts typing (this triggers a * being
             * appended to the tab title, thus triggering this call).
             */
            private void maybeMakeSelectedTabVisible( ComplexListDataEvent clde ) {
                if( !container.isShowing() || container.getWidth() < 10 ) {
                    //Java module fires icon changes from badging before the
                    //main window has been validated for the first time
                    return;
                }
                if( container.getType() == TabbedType.EDITOR ) {
                    int idx = container.getModel().getSelectedIndex();
                    //If more than one tab changed, it's probably not an event we want.
                    //Only do this if there is only one.
                    if( (clde.getIndex0() == clde.getIndex1()) && clde.getIndex0() == idx ) {
                        container.setSelectedIndex( idx );
                    }
                }
            }

            /**
             * DefaultTabDataModel will always call this method with an instance
             * of ComplexListDataEvent.
             */
            @Override
            public void contentsChanged( ListDataEvent e ) {
                //Only need to reread components on setTab (does winsys even use it?)
                if( e instanceof ComplexListDataEvent ) {
                    ComplexListDataEvent clde = ( ComplexListDataEvent ) e;
                    int index = clde.getIndex0();
                    if( clde.isUserObjectChanged() && index != -1 ) {
                        Component comp = container.getComponent( index );
                        container.remove( comp );

                        boolean add = index == container.getModel().getSelectedIndex();

                        if( add ) {
                            addToContainer( container.getDataModel().getTab( index ), index );
                        }
                    }
                    if( clde.isTextChanged() ) {
                        maybeMakeSelectedTabVisible( clde );
                    }
                }
            }
        };
        controller = new Controller();
        container.addMouseListener( controller );
    }

    private void addToContainer( TabData tabData, int index ) {
        if( index > container.getTabCount() )
            index = -1;
        container.add( tabData.getComponent(), index );
        index = container.indexOfComponent( tabData.getComponent() );
        container.setTabComponentAt( index, new CloseableTabComponent( tabData.getIcon(),
                tabData.getText(), true, tabData.getTooltip(), container, controller ) );
    }

    /**
     * Convenience method for fetching a component from a TabData object via the
     * container's ComponentConverter
     */
    protected final Component toComp( TabData data ) {
        return container.getComponentConverter().getComponent( data );
    }

    /**
     * Begin listening to the model for changes in the selection, which should
     * cause us to update the displayed component in the content
     * contentDisplayer. Listening starts when the component is first shown, and
     * stops when it is hidden; if you override
     * <code>createComponentListener()</code>, you will need to call this method
     * when the component is shown.
     */
    public void attachModelAndSelectionListeners() {
        container.getDataModel().addComplexListDataListener( modelListener );
    }

    /**
     * Stop listening to the model for changes in the selection, which should
     * cause us to update the displayed component in the content
     * contentDisplayer, and changes in the data model which can affect the
     * displayed component. Listening starts when the component is first shown,
     * and stops when it is hidden; if you override
     * <code>createComponentListener()</code>, you will need to call this method
     * when the component is hidden.
     */
    protected void detachModelAndSelectionListeners() {
        container.getDataModel().removeComplexListDataListener( modelListener );

    }

    /**
     * Listen to mouse events and handles selection behaviour and close icon
     * button behaviour.
     */
    protected class Controller extends MouseAdapter
            implements MouseMotionListener {

        protected boolean shouldReact( MouseEvent e ) {
            boolean isLeft = SwingUtilities.isLeftMouseButton( e );
            return isLeft;
        }

        @Override
        public void mousePressed( MouseEvent e ) {

            Point p = e.getPoint();
            p = SwingUtilities.convertPoint( e.getComponent(), p, container );
            int tabIndex = container.indexAtLocation( p.x, p.y );

            SingleSelectionModel sel = container.getModel();
            //invoke possible selection change
            if( tabIndex >= 0 && e.getComponent() != container ) {
                CloseableTabComponent tab = ( CloseableTabComponent ) container.getTabComponentAt( tabIndex );
                if( tab.isInCloseButton( e ) ) {
                    return;
                }
                tabIndex = container.indexOf( container.getComponentAt( tabIndex ) );
                boolean change = shouldPerformAction( TabDisplayer.COMMAND_SELECT,
                        tabIndex, e );
                if( change ) {

                    sel.setSelectedIndex( tabIndex );
                    Component tc = container.getDataModel().getTab( tabIndex ).getComponent();
                    if( null != tc && tc instanceof TopComponent && !(( TopComponent ) tc).isAncestorOf( KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner() ) ) {
                        (( TopComponent ) tc).requestActive();
                    }
                }
            }
            if( e.isPopupTrigger() ) {
                //Post a popup menu show request

                shouldPerformAction( TabDisplayer.COMMAND_POPUP_REQUEST, tabIndex, e );
            }
        }

        @Override
        public void mouseClicked( MouseEvent e ) {
            Point p = e.getPoint();
            p = SwingUtilities.convertPoint( e.getComponent(), p, container );
            int i = container.indexAtLocation( p.x, p.y );
            int tabIndex = i;
            if( i >= 0 )
                tabIndex = container.indexOf( container.getComponentAt( i ) );
            if( e.getClickCount() >= 2 && !e.isPopupTrigger() ) {
                SingleSelectionModel sel = container.getModel();
                // invoke possible selection change
                if( i >= 0 ) {
                    boolean change = shouldPerformAction( TabDisplayer.COMMAND_SELECT,
                            tabIndex, e );
                    if( change ) {
                        sel.setSelectedIndex( i );
                    }
                }
                if( i != -1 && e.getButton() == MouseEvent.BUTTON1 ) {
                    //Post a maximize request
                    shouldPerformAction( TabDisplayer.COMMAND_MAXIMIZE, tabIndex, e );
                }
            } else if( e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && i >= 0 ) {
                CloseableTabComponent tab = ( CloseableTabComponent ) container.getTabComponentAt( i );
                if( tab.isInCloseButton( e ) ) {
                    String command = TabbedContainer.COMMAND_CLOSE;
                    if( container.getType() == TabbedType.EDITOR ) {
                        if( (e.getModifiers() & MouseEvent.SHIFT_MASK) > 0 ) {
                            command = TabbedContainer.COMMAND_CLOSE_ALL;
                        } else if( (e.getModifiers() & MouseEvent.ALT_MASK) > 0 ) {
                            command = TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS;
                        }
                    }
                    shouldPerformAction( command, tabIndex, e );
                }
            } else if( e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON2 && i >= 0 ) {
                //close on middle click
                CloseableTabComponent tab = ( CloseableTabComponent ) container.getTabComponentAt( i );
                String command = TabbedContainer.COMMAND_CLOSE;
                shouldPerformAction( command, tabIndex, e );
            }
        }

        @Override
        public void mouseReleased( MouseEvent e ) {
            // close button must not be active when selection change was
            // triggered by mouse press

            Point p = e.getPoint();
            p = SwingUtilities.convertPoint( e.getComponent(), p, container );
            int i = container.indexAtLocation( p.x, p.y );
            if( e.isPopupTrigger() ) {
                if( i >= 0 )
                    i = container.indexOf( container.getComponentAt( i ) );
                //Post a popup menu show request
                shouldPerformAction( TabDisplayer.COMMAND_POPUP_REQUEST, i, e );
            }
        }

        @Override
        public void mouseDragged( MouseEvent e ) {
        }

        @Override
        public void mouseMoved( MouseEvent e ) {
        }

        @Override
        public void mouseEntered( MouseEvent e ) {
            Point p = e.getPoint();
            p = SwingUtilities.convertPoint( e.getComponent(), p, container );
            int i = container.indexAtLocation( p.x, p.y );
            if( i >= 0 ) {
                makeRollover( i );
            }
        }
    } // end of Controller

    /**
     * Allows ActionListeners attached to the container to determine if the
     * event should be acted on. Delegates to
     * <code>container.postActionEvent()</code>. This method will create a
     * TabActionEvent with the passed string as an action command, and cause the
     * container to fire this event. It will return true if no listener on the
     * displayer consumed the TabActionEvent; consuming the event is the way a
     * listener can veto a change, or provide special handling for it.
     *
     * @param command The action command - this should be
     * TabDisplayer.COMMAND_SELECT or TabDisplayer.COMMAND_CLOSE, but private
     * contracts between custom UIs and components are also an option.
     * @param tab The index of the tab upon which the action should act, or -1
     * if non-applicable
     * @param event A mouse event which initiated the action, or null
     * @return true if the event posted was not consumed by any listener
     */
    protected final boolean shouldPerformAction( String command, int tab,
            MouseEvent event ) {
        TabActionEvent evt = new TabActionEvent( container, command, tab, event );
        container.postActionEvent( evt );
        return !evt.isConsumed();
    }

    /**
     * A hack to invoke rollover effect on tab header under mouse cursor when
     * the mouse is within the custom tab header component.
     * @param tabIndex
     */
    private void makeRollover( int tabIndex ) {
        if( !(container.getUI() instanceof BasicTabbedPaneUI) ) {
            return;
        }
        BasicTabbedPaneUI ui = ( BasicTabbedPaneUI ) container.getUI();
        try {
            Method m = container.getUI().getClass().getDeclaredMethod( "setRolloverTab", Integer.TYPE ); //NOI18N
            m.setAccessible( true );
            m.invoke( ui, tabIndex );
        } catch( Exception e ) {
            //ignore
        }
    }
}
