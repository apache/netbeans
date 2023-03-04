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

/**
 * Superclass that implements DescriptionInterface for Ejb2.0 beans.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

public abstract class DescriptionBeanSingle extends EnclosingBean implements DescriptionInterface {

    public DescriptionBeanSingle(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    // methods implemented by specific s2b beans
    public String getDescription() {return null;}
    public void setDescription(String description){}
    
    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        if (locale==null) setDescription(description);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    
    public void setAllDescriptions(java.util.Map descriptions) throws VersionNotSupportedException {
        throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    
    public String getDescription(String locale) throws VersionNotSupportedException {
        if (locale==null) return getDescription();
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public String getDefaultDescription() {
        return getDescription();
    }
    public java.util.Map getAllDescriptions() {
        java.util.Map map = new java.util.HashMap();
        map.put(null, getDescription());
        return map;
    }
    
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        if (locale==null) setDescription(null);
        else throw new VersionNotSupportedException("2.0"); // NOI18N
    }
    public void removeDescription() {
        setDescription(null);
    }
    public void removeAllDescriptions() {
        setDescription(null);
    }
}
