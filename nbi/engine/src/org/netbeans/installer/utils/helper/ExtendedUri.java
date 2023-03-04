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

package org.netbeans.installer.utils.helper;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Kirill Sorokin
 */
public class ExtendedUri {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private URI remote;
    private List<URI> alternates;
    private URI local;
    private long size;
    private String md5;
    
    public ExtendedUri(
            final URI remote, 
            final long size, 
            final String md5) {
        this.remote = remote;
        this.size   = size;
        this.md5    = md5;
        
        this.alternates = new LinkedList<URI>();
    }
    
    public ExtendedUri(
            final URI remote, 
            final List<URI> alternates, 
            final long size, 
            final String md5) {
        this(remote, size, md5);
        
        this.alternates.addAll(alternates);
    }
    
    public ExtendedUri(
            final URI remote, 
            final URI local, 
            final long size, 
            final String md5) {
        this(remote, size, md5);
        
        this.local  = local;
    }
    
    public ExtendedUri(
            final URI remote, 
            final List<URI> alternates, 
            final URI local, 
            final long size, 
            final String md5) {
        this(remote, alternates, size, md5);
        
        this.local  = local;
    }
    
    public URI getRemote() {
        return remote;
    }
    
    public void setRemote(final URI remote) {
        this.remote = remote;
    }
    
    public List<URI> getAlternates() {
        return new LinkedList<URI>(alternates);
    }
    
    public URI getLocal() {
        return local;
    }
    
    public void setLocal(final URI local) {
        this.local = local;
    }
    
    public long getSize() {
        return size;
    }
    
    public String getMd5() {
        return md5;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String RESOURCE_SCHEME = 
            "resource"; // NOI18N
    
    public static final String HTTP_SCHEME = 
            "http"; // NOI18N
    
    public static final String FILE_SCHEME = 
            "file"; // NOI18N
}
