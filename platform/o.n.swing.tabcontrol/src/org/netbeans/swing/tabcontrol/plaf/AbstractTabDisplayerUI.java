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
/*
 * AbstractTabDisplayerUI.java
 *
 * Created on March 16, 2004, 6:16 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabDisplayerUI;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Base class for the implementations of TabDisplayerUI in this package. Uses
 * TabLayoutModel for managing the layout of the tabs.  Defines an SPI for 
 * UI delegates for TabDisplayer.
 * <p>
 * For most use cases, it will make more sense to subclass BasicTabDisplayerUI or
 * BasicScrollingTabDisplayerUI, which handle most of the logic any implementation
 * will need.
 *
 *
 * @see BasicTabDisplayerUI
 * @see BasicScrollingTabDisplayerUI
 * 
 * @author Tim Boudreau
 */
public abstract class AbstractTabDisplayerUI extends TabDisplayerUI {
    /**
     * Layout model, which will be initialized in <code>installUI()</code> by calling
     * <code>createLayoutModel</code>.  The layout model provides tab coordinates.
     */
    protected TabLayoutModel layoutModel = null;
    /**
     * Mouse listener (which may optionally implement MouseWheelListener and MouseMotionListener),
     * which handles mouse events over the tab, triggering selection model changes, repaints, etc.
     */
    protected MouseListener mouseListener = null;
    /**
     * Component listener - mainly used to detach listeners when the component is hidden,
     * and reattach them when it is shown.  Also used by BasicScrollingTabDisplayerUI to
     * trigger re-layouts of scrolled tabs when the size of the component changes.
     */
    protected ComponentListener componentListener = null;
    /**
     * A property change listener to listen on any changes from the component which should
     * trigger repainting or other operations.  The default implementation simply listens for
     * changes in the <code>active</code> property to trigger a repaint.
     */
    protected PropertyChangeListener propertyChangeListener = null;
    /**
     * Listener on the TabDataModel.  Responsible for repainting on model changes.  Note that
     * DefaultTabSelectionModel also listens on the model and automatically updates the
     * selected index if, say, tabs are inserted before the currently selected tab.
     */
    protected ModelListener modelListener = null;
    /**
     * A change listener which listens on the selection model and repaints as needed when
     * the selection changes.
     */
    protected ChangeListener selectionListener = null;
    
    protected HierarchyListener hierarchyListener = null;

    /**
     * Creates a new instance of AbstractTabDisplayerUI
     */
    public AbstractTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    /** installUI is final to ensure listeners, etc. are created and attached.  Subclasses that
     * need to perform initialization on install should override <code>install()</code>,
     * and refer to the <code>displayer</code> instance field, which will be initialized here.
     *
     * @param c An instance of TabDisplayer
     */
    public final void installUI(JComponent c) {
        assert c == displayer;
        super.installUI(c); //installs the selection model
        ToolTipManager.sharedInstance().registerComponent(displayer);
        layoutModel = createLayoutModel();
        mouseListener = createMouseListener();
        componentListener = createComponentListener();
        modelListener = createModelListener();
        propertyChangeListener = createPropertyChangeListener();
        selectionListener = createSelectionListener();
        hierarchyListener = createHierarchyListener();
        install();
        installListeners();
        displayer.setFont(createFont());
    }

    /** This method is final - subclasses that need to deinitialize should override
     * <code>uninstall()</code>, and remove any listeners, null any unneeded references, etc.
     *
     * @param c
     */
    public final void uninstallUI(JComponent c) {
        assert c == displayer;
        ToolTipManager.sharedInstance().unregisterComponent(displayer);
        super.uninstallUI(c);
        // #47644 - first uninstallListeners, then uninstall, so that tabState can't be null while
        // listeners are active. Only probable fix, but certainly right thing to do.
        uninstallListeners();
        uninstall();
        layoutModel = null;
        mouseListener = null;
        selectionModel = null;
        componentListener = null;
        selectionListener = null;
    }

    /**
     * Called after creating the layout model, selection model and mouse
     * listener, but before installing the mouse listener and selection model.
     * Subclasses may use this method to do anything they need to do at ui
     * install time.
     */
    protected void install() {
        //do nothing
    }

    /**
     * Called after uninstalling the mouse listener and selection model, but
     * before references to that or the layout model or displayer have been
     * nulled.  Subclasses may use this method to do any cleanup they need to do
     * at uninstall time.
     */
    protected void uninstall() {
        //do nothing
    }

