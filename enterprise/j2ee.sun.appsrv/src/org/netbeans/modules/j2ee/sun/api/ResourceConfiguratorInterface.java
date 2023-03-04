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
/*
 * ResourceConfigurationInterface.java
 *
 * Created on August 13, 2005, 8:38 AM
 */
package org.netbeans.modules.j2ee.sun.api;

import java.io.File;
import java.util.HashSet;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 *
 * @author Nitya Doraisamy
 */
public interface ResourceConfiguratorInterface {

    public boolean isJMSResourceDefined(String jndiName, File dir);

    public void createJMSResource(String jndiName, String msgDstnType, String msgDstnName, String ejbName, File dir, String resourcesXmlName);
    
    public MessageDestination createJMSResource(String jndiName, MessageDestination.Type type, String ejbName, File dir, String resourcesXmlName);

    public void createJDBCDataSourceFromRef(String refName, String databaseInfo, File dir);

    public String createJDBCDataSourceForCmp(String beanName, String databaseInfo, File dir);
    
    public Datasource createDataSource(String jndiName, String url, String username, String password, String driver, File dir, String resourcesXmlName) throws DatasourceAlreadyExistsException;
    
    public HashSet getServerDataSources();  
    
    public HashSet getServerDestinations();  
    
    public HashSet getResources(File dir);   
    
    public HashSet getMessageDestinations(File dir);   
    
}
