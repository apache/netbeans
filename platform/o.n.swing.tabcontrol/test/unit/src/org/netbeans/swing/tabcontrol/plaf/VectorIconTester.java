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
package org.netbeans.swing.tabcontrol.plaf;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabDisplayerUI;
import org.openide.awt.GraphicsUtils;
import org.openide.util.ImageUtilities;

/**
 * Utility for previewing custom-painted vector icons, at various resolutions and comparing
 * side-by-side with existing LAFs. When implementing new vector icons, invoke this utility using
 * the "Debug" command in NetBeans, then invoke "Apply Code Changes" to have your latest changes
 * immediately be reflected on the screen in this utility.
 *
 * <p>Each displayed column corresponds to one LAF or ComponentUI implementation, as specified in
 * the getIcons method below. The last column displays either the first or the second LAF in the
 * list, toggleable with the Shift key, to facilitate comparison and visual alignment. Pressing
 * Space, or holding down Shift, will will enable a mode where the last column quickly toggles back
 * and forth between the first two LAFs for comparison. A copy of the output with also be written to
 * a PNG file in the temporary directory when the utility is first launched.
 *
 * <p>This utility is currently configured to show icons related to the NetBeans tabcontrol widget.
 * It can be copied to other modules during development and reconfigured to show other icons, by
 * modifying the {@code getIcons()} method.
 *
 * @author Eirik Bakke
 */
public class VectorIconTester extends javax.swing.JFrame {
    private static final boolean TEST_AQUA = true;
    private static final boolean TEST_WIN8 = true;
    private final JScrollPane scrollPane;
    private final IconPreviewPane iconPreviewPane;

    public VectorIconTester() {
        iconPreviewPane = new IconPreviewPane(getIcons());
        scrollPane = new JScrollPane(iconPreviewPane);
        initComponents();
    }

