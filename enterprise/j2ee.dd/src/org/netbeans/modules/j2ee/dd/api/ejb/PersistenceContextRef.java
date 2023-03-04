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

package org.netbeans.modules.j2ee.dd.api.ejb;

import org.netbeans.modules.j2ee.dd.api.common.InjectionTarget;

/**
 *
 * @author Martin Adamek
 */
public interface PersistenceContextRef {
    
    int addDescription(String value);
    int addInjectionTarget(InjectionTarget value);
    int addPersistenceProperty(Property value);
    String[] getDescription();
    String getDescription(int index);
    InjectionTarget[] getInjectionTarget();
    InjectionTarget getInjectionTarget(int index);
    String getMappedName();
    String getPersistenceContextRefName();
    String getPersistenceContextType();
    Property[] getPersistenceProperty();
    Property getPersistenceProperty(int index);
    String getPersistenceUnitName();
    InjectionTarget newInjectionTarget();
    Property newProperty();
    int removeDescription(String value);
    int removeInjectionTarget(InjectionTarget value);
    int removePersistenceProperty(Property value);
    void setDescription(int index, String value);
    void setDescription(String[] value);
    void setInjectionTarget(int index, InjectionTarget value);
    void setInjectionTarget(InjectionTarget[] value);
    void setMappedName(String value);
    void setPersistenceContextRefName(String value);
    void setPersistenceContextType(String value);
    void setPersistenceProperty(int index, Property value);
    void setPersistenceProperty(Property[] value);
    void setPersistenceUnitName(String value);
    int sizeDescription();
    int sizeInjectionTarget();
    int sizePersistenceProperty();
    
}
