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

package org.netbeans.modules.javaee.project.api.ui.utils;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Provides methods to set warning or error message to JLabel, properly
 * decorating it with icon and font color.
 *
 * @author Petr Hejl
 */
public final class MessageUtils {

    private MessageUtils() {
        super();
    }

    /**
     * Sets the message to the given label.
     *
     * @param label label where the message will be displayed
     * @param type type of the message
     * @param message the message to display
     */
    public static void setMessage(JLabel label, MessageType type, String message) {
        Parameters.notNull("type", type);
        Parameters.notNull("message", message);

        label.setForeground(type.getColor());
        setMessage(label, type.getIcon(), message);
    }

    /**
     * Clears the message placed in the JLabel.
     *
     * @param label label to clear
     */
    public static void clear(JLabel label) {
        setMessage(label, (Icon) null, (String) null);
    }

    private static void setMessage(JLabel label, Icon icon, String message) {
        label.setIcon(message == null ? null : icon);
        label.setText(message);
        label.setToolTipText(message);
    }

    /**
     * The type of the message.
     */
    public static enum MessageType {

        /**
         * Error message.
         */
        ERROR  {
            protected Icon getIcon() {
                return ImageUtilities.loadImageIcon("org/netbeans/modules/javaee/project/ui/resources/error.gif", false); // NOI18N
            }

            protected Color getColor() {
                Color errorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
                if (errorForeground == null) {
                    errorForeground = new Color(255, 0, 0);
                }
                return errorForeground;
            }
        },

        /**
         * Warning message.
         */
        WARNING {
            protected Icon getIcon() {
                return ImageUtilities.loadImageIcon("org/netbeans/modules/javaee/project/ui/resources/warning.gif", false); // NOI18N
            }

            protected Color getColor() {
                Color warningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
                if (warningForeground == null) {
                    warningForeground = new Color(51, 51, 51);
                }
                return warningForeground;
            }
        };

        /**
         * Returns the icon representing this message type.
         *
         * @return the icon representing this message type
         */
        protected abstract Icon getIcon();

        /**
         * Returns the font color for this message type.
         *
         * @return the font color for this message type
         */
        protected abstract Color getColor();

    }
}
