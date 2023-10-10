/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.persistence.dd.common;

/**
 *
 * @author sp153251
 */
public interface PersistenceUnit {
    
        String JAVAX_NAMESPACE = "javax.persistence.";
        String JAKARTA_NAMESPACE = "jakarta.persistence.";

	String NAME = "Name";	// NOI18N
	String TRANSACTIONTYPE = "TransactionType";	// NOI18N
	String DESCRIPTION = "Description";	// NOI18N
	String PROVIDER = "Provider";	// NOI18N
	String JTA_DATA_SOURCE = "JtaDataSource";	// NOI18N
	String NON_JTA_DATA_SOURCE = "NonJtaDataSource";	// NOI18N
	String MAPPING_FILE = "MappingFile";	// NOI18N
	String JAR_FILE = "JarFile";	// NOI18N
	String CLASS2 = "Class2";	// NOI18N
	String EXCLUDE_UNLISTED_CLASSES = "ExcludeUnlistedClasses";	// NOI18N
	String PROPERTIES = "Properties";	// NOI18N

        String JTA_TRANSACTIONTYPE="JTA";//NOI18N
        String RESOURCE_LOCAL_TRANSACTIONTYPE="RESOURCE_LOCAL";//NOI18N

        void setName(java.lang.String value);
        java.lang.String getName();
        void setTransactionType(java.lang.String value);
        java.lang.String getTransactionType();
        void setDescription(java.lang.String value);
        java.lang.String getDescription();
        void setProvider(java.lang.String value);
        java.lang.String getProvider();
        void setJtaDataSource(java.lang.String value);
        java.lang.String getJtaDataSource();
        void setNonJtaDataSource(java.lang.String value);
        java.lang.String getNonJtaDataSource();
        void setMappingFile(int index, java.lang.String value);
        java.lang.String getMappingFile(int index);
        int sizeMappingFile();
        void setMappingFile(java.lang.String[] value);
        java.lang.String[] getMappingFile();
        int addMappingFile(java.lang.String value);
        int removeMappingFile(java.lang.String value);
        void setJarFile(int index, java.lang.String value);
        java.lang.String getJarFile(int index);
        int sizeJarFile();
        void setJarFile(java.lang.String[] value);
        java.lang.String[] getJarFile();
        int addJarFile(java.lang.String value);

        int removeJarFile(java.lang.String value);

        void setClass2(int index, java.lang.String value);
        java.lang.String getClass2(int index);
        int sizeClass2();
        void setClass2(java.lang.String[] value);
        java.lang.String[] getClass2();
        int addClass2(java.lang.String value);
        int removeClass2(java.lang.String value);

        void setExcludeUnlistedClasses(boolean value);
        boolean isExcludeUnlistedClasses();

        void setProperties(Properties valueInterface);
        Properties getProperties();
        Properties newProperties();
}
