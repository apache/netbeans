/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.Target;

public class ServerString implements java.io.Serializable {

    private final String plugin;
    private final String instance;
    /** <i>NonNull</i> */
    private final String[] targets;
    private final transient ServerInstance serverInstance;
    private transient String[] theTargets;
    private static final long serialVersionUID = 923457209372L;

    protected ServerString(String plugin, String instance, String[] targets, ServerInstance serverInstance) {
        if (targets == null) {
            this.targets = new String[0];
        } else {
            this.targets = targets.clone();
        }
        this.plugin = plugin;
        this.instance = instance;
        this.serverInstance = serverInstance;
    }

    public ServerString(String plugin, String instance, String[] targets) {
        this(plugin, instance, targets, null);
    }

    public ServerString(ServerInstance instance) {
        this(instance.getServer().getShortName(), instance.getUrl(), null, instance);
    }

    public ServerString(ServerTarget target) {
        this(target.getInstance().getServer().getShortName(),
                target.getInstance().getUrl(), new String[] {target.getName()}, null);
    }

    public ServerString(ServerInstance instance, String targetName) {
        this(instance.getServer().getShortName(),
                instance.getUrl(),
                (targetName != null && ! "".equals(targetName.trim())) ? new String[] {targetName} : null,
                instance);
    }

    public String getPlugin() {
        return plugin;
    }

    public String getUrl() {
        return instance;
    }

    public String[] getTargets() {
        return getTargets(false);
    }

    /**
     * <i>This method can have ugly side effect of starting the server.</i>
     * 
     * @param concrete
     * @return
     */
    public String[] getTargets(boolean concrete) {
        if (!concrete || targets.length > 0) {
            return targets.clone();
        }

        if (theTargets != null) {
            return theTargets.clone();
        }

        ServerTarget[] serverTargets = getServerInstance().getTargets();
        theTargets = new String[serverTargets.length];
        for (int i = 0; i < theTargets.length; i++) {
            theTargets[i] = serverTargets[i].getName();
        }
        return theTargets.clone();
    }

    public Server getServer() {
        return ServerRegistry.getInstance().getServer(plugin);
    }

    public ServerInstance getServerInstance() {
        if (serverInstance != null) {
            return serverInstance;
        }
        return ServerRegistry.getInstance().getServerInstance(instance);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Server ").append(plugin); // NOI18N
        buffer.append(" Instance ").append(instance); // NOI18N
        if (/*targets == null || */targets.length == 0) {
            buffer.append(" Targets none"); // NOI18N
        } else {
            buffer.append(" Targets ").append(targets.length); // NOI18N
        }
        return buffer.toString();
    }

    /**
     * <i>This method can have ugly side effect of starting the server.</i>
     * 
     * @return
     */
    public Target[] toTargets() {
        String[] targetNames = getTargets(true);
        Target[] ret = new Target[targetNames.length];
        for (int i = 0; i < targetNames.length; i++) {
            ret[i] = getServerInstance().getServerTarget(targetNames[i]).getTarget();
        }
        return ret;
    }
}
