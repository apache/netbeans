/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
