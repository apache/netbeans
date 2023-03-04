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

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.netbeans.modules.j2ee.dd.api.common.InjectionTarget;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 * Default implemetation of {@link ResourceRef}.
 * For getting all {@link ResourceRef}s use one of
 * {@link CommonAnnotationHelper#getResourceRefs CommonAnnotationHelper.getResourceRefs()} methods.
 * <p>
 * For more info see {@link ResourceImpl}.
 * @author Tomas Mysik
 * @see SimpleResourceRefImpl
 */
public class ResourceRefImpl implements ResourceRef {
    
    private final ResourceImpl resource;
    
    /**
     * Create a new instance of ResourceRefImpl.
     * @param resource {@link javax.annotation.Resource @Resource} implementation.
     */
    public ResourceRefImpl(ResourceImpl resource) {
        this.resource = resource;
    }
    
    // <editor-fold desc="Model implementation">
    public String getResRefName() {
        return resource.getName();
    }

    public String getResType() {
        return resource.getType();
    }

    public String getResAuth() {
        return resource.getAuthenticationType();
    }
    
    public String getResSharingScope() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getMappedName() throws VersionNotSupportedException {
        return resource.getMappedName();
    }

    public String getDefaultDescription() {
        return resource.getDescription();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    public void setResRefName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResAuth(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResSharingScope(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMappedName(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInjectionTarget(int index, InjectionTarget valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InjectionTarget getInjectionTarget(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeInjectionTarget() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInjectionTarget(InjectionTarget[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InjectionTarget[] getInjectionTarget() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InjectionTarget newInjectionTarget() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // </editor-fold>
}
