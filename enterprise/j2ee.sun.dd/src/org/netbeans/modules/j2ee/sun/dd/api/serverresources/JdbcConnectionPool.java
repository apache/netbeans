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
 * JdbcConnectionPool.java
 *
 * Created on November 21, 2004, 4:47 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface JdbcConnectionPool {

        public static final String NAME = "Name";	// NOI18N
	public static final String DATASOURCECLASSNAME = "DatasourceClassname";	// NOI18N
	public static final String RESTYPE = "ResType";	// NOI18N
	public static final String STEADYPOOLSIZE = "SteadyPoolSize";	// NOI18N
	public static final String MAXPOOLSIZE = "MaxPoolSize";	// NOI18N
	public static final String MAXWAITTIMEINMILLIS = "MaxWaitTimeInMillis";	// NOI18N
	public static final String POOLRESIZEQUANTITY = "PoolResizeQuantity";	// NOI18N
	public static final String IDLETIMEOUTINSECONDS = "IdleTimeoutInSeconds";	// NOI18N
	public static final String TRANSACTIONISOLATIONLEVEL = "TransactionIsolationLevel";	// NOI18N
	public static final String ISISOLATIONLEVELGUARANTEED = "IsIsolationLevelGuaranteed";	// NOI18N
	public static final String ISCONNECTIONVALIDATIONREQUIRED = "IsConnectionValidationRequired";	// NOI18N
	public static final String CONNECTIONVALIDATIONMETHOD = "ConnectionValidationMethod";	// NOI18N
	public static final String VALIDATIONTABLENAME = "ValidationTableName";	// NOI18N
	public static final String FAILALLCONNECTIONS = "FailAllConnections";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        public static final String NONTRANSACTIONALCONNECTIONS = "NonTransactionalConnections";	// NOI18N
	public static final String ALLOWNONCOMPONENTCALLERS = "AllowNonComponentCallers";	// NOI18N
        
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setDatasourceClassname(java.lang.String value);

	public java.lang.String getDatasourceClassname();

	public void setResType(java.lang.String value);

	public java.lang.String getResType();

	public void setSteadyPoolSize(java.lang.String value);

	public java.lang.String getSteadyPoolSize();

	public void setMaxPoolSize(java.lang.String value);

	public java.lang.String getMaxPoolSize();

	public void setMaxWaitTimeInMillis(java.lang.String value);

	public java.lang.String getMaxWaitTimeInMillis();

	public void setPoolResizeQuantity(java.lang.String value);

	public java.lang.String getPoolResizeQuantity();

	public void setIdleTimeoutInSeconds(java.lang.String value);

	public java.lang.String getIdleTimeoutInSeconds();

	public void setTransactionIsolationLevel(java.lang.String value);

	public java.lang.String getTransactionIsolationLevel();

	public void setIsIsolationLevelGuaranteed(java.lang.String value);

	public java.lang.String getIsIsolationLevelGuaranteed();

	public void setIsConnectionValidationRequired(java.lang.String value);

	public java.lang.String getIsConnectionValidationRequired();

	public void setConnectionValidationMethod(java.lang.String value);

	public java.lang.String getConnectionValidationMethod();

	public void setValidationTableName(java.lang.String value);

	public java.lang.String getValidationTableName();

	public void setFailAllConnections(java.lang.String value);

	public java.lang.String getFailAllConnections();

	public void setDescription(String value);

	public String getDescription();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();
        
        //Resource 1.2
        public void setNonTransactionalConnections(java.lang.String value);

	public java.lang.String getNonTransactionalConnections();
        
        public void setAllowNonComponentCallers(java.lang.String value);

	public java.lang.String getAllowNonComponentCallers();

}
