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
