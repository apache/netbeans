/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* This file is based in part on the commit
 * https://github.com/JetBrains/intellij-community/commit/34b9dfd0585937c3731e06a89554d1dc86f7f235
 * from 2016-12-14 in the IntelliJ IDEA Community Edition repository, as referenced by Pavel Fatin
 * on the AWT Dev mailing list on 2017-01-04
 * ( http://mail.openjdk.java.net/pipermail/awt-dev/2017-January/012507.html ), and retrieved on
 * 2018-09-17. The source files in question are as follows:
 *   com.intellij.ui.components.SmoothScrollPane (author Pavel Fatin)
 *   com.intellij.ui.components.JBScrollPane
 *
 * The original code has been modified by Eirik Bakke (ebakke@ultorg.com) for standalone use outside
 * the IntelliJ IDE. In particular, we use here a custom ScrollPaneUI implementation instead of the
 * original approach of accessing the mouseScrollListener by reflection.
 *
 * The complete license header for the original files (identical for both files) is included above.
 * The NOTICE file is pasted in licenseinfo.xml.
 */
package org.netbeans.swing.plaf.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;

/**
 * Scroll pane UI that can handle high-precision mouse wheel events. Although Java 7 introduced
 * {@link MouseWheelEvent#getPreciseWheelRotation()} method, {@link BasicScrollPaneUI} does not yet
 * use it, which causes the following problems on Windows:
 *
 * <ul>
 *   <li>When the user has configured a wheel scroll speed higher than "1 line per notch" in the
 *       Windows control panel, the NetBeans editor (and other scroll panes) can only be scrolled in
 *       large discrete jumps. This happens both with a trackpad and an external mouse. Setting the
 *       scroll speed to "1 line per notch" is an undesirable workaround, as it makes scrolling slow
 *       on every application on the machine, including smooth-scrolling ones such as Chrome.
 *   <li>When scrolling in code completion and documentation popups, the editor underneath scrolls
 *       as well, due to {@code BasicScrollPaneUI} not disposing the event correctly when
 *       {@MouseWheelEvent.getWheelRotation()} returns zero (e.g. with
 *       {@code preciseWheelRotation=0.25}).
 * </ul>
 *
 * <p>Tested on Java 8 and Java 10.0.2 on Windows 10 on a Lenovo X1 Carbon 6th generation laptop,
 * with both the trackpad and an external USB mouse.
 *
 * @see BasicScrollPaneUI.Handler#mouseWheelMoved(MouseWheelEvent)
 * @see javax.swing.plaf.basic.BasicScrollBarUI#scrollByUnits
 */
public class SmoothScrollPaneUI extends BasicScrollPaneUI {
    private static final double EPSILON = 1e-5d;

    private SmoothScrollPaneUI() { }

    // Will be called reflectively by the UI manager.
    public static ComponentUI createUI(JComponent x) {
        return new SmoothScrollPaneUI();
    }

    @Override
    protected MouseWheelListener createMouseWheelListener() {
        final MouseWheelListener delegate = super.createMouseWheelListener();
        return new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent evt) {
                handleMouseWheelEvent(evt, delegate);
            }
        };
    }

    private void handleMouseWheelEvent(MouseWheelEvent evt, MouseWheelListener delegate) {
        if (scrollpane.isWheelScrollingEnabled() &&
            evt.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
        {
            mouseWheelMoved(evt);
            evt.consume();
        } else {
            delegate.mouseWheelMoved(evt);
        }
    }

    private void mouseWheelMoved(MouseWheelEvent e) {
        /* The code in this method is taken directly from Pavel Fatin's original IntelliJ patch.
        Some formatting changes have been applied. */
        /* The shift modifier will be enabled for horizontal touchbar scroll events, even when the
        actual shift key is not pressed, on both Windows and MacOS (though not on Java 8 on
        Windows). */
        JScrollBar scrollbar = e.isShiftDown()
                ? scrollpane.getHorizontalScrollBar() : scrollpane.getVerticalScrollBar();
        int orientation = scrollbar.getOrientation();
        JViewport viewport = scrollpane.getViewport();
        if (viewport == null || !(viewport.getView() instanceof Scrollable)) {
            return;
        }
        Scrollable view = (Scrollable) viewport.getView();
        double rotation = e.getPreciseWheelRotation();
        /* Use (0, 0) view position to obtain constant unit increment (which might otherwise be
        variable on smaller-than-unit scrolling). */
        Rectangle r = new Rectangle(new Point(0, 0), viewport.getViewSize());
        int unitIncrement = view.getScrollableUnitIncrement(r, orientation, 1);
        double delta = rotation * e.getScrollAmount() * unitIncrement;
        boolean limitDelta = Math.abs(rotation) < 1.0d + EPSILON;
        int blockIncrement = view.getScrollableBlockIncrement(r, orientation, 1);
        double adjustedDelta = limitDelta
                ? Math.max(-(double) blockIncrement, Math.min(delta, (double) blockIncrement))
                : delta;
        int value = scrollbar.getValue();
        int newValue = Math.max(scrollbar.getMinimum(),
                Math.min((int) Math.round(value + adjustedDelta), scrollbar.getMaximum()));
        if (newValue != value) {
            scrollbar.setValue(newValue);
        }
    }
}
