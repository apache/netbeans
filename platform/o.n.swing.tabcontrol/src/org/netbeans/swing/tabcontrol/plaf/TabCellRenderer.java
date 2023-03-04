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
 * TabCellRenderer.java
 *
 * Created on December 2, 2003, 6:25 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabData;

import javax.swing.*;
import java.awt.*;

/**
 * Interface for a component that can render a tab and handle other aspects of
 * its functionality. Implementing this interface is the main aspect of implementing
 * an additional look and feel.
 * <p>
 * Note:  The component returned by getRendererComponent() should be capable of
 * painting itself properly without needing to be added into the AWT hierarchy - it
 * will be painted directly with a call to its paint() method, not by using
 * SwingUtilities.paintComponent(), for performance reasons.
 */
public interface TabCellRenderer {
    /**
     * Configures and returns a component which can appropriately paint the
     * passed tab in its current state
     */
    public JComponent getRendererComponent(TabData data, Rectangle bounds,
                                           int state);

    /**
     * Get a command string which is identified with a region of the tab cell
     * renderer, such as TabbedContainer.COMMAND_CLOSE if the point is inside the
     * close button, or TabbedContainer.COMMAND_SELECT if the point is over the
     * tab title.  The command returned should be the one that would be executed
     * were the default mouse button to be pressed.  Primarily this method is used
     * to determine what, if any, visual feedback to show to the user as the mouse
     * is moved over tabs.
     *
     * @param p A point (presumably) within the bounds of the passed rectangle
     * @param tabState The state of the tab, such as selected, clip right, etc.
     * @param bounds The bounds of the tab, defining the coordinate space in which
     *        the point parameter should be evaluated
     * @return A command string.  TabDisplayer/TabbedContainer define a small number,
     *        but other UIs can define whatever private contract they want.  If the
     *        action is performed, an action event with this string will be fired
     *        from the tabbed container, so client code can handle the actual work
     *        of the action as needed
     */
    public String getCommandAtPoint (Point p, int tabState, Rectangle bounds);


    /**
     * Get a command string which is identified with a region of the tab cell
     * renderer, such as TabbedContainer.COMMAND_CLOSE if the point is inside the
     * close button, or TabbedContainer.COMMAND_SELECT if the point is over the
     * tab title.
     *
     * Note that this method may return a different result than
     * <code>getCommandAtPoint (Point p, int tabState, Rectangle bounds)</code> -
     * that method is primarily used for painting logic (such as whether to
     * draw a rectangle around a close button);  this one is to determine the
     * actual action, if any to perform.  So it is free to return null, some
     * other action, etc.
     *
     * @param p A point (presumably) within the bounds of the passed rectangle
     * @param tabState The state of the tab, such as selected, clip right, etc.
     * @param bounds The bounds of the tab, defining the coordinate space in which
     *        the point parameter should be evaluated
     * @param mouseButton The mouse button used to produce the event, as defined in MouseEvent
     * @param eventType The event type, as defined in MouseEvent
     * @param modifiers The modifiers mask, as defined in MouseEvent
     *
     * @return A command string or null
     */
    public String getCommandAtPoint (Point p, int tabState, Rectangle bounds, int mouseButton,
                                     int eventType, int modifiers);


    /**
     * Get the shape that represents those pixels actually occupied by the tab
     * on-screen.  This is used for determining whether a mouse event really
     * occured on a tab, and for painting.<p> <b>A note on painting of tab drag
     * and drop indication:  <code>AbstractTabsUI</code> contains generic
     * support for drawing drag and drop target indications. If want to use it
     * rather than write your own, you need to specify the polygon returned by
     * this method with the following point order:  The last two points in the
     * point array of the polygon <strong>must be the bottom left corner,
     * followed by the bottom right corner</strong>.  In other words, start at
     * the upper left corner when constructing the polygon, and end at the
     * bottom right corner, using no more than one point for the bottom left and
     * right corners:
     * <pre>
     * start here -->    /---------
     *                            |
     * finish here -->   ----------
     * </pre>
     */
    public Polygon getTabShape(int tabState, Rectangle bounds);

    /**
     * Returns the number of pixels this renderer wants added to the base width
     * and height of the icon and text to fit decorations such as close butons,
     * drag indicators, etc.  This method is called only <strong>once</strong>,
     * during initialization, if this renderer is the default renderer created
     * by <code>TabsUI.createDefaultRenderer()</code>. The values it returns
     * cannot be changed dynamically. The result will be passed as padX/padY
     * arguments to the constructor of DefaultTabLayoutModel (unless
     * createLayoutModel is overridden with some custom model - then all bets
     * are off).
     */
    public Dimension getPadding();

    /**
     * Returns the number of pixels to be added to the width of a cell if the
     * cell is the selected index.  This method is also called only once, on
     * initialization, for the default renderer
     */
    public int getPixelsToAddToSelection();

    public Color getSelectedBackground(); //XXX delete me

    public Color getSelectedActivatedBackground(); //XXX delete me

    public boolean isShowCloseButton();
    
    public void setShowCloseButton(boolean val);
}
