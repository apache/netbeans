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

package org.netbeans.modules.refactoring.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * It is a temporary solution to improve the performance. Later, it should be
 * moved to {@link SourceUtils} as API.
 *
 * @author Jan Pokorsky
 */
public final class SourceUtilsEx {

    /**
     * Returns a source file in which the passed element
     * is declared in. This tuned up version of {@code SourceUtils.getFile}
     * is necessary as sequential invocations of {@code SourceUtils.getFile} are
     * excessively slow.
     *
     * @param element an element to find {@link FileObject} for
     * @param cpInfo scope where the file will be searched
     * @param cache a cache
     * @return the source file
     * @see SourceUtils#getFile(org.netbeans.api.java.source.ElementHandle, org.netbeans.api.java.source.ClasspathInfo) SourceUtils.getFile
     */
    public static FileObject getFile(final Element element, final ClasspathInfo cpInfo, final Cache cache) {
        Parameters.notNull("element", element); //NOI18N
        Parameters.notNull("cpInfo", cpInfo);   //NOI18N
        Parameters.notNull("cache", cache);   //NOI18N

        Element current = element;
        Element prev = current.getKind() == ElementKind.PACKAGE ? current : null;
        while (current.getKind() != ElementKind.PACKAGE) {
            prev = current;
            current = current.getEnclosingElement();
        }
        if (prev == null) {
            return null;
        }

        final ElementKind kind = prev.getKind();
        String fqn;
        if (kind.isClass() || kind.isInterface()) {
            fqn = ((TypeElement) prev).getQualifiedName().toString();
        } else if (kind == ElementKind.PACKAGE) {
            fqn = ((PackageElement) prev).getQualifiedName().toString();
        } else {
            return null;
        }

        Object cached = cache.cacheOfSrcFiles.get(fqn);
        if (cached == null) {
            final ElementHandle<? extends Element> handle = ElementHandle.create(prev);
            cached = SourceUtils.getFile(handle, cpInfo);
            cache.cacheOfSrcFiles.put(fqn, cached != null ? cached : Cache.NULL);
        } else if (cached == Cache.NULL) {
            cached = null;
        }
        return (FileObject) cached;
    }

    /**
     * Cached environment that helps to speed up
     * {@link Element} to {@link FileObject} translations.
     */
    public static final class Cache {

        private static final Object NULL = new Object();
        /**
         * mapping of top level classes FQN to their source files
         */
        private final Map<String, Object> cacheOfSrcFiles = new HashMap<String, Object>();
    }

    /**
     * Returns a collection of source files in which the passed handles
     * are declared. This tuned up version of {@code SourceUtils.getFile}
     * is necessary as sequential calls to {@code SourceUtils.getFile} are
     * excessively slow.
     *
     * @param handles to find {@link FileObject}s for
     * @param cpInfo classpaths for resolving handle
     * @return collection of {@link FileObject}s
     * @see SourceUtils#getFile(org.netbeans.api.java.source.ElementHandle, org.netbeans.api.java.source.ClasspathInfo) SourceUtils.getFile
     */
    public static Collection<FileObject> getFiles(final Collection<ElementHandle<? extends Element>> handles, final ClasspathInfo cpInfo, AtomicBoolean cancel) {
        Parameters.notNull("handle", handles);
        Parameters.notNull("cpInfo", cpInfo);

        List<FileObject> result = new ArrayList<FileObject>(handles.size());
        Map<String, List<ResolvedElementHandle>> handlesPerPackages = new HashMap<String, List<ResolvedElementHandle>>();

        try {
            ClassPath cp = ClassPathSupport.createProxyClassPath(
                    new ClassPath[] {
                        cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),
                        createClassPath(cpInfo,ClasspathInfo.PathKind.BOOT),
                        createClassPath(cpInfo,ClasspathInfo.PathKind.COMPILE),
                    });

            for (ElementHandle<? extends Element> handle : handles) {
                if(cancel != null && cancel.get()) {
                    return Collections.<FileObject>emptySet();
                }
                ResolvedElementHandle resolvedEHandle = ResolvedElementHandle.create(handle);
                List<ResolvedElementHandle> l = handlesPerPackages.get(resolvedEHandle.pkgName);
                if (l == null) {
                    l = new ArrayList<ResolvedElementHandle>();
                    handlesPerPackages.put(resolvedEHandle.pkgName, l);
                }
                l.add(resolvedEHandle);
            }

            for (Map.Entry<String, List<ResolvedElementHandle>> entry : handlesPerPackages.entrySet()) {
                searchFiles(entry.getKey(), entry.getValue(), cp, result);
            }

        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return result;
    }

    private static final class ResolvedElementHandle {
        String pkgName;
        String className;
        String[] signature;
        boolean pkg;
        private String sourceFileName;