    public void dumpGraphicsToFile() {
        final BufferedImage bi = new BufferedImage(
                iconPreviewPane.getSize().width, iconPreviewPane.getSize().height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        try {
            iconPreviewPane.paintComponent(g);
        } finally {
            g.dispose();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File tempFile = Files.createTempFile("VectorIconTester", ".png").toFile();
                    ImageIO.write(bi, "PNG", tempFile);
                    System.out.println("Output was written to " + tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initComponents() {
        setTitle("Vector Icon Tester");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        setSize(800, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private static Map<String, Icon> getIcons() {
        Map<String, Icon> ret = new LinkedHashMap<String, Icon>();
        if (TEST_AQUA) {
            addTabDisplayerIcons(ret, "mac", (TabDisplayerUI) AquaViewTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "mac", (TabDisplayerUI) AquaEditorTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "macvec", (TabDisplayerUI) AquaVectorViewTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "macvec", (TabDisplayerUI) AquaVectorEditorTabDisplayerUI.createUI(new TabDisplayer()));
        }
        if (TEST_WIN8) {
            addTabDisplayerIcons(ret, "win8", (TabDisplayerUI) Windows8ViewTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "win8", (TabDisplayerUI) Windows8EditorTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "win8vec", (TabDisplayerUI) Windows8VectorViewTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "win8vec", (TabDisplayerUI) Windows8VectorEditorTabDisplayerUI.createUI(new TabDisplayer()));
        }

        if (false) {
            addTabDisplayerIcons(ret, "gtk", (TabDisplayerUI) GtkViewTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "gtk", (TabDisplayerUI) GtkEditorTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "nimbus", (TabDisplayerUI) NimbusViewTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "nimbus", (TabDisplayerUI) NimbusEditorTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "metal", (TabDisplayerUI) MetalViewTabDisplayerUI.createUI(new TabDisplayer()));
            addTabDisplayerIcons(ret, "metal", (TabDisplayerUI) MetalEditorTabDisplayerUI.createUI(new TabDisplayer()));
        }
        if (false) {
            // Icons that are not specialized by LAF.
            ret.put("gen_arrow", ImageUtilities.loadImageIcon("org/openide/awt/resources/arrow.png", false));
            ret.put("gen_busy_icon", ImageUtilities.loadImageIcon("org/netbeans/swing/tabcontrol/resources/busy_icon.png", false));
            ret.put("gen_toolbar_arrow_horizontal", ImageUtilities.loadImageIcon("org/openide/awt/resources/toolbar_arrow_horizontal.png", false));
            ret.put("gen_toolbar_arrow_vertical", ImageUtilities.loadImageIcon("org/openide/awt/resources/toolbar_arrow_vertical.png", false));
            /* These are actually private classes in the openide.awt module. They must be copied in here if
            they are to be shown with the utility. */
            /*
            ret.put("genvec_arrow", ArrowIcon.INSTANCE_DEFAULT);
            ret.put("genvec_toolbar_arrow_horizontal", ToolbarArrowIcon.INSTANCE_HORIZONTAL);
            ret.put("genvec_toolbar_arrow_vertical", ToolbarArrowIcon.INSTANCE_VERTICAL);
             */
        }
        return Collections.unmodifiableMap(ret);
    }

    /**
     * Utility method to add icons specific to the tabcontrol LAFs. Irrelevant when testing icons in
     * other modules.
     */
    private static void addTabDisplayerIcons(
            Map<String, Icon> toMap, String prefix, TabDisplayerUI tabDisplayerUI)
    {
        Map<String, Integer> buttonIDs = new LinkedHashMap<String, Integer>();
        // ViewTabDisplayerUI
        buttonIDs.put("close", TabControlButton.ID_CLOSE_BUTTON);
        buttonIDs.put("slide_right", TabControlButton.ID_SLIDE_RIGHT_BUTTON);
        buttonIDs.put("slide_left", TabControlButton.ID_SLIDE_LEFT_BUTTON);
        buttonIDs.put("slide_down", TabControlButton.ID_SLIDE_DOWN_BUTTON);
        buttonIDs.put("pin", TabControlButton.ID_PIN_BUTTON);
        buttonIDs.put("restore_group", TabControlButton.ID_RESTORE_GROUP_BUTTON);
        buttonIDs.put("slide_group", TabControlButton.ID_SLIDE_GROUP_BUTTON);
        // EditorTabDisplayerUI
        buttonIDs.put("scroll_left", TabControlButton.ID_SCROLL_LEFT_BUTTON);
        buttonIDs.put("scroll_right", TabControlButton.ID_SCROLL_RIGHT_BUTTON);
        buttonIDs.put("drop_down", TabControlButton.ID_DROP_DOWN_BUTTON);
        buttonIDs.put("maximize", TabControlButton.ID_MAXIMIZE_BUTTON);
        buttonIDs.put("restore", TabControlButton.ID_RESTORE_BUTTON);

        Map<String, Integer> buttonStates = new LinkedHashMap<String, Integer>();
        buttonStates.put("default", TabControlButton.STATE_DEFAULT);
        buttonStates.put("pressed", TabControlButton.STATE_PRESSED);
        buttonStates.put("disabled", TabControlButton.STATE_DISABLED);
        buttonStates.put("rollover", TabControlButton.STATE_ROLLOVER);
        for (Entry<String, Integer> buttonID : buttonIDs.entrySet()) {
            for (Entry<String, Integer> buttonState : buttonStates.entrySet()) {
                Icon icon = tabDisplayerUI.getButtonIcon(buttonID.getValue(), buttonState.getValue());
                if (icon == null) {
                    continue;
                }
                String key = prefix + "_" + buttonID.getKey() + "_" + buttonState.getKey();
                Icon otherIcon = toMap.put(key, icon);
                if (otherIcon != null && !otherIcon.equals(icon)) {
                    throw new RuntimeException("Two related LAF classes both returned icons for key "
                            + key + "; not sure which one to display");
                }
            }
        }
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                final VectorIconTester vit = new VectorIconTester();
                vit.setVisible(true);
                vit.validate();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        vit.dumpGraphicsToFile();
                    }
                });
            }
        });
    }

