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
 * Cardinality.java
 *
 * Created on January 5, 2006, 3:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents the cardinality of the component type.
 * @author Chris Webster
 */
public interface Cardinality {

        /**
         * @return maxOccurs attribute value if set, otherwise null.
         */
	String getMaxOccurs();
	
        /**
         * Set maxOccurs attribute value.
         * @param max maxOccurs value; null value means reset to default.
         */
	void setMaxOccurs(String max);
	
        /**
         * @return default values for attribute minOccurs.
         */
        String getMaxOccursDefault();
        
        /**
         * @return the actual value set by user or default value if not set.
         */
        String getMaxOccursEffective();
        
        /**
         * @return minOccurs attribute value if set, otherwise null.
         */
	Integer getMinOccurs();
        
        /**
         * Set minOccurs attribute value.
         * @param min minOccurs value; null value means reset to default.
         */
	void setMinOccurs(Integer min);

        /**
         * @return default value for attribute minOccurs.
         */
        int getMinOccursDefault();
        
        /**
         * @return the actual value set by user or default value if not set.
         */
        int getMinOccursEffective();
}
