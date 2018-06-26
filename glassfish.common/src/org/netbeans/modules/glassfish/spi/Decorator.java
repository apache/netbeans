/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.spi;

import java.awt.Image;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Peter Williams
 */
public abstract class Decorator {

    public static final String DISABLED = "disabled ";

    public static final Image DISABLED_BADGE =
            ImageUtilities.loadImage("org/netbeans/modules/glassfish/common/resources/disabled-badge.gif"); // NOI18N
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