        static ResolvedElementHandle create(ElementHandle<? extends Element> handle) {
            ResolvedElementHandle reh = new ResolvedElementHandle();
            reh.pkg = handle.getKind() == ElementKind.PACKAGE;
            reh.signature = getElementHandleSignature(handle);
            assert reh.signature.length >= 1;
            if (reh.pkg) {
                reh.pkgName = convertPackage2Folder(reh.signature[0]);
            } else {
                int index = reh.signature[0].lastIndexOf('.');                          //NOI18N
                if (index < 0) {
                    reh.pkgName = "";                                             //NOI18N
                    reh.className = reh.signature[0];
                } else {
                    reh.pkgName = convertPackage2Folder(reh.signature[0].substring(0, index));
                    reh.className = reh.signature[0].substring(index + 1);
                }
            }
            return reh;
        }

        public String getSourceFileName() {
            if (sourceFileName == null) {
                sourceFileName = SourceUtilsEx.getSourceFileName(className);
            }
            return sourceFileName;
        }


    }

    private static void searchFiles(String pkgName, List<ResolvedElementHandle> handles, ClassPath cp, List<FileObject> result) throws IOException {

        List<FileObject> fos = cp.findAllResources(pkgName);
        for (FileObject fo : fos) {
            FileObject root = cp.findOwnerRoot(fo);
            assert root != null;
            FileObject[] sourceRoots = SourceForBinaryQuery.findSourceRoots(root.toURL()).getRoots();
            ClassPath sourcePath = ClassPathSupport.createClassPath(sourceRoots);
            LinkedList<FileObject> folders = new LinkedList<FileObject>(sourcePath.findAllResources(pkgName));

            for (Iterator<ResolvedElementHandle> it = handles.iterator(); it.hasNext();) {
                ResolvedElementHandle handle = it.next();
                if (handle.pkg) {
                    result.add(folders.isEmpty() ? fo : folders.get(0));
                    it.remove();
                    break;
                }
            }

            if (handles.isEmpty()) {
                return;
            }

            boolean caseSensitive = isCaseSensitive();
            folders.add(fo);
            for (FileObject folder : folders) {
                FileObject[] children = folder.getChildren();
                for (FileObject child : children) {
                    searchChildren(child, handles, result, caseSensitive);
                    if (handles.isEmpty()) {
                        return;
                    }
                }
            }


            for (Iterator<ResolvedElementHandle> it = handles.iterator(); it.hasNext();) {
                ResolvedElementHandle handle = it.next();
                FileObject foundFo;
                if (sourceRoots.length == 0) {
                    foundFo = findSource(handle.signature[0], root);
                } else {
                    foundFo = findSource(handle.signature[0], sourceRoots);
                }
                if (foundFo != null) {
                    it.remove();
                    result.add(foundFo);
                }
            }
        }
    }

    private static void searchChildren(FileObject child, List<ResolvedElementHandle> handles, List<FileObject> result, boolean caseSensitive) {
        for (Iterator<ResolvedElementHandle> it = handles.iterator(); it.hasNext();) {
            ResolvedElementHandle handle = it.next();
            if (((caseSensitive && child.getName().equals(handle.getSourceFileName())) ||
                    (!caseSensitive && child.getName().equalsIgnoreCase(handle.getSourceFileName()))) &&
                    (child.isData() && ("java".equalsIgnoreCase(child.getExt()) || "class".equalsIgnoreCase(child.getExt())))) {
                it.remove();
                result.add(child);
            }

        }
    }

    /** copied
     * @see SourceUtils#isCaseSensitive
     */
    private static boolean isCaseSensitive () {
        return ! new File ("a").equals (new File ("A"));    //NOI18N
    }

    /** copied
     * @see SourceUtils#getSourceFileName(java.lang.String)
     */
    private static String getSourceFileName(String classFileName) {
        int index = classFileName.indexOf('$');
        return index == -1 ? classFileName : classFileName.substring(0, index);
    }

    /** helper method
     * @see ElementHandle#getSignature
     */
    private static String[] getElementHandleSignature(ElementHandle handle) {
        try {
            Method method = ElementHandle.class.getDeclaredMethod("getSignature"); //NOI18N
            method.setAccessible(true);
            return (String[]) method.invoke(handle);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** helper method
     * @see SourceUtils#createClassPath(org.netbeans.api.java.source.ClasspathInfo, org.netbeans.api.java.source.ClasspathInfo.PathKind)
     */
    private static ClassPath createClassPath(ClasspathInfo cpInfo, PathKind kind) {
        try {
            Method method = SourceUtils.class.getDeclaredMethod("createClassPath", ClasspathInfo.class, PathKind.class); //NOI18N
            method.setAccessible(true);
            return (ClassPath) method.invoke(null, cpInfo, kind);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** helper method
     * @see SourceUtils#findSource(java.lang.String, org.openide.filesystems.FileObject[])
     */
    private static FileObject findSource(final String binaryName, final FileObject... fos) throws IOException {
        try {
            Method method = SourceUtils.class.getDeclaredMethod("findSource", String.class, FileObject[].class); //NOI18N
            method.setAccessible(true);
            return (FileObject) method.invoke(null, binaryName, fos);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            }
            throw new IllegalStateException(ex);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** copied
     * @see org.netbeans.modules.java.source.parsing.FileObjects#convertPackage2Folder(java.lang.String)
     */
    private static String convertPackage2Folder( String packageName ) {
        return packageName.replace( '.', '/' );
    }

}
