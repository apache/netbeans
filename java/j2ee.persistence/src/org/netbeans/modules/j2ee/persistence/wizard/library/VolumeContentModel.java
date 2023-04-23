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

package org.netbeans.modules.j2ee.persistence.wizard.library;

import javax.swing.AbstractListModel;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import org.netbeans.spi.project.libraries.LibraryImplementation;


/**
 * copy of class from j2seplatform
 */
class VolumeContentModel extends AbstractListModel/*<String>*/ {

    private LibraryImplementation impl;
    private String volumeType;
    private List/*<URL>*/ content;

    public VolumeContentModel (LibraryImplementation impl, String volumeType) {
        //TODO: Should listen on the impl
        this.impl = impl;
        this.volumeType = volumeType;
        List l = this.impl.getContent (volumeType);
        if (l != null) {
            this.content = new ArrayList(l);
        }
        else {
            content = new ArrayList();
        }
    }

    @Override
    public int getSize() {
        return this.content.size();
    }

    @Override
    public Object getElementAt(int index) {
        if (index < 0 || index >= this.content.size()) {
            throw new IllegalArgumentException();
        }
        return this.content.get (index);
    }

    public void addResource (URL resource) {        
        this.content.add (resource);
        int index = this.content.size()-1;
        this.impl.setContent (this.volumeType, content);
        this.fireIntervalAdded(this,index,index);
    }

    public void removeResources (int[] indices) {
        for (int i=indices.length-1; i>=0; i--) {
            this.content.remove(indices[i]);
        }
        this.impl.setContent (this.volumeType, content);
        this.fireIntervalRemoved(this,indices[0],indices[indices.length-1]);
    }

    public void moveUp (int[] indices) {
        for (int i=0; i< indices.length; i++) {
            Object value = this.content.remove(indices[i]);
            this.content.add(indices[i]-1,value);
        }
        this.impl.setContent (this.volumeType, content);
        this.fireContentsChanged(this,indices[0]-1,indices[indices.length-1]);
    }

    public void moveDown (int[] indices) {
        for (int i=indices.length-1; i>=0; i--) {
            Object value = this.content.remove(indices[i]);
            this.content.add(indices[i]+1,value);
        }
        this.impl.setContent (this.volumeType, content);
        this.fireContentsChanged(this,indices[0],indices[indices.length-1]+1);
    }

}
