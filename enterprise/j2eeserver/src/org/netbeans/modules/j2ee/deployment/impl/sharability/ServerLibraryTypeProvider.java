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

package org.netbeans.modules.j2ee.deployment.impl.sharability;

import java.beans.Customizer;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class ServerLibraryTypeProvider implements LibraryTypeProvider {

    public static final String LIBRARY_TYPE = "serverlibrary"; // NOI18N

    public static final String VOLUME_CLASSPATH = "classpath";

    public static final String VOLUME_EMBEDDABLE_EJB_CLASSPATH = "embedabbleejb";

    public static final String VOLUME_WS_COMPILE_CLASSPATH = "wscompile";

    public static final String VOLUME_WS_GENERATE_CLASSPATH = "wsgenerate";

    public static final String VOLUME_WS_IMPORT_CLASSPATH = "wsimport";

    public static final String VOLUME_WS_INTEROP_CLASSPATH = "wsinterop";

    public static final String VOLUME_WS_JWSDP_CLASSPATH = "wsjwsdp";

    public static final String VOLUME_JAVADOC = "javadoc";

    public static final String VOLUME_SOURCE = "src";

    // This is runtime only
    //public static final String VOLUME_APP_CLIENT_CLASSPATH = "appclient";

    private static final String LIB_PREFIX = "libs.";

    private static final String[] VOLUME_TYPES = new String[] {
            VOLUME_CLASSPATH,
            VOLUME_EMBEDDABLE_EJB_CLASSPATH,
            VOLUME_WS_COMPILE_CLASSPATH,
            VOLUME_WS_GENERATE_CLASSPATH,
            VOLUME_WS_IMPORT_CLASSPATH,
            VOLUME_WS_INTEROP_CLASSPATH,
            VOLUME_WS_JWSDP_CLASSPATH,
            VOLUME_JAVADOC,
            VOLUME_SOURCE
    };

    private ServerLibraryTypeProvider() {
        super();
    }

    public static LibraryTypeProvider create() {
        return new ServerLibraryTypeProvider();
    }

    public LibraryImplementation createLibrary() {
        return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
    }

    public Customizer getCustomizer(String volumeType) {
        // avoid user confusion
        if (VOLUME_CLASSPATH.equals(volumeType) || VOLUME_JAVADOC.equals(volumeType)
                || VOLUME_SOURCE.equals(volumeType)) {
            return new ServerVolumeCustomizer(volumeType);
        }
        return null;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ServerLibraryTypeProvider.class, "ServerLibraryTypeProvider.typeName");
    }

    public String getLibraryType() {
        return LIBRARY_TYPE;
    }

    public String[] getSupportedVolumeTypes() {
        return VOLUME_TYPES.clone();
    }

    // XXX copied from j2se
    public void libraryCreated(final LibraryImplementation libraryImpl) {
        assert libraryImpl != null;
        ProjectManager.mutex().postWriteRequest(
                new Runnable() {
                    public void run () {
                        try {
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            boolean save = addLibraryIntoBuild(libraryImpl, props);
                            if (save) {
                                PropertyUtils.putGlobalProperties(props);
                            }
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                        }
                    }
                }
        );
    }

    // XXX copied from j2se
    public void libraryDeleted(final LibraryImplementation libraryImpl) {
        assert libraryImpl != null;
        ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                    try {
                        EditableProperties props = PropertyUtils.getGlobalProperties();
                        for (int i = 0; i < VOLUME_TYPES.length; i++) {
                            String property = LIB_PREFIX + libraryImpl.getName() + '.' + VOLUME_TYPES[i];  //NOI18N
                            props.remove(property);
                        }
                        PropertyUtils.putGlobalProperties(props);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    }
                }
            });
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    // XXX copied from j2se
    private static boolean addLibraryIntoBuild(LibraryImplementation impl, EditableProperties props) {
        boolean modified = false;
        for (int i = 0; i < VOLUME_TYPES.length; i++) {
            String propName = LIB_PREFIX + impl.getName() + '.' + VOLUME_TYPES[i];     //NOI18N
            List roots = impl.getContent(VOLUME_TYPES[i]);
            if (roots == null) {
                //Non valid library, but try to recover
                continue;
            }
            StringBuffer propValue = new StringBuffer();
            boolean first = true;
            for (Iterator rootsIt = roots.iterator(); rootsIt.hasNext();) {
                URL url = (URL) rootsIt.next();
                if ("jar".equals(url.getProtocol())) {
                    url = FileUtil.getArchiveFile(url);
                    // XXX check whether this is really the root
                }
                File f = null;
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    f = FileUtil.toFile(fo);
                } else if ("file".equals(url.getProtocol())) {    //NOI18N
                    //If the file does not exist (eg library from cleaned project)
                    // and it is a file protocol URL, add it.
                    URI uri = URI.create(url.toExternalForm());
                    if (uri != null) {
                        f = new File(uri);
                    }
                }
                if (f != null) {
                    if (!first) {
                        propValue.append(File.pathSeparatorChar);
                    }
                    first = false;
                    f = FileUtil.normalizeFile(f);
                    propValue.append(f.getAbsolutePath());
                } else {
                    ErrorManager.getDefault().log("ServerLibraryTypeProvider: Can not resolve URL: " + url); //NOI18N
                }
            }
            String oldValue = props.getProperty(propName);
            String newValue = propValue.toString();
            if (!newValue.equals(oldValue)) {
                    props.setProperty(propName, newValue);
                    modified = true;
            }
        }
        return modified;
    }
}
