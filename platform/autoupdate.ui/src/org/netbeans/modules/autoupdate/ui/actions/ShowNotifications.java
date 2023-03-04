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
package org.netbeans.modules.autoupdate.ui.actions;

import org.netbeans.api.autoupdate.UpdateUnitProvider;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Dmitry Lipin
 */
public class ShowNotifications {

    private static final Logger LOGGER = Logger.getLogger(ShowNotifications.class.getName());
    private static final String SEPARATOR = " ";
    private static final String PROP = "shown-notifications";
    private static final Map<String, Notification> notifications = new HashMap<String, Notification>();

    public static void checkNotification(UpdateUnitProvider p) {
        LOGGER.finest("Checking notification for " + p.getDisplayName());
        String m = getMessage(p);
        if (m != null) {
            LOGGER.finest("Check message " + m.hashCode() + " to show... \n" + m + "\n");
            if (add(m)) {
                LOGGER.finest("Show message " + m.hashCode());
                showNotification(m, p);
            } else {
                LOGGER.finest("Message " + m.hashCode() + "... was already shown");
            }
        }
    }

    private static String getMessage(UpdateUnitProvider p) {
        String desc = p.getDescription();
        if (desc != null) {
            String token = "<a name=\"autoupdate_catalog_parser\"";
            if (desc.indexOf(token) != -1) {
                return desc;
            }
        }
        return null;
    }

    private static boolean add(String notification) {
        synchronized (ShowNotifications.class) {
            final Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate/ui"); // NOI18N
            List<String> alreadyShown = new ArrayList<String>();
            for (String s : p.get(PROP, "").split(SEPARATOR)) {
                if (s.length() > 0) {
                    alreadyShown.add(s);
                }
            }
            boolean add = true;
            String hashCode = "" + notification.hashCode();

            for (String s : alreadyShown) {
                if (hashCode.equals(s)) {
                    add = false;
                }
            }
            if (add) {
                alreadyShown.add(hashCode);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < alreadyShown.size(); i++) {
                    if (i != 0) {
                        sb.append(SEPARATOR);
                    }
                    sb.append(alreadyShown.get(i));
                }
                p.put(PROP, sb.toString());
            }

            return add;
        }
    }

    private static void showNotification(String notification, UpdateUnitProvider p) {
        if (notifications.get(p.getName()) != null) {
            notifications.get(p.getName()).clear();
        }
        Notification n = NotificationDisplayer.getDefault().notify(
                getTitle(notification),
                ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/info_icon.png", false),
                createInfoPanel(notification), createInfoPanel(notification),
                NotificationDisplayer.Priority.HIGH);

        notifications.put(p.getName(), n);
    }

    private static String getTitle(String notification) {
        return getMessagePart(notification, true);
    }

    private static String getDetails(String notification) {
        return getMessagePart(notification, false);
    }

    private static String getMessagePart(String message, boolean leading) {
        String title = message;
        //crop leading new line characters
        String[] sep = {"\r\n", "\r", "\n", "<br>", "<br/>"};
        for(String s : sep) {
            if(title.indexOf(s)==0) {
                title = getMessagePart(title.substring(s.length()), leading);
            }
        }

        String sp = null;
        int idx = -1;
        for(String s : sep) {
            int i = title.indexOf(s);
            if(i!=-1) {
                if(idx==-1 || i < idx) {
                    sp = s;
                    idx = i;
                }
            }
        }

        if(sp==null) {
            return leading ? title : "";
        } else {
            return leading ? title.substring(0, idx) : title.substring(idx + sp.length());
        }
    }

    

    private static JTextPane createInfoPanel(String notification) {
        JTextPane balloon = new JTextPane();
        balloon.setContentType("text/html");
        String text = getDetails(notification).replaceAll("(\r\n|\n|\r)", "<br>");
        balloon.setText(text);
        balloon.setOpaque(false);
        balloon.setEditable(false);
        balloon.setBorder(new EmptyBorder(0, 0, 0, 0));


        if (UIManager.getLookAndFeel().getID().equals("Nimbus")) {
            //#134837
            //http://forums.java.net/jive/thread.jspa?messageID=283882
            balloon.setBackground(new Color(0, 0, 0, 0));
        }

        balloon.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    URLDisplayer.getDefault().showURL(e.getURL());
                }
            }
        });
        return balloon;
    }
}

