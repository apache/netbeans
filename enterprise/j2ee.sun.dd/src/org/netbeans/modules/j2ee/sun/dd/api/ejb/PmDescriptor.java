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
 * PmDescriptor.java
 *
 * Created on November 18, 2004, 10:36 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PmDescriptor extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String PM_IDENTIFIER = "PmIdentifier";	// NOI18N
    public static final String PM_VERSION = "PmVersion";	// NOI18N
    public static final String PM_CONFIG = "PmConfig";	// NOI18N
    public static final String PM_CLASS_GENERATOR = "PmClassGenerator";	// NOI18N
    public static final String PM_MAPPING_FACTORY = "PmMappingFactory";	// NOI18N
        
    /** Setter for pm-identifier property
     * @param value property value
     */
    public void setPmIdentifier(java.lang.String value);
    /** Getter for pm-identifier property.
     * @return property value
     */
    public java.lang.String getPmIdentifier();
    /** Setter for pm-version property
     * @param value property value
     */
    public void setPmVersion(java.lang.String value);
    /** Getter for pm-version property.
     * @return property value
     */
    public java.lang.String getPmVersion();
    /** Setter for pm-config property
     * @param value property value
     */
    public void setPmConfig(java.lang.String value);
    /** Getter for pm-config property.
     * @return property value
     */
    public java.lang.String getPmConfig();
    /** Setter for pm-class-generator property
     * @param value property value
     */
    public void setPmClassGenerator(java.lang.String value);
    /** Getter for pm-class-generator property.
     * @return property value
     */
    public java.lang.String getPmClassGenerator();
    /** Setter for pm-mapping-factory property
     * @param value property value
     */
    public void setPmMappingFactory(java.lang.String value);
    /** Getter for pm-mapping-factory property.
     * @return property value
     */
    public java.lang.String getPmMappingFactory();
    
}
