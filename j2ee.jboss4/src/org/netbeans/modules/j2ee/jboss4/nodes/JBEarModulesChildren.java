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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.JBRemoteAction;
import org.netbeans.modules.j2ee.jboss4.JBoss5ProfileServiceProxy;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the enterprise application node.
 *
 * @author Michal Mocnak
 */
public class JBEarModulesChildren extends Children.Keys {

    private static final Logger LOGGER = Logger.getLogger(JBEarApplicationsChildren.class.getName());

    private Lookup lookup;
    private String j2eeAppName;
    
    public JBEarModulesChildren(Lookup lookup, String j2eeAppName) {
        this.lookup = lookup;
        this.j2eeAppName = j2eeAppName;
    }
    
    public void updateKeys(){
        setKeys(new Object[] {Util.WAIT_NODE});
        
        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();
            JBDeploymentManager dm = (JBDeploymentManager)lookup.lookup(JBDeploymentManager.class);
            
            public void run() {
                try {
                    dm.invokeRemoteAction(new JBRemoteAction<Void>() {

                        @Override
                        public Void action(MBeanServerConnection connection, JBoss5ProfileServiceProxy profileService) throws Exception {
                            // Query to the jboss4 server
                            ObjectName searchPattern = new ObjectName("jboss.management.local:J2EEApplication="+j2eeAppName+",*");
                            Method method = connection.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class});
                            method = Util.fixJava4071957(method);
                            Set managedObj = (Set) method.invoke(connection, new Object[] {searchPattern, null});

                            Iterator it = managedObj.iterator();

                            // Query results processing
                            while(it.hasNext()) {
                                try {
                                    ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                                    String name = elem.getKeyProperty("name");

                                    if(elem.getKeyProperty("j2eeType").equals("EJBModule"))
                                        keys.add(new JBEjbModuleNode(name, lookup));
                                    else if(elem.getKeyProperty("j2eeType").equals("WebModule")) {
                                        String url = "http://"+dm.getHost()+":"+dm.getPort();
                                        String descr = (String)Util.getMBeanParameter(connection, "jbossWebDeploymentDescriptor", elem.getCanonicalName());
                                        String context = Util.getWebContextRoot(descr, name);
                                        keys.add(new JBWebModuleNode(name, lookup, (context == null) ? null : url+context));
                                    }
                                } catch (Exception ex) {
                                    LOGGER.log(Level.INFO, null, ex);
                                }
                            }
                            return null;
                        }

                    });
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
                
                setKeys(keys);
            }
        }, 0);
        
    }
    
    protected void addNotify() {
        updateKeys();
    }
    
    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }
    
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof JBEjbModuleNode){
            return new Node[]{(JBEjbModuleNode)key};
        }
        
        if (key instanceof JBWebModuleNode){
            return new Node[]{(JBWebModuleNode)key};
        }
        
        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }
        
        return null;
    }
}
