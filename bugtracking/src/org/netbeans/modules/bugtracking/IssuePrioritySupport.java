/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
        return icons.toArray(new Image[icons.size()]);
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
