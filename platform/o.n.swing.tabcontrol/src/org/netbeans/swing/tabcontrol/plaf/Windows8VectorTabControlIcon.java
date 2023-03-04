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
package org.netbeans.swing.tabcontrol.plaf;

import org.openide.util.VectorIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
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

/**
 * Scalable vector icons for the Windows tab control L&amp;F, for use with HiDPI screens. These
 * icons look good at all of the standard scaling factors available on Windows (see superclass
 * Javadoc). At 100% scale, they look nearly identical to bitmap icons that were used previously for
 * the Windows 8 LAF.
 *
 * @author Eirik Bakke
 */
@SuppressWarnings("serial")
final class Windows8VectorTabControlIcon extends VectorIcon {
    private static final Map<Entry<Integer,Integer>,Icon> INSTANCES = populateInstances();
    private final int buttonId;
    private final int buttonState;

    private static void populateOne(
            Map<Entry<Integer,Integer>,Icon> toMap, int buttonId, int buttonState)
    {
        toMap.put(new SimpleEntry<Integer,Integer>(buttonId, buttonState),
                new Windows8VectorTabControlIcon(buttonId, buttonState));
    }

    private static Map<Entry<Integer,Integer>,Icon> populateInstances() {
        // The string keys of these maps aren't currently used, but are useful for debugging.
        Map<String, Integer> buttonIDs = new LinkedHashMap<String, Integer>();
        // ViewTabDisplayerUI
        buttonIDs.put("close", TabControlButton.ID_CLOSE_BUTTON);
        /* These don't seem to be in use anymore. Or at least they don't have modernized icons in
        the Windows8 LAF. */
        //buttonIDs.put("slide_right", TabControlButton.ID_SLIDE_RIGHT_BUTTON);
        //buttonIDs.put("slide_left", TabControlButton.ID_SLIDE_LEFT_BUTTON);
        //buttonIDs.put("slide_down", TabControlButton.ID_SLIDE_DOWN_BUTTON);
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
        Map<Entry<Integer,Integer>,Icon> ret = new LinkedHashMap<Entry<Integer,Integer>,Icon>();
        for (Entry<String,Integer> buttonID : buttonIDs.entrySet()) {
          for (Entry<String,Integer> buttonState : buttonStates.entrySet()) {
              populateOne(ret, buttonID.getValue(), buttonState.getValue());
          }
        }
        // Effectively immutable upon assignment to the final static variable.
        return Collections.unmodifiableMap(ret);
    }

    private Windows8VectorTabControlIcon(int buttonId, int buttonState) {
        super(14, buttonId == TabControlButton.ID_CLOSE_BUTTON ? 15 : 14);
        this.buttonId = buttonId;
        this.buttonState = buttonState;
    }

