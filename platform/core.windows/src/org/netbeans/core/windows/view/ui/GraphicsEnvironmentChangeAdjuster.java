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
package org.netbeans.core.windows.view.ui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/* Note to developers: To manually test the behavior of this class without running the entire IDE,
run GraphicsEnvironmentChangeAdjusterTester.java in the test sources. */
/**
 * Logic for automatically applying adjustments to the main window after changes in the graphics
 * environment, such as when connecting or disconnecting an external monitor, or when the HiDPI
 * scaling level is changed.
 *
 * <p>The adjustments fixes bugs observed on Windows 10 and Windows 11, where (1) windows may end up
 * outside the visible screen area after a change in display configuration, or (2) window graphics
 * may appear garbled after a change in HiDPI scaling on any monitor, or a connect/disconnect of an
 * external monitor. On Windows 10 the latter bug was sometimes observed to make the IDE appear
 * completely unresponsive, with the only way to get out of this state being to enable and disable
 * full-screen mode via keyboard shortcut.
 *
 * <p>Methods in this class may be called from any thread.
 *
 * @author Eirik Bakke
 */
final class GraphicsEnvironmentChangeAdjuster {
    private static final Logger LOGGER =
            Logger.getLogger(GraphicsEnvironmentChangeAdjuster.class.getName());

    private static final RequestProcessor CHANGE_DETECTION_RP =
            new RequestProcessor("Detect possible GraphicsEnvironment changes", 1, false, false);
    private static final int CHANGE_DETECTION_COALESCING_DELAY_MS = 1000;
    private static final Task CHANGE_DETECTION_TASK = CHANGE_DETECTION_RP.create(
            GraphicsEnvironmentChangeAdjuster::handlePossibleGraphicsEnvironmentChange);

    private static final RequestProcessor ADJUST_WINDOWS_RP =
            new RequestProcessor("Adjust windows on GraphicsEnvironment change", 1, false, false);
    private static final int ADJUST_WINDOWS_DELAY_MS = 2000;
    private static final Task ADJUST_WINDOWS_TASK = ADJUST_WINDOWS_RP.create(() -> {
        SwingUtilities.invokeLater(() -> performAdjustments());
    });

    private static final Set<JFrame> REGISTERED_WINDOWS =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static Set<GraphicsConfigurationRelevantFields> PREVIOUSLY_OBSERVED_ENVIRONMENT = null;
    private static final Object PREVIOUSLY_OBSERVED_ENVIRONMENT_LOCK = new Object();

    private GraphicsEnvironmentChangeAdjuster() {
    }

    public static void registerWindow(JFrame window) {
        if (window == null) {
            throw new NullPointerException();
        }
        REGISTERED_WINDOWS.add(window);
    }

    public static void unregisterWindow(JFrame window) {
        if (window == null) {
            throw new NullPointerException();
        }
        REGISTERED_WINDOWS.remove(window);
    }

    /**
     * Schedule a check to see if the graphics environment has changed. This method should be cheap
     * to call in the common case where no change has occured.
     */
    public static void notifyPossibleGraphicsEnvironmentChange() {
        if (REGISTERED_WINDOWS.isEmpty()) {
            return;
        }
        CHANGE_DETECTION_TASK.schedule(CHANGE_DETECTION_COALESCING_DELAY_MS);
    }

