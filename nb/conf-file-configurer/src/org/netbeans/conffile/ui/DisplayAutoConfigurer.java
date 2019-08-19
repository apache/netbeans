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
package org.netbeans.conffile.ui;

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Figures out a base font size for the UI, erring on the side of large but
 * readable rather than small but unreadable.
 *
 * @author Tim Boudreau
 */
public class DisplayAutoConfigurer {

    // Monitor diagonals for determining a font size based
    // on monitor size
    private static final int TRADITIONAL_MONITOR_480 = 640 * 480;
    private static final int TRADITIONAL_MONITOR_600 = 800 * 600;
    private static final int TRADITIONAL_MONITOR_768 = 1024 * 768;
    private static final int TRADITIONAL_MONITOR_1024 = 1280 * 1024;
    private static final int TRADITIONAL_MONITOR_1200 = 1600 * 1200;
    private static final int LAPTOP_RETINA = 2560 * 1600;
    private static final int HI_DEF_MONITOR_720 = 1280 * 720;
    private static final int HI_DEF_MONITOR_900 = 1600 * 900;
    private static final int HI_DEF_MONITOR_FHD = 1920 * 1080;
    private static final int HI_DEF_MONITOR_QHD = 2560 * 1440;
    private static final int HI_DEF_MONITOR_4K = 3840 * 2160;
    private static final int HI_DEF_MONITOR_4K_ULTRAWIDE = 3440 * 1440;
    private static final int HI_DEF_MONITOR_8K = 7680 * 4320;

