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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.classpath.ClassPathProvider.class, position=150)
public class J2SELibraryClassPathProvider implements ClassPathProvider {
    
    private static final Set<? extends String> SUPPORTED_CLASS_PATH_TYPES =
            new HashSet<String>(Arrays.asList(new String[]{
                ClassPath.SOURCE,
                ClassPath.BOOT,
                ClassPath.COMPILE,
                JavaClassPathConstants.MODULE_BOOT_PATH
            }));

    public ClassPath findClassPath(
            @NonNull final FileObject file,
            @NonNull final String type) {
        assert file != null;
        if (!SUPPORTED_CLASS_PATH_TYPES.contains(type)) {
            return null;
        }
        Library ll = this.getLastUsedLibrary(file);
        if (ll != null) {
            ClassPath[] cp = findClassPathOrNull(file, type, ll);
            return cp != null ? cp[0] : null;
        }
        else {
            for (LibraryManager mgr : LibraryManager.getOpenManagers()) {
                for (Library lib : mgr.getLibraries()) {
                    ClassPath[] cp = findClassPathOrNull(file, type, lib);
                    if (cp != null) {
                        return cp[0];
                    }
                }
            }
            return null;
        }
    }

    
    private ClassPath[] findClassPathOrNull(
            @NonNull final FileObject file,
            @NonNull final String type,
            @NonNull final Library lib) {
        if (lib.getType().equals(J2SELibraryTypeProvider.LIBRARY_TYPE)) {
            List<URL> resources = lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_SRC);
            try {                
                FileObject root = getOwnerRoot(file,resources);
                if (root != null) {
                    final JavaPlatform defPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
                    setLastUsedLibrary(root, lib);
                    if (ClassPath.SOURCE.equals(type)) {
                        return new ClassPath[] {ClassPathSupport.createClassPath(resources.toArray(new URL[0]))};
                    } else if (ClassPath.COMPILE.equals(type)) {
                        resources = lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                        return new ClassPath[] {ClassPathSupport.createClassPath(resources.toArray(new URL[0]))};
                    } else if (ClassPath.BOOT.equals(type)) {
                        return new ClassPath[] {defPlatform.getBootstrapLibraries()};
                    } else if (JavaClassPathConstants.MODULE_BOOT_PATH.equals(type) &&
                            Util.JDK9.compareTo(defPlatform.getSpecification().getVersion()) <= 0) {
                        return new ClassPath[] {defPlatform.getBootstrapLibraries()};
                    } else {
                        return new ClassPath[] {null};
                    }
                }
            } catch (final IllegalArgumentException e) {
                final IllegalArgumentException ne = new IllegalArgumentException("LibraryImplementation:["+getImplClassName(lib)+"] returned wrong root:" + e.getMessage());
                Exceptions.printStackTrace(ne.initCause(e));
            }
        }
        return null;
    }

    private FileObject getOwnerRoot (
            @NonNull final FileObject fo,
            @NonNull final List<? extends URL> roots) {
        final URL foURL = toURL(fo);
        if (foURL == null) {
            //template or other nbfs
            return null;
        }
        final URL archiveFileURL = FileUtil.getArchiveFile(foURL);
        final boolean isInArchive = archiveFileURL != null;
        final FileObject ownerFo = isInArchive ? URLMapper.findFileObject(archiveFileURL) : null;
        final Set<FileObject> candidates = new HashSet<FileObject>();
        for (URL root : roots) {
            if ("jar".equals(root.getProtocol())) {  //NOI18N
                if (isInArchive && ownerFo != null) {
                    final URL rootFileUrl = FileUtil.getArchiveFile(root);
                    if (rootFileUrl != null) {  //May be null in case of broken url
                        final FileObject rootFileFo = URLMapper.findFileObject(rootFileUrl);
                        if (ownerFo.equals(rootFileFo)) {
                            final FileObject rootFo = URLMapper.findFileObject(root);
                            if (rootFo != null) {
                                candidates.add(rootFo);
                                //possibly break, but in general single archive may have more roots
                            }
                        }
                    }
                }
            }
            else {
                if (!isInArchive) {
                    final FileObject rootFo = URLMapper.findFileObject(root);
                    if (rootFo != null) {
                        candidates.add(rootFo);
                    }
                }
            }
        }
        return candidates.isEmpty() ? null : findOwnerRoot(fo,candidates);
    }

    private static FileObject findOwnerRoot (final FileObject resource, final Set<? extends FileObject> roots) {
        for (FileObject f = resource; f != null; f = f.getParent()) {
            if (roots.contains(f)) {
                return f;
            }
        }
        return null;
    }
    
    private static String getImplClassName (final Library lib) {
        String result = ""; //NOI18N
        try {
            final Class cls = lib.getClass();
            final Field fld = cls.getDeclaredField("impl"); //NOI18N
            if (fld != null) {                            
                fld.setAccessible(true);
                Object res = fld.get(lib);                            
                if (res != null) {
                    result = res.getClass().getName();
                }
            }
        } catch (NoSuchFieldException noSuchFieldException) {
            //Not needed
        } catch (SecurityException securityException) {
            //Not needed
        } catch (IllegalArgumentException illegalArgumentException) {
            //Not needed
        } catch (IllegalAccessException illegalAccessException) {
            //Not needed
        }
        return result;
    }
    
            
    private synchronized Library getLastUsedLibrary (FileObject fo) {
        if (this.lastUsedRoot != null && FileUtil.isParentOf(this.lastUsedRoot,fo)) {
            return this.lastUsedLibrary;
        }
        else {
            return null;
        }
    }

    private synchronized void setLastUsedLibrary (FileObject root, Library lib) {
        this.lastUsedRoot = root;
        this.lastUsedLibrary = lib;
    }
    
    private synchronized URL toURL(@NonNull final FileObject file) {
        final FileObject luw = lastUsedFile == null ? null : lastUsedFile.get();
        if (luw != file) {
            lastUsedFileURL = URLMapper.findURL(file, URLMapper.EXTERNAL);
            lastUsedFile = new WeakReference<FileObject>(file);
        }
        return lastUsedFileURL;
    }

    //@GuardedBy("this")
    private FileObject lastUsedRoot;
    //@GuardedBy("this")
    private Library lastUsedLibrary;
    //@GuardedBy("this")
    private Reference<FileObject> lastUsedFile;
    //@GuardedBy("this")
    private URL lastUsedFileURL;
    
    
}