    /**
     * Installs the mouse listener returned by createMouseListener into the
     * control.  If the mouse listener implements MouseMotionListener or
     * MouseWheelListener, it will be installed as such as well.
     */
    protected final void installListeners() {
        displayer.addHierarchyListener (hierarchyListener);
        displayer.addPropertyChangeListener(propertyChangeListener);
        if (componentListener != null) {
            displayer.addComponentListener(componentListener);
        }
        displayer.getModel().addComplexListDataListener(modelListener);
        displayer.getModel().addChangeListener(modelListener);
        if (mouseListener != null) {
            displayer.addMouseListener(mouseListener);
            if (mouseListener instanceof MouseMotionListener) {
                displayer.addMouseMotionListener(
                        (MouseMotionListener) mouseListener);
            }
            if (mouseListener instanceof MouseWheelListener) {
                displayer.addMouseWheelListener((MouseWheelListener) mouseListener);
            }
        }
        selectionModel.addChangeListener(selectionListener);
    }

    /**
     * Installs the mouse listener returned by createMouseListener into the
     * control.  If the mouse listener implements MouseMotionListener or
     * MouseWheelListener, it will be removed as such as well.
     */
    protected final void uninstallListeners() {
        if (mouseListener instanceof MouseMotionListener) {
            displayer.removeMouseMotionListener(
                    (MouseMotionListener) mouseListener);
        }
        if (mouseListener instanceof MouseWheelListener) {
            displayer.removeMouseWheelListener(
                    (MouseWheelListener) mouseListener);
        }
        if (mouseListener != null) {
            displayer.removeMouseListener(mouseListener);
        }
        if (componentListener != null) {
            displayer.removeComponentListener(componentListener);
        }
        displayer.getModel().removeComplexListDataListener(modelListener);
        displayer.getModel().removeChangeListener(modelListener);
        displayer.removePropertyChangeListener(propertyChangeListener);
        displayer.removeHierarchyListener(hierarchyListener);
        selectionModel.removeChangeListener(selectionListener);
        mouseListener = null;
        componentListener = null;
        propertyChangeListener = null;
        selectionListener = null;
        modelListener = null;
        hierarchyListener = null;
    }
    
    protected HierarchyListener createHierarchyListener() {
        return new DisplayerHierarchyListener();
    }

    /**
     * Create an instance of TabLayoutModel which will provide coordinates for
     * tabs
     */
    protected abstract TabLayoutModel createLayoutModel(); //XXX move this to BasicTabDisplayerUI

    /**
     * Create the mouse listener that will be responsible for changing the
     * selection on mouse events, triggering repaints on mouse enter/exit/motion, etc.
     * The installation code will detect if the resulting listener also implements
     * MouseWheelListener or MouseMotionListener, and if so, will add it as such.
     *
     * @return A mouse listener, which may also implement MouseMotionListener and/or
     * MouseWheelListener
     */
    protected abstract MouseListener createMouseListener();

    /**
     * Create a <code>ChangeListener</code> to be attached to the selection model.  This
     * listener will be responsible for repainting the appropriate areas on selection changes.
     *
     * @return A changeListener that will be notified of selection changes
     */
    protected abstract ChangeListener createSelectionListener();

    protected Font createFont() {
        return UIManager.getFont("controlFont"); //NOI18N
    }

    /**
     * Create a listener on the data model that triggers repaints on appropriate
     * changes.
     */
    protected ModelListener createModelListener() {
        return new ModelListener();
    }

    /**
     * Create a ComponentListener that may be needed to handle resize, show,
     * hide, etc.  Returns null by default.
     */
    protected ComponentListener createComponentListener() {
        return null;
    }

    /**
     * Create a PropertyChangeListener which listens on any interesting
     * properties of the control
     */
    protected PropertyChangeListener createPropertyChangeListener() {
        return new DisplayerPropertyChangeListener();
    }

    /** Creates an instance of <code>DefaultTabSelectionModel</code> */
    protected SingleSelectionModel createSelectionModel() {
        return new DefaultTabSelectionModel(displayer.getModel());
    }

