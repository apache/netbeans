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

package org.netbeans.modules.spring.api.beans.model;

import java.util.List;
import java.util.Set;

/**
 * Describes a single bean definition.
 *
 * @author Andrei Badea
 */
public interface SpringBean {

    /**
     * Returns the id of this bean.
     *
     * @return the id or null.
     */
    String getId();

    /**
     * Returns the other names of this bean.
     *
     * @return the names; never null.
     */
    List<String> getNames();

    /**
     * Returns the implementation class of this bean.
     *
     * @return the implementation class or null.
     */
    String getClassName();

    /**
     * Returns the parent bean of this bean.
     *
     * @return the factory bean.
     */
    String getParent();

    /**
     * Returns the factory bean that creates this bean.
     *
     * @return the factory bean or null.
     */
    String getFactoryBean();

    /**
     * Returns the factory method that creates this bean.
     *
     * @return the factory method or null.
     */
    String getFactoryMethod();
    
    /**
     * Returns the list of properties defined in this bean
     * 
     * @return list of properties; never null
     */
    Set<SpringBeanProperty> getProperties();

    /**
     * Returns the location of this bean.
     *
     * @return the location or null.
     */
    Location getLocation();
}
