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
package org.netbeans.swing.laf.flatlaf.ui;

import com.formdev.flatlaf.util.UIScale;
import org.openide.util.VectorIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.plaf.TabControlButton;

/**
 * Scalable vector icons for the FlatLaf tab control L&amp;F, for use with HiDPI screens. These
 * icons look good at all of the standard scaling factors available on Windows (see superclass
 * Javadoc).
 *
 * This is a copy of Windows8VectorTabControlIcon with modifications:
 * - use "close" icon from AquaVectorTabControlIcon
 * - arrow icons changed to match arrows in FlatLaf (chevron or triangle)
 * - minimize, maximize and restore icons changed
 * - increased size from 14x14 to 16x16
 * - scales in Java 8 and Linux
 *
 * @author Eirik Bakke
 */
@SuppressWarnings("serial")
final class FlatTabControlIcon extends VectorIcon {
    private static final boolean chevron = "chevron".equals(UIManager.getString("Component.arrowType")); // NOI18N
    private static final int arc = UIManager.getInt("TabControlIcon.arc"); // NOI18N
    private static final Color foreground = UIManager.getColor("TabControlIcon.foreground"); // NOI18N
    private static final Color disabledForeground = UIManager.getColor("TabControlIcon.disabledForeground"); // NOI18N
    private static final Color rolloverBackground = UIManager.getColor("TabControlIcon.rolloverBackground"); // NOI18N
    private static final Color pressedBackground = UIManager.getColor("TabControlIcon.pressedBackground"); // NOI18N
    private static final Color closeRolloverBackground = UIManager.getColor("TabControlIcon.close.rolloverBackground"); // NOI18N
    private static final Color closeRolloverForeground = UIManager.getColor("TabControlIcon.close.rolloverForeground"); // NOI18N

    private static final Map<Entry<Integer,Integer>,Icon> INSTANCES = populateInstances();
    private final int buttonId;
    private final int buttonState;
    private final float userScaleFactor; // for Java 8 and Linux

    private static void populateOne(
            Map<Entry<Integer,Integer>,Icon> toMap, int buttonId, int buttonState)
    {
        toMap.put(new SimpleEntry<Integer,Integer>(buttonId, buttonState),
                new FlatTabControlIcon(buttonId, buttonState));
    }

    private static Map<Entry<Integer,Integer>,Icon> populateInstances() {
        // The string keys of these maps aren't currently used, but are useful for debugging.
        Map<String, Integer> buttonIDs = new LinkedHashMap<String, Integer>();
        // ViewTabDisplayerUI
        buttonIDs.put("close", TabControlButton.ID_CLOSE_BUTTON); // NOI18N
        // These don't seem to be in use anymore.
        //buttonIDs.put("slide_right", TabControlButton.ID_SLIDE_RIGHT_BUTTON); // NOI18N
        //buttonIDs.put("slide_left", TabControlButton.ID_SLIDE_LEFT_BUTTON); // NOI18N
        //buttonIDs.put("slide_down", TabControlButton.ID_SLIDE_DOWN_BUTTON); // NOI18N
        buttonIDs.put("pin", TabControlButton.ID_PIN_BUTTON); // NOI18N
        buttonIDs.put("restore_group", TabControlButton.ID_RESTORE_GROUP_BUTTON); // NOI18N
        buttonIDs.put("slide_group", TabControlButton.ID_SLIDE_GROUP_BUTTON); // NOI18N
        // EditorTabDisplayerUI
        buttonIDs.put("scroll_left", TabControlButton.ID_SCROLL_LEFT_BUTTON); // NOI18N
        buttonIDs.put("scroll_right", TabControlButton.ID_SCROLL_RIGHT_BUTTON); // NOI18N
        buttonIDs.put("drop_down", TabControlButton.ID_DROP_DOWN_BUTTON); // NOI18N
        buttonIDs.put("maximize", TabControlButton.ID_MAXIMIZE_BUTTON); // NOI18N
        buttonIDs.put("restore", TabControlButton.ID_RESTORE_BUTTON); // NOI18N
        Map<String, Integer> buttonStates = new LinkedHashMap<String, Integer>();
        buttonStates.put("default", TabControlButton.STATE_DEFAULT); // NOI18N
        buttonStates.put("pressed", TabControlButton.STATE_PRESSED); // NOI18N
        buttonStates.put("disabled", TabControlButton.STATE_DISABLED); // NOI18N
        buttonStates.put("rollover", TabControlButton.STATE_ROLLOVER); // NOI18N
        Map<Entry<Integer,Integer>,Icon> ret = new LinkedHashMap<Entry<Integer,Integer>,Icon>();
        for (Entry<String,Integer> buttonID : buttonIDs.entrySet()) {
          for (Entry<String,Integer> buttonState : buttonStates.entrySet()) {
              populateOne(ret, buttonID.getValue(), buttonState.getValue());
          }
        }
        // Effectively immutable upon assignment to the final static variable.
        return Collections.unmodifiableMap(ret);
    }

