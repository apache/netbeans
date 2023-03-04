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
 *//*
 * TabData.java
 *
 * Created on May 26, 2003, 3:54 PM
 */

package org.netbeans.swing.tabcontrol;

import javax.swing.*;
import java.awt.*;

/**
 * Class representing data needed to represent a component in a tab. While
 * immutable to client code, changes to the data model may change the values
 * returned by the methods of this class.  TabData objects are the data
 * component of TabDataModel.
 * <p/>
 * TabData objects implement their <code>equals()</code> and
 * <code>hashCode</code> contract based on the equality of the user object and
 * the text.  The icon and the tooltip text are not considered when testing
 * equality.
 *
 * @author Tim Boudreau
 * @see TabDataModel
 * @see DefaultTabDataModel
 */
public final class TabData implements Comparable {
    //Fields below are intentionally package-private, not private.
    Icon icon;
    String txt;
    String tip;
    Object userObject;

    /**
     * Create a new TabData object.
     *
     * @param userObject The object or component that should be displayed when
     *                   the tab is selected.  For use in TabbedContainer, this
     *                   should be an instance of <code>java.awt.Component</code>
     * @param i          The icon for the tab
     * @param caption    The caption for the tab
     * @param tooltip    The tooltip for the tab
     */
    public TabData(Object userObject, Icon i, String caption, String tooltip) {
        this.userObject = userObject;
        icon = i;
        txt = caption;
        tip = tooltip;
    }
    
    
    public Object getUserObject() {
        return userObject;
    }

    /**
     * The component for the tab. Returns null if the user object is not an
     * instance of Component.
     *
     * @return The component
     */
    public Component getComponent() {
        if (userObject instanceof Component) {
            return (Component) userObject;
        } else {
            return null;
        }
    }

    /**
     * The icon for the tab.  Note that this method is guaranteed to return
     * non-null - if the icon specified is null, a 0-width, 0-height icon whose
     * paintIcon method is a no-op will be returned.
     *
     * @return The icon
     */
    public Icon getIcon() {
        if (icon == null) {
            return NO_ICON;
        } else {
            return icon;
        }
    }

    /**
     * The text for the tab
     *
     * @return The text
     */
    public String getText() {
        return txt;
    }

    /**
     * The tooltip for the tab
     *
     * @return The tooltip text
     */
    public String getTooltip() {
        return tip;
    }

    /**
     * Get a string representation of this object
     *
     * @return String representation of this object
     */
    public String toString() {
        return txt;
    }

    /**
     * Returns true if the text and component properties of this TabData object
     * match the passed one.  Tooltip and icon equality are not evaluated.
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof TabData) {
            TabData td = (TabData) o;
            boolean result = td.userObject.equals(userObject)
                    && td.txt.equals(txt);
            return result;
        } else {
            return false;
        }
    }

    /**
     * Munges the text and component hash codes
     */
    public int hashCode() {
        return (txt == null ? 0 : txt.hashCode())
                ^ (userObject == null ? 0 : userObject.hashCode());
    }

    /**
     * Compares the text based on java.lang.String's implementation of
     * Comparable.
     */
    public int compareTo(Object o) {
        String arg1, arg2;
        arg1 = getText();
        if (o instanceof TabData) {
            arg2 = ((TabData) o).getText();
        } else {
            arg2 = null;
        }
        if (arg2 == null) {
            if (arg1 == null) {
                // both with null-name, equal
                return 0;
            } else {
                // any name before null-name
                return 1;
            }
        } else {
            if (arg1 == null) {
                // null-name after any name
                return -1;
            } else {
                // compare by names
                return arg1.compareTo(arg2);
            }
        }
    }

    /**
     * An empty icon to be used when null is passed for the icon - internally we
     * don't support null icons, but Components may legally have them
     */
    static final Icon NO_ICON = new Icon() {
        public int getIconHeight() {
            return 0;
        }

        public int getIconWidth() {
            return 0;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public String toString() {
            return "empty icon";
        }; //NOI18N
    };
}
