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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import javax.swing.Action;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.*;


/**
 * Instance node is a base for any manager node. The behaviour of this base instance 
 * node can be customized/extended by the manager node provided by the plugin.
 *
 * @author George FinKlang
 */
public class InstanceNode extends AbstractNode implements ServerInstance.StateListener {
    
    protected ServerInstance instance;
    
    public InstanceNode(ServerInstance instance, boolean addStateListener) {
        super(new InstanceChildren(instance));
        this.instance = instance;
        setIconBase(instance.getServer().getIconBase());
        getCookieSet().add(instance);
        if (addStateListener) {
            instance.addStateListener(this);
        }
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public org.openide.nodes.Node.Cookie getCookie(Class type) {
        if (ServerInstance.class.isAssignableFrom(type)) {
            return instance;
        }
        return super.getCookie(type);
    }
    
    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
    
    // StateListener implementation -------------------------------------------
    
    public void stateChanged(int oldState, int newState) {
        if (instance.getServerState() != ServerInstance.STATE_WAITING
            && instance.getServerState() != ServerInstance.STATE_SUSPENDED) {
            setChildren(new InstanceChildren(instance));
            getChildren().getNodes(true);
        } else if (instance.getServerState() == ServerInstance.STATE_SUSPENDED) {
            setChildren(Children.LEAF);
        }
    }
    
     public static class InstanceChildren extends Children.Keys {
        ServerInstance serverInstance;
        public InstanceChildren(ServerInstance inst) {
            this.serverInstance = inst;
        }
        protected void addNotify() {
            setKeys(serverInstance.getTargets());
        }
        protected void removeNotify() {
            setKeys(java.util.Collections.EMPTY_SET);
        }
        protected org.openide.nodes.Node[] createNodes(Object obj) {
            ServerTarget child = (ServerTarget) obj;
            //return new Node[] { new TargetBaseNode(org.openide.nodes.Children.LEAF, child) };
            return new Node[] { serverInstance.getServer().
                                 getNodeProvider().createTargetNode(child) };
        }
    }
    
}
