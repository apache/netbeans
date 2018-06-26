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

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.openide.ErrorManager;


/**
 *
 * @author peterw99
 */
public class Util {

    private Util() {
    }

    /** Error/warning message icons.
     */
    private static final String errorIconPath =
            "org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/resources/errorIcon.png"; // NOI18N
    private static final String warningIconPath =
            "org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/resources/warningIcon.png"; // NOI18N

    public static ImageIcon errorMessageIcon;
    public static ImageIcon warningMessageIcon;

    static {
        // Diagnostic test code to try to get more information about a suspicious intermittant exception
        // (This is circa NB 4.1, so it may not be necessary anymore).
        try {
            errorMessageIcon = new ImageIcon(Utils.getResourceURL(errorIconPath, InputDialog.class));
        } catch(NullPointerException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            errorMessageIcon = null;
        }
        try {
            warningMessageIcon = new ImageIcon(Utils.getResourceURL(warningIconPath, InputDialog.class));
        } catch(NullPointerException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            warningMessageIcon = null;
        }
    }


    /** !PW Foreground color for error message text when in the NetBeans IDE.
     *  See http://ui.netbeans.org/docs/inline_errors/index.html, about
     *  halfway down, for specification. (Was light blue: [89, 79, 191])
     */
    private static final Object colorMonitor = new Object();
    private static Color errorTextForegroundColor = null;
    private static Color warningTextForegroundColor = null;

    public static Color getErrorForegroundColor() {
        Color result = null;

        synchronized(colorMonitor) {
            if(errorTextForegroundColor == null) {
                errorTextForegroundColor = UIManager.getColor("nb.errorForeground");
                if(errorTextForegroundColor == null) {
                    errorTextForegroundColor = new Color(89, 79, 191); // See http://ui.netbeans.org/docs/inline_errors/index.html
                }
            }

            result = errorTextForegroundColor;
        }

        return result;
    }

    public static Color getWarningForegroundColor() {
        Color result = null;

        synchronized(colorMonitor) {
            if(warningTextForegroundColor == null) {
                warningTextForegroundColor = UIManager.getColor("Label.foreground");
                if(warningTextForegroundColor == null) {
                    warningTextForegroundColor = new Color(0,0,0); // black
                }
            }

            result = warningTextForegroundColor;
        }

        return result;
    }

}
