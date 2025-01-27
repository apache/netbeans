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

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.Color;
import javax.swing.UIManager;


/**
 *
 * @author peterw99
 */
public class Util {

    private Util() {
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
