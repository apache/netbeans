/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
