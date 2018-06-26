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
/*
 * Resources.java
 *
 * Created on November 21, 2004, 2:47 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;

import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;

/**
 * @author Nitya Doraisamy
 */
public interface Resources extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {
    
        public static final String VERSION_1_0 = "1.00"; //NOI18N
        public static final String VERSION_1_2 = "1.20"; //NOI18N
        public static final String VERSION_1_3 = "1.30"; //NOI18N
        public static final String VERSION_1_5 = "1.50"; //NOI18N
        
        public static final String CUSTOM_RESOURCE = "CustomResource";	// NOI18N
	public static final String EXTERNAL_JNDI_RESOURCE = "ExternalJndiResource";	// NOI18N
	public static final String JDBC_RESOURCE = "JdbcResource";	// NOI18N
	public static final String MAIL_RESOURCE = "MailResource";	// NOI18N
	public static final String PERSISTENCE_MANAGER_FACTORY_RESOURCE = "PersistenceManagerFactoryResource";	// NOI18N
	public static final String ADMIN_OBJECT_RESOURCE = "AdminObjectResource";	// NOI18N
	public static final String CONNECTOR_RESOURCE = "ConnectorResource";	// NOI18N
	public static final String RESOURCE_ADAPTER_CONFIG = "ResourceAdapterConfig";	// NOI18N
	public static final String JDBC_CONNECTION_POOL = "JdbcConnectionPool";	// NOI18N
	public static final String CONNECTOR_CONNECTION_POOL = "ConnectorConnectionPool";	// NOI18N
	public static final String JMS_RESOURCE = "JmsResource";	// NOI18N
        
	public void setCustomResource(int index, CustomResource value); 
	public CustomResource getCustomResource(int index);
	public int sizeCustomResource();
	public void setCustomResource(CustomResource[] value);
	public CustomResource[] getCustomResource();
	public int addCustomResource(CustomResource value);
	public int removeCustomResource(CustomResource value);
	public CustomResource newCustomResource();

	public void setExternalJndiResource(int index, ExternalJndiResource value);
	public ExternalJndiResource getExternalJndiResource(int index);
	public int sizeExternalJndiResource();
	public void setExternalJndiResource(ExternalJndiResource[] value);
	public ExternalJndiResource[] getExternalJndiResource();
	public int addExternalJndiResource(ExternalJndiResource value);
	public int removeExternalJndiResource(ExternalJndiResource value);
	public ExternalJndiResource newExternalJndiResource();

	public void setJdbcResource(int index, JdbcResource value);
	public JdbcResource getJdbcResource(int index);
	public int sizeJdbcResource();
	public void setJdbcResource(JdbcResource[] value);
	public JdbcResource[] getJdbcResource();
	public int addJdbcResource(JdbcResource value);
	public int removeJdbcResource(JdbcResource value);
	public JdbcResource newJdbcResource();

	public void setMailResource(int index, MailResource value);
	public MailResource getMailResource(int index);
	public int sizeMailResource();
	public void setMailResource(MailResource[] value);
	public MailResource[] getMailResource();
	public int addMailResource(MailResource value);
	public int removeMailResource(MailResource value);
	public MailResource newMailResource();

	public void setPersistenceManagerFactoryResource(int index, PersistenceManagerFactoryResource value);
	public PersistenceManagerFactoryResource getPersistenceManagerFactoryResource(int index);
	public int sizePersistenceManagerFactoryResource();
	public void setPersistenceManagerFactoryResource(PersistenceManagerFactoryResource[] value);
	public PersistenceManagerFactoryResource[] getPersistenceManagerFactoryResource();
	public int addPersistenceManagerFactoryResource(PersistenceManagerFactoryResource value);
	public int removePersistenceManagerFactoryResource(PersistenceManagerFactoryResource value);
	public PersistenceManagerFactoryResource newPersistenceManagerFactoryResource();

	public void setAdminObjectResource(int index, AdminObjectResource value);
	public AdminObjectResource getAdminObjectResource(int index);
	public int sizeAdminObjectResource();
	public void setAdminObjectResource(AdminObjectResource[] value);
	public AdminObjectResource[] getAdminObjectResource();
	public int addAdminObjectResource(AdminObjectResource value);
	public int removeAdminObjectResource(AdminObjectResource value);
	public AdminObjectResource newAdminObjectResource();

	public void setConnectorResource(int index, ConnectorResource value);
	public ConnectorResource getConnectorResource(int index);
	public int sizeConnectorResource();
	public void setConnectorResource(ConnectorResource[] value);
	public ConnectorResource[] getConnectorResource();
	public int addConnectorResource(ConnectorResource value);
	public int removeConnectorResource(ConnectorResource value);
	public ConnectorResource newConnectorResource();

	public void setResourceAdapterConfig(int index, ResourceAdapterConfig value);
	public ResourceAdapterConfig getResourceAdapterConfig(int index);
	public int sizeResourceAdapterConfig();
	public void setResourceAdapterConfig(ResourceAdapterConfig[] value);
	public ResourceAdapterConfig[] getResourceAdapterConfig();
	public int addResourceAdapterConfig(ResourceAdapterConfig value);
	public int removeResourceAdapterConfig(ResourceAdapterConfig value);
	public ResourceAdapterConfig newResourceAdapterConfig();

	public void setJdbcConnectionPool(int index, JdbcConnectionPool value);
	public JdbcConnectionPool getJdbcConnectionPool(int index);
	public int sizeJdbcConnectionPool();
	public void setJdbcConnectionPool(JdbcConnectionPool[] value);
	public JdbcConnectionPool[] getJdbcConnectionPool();
	public int addJdbcConnectionPool(JdbcConnectionPool value);
	public int removeJdbcConnectionPool(JdbcConnectionPool value);
	public JdbcConnectionPool newJdbcConnectionPool();

	public void setConnectorConnectionPool(int index, ConnectorConnectionPool value);
	public ConnectorConnectionPool getConnectorConnectionPool(int index);
	public int sizeConnectorConnectionPool();
	public void setConnectorConnectionPool(ConnectorConnectionPool[] value);
	public ConnectorConnectionPool[] getConnectorConnectionPool();
	public int addConnectorConnectionPool(ConnectorConnectionPool value);
	public int removeConnectorConnectionPool(ConnectorConnectionPool value);
	public ConnectorConnectionPool newConnectorConnectionPool();

//	public void setJmsResource(int index, JmsResource value);
//	public JmsResource getJmsResource(int index);
//	public int sizeJmsResource();
//	public void setJmsResource(JmsResource[] value);
//	public JmsResource[] getJmsResource();
//	public int addJmsResource(JmsResource value);
//	public int removeJmsResource(JmsResource value);
//	public JmsResource newJmsResource();
        
        public void write(java.io.File f) throws java.io.IOException, Schema2BeansRuntimeException;
}
