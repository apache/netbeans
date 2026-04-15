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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.LibraryInfo;
import org.netbeans.modules.web.jsfapi.api.LibraryType;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * Access to facelet library descriptors in bundled web.jsf20 library's javax.faces.jar
 * Also provides some useful methods for getting default library's displayname or
 * default prefix.
 *
 * @author marekfukala
 */
public class DefaultFaceletLibraries {

    private static DefaultFaceletLibraries INSTANCE;

    private static String getJarPath(JsfVersion version) {
        if (version == null) {
            version = JsfVersion.JSF_2_3;
        }
        return switch (version) {
            case JSF_4_1 -> "modules/ext/jsf-4_1/jakarta.faces.jar";
            case JSF_4_0 -> "modules/ext/jsf-4_0/jakarta.faces.jar";
            case JSF_3_0 -> "modules/ext/jsf-3_0/jakarta.faces.jar";
            default -> "modules/ext/jsf-2_3/javax.faces.jar";
        };
    }

    private File jsfImplJar;
    private Collection<FileObject> libraryDescriptorsFiles;
    private Map<String, FaceletsLibraryDescriptor> librariesDescriptors;
    private static Map<String, Library> jsf22FaceletPseudoLibraries;

    public static synchronized DefaultFaceletLibraries getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultFaceletLibraries(JsfVersion.JSF_2_3);
        }
        return INSTANCE;
    }

    public DefaultFaceletLibraries(File jsfImplJar) {
        this.jsfImplJar = jsfImplJar;

        init(jsfImplJar);
    }

    public DefaultFaceletLibraries(JsfVersion version) {
        this(InstalledFileLocator.getDefault().locate(
                getJarPath(version),
                "org.netbeans.modules.web.jsf20", false) //NOI18N
        );
    }

    private void init(File jsfImplJar) {
        assert jsfImplJar != null;

        FileObject jsfImplJarFo = FileUtil.getArchiveRoot(FileUtil.toFileObject(jsfImplJar));
        libraryDescriptorsFiles = findLibraryDescriptors(jsfImplJarFo, ".taglib.xml"); //NOI18N

    }

    public File getJsfImplJar() {
        return jsfImplJar;
    }

    public Collection<FileObject> getLibrariesDescriptorsFiles() {
        return this.libraryDescriptorsFiles;
    }

    public synchronized Map<String, FaceletsLibraryDescriptor> getLibrariesDescriptors() {
        if(librariesDescriptors == null) {
            librariesDescriptors = new HashMap<>();
            parseLibraries();
        }
        return librariesDescriptors;
    }

    private void parseLibraries() {
        for(FileObject lfo : getLibrariesDescriptorsFiles()) {
            FaceletsLibraryDescriptor descritor;
            try {
                descritor = FaceletsLibraryDescriptor.create(lfo);
                librariesDescriptors.put(descritor.getNamespace(), descritor);
            } catch (LibraryDescriptorException ex) {
                Logger.getGlobal().log(Level.WARNING, "Error parsing facelets library " +
                        FileUtil.getFileDisplayName(lfo) + " from file " + jsfImplJar, ex);
            }
        }

    }

    public static String getLibraryDisplayName(String uri) {
        LibraryInfo li = DefaultLibraryInfo.forNamespace(uri);
        return li != null ? li.getDisplayName() : null;
    }

    public static String getLibraryDefaultPrefix(String uri) {
        LibraryInfo li = DefaultLibraryInfo.forNamespace(uri);
        return li != null ? li.getDefaultPrefix() : null;
    }

     private static Collection<FileObject> findLibraryDescriptors(FileObject classpathRoot, String suffix) {
        Collection<FileObject> files = new ArrayList<>();
        Enumeration<? extends FileObject> fos = classpathRoot.getChildren(true); //scan all files in the jar
        while (fos.hasMoreElements()) {
            FileObject file = fos.nextElement();
            if(!file.isValid() || !file.isData()) {
                continue;
}
            if (file.getNameExt().toLowerCase(Locale.US).endsWith(suffix)) { //NOI18N
                //found library, create a new instance and cache it
                files.add(file);
            }
        }
        return files;
    }

    protected static synchronized Map<String, Library> getJsf22FaceletPseudoLibraries(FaceletsLibrarySupport support) {
        if (jsf22FaceletPseudoLibraries == null) {
            Map<String, Library> map = new HashMap<>(2);
            DefaultLibraryInfo.JSF.getValidNamespaces().stream()
                    .filter(namespace -> namespace.startsWith(NamespaceUtils.JCP_ORG_LOCATION))
                    .forEach(namespace -> map.put(namespace, new JsfFaceletPseudoLibrary(support, DefaultLibraryInfo.JSF)));
            DefaultLibraryInfo.PASSTHROUGH.getValidNamespaces().stream()
                    .filter(namespace -> namespace.startsWith(NamespaceUtils.JCP_ORG_LOCATION))
                    .forEach(namespace -> map.put(namespace, new JsfFaceletPseudoLibrary(support, DefaultLibraryInfo.PASSTHROUGH)));

            jsf22FaceletPseudoLibraries = Collections.unmodifiableMap(map);
        }
        return jsf22FaceletPseudoLibraries;
    }

    private static class JsfFaceletPseudoLibrary implements Library {

        private final Set<String> validNamespaces;
        private final String prefix;
        private final String displayName;

        public JsfFaceletPseudoLibrary(FaceletsLibrarySupport support, DefaultLibraryInfo defaultLibraryInfo) {
            this.validNamespaces = defaultLibraryInfo.getValidNamespaces();
            this.prefix = defaultLibraryInfo.getDefaultPrefix();
            this.displayName = defaultLibraryInfo.getDisplayName();
        }

        @Override
        public String getDefaultPrefix() {
            return prefix;
        }

        @Override
        public String getDefaultNamespace() {
            return null;
        }

        @Override
        public LibraryType getType() {
            return LibraryType.CLASS;
        }

        @Override
        public String getNamespace() {
            return validNamespaces.iterator().next();
        }

        @Override
        public Collection<? extends LibraryComponent> getComponents() {
            return Collections.emptyList();
        }

        @Override
        public LibraryComponent getComponent(String componentName) {
            return null;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Set<String> getValidNamespaces() {
            return validNamespaces;
        }
    }
}
