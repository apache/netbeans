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

package org.netbeans.modules.websvc.design.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.EventObject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import org.netbeans.api.visual.widget.Scene;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Manages the zoom level for a particular Scene instance.
 *
 * @author Nathan Fiedler
 */
public class ZoomManager {
    /** The default zoom percent value. */
    public static final int DEFAULT_ZOOM_PERCENT = 100;
    /** The minimum zoom percent value. */
    public static final int MIN_ZOOM_PERCENT = 33;
    /** The maximum zoom percent value. */
    public static final int MAX_ZOOM_PERCENT = 200;
    /** Point at which the zoom increments/decrements more or less
     * (less below the threshold, more above the threshold). */
    private static final int ZOOM_STEP_THRESHOLD = DEFAULT_ZOOM_PERCENT;
    /** Small zoom increment, when below the threshold. */
    private static final int ZOOM_STEP_SMALL = 5;
    /** Large zoom increment, when above the threshold. */
    private static final int ZOOM_STEP_LARGE = 25;
    /** The scene to zoom in/out. */
    private Scene scene;
    /** The zoom factor in the form of a percentage (e.g. 75%). */
    private int zoomPercentage = DEFAULT_ZOOM_PERCENT;
    /** List of zoom listeners. */
    private EventListenerList listeners;

    /**
     * Creates a new instance of ZoomManager.
     *
     * @param  scene  the scene to be managed.
     */
    public ZoomManager(Scene scene) {
        this.scene = scene;
        listeners = new EventListenerList();
        scene.addSceneListener(new Scene.SceneListener(){
            public void sceneRepaint() {
            }

            public void sceneValidating() {
            }

            public void sceneValidated() {
                int newZoomPercentage = (int)(getScene().getZoomFactor()*100);
                if(newZoomPercentage != zoomPercentage) {
                    zoomPercentage = newZoomPercentage;
                    fireZoomEvent(zoomPercentage);
                }
            }
        });
    }

    /**
     * Adds the given listener to this manager instance. It will be notified
     * when the zoom value is changed.
     *
     * @param  listener  listener to be added.
     */
    public void addZoomListener(ZoomListener listener) {
        listeners.add(ZoomListener.class, listener);
    }

    /**
     * Adds zoom actions to the given toolbar (no separators are added).
     *
     * @param  toolbar  to which the actions are added.
     */
    public void addToolbarActions(JToolBar toolbar) {
        toolbar.add(new FitDiagramAction(this));
        toolbar.add(new FitWidthAction(this));
        toolbar.add(new ZoomDefaultAction(this));
        toolbar.add(new ZoomComboBox(this));
        ZoomInAction inAction = new ZoomInAction(this);
        addZoomListener(inAction);
        toolbar.add(inAction);
        ZoomOutAction outAction = new ZoomOutAction(this);
        addZoomListener(outAction);
        toolbar.add(outAction);
    }

    /**
     * Determine the zoom percentage if the user is zooming in
     * (e.g. from 75 to 80, 100 to 125, etc.).
     *
     * @param  percent  the current percent value.
     * @return  the decreased percent value.
     */
    public static int calculateZoomInValue(int percent) {
        int newZoomValue;
        if (percent >= ZOOM_STEP_THRESHOLD) {
            newZoomValue = ((percent + ZOOM_STEP_LARGE) / ZOOM_STEP_LARGE)
            * ZOOM_STEP_LARGE;
        } else {
            newZoomValue = ((percent + ZOOM_STEP_SMALL) / ZOOM_STEP_SMALL)
            * ZOOM_STEP_SMALL;
        }
        return newZoomValue;
    }

    /**
     * Determine the zoom percentage if the user is zooming out
     * (e.g. from 75 to 70, 150 to 125, etc.).
     *
     * @param  percent  the current percent value.
     * @return  the increased percent value.
     */
    public static int calculateZoomOutValue(int percent) {
        int newZoomValue;
        if (percent > ZOOM_STEP_THRESHOLD) {
            newZoomValue = ((percent - 1) / ZOOM_STEP_LARGE) * ZOOM_STEP_LARGE;
        } else {
            newZoomValue = ((percent - 1) / ZOOM_STEP_SMALL) * ZOOM_STEP_SMALL;
        }
        return newZoomValue;
    }

