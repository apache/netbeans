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

package org.netbeans.modules.project.libraries;

import java.net.URL;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.openide.util.Parameters;

public final class DefaultLibraryImplementation implements LibraryImplementation3 {

    private String description;

    private Map<String,List<URL>> contents;

    // library 'binding name' as given by user
    private String name;

    private String libraryType;

    private String localizingBundle;

    private String displayName;

    private List<PropertyChangeListener> listeners;
    
    private volatile Map<String,String> properties = Collections.<String,String>emptyMap();

    /**
     * Create new LibraryImplementation supporting given <tt>library</tt>.
     */
    public DefaultLibraryImplementation (
            @NonNull final String libraryType,
            @NonNull final String[] volumeTypes) {
        Parameters.notNull("libraryType", libraryType); //NOI18N
        Parameters.notNull("volumeTypes", volumeTypes); //NOI18N
        this.libraryType = libraryType;
        this.contents = new HashMap<String,List<URL>>();
        for (String vtype : volumeTypes) {
            Parameters.notNull("volumeTypes", vtype);   //NOI18N
            this.contents.put(vtype, Collections.<URL>emptyList());
        }
    }


    @Override
    public String getType() {
        return libraryType;
    }

    @Override
    public void setName(final String name) throws UnsupportedOperationException {
        String oldName = this.name;
        this.name = name;
        this.firePropertyChange (PROP_NAME, oldName, this.name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<URL> getContent(String contentType) throws IllegalArgumentException {
        List<URL> content = contents.get(contentType);
        if (content == null) {
            throw new IllegalArgumentException (
                    MessageFormat.format(
                        "Volume: {0} is not support by this library. The only acceptable values are: {1}",  //NOI18N
                        contentType,
                        contents.keySet()
                        ));
        }
        return Collections.unmodifiableList (content);
    }

    @Override
    public void setContent(String contentType, List<URL> path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException ();
        }
        if (this.contents.keySet().contains(contentType)) {
            this.contents.put(contentType, new ArrayList<URL>(path));
            this.firePropertyChange(PROP_CONTENT,null,null);
        } else {
            throw new IllegalArgumentException (
                    MessageFormat.format(
                        "Volume: {0} is not support by this library. The only acceptable values are: {1}",  //NOI18N
                        contentType,
                        contents.keySet()
                        ));
        }
    }

    @Override
    public String getDescription () {
            return this.description;
    }

    @Override
    public void setDescription (String text) {
        String oldDesc = this.description;
        this.description = text;
        this.firePropertyChange (PROP_DESCRIPTION, oldDesc, this.description);
    }

    @Override
    public String getLocalizingBundle() {
        return this.localizingBundle;
    }

    @Override
    public void setLocalizingBundle(String resourceName) {
        this.localizingBundle = resourceName;
    }

    @Override
    public @CheckForNull String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(final @NullAllowed String displayName) {
        final String oldDisplayName = this.displayName;
        this.displayName = displayName;
        this.firePropertyChange (PROP_DISPLAY_NAME, oldDisplayName, this.displayName);
    }

    @Override
    public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
        if (this.listeners == null)
            this.listeners = new ArrayList<PropertyChangeListener>();
        this.listeners.add (l);
    }

    @Override
    public synchronized void removePropertyChangeListener (PropertyChangeListener l) {
        if (this.listeners == null)
            return;
        this.listeners.remove (l);
    }

    @Override
    public String toString() {
        return "DefaultLibraryImplementation[" + name + "]"; // NOI18N
    }

    @Override
    public boolean equals (final Object other) {
        if (other instanceof DefaultLibraryImplementation) {
            final DefaultLibraryImplementation otherLib = (DefaultLibraryImplementation) other;
            return (name == null ? otherLib.name == null : name.equals(otherLib.name)) &&
                   (libraryType == null ? otherLib.libraryType == null : libraryType.equals(otherLib.libraryType));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 31;
        hash = hash*17 + (name == null? 0 : name.hashCode());
        hash = hash*17 + (libraryType == null ? 0 : libraryType.hashCode());
        return hash;
    }

    private void firePropertyChange (String propName, Object oldValue, Object newValue) {
        List<PropertyChangeListener> ls;
        synchronized (this) {
            if (this.listeners == null)
                return;
            ls = new ArrayList<PropertyChangeListener>(listeners);
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, propName, oldValue, newValue);
        for (PropertyChangeListener l : ls) {
            l.propertyChange(event);
        }
    }

    @NonNull
    @Override
    public Map<String,String> getProperties() {
        return properties;
    }
    
    @Override
    public void setProperties(@NonNull final Map<String,String> props) {
        Parameters.notNull("props", props); //NOI18N
        final Map<String,String> oldProps = properties;
        properties = new HashMap<String, String>(props);
        firePropertyChange (PROP_PROPERTIES, oldProps, properties);
    }
}
