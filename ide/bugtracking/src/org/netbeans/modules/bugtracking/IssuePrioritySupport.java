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

package org.netbeans.modules.bugtracking;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bugtracking.spi.IssuePriorityInfo;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Tomas Stupka
 */
public class IssuePrioritySupport {
    private final Map<String, IssuePriorityInfo> mapping = new HashMap<String, IssuePriorityInfo>(5);
        
    private final HashMap<String, Integer> order = new HashMap<String, Integer>(5);

    private static final List<Image> icons = new ArrayList<Image>(5);
    private static final Image defaultIcon;
    static {
        defaultIcon = ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/task.png", true); // NOI18N

        icons.add(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/taskP1.png", true)); // NOI18N
        icons.add(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/taskP2.png", true)); // NOI18N
        icons.add(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/taskP3.png", true)); // NOI18N
        icons.add(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/taskP4.png", true)); // NOI18N
        icons.add(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/taskP5.png", true)); // NOI18N
    }

    public IssuePrioritySupport(IssuePriorityInfo[] pis) {
        for (int i = 0; i < pis.length; i++) {
            IssuePriorityInfo info = pis[i];
            mapping.put(info.getID(), info);
            order.put(info.getID(), i);
        }
    }

    public static Image getDefaultIcon() {
        return defaultIcon;
    }    

    public static Image[] getIcons() {
        return icons.toArray(new Image[0]);
    }
    
    private IssuePriorityInfo getInfo(String id) {
        return mapping != null ? mapping.get(id) : null;
    }

    public String getName(String id) {
        IssuePriorityInfo info = getInfo(id);
        String name = info != null ? info.getDisplayName() : null; 
        return name != null ? name : ""; // NOI18N
    }

    public Image getIcon(String id) {
        IssuePriorityInfo info = getInfo(id);
        Image icon = null;
        if(info != null) {
            icon = info.getIcon();
            if(icon == null) {
                Integer idx = order.get(id);
                icon = idx < icons.size() ? icons.get(idx) : getDefaultIcon();
            }
        } 
        if(icon == null) {
            icon = getDefaultIcon();
        }
        return icon;
    }
        
}