    private static Set<GraphicsConfigurationRelevantFields> getCurrentEnvironment() {
        Set<GraphicsConfigurationRelevantFields> ret = new LinkedHashSet<>();
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            if (gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                ret.add(new GraphicsConfigurationRelevantFields(gd.getDefaultConfiguration()));
            }
        }
        return Collections.unmodifiableSet(ret);
    }

    private static void handlePossibleGraphicsEnvironmentChange() {
        synchronized (PREVIOUSLY_OBSERVED_ENVIRONMENT_LOCK) {
            Set<GraphicsConfigurationRelevantFields> prevEnv = PREVIOUSLY_OBSERVED_ENVIRONMENT;
            Set<GraphicsConfigurationRelevantFields> curEnv = getCurrentEnvironment();
            PREVIOUSLY_OBSERVED_ENVIRONMENT = curEnv;
            // Don't count the initial call as a change (prevEnv == null case).
            if (prevEnv == null || prevEnv.equals(curEnv)) {
                return;
            }
        }
        ADJUST_WINDOWS_TASK.schedule(ADJUST_WINDOWS_DELAY_MS);
    }

    private static <C extends Component> C getIfDescends(Set<? extends Component> ancestors, C component) {
        if (component == null) {
            return null;
        }
        for (Component ancestorCandidate : ancestors) {
            if (SwingUtilities.isDescendingFrom(component, ancestorCandidate)) {
                return component;
            }
        }
        return null;
    }

    /**
     * Perform window adjustments after a known change in the graphics environment. Must be called
     * on the Event Dispatch Thread only.
     */
    private static void performAdjustments() {
        LOGGER.info("Adjusting window bounds after change in display environment");
        Set<JFrame> registeredWindows = new LinkedHashSet<>(REGISTERED_WINDOWS);
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Window activeWindow = getIfDescends(registeredWindows, kfm.getActiveWindow());
        Component focusOwner = getIfDescends(Collections.singleton(activeWindow), kfm.getFocusOwner());
        MainWindow mainWindow = MainWindow.getInstance();
        JFrame mainWindowFrame = mainWindow.getFrame();
        boolean fullScreen =
                registeredWindows.contains(mainWindowFrame) && mainWindow.isFullScreenMode();
        if (fullScreen) {
            // Always turn full-screen mode off after a change in the graphics environment.
            mainWindow.setFullScreenMode(false);
        }
        for (JFrame window : registeredWindows) {
            performAdjustments(window, fullScreen && window == mainWindowFrame);
        }
        if (focusOwner != null) {
            SwingUtilities.invokeLater(() -> {
                focusOwner.requestFocus();
            });
        }
    }

    /**
     * Perform window adjustments after a known change in the graphics environment. Must be called
     * on the Event Dispatch Thread only.
     */
    private static void performAdjustments(JFrame window, boolean wasFullScreen) {
        boolean wasMaximizedOrFullScreen =
                wasFullScreen || window.getExtendedState() == JFrame.MAXIMIZED_BOTH;
        /* Cycle window visibility to fix a bug observed on Windows (at least on Java 17.0.2 and
        earlier, with HiDPI scaling enabled), where window graphics may become garbled or no longer
        painted after a change in HiDPI scaling or after disconnecting/reconnecting an external
        monitor. */
        window.setVisible(false);
        /* Wrap the next part in an invokeLater, to avoid an exception that was once observed
        when calling setVisible(true) immediately after the setVisible(false):
            java.lang.IndexOutOfBoundsException: Index 3 out of bounds for length 3
              at java.base/jdk.internal.util.Preconditions.outOfBounds(Unknown Source)
              at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Unknown Source)
              at java.base/jdk.internal.util.Preconditions.checkIndex(Unknown Source)
              at java.base/java.util.Objects.checkIndex(Unknown Source)
              at java.base/java.util.ArrayList.get(Unknown Source)
              at java.desktop/java.awt.Container.createHierarchyEvents(Unknown Source)
              at java.desktop/java.awt.Container.createHierarchyEvents(Unknown Source)
              at java.desktop/java.awt.Container.createHierarchyEvents(Unknown Source)
              at java.desktop/java.awt.Container.createHierarchyEvents(Unknown Source)
              at java.desktop/java.awt.Container.createHierarchyEvents(Unknown Source)
              at java.desktop/java.awt.Container.createHierarchyEvents(Unknown Source)
              at java.desktop/java.awt.Container.createHierarchyEvents(Unknown Source)
              at java.desktop/java.awt.Container.createHierarchyEvents(Unknown Source)
              at java.desktop/java.awt.Component.show(Unknown Source)
              at java.desktop/java.awt.Window.show(Unknown Source)
              at java.desktop/java.awt.Component.show(Unknown Source)
              at java.desktop/java.awt.Component.setVisible(Unknown Source)
              at java.desktop/java.awt.Window.setVisible(Unknown Source)
              at org.netbeans.core.windows.view.ui.GraphicsEnvironmentChangeAdjuster.performAdjustments(GraphicsEnvironmentChangeAdjuster.java:184)

        (Update 7 months later: I now got the same trace again, despite the invokeLater
        wrapping.) */
        SwingUtilities.invokeLater(() -> {
            performAdjustmentsContinued(window, wasMaximizedOrFullScreen);
        });
    }

    private static void performAdjustmentsContinued(
            JFrame window, boolean wasMaximizedOrFullScreen)
    {
        window.setVisible(true);
        if (wasMaximizedOrFullScreen) {
            window.setExtendedState(Frame.NORMAL);
        }
        // Wrap in invokeLater in case the window bounds are still updating somehow.
        SwingUtilities.invokeLater(() -> {
            GraphicsConfiguration gc = window.getGraphicsConfiguration();
            if (gc == null) {
                return;
            }
            /* NOTE: On Linux, Utilities.getUsableScreenBounds uses a cache that we might need to
                     invalidate. But currently this code runs only on Windows. */
            Rectangle usableScreenBounds = Utilities.getUsableScreenBounds(gc);
            Rectangle oldWindowBounds = window.getBounds();
            /* Ensure some top/left margin to make it easier to see the difference between the
            maximized and unmaximized state. */
            final int MARGIN = 25;
            Rectangle newWindowBounds = new Rectangle(
                    Math.max(oldWindowBounds.x, usableScreenBounds.x + MARGIN),
                    Math.max(oldWindowBounds.y, usableScreenBounds.y + MARGIN),
                    Math.min(oldWindowBounds.width , usableScreenBounds.width  - MARGIN),
                    Math.min(oldWindowBounds.height, usableScreenBounds.height - MARGIN)
            );
            newWindowBounds.x = Math.min(newWindowBounds.x,
                    usableScreenBounds.x + usableScreenBounds.width  - newWindowBounds.width);
            newWindowBounds.y = Math.min(newWindowBounds.y,
                    usableScreenBounds.y + usableScreenBounds.height - newWindowBounds.height);
            /* Always trigger a change in both size and position, to try to get the window out of
            an occasional buggy state where it will maximize to the wrong size. */
            window.setBounds(newWindowBounds.x + 1, newWindowBounds.y + 1,
                newWindowBounds.width - 1, newWindowBounds.height - 1);
            window.setBounds(newWindowBounds);
            if (wasMaximizedOrFullScreen) {
                Rectangle fullScreenBounds = gc.getBounds();
                float aspectRatio = fullScreenBounds.width / (float) fullScreenBounds.height;
                // Don't auto-maximize on Ultrawide monitors.
                if (aspectRatio <= 2.0) {
                   window.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            }
        });
    }

    private static final class GraphicsConfigurationRelevantFields {
        /**
         * Window bounds in logical coordinates. I.e. a screen of 2560x1440 device pixel resolution
         * at 150% HiDPI scaling will report a bounds with logical width=1707 and height=960.
         */
        final Rectangle bounds;
        /**
         * HiDPI scaling factor.
         */
        final double transformScale;

        public GraphicsConfigurationRelevantFields(GraphicsConfiguration gc) {
            this.bounds = gc.getBounds();
            this.transformScale = gc.getDefaultTransform().getScaleX();
        }

        @Override
        public int hashCode() {
            return Objects.hash(bounds, transformScale);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof GraphicsConfigurationRelevantFields)) {
                return false;
            }
            GraphicsConfigurationRelevantFields other = (GraphicsConfigurationRelevantFields) obj;
            return Objects.equals(this.bounds, other.bounds)
                    && this.transformScale == other.transformScale;
        }

        @Override
        public String toString() {
            return "GCRF(" + bounds + ", " + transformScale + ")";
        }
    }
}
