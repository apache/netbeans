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
package org.netbeans.modules.cloud.oracle;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * Represents a profile that can't connect to its tenancy. 
 */
public class BrokenProfileNode extends AbstractNode {
    
    private static final String ORCL_ICON = "org/netbeans/modules/cloud/oracle/resources/tenancy.svg"; // NOI18N
    private static final String BADGE_ICON = "org/netbeans/modules/cloud/oracle/resources/error-badge.svg"; // NOI18N
    
    private final TenancyInstance instance;
    
    public BrokenProfileNode(TenancyInstance instance) {
        super(Children.LEAF, Lookups.fixed(instance.profile));
        this.instance = instance;
        setName(instance.profile.getId()); 
        setDisplayName(instance.getDisplayName());
        setIconBaseWithExtension(ORCL_ICON);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> commonActions = OCINode.actionsForPath(
                "Cloud/Oracle/BrokenProfile/Actions", getLookup());
        List<Action> result = new ArrayList<>();
        for (Action commonAction : commonActions) {
            if (commonAction.isEnabled()) {
                result.add(commonAction);
            }
        }
        return result.toArray(new Action[0]);
    }
    
    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }
    
    private Image badgeIcon(Image origImg) {
        return ImageUtilities.mergeImages(origImg, ImageUtilities.loadImage(BADGE_ICON), 8, 8);
    }
}
