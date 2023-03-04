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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Manages extensible (freeform) metadata in an Ant-based project.
 * @author Jesse Glick
 */
final class ExtensibleMetadataProviderImpl implements AuxiliaryConfiguration, CacheDirectoryProvider {

    /**
     * Relative path from project directory to the required private cache directory.
     */
    private static final String CACHE_PATH = "nbproject/private"; // NOI18N
    
    private final AntProjectHelper helper;
    
    ExtensibleMetadataProviderImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public FileObject getCacheDirectory() throws IOException {
        return FileUtil.createFolder(helper.getProjectDirectory(), CACHE_PATH);
    }
    
    public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
        if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        return helper.getConfigurationFragment(elementName, namespace, shared);
    }
    
    public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
        if (fragment.getNamespaceURI() == null || fragment.getNamespaceURI().length() == 0) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        if (fragment.getLocalName().equals(helper.getType().getPrimaryConfigurationDataElementName(shared)) &&
                fragment.getNamespaceURI().equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
            throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
        }
        helper.putConfigurationFragment(fragment, shared);
    }
    
    public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
        if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        if (elementName.equals(helper.getType().getPrimaryConfigurationDataElementName(shared)) &&
                namespace.equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
            throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
        }
        return helper.removeConfigurationFragment(elementName, namespace, shared);
    }
    
}
