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
package org.netbeans.modules.gradle;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JLabel;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;

/**
 *
 * @author lkishalmi
 */
public class GradleProjectErrorNotifications {
    private final Set<Notification> notifictions = new HashSet<>();

    public synchronized void openNotification(String title, String problem, String details) {
        StringBuilder sb = new StringBuilder(details.length());
        sb.append("<html>");                                 //NOI18N
        String[] lines = details.split("\n");                //NOI18N
        for (String line : lines) {
            sb.append(line).append("<br/>");                 //NOI18N
        }
        Notification ntn = NotificationDisplayer.getDefault().notify(title,
                NbGradleProject.getWarningIcon(),
                new JLabel(problem),
                new JLabel(sb.toString()),
                NotificationDisplayer.Priority.LOW, NotificationDisplayer.Category.WARNING);
        notifictions.add(ntn);
    }

    public synchronized void clear() {
        Iterator<Notification> it = notifictions.iterator();
        while(it.hasNext()) {
            it.next().clear();
            it.remove();
        }
    }

    public static String bulletedList(Collection<? extends Object> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");                                   //NOI18N
        for (Object element : elements) {
            sb.append("<li>");                               //NOI18N
            String[] lines = String.valueOf(element).split("\n"); //NOI18N
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                sb.append(lineWrap(line, 78));
                if (i < lines.length - 1) {
                    sb.append("<br/>");                      //NOI18N
                }
            }
            sb.append("</li>");                              //NOI18N
        }
        sb.append("</ul>");                                  //NOI18N
        return sb.toString();
    }


    private static String lineWrap(String line, int maxCol) {
        StringBuilder sb = new StringBuilder(line.length());
        String[] parts = line.split(" ");                    //NOI18N
        int col = 0;
        String delim = "";                                   //NOI18N
        for (String part : parts) {
            if ((sb.length() > 0) && (col + part.length() > maxCol)) {
                sb.append("<br/>").append(part);             //NOI18N
                col = part.length();
            } else {
                sb.append(delim).append(part);
                col += delim.length() + part.length();
                delim = " ";                                 //NOI18N
            }
        }
        return sb.toString();
    }
}
