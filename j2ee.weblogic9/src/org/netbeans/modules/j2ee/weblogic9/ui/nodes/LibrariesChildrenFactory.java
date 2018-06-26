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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;


/**
 * @author ads
 *
 */
public class LibrariesChildrenFactory extends ChildFactory<ResourceNode>
        implements RefreshModulesCookie {

    private static final Logger LOGGER = Logger.getLogger(LibrariesChildrenFactory.class.getName());

    private final Lookup lookup;

    LibrariesChildrenFactory(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void refresh() {
        refresh(false);
    }

    @Override
    protected boolean createKeys(List<ResourceNode> nodes) {
        WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

        WLConnectionSupport support = manager.getConnectionSupport();
        try {
            Map<String,String> libraries = support.executeAction(
                    new WLConnectionSupport.JMXRuntimeAction<Map<String,String>>() {

                @Override
                public Map<String,String> call( MBeanServerConnection con,
                        ObjectName service) throws Exception {
                    Map<String,String> map = new HashMap<String, String>();

                    ObjectName domainConfig = (ObjectName) con
                            .getAttribute(service,
                                    "DomainConfiguration"); // NOI18N

                    fillLibraries( con, domainConfig,
                            "Libraries", map);              // NOI18N
                    fillLibraries( con, domainConfig,
                            "InternalLibraries", map);      // NOI18N
                    fillLibraries( con, domainConfig,
                            "InternalAppDeployments", map); // NOI18N
                    fillLibraries( con, domainConfig,
                            "AppDeployments", map);         // NOI18N
                    return map;
                }
            });
            for (Entry<String, String > entry : libraries.entrySet()) {
                String path = entry.getKey();
                String name = entry.getValue();
                nodes.add(new ResourceNode(Children.LEAF, ResourceNodeType.LIBRARY,
                        name, path ));
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return true;
    }

    private void fillLibraries(MBeanServerConnection connection, ObjectName domainConfig,
            String attrName, Map<String,String> libraries)
        throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        ObjectName beans[] = (ObjectName[]) connection.getAttribute(domainConfig, attrName);
        for (ObjectName bean : beans) {
            String type = connection.getAttribute(bean, "Type").toString(); // NOI18N
            if ("Library".equals( type )) { // NOI18N
                String name = connection.getAttribute(bean, "Name").toString(); // NOI18N
                Object path = connection.getAttribute(bean, "AbsoluteInstallDir"); // NOI18N
                if (path == null) {
                    path = connection.getAttribute(bean, "AbsoluteSourcePath");      // NOI18N
                }
                if (path == null) {
                    path = name ;
                }
                libraries.put(path.toString(), name);
            }
        }
    }

    @Override
    protected Node createNodeForKey( ResourceNode key ) {
        return key;
    }

}
