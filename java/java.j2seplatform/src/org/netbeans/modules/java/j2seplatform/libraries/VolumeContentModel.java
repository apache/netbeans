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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.net.URI;
import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.net.URL;

import java.util.List;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.NbCollections;

class VolumeContentModel extends AbstractListModel/*<String>*/ {

    private final LibraryImplementation impl;
    private final LibraryStorageArea area;
    private final String volumeType;
    private final boolean supportsURIContent;
    private List<Object> content;

    public VolumeContentModel (LibraryImplementation impl, LibraryStorageArea area, String volumeType) {
        //TODO: Should listen on the impl
        this.impl = impl;
        this.area = area;
        this.volumeType = volumeType;
        this.supportsURIContent = LibrariesSupport.supportsURIContent(impl);
        if (supportsURIContent) {
            List<URI> l = LibrariesSupport.getURIContent(impl, volumeType, LibrariesSupport.ConversionMode.FAIL);
            if (l != null) {
                content = new ArrayList<Object>(l);
            }
        } else {
            List<URL> l = this.impl.getContent (volumeType);
            if (l != null) {
                content = new ArrayList<Object>(l);
            }
        }
        if (content == null) {
            content = new ArrayList<Object>();
        }
    }
    
    public LibraryStorageArea getArea() {
        return area;
    }

    public int getSize() {
        return content.size();
    }

    public Object getElementAt(int index) {
        if (index < 0 || index >= content.size())
            throw new IllegalArgumentException();
        return content.get (index);
    }

    public void addResource (URL resource) {
        assert !supportsURIContent;
        content.add (resource);
        int index = content.size()-1;
        propagateContent();
        fireIntervalAdded(this,index,index);
    }

    public void addResource (URI resource) {
        assert supportsURIContent;
        content.add (resource);
        int index = content.size()-1;
        propagateContent();
        fireIntervalAdded(this,index,index);
    }

    private void propagateContent() {
        if (supportsURIContent) {
            LibrariesSupport.setURIContent(
                impl,
                volumeType,
                NbCollections.checkedListByCopy(content, URI.class, true),
                LibrariesSupport.ConversionMode.FAIL);
        } else {
            impl.setContent (volumeType, NbCollections.checkedListByCopy(content, URL.class, true));
        }
    }

    public void removeResources (int[] indices) {
        for (int i=indices.length-1; i>=0; i--) {
            content.remove(indices[i]);
        }
        propagateContent();
        fireIntervalRemoved(this,indices[0],indices[indices.length-1]);
    }

    public void moveUp (int[] indices) {
        for (int i=0; i< indices.length; i++) {
            Object value = content.remove(indices[i]);
            content.add(indices[i]-1,value);
        }
        propagateContent();
        fireContentsChanged(this,indices[0]-1,indices[indices.length-1]);
    }

    public void moveDown (int[] indices) {
        for (int i=indices.length-1; i>=0; i--) {
            Object value = content.remove(indices[i]);
            content.add(indices[i]+1,value);
        }
        propagateContent();
        fireContentsChanged(this,indices[0],indices[indices.length-1]+1);
    }

}
