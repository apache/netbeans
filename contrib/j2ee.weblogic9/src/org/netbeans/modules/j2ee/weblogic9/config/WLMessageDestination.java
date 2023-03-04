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
package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 *
 * @author Petr Hejl
 */
public class WLMessageDestination implements MessageDestination, WLApplicationModule {

    private final String jndiName;

    private final String resourceName;

    private final Type type;

    private final File origin;

    private final boolean system;

    public WLMessageDestination(String resourceName, String jndiName, Type type,
            File origin, boolean system) {
        this.resourceName = resourceName;
        this.jndiName = jndiName;
        this.type = type;
        this.origin = origin;
        this.system = system;
    }

    // this is the JNDI name
    @Override
    public String getName() {
        return jndiName;
    }

    // the object name
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public File getOrigin() {
        return origin;
    }

    public boolean isSystem() {
        return system;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WLMessageDestination other = (WLMessageDestination) obj;
        if ((this.jndiName == null) ? (other.jndiName != null) : !this.jndiName.equals(other.jndiName)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.jndiName != null ? this.jndiName.hashCode() : 0);
        hash = 43 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
}