    /**
     * Fires zoom events to the registered listeners, if any.
     *
     * @param  percent  the new percent value.
     */
    private void fireZoomEvent(int percent) {
        Object[] list = listeners.getListenerList();
        ZoomEvent event = null;
        for (int ii = list.length - 2; ii >= 0; ii -= 2) {
            if (list[ii] == ZoomListener.class) {
                if (event == null) {
                    event = new ZoomEvent(this, percent);
                }
                ((ZoomListener) list[ii + 1]).zoomChanged(event);
            }
        }
    }

    /**
     * Return the Scene for which this manager is controlling the zoom.
     *
     * @return  Scene managed by this manager.
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Return the zoom factor for the Scene mananged by this ZoomManager
     * instance. The value represents a percentage (e.g. 100%) and
     * is always a positive number.
     *
     * @return  current zoom percentage.
     */
    public int getZoom() {
        return zoomPercentage;
    }

    /**
     * Removes the given listener from this manager instance, such that it
     * will no longer receive zoom events.
     *
     * @param  listener  listener to be removed.
     */
    public void removeZoomListener(ZoomListener listener) {
        listeners.remove(ZoomListener.class, listener);
    }

    /**
     * Set the zoom factor for the Scene mananged by this ZoomManager
     * instance. The value represents a percentage (e.g. 100%) and
     * must be a positive number. Any value outside of the defined
     * range (<tt>MIN_ZOOM_PERCENT</tt> and <tt>MAX_ZOOM_PERCENT</tt>)
     * will be forced into that range.
     *
     * @param  percent  the percent value (e.g. 50 for half-size,
     *                  200 for double-size).
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public void setZoom(int percent) {
        JScrollPane pane = (JScrollPane) SwingUtilities.getAncestorOfClass(
                JScrollPane.class, scene.getView());
        assert pane != null : "Scene view component not in a JScrollPane?!?";
        if (pane == null) {
            return;
        }
        JViewport viewport = pane.getViewport();
        Rectangle visRect = viewport.getViewRect();
        Point center = new Point(visRect.x + visRect.width / 2,
                visRect.y + visRect.height / 2);
        setZoom(percent, center);
    }

    /**
     * Set the zoom factor for the Scene mananged by this ZoomManager
     * instance. The value represents a percentage (e.g. 100%) and
     * must be a positive number. Any value outside of the defined
     * range (<tt>MIN_ZOOM_PERCENT</tt> and <tt>MAX_ZOOM_PERCENT</tt>)
     * will be forced into that range.
     *
     * @param  percent  the percent value (e.g. 50 for half-size,
     *                  200 for double-size).
     * @param  center   the point at which to zoom in and keep centered.
     */
    public void setZoom(int percent, Point center) {
        if (percent < MIN_ZOOM_PERCENT) {
            percent = MIN_ZOOM_PERCENT;
        } else if (percent > MAX_ZOOM_PERCENT) {
            percent = MAX_ZOOM_PERCENT;
        }

        // Find the current center point prior to zooming.
        Point sceneCenter = scene.convertViewToScene(center);
        zoomPercentage = percent;
        // Convert the percent value to the zoom factor Scene is expecting
        // (a double that acts as the multiplier to the component sizes and
        // locations, such that 0.5 is 50%, 1.0 is 100%, and 2.0 is 200%.
        double factor = ((double) percent) / 100.0d;
        scene.setZoomFactor(factor);
        // Setting the zoom factor alone is not enough, must force
        // validation and repainting of the scene for it to work.
        scene.validate();
        scene.repaint();

        // Find the new center point and scroll the view after zooming.
        Point newViewCenter = scene.convertSceneToView(sceneCenter);
        JComponent view = scene.getView();
        Rectangle visRect = view.getVisibleRect();
        visRect.x = newViewCenter.x - (center.x - visRect.x);
        visRect.y = newViewCenter.y - (center.y - visRect.y);
        Dimension viewSize = view.getSize();
        if (visRect.x + visRect.width > viewSize.width) {
            visRect.x = viewSize.width - visRect.width;
        }
        if (visRect.y + visRect.height > viewSize.height) {
            visRect.y = viewSize.height - visRect.height;
        }
        if (visRect.x < 0) {
            visRect.x = 0;
        }
        if (visRect.y < 0) {
            visRect.y = 0;
        }
        view.scrollRectToVisible(visRect);
        view.revalidate();
        view.repaint();

        // Notify registered listeners so they may update their state.
        fireZoomEvent(percent);
    }

