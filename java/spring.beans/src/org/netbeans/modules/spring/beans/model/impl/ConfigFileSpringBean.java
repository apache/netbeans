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

package org.netbeans.modules.spring.beans.model.impl;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeanProperty;

/**
 *
 * @author Andrei Badea
 */
public class ConfigFileSpringBean implements SpringBean {

    private final String id;
    private final List<String> names;
    private final String className;
    private final String parent;
    private final String factoryBean;
    private final String factoryMethod;
    private final Set<SpringBeanProperty> properties;
    private final Location location;

    public ConfigFileSpringBean(
            String id,
            List<String> names,
            String className,
            String parent,
            String factoryBean,
            String factoryMethod,
            Set<SpringBeanProperty> properties,
            Location location) {
        this.id = id;
        this.names = names;
        this.className = className;
        this.parent = parent;
        this.factoryBean = factoryBean;
        this.factoryMethod = factoryMethod;
        this.properties = properties;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public List<String> getNames() {
        return names;
    }

    public String getClassName() {
        return className;
    }

    public String getParent() {
        return parent;
    }

    public String getFactoryBean() {
        return factoryBean;
    }

    public String getFactoryMethod() {
        return factoryMethod;
    }

    public Location getLocation() {
        return location;
    }

    public Set<SpringBeanProperty> getProperties() {
        return properties;
    }
}
