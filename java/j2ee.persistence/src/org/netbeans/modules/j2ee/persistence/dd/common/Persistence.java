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

import java.beans.PropertyChangeListener;

/**
 *
 * @author sp153251
 */
public interface Persistence {
	static public final String VERSION = "Version";	// NOI18N
	static public final String PERSISTENCE_UNIT = "PersistenceUnit";	// NOI18N
        
        /**
         * Jakarta EE 11 - JPA 3.2 (Schema v3.2)
         */
        public static final String VERSION_3_2="3.2"; //NOI18N
        /**
         * Jakarta EE 10 - JPA 3.1 (Schema v3.0)
         */
        public static final String VERSION_3_1="3.1"; //NOI18N
        /**
         * Jakarta EE 9/9.1 - JPA 3.0 (Schema v3.0)
         */
        public static final String VERSION_3_0="3.0"; //NOI18N
        // Jakarta EE 8
        public static final String VERSION_2_2="2.2"; //NOI18N
        public static final String VERSION_2_1="2.1"; //NOI18N
        public static final String VERSION_2_0="2.0"; //NOI18N
        public static final String VERSION_1_0="1.0"; //NOI18N

        public void addPropertyChangeListener(PropertyChangeListener l);
        public void removePropertyChangeListener(PropertyChangeListener l);
        public void setVersion(java.lang.String value);
        public java.lang.String getVersion();
        public void setPersistenceUnit(int index, PersistenceUnit value);
        public PersistenceUnit getPersistenceUnit(int index);
        public int sizePersistenceUnit();
        public void setPersistenceUnit(PersistenceUnit[] value);
        public PersistenceUnit[] getPersistenceUnit();
        public int addPersistenceUnit(PersistenceUnit value);
        public int removePersistenceUnit(PersistenceUnit value);
        public PersistenceUnit newPersistenceUnit();
        public void validate() throws org.netbeans.modules.schema2beans.ValidateException;
}