    /**
     * Manages the combobox for setting the zoom level.
     */
    private static class ZoomComboBox extends JComboBox {
        /** The associated zoom manager. */
        private ZoomManager manager;

        /**
         * Creates a new instance of ZoomComboBox.
         *
         * @param  manager  the zoom manager.
         */
        public ZoomComboBox(ZoomManager manager) {
            super(new Model());
            this.manager = manager;
            // The combo will expand to fill all available space, so
            // instead, give it a prototype value and then ask for the
            // preferred size, making that the maximum size
            // (make it wide enough to accomodate the '%').
            setPrototypeDisplayValue(100000);
            setMaximumSize(getPreferredSize());
            setEditable(true);
            Listener l = new Listener(manager);
            // We can't listen to ourselves, so use a delegate.
            addActionListener(l);
            manager.addZoomListener(l);
        }

        /**
         * Combobox model, provides default zoom values.
         */
        private static class Model extends DefaultComboBoxModel {

            /**
             * Creates a new instance of Model.
             */
            public Model() {
                addElement(new Value(33));
                addElement(new Value(50));
                addElement(new Value(75));
                // We are assuming the default is between 75 and 150...
                Value def = new Value(DEFAULT_ZOOM_PERCENT);
                addElement(def);
                addElement(new Value(150));
                addElement(new Value(200));
                setSelectedItem(def);
            }
        }

        /**
         * Class Value represents a combobox element.
         */
        private static class Value {
            /** The value of the combobox element. */
            private int value;
            /** The String to represent this value. */
            private String str;

            /**
             * Creates a new instance of Value.
             *
             * @param  value  the zoom value (e.g. 75, 100, 150).
             */
            public Value(int value) {
                this.value = value;
                str = value + "%";
            }

            public boolean equals(Object o) {
                if (o instanceof Value) {
                    return value == ((Value) o).getValue();
                }
                return false;
            }

            public int hashCode() {
                return value;
            }

            /**
             * Returns the integer value of this instance.
             *
             * @return  integer value.
             */
            public int getValue() {
                return value;
            }

            public String toString() {
                return str;
            }
        }

        /**
         * Listener to the combobox and zoom manager.
         */
        private class Listener implements ActionListener, ZoomListener {
            /** The associated zoom manager. */
            private ZoomManager manager;

            /**
             * Creates a new instance of Listener.
             *
             * @param  manager  the zoom manager.
             */
            public Listener(ZoomManager manager) {
                this.manager = manager;
            }

            public void actionPerformed(ActionEvent event) {
                Object src = event.getSource();
                String cmd = event.getActionCommand();
                if (src == ZoomComboBox.this &&
                        cmd.equals(ZoomComboBox.this.getActionCommand())) {
                    // Ignore the "edited" action, since the "changed" action
                    // is sent on both accounts (selection or edit).
                    Object item = ZoomComboBox.this.getSelectedItem();
                    Value value = null;
                    if (item instanceof String) {
                        String str = (String) item;
                        if (str.endsWith("%")) {
                            str = str.substring(0, str.length() - 1);
                        }
                        try {
                            int i = Integer.parseInt(str);
                            value = new Value(i);
                        } catch (NumberFormatException nfe) {
                            // ignore and fall through
                        }
                    } else if (item instanceof Value) {
                        value = (Value) item;
                    }
                    if (value == null) {
                        value = new Value(ZoomComboBox.this.manager.getZoom());
                    }
                    manager.setZoom(value.getValue());
                }
            }

