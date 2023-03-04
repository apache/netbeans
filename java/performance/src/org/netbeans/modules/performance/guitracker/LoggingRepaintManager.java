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
package org.netbeans.modules.performance.guitracker;

import java.awt.Component;
import java.awt.Container;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.RepaintManager;

/**
 * A repaint manager which will logs information about interesting events.
 *
 * @author Tim Boudreau, rkubacki@netbeans.org, mmirilovic@netbeans.org
 */
public class LoggingRepaintManager extends RepaintManager {

    private static final long MAX_TIMEOUT = 60 * 1000L;

    private static final boolean DEBUG_MODE = false;

    private static final String OS_NAME = System.getProperty("os.name", "");

    private final ActionTracker tr;

    private RepaintManager orig = null;

    private long lastPaint = 0L;

    private boolean hasDirtyMatches = false;

    private final LinkedList<RegionFilter> regionFilters;

    /**
     * Creates a new instance of LoggingRepaintManager
     *
     * @param tr
     */
    public LoggingRepaintManager(ActionTracker tr) {
        this.tr = tr;
        regionFilters = new LinkedList<RegionFilter>();
        resetRegionFilters();  // filter default button on Vista, Windows 7 and Windows 8 - see issue 100961
        // lastPaint = System.nanoTime();
    }

    /**
     * Enable / disable our Repaint Manager
     *
     * @param val true - enable, false - disable
     */
    public void setEnabled(boolean val) {
        if (isEnabled() != val) {
            if (val) {
                enable();
            } else {
                disable();
            }
        }
    }

    /**
     * Get an answer on question "Is Repaint Manager enabled?"
     *
     * @return true - repaint manager is enabled, false - it's disabled
     */
    public boolean isEnabled() {
        return orig != null;
    }

    /**
     * Enable Repaint Manager
     */
    private void enable() {
        orig = currentManager(new JLabel()); //could be null for standard impl
        setCurrentManager(this);
    }

    /**
     * Disable Repaint Manager
     */
    private void disable() {
        setCurrentManager(orig);
        orig = null;
    }

