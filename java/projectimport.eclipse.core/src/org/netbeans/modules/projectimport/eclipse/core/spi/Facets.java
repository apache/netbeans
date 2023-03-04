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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.util.List;

/**
 * Representation of org.eclipse.wst.common.project.facet.core.xml file.
 */
public final class Facets {

    List<Facet> installed;

    public Facets(List<Facet> installed) {
        this.installed = installed;
    }

    public List<Facet> getInstalled() {
        return installed;
    }
    
    public boolean hasInstalledFacet(String name) {
        for (Facet f : installed) {
            if (name.equals(f.getName())) {
                return true;
            }
        }
        return false;
    }

    public Facet getFacet(String name) {
        for (Facet f : installed) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Facets[installed="+installed+"]"; // NOI18N
    }
    
    public static final class Facet {
        private String name;
        private String version;

        public Facet(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return name+"-"+version; // NOI18N
        }
        
    }
    
}
