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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Icon;

/**
 * Scalable vector icons for the Aqua tab control L&amp;F. These icons look good both at the
 * regular 100% scale as well as at the 200% scale used on Retina screens. At 100% scale, they are
 * sized and aligned exactly like the bitmap icons that were previously used for the Aqua LAF, and
 * look mostly the same, with a few updates to match current design standards:
 *
 * <ul>
 *   <li>The slight bevel effect that was present on some of the bitmap buttons, and the slight
 *       gradient that was present on the circular bitmap buttons, has been dropped in the name of
 *       "flat design". (Apple HID guidelines say
 *       <a href="https://developer.apple.com/design/human-interface-guidelines/macos/buttons/bevel-buttons">"Avoid
 *       using bevel buttons"</a>.)
 *   <li>Rounded rectangle buttons, including the segmented "scroll left/right" buttons, have had
 *       their rounding radius increased slightly, to match that of
 *       <a href="https://developer.apple.com/design/human-interface-guidelines/macos/selectors/segmented-controls">segmented controls</a>
 *       on MacOS High Sierra.
 *   <li>The smaller buttons now only show their solid backgrounds on rollover and press. This
 *       reduces visual clutter, and is consistent with buttons in XCode, Finder, Chrome, and
 *       Photoshop on MacOS High Sierra. To remain visible, some of these icons have had their
 *       contents enlarged slightly.
 *   <li>Except for the "x" button that closes a tab, the background shape of the small buttons have
 *       been changed from a circle to a rounded rectangle, for consistency with other MacOS
 *       apps (e.g. the "Mailboxes" icon toolbar button in the Mail app, or the "Refresh" button on
 *       Chrome). This also allows the symbols inside to be made slightly larger; see above. (Apple
 *       HID guidelines:
 *       <a href="https://developer.apple.com/design/human-interface-guidelines/macos/buttons/round-buttons">"Avoid
 *       using round buttons."</a>.)
 *   <li>The circular "x" that closes a tab is given a red color on rollover and press, like in
 *       Chrome for MacOS (and Windows), and like on NetBeans' Windows 8 LAF.
 *   <li>Some slight brightness variations that were present between the previous bitmap button
 *       types have been dropped, as these seemed to be accidental.
 * </ul>
 *
 * @author Eirik Bakke
 */
@SuppressWarnings("serial")
final class AquaVectorTabControlIcon extends VectorIcon {
    private static final Map<Entry<Integer,Integer>,Icon> INSTANCES = populateInstances();
    private final int buttonId;
    private final int buttonState;

    private static void populateOne(
            Map<Entry<Integer,Integer>,Icon> toMap, int buttonId, int buttonState)
    {
        final int width;
        final int height;
        switch (buttonId) {
            case TabControlButton.ID_CLOSE_BUTTON:
                width = 14;
                height = 12;
                break;
            case TabControlButton.ID_RESTORE_GROUP_BUTTON:
            case TabControlButton.ID_SLIDE_GROUP_BUTTON:
                width = 16;
                height = 16;
                break;
            case TabControlButton.ID_PIN_BUTTON:
                /* The pin button is shown next to the close button of a minimized panel that is
                shown temporarily when the user hovers over its icon. So it must be the same size as
                the close button. */
                width = 14;
                height = 12;
                break;
            case TabControlButton.ID_SCROLL_LEFT_BUTTON:
                width = 26;
                height = 15;
                break;
            case TabControlButton.ID_SCROLL_RIGHT_BUTTON:
                width = 25;
                height = 15;
                break;
            case TabControlButton.ID_DROP_DOWN_BUTTON:
            case TabControlButton.ID_MAXIMIZE_BUTTON:
            case TabControlButton.ID_RESTORE_BUTTON:
                width = 20;
                height = 15;
                break;
            default:
                throw new IllegalArgumentException();
        }
        toMap.put(new SimpleEntry<Integer,Integer>(buttonId, buttonState),
                new AquaVectorTabControlIcon(buttonId, buttonState, width, height));
    }

