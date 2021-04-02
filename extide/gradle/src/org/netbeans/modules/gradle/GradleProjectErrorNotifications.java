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
        sb.append("<html>");
        String[] lines = details.split("\n");
        for (String line : lines) {
            sb.append(line).append("<br/>");
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
        sb.append("<ul>");
        for (Object element : elements) {
            sb.append("<li>");
            String[] lines = element.toString().split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                sb.append(line);
                if (i < lines.length - 1) {
                    sb.append("<br/>");
                }
            }
            sb.append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }


}
