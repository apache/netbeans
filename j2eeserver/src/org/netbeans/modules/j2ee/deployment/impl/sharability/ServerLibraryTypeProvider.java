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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
