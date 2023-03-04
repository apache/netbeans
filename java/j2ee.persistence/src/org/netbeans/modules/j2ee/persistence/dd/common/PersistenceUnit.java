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

package org.netbeans.modules.j2ee.persistence.dd.common;

/**
 *
 * @author sp153251
 */
public interface PersistenceUnit {
	static public final String NAME = "Name";	// NOI18N
	static public final String TRANSACTIONTYPE = "TransactionType";	// NOI18N
	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String PROVIDER = "Provider";	// NOI18N
	static public final String JTA_DATA_SOURCE = "JtaDataSource";	// NOI18N
	static public final String NON_JTA_DATA_SOURCE = "NonJtaDataSource";	// NOI18N
	static public final String MAPPING_FILE = "MappingFile";	// NOI18N
	static public final String JAR_FILE = "JarFile";	// NOI18N
	static public final String CLASS2 = "Class2";	// NOI18N
	static public final String EXCLUDE_UNLISTED_CLASSES = "ExcludeUnlistedClasses";	// NOI18N
	static public final String PROPERTIES = "Properties";	// NOI18N

        //
        public static final String JTA_TRANSACTIONTYPE="JTA";//NOI18N
        public static final String RESOURCE_LOCAL_TRANSACTIONTYPE="RESOURCE_LOCAL";//NOI18N

        public void setName(java.lang.String value);
        public java.lang.String getName();
        public void setTransactionType(java.lang.String value);
        public java.lang.String getTransactionType();
        public void setDescription(java.lang.String value);
        public java.lang.String getDescription();
        public void setProvider(java.lang.String value);
        public java.lang.String getProvider();
        public void setJtaDataSource(java.lang.String value);
        public java.lang.String getJtaDataSource();
        public void setNonJtaDataSource(java.lang.String value);
        public java.lang.String getNonJtaDataSource();
        public void setMappingFile(int index, java.lang.String value);
        public java.lang.String getMappingFile(int index);
        public int sizeMappingFile();
        public void setMappingFile(java.lang.String[] value);
        public java.lang.String[] getMappingFile();
        public int addMappingFile(java.lang.String value);
        public int removeMappingFile(java.lang.String value);
        public void setJarFile(int index, java.lang.String value);
        public java.lang.String getJarFile(int index);
        public int sizeJarFile();
        public void setJarFile(java.lang.String[] value);
        public java.lang.String[] getJarFile();
        public int addJarFile(java.lang.String value);

        public int removeJarFile(java.lang.String value);

        public void setClass2(int index, java.lang.String value);
        public java.lang.String getClass2(int index);
        public int sizeClass2();
        public void setClass2(java.lang.String[] value);
        public java.lang.String[] getClass2();
        public int addClass2(java.lang.String value);
        public int removeClass2(java.lang.String value);

        public void setExcludeUnlistedClasses(boolean value);
        public boolean isExcludeUnlistedClasses();

        public void setProperties(Properties valueInterface);
        public Properties getProperties();
        public Properties newProperties();
}