    /**
     * Log the action when region is add to dirty regions.
     *
     * @param c component which is add to this region
     * @param x point where the region starts
     * @param y point where the region starts
     * @param w width of the region
     * @param h hieght of the region
     */
    @Override
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        synchronized (this) {
            String log = logContainerAndItsParents(c) + ", " + x + "," + y
                    + "," + w + "," + h + ", " + Thread.currentThread().getName();

            // fix for issue 73361, It looks like the biggest cursor is on Sol 10 (11,19) in textfields
            // of some dialogs
            if (w > 11 || h > 19) { // painted region isn't cursor (or painted region is greater than cursor)
                if (regionFilters != null && !acceptedByRegionFilters(c)) {
                    tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "IGNORED DirtyRegion: " + log);
                } else { // no filter || accepted by filter =>  measure it
                    tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "ADD DirtyRegion: " + log);
                    hasDirtyMatches = true;
                }
            }
        }
        super.addDirtyRegion(c, x, y, w, h);
    }
    
    public static String logComponent(Component c) {
        return c.getClass().getName() + "/" + c.getName();
    }

    public static String logContainerAndItsParents(Container c) {
        if (DEBUG_MODE) {
            return logComponent(c) + getContainersChain(c);
        } else {
            return logComponent(c);
        }
    }

    public static String getContainersChain(Container container) {
        StringBuilder ret = new StringBuilder();
        do {
            container = container.getParent();
            if (container == null) {
                break;
            }
            ret.append(" <- ").append(logComponent(container));
        } while (true);
        return ret.toString();
    }

    /**
     * Check all region filters
     *
     * @param c component to be checked
     * @return true - it's accepted, false it isn't accepted
     */
    public synchronized boolean acceptedByRegionFilters(JComponent c) {
        for (RegionFilter filter : regionFilters) {
            if (!filter.accept(c)) // if not accepted it has to be IGNORED
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Set region filter
     *
     * @param filter
     */
    public void addRegionFilter(RegionFilter filter) {
        if (filter != null) {
            tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "FILTER: " + filter.getFilterName());
            regionFilters.add(filter);
        }
    }

    /**
     * Remove region filter
     *
     * @param filter
     */
    public void removeRegionFilter(RegionFilter filter) {
        if (filter != null) {
            tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "REMOVE FILTER: " + filter.getFilterName());
            regionFilters.remove(filter);
        }
    }

    /**
     * Reset region filters
     */
    public void resetRegionFilters() {
        tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "FILTER: reset");
        regionFilters.clear();
    }

    /**
     * Region filter - define paints those will be accepted
     */
    public interface RegionFilter {

        /**
         * Accept paints from component
         *
         * @param c component
         * @return true - paint is accepted, false it isn't
         */
        public boolean accept(JComponent c);

        /**
         * Get filter name
         *
         * @return name of the filter
         */
        public String getFilterName();
    }

    /**
     * Ignores paints from Status Line
     */
    public static final RegionFilter IGNORE_STATUS_LINE_FILTER = new RegionFilter() {

        private JLabel statusLabel;
        private JComponent statusPanel;
        private JComponent statusLayeredPane;

        @Override
        public boolean accept(JComponent c) {
            if (statusLabel == null && c instanceof JLabel && "AutoHideStatusTextLabel".equals(c.getName())) {
                // ignore Label, parent JPanel and parent JLayeredPane (see org.netbeans.core.windows.view.ui.AutoHideStatusText)
                statusLabel = (JLabel) c;
                statusPanel = (JComponent) c.getParent();
                statusLayeredPane = (JComponent) statusPanel.getParent();
            }
            // ignore also org.netbeans.core.windows.view.ui.StatusLine and org.netbeans.modules.editor.impl.StatusLineComponent
            return c != statusLabel && c != statusPanel && c != statusLayeredPane && !c.getClass().getName().contains("StatusLine");
        }

        @Override
        public String getFilterName() {
            return "Ignores StatusLine content";
        }
    };

    /**
     * Ignores paints from ExplorerTree
     */
    public static final RegionFilter IGNORE_EXPLORER_TREE_FILTER = new RegionFilter() {

        @Override
        public boolean accept(JComponent c) {
            String cn = c.getClass().getName();
            if ("org.openide.explorer.view.TreeView$ExplorerTree".equals(cn)) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public String getFilterName() {
            return "Ignores TreeView$ExplorerTree";
        }

    };

    /**
     * Ignores paints from DiffSidebar
     */
    public static final RegionFilter IGNORE_DIFF_SIDEBAR_FILTER = new RegionFilter() {

        @Override
        public boolean accept(JComponent c) {
            String cn = c.getClass().getName();
            if ("org.netbeans.modules.versioning.diff.DiffSidebar".equals(cn)) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public String getFilterName() {
            return "Ignores versioning.diff.DiffSidebar";
        }

    };

    /**
     * Accept paints only from Explorer : - org.openide.explorer.view
     */
    public static final RegionFilter EXPLORER_FILTER = new RegionFilter() {

        @Override
        public boolean accept(JComponent c) {
            for (Class clz = c.getClass(); clz != null; clz = clz.getSuperclass()) {
                if (clz.getPackage().getName().equals("org.openide.explorer.view")) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getFilterName() {
            return "Accept paints from package: org.openide.explorer.view";
        }
    };

    /**
     * Accept paints only from Editor : - org.openide.text.QuietEditorPane
     */
    public static final RegionFilter EDITOR_FILTER = new RegionFilter() {

        @Override
        public boolean accept(JComponent c) {
            return c.getClass().getName().equals("org.openide.text.QuietEditorPane");
        }

        @Override
        public String getFilterName() {
            return "Accept paints from org.openide.text.QuietEditorPane";
        }
    };

    /**
     * Log the action when dirty regions are painted.
     */
    @Override
    public void paintDirtyRegions() {
        super.paintDirtyRegions();
        //System.out.println("Done superpaint ("+tr+","+hasDirtyMatches+").");
        if (tr != null && hasDirtyMatches) {
            lastPaint = System.nanoTime();
            tr.add(ActionTracker.TRACK_PAINT, "PAINTING - done");
            //System.out.println("Done painting - " +tr);
            hasDirtyMatches = false;
        }
    }

    /**
     * waits and returns when there is at least timeout milliseconds without any
     * painting processing
     *
     * @param timeout
     * @return time of last painting
     */
    public long waitNoPaintEvent(long timeout) {
        return waitNoPaintEvent(timeout, false);
    }

    /**
     * waits and returns when there is at least timeout milliseconds without any
     * painting processing.
     *
     * @param afterPaint when set to true then this method checks if there was
     * any paint and measures quiet period from this time
     *
     * @return time of last painting
     */
    private long waitNoPaintEvent(long timeout, boolean afterPaint) {
        long current = System.nanoTime();
        long first = current;
        while ((ActionTracker.nanoToMili(current - lastPaint) < timeout) || ((lastPaint == 0L) && afterPaint)) {
            try {
                Thread.sleep(Math.min(ActionTracker.nanoToMili(current - lastPaint) + 20, timeout));
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            current = System.nanoTime();
            if (ActionTracker.nanoToMili(current - first) > MAX_TIMEOUT) {
                return ActionTracker.nanoToMili(lastPaint);
            }
        }
        return ActionTracker.nanoToMili(lastPaint);
    }

    /**
     * Utility method used from NetBeans to measure startup time. Initializes
     * RepaintManager and associated ActionTracker and than waits until paint
     * happens and there is 5 seconds of inactivity.
     *
     * @return time of last paint
     */
    public static long measureStartup() {
        // load our EQ and repaint manager
        ActionTracker tr = ActionTracker.getInstance();
        LoggingRepaintManager rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);

        tr.startNewEventList("Startup time measurement");

        long waitAfterStartup = Long.getLong("org.netbeans.performance.waitafterstartup", 10000).longValue();
        long time = rm.waitNoPaintEvent(waitAfterStartup, true);

        String fileName = System.getProperty("org.netbeans.log.startup.logfile");
        java.io.File logFile = new java.io.File(fileName.substring(0, fileName.lastIndexOf('.')) + ".xml");

        tr.stopRecording();
        try {
            tr.exportAsXML(new java.io.PrintStream(logFile));
        } catch (Exception exc) {
            System.err.println("Exception rises during writing log from painting of the main window :");
            exc.printStackTrace(System.err);
        }

        rm.setEnabled(false);
        return time;
    }
}
