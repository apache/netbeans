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
package org.netbeans.modules.cnd.repository.impl.spi;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public final class LayerDescriptor {

    public static final String RO_FRAGMENT = "r/o"; //NOI18N
    public static final String RW_FRAGMENT = "r/w"; //NOI18N
    private final URI uri;
    private final boolean isWritable;

    public LayerDescriptor(URI uri) {
        this.uri = removeFragment(uri);
        boolean _isWritable = true;
        String fragment = uri.getFragment();
        if (fragment != null) {
            if (fragment.contains(RO_FRAGMENT)) {
                _isWritable = false;
            } else if (uri.getFragment().contains(RW_FRAGMENT)) {
                _isWritable = true;
            }
        }
        isWritable = _isWritable;
    }

    // TODO: decribe schema
    public URI getURI() {
        return uri;
    }

    public boolean isWritable() {
        return isWritable;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.uri != null ? this.uri.hashCode() : 0);
        hash = 37 * hash + (this.isWritable ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LayerDescriptor other = (LayerDescriptor) obj;
        if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
            return false;
        }
        if (this.isWritable != other.isWritable) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return uri.toString() + (isWritable ? "#r/w" : "#r/o"); // NOI18N
    }

    private URI removeFragment(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
        } catch (URISyntaxException ex) {
            return uri;
        }
    }
}
