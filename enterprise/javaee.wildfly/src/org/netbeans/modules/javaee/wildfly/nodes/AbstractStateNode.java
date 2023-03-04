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
package org.netbeans.modules.javaee.wildfly.nodes;

import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public abstract class AbstractStateNode extends AbstractNode {

    public static final String WAITING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/waiting.png"; // NOI18N
    public static final String RUNNING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/running.png"; // NOI18N
    public static final String DEBUGGING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/debugging.png"; // NOI18N
    public static final String SUSPENDED_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/suspended.png"; // NOI18N
    public static final String PROFILING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/profiling.png"; // NOI18N
    public static final String PROFILER_BLOCKING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/profilerblocking.png"; // NOI18N

    public AbstractStateNode(Children children) {
        super(children);
    }

    public AbstractStateNode(Children children, Lookup lookup) {
        super(children, lookup);
    }

    protected abstract Image getOriginalIcon(int type);

    protected abstract Image getOriginalOpenedIcon(int type);

    protected abstract boolean isRunning();

    protected abstract boolean isWaiting();

    @Override
    public Image getIcon(int type) {
        return badgeIcon(getOriginalIcon(type));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(getOriginalOpenedIcon(type));
    }

    // private helper methods -------------------------------------------------
    private Image badgeIcon(Image origImg) {
        Image badge = null;
        if (isWaiting()) {
            badge = ImageUtilities.loadImage(WAITING_ICON);
        } else if (isRunning()) {
            badge = ImageUtilities.loadImage(RUNNING_ICON);
        }
        /*  case ServerInstance.STATE_DEBUGGING :
         badge = ImageUtilities.loadImage(DEBUGGING_ICON);
         break;
         case ServerInstance.STATE_SUSPENDED :
         badge = ImageUtilities.loadImage(SUSPENDED_ICON);
         break;
         case ServerInstance.STATE_PROFILING :
         badge = ImageUtilities.loadImage(PROFILING_ICON);
         break;
         case ServerInstance.STATE_PROFILER_BLOCKING :
         badge = ImageUtilities.loadImage(PROFILER_BLOCKING_ICON);
         break;
         case ServerInstance.STATE_PROFILER_STARTING :
         badge = ImageUtilities.loadImage(WAITING_ICON);
         break;
         default:
         break;*/
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }
}