    private static Map<Entry<Integer,Integer>,Icon> populateInstances() {
        // The string keys of these maps aren't currently used, but are useful for debugging.
        Map<String, Integer> buttonIDs = new LinkedHashMap<String, Integer>();
        // ViewTabDisplayerUI
        buttonIDs.put("close", TabControlButton.ID_CLOSE_BUTTON);
        // These don't seem to be in use anymore.
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

    private AquaVectorTabControlIcon(int buttonId, int buttonState, int width, int height) {
        super(width, height);
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
        if (buttonId == TabControlButton.ID_MAXIMIZE_BUTTON ||
            buttonId == TabControlButton.ID_RESTORE_BUTTON ||
            buttonId == TabControlButton.ID_DROP_DOWN_BUTTON ||
            buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON ||
            buttonId == TabControlButton.ID_SCROLL_RIGHT_BUTTON)
        {
            paintLargerRectangleIcon(c, g, width, height, scaling);
        } else if (buttonId == TabControlButton.ID_CLOSE_BUTTON) {
            paintSmallCircleCloseIcon(c, g, width, height, scaling);
        } else {
            paintSmallRectangleIcon(c, g, width, height, scaling);
        }
    }

    private void paintSmallCircleCloseIcon(
            Component c, Graphics2D g, int width, int height, double scaling)
    {
        // Background circle diameter.
        double d = Math.min(width, height);
        Color bgColor = new Color(0, 0, 0, 0); // Alpha zero means no background.
        /* Use transparency to achieve the right dark gray level, to make sure symbols are equally
        visible on all backgrounds. */
        Color fgColor = new Color(0, 0, 0, 168);
        if (buttonState == TabControlButton.STATE_ROLLOVER) {
            fgColor = Color.WHITE;
            /* Red, with some transparency to blend onto the background. Chrome would have
            (244, 65, 54, 255), here, but the value below works better with our expected
            backgrounds. */
            bgColor = new Color(255, 35, 25, 215);
        } else if (buttonState == TabControlButton.STATE_PRESSED) {
            fgColor = Color.WHITE;
            // Slightly darker red. Chrome would have (196, 53, 43, 255) here; see above.
            bgColor = new Color(185, 43, 33, 215);
        } else if (buttonState == TabControlButton.STATE_DISABLED) {
            // Light grey (via transparent black to work well on any background).
            fgColor = new Color(0, 0, 0, 60);
        }
        if (bgColor.getAlpha() > 0) {
            double circPosX = (width - d) / 2.0;
            double circPosY = (height - d) / 2.0;
            Shape bgCircle = new Ellipse2D.Double(circPosX, circPosY, d, d);
            g.setColor(bgColor);
            g.fill(bgCircle);
        }
        g.setColor(fgColor);
        double strokeWidth = 1.4 * scaling;
        // Middle x and y.
        double mx = width / 2.0;
        double my = height / 2.0;
        // Radius of the cross ("X") symbol.
        double cr = 0.45 * (d / 2.0);
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
    }

    private void paintSmallRectangleIcon(
            Component c, Graphics2D g, int width, int height, double scaling)
    {
        Color bgColor = new Color(0, 0, 0, 0); // Alpha zero means no background.
        /* Use transparency to achieve the right dark gray level, to make sure symbols are equally
        visible on all backgrounds. */
        Color fgColor = new Color(0, 0, 0, 168);
        if (buttonState == TabControlButton.STATE_DISABLED) {
            // Light grey (via transparent black to work well on any background).
            fgColor = new Color(0, 0, 0, 60);
        } else if (buttonState == TabControlButton.STATE_ROLLOVER) {
            /* Light grey (via transparent black), like in XCode tab close buttons, the Chrome
            "refresh" button, or the Mail app's "Mailboxes" button (used the slightly darker level
            from the latter). */
            bgColor = new Color(0, 0, 0, 51);
            fgColor = Color.WHITE;
        } else if (buttonState == TabControlButton.STATE_PRESSED) {
            /* Slightly darker light grey (via transparent black). Same as in the aforementioned
            "Mailboxes" icon. */
            bgColor = new Color(0, 0, 0, 94);
            fgColor = Color.WHITE;
        }
        if (bgColor.getAlpha() > 0) {
            /* Use the same rounding radius as in paintLargerRectangleIcon. Same as in the
            aforementioned "Mailboxes" icon. */
            double arc = scaling * 6.0;
            Shape bgRect = new RoundRectangle2D.Double(0, 0, width, height, arc, arc);
            g.setColor(bgColor);
            g.fill(bgRect);
        }
        g.setColor(fgColor);
        if (buttonId == TabControlButton.ID_RESTORE_GROUP_BUTTON) {
            // Draw one little window on top of another.
            int marginX = round(3 * scaling);
            int marginY = round(3 * scaling);
            int winWidth = round(7.0 * scaling);
            int winHeight = round(6.0 * scaling);
            // Upper right-hand corner.
            int win1X = width - marginX - winWidth;
            int win1Y = marginY;
            /* Lower left-hand corner. Make sure the window symbols are not too close on any scaling
            level. */
            int win2X = Math.min((int) Math.floor(win1X - 2 * scaling), marginX);
            int win2Y = Math.max((int) Math.ceil(win1Y + 2 * scaling), round(height - 9.5 * scaling));
            Area win1 = getWindowSymbol(scaling, win1X, win1Y, winWidth, winHeight);
            Area win2 = getWindowSymbol(scaling, win2X, win2Y, winWidth, winHeight);
            // Make window 2 appear "on top of" window 1.
            win1.subtract(new Area(win2.getBounds2D()));
            g.fill(win1);
            g.fill(win2);
        } else if (buttonId == TabControlButton.ID_SLIDE_GROUP_BUTTON) {
            int marginX = (int) (3 * scaling);
            int marginTop = (int) (4 * scaling);
            int marginBot = (int) (4 * scaling);
            Area win = getWindowSymbol(scaling, marginX, marginTop,
                    width - 2 * marginX,
                    height - marginTop - marginBot);
            g.fill(win);
        } else if (buttonId == TabControlButton.ID_PIN_BUTTON) {
            int marginX = (int) (3 * scaling);
            int marginTop = (int) (2 * scaling);
            int marginBot = (int) (2 * scaling);
            Area win = getWindowSymbol(scaling, marginX, marginTop,
                    width - 2 * marginX,
                    height - marginTop - marginBot);
            g.fill(win);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void paintLargerRectangleIcon(
            Component c, Graphics2D g, int width, int height, double scaling)
    {
        final Color bgTopColor;
        final Color bgBotColor;
        final Color symbolColor;
        final Color borderColor;
        // These colors taken from the previous bitmap icons.
        if (buttonState == TabControlButton.STATE_DEFAULT) {
            bgTopColor = new Color(191, 191, 191);
            bgBotColor = new Color(135, 135, 135);
            borderColor = new Color(81, 81, 81);
            symbolColor = new Color(48, 48, 48);
        } else if (buttonState == TabControlButton.STATE_PRESSED) {
            bgTopColor = new Color(182, 182, 182);
            bgBotColor = new Color(129, 129, 129);
            borderColor = new Color(81, 81, 81);
            symbolColor = new Color(45, 45, 45);
        } else if (buttonState == TabControlButton.STATE_DISABLED) {
            bgTopColor = new Color(166, 166, 166);
            bgBotColor = new Color(137, 137, 137);
            borderColor = new Color(111, 111, 111);
            symbolColor = new Color(97, 97, 97);
        } else if (buttonState == TabControlButton.STATE_ROLLOVER) {
            bgTopColor = new Color(198, 198, 198);
            bgBotColor = new Color(149, 149, 149);
            borderColor = new Color(81, 81, 81);
            symbolColor = new Color(77, 77, 77);
        } else {
            throw new IllegalArgumentException();
        }
        /* Pick a stroke width that will make the outer border 1 physical pixel wide on both 100%
        and 200% (Retina) scaling, for consistency with native segmented controls. */
        int strokeWidth = round(0.6 * scaling);
        g.setPaint(new GradientPaint(new Point2D.Double(0, strokeWidth),
                bgTopColor,
                new Point2D.Double(0, height - strokeWidth),
                bgBotColor));
        /* Make the scroll left and right buttons extend beyond their right and left edges,
        respectively. Then clip them at the icon's dimensions to get the correct segmented control
        effect. */
        int rectExtraDir;
        if (buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON) {
            rectExtraDir = 1;
        } else if (buttonId == TabControlButton.ID_SCROLL_RIGHT_BUTTON) {
            rectExtraDir = -1;
        } else {
            rectExtraDir = 0;
        }
        /* Use a rounded rectangle radius consistent with that of segmented buttons and comboboxes
        on MacOS High Sierra. (To match the old bitmap Aqua LAF exactly, we could have used 4.0
        here instead.) */
        double arc = scaling * 6.0;
        double rectExtraX = rectExtraDir * (strokeWidth + arc);
        Shape rect = new RoundRectangle2D.Double(
                strokeWidth / 2.0 + (rectExtraDir < 0 ? rectExtraX : 0),
                strokeWidth / 2.0,
                width - strokeWidth + Math.abs(rectExtraX),
                height - strokeWidth, arc, arc);
        g.clipRect(0, 0, width, height);
        // Draw the gradient background of the rounded rectangle.
        g.fill(rect);
        // Now draw the border around the rounded rectangle.
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(borderColor);
        g.draw(rect);
        // The width to use for centering.
        int useWidth;
        if (buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON) {
            // The scroll left button includes the separator line against the scroll right button.
            g.fillRect(width - strokeWidth, 0, strokeWidth, height);
            useWidth = width - strokeWidth;
        } else {
            useWidth = width;
        }
        g.setColor(symbolColor);
        if (buttonId == TabControlButton.ID_MAXIMIZE_BUTTON) {
            int marginX = round(4 * scaling);
            int marginTop = round(3 * scaling);
            int marginBot = round(3 * scaling);
            /* Draw one larger window symbol. The getWindowSymbol method ensures we are using the
            same window border thickness as for ID_RESTORE_BUTTON. */
            g.fill(getWindowSymbol(scaling, marginX, marginTop,
                    width - 2 * marginX, height - marginTop - marginBot));
        } else if (buttonId == TabControlButton.ID_RESTORE_BUTTON) {
            // Draw one little window on top of another.
            int marginX = round(4 * scaling);
            int marginTop = round(2 * scaling);
            int marginBot = round(2.5 * scaling);
            int winWidth = round(9 * scaling);
            int winHeight = round(7.0 * scaling);
            // Upper right-hand corner.
            int win1X = width - marginX - winWidth;
            int win1Y = marginTop;
            /* Lower left-hand corner. Make sure the window symbols are not too close on any scaling
            level. */
            int win2X = Math.min((int) Math.floor(win1X - 2 * scaling), marginX);
            int win2Y = Math.max(win1Y + round(2.7 * scaling), height - winHeight - marginBot);
            Area win1 = getWindowSymbol(scaling, win1X, win1Y, winWidth, winHeight);
            Area win2 = getWindowSymbol(scaling, win2X, win2Y, winWidth, winHeight);
            // Make window 2 appear "on top of" window 1.
            win1.subtract(new Area(win2.getBounds2D()));
            g.fill(win1);
            g.fill(win2);
        } else if (buttonId == TabControlButton.ID_DROP_DOWN_BUTTON ||
            buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON ||
            buttonId == TabControlButton.ID_SCROLL_RIGHT_BUTTON)
        {
            if (buttonId == TabControlButton.ID_SCROLL_LEFT_BUTTON) {
                // Rotate 90 degrees clockwise, with a small position adjustment.
                g.translate(round(1 * scaling), 0);
                g.rotate(Math.PI / 2.0, useWidth / 2.0, height / 2.0);
            } else if (buttonId == TabControlButton.ID_SCROLL_RIGHT_BUTTON) {
                // Rotate 90 degrees counterclockwise, with a small position adjustment.
                g.translate(-round(1 * scaling), 0);
                g.rotate(-Math.PI / 2.0, useWidth / 2.0, height / 2.0);
            }
            double arrowWidth, arrowHeight;
            if (buttonId == TabControlButton.ID_DROP_DOWN_BUTTON) {
                /* Make the arrow a tiny bit wider than in the old bitmap icons here. Using
                arrowWidth = 5.0 would have given the exact same dimensions as the old bitmap icons
                at 100% scaling. */
                arrowWidth = 6.0 * scaling;
                arrowHeight = 4.0 * scaling;
            } else {
                // These dimensions match the old bitmap icons at 100% scaling.
                arrowWidth = 6.7 * scaling;
                arrowHeight = 3.8 * scaling;
            }

            /* Draw a simple arrowhead triangle pointing downwards (before any rotations). Keep the
            top line aligned to device pixels. No need to round the other positions. */
            final int y = round((height - arrowHeight) / 2.0);
            final double marginX = (useWidth - arrowWidth) / 2.0;
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
     * Make a small window symbol. This is used in several of the icons here. All coordinates are
     * in device pixels.
     */
    private Area getWindowSymbol(double scaling, int x, int y, int width, int height) {
        /* Pick a thickness that will make the window symbol border 2 physical pixels wide at 200%
        scaling, to look consistent with the rest of the UI, including borders and icons that do not
        have any special Retina support. */
        int borderThickness = round(0.8 * scaling);
        int titleBarHeight =
                (buttonId == TabControlButton.ID_SLIDE_GROUP_BUTTON ||
                buttonId == TabControlButton.ID_PIN_BUTTON)
                ? borderThickness
                : Math.max(round(1.6 * scaling), borderThickness + height / 7);
        int windowX = round(x);
        int windowY = round(y);
        Area ret = new Area(new Rectangle2D.Double(
                windowX, windowY, width, height));
        ret.subtract(new Area(new Rectangle2D.Double(
                windowX + borderThickness, windowY + titleBarHeight,
                width - borderThickness * 2,
                height - borderThickness - titleBarHeight)));
        if (buttonId == TabControlButton.ID_SLIDE_GROUP_BUTTON) {
            ret.add(new Area(new Rectangle2D.Double(
                windowX + borderThickness * 2,
                windowY + height - borderThickness * 4,
                round((width - borderThickness * 4) * 0.67),
                borderThickness * 2)));
        } else if (buttonId == TabControlButton.ID_PIN_BUTTON) {
            int marginX = round(width * 0.3);
            int marginY = round(height * 0.3);
            ret.add(new Area(new Rectangle2D.Double(
                windowX + marginX, windowY + marginY,
                width - marginX * 2,
                height - marginY * 2)));
        }
        return ret;
    }
}
