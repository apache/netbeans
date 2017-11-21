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

package org.netbeans.modules.hibernate.loaders.mapping;

import org.netbeans.modules.hibernate.loaders.cfg.*;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateMappingMetadata {
    private static final HibernateMappingMetadata DEFAULT = new HibernateMappingMetadata();
    private Map<FileObject,HibernateMapping> ddMap;
    
    private HibernateMappingMetadata() {
        ddMap = new WeakHashMap<FileObject,HibernateMapping>(5);
    }
    
    /**
     * Use this to get singleton instance of provider
     *
     * @return singleton instance
     */
    public static HibernateMappingMetadata getDefault() {
        return DEFAULT;
    }
    
    /**
     * Provides root element as defined in hibernate-configuration-3.0.dtd
     * 
     * @param fo FileObject represnting Hibernate configuration file
     * It can be retrieved from {@link PersistenceProvider} for any file
     * @throws java.io.IOException 
     * @return root element of schema or null if it doesn't exist for provided 
     * persistence.xml deployment descriptor
     * @see PersistenceProvider
     */
    public HibernateMapping getRoot(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        HibernateMapping mapping = null;
        synchronized (ddMap) {
            mapping = (HibernateMapping) ddMap.get(fo);
            if (mapping == null) {
                mapping = HibernateMapping.createGraph(fo.getInputStream());
                ddMap.put(fo, mapping);
            }
        }
        return mapping;
    }

}
