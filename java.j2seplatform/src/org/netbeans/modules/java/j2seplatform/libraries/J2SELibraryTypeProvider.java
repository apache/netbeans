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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;

import java.beans.Customizer;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class J2SELibraryTypeProvider implements LibraryTypeProvider {

    private J2SELibraryTypeProvider () {
    }

    private static final Logger LOG = Logger.getLogger(J2SELibraryTypeProvider.class.getName());

    private static final String LIB_PREFIX = "libs.";       //NOI18N
    public static final String LIBRARY_TYPE = "j2se";       //NOI18N
    public static final String VOLUME_TYPE_CLASSPATH = "classpath";       //NOI18N
    public static final String VOLUME_TYPE_SRC = "src";       //NOI18N
    public static final String VOLUME_TYPE_JAVADOC = "javadoc";       //NOI18N
    public static final String VOLUME_TYPE_MAVEN_POM = "maven-pom"; //NOI18N
    public static final String[] VOLUME_TYPES = new String[] {
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC,
        VOLUME_TYPE_MAVEN_POM
    };

    private static final Set<String> VOLUME_TYPES_REQUIRING_FOLDER = new HashSet<String>(Arrays.asList(new String[] {
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC,
    }));

    @Override
    public String getLibraryType() {
        return LIBRARY_TYPE;
    }

    @Override
    public String getDisplayName () {
        return NbBundle.getMessage (J2SELibraryTypeProvider.class,"TXT_J2SELibraryType");
    }

    @Override
    public String[] getSupportedVolumeTypes () {
        return VOLUME_TYPES;
    }

    @Override
    public LibraryImplementation createLibrary() {
        return new J2SELibraryImpl ();
    }


    @Override
    public void libraryCreated(final LibraryImplementation libraryImpl) {
        assert libraryImpl != null;
        ProjectManager.mutex().postWriteRequest(
                new Runnable () {
                    @Override
                    public void run () {
                        try {
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            boolean save = addLibraryIntoBuild(libraryImpl,props);
                            if (save) {
                                PropertyUtils.putGlobalProperties (props);
                            }
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                }
        );
    }

    @Override
    public void libraryDeleted(final LibraryImplementation libraryImpl) {
        assert libraryImpl != null;
        ProjectManager.mutex().postWriteRequest(new Runnable () {
                @Override
                public void run() {
                    try {
                        EditableProperties props = PropertyUtils.getGlobalProperties();
                        for (int i=0; i < VOLUME_TYPES.length; i++) {
                            String property = LIB_PREFIX + libraryImpl.getName() + '.' + VOLUME_TYPES[i];  //NOI18N
                            props.remove(property);
                        }
                        PropertyUtils.putGlobalProperties(props);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            });
    }

    @Override
    public Customizer getCustomizer(String volumeType) {
        if (VOLUME_TYPES[0].equals(volumeType)||
            VOLUME_TYPES[1].equals(volumeType)||
            VOLUME_TYPES[2].equals(volumeType)) {
            return new J2SEVolumeCustomizer (volumeType);
        }
        else {
            return null;
        }
    }


    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public static LibraryTypeProvider create () {
        return new J2SELibraryTypeProvider();
    }

    private static boolean addLibraryIntoBuild(LibraryImplementation impl, EditableProperties props) {
        boolean modified = false;
        for (int i=0; i<VOLUME_TYPES.length; i++) {
            String propName = LIB_PREFIX + impl.getName() + '.' + VOLUME_TYPES[i];     //NOI18N
            final List<URL> roots = impl.getContent (VOLUME_TYPES[i]);
            if (roots == null) {
                //Non valid library, but try to recover
                continue;
            }
            final StringBuilder propValue = new StringBuilder();
            boolean first = true;
            for (URL url : roots) {
                if ("jar".equals(url.getProtocol())) {  //NOI18N
                    url = FileUtil.getArchiveFile (url);
                    if (url == null) {
                        LOG.log(
                            Level.WARNING,
                            "Ignoring wrong jar protocol URL: {0}",
                            url);
                        continue;
                    }
                }
                File f = null;
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    f = FileUtil.toFile(fo);
                } else if ("file".equals(url.getProtocol())) {    //NOI18N
                    //If the file does not exist (eg library from cleaned project)
                    // and it is a file protocol URL, add it.
                    URI uri = null;
                    try {
                        uri = new URI (url.toExternalForm());
                    } catch (URISyntaxException use) {
                        try {
                            //Try to recover wrong URL
                            uri = new URI("file", null, url.getFile(), null);   //NOI18N
                        } catch (URISyntaxException e) {
                            LOG.log (
                                Level.WARNING,
                                "Invalid root URL: {0}",    //NOI18N
                                url);
                        }
                    }
                    if (uri != null) {
                        f = Utilities.toFile(uri);
                    }
                }
                if (f != null) {
                    if (!first) {
                        propValue.append(File.pathSeparatorChar);
                    }
                    first = false;
                    f = FileUtil.normalizeFile(f);
                    propValue.append (f.getAbsolutePath());
                } else {
                    LOG.log (
                        Level.WARNING,
                        "Can not resolve URL: {0}",    //NOI18N
                        url);
                }
            }
            String oldValue = props.getProperty (propName);
            String newValue = propValue.toString();
            if (!newValue.equals(oldValue)) {
                    props.setProperty (propName, newValue);
                    modified = true;
            }
        }
        return modified;
    }

    //Like DefaultLibraryTypeProvider but in addition checks '/' on the end of folder URLs.
    private static class J2SELibraryImpl implements LibraryImplementation3 {
        private String description;

        private Map<String,List<URL>> contents;

        // library 'binding name' as given by user
        private String name;

        private String displayName;

        private String localizingBundle;

        private Map<String,String> properties;

        private List<PropertyChangeListener> listeners;

        /**
         * Create new LibraryImplementation supporting given <tt>library</tt>.
         */
        public J2SELibraryImpl () {
            this.contents = new HashMap<String,List<URL>>();
            for (String vtype : VOLUME_TYPES) {
                this.contents.put(vtype, Collections.<URL>emptyList());
            }
            this.properties = Collections.<String,String>emptyMap();
        }


        @Override
        public String getType() {
            return LIBRARY_TYPE;
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
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public List<URL> getContent(String contentType) throws IllegalArgumentException {
            List<URL> content = contents.get(contentType);
            if (content == null)
                throw new IllegalArgumentException ();
            return Collections.unmodifiableList (content);
        }

        @Override
        public void setContent(final String contentType, List<URL> path) throws IllegalArgumentException {
            if (path == null) {
                throw new IllegalArgumentException ();
            }
            if (this.contents.keySet().contains(contentType)) {
                if (VOLUME_TYPES_REQUIRING_FOLDER.contains(contentType)) {
                    path = check (path, name);
                }
                this.contents.put(contentType, new ArrayList<URL>(path));
                this.firePropertyChange(PROP_CONTENT,null,null);
            } else {
                throw new IllegalArgumentException ("Volume '"+contentType+
                    "' is not support by this library. The only acceptable values are: "+contents.keySet());
            }
        }

        private static List<URL> check (final List<? extends URL> resources, final String libName) {
            final List<URL> checkedResources = new ArrayList<URL>(resources.size());
            for (URL u : resources) {
                final String surl = u.toString();
                if (!Util.isRemote(u) && !surl.endsWith("/")) {              //NOI18N
                    try {
                        if (FileUtil.isArchiveFile(u)) {
                            LOG.warning(String.format("Wrong Classpath entry %s in Library: %s", u.toString(), libName==null? "" : libName));   //NOI18N
                            u = FileUtil.getArchiveRoot(u);
                        } else {
                            if ("file".equals(u.getProtocol())) { //NOI18N
                                final FileObject fo = URLMapper.findFileObject(u);
                                if (fo != null && !fo.isFolder()) {
                                    LOG.log(
                                        Level.INFO,
                                        "Ignoring wrong reference: {0} from library: {1}",  //NOI18N
                                        new Object[]{
                                            u,
                                            libName
                                        });
                                    continue;
                                }
                            }
                            u = new URL (surl+'/');         //NOI18N
                        }
                    } catch (MalformedURLException e) {
                        //Never thrown
                        Exceptions.printStackTrace(e);
                    }
                }
                checkedResources.add(u);
            }
            return checkedResources;
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
            return this.getClass().getName()+"[" + name + "]"; // NOI18N
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

        @Override
        @NonNull
        public Map<String, String> getProperties() {
            return Collections.<String,String>unmodifiableMap(properties);
        }

        @Override
        public void setProperties(@NonNull final Map<String, String> properties) {
            this.properties = new HashMap<String, String>(properties);
            this.firePropertyChange(PROP_CONTENT,null,null);
        }
    }

}
