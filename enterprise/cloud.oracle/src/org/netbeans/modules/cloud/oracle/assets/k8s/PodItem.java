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
package org.netbeans.modules.cloud.oracle.assets.k8s;

import java.util.Objects;

/**
 *
 * @author Jan Horvath
 */
public class PodItem {
    final ClusterItem cluster;
    final String namespace;
    final String name;

    public PodItem(ClusterItem cluster, String namespace, String name) {
        this.cluster = cluster;
        this.namespace = namespace;
        this.name = name;
    }

    public ClusterItem getCluster() {
        return cluster;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.cluster);
        hash = 29 * hash + Objects.hashCode(this.namespace);
        hash = 29 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PodItem other = (PodItem) obj;
        if (!Objects.equals(this.namespace, other.namespace)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.cluster, other.cluster);
    }
    
}