    /**
     * @return null if the requested icon is not available in vector format
     */
    public static Icon get(int buttonId, int buttonState) {
        return INSTANCES.get(new SimpleEntry<Integer,Integer>(buttonId, buttonState));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g, int width, int height, double scaling) {
        Color bgColor = new Color(0, 0, 0, 0); // Alpha zero means no background.
        Color fgColor = new Color(86, 86, 86, 255);
        {
            Color closeColor = (buttonId == TabControlButton.ID_CLOSE_BUTTON)
                    // A nice red.
                    ? new Color(199, 79, 80, 255) : null;
            if (buttonState == TabControlButton.STATE_DISABLED) {
                // Light grey (via transparent black to work well on any background).
                fgColor = new Color(0, 0, 0, 45);
            } else if (buttonState == TabControlButton.STATE_PRESSED) {
                // A nice blue.
                bgColor = closeColor != null ? closeColor : new Color(57, 100, 178, 255);
                fgColor = Color.WHITE;
            } else if (buttonState == TabControlButton.STATE_ROLLOVER) {
                if (closeColor != null) {
                    bgColor = closeColor;
                    fgColor = Color.WHITE;
                } else {
                    /* Light blue, via transparency to work well on any background. In the grey
                    toolbar, it comes out similar to the hover background used for toolbar buttons. */
                    bgColor = new Color(0, 132, 247, 49);
                }
            }
        }
        if (bgColor.getAlpha() > 0) {
            g.setColor(bgColor);
            g.fillRect(0, 0, width, height);
        }
        g.setColor(fgColor);
        if (buttonId == TabControlButton.ID_CLOSE_BUTTON) {
            // Draw an "X" with a flat top and bottom.
            if (getIconWidth() == width && getIconHeight() == height) {
                // For the unscaled case, this icon looks better without anti-aliasing.
                setAntiAliasing(g, false);
            }
            // Use a slightly heavier line when there's a non-light background.
            double strokeWidth = (bgColor.getAlpha() > 0 ? 1.0 : 0.8) * scaling;
            if (scaling > 1.0) {
                // Use a heavier line when we have more pixels available.
                strokeWidth *= 1.5f;
            }
            double marginX = 3.5 * scaling; // Don't round this one.
            int topMarginY = round(4 * scaling);
            int botMarginY = round(4 * scaling);
            // Flatten the top and bottom.
            g.clip(new Rectangle2D.Double(0, topMarginY, width, height - topMarginY - botMarginY));
            // Draw the "X".
            g.setStroke(new BasicStroke((float) strokeWidth));
            g.draw(new Line2D.Double(marginX, topMarginY, width - marginX, height - botMarginY));
            g.draw(new Line2D.Double(width - marginX, topMarginY, marginX, height - botMarginY));
        } else if (buttonId == TabControlButton.ID_PIN_BUTTON ||
                buttonId == TabControlButton.ID_RESTORE_GROUP_BUTTON ||
                buttonId == TabControlButton.ID_RESTORE_BUTTON)
        {
            // Draw one little window on top of another.
            int margin = round(2 * scaling);
            int winWidth = round(6.5 * scaling);
            int winHeight = round(5.5 * scaling);
            // Upper right-hand corner.
            int win1X = width - margin - winWidth;
            int win1Y = margin;
            // Lower left-hand corner.
            int win2X = margin;
            int win2Y = round(5.5 * scaling);
            Area win1 = getWindowSymbol(scaling, win1X, win1Y, winWidth, winHeight);
            Area win2 = getWindowSymbol(scaling, win2X, win2Y, winWidth, winHeight);
            // Make window 2 appear "on top of" window 1.
            win1.subtract(new Area(win2.getBounds2D()));
            g.fill(win1);
            g.fill(win2);
        } else if (buttonId == TabControlButton.ID_MAXIMIZE_BUTTON) {
            int marginX = round(2.2 * scaling);
            int marginY = round(3 * scaling);
            int windowHeight = round(7.5 * scaling);
            /* Draw one larger window. The getWindowSymbol method ensures we are using the same
            window border thickness as for ID_RESTORE_BUTTON. */
            g.fill(getWindowSymbol(scaling, marginX, marginY, width - 2 * marginX, windowHeight));
        } else if (buttonId == TabControlButton.ID_SLIDE_GROUP_BUTTON) {
            // Draw a simple bar towards the bottom of the icon.
            int marginX = round(2 * scaling);
            int barX = marginX;
            int barY = round(8 * scaling);
            int barWidth = width - marginX * 2;
            // Use the same thickness as the title bar in getWindowSymbol.
            int barThickness = round(1.8 * scaling);
            g.fill(new Rectangle2D.Double(barX, barY, barWidth, barThickness));
        } else if (buttonId == TabControlButton.ID_DROP_DOWN_BUTTON ||
                   buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON ||
                   buttonId == TabControlButton.ID_SCROLL_RIGHT_BUTTON)
        {
            if (getIconWidth() == width && getIconHeight() == height) {
                // For the regular 100% scaling level, this icon looks better without anti-aliasing.
                setAntiAliasing(g, false);
            }
            if (buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON) {
                // Rotate 90 degrees clockwise, with a small position adjustment.
                g.translate(-round(1 * scaling), 0);
                g.rotate(Math.PI / 2.0, width / 2.0, height / 2.0);
            } else if (buttonId == TabControlButton.ID_SCROLL_RIGHT_BUTTON) {
                // Rotate 90 degrees counterclockwise, with a small position adjustment.
                g.translate(round(1 * scaling), 0);
                g.rotate(-Math.PI / 2.0, width / 2.0, height / 2.0);
            }
            /* Draw a simple arrowhead triangle pointing downwards (before any rotations). Keep the
            top line pixel-aligned. No need to round the other positions. */
            final int y = round(4.0 * scaling);
            final double arrowWidth = (scaling == 1.0 ? 12.0 : 10.0) * scaling;
            final double arrowHeight = 5.0 * scaling;
            final double marginX = (width - arrowWidth) / 2.0;
            final double arrowMidX = marginX + arrowWidth / 2.0;
            Path2D.Double arrowPath = new Path2D.Double();
            arrowPath.moveTo(arrowMidX - arrowWidth / 2.0, y);
            arrowPath.lineTo(arrowMidX, y + arrowHeight);
            arrowPath.lineTo(arrowMidX + arrowWidth / 2.0, y);
            arrowPath.closePath();
            g.fill(arrowPath);
        }
    }

    /**
     * Make a small window symbol (hollow rectangle with a thicker "title bar" on top). This is used
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
        int titleBarHeight = Math.max(round(1.8 * scaling), borderThickness + 1);
        Area ret = new Area(new Rectangle2D.Double(x, y, width, height));
        ret.subtract(new Area(new Rectangle2D.Double(
                x + borderThickness, y + titleBarHeight,
                width - borderThickness * 2,
                height - borderThickness - titleBarHeight)));
        return ret;
    }
}
