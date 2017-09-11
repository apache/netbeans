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
                        return new ClassPath[] {ClassPathSupport.createClassPath(resources.toArray(new URL[resources.size()]))};
                    } else if (ClassPath.COMPILE.equals(type)) {
                        resources = lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                        return new ClassPath[] {ClassPathSupport.createClassPath(resources.toArray(new URL[resources.size()]))};
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
