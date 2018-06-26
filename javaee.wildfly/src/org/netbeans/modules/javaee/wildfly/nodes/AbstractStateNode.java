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