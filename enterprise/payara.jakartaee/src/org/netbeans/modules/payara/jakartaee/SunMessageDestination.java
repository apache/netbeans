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
package org.netbeans.modules.payara.jakartaee;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 *
 * @author Nitya Doraisamy
 */
public class SunMessageDestination implements MessageDestination {
    private String name;
    private Type type;
    private File resourceDir;

    public SunMessageDestination(String name, Type type) {
        this(name, type, null);
    }
    
    public SunMessageDestination(String name, Type type, File resourceDir) {
        this.name = name;
        this.type = type;
        this.resourceDir = resourceDir;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
   
    File getResourceDir() {
        return resourceDir;
    }

    void setResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }

    @Override
    public String toString() {
        return "[ " + name + " : " + type.toString() + " ]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SunMessageDestination other = (SunMessageDestination) obj;
        if (this.name == null || !this.name.equals(other.name)) {
            return false;
        }
        if (this.type == null || !this.type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
}
