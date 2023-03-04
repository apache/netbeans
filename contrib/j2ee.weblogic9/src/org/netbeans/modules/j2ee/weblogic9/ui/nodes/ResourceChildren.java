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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class ResourceChildren extends WLNodeChildren<ResourceNode> {

    ResourceChildren(Lookup lookup) {
        setKeys(new ResourceNode[] { 
                createJDBCNode(lookup),
                createConnectorsNode(lookup),
                createJavaMail(lookup),
                createLibraries(lookup)});
    } 

    private ResourceNode createConnectorsNode( Lookup lookup ) {
        return new ResourceNode( new ConnectorsChildren(lookup), ResourceNodeType.CONNECTORS,
                NbBundle.getMessage(ResourceChildren.class, "LBL_Connectors") );
    }

    private ResourceNode createJDBCNode( Lookup lookup ) {
        return new ResourceNode(new JdbcChildren(lookup) , ResourceNodeType.JDBC,
                NbBundle.getMessage(ResourceChildren.class, "LBL_JDBC"));   // NOI18N
    }
    
    private ResourceNode createJavaMail( Lookup lookup ) {
        return new ResourceNode(new JavaMailChildrenFactory(lookup) , 
                ResourceNodeType.JAVA_MAIL,
                    NbBundle.getMessage(ResourceChildren.class, "LBL_JavaMail"));   // NOI18N
    }
    
    private ResourceNode createLibraries( Lookup lookup ) {       
        // TODO proxy for LibrariesChildrenFactory ?
        return new ResourceNode(new LibrariesChildrenFactory(lookup), ResourceNodeType.LIBRARY,
                NbBundle.getMessage(ResourceChildren.class, "LBL_Libraries"));   // NOI18N
    }
    
    private ResourceNode createTuxedoResources( Lookup lookup ){
        return new ResourceNode ( new TuxedoChildren( lookup ) , 
                ResourceNodeType.TUXEDO, 
                    NbBundle.getMessage(ResourceChildren.class, "LBL_Interoperability"));   // NOI18N
    }
}
