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
package org.netbeans.lib.uihandler;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JButton;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach
 */
final class Decorations {
    
    private static final SimpleFormatter FORMATTER = new SimpleFormatter();
    
    static void decorate(LogRecord r, Decorable d) {
        if (r.getMessage() == null) {
            d.setName("Seq: " + r.getSequenceNumber());
        } else {
            d.setName(r.getMessage());
        }
        if (r.getResourceBundle() != null) {
            try {
                String msg = r.getResourceBundle().getString(r.getMessage());
                if (r.getParameters() != null) {
                    msg = MessageFormat.format(msg, r.getParameters());
                }
                d.setDisplayName(msg);
            } catch (MissingResourceException ex) {
                Logger.getAnonymousLogger().log(Level.INFO, null, ex);
            }
            
            
            try {
                String iconBase = r.getResourceBundle().getString(r.getMessage() + "_ICON_BASE"); // NOI18N
                d.setIconBaseWithExtension(iconBase);
            } catch (MissingResourceException ex) {
                // ok, use default
                d.setIconBaseWithExtension("org/netbeans/lib/uihandler/def.png");
            }
        }
        
        
        String htmlKey;
        if (r.getThrown() != null) {
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/exception.gif");
            htmlKey = "HTML_exception";
        }
        
        if ("UI_ACTION_BUTTON_PRESS".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(cutAmpersand(getParam(r, 4)));
            String thru = getParam(r, 1, String.class);
            if ((thru != null && thru.contains("Toolbar")) || getParam(r, 0, Object.class) instanceof JButton) {
                d.setIconBaseWithExtension("org/netbeans/lib/uihandler/toolbars.gif");
                htmlKey = "HTML_toolbar";
            } else if (thru != null && thru.contains("MenuItem")) {
                d.setIconBaseWithExtension("org/netbeans/lib/uihandler/menus.gif");
                htmlKey = "HTML_menu";
            }
        } else if ("UI_ACTION_KEY_PRESS".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(cutAmpersand(getParam(r, 4)));
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/key.png");
            htmlKey = "HTML_key";
        } else if ("UI_ACTION_EDITOR".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(cutAmpersand(getParam(r, 4)));
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/key.png");
            htmlKey = "HTML_key";
        } else if ("UI_ENABLED_MODULES".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(NbBundle.getMessage(Decorations.class, "MSG_EnabledModules"));
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/module.gif");
            htmlKey = null;
        } else if ("UI_DISABLED_MODULES".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(NbBundle.getMessage(Decorations.class, "MSG_DisabledModules"));
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/module.gif");
            htmlKey = null;
        } else if ("UI_USER_CONFIGURATION".equals(r.getMessage())){// NOI18N
            d.setDisplayName(NbBundle.getMessage(Decorations.class, "MSG_USER_CONFIGURATION"));
            htmlKey = null;
        }
        
        
        
        
        d.setShortDescription(FORMATTER.format(r));
        
    }

    private static <T> T getParam(LogRecord r, int index, Class<T> type) {
        if (r == null || r.getParameters() == null || r.getParameters().length <= index) {
            return null;
        }
        Object o = r.getParameters()[index];
        return type.isInstance(o) ? type.cast(o) : null;
    }
    private static String getParam(LogRecord r, int index) {
        Object[] arr = r.getParameters();
        if (arr == null || arr.length <= index || !(arr[index] instanceof String)) {
            return "";
        }
        return (String)arr[index];
    }
    static String cutAmpersand(String text) {
        // XXX should this also be deprecated by something in Mnemonics?
        int i;
        String result = text;

        /* First check of occurence of '(&'. If not found check
          * for '&' itself.
          * If '(&' is found then remove '(&??'.
          */
        i = text.indexOf("(&"); // NOI18N

        if ((i >= 0) && ((i + 3) < text.length()) && /* #31093 */
                (text.charAt(i + 3) == ')')) { // NOI18N
            result = text.substring(0, i) + text.substring(i + 4);
        } else {
            //Sequence '(&?)' not found look for '&' itself
            i = text.indexOf('&');

            if (i < 0) {
                //No ampersand
                result = text;
            } else if (i == (text.length() - 1)) {
                //Ampersand is last character, wrong shortcut but we remove it anyway
                result = text.substring(0, i);
            } else {
                //Remove ampersand from middle of string
                //Is ampersand followed by space? If yes do not remove it.
                if (" ".equals(text.substring(i + 1, i + 2))) {
                    result = text;
                } else {
                    result = text.substring(0, i) + text.substring(i + 1);
                }
            }
        }

        return result;
    }
}
