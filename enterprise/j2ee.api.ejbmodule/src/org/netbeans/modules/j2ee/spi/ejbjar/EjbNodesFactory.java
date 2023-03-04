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

package org.netbeans.modules.j2ee.spi.ejbjar;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.nodes.Node;

/** 
 * This class should be implemented by a module that provides nodes
 * for EJBs based on the elements from ejb-jar.xml (J2EE DD API).
 *
 * @author Pavel Buzek
 */
public interface EjbNodesFactory {
   
    /** This can be used to identify the EJB container node.*/
    public static final String CONTAINER_NODE_NAME = "EJBS"; // NOI18N
    
    Node createSessionNode(String ejbClass, EjbJar ejbModule, Project project);
    Node createEntityNode(String ejbClass, EjbJar ejbModule, Project project);
    Node createMessageNode(String ejbClass, EjbJar ejbModule, Project project);
    
}