            public void zoomChanged(ZoomEvent event) {
                // Set the selected combobox value.
                ZoomComboBox.this.removeActionListener(this);
                ZoomComboBox.this.setSelectedItem(new Value(event.getPercent()));
                ZoomComboBox.this.addActionListener(this);
            }
        }
    }

    /**
     * Event object representing a change in the zoom level of a ZoomManager.
     */
    private static class ZoomEvent extends EventObject {
        /** Percent value of the zoom manager at the time of the event. */
        private int percent;

        /**
         * Creates a new instance of ZoomEvent.
         *
         * @param  src      the source of the event.
         * @param  percent  the new zoom percent value.
         */
        public ZoomEvent(Object src, int percent) {
            super(src);
            this.percent = percent;
        }

        /**
         * Returns the percent value of the zoom manager at the time of
         * the event.
         *
         * @return  percent value.
         */
        public int getPercent() {
            return percent;
        }
    }

    /**
     * The listener interface for receiving zoom events.
     */
    private static interface ZoomListener extends EventListener {

        /**
         * The zoom level of the ZoomManager has changed.
         *
         * @param  event  the zoom event.
         */
        void zoomChanged(ZoomEvent event);
    }

    /**
     * Implements the fit-diagram feature, such that it sets the zoom to
     * show the Scene contents at the largest percentage while still
     * fitting within the available scroll area.
     */
    private static class FitDiagramAction extends AbstractAction {
        /** The associated ZoomManager. */
        private ZoomManager manager;

        /**
         * Creates a new instance of FitDiagramAction.
         *
         * @param  manager  the zoom manager.
         */
        public FitDiagramAction(ZoomManager manager) {
            this.manager = manager;
            String path = NbBundle.getMessage(FitDiagramAction.class,
                    "IMG_FitDiagramAction");
            Icon icon = ImageUtilities.loadIcon(path);
            if (icon != null) {
                putValue(Action.SMALL_ICON, icon);
            }
            String desc = NbBundle.getMessage(FitDiagramAction.class,
                    "LBL_FitDiagramAction");
            putValue(Action.NAME, desc); // for accessibility
            putValue(Action.SHORT_DESCRIPTION, desc);
        }