    private static final class IconPreviewPane extends JPanel implements Scrollable {
        private static final boolean INCLUDE_HUGE_ICON = true;
        private static final int ICON_BASE_SIZE_X = TEST_AQUA ? 26 : 16;
        private static final int ICON_BASE_SIZE_Y = 16;
        private static final int ICON_ROW_HEIGHT
                = Math.max(ICON_BASE_SIZE_Y * 3 + 8, (INCLUDE_HUGE_ICON ? 16 * 8 + 16 : 0));
        private final Map<String, Map<String, Icon>> iconsByLAF;
        private final Set<String> namesAfterLAF;
        private int preferredWidth = 300;
        /**
         * If true, show the first entry in namesAfterLAF in the last timer-switched column,
         * otherwise show the second.
         */
        private boolean timerSwitchState = false;
        /**
         * Continuously switch between the two first LAFs in the last column when Shift is held down
         * (or toggled with space).
         */
        private final Timer lafSwitchTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchLastColumnLAF();
            }
        });
        /**
         * Continuously repaint in case "Apply Code Changes" was applied in the debugger to modify
         * the drawing routine.
         */
        private final Timer repaintTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });

        public void switchLastColumnLAF() {
            timerSwitchState = !timerSwitchState;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(preferredWidth,
                    namesAfterLAF.size() * ICON_ROW_HEIGHT + 2 * ICON_ROW_HEIGHT);
        }

        public IconPreviewPane(Map<String, Icon> icons) {
            this.iconsByLAF = new LinkedHashMap<String, Map<String, Icon>>();
            this.namesAfterLAF = new LinkedHashSet<String>();
            for (Entry<String, Icon> iconEntry : icons.entrySet()) {
                String name = iconEntry.getKey();
                int pos = name.indexOf("_");
                if (pos < 2) {
                    throw new RuntimeException();
                }
                String lafPrefix = name.substring(0, pos);
                Map<String, Icon> inLAFmap = iconsByLAF.get(lafPrefix);
                if (inLAFmap == null) {
                    inLAFmap = new LinkedHashMap<String, Icon>();
                    iconsByLAF.put(lafPrefix, inLAFmap);
                }
                String nameAfterLAF = name.substring(pos + 1);
                if (nameAfterLAF.isEmpty()) {
                    throw new RuntimeException();
                }
                inLAFmap.put(nameAfterLAF, iconEntry.getValue());
                namesAfterLAF.add(nameAfterLAF);
                // Some of the mac icons are actually 26 pixels wide.
                if (false) {
                    Icon icon = iconEntry.getValue();
                    if (icon.getIconWidth() > ICON_BASE_SIZE_X) {
                        throw new RuntimeException();
                    }
                    if (icon.getIconHeight() > ICON_BASE_SIZE_Y) {
                        throw new RuntimeException();
                    }
                }
            }
            this.lafSwitchTimer.setRepeats(true);
            this.repaintTimer.setRepeats(true);
            this.repaintTimer.start();
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent evt) {
                    KeyStroke ks = KeyStroke.getKeyStrokeForEvent(evt);
                    if (KeyStroke.getAWTKeyStroke("shift pressed SHIFT").equals(ks)) {
                        if (!lafSwitchTimer.isRunning()) {
                            switchLastColumnLAF();
                            lafSwitchTimer.start();
                        }
                    } else if (KeyStroke.getAWTKeyStroke("pressed SPACE").equals(ks)) {
                        if (lafSwitchTimer.isRunning()) {
                            lafSwitchTimer.stop();
                        } else {
                            switchLastColumnLAF();
                            lafSwitchTimer.start();
                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent evt) {
                    if (KeyStroke.getAWTKeyStroke("released SHIFT").equals(KeyStroke.getKeyStrokeForEvent(evt))) {
                        lafSwitchTimer.stop();
                    }
                }
            });
            setFocusable(true);
            requestFocusInWindow();
        }

        private static Graphics2D createGraphicsWithRenderingHintsConfigured(Graphics basedOn) {
            Graphics2D ret = (Graphics2D) basedOn.create();
            GraphicsUtils.configureDefaultRenderingHints(basedOn);
            return ret;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = createGraphicsWithRenderingHintsConfigured(g);
            try {
                paintComponent(g2, (Graphics2D) g);
            } finally {
                g2.dispose();
            }
        }

        private void paintComponent(Graphics2D g, Graphics2D originalGraphics) {
            List<String> columnsToShow = new ArrayList<String>(iconsByLAF.keySet());
            if (columnsToShow.size() >= 2) {
                columnsToShow.add(timerSwitchState ? columnsToShow.get(0) : columnsToShow.get(1));
            }
            Font font = new Font("Arial", Font.PLAIN, 12);
            final int fontAscent = g.getFontMetrics(font).getAscent();
            int x = 30;
            final int START_Y = 30;
            final int LAF_HEADING_Y_MARGIN = 30;
            int y = START_Y;
            g.setFont(font);
            g.setColor(Color.BLACK);
            // First column: name of button types.
            y += LAF_HEADING_Y_MARGIN;
            for (String nameAfterLAF : namesAfterLAF) {
                g.drawString(nameAfterLAF, x, y + fontAscent);
                y += ICON_ROW_HEIGHT;
            }
            x += 200;
            int columnIndex = 0;
            for (String lafName : columnsToShow) {
                Map<String, Icon> lafIcons = iconsByLAF.get(lafName);
                if (lafIcons == null) {
                    throw new RuntimeException();
                }
                y = START_Y;
                g.setFont(font);
                g.setColor(Color.BLACK);
                String columnTitle;
                if (columnIndex == columnsToShow.size() - 1 && columnsToShow.size() > 1) {
                    columnTitle = lafName + " (shift/space to toggle)";
                } else {
                    columnTitle = lafName;
                }
                g.drawString(columnTitle, x, y + fontAscent);
                y += LAF_HEADING_Y_MARGIN;
                int xAdvance = 0;
                for (String nameAfterLAF : namesAfterLAF) {
                    Icon icon = lafIcons.get(nameAfterLAF);
                    if (icon != null) {
                        /* Use the original graphics object here to make sure that icon painters set
                        their own rendering hints as necessary. */
                        xAdvance = Math.max(xAdvance, paintIconRow(g, originalGraphics, icon, x, y));
                    }
                    y += ICON_ROW_HEIGHT;
                }
                x += xAdvance;
                columnIndex++;
            }
            if (x != preferredWidth) {
                preferredWidth = x;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        revalidate();
                    }
                });
            }
        }

        /**
         * @return the X advance
         */
        private int paintIconRow(Graphics2D g, Graphics2D originalGraphics, Icon icon, int x, int y) {
            // Show one column of icons in a different background, to test transparency.
            g.setColor(Color.GREEN);
            // Darker gray, for testing against darker LAF backgrounds.
            //g.setColor(new Color(150, 150, 150, 255));
            g.fillRect(x - 5, y - 5, ICON_BASE_SIZE_X + 10, ICON_ROW_HEIGHT + 10);
            double useX = x;
            int ret = 0;
            for (int i = 0; i < 2; i++) {
                if (!INCLUDE_HUGE_ICON && i == 1) {
                    break; // Not enough space for a row simulating misalignment.
                }
                useX = x;
                double useY = y + (i == 0 ? 0 : (16 * 4));
                if (i == 1) {
                    /* Simulate misalignment. Note that there won't really be misalignment at 100% scaling,
                    since it's typically an artifact of non-integral HiDPI scaling. */
                    useX += 0.49;
                    useY += 0.49;
                }
                // Misalignment only needs to be simulated on non-integral HiDPI scalings.
                useX += paintIcon(g, originalGraphics, i == 0 ? icon : null, useX, useY, 1.0);
                useX += paintIcon(g, originalGraphics, i == 0 ? icon : null, useX, useY, 1.0);
                useX += paintIcon(g, originalGraphics, icon, useX, useY, 1.25);
                useX += paintIcon(g, originalGraphics, icon, useX, useY, 1.5);
                useX += paintIcon(g, originalGraphics, icon, useX, useY, 1.75);
                useX += paintIcon(g, originalGraphics, i == 0 ? icon : null, useX, useY, 2.0);
                useX += paintIcon(g, originalGraphics, icon, useX, useY, 2.25);
                useX += paintIcon(g, originalGraphics, i == 0 ? icon : null, useX, useY, 3.0);
                if (i == 0) {
                    if (INCLUDE_HUGE_ICON) {
                        useX += paintIcon(g, originalGraphics, icon, useX, y, 8.0);
                    }
                    ret = (int) (useX - x);
                }
            }
            return ret;
        }

        /**
         * @param icon if null, don't paint, just return the advance
         * @return the X advance
         */
        private int paintIcon(
                Graphics2D newg, Graphics2D originalGraphics, Icon icon, double x, double y, double scaling)
        {
            String resstr;
            boolean aligned = x == (int) x && y == (int) y;
            if (icon == null) {
                resstr = "";
            } else if (!aligned) {
                resstr = "misalign";
            } else if (scaling > 1.0) {
                resstr = ((int) Math.round(scaling * 100)) + "%";
            } else {
                resstr = icon.getIconWidth() + "x" + icon.getIconHeight();
            }
            newg.setFont(new Font("Arial", Font.PLAIN, 8));
            newg.setColor(Color.BLACK);
            if (!resstr.isEmpty()) {
                newg.drawString(resstr, (int) x,
                        ((int) y) - originalGraphics.getFontMetrics().getDescent());
            }
            AffineTransform oldTransform = originalGraphics.getTransform();
            originalGraphics.translate(x, y);
            originalGraphics.scale(scaling, scaling);
            if (icon != null) {
                // Make it evident if the icon forgets to set the color or shape.
                originalGraphics.setColor(Color.PINK);
                originalGraphics.setStroke(new BasicStroke((int) (10 * scaling)));
                /* Paint the icon with a non-zero x/y offset, to make sure its implementation
                handles this correctly. */
                originalGraphics.translate(-10, -15);
                icon.paintIcon(this, originalGraphics, 10, 15);
                originalGraphics.translate(10, 15);
            }
            originalGraphics.setTransform(oldTransform);
            if (icon != null && scaling > 4 && ((int) scaling) == scaling) {
                int s = (int) scaling;
                // Display a pixel grid on top
                originalGraphics.setColor(new Color(0, 0, 0, 60));
                originalGraphics.setStroke(new BasicStroke(1));
                int w = icon.getIconWidth();
                int h = icon.getIconHeight();
                // Vertical lines.
                for (int gridX = 0; gridX <= w; gridX++) {
                    originalGraphics.drawLine(
                            (int) x + gridX * s, (int) y, (int) x + gridX * s, (int) y + h * s);
                }
                // Horizontal lines.
                for (int gridY = 0; gridY <= h; gridY++) {
                    originalGraphics.drawLine(
                            (int) x, (int) y + gridY * s, (int) x + w * s, (int) y + gridY * s);
                }
            }
            return ICON_BASE_SIZE_X + (int) Math.ceil(scaling * ICON_BASE_SIZE_X);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return ICON_ROW_HEIGHT;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return ICON_ROW_HEIGHT;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