    /**
     * A very basic implementation of dropIndexOfPoint, which simply iterates
     * all of the tab rectangles to see if they contain the point.  It is
     * preferred to override this and provide a more efficient implementation
     * unless the UI is not designed to display more than a few tabs.
     *
     * @param p A point
     * @return The index, or -1 if none
     */
    public int dropIndexOfPoint(Point p) {
        Point p2 = toDropPoint(p);
        int max = displayer.getModel().size();
        for (int i=0; i < max; i++) {
            Rectangle r = getTabRect (i, null);
            if (r.contains(p2)) {
                return i;
            }
        }
        return -1;
    }

    /** Convenience method called by ModelListener.stateChanged() when the data model changes.
     * Eliminates the need for custom subclasses where the only purpose is to discard some
     * small amount of cached data.  The default implementation simply calls <code>displayer.repaint()</code>.
     */
    protected void modelChanged() {
        displayer.repaint();
    }

    private Point scratchPoint = new Point();
    /** Converts a point into a point in the coordinate space of the tabs area, for
     * determining what index a drop operation should affect.
     *
     * @param location A point in the coordinate space of the container
     * @return A point in the coordinate space of the tab display area
     */
    protected Point toDropPoint (Point location) {
        //Construct a point within the tabs area retaining the relevant coordinate that
        //will allow it to work
        if (displayer.getWidth() > displayer.getHeight()) {
            //horizontal tabs area
            scratchPoint.setLocation(location.x, (displayer.getHeight() / 2));
        } else {
            //vertical tabs area
            scratchPoint.setLocation (displayer.getWidth() / 2, location.y);
        }
        return scratchPoint;
    }

    /** Does nothing, no shortcuts */
    public void unregisterShortcuts(JComponent comp) {
        // no operation
    }
    
    /** Does nothing, no shortcuts */
    public void registerShortcuts(JComponent comp) {
        // no operation
    }
    
    /**
     * A property change listener which will repaint the selected tab when the
     * &quot;active&quot; property changes on the tab displayer
     */
    protected class DisplayerPropertyChangeListener
            implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            if (displayer.isShowing()
                    && TabDisplayer.PROP_ACTIVE.equals(e.getPropertyName())) {
                activationChanged();
            }
        }

        /**
         * Called if PROP_ACTIVE on the displayer changes
         */
        protected void activationChanged() {
            int i = selectionModel.getSelectedIndex();
            if (i != -1) {
                Rectangle r = new Rectangle();
                getTabRect(i, r);
                if (r.width != 0 && r.height != 0) {
                    displayer.repaint(r.x, r.y, r.width, r.height);
                }
            }
        }
    }

    /**
     * A hierarchy listener which registers the component with ToolTipManager
     * when displayed, and de-registers it when hidden
     */
    protected class DisplayerHierarchyListener implements HierarchyListener {
        public DisplayerHierarchyListener() {
            
        }
        
        public void hierarchyChanged(HierarchyEvent e) {
            if (e.getChanged() == displayer && (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (displayer.isShowing()) {
                    ToolTipManager.sharedInstance().registerComponent(displayer);
                } else {
                    ToolTipManager.sharedInstance().unregisterComponent(displayer);
                }
            }
        }
    }

    /**
     * Simple implementation of a listener on a TabDataModel.  The default implementation
     * simply does a full repaint of the tabs area on a ChangeEvent.  More optimized
     * implementations are possible by handling ListDataEvents.
     */
    protected class ModelListener implements ComplexListDataListener,
            ChangeListener {

        private boolean checkVisible = false;
        /**
         * No-op implementation
         */
        public void contentsChanged(ListDataEvent e) {
            //do nothing
        }

        /**
         * No-op implementation
         */
        public void indicesAdded(ComplexListDataEvent e) {
            //do nothing
        }

        /**
         * No-op implementation
         */
        public void indicesChanged(ComplexListDataEvent e) {
            //do nothing
        }

        /**
         * No-op implementation
         */
        public void indicesRemoved(ComplexListDataEvent e) {
            //do nothing
        }

        /**
         * No-op implementation
         */
        public void intervalAdded(ListDataEvent e) {
            //do nothing
        }

        /**
         * No-op implementation
         */
        public void intervalRemoved(ListDataEvent e) {
            //do nothing
        }

        /**
         * Called whenever any change happens in the data model (one of the above methods will also
         * be called with specific data about the change).  This method is final, and simply calls
         * <code>modelChanged()</code>.  To discard some cached data when the model changes, simply
         * override that.
         */
        public final void stateChanged(ChangeEvent e) {
            modelChanged();
        }
    }

}
