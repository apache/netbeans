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

package org.netbeans.modules.project.libraries.ui;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.ForwardingLibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Parameters;

/**
 *
 * @author  Tomas Zezula
 */
public class ProxyLibraryImplementation extends ForwardingLibraryImplementation {
    private final LibrariesModel model;
    private Map<String,List<URL>> newContents;
    private String newName;
    private String newDisplayName;
    private String newDescription;
    private Map<String,List<URI>> newURIContents;

    @SuppressWarnings("LeakingThisInConstructor")
    private ProxyLibraryImplementation (
            @NonNull final LibraryImplementation original,
            @NonNull final LibrariesModel model) {
        super(original);
        Parameters.notNull("model", model); //NOI18N
        this.model = model;
    }
    
    public static ProxyLibraryImplementation createProxy(LibraryImplementation original, LibrariesModel model) {
        return new ProxyLibraryImplementation(original, model);
    }

    protected LibrariesModel getModel() {
        return model;
    }

    @Override
    public synchronized List<URL> getContent(String volumeType) throws IllegalArgumentException {
        List<URL> result = null;
        if (newContents == null || (result = newContents.get(volumeType)) == null) {
            return super.getContent (volumeType);
        } else {
            return result;
        }
    }

    @Override
    public synchronized String getDescription() {
        if (this.newDescription != null) {
            return this.newDescription;
        } else {
            return super.getDescription();
        }
    }

    @Override
    public synchronized String getName() {
        if (this.newName != null) {
            return this.newName;
        } else {
            return super.getName ();
        }
    }

    @Override
    public String getDisplayName() {
        if (!LibrariesSupport.supportsDisplayName(getDelegate())) {
            throw new IllegalStateException("Delegate does not support displayName");   //NOI18N
        }
        synchronized (this) {
            return newDisplayName != null ?
                newDisplayName :
                super.getDisplayName();
        }
    }
    
    @Override
    public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
        synchronized (this) {
            if (this.newContents == null) {
                this.newContents = new HashMap<String,List<URL>>();
            }
            this.newContents.put (volumeType, path);
            this.model.modifyLibrary(this);
        }
        firePropertyChange(PROP_CONTENT,null,null);
    }
    
    @Override
    public void setDescription(String text) {
        final String oldDescription;
        synchronized (this) {
            oldDescription = getDescription();
            this.newDescription = text;
            this.model.modifyLibrary(this);
        }
        firePropertyChange(PROP_DESCRIPTION,oldDescription,this.newDescription);
    }
    
    @Override
    public synchronized void setName(String name) {
        final String oldName;
        synchronized (this) {
            oldName = getName();
            this.newName = name;
            this.model.modifyLibrary(this);
        }
        firePropertyChange(PROP_NAME,oldName,this.newName);
    }

    @Override
    public void setDisplayName(final @NullAllowed String displayName) {
        final String oldName;
        synchronized (this) {
            oldName = getDisplayName();
            this.newDisplayName = displayName;
            this.model.modifyLibrary(this);
        }
        firePropertyChange(PROP_DISPLAY_NAME, oldName, displayName);
    }

    @Override
    public int hashCode() {
        return this.getDelegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProxyLibraryImplementation) {
            return this.getDelegate().equals(((ProxyLibraryImplementation)obj).getDelegate());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Proxy[" + getDelegate() + "]"; // NOI18N
    }

    
    @Override
    public synchronized List<URI> getURIContent(String volumeType) throws IllegalArgumentException {
        List<URI> result = null;
        if (newURIContents == null || (result = newURIContents.get(volumeType)) == null) {
            return super.getURIContent(volumeType);
        } else {
            return result;
        }
    }

    @Override
    public void setURIContent(String volumeType, List<URI> path) throws IllegalArgumentException {
        synchronized (this) {
            if (newURIContents == null) {
                newURIContents = new HashMap<String,List<URI>>();
            }
            newURIContents.put(volumeType, path);
            getModel().modifyLibrary(this);
        }
        firePropertyChange(PROP_CONTENT,null,null);
    }

    @CheckForNull
    synchronized Map<String,List<URI>> getNewURIContents() {
        return newURIContents == null ?
                null :
                Collections.unmodifiableMap(newURIContents);
    }

    @CheckForNull
    synchronized Map<String,List<URL>> getNewContents() {
        return newContents == null ?
                null :
                Collections.unmodifiableMap(newContents);
    }
}