        public void actionPerformed(ActionEvent e) {
            Scene scene = manager.getScene();
            JScrollPane pane = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class, scene.getView());
            if (pane == null) {
                // Unlikely, but we cannot assume it exists.
                return;
            }
            JViewport viewport = pane.getViewport();
            Rectangle visRect = viewport.getViewRect();
            Rectangle compRect = scene.getPreferredBounds();
            int zoomX = visRect.width * 100 / compRect.width;
            int zoomY = visRect.height * 100 / compRect.height;
            int zoom = Math.min(zoomX, zoomY);
            manager.setZoom(zoom);
        }
    }

    /**
     * Implements the fit-width feature, such that it sets the zoom to
     * show the Scene contents at the largest percentage while still
     * fitting within the width of the available scroll area.
     */
    private static class FitWidthAction extends AbstractAction {
        /** The associated ZoomManager. */
        private ZoomManager manager;

        /**
         * Creates a new instance of FitWidthAction.
         *
         * @param  manager  the zoom manager.
         */
        public FitWidthAction(ZoomManager manager) {
            this.manager = manager;
            String path = NbBundle.getMessage(FitWidthAction.class,
                    "IMG_FitWidthAction");
            Icon icon = ImageUtilities.loadIcon(path);
            if (icon != null) {
                putValue(Action.SMALL_ICON, icon);
            }
            String desc = NbBundle.getMessage(FitWidthAction.class,
                    "LBL_FitWidthAction");
            putValue(Action.NAME, desc); // for accessibility
            putValue(Action.SHORT_DESCRIPTION, desc);
        }

        public void actionPerformed(ActionEvent e) {
            Scene scene = manager.getScene();
            JScrollPane pane = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class, scene.getView());
            if (pane == null) {
                // Unlikely, but we cannot assume it exists.
                return;
            }
            JViewport viewport = pane.getViewport();
            Rectangle visRect = viewport.getViewRect();
            Rectangle compRect = scene.getPreferredBounds();
            int zoom = visRect.width * 100 / compRect.width;
            manager.setZoom(zoom);
        }
    }

    /**
     * Implements the 100% zoom feature, such that it sets the zoom percent
     * to the fixed value of 100 (the default zoom level).
     */
    private static class ZoomDefaultAction extends AbstractAction {
        /** The associated ZoomManager. */
        private ZoomManager manager;

        /**
         * Creates a new instance of ZoomDefaultAction.
         *
         * @param  manager  the zoom manager.
         */
        public ZoomDefaultAction(ZoomManager manager) {
            this.manager = manager;
            String path = NbBundle.getMessage(ZoomDefaultAction.class,
                    "IMG_ZoomDefaultAction");
            Icon icon = ImageUtilities.loadIcon(path);
            if (icon != null) {
                putValue(Action.SMALL_ICON, icon);
            }
            String desc = NbBundle.getMessage(ZoomDefaultAction.class,
                    "LBL_ZoomDefaultAction");
            putValue(Action.NAME, desc); // for accessibility
            putValue(Action.SHORT_DESCRIPTION, desc);
        }

        public void actionPerformed(ActionEvent e) {
            manager.setZoom(ZoomManager.DEFAULT_ZOOM_PERCENT);
        }
    }

    /**
     * Implements the zoom-in feature, such that it sets the zoom percent
     * to a decreased amount for the scene.
     */
    private static class ZoomInAction extends AbstractAction implements ZoomListener {
        /** The associated ZoomManager. */
        private ZoomManager manager;

        /**
         * Creates a new instance of ZoomInAction.
         *
         * @param  manager  the zoom manager.
         */
        public ZoomInAction(ZoomManager manager) {
            this.manager = manager;
            String path = NbBundle.getMessage(ZoomInAction.class,
                    "IMG_ZoomInAction");
            Icon icon = ImageUtilities.loadIcon(path);
            if (icon != null) {
                putValue(Action.SMALL_ICON, icon);
            }
            String desc = NbBundle.getMessage(ZoomInAction.class,
                    "LBL_ZoomInAction");
            putValue(Action.NAME, desc); // for accessibility
            putValue(Action.SHORT_DESCRIPTION, desc);
        }

        public void actionPerformed(ActionEvent e) {
            int percent = manager.getZoom();
            percent = ZoomManager.calculateZoomInValue(percent);
            manager.setZoom(percent);
        }

        public void zoomChanged(ZoomEvent event) {
            boolean enable = event.getPercent() < MAX_ZOOM_PERCENT;
            setEnabled(enable);
        }
    }

    /**
     * Implements the zoom-out feature, such that it sets the zoom percent
     * to an increased amount for the scene.
     */
    private static class ZoomOutAction extends AbstractAction implements ZoomListener {
        /** The associated ZoomManager. */
        private ZoomManager manager;

        /**
         * Creates a new instance of ZoomOutAction.
         *
         * @param  manager  the zoom manager.
         */
        public ZoomOutAction(ZoomManager manager) {
            this.manager = manager;
            String path = NbBundle.getMessage(ZoomOutAction.class,
                    "IMG_ZoomOutAction");
            Icon icon = ImageUtilities.loadIcon(path);
            if (icon != null) {
                putValue(Action.SMALL_ICON, icon);
            }
            String desc = NbBundle.getMessage(ZoomOutAction.class,
                    "LBL_ZoomOutAction");
            putValue(Action.NAME, desc); // for accessibility
            putValue(Action.SHORT_DESCRIPTION, desc);
        }

        public void actionPerformed(ActionEvent e) {
            int percent = manager.getZoom();
            percent = ZoomManager.calculateZoomOutValue(percent);
            manager.setZoom(percent);
        }

        public void zoomChanged(ZoomEvent event) {
            boolean enable = event.getPercent() > MIN_ZOOM_PERCENT;
            setEnabled(enable);
        }
    }
}