    private FlatTabControlIcon(int buttonId, int buttonState) {
        super(UIScale.scale(16), UIScale.scale(16));
        this.buttonId = buttonId;
        this.buttonState = buttonState;
        this.userScaleFactor = UIScale.getUserScaleFactor();
    }

    /**
     * @return null if the requested icon is not available in vector format
     */
    public static Icon get(int buttonId, int buttonState) {
        return INSTANCES.get(new SimpleEntry<Integer,Integer>(buttonId, buttonState));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g, int width, int height, double scaling) {
        // scale on Java 8 and Linux
        scaling *= userScaleFactor;

        Color bgColor = new Color(0, 0, 0, 0); // Alpha zero means no background.
        Color fgColor = foreground;
        {
            Color closeColor = (buttonId == TabControlButton.ID_CLOSE_BUTTON)
                    ? closeRolloverBackground : null;
            if (buttonState == TabControlButton.STATE_DISABLED) {
                fgColor = disabledForeground;
            } else if (buttonState == TabControlButton.STATE_PRESSED) {
                bgColor = closeColor != null ? closeColor : pressedBackground;
                if (closeColor != null && closeRolloverForeground != null) {
                    fgColor = closeRolloverForeground;
                }
            } else if (buttonState == TabControlButton.STATE_ROLLOVER) {
                bgColor = closeColor != null ? closeColor : rolloverBackground;
                if (closeColor != null && closeRolloverForeground != null) {
                    fgColor = closeRolloverForeground;
                }
            }
        }
        if (bgColor.getAlpha() > 0) {
            int scaledArc = round(arc * 2 * scaling);
            g.setColor(bgColor);
            g.fillRoundRect(0, 0, width, height, scaledArc, scaledArc);
        }
        g.setColor(fgColor);
        if (buttonId == TabControlButton.ID_CLOSE_BUTTON) {
            // Draw an "X".
            // Use a slightly heavier line when there's a non-light background.
            double strokeWidth = (bgColor.getAlpha() > 0 ? 1.0 : 0.8) * scaling;
            if (scaling > 1.0) {
                // Use a heavier line when we have more pixels available.
                strokeWidth *= 1.5f;
            } else if (strokeWidth < 1.0) {
                strokeWidth = 1.0;
            }
            // Middle x and y.
            double mx = width / 2.0;
            double my = height / 2.0;
            // Radius of the cross ("X") symbol.
            double cr = 3.25 * scaling;
            /* Draw the "X". Fill the Shape of the entire cross rather than painting each line
            separately as a Stroke, to avoid the intersecting area getting a higher opacity. */
            Stroke stroke = new BasicStroke(
                    (float) strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            Area area = new Area();
            area.add(new Area(stroke.createStrokedShape(
                    new Line2D.Double(mx - cr, my - cr, mx + cr, my + cr))));
            area.add(new Area(stroke.createStrokedShape(
                    new Line2D.Double(mx + cr, my - cr, mx - cr, my + cr))));
            g.fill(area);
        } else if (buttonId == TabControlButton.ID_PIN_BUTTON ||
                buttonId == TabControlButton.ID_RESTORE_GROUP_BUTTON ||
                buttonId == TabControlButton.ID_RESTORE_BUTTON)
        {
            // Draw one little window on top of another.
            int wh = round(8 * scaling);
            // Upper right-hand corner.
            int win1X = round(5 * scaling);
            int win1Y = round(3 * scaling);
            // Lower left-hand corner.
            int win2X = round(3 * scaling);
            int win2Y = round(5 * scaling);
            Area win1 = getWindowSymbol(scaling, win1X, win1Y, wh, wh);
            Area win2 = getWindowSymbol(scaling, win2X, win2Y, wh, wh);
            // Make window 2 appear "on top of" window 1.
            win1.subtract(new Area(win2.getBounds2D()));
            g.fill(win1);
            g.fill(win2);
        } else if (buttonId == TabControlButton.ID_MAXIMIZE_BUTTON) {
            int xy = round(3 * scaling);
            int wh = round(10 * scaling);
            /* Draw one larger window. The getWindowSymbol method ensures we are using the same
            window border thickness as for ID_RESTORE_BUTTON. */
            g.fill(getWindowSymbol(scaling, xy, xy, wh, wh));
        } else if (buttonId == TabControlButton.ID_SLIDE_GROUP_BUTTON) {
            // Draw a simple bar towards the bottom of the icon.
            int barX = round(3 * scaling);
            int barY = round(7 * scaling);
            int barWidth = round(10 * scaling);
            int barThickness = round(2 * scaling);
            g.fill(new Rectangle2D.Double(barX, barY, barWidth, barThickness));
        } else if (buttonId == TabControlButton.ID_DROP_DOWN_BUTTON ||
                   buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON ||
                   buttonId == TabControlButton.ID_SCROLL_RIGHT_BUTTON)
        {
            if (buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON) {
                // Rotate 90 degrees clockwise, with a small position adjustment.
                g.rotate(Math.PI / 2.0, width / 2.0, height / 2.0);
            } else if (buttonId == TabControlButton.ID_SCROLL_RIGHT_BUTTON) {
                // Rotate 90 degrees counterclockwise, with a small position adjustment.
                g.rotate(-Math.PI / 2.0, width / 2.0, height / 2.0);
            }
            /* Draw a simple arrowhead chevron or triangle pointing downwards (before any rotations). Keep the
            top line pixel-aligned. No need to round the other positions. */
            final int y = round((height - 4.0 * scaling) / 2.0);
            final double arrowWidth = (chevron ? 8.0 : 9.0) * scaling;
            final double arrowHeight = (chevron ? 4.0 : 5.0) * scaling;
            double arrowMidX = width / 2.0;
            if (!chevron && scaling == 1) {
                // shift the triangle a half pixel gives a nice 1px bottom edge
                arrowMidX -= 0.5;
                // fix vertical alignment of left and right scroll buttons
                if (buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON) {
                    arrowMidX += 1.0;
                }
            }
            Path2D.Double arrowPath = new Path2D.Double();
            arrowPath.moveTo(arrowMidX - arrowWidth / 2.0, y);
            arrowPath.lineTo(arrowMidX, y + arrowHeight);
            arrowPath.lineTo(arrowMidX + arrowWidth / 2.0, y);
            if (chevron) {
                // use same stroke control rendering hint as in FlatLaf
                Utils.setRenderingHints( g );

                g.setStroke(new BasicStroke((float) scaling));
                g.draw(arrowPath);
            } else {
                arrowPath.closePath();
                g.fill(arrowPath);
            }
        }
    }

    /**
     * Make a small window symbol (hollow rectangle). This is used
     * in a couple of the icons here. All coordinates are in device pixels.
     */
    private static Area getWindowSymbol(
            double scaling, int x, int y, int width, int height)
    {
        /* Pick a thickness that will make the window symbol border 2 physical pixels wide at 200%
        scaling, to look consistent with the rest of the UI, including existing icons that do not
        have any special HiDPI support. Lower scaling levels will yield a 1 physical pixel wide
        border. */
        int borderThickness = round(0.8 * scaling);
        Area ret = new Area(new Rectangle2D.Double(x, y, width, height));
        ret.subtract(new Area(new Rectangle2D.Double(
                x + borderThickness, y + borderThickness,
                width - borderThickness * 2,
                height - borderThickness * 2)));
        return ret;
    }
}
