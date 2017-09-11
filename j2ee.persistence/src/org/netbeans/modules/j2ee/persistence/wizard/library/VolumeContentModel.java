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

    public int getSize() {
        return this.content.size();
    }

    public Object getElementAt(int index) {
        if (index < 0 || index >= this.content.size())
            throw new IllegalArgumentException();
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
