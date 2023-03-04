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

package org.netbeans.modules.j2ee.persistence.spi.moduleinfo;

/**
 * This interface provides information on the project module, such
 * as its type and version number. It should be implemented by projects that 
 * provide EJB or Web modules. 
 * 
 * @author Erno Mononen
 */
public interface JPAModuleInfo {

    enum ModuleType {
        EJB, 
        WEB
    }

    String JPACHECKSUPPORTED = "jpaversionverification";//NOI18N
    String JPAVERSIONPREFIX = "jpa";//NOI18N
    /**
     * Gets the type of our module.
     * 
     * @return the type of the module.
     */ 
    ModuleType getType();
    
    /**
     * Gets the version number of our module, i.e. for an EJB module
     * it might be <tt>"2.1" or "3.0"</tt> and for a Web module <tt>"2.4" or "2.5"</tt>.
     * 
     * @return the version number of the module.
     */ 
    String getVersion();

    /**
     * get if module support corresponding jpa version
     * @return true if supported, false if unsupported, null if unknown (may be considered as all versions are supported for backward compartibility)
     */
    Boolean isJPAVersionSupported (String version);
}
