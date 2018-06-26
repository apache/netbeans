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


package org.netbeans.tests.j2eeserver.plugin.registry;

import org.netbeans.tests.j2eeserver.plugin.jsr88.*;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
/**
 *
 * @author  nn136682
 */
public class NodeFactory implements org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory {

    /** Creates a new instance of NodeFactory */
    public NodeFactory() {
    }

    public org.openide.nodes.Node getFactoryNode(org.openide.util.Lookup lookup) {
        DeploymentFactory depFactory = (DeploymentFactory) lookup.lookup(DeploymentFactory.class);
        if (depFactory == null || ! (depFactory instanceof TestDeploymentFactory)) {
            System.out.println("WARNING: getFactoryNode lookup returned "+depFactory);
            return null;
        }
        System.out.println("INFO: getFactoryNode returning new plugin node");
        return new PluginNode((TestDeploymentFactory)depFactory);
    }
    
    public org.openide.nodes.Node getManagerNode(org.openide.util.Lookup lookup) {
        DeploymentManager depManager = (DeploymentManager) lookup.lookup(DeploymentManager.class);
        if (depManager == null || ! (depManager instanceof TestDeploymentManager)) {
            System.out.println("WARNING: getManagerNode lookup returned "+depManager);
            return null;
        }
        System.out.println("INFO: getManagerNode returning new Manager node");
        return new ManagerNode((TestDeploymentManager)depManager);
    }
    
    public org.openide.nodes.Node getTargetNode(org.openide.util.Lookup lookup) {
        Target target = (Target) lookup.lookup(Target.class);
        if (target == null || ! (target instanceof TestTarget) ) {
            System.out.println("WARNING: getTargetNode lookup returned "+target);
            return null;
        }
        System.out.println("INFO: getManagerNode returning new Target node");
        return new TargNode((TestTarget)target);
    }
}