    public static boolean isCrtAspectRatio(DisplayMode m) {
        int px = m.getWidth() * m.getHeight();
        switch (px) {
            case TRADITIONAL_MONITOR_1200:
            case TRADITIONAL_MONITOR_1024:
            case TRADITIONAL_MONITOR_768:
            case TRADITIONAL_MONITOR_600:
            case TRADITIONAL_MONITOR_480:
                return true;
        }
        double w = m.getWidth();
        double h = m.getHeight();
        double fraction = w / h;

        double sixteenByNine = 16D / 9D;
        double fourByThree = 4D / 3D;
        double fiveByFour = 5D / 4D;

        double[] distances = new double[]{
            Math.abs(sixteenByNine - fraction),
            Math.abs(fourByThree - fraction),
            Math.abs(fiveByFour - fraction)
        };
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] < min) {
                min = distances[i];
                minIndex = i;
            }
        }
        switch (minIndex) {
            case 0:
                return false;
            case 1:
            case 2:
                return true;
            default:
                return true;
        }
    }

    static GraphicsConfiguration getGraphicsConfigurationById(String id) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice dev : env.getScreenDevices()) {
            if (id.equals(dev.getIDstring())) {
                return dev.getDefaultConfiguration();
            }
        }
        return null;
    }

    public static DisplayMode displayModeNonGui(boolean largest) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (env.isHeadlessInstance()) {
            return new DisplayMode(800, 600, 16, 30);
        }
        GraphicsDevice device = env.getDefaultScreenDevice();
        if (largest) {
            return findLargestDisplayMode(device.getDisplayModes());
        } else {
            return device.getDisplayMode();
        }
    }

    private static boolean sameResolution(DisplayMode a, DisplayMode b) {
        return a.getWidth() == b.getWidth() && a.getHeight() == b.getHeight();
    }

    private static boolean greaterResolution(DisplayMode test, DisplayMode than) {
        return test.getWidth() * test.getHeight() > than.getWidth() * than.getHeight();
    }

    public static boolean LargerGraphicsModeExists(Component componentOnScreen) {
        GraphicsConfiguration dev = componentOnScreen.getGraphicsConfiguration();
        if (dev == null) {
            return false;
        }

        DisplayMode current = dev.getDevice().getDisplayMode();
        DisplayMode largest = largestDisplayModeAnyMonitor();

        return !sameResolution(current, largest) && greaterResolution(largest, current);
    }

    private static DisplayMode largestDisplayModeAnyMonitor() {
        Set<DisplayMode> modes = new HashSet<>();
        for (GraphicsDevice dev : getScreenDevices()) {
            modes.addAll(Arrays.asList(dev.getDisplayModes()));
        }
        DisplayMode largest = findLargestDisplayMode(modes.toArray(new DisplayMode[modes.size()]));
        return largest;
    }

    public static void appendSig(StringBuilder sb) {
        for (GraphicsDevice gd : getScreenDevices()) {
            sb.append(gd.getIDstring());
            for (DisplayMode dm : gd.getDisplayModes()) {
                sb.append(dm.getWidth()).append('x')
                        .append(dm.getHeight()).append('-');
            }
        }
    }

    private static DisplayMode findLargestDisplayMode(DisplayMode[] mode) {
        if (mode.length == 0) {
            return null;
        }
        Arrays.sort(mode, new DisplayModeComparator());
        return mode[0];
    }

    private static final class DisplayModeComparator implements Comparator<DisplayMode> {

        @Override
        public int compare(DisplayMode a, DisplayMode b) {
            return Integer.compare(b.getWidth() * b.getHeight(), a.getWidth() * a.getHeight());
        }
    }

    private static List<GraphicsDevice> getScreenDevices() {
        List<GraphicsDevice> devices = new ArrayList<>();
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            if (device.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                devices.add(device);
            }
        }
        return devices;
    }

    static GraphicsDevice getLargestGraphicsDevice() {
        List<GraphicsDevice> devices = getScreenDevices();
        Collections.sort(devices, new GraphicsDeviceComparator());
        return devices.isEmpty() ? null : devices.get(0);
    }

    public static boolean isMultipleScreens() {
        return getScreenDevices().size() > 1;
    }

    public static int adjustFontSizeForScreenSize(int initialTarget) {
        if (GraphicsEnvironment.isHeadless()) { // possible in tests
            return initialTarget;
        }
        GraphicsDevice largest = getLargestGraphicsDevice();
        DisplayMode dm = largest.getDisplayMode();
        return adjustFontSizeForScreenSize(initialTarget, largest, false, dm.getWidth() > 3000);
    }

    public static int adjustFontSizeForScreenSize(int initialTarget, GraphicsDevice device,
            boolean useLargest, boolean physicallyLargeMonitor) {

        DisplayMode currentDisplayMode = device.getDisplayMode();
        DisplayMode largestDisplayMode = findLargestDisplayMode(device.getDisplayModes());

        if (largestDisplayMode == null) {
            largestDisplayMode = device.getDisplayMode();
        }

        if (useLargest && largestDisplayMode != null) {
            currentDisplayMode = largestDisplayMode;
        }
        int totalPixels = currentDisplayMode.getWidth() * currentDisplayMode.getHeight();
        return baseFontSizeByPixelCount(totalPixels, initialTarget, physicallyLargeMonitor);
    }

    private static final class GraphicsDeviceComparator implements Comparator<GraphicsDevice> {

        @Override
        public int compare(GraphicsDevice a, GraphicsDevice b) {
            DisplayMode dm = a.getDisplayMode();
            int awh = dm.getWidth() * dm.getHeight();
            int bwh = dm.getWidth() * dm.getHeight();
            return -Integer.compare(awh, bwh);
        }
    }

    /**
     * Find base values based on the diagonal pixel count of the screen.
     *
     * @param diagonal The reported width * height of the screen
     * @param defaultBaseSize The default base font size
     * @return
     */
    static int baseFontSizeByPixelCount(int diagonal, int defaultBaseSize, boolean physicallyLargeMonitor) {
        switch (diagonal) {
            case HI_DEF_MONITOR_8K:
                return physicallyLargeMonitor ? 28 : 36;
            case HI_DEF_MONITOR_4K:
                return physicallyLargeMonitor ? 22 : 28;
            case HI_DEF_MONITOR_4K_ULTRAWIDE:
                return physicallyLargeMonitor ? 18 : 22;
            case HI_DEF_MONITOR_QHD:
                return physicallyLargeMonitor ? 16 : 20;
            case HI_DEF_MONITOR_FHD:
                return physicallyLargeMonitor ? 14 : 18;
            case HI_DEF_MONITOR_900:
                return physicallyLargeMonitor ? 13 : 14;
            case LAPTOP_RETINA:
                return physicallyLargeMonitor ? 14 : 15;
            case HI_DEF_MONITOR_720:
                return physicallyLargeMonitor ? 11 : 13;
            case TRADITIONAL_MONITOR_1200:
                return physicallyLargeMonitor ? 12 : 15;
            case TRADITIONAL_MONITOR_1024:
                return physicallyLargeMonitor ? 11 : 13;
            case TRADITIONAL_MONITOR_768:
                return physicallyLargeMonitor ? 10 : 12;
            case TRADITIONAL_MONITOR_600:
                return physicallyLargeMonitor ? 9 : 11;
            case TRADITIONAL_MONITOR_480:
                return physicallyLargeMonitor ? 8 : 10;
            default:
                // If we did not get handed an exact match, find the
                // nearest.  Simply setting your window manager to have
                // screen margins will result in an inexact match.
                int[] allResolutions = new int[]{
                    TRADITIONAL_MONITOR_1200, TRADITIONAL_MONITOR_1024,
                    TRADITIONAL_MONITOR_768,
                    TRADITIONAL_MONITOR_600, TRADITIONAL_MONITOR_480,
                    HI_DEF_MONITOR_8K, HI_DEF_MONITOR_4K, HI_DEF_MONITOR_QHD,
                    HI_DEF_MONITOR_FHD, HI_DEF_MONITOR_900, HI_DEF_MONITOR_720
                };
                Arrays.sort(allResolutions);
                int direction = 0;
                // Find the nearest and recursively call this method, this time
                // with a constant that will be caught by ths switch above.
                for (int i = 0; i < allResolutions.length; i++) {
                    int currentResolution = allResolutions[i];
                    int currentDirection = Integer.compare(diagonal, currentResolution);
                    if (direction == 0) {
                        direction = currentDirection;
                    } else if (currentDirection != direction) {
                        return baseFontSizeByPixelCount(allResolutions[i],
                                defaultBaseSize, physicallyLargeMonitor);
                    }
                }
        }
        return defaultBaseSize;
    }
}
