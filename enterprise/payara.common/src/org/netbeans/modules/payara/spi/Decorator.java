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

package org.netbeans.modules.payara.spi;

import java.awt.Image;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Peter Williams
 * @author Gaurav Gupta
 */
public abstract class Decorator {

    public static final String DISABLED = "disabled ";

    public static final Image DISABLED_BADGE =
            ImageUtilities.loadImage("org/netbeans/modules/payara/common/resources/disabled-badge.gif"); // NOI18N
    /**
     * Returns the badge to be used to decorate the default icon for this node.
     * 
     * @return badge to apply to default icon for this node.
     */
    public Image getIconBadge() {
        return null;
    }

    /**
     * Find an icon for this node (closed).
     * 
     * @param type constant from BeanInfo
     * @return icon used to represent the node
     */
    public Image getIcon(int type) {
        return null;
    }
    
    /**
     * Find an icon for this node (opened).
     * 
     * @param type constant from BeanInfo
     * @return icon used to represent the node
     */
    public Image getOpenedIcon(int type) {
        return null;
    }
    
    /**
     * Should a refresh action be displayed for this node.
     * 
     * @return true if refresh is supported.
     */
    public boolean isRefreshable() {
        return false;
    }
    
    /**
     * Can this node be deployed to?
     * 
     * @return true if deploy is supported (typically folder or instance nodes only.)
     */
    public boolean canDeployTo() {
        return false;
    }
    
    /**
     * Can this node be undeployed?
     * 
     * @return true if undeploy is supported.
     */
    public boolean canUndeploy() {
        return false;
    }

    /**
     * Can this node be undeployed?
     * 
     * @return true if undeploy is supported.
     */
    public boolean canUnregister() {
        return false;
    }

    /**
     * Can this node be executed to show a browser page?
     * 
     * @return true if can be shown by a browser.
     */
    public boolean canShowBrowser() {
        return false;
    }
    
    /**
     * Can the user edit details about this objects config on the server?
     *
     * @return true if the object has a customizer dialog
     */
    public boolean canEditDetails() {
        return false;
    }

    /**
     * Can this node be enabled?
     *
     * @return true if enable is supported.
     */
    public boolean canEnable() {
        return false;
    }

    /**
     * Can this node be disabled?
     *
     * @return true if disable is supported.
     */
    public boolean canDisable() {
        return false;
    }

    /**
     * Can CDI Probe mode be enabled?
     *
     * @return true if CDI Probe mode enable is supported.
     */
    public boolean canCDIProbeEnable() {
        return false;
    }

    /**
     * Can CDI Probe mode be disabled?
     *
     * @return true if CDI Probe mode disable is supported.
     */
    public boolean canCDIProbeDisable() {
        return false;
    }

    /**
     * Can this node be tested?
     *
     * @return true if test is supported.
     */
    public boolean canTest() {
        return false;
    }

    /**
     * Can this node be copied?
     *
     * @return true if copy is supported.
     */
    public boolean canCopy() {
        return false;
    }

}